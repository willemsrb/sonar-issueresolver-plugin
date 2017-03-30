package nl.futureedge.sonar.plugin.issueresolver.helper;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.sonar.api.server.ws.LocalConnector;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarqube.ws.Issues.Comment;
import org.sonarqube.ws.Issues.Issue;
import org.sonarqube.ws.Issues.SearchWsResponse;
import org.sonarqube.ws.client.PostRequest;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.WsConnector;
import org.sonarqube.ws.client.WsRequest;
import org.sonarqube.ws.client.WsResponse;
import org.sonarqube.ws.client.issue.SearchWsRequest;

import nl.futureedge.sonar.plugin.issueresolver.issues.IssueData;
import nl.futureedge.sonar.plugin.issueresolver.issues.IssueKey;
import nl.futureedge.sonar.plugin.issueresolver.ws.ImportResult;

/**
 * Issue functionality.
 */
public final class IssueHelper {
	
	private static final Logger LOGGER = Loggers.get(IssueHelper.class);
	
	private static final String PATH_TRANSITION = "api/issues/do_transition";
	private static final String PATH_ASSIGN = "api/issues/assign";
	private static final String PATH_ADD_COMMENT = "api/issues/add_comment";
	
	private static final String PARAM_ISSUE = "issue";
	private static final String PARAM_TRANSITION = "transition";
	private static final String PARAM_ASSIGNEE = "assignee";
	private static final String PARAM_TEXT = "text";
	
	
	private IssueHelper() {
	}

	/**
	 * Loop over issues.
	 * 
	 * @param localConnector
	 *            local connector
	 * @param searchIssuesRequest
	 *            search request
	 * @param consumer
	 *            callback called for each issues
	 */
	public static void forEachIssue(final LocalConnector localConnector, final SearchWsRequest searchIssuesRequest,
			final BiConsumer<SearchWsResponse, Issue> consumer) {
		// Loop through all issues of the project
		final WsClient wsClient = WsClientFactories.getLocal().newClient(localConnector);

		boolean doNextPage = true;
		while (doNextPage) {
			LOGGER.debug("Listing issues for project {}; page {}", searchIssuesRequest.getProjectKeys(),
					searchIssuesRequest.getPage());
			final SearchWsResponse searchIssuesResponse = wsClient.issues().search(searchIssuesRequest);
			for (final Issue issue : searchIssuesResponse.getIssuesList()) {
				consumer.accept(searchIssuesResponse, issue);
			}

			doNextPage = searchIssuesResponse.getPaging().getTotal() > (searchIssuesResponse.getPaging().getPageIndex()
					* searchIssuesResponse.getPaging().getPageSize());
			searchIssuesRequest.setPage(searchIssuesResponse.getPaging().getPageIndex() + 1);
			searchIssuesRequest.setPageSize(searchIssuesResponse.getPaging().getPageSize());
		}
	}

	/**
	 * Resolve issues.
	 * 
	 * @param localConnector
	 *            local connector
	 * @param importResult
	 *            result
	 * @param preview
	 *            true if issues should not be actually resolved.
	 * @param projectKey
	 *            project key
	 * @param issues
	 *            issues
	 */
	public static void resolveIssues(final LocalConnector localConnector, final ImportResult importResult,
			final boolean preview, final String projectKey, final Map<IssueKey, IssueData> issues) {
		// Read issues from project, match and resolve
		importResult.setPreview(preview);

		final WsClient wsClient = WsClientFactories.getLocal().newClient(localConnector);

		// Loop through all issues of the project
		final SearchWsRequest searchIssuesRequest = SearchHelper.findIssuesForImport(projectKey);

		forEachIssue(localConnector, searchIssuesRequest, (searchIssuesResponse, issue) -> {
			final IssueKey key = IssueKey.fromIssue(issue, searchIssuesResponse.getComponentsList());
			LOGGER.debug("Try to match issue: {}", key);
			// Match with issue from data
			final IssueData data = issues.remove(key);

			if (data != null) {
				importResult.registerMatchedIssue();
				
				// Handle issue, if data is found
				handleTransition(wsClient.wsConnector(), issue, data.getStatus(), data.getResolution(), preview,
						importResult);
				handleAssignee(wsClient.wsConnector(), issue, data.getAssignee(), preview, importResult);
				handleComments(wsClient.wsConnector(), issue, data.getComments(), preview, importResult);
			}
		});
	}

	/* ************** ********** ************** */
	/* ************** TRANSITION ************** */
	/* ************** ********** ************** */

	private static void handleTransition(final WsConnector wsConnector, final Issue issue, final String status,
			final String resolution, final boolean preview, final ImportResult importResult) {
		final String transition = determineTransition(issue.getKey(), issue.getStatus(), issue.getResolution(), status,
				resolution, importResult);
		if (transition != null) {
			if (!preview) {
				transitionIssue(wsConnector, issue.getKey(), transition, importResult);
			}
			importResult.registerTransitionedIssue();
		}
	}

	private static String determineTransition(final String issue, final String currentStatus,
			final String currentResolution, final String wantedStatus, final String wantedResolution,
			final ImportResult importResult) {
		final String transition;
		if (TransitionHelper.noAction(currentStatus, currentResolution, wantedStatus, wantedResolution)) {
			transition = null;
		} else if (TransitionHelper.shouldConfirm(currentStatus, wantedStatus)) {
			transition = "confirm";
		} else if (TransitionHelper.shouldUnconfirm(currentStatus, wantedStatus)) {
			transition = "unconfirm";
		} else if (TransitionHelper.shouldReopen(currentStatus, wantedStatus)) {
			transition = "reopen";
		} else if (TransitionHelper.shouldResolveFixed(currentStatus, wantedStatus, wantedResolution)) {
			transition = "resolve";
		} else if (TransitionHelper.shouldResolveFalsePositive(currentStatus, wantedStatus, wantedResolution)) {
			transition = "falsepositive";
		} else if (TransitionHelper.shouldReopen(currentStatus, wantedStatus, wantedResolution)) {
			transition = "wontfix";
		} else {
			importResult.registerMatchFailure("Could not determine transition for issue with key '" + issue
					+ "'; current status is '" + currentStatus + "' and resolution is '" + currentResolution
					+ "'; wanted status is '" + wantedStatus + "' and resolution is '" + wantedResolution + "'");
			transition = null;
		}
		return transition;
	}

	private static void transitionIssue(final WsConnector wsConnector, final String issue, final String transition,
			final 	ImportResult importResult) {
		final WsRequest request = new PostRequest(PATH_TRANSITION).setParam(PARAM_ISSUE, issue)
				.setParam(PARAM_TRANSITION, transition);
		final WsResponse response = wsConnector.call(request);

		if (!response.isSuccessful()) {
			LOGGER.debug("Failed to transition issue: " + response.content());
			importResult.registerTransitionFailure(
					"Could not transition issue with key '" + issue + "' using transition '" + transition + "'");
		}
	}

	/* ************** ******** ************** */
	/* ************** ASSIGNEE ************** */
	/* ************** ******** ************** */

	private static void handleAssignee(final WsConnector wsConnector, final Issue issue, final String assignee,
			final boolean preview, final ImportResult importResult) {
		if (assignee != null && !"".equals(assignee) && !assignee.equals(issue.getAssignee())) {
			if (!preview) {
				assignIssue(wsConnector, issue.getKey(), assignee, importResult);
			}
			importResult.registerAssignedIssue();
		}
	}

	private static void assignIssue(final WsConnector wsConnector, final String issue, final String assignee,
			final ImportResult importResult) {
		final WsRequest request = new PostRequest(PATH_ASSIGN).setParam(PARAM_ISSUE, issue).setParam(PARAM_ASSIGNEE,
				assignee);
		final WsResponse response = wsConnector.call(request);

		if (!response.isSuccessful()) {
			LOGGER.debug("Failed to assign issue: " + response.content());
			importResult.registerTransitionFailure(
					"Could not assign issue with key '" + issue + "' to user '" + assignee + "'");
		}
	}

	/* ************** ******* ************** */
	/* ************** COMMENT ************** */
	/* ************** ******* ************** */

	private static void handleComments(final WsConnector wsConnector, final Issue issue, final List<String> comments,
			final boolean preview, final ImportResult importResult) {
		boolean commentAdded = false;
		for (final String comment : comments) {
			if (!alreadyContainsComment(issue.getComments().getCommentsList(), comment)) {
				commentAdded = true;
				if (!preview) {
					addComment(wsConnector, issue.getKey(), comment, importResult);
				}
			}
		}

		if (commentAdded) {
			importResult.registerCommentedIssue();
		}
	}

	private static boolean alreadyContainsComment(final List<Comment> currentComments, final String comment) {
		for (Comment currentComment : currentComments) {
			if (currentComment.getMarkdown().equals(comment)) {
				return true;
			}
		}
		return false;
	}

	private static void addComment(final WsConnector wsConnector, final String issue, final String text,
			final ImportResult importResult) {
		final WsRequest request = new PostRequest(PATH_ADD_COMMENT).setParam(PARAM_ISSUE, issue).setParam(PARAM_TEXT,
				text);
		final WsResponse response = wsConnector.call(request);

		if (!response.isSuccessful()) {
			LOGGER.debug("Failed to add comment to issue: " + response.content());
			importResult.registerTransitionFailure("Could not add comment to issue with key '" + issue + "'");
		}
	}

}

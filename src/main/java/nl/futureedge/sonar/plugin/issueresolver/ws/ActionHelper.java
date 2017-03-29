package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.util.Arrays;
import java.util.Collections;
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

/**
 * Helpers.
 */
public final class ActionHelper {

	private static final Logger LOGGER = Loggers.get(UpdateAction.class);

	private ActionHelper() {
	}

	/**
	 * Create search request for resolved issues.
	 * 
	 * @param projectKey
	 *            project key
	 * @return search request
	 */
	public static SearchWsRequest findResolvedIssuesFor(String projectKey) {
		final SearchWsRequest searchIssuesRequest = new SearchWsRequest();
		searchIssuesRequest.setProjectKeys(Collections.singletonList(projectKey));
		searchIssuesRequest.setAdditionalFields(Collections.singletonList("comments"));
		searchIssuesRequest.setStatuses(Collections.singletonList("RESOLVED"));
		searchIssuesRequest.setResolutions(Arrays.asList("FALSE-POSITIVE", "WONTFIX"));
		searchIssuesRequest.setPage(1);
		searchIssuesRequest.setPageSize(100);
		return searchIssuesRequest;
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
	public static void forEachIssue(LocalConnector localConnector, SearchWsRequest searchIssuesRequest,
			BiConsumer<SearchWsResponse, Issue> consumer) {
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
	public static void resolveIssues(LocalConnector localConnector, ImportResult importResult, boolean preview,
			String projectKey, Map<IssueKey, IssueData> issues) {
		// Read issues from project, match and resolve
		importResult.setPreview(preview);

		final WsClient wsClient = WsClientFactories.getLocal().newClient(localConnector);

		// Loop through all issues of the project
		final SearchWsRequest searchIssuesRequest = new SearchWsRequest();
		searchIssuesRequest.setAdditionalFields(Collections.singletonList("comments"));
		searchIssuesRequest.setProjectKeys(Collections.singletonList(projectKey));
		searchIssuesRequest.setPage(1);
		searchIssuesRequest.setPageSize(100);

		boolean doNextPage = true;
		while (doNextPage) {
			LOGGER.debug("Listing issues for project {}; page {}", projectKey, searchIssuesRequest.getPage());
			final SearchWsResponse searchIssuesResponse = wsClient.issues().search(searchIssuesRequest);
			for (final Issue issue : searchIssuesResponse.getIssuesList()) {
				final IssueKey key = IssueKey.fromIssue(issue, searchIssuesResponse.getComponentsList());
				LOGGER.debug("Try to match issue: {}", key);
				// Match with issue from data
				final IssueData data = issues.remove(key);

				if (data != null) {
					// Handle issue, if data is found
					handleIssue(wsClient.wsConnector(), issue, data, preview, importResult);
				}
			}

			doNextPage = searchIssuesResponse.getPaging().getTotal() > (searchIssuesResponse.getPaging().getPageIndex()
					* searchIssuesResponse.getPaging().getPageSize());
			searchIssuesRequest.setPage(searchIssuesResponse.getPaging().getPageIndex() + 1);
			searchIssuesRequest.setPageSize(searchIssuesResponse.getPaging().getPageSize());
		}

		importResult.registerUnmatchedIssues(issues.size());
	}

	private static void handleIssue(final WsConnector wsConnector, final Issue issue, final IssueData data,
			final boolean preview, final ImportResult importResult) {
		if ("OPEN".equals(issue.getStatus()) || "CONFIRMED".equals(issue.getStatus())
				|| "REOPENED".equals(issue.getStatus())) {
			LOGGER.info("Resolving matched issue key " + issue.getKey());
			if (!preview) {
				resolveIssue(wsConnector, issue.getKey(), data.getResolution());
				handleComments(wsConnector, issue, data.getComments());
			}
			importResult.registerResolvedIssue();

		} else {
			LOGGER.info("Could not resolve matched issue key " + issue.getKey() + "; status was " + issue.getStatus());
			importResult.registerUnresolvedIssue();
		}
	}

	private static void resolveIssue(final WsConnector wsConnector, final String issue, final String transition) {
		final WsRequest request = new PostRequest("api/issues/do_transition").setParam("issue", issue)
				.setParam("transition", transition);
		final WsResponse response = wsConnector.call(request);

		if (!response.isSuccessful()) {
			LOGGER.debug("Failed to resolve issue: " + response.content());
			throw new IllegalStateException("Could not resolve issue '" + issue + "'");
		}
	}

	private static void handleComments(final WsConnector wsConnector, final Issue issue, final List<String> comments) {
		for (final String comment : comments) {
			if (!alreadyContainsComment(issue.getComments().getCommentsList(), comment)) {
				addComment(wsConnector, issue.getKey(), comment);
			}
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

	private static void addComment(final WsConnector wsConnector, final String issue, final String text) {
		final WsRequest request = new PostRequest("api/issues/add_comment").setParam("issue", issue).setParam("text",
				text);
		final WsResponse response = wsConnector.call(request);

		if (!response.isSuccessful()) {
			throw new IllegalStateException("Could not add comment to issue '" + issue + "'");
		}
	}

}

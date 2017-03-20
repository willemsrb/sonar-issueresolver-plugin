package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Request.Part;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService.NewAction;
import org.sonar.api.server.ws.WebService.NewController;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.utils.text.JsonWriter;
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
import nl.futureedge.sonar.plugin.issueresolver.json.JsonReader;

/**
 * Import action.
 */
public final class ImportAction implements IssueResolverWsAction {

	public static final String ACTION = "import";
	public static final String PARAM_PROJECT_KEY = "projectKey";
	public static final String PARAM_PREVIEW = "preview";
	public static final String PARAM_DATA = "data";

	private static final Logger LOGGER = Loggers.get(ImportAction.class);

	@Override
	public void define(final NewController controller) {
		LOGGER.debug("Defining import action ...");
		final NewAction action = controller.createAction(ACTION)
				.setDescription("Import issues that have exported with the export function.").setHandler(this)
				.setPost(true);
		action.createParam(PARAM_PROJECT_KEY).setDescription("Project to import issues to").setRequired(true);
		action.createParam(PARAM_PREVIEW).setDescription("If import should be a preview").setDefaultValue("false");
		action.createParam(PARAM_DATA).setDescription("Exported resolved issue data").setRequired(true);
		LOGGER.debug("Import action defined");
	}

	@Override
	public void handle(final Request request, final Response response) {
		LOGGER.info("Handle issueresolver import request ...");
		final ImportResult importResult = new ImportResult();

		// Read issue data from request
		final Map<IssueKey, IssueData> issues = readIssues(request, importResult);
		LOGGER.info("Read " + importResult.getIssues() + " issues (having " + importResult.getDuplicateKeys()
				+ " duplicate keys)");

		// Read issues from project, match and resolve
		final boolean preview = request.mandatoryParamAsBoolean(PARAM_PREVIEW);
		importResult.setPreview(preview);

		final String projectKey = request.mandatoryParam(PARAM_PROJECT_KEY);
		final WsClient wsClient = WsClientFactories.getLocal().newClient(request.localConnector());

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
				final IssueKey key = IssueKey.fromIssue(issue);
				LOGGER.debug("Try to match issue: " + key);
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
		LOGGER.info("Unmatched issues: " + importResult.getUnmatchedIssues());
		LOGGER.info("Unresolved issues: " + importResult.getUnresolvedIssues());
		LOGGER.info("Resolved issues: " + importResult.getResolvedIssues());

		// Sent result
		final JsonWriter responseWriter = response.newJsonWriter();
		importResult.write(responseWriter);
		responseWriter.close();
		LOGGER.debug("Issueresolver import request done");
	}

	/* ************** READ ************** */
	/* ************** READ ************** */
	/* ************** READ ************** */

	private Map<IssueKey, IssueData> readIssues(final Request request, final ImportResult importResult) {
		final Part data = request.mandatoryParamAsPart(PARAM_DATA);
		final Map<IssueKey, IssueData> issues;

		try (final JsonReader reader = new JsonReader(data.getInputStream())) {
			reader.beginObject();

			// Version
			final int version = reader.propAsInt("version");
			switch (version) {
			case 1:
				issues = readIssuesVersionOne(reader, importResult);
				break;
			default:
				throw new IllegalStateException("Unknown version '" + version + "'");
			}
			reader.endObject();
		} catch (IOException e) {
			throw new IllegalStateException("Unexpected error during data parse", e);
		}
		return issues;
	}

	private Map<IssueKey, IssueData> readIssuesVersionOne(final JsonReader reader, final ImportResult importResult)
			throws IOException {
		final Map<IssueKey, IssueData> issues = new HashMap<>();

		reader.assertName("issues");
		reader.beginArray();
		while (reader.hasNext()) {
			reader.beginObject();
			final IssueKey key = IssueKey.read(reader);
			LOGGER.debug("Read issue: " + key);
			final IssueData data = IssueData.read(reader);
			importResult.registerIssue();

			if (issues.put(key, data) != null) {
				importResult.registerDuplicateKey();
			}
			reader.endObject();
		}
		reader.endArray();

		return issues;
	}

	/* ************** RESOLVE ************** */
	/* ************** RESOLVE ************** */
	/* ************** RESOLVE ************** */

	private void handleIssue(final WsConnector wsConnector, final Issue issue, final IssueData data,
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

	private void resolveIssue(final WsConnector wsConnector, final String issue, final String transition) {
		final WsRequest request = new PostRequest("api/issues/do_transition").setParam("issue", issue)
				.setParam("transition", transition);
		final WsResponse response = wsConnector.call(request);

		if (!response.isSuccessful()) {
			LOGGER.debug("Failed to resolve issue: " + response.content());
			throw new IllegalStateException("Could not resolve issue '" + issue + "'");
		}
	}

	private void handleComments(final WsConnector wsConnector, final Issue issue, final List<String> comments) {
		for (final String comment : comments) {
			if(!alreadyContainsComment(issue.getComments().getCommentsList(), comment)) {
				addComment(wsConnector, issue.getKey(), comment);
			}
		}
	}

	private boolean alreadyContainsComment(final List<Comment> currentComments, final String comment) {
		for(Comment currentComment : currentComments) {
			if(currentComment.getMarkdown().equals(comment)) {
				return true;
			}
		}
		return false;
	}

	private void addComment(final WsConnector wsConnector, final String issue, final String text) {
		final WsRequest request = new PostRequest("api/issues/add_comment").setParam("issue", issue).setParam("text",
				text);
		final WsResponse response = wsConnector.call(request);

		if (!response.isSuccessful()) {
			throw new IllegalStateException("Could not add comment to issue '" + issue + "'");
		}
	}

	/**
	 * Import result.
	 */
	private static final class ImportResult {

		private boolean preview = false;
		private int issues = 0;
		private int duplicateKeys = 0;
		private int unmatchedIssues = 0;
		private int unresolvedIssues = 0;
		private int resolvedIssues = 0;

		public void setPreview(final boolean preview) {
			this.preview = preview;
		}

		public void registerIssue() {
			issues++;
		}

		public int getIssues() {
			return issues;
		}

		public void registerDuplicateKey() {
			duplicateKeys++;
		}

		public int getDuplicateKeys() {
			return duplicateKeys;
		}

		public void registerUnmatchedIssues(final int unmatchedIssues) {
			this.unmatchedIssues = unmatchedIssues;
		}

		public int getUnmatchedIssues() {
			return unmatchedIssues;
		}

		public void registerUnresolvedIssue() {
			unresolvedIssues++;
		}

		public int getUnresolvedIssues() {
			return unresolvedIssues;
		}

		public void registerResolvedIssue() {
			resolvedIssues++;
		}

		public int getResolvedIssues() {
			return resolvedIssues;
		}

		public void write(final JsonWriter writer) {
			writer.beginObject();
			writer.prop("preview", preview);
			writer.prop("issues", issues);
			writer.prop("duplicateKeys", duplicateKeys);
			writer.prop("unmatchedIssues", unmatchedIssues);
			writer.prop("unresolvedIssues", unresolvedIssues);
			writer.prop("resolvedIssues", resolvedIssues);
			writer.endObject();
		}
	}
}

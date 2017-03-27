package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService.NewAction;
import org.sonar.api.server.ws.WebService.NewController;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.utils.text.JsonWriter;
import org.sonarqube.ws.Issues.Component;
import org.sonarqube.ws.Issues.Issue;
import org.sonarqube.ws.Issues.SearchWsResponse;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.issue.SearchWsRequest;

import nl.futureedge.sonar.plugin.issueresolver.issues.IssueData;
import nl.futureedge.sonar.plugin.issueresolver.issues.IssueKey;

/**
 * Export action.
 */
public class ExportAction implements IssueResolverWsAction {

	public static final String ACTION = "export";
	public static final String PARAM_PROJECT_KEY = "projectKey";

	private static final Logger LOGGER = Loggers.get(ExportAction.class);

	@Override
	public void define(final NewController controller) {
		LOGGER.debug("Defining export action ...");
		final NewAction action = controller.createAction(ACTION)
				.setDescription("Export resolved issues with the status false positive or won't fix.").setHandler(this)
				.setPost(false);

		action.createParam(PARAM_PROJECT_KEY).setDescription("Project to export issues for").setRequired(true);
		LOGGER.debug("Export action defined");
	}

	@Override
	public void handle(final Request request, final Response response) {
		LOGGER.debug("Handle issueresolver export request");
		response.setHeader("Content-Disposition", "attachment; filename=\"resolved-issues.json\"");

		final JsonWriter responseWriter = response.newJsonWriter();
		writeStart(responseWriter);

		// Loop through all issues of the project
		final WsClient wsClient = WsClientFactories.getLocal().newClient(request.localConnector());
		
		final SearchWsRequest searchIssuesRequest = new SearchWsRequest();
		final String projectKey = request.mandatoryParam(PARAM_PROJECT_KEY);
		searchIssuesRequest.setProjectKeys(Collections.singletonList(projectKey));
		searchIssuesRequest.setAdditionalFields(Collections.singletonList("comments"));
		searchIssuesRequest.setStatuses(Collections.singletonList("RESOLVED"));
		searchIssuesRequest.setResolutions(Arrays.asList("FALSE-POSITIVE", "WONTFIX"));
		searchIssuesRequest.setPage(1);
		searchIssuesRequest.setPageSize(100);

		boolean doNextPage = true;
		while(doNextPage) {
			LOGGER.debug("Listing issues for project {}; page {}" ,projectKey,searchIssuesRequest.getPage());
			final SearchWsResponse searchIssuesResponse = wsClient.issues().search(searchIssuesRequest);
			for (final Issue issue : searchIssuesResponse.getIssuesList()) {
				writeIssue(responseWriter, issue, searchIssuesResponse.getComponentsList());
			}
			
			doNextPage = searchIssuesResponse.getPaging().getTotal() > (searchIssuesResponse.getPaging().getPageIndex() * searchIssuesResponse.getPaging().getPageSize());
			searchIssuesRequest.setPage(searchIssuesResponse.getPaging().getPageIndex() + 1);
			searchIssuesRequest.setPageSize(searchIssuesResponse.getPaging().getPageSize());
		}

		writeEnd(responseWriter);
		responseWriter.close();
		LOGGER.debug("Issueresolver export request done");
	}

	private void writeStart(final JsonWriter writer) {
		writer.beginObject();
		writer.prop("version", 1);
		writer.name("issues");
		writer.beginArray();
	}

	private void writeIssue(final JsonWriter writer, final Issue issue, List<Component> components) {
		writer.beginObject();

		final IssueKey key = IssueKey.fromIssue(issue, components);
		key.write(writer);

		final IssueData data = IssueData.fromIssue(issue);
		data.write(writer);

		writer.endObject();
	}

	private void writeEnd(final JsonWriter writer) {
		writer.endArray();
		writer.endObject();
		writer.close();
	}

}

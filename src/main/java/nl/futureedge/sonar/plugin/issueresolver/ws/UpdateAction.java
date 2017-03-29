package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.util.HashMap;
import java.util.Map;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService.NewAction;
import org.sonar.api.server.ws.WebService.NewController;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.utils.text.JsonWriter;

import nl.futureedge.sonar.plugin.issueresolver.issues.IssueData;
import nl.futureedge.sonar.plugin.issueresolver.issues.IssueKey;

/**
 * Update action.
 */
public final class UpdateAction implements IssueResolverWsAction {

	public static final String ACTION = "update";
	public static final String PARAM_PROJECT_KEY = "projectKey";
	public static final String PARAM_FROM_PROJECT_KEY = "fromProjectKey";
	public static final String PARAM_PREVIEW = "preview";

	private static final Logger LOGGER = Loggers.get(UpdateAction.class);

	@Override
	public void define(final NewController controller) {
		LOGGER.debug("Defining update action ...");
		final NewAction action = controller.createAction(ACTION)
				.setDescription("Update issues from in one project based on another.").setHandler(this).setPost(true);
		action.createParam(PARAM_PROJECT_KEY).setDescription("Project to resolve issues in").setRequired(true);
		action.createParam(PARAM_FROM_PROJECT_KEY).setDescription("Project to read issues from").setRequired(true);
		action.createParam(PARAM_PREVIEW).setDescription("If import should be a preview").setDefaultValue("false");
		LOGGER.debug("Update action defined");
	}

	@Override
	public void handle(final Request request, final Response response) {
		LOGGER.info("Handle issueresolver update request ...");
		final ImportResult importResult = new ImportResult();

		// Read issues from project
		final Map<IssueKey, IssueData> issues = new HashMap<>();
		ActionHelper.forEachIssue(request.localConnector(),
				ActionHelper.findResolvedIssuesFor(request.mandatoryParam(PARAM_FROM_PROJECT_KEY)),
				(searchIssuesResponse, issue) -> {
					issues.put(IssueKey.fromIssue(issue, searchIssuesResponse.getComponentsList()),
							IssueData.fromIssue(issue));
					importResult.registerIssue();
				});
		LOGGER.info("Read " + importResult.getIssues() + " issues");

		ActionHelper.resolveIssues(request.localConnector(), importResult,
				request.mandatoryParamAsBoolean(PARAM_PREVIEW), request.mandatoryParam(PARAM_PROJECT_KEY), issues);

		LOGGER.info("Unmatched issues: " + importResult.getUnmatchedIssues());
		if (LOGGER.isDebugEnabled()) {
			for (IssueKey unmatchedIssue : issues.keySet()) {
				LOGGER.debug(" - {}", unmatchedIssue);
			}
		}
		LOGGER.info("Unresolved issues: " + importResult.getUnresolvedIssues());
		LOGGER.info("Resolved issues: " + importResult.getResolvedIssues());

		// Sent result
		final JsonWriter responseWriter = response.newJsonWriter();
		importResult.write(responseWriter);
		responseWriter.close();
		LOGGER.debug("Issueresolver update request done");
	}
}

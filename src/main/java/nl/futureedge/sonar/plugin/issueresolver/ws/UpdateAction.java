package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService.NewAction;
import org.sonar.api.server.ws.WebService.NewController;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.utils.text.JsonWriter;

import nl.futureedge.sonar.plugin.issueresolver.helper.IssueHelper;
import nl.futureedge.sonar.plugin.issueresolver.helper.SearchHelper;
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
	public static final String PARAM_SKIP_ASSIGN = "skipAssign";
	public static final String PARAM_SKIP_COMMENTS = "skipComments";

	private static final String BOOLEAN_FALSE = "false";
	private static final List<String> BOOLEAN_VALUES = Arrays.asList("true", BOOLEAN_FALSE);

	private static final Logger LOGGER = Loggers.get(UpdateAction.class);

	@Override
	public void define(final NewController controller) {
		LOGGER.debug("Defining update action ...");
		final NewAction action = controller.createAction(ACTION)
				.setDescription("Update issues from in one project based on another.")
				.setResponseExample(getClass().getResource("/response-examples/import.json")).setHandler(this)
				.setPost(true);

		action.createParam(PARAM_PROJECT_KEY).setDescription("Project to resolve issues in")
				.setExampleValue("my-project").setRequired(true);
		action.createParam(PARAM_FROM_PROJECT_KEY).setDescription("Project to read issues from")
				.setExampleValue("my-other-project").setRequired(true);
		action.createParam(PARAM_PREVIEW).setDescription("If import should be a preview")
				.setPossibleValues(BOOLEAN_VALUES).setDefaultValue(BOOLEAN_FALSE);
		action.createParam(PARAM_SKIP_ASSIGN).setDescription("If assignment should be skipped")
				.setPossibleValues(BOOLEAN_VALUES).setDefaultValue(BOOLEAN_FALSE);
		action.createParam(PARAM_SKIP_COMMENTS).setDescription("If comments should be skipped")
				.setPossibleValues(BOOLEAN_VALUES).setDefaultValue(BOOLEAN_FALSE);
		LOGGER.debug("Update action defined");
	}

	@Override
	public void handle(final Request request, final Response response) {
		LOGGER.info("Handle issueresolver update request ...");
		final ImportResult importResult = new ImportResult();

		// Read issues from project
		final Map<IssueKey, IssueData> issues = new HashMap<>();
		IssueHelper.forEachIssue(request.localConnector(),
				SearchHelper.findIssuesForExport(request.mandatoryParam(PARAM_FROM_PROJECT_KEY)),
				(searchIssuesResponse, issue) -> {
					issues.put(IssueKey.fromIssue(issue, searchIssuesResponse.getComponentsList()),
							IssueData.fromIssue(issue));
					importResult.registerIssue();
				});
		LOGGER.info("Read " + importResult.getIssues() + " issues");

		IssueHelper.resolveIssues(request.localConnector(), importResult,
				request.mandatoryParamAsBoolean(PARAM_PREVIEW), request.mandatoryParamAsBoolean(PARAM_SKIP_ASSIGN),
				request.mandatoryParamAsBoolean(PARAM_SKIP_COMMENTS), request.mandatoryParam(PARAM_PROJECT_KEY),
				issues);

		// Sent result
		final JsonWriter responseWriter = response.newJsonWriter();
		importResult.write(responseWriter);
		responseWriter.close();
		LOGGER.debug("Issueresolver update request done");
	}
}

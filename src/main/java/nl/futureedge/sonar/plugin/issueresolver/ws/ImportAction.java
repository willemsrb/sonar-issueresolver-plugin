package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.io.IOException;
import java.util.Arrays;
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

import nl.futureedge.sonar.plugin.issueresolver.helper.IssueHelper;
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
	public static final String PARAM_SKIP_ASSIGN = "skipAssign";
	public static final String PARAM_SKIP_COMMENTS = "skipComments";

	private static final String BOOLEAN_FALSE = "false";
	private static final List<String> BOOLEAN_VALUES = Arrays.asList("true", BOOLEAN_FALSE);

	private static final Logger LOGGER = Loggers.get(ImportAction.class);

	@Override
	public void define(final NewController controller) {
		LOGGER.debug("Defining import action ...");
		final NewAction action = controller.createAction(ACTION)
				.setDescription("Import issues that have exported with the export function.")
				.setResponseExample(getClass().getResource("/response-examples/import.json")).setHandler(this)
				.setPost(true);

		action.createParam(PARAM_PROJECT_KEY).setDescription("Project to import issues to")
				.setExampleValue("my-project").setRequired(true);
		action.createParam(PARAM_PREVIEW).setDescription("If import should be a preview")
				.setPossibleValues(BOOLEAN_VALUES).setDefaultValue(BOOLEAN_FALSE);
		action.createParam(PARAM_SKIP_ASSIGN).setDescription("If assignment should be skipped")
				.setPossibleValues(BOOLEAN_VALUES).setDefaultValue(BOOLEAN_FALSE);
		action.createParam(PARAM_SKIP_COMMENTS).setDescription("If comments should be skipped")
				.setPossibleValues(BOOLEAN_VALUES).setDefaultValue(BOOLEAN_FALSE);
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

		IssueHelper.resolveIssues(request.localConnector(), importResult,
				request.mandatoryParamAsBoolean(PARAM_PREVIEW), request.mandatoryParamAsBoolean(PARAM_SKIP_ASSIGN),
				request.mandatoryParamAsBoolean(PARAM_SKIP_COMMENTS), request.mandatoryParam(PARAM_PROJECT_KEY),
				issues);

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

			if (issues.containsKey(key)) {
				importResult.registerDuplicateKey();
			} else {
				issues.put(key, data);
			}
			reader.endObject();
		}
		reader.endArray();

		return issues;
	}
}

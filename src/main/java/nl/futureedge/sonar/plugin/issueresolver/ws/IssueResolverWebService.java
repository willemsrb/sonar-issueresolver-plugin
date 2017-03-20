package nl.futureedge.sonar.plugin.issueresolver.ws;

import org.sonar.api.server.ws.WebService;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * Issue resolver web service.
 */
public final class IssueResolverWebService implements WebService {

	/**
	 * Controller path.
	 */
	public static final String CONTROLLER_PATH = "api/issueresolver";

	private static final Logger LOGGER = Loggers.get(ExportAction.class);

	private final IssueResolverWsAction[] actions;

	/**
	 * Constructor.
	 * @param actions issue resolver actions
	 */
	public IssueResolverWebService(final IssueResolverWsAction... actions) {
		this.actions = actions;
	}

	@Override
	public void define(final Context context) {
		LOGGER.debug("Defining controller ...");
		// Define the service
		final NewController controller = context.createController(CONTROLLER_PATH);
		controller.setDescription("Export and import resolved issues (false-positive and won't fix)");

		// Define actions
		for (final IssueResolverWsAction action : actions) {
			action.define(controller);
		}

		// Apply changes
		controller.done();
		LOGGER.debug("Controller defined");
	}
}

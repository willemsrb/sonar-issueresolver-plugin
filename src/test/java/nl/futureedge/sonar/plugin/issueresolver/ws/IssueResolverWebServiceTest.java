package nl.futureedge.sonar.plugin.issueresolver.ws;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService;
import org.sonar.api.server.ws.WebService.Action;
import org.sonar.api.server.ws.WebService.Controller;
import org.sonar.api.server.ws.WebService.NewController;
import org.sonar.api.utils.Version;

public class IssueResolverWebServiceTest {

	@Test
	public void test() {

		final IssueResolverWebService subject = new IssueResolverWebService(new ExportAction(), new ImportAction());

		final WebService.Context context = new WebService.Context();
		Assert.assertEquals(0, context.controllers().size());
		subject.define(context);
		Assert.assertEquals(1, context.controllers().size());

		// Controller
		Controller controller = context.controller("api/issueresolver");
		Assert.assertNotNull(controller);
		Assert.assertEquals(2, controller.actions().size());

		// Export
		Action exportAction = controller.action("export");
		Assert.assertNotNull(exportAction);
		Assert.assertTrue(exportAction.handler() instanceof ExportAction);
		Assert.assertEquals(1, exportAction.params().size());
		Assert.assertNotNull(exportAction.param("projectKey"));

		// Import
		Action importAction = controller.action("import");
		Assert.assertNotNull(importAction);
		Assert.assertTrue(importAction.handler() instanceof ImportAction);
		Assert.assertEquals(3, importAction.params().size());
		Assert.assertNotNull(importAction.param("projectKey"));
		Assert.assertNotNull(importAction.param("preview"));
		Assert.assertNotNull(importAction.param("data"));
	}

}

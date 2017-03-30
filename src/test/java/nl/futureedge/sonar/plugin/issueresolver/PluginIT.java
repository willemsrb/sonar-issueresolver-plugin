package nl.futureedge.sonar.plugin.issueresolver;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.wsclient.connectors.ConnectionException;
import org.sonar.wsclient.services.CreateQuery;
import org.sonar.wsclient.services.Model;
import org.sonar.wsclient.services.Query;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.OrchestratorBuilder;
import com.sonar.orchestrator.build.MavenBuild;
import com.sonar.orchestrator.container.Server;
import com.sonar.orchestrator.locator.FileLocation;

import nl.futureedge.sonar.plugin.issueresolver.ws.ExportAction;
import nl.futureedge.sonar.plugin.issueresolver.ws.ImportAction;
import nl.futureedge.sonar.plugin.issueresolver.ws.IssueResolverWebService;
import nl.futureedge.sonar.plugin.issueresolver.ws.UpdateAction;


@RunWith(Parameterized.class)
public class PluginIT {

	private static final Logger LOGGER = Loggers.get(ExportAction.class);

	private static final String RESULT_NO_ISSUES = "{\"version\":1,\"issues\":[]}";
	private static final String RESULT_ISSUES = "{\"version\":1,\"issues\":["
			+ "{\"longName\":\"src/main/java/TestClass.java\",\"rule\":\"squid:S1220\",\"line\":0,\"status\":\"RESOLVED\",\"resolution\":\"WONTFIX\",\"assignee\":\"\",\"comments\":[]},"
			+ "{\"longName\":\"src/main/java/TestClass.java\",\"rule\":\"squid:S106\",\"line\":6,\"status\":\"RESOLVED\",\"resolution\":\"FALSE-POSITIVE\",\"assignee\":\"\",\"comments\":[]}"
			+ "]}";

	@Parameters
	public static Collection<Object[]> sonarQubeVersions() {
		return Arrays.asList(new Object[][] { { "6.3" } });
	}

	private final String sonarQubeVersion;
	private Orchestrator orchestrator;

	public PluginIT(final String sonarQubeVersion) {
		this.sonarQubeVersion = sonarQubeVersion;
	}

	@Before
	public void setupSonarQube() {
		System.getProperties().setProperty("sonar.runtimeVersion", sonarQubeVersion);

		final OrchestratorBuilder builder = Orchestrator.builderEnv();
		builder.addPlugin(FileLocation.byWildcardMavenFilename(new File("target"), "sonar-issueresolver-plugin-*.jar"));
		builder.setOrchestratorProperty("javaVersion", "4.2.1").addPlugin("java");

		// Enable debug logging for web components
		builder.setServerProperty("sonar.log.level.web", "DEBUG");

		orchestrator = builder.build();
		orchestrator.start();
	}

	@After
	public void teardownSonarQube() {
		if (orchestrator != null) {
			orchestrator.stop();
		}
	}

	public void runSonar(String branch) {
		final File pom = new File(new File(".", "target/it"), "pom.xml");

		final MavenBuild install = MavenBuild.create(pom).setGoals("clean verify");
		Assert.assertTrue("'clean verify' failed", orchestrator.executeBuild(install).isSuccess());

		final HashMap<String, String> sonarProperties = new HashMap<>();
		sonarProperties.put("sonar.login", "");
		sonarProperties.put("sonar.password", "");
		sonarProperties.put("sonar.skip", "false");
		sonarProperties.put("sonar.scanner.skip", "false");

		if (branch != null) {
			sonarProperties.put("sonar.branch", branch);
		}

		final MavenBuild sonar = MavenBuild.create(pom).setGoals("sonar:sonar").setProperties(sonarProperties);
		Assert.assertTrue("'sonar:sonar' failed", orchestrator.executeBuild(sonar).isSuccess());
	}

	@Test
	public void test() {
		// MASTER
		runSonar(null);
		final String masterProjectKey = "nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin-it";

		// Export issues (no issues expected)
		final String resultA = exportIssues(masterProjectKey);
		LOGGER.info("Result A: {}", resultA);
		Assert.assertEquals(RESULT_NO_ISSUES, resultA);

		// Resolve issues
		resolveIssues(masterProjectKey);

		// Export issues (two issues expected)
		final String resultB = exportIssues(masterProjectKey);
		LOGGER.info("Result B: {}", resultB);
		Assert.assertEquals(RESULT_ISSUES, resultB);

		// BRANCH
		runSonar("branchOne");
		final String branchOneProjectKey = "nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin-it:branchOne";

		// Export issues (no issues expected)
		final String resultC = exportIssues(branchOneProjectKey);
		LOGGER.info("Result C: {}", resultC);
		Assert.assertEquals(RESULT_NO_ISSUES, resultC);

		// Import issues from master export into branch
		importResolvedIssues(branchOneProjectKey, resultB);

		// Export issues (two issues expected)
		final String resultD = exportIssues(branchOneProjectKey);
		LOGGER.info("Result D: {}", resultD);
		Assert.assertEquals(RESULT_ISSUES, resultD);

		// SECOND BRANCH
		runSonar("branchTwo");
		final String branchTwoProjectKey = "nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin-it:branchTwo";

		// Export issues (no issues expected)
		final String resultE = exportIssues(branchTwoProjectKey);
		LOGGER.info("Result E: {}", resultE);
		Assert.assertEquals(RESULT_NO_ISSUES, resultE);

		// Import issues from master export into branch
		updateResolvedIssues(masterProjectKey, branchTwoProjectKey);

		// Export issues (two issues expected)
		final String resultF = exportIssues(branchTwoProjectKey);
		LOGGER.info("Result F: {}", resultF);
		Assert.assertEquals(RESULT_ISSUES, resultF);
	}

	private String exportIssues(final String projectKey) {
		LOGGER.info("Exporting issues for project {}", projectKey);
		final ExportQuery exportQuery = new ExportQuery(projectKey);
		return orchestrator.getServer().getAdminWsClient().getConnector().execute(exportQuery);
	}

	private void resolveIssues(String projectKey) {
		LOGGER.info("Listing issues for {}", projectKey);
		final SearchIssuesQuery issuesQuery = new SearchIssuesQuery(projectKey);
		final String issuesResult = orchestrator.getServer().getAdminWsClient().getConnector().execute(issuesQuery);
		final JsonObject json = new JsonParser().parse(issuesResult).getAsJsonObject();
		final JsonArray issues = json.get("issues").getAsJsonArray();
		LOGGER.info("Project {} has {} issues", projectKey, issues.size());
		for (final JsonElement issueElement : issues) {
			final JsonObject issue = issueElement.getAsJsonObject();
			if ("squid:S1220".equals(issue.get("rule").getAsString())) {
				resolveIssue(issue.get("key").getAsString(), "wontfix");
			}
			if ("squid:S106".equals(issue.get("rule").getAsString())) {
				resolveIssue(issue.get("key").getAsString(), "falsepositive");
			}
		}
	}

	private void resolveIssue(String issueKey, String transition) {
		LOGGER.info("Resolving issue {} with transition {}", issueKey, transition);
		final ResolveIssueQuery resolveIssueQuery = new ResolveIssueQuery(issueKey, transition);
		Assert.assertNotNull(orchestrator.getServer().getAdminWsClient().getConnector().execute(resolveIssueQuery));
	}

	private String importResolvedIssues(final String projectKey, final String data) {
		// Cannot use query because we use the fileupload
		LOGGER.info("Importing issues into project {}", projectKey);

		// Use httpclient 4
		final CredentialsProvider provider = new BasicCredentialsProvider();
		provider.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(Server.ADMIN_LOGIN, Server.ADMIN_PASSWORD));

		final AuthCache authCache = new BasicAuthCache();
		authCache.put(new HttpHost("localhost", orchestrator.getServer().port()), new BasicScheme());

		final HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();

		final HttpClientContext context = HttpClientContext.create();
		context.setCredentialsProvider(provider);
		context.setAuthCache(authCache);

		final HttpPost post = new HttpPost(orchestrator.getServer().getUrl() + "/"
				+ IssueResolverWebService.CONTROLLER_PATH + "/" + ImportAction.ACTION);
		post.setHeader("Accept", "application/json");

		// Set data
		final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addPart("projectKey", new StringBody(projectKey, ContentType.MULTIPART_FORM_DATA));
		builder.addPart("data",
				new ByteArrayBody(data.getBytes(), ContentType.MULTIPART_FORM_DATA, "resolved-issues.json"));
		post.setEntity(builder.build());

		try {
			final HttpResponse response = client.execute(post, context);
			final HttpEntity entity = response.getEntity();
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return EntityUtils.toString(entity);
			} else {
				throw new ConnectionException("HTTP error: " + response.getStatusLine().getStatusCode() + ", msg: "
						+ response.getStatusLine().getReasonPhrase() + ", query: " + post.toString());
			}
		} catch (IOException e) {
			throw new ConnectionException("Query: " + post.getURI(), e);
		} finally {
			post.releaseConnection();
		}
	}

	private String updateResolvedIssues(final String fromProjectKey, final String projectKey) {
		LOGGER.info("Updating issues from project {} to project {}", fromProjectKey, projectKey);
		final UpdateQuery updateQuery = new UpdateQuery(fromProjectKey, projectKey);
		return orchestrator.getServer().getAdminWsClient().getConnector().execute(updateQuery);
	}

	/**
	 * Export issues.
	 */
	private final class ExportQuery extends Query<Model> {

		public static final String BASE_URL = "/" + IssueResolverWebService.CONTROLLER_PATH + "/" + ExportAction.ACTION;

		private String projectKey;

		public ExportQuery(final String projectKey) {
			this.projectKey = projectKey;
		}

		@Override
		public Class<Model> getModelClass() {
			return Model.class;
		}

		@Override
		public String getUrl() {
			final StringBuilder url = new StringBuilder(BASE_URL);
			url.append('?');
			appendUrlParameter(url, ExportAction.PARAM_PROJECT_KEY, projectKey);
			return url.toString();
		}
	}

	/**
	 * Search issues.
	 */
	private final class SearchIssuesQuery extends Query<Model> {

		public static final String BASE_URL = "/api/issues/search";

		private String projectKey;

		public SearchIssuesQuery(final String projectKey) {
			this.projectKey = projectKey;
		}

		@Override
		public Class<Model> getModelClass() {
			return Model.class;
		}

		@Override
		public String getUrl() {
			final StringBuilder url = new StringBuilder(BASE_URL);
			url.append('?');
			appendUrlParameter(url, "projectKeys", projectKey);
			return url.toString();
		}
	}

	/**
	 * Resolve issues.
	 */
	private final class ResolveIssueQuery extends CreateQuery<Model> {

		public static final String BASE_URL = "/api/issues/do_transition";

		private String issueKey;
		private String transition;

		public ResolveIssueQuery(final String issueKey, final String transition) {
			this.issueKey = issueKey;
			this.transition = transition;
		}

		@Override
		public Class<Model> getModelClass() {
			return Model.class;
		}

		@Override
		public String getUrl() {
			final StringBuilder url = new StringBuilder(BASE_URL);
			url.append('?');
			appendUrlParameter(url, "issue", issueKey);
			appendUrlParameter(url, "transition", transition);
			return url.toString();
		}
	}

	/**
	 * Update issues.
	 */
	private final class UpdateQuery extends CreateQuery<Model> {

		public static final String BASE_URL = "/" + IssueResolverWebService.CONTROLLER_PATH + "/" + UpdateAction.ACTION;

		private String fromProjectKey;
		private String projectKey;

		public UpdateQuery(final String fromProjectKey, final String projectKey) {
			this.fromProjectKey = fromProjectKey;
			this.projectKey = projectKey;
		}

		@Override
		public Class<Model> getModelClass() {
			return Model.class;
		}

		@Override
		public String getUrl() {
			final StringBuilder url = new StringBuilder(BASE_URL);
			url.append('?');
			appendUrlParameter(url, UpdateAction.PARAM_FROM_PROJECT_KEY, fromProjectKey);
			appendUrlParameter(url, UpdateAction.PARAM_PROJECT_KEY, projectKey);
			return url.toString();
		}
	}
}

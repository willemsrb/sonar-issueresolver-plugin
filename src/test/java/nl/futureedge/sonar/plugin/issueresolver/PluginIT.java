package nl.futureedge.sonar.plugin.issueresolver;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.OrchestratorBuilder;
import com.sonar.orchestrator.build.MavenBuild;
import com.sonar.orchestrator.locator.FileLocation;

@RunWith(Parameterized.class)
public class PluginIT {

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
		
		orchestrator = builder.build();
		orchestrator.start();
	}

	@After
	public void teardownSonarQube() {
		if (orchestrator != null) {
			orchestrator.stop();
		}
	}

	public void runSonar() {
		final File pom = new File(new File(".", "target/it"), "pom.xml");

		final MavenBuild install = MavenBuild.create(pom).setGoals("clean verify");
		Assert.assertTrue("'clean verify' failed", orchestrator.executeBuild(install).isSuccess());

		final HashMap<String, String> sonarProperties = new HashMap<>();
		sonarProperties.put("sonar.login", "");
		sonarProperties.put("sonar.password", "");
		sonarProperties.put("sonar.skip", "false");
		sonarProperties.put("sonar.scanner.skip", "false");

		final MavenBuild sonar = MavenBuild.create(pom).setGoals("sonar:sonar").setProperties(sonarProperties);
		Assert.assertTrue("'sonar:sonar' failed", orchestrator.executeBuild(sonar).isSuccess());
	}

	@Test
	public void test() {
		runSonar();
	}

}

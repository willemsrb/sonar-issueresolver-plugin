package nl.futureedge.sonar.plugin.issueresolver;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import nl.futureedge.sonar.plugin.issueresolver.IssueResolverPlugin;

public class IssueResolverPluginTest {

	@Test
	public void test() {
		final IssueResolverPlugin subject = new IssueResolverPlugin();
		final Plugin.Context context = new Plugin.Context(SonarRuntimeImpl.forSonarQube(Version.create(5, 6), SonarQubeSide.SERVER));

		Assert.assertEquals(0, context.getExtensions().size());
		subject.define(context);
		Assert.assertEquals(4, context.getExtensions().size());
	}

}

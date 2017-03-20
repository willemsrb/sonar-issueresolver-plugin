package nl.futureedge.sonar.plugin.issueresolver.page;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.web.page.Context;

public class IssueResolverPageTest {

	@Test
	public void test() {
		final IssueResolverPage subject = new IssueResolverPage();
		final Context context = new Context();

		Assert.assertEquals(0, context.getPages().size());
		subject.define(context);
		Assert.assertEquals(1, context.getPages().size());
	}

}

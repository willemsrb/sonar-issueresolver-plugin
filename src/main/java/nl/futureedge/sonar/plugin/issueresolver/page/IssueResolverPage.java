package nl.futureedge.sonar.plugin.issueresolver.page;

import org.sonar.api.web.page.Context;
import org.sonar.api.web.page.Page;
import org.sonar.api.web.page.Page.Qualifier;
import org.sonar.api.web.page.Page.Scope;
import org.sonar.api.web.page.PageDefinition;

/**
 * Issue resolver page.
 */
public final class IssueResolverPage implements PageDefinition {

	@Override
	public void define(final Context context) {
		final Page issueresolverPage = Page.builder("issueresolver/entrypoint").setName("Issue resolver")
				.setScope(Scope.COMPONENT).setComponentQualifiers(Qualifier.PROJECT).setAdmin(true).build();
		context.addPage(issueresolverPage);
	}
}

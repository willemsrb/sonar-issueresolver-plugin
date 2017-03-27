package nl.futureedge.sonar.plugin.issueresolver.issues;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.sonar.api.utils.text.JsonWriter;
import org.sonarqube.ws.Issues.Component;
import org.sonarqube.ws.Issues.Issue;

import nl.futureedge.sonar.plugin.issueresolver.json.JsonReader;

/**
 * Issue key; used to match issues.
 */
public final class IssueKey {

	private static final String NAME_LONG_NAME = "longName";
	private static final String NAME_RULE = "rule";
	private static final String NAME_LINE = "line";

	private String longName;
	private String rule;
	private int line;

	/**
	 * Constructor.
	 * 
	 * @param rule
	 *            rule
	 * @param component
	 *            component
	 * @param line
	 *            line
	 */
	private IssueKey(final String longName, final String rule, final int line) {
		this.longName = longName;
		this.rule = rule;
		this.line = line;
	}

	/**
	 * Construct key from search.
	 * 
	 * @param issue
	 *            issue from search
	 * @return issue key
	 */
	public static IssueKey fromIssue(final Issue issue, List<Component> components) {
		final Component component = findComponent(components, issue.getComponent());
		return new IssueKey(component.getLongName(), issue.getRule(), issue.getTextRange().getStartLine());
	}

	private static Component findComponent(final List<Component> components, final String key) {
		for (final Component component : components) {
			if (key.equals(component.getKey())) {
				return component;
			}
		}

		throw new IllegalStateException("Component of issue not found");
	}

	/**
	 * Construct key from export data.
	 * 
	 * @param reader
	 *            json reader
	 * @return issue key
	 * @throws IOException
	 *             IO errors in underlying json reader
	 */
	public static IssueKey read(final JsonReader reader) throws IOException {
		return new IssueKey(reader.prop(NAME_LONG_NAME), reader.prop(NAME_RULE), reader.propAsInt(NAME_LINE));
	}

	/**
	 * Write key to export data.
	 * 
	 * @param writer
	 *            json writer
	 */
	public void write(final JsonWriter writer) {
		writer.prop(NAME_LONG_NAME, longName);
		writer.prop(NAME_RULE, rule);
		writer.prop(NAME_LINE, line);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(longName).append(rule).append(line).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		final IssueKey that = (IssueKey) obj;
		return new EqualsBuilder().append(longName, that.longName).append(rule, that.rule).append(line, that.line)
				.isEquals();
	}

	@Override
	public String toString() {
		return "IssueKey [longName=" + longName + ", rule=" + rule + ", line=" + line + "]";
	}

}

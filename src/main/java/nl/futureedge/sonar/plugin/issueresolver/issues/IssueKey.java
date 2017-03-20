package nl.futureedge.sonar.plugin.issueresolver.issues;

import java.io.IOException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.sonar.api.utils.text.JsonWriter;
import org.sonarqube.ws.Issues.Issue;

import nl.futureedge.sonar.plugin.issueresolver.json.JsonReader;

/**
 * Issue key; used to match issues.
 */
public final class IssueKey {

	private static final String NAME_RULE = "rule";
	private static final String NAME_COMPONENT = "component";
	private static final String NAME_START = "start";
	private static final String NAME_OFFSET = "offset";

	private String rule;
	private String component;
	private int start;
	private int offset;

	/**
	 * Constructor.
	 * 
	 * @param rule
	 *            rule
	 * @param component
	 *            component
	 * @param start
	 *            start
	 * @param offset
	 *            offset
	 */
	private IssueKey(final String rule, final String component, final int start, final int offset) {
		this.rule = rule;
		this.component = component;
		this.start = start;
		this.offset = offset;
	}

	/**
	 * Construct key from search.
	 * 
	 * @param issue
	 *            issue from search
	 * @return issue key
	 */
	public static IssueKey fromIssue(final Issue issue) {
		return new IssueKey(issue.getRule(), issue.getComponent(), issue.getTextRange().getStartLine(),
				issue.getTextRange().getStartOffset());
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
		return new IssueKey(reader.prop(NAME_RULE), reader.prop(NAME_COMPONENT), reader.propAsInt(NAME_START),
				reader.propAsInt(NAME_OFFSET));
	}

	/**
	 * Write key to export data.
	 * 
	 * @param writer
	 *            json writer
	 */
	public void write(final JsonWriter writer) {
		writer.prop(NAME_RULE, rule);
		writer.prop(NAME_COMPONENT, component);
		writer.prop(NAME_START, start);
		writer.prop(NAME_OFFSET, offset);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(rule).append(component).append(start).append(offset).toHashCode();
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
		return new EqualsBuilder().append(rule, that.rule).append(component, that.component).append(start, that.start)
				.append(offset, that.offset).isEquals();
	}
}

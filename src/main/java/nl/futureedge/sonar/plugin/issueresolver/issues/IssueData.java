package nl.futureedge.sonar.plugin.issueresolver.issues;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sonar.api.utils.text.JsonWriter;
import org.sonarqube.ws.Issues.Comment;
import org.sonarqube.ws.Issues.Issue;

import nl.futureedge.sonar.plugin.issueresolver.json.JsonReader;

/**
 * Issue data; used to resolve issues.
 */
public final class IssueData {

	private static final String NAME_STATUS = "status";
	private static final String NAME_RESOLUTION = "resolution";
	private static final String NAME_ASSIGNEE = "assignee";
	private static final String NAME_COMMENTS = "comments";

	private final String status;
	private final String resolution;
	private final String assignee;
	private final List<String> comments;

	/**
	 * Constructor.
	 * 
	 * @param status
	 *            status
	 * @param resolution
	 *            resolution
	 * @param assignee
	 *            assignee
	 * @param comments
	 *            comments
	 */
	private IssueData(final String status, final String resolution, final String assignee,
			final List<String> comments) {
		this.status = status;
		this.resolution = resolution;
		this.assignee = assignee;
		this.comments = comments;
	}

	/**
	 * Construct data from search.
	 * 
	 * Reads the markdown format of comments.
	 * 
	 * @param issue
	 *            issue from search
	 * @return issue data
	 */
	public static IssueData fromIssue(final Issue issue) {
		final List<String> comments = new ArrayList<>();
		for (final Comment comment : issue.getComments().getCommentsList()) {
			comments.add(comment.getMarkdown());
		}

		return new IssueData(issue.getStatus(), issue.getResolution(), issue.getAssignee(), comments);
	}

	/**
	 * Construct data from export data.
	 * 
	 * @param reader
	 *            json reader
	 * @return issue data
	 * @throws IOException
	 *             IO errors in underlying json reader
	 */
	public static IssueData read(final JsonReader reader) throws IOException {
		return new IssueData(reader.prop(NAME_STATUS), reader.prop(NAME_RESOLUTION), reader.prop(NAME_ASSIGNEE),
				reader.propValues(NAME_COMMENTS));
	}

	/**
	 * Write data to export data.
	 * 
	 * @param writer
	 *            json writer
	 */
	public void write(final JsonWriter writer) {
		writer.prop(NAME_STATUS, status);
		writer.prop(NAME_RESOLUTION, resolution);
		writer.prop(NAME_ASSIGNEE, assignee);

		writer.name(NAME_COMMENTS);
		writer.beginArray();
		writer.values(comments);
		writer.endArray();
	}

	/**
	 * Status.
	 * 
	 * @return status
	 */
	public String getStatus() {
		return status;
	}

	
	/**
	 * Resolution.
	 * 
	 * @return resolution
	 */
	public String getResolution() {
		return resolution;
	}

	/**
	 * Assignee.
	 * 
	 * @return assignee
	 */
	public String getAssignee() {
		return assignee;
	}

	/**
	 * Comments (markdown format).
	 * 
	 * @return list of comments
	 */
	public List<String> getComments() {
		return comments;
	}

}

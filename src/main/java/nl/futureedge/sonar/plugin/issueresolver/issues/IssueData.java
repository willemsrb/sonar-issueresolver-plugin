package nl.futureedge.sonar.plugin.issueresolver.issues;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.api.utils.text.JsonWriter;
import org.sonarqube.ws.Issues.Comment;
import org.sonarqube.ws.Issues.Issue;

import nl.futureedge.sonar.plugin.issueresolver.json.JsonReader;

/**
 * Issue data; used to resolve issues.
 */
public final class IssueData {
	
	private static final String NAME_COMMENTS = "comments";
	private static final String NAME_RESOLUTION = "resolution";
	
	private static final Map<String,String> RESOLUTIONS = new HashMap<>();
	static {
		RESOLUTIONS.put("FALSE-POSITIVE", "falsepositive");
		RESOLUTIONS.put("WONTFIX", "wontfix");
	}
	
	private final String resolution;
	private final List<String> comments;

	/**
	 * Constructor; 
	 * @param resolution resolution
	 * @param comments comments
	 */
	private IssueData(final String resolution, final List<String> comments) {
		this.resolution = resolution;
		this.comments = comments;
	}

	/**
	 * Construct data from search. 
	 * 
	 * Translates resolution from search_issues (FALSE-POSITIVE or WONTFIX) value to do_transition (falsepositive or wontfix) value.
	 * Reads the markdown format of comments.
	 * 
	 * @param issue issue from search
	 * @return issue data
	 */
	public static IssueData fromIssue(final Issue issue) {
		final List<String> comments = new ArrayList<>();
		for (final Comment comment : issue.getComments().getCommentsList()) {
			comments.add(comment.getMarkdown());
		}
		
		return new IssueData(RESOLUTIONS.get(issue.getResolution()), comments);
	}

	/**
	 * Construct data from export data.
	 * @param reader json reader
	 * @return issue data
	 * @throws IOException IO errors in underlying json reader
	 */
	public static IssueData read(final JsonReader reader) throws IOException {
		return new IssueData(reader.prop(NAME_RESOLUTION), reader.propValues(NAME_COMMENTS));
	}

	/**
	 * Write data to export data.
	 * @param writer json writer
	 */
	public void write(final JsonWriter writer) {
		// Resolution
		writer.prop(NAME_RESOLUTION, resolution);

		// Comments
		writer.name(NAME_COMMENTS);
		writer.beginArray();
		writer.values(comments);
		writer.endArray();
	}

	/**
	 * Resolution (do_transition value).
	 * @return resolution
	 */
	public String getResolution() {
		return resolution;
	}

	/**
	 * Comments (markdown format).
	 * @return list of comments
	 */
	public List<String> getComments() {
		return comments;
	}

}

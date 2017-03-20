package nl.futureedge.sonar.plugin.issueresolver.issues;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.utils.text.JsonWriter;
import org.sonarqube.ws.Issues.Comment;
import org.sonarqube.ws.Issues.Comments;
import org.sonarqube.ws.Issues.Issue;

import nl.futureedge.sonar.plugin.issueresolver.json.JsonReader;

public class IssueDataTest {

	@Test
	public void test() throws IOException {
		final Issue issue = ReflectionTestUtils.build(Issue.class, "resolution_", "FALSE-POSITIVE", "comments_",
				ReflectionTestUtils.build(Comments.class, "comments_",
						Arrays.asList(ReflectionTestUtils.build(Comment.class, "markdown_", "Comment one"),
								ReflectionTestUtils.build(Comment.class, "markdown_", "Comment two"))));

		final IssueData data = IssueData.fromIssue(issue);
		Assert.assertEquals("falsepositive", data.getResolution());
		Assert.assertEquals(Arrays.asList("Comment one", "Comment two"), data.getComments());

		final String json;
		try (final StringWriter writer = new StringWriter()) {
			final JsonWriter jsonWriter = JsonWriter.of(writer);
			jsonWriter.beginObject();
			data.write(jsonWriter);
			jsonWriter.endObject();
			jsonWriter.close();

			json = writer.toString();
		}
		Assert.assertEquals("{\"resolution\":\"falsepositive\",\"comments\":[\"Comment one\",\"Comment two\"]}", json);

		final IssueData readData;
		try (final ByteArrayInputStream bais = new ByteArrayInputStream(json.getBytes("UTF-8"));
				final JsonReader reader = new JsonReader(bais)) {
			reader.beginObject();
			readData = IssueData.read(reader);
			reader.endObject();
		}
		
		Assert.assertEquals("falsepositive", readData.getResolution());
		Assert.assertEquals(Arrays.asList("Comment one", "Comment two"), readData.getComments());
	}
}

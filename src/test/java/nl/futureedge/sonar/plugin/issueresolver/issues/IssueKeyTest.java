package nl.futureedge.sonar.plugin.issueresolver.issues;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.utils.text.JsonWriter;
import org.sonarqube.ws.Common.TextRange;
import org.sonarqube.ws.Issues.Component;
import org.sonarqube.ws.Issues.Issue;

import nl.futureedge.sonar.plugin.issueresolver.json.JsonReader;

public class IssueKeyTest {

	@Test
	public void test() throws IOException {
		final Issue issue = ReflectionTestUtils.build(Issue.class, "rule_", "test:rule001", "component_",
				"nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java",
				"textRange_", ReflectionTestUtils.build(TextRange.class, "startLine_", 13, "startOffset_", 65));
		final Component component = ReflectionTestUtils.build(Component.class, "key_", "nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java",
			"longName_", "src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java");
		final List<Component> components = Arrays.asList(component);

		final IssueKey key = IssueKey.fromIssue(issue, components);

		final String json;
		try (final StringWriter writer = new StringWriter()) {
			final JsonWriter jsonWriter = JsonWriter.of(writer);
			jsonWriter.beginObject();
			key.write(jsonWriter);
			jsonWriter.endObject();
			jsonWriter.close();

			json = writer.toString();
		}
		Assert.assertEquals(
				"{\"longName\":\"src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java\",\"rule\":\"test:rule001\",\"line\":13}",
				json);

		final IssueKey readKey;
		try (final ByteArrayInputStream bais = new ByteArrayInputStream(json.getBytes("UTF-8"));
				final JsonReader reader = new JsonReader(bais)) {
			reader.beginObject();
			readKey = IssueKey.read(reader);
			reader.endObject();
		}

		Assert.assertEquals(key.hashCode(), readKey.hashCode());
		Assert.assertEquals(key, readKey);
		Assert.assertFalse(key.equals(null));
		Assert.assertTrue(key.equals(key));
		Assert.assertFalse(key.equals(new Object()));
	}
}

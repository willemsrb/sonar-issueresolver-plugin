package nl.futureedge.sonar.plugin.issueresolver.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

public class JsonReaderTest {

	private static final String JSON = "{\"version\":1,\"issues\":[{\"rule\":\"squid:S1161\",\"component\":\"nl.future-edge.sonarqube"
			+ ".plugins:sonar-issueresolver-plugin:src/main/java/nl/futureedge/sonar/plugin/issueresolver/IssueResolverPage.java\","
			+ "\"line\":13,\"offset\":15,\"resolution\":\"falsepositive\",\"comments\":[\"One\", \"Two\"]},{\"rule\":\"squid:S1161\",\"component\":\"nl.future-edge.sonarqube"
			+ ".plugins:sonar-issueresolver-plugin:src/main/java/nl/futureedge/sonar/plugin/issueresolver/IssueResolverPage.java\","
			+ "\"line\":17,\"offset\":15,\"resolution\":\"wontfix\",\"comments\":[]}]}";

	@Test
	public void test() throws IOException {
		try (InputStream inputStream = new ByteArrayInputStream(JSON.getBytes("UTF-8"));
				JsonReader reader = new JsonReader(inputStream)) {
			reader.beginObject();
			Assert.assertEquals(1, reader.propAsInt("version"));
			reader.assertName("issues");
			reader.beginArray();
			// Issue 1
			Assert.assertTrue(reader.hasNext());
			reader.beginObject();
			Assert.assertEquals("squid:S1161", reader.prop("rule"));
			Assert.assertEquals(
					"nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:src/main/java/nl/futureedge/sonar/plugin/issueresolver/IssueResolverPage.java",
					reader.prop("component"));
			Assert.assertEquals(13, reader.propAsInt("line"));
			Assert.assertEquals(15, reader.propAsInt("offset"));
			Assert.assertEquals("falsepositive", reader.prop("resolution"));
			Assert.assertEquals(Arrays.asList("One", "Two"), reader.propValues("comments"));
			Assert.assertFalse(reader.hasNext());
			reader.endObject();

			// Issue 2
			Assert.assertTrue(reader.hasNext());
			reader.beginObject();
			Assert.assertEquals("squid:S1161", reader.prop("rule"));
			Assert.assertEquals(
					"nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:src/main/java/nl/futureedge/sonar/plugin/issueresolver/IssueResolverPage.java",
					reader.prop("component"));
			Assert.assertEquals(17, reader.propAsInt("line"));
			Assert.assertEquals(15, reader.propAsInt("offset"));
			Assert.assertEquals("wontfix", reader.prop("resolution"));
			Assert.assertEquals(Collections.<String>emptyList(), reader.propValues("comments"));
			Assert.assertFalse(reader.hasNext());
			reader.endObject();

			Assert.assertFalse(reader.hasNext());
			reader.endArray();
			Assert.assertFalse(reader.hasNext());
			Assert.assertFalse(reader.isEndOfDocument());
			reader.endObject();
			Assert.assertTrue(reader.isEndOfDocument());
			reader.close();
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidName() throws IOException {
		try (InputStream inputStream = new ByteArrayInputStream(JSON.getBytes("UTF-8"));
				JsonReader reader = new JsonReader(inputStream)) {
			reader.beginObject();
			reader.assertName("notVersion");
		}
	}
}

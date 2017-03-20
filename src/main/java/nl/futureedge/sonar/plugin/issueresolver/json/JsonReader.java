package nl.futureedge.sonar.plugin.issueresolver.json;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.stream.JsonToken;

/**
 * JSON Reader.
 */
public final class JsonReader implements Closeable {
	private final com.google.gson.stream.JsonReader stream;

	public JsonReader(final InputStream inputStream) throws IOException {
		stream = new com.google.gson.stream.JsonReader(new InputStreamReader(inputStream, "UTF-8"));
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	public void beginObject() throws IOException {
		stream.beginObject();
	}

	public void endObject() throws IOException {
		stream.endObject();
	}

	public void beginArray() throws IOException {
		stream.beginArray();
	}

	public void endArray() throws IOException {
		stream.endArray();
	}

	public String prop(final String name) throws IOException {
		assertName(name);
		return stream.nextString();
	}

	public int propAsInt(final String name) throws IOException {
		assertName(name);
		return stream.nextInt();
	}

	public List<String> propValues(final String name) throws IOException {
		assertName(name);

		List<String> result = new ArrayList<>();
		stream.beginArray();
		while (stream.hasNext()) {
			result.add(stream.nextString());
		}

		stream.endArray();
		return result;
	}

	public void assertName(final String name) throws IOException {
		String actual = stream.nextName();
		if (!Objects.equals(name, actual)) {
			throw new IllegalStateException("Expected name '" + name + "' but was '" + actual + "'");
		}
	}

	public boolean hasNext() throws IOException {
		return stream.hasNext();
	}

	public boolean isEndOfDocument() throws IOException {
		return JsonToken.END_DOCUMENT == stream.peek();
	}
}

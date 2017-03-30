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

	/**
	 * Constructor.
	 * 
	 * @param inputStream
	 *            input stream
	 * @throws IOException
	 *             on IO errors
	 */
	public JsonReader(final InputStream inputStream) throws IOException {
		stream = new com.google.gson.stream.JsonReader(new InputStreamReader(inputStream, "UTF-8"));
	}

	/**
	 * Close.
	 */
	@Override
	public void close() throws IOException {
		stream.close();
	}

	/**
	 * Begin object.
	 * 
	 * @throws IOException
	 *             on IO errors
	 */
	public void beginObject() throws IOException {
		stream.beginObject();
	}

	/**
	 * End object.
	 * 
	 * @throws IOException
	 *             on IO errors
	 */
	public void endObject() throws IOException {
		stream.endObject();
	}

	/**
	 * Begin array.
	 * 
	 * @throws IOException
	 *             on IO errors
	 */
	public void beginArray() throws IOException {
		stream.beginArray();
	}

	/**
	 * End array.
	 * 
	 * @throws IOException
	 *             on IO errors
	 */
	public void endArray() throws IOException {
		stream.endArray();
	}

	/**
	 * Read a property; asserts it is the next property in the JSON stream.
	 * 
	 * @param name
	 *            property name
	 * @return property value
	 * @throws IOException
	 *             on IO errors
	 * @throws IllegalStateException
	 *             if the property isn't available
	 */
	public String prop(final String name) throws IOException {
		assertName(name);
		return stream.nextString();
	}

	/**
	 * Read a property as an integer; asserts it is the next property in the
	 * JSON stream.
	 * 
	 * @param name
	 *            property name
	 * @return property value
	 * @throws IOException
	 *             on IO errors
	 * @throws IllegalStateException
	 *             if the property isn't available
	 */
	public int propAsInt(final String name) throws IOException {
		assertName(name);
		return stream.nextInt();
	}

	/**
	 * Read a property as a list of values; asserts it is the next property in
	 * the JSON stream.
	 * 
	 * @param name
	 *            property name
	 * @return property value
	 * @throws IOException
	 *             on IO errors
	 * @throws IllegalStateException
	 *             if the property isn't available
	 */
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

	/**
	 * Assert the next property is of a given name.
	 * 
	 * @param name
	 *            property name
	 * @throws IOException
	 *             on IO errors
	 * @throws IllegalStateException
	 *             if the property isn't available
	 */
	public void assertName(final String name) throws IOException {
		String actual = stream.nextName();
		if (!Objects.equals(name, actual)) {
			throw new IllegalStateException("Expected name '" + name + "' but was '" + actual + "'");
		}
	}

	/**
	 * Checks if the stream has a next value.
	 * 
	 * @return true if the stream contains a next value
	 * @throws IOException
	 *             on IO errors
	 */
	public boolean hasNext() throws IOException {
		return stream.hasNext();
	}

	/**
	 * Checks if the stream contains nothing more.
	 * 
	 * @return true, if the json stream contain nothing more
	 * @throws IOException
	 *             on IO errors
	 */
	public boolean isEndOfDocument() throws IOException {
		return JsonToken.END_DOCUMENT == stream.peek();
	}
}

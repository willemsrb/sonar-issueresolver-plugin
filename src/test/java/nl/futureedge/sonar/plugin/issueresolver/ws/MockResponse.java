package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.sonar.api.server.ws.LocalConnector;
import org.sonar.api.server.ws.Response;
import org.sonar.api.utils.text.JsonWriter;
import org.sonar.api.utils.text.XmlWriter;

public class MockResponse implements Response, Response.Stream {

	private Map<String,String> headers = new HashMap<>();
	private String mediaType;
	private int status;
	private ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
	/* ************************************ */
	/* *** RESPONSE *********************** */
	/* ************************************ */
	
	@Override
	public JsonWriter newJsonWriter() {
		return JsonWriter.of(new OutputStreamWriter(output()));
	}

	@Override
	public XmlWriter newXmlWriter() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Response noContent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Response setHeader(String name, String value) {
		headers.put(name, value);
		return this;
	}

	@Override
	public Collection<String> getHeaderNames() {
		return headers.keySet();
	}

	@Override
	public String getHeader(String name) {
		return headers.get(name);
	}

	@Override
	public Stream stream() {
		return this;
	}
	
	/* ************************************ */
	/* *** STREAM ************************* */
	/* ************************************ */
	
	@Override
	public Stream setMediaType(String s) {
		mediaType = s;
		return this;
	}

	@Override
	public Stream setStatus(int httpStatus) {
		status = httpStatus;
		return this;
	}

	@Override
	public OutputStream output() {
		return baos;
	}
	
	/* ************************************ */
	/* *** OUTPUT ************************* */
	/* ************************************ */
	
	public byte[] result() {
		return baos.toByteArray();
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getMediaType() {
		return mediaType;
	}

	public int getStatus() {
		return status;
	}

	
	
}

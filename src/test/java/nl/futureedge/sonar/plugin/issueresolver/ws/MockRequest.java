package nl.futureedge.sonar.plugin.issueresolver.ws;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.server.ws.LocalConnector;
import org.sonar.api.server.ws.LocalConnector.LocalRequest;
import org.sonar.api.server.ws.LocalConnector.LocalResponse;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.internal.PartImpl;

import com.google.common.collect.Maps;

public class MockRequest extends Request {

	private final Map<String, String> params = Maps.newHashMap();
	private final Map<String, Part> parts = Maps.newHashMap();
	private String method = "GET";
	private String mediaType = "application/json";
	private String path;

	private LocalConnector localConnectorMock = Mockito.mock(LocalConnector.class, new LocalRequestAnswer());

	/* ************************************ */
	/* *** INPUT ************************** */
	/* ************************************ */

	public void setMethod(String method) {
		checkNotNull(method);
		this.method = method;
	}

	public void setMediaType(String mediaType) {
		checkNotNull(mediaType);
		this.mediaType = mediaType;
	}

	public void setParam(String key, @Nullable String value) {
		if (value == null) {
			params.remove(key);
		} else {
			params.put(key, value);
		}
	}

	public void setPart(String key, InputStream input, String fileName) {
		parts.put(key, new PartImpl(input, fileName));
	}

	public void setPath(String path) {
		this.path = path;
	}

	/* ************************************ */
	/* *** RESPONSE *********************** */
	/* ************************************ */

	@Override
	public String method() {
		return method;
	}

	@Override
	public String getMediaType() {
		return mediaType;
	}

	@Override
	public boolean hasParam(String key) {
		return params.keySet().contains(key);
	}

	@Override
	public String param(String key) {
		return params.get(key);
	}

	@Override
	public List<String> multiParam(String key) {
		String value = params.get(key);
		return value == null ? emptyList() : singletonList(value);
	}

	@Override
	public InputStream paramAsInputStream(String key) {
		return IOUtils.toInputStream(param(key));
	}

	@Override
	public Part paramAsPart(String key) {
		return parts.get(key);
	}

	/**
	 * Returns the {@link Mockito} mock used for the local connector.
	 * 
	 * @return local connector mock
	 */
	@Override
	public LocalConnector localConnector() {
		return localConnectorMock;
	}

	@Override
	public String getPath() {
		return path;
	}

	/* ************************************ */
	/* *** RESPONSE *********************** */
	/* ************************************ */

	private Map<String, List<LocalRequestData>> localRequests = new HashMap<>();

	public void mockLocalRequest(String path, Map<String, String> paramsToCheck, byte[] resultToSend) {
		mockLocalRequest(path, paramsToCheck, 200, resultToSend);
	}
	
	public void mockLocalRequest(String path, Map<String, String> paramsToCheck, int status, byte[] resultToSend) {
		if (!localRequests.containsKey(path)) {
			localRequests.put(path, new ArrayList<LocalRequestData>());
		}

		localRequests.get(path).add(new LocalRequestData(paramsToCheck,status, resultToSend));
	}
	public void validateNoMoreLocalRequests() {
		for(Map.Entry<String,List<LocalRequestData>> localRequest : localRequests.entrySet()) {
			Assert.assertTrue("Not all requests for " + localRequest.getKey() + " have been called" , localRequest.getValue().isEmpty());
		}
	}
	
	private final class LocalRequestAnswer implements Answer<LocalResponse> {

		@Override
		public LocalResponse answer(InvocationOnMock invocation) throws Throwable {
			LocalRequest request = invocation.getArgument(0);

			if (!localRequests.containsKey(request.getPath())) {
				throw new IllegalStateException("No local request for '" + request.getPath() + "' expected");
			}
			
			if(localRequests.get(request.getPath()).isEmpty()) {
				throw new IllegalStateException("No more local requests for '" + request.getPath() + "' expected");
			}
			
			final LocalRequestData requestData = localRequests.get(request.getPath()).remove(0);
			for (Map.Entry<String, String> param : requestData.getParamsToCheck().entrySet()) {
				Assert.assertEquals("Local request parameter different", param.getValue(),
						request.getParam(param.getKey()));
			}

			return requestData;
		}
	}

	private final class LocalRequestData implements LocalResponse {
		private final Map<String, String> paramsToCheck;
		private final int status;
		private final byte[] resultToSend;

		public LocalRequestData(Map<String, String> paramsToCheck, int status, byte[] resultToSend) {
			super();
			this.paramsToCheck = paramsToCheck;
			this.status = status;
			this.resultToSend = resultToSend;
		}

		public Map<String, String> getParamsToCheck() {
			return paramsToCheck;
		}

		@Override
		public int getStatus() {
			return status;
		}

		@Override
		public String getMediaType() {
			return "application/json";
		}

		@Override
		public byte[] getBytes() {
			return resultToSend;
		}


		@Override
		public Collection<String> getHeaderNames() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getHeader(String name) {
			throw new UnsupportedOperationException();
		}
	}

}

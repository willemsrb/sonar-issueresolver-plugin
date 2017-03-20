package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Issues;

public class ExportActionTest {

	@Test
	public void test() throws IOException {
		// Request
		final MockRequest request = new MockRequest();
		request.setParam("projectKey", "my-project-key");

		// Local call (first page)
		final Map<String,String> localRequestParamsToCheckPageOne = new HashMap<>();
		localRequestParamsToCheckPageOne.put("projectKeys","my-project-key");
		localRequestParamsToCheckPageOne.put("additionalFields","comments");
		localRequestParamsToCheckPageOne.put("statuses","RESOLVED");
		localRequestParamsToCheckPageOne.put("resolutions","FALSE-POSITIVE,WONTFIX");
		localRequestParamsToCheckPageOne.put("p","1");
		localRequestParamsToCheckPageOne.put("ps", "100");
		
		final Issues.SearchWsResponse.Builder localRequestResponsePageOne = Issues.SearchWsResponse.newBuilder();
		localRequestResponsePageOne.setPaging(Common.Paging.newBuilder().setTotal(2).setPageIndex(1).setPageSize(1));
		localRequestResponsePageOne.addIssues(Issues.Issue.newBuilder().setKey("AVrdUwSCGyMCMhQpQjBw")
				.setRule("xml:IllegalTabCheck").setComponent("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:pom.xml")
				.setTextRange(Common.TextRange.newBuilder().setStartLine(4).setStartOffset(0))
				.setResolution("FALSE-POSITIVE")
				.setStatus("RESOLVED")
				);
		request.mockLocalRequest("api/issues/search", localRequestParamsToCheckPageOne, localRequestResponsePageOne.build().toByteArray());

		// Local call (second page)
		final Map<String,String> localRequestParamsToCheckPageTwo = new HashMap<>();
		localRequestParamsToCheckPageTwo.put("projectKeys","my-project-key");
		localRequestParamsToCheckPageTwo.put("additionalFields","comments");
		localRequestParamsToCheckPageTwo.put("statuses","RESOLVED");
		localRequestParamsToCheckPageTwo.put("resolutions","FALSE-POSITIVE,WONTFIX");
		localRequestParamsToCheckPageTwo.put("p","2");
		localRequestParamsToCheckPageTwo.put("ps", "1");

		final Issues.SearchWsResponse.Builder localRequestResponsePageTwo = Issues.SearchWsResponse.newBuilder();
		localRequestResponsePageTwo.setPaging(Common.Paging.newBuilder().setTotal(2).setPageIndex(2).setPageSize(1));
		localRequestResponsePageTwo.addIssues(Issues.Issue.newBuilder().setKey("AVrdUwS9GyMCMhQpQjBx")
				.setRule("squid:S3776").setComponent("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
				.setTextRange(Common.TextRange.newBuilder().setStartLine(64).setStartOffset(16))
				.setResolution("WONTFIX")
				.setStatus("RESOLVED")
				);
		request.mockLocalRequest("api/issues/search", localRequestParamsToCheckPageTwo, localRequestResponsePageTwo.build().toByteArray());
		
		// Response
		final MockResponse response = new MockResponse();

		// Execute
		final ExportAction subject = new ExportAction();
		subject.handle(request, response);

		// Validate
		final String result = new String(response.result(), "UTF-8");
		Assert.assertEquals("{\"version\":1,\"issues\":[{\"rule\":\"xml:IllegalTabCheck\",\"component\":\"nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:pom.xml\",\"start\":4,\"offset\":0,\"resolution\":\"falsepositive\",\"comments\":[]},{\"rule\":\"squid:S3776\",\"component\":\"nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java\",\"start\":64,\"offset\":16,\"resolution\":\"wontfix\",\"comments\":[]}]}", result);
	}
}

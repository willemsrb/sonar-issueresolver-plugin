package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.internal.apachecommons.io.IOUtils;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Issues;

public class ImportActionTest {

	@Test
	public void test() throws IOException {
		// Request
		final MockRequest request = new MockRequest();
		request.setParam("projectKey", "my-project-key");
		request.setParam("preview", "false");
		request.setPart("data",
				new ByteArrayInputStream(
						IOUtils.toString(ImportActionTest.class.getResourceAsStream("ImportActionTest-request.json"))
								.getBytes("UTF-8")),
				"resolved-issues.json");

		// Local call (first page)
		final Map<String, String> localRequestParamsToCheckPageOne = new HashMap<>();
		localRequestParamsToCheckPageOne.put("projectKeys", "my-project-key");
		localRequestParamsToCheckPageOne.put("additionalFields", "comments");
		localRequestParamsToCheckPageOne.put("p", "1");
		localRequestParamsToCheckPageOne.put("ps", "100");

		final Issues.SearchWsResponse.Builder localRequestResponsePageOne = Issues.SearchWsResponse.newBuilder();
		localRequestResponsePageOne.setPaging(Common.Paging.newBuilder().setTotal(3).setPageIndex(1).setPageSize(2));
		localRequestResponsePageOne
				.addIssues(Issues.Issue.newBuilder().setKey("TotaleAndereKey1").setRule("xml:IllegalTabCheck")
						.setComponent("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:pom.xml")
						.setTextRange(Common.TextRange.newBuilder().setStartLine(4).setStartOffset(0))
						.setResolution("FALSE-POSITIVE").setStatus("RESOLVED"));
		localRequestResponsePageOne
		.addIssues(Issues.Issue.newBuilder().setKey("TotaleAndereKey1b").setRule("xml:IllegalTabCheck")
				.setComponent("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:pom.xml")
				.setTextRange(Common.TextRange.newBuilder().setStartLine(14).setStartOffset(0))
				.setStatus("OPEN"));

		request.mockLocalRequest("api/issues/search", localRequestParamsToCheckPageOne,
				localRequestResponsePageOne.build().toByteArray());

		// Local call (second page)
		final Map<String, String> localRequestParamsToCheckPageTwo = new HashMap<>();
		localRequestParamsToCheckPageTwo.put("projectKeys", "my-project-key");
		localRequestParamsToCheckPageTwo.put("additionalFields", "comments");
		localRequestParamsToCheckPageTwo.put("p", "2");
		localRequestParamsToCheckPageTwo.put("ps", "2");

		final Issues.SearchWsResponse.Builder localRequestResponsePageTwo = Issues.SearchWsResponse.newBuilder();
		localRequestResponsePageTwo.setPaging(Common.Paging.newBuilder().setTotal(2).setPageIndex(2).setPageSize(1));
		localRequestResponsePageTwo
				.addIssues(Issues.Issue.newBuilder().setKey("TotaleAndereKey2").setRule("squid:S3776")
						.setComponent(
								"nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
						.setTextRange(Common.TextRange.newBuilder().setStartLine(64).setStartOffset(16))
						.setComments(Issues.Comments.newBuilder()
								.addComments(Issues.Comment.newBuilder().setMarkdown("Comment one")))
						.setStatus("OPEN"));
		request.mockLocalRequest("api/issues/search", localRequestParamsToCheckPageTwo,
				localRequestResponsePageTwo.build().toByteArray());

		// Local call (resolve (second) issue)
		final Map<String, String> localRequestParamsResolveIssue = new HashMap<>();
		localRequestParamsResolveIssue.put("issue", "TotaleAndereKey2");
		localRequestParamsResolveIssue.put("transition", "wontfix");

		Issues.Operation.Builder localRequestResponseResolveIssue = Issues.Operation.newBuilder();
		request.mockLocalRequest("api/issues/do_transition", localRequestParamsResolveIssue,
				localRequestResponseResolveIssue.build().toByteArray());

		// Local call (add (second) comment)
		final Map<String, String> localRequestParamsAddComment = new HashMap<>();
		localRequestParamsAddComment.put("issue", "TotaleAndereKey2");
		localRequestParamsAddComment.put("text", "Comment two");

		Issues.Operation.Builder localRequestResponseAddComment = Issues.Operation.newBuilder();
		request.mockLocalRequest("api/issues/add_comment", localRequestParamsAddComment,
				localRequestResponseAddComment.build().toByteArray());

		// Response
		final MockResponse response = new MockResponse();

		// Execute
		final ImportAction subject = new ImportAction();
		subject.handle(request, response);

		// Validate
		final String result = new String(response.result(), "UTF-8");
		Assert.assertEquals(
				"{\"preview\":false,\"issues\":4,\"duplicateKeys\":1,\"unmatchedIssues\":1,\"unresolvedIssues\":1,\"resolvedIssues\":1}",
				result);
	}
}

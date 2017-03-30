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
				new ByteArrayInputStream(removeInvalidJsonComments(
						IOUtils.toString(ImportActionTest.class.getResourceAsStream("ImportActionTest-request.json")))
								.getBytes("UTF-8")),
				"resolved-issues.json");

		// Local call - Search - First page
		final Map<String, String> localRequestParamsToCheckPageOne = new HashMap<>();
		localRequestParamsToCheckPageOne.put("projectKeys", "my-project-key");
		localRequestParamsToCheckPageOne.put("additionalFields", "comments");
		localRequestParamsToCheckPageOne.put("p", "1");
		localRequestParamsToCheckPageOne.put("ps", "100");

		final Issues.SearchWsResponse.Builder localRequestResponsePageOne = Issues.SearchWsResponse.newBuilder();
		localRequestResponsePageOne.setPaging(Common.Paging.newBuilder().setTotal(9).setPageIndex(1).setPageSize(6));
		// MATCHED ISSUE (NO ACTION)
		localRequestResponsePageOne
				.addIssues(Issues.Issue.newBuilder().setKey("TotaleAndereKey4").setRule("xml:IllegalTabCheck")
						.setComponent("nl.future-edge.sonarqube.plugins:myBranch:sonar-issueresolver-plugin:pom.xml")
						.setTextRange(Common.TextRange.newBuilder().setStartLine(4).setStartOffset(0))
						.setResolution("FALSE-POSITIVE").setStatus("RESOLVED"));
		// UNMATCHED ISSUE
		localRequestResponsePageOne.addIssues(Issues.Issue.newBuilder().setKey("TotaleAndereKey14")
				.setRule("xml:IllegalTabCheck")
				.setComponent("nl.future-edge.sonarqube.plugins:myBranch:sonar-issueresolver-plugin:pom.xml")
				.setTextRange(Common.TextRange.newBuilder().setStartLine(14).setStartOffset(0)).setStatus("OPEN"));
		// MATCHED ISSUE (CONFIRM; NO ASSIGN)
		localRequestResponsePageOne
				.addIssues(Issues.Issue.newBuilder().setKey("TotaleAndereKey55").setRule("squid:S3776")
						.setComponent(
								"nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
						.setTextRange(Common.TextRange.newBuilder().setStartLine(55).setStartOffset(16))
						.setAssignee("admin").setStatus("OPEN"));
		// MATCHED ISSUE (UNCONFIRM)
		localRequestResponsePageOne
				.addIssues(Issues.Issue.newBuilder().setKey("TotaleAndereKey56").setRule("squid:S3776")
						.setComponent(
								"nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
						.setTextRange(Common.TextRange.newBuilder().setStartLine(56).setStartOffset(16))
						.setAssignee("admin").setStatus("CONFIRMED"));
		// MATCHED ISSUE (REOPEN)
		localRequestResponsePageOne.addIssues(Issues.Issue.newBuilder().setKey("TotaleAndereKey57")
				.setRule("squid:S3776")
				.setComponent(
						"nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
				.setTextRange(Common.TextRange.newBuilder().setStartLine(57).setStartOffset(16)).setStatus("RESOLVED"));
		// MATCHED ISSUE (RESOLVE FIXED)
		localRequestResponsePageOne.addIssues(Issues.Issue.newBuilder().setKey("TotaleAndereKey58")
				.setRule("squid:S3776")
				.setComponent(
						"nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
				.setTextRange(Common.TextRange.newBuilder().setStartLine(58).setStartOffset(16)).setStatus("OPEN"));
		localRequestResponsePageOne.addComponents(Issues.Component.newBuilder()
				.setKey("nl.future-edge.sonarqube.plugins:myBranch:sonar-issueresolver-plugin:pom.xml")
				.setLongName("pom.xml"));
		localRequestResponsePageOne.addComponents(Issues.Component.newBuilder()
				.setKey("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
				.setLongName("src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java"));
		request.mockLocalRequest("api/issues/search", localRequestParamsToCheckPageOne,
				localRequestResponsePageOne.build().toByteArray());

		// Local call - Search - Second page
		final Map<String, String> localRequestParamsToCheckPageTwo = new HashMap<>();
		localRequestParamsToCheckPageTwo.put("projectKeys", "my-project-key");
		localRequestParamsToCheckPageTwo.put("additionalFields", "comments");
		localRequestParamsToCheckPageTwo.put("p", "2");
		localRequestParamsToCheckPageTwo.put("ps", "6");

		final Issues.SearchWsResponse.Builder localRequestResponsePageTwo = Issues.SearchWsResponse.newBuilder();
		localRequestResponsePageTwo.setPaging(Common.Paging.newBuilder().setTotal(9).setPageIndex(2).setPageSize(6));
		// MATCHED ISSUE (RESOLVE FALSE-POSITIVE)
		localRequestResponsePageTwo.addIssues(Issues.Issue.newBuilder().setKey("TotaleAndereKey59")
				.setRule("squid:S3776")
				.setComponent(
						"nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
				.setTextRange(Common.TextRange.newBuilder().setStartLine(59).setStartOffset(16)).setStatus("REOPENED"));
		// MATCHED ISSUE (RESOLVE WONTFIX; ADD COMMENT)
		localRequestResponsePageTwo
				.addIssues(Issues.Issue.newBuilder().setKey("TotaleAndereKey60").setRule("squid:S3776")
						.setComponent(
								"nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
						.setTextRange(Common.TextRange.newBuilder().setStartLine(60).setStartOffset(16))
						.setComments(Issues.Comments.newBuilder()
								.addComments(Issues.Comment.newBuilder().setMarkdown("Comment one")))
						.setStatus("CONFIRMED"));
		// MATCHED ISSUE (MATCH FAILURE)
		localRequestResponsePageTwo
				.addIssues(Issues.Issue.newBuilder().setKey("TotaleAndereKey61").setRule("squid:S3776")
						.setComponent(
								"nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
						.setTextRange(Common.TextRange.newBuilder().setStartLine(61).setStartOffset(16))
						.setStatus("RESOLVED").setResolution("WONTFIX"));

		localRequestResponsePageTwo.addComponents(Issues.Component.newBuilder()
				.setKey("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
				.setLongName("src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java"));
		request.mockLocalRequest("api/issues/search", localRequestParamsToCheckPageTwo,
				localRequestResponsePageTwo.build().toByteArray());

		// Local call - MATCHED ISSUE (NO ACTION; ASSIGN) - Assign
		final Map<String, String> localRequestParamsAssign4 = new HashMap<>();
		localRequestParamsAssign4.put("issue", "TotaleAndereKey4");
		localRequestParamsAssign4.put("assignee", "admin");

		request.mockLocalRequest("api/issues/assign", localRequestParamsAssign4,
				Issues.Operation.newBuilder().build().toByteArray());

		// Local call - MATCHED ISSUE (CONFIRM; NO ASSIGN)
		final Map<String, String> localRequestParamsTransition55 = new HashMap<>();
		localRequestParamsTransition55.put("issue", "TotaleAndereKey55");
		localRequestParamsTransition55.put("transition", "confirm");

		request.mockLocalRequest("api/issues/do_transition", localRequestParamsTransition55,
				Issues.Operation.newBuilder().build().toByteArray());

		// Local call - MATCHED ISSUE (UNCONFIRM; REASSIGN)
		final Map<String, String> localRequestParamsTransition56 = new HashMap<>();
		localRequestParamsTransition56.put("issue", "TotaleAndereKey56");
		localRequestParamsTransition56.put("transition", "unconfirm");

		request.mockLocalRequest("api/issues/do_transition", localRequestParamsTransition56,
				Issues.Operation.newBuilder().build().toByteArray());

		final Map<String, String> localRequestParamsAssign56 = new HashMap<>();
		localRequestParamsAssign56.put("issue", "TotaleAndereKey56");
		localRequestParamsAssign56.put("assignee", "unknown");

		request.mockLocalRequest("api/issues/assign", localRequestParamsAssign56, 400,
				Issues.Operation.newBuilder().build().toByteArray());

		// Local call - MATCHED ISSUE (REOPEN)
		final Map<String, String> localRequestParamsTransition57 = new HashMap<>();
		localRequestParamsTransition57.put("issue", "TotaleAndereKey57");
		localRequestParamsTransition57.put("transition", "reopen");

		request.mockLocalRequest("api/issues/do_transition", localRequestParamsTransition57, 400,
				Issues.Operation.newBuilder().build().toByteArray());

		// Local call - MATCHED ISSUE (RESOLVE FIXED)
		final Map<String, String> localRequestParamsTransition58 = new HashMap<>();
		localRequestParamsTransition58.put("issue", "TotaleAndereKey58");
		localRequestParamsTransition58.put("transition", "resolve");

		request.mockLocalRequest("api/issues/do_transition", localRequestParamsTransition58,
				Issues.Operation.newBuilder().build().toByteArray());

		// Local call - MATCHED ISSUE (RESOLVE FALSE-POSITIVE)
		final Map<String, String> localRequestParamsTransition59 = new HashMap<>();
		localRequestParamsTransition59.put("issue", "TotaleAndereKey59");
		localRequestParamsTransition59.put("transition", "falsepositive");

		request.mockLocalRequest("api/issues/do_transition", localRequestParamsTransition59,
				Issues.Operation.newBuilder().build().toByteArray());

		// Local call - MATCHED ISSUE (RESOLVE WONTFIX; ADD COMMENT)
		final Map<String, String> localRequestParamsTransition60 = new HashMap<>();
		localRequestParamsTransition60.put("issue", "TotaleAndereKey60");
		localRequestParamsTransition60.put("transition", "wontfix");

		request.mockLocalRequest("api/issues/do_transition", localRequestParamsTransition60,
				Issues.Operation.newBuilder().build().toByteArray());

		final Map<String, String> localRequestParamsAddComment60a = new HashMap<>();
		localRequestParamsAddComment60a.put("issue", "TotaleAndereKey60");
		localRequestParamsAddComment60a.put("text", "Comment two");

		request.mockLocalRequest("api/issues/add_comment", localRequestParamsAddComment60a,
				Issues.Operation.newBuilder().build().toByteArray());

		final Map<String, String> localRequestParamsAddComment60b = new HashMap<>();
		localRequestParamsAddComment60b.put("issue", "TotaleAndereKey60");
		localRequestParamsAddComment60b.put("text", "Comment three");

		request.mockLocalRequest("api/issues/add_comment", localRequestParamsAddComment60b, 400,
				Issues.Operation.newBuilder().build().toByteArray());

		// Response
		final MockResponse response = new MockResponse();

		// Execute
		final ImportAction subject = new ImportAction();
		subject.handle(request, response);

		request.validateNoMoreLocalRequests();

		// Validate
		final String result = new String(response.result(), "UTF-8");
		Assert.assertEquals(
				"{\"preview\":false,\"issues\":10,\"duplicateKeys\":1,"
						+ "\"matchedIssues\":8,\"matchFailures\":[\"Could not determine transition for issue with key 'TotaleAndereKey61'; current status is 'RESOLVED' and resolution is 'WONTFIX'; wanted status is 'RESOLVED' and resolution is 'FALSE-POSITIVE'\"],"
						+ "\"transitionedIssues\":6,\"transitionFailures\":[\"Could not transition issue with key 'TotaleAndereKey57' using transition 'reopen'\"],"
						+ "\"assignedIssues\":2,\"assignFailures\":[\"Could not assign issue with key 'TotaleAndereKey56' to user 'unknown'\"],"
						+ "\"commentedIssues\":1,\"commentFailures\":[\"Could not add comment to issue with key 'TotaleAndereKey60'\"]}",
				result);
	}

	private String removeInvalidJsonComments(String json) {
		String result = json.replaceAll("(?m)//.*$", "");
		System.out.println(result);
		return result;
	}

	@Test(expected=IllegalStateException.class)
	public void invalidData() throws IOException {
		final MockRequest request = new MockRequest();
		request.setParam("projectKey", "my-project-key");
		request.setParam("preview", "false");
		request.setPart("data", new ByteArrayInputStream("{\"version\":1}".getBytes("UTF-8")), "resolved-issues.json");

		// Response
		final MockResponse response = new MockResponse();

		// Execute
		final ImportAction subject = new ImportAction();
		subject.handle(request, response);
	}
	
	@Test(expected=IllegalStateException.class)
	public void invalidVersion() throws IOException {
		final MockRequest request = new MockRequest();
		request.setParam("projectKey", "my-project-key");
		request.setParam("preview", "false");
		request.setPart("data", new ByteArrayInputStream("{\"version\":0}".getBytes("UTF-8")), "resolved-issues.json");

		// Response
		final MockResponse response = new MockResponse();

		// Execute
		final ImportAction subject = new ImportAction();
		subject.handle(request, response);
	}
}

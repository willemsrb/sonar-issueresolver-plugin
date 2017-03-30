package nl.futureedge.sonar.plugin.issueresolver.helper;

import java.util.Arrays;
import java.util.Collections;

import org.sonarqube.ws.client.issue.SearchWsRequest;

/**
 * Search functionality.
 */
public final class SearchHelper {

	private SearchHelper() {
	}

	/**
	 * Create search request for resolved issues.
	 * 
	 * @param projectKey
	 *            project key
	 * @return search request
	 */
	public static SearchWsRequest findIssuesForExport(final String projectKey) {
		final SearchWsRequest searchIssuesRequest = new SearchWsRequest();
		searchIssuesRequest.setProjectKeys(Collections.singletonList(projectKey));
		searchIssuesRequest.setAdditionalFields(Collections.singletonList("comments"));
		searchIssuesRequest.setStatuses(Arrays.asList("CONFIRMED", "REOPENED", "RESOLVED"));
		searchIssuesRequest.setPage(1);
		searchIssuesRequest.setPageSize(100);
		return searchIssuesRequest;
	}

	public static SearchWsRequest findIssuesForImport(final String projectKey) {
		final SearchWsRequest searchIssuesRequest = new SearchWsRequest();
		searchIssuesRequest.setProjectKeys(Collections.singletonList(projectKey));
		searchIssuesRequest.setAdditionalFields(Collections.singletonList("comments"));
		searchIssuesRequest.setPage(1);
		searchIssuesRequest.setPageSize(100);
		return searchIssuesRequest;
	}
	
}

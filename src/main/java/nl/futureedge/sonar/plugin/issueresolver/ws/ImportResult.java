package nl.futureedge.sonar.plugin.issueresolver.ws;

import org.sonar.api.utils.text.JsonWriter;

/**
 * Import result.
 */
public final class ImportResult {

	private boolean preview = false;
	private int issues = 0;
	private int duplicateKeys = 0;
	private int unmatchedIssues = 0;
	private int unresolvedIssues = 0;
	private int resolvedIssues = 0;

	public void setPreview(final boolean preview) {
		this.preview = preview;
	}

	public void registerIssue() {
		issues++;
	}

	public int getIssues() {
		return issues;
	}

	public void registerDuplicateKey() {
		duplicateKeys++;
	}

	public int getDuplicateKeys() {
		return duplicateKeys;
	}

	public void registerUnmatchedIssues(final int unmatchedIssues) {
		this.unmatchedIssues = unmatchedIssues;
	}

	public int getUnmatchedIssues() {
		return unmatchedIssues;
	}

	public void registerUnresolvedIssue() {
		unresolvedIssues++;
	}

	public int getUnresolvedIssues() {
		return unresolvedIssues;
	}

	public void registerResolvedIssue() {
		resolvedIssues++;
	}

	public int getResolvedIssues() {
		return resolvedIssues;
	}

	public void write(final JsonWriter writer) {
		writer.beginObject();
		writer.prop("preview", preview);
		writer.prop("issues", issues);
		writer.prop("duplicateKeys", duplicateKeys);
		writer.prop("unmatchedIssues", unmatchedIssues);
		writer.prop("unresolvedIssues", unresolvedIssues);
		writer.prop("resolvedIssues", resolvedIssues);
		writer.endObject();
	}
}
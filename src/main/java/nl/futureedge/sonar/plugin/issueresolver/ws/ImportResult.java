package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.utils.text.JsonWriter;

/**
 * Import result.
 */
public final class ImportResult {

	private boolean preview = false;
	private int issues = 0;
	private int duplicateKeys = 0;
	private int matchedIssues = 0;
	private List<String> matchFailures = new ArrayList<>();
	private int transitionedIssues = 0;
	private List<String> transitionFailures = new ArrayList<>();
	private int assignedIssues = 0;
	private List<String> assignFailures = new ArrayList<>();
	private int commentedIssues = 0;
	private List<String> commentFailures = new ArrayList<>();

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

	public void registerMatchedIssue() {
		matchedIssues++;
	}

	public int getMatchedIssues() {
		return matchedIssues;
	}

	public void registerMatchFailure(String failure) {
		matchFailures.add(failure);
	}

	public void registerTransitionedIssue() {
		transitionedIssues++;
	}

	public int getTransitionedIssues() {
		return transitionedIssues;
	}

	public void registerTransitionFailure(String failure) {
		transitionFailures.add(failure);
	}

	public void registerAssignedIssue() {
		assignedIssues++;
	}

	public int getAssignedIssues() {
		return assignedIssues;
	}

	public void registerAssignFailure(String failure) {
		assignFailures.add(failure);
	}

	public void registerCommentedIssue() {
		commentedIssues++;
	}

	public int getCommentedIssues() {
		return commentedIssues;
	}

	public void registerCommentFailure(String failure) {
		commentFailures.add(failure);
	}

	public void write(final JsonWriter writer) {
		writer.beginObject();
		writer.prop("preview", preview);
		writer.prop("issues", issues);
		writer.prop("duplicateKeys", duplicateKeys);
		writer.prop("matchedIssues", matchedIssues);
		writer.name("matchFailures");
		writer.beginArray();
		writer.values(matchFailures);
		writer.endArray();		
		writer.prop("transitionedIssues", transitionedIssues);
		writer.name("transitionFailures");
		writer.beginArray();
		writer.values(transitionFailures);
		writer.endArray();
		writer.prop("assignedIssues", assignedIssues);
		writer.name("assignFailures");
		writer.beginArray();
		writer.values(assignFailures);
		writer.endArray();
		writer.prop("commentedIssues", commentedIssues);
		writer.name("commentFailures");
		writer.beginArray();
		writer.values(commentFailures);
		writer.endArray();
		writer.endObject();
	}
}
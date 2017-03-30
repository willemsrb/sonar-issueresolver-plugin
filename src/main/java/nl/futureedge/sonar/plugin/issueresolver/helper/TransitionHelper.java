package nl.futureedge.sonar.plugin.issueresolver.helper;

import java.util.Objects;

import org.sonar.api.issue.Issue;

/**
 * Transition helper.
 */
public final class TransitionHelper {

	private static final String OPEN = Issue.STATUS_OPEN;
	private static final String REOPENED = Issue.STATUS_REOPENED;
	private static final String CONFIRMED = Issue.STATUS_CONFIRMED;
	private static final String RESOLVED = Issue.STATUS_RESOLVED;

	private static final String FIXED = Issue.RESOLUTION_FIXED;
	private static final String FALSE_POSITIVE = Issue.RESOLUTION_FALSE_POSITIVE;
	private static final String WONT_FIX = Issue.RESOLUTION_WONT_FIX;

	private TransitionHelper() {
	}

	public static boolean noAction(final String currentStatus, final String currentResolution,
			final String wantedStatus, final String wantedResolution) {
		return noActionStatus(currentStatus, wantedStatus) && noActionResolution(currentResolution, wantedResolution);
	}

	private static boolean noActionStatus(final String currentStatus, final String wantedStatus) {
		final String current = REOPENED.equals(currentStatus) ? OPEN : currentStatus;
		final String wanted = REOPENED.equals(wantedStatus) ? OPEN : wantedStatus;

		return Objects.equals(current, wanted);
	}

	private static boolean noActionResolution(final String currentResolution, final String wantedResolution) {
		return Objects.equals(currentResolution, wantedResolution);
	}

	public static boolean shouldConfirm(final String currentStatus, final String wantedStatus) {
		return CONFIRMED.equals(wantedStatus) && (OPEN.equals(currentStatus) || REOPENED.equals(currentStatus));
	}

	public static boolean shouldUnconfirm(final String currentStatus, final String wantedStatus) {
		return REOPENED.equals(wantedStatus) && CONFIRMED.equals(currentStatus);
	}

	public static boolean shouldReopen(final String currentStatus, final String wantedStatus) {
		return REOPENED.equals(wantedStatus) && RESOLVED.equals(currentStatus);
	}

	private static boolean shouldResolve(final String currentStatus, final String wantedStatus) {
		return RESOLVED.equals(wantedStatus)
				&& (OPEN.equals(currentStatus) || REOPENED.equals(currentStatus) || CONFIRMED.equals(currentStatus));
	}

	public static boolean shouldResolveFixed(final String currentStatus, final String wantedStatus,
			final String wantedResolution) {
		return shouldResolve(currentStatus, wantedStatus) && FIXED.equals(wantedResolution);
	}

	public static boolean shouldResolveFalsePositive(final String currentStatus, final String wantedStatus,
			final String wantedResolution) {
		return shouldResolve(currentStatus, wantedStatus) && FALSE_POSITIVE.equals(wantedResolution);
	}

	public static boolean shouldReopen(final String currentStatus, final String wantedStatus,
			final String wantedResolution) {
		return shouldResolve(currentStatus, wantedStatus) && WONT_FIX.equals(wantedResolution);
	}

}

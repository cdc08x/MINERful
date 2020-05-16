package minerful.logmaker.params;

/**
 * The criterion according to which traces should be sorted in the event log
 */
public enum SortingCriterion {
	/**
	 * Sort by the timestamp of the first event in the trace, ascending
	 */
	FIRST_EVENT_ASC,
	/**
	 * Sort by the timestamp of the last event in the trace, ascending
	 */
	LAST_EVENT_ASC,
	/**
	 * Sort by the length of the trace (in the number of events), ascending
	 */
	TRACE_LENGTH_ASC,
	/**
	 * Sort by the length of the trace (in the number of events), descending
	 */
	TRACE_LENGTH_DESC
}
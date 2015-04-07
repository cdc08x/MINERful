package minerful.logparser;

import minerful.concept.Event;

public interface LogEventParser {

	public Character evtIdentifier();
	
	public Event getEvent();

}
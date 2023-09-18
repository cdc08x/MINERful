package minerful.automaton.encdec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import minerful.io.encdec.TaskCharEncoderDecoder;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

public class AutomatonDotPrinter extends AbstractAutomatonDotPrinter {
	// TODO: this has to be changed into something read from a properties file!
	public static final boolean RAW_DOT = true;
	
	public AutomatonDotPrinter(NavigableMap<Character, String> translationMap) {
		super(translationMap);
	}

	public String printDot(Automaton automaton) {
		if (!RAW_DOT)
			return this.printDot(automaton, null);
		else
			return this.printRawDot(automaton);
	}

	public String printDot(Automaton automaton, Character emphasizedActivity) {
		if (RAW_DOT)
			return this.printRawDot(automaton, emphasizedActivity);

		StringBuilder sBuilder = new StringBuilder();
		
		// Put the header of the file
		sBuilder.append(DOT_INIT);
		
		// Start from the init state
		Set<State> states = automaton.getStates();
		State initState = automaton.getInitialState();
		
		Map<State, String> statesIdMap = defineStateNodesIds(states);
		for (State state : states) {
			// If this is the init state, start the automaton with it
			if (state.isAccept()) {
				sBuilder.append(String.format(DOT_TEMPLATES.getProperty("acceptingStateNodeTemplate"), statesIdMap.get(state)));
			}
			if (state.equals(initState)) {
				sBuilder.append(String.format(DOT_TEMPLATES.getProperty("startTemplate"), statesIdMap.get(state)));
			}
		}
		for (State state : states) {
			sBuilder.append(printDot(state, statesIdMap, emphasizedActivity));
		}

		sBuilder.append(DOT_END);
		return sBuilder.toString();
	}
	
	private String printDot(State state, Map<State, String> statesIdMap, Character emphasizedActivity) {
		StringBuilder sBuilder = new StringBuilder();
		boolean goForNot = false, goForNotRequiredNow = false;
		NavigableMap<State, Collection<Character>> howToGetThere = new TreeMap<State, Collection<Character>>();
		String stateNodeName = statesIdMap.get(state);
		Collection<Character>
			outGoingWays = new TreeSet<Character>(),
			outGoingWaysToOneState = null,
			howNotToGetThere = null;
		EmphasizableLabelPojo eLaPo = null;
		
		// Get the output transitions, and labels.
		
		for (Transition trans : state.getTransitions()) {
			if (!howToGetThere.containsKey(trans.getDest()))
				howToGetThere.put(trans.getDest(), new ArrayList<Character>());
			outGoingWaysToOneState = this.transMap.headMap(trans.getMax(),true).tailMap(trans.getMin(),true).keySet();
			outGoingWays.addAll(outGoingWaysToOneState);
			howToGetThere.put(trans.getDest(), outGoingWaysToOneState);
		}

		Set<State> reachableStates = howToGetThere.keySet();
		String
			actiNodeName = null,
			targetStateNodeName = null, blaurghStateName = null,
			compensatingStateName = null;
		int activityCounter = 0;
		for (State reachableState : reachableStates) {
			goForNotRequiredNow = false;
			targetStateNodeName = statesIdMap.get(reachableState);

			// Then, for each reached state, check numbers: how many are the outgoing connections?
			// If they are more than half of the alphabet size, start considering the "not" transitions
			if (howToGetThere.get(reachableState).size() > (this.getAlphabetSize() / 2)) {
				goForNotRequiredNow = true;
			} else {
				goForNotRequiredNow = false;
			}
			goForNot = goForNot || goForNotRequiredNow;
			
			
			// Add the new activity node
			if (goForNotRequiredNow) {
			} else {
				eLaPo = buildActivityLabel(howToGetThere.get(reachableState), emphasizedActivity);
				actiNodeName = String.format(DOT_TEMPLATES.getProperty("activityNodeNameTemplate"), stateNodeName, ++activityCounter);
				sBuilder.append(String.format(DOT_TEMPLATES.getProperty("activityNodeTemplate"), actiNodeName, eLaPo.label));
				if (eLaPo.emphasized) {
					sBuilder.append(String.format(DOT_TEMPLATES.getProperty("emphasizedTransitionTemplate"), stateNodeName, actiNodeName, targetStateNodeName));
				}
				else {
					sBuilder.append(String.format(DOT_TEMPLATES.getProperty("transitionTemplate"), stateNodeName, actiNodeName, targetStateNodeName));
				}
			}
		}
		
		if (goForNot) {
			// good transitions (the remaining guys) are thus labelled with "*"
			actiNodeName = String.format(DOT_TEMPLATES.getProperty("activityNodeNameTemplate"), stateNodeName, ++activityCounter);
			compensatingStateName = String.format(DOT_TEMPLATES.getProperty("compensatingActivityWrapperStateNodeTemplate"), stateNodeName, actiNodeName);
			sBuilder.append(String.format(DOT_TEMPLATES.getProperty("compensationForNotTransitionTemplate"), stateNodeName, compensatingStateName, targetStateNodeName));

			howNotToGetThere = this.makeItHowNotToGetThere(outGoingWays);
			
			if (howNotToGetThere.size() > 0) {
				eLaPo = buildActivityLabel(howNotToGetThere, emphasizedActivity);
				blaurghStateName = String.format(DOT_TEMPLATES.getProperty("blaurghStateNodeTemplate"), stateNodeName);
				// "not" transitions lead to blaurgh states
				actiNodeName = String.format(DOT_TEMPLATES.getProperty("activityNodeNameTemplate"), stateNodeName, ++activityCounter);
				sBuilder.append(String.format(DOT_TEMPLATES.getProperty("activityNodeTemplate"), actiNodeName, eLaPo.label));
				if (eLaPo.emphasized)
					sBuilder.append(String.format(DOT_TEMPLATES.getProperty("emphasizedNotTransitionTemplate"), stateNodeName, actiNodeName, blaurghStateName));
				else
					sBuilder.append(String.format(DOT_TEMPLATES.getProperty("notTransitionTemplate"), stateNodeName, actiNodeName, blaurghStateName));
			}
		}
		
		return sBuilder.toString();
	}
	
	private EmphasizableLabelPojo buildActivityLabel(Collection<Character> waysList, Character emphasizedActivity) {
		StringBuilder actiSBuilder = new StringBuilder();
		
		Iterator<Character> howToIterator = waysList.iterator();
		Character way = howToIterator.next();
		actiSBuilder.append(String.format(DOT_TEMPLATES.getProperty("activityLabelTemplateStarter"), this.transMap.get(way)));
		boolean emphasizeIt = false;
		if (emphasizedActivity != null && way.equals(emphasizedActivity)) {
			emphasizeIt = true;
		}
		while (howToIterator.hasNext()) {
			way = howToIterator.next();
			if (emphasizedActivity != null && !emphasizeIt && way.equals(emphasizedActivity)) {
				emphasizeIt = true;
			}
			actiSBuilder.append(String.format(DOT_TEMPLATES.getProperty("activityLabelTemplate"), this.transMap.get(way)));
		}
		
		return new EmphasizableLabelPojo(actiSBuilder.toString().trim(), emphasizeIt);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public String printRawDot(Automaton automaton) {
		return this.printRawDot(automaton, null);
	}

	public String printRawDot(Automaton automaton, Character emphasizedActivity) {
		String dotString = automaton.toDot();
		return this.replaceIdentifiersWithActivityNamesInDotAutomaton(dotString, emphasizedActivity);
	}

	public String replaceIdentifiersWithActivityNamesInDotAutomaton(String automatonDotFormat, Character basingCharacter) {
		BufferedReader buRead = new BufferedReader(new StringReader(automatonDotFormat));
		StringBuilder sBuil = new StringBuilder();
		String line;
		Character
				activityId = null,
				activityIdForUpperBoundInRange = null;
		Pattern patternForSingleLabel = Pattern.compile("label=\"(.|\\\\[^\"-]+)\"");
		// Originarily, label="\"(.)\"", but it did not capture, e.g., label="\u00eb"
		Pattern patternForRangeLabel = Pattern.compile("label=\"(.|\\\\[^\"-]+)-(.|\\\\[^\"-]+)\"");
		// Originarily, label="\"(.)-(.)\"", but it did not capture, e.g., label="\u00eb-\u00ef"
		Matcher m = null;
		try {
			line = buRead.readLine();
			while (line != null) {
				m = patternForSingleLabel.matcher(line);
				if (m.find()) {
					activityId = TaskCharEncoderDecoder.encodedCharFromString(m.group(1));
					line = m.replaceFirst("label=\""
							+
							(	(transMap.containsKey(activityId))
								?
								(transMap.get(activityId) +
										(
											(basingCharacter != null && activityId.compareTo(basingCharacter) == 0)
											? "\",color=firebrick,style=bold"
											: "\""
										)
								)
								: (activityId + "\"")
							)
					);
				} else {
					m = patternForRangeLabel.matcher(line);
					if (m.find()) {
						activityId = TaskCharEncoderDecoder.encodedCharFromString(m.group(1));
						activityIdForUpperBoundInRange = TaskCharEncoderDecoder.encodedCharFromString(m.group(2));
						line = m.replaceFirst("label=\""
								+
								replaceIdentifiersWithActivityNamesInDotAutomatonLabel(activityId, activityIdForUpperBoundInRange)
								+ "\""
						);
					}
				}
				sBuil.append(line);
				sBuil.append("\n");
				line = buRead.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		return sBuil.toString();
	}
	
	private String replaceIdentifiersWithActivityNamesInDotAutomatonLabel(Character from, Character to) {
		StringBuilder sBuil = new StringBuilder();
		NavigableMap<Character, String> subMap = transMap.tailMap(from, true).headMap(to, true);
		if (subMap.size() > 0) {
			Iterator<String> actIterator = subMap.values().iterator();
			while (actIterator.hasNext()) {
				sBuil.append(actIterator.next());
				if (actIterator.hasNext()) {
					sBuil.append("\\\\n");
				}
			}
		}
		
		return sBuil.toString().trim();
	}
}
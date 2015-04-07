package minerful.io;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import minerful.MinerFulMinerStarter;
import minerful.automaton.SubAutomaton;
import minerful.automaton.concept.WeightedAutomaton;
import minerful.automaton.encdec.AutomatonDotPrinter;
import minerful.automaton.encdec.TsmlEncoder;
import minerful.automaton.encdec.WeightedAutomatonFactory;
import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskClass;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.index.LinearConstraintsIndexFactory;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.io.encdec.declare.DeclareEncoderDecoder;
import minerful.logparser.LogParser;
import dk.brics.automaton.Automaton;

public class ConstraintsPrinter {
	public static final int SUBAUTOMATA_MAXIMUM_ACTIVITIES_BEFORE_AND_AFTER = 3;
	public static final double MINIMUM_THRESHOLD = 0.0;
	private static final int HALF_NUMBER_OF_BARS = 10;
	// FIXME Make it user-customisable
	private static final boolean PRINT_ONLY_IF_ADDITIONAL_INFO_IS_GIVEN = false;
	private TaskCharRelatedConstraintsBag bag;
	private double supportThreshold;
	private double interestThreshold;
	private ProcessModel process;
	private Automaton processAutomaton;
	private NavigableMap<Constraint, String> additionalCnsIndexedInfo;

	public ConstraintsPrinter(TaskCharRelatedConstraintsBag bag) {
		this(bag, Constraint.MIN_SUPPORT, Constraint.MIN_INTEREST_FACTOR);
	}

	public ConstraintsPrinter(TaskCharRelatedConstraintsBag bag,
			double threshold, double interest) {
		this(bag, threshold, interest, null);
	}
	
	public ConstraintsPrinter(TaskCharRelatedConstraintsBag bag,
			Double supportThreshold, Double interestThreshold,
			NavigableMap<Constraint, String> additionalCnsIndexedInfo) {
		this.bag = bag;
		this.supportThreshold = supportThreshold;
		this.interestThreshold = interestThreshold;
		this.additionalCnsIndexedInfo = (additionalCnsIndexedInfo == null) ? new TreeMap<Constraint, String>() : additionalCnsIndexedInfo;
		this.process = new ProcessModel(this.bag, "Discovered process model");
	}

	public String printBag() {
        StringBuilder sBld = new StringBuilder();
        // The first pass is to understand how to pad the constraints' names
		int
			maxPadding =  computePaddingForConstraintNames(),
			i = 0;

        for (TaskChar key : bag.getTaskChars()) {
            sBld.append("\n\t[");

            sBld.append(key);
            sBld.append("] => {\n"
                    + "\t\t");
            for (Constraint c : bag.getConstraintsOf(key)) {
            	i++;
        		sBld.append(printConstraintsData(c, this.additionalCnsIndexedInfo.get(c), maxPadding, HALF_NUMBER_OF_BARS));
                sBld.append("\n\t\t");
            }
            sBld.append("\n\t}\n");
        }
//        sBld.append("\n");
//        sBld.append("Constraints shown: " + i + "\n");

        return sBld.toString();
	}

    public String printBagAsMachineReadable() {
    	return this.printBagAsMachineReadable(true);
    }
    
	public String printBagAsMachineReadable(boolean withNumericalIndex) {
        StringBuilder
        	sBufLegend = new StringBuilder(),
        	sBuffIndex = new StringBuilder(),
        	sBuffValues = new StringBuilder(),
        	superSbuf = new StringBuilder();

        int i = 0;
        
        TaskCharRelatedConstraintsBag redundaBag = bag.createRedundantCopy(bag.getTaskChars());
        
        for (TaskChar key : redundaBag.getTaskChars()) {
        	for (Constraint c : redundaBag.getConstraintsOf(key)) {
    			if (withNumericalIndex) {
        			sBuffIndex.append(++i);
        			sBuffIndex.append(';');
    			}
    			sBufLegend.append('\'');
    			sBufLegend.append(c.toString().replaceAll("\\W", " ").trim().replaceAll(" ", "_"));
    			sBufLegend.append('\'');
    			sBufLegend.append(';');
    			sBuffValues.append(String.format(Locale.ENGLISH, "%.3f", c.support * 100));
    			sBuffValues.append(';');
        	}
        }
        
        superSbuf.append("Machine-readable results:\n");
        superSbuf.append("Legend: ");
        if (withNumericalIndex) {
	        superSbuf.append(sBuffIndex.substring(0, sBuffIndex.length() -1));
	        superSbuf.append("\r\n");
        }
        superSbuf.append(sBufLegend.substring(0, sBufLegend.length() -1));
        superSbuf.append("\r\n\r\nSupport values: ");
        superSbuf.append(sBuffValues.substring(0, sBuffValues.length() -1));
        
        return superSbuf.toString();
	}
    
	public String printBagCsv() {
        StringBuilder
        	superSbuf = new StringBuilder();

        superSbuf.append("'Constraint template';'Implying activity';'Implied activity';'Support';'Confidence level';'Interest factor'\n");

        for (TaskChar key : bag.getTaskChars()) {
        	for (Constraint c : bag.getConstraintsOf(key)) {
        		superSbuf.append('\'');
//        		superSbuf.append(c.toString().replaceAll("\\W", " ").trim().replaceAll(" ", "_"));
        		superSbuf.append(c.getName());
        		superSbuf.append('\'');
        		superSbuf.append(';');
        		superSbuf.append('\'');
        		superSbuf.append(c.getBase());
        		superSbuf.append('\'');
        		superSbuf.append(';');
        		superSbuf.append('\'');
        		superSbuf.append(c.getImplied() == null ? "" : c.getImplied());
        		superSbuf.append('\'');
        		superSbuf.append(';');
        		superSbuf.append(String.format(Locale.ENGLISH, "%.3f", c.support * 100));
        		superSbuf.append(';');
        		superSbuf.append(String.format(Locale.ENGLISH, "%.3f", c.confidence * 100));
        		superSbuf.append(';');
        		superSbuf.append(String.format(Locale.ENGLISH, "%.3f", c.interestFactor * 100));
//    			sBuffValues.append(';');
        		superSbuf.append('\n');
        	}
        }
        
        return superSbuf.toString();
	}
	
	private String printConstraintsCollection(Collection<Constraint> constraintsCollection) {
		StringBuilder sBld = new StringBuilder();

        // The first pass is to understand how to pad the constraints' names
		int
			maxPadding =  computePaddingForConstraintNames(constraintsCollection),
			i = 0;
				
        for (Constraint c : constraintsCollection) {
        	i++;
        	sBld.append("\n\t");
    		sBld.append(printConstraintsData(c, this.additionalCnsIndexedInfo.get(c), maxPadding, HALF_NUMBER_OF_BARS));
        }
        sBld.append("\n\n");
        sBld.append("Constraints shown: " + i + "\n");

		return sBld.toString();
	}

	public String printUnfoldedBag() {
		return printConstraintsCollection(LinearConstraintsIndexFactory.getAllConstraints(bag));
	}

	public String printUnfoldedBagOrderedBySupport() {
		return printConstraintsCollection(LinearConstraintsIndexFactory.getAllConstraintsSortedBySupport(bag));
	}

	public String printUnfoldedBagOrderedByInterest() {
		return printConstraintsCollection(LinearConstraintsIndexFactory.getAllConstraintsSortedByInterest(bag));
	}

	public int computePaddingForConstraintNames() {
		return computePaddingForConstraintNames(LinearConstraintsIndexFactory.getAllConstraints(bag));
	}
	
	public int computePaddingForConstraintNames(Collection<Constraint> constraintsSet) {
        int 	maxPadding = 0,
        		auxConstraintStringLength = 0;
    	for (Constraint c : constraintsSet) {
        	auxConstraintStringLength = c.toString().length();
        	if (maxPadding < auxConstraintStringLength) {
        		maxPadding = auxConstraintStringLength;
        	}
    	}
        // As a rule of thumb...
        maxPadding += 3;
        
        return maxPadding;
	}

    public String printConstraintsData(Constraint constraint, String additionalInfo, int maxPadding, int halfNumberOfBars) {
    	if (PRINT_ONLY_IF_ADDITIONAL_INFO_IS_GIVEN) {
    		if (additionalInfo == null || additionalInfo.isEmpty()) {
    			return "";
    		}
    	}
    	
    	StringBuilder sBld = new StringBuilder();
    	int barsCounter = -halfNumberOfBars;
        double relativeSupport = constraint.getRelativeSupport(supportThreshold);

        sBld.append(String.format(Locale.ENGLISH, "%7.3f%% ", constraint.support * 100));
        sBld.append(String.format("%-" + maxPadding + "s", constraint.toString()));
        sBld.append(String.format(Locale.ENGLISH, "%8.3f%% ", relativeSupport * 100));
        if (relativeSupport != 0) {
            for (; (barsCounter < relativeSupport * halfNumberOfBars && barsCounter <= 0); barsCounter++) {
                sBld.append(' ');
            }
            for (; (barsCounter >= relativeSupport * halfNumberOfBars && barsCounter <= 0) || (barsCounter < relativeSupport * halfNumberOfBars && barsCounter >= 0); barsCounter++) {
                sBld.append('|');
            }
        }
        for (; barsCounter <= halfNumberOfBars; barsCounter++) {
        	sBld.append(' ');
        }
        sBld.append(String.format(Locale.ENGLISH, " conf.: %7.3f; ", constraint.confidence));
        sBld.append(String.format(Locale.ENGLISH, " int'f: %7.3f; ", constraint.interestFactor));
        
       	if (additionalInfo != null)
        	sBld.append(additionalInfo);
        
        return sBld.toString();
    }
    
    public void printConDecModel(File outFile) throws IOException {
		new DeclareEncoderDecoder(process).marshal(outFile.getCanonicalPath());
    }
    
    public String printWeightedXmlAutomaton(LogParser logParser) throws JAXBException {
		if (this.processAutomaton == null)
			processAutomaton = this.process.buildAutomaton();
		
		WeightedAutomatonFactory wAF = new WeightedAutomatonFactory(TaskCharEncoderDecoder.getTranslationMap(bag));
		WeightedAutomaton wAut = wAF.augmentByReplay(processAutomaton, logParser);

		if (wAut == null)
			return null;
		
		JAXBContext jaxbCtx = JAXBContext.newInstance(WeightedAutomaton.class);
		Marshaller marsh = jaxbCtx.createMarshaller();
		marsh.setProperty("jaxb.formatted.output", true);
		StringWriter strixWriter = new StringWriter();
		marsh.marshal(wAut, strixWriter);
		strixWriter.flush();
		StringBuffer strixBuffer = strixWriter.getBuffer();

		// OINK
		strixBuffer.replace(strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3), strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3),
				" xmlns=\"" + MinerFulMinerStarter.MINERFUL_XMLNS + "\"");
		
		return strixWriter.toString();
    }
    
    public NavigableMap<String, String> printWeightedXmlSubAutomata(LogParser logParser) throws JAXBException {
		Collection<SubAutomaton> partialAutomata =
//				this.process.buildSubAutomata(ConstraintsPrinter.SUBAUTOMATA_MAXIMUM_ACTIVITIES_BEFORE_AND_AFTER);
				this.process.buildSubAutomata();
		WeightedAutomatonFactory wAF = new WeightedAutomatonFactory(TaskCharEncoderDecoder.getTranslationMap(bag));
		NavigableMap<Character, TaskClass> idsNamesMap = TaskCharEncoderDecoder.getTranslationMap(bag);

		NavigableMap<String, String> partialAutomataXmls = new TreeMap<String, String>();
		
		WeightedAutomaton wAut = null;
		StringWriter strixWriter = null;
		StringBuffer strixBuffer = null;
	
		JAXBContext jaxbCtx = JAXBContext.newInstance(WeightedAutomaton.class);
		Marshaller marsh = jaxbCtx.createMarshaller();
		marsh.setProperty("jaxb.formatted.output", true);

		for (SubAutomaton partialAuto : partialAutomata) {
			wAut = wAF.augmentByReplay(partialAuto.automaton, logParser, true);
			if (wAut != null) {
				strixWriter = new StringWriter();
				marsh.marshal(wAut, strixWriter);
				strixWriter.flush();
				strixBuffer = strixWriter.getBuffer();

				// OINK
				strixBuffer.replace(strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3), strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3),
						" xmlns=\"" + MinerFulMinerStarter.MINERFUL_XMLNS + "\"");
				partialAutomataXmls.put(idsNamesMap.get(partialAuto.basingCharacter).getName(), strixWriter.toString());
			}
		}
		return partialAutomataXmls;
    }

	public String printDotAutomaton() {
		if (this.processAutomaton == null)
			processAutomaton = this.process.buildAutomaton();
		NavigableMap<Character, String> stringMap = new TreeMap<Character, String>();
		NavigableMap<Character, TaskClass> charToClassMap = TaskCharEncoderDecoder.getTranslationMap(bag);
		for (Character key : charToClassMap.keySet())
			stringMap.put(key, charToClassMap.get(key).getName());
		return new AutomatonDotPrinter(stringMap).printDot(processAutomaton);
	}
	
	public String printTSMLAutomaton() {
		if (this.processAutomaton == null)
			processAutomaton = this.process.buildAutomaton();
		NavigableMap<Character, String> idsNamesMap = new TreeMap<Character, String>();
		NavigableMap<Character, TaskClass> charToClassMap = TaskCharEncoderDecoder.getTranslationMap(bag);
		for (Character key : charToClassMap.keySet())
			idsNamesMap.put(key, charToClassMap.get(key).getName());
		return new TsmlEncoder(idsNamesMap).automatonToTSML(processAutomaton, this.process.getName());
	}
	
	public NavigableMap<String, String> printDotPartialAutomata() {
		NavigableMap<String, String> partialAutomataDots = new TreeMap<String, String>();
		Collection<SubAutomaton> partialAutomata =
				this.process.buildSubAutomata(ConstraintsPrinter.SUBAUTOMATA_MAXIMUM_ACTIVITIES_BEFORE_AND_AFTER);
		String dotFormattedAutomaton = null;
		NavigableMap<Character, TaskClass> charToClassMap = TaskCharEncoderDecoder.getTranslationMap(bag);
		NavigableMap<Character, String> idsNamesMap = new TreeMap<Character, String>();
		for (Character key : charToClassMap.keySet())
			idsNamesMap.put(key, charToClassMap.get(key).getName());
		AutomatonDotPrinter autoDotPrinter = new AutomatonDotPrinter(idsNamesMap);
		for (SubAutomaton partialAutomaton : partialAutomata) {
			//dotFormattedAutomaton = partialAutomaton.automaton.toDot();
			if (partialAutomaton.automaton.getInitialState().getTransitions().size() > 0) {
				dotFormattedAutomaton = autoDotPrinter.printDot(partialAutomaton.automaton, partialAutomaton.basingCharacter);//.replaceIdentifiersWithActivityNamesInDotAutomaton(dotFormattedAutomaton, idsNamesMap, partialAutomaton.basingCharacter);
				partialAutomataDots.put(idsNamesMap.get(partialAutomaton.basingCharacter), dotFormattedAutomaton);
			}
		}
		return partialAutomataDots;
	}
	

	public TaskCharRelatedConstraintsBag getBag() {
		return bag;
	}
	public double getSupportThreshold() {
		return supportThreshold;
	}
	public double getInterestThreshold() {
		return interestThreshold;
	}
}
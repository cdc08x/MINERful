package minerful.io;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.TreeSet;

import minerful.automaton.AutomatonFactory;
import minerful.automaton.SubAutomaton;
import minerful.automaton.concept.weight.WeightedAutomaton;
import minerful.automaton.encdec.AutomatonDotPrinter;
import minerful.automaton.encdec.TsmlEncoder;
import minerful.automaton.encdec.WeightedAutomatonFactory;
import minerful.concept.AbstractTaskClass;
import minerful.concept.ProcessSpecification;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintMeasuresManager;
import minerful.concept.constraint.ConstraintsBag;
import minerful.index.LinearConstraintsIndexFactory;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.io.encdec.csv.CsvEncoder;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.encdec.declaremap.DeclareMapReaderWriter;
import minerful.io.encdec.nusmv.NuSMVEncoder;
import minerful.logparser.LogParser;
import minerful.miner.ProbabilisticRelationConstraintsMiner.ConstraintMeasures;
import dk.brics.automaton.Automaton;

public class ConstraintsPrinter {
	private static final String MACHINE_READABLE_RESULTS_SUPPORT_TEXT_SIGNAL = "Measures: ";
	public static final String MACHINE_READABLE_RESULTS_LEGEND_TEXT_SIGNAL = "Legend: ";
	public static final String MACHINE_READABLE_RESULTS_TEXT_SIGNAL = "Machine-readable results: ";

	public static final int SUBAUTOMATA_MAXIMUM_ACTIVITIES_BEFORE_AND_AFTER = // 3;
			AutomatonFactory.NO_LIMITS_IN_ACTIONS_FOR_SUBAUTOMATA;
	public static final double MINIMUM_THRESHOLD = 0.0;
//	private static final int HALF_NUMBER_OF_BARS = 10;
	// FIXME Make it user-customisable
	private static final boolean PRINT_ONLY_IF_ADDITIONAL_INFO_IS_GIVEN = false;
	private ProcessSpecification processSpecification;
	private Automaton processAutomaton;
	private NavigableMap<Constraint, String> additionalCnsIndexedInfo;

	public ConstraintsPrinter(ProcessSpecification processSpecification) {
		this(processSpecification, null);
	}
	
	public ConstraintsPrinter(ProcessSpecification processSpecification,
			NavigableMap<Constraint, String> additionalCnsIndexedInfo) {
		this.processSpecification = processSpecification;
		this.additionalCnsIndexedInfo = (additionalCnsIndexedInfo == null) ? new TreeMap<Constraint, String>() : additionalCnsIndexedInfo;
	}

	public String printBag() {
        StringBuilder sBld = new StringBuilder();
        // The first pass is to understand how to pad the constraints' names
		int
			maxPadding =  computePaddingForConstraintNames();

        for (TaskChar key : this.processSpecification.bag.getTaskChars()) {
            sBld.append("\n\t[");

            sBld.append(key);
            sBld.append("] => {\n"
                    + "\t\t");
            for (Constraint c : this.processSpecification.bag.getConstraintsOf(key)) {
            	if (!c.isMarkedForExclusionOrForbidden()) {
	        		sBld.append(printConstraintsData(c, this.additionalCnsIndexedInfo.get(c), maxPadding)); //, HALF_NUMBER_OF_BARS));
	                sBld.append("\n\t\t");
            	}
            }
            sBld.append("\n\t}\n");
        }

        return sBld.toString();
	}

    public String printBagAsMachineReadable() {
    	return this.printBagAsMachineReadable(true, true, true);
    }
    
	public String printBagAsMachineReadable(boolean withNumericalIndex, boolean withTextSignals, boolean withHeaders) {
        StringBuilder
        	sBufLegend = new StringBuilder(),
        	sBuffIndex = new StringBuilder(),
        	sBuffValues = new StringBuilder(),
        	superSbuf = new StringBuilder();

        int i = 0;
        
        ConstraintsBag redundaBag = this.processSpecification.bag.createRedundantCopy(this.processSpecification.bag.getTaskChars());
        
        for (TaskChar key : redundaBag.getTaskChars()) {
        	for (Constraint c : redundaBag.getConstraintsOf(key)) {
    			if (withNumericalIndex) {
        			sBuffIndex.append(i+1);
        			sBuffIndex.append(';');
    			}
    			sBufLegend.append('\'');
    			sBufLegend.append(c.toString().replace("'", "\\'"));
    			sBufLegend.append('\'');
    			sBufLegend.append(';');
    			sBuffValues.append(String.format(Locale.ENGLISH, "%.9f", c.getEventBasedMeasures().getSupport() * 100));
    			sBuffValues.append(';');
    			sBufLegend.append(';');
    			sBuffValues.append(String.format(Locale.ENGLISH, "%.9f", c.getEventBasedMeasures().getConfidence() * 100));
    			sBuffValues.append(';');
    			sBufLegend.append(';');
    			sBuffValues.append(String.format(Locale.ENGLISH, "%.9f", c.getEventBasedMeasures().getCoverage() * 100));
    			sBuffValues.append(';');
    			sBufLegend.append(';');
    			sBuffValues.append(String.format(Locale.ENGLISH, "%.9f", c.getTraceBasedMeasures().getSupport() * 100));
    			sBuffValues.append(';');
    			sBufLegend.append(';');
    			sBuffValues.append(String.format(Locale.ENGLISH, "%.9f", c.getTraceBasedMeasures().getConfidence() * 100));
    			sBuffValues.append(';');
    			sBufLegend.append(';');
    			sBuffValues.append(String.format(Locale.ENGLISH, "%.9f", c.getTraceBasedMeasures().getCoverage() * 100));
    			sBuffValues.append(';');    			
    			i++;
        	}
        }
        
        if (withTextSignals) {
	        superSbuf.append(MACHINE_READABLE_RESULTS_TEXT_SIGNAL);
	        superSbuf.append("\r\n");
	        superSbuf.append(MACHINE_READABLE_RESULTS_LEGEND_TEXT_SIGNAL);
        }
        if (withNumericalIndex) {
	        superSbuf.append(sBuffIndex.substring(0, sBuffIndex.length() -1));
	        superSbuf.append("\r\n");
        }
        if (withHeaders) {
        	superSbuf.append(sBufLegend.substring(0, sBufLegend.length() -1));
        	superSbuf.append("\r\n");
        	if (i > 0)
        		superSbuf.append("'Support';'Confidence';'Coverage';'Trace support';'Trace confidence';'Trace coverage'");
	        for (int j = 1; j < i; j++) {
	        	superSbuf.append(";'Support';'Confidence';'Coverage';'Trace support';'Trace confidence';'Trace coverage'");
	        }
	        superSbuf.append("\r\n");
        }
        if (withTextSignals) {
        	superSbuf.append(MACHINE_READABLE_RESULTS_SUPPORT_TEXT_SIGNAL);
        }
        superSbuf.append(sBuffValues.substring(0, sBuffValues.length() -1));
        
        return superSbuf.toString();
	}
    
	/**
	 * Prints the constraints in a CSV format. The constraints that are marked for exclusion are not included in the print-out.
	 * @return A string containing the list of process specification' constraints in a CSV format.  
	 */	
	public String printBagCsv() {
        return this.printBagCsv(CsvEncoder.PRINT_OUT_ELEMENT.values());
	}
	
	/**
	 * Prints the CSV format of the constraints bag. The columns appearing in the file can be customised.
	 * @param columns A sorted set of columns. See the <code>PRINT_OUT_ELEMENT</code> enumeration.
	 * @return A CSV string containing the constraints bag.
	 */
	public String printBagCsv(CsvEncoder.PRINT_OUT_ELEMENT... columns) {
		return new CsvEncoder().printAsCsv(
				new TreeSet<CsvEncoder.PRINT_OUT_ELEMENT>(Arrays.asList(columns)),
				this.processSpecification
		);
	}
	
	private String printConstraintsCollection(Collection<Constraint> constraintsCollection) {
		StringBuilder sBld = new StringBuilder();

        // The first pass is to understand how to pad the constraints' names
		int
			maxPadding =  computePaddingForConstraintNames(constraintsCollection),
			i = 0;
				
        for (Constraint c : constraintsCollection) {
        	if (!c.isMarkedForExclusionOrForbidden()) {
	        	i++;
	        	sBld.append("\n\t");
	    		sBld.append(printConstraintsData(c, this.additionalCnsIndexedInfo.get(c), maxPadding)); //, HALF_NUMBER_OF_BARS));
        	}
        }
        sBld.append("\n\n");
        sBld.append("Constraints shown: " + i + "\n");

		return sBld.toString();
	}

	public String printUnfoldedBag() {
		return printConstraintsCollection(this.processSpecification.getAllConstraints());
	}

	public String printUnfoldedBagOrderedBySupport() {
		return printConstraintsCollection(LinearConstraintsIndexFactory.getAllConstraintsSortedBySupport(this.processSpecification.bag));
	}

	public String printUnfoldedBagOrderedByInterest() {
		return printConstraintsCollection(LinearConstraintsIndexFactory.getAllConstraintsSortedByInterest(this.processSpecification.bag));
	}

	public int computePaddingForConstraintNames() {
		return computePaddingForConstraintNames(this.processSpecification.getAllConstraints());
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

    public String printConstraintsData(Constraint constraint, String additionalInfo, int maxPadding) {//, int halfNumberOfBars) {
    	if (PRINT_ONLY_IF_ADDITIONAL_INFO_IS_GIVEN) {
    		if (additionalInfo == null || additionalInfo.isEmpty()) {
    			return "";
    		}
    	}
    	
    	StringBuilder sBld = new StringBuilder();
//    	int barsCounter = -halfNumberOfBars;
//        double relativeSupport = constraint.getRelativeSupport(supportThreshold);

        sBld.append(String.format("%-" + maxPadding + "s", constraint.toString()));
//        sBld.append(String.format(Locale.ENGLISH, "%8.3f%% ", relativeSupport * 100));

//        if (relativeSupport != 0) {
//            for (; (barsCounter < relativeSupport * halfNumberOfBars && barsCounter <= 0); barsCounter++) {
//                sBld.append(' ');
//            }
//            for (; (barsCounter >= relativeSupport * halfNumberOfBars && barsCounter <= 0) || (barsCounter < relativeSupport * halfNumberOfBars && barsCounter >= 0); barsCounter++) {
//                sBld.append('|');
//            }
//        }
//
//        for (; barsCounter <= halfNumberOfBars; barsCounter++) {
//        	sBld.append(' ');
//        }
        sBld.append(String.format(Locale.ENGLISH, "ev.conf.: %4.3f; ", constraint.getEventBasedMeasures().getConfidence()));
        sBld.append(String.format(Locale.ENGLISH, " ev.covr.: %4.3f; ", constraint.getEventBasedMeasures().getCoverage()));
        sBld.append(String.format(Locale.ENGLISH, " ev.supp.: %4.3f; ", constraint.getEventBasedMeasures().getSupport()));
        sBld.append(String.format(Locale.ENGLISH, " tr.conf.: %4.3f; ", constraint.getTraceBasedMeasures().getConfidence()));
        sBld.append(String.format(Locale.ENGLISH, " tr.covr.: %4.3f; ", constraint.getTraceBasedMeasures().getCoverage()));
        sBld.append(String.format(Locale.ENGLISH, " tr.supp.: %4.3f; ", constraint.getTraceBasedMeasures().getSupport()));
        if (constraint.getTraceBasedMeasures().getFitness() != ConstraintMeasuresManager.UNKNOWN_FITNESS) {
        	sBld.append(String.format(Locale.ENGLISH, " fit: %4.3f; ", constraint.getTraceBasedMeasures().getFitness()));
        }
        
       	if (additionalInfo != null)
        	sBld.append(additionalInfo);
        
        return sBld.toString();
    }
    
    public void saveAsConDecSpecification(File outFile) throws IOException {
    	DeclareMapEncoderDecoder deMapEnDec = new DeclareMapEncoderDecoder(processSpecification);
    	DeclareMapReaderWriter.marshal(outFile.getCanonicalPath(), deMapEnDec.createDeclareMap());
    }
    
    public String printNuSMV() {
    	NuSMVEncoder nuSmvDec = new NuSMVEncoder(processSpecification);
    	return nuSmvDec.printAsNuSMV();
    }
    
    // public String printWeightedXmlAutomaton(LogParser logParser, boolean skimIt) throws JAXBException {
	// 	if (this.processAutomaton == null)
	// 		processAutomaton = this.processSpecification.buildAutomaton();
		
	// 	WeightedAutomatonFactory wAF = new WeightedAutomatonFactory(TaskCharEncoderDecoder.getTranslationMap(this.processSpecification.bag));
	// 	WeightedAutomaton wAut = wAF.augmentByReplay(processAutomaton, logParser, skimIt);

	// 	if (wAut == null)
	// 		return null;
		
		// JAXBContext jaxbCtx = JAXBContext.newInstance(WeightedAutomaton.class);
		// Marshaller marsh = jaxbCtx.createMarshaller();
		// marsh.setProperty("jaxb.formatted.output", true);
		// StringWriter strixWriter = new StringWriter();
		// marsh.marshal(wAut, strixWriter);
		// strixWriter.flush();
		// StringBuffer strixBuffer = strixWriter.getBuffer();

		// OINK
	// 	strixBuffer.replace(strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3), strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3),
	// 			" xmlns=\"" + ProcessSpecification.MINERFUL_XMLNS + "\"");
		
	// 	return strixWriter.toString();
    // }
    
//     public NavigableMap<String, String> printWeightedXmlSubAutomata(LogParser logParser) throws JAXBException {
// 		Collection<SubAutomaton> partialAutomata =
// //				this.process.buildSubAutomata(ConstraintsPrinter.SUBAUTOMATA_MAXIMUM_ACTIVITIES_BEFORE_AND_AFTER);
// 				this.processSpecification.buildSubAutomata();
// 		WeightedAutomatonFactory wAF = new WeightedAutomatonFactory(TaskCharEncoderDecoder.getTranslationMap(this.processSpecification.bag));
// 		NavigableMap<Character, AbstractTaskClass> idsNamesMap = TaskCharEncoderDecoder.getTranslationMap(this.processSpecification.bag);

// 		NavigableMap<String, String> partialAutomataXmls = new TreeMap<String, String>();
		
// 		WeightedAutomaton wAut = null;
// 		StringWriter strixWriter = null;
// 		StringBuffer strixBuffer = null;
	
// 		JAXBContext jaxbCtx = JAXBContext.newInstance(WeightedAutomaton.class);
// 		Marshaller marsh = jaxbCtx.createMarshaller();
// 		marsh.setProperty("jaxb.formatted.output", true);

// 		for (SubAutomaton partialAuto : partialAutomata) {
// 			wAut = wAF.augmentByReplay(partialAuto.automaton, logParser, false, true);
// 			if (wAut != null) {
// 				strixWriter = new StringWriter();
// 				marsh.marshal(wAut, strixWriter);
// 				strixWriter.flush();
// 				strixBuffer = strixWriter.getBuffer();

// 				// OINK
// 				strixBuffer.replace(strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3), strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3),
// 						" xmlns=\"" + ProcessSpecification.MINERFUL_XMLNS + "\"");
// 				partialAutomataXmls.put(idsNamesMap.get(partialAuto.basingCharacter).getName(), strixWriter.toString());
// 			}
// 		}
// 		return partialAutomataXmls;
//     }

	public String printDotAutomaton() {
		if (this.processAutomaton == null)
			processAutomaton = this.processSpecification.buildAutomaton();
		
		NavigableMap<Character, String> stringMap = new TreeMap<Character, String>();
		NavigableMap<Character, AbstractTaskClass> charToClassMap = TaskCharEncoderDecoder.getTranslationMap(this.processSpecification.bag);
		for (Character key : charToClassMap.keySet())
			stringMap.put(key, charToClassMap.get(key).getName());

		return new AutomatonDotPrinter(stringMap).printDot(processAutomaton);
	}
	
	public String printTSMLAutomaton() {
		if (this.processAutomaton == null)
			processAutomaton = this.processSpecification.buildAutomaton();
		NavigableMap<Character, String> idsNamesMap = new TreeMap<Character, String>();
		NavigableMap<Character, AbstractTaskClass> charToClassMap = TaskCharEncoderDecoder.getTranslationMap(this.processSpecification.bag);
		for (Character key : charToClassMap.keySet())
			idsNamesMap.put(key, charToClassMap.get(key).getName());
		return new TsmlEncoder(idsNamesMap).automatonToTSML(processAutomaton, this.processSpecification.getName());
	}
	
	public NavigableMap<String, String> printDotPartialAutomata() {
		NavigableMap<String, String> partialAutomataDots = new TreeMap<String, String>();
		Collection<SubAutomaton> partialAutomata =
				this.processSpecification.buildSubAutomata(ConstraintsPrinter.SUBAUTOMATA_MAXIMUM_ACTIVITIES_BEFORE_AND_AFTER);
		String dotFormattedAutomaton = null;
		NavigableMap<Character, AbstractTaskClass> charToClassMap = TaskCharEncoderDecoder.getTranslationMap(this.processSpecification.bag);
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

	public ConstraintsBag getBag() {
		return this.processSpecification.bag;
	}
}
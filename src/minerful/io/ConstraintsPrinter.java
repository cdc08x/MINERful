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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import minerful.automaton.AutomatonFactory;
import minerful.automaton.SubAutomaton;
import minerful.automaton.concept.weight.WeightedAutomaton;
import minerful.automaton.encdec.AutomatonDotPrinter;
import minerful.automaton.encdec.TsmlEncoder;
import minerful.automaton.encdec.WeightedAutomatonFactory;
import minerful.concept.AbstractTaskClass;
import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.index.LinearConstraintsIndexFactory;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.io.encdec.csv.CsvEncoder;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.encdec.declaremap.DeclareMapReaderWriter;
import minerful.logparser.LogParser;
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
	private ProcessModel processModel;
	private Automaton processAutomaton;
	private NavigableMap<Constraint, String> additionalCnsIndexedInfo;

	public ConstraintsPrinter(ProcessModel processModel) {
		this(processModel, null);
	}
	
	public ConstraintsPrinter(ProcessModel processModel,
			NavigableMap<Constraint, String> additionalCnsIndexedInfo) {
		this.processModel = processModel;
		this.additionalCnsIndexedInfo = (additionalCnsIndexedInfo == null) ? new TreeMap<Constraint, String>() : additionalCnsIndexedInfo;
	}

	public String printBag() {
        StringBuilder sBld = new StringBuilder();
        // The first pass is to understand how to pad the constraints' names
		int
			maxPadding =  computePaddingForConstraintNames();

        for (TaskChar key : this.processModel.bag.getTaskChars()) {
            sBld.append("\n\t[");

            sBld.append(key);
            sBld.append("] => {\n"
                    + "\t\t");
            for (Constraint c : this.processModel.bag.getConstraintsOf(key)) {
            	if (!c.isMarkedForExclusion()) {
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
        
        ConstraintsBag redundaBag = this.processModel.bag.createRedundantCopy(this.processModel.bag.getTaskChars());
        
        for (TaskChar key : redundaBag.getTaskChars()) {
        	for (Constraint c : redundaBag.getConstraintsOf(key)) {
    			if (withNumericalIndex) {
        			sBuffIndex.append(i+1);
        			sBuffIndex.append(';');
    			}
    			sBufLegend.append('\'');
    			// BUG-FIX: there is no reason why we have to flatten all non-word characters into "_".
    			// This creates tremendous issues with logs like BPIC 2012, where we have both
    			// "A_ACCEPTED" and "W_Completeren aanvraag"
    			sBufLegend.append(c.toString().replace("'", "\\'")); //.replaceAll("\\W", " ").trim().replaceAll(" ", "_"));
    			sBufLegend.append('\'');
    			sBufLegend.append(';');
    			sBuffValues.append(String.format(Locale.ENGLISH, "%.9f", c.getSupport() * 100));
    			sBuffValues.append(';');
    			sBufLegend.append(';');
    			sBuffValues.append(String.format(Locale.ENGLISH, "%.9f", c.getConfidence() * 100));
    			sBuffValues.append(';');
    			sBufLegend.append(';');
    			sBuffValues.append(String.format(Locale.ENGLISH, "%.9f", c.getInterestFactor() * 100));
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
        		superSbuf.append("'Support';'Confidence';'InterestF'");
	        for (int j = 1; j < i; j++) {
	        	superSbuf.append(";'Support';'Confidence';'InterestF'");
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
	 * @return A string containing the list of process model' constraints in a CSV format.  
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
				this.processModel
		);
	}
	
	private String printConstraintsCollection(Collection<Constraint> constraintsCollection) {
		StringBuilder sBld = new StringBuilder();

        // The first pass is to understand how to pad the constraints' names
		int
			maxPadding =  computePaddingForConstraintNames(constraintsCollection),
			i = 0;
				
        for (Constraint c : constraintsCollection) {
        	if (!c.isMarkedForExclusion()) {
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
		return printConstraintsCollection(LinearConstraintsIndexFactory.getAllConstraints(this.processModel.bag));
	}

	public String printUnfoldedBagOrderedBySupport() {
		return printConstraintsCollection(LinearConstraintsIndexFactory.getAllConstraintsSortedBySupport(this.processModel.bag));
	}

	public String printUnfoldedBagOrderedByInterest() {
		return printConstraintsCollection(LinearConstraintsIndexFactory.getAllConstraintsSortedByInterest(this.processModel.bag));
	}

	public int computePaddingForConstraintNames() {
		return computePaddingForConstraintNames(LinearConstraintsIndexFactory.getAllConstraints(this.processModel.bag));
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

        sBld.append(String.format(Locale.ENGLISH, "%7.3f%% ", constraint.getSupport() * 100));
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
        sBld.append(String.format(Locale.ENGLISH, " conf.: %7.3f; ", constraint.getConfidence()));
        sBld.append(String.format(Locale.ENGLISH, " int'f: %7.3f; ", constraint.getInterestFactor()));
        if (constraint.getFitness() != null) {
        	sBld.append(String.format(Locale.ENGLISH, " fit: %7.3f; ", constraint.getFitness()));
        }
        
       	if (additionalInfo != null)
        	sBld.append(additionalInfo);
        
        return sBld.toString();
    }
    
    public void saveAsConDecModel(File outFile) throws IOException {
    	DeclareMapEncoderDecoder deMapEnDec = new DeclareMapEncoderDecoder(processModel);
    	DeclareMapReaderWriter.marshal(outFile.getCanonicalPath(), deMapEnDec.createDeclareMap());
    }
    
    public String printWeightedXmlAutomaton(LogParser logParser, boolean skimIt) throws JAXBException {
		if (this.processAutomaton == null)
			processAutomaton = this.processModel.buildAutomaton();
		
		WeightedAutomatonFactory wAF = new WeightedAutomatonFactory(TaskCharEncoderDecoder.getTranslationMap(this.processModel.bag));
		WeightedAutomaton wAut = wAF.augmentByReplay(processAutomaton, logParser, skimIt);

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
				" xmlns=\"" + ProcessModel.MINERFUL_XMLNS + "\"");
		
		return strixWriter.toString();
    }
    
    public NavigableMap<String, String> printWeightedXmlSubAutomata(LogParser logParser) throws JAXBException {
		Collection<SubAutomaton> partialAutomata =
//				this.process.buildSubAutomata(ConstraintsPrinter.SUBAUTOMATA_MAXIMUM_ACTIVITIES_BEFORE_AND_AFTER);
				this.processModel.buildSubAutomata();
		WeightedAutomatonFactory wAF = new WeightedAutomatonFactory(TaskCharEncoderDecoder.getTranslationMap(this.processModel.bag));
		NavigableMap<Character, AbstractTaskClass> idsNamesMap = TaskCharEncoderDecoder.getTranslationMap(this.processModel.bag);

		NavigableMap<String, String> partialAutomataXmls = new TreeMap<String, String>();
		
		WeightedAutomaton wAut = null;
		StringWriter strixWriter = null;
		StringBuffer strixBuffer = null;
	
		JAXBContext jaxbCtx = JAXBContext.newInstance(WeightedAutomaton.class);
		Marshaller marsh = jaxbCtx.createMarshaller();
		marsh.setProperty("jaxb.formatted.output", true);

		for (SubAutomaton partialAuto : partialAutomata) {
			wAut = wAF.augmentByReplay(partialAuto.automaton, logParser, false, true);
			if (wAut != null) {
				strixWriter = new StringWriter();
				marsh.marshal(wAut, strixWriter);
				strixWriter.flush();
				strixBuffer = strixWriter.getBuffer();

				// OINK
				strixBuffer.replace(strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3), strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3),
						" xmlns=\"" + ProcessModel.MINERFUL_XMLNS + "\"");
				partialAutomataXmls.put(idsNamesMap.get(partialAuto.basingCharacter).getName(), strixWriter.toString());
			}
		}
		return partialAutomataXmls;
    }

	public String printDotAutomaton() {
		if (this.processAutomaton == null)
			processAutomaton = this.processModel.buildAutomaton();
		
		NavigableMap<Character, String> stringMap = new TreeMap<Character, String>();
		NavigableMap<Character, AbstractTaskClass> charToClassMap = TaskCharEncoderDecoder.getTranslationMap(this.processModel.bag);
		for (Character key : charToClassMap.keySet())
			stringMap.put(key, charToClassMap.get(key).getName());

		return new AutomatonDotPrinter(stringMap).printDot(processAutomaton);
	}
	
	public String printTSMLAutomaton() {
		if (this.processAutomaton == null)
			processAutomaton = this.processModel.buildAutomaton();
		NavigableMap<Character, String> idsNamesMap = new TreeMap<Character, String>();
		NavigableMap<Character, AbstractTaskClass> charToClassMap = TaskCharEncoderDecoder.getTranslationMap(this.processModel.bag);
		for (Character key : charToClassMap.keySet())
			idsNamesMap.put(key, charToClassMap.get(key).getName());
		return new TsmlEncoder(idsNamesMap).automatonToTSML(processAutomaton, this.processModel.getName());
	}
	
	public NavigableMap<String, String> printDotPartialAutomata() {
		NavigableMap<String, String> partialAutomataDots = new TreeMap<String, String>();
		Collection<SubAutomaton> partialAutomata =
				this.processModel.buildSubAutomata(ConstraintsPrinter.SUBAUTOMATA_MAXIMUM_ACTIVITIES_BEFORE_AND_AFTER);
		String dotFormattedAutomaton = null;
		NavigableMap<Character, AbstractTaskClass> charToClassMap = TaskCharEncoderDecoder.getTranslationMap(this.processModel.bag);
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
		return this.processModel.bag;
	}
}
package minerful.examples.api.formal;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dk.brics.automaton.RegExp;
import minerful.MinerFulOutputManagementLauncher;
import minerful.concept.ProcessSpecification;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.io.ConstraintsPrinter;
import minerful.io.encdec.ProcessSpecificationEncoderDecoder;
import minerful.io.params.OutputSpecificationParameters;
import minerful.logparser.LogParser;
import minerful.logparser.XesLogParser;

public class AllLTLfRegExpAndAutomataPrinter {
	public static final File OUTPUT_DIR = new File("/Users/ceciliaiacometta/Desktop/examples/re-ltlf-fsa");
	public static final File AUTOMA_DIR = new File(OUTPUT_DIR.getPath().concat("/automata"));
	public static final File RES_FILE = new File(OUTPUT_DIR.getPath().concat("/MINERful-discoverable-constraints--regular-expressions.json"));
	public static final File LTLFES_FILE = new File(OUTPUT_DIR.getPath().concat("/MINERful-discoverable-constraints--LTLf-expressions.json"));
	public static final File NEG_RES_FILE = new File(OUTPUT_DIR.getPath().concat("/MINERful-negative-constraints--regular-expressions.json"));
    public static final File NEG_LTLFES_FILE = new File(OUTPUT_DIR.getPath().concat("/MINERful-negative-constraints--LTLf-expressions.json"));
	public static final TaskChar BASE = new TaskChar('a');
	public static final TaskChar IMPLIED = new TaskChar('b');
	public static final TaskChar TERTIUM_NON_DATUR = new TaskChar('x');

	public static void main(String[] args) throws IOException {
		// Generate all constraints that MINERful can discover taking a (base, defined above) and b (implied, as defined above)
		Collection<Constraint> disCons = MetaConstraintUtils.getAllDiscoverableConstraints(BASE, IMPLIED);
//		RegExp rex = new Reg
		
		HashMap<String, String> disCoLTLfEs = new HashMap<String, String>(disCons.size()),
		                        disCoREs = new HashMap<String, String>(disCons.size()),
								negDisCoLTLfEs = new HashMap<String, String>(disCons.size()),
                                negDisCoREs = new HashMap<String, String>(disCons.size());
		PrintWriter outWriter = null;
		String rex = "",
		       constraName = "",
		       fileName = "";
		String neg_rex = "",
		       neg_constraName = "",
		       neg_fileName = "";

		Gson gson = new GsonBuilder().disableHtmlEscaping()
			    .setPrettyPrinting()
			    .create();
		
		for (Constraint disCon : disCons) {
			System.out.printf("Processing %s\n", disCon.toString());

			rex = disCon.getRegularExpression();
			constraName = disCon.toString();
			disCoLTLfEs.put(constraName, disCon.getLTLpfExpression());
			
			disCoREs.put(constraName, rex);

			fileName = String.format("%1$s/%2$s.dot", AUTOMA_DIR, constraName.replaceAll("\\W", "_"));
			System.out.printf("Writing the automaton DOT diagram of %s on %s\n", constraName, fileName);
//			System.out.println(String.format("(%1$s)&([%2$s%3$s%4$s]*)",
//									rex,
//									BASE.toString(),
//									IMPLIED.toString(),
//									TERTIUM_NON_DATUR.toString()));
			outWriter = new PrintWriter(fileName);
			outWriter.print(
					new RegExp(
							String.format("(%1$s)&([%2$s%3$s%4$s]*)",
									rex,
									BASE.toString(),
									IMPLIED.toString(),
									TERTIUM_NON_DATUR.toString())).toAutomaton().toDot());
			outWriter.flush();
			outWriter.close();		
			
			neg_rex = disCon.getViolatingRegularExpression();
			neg_constraName = disCon.toString();
			negDisCoLTLfEs.put(neg_constraName, disCon.getViolatingLTLpfExpression());
			
			negDisCoREs.put(neg_constraName, neg_rex);

			neg_fileName = String.format("%1$s/violated_%2$s.dot", AUTOMA_DIR, constraName.replaceAll("\\W", "_"));
			System.out.printf("Writing the automaton DOT diagram of %s on %s\n", neg_constraName, neg_fileName);
//			System.out.println(String.format("(%1$s)&([%2$s%3$s%4$s]*)",
//									rex,
//									BASE.toString(),
//									IMPLIED.toString(),
//									TERTIUM_NON_DATUR.toString()));
			outWriter = new PrintWriter(neg_fileName);
			outWriter.print(
					new RegExp(
							String.format("(%1$s)&([%2$s%3$s%4$s]*)",
									neg_rex,
									BASE.toString(),
									IMPLIED.toString(),
									TERTIUM_NON_DATUR.toString())).toAutomaton().toDot());
			outWriter.flush();
			outWriter.close();		
			
		}
		
		System.out.printf("Printing all regular expressions on the %s file\n", RES_FILE);
		outWriter = new PrintWriter(RES_FILE);
		outWriter.print(gson.toJson(disCoREs));
		outWriter.flush();
		outWriter.close();
		

		System.out.printf("Printing all LTLf expressions on the %s file\n", LTLFES_FILE);
		outWriter = new PrintWriter(LTLFES_FILE);
		outWriter.print(gson.toJson(disCoLTLfEs));
		outWriter.flush();
		outWriter.close();

		System.out.printf("Printing all regular expressions on the %s file\n", NEG_RES_FILE);
		outWriter = new PrintWriter(NEG_RES_FILE);
		outWriter.print(gson.toJson(negDisCoREs));
		outWriter.flush();
		outWriter.close();
		

		System.out.printf("Printing all LTLf expressions on the %s file\n", NEG_LTLFES_FILE);
		outWriter = new PrintWriter(NEG_LTLFES_FILE);
		outWriter.print(gson.toJson(negDisCoLTLfEs));
		outWriter.flush();
		outWriter.close();

//		ProcessSpecification proSpec =
//			new ProcessSpecificationEncoderDecoder()
////		/* Alternative 1: load from file. Uncomment the following line to use this method. */ 
////			.readFromJsonFile(new File("/home/cdc08x/Code/MINERful/temp/BPIC2012-disco.json"));
////		/* Alternative 2: load from a (minimal) string version of the JSON specification. Uncomment the following line to use this method. */ 
//			.readFromJsonString(processJsonMin);
//		
//		/*
//		 * Specifies the parameters used to create the automaton
//		 */		
//		OutputSpecificationParameters outParams = new OutputSpecificationParameters();
//		outParams.fileToSaveDotFileForAutomaton = OUTPUT_DOT_FILE;
//		
//		// With the following command, the DOT file is stored directly in the output file.
//		new MinerFulOutputManagementLauncher().manageOutput(proSpec, outParams);
//		
//		// If you prefer to retain the DOT string in memory:
//		ConstraintsPrinter cPrin = new ConstraintsPrinter(proSpec);
//		String dotString = cPrin.printDotAutomaton();
//		System.out.println(dotString);  // Prints out the whole DOT file
		
		System.out.println("Done");
		
		System.exit(0);
	}
}
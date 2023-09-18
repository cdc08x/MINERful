/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.stringsmaker.params;

import java.io.File;

import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.logmaker.params.LogMakerParameters;
import minerful.params.ParamsManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class StringTracesMakerCmdParameters extends ParamsManager {
	public static final String OUTPUT_FILE_PARAM_NAME = "oLF";
    public static final String OUT_ENC_PARAM_NAME = "oE";
	public static final char SIZE_PARAM_NAME = 'L';
	public static final char MAX_LEN_PARAM_NAME = 'M';
	public static final char MIN_LEN_PARAM_NAME = 'm';
	public static final char ALPHABET_PARAM_NAME = 'a';
	public static final char REG_EXPS_PARAM_NAME = 'r';

	public static final String ALPHABET_CHARACTERS_SEPARATOR = ":";
    public static final Character[] TEST_ALPHABET = 
            {'n', 'p', 'r', 'c'};
    public static final String TEST_REGEXP =
            //      "[bcdef]*((a[acdef]*b)|(b[bcdef]*a))+[bcdef]*";
            //      "[bc]*((a[ac]*b)|(b[bc]*a))+[b]*";

            "("
            +
            // 		"[prc]*(n[prc]*)+[prc]*" + // Participation(n)
            //    	")&(" +
            //		"[prc]*(n)?[prc]*" + // Once(n)
            //		")&(" +
            //		"[nprc]*n" + // End(n)
            //   	")&(" +
            "[rc]*(p[nprc]*n)*[rc]*" + // Succession(p, n)
            //   	")&(" +
            //		"[npc]*(r[nprc]p)*[npc]*" + // Response(r, p)
            //    	")&(" +
            //    	"[npr]*((c[nprc]*p)|(p[nprc]*c))*[npr]*" + // RespondedExistence(c, p)
            //     	"[npr]*(r[npr]*c)*[npr]*" + // AlternatePrecedence(r, c); ^[^s]*(r[^s]*s)*[^s]*$
            // 		"n[nrc]+[nprc]+c" +
            ")";
    public static final Long DEFAULT_SIZE = 100L;
    public static final Integer DEFAULT_MIN_TRACE_LENGTH = 0;
    public static final Integer DEFAULT_MAX_TRACE_LENGTH = Integer.MAX_VALUE;
            
    public String[] regexps;
    public Character[] alphabet;
    public Integer minChrsPerString;
    public Integer maxChrsPerString;
    public Long size;
    public File logFile;
    public LogMakerParameters.Encoding outputEncoding;
    
    public StringTracesMakerCmdParameters() {
    	super();
    	regexps = new String[]{TEST_REGEXP};
        alphabet = TEST_ALPHABET;
        minChrsPerString = DEFAULT_MIN_TRACE_LENGTH;
        maxChrsPerString = DEFAULT_MAX_TRACE_LENGTH;
        size = DEFAULT_SIZE;
        logFile = null;
        outputEncoding = LogMakerParameters.Encoding.strings;
    }
    
    public StringTracesMakerCmdParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public StringTracesMakerCmdParameters(String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
        // validate that block-size has been set
        this.regexps = line.getOptionValues(StringTracesMakerCmdParameters.REG_EXPS_PARAM_NAME);
        if (this.regexps == null)
            this.regexps = new String[]{TEST_REGEXP};
        if (line.getOptionValues(StringTracesMakerCmdParameters.ALPHABET_PARAM_NAME) != null) {
            this.alphabet = TaskCharEncoderDecoder.faultyEncode(line.getOptionValue(StringTracesMakerCmdParameters.ALPHABET_PARAM_NAME).toString().split(ALPHABET_CHARACTERS_SEPARATOR));
        }
        this.minChrsPerString =
        		Integer.valueOf(
        				line.getOptionValue(StringTracesMakerCmdParameters.MIN_LEN_PARAM_NAME,this.minChrsPerString.toString()));
        this.maxChrsPerString =
        		Integer.valueOf(
        				line.getOptionValue(StringTracesMakerCmdParameters.MAX_LEN_PARAM_NAME, this.maxChrsPerString.toString()));
        this.size =
        		Long.valueOf(line.getOptionValue(StringTracesMakerCmdParameters.SIZE_PARAM_NAME, this.size.toString()));
        this.outputEncoding = Enum.valueOf(
        		LogMakerParameters.Encoding.class,
        		line.getOptionValue(OUT_ENC_PARAM_NAME, this.outputEncoding.toString())
		);
       	this.logFile = openOutputFile(line, OUTPUT_FILE_PARAM_NAME);
	}
    
	@Override
    public Options addParseableOptions(Options options) {
		Options myOptions = listParseableOptions();
		for (Object myOpt: myOptions.getOptions())
			options.addOption((Option)myOpt);
        return options;
	}
	
	@Override
    public Options listParseableOptions() {
    	return parseableOptions();
    }
	@SuppressWarnings("static-access")
	public static Options parseableOptions() {
		Options options = new Options();
        options.addOption(
                Option.builder(String.valueOf(StringTracesMakerCmdParameters.REG_EXPS_PARAM_NAME))
                        .hasArgs().argName("reg exp list")
                        .longOpt("regexp")
                        .desc("unbound regular expressions list generating the strings (in conjunction)")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(String.valueOf(StringTracesMakerCmdParameters.ALPHABET_PARAM_NAME))
                        .hasArg().argName("alphabet")
                        .longOpt("alphabet")
                        .desc("\"" + ALPHABET_CHARACTERS_SEPARATOR + "\"-separated list of characters in the alphabet (e.g., a:b:c)")
                        .build()
        );
        options.addOption(
                Option.builder(String.valueOf(StringTracesMakerCmdParameters.MIN_LEN_PARAM_NAME))
                        .hasArg().argName("min_length")
                        .longOpt("minlen")
                        .desc("minimum length of the generated strings")
                        .type(Integer.class)
                        .build()
        );
        options.addOption(
                Option.builder(String.valueOf(StringTracesMakerCmdParameters.MAX_LEN_PARAM_NAME))
                        .hasArg().argName("max_length")
                        .longOpt("maxlen")
                        .desc("maximum length of the generated strings")
                        .type(Integer.class)
                        .build()
        );
        options.addOption(
                Option.builder(String.valueOf(StringTracesMakerCmdParameters.SIZE_PARAM_NAME))
                        .hasArg().argName("number of strings")
                        .longOpt("size")
                        .desc("number of strings to run on")
                        .type(Double.class)
                        .build()
        );
        options.addOption(
                Option.builder(StringTracesMakerCmdParameters.OUTPUT_FILE_PARAM_NAME)
                        .hasArg().argName("file path")
                        .longOpt("out-log")
                        .desc("path to the file to write the log in")
                        .type(String.class)
                        .build()
    	);
        options.addOption(
                Option.builder(StringTracesMakerCmdParameters.OUT_ENC_PARAM_NAME)
                        .hasArg().argName("encoding")
                        .longOpt("out-enc")
                        .desc("encoding language for output log " + printValues(LogMakerParameters.Encoding.values()))
                        .type(String.class)
                        .build()
    	);
        
        return options;
    }

    public int getNumberOfConstraints() {
        return this.regexps.length;
    }
    
    public String printAlphabet() {
        StringBuilder alphabetStringBuffer = new StringBuilder();
        alphabetStringBuffer.append("{");
        for (Character chr: this.alphabet) {
            alphabetStringBuffer.append(chr);
            alphabetStringBuffer.append(", ");
        }
        return alphabetStringBuffer.substring(0, alphabetStringBuffer.length() -2) + "}";
    }
    
    public String printMaxChrsPerString() {
        return (this.isMaxChrsPerStringGiven() ? String.valueOf(this.maxChrsPerString) : "*");
    }
    public String printMinChrsPerString() {
        return (this.isMinChrsPerStringGiven() ? String.valueOf(this.minChrsPerString) : "0");
    }
    public String printRegExps() {
        StringBuffer regExpsStringBuffer = new StringBuffer();
        regExpsStringBuffer.append("\n{");
        for (String re: this.regexps) {
            regExpsStringBuffer.append("\n\t");
            regExpsStringBuffer.append(re);
            regExpsStringBuffer.append(",");
        }
        return regExpsStringBuffer.substring(0, regExpsStringBuffer.length() -1) + "\n}";
    }
    
    public boolean isMinChrsPerStringGiven() {
        return (this.minChrsPerString > 0);
    }

    public boolean isMaxChrsPerStringGiven() {
        return (this.maxChrsPerString < Integer.MAX_VALUE);
    }
}
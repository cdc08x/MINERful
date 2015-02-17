package minerful;

import java.util.Properties;

import minerful.params.SystemCmdParameters;

import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public abstract class AbstractMinerFulStarter {

    protected static Logger logger;
    
    public abstract Options setupOptions();
   
    public static void configureLogging(SystemCmdParameters.DebugLevel debugLevel) {
    	String threshold = "ALL";
    	switch (debugLevel) {
    	case none:
            threshold = "INFO";
            break;
    	case info:
    		threshold = "INFO";
    		break;
    	case all:
    		threshold = "ALL";
    		break;
        case trace:
            threshold = "TRACE";
            break;
    	case debug:
            threshold = "DEBUG";
            break;
    	default:
    		break;
    	}

        Properties debugProperties = new Properties();
        debugProperties.setProperty("log4j.rootLogger", "ALL, A1");
        debugProperties.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        debugProperties.setProperty("log4j.appender.A1.Threshold", threshold);
        debugProperties.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
        debugProperties.setProperty("log4j.appender.A1.layout.ConversionPattern", "%p [%t] %c{2} (%M:%L) - %m%n");
        PropertyConfigurator.configure(debugProperties);

        if (logger == null)
    		logger = Logger.getLogger(AbstractMinerFulStarter.class.getCanonicalName());
    }
}

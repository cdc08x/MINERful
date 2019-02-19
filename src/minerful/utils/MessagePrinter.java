package minerful.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Properties;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import minerful.params.SystemCmdParameters;

public class MessagePrinter {
	Logger logger = null;
	
	static DecimalFormat df = new DecimalFormat("0");
	static { df.setMaximumFractionDigits(16); }

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
        debugProperties.setProperty("log4j.rootLogger", threshold + ", A1");
        debugProperties.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        debugProperties.setProperty("log4j.appender.A1.Threshold", threshold);
        debugProperties.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
        debugProperties.setProperty("log4j.appender.A1.layout.ConversionPattern", "%p [%t] %c{2} (%M:%L) - %m%n");
        PropertyConfigurator.configure(debugProperties);
    }
    
    protected MessagePrinter(Class<?> invokingClass) {
    	this.logger = Logger.getLogger(invokingClass);
    }
    protected MessagePrinter(Object invokingObject) {
    	this(invokingObject.getClass());
    }
	
	public static MessagePrinter getInstance(Object source) {
		return new MessagePrinter(source.getClass());
	}
	
	public static MessagePrinter getInstance(Class<?> invokingClass) {
		return new MessagePrinter(invokingClass);
	}

	public static void printlnOut(String s) {
		System.out.println(s);
	}
	public static void printlnOut() {
		System.out.println();
	}
	public static void printOut(String s) {
		System.out.print(s);
	}
	public static void printlnError(String s) {
		System.err.println(s);
	}
	
	public static String formatFloatNumForCSV(float num) {
		return df.format(num);
	}
	public static String formatFloatNumForCSV(double num) {
		return df.format(num);
	}
	
	public void info(String message) {
		this.logger.info(message);
	}
	public void info(String pattern, Object... params) {
		LogMF.info(this.logger, pattern, params);
	}
	public void warn(String message) {
		this.logger.warn(message);
	}
	public void warn(String pattern, Object... params) {
		LogMF.warn(this.logger, pattern, params);
	}
	public void debug(String message) {
		this.logger.debug(message);
	}
	public void debug(String pattern, Object... params) {
		LogMF.debug(this.logger, pattern, params);
	}
	public void trace(String message) {
		this.logger.trace(message);
	}
	public void trace(String pattern, Object... params) {
		LogMF.trace(this.logger, pattern, params);
	}
	public void error(String message) {
		this.logger.error(message);
	}
	public void error(String pattern, Object... params) {
		LogMF.error(this.logger, pattern, params);
	}
	public void error(String message, Throwable e) {
		this.logger.error(message, e);
	}
	public void error(String pattern, Throwable e, Object... params) {
		LogMF.error(this.logger, e, pattern, params);
	}
	public void fatal(String message) {
		this.logger.fatal(message);
	}
	public void fatal(String pattern, Object... params) {
		LogMF.fatal(this.logger, pattern, params);
	}
	public void fatal(String message, Throwable e) {
		this.logger.fatal(message, e);
	}
	public void fatal(String pattern, Throwable e, Object... params) {
		LogMF.fatal(this.logger, e, pattern, params);
	}
}
package ca.concordia.jdeodorant.eclipse.commandline.utility;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

public class FileLogger {
	
	private static Logger logBackLogger = null;
	private static LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
	private static List<FileAppender<ILoggingEvent>> appenders = new ArrayList<>();
	
	public static org.slf4j.Logger getLogger(Class<?> clazz) {
	
	 	logBackLogger = loggerContext.getLogger(clazz.getName());
	 	
	 	if (appenders.size() > 0)
	 		for (FileAppender<ILoggingEvent> appender : appenders)
	 			logBackLogger.addAppender(appender);
	    
	    return logBackLogger;
		
	}
	
	public static void addFileAppender(String filePath, boolean append) {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
	    FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
	    fileAppender.setContext(loggerContext);
	    fileAppender.setName("css-analyzer");
	    // set the file name
	    fileAppender.setFile(filePath);
	    fileAppender.setAppend(append);

	    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
	    encoder.setContext(loggerContext);
	    //encoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level - %msg [%C{1}]%n");
	    encoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level - %msg%n");
	    encoder.start();

	    fileAppender.setEncoder(encoder);
	    fileAppender.start();

	    appenders.add(fileAppender);
	    
	    logBackLogger.addAppender(fileAppender);
	}
}

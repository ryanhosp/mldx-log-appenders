package com.dfkjtech.mldx;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

import com.dfkjtech.mldx.s3.RollingFileS3Appender;

public class AppenderUtil {
	
	/**
	 * Calls the rollOver event on all MLDX Appenders.
	 * This flushes the log contents to the external secondary store (e.g. S3).
	 * Typically this is called on shutdown of server.
	 * 
	 * @param loggerName: Use same name as configured in log4j logger configuration.
	 */
	public static void rollOverAppenders(String loggerName) {
		Logger logger = Logger.getLogger(loggerName);
		@SuppressWarnings("rawtypes")
		Enumeration appenders = logger.getAllAppenders();
		while(appenders.hasMoreElements()){
			Appender appender = (Appender) appenders.nextElement();
			if(appender instanceof RollingFileS3Appender) {
				((RollingFileS3Appender) appender).rollOver();
			}
		}		
	}
}

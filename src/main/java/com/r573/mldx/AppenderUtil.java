/*
 * MLDX Log Appenders
 * Project hosted at https://github.com/ryanhosp/mldx-log-appenders/
 * Copyright 2012 - 2013 Ho Siaw Ping Ryan
 *    
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.r573.mldx;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

import com.r573.mldx.s3.RollingFileS3Appender;

public class AppenderUtil {
	
	/**
	 * Calls the rollOver event on all MLDX Appenders.
	 * This flushes the log contents to the external secondary store (e.g. S3).
	 * Typically this is called on shutdown of server.
	 * 
	 * @param loggerName: Use same name as configured in log4j logger configuration.
	 * @param synchronousUpload: Perform the uploading synchronously or not.
	 */
	public static void rollOverAppenders(String loggerName, boolean synchronousUpload) {
		Logger logger = Logger.getLogger(loggerName);
		@SuppressWarnings("rawtypes")
		Enumeration appenders = logger.getAllAppenders();
		while(appenders.hasMoreElements()){
			Appender appender = (Appender) appenders.nextElement();
			if(appender instanceof RollingFileS3Appender) {
				((RollingFileS3Appender) appender).rollOver(synchronousUpload);
			}
		}		
	}
}

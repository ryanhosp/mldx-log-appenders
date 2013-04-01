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
package com.r573.mldx.logger.logback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TriggeringPolicy;
import ch.qos.logback.core.util.FileSize;

import com.r573.mldx.logger.log4j.Log4JInternalLogger;
import com.r573.mldx.s3.credentials.AbstractCredentialsProvider;
import com.r573.mldx.s3.credentials.EnvironmentCredentialsProvider;
import com.r573.mldx.util.AbstractInternalLogger;
import com.r573.mldx.util.Constants;
import com.r573.mldx.util.LogFileHelper;

public class RollingFileS3Appender<E> extends RollingFileAppender<E> {
			
	private AbstractCredentialsProvider credentialsProviderInstance;
	private AbstractInternalLogger internalLogger;
	
	private String bucket;

	// Hardcoded to environment credentials provider for now
	private String credentialsProvider = EnvironmentCredentialsProvider.class.getName();
	
	private HashMap<String, String> credentialsProviderParams = new HashMap<String, String>();
	
	public void rollover(boolean synchronousUpload) {
		FixedWindowRollingPolicy rollingPolicy = (FixedWindowRollingPolicy)getRollingPolicy();
		int maxBackupIndex = rollingPolicy.getMaxIndex();
		
		final ArrayList<File> uploadFileList = new ArrayList<File>();
		// before rollover, look for existing backups (there should not be any but doing this as a precautionary measure)
		// and prepare them for upload.
		LogFileHelper.compressFilesForUploading(uploadFileList, fileName, maxBackupIndex, Constants.BUFFER_SIZE);
		
		super.rollover();

		// one file will be rolled over, prepare that for copying as well
		LogFileHelper.compressFilesForUploading(uploadFileList, fileName, maxBackupIndex, Constants.BUFFER_SIZE);
		
		LogFileHelper.uploadFiles(uploadFileList, getBucket(), getCredentialsProviderInstance(), synchronousUpload, getInternalLoggerInstance());
		
	}
	
	@Override
	public void rollover() {
		// by default, perform the rollover and S3 upload asynchronously
		rollover(false);
	}

	// this is not initialized in the constructor because the parameters it depends on may not have been read yet	
	private AbstractCredentialsProvider getCredentialsProviderInstance(){
		if(credentialsProviderInstance == null) {
			if(credentialsProviderParams.size() == 0){
				credentialsProviderParams.put("accessKeyEnvVarName", "AWS_ACCESS_KEY");
				credentialsProviderParams.put("secretKeyEnvVarName", "AWS_SECRET_KEY");
			}
			credentialsProviderInstance = LogFileHelper.getCredentialsProvider(credentialsProvider,credentialsProviderParams, getInternalLoggerInstance());
		}
		return credentialsProviderInstance;
	}
	
	// this is not initialized in the constructor because in some cases, static dependencies may not have been initialized yet 
	private AbstractInternalLogger getInternalLoggerInstance() {
		if(internalLogger == null) {
			internalLogger = new Log4JInternalLogger();
		}
		return internalLogger;
	}
	
	@Override
	public void setRollingPolicy(RollingPolicy policy) {
		FixedWindowRollingPolicy fixedRollingPolicy = (FixedWindowRollingPolicy) policy;
		if(fixedRollingPolicy.getMaxIndex() < 1) {
			throw new IllegalArgumentException("maxBackupIndex needs to be 1 or more.");
		}
		super.setRollingPolicy(policy);
	}

	@Override
	public void setTriggeringPolicy(TriggeringPolicy<E> policy) {
		SizeBasedTriggeringPolicy<E> sizeBasedPolicy = (SizeBasedTriggeringPolicy<E>) policy;
		FileSize maxFileSize = FileSize.valueOf(sizeBasedPolicy.getMaxFileSize());
		if(maxFileSize.getSize() > Constants.FILE_SIZE_LIMIT) {
			throw new IllegalArgumentException("Exceeded file size limit " + Constants.FILE_SIZE_LIMIT);
		}
		super.setTriggeringPolicy(policy);
	}

	private String getBucket() {
		if(bucket == null){
			bucket = getContext().getProperty("BUCKET");
		}
		return bucket;
	}
}

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
package com.r573.mldx.logger.log4j;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.RollingFileAppender;

import com.r573.mldx.s3.credentials.AbstractCredentialsProvider;
import com.r573.mldx.util.AbstractInternalLogger;
import com.r573.mldx.util.Constants;
import com.r573.mldx.util.LogFileHelper;

public class RollingFileS3Appender extends RollingFileAppender {		
	private AbstractCredentialsProvider credentialsProviderInstance;
	private AbstractInternalLogger internalLogger;
	
	private String bucket;
	private String credentialsProvider;
	
	private HashMap<String, String> credentialsProviderParams = new HashMap<String, String>();
		
	public void rollOver(boolean synchronousUpload) {
		final ArrayList<File> uploadFileList = new ArrayList<File>();
		// before rollover, look for existing backups (there should not be any but doing this as a precautionary measure)
		// and prepare them for upload.
		LogFileHelper.compressFilesForUploading(uploadFileList, fileName, maxBackupIndex, Constants.BUFFER_SIZE);
		
		super.rollOver();

		// one file will be rolled over, prepare that for copying as well
		LogFileHelper.compressFilesForUploading(uploadFileList, fileName, maxBackupIndex, Constants.BUFFER_SIZE);
		
		LogFileHelper.uploadFiles(uploadFileList, bucket, getCredentialsProviderInstance(), synchronousUpload, getInternalLoggerInstance());
	}
	
	@Override
	public void rollOver() {
		// by default, perform the rollover and S3 upload asynchronously
		rollOver(false);
	}
	
	// this is not initialized in the constructor because the parameters it depends on may not have been read yet	
	private AbstractCredentialsProvider getCredentialsProviderInstance(){
		if(credentialsProviderInstance == null) {
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
	public void setMaxFileSize(String value) {
		super.setMaxFileSize(value);
		// the parent setter converts the String value to a long value, so let the parent run first
		checkMaxFileSize();
	}

	private void checkMaxFileSize() {
		if(maxFileSize > Constants.FILE_SIZE_LIMIT) {
			throw new IllegalArgumentException("Exceeded file size limit " + Constants.FILE_SIZE_LIMIT);
		}
	}

	@Override
	public void setMaximumFileSize(long maxFileSize) {
		checkMaxFileSize();
		super.setMaximumFileSize(maxFileSize);
	}

	@Override
	public void setMaxBackupIndex(int maxBackups) {
		if(maxBackupIndex <1){
			throw new IllegalArgumentException("maxBackupIndex needs to be 1 or more.");
		}
		super.setMaxBackupIndex(maxBackups);
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	
	public String getCredentialsProvider() {
		return credentialsProvider;
	}

	public void setCredentialsProvider(String credentialsProvider) {
		this.credentialsProvider = credentialsProvider;
	}

	/* ------------------------------------------------
	 *  for env var encrypted file credentials provider
	 */
	
	public String getAccessKeyEnvVarName() {
		return credentialsProviderParams.get("accessKeyEnvVarName");
	}

	public void setAccessKeyEnvVarName(String accessKeyEnvVarName) {
		credentialsProviderParams.put("accessKeyEnvVarName",accessKeyEnvVarName);
	}

	public String getSecretKeyEnvVarName() {
		return credentialsProviderParams.get("secretKeyEnvVarName");
	}

	public void setSecretKeyEnvVarName(String secretKeyEnvVarName) {
		credentialsProviderParams.put("secretKeyEnvVarName",secretKeyEnvVarName);
	}

	/* for env var credentials provider
	 * ------------------------------------------------
	 */

	
	
	/* ------------------------------------------------
	 *  for jets3t encrypted file credentials provider
	 */
	
	public String getCredentialsFilePath() {
		return credentialsProviderParams.get("credentialsFilePath");
	}

	public void setCredentialsFilePath(String credentialsFilePath) {
		credentialsProviderParams.put("credentialsFilePath",credentialsFilePath);
	}

	public String getCredentialsFilePassword() {
		return credentialsProviderParams.get("credentialsFilePassword");
	}

	public void setCredentialsFilePassword(String credentialsFilePassword) {
		credentialsProviderParams.put("credentialsFilePassword",credentialsFilePassword);
	}

	/* for jets3t encrypted file credentials provider
	 * ------------------------------------------------
	 */

}

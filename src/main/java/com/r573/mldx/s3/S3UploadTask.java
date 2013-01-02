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
package com.r573.mldx.s3;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.apache.log4j.helpers.LogLog;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.security.ProviderCredentials;

import com.r573.mldx.s3.credentials.AbstractCredentialsProvider;

public class S3UploadTask implements Runnable {
	private ProviderCredentials awsCredentials;
	private ArrayList<File> uploadFileList;
	private String bucket;
	
	public S3UploadTask(ArrayList<File> uploadFileList, String bucket, AbstractCredentialsProvider credentialsProvider){
		this.uploadFileList = uploadFileList;
		this.bucket = bucket;
		if(credentialsProvider == null) {
			awsCredentials = null;
		}
		else {
			awsCredentials = credentialsProvider.getAWSCredentials();
		}
	}

	@Override
	public void run() {
		if(awsCredentials == null) {
			LogLog.error("No available AWS credentials, file will not be uploaded.");
			return;
		}
		RestS3Service s3Client;
		try {
			s3Client = new RestS3Service(awsCredentials);
			for(File uploadFile : uploadFileList) {
				StorageObject storageObject = new StorageObject(uploadFile);
				s3Client.putObject(bucket, storageObject);
				uploadFile.delete();
			}
		}
		catch (ServiceException e) {
			LogLog.error("Exception uploading file to S3 " + e.getClass().getName() + ", " + e.getMessage(),e);
		}
		catch (IOException e) {
			LogLog.error("Exception uploading file to S3 " + e.getClass().getName() + ", " + e.getMessage(),e);
		}
		catch (NoSuchAlgorithmException e) {
			LogLog.error("Exception uploading file to S3 " + e.getClass().getName() + ", " + e.getMessage(),e);
		}
	}
}

/*
 * Copyright 2012 DFKJ Technologies Pte Ltd
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
package com.dfkjtech.mldx.s3;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.security.ProviderCredentials;

import com.dfkjtech.mldx.s3.credentials.AbstractCredentialsProvider;

public class S3UploadTask implements Runnable {
	private ProviderCredentials awsCredentials;
	private ArrayList<File> uploadFileList;
	private String bucket;
	
	public S3UploadTask(ArrayList<File> uploadFileList, String bucket, AbstractCredentialsProvider credentialsProvider){
		this.uploadFileList = uploadFileList;
		this.bucket = bucket;
		awsCredentials = credentialsProvider.getAWSCredentials();
	}

	@Override
	public void run() {
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
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}

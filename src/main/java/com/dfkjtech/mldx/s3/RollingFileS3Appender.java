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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.RollingFileAppender;

import com.dfkjtech.mldx.s3.credentials.AbstractCredentialsProvider;

public class RollingFileS3Appender extends RollingFileAppender {
	// 3.5 billion. ZIP limit is 4 billion. Some buffer to be safe.
	public static final long FILE_SIZE_LIMIT = 3500000000L;
	// buffer of 10MB
	private static final int BUFFER_SIZE = 10000000;
		
	private AbstractCredentialsProvider credentialsProviderInstance;
	
	private String bucket;
	private String credentialsProvider;
	
	private HashMap<String, String> credentialsProviderParams = new HashMap<String, String>();
		
	public void rollOver(boolean synchronousUpload) {
		final ArrayList<File> uploadFileList = new ArrayList<File>();
		// before rollover, rename all backup files (if any) for copying
		prepareFilesForUploading(uploadFileList);
		
		super.rollOver();

		// one file will be rolled over, prepare that for copying as well
		prepareFilesForUploading(uploadFileList);
		
		S3UploadTask uploadTask = new S3UploadTask(uploadFileList, bucket, getCredentialsProviderInstance());
		if(synchronousUpload) {
			uploadTask.run();
		}
		else {
			Thread t = new Thread(uploadTask);
			t.start();
		}
	}
	
	@Override
	public void rollOver() {
		// by default, perform the rollover and S3 upload asynchronously
		rollOver(false);
	}
	
	private AbstractCredentialsProvider getCredentialsProviderInstance(){
		if(credentialsProviderInstance == null) {
			try {
				credentialsProviderInstance = (AbstractCredentialsProvider) Class.forName(credentialsProvider).newInstance();
				credentialsProviderInstance.setParams(credentialsProviderParams);
			}
			catch (InstantiationException e) {
				// TODO: improve exception handling
				throw new RuntimeException(e);
			}
			catch (IllegalAccessException e) {
				// TODO: improve exception handling
				throw new RuntimeException(e);
			}
			catch (ClassNotFoundException e) {
				// TODO: improve exception handling
				throw new RuntimeException(e);
			}
		}
		return credentialsProviderInstance;
	}

	private void prepareFilesForUploading(final ArrayList<File> uploadFileList) {
		for(int i = 1; i <= maxBackupIndex; i++) {
			String thisFileName = fileName + "." + i;
			File backupFile = new File(thisFileName);
			
			String fileNameWithoutDir = backupFile.getName();
			if(backupFile.exists()) {
				String copyFileName = thisFileName + "." + UUID.randomUUID().toString() + ".zip";
				
				BufferedInputStream bis = null;
	            ZipOutputStream zos = null;
	            try {
	    			File copyFile = new File(copyFileName);                     
	    			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(copyFile),BUFFER_SIZE);
	    			zos = new ZipOutputStream(bos);
	    			zos.setLevel(9);
	    			
	    			ZipEntry zipEntry = new ZipEntry(fileNameWithoutDir);
	    			zos.putNextEntry(zipEntry);
	    			
	    			FileInputStream fis = new FileInputStream(backupFile);
	                bis = new BufferedInputStream(fis,BUFFER_SIZE);
	                
	                byte[] buf = new byte[BUFFER_SIZE];
	                int bytesRead = 0;
	                while((bytesRead = bis.read(buf)) != -1) {
	                	if(bytesRead < BUFFER_SIZE) {
	                		byte[] buf2 = new byte[bytesRead];
	                		System.arraycopy(buf, 0, buf2, 0, bytesRead);
	                		buf = buf2;
	                	}
	                	zos.write(buf);
	                	buf = new byte[BUFFER_SIZE];
	                }
	                zos.closeEntry();
	                bis.close();
					backupFile.delete();
					uploadFileList.add(copyFile);	
	            }
	            catch(IOException e) {
	            	throw new RuntimeException(e);
	            }
	            finally{
					try {
						if (bis != null) {
							bis.close();
						}
						if (zos != null) {
							zos.close();
						}
					} catch (IOException e) {
						// nothing needs to be done here
					}	
	            }
			}
		}
	}
	
	@Override
	public void setMaxFileSize(String value) {
		super.setMaxFileSize(value);
		// the parent setter converts the String value to a long value, so let the parent run first
		checkMaxFileSize();
	}

	private void checkMaxFileSize() {
		if(maxFileSize > FILE_SIZE_LIMIT) {
			throw new IllegalArgumentException("Exceeded file size limit " + FILE_SIZE_LIMIT);
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

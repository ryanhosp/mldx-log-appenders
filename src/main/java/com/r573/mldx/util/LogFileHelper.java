package com.r573.mldx.util;

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

import com.r573.mldx.s3.S3UploadTask;
import com.r573.mldx.s3.credentials.AbstractCredentialsProvider;

public class LogFileHelper {
	
	public static void uploadFiles(ArrayList<File> uploadFileList, String bucket, AbstractCredentialsProvider credentialsProvider, boolean synchronousUpload, AbstractInternalLogger internalLogger){
		try {
			S3UploadTask uploadTask = new S3UploadTask(uploadFileList, bucket, credentialsProvider, internalLogger);
			if(synchronousUpload) {
				uploadTask.run();
			}
			else {
				Thread t = new Thread(uploadTask);
				t.start();
			}			
		}
		catch(Exception e){
			internalLogger.logError("Error uploading files", e);
		}
	}
	
	public static void compressFilesForUploading(ArrayList<File> uploadFileList, String fileName, int maxBackupIndex, int bufferSize) {
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
	    			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(copyFile),bufferSize);
	    			zos = new ZipOutputStream(bos);
	    			zos.setLevel(9);
	    			
	    			ZipEntry zipEntry = new ZipEntry(fileNameWithoutDir);
	    			zos.putNextEntry(zipEntry);
	    			
	    			FileInputStream fis = new FileInputStream(backupFile);
	                bis = new BufferedInputStream(fis,bufferSize);
	                
	                byte[] buf = new byte[bufferSize];
	                int bytesRead = 0;
	                while((bytesRead = bis.read(buf)) != -1) {
	                	if(bytesRead < bufferSize) {
	                		byte[] buf2 = new byte[bytesRead];
	                		System.arraycopy(buf, 0, buf2, 0, bytesRead);
	                		buf = buf2;
	                	}
	                	zos.write(buf);
	                	buf = new byte[bufferSize];
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
	
	public static AbstractCredentialsProvider getCredentialsProvider(String credentialsProvider,HashMap<String, String> credentialsProviderParams, AbstractInternalLogger internalLogger){
		try {
			AbstractCredentialsProvider credentialsProviderInstance = (AbstractCredentialsProvider) Class.forName(credentialsProvider).newInstance();
			credentialsProviderInstance.setParams(credentialsProviderParams);
			return credentialsProviderInstance;
		}
		catch (InstantiationException e) {
			internalLogger.logError("Error getting credentials provider " + credentialsProvider, e);
			return null;
		}
		catch (IllegalAccessException e) {
			internalLogger.logError("Error getting credentials provider " + credentialsProvider, e);
			return null;
		}
		catch (ClassNotFoundException e) {
			internalLogger.logError("Error getting credentials provider " + credentialsProvider, e);
			return null;
		}
	}
}

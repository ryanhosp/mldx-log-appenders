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

package com.dfkjtech.mldx.s3.credentials;

import java.util.HashMap;

import org.jets3t.service.security.ProviderCredentials;

public abstract class AbstractCredentialsProvider {
	protected HashMap<String, String> params;

	public HashMap<String, String> getParams() {
		return params;
	}
	public void setParams(HashMap<String, String> params) {
		this.params = params;
	}
	
	public abstract ProviderCredentials getAWSCredentials();
}

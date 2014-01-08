/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redpill_linpro.springframework.beans.factory.config;

import java.util.Properties;

import jetcd.EtcdClient;
import jetcd.EtcdClientFactory;
import jetcd.EtcdException;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * Subclass of PropertyPlaceholderConfigurer that supports loading properties 
 * from a etcd key-value store.
 *
 * <p>Tries to resolve placeholders as keys in the given base directory. Default is no directory.
 * </p>
 *
 * @author Pontus Ullgren
 * @see #setServerUrl
 * @see #setBaseDirectory
 */
public class EtcdPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements InitializingBean {

	private EtcdClient client;
        private String serverUrl;
        private String baseDirectory = "";

	/**
	 * Set the URL to the etcd server
	 */
	public void setServerUrl(String serverUrl) {
		this.client = EtcdClientFactory.newInstance(serverUrl);
	}

	/**
	 * Sets the base directory to used to resolve the 
	 * placeholders.
	 */
	public void setBaseDirectory(String baseDirectory) {
		if ( baseDirectory != null && !baseDirectory.endsWith("/") ) {
			baseDirectory += baseDirectory+"/";
		}
		this.baseDirectory = baseDirectory;
	}

	/**
	 */
	@Override
	public void afterPropertiesSet() {
		if ( this.client == null) {
			throw new IllegalArgumentException("serverUrl is required");
                }
	}

	/**
	 * This implementation tries to resolve placeholders first by querying the etcd server,
         * then in the passed-in properties.
	 */
	@Override
	protected String resolvePlaceholder(String placeholder, Properties props) {
                String value = null;
		if ( this.client == null ) {
			this.client = createEtcdClient();
		}
		try {
			value = this.client.get(
					this.baseDirectory + placeholder);
		} catch (Exception e) {
			// Ignore any error so we can check in passed-in properties
		}
		if (value == null) {
			value = props.getProperty(placeholder);
		}
		return value;
	}

	/** Used to set a EtcdClient when running unit tests
	 */
	protected void setEtcdClient(EtcdClient client) {
		this.client = client;
        }

	/** Creates a new EtcdClient using the provided serverUrl
	  */
	private EtcdClient createEtcdClient() {
		return EtcdClientFactory.newInstance(this.serverUrl);
        }
}

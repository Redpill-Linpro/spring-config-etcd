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

import java.util.Map;
import java.util.Properties;

import jetcd.EtcdClient;
import jetcd.EtcdClientFactory;
import jetcd.EtcdException;

public class StubEtcdClient implements EtcdClient {
    private Properties properties;

    public StubEtcdClient(Properties properties) {
         this.properties = properties;
    }

    /**
     * Retrieve the value of the given key, if set.
     *
     * @param key Key to look up
     * @return value Value for the given key
     * @throws EtcdException in case of an error (e.g. key doesn't exist)
     */
    public String get(String key) throws EtcdException {
        return (String)this.properties.get(key);
    }

    /**
     * Set value for the given key.
     *
     * @param key Key to set value for
     * @param value New value for the key
     * @throws EtcdException in case of an error
     */
    public void set(String key, String value) throws EtcdException {
       throw new EtcdException(1, "Not implemented", "Not implemented", 0);
    }

    /**
     * Set value for the given key with given TTL.
     *
     * @param key Key to set value for
     * @param value New value for the key
     * @param ttl Key will expire after these many seconds
     * @throws EtcdException in case of an error
     */
    public void set(String key, String value, int ttl) throws EtcdException {
       throw new EtcdException(1, "Not implemented", "Not implemented", 0);
    }

    /**
     * Delete value for the given key.
     *
     * @param key Key to delete value for
     * @throws EtcdException in case of an error
     */
    public void delete(String key) throws EtcdException {
       throw new EtcdException(1, "Not implemented", "Not implemented", 0);
    }

    /**
     * List directory at given path.
     *
     * @param path Given path
     * @return Map of key,value pairs under the path
     * @throws EtcdException in case of an error
     */
    public Map<String, String> list(String path) throws EtcdException {
       throw new EtcdException(1, "Not implemented", "Not implemented", 0);
    }

    /**
     * Compare the value at a given key and swap with newValue if the oldValue
     * matches.
     *
     * @param key Key to test/set value at
     * @param oldValue Old value
     * @param newValue New value
     * @throws EtcdException in case of an error
     */
    public void compareAndSwap(String key, String oldValue, String newValue)
        throws EtcdException {
       throw new EtcdException(1, "Not implemented", "Not implemented", 0);
    }
}

/*
 * Copyright 2002-2013 the original author or authors.
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.registerWithGeneratedName;

import jetcd.EtcdClient;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.tests.sample.beans.TestBean;

/**
 * Unit tests for {@link EtcdPlaceholderConfigurer}.
 *
 */
public class EtcdPlaceholderConfigurerTests {
	private static final String P1 = "p1";
	private static final String D1 = "d1";
	private static final String P1_LOCAL_PROPS_VAL = "p1LocalPropsVal";
	private static final String P1_SYSTEM_PROPS_VAL = "p1SystemPropsVal";
	private static final String P1_SYSTEM_ENV_VAL = "p1SystemEnvVal";
	private static final String D1_DATABASE_VAL = "d1DatabaseVal";

	private DefaultListableBeanFactory bf;
	private EtcdPlaceholderConfigurer epc;
	private Properties jpcProperties;

	private AbstractBeanDefinition p1BeanDef;

	@BeforeClass
	public static void initDb() {			
	}

	@Before
	public void setUp() {
		p1BeanDef = rootBeanDefinition(TestBean.class)
			.addPropertyValue("name", "${"+P1+"}")
			.getBeanDefinition();

		bf = new DefaultListableBeanFactory();

		jpcProperties = new Properties();
		jpcProperties.setProperty(P1, P1_LOCAL_PROPS_VAL);
		System.setProperty(P1, P1_SYSTEM_PROPS_VAL);
		getModifiableSystemEnvironment().put(P1, P1_SYSTEM_ENV_VAL);
		
		epc = new EtcdPlaceholderConfigurer();
		epc.setProperties(jpcProperties);
		Resource resource = new ClassPathResource("JdbcPlaceholderConfigurerTests.properties", this.getClass());
		epc.setLocation(resource);
                epc.setServerUrl("http://127.0.0.1:4001");
                // Set a stub EtcdClient for testing purpose
                Properties etcdProperties = new Properties();
                etcdProperties.setProperty(D1, D1_DATABASE_VAL);
                EtcdClient stubclient = new StubEtcdClient(etcdProperties);
                // 
                epc.setEtcdClient(stubclient);
		epc.afterPropertiesSet();
	}	

	@After
	public void tearDown() {
		System.clearProperty(P1);
		getModifiableSystemEnvironment().remove(P1);
	}

	@Test
	public void databaseProperties() {
		getModifiableSystemEnvironment().put("otherKey", "true");
		p1BeanDef = rootBeanDefinition(TestBean.class)
			.addPropertyValue("name", "${my.name}")
			.addPropertyValue("sex", "${"+D1+"}")
			.addPropertyValue("jedi", "${otherKey}")
			.getBeanDefinition();
		registerWithGeneratedName(p1BeanDef, bf);
		epc.postProcessBeanFactory(bf);
		TestBean bean = bf.getBean(TestBean.class);
		System.err.println(bean.getSex());
		assertThat(bean.getName(), equalTo("foo"));
		assertThat(bean.getSex(), equalTo(D1_DATABASE_VAL));
		assertThat(bean.isJedi(), equalTo(true));
	}

	@SuppressWarnings("unchecked")
	private static Map<String, String> getModifiableSystemEnvironment() {
		// for os x / linux
		Class<?>[] classes = Collections.class.getDeclaredClasses();
		Map<String, String> env = System.getenv();
		for (Class<?> cl : classes) {
			if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
				try {
					Field field = cl.getDeclaredField("m");
					field.setAccessible(true);
					Object obj = field.get(env);
					if (obj != null && obj.getClass().getName().equals("java.lang.ProcessEnvironment$StringEnvironment")) {
						return (Map<String, String>) obj;
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}

		// for windows
		Class<?> processEnvironmentClass;
		try {
			processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		try {
			Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
			theCaseInsensitiveEnvironmentField.setAccessible(true);
			Object obj = theCaseInsensitiveEnvironmentField.get(null);
			return (Map<String, String>) obj;
		} catch (NoSuchFieldException e) {
			// do nothing
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		try {
			Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
			theEnvironmentField.setAccessible(true);
			Object obj = theEnvironmentField.get(null);
			return (Map<String, String>) obj;
		} catch (NoSuchFieldException e) {
			// do nothing
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		throw new IllegalStateException();
	}
}

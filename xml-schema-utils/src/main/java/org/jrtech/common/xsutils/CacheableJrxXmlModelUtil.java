/*
 * Copyright (c) 2016-2026 Jumin Rubin
 * LinkedIn: https://www.linkedin.com/in/juminrubin/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jrtech.common.xsutils;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.xml.sax.SAXException;

import com.google.common.cache.LoadingCache;
import com.sun.xml.xsom.XSSchema;

/**
 * The class <code>CacheableJrxXmlModelUtil</code> is a version of JrxXmlModelUtil with schema caching mechanism to
 * reduce I/O processing of the XSD loading.
 * 
 */
public class CacheableJrxXmlModelUtil extends JrxXmlModelUtil {

	public static final String XSD_CACHE_NAME = "xsdCache";

	public static final String XSD_URL_CACHE_NAME = "xsdUrlCache";

	private static LoadingCache<String, XSSchema> schemaCache = null;

	private static LoadingCache<String, String[]> schemaUrlCache = null;

	public static CacheableJrxXmlModelUtil newInstance() {
		return new CacheableJrxXmlModelUtil();
	}

	protected CacheableJrxXmlModelUtil() {
		super();
	}

	@Override
	public String[] addSchema(URL xmlSchemaUrl) throws SAXException {
		String[] namespaceUriArray = getSchemaUrlCache().getIfPresent(xmlSchemaUrl.toString());
		if (namespaceUriArray == null) {
			namespaceUriArray = super.addSchema(xmlSchemaUrl);
			getSchemaUrlCache().put(xmlSchemaUrl.toString(), namespaceUriArray);
			for (String namespaceUri : namespaceUriArray) {
				XSSchema xsSchema = super.getSchema(namespaceUri);
				getSchemaCache().put(namespaceUri, xsSchema);
			}
		}

		return namespaceUriArray;
	}

	@Override
	public String addSchema(XSSchema xsSchema) {
		String namespaceUri = xsSchema.getTargetNamespace();
		if (!getSchemaCache().asMap().containsKey(namespaceUri)) {
			getSchemaCache().put(namespaceUri, xsSchema);
		}

		return super.addSchema(xsSchema);
	}

	@Override
	public XSSchema getSchema(String namespaceUri) {
		XSSchema xsSchema = super.getSchema(namespaceUri);
		if (xsSchema != null)
			return xsSchema;

		xsSchema = getSchemaCache().getIfPresent(namespaceUri);
		if (xsSchema != null) {
			super.addSchema(xsSchema);
		}

		return xsSchema;
	}

	@Override
	protected boolean hasSchemas() {
		return getSchemaCache().size() > 0;
	}

	@Override
	protected boolean hasSchema(String namespaceUri) {
		return getSchemaCache().asMap().containsKey(namespaceUri);
	}

	protected static LoadingCache<String, XSSchema> getSchemaCache() {
		if (schemaCache != null) {
			return schemaCache;
		}
		
		synchronized (schemaCache) {
			schemaCache = CacheUtil.createCache(200, 300, TimeUnit.SECONDS);
		}
		
		return schemaCache;
	}
	
	protected static LoadingCache<String, String[]> getSchemaUrlCache() {
		if (schemaUrlCache != null) {
			return schemaUrlCache;
		}
		
		synchronized (schemaUrlCache) {
			schemaUrlCache = CacheUtil.createCache(200, 300, TimeUnit.SECONDS);
		}
		
		return schemaUrlCache;
	}
}

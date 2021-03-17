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
package org.jrtech.common.utils;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ResourceLocatorUtil {
    public static final String JAVA_CLASSPATH_PREFIX = "classpath:";

    public static final String JAVA_JAR_FILE_PREFIX = "jar:file:";

    public static final String DEFAULT_BASE_URL = "file:///";

    public static ResourceLocatorUtil newInstance() {
        return new ResourceLocatorUtil();
    }

    public URL resolveUrlFromLocationString(String location) throws MalformedURLException, IOException {
        return resolveUrlFromRelativeLocation("", location);
    }

    public URL resolveUrlFromRelativeLocation(String baseUrl, String relativeLocation) throws MalformedURLException,
            IOException {
        if (baseUrl.equals("")) {
            baseUrl = DEFAULT_BASE_URL;
        }

        if (baseUrl.startsWith(JAVA_CLASSPATH_PREFIX)) {
            if (relativeLocation.startsWith("/")) {
                return getClass().getResource(relativeLocation);
            } else {
                return getClass().getResource(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + relativeLocation);
            }
        }

        return resolveUrlFromRelativeLocation(new URL(baseUrl), relativeLocation);
    }

    public URL resolveUrlFromRelativeLocation(URL baseUrl, String relativeLocation) throws MalformedURLException,
            IOException {
        URL url = null;
        String baseUriString = "";

        if (baseUrl != null)
            baseUriString = baseUrl.toString();

        if (baseUriString.equals("")) {
            baseUrl = new URL(DEFAULT_BASE_URL);
            baseUriString = baseUrl.toString();
        }

        if (relativeLocation.startsWith(JAVA_CLASSPATH_PREFIX)) {
            String classpathLocation = relativeLocation.substring(JAVA_CLASSPATH_PREFIX.length());
            url = getClass().getResource(classpathLocation);
        } else if (relativeLocation.startsWith("file:") || relativeLocation.startsWith("ftp:")
                || relativeLocation.startsWith("http:") || relativeLocation.startsWith("https:")
                || relativeLocation.startsWith("jar:")) {
            url = new URL(relativeLocation);
        } else if (relativeLocation.startsWith("/")
                && (baseUriString.startsWith(JAVA_JAR_FILE_PREFIX) || baseUriString.startsWith(JAVA_CLASSPATH_PREFIX))) {
            url = getClass().getResource(relativeLocation);
        } else {
            url = new URL(baseUrl, relativeLocation);
        }

        return url;
    }

}

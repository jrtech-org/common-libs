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
package org.jrtech.common.preferences;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jrtech.common.xmlutils.XmlUtils;
import org.junit.Test;
import org.w3c.dom.Document;

import org.junit.Assert;

public class TestPreferenceLoader {

    @Test
    public void loadFromXmlString() {
        String resourcePath = "/data/global-preference-config.xml";

        InputStream xmlContentStream = getClass().getResourceAsStream(resourcePath);
        if (xmlContentStream == null) {
            Assert.fail(MessageFormat.format("Input stream from path: ''{0}'' not found.", resourcePath));
            return;
        }

        String xmlContent = null;
        try {
            xmlContent = IOUtils.toString(xmlContentStream, TestSystemConstants.DEFAULT_CHARSET);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Fail to convert xml content stream to string.");
            return;
        } finally {
            if (xmlContentStream != null) {
                try {
                    xmlContentStream.close();
                } catch (IOException e) {
                }
            }
        }

        PreferenceLoader loader = new PreferenceLoader();
        List<Preference> preferenceList;
        try {
            preferenceList = loader.load(xmlContent);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Fail to load preferences from xml content stream.");
            return;
        }

        checkLoadedPreferences(preferenceList, loader);
    }

    @Test
    public void loadFromInputStream() {
        String resourcePath = "/data/global-preference-config.xml";

        InputStream xmlContentStream = getClass().getResourceAsStream(resourcePath);
        if (xmlContentStream == null) {
            Assert.fail(MessageFormat.format("Input stream from path: ''{0}'' not found.", resourcePath));
            return;
        }

        PreferenceLoader loader = new PreferenceLoader();
        List<Preference> preferenceList;
        try {
            preferenceList = loader.load(xmlContentStream);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Fail to load preferences from xml content stream.");
            return;
        } finally {
            if (xmlContentStream != null) {
                try {
                    xmlContentStream.close();
                } catch (IOException e) {
                }
            }
        }

        checkLoadedPreferences(preferenceList, loader);
    }

    private void checkLoadedPreferences(List<Preference> preferenceList, PreferenceLoader loader) {
        Assert.assertNotNull(preferenceList);

        for (int i = 0; i < preferenceList.size(); i++) {
            Preference config = preferenceList.get(i);
            Document xmlDoc;
            try {
                xmlDoc = loader.export(config);
                System.out.println("\nDocument\n" + XmlUtils.nodeToString(xmlDoc));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

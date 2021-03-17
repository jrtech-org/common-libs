package org.jrtech.common.preferences.table;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jrtech.common.preferences.Preference;
import org.jrtech.common.preferences.PreferenceLoader;
import org.jrtech.common.preferences.TestSystemConstants;
import org.jrtech.common.xmlutils.XmlUtils;
import org.junit.Test;
import org.w3c.dom.Document;

import org.junit.Assert;

public class TestTableConfigurationLoader {

    @Test
    public void loadFromXmlString() {
        String resourcePath = "/data/data-table.xml";

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

        TableConfigurationLoader loader = new TableConfigurationLoader();
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
        String resourcePath = "/data/data-table.xml";

        InputStream xmlContentStream = getClass().getResourceAsStream(resourcePath);
        if (xmlContentStream == null) {
            Assert.fail(MessageFormat.format("Input stream from path: ''{0}'' not found.", resourcePath));
            return;
        }

        TableConfigurationLoader loader = new TableConfigurationLoader();
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

    private void checkLoadedPreferences(List<Preference> preferenceList, TableConfigurationLoader loader) {
        Assert.assertNotNull(preferenceList);

        for (int i = 0; i < preferenceList.size(); i++) {
            TableConfiguration config = (TableConfiguration) preferenceList.get(i);
            System.out.println(config + "\nColumns:");
            for (int j = 0; j < config.getColumns().size(); j++) {
                System.out.println(config.getColumns().get(j));
            }
            Document xmlDoc;
            try {
                xmlDoc = loader.export(config);
                System.out.println("\nDocument\n" + XmlUtils.nodeToString(xmlDoc));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void convertToTableConfiguration() throws Exception {
        String datasourceFileName = "src/test/resources/data/system-data-table-pref.xml";

        PreferenceLoader prefLoader = new PreferenceLoader();
        String xmlContent = FileUtils.readFileToString(new File(datasourceFileName),
                TestSystemConstants.DEFAULT_CHARSET);
        List<Preference> prefList = prefLoader.load(xmlContent);
        Assert.assertNotNull(prefList);

        for (Preference pref : prefList) {
            TableConfigurationLoader tableConfigLoader = new TableConfigurationLoader();
            TableConfiguration config = tableConfigLoader.convert(pref);
            Assert.assertNotNull(config);
            int i = 0;
            for (TableColumnConfiguration column : config.getColumns()) {
                if (i < 1) {
                    column.setName("changedColumnName");
                }
                i++;
            }
            Preference convertedPref = tableConfigLoader.convert(config);
            System.out.println(XmlUtils.documentToString(prefLoader.export(convertedPref)));
            Assert.assertNotSame(pref, convertedPref);
        }
        Document xmlDoc = prefLoader.export(prefList);
        System.out.println("\nConfig Document\n" + XmlUtils.nodeToString(xmlDoc));
    }

    @Test
    public void synchronizeDefinitionFromObject() throws Exception {
        String datasourceFileName = "src/test/resources/data/system-data-table-pref.xml";

        PreferenceLoader prefLoader = new PreferenceLoader();
        String xmlContent = FileUtils.readFileToString(new File(datasourceFileName),
                TestSystemConstants.DEFAULT_CHARSET);
        List<Preference> prefList = prefLoader.load(xmlContent);
        Assert.assertNotNull(prefList);

        for (Preference pref : prefList) {
            TableConfigurationLoader tableConfigLoader = new TableConfigurationLoader();
            TableConfiguration config = tableConfigLoader.convert(pref);
            Assert.assertNotNull(config);
//            String prevDefValue = config.getDefinitionValue();
            config.setDefinitionValue("");
            tableConfigLoader.synchronizeDefinitionFromObject(config);
//            String nextDefValue = config.getDefinitionValue();
        }
    }

}

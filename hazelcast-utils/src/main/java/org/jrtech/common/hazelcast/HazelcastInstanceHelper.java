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
package org.jrtech.common.hazelcast;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.collection.ItemListener;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.ItemListenerConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.UrlXmlConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import com.hazelcast.map.IMap;

public class HazelcastInstanceHelper {

    public static final String HAZELCAST_MODE = "hazelcast.mode";

    public static final String DEFAULT_CACHE = "defaultCache";

    private static Logger log = LoggerFactory.getLogger(HazelcastInstanceHelper.class);

    private static HazelcastInstance hzInstance = null;

    public static enum MODE {
        P2P, CS, TEST;

        public static MODE fromString(String value) {
            return fromString(value, P2P);
        }

        public static MODE fromString(String value, MODE defaultValue) {
            for (MODE mode : values()) {
                if (mode.name().equalsIgnoreCase(value)) {
                    return mode;
                }
            }

            return defaultValue;
        }
    };

    public synchronized static final HazelcastInstance getHazelcastInstance() {
        if (hzInstance != null) {
            return hzInstance;
        }

        String hazelcastModeString = System.getProperty(HAZELCAST_MODE, MODE.P2P.name().toLowerCase());
        MODE hazelcastMode = MODE.fromString(hazelcastModeString, MODE.P2P);

        String environmentClusterName = System.getenv(HazelcastConstants.SYSENV_CLUSTER_NAME);

        switch (hazelcastMode) {
        case CS:
            log.info("Start Hazelcast in Client/Server mode.");
            try {
                hzInstance = initHazelcastWithClientServerMode(environmentClusterName);
            } catch (IOException e) {
                log.warn("Cannot start Hazelcast in Client/Server mode. Fallback to P2P mode.");
            }

            if (hzInstance == null) {
                log.info("Start Hazelcast in P2P mode.");
                hzInstance = initHazelcastWithP2PMode(environmentClusterName);
            }
            break;
        case TEST:
            log.info("Start Hazelcast in Test mode.");
            hzInstance = initHazelcastWithTestMode(environmentClusterName);
            break;
        default:
            log.info("Start Hazelcast in P2P mode.");
            hzInstance = initHazelcastWithP2PMode(environmentClusterName);
            break;
        }

        if (hzInstance == null) {
            log.info("Start Hazelcast in TEST mode.");
            hzInstance = initHazelcastWithTestMode(environmentClusterName);
        }

        return hzInstance;
    }

    private static HazelcastInstance initHazelcastWithP2PMode(String environmentClusterName) {
        Config hzConfig = null;

        String hzConfigFile = System.getProperty(HazelcastConstants.SYSPROP_CONFIG_FILE, "");
        if (hzConfigFile != null && hzConfigFile.length() > 1) {
            try {
                File configFile = new File(new URL(hzConfigFile).toURI());
                if (configFile.exists()) {
                    try {
                        hzConfig = new FileSystemXmlConfig(configFile);
                        log.info("Hazelcast config file: '" + configFile.toString() + "'");
                    } catch (Exception e) {
                        log.warn("Failure in loading Hazelcast P2P config from location: '" + hzConfigFile + "'", e);
                        hzConfig = null;
                    }
                } else {
                    log.warn("Hazelcast P2P config cannot be found at defined location: '" + hzConfigFile + "'");
                }
            } catch (Exception e) {
                log.info("Invalid config file URL: '" + hzConfigFile + "'.", e);
            }
        }

        if (hzConfig == null) {
            URL url = null;

            // Load from classpath -> $TOMCAT_HOME/lib
            url = HazelcastInstanceHelper.class.getResource("/" + HazelcastConstants.HZ_P2P_CONFIG_XML);
            if (url == null) {
                log.warn("Hazelcast P2P config is not found in the classpath!");
                return null;
            }

            try {
                hzConfig = new UrlXmlConfig(url);
            } catch (Exception e) {
                log.warn("Fail to load Hazelcast P2P config from classpath.", e);
                return null;
            }
            log.info("Hazelcast P2P config file: " + url.toString());
        }
        hzConfig.setInstanceName(HazelcastConstants.HZ_INSTANCE_NAME);
        if (environmentClusterName != null && environmentClusterName.trim().length() > 0) {
            hzConfig.setClusterName(environmentClusterName);
            log.info("Hazelcast is running with cluster name: '" + environmentClusterName + "'");
        }

        return HazelcastInstanceFactory.getOrCreateHazelcastInstance(hzConfig);
    }

    private static HazelcastInstance initHazelcastWithTestMode(String environmentClusterName) {
        Config hzConfig = new Config(HazelcastConstants.HZ_INSTANCE_NAME);
        hzConfig.setProperty("hazelcast.logging.type", "slf4j");

        NetworkConfig netConfig = hzConfig.getNetworkConfig();
        // Use local only
        netConfig.getInterfaces().clear();
        netConfig.getInterfaces().addInterface("127.0.0.1");

        // No Multicast -> Only to itself
        JoinConfig joinConfig = netConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getAwsConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(true);
        List<String> members = new ArrayList<String>();
        members.add("127.0.0.1");
        joinConfig.getTcpIpConfig().setMembers(members);

        if (environmentClusterName != null && environmentClusterName.trim().length() > 0) {
            hzConfig.setClusterName(environmentClusterName);
        } else {
            // Ensure for the test case it is unique
            hzConfig.setClusterName(UUID.randomUUID().toString());
        }
        return HazelcastInstanceFactory.getOrCreateHazelcastInstance(hzConfig);
    }

    private static HazelcastInstance initHazelcastWithClientServerMode(String environmentClusterName)
            throws IOException {
        XmlClientConfigBuilder configBuilder = null;

        String hzConfigFile = System.getProperty(HazelcastConstants.SYSPROP_CONFIG_FILE, "");
        if (hzConfigFile != null && hzConfigFile.length() > 1) {
            try {
                File configFile = new File(new URL(hzConfigFile).toURI());
                if (configFile.exists()) {
                    try {
                        configBuilder = new XmlClientConfigBuilder(configFile);
                        log.info("Hazelcast config file URL: '" + hzConfigFile + "'");
                    } catch (Exception e) {
                        log.warn("Failure in loading Hazelcast Client/Server config from location: '" + hzConfigFile
                                + "'", e);
                        configBuilder = null;
                    }
                } else {
                    log.warn("Hazelcast Client/Server config cannot be found at defined location: '" + hzConfigFile
                            + "'");
                }
            } catch (Exception e) {
                log.info("Invalid config file URL: '" + hzConfigFile + "'.", e);
            }
        }

        if (configBuilder == null) {
            URL url = null;

            // Load from classpath -> $TOMCAT_HOME/lib
            url = HazelcastInstanceHelper.class.getResource("/" + HazelcastConstants.HZ_CS_CONFIG_XML);
            if (url == null) {
                log.warn("Hazelcast Client/Server Mode config is not found!");
                return null;
            }
            try {
                configBuilder = new XmlClientConfigBuilder(url);
            } catch (Exception e) {
                log.warn("Failure in loading Hazelcast Client/Server config from location: '" + url + "'", e);
                configBuilder = null;
            }
        }
        ClientConfig hzClientConfig = configBuilder.build();

        if (HazelcastClient.getAllHazelcastClients().size() > 0) {
            return HazelcastClient.getAllHazelcastClients().iterator().next();
        }
        if (environmentClusterName != null && environmentClusterName.trim().length() > 0) {
            hzClientConfig.setClusterName(environmentClusterName);
            log.info("Hazelcast Client is connecting to cluster name: '" + environmentClusterName + "'");
        }
        return HazelcastClient.newHazelcastClient(hzClientConfig);
    }

    public void createCache(String cacheName, EvictionPolicy evictionPolicy, int maxSize, int timeToIdleSeconds,
            int timeToLiveSeconds, boolean distributed) {
        createCache(cacheName, evictionPolicy, maxSize, timeToIdleSeconds, timeToLiveSeconds, distributed, null);
    }

    public void createCache(String cacheName, EvictionPolicy evictionPolicy, int maxSize, int timeToIdleSeconds,
            int timeToLiveSeconds, boolean distributed, IndexConfig[] indexDefinitions) {
        HazelcastInstance hzInstance = getHazelcastInstance();
        MapConfig cacheConfig = null;
        if (hzInstance.getConfig().getMapConfigs().containsKey(cacheName)) {
            if (log.isDebugEnabled())
                log.debug("Cache with name: " + cacheName + " is already available.");
            cacheConfig = hzInstance.getConfig().getMapConfigs().get(cacheName);
        } else {
            if (log.isDebugEnabled())
                log.debug("Create cache: " + cacheName);
            cacheConfig = new MapConfig(cacheName);
            cacheConfig.setTimeToLiveSeconds(timeToLiveSeconds);
            cacheConfig.setMaxIdleSeconds(timeToIdleSeconds);
            
            EvictionConfig evictionConfig = cacheConfig.getEvictionConfig();
            if (evictionConfig == null) {
            	evictionConfig = new EvictionConfig();
            	cacheConfig.setEvictionConfig(evictionConfig);
            }
            evictionConfig.setMaxSizePolicy(MaxSizePolicy.ENTRY_COUNT);
            evictionConfig.setSize(maxSize);

            if (!distributed) {
                cacheConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
                NearCacheConfig ncConfig = cacheConfig.getNearCacheConfig();
                ncConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
                ncConfig.setCacheLocalEntries(true);
            }

            if (evictionPolicy != null)
                evictionConfig.setEvictionPolicy(evictionPolicy);

            if (indexDefinitions != null && indexDefinitions.length > 0) {
                for (IndexConfig indexDef : indexDefinitions) {
                    cacheConfig.addIndexConfig(indexDef);
                }
                if (log.isDebugEnabled())
                    log.debug("Adding index to cache: " + cacheName + " -> " + cacheConfig.getIndexConfigs().size());
            }

            hzInstance.getConfig().addMapConfig(cacheConfig);
        }
        hzInstance.getMap(cacheName);
    }

    public void createQueue(String queueName, ItemListener<?> listener) {
        HazelcastInstance hzInstance = getHazelcastInstance();
        QueueConfig queueConfig = null;
        if (hzInstance.getConfig().getQueueConfigs().containsKey(queueName)) {
            if (log.isDebugEnabled())
                log.debug("Queue with name: " + queueName + " is already available.");
            queueConfig = hzInstance.getConfig().getQueueConfigs().get(queueName);
        } else {
            if (log.isDebugEnabled())
                log.debug("Create queue: " + queueName);
            queueConfig = new QueueConfig();
            queueConfig.setName(queueName);
            queueConfig.addItemListenerConfig(new ItemListenerConfig(listener, true));

            hzInstance.getConfig().addQueueConfig(queueConfig);
        }
        hzInstance.getQueue(queueName);
    }

    public void createDefaultCache() {
        createSimpleCache(DEFAULT_CACHE, 0);
    }

    public void createSimpleCache(String cacheName, int maxSize) {
        createSimpleCache(cacheName, maxSize, null);
    }

    public <K, V> IMap<K, V> createSimpleCache(String cacheName, int maxSize, IndexConfig[] indexDefinitions) {
        HazelcastInstance hzInstance = getHazelcastInstance();
        MapConfig cacheConfig = hzInstance.getConfig().getMapConfig(cacheName);
        if (cacheConfig == null) {
            cacheConfig = new MapConfig(cacheName);
            
            if (cacheConfig.getEvictionConfig() == null) {
            	EvictionConfig evictionConfig = new EvictionConfig();
            	evictionConfig.setMaxSizePolicy(MaxSizePolicy.ENTRY_COUNT);
            	evictionConfig.setSize(maxSize);
            	
            	cacheConfig.setEvictionConfig(evictionConfig);
            } else {
            	cacheConfig.getEvictionConfig().setMaxSizePolicy(MaxSizePolicy.ENTRY_COUNT);
            	cacheConfig.getEvictionConfig().setSize(maxSize);
            }

            if (indexDefinitions != null && indexDefinitions.length > 0) {
            	cacheConfig.setIndexConfigs(Arrays.asList(indexDefinitions));
            }
            hzInstance.getConfig().addMapConfig(cacheConfig);
        }
        return hzInstance.getMap(cacheName);
    }

}

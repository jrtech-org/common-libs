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

import java.lang.management.ManagementFactory;

public class HazelcastConstants {
	
	public static final String SYSENV_CLUSTER_NAME = "SYS_CLUSTER_NAME";
	
	public static final String SYSPROP_CONFIG_FILE = "hzConfigFile";
	
	public static final String HZ_P2P_CONFIG_XML = "hazelcast-p2p.xml";
	
	public static final String HZ_CS_CONFIG_XML = "hazelcast-cs.xml";
	
	public static final String HZ_TEST_CONFIG_XML = "hazelcast-test.xml";
	
	public static final String HZ_INSTANCE_NAME = "SYS-HZ [" + ManagementFactory.getRuntimeMXBean().getName() + "]";
	
	public static final String APP_COMMON_TOPIC_NAME = "app.common.notificationTopic";
}

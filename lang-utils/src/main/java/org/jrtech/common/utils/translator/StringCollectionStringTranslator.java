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
package org.jrtech.common.utils.translator;


import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

public class StringCollectionStringTranslator implements StringTranslator<Collection<String>> {

	private static final long serialVersionUID = 7396731821088529725L;

    @Override
	public String translate(Collection<String> o) {
		if (o == null) return null;
		
		return StringUtils.join(o, ";");
	}

}

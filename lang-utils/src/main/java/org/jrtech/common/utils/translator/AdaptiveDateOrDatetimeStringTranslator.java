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


import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.jrtech.common.utils.DatetimeTextParser;

public class AdaptiveDateOrDatetimeStringTranslator implements FormatLocaleAndTimeZoneAwareStringTranslator<Date> {

	private static final long serialVersionUID = -3852294602804998432L;
    
	private static DatetimeTextParser dtp = new DatetimeTextParser();

	@Override
	public String translate(Date o, Locale locale, TimeZone timezone) {
	    if (o == null) return "";
	    
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(o);
	    if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0) {
	        // Potentially time is zero out for date only
	        return dtp.getStringFromDate(o, locale);
	    }
	    
		return dtp.getStringFromDateTime(o, locale, timezone);
	}

}

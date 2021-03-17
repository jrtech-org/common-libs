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


import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class LocaleUtils {
    public static final String GMT_STRING = "GMT";

    public static final String TIMEZONE_ID_UTC = "UTC";

    public static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone(TIMEZONE_ID_UTC);

    public static final Pattern LANGUAGE_CODE_2DIGIT_PATTERN = Pattern.compile("[a-zA-Z]{2}([ ][_][a-zA-Z]{2})?");

    public static final Pattern LANGUAGE_CODE_3DIGIT_PATTERN = Pattern.compile("[a-zA-Z]{3}([ ][_][a-zA-Z]{3})?");

    public static final Pattern LANGUAGE_DESCRIPTION_PATTERN = Pattern.compile("[a-zA-Z]{3,}([ ][(][a-zA-Z ]{3,}[)])?");

    private static final SortedMap<String, String> LOCALE_LANG_CODE2DESC_MAP = new TreeMap<>();

    private static final SortedMap<String, String> LOCALE_LANG_DESC2CODE_MAP = new TreeMap<>();
    
    private static final SortedMap<String, String> TIMEZONE_OFFSET_MAP = new TreeMap<>();

    private static final ConcurrentMap<String, Locale> DEFAULT_FORMAT_LOCALE_BY_LANGUAGE = new ConcurrentHashMap<>();

    static {
        DEFAULT_FORMAT_LOCALE_BY_LANGUAGE.put(Locale.ENGLISH.getLanguage(), Locale.US);
        DEFAULT_FORMAT_LOCALE_BY_LANGUAGE.put(Locale.GERMAN.getLanguage(), new Locale("de", "CH"));
        DEFAULT_FORMAT_LOCALE_BY_LANGUAGE.put(Locale.FRENCH.getLanguage(), new Locale("fr", "CH"));
        DEFAULT_FORMAT_LOCALE_BY_LANGUAGE.put(Locale.ITALIAN.getLanguage(), new Locale("it", "CH"));
    }

    public static Map<String, Locale> getLocales() {
        return getLocales(Locale.ENGLISH);
    }

    public static Map<String, Locale> getLocales(Locale languageLocale) {
        SortedMap<String, Locale> formatLocaleMap = new TreeMap<String, Locale>();

        Locale[] locales = Locale.getAvailableLocales();
        for (int i = 0; i < locales.length; i++) {
            Locale locale = locales[i];
            String key = getLocaleDisplayLabel(locale, languageLocale);
            if (!formatLocaleMap.containsKey(key) && !locale.getCountry().equals("")) {
                formatLocaleMap.put(key, locale);
            }
        }

        return formatLocaleMap;
    }

    public static Map<String, Locale> getFormatLocales() {
        SortedMap<String, Locale> formatLocaleMap = new TreeMap<String, Locale>();

        Map<String, Locale> allLocales = getLocales();

        Locale[] locales = new Locale[] { Locale.US, Locale.UK, Locale.CANADA, allLocales.get("English (Australia)"),
                Locale.FRANCE, Locale.CANADA_FRENCH, allLocales.get("French (Belgium)"),
                allLocales.get("French (Luxembourg)"), allLocales.get("French (Switzerland)"), Locale.GERMANY,
                allLocales.get("German (Austria)"), allLocales.get("German (Luxembourg)"),
                allLocales.get("German (Switzerland)"), Locale.ITALY, allLocales.get("Italian (Switzerland)") };
        for (int i = 0; i < locales.length; i++) {
            Locale locale = locales[i];
            String key = getLocaleDisplayLabel(locale, Locale.ENGLISH);
            if (!formatLocaleMap.containsKey(key) && !locale.getCountry().equals("")) {
                formatLocaleMap.put(key, locale);
            }
        }

        return formatLocaleMap;
    }

    public static String getLocaleDisplayLabel(Locale locale) {
        return getLocaleDisplayLabel(locale, Locale.ENGLISH);
    }

    public static String getLocaleDisplayLabel(Locale locale, Locale languageLocale) {
        String languageName = locale.getDisplayLanguage(languageLocale);
        String countryName = locale.getDisplayCountry(languageLocale);
        return getLocaleDisplayLabel(languageName, countryName);
    }

    public static String getLocaleDisplayLabel(String languageName, String countryName) {
        return languageName + " (" + countryName + ")";
    }

    public static String getGmtString(TimeZone timezone) {
        String gmtString = TIMEZONE_OFFSET_MAP.get(timezone.getID());

        if (gmtString != null) {
            return gmtString;
        }
        
        gmtString = "";
        int offset = timezone.getOffset(System.currentTimeMillis());
        int hourDiff = Math.abs(offset / 3600000);
        int minuteDiff = Math.abs((offset % 3600000) / 60000);
        String hourAndMinuteOffsetString = "";
        hourAndMinuteOffsetString = StringUtils.leftPad("" + hourDiff, 2, '0');
        hourAndMinuteOffsetString += ":" + StringUtils.leftPad("" + minuteDiff, 2, '0');

        if (hourAndMinuteOffsetString.equals("00:00")) {
            gmtString = GMT_STRING;
            hourAndMinuteOffsetString = "";
        } else {
            if (offset > 0) {
                hourAndMinuteOffsetString = "+" + hourAndMinuteOffsetString;
            } else {
                hourAndMinuteOffsetString = "-" + hourAndMinuteOffsetString;
            }
        }
        
        gmtString += hourAndMinuteOffsetString.equals("") ? "" : hourAndMinuteOffsetString
                + (timezone.useDaylightTime() ? " DST" : "");
        
        TIMEZONE_OFFSET_MAP.put(timezone.getID(), gmtString);

        return gmtString;
    }

    public static Locale getLocaleFromString(String localeString) {
        if (localeString == null || "".equals(localeString))
            return null;

        if (LANGUAGE_CODE_2DIGIT_PATTERN.matcher(localeString).matches()) {
            return org.apache.commons.lang3.LocaleUtils.toLocale(localeString);
        } else if (LANGUAGE_CODE_3DIGIT_PATTERN.matcher(localeString).matches()) {
            // Temporary -> Need a better solution for the future
            String langCode = getLanguageLocaleCodeByISO3Code(localeString);
            if (langCode != null) {
                return new Locale(langCode);
            }
        } else if (LANGUAGE_DESCRIPTION_PATTERN.matcher(localeString).matches()) {
            return getFormatLocales().get(localeString);
        }

        return null;
    }

    public static String getLanguageLocaleCodeByISO3Code(String iso3Code) {
        String desc = getLanguageLocaleDescriptionByCode(iso3Code);

        return getLanguageLocale2DigitCodeByDescription(desc);
    }

    public static String getLanguageLocaleDescriptionByCode(String languageCode) {
        if (LOCALE_LANG_CODE2DESC_MAP.isEmpty()) {
            Map<String, Locale> localeMap = getLocales();
            for (Locale locale : localeMap.values()) {
                LOCALE_LANG_CODE2DESC_MAP.put(locale.getLanguage().toLowerCase(), locale.getDisplayLanguage(Locale.US));
                LOCALE_LANG_CODE2DESC_MAP.put(locale.getISO3Language().toLowerCase(),
                        locale.getDisplayLanguage(Locale.US));
            }
        }

        return LOCALE_LANG_CODE2DESC_MAP.get(languageCode.toLowerCase());
    }

    public static String getLanguageLocale2DigitCodeByDescription(String languageDescription) {
        if (LOCALE_LANG_DESC2CODE_MAP.isEmpty()) {
            Map<String, Locale> localeMap = getLocales();
            for (Locale locale : localeMap.values()) {
                LOCALE_LANG_DESC2CODE_MAP.put(locale.getDisplayLanguage(Locale.US).toLowerCase(), locale.getLanguage());
                LOCALE_LANG_DESC2CODE_MAP.put(getLocaleDisplayLabel(locale).toLowerCase(), locale.getLanguage());
            }
        }

        return LOCALE_LANG_DESC2CODE_MAP.get(languageDescription.toLowerCase());
    }

    public static Locale fillCountry(Locale locale) {
        if (locale.getCountry() != null && !locale.getCountry().equals(""))
            return locale;

        if (DEFAULT_FORMAT_LOCALE_BY_LANGUAGE.containsKey(locale.getLanguage())) {
            return DEFAULT_FORMAT_LOCALE_BY_LANGUAGE.get(locale.getLanguage());
        }

        Map<String, Locale> localeMap = getFormatLocales();
        for (Locale l : localeMap.values()) {
            if (l.getLanguage().equals(locale.getLanguage())) {
                return l;
            }
        }
        return null;
    }
}

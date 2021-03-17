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


import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;

public class DatetimeTextParser implements Serializable {

	private static final long serialVersionUID = -9167025812265549764L;
	
	public static final Pattern ISO_DATE_ONLY_PATTERN = Pattern.compile("[0-9]{4,}[-][0-9]{2}[-][0-9]{2}[T]00[:]00[:]00([.]000)?([Z]|([+-][0-9]{2}[:][0-9]{2}))");

	public static final String RANGE_DELIMITER_NO_SPACE = "..";
	public static final String RANGE_DELIMITER = " " + RANGE_DELIMITER_NO_SPACE + " ";
//	public static final String RANGE_SYMBOL = "\\ ?\\.\\.\\ ?";
	public static final String DATE_DELIMITER = "[\\-\\/\\.]";
	public static final String TIME_DELIMITER = "[\\:\\.]";

	// Value expressions
	public static final String VALUE_DATE = "(0?[1-9]|[1-2][0-9]|3[0-1])";
	public static final String VALUE_MONTH = "(0?[1-9]|[1][0-2])";
	public static final String VALUE_YEAR = "[1-9][0-9]*";

	public static final String VALUE_AM = "[aA][mM]";
	public static final Pattern VALUE_AM_SUFFIX_PATTERN = Pattern.compile(".*" + VALUE_AM);
	public static final String VALUE_PM = "[pP][mM]";
	public static final Pattern VALUE_PM_SUFFIX_PATTERN = Pattern.compile(".*" + VALUE_PM);
	public static final String VALUE_AMPM = "(" + VALUE_AM + "|" + VALUE_PM + ")";
	public static final Pattern VALUE_AMPM_SUFFIX_PATTERN = Pattern.compile(".*" + VALUE_AMPM);
	public static final String VALUE_HOUR24 = "(0?[0-9]|1[0-9]|2[0-4])";
	public static final String VALUE_HOUR12 = "(0?[0-9]|1[0-2])";
	public static final String VALUE_MINUTE = "(0?[0-9]|[1-5][0-9])";
	public static final String VALUE_SECOND = VALUE_MINUTE;
	public static final String VALUE_MILLISECOND = "([1-9]?[0-9]|[1-9][0-9][0-9])";

	public static final String VALUE_TODAY_EXP = "[tT]([oO]([dD]([aA]([yY])?)?)?)?";
	public static final String VALUE_TOMORROW_EXP = "[tT][oO][mM]([oO]([rR]([rR]([oO]([wW])?)?)?)?)?";
	public static final String VALUE_YESTERDAY_EXP = "[yY]([eE]([sS]([tT]([eE]([rR]([dD]([aA]([yY])?)?)?)?)?)?)?)?";
	public static final String VALUE_NOW_EXP = "[nN]([oO]([wW])?)?";

	public static final String UNIT_DAY_EXP = "[bB]?[dD]";
	public static final String UNIT_HOUR_EXP = "[bB]?[hH]";
	public static final String UNIT_EXP = "(" + UNIT_DAY_EXP + "|" + UNIT_HOUR_EXP + ")";
	public static final Pattern UNIT_PATTERN = Pattern.compile(UNIT_EXP);

	public static final String VALUE_DATE_DAY_EXP = VALUE_DATE + "" + DATE_DELIMITER + "" + VALUE_MONTH + ""
	        + DATE_DELIMITER + "" + VALUE_YEAR;

	public static final String VALUE_DATE_MONTH_EXP = VALUE_MONTH + "" + DATE_DELIMITER + "" + VALUE_DATE + ""
	        + DATE_DELIMITER + "" + VALUE_YEAR;

	public static final String VALUE_DATE_YEAR_EXP = VALUE_YEAR + "" + DATE_DELIMITER + "" + VALUE_MONTH + ""
	        + DATE_DELIMITER + "" + VALUE_DATE;

	public static final String VALUE_DATE_EXP = "(" + VALUE_DATE_DAY_EXP + "|" + VALUE_DATE_MONTH_EXP + "|"
	        + VALUE_DATE_YEAR_EXP + ")";

	public static final String VALUE_TIME_24_EXP = "(" + VALUE_HOUR24 + ")(" + TIME_DELIMITER + "(" + VALUE_MINUTE
	        + ")(" + TIME_DELIMITER + "(" + VALUE_SECOND + ")(" + TIME_DELIMITER + "(" + VALUE_MILLISECOND + ")" + ")?"
	        + ")?" + ")?";

	public static final String VALUE_TIME_12_EXP = "(" + VALUE_HOUR12 + ")(" + TIME_DELIMITER + "(" + VALUE_MINUTE
	        + ")(" + TIME_DELIMITER + "(" + VALUE_SECOND + ")(" + TIME_DELIMITER + "(" + VALUE_MILLISECOND + ")" + ")?"
	        + ")?" + ")?[\\ ]?" + VALUE_AMPM + "";

	public static final String VALUE_TIME_EXP = "(" + VALUE_TIME_24_EXP + "|" + VALUE_TIME_12_EXP + ")";

	public static final String VALUE_KEYWORD_EXP = "(" + VALUE_TOMORROW_EXP + "|" + VALUE_TODAY_EXP + "|"
	        + VALUE_YESTERDAY_EXP + "|" + VALUE_NOW_EXP + ")";

	public static final Pattern VALUE_KEYWORD_PATTERN = Pattern.compile(VALUE_KEYWORD_EXP);

	public static final String VALUE_KEYWORD_OFFSET_EXP = "[\\+\\-]\\s?\\d*" + UNIT_EXP + "?";

	public static final Pattern VALUE_KEYWORD_OFFSET_PATTERN = Pattern.compile(VALUE_KEYWORD_OFFSET_EXP);

	public static final String VALUE_KEYWORD_AND_OFFSET_EXP = VALUE_KEYWORD_EXP + "\\s?(" + VALUE_KEYWORD_OFFSET_EXP
	        + ")?";
	public static final Pattern VALUE_KEYWORD_AND_OFFSET_PATTERN = Pattern.compile(VALUE_KEYWORD_AND_OFFSET_EXP);

	public static final String VALUE_DATE_TIME_EXP = "(" + VALUE_DATE_EXP + ")[\\ ](" + VALUE_TIME_EXP + ")";
	public static final Pattern VALUE_DATE_TIME_PATTERN = Pattern.compile(VALUE_DATE_TIME_EXP);

	public static final String VALUE_CONTENT_EXP = "(" + VALUE_KEYWORD_AND_OFFSET_EXP + "|" + VALUE_DATE_EXP + "(\\s"
	        + VALUE_TIME_EXP + ")?)";

	// Date/Time Format expressions
	public static final String FORMAT_NORMALIZED_DATE = "yyyyMMdd";
	public static final String FORMAT_DAY = "dd?";
	public static final String FORMAT_MONTH = "MM?";
	public static final String FORMAT_YEAR = "(YY)?YY|(yy)?yy";

	public static final String FORMAT_HOUR24 = "HH?|kk?";
	public static final String FORMAT_HOUR12 = "hh?|KK?";
	public static final String FORMAT_MINUTE = "mm?";
	public static final String FORMAT_SECOND = "ss?";
	public static final String FORMAT_MILLISECOND = "S{1,3}";

	public static final String ISO_DATE_TIME_END = "9999-12-31T00:00:00.000+01:00";
	
	public static final SimpleDateFormat TWO_DIGIT_YEAR_FORMATTER = new SimpleDateFormat("yy");

	private SortedMap<String, Keyword> usedKeywordMap = Collections
	        .synchronizedSortedMap(new TreeMap<String, Keyword>());

	private boolean defaultDateToday = false;

	public String getRegularExpressionFromLocaleFormat(String localeFormat) {
		String result = localeFormat;

		return result;
	}

	public Date[] getDatesFromString(String textValue, Locale locale, TimeZone timezone)
	        throws InvalidDateTimeValueException, ParseException {
		List<Date> result = new ArrayList<Date>();
		if (textValue == null || textValue.equals("")) {
			return result.toArray(new Date[0]);
		}

		int dateRangeSeparatorPos = textValue.indexOf("..");
		String[] dateTimeTextValueArray = null;
		if (dateRangeSeparatorPos >= 0) {
			dateTimeTextValueArray = new String[] { textValue.substring(0, dateRangeSeparatorPos).trim(),
			        textValue.substring(dateRangeSeparatorPos + 2, textValue.length()).trim() };
		} else {
			dateTimeTextValueArray = new String[] { textValue.trim() };
		}

		for (int i = 0; i < dateTimeTextValueArray.length; i++) {
			String dateTimeTextValue = dateTimeTextValueArray[i].trim();

			if (dateTimeTextValue == null || dateTimeTextValue.equals("") || dateTimeTextValue.equalsIgnoreCase("null")) {
				result.add(null);
				continue;
			}

			boolean isValid = false;

			if (analyseDateValueWithKeyword(dateTimeTextValue) != null) {
				Date parsedDate = parseKeywordValue(dateTimeTextValue, timezone);
				result.add(parsedDate);
				isValid = true;
			} else {
				if (VALUE_DATE_TIME_PATTERN.matcher(dateTimeTextValue).matches()) {
					// date and time value
					String dateTextValue = dateTimeTextValue.substring(0, dateTimeTextValue.indexOf(" ")).trim();
					String timeTextValue = dateTimeTextValue.substring(dateTimeTextValue.indexOf(" ")).trim();
					Calendar dateCal = parseDate(dateTextValue, locale, timezone);
					Calendar timeCal = parseTime(timeTextValue, locale, timezone);
					dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
					dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
					dateCal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
					dateCal.set(Calendar.MILLISECOND, timeCal.get(Calendar.MILLISECOND));
					result.add(dateCal.getTime());
					isValid = true;
				} else {
					String regExPattern = getDateFormatPatternRegEx(locale);
					// check if either date or time is provided
					if (dateTimeTextValue.matches(regExPattern)) {
						Calendar cal = parseDate(dateTimeTextValue, locale, timezone);
						result.add(cal.getTime());
						isValid = true;
					} else {
						Calendar cal = parseTime(dateTimeTextValue, locale, timezone);
						result.add(cal.getTime());
						isValid = true;
					}
				}
			}

			if (!isValid) {
				throw new InvalidDateTimeValueException();
			}
		}

		return result.toArray(new Date[0]);
	}

	public String getTimeFormatPattern(Locale locale, TimeZone timeZone) {
		return getTimeFormatPattern(locale, timeZone, DateFormat.MEDIUM);
	}

	public String getTimeFormatPatternRegEx(Locale locale, TimeZone timeZone) {
		String result = getTimeFormatPattern(locale, timeZone);
		result = StringUtils.replace(result, ":", "\\:");
		result = StringUtils.replace(result, ".", "\\.");
		result = result.replaceFirst(FORMAT_HOUR24, VALUE_HOUR24);
		result = result.replaceFirst(FORMAT_HOUR12, VALUE_HOUR12);
		result = result.replaceFirst(FORMAT_MINUTE, VALUE_MINUTE);
		result = result.replaceFirst(FORMAT_SECOND, VALUE_SECOND);
		result = result.replaceFirst(FORMAT_MILLISECOND, VALUE_MILLISECOND);

		return result;
	}

	public String getDateFormatPattern(Locale locale) {
		return getDateFormatPattern(locale, DateFormat.SHORT);
	}

	public String getDateFormatPatternRegEx(Locale locale) {
		String result = getDateFormatPattern(locale);
		result = StringUtils.replace(result, "/", "\\/");
		result = StringUtils.replace(result, "-", "\\-");
		result = StringUtils.replace(result, ".", "\\.");
		result = result.replaceFirst(FORMAT_YEAR, VALUE_YEAR);
		result = result.replaceFirst(FORMAT_MONTH, VALUE_MONTH);
		result = result.replaceFirst(FORMAT_DAY, VALUE_DATE);

		return result;
	}

	public String parseKeywordValueToISODateTime(String value) {
		return parseKeywordValueToISODateTime(value, LocaleUtils.TIMEZONE_UTC, LocaleUtils.TIMEZONE_UTC);
	}

	public String parseKeywordValueToISODateTime(String value, TimeZone timeZone) {
		return parseKeywordValueToISODateTime(value, timeZone, timeZone);
	}
	
	public String parseKeywordValueToISODateTime(String value, TimeZone keywordTimeZone, TimeZone targetTimeZone) {
		Date date = parseKeywordValue(value, keywordTimeZone);
		if (date == null)
			return null;

		return getISODateTimeFromDate(date, targetTimeZone);
	}

	public Date parseKeywordValue(String value) {
		return parseKeywordValue(value, TimeZone.getDefault());
	}
	
	public Date parseKeywordValue(String value, TimeZone timeZone) {
		KeywordText keywordText = analyseDateValueWithKeyword(value);

		if (keywordText == null)
			return null;

		// Keyword usedKeyword = getUsedKeywordMap().get(value);

		Calendar cal = null;
		// Date theDate = null;
		if (Keyword.TODAY.equals(keywordText.getKeyword())) {
			cal = Calendar.getInstance(timeZone);
			cal.setTimeInMillis(System.currentTimeMillis());
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.HOUR_OF_DAY, 0);
		} else if (Keyword.TOMORROW.equals(keywordText.getKeyword())) {
			cal = Calendar.getInstance(timeZone);
			cal.setTimeInMillis(System.currentTimeMillis());
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.HOUR_OF_DAY, 0);

			cal.add(Calendar.DATE, 1);
		} else if (Keyword.YESTERDAY.equals(keywordText.getKeyword())) {
			cal = Calendar.getInstance(timeZone);
			cal.setTimeInMillis(System.currentTimeMillis());
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.HOUR_OF_DAY, 0);

			cal.add(Calendar.DATE, -1);
		} else if (Keyword.NOW.equals(keywordText.getKeyword())) {
			cal = Calendar.getInstance(timeZone);
			cal.setTimeInMillis(System.currentTimeMillis());
		}

		if (cal == null)
			return null;

		// Calculate offset
		if (keywordText.getOffset() != 0) {
			if (KeywordOffsetUnit.HOUR.equals(keywordText.getOffsetUnit())) {
				cal.add(Calendar.HOUR_OF_DAY, keywordText.getOffset());
			} else if (KeywordOffsetUnit.BUSINESS_DAY.equals(keywordText.getOffsetUnit())) {
				cal.add(Calendar.DAY_OF_MONTH, keywordText.getOffset());
			} else if (KeywordOffsetUnit.BUSINESS_HOUR.equals(keywordText.getOffsetUnit())) {
				cal.add(Calendar.HOUR_OF_DAY, keywordText.getOffset());
			} else {
				cal.add(Calendar.DAY_OF_MONTH, keywordText.getOffset()); // default day
			}
		}

		return cal.getTime();
	}

	private Calendar parseDate(String value, Locale formatLocale, TimeZone timezone) {
		String datePattern = getDateFormatPattern(formatLocale);
		String[] datePatternBlockArray = datePattern.split(DATE_DELIMITER);
		String[] dateValueBlockArray = value.split(DATE_DELIMITER);

		Calendar cal = Calendar.getInstance(timezone, formatLocale);
		cal.setTimeInMillis(0);
		if (defaultDateToday) {
			cal.setTimeInMillis(System.currentTimeMillis());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
		}
		if (defaultDateToday) {
			cal.setTimeInMillis(System.currentTimeMillis());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
		}

		for (int i = 0; i < datePatternBlockArray.length; i++) {
			String dateSegmentValue = "";
			if (dateValueBlockArray.length > i) {
				dateSegmentValue = dateValueBlockArray[i].trim();
			}
			if (datePatternBlockArray[i].equals("dd") || datePatternBlockArray[i].equals("d")) {
				if (dateSegmentValue.equals("")) {
					cal.set(Calendar.DATE, getDefaultDateValue(formatLocale, timezone, Calendar.DATE));
				} else {
					cal.set(Calendar.DATE, Integer.parseInt(dateSegmentValue));
				}
			} else if (datePatternBlockArray[i].equals("MM") || datePatternBlockArray[i].equals("M")) {
				if (dateSegmentValue.equals("")) {
					cal.set(Calendar.MONTH, getDefaultDateValue(formatLocale, timezone, Calendar.MONTH));
				} else {
					// WARNING!!! Month in Java's Calendar begins with 0 = January
					cal.set(Calendar.MONTH, Integer.parseInt(dateSegmentValue) - 1);
				}
			} else if (datePatternBlockArray[i].equalsIgnoreCase("yy") || datePatternBlockArray[i].equals("yyyy")) {
				int year = 0;
				int defaultYear = getDefaultDateValue(formatLocale, timezone, Calendar.YEAR);
				if (dateSegmentValue.equals("")) {
					year = defaultYear;
				} else {
					year = Integer.parseInt(dateSegmentValue);
				}
				if ((year < 100)) {
					try {
	                    Date tempDate = TWO_DIGIT_YEAR_FORMATTER.parse(dateSegmentValue);
	                    Calendar tempCal = Calendar.getInstance();
	                    tempCal.setTime(tempDate);
	                    year = tempCal.get(Calendar.YEAR);
                    } catch (ParseException e) {
                    	// Custom inter
	                    // Adding prefix for 2-digit year with last century when it is over 10 years from current year (in 2 digits)
	                    if (year < defaultYear + 10) {
	                    	// adding 19xx prefix
	                    	year += 1900;
	                    } else {
	                    	// adding 20xx prefix
	                    	year += 2000;
	                    }
                    }
				}
				cal.set(Calendar.YEAR, year);
			}
		}

		return cal;
	}

	private int getDefaultDateValue(Locale formatLocale, TimeZone timezone, int field) {
		Calendar defaultCal = Calendar.getInstance(timezone, formatLocale);
		defaultCal.setTimeInMillis(System.currentTimeMillis());

		return defaultCal.get(field);
	}

	/**
	 * This method use regular expression to parse the time value because the string input can be just hour or
	 * hour+minute or hour+minute+second and/or am/pm. Thus, it cannot be replaced with SimpleDateFormat parser.<br>
	 * 
	 * @param value
	 * @param formatLocale
	 * @param timeZone
	 * @return
	 */
	private Calendar parseTime(String value, Locale formatLocale, TimeZone timeZone) {
		boolean hasAm = false;
		boolean hasPm = false;

		String processValue = value;

		if (VALUE_AM_SUFFIX_PATTERN.matcher(processValue).matches()) {
			hasAm = true;
		} else if (VALUE_PM_SUFFIX_PATTERN.matcher(processValue).matches()) {
			hasPm = true;
		}
		if (VALUE_AMPM_SUFFIX_PATTERN.matcher(processValue).matches()) {
			processValue = processValue.replaceFirst(VALUE_AMPM, "");
		}

		String timePattern = getTimeFormatPattern(formatLocale, timeZone);
		if (timePattern.contains("a")) {
			timePattern = timePattern.replace("a", "");
		}
		String[] timePatternBlockArray = timePattern.split(TIME_DELIMITER);
		String[] timeValueBlockArray = processValue.split(TIME_DELIMITER);

		Calendar cal = Calendar.getInstance(timeZone, formatLocale);
		cal.clear();
		cal.setTimeInMillis(0);

		for (int i = 0; i < timePatternBlockArray.length; i++) {
			String timePatternSegment = timePatternBlockArray[i].trim();
			String dateSegmentValue = "";
			if (timeValueBlockArray.length > i) {
				dateSegmentValue = timeValueBlockArray[i].trim();
			}
			if (timePatternSegment.equalsIgnoreCase("hh") || timePatternSegment.equalsIgnoreCase("h")) {
				if (dateSegmentValue.equals("")) {
					dateSegmentValue = "0";
				}
				int hourValue = Integer.parseInt(dateSegmentValue);
				if (hasAm && hourValue == 12) {
					hourValue = 0;
				}
				if (hasPm && hourValue < 12) {
					hourValue += 12; // add offset for hour of day except for 12pm
				}
				cal.set(Calendar.HOUR_OF_DAY, hourValue);
			} else if (timePatternSegment.equals("mm") || timePatternSegment.equals("m")) {
				if (dateSegmentValue.equals("")) {
					dateSegmentValue = "0";
				}
				cal.set(Calendar.MINUTE, Integer.parseInt(dateSegmentValue));
			} else if (timePatternSegment.equals("ss") || timePatternSegment.equals("s")) {
				if (dateSegmentValue.equals("")) {
					dateSegmentValue = "0";
				}
				cal.set(Calendar.SECOND, Integer.parseInt(dateSegmentValue));
			} else if (timePatternSegment.equals("SSS") || timePatternSegment.equals("S")) {
				if (dateSegmentValue.equals("")) {
					dateSegmentValue = "0";
				}
				cal.set(Calendar.MILLISECOND, Integer.parseInt(dateSegmentValue));
			}
		}

		return cal;
	}

	public String getTimeFormatPattern(Locale locale, TimeZone timeZone, int style) {
		DateFormat df = DateFormat.getTimeInstance(style, locale);
		df.setTimeZone(timeZone);
		return ((SimpleDateFormat) df).toPattern();
	}

	public String getDateFormatPattern(Locale locale, int style) {
		DateFormat df = DateFormat.getDateInstance(style, locale);
		String pattern = ((SimpleDateFormat) df).toPattern();
		return normalizeYearPattern(pattern);
	}

	public String getDateTimeFormatPattern(Locale locale, TimeZone timeZone, int dateStyle, int timeStyle) {
		DateFormat df = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
		df.setTimeZone(timeZone);
		String pattern = ((SimpleDateFormat) df).toPattern();
		return normalizeYearPattern(pattern);
	}

	public String getDateTimeFormatPattern(Locale locale, TimeZone timeZone) {
		return getDateTimeFormatPattern(locale, timeZone, DateFormat.SHORT, DateFormat.MEDIUM);
	}

	public String normalizeYearPattern(String pattern) {
		// compensate 2-digit year
		return pattern.replaceFirst(FORMAT_YEAR, "yyyy");
	}

	public String getStringFromDate(Date date, Locale locale, TimeZone timeZone) {
		if (date == null)
			return null;
		String pattern = getDateFormatPattern(locale);
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		sdf.setTimeZone(timeZone);

		return sdf.format(date);
	}
	
	/**
	 * Returns a date as string according to the locale and ignore TimeZone. 
	 * 
	 * @param date
	 * @param locale
	 * @return
	 */
	public String getStringFromDate(Date date, Locale locale) {
		if (date == null)
			return null;
		String pattern = getDateFormatPattern(locale);
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);

		return sdf.format(date);
	}

	public String getStringFromNormalizedDateString(String date, Locale locale, TimeZone timeZone) {
		if (date == null)
			return null;
		Date d = getDateFromNormalizedString(date);
		return getStringFromDate(d, locale, timeZone);
	}

	public String getNormalizedStringFromDate(Date date) {
		String pattern = FORMAT_NORMALIZED_DATE;
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}

	public Date getDateFromNormalizedString(String date) {
		if (date == null)
			return null;
		String pattern = FORMAT_NORMALIZED_DATE;
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		try {
			return sdf.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	public String getStringFromDateTime(Date date, Locale locale, TimeZone timeZone) {
		if (date == null || locale == null || timeZone == null)
			return "";

		String pattern = getDateTimeFormatPattern(locale, timeZone);
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		sdf.setTimeZone(timeZone);

		return sdf.format(date);
	}

	public String getStringFromTime(Date date, Locale locale, TimeZone timeZone) {
		if (date == null || locale == null || timeZone == null)
			return "";

		String pattern = getTimeFormatPattern(locale, timeZone);
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		sdf.setTimeZone(timeZone);

		return sdf.format(date);
	}

	public String getISODateTimeFromString(String value, Locale formatLocale) {
		if (value == null || value.equals(""))
			return "";

		Date cal = null;
		try {
			cal = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, formatLocale).parse(value);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (cal == null)
			return "";

		return getISODateTimeFromDate(cal);
	}

	public String getISODateTimeFromDate(Date value) {
		if (value == null)
			return "";

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(value);
		return DatatypeConverter.printDateTime(calendar);
	}

	public String getISODateTimeFromDate(Date value, TimeZone timezone) {
		if (value == null)
			return "";

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(value);
		calendar.setTimeZone(timezone);
		return DatatypeConverter.printDateTime(calendar);
	}

	public String getISODateFromDate(Date value, TimeZone timezone) {
		if (value == null)
			return "";

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(value);
		calendar.setTimeZone(timezone);
		return DatatypeConverter.printDate(calendar);
	}

	public String getISODateFromDate(Calendar value, TimeZone timezone) {
		if (value == null)
			return "";

		return DatatypeConverter.printDate(value);
	}

	public String getISOTimeFromDate(Date value, TimeZone timezone) {
		if (value == null)
			return "";

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(value);
		calendar.setTimeZone(timezone);
		return DatatypeConverter.printTime(calendar);
	}

	public Date getDateFromString(String dateTime, Locale formatLocale, TimeZone timezone) {
		if (dateTime == null || dateTime.equals(""))
			return null;
		SimpleDateFormat df = new SimpleDateFormat(getDateFormatPattern(formatLocale));

		try {
			return df.parse(dateTime);
		} catch (ParseException e) {
			return null;
		}
	}

	public Date getDateTimeFromString(String dateTime, Locale formatLocale, TimeZone timezone) {
		if (dateTime == null || dateTime.equals(""))
			return null;
		SimpleDateFormat df = new SimpleDateFormat(getDateTimeFormatPattern(formatLocale, timezone));

		try {
			return df.parse(dateTime);
		} catch (ParseException e) {
			return null;
		}
	}

	public String getStringFromISODateTime(String isoDateTime, Locale formatLocale, TimeZone timeZone) {
		if (isoDateTime == null || isoDateTime.equals(""))
			return "";
		Date dateo = getDateFromISODateTime(isoDateTime);
		SimpleDateFormat df = new SimpleDateFormat(getDateTimeFormatPattern(formatLocale, timeZone));

		return df.format(dateo);
	}

	public String getStringFromISODate(String isoDate, Locale formatLocale) {
		if (isoDate == null || isoDate.equals(""))
			return "";
		Date dateo = getDateFromISODate(isoDate);
		SimpleDateFormat df = new SimpleDateFormat(getDateFormatPattern(formatLocale));

		return df.format(dateo);
	}

	public Date getDateFromISODate(String isoDate) {
		if (isoDate == null || isoDate.equals(""))
			return null;
		Calendar cal = DatatypeConverter.parseDate(isoDate);
		cal.setTimeZone(TimeZone.getDefault());
		return cal.getTime();
	}

	public Date getDateFromISODateTime(String isoDateTime) {
		if (isoDateTime == null || isoDateTime.equals(""))
			return null;
		Calendar cal = DatatypeConverter.parseDateTime(isoDateTime);
		return cal.getTime();
	}

	public Date[] getDatesFromISODateTime(String textValue) throws InvalidDateTimeValueException, ParseException {
		List<Date> result = new ArrayList<Date>();
		if (textValue == null || textValue.equals("")) {
			return result.toArray(new Date[0]);
		}

		String[] dateTimeTextValueArray = textValue.split("\\.{2}");

		for (int i = 0; i < dateTimeTextValueArray.length; i++) {
			String dateTimeTextValue = dateTimeTextValueArray[i].trim();
			boolean isValid = false;
			// ISODatetime shall not contain keyword
			// if (analyseDateValueWithKeyword(dateTimeTextValue) != null) {
			// Date parsedDate = parseKeywordValue(textValue);
			// result.add(parsedDate);
			// isValid = true;
			// } else {
			try {
				Date parsedDate = getDateFromISODateTime(dateTimeTextValue);
				result.add(parsedDate);
				isValid = true;
			} catch (Exception e) {
				isValid = false;
			}
			// }

			if (!isValid) {
				throw new InvalidDateTimeValueException();
			}
		}

		return result.toArray(new Date[0]);
	}

	public String getStringFromISOTime(String isoTime, Locale formatLocale) {
		if (isoTime.equals(""))
			return "";
		Calendar cal = DatatypeConverter.parseTime(isoTime);
		return DateFormat.getTimeInstance(DateFormat.SHORT, formatLocale).format(cal.getTime());
	}

	public String getStringFromISOTime(String isoTime, Locale formatLocale, TimeZone targetTimeZone) {
		if (isoTime.equals(""))
			return "";
		Calendar cal = DatatypeConverter.parseTime(isoTime);
		cal.setTimeZone(targetTimeZone);
		return DateFormat.getTimeInstance(DateFormat.SHORT, formatLocale).format(cal.getTime());
	}

	public Map<String, Keyword> getUsedKeywordMap() {
		synchronized (usedKeywordMap) {
			if (usedKeywordMap.isEmpty()) {
				usedKeywordMap.put("today", Keyword.TODAY);
				usedKeywordMap.put("toda", Keyword.TODAY);
				usedKeywordMap.put("tod", Keyword.TODAY);
				usedKeywordMap.put("to", Keyword.TODAY);
				usedKeywordMap.put("t", Keyword.TODAY);
				usedKeywordMap.put("tomorrow", Keyword.TOMORROW);
				usedKeywordMap.put("tomorro", Keyword.TOMORROW);
				usedKeywordMap.put("tomorr", Keyword.TOMORROW);
				usedKeywordMap.put("tomor", Keyword.TOMORROW);
				usedKeywordMap.put("tomo", Keyword.TOMORROW);
				usedKeywordMap.put("tom", Keyword.TOMORROW);
				usedKeywordMap.put("yesterday", Keyword.YESTERDAY);
				usedKeywordMap.put("yesterda", Keyword.YESTERDAY);
				usedKeywordMap.put("yesterd", Keyword.YESTERDAY);
				usedKeywordMap.put("yester", Keyword.YESTERDAY);
				usedKeywordMap.put("yeste", Keyword.YESTERDAY);
				usedKeywordMap.put("yest", Keyword.YESTERDAY);
				usedKeywordMap.put("yes", Keyword.YESTERDAY);
				usedKeywordMap.put("ye", Keyword.YESTERDAY);
				usedKeywordMap.put("y", Keyword.YESTERDAY);
				usedKeywordMap.put("now", Keyword.NOW);
				usedKeywordMap.put("no", Keyword.NOW);
				usedKeywordMap.put("n", Keyword.NOW);
			}
		}

		return usedKeywordMap;
	}

	public void setUsedKeywordMap(Map<String, Keyword> keywordMap) {
		if (keywordMap instanceof SortedMap) {
			this.usedKeywordMap = (SortedMap<String, Keyword>) keywordMap;
		} else {
			this.usedKeywordMap = Collections.synchronizedSortedMap(new TreeMap<String, DatetimeTextParser.Keyword>(
			        keywordMap));
		}
	}

	public boolean isDefaultDateToday() {
		return defaultDateToday;
	}

	public void setDefaultDateToday(boolean defaultDateToday) {
		this.defaultDateToday = defaultDateToday;
	}

	public KeywordText analyseDateValueWithKeyword(String value) {
		if (value == null)
			return null;

		boolean isMatched = VALUE_KEYWORD_AND_OFFSET_PATTERN.matcher(value).matches();

		if (!isMatched)
			return null;

		// find keyword
		String keywordValue = null;
		String offsetString = null;

		int offset = 0;
		String offsetUnit = null;

		int lastPos = 0;
		Matcher m = VALUE_KEYWORD_PATTERN.matcher(value);
		if (m.find()) {
			keywordValue = value.substring(m.start(), m.end());
			lastPos = m.end();
		}

		if (keywordValue == null)
			return null;

		Keyword keyword = getUsedKeywordMap().get(keywordValue.toLowerCase());

		if (keyword == null)
			return null;

		// find offset
		value = value.substring(lastPos);
		m = VALUE_KEYWORD_OFFSET_PATTERN.matcher(value);
		if (m.find()) {
			offsetString = value.substring(m.start(), m.end());
		}

		if (offsetString != null && !offsetString.equals("")) {
			// find unit
			m = UNIT_PATTERN.matcher(value);
			if (m.find()) {
				offsetUnit = value.substring(m.start(), m.end());
			}

			if (offsetUnit == null) {
				offset = Integer.parseInt(StringUtils.replace(offsetString, "+", "").replaceAll("\\s", ""));
			} else {
				offset = Integer.parseInt(StringUtils.replace(offsetString.replaceAll(DatetimeTextParser.UNIT_EXP, ""),
				        "+", "").replaceAll("\\s", ""));
			}
		}

		return new KeywordText(keyword, offset, KeywordOffsetUnit.fromValue(offsetUnit));
	}

	public enum Keyword {
		TODAY("today"), TOMORROW("tomorrow"), YESTERDAY("yesterday"), NOW("now");

		private String value;

		Keyword(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static Keyword fromValue(String value) {
			for (Keyword k : Keyword.values()) {
				if (k.value.equals(value))
					return k;
			}

			return null;
		}
	}

	public enum KeywordOffsetUnit {
		BUSINESS_DAY("bd"), BUSINESS_HOUR("bh"), DAY("d"), HOUR("h");

		private String value;

		private KeywordOffsetUnit(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static KeywordOffsetUnit fromValue(String value) {
			for (KeywordOffsetUnit k : KeywordOffsetUnit.values()) {
				if (k.value.equals(value))
					return k;
			}

			return DAY;
		}
	}

	public class KeywordText implements Serializable {
		private static final long serialVersionUID = 8665687790327522746L;

		private Keyword keyword;
		private int offset;
		private KeywordOffsetUnit offsetUnit;

		public KeywordText(Keyword keyword, int offset, KeywordOffsetUnit offsetUnit) {
			this.keyword = keyword;
			this.offset = offset;
			this.offsetUnit = offsetUnit;
		}

		public Keyword getKeyword() {
			return keyword;
		}

		public int getOffset() {
			return offset;
		}

		public KeywordOffsetUnit getOffsetUnit() {
			return offsetUnit;
		}

		@Override
		public String toString() {
			return "" + keyword + offset + offsetUnit;
		}
	}
}

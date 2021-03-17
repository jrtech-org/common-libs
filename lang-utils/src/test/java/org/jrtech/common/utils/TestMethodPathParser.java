package org.jrtech.common.utils;



import org.junit.Test;

import org.junit.Assert;

public class TestMethodPathParser {

	private MethodPathParser parser = new MethodPathParser();

	@Test
	public void testMainCases() throws Exception {
		String[][] testData = new String[][] {
			// @formatter:off
		    { "getName//", Boolean.TRUE.toString(), Boolean.FALSE.toString() },
			{ "getName/getDate", Boolean.TRUE.toString(), Boolean.TRUE.toString() },
			{ "getName/Time()", Boolean.FALSE.toString(), Boolean.TRUE.toString() },
			{ "getPackage@getName", Boolean.FALSE.toString(), Boolean.FALSE.toString() },
			{ "get3rdParty", Boolean.TRUE.toString(), Boolean.TRUE.toString() },
			{ "getName/get3rdParty", Boolean.TRUE.toString(), Boolean.TRUE.toString() },
			// @formatter:on
		};
		for (String[] testDataItem : testData) {
			try {
				boolean isValid = showTestInfo(testDataItem[0]);
				Assert.assertEquals("Method input validity does not pass test [Expected/Actual]: [" + testDataItem[2] + "/" + isValid + "]", Boolean.parseBoolean(testDataItem[2]), isValid);
			} catch (InvalidMethodPathExpression e) {
				Assert.assertFalse("Method name retrieval does not pass test [Expected/Actual]: [" + testDataItem[1] + "/false]", Boolean.parseBoolean(testDataItem[1]));
			}
		}
	}

	private boolean showTestInfo(String input) throws Exception {
		boolean isValid = parser.isValid(input);
		System.out.println("\n" + input + "\nMatch status: " + isValid);
		String methodName = parser.getMethodName(input);
		System.out.println("Method name: " + methodName);
		System.out.println("After chop value: " + parser.chopMethodNameFromMethodPathExpression(input, methodName));
		return isValid;
	}
}

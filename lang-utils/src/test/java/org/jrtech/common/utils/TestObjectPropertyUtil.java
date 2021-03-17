package org.jrtech.common.utils;



import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class TestObjectPropertyUtil {

	private Message testData;
	private String[] statusArray = new String[] { "Draft", "Ready To Sent", "Sent", "Acknowledge", "Not Acknowledge" };
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	@Before
	public void init() {
		Random random = new Random();
		testData = new Message();
		testData.setMessageId(UUID.randomUUID().getMostSignificantBits());
		testData.setCounterparty("Counterparty #" + random.nextInt(10));
		long createdTimeMillis = System.currentTimeMillis() - random.nextInt(50000000);
		testData.setCreatedAt(new Timestamp(createdTimeMillis));
		testData.setCreatedBy("User #" + random.nextInt(3));
		long modifiedTimeMillis = createdTimeMillis
		        + random.nextInt((int) (System.currentTimeMillis() - createdTimeMillis));
		testData.setModifiedAt(new Timestamp(modifiedTimeMillis));
		testData.setModifiedBy("User #" + random.nextInt(3));

		testData.setDirection(random.nextBoolean() ? "OUT" : "IN");
		testData.setStatus(statusArray[random.nextInt(statusArray.length - 1)]);
		testData.setTransactionReference("REF100001");
		testData.setType("103");

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, (random.nextInt(3) * (random.nextBoolean() ? 1 : -1)));
		testData.setPropertyValue("valueDate", sdf.format(cal.getTime()));
		cal.add(Calendar.DATE, random.nextInt(3) + 1);
		testData.setPropertyValue("bookingDate", sdf.format(cal.getTime()));
		testData.setPropertyValue("currency", "CHF");
		testData.setPropertyValue("amount", NumberFormat.getInstance().format(random.nextInt(100) * 1000));

		testData.publicField = "filter";
	}

	@Test
	public void testGetFieldProperty() throws InvalidMethodPathExpression {
		ObjectPropertyUtil util = new ObjectPropertyUtil();
		String propertyName = "cofi";
		System.out.println(propertyName + ": " + util.getPropertyValue(testData, propertyName));
	}

	@Test
	public void testGetterMethod() throws InvalidMethodPathExpression {
		ObjectPropertyUtil util = new ObjectPropertyUtil();
		String propertyName = "modifiedAt/time";
		System.out.println(propertyName + ": " + util.getPropertyValue(testData, propertyName, ""));
		System.out.println(propertyName + " (default): " + util.getPropertyValue(testData, propertyName, "default"));
	}

	@Test
	public void testGetExtendedProperty() throws InvalidMethodPathExpression {
		ObjectPropertyUtil util = new ObjectPropertyUtil();
		String retrievingMethod = "getPropertyValue";
		System.out.println("Value: " + testData.getValue());
		String propertyName = "valueDate";
		System.out.println(propertyName + ": " + util.getPropertyValue(testData, propertyName, retrievingMethod));
		propertyName = "bookingDate";
		System.out.println(propertyName + ": " + util.getPropertyValue(testData, propertyName, retrievingMethod));
		propertyName = "currency";
		System.out.println(propertyName + ": " + util.getPropertyValue(testData, propertyName, retrievingMethod));
		propertyName = "amount";
		System.out.println(propertyName + ": " + util.getPropertyValue(testData, propertyName, retrievingMethod));
		propertyName = "direction";
		System.out.println(propertyName + ": " + util.getPropertyValue(testData, propertyName, "default"));
	}
}

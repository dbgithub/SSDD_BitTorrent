package es.deusto.ingenieria.ssdd.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JMSXMLMessagesTEST {
	
	JMSXMLMessages util;
	String message;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		util = new JMSXMLMessages();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConvertToStringKeepAlive() {
		message = util.convertToStringKeepAlive("1", "Master", "127.0.0.1", 8080);
		System.err.println("testConvertToStringKeepAlive:");
		System.out.println(message);
		assertTrue(message != "");
		assertNotNull(message);
		// -----------------
		StringToXML(message);
	}

	@Test
	public void testConvertToStringIDSelection() {
		fail("Not yet implemented");
	}

	@Test
	public void testConvertToStringNegativeID() {
		fail("Not yet implemented");
	}

	@Test
	public void testConvertToStringUpdateRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testConvertToStringSlaveResponse() {
		fail("Not yet implemented");
	}

	@Test
	public void testConvertToStringUpdate() {
		fail("Not yet implemented");
	}

	@Test
	public void testConvertToStringMasterProclamation() {
		fail("Not yet implemented");
	}
	
	private void StringToXML(String msg) {
		org.w3c.dom.Document xmldoc = util.convertFromStringToXML(msg);
		System.err.println("\ttestConvertFromStringToXML:");
		System.out.println("\t"+xmldoc.getElementsByTagName("message").item(0).getTextContent());
		assertNotNull(xmldoc);
	}

}

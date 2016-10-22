package es.deusto.ingenieria.ssdd.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

public class JMSXMLMessages {
	
	public JMSXMLMessages(){
		
	}
	
	public String convertToStringKeepAlive(String idTracker, String typeTracker){
		Document doc = new Document();
		Element xml=new Element("message");
		
		Element head = new Element("head");
		Element body = new Element("body");
		
		Element type = new Element("type");
		type.addContent("KeepAlive");
		head.addContent(type);
		
		Element source=new Element("source");
		Element id = new Element("id");
		id.addContent(idTracker);
		Element typeofTracker = new Element("typeTracker");
		typeofTracker.addContent(typeTracker);
		source.addContent(id);
		source.addContent(typeofTracker);
		body.addContent(source);
		
		xml.addContent(head);
		xml.addContent(body);
		doc.addContent(xml);
		
		return new XMLOutputter().outputString(doc);
		
	}
	
	public String convertToStringIDSelection(String id)
	{
		Document doc = new Document();
		Element xml=new Element("message");
		
		Element head = new Element("head");
		Element body = new Element("body");
		
		Element type = new Element("type");
		type.addContent("IDSelection");
		head.addContent(type);
		
		Element elid = new Element("id");
		elid.addContent(id);
		body.addContent(elid);
		
		xml.addContent(head);
		xml.addContent(body);
		doc.addContent(xml);
		
		return new XMLOutputter().outputString(doc);
	}
	
	public String convertToStringNegativeID()
	{
		Document doc = new Document();
		Element xml=new Element("message");
		
		Element head = new Element("head");
		Element body = new Element("body");
		
		Element type = new Element("type");
		type.addContent("NegativeIDReq");
		head.addContent(type);
		
		xml.addContent(head);
		xml.addContent(body);
		doc.addContent(xml);
		
		return new XMLOutputter().outputString(doc);
	}
	
	public String convertToStringUpdateRequest(int updateID)
	{
		Document doc = new Document();
		Element xml=new Element("message");
		
		Element head = new Element("head");
		Element body = new Element("body");
		
		Element type = new Element("type");
		Element updateid = new Element("updateid");
		type.addContent("UpdateRequest");
		updateid.addContent(updateID+"");
		head.addContent(type);
		head.addContent(updateid);
		
		xml.addContent(head);
		xml.addContent(body);
		doc.addContent(xml);
		
		return new XMLOutputter().outputString(doc);
	}
	
	public String convertToStringSlaveResponse(int updateID, String slaveID, String status)
	{
		Document doc = new Document();
		Element xml=new Element("message");
		
		Element head = new Element("head");
		Element body = new Element("body");
		
		Element type = new Element("type");
		Element updateid = new Element("updateid");
		type.addContent("SlaveResponse");
		updateid.addContent(updateID+"");
		head.addContent(type);
		head.addContent(updateid);
		
		Element id = new Element("slaveID");
		id.addContent(slaveID);
		Element elstatus = new Element("status");
		elstatus.addContent(status);
		body.addContent(id);
		body.addContent(elstatus);
		
		xml.addContent(head);
		xml.addContent(body);
		doc.addContent(xml);
		
		return new XMLOutputter().outputString(doc);
	}
	
	public String convertToStringUpdate(String resolution, String infoHash, String peerID, String ip, int port){
		Document doc = new Document();
		Element xml=new Element("message");
		
		Element head = new Element("head");
		Element body = new Element("body");
		
		Element type = new Element("type");
		type.addContent("Update");
		head.addContent(type);
		
		Element elresolution = new Element("resolution");
		elresolution.addContent(resolution);
		Element info = new Element("info");
		info.setAttribute("torrentHash", infoHash);
		Element peerid = new Element("peerid");
		peerid.addContent(peerID);
		Element elip = new Element("ip");
		elip.addContent(ip);
		Element elport = new Element("port");
		elport.addContent(port+"");
		
		info.addContent(peerid);
		info.addContent(elip);
		info.addContent(elport);
		
		body.addContent(elresolution);
		body.addContent(info);
		
		xml.addContent(head);
		xml.addContent(body);
		doc.addContent(xml);
		
		return new XMLOutputter().outputString(doc);
		
	}
	
	public Document convertFromStringToXML(String xml){
		SAXBuilder builder = new SAXBuilder();
		InputStream stream = null;
		Document anotherDocument = null;
		try {
			stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			anotherDocument = builder.build(stream);
		} catch (JDOMException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return anotherDocument;
	}
	
	
	
	public static void main(String[]args){
		 String xml = new JMSXMLMessages().convertToStringKeepAlive("id1", "Master");
		 System.out.println(xml);
		 Document doc = new JMSXMLMessages().convertFromStringToXML(xml);
		 System.out.println(doc);
		 System.out.println(new XMLOutputter().outputString(doc));
		
	}

}

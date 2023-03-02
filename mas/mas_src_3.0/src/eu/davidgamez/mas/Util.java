package eu.davidgamez.mas;

//Java imports
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import eu.davidgamez.mas.exception.MASXmlException;


public class Util implements Constants {

	public static Document getXMLDocument(String xmlString) throws Exception{
		//Construct the builder
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		
		//Need 
		Document document = builder.parse(new InputSource(new StringReader(xmlString)));
		return document;
	}


	public static String getStringParameter(String elementName, Document xmlDoc) throws MASXmlException{
		NodeList nodeList = xmlDoc.getElementsByTagName(elementName);
		
		//Run some checks
		if(nodeList.getLength() != 1)
			throw new MASXmlException("Expecting 1 element for tag name '" + elementName + "'; found " + nodeList.getLength() + " elements.");
		if(nodeList.item(0).getFirstChild().getNodeType() != Node.TEXT_NODE)
			throw new MASXmlException("Wrong element type; Expecting " + Node.TEXT_NODE + " found " + nodeList.item(0).getFirstChild().getNodeType());
		
		//Return data as a string
		return nodeList.item(0).getFirstChild().getNodeValue();
	}
	
	public static int getIntParameter(String elementName, Document xmlDoc) throws MASXmlException{
		NodeList nodeList = xmlDoc.getElementsByTagName(elementName);
		
		//Run some checks
		if(nodeList.getLength() != 1)
			throw new MASXmlException("Expecting 1 element for tag name '" + elementName + "'; found " + nodeList.getLength() + " elements.");
		if(nodeList.item(0).getFirstChild().getNodeType() != Node.TEXT_NODE)
			throw new MASXmlException("Wrong element type; Expecting " + Node.TEXT_NODE + " found " + nodeList.item(0).getFirstChild().getNodeType());
		
		//Return data as an integer
		return Integer.parseInt(nodeList.item(0).getFirstChild().getNodeValue());
	}
	
	public static int [] getIntArrayParameter(String elementName, Document xmlDoc) throws MASXmlException{
		String arrStr = getStringParameter(elementName, xmlDoc);
		String [] strArr = arrStr.trim().split(",");
		int [] intArr = new int[strArr.length];
		for(int i=0; i<strArr.length; ++i){
			intArr[i] = Integer.parseInt(strArr[i]);
			System.out.println("ARRAY: " + intArr[i]);
		}
		return  intArr;
	}
	
	
	public static double getDoubleParameter(String elementName, Document xmlDoc) throws MASXmlException{
		NodeList nodeList = xmlDoc.getElementsByTagName(elementName);
		
		//Run some checks
		if(nodeList.getLength() != 1)
			throw new MASXmlException("Expecting 1 element for tag name '" + elementName + "'; found " + nodeList.getLength() + " elements.");
		if(nodeList.item(0).getFirstChild().getNodeType() != Node.TEXT_NODE)
			throw new MASXmlException("Wrong element type; Expecting " + Node.TEXT_NODE + " found " + nodeList.item(0).getFirstChild().getNodeType());
		
		//Return data as an integer
		return Double.parseDouble(nodeList.item(0).getFirstChild().getNodeValue());
	}
	
	
	public static float getFloatParameter(String elementName, Document xmlDoc) throws MASXmlException{
		NodeList nodeList = xmlDoc.getElementsByTagName(elementName);
		
		//Run some checks
		if(nodeList.getLength() != 1)
			throw new MASXmlException("Expecting 1 element for tag name '" + elementName + "'; found " + nodeList.getLength() + " elements.");
		if(nodeList.item(0).getFirstChild().getNodeType() != Node.TEXT_NODE)
			throw new MASXmlException("Wrong element type; Expecting " + Node.TEXT_NODE + " found " + nodeList.item(0).getFirstChild().getNodeType());
		
		//Return data as an integer
		return Float.parseFloat(nodeList.item(0).getFirstChild().getNodeValue());
	}
	
	
	public static boolean getBoolParameter(String elementName, Document xmlDoc) throws MASXmlException{
		NodeList nodeList = xmlDoc.getElementsByTagName(elementName);
		
		//Run some checks
		if(nodeList.getLength() != 1)
			throw new MASXmlException("Expecting 1 element for tag name '" + elementName + "'; found " + nodeList.getLength() + " elements.");
		if(nodeList.item(0).getFirstChild().getNodeType() != Node.TEXT_NODE)
			throw new MASXmlException("Wrong element type; Expecting " + Node.TEXT_NODE + " found " + nodeList.item(0).getFirstChild().getNodeType());
		
		//Return data as an integer
		return Boolean.parseBoolean(nodeList.item(0).getFirstChild().getNodeValue());
	}
	
	
	/** Rounds the float to the specified number of places */
	public static float round(float val, int numPlaces){
		val *= Math.pow(10, numPlaces);
		val = Math.round(val);
		val /= Math.pow(10, numPlaces);
		return val;
	}
}

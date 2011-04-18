/**
 *    Copyright (C) 2009-2010  Philippe Busque
 *    http://dafavdownloader.sourceforge.net/
 *    
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.dragoniade.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jaxen.JaxenException;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

public class XmlToolkit {

	private static XmlToolkit instance;
	
	private DocumentBuilder documentBuilder;
	DOMImplementationRegistry registry;
	private XmlToolkit() {
		try {
			DocumentBuilderFactory wDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
			wDocumentBuilderFactory.setNamespaceAware(false);
			documentBuilder = wDocumentBuilderFactory.newDocumentBuilder();
			registry = DOMImplementationRegistry.newInstance();
		} catch (Exception e) {		
		}
	}
	
	public String getNodeAsString(Node pNode, String pXPath) {
		if (pNode == null) {
			throw new NullPointerException();
		}
		Node wNode = getSingleNode(pNode, pXPath);
		if (wNode == null) {
			return "";
		}
		Node fs = wNode.getFirstChild();
		if (fs == null) {
			return "";
		}
		return fs.getNodeValue();
	}

	public long getNodeAsLong(Node pNode, String pXPath) {
		if (pNode == null) {
			throw new NullPointerException();
		}
		Node wNode = getSingleNode(pNode, pXPath);
		Node fs = wNode.getFirstChild();
		if (fs == null) {
			return Long.MIN_VALUE;
		}

		return Long.parseLong(fs.getNodeValue());
	}
	public int getNodeAsInt(Node pNode, String pXPath) {
		if (pNode == null) {
			throw new NullPointerException();
		}
		Node wNode = getSingleNode(pNode, pXPath);
		Node fs = wNode.getFirstChild();
		if (fs == null) {
			return Integer.MIN_VALUE;
		}

		return Integer.parseInt(fs.getNodeValue());
	}	
	public Node getSingleNode(Node pNode, String pXPath) {
		if (pNode == null) {
			throw new NullPointerException();
		}
		try {
			DOMXPath wDOMXPath = new DOMXPath(pXPath);
			return (Node) wDOMXPath.selectSingleNode(pNode);
		} catch (JaxenException e) {			
			throw new RuntimeException(e);
		}
	}
	public float getNodeAsFloat(Node pNode, String pXPath) {
		if (pNode == null) {
			throw new NullPointerException();
		}
		Node wNode = getSingleNode(pNode, pXPath);
		String wValue = wNode.getFirstChild().getNodeValue();
		if (wValue == null) {
			return Float.MIN_VALUE;
		}

		return Float.parseFloat(wValue);
	}	
	
	public double getNodeAsDouble(Node pNode, String pXPath) {
		if (pNode == null) {
			throw new NullPointerException();
		}
		Node wNode = getSingleNode(pNode, pXPath);
		String wValue = wNode.getFirstChild().getNodeValue();
		if (wValue == null) {
			return Double.MIN_VALUE;
		}

		return Double.parseDouble(wValue);
	}	
	public List<?> getMultipleNodes(Node pNode, String pXPath) {
		if (pNode == null) {
			throw new NullPointerException();
		}
		try {
			DOMXPath wDOMXPath = new DOMXPath(pXPath);
			return wDOMXPath.selectNodes(pNode);
		} catch (JaxenException e) {			
			throw new RuntimeException(e);
		}
	}
	
	public String getString(Element pDoc, boolean indent) {
		if (pDoc == null) {
			throw new NullPointerException();
		}
		
		DOMImplementationLS impl = 
		    (DOMImplementationLS)registry.getDOMImplementation("LS");
		LSOutput output = impl.createLSOutput();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		output.setByteStream(baos);
		output.setEncoding("UTF-8");
		
		LSSerializer writer = impl.createLSSerializer();
		writer.getDomConfig().setParameter("format-pretty-print", indent);
		writer.write(pDoc,output);
		String value;
		try {
			value = new String(baos.toByteArray(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} 
		return value;
	}
	public String getString(Element pDoc) {
		return getString(pDoc,true);
	}
	
	
	public Node addNode(Node pNode, String pName, String pValue) {
		if (pNode == null || pName == null) {
			throw new NullPointerException();
		}
		if (pNode.getNodeType() == Node.ATTRIBUTE_NODE) {
			throw new XmlToolkitException("You cannot add a new element to an attribute"); 
		}
		if (pName.charAt(0) == '@') {
			if (pNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				throw new XmlToolkitException("You cannot add an attribute to an attribute"); 
			}
			pName = pName.substring(1);
			((Element) pNode).setAttribute(pName,pValue);
			
			return pNode;
			
		} else {
			Node wNode = pNode.getOwnerDocument().createElement(pName);
			pNode.appendChild(wNode);
			if (pValue != null) {
				Node wValue = pNode.getOwnerDocument().createTextNode(pValue);
				wNode.appendChild(wValue);	
			}		
			return wNode;
		}
	}
	
	public Node addNode(Node pNode, String pName) {
		return addNode(pNode, pName, null);
	}
	
	public Element addElement(Node pNode, String pName, String pValue) {
		if (pNode == null || pName == null) {
			throw new NullPointerException();
		}
		if (pNode.getNodeType() == Node.ATTRIBUTE_NODE) {
			throw new XmlToolkitException("You cannot add a new element to an attribute"); 
		}
		
		Element wNode = pNode.getOwnerDocument().createElement(pName);
		pNode.appendChild(wNode);
		if (pValue != null) {
			Node wValue = pNode.getOwnerDocument().createTextNode(pValue);
			wNode.appendChild(wValue);	
		}		
		return wNode;
		

	}
	
	public Element addElement(Node pNode, String pName) {
		return addElement(pNode, pName, null);
	}
	
	public Node addFloatingElement(Node pNode, String pName, String pValue) {
		if (pNode == null || pName == null) {
			throw new NullPointerException();
		}
		if (pNode.getNodeType() == Node.ATTRIBUTE_NODE) {
			throw new XmlToolkitException("You cannot add a new element to an attribute"); 
		}
		if (pName.charAt(0) == '@') {
			throw new XmlToolkitException("You cannot add a floating attribute"); 
		} else {
			Node wNode = pNode.getOwnerDocument().createElement(pName);
			if (pValue != null) {
				Node wValue = pNode.getOwnerDocument().createTextNode(pValue);
				wNode.appendChild(wValue);	
			}		
			return wNode;
		}

	}
	
	public Node addFloatingElement(Node pNode, String pName) {
		return addFloatingElement(pNode, pName, null);
	}
	
	public Element parseDocument(InputStream pInput) {
		Document wDocument;
		try {
			// wDocument = documentBuilder.parse(pInput);
			DocumentBuilderFactory wDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
			wDocumentBuilderFactory.setNamespaceAware(false);
			DocumentBuilder documentBuilder = wDocumentBuilderFactory.newDocumentBuilder();
			wDocument = documentBuilder.parse(pInput);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		return wDocument.getDocumentElement();
	}
	public Element parseDocument(String pDocument) {
		ByteArrayInputStream wInput = new ByteArrayInputStream(pDocument.getBytes());
		return parseDocument(wInput);
	}
	
	public Element createEmptyDocument(String pRootName) {
		Document wDocument =documentBuilder.newDocument();
		wDocument.appendChild(wDocument.createElement(pRootName));
		return wDocument.getDocumentElement();		
	}
	public Element createEmptyDocument(Element pRootElement) {
		Document wDocument =documentBuilder.newDocument();
		wDocument.appendChild(wDocument.importNode(pRootElement,true));
		return	wDocument.getDocumentElement();
				
	}
	public Node importNode(Node pSource, Node pTarget, boolean pDeep) {
		if (pSource == null || pTarget == null) {
			throw new NullPointerException();
		}
		Node wImported = pTarget.getOwnerDocument().importNode(pSource, pDeep);
		pTarget.appendChild(wImported);
		return wImported;
	}
	
	public Node removeNode(Node pNode) {
		if (pNode == null) {
			throw new NullPointerException();
		}
		pNode.getParentNode().removeChild(pNode);
		return pNode;
	}
	
	public boolean nodeExist(Node pNode, String pXPath) {
		return getSingleNode(pNode,pXPath) != null;
	}
	
	public Element ensureSimpleExistence(Element pNode,String pPath) {
		String[] tokens = pPath.split("/");
		Element current = pNode;
		Element nextNode;
		for (int i=0;i<tokens.length;i++) {
			nextNode = (Element) getSingleNode(current,tokens[i]);
			if (nextNode == null) {
				nextNode = (Element) addElement(current,tokens[i]);
			}
			current = nextNode;
		}
		
		return current;
	}
	public Element ensureSimpleExistence(Element pNode,String pPath, String value) {
		return ensureSimpleExistence( pNode, pPath, value,false);
	}
	
	public Element ensureSimpleExistence(Element pNode,String pPath, String value, boolean createAll) {
		Element current = pNode;
		Element nextNode;
		if (pPath.length() == 0) {
			return pNode;
		}		
		
		String[] tokens = pPath.split("/");				
		for (int i=0;i<tokens.length-1;i++) {
			if (createAll) {
				nextNode = null;
			} else {
				nextNode = (Element) getSingleNode(current,tokens[i]);	
			}
			
			if (nextNode == null) {
				nextNode = (Element) addElement(current,tokens[i]);
			}
			current = nextNode;
			
		}
		String wName = tokens[tokens.length-1];
		if (wName.charAt(0) == '@') {
			current.setAttribute(wName.substring(1),value);
		}else {
			current = (Element) addElement(current,wName,value);
		}
		return current;
	}
	
	public Element ensureSimpleExistenceOverwrite(Element pNode,String pPath, String value, boolean overwrite) {
		return ensureSimpleExistenceOverwrite(pNode,pPath,value,false,overwrite);
	}
	public Element ensureSimpleExistenceOverwrite(Element pNode,String pPath, String value, boolean createAll, boolean overwrite) {
		Element current = pNode;
		Element nextNode;
		if (pPath.length() == 0) {
			return pNode;
		}		
		
		String[] tokens = pPath.split("/");				
		for (int i=0;i<tokens.length-1;i++) {
			if (createAll) {
				nextNode = null;
			} else {
				nextNode = (Element) getSingleNode(current,tokens[i]);	
			}
			
			if (nextNode == null) {
				nextNode = (Element) addElement(current,tokens[i]);
			}
			current = nextNode;
			
		}
		String wName = tokens[tokens.length-1];
		if (wName.charAt(0) == '@') {
			current.setAttribute(wName.substring(1),value);
		}else {
			nextNode = (Element) getSingleNode(current,wName);
			if (nextNode == null) {
				current = (Element) addElement(current,wName,value);	
			} else  {
				current = nextNode;
				if (overwrite) {
					Node firstChild = current.getFirstChild();
					if (firstChild != null) {
						current.removeChild(firstChild);	
					}
					if (value != null) {
						Node wValue = current.getOwnerDocument().createTextNode(value);
						current.appendChild(wValue);	
					}	
				}
			}
		}
		return current;
	}
	
	public static synchronized XmlToolkit getInstance() {
		if (instance == null) {
			instance = new XmlToolkit();
		}
	
		return instance;
	}
	
	public class XmlToolkitException extends RuntimeException {
		private static final long serialVersionUID = -8814862696425686017L;

		public XmlToolkitException(String pCause) {
			super(pCause);
		}
		
		public XmlToolkitException(Exception e) {
			super(e);
		}
		
	}
}

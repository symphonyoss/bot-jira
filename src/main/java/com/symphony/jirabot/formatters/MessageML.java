/*
 *
 *
 * Copyright 2016 Symphony Communication Services, LLC
 *
 * Licensed to Symphony Communication Services, LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package com.symphony.jirabot.formatters; /**
 * Created by ryan.dsouza on 6/3/16.
 */

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.StringWriter;

public class MessageML {

    private static final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder documentBuilder;
    private Document document;
    private Element rootElement;

    private int numChildren;

    public MessageML() {

        try {
            this.documentBuilder = builderFactory.newDocumentBuilder();
            this.document = this.documentBuilder.newDocument();

            this.rootElement = this.document.createElement("messageML");
            this.document.appendChild(this.rootElement);
            this.numChildren = 0;
        }
        catch(ParserConfigurationException exception) {
            throw new RuntimeException("Unable to create XML document", exception);
        }
    }

    private void addSimpleTag(Element parentElement, String tag, String text) {
        Element element = this.document.createElement(tag);
        Text elementText = this.document.createTextNode(text);

        element.appendChild(elementText);
        parentElement.appendChild(element);
        this.numChildren++;
    }

    private void addSimpleTag(String tag, String text) {
        this.addSimpleTag(this.rootElement, tag, text);
    }

    public void addParagraph(String text) {
        Text elementText = this.document.createTextNode(text);
        this.rootElement.appendChild(elementText);
        this.numChildren++;
        //this.addSimpleTag("p", text);
    }

    public void addBoldText(String text) {
        this.addSimpleTag("b", text);
    }

    public void addItalicText(String text) {
        this.addSimpleTag("i", text);
    }

    private void addSimpleTag(String tag) {
        this.addSimpleTag(tag, this.rootElement);
    }

    public void addLineBreak() {
        this.addSimpleTag("br");
    }

    public void addChime() {
        this.addSimpleTag("chime");
    }
    private void addSimpleTag(String tag, Element root) {
        Element element = this.document.createElement(tag);
        root.appendChild(element);
        this.numChildren++;
    }

    private void appendTag(String attributeName, String tagType, String text) {
        Element cashTag = this.document.createElement(tagType);
        Attr tagAttribute = this.document.createAttribute(attributeName);
        tagAttribute.setValue(text);
        cashTag.setAttributeNode(tagAttribute);
        this.rootElement.appendChild(cashTag);
        this.numChildren++;
    }

    public void addCashTag(String text) {
        this.appendTag("tag", "cash", text);
    }

    public void addHashTag(String text) {
        this.appendTag("tag", "hash", text);
    }

    public void addLink(String link) {
        this.appendTag("href", "a", link);
    }

    public void addBulletPoints(String... items) {
        Element parentElement = this.document.createElement("ul");

        for(String item : items) {
            Element listedItem = this.document.createElement("li");
            Text text = this.document.createTextNode(item);
            listedItem.appendChild(text);
            parentElement.appendChild(listedItem);
            this.numChildren++;
        }

        this.rootElement.appendChild(parentElement);
    }

    @Override
    public String toString() {

        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(this.document);
            transformer.transform(source, result);
            String xmlString = result.getWriter().toString();

            String resultString = xmlString.replaceAll("[\u0000-\u001f]", "");

            if(resultString.equals("<messageML/>")) {
                return "";
            }

            return resultString;
        }
        catch(TransformerException exception) {
            throw new RuntimeException("Unable to convert PresentationML to string", exception);
        }
    }

    public int getNumChildren() {
        return this.numChildren;
    }

    public static void main(String[] ryan) {

        MessageML messageML = new MessageML();

        messageML.addBoldText("Bold text");
        messageML.addLineBreak();
        messageML.addItalicText("Italic text");
        messageML.addLineBreak();
        messageML.addBulletPoints("Item 1", "Item 2", "Item 3");
        messageML.addCashTag("HELLO");
        messageML.addHashTag("yolo");
        System.out.println(messageML.toString());
    }
}

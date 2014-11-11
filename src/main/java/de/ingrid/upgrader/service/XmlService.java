/*
 * **************************************************-
 * ingrid-upgrader
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.upgrader.service;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

public class XmlService {

    protected static final Logger LOG = Logger.getLogger(XmlService.class);

    private final Document _doc;

    public XmlService() throws Exception {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (final Exception e) {
            LOG.error("unable to create document!");
            throw new Exception();
        }
        _doc = db.newDocument();
    }

    public XmlService(final File file) throws Exception {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder db = dbf.newDocumentBuilder();
            _doc = db.parse(file);
        } catch (final Exception e) {
            LOG.error("unable to create document!");
            throw new Exception();
        }
    }

    public static NodeList findNodeList(final Element element, final String path) throws Exception {
        return nodeList(element, path, true);
    }

    public static Node findNode(final Element element, final String path) throws Exception {
        return findNodeList(element, path).item(0);
    }

    public static Element findElement(final Element element, final String path) throws Exception {
        return (Element) findNode(element, path);
    }

    public static NodeList getNodeList(final Element element, final String path) {
        NodeList nodeList = null;
        try {
            nodeList = nodeList(element, path, false);
        } catch (final Exception e) {
        }
        return nodeList;
    }

    public static Node getNode(final Element element, final String path) {
        final NodeList nodeList = getNodeList(element, path);
        return nodeList == null ? null : nodeList.item(0);
    }

    public static Element getElement(final Element element, final String path) {
        final Node node = getNode(element, path);
        return node == null ? null : (Element) node;
    }

    public static String getPath(final Node node) {
        Node current = node;
        final StringBuilder sb = new StringBuilder(current.getNodeName());
        while ((current = current.getParentNode()) != null) {
            sb.insert(0, "/");
            sb.insert(0, current.getNodeName());
        }
        return sb.toString();
    }

    public static Element addNode(final Document doc, final Node node, final String key, final String value) {
        final Element el = doc.createElement(key);
        if (value != null) {
            el.setTextContent(value);
        }
        node.appendChild(el);
        return el;
    }

    public static Attr addAttribute(final Document document, final Node node, final String key, final String value) {
        final Attr attr = document.createAttribute(key);
        attr.setTextContent(value);
        node.getAttributes().setNamedItem(attr);
        return attr;
    }

    private static void writeDocument(final Document document, final Writer out) throws Exception {
        try {
            final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            final LSSerializer writer = impl.createLSSerializer();
            // set encoding and writer
            final LSOutput output = impl.createLSOutput();
            output.setEncoding("utf-8");
            output.setCharacterStream(out);
            writer.write(document, output);
        } catch (final Exception e) {
            LOG.error("unable to store xml file!");
            throw new Exception();
        }
    }

    public static void write(final Document document, final OutputStream out) throws Exception {
        writeDocument(document, new OutputStreamWriter(out));
    }

    public static void write(final Document document, final OutputStreamWriter out) throws Exception {
        writeDocument(document, out);
    }

    public static void write(final Document document, final File file) throws Exception {
        writeDocument(document, new FileWriter(file));
    }

    private static NodeList nodeList(final Element element, final String path, final boolean log) throws Exception {
        final NodeList list = element.getElementsByTagName(path);
        if (list == null || list.getLength() < 1) {
            if (log) {
                LOG.error("missing node '" + path + "' in '" + getPath(element) + "'");
            }
            throw new Exception();
        }
        return list;
    }

    public Document getDocument() {
        return _doc;
    }

    public Element addNode(final Node node, final String key, final String value) {
        return addNode(_doc, node, key, value);
    }

    public Attr addAttribute(final Node node, final String key, final String value) {
        return addAttribute(_doc, node, key, value);
    }

    public void write(final OutputStream out) throws Exception {
        write(_doc, out);
    }

    public void write(final OutputStreamWriter out) throws Exception {
        write(_doc, out);
    }

    public void write(final File file) throws Exception {
        write(_doc, file);
    }
}

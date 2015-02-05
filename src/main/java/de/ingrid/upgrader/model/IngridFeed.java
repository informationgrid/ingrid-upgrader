/*
 * **************************************************-
 * ingrid-upgrader
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
package de.ingrid.upgrader.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.w3c.dom.Element;

import de.ingrid.upgrader.web.DetailsServlet;
import de.ingrid.upgrader.web.DownloadServlet;

public class IngridFeed extends AtomFeed {

    private final Map<Integer, Document> _documents;

    private final String _name = "http://www.portalu.de";

    private final String _title = "Komponenten Versionen";

    private final String _url;

    public IngridFeed(final Map<Integer, Document> documents, final String url) throws Exception {
        super();
        _documents = documents;
        _url = url + (url.endsWith("/") ? "" : "/");
        build();
    }

    private List<Integer> getSortedDocumentIDs(Map<Integer, Document> documents) {
        List<Integer> sortedIds = new ArrayList<Integer>();
        List<String> toSort = new ArrayList<String>();
        Map<String, Integer> idDocMap = new HashMap<String, Integer>();
        for (Integer id : documents.keySet()) {
            final Document document = documents.get(id);
            String fileName = getFileName(document.get(IKeys.PATH_FIELD));
            idDocMap.put(fileName, id);
            toSort.add(fileName);
        }
        Collections.sort(toSort);
        for (String value : toSort) {
            sortedIds.add(idDocMap.get(value));
        }
        
        return sortedIds;
    }

    protected void build() {
        // root node
        final Element feed = _xml.addNode(_xml.getDocument(), "feed", null);
        _xml.addAttribute(feed, "xmlns", "http://www.w3.org/2005/Atom");
        // author
        final Element author = _xml.addNode(feed, "author", null);
        _xml.addNode(author, "name", _name);
        // title
        _xml.addNode(feed, "title", _title);
        // id
        _xml.addNode(feed, "id", _url);
        // updated
        _xml.addNode(feed, "updated", getFirstDate());
        
        List<Integer> sortedDocumentIDs = getSortedDocumentIDs(_documents);

        // entries
        for (final Integer id : sortedDocumentIDs) {
            final Document document = _documents.get(id);
            // entry
            final Element entry = _xml.addNode(feed, "entry", null);
            // title
            final String fileName = getFileName(document.get(IKeys.PATH_FIELD));
            _xml.addNode(entry, "title", fileName);
            // link
            final Element link = _xml.addNode(entry, "link", null);
            _xml.addAttribute(link, "href", _url + DownloadServlet.DOWNLOAD + "/" + fileName + "?" + IKeys.ID_PARAMETER
                    + "=" + id);
            // id
            _xml.addNode(entry, "id", document.get(IKeys.ID_FIELD));
            // version
            _xml.addNode(entry, "type", getFieldFromDoc(document, IKeys.IPLUG_TYPE_FIELD));
            // version
            _xml.addNode(entry, "version", getFieldFromDoc(document, IKeys.VERSION_FIELD));
            // version
            _xml.addNode(entry, "build", getFieldFromDoc(document, IKeys.BUILD_FIELD));
            // changelog-link
            // always show changeloglink, even if only manifest information is available
            //if (new File(DetailsServlet.getPathOnly(document.get(IKeys.PATH_FIELD)) + IKeys.CHANGELOG_FILE).exists()) {
            _xml.addNode(entry, "changelogLink", _url + DetailsServlet.DETAILS + "?" + IKeys.ID_PARAMETER + "=" + id);
            //}
            // updated
            final long time = Long.parseLong(document.get(IKeys.UPDATED_FIELD));
            _xml.addNode(entry, "updated", getDate(time));
            // summary
            final String summary = createSummary(document, id);
            final Element content = _xml.addNode(entry, "summary", summary);
            _xml.addAttribute(content, "type", "html");
        }
    }

    public static String getDate(final long time) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss'Z'");
        Date date = new Date(time);
        return df.format(date);
    }

    private String createSummary(final Document document, final int id) {
        // iplug type
        final String iplug = getFieldFromDoc(document, IKeys.IPLUG_TYPE_FIELD);
        // iplug version
        final String version = getFieldFromDoc(document,IKeys.VERSION_FIELD);
        final String build = getFieldFromDoc(document,IKeys.BUILD_FIELD).equals("") ? "" : " Build:" + getFieldFromDoc(document,IKeys.BUILD_FIELD);
        // link
        final String link = _url + DetailsServlet.DETAILS + "?" + IKeys.ID_PARAMETER + "=" + id;
        // return
        return "component: '" + iplug + "'<br />version: '" + version + build + "'<br /><a href=\"" + link + "\">(more details)</a>";
    }

    private String getFieldFromDoc(final Document document, final String field) {
        String value = document.get(field);
        if (value == null) {
            value = "unknown";
        }
        return value;
    }

    private static String getFileName(final String path) {
        String file = path;
        int index = -1;
        while ((index = file.indexOf('/')) > -1) {
            file = file.substring(index + 1);
        }
        return file;
    }

    private String getFirstDate() {
        long time = Long.MIN_VALUE;
        for (final Integer id : _documents.keySet()) {
            final Document document = _documents.get(id);
            final long current = Long.parseLong(document.get(IKeys.UPDATED_FIELD));
            if (current > time) {
                time = current;
            }
        }
        return getDate(time);
    }
}

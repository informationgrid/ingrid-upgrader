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
package de.ingrid.upgrader.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;

import de.ingrid.upgrader.model.AtomFeed;
import de.ingrid.upgrader.model.IngridFeed;
import de.ingrid.upgrader.service.LuceneSearcher;

public class FeedServlet extends HttpServlet {

    protected static final Logger LOG = Logger.getLogger(FeedServlet.class);

    public static final String URI = "upgrader";

    private static final long serialVersionUID = 2266104277129346263L;

    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
            IOException {
        // redirect
        if (!request.getRequestURI().endsWith(URI)) {
            response.sendRedirect(URI);
            return;
        }

        // get searcher
        final LuceneSearcher searcher = LuceneSearcher.getInstance();
        if (searcher == null) {
            return;
        }

        // search
        Map<Integer, Document> results;
        try {
            // get request map
            final Map<String, String> parameters = new HashMap<String, String>();
            final Map<String, String[]> map = request.getParameterMap();
            for (final String key : map.keySet()) {
                final String[] values = map.get(key);
                if (values != null) {
                    final StringBuilder value = new StringBuilder();
                    for (int i = 0; i < values.length; i++) {
                        value.append(values[i]);
                        if (i != (values.length - 1)) {
                            value.append(" ");
                        }
                    }
                    parameters.put(key, value.toString());
                }
            }
            // do search
            results = searcher.search(parameters);
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }

        // create feed and print it
        try {
            // get defined url
            String url = System.getProperty("url");
            if (url == null || "".equals(url.trim())) {
                // if no url exists use the requested url
                url = request.getRequestURL().toString();
                final int pos = url.indexOf(URI);
                if (pos >= 0) {
                    url = url.substring(0, pos + URI.length());
                } else {
                    url = url + "/" + URI;
                }
            }
            if (!url.endsWith("/")) {
                url = url + "/";
            }
            final AtomFeed feed = new IngridFeed(results, url);
            feed.print(response);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}

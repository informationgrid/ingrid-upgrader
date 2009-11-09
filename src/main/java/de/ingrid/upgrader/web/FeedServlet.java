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
            final AtomFeed feed = new IngridFeed(results);
            feed.print(response);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}

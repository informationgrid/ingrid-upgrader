package de.ingrid.upgrader.servlet;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.document.Document;


import de.ingrid.upgrader.model.AtomFeed;
import de.ingrid.upgrader.model.IKeys;
import de.ingrid.upgrader.service.LuceneSearcher;

public class MainServlet extends HttpServlet {

    private static final long serialVersionUID = 2266104277129346263L;

    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
            IOException {
        // get request map
        final Map<String, String> parameters = request.getParameterMap();

        // open searcher
        final String indexPath = System.getProperty(IKeys.INDEX_PARAMTER);
        try {
            LuceneSearcher.createInstance(new File(indexPath));
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }
        final LuceneSearcher searcher = LuceneSearcher.getInstance();

        // search
        List<Document> results;
        try {
            results = searcher.search(parameters);
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }

        // create feed and print it
        final AtomFeed feed = new AtomFeed(results);
        try {
            feed.print(response);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}

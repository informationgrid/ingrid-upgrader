package de.ingrid.upgrader.service;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

public class LuceneSearcher {

    private static LuceneSearcher _instance;

    private IndexReader _reader;

    private IndexSearcher _searcher;

    private final StandardAnalyzer _analyzer;

    private LuceneSearcher(final File indexFolder) throws Exception {
        openReader(indexFolder);
        _analyzer = new StandardAnalyzer();
    }

    public static LuceneSearcher createInstance(final File sourceFolder) throws Exception {
        if (_instance == null) {
            _instance = new LuceneSearcher(sourceFolder);
        }
        return _instance;
    }

    public static void resetInstance() {
        _instance = null;
    }

    public static LuceneSearcher getInstance() {
        return _instance;
    }

    public IndexReader getReader() {
        return _reader;
    }

    public void closeReader() throws Exception {
        _reader.close();
    }

    public void openReader(final File indexFolder) throws Exception {
        _reader = IndexReader.open(indexFolder);
        _searcher = new IndexSearcher(_reader);
    }

    public Map<Integer, Document> search(final Map<String, String> parameters) throws Exception {
        final Map<Integer, Document> results = new HashMap<Integer, Document>();
        if (parameters == null || parameters.size() < 1) {
            // return all documents
            for (int i = 0; i < _reader.maxDoc(); i++) {
                results.put(i, _reader.document(i));
            }
        } else {
            final StringBuilder sb = new StringBuilder("");
            final Iterator<String> it = parameters.keySet().iterator();
            while (it.hasNext()) {
                final String key = it.next();
                final String value = parameters.get(key);
                sb.append("+");
                sb.append(key);
                sb.append(":");
                if (value.contains(" ")) {
                    sb.append(" ");
                    sb.append(value);
                    sb.append(" ");
                } else {
                    sb.append(value);
                }
                if (it.hasNext()) {
                    sb.append(" ");
                }
            }
            final Query query = QueryParser.parse(sb.toString(), "contents", _analyzer);
            // do search
            final Hits hits = _searcher.search(query);
            // add hits
            for (int i = 0; i < hits.length(); i++) {
                results.put(hits.id(i), hits.doc(i));
            }
        }
        return results;
    }
}

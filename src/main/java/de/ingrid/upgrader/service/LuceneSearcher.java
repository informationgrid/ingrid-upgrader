package de.ingrid.upgrader.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

import de.ingrid.upgrader.model.IKeys;

public class LuceneSearcher {

    private static LuceneSearcher _instance;

    private final IndexReader _reader;

    private final IndexSearcher _searcher;

    private LuceneSearcher(final File sourceFolder) throws Exception {
        final File indexFolder = new File(sourceFolder, IKeys.INDEX_FOLDER);
        _reader = IndexReader.open(indexFolder);
        _searcher = new IndexSearcher(_reader);
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

    public List<Document> search(final Map<String, String> parameters) throws Exception {
        final List<Document> results = new ArrayList<Document>();
        if (parameters == null || parameters.size() < 1) {
            // return all documents
            for (int i = 0; i < _reader.maxDoc(); i++) {
                results.add(_reader.document(i));
            }
        } else {
            // return filtered documents
            final BooleanQuery query = new BooleanQuery();
            // build query
            for (final String key : parameters.keySet()) {
                final String value = parameters.get(key);
                // create term
                final Term term = new Term(key, value);
                // create part query
                Query part = null;
                if (value.contains("~")) {
                    part = new FuzzyQuery(term);
                } else if (value.contains("*") || value.contains("?")) {
                    part = new WildcardQuery(term);
                } else {
                    part = new TermQuery(term);
                }
                // add query to main query
                query.add(part, true, false);
            }
            // do search
            final Hits hits = _searcher.search(query);
            // add hits
            for (int i = 0; i < hits.length(); i++) {
                results.add(hits.doc(i));
            }
        }
        return results;
    }
}

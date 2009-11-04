package de.ingrid.upgrader.model;

import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.document.Document;

public class AtomFeed {

    private org.w3c.dom.Document _doc;

    private final List<Document> _documents;

    public AtomFeed(final List<Document> documents) {
        _documents = documents;
        build();
    }

    public void print(final HttpServletResponse response) throws Exception {
        // set content type
        response.setContentType("application/atom+xml");
        // get output stream
        final ServletOutputStream out = response.getOutputStream();
        // write response
        out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        out.println("<feed xmlns=\"http://www.w3.org/2005/Atom\">");
        out.println("  <author>");
        out.println("    <name>Autor des Weblogs</name>");
        out.println("  </author>");
        out.println("  <title>Titel des Weblogs</title>");
        out.println("  <id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>");
        out.println("  <updated>2003-12-14T10:20:09Z</updated>");
        out.println("");
        out.println("  <entry>");
        out.println("    <title>Titel des Weblog-Eintrags</title>");
        out.println("    <link href=\"http://example.org/2003/12/13/atom-beispiel\"/>");
        out.println("    <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>");
        out.println("    <updated>2003-12-13T18:30:02Z</updated>");
        out.println("    <summary>Zusammenfassung des Weblog-Eintrags</summary>");
        out.println("    <content>Volltext des Weblog-Eintrags</content>");
        out.println("  </entry>");
        out.println("</feed>");
    }

    private void build() {
    }
}

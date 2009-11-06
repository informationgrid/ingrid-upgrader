package de.ingrid.upgrader.model;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Map;
import java.util.zip.CRC32;

import org.apache.lucene.document.Document;
import org.w3c.dom.Element;

import de.ingrid.upgrader.servlet.DetailsServlet;

public class IngridFeed extends AtomFeed {

    private final Map<Integer, Document> _documents;

    private final String _name = "http://www.portalu.de";

    private final String _title = "iPlug Versionen";

    private String _url = "http://{HOST}:8080/";

    public IngridFeed(final Map<Integer, Document> documents) throws Exception {
        super();
        _documents = documents;
        _url = _url.replace("{HOST}", InetAddress.getLocalHost().getCanonicalHostName());
        build();
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
        _xml.addNode(feed, "id", generateId(_title, _url));
        // updated
        _xml.addNode(feed, "updated", getFirstDate());

        // entries
        for (final Integer id : _documents.keySet()) {
            final Document document = _documents.get(id);
            // entry
            final Element entry = _xml.addNode(feed, "entry", null);
            // title
            _xml.addNode(entry, "title", document.get(IKeys.PATH_FIELD));
            // link
            final Element link = _xml.addNode(entry, "link", null);
            _xml.addAttribute(link, "href", _url + DetailsServlet.URI + "?id=" + id);
            // id
            _xml.addNode(entry, "id", "" + id);
            // updated
            final long time = Long.parseLong(document.get(IKeys.UPDATED_FIELD));
            _xml.addNode(entry, "updated", getDate(time));
            // summary
            final String summary = createSummary(document);
            _xml.addNode(entry, "summary", summary);
        }
    }

    public static String generateId(final Object hash, final Object crc) {
        final CRC32 crc32 = new CRC32();
        crc32.update(crc.toString().getBytes());
        return "ingrid:id:" + Math.abs(hash.hashCode()) + "-" + crc32.getValue();
    }

    public static String getDate(final long time) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        // get date
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);
        final int day = cal.get(Calendar.DATE);
        final int hour = cal.get(Calendar.HOUR);
        final int minute = cal.get(Calendar.MINUTE);
        final int second = cal.get(Calendar.SECOND);
        // build string
        final StringBuilder sb = new StringBuilder();
        sb.append(year);
        sb.append("-");
        sb.append(month < 10 ? "0" + month : month);
        sb.append("-");
        sb.append(day < 10 ? "0" + day : day);
        sb.append("T");
        sb.append(hour < 10 ? "0" + hour : hour);
        sb.append(":");
        sb.append(minute < 10 ? "0" + minute : minute);
        sb.append(":");
        sb.append(second < 10 ? "0" + second : second);
        sb.append("Z");
        return sb.toString();
    }

    private static String createSummary(final Document document) {
        // dataname
        String file = document.get(IKeys.PATH_FIELD);
        int index = -1;
        while ((index = file.indexOf('/')) > -1) {
            file = file.substring(index + 1);
        }
        // iplug type
        String iplug = document.get(IKeys.IPLUG_TYPE_FIELD);
        if (iplug == null) {
            iplug = "unknown";
        }
        // iplug version
        String version = document.get(IKeys.VERSION_FIELD);
        if (version == null) {
            version = "unknown";
        }
        // return
        return "file: " + file + " - iPlug: '" + iplug + "' - version: '" + version + "'";
    }

    private String getFirstDate() {
        long time = Long.MAX_VALUE;
        for (final Integer id : _documents.keySet()) {
            final Document document = _documents.get(id);
            final long current = Long.parseLong(document.get(IKeys.UPDATED_FIELD));
            if (current < time) {
                time = current;
            }
        }
        return getDate(time);
    }
}

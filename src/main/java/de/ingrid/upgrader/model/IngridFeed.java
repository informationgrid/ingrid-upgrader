package de.ingrid.upgrader.model;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.CRC32;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.w3c.dom.Element;

public class IngridFeed extends AtomFeed {

    private final Map<Integer, Document> _documents;

    private final String _name = "http://www.portalu.de";

    private final String _url = "http://www.portalu.de";

    private final String _title = "iPlug Versionen";

    public IngridFeed(final Map<Integer, Document> documents) throws Exception {
        super();
        _documents = documents;
    }

    @Override
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
            _xml.addAttribute(link, "href", _url + "?id=" + id);
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

    @SuppressWarnings("unchecked")
    private static String createSummary(final Document document) {
        final StringBuilder sb = new StringBuilder("");
        final Enumeration<Field> fields = document.fields();
        while (fields.hasMoreElements()) {
            final Field field = fields.nextElement();
            sb.append("'");
            sb.append(field.name());
            sb.append("'");
            sb.append(":");
            sb.append("'");
            sb.append(field.stringValue());
            sb.append("'");
            if (fields.hasMoreElements()) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
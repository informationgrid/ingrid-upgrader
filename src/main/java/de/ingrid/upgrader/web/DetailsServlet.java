package de.ingrid.upgrader.web;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import de.ingrid.upgrader.model.IKeys;
import de.ingrid.upgrader.service.LuceneSearcher;

public class DetailsServlet extends HttpServlet {

    public static final String DETAILS = "details";

    public static final String URI = FeedServlet.URI + "/" + DETAILS;

    protected static final Logger LOG = Logger.getLogger(DetailsServlet.class);

    private static final long serialVersionUID = 1512560787652617165L;

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
            IOException {
        // get parameter
        Integer id;
        try {
            id = Integer.parseInt(request.getParameter(IKeys.ID_PARAMETER));
        } catch (final Exception e) {
            return;
        }

        // get searcher
        final LuceneSearcher searcher = LuceneSearcher.getInstance();
        if (searcher == null) {
            return;
        }

        response.setContentType("text/html; charset=UTF-8");

        // get document
        final Document document = searcher.getReader().document(id);
        final ServletOutputStream out = response.getOutputStream();

        // print details
        printHtml(out, id, document);

        // print changelog if available
        printChangelog(out, document);

    }

    private void printChangelog(final ServletOutputStream out, final Document document) {
        String path = null;
        try {
            path = getPathOnly(document.get(IKeys.PATH_FIELD));
            final FileInputStream cssFile = new FileInputStream(path + IKeys.CHANGELOG_STYLE);
            final FileInputStream file = new FileInputStream(path + IKeys.CHANGELOG_FILE);
            final byte[] bytes = new byte[1024];
            int read = -1;

            // first write the main style sheet
            // it won't be the original view but pretty close!
            out.println("<style type=\"text/css\" media=\"all\">");
            while ((read = cssFile.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.println("</style>");

            // and then the content
            while ((read = file.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }

        } catch (final FileNotFoundException e) {
            // no changelog available
            LOG.debug("No changelog-File found on path: " + path + "/site");
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    static public String getPathOnly(final String filePath) {
        String file = filePath.replace('\\', '/');
        int index = -1;
        int oldIndex = 0;
        while ((index = file.indexOf('/')) > -1) {
            oldIndex += index + 1;
            file = file.substring(index + 1);
        }
        return filePath.substring(0, oldIndex);
    }

    private void printHtml(final ServletOutputStream out, final int id, final Document document) throws IOException {
        out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">");
        out.println("<head>");
        out.println("<title>Document details #" + id + "</title>");
        out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
        out.println("</head>");
        out.println("<body>");
        out.println("<h3>DETAILS FOR DOCUMENT #" + id + "</h3>");
        print(out, document);
        out.println("</body>");
        out.println("</html>");
    }

    @SuppressWarnings("unchecked")
    private void print(final ServletOutputStream out, final Document document) throws IOException {
        out.println("<table border=\"1\">");
        out.println("<tr>");
        out.println("<th>Name</th>");
        out.println("<th>Value</th>");
        out.println("</tr>");
        final Enumeration<Field> fields = document.fields();
        while (fields.hasMoreElements()) {
            final Field field = fields.nextElement();
            out.println("<tr>");
            out.println("<td>" + field.name() + "</td>");
            out.println("<td>" + field.stringValue() + "</td>");
            out.println("</tr>");
        }
        out.println("</table>");
    }
}

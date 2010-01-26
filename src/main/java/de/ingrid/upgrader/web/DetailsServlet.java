package de.ingrid.upgrader.web;

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

    public static final String URI = "details";

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

        // get document
        final Document document = searcher.getReader().document(id);
        final ServletOutputStream out = response.getOutputStream();

        // print details
        printHtml(out, id, document);
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

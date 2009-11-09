package de.ingrid.upgrader.web;

import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;

import de.ingrid.upgrader.model.IKeys;
import de.ingrid.upgrader.service.LuceneSearcher;

public class DownloadServlet extends HttpServlet {

    protected final Logger LOG = Logger.getLogger(DownloadServlet.class);

    public static final String URI = "upgrader/download";

    private static final long serialVersionUID = 4145648142533979532L;

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

        // open searcher and get document
        final LuceneSearcher searcher = LuceneSearcher.getInstance();
        if (searcher == null) {
            return;
        }
        final Document document = searcher.getReader().document(id);

        // get file path
        final String path = document.get(IKeys.PATH_FIELD);
        LOG.info("request from '" + request.getRemoteAddr() + "' for file '" + path + "'");

        // set header
        response.setHeader("Pragma", "No-Cache");
        response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
        response.setDateHeader("Expires", 0);
        if (path.endsWith("jar")) {
            response.setContentType("application/java-archive");
        } else {
            response.setContentType("application/zip");
        }

        // write file to output
        try {
            final FileInputStream file = new FileInputStream(path);
            final ServletOutputStream out = response.getOutputStream();
            final byte[] bytes = new byte[1024];
            int read = -1;
            while ((read = file.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
            file.close();
        } catch (final Exception e) {
            LOG.error("returning file '" + path + "' to '" + request.getRemoteAddr() + "' failed");
            return;
        }
        LOG.info("file '" + path + "' successfully returned to '" + request.getRemoteAddr() + "'");
    }
}

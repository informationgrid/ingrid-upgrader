package de.ingrid.upgrader.model;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

public abstract class AtomFeed extends AbstractFeed {

    public AtomFeed() throws Exception {
        super();
    }

    public void print(final HttpServletResponse response) throws Exception {
        // set content type
        response.setContentType("application/atom+xml");
        // get output stream
        final ServletOutputStream out = response.getOutputStream();
        // write response
        print(out);
    }
}

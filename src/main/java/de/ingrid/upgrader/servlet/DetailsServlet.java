package de.ingrid.upgrader.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DetailsServlet extends HttpServlet {

    public static final String URI = "details";

    private static final long serialVersionUID = 1512560787652617165L;

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
            IOException {
        final ServletOutputStream out = response.getOutputStream();
        out.println("details");
    }
}

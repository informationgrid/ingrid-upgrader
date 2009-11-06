package de.ingrid.upgrader.model;

import java.io.OutputStream;

public interface IFeed {

    public void print(final OutputStream out) throws Exception;
}

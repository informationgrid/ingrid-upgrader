package de.ingrid.upgrader.model;

import java.io.OutputStream;

import de.ingrid.upgrader.service.XmlService;

public abstract class AbstractFeed implements IFeed {

    protected final XmlService _xml;

    public AbstractFeed() throws Exception {
        _xml = new XmlService();
        build();
    }

    public void print(final OutputStream out) throws Exception {
        // write response
        _xml.write(out);
    }

    protected abstract void build() throws Exception;
}

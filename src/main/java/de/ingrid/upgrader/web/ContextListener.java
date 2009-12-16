package de.ingrid.upgrader.web;

import java.io.File;
import java.util.Timer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import de.ingrid.upgrader.model.IKeys;
import de.ingrid.upgrader.service.ManifestIndexer;

public class ContextListener implements ServletContextListener {

    protected static final Logger LOG = Logger.getLogger(ContextListener.class);

    public void contextDestroyed(final ServletContextEvent contextEvent) {
    }

    public void contextInitialized(final ServletContextEvent contextEvent) {
        // context
        final ServletContext context = contextEvent.getServletContext();

        // source
        String source = System.getProperty(IKeys.SOURCE_IDENTIFIER);
        if (source == null) {
            source = "conf/";
            LOG.warn("no source path set - using default: " + source);
        }
        final File sourceFolder = new File(source);
        if (!sourceFolder.exists()) {
            sourceFolder.mkdirs();
        }
        context.setAttribute(IKeys.SOURCE_IDENTIFIER, sourceFolder);

        // target
        String index = System.getProperty(IKeys.INDEX_IDENTIFIER);
        if (index == null) {
            index = "conf/";
            LOG.warn("no index path set - using default: " + index);
        }
        final File targetFolder = new File(index);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }
        context.setAttribute(IKeys.INDEX_IDENTIFIER, targetFolder);

        // indexing task
        int period = 1000 * 60 * 60; // one hour
        try {
            period = Integer.parseInt(System.getProperty(IKeys.PERIOD_IDENTIFIER));
        } catch (final Exception e) {
            LOG.warn("no or invalid period time set - using default: " + period + "ms");
        }
        final ManifestIndexer indexer = new ManifestIndexer(sourceFolder, targetFolder);
        final Timer timer = new Timer();
        timer.schedule(indexer, 2000, period); // start after 2secs

        // set context
        String contextPath = context.getContextPath();
        if ("/".equals(contextPath)) {
            contextPath = "";
        }
        System.setProperty(IKeys.CONTEXT_IDENTIFIER, contextPath);
    }
}

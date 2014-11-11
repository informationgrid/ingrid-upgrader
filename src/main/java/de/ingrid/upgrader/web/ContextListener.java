/*
 * **************************************************-
 * ingrid-upgrader
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
    }
}

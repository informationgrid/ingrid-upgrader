package de.ingrid.upgrader.service;

import java.io.File;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

import de.ingrid.upgrader.model.IKeys;

public class ManifestIndexer extends Thread {

    protected static final Logger LOG = Logger.getLogger(ManifestIndexer.class);

    private final File _sourceFolder;

    private final File _targetFolder;

    private static boolean _isRunning = false;

    public ManifestIndexer(final File sourceFolder, final File targetFolder) {
        _sourceFolder = sourceFolder;
        _targetFolder = targetFolder;
    }

    @Override
    public void run() {
        LOG.debug("try to start indexing");
        if (!_isRunning) {
            LOG.info("begin indexing...");
            _isRunning = true;
            try {
                index();
            } catch (final Exception e) {
                LOG.error("indexing failed!", e);
            }
            LOG.info("... indexing done");
            _isRunning = false;
        } else {
            LOG.info("indexing is still running");
        }
    }

    private void index() throws Exception {
        // create tmp index folder
        LOG.debug(" create tmp index folder");
        final File tmp = new File(_targetFolder, IKeys.TEMP_FOLDER);
        if (!tmp.exists()) {
            tmp.mkdirs();
        }

        // indexer
        LOG.debug(" initialize index writer");
        final StandardAnalyzer analyzer = new StandardAnalyzer();
        final IndexWriter writer = new IndexWriter(tmp, analyzer, true);

        // add files to index
        LOG.debug(" adding documents");
        addDocuments(writer, _sourceFolder);

        // optimize
        LOG.debug(" optimizing and closing writer");
        writer.optimize();
        writer.close();

        // rename index
        LOG.debug(" renaming tmp index folder");
        final File folder = new File(_targetFolder, IKeys.INDEX_FOLDER);
        delete(folder);
        tmp.renameTo(folder);
    }

    private void addDocuments(final IndexWriter writer, final File file) throws Exception {
        if (file.isDirectory()) {
            // go through all files
            final File[] files = file.listFiles();
            if (files != null) {
                for (final File next : files) {
                    addDocuments(writer, next);
                }
            }
        } else {
            // check ending
            final String ending = getFileEnding(file);
            if ("jar".equals(ending) || "zip".equals(ending)) {
                LOG.debug("  a " + ending + "-file was found");
                final Document doc = fileToDocument(file);
                if (doc != null) {
                    writer.addDocument(doc);
                }
            }
        }
    }

    private Document fileToDocument(final File file) {
        // open manifest file
        Manifest man = null;
        final String path = file.getAbsolutePath();
        try {
            final URL url = new URL("jar:file:" + path + "!/META-INF/MANIFEST.MF");
            LOG.debug("   opening manifest file");
            man = new Manifest(url.openStream());

        } catch (final Exception e) {
            LOG.debug("   unable to open MANIFEST.MF!");
        }

        // get attributes
        Document doc = null;
        if (man != null) {
            final Attributes attr = man.getMainAttributes();
            if (attr != null) {
                LOG.debug("   creating document");
                doc = new Document();
                // add path
                doc.add(Field.Text(IKeys.PATH_FIELD, path));
                // add last modified
                final long updated = file.lastModified();
                doc.add(Field.Keyword(IKeys.UPDATED_FIELD, "" + updated));
                // add attributes
                for (final Object obj : attr.keySet()) {
                    final String key = obj.toString();
                    final String value = attr.getValue(key);
                    final Field field = Field.Text(key, value);
                    doc.add(field);
                }
            }
        }
        return doc;
    }

    private static String getFileEnding(final File file) {
        String name = file.getName();
        int index = -1;
        while ((index = name.indexOf('.')) > -1) {
            name = name.substring(index + 1);
        }
        return name.toLowerCase();
    }

    private static void delete(final File file) {
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (files != null) {
                for (final File next : files) {
                    delete(next);
                }
            }
        }
        file.delete();
    }

    public static void main(final String[] args) throws Exception {
        if (args == null || args.length < 2) {
            LOG.info("required: <source folder> <target folder>");
            System.exit(0);
        }
        // paths
        final String source = args[0];
        final String target = args[1];
        // indexer
        final ManifestIndexer indexer = new ManifestIndexer(new File(source), new File(target));
        indexer.index();
    }
}

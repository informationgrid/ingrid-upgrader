package de.ingrid.upgrader.service;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.CRC32;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

import de.ingrid.upgrader.model.IKeys;

public class ManifestIndexer extends TimerTask {

    protected static final Logger LOG = Logger.getLogger(ManifestIndexer.class);

    private final File _sourceFolder;

    private final File _targetFolder;

    private static boolean _isRunning = false;

    private static Integer _hash = null;

    public ManifestIndexer(final File sourceFolder, final File targetFolder) {
        _sourceFolder = sourceFolder;
        _targetFolder = targetFolder;
    }

    @Override
    public void run() {
        LOG.debug("try to start indexer");
        if (!_isRunning) {
            // lock thread
            _isRunning = true;
            // check filesystem
            LOG.info("checking filesystem...");
            final List<File> files = new ArrayList<File>();
            getCompressedFiles(files, _sourceFolder);
            final int hash = files.hashCode();
            if (_hash == null || hash != _hash) {
                // index
                LOG.info("... filesystem changed");
                LOG.info("begin indexing...");
                try {
                    index(files);
                } catch (final Exception e) {
                    LOG.error("indexing failed!", e);
                }
                _hash = hash;
                LOG.info("... indexing done");
            } else {
                LOG.info("... no changes");
            }
            // release thread
            _isRunning = false;
        } else {
            LOG.debug("indexer is still running");
        }
    }

    private void getCompressedFiles(final List<File> files, final File file) {
        if (file.isDirectory()) {
            // go through all files
            final File[] dirFiles = file.listFiles();
            if (dirFiles != null) {
                for (final File next : dirFiles) {
                    getCompressedFiles(files, next);
                }
            }
        } else {
            // check ending
            final String ending = getFileEnding(file);
            if ("jar".equals(ending) || "zip".equals(ending)) {
                LOG.debug("  a " + ending + "-file was found");
                files.add(file);
            }
        }
    }

    private void index(final List<File> files) throws Exception {
        // create tmp index folder
        LOG.debug("  create tmp index folder");
        final File tmp = new File(_targetFolder, IKeys.TEMP_FOLDER);
        if (!tmp.exists()) {
            tmp.mkdirs();
        }

        // indexer
        LOG.debug("  initialize index writer");
        final StandardAnalyzer analyzer = new StandardAnalyzer();
        final IndexWriter writer = new IndexWriter(tmp, analyzer, true);

        // add files to index
        LOG.debug("  adding documents");
        for (final File file : files) {
            final Document doc = fileToDocument(file);
            if (doc != null) {
                writer.addDocument(doc);
            }
        }

        // optimize
        LOG.debug("  optimizing and closing writer");
        writer.optimize();
        writer.close();

        // close searcher
        final LuceneSearcher searcher = LuceneSearcher.getInstance();
        if (searcher != null) {
            searcher.closeReader();
        }

        // rename index
        LOG.debug("  renaming tmp index folder");
        final File folder = new File(_targetFolder, IKeys.INDEX_FOLDER);
        delete(folder);
        tmp.renameTo(folder);

        // open new searcher
        if (searcher == null) {
            LuceneSearcher.createInstance(folder);
        } else {
            searcher.openReader(folder);
        }
    }

    private Document fileToDocument(final File file) {
        // open manifest file
        Manifest man = null;
        final String path = file.getAbsolutePath();
        try {
            final URL url = new URL("jar:file:" + path + "!/META-INF/MANIFEST.MF");
            man = new Manifest(url.openStream());

        } catch (final Exception e) {
            LOG.debug("unable to open MANIFEST.MF!");
        }

        // get attributes
        Document doc = null;
        if (man != null) {
            final Attributes attr = man.getMainAttributes();
            if (attr != null) {
                doc = new Document();
                // add path
                doc.add(Field.Text(IKeys.PATH_FIELD, path));
                // add last modified
                final long updated = file.lastModified();
                doc.add(Field.Keyword(IKeys.UPDATED_FIELD, "" + updated));
                // add id
                doc.add(Field.Keyword(IKeys.ID_FIELD, generateId(path, updated)));
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

    private static String generateId(final Object crc, final Object hash) {
        final CRC32 crc32 = new CRC32();
        crc32.update(crc.toString().getBytes());
        return crc32.getValue() + "-" + Math.abs(hash.toString().hashCode());
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
        indexer.run();
    }
}

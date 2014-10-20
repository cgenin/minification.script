package net.genin.maven.plugin;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * Writers for copy, mkdirs.
 */
public class WriterManager {

    private final String root;

    private final URL file;

    private final String destDir;

    private String tofile;


    public WriterManager(String root, URL file, String dest) {
        this.root = root;
        this.file = file;
        this.destDir = dest;
    }


    public WriterManager run() throws Exception {
        final String apFile = new File(file.toURI()).getAbsolutePath();
        final String substring = apFile.substring(root.length());
        tofile = destDir + substring;
        return this;
    }

    public void copy() throws Exception {
        mkdirs();
        Files.copy(new File(file.toURI()), file());
    }

    public void write(final InputStream inputStream) throws Exception {
        Preconditions.checkNotNull(inputStream);
        mkdirs();
        ByteStreams.copy(inputStream, new FileOutputStream(file()));
    }

    public WriterManager mkdirs() throws Exception {
        Files.createParentDirs(file());
        return this;
    }

    public File file() {
        return new File(tofile);
    }

}

package net.genin.maven.plugin;

import com.google.common.io.Files;
import com.google.common.io.Resources;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 * Test of WriterManager;
 */
public class WriterManagerTest {

    private final static String destdir = "target/WriterManagerTest";
    public static final String root = new File("target/test-classes/writermanager").getAbsolutePath();


    @Test
    public void test() throws Exception {
        final URL url = Resources.getResource("writermanager/1.text");
        new WriterManager(root, url, destdir).run().copy();

        final File dest = new File("target/WriterManagerTest/1.text");
        assertTrue(dest.exists());
        assertTrue(Files.equal(new File(url.toURI()), dest));
    }
}

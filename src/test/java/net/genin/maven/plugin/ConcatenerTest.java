package net.genin.maven.plugin;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test class for Minify.
 */
public class ConcatenerTest {

    @Test
    public void addFiles() throws Exception {
        final URL concatUrl = Resources.getResource("concat");
        final String root = new File(concatUrl.toURI()).getAbsolutePath();

        final Concatener concatener = new Concatener.Builder(root).build();

        final InputStream stream = concatener.stream(Lists.newArrayList("file1.js", "file2.js"));
        assertNotNull(stream);
        final String text = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
        assertEquals("alert('truc');\n// dqsdqsdqsdsqdqs\r\n" +
                "// dqsdqsdqsdqs", text);
    }

    @Test
    public void mustManageOneFile() throws Exception {
        final URL concatUrl = Resources.getResource("concat");
        final String root = new File(concatUrl.toURI()).getAbsolutePath();

        final Concatener concatener = new Concatener.Builder(root).build();

        final InputStream stream = concatener.stream(Lists.newArrayList("file1.js"));
        assertNotNull(stream);
        final String text = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));

        assertEquals("alert('truc');", text);
    }

    @Test
    public void mustManageNoFile() throws Exception {
        final URL concatUrl = Resources.getResource("concat");
        final String root = new File(concatUrl.toURI()).getAbsolutePath();

        final Concatener concatener = new Concatener.Builder(root).build();

        final InputStream stream = concatener.stream(Lists.newArrayList());
        assertNotNull(stream);
        final String text = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
        assertEquals("", text);
    }

    @Test
    public void specifySeparatorChar() throws Exception {
        final URL concatUrl = Resources.getResource("concat");
        final String root = new File(concatUrl.toURI()).getAbsolutePath();

        final Concatener concatener = new Concatener.Builder(root).joiner('|').build();

        final InputStream stream = concatener.stream(Lists.newArrayList("file1.js", "file2.js"));
        assertNotNull(stream);
        final String text = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
        assertEquals("alert('truc');|// dqsdqsdqsdsqdqs\r\n" +
                "// dqsdqsdqsdqs", text);
    }
}

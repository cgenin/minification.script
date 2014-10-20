package net.genin.maven.plugin;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Test class for Minify.
 */
public class MinifyTest {

    @Test
    public void testUglifyJs() throws Exception {
        final URL minifyUrl = Resources.getResource("minify");
        final String root = new File(minifyUrl.toURI()).getAbsolutePath();

        final Minify minify = new Minify.Builder(root).toUglifyJs().build();

        final InputStream stream = minify.stream(Lists.newArrayList("a.js", "b.js"));
        assertNotNull(stream);

        final String uglify = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));

        assertEquals("function test(e){alert(e)}$(document).ready(function(){var e=$(\"essai\").val();test(e)})", uglify);
    }

    @Ignore
    @Test
    public void testUglifyJsWithJquery() throws Exception {
        final URL minifyUrl = Resources.getResource("minify");
        final String root = new File(minifyUrl.toURI()).getAbsolutePath();

        final Minify minify = new Minify.Builder(root).toUglifyJs().build();

        final InputStream stream = minify.stream(Lists.newArrayList("/lib/jquery-1.9.1.min.js", "components/component.js"));
        assertNotNull(stream);

        final String uglify = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));

        final URL resource = Resources.getResource("result.js");
        final StringBuilder result = new StringBuilder();
        Resources.asCharSource(resource, Charsets.UTF_8).copyTo(result);
        assertEquals(result.toString(), uglify);
    }

    @Test
    public void testGoogleClosure() throws Exception {
        final URL minifyUrl = Resources.getResource("minify");
        final String root = new File(minifyUrl.toURI()).getAbsolutePath();

        final Minify minify = new Minify.Builder(root).toGoogleClosure().build();

        final InputStream stream = minify.stream(Lists.newArrayList("a.js", "b.js"));
        assertNotNull(stream);

        final String uglify = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));

        assertEquals("function test(a){alert(a)}$(document).ready(function(){var a=$(\"essai\").val();test(a)});", uglify);
    }

    @Test
    public void testBeautifyJs() throws Exception {
        final URL minifyUrl = Resources.getResource("minify");
        final String root = new File(minifyUrl.toURI()).getAbsolutePath();

        final Minify minify = new Minify.Builder(root).toBeautifyJs().build();

        final InputStream stream = minify.stream(Lists.newArrayList("a.js", "b.js"));
        assertNotNull(stream);

        final String uglify = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));

        assertEquals("function test(e) {\n" +
                "    alert(e);\n" +
                "}\n" +
                "\n" +
                "$(document).ready(function() {\n" +
                "    var e = $(\"essai\").val();\n" +
                "    test(e);\n" +
                "});", uglify);
    }


}

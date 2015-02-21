package net.genin.maven.plugin;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.junit.Test;

import javax.activation.FileDataSource;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by skarb on 19/10/2014.
 */
public class ScriptMojoTest {

    @Test
    public void test() throws Exception {
        final ScriptMojo mojo = new ScriptMojo();

        mojo.setRoot(new File("target/test-classes/mojo").getAbsolutePath());
        mojo.setDestDir("target/mojoTest");
        mojo.setProcessor("UglifyJs");
        mojo.execute();
        assertEssaiHtml();
        verifyMinifyJs();
    }

    private void verifyMinifyJs() {
        final ArrayList<String> fs = Lists.newArrayList(new File("target/mojoTest/min").list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".js");
            }
        }));
        assertEquals(1,fs.size());
        assertEquals("essai.min.js",fs.get(0));
    }

    private void assertEssaiHtml() throws Exception {
        final String target = Resources.toString(new File("target/mojoTest/essai.html").toURI().toURL(), Charsets.UTF_8);
        assertNotNull(target);
        assertTrue(target.contains("</div> \n" +
                "<script type=\"application/javascript\" src=\"min/essai.min.js\"></script></body>"));
    }
}

package net.genin.maven.plugin;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Test  of Jsx modification.
 */
public class JsxMojoTest {

    @Test
    public void test() throws Exception {
        final ScriptMojo mojo = new ScriptMojo();

        mojo.setRoot(new File("target/test-classes/jsxmojo").getAbsolutePath());
        mojo.setDestDir("target/jsxmojoTest");
        mojo.setProcessor("UglifyJs");
        Path currentRelativePath = Paths.get("");
        mojo.setJsxBaseDirectory(currentRelativePath.toFile());
        mojo.execute();
        assertEssaiHtml();
        verifyMinifyJs();
    }

    private void verifyMinifyJs() {
        final ArrayList<String> fs = Lists.newArrayList(new File("target/jsxmojoTest/min").list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".js");
            }
        }));
        assertEquals(1, fs.size());
        assertEquals("test.min.js", fs.get(0));
    }

    private void assertEssaiHtml() throws Exception {
        final String target = Resources.toString(new File("target/jsxmojoTest/essai.html").toURI().toURL(), Charsets.UTF_8);
        assertNotNull(target);
        assertTrue(target.contains("</div> \n" +
                "<script type=\"application/javascript\" src=\"min/test.min.js\"></script></body>"));
    }
}

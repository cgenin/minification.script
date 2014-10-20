package net.genin.maven.plugin;

import com.google.common.io.Resources;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

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

    }
}

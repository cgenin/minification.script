package net.genin.maven.plugin;

import com.google.common.io.Resources;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by skarb on 21/02/2015.
 */
public class JsxTest {

    @Test
    public void genetrateJsx() throws Exception {
        final File s = new File(Resources.getResource("jsx").toURI());
        Path currentRelativePath = Paths.get("");
        final Jsx jsx = new Jsx(s.getAbsolutePath(), "target/jsx",
                currentRelativePath.toAbsolutePath().toString());
        new Jsx.Executable(Mockito.mock(Log.class)).run(jsx);

    }
}

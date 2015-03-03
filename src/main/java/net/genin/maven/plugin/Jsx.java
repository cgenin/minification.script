package net.genin.maven.plugin;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * @parameter.
 */
public class Jsx {

    /**
     * source directory for jsx files.
     */
    private final String source;


    /**
     * dest directory for jsx files.
     */
    private final String destination;


    /**
     * dest directory for jsx files.
     */
    private final String basedirectory;


    public Jsx(String source, String destination, String basedirectory) {
        this.source = source;
        this.destination = destination;
        this.basedirectory = basedirectory;
    }

    static class Executable {

        private Log log;

        public Executable(Log log) {
            this.log = log;
        }

        public void run(Jsx jsx) throws Exception {
            final Jsx j = MoreObjects.firstNonNull(jsx, new Jsx(null, null, null));

            if (Strings.isNullOrEmpty(j.source) || Strings.isNullOrEmpty(j.destination)) {
                log.warn("No Jsx transformation run. No configuration found.");
                return;
            }

            final File directory = new File(jsx.basedirectory);
            final ArrayList<String> p = Lists.newArrayList();
            p.addAll(Lists.newArrayList("cmd", "/C"));
            p.addAll(Lists.newArrayList("jsx",
                    "--source-charset", "utf8", "--output-charset", "utf8",
                    "--no-cache-dir",
                    j.source, j.destination));

            final Process process = new ProcessBuilder().command(p).directory(directory).redirectErrorStream(true).start();


            process.waitFor();
            final int i = process.exitValue();

            ByteStreams.copy(process.getInputStream(), System.out);
            ByteStreams.copy(process.getErrorStream(), System.err);
            log.info("jsx exitValue : " + i);
        }
    }


}

package net.genin.maven.plugin;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import ro.isdc.wro.extensions.processor.js.BeautifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Minify and concat js files.
 */
public class Minify {
    /**
     * The configuration for the minifier.
     */
    private final Builder builder;

    /**
     * Constructor.
     * @param builder configuration
     */
    private Minify(Builder builder) {
        this.builder = builder;
    }

    /**
     * Create a stream which represents the minified files.
     * @param srcFiles the list of source files.
     * @return the stream.
     * @throws Exception
     */
    public InputStream stream(List<String> srcFiles) throws Exception {
        Preconditions.checkNotNull(srcFiles);

        final List<CharSource> urls = srcFiles.stream().map(e -> {
            try {
                return builder.root.toURI().toString() + e;
            } catch (URISyntaxException e1) {
                throw new RuntimeException(e1);
            }
        }).map(e -> {
            try {
                return Resources.asCharSource(new URL(e), Charsets.UTF_8);
            } catch (MalformedURLException e1) {
                throw new RuntimeException(e1);
            }
        }).collect(Collectors.toList());

        final CharSource all = CharSource.concat(urls);

        final StringBuilder target = new StringBuilder();
        builder.processor.process(all.openBufferedStream(), CharStreams.asWriter(target));
        return new ByteArrayInputStream(target.toString().getBytes());
    }

    /**
     * Class for the configuration.
     */
    public static class Builder {
        /**
         * root directory.
         */
        URL root;
        /**
         * The resource processor.
         */
        ResourcePostProcessor processor;

        /**
         * Constructor.
         * @param root root directory.
         */
        public Builder(final String root) {
            try {
                this.root = new File(root).toURI().toURL();
            } catch (Exception e) {
                Throwables.propagate(e);
            }

            processor = new UglifyJsProcessor();
        }

        /**
         * Use uglify processor.
         * @return the builder
         */
        public Builder toUglifyJs() {
            processor = new UglifyJsProcessor();
            return this;
        }

        /**
         * Use the BeautifyJs processor.
         * @return the builder.
         */
        public Builder toBeautifyJs() {
            processor = new BeautifyJsProcessor();
            return this;
        }

        /**
         * Use the GoogleClosure processor.
         * @return the builder.
         */
        public Builder toGoogleClosure() {
            processor = new GoogleClosureCompressorProcessor();
            return this;
        }

        /**
         * build the minifier.
         * @return the moinifier.
         */
        public Minify build() {
            return new Minify(this);
        }

    }

}

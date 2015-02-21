package net.genin.maven.plugin;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.Resources;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Concatenation of files.
 */
public class Concatener {
    /**
     * The configuration for the Concatenation.
     */
    private final Builder builder;

    /**
     * Constructor.
     *
     * @param builder configuration
     */
    private Concatener(Builder builder) {
        this.builder = builder;
    }

    /**
     * Create a stream which represents the concatenated files.
     *
     * @param srcFiles the list of source files.
     * @return the stream.
     * @throws Exception
     */
    public InputStream stream(List<String> srcFiles) throws Exception {
        Preconditions.checkNotNull(srcFiles);

        final List<String> all = srcFiles.stream().map(e -> {
            try {
                return builder.root.toURI().toString() + e;
            } catch (URISyntaxException e1) {
                throw new RuntimeException(e1);
            }
        }).map(e -> {
            try {
                return Resources.toString(new URL(e), Charsets.UTF_8).trim();
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        }).collect(Collectors.toList());

        final String target = builder.joiner.join(all);
        return new ByteArrayInputStream(target.getBytes());
    }

    /**
     * Class for the configuration.
     */
    public static class Builder {
        /**
         * root directory.
         */
        URL root;

        Joiner joiner = Joiner.on('\n');

        /**
         * Constructor.
         *
         * @param root root directory.
         */
        public Builder(final String root) {
            try {
                this.root = new File(root).toURI().toURL();
            } catch (Exception e) {
                Throwables.propagate(e);
            }
        }

        /**
         * Specify the joiner Character
         * @param c the character
         * @return the builder
         */
        public Builder joiner(Character c) {
            Preconditions.checkNotNull(c);
            joiner = Joiner.on(c);
            return this;
        }

        /**
         * build the concatenator.
         *
         * @return the concatenator.
         */
        public Concatener build() {
            return new Concatener(this);
        }
    }
}

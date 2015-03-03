package net.genin.maven.plugin;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Mojo for minifying js scripts.
 *
 * @goal minify-scripts
 * @phase generate-resources
 */
public class ScriptMojo extends AbstractMojo {
    public static final String JSX_SRC_DIR = "target/jsx/source";
    public static final String JSX_DEST_DIR = "target/jsx/dest";
    /**
     * Root directory.
     *
     * @parameter
     * @required
     */
    private String root;

    /**
     * Destination directory.
     *
     * @parameter
     * @required
     */
    private String destDir;

    /**
     * extensions for the templates files.
     *
     * @parameter
     */
    private String[] extensions;
    /**
     * Jsx.
     *
     * @parameter expression="${basedir}"
     */
    private File jsxBaseDirectory;

    /**
     * Jsx Source.
     *
     * @parameter
     */
    private String jsxSource;

    /**
     * Jsx Source.
     *
     * @parameter
     */
    private String jsxDestination;

    /**
     * Processor for js minifier.
     *
     * @parameter default-value="UglifyJs"
     */
    private String processor;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("initialize");
//           INIT parameters.
            final ArrayList<String> exts = Lists.newArrayList(MoreObjects.firstNonNull(extensions, new String[]{"html"}));
            final URL file = new File(root).toURI().toURL();
            final Minify.Builder builder = new Minify.Builder(root);
            final Minify.Builder jsxbuilder = new Minify.Builder(JSX_DEST_DIR);
            switch (processor) {
                case "BeautifyJs":
                    builder.toBeautifyJs();
                    jsxbuilder.toBeautifyJs();
                    break;
                case "GoogleClosure":
                    builder.toGoogleClosure();
                    jsxbuilder.toGoogleClosure();
                    break;
                case "UglifyJs":
                default:
                    builder.toUglifyJs();
                    jsxbuilder.toUglifyJs();
            }
            final Minify minify = builder.build();
            final Concatener concatener = new Concatener.Builder(root).build();
            getLog().info("Launch ...");
            final ImmutableMap.Builder<String, String> jsxs = new ImmutableMap.Builder<>();
            new Template.Builder(exts).notTemplate((n) -> {
//              If not an template just copy to dest dir.
                try {
                    getLog().info("Copy " + n.url);
                    new WriterManager(root, n.url, destDir).run().copy();
                } catch (Exception e) {
                    Throwables.propagate(e);
                }
            }).template((t) -> {
                try {
                    final ArrayListMultimap<String, String> scripts = t.scripts();
//                  if no annoted scripts.
                    if (scripts.isEmpty()) {
                        getLog().info("No Script - Copy " + t.url);
                        new WriterManager(root, t.url, destDir).run().copy();
                    } else {
                        getLog().info("Treat " + t.url);
//                      Copy the modified template.
                        new WriterManager(root, t.url, destDir).run().write(t.stream());

                        scripts.keySet().forEach(k -> {
//                          for each scripts
                            try {
                                final String s = root + "/" + k;
                                if (!t.toAction(k).equals(Template.Action.jsx)) {
//                              Minify and write to destination directory.

                                    final Template.Action action = t.toAction(k);
                                    final InputStream stream = getInputStream(minify, concatener, scripts, k, action);
                                    new WriterManager(root, new File(s).toURI().toURL(), destDir).run().write(stream);
                                } else {
                                    final String src = scripts.get(k).iterator().next();
                                    jsxs.put(k, src);
                                    final File jsx = new File(JSX_SRC_DIR);
                                    final String s1 = new File(root).toURI().toString() + "/" + src;
//                                  Copy to Jsx-source
                                    new WriterManager(root, new URI(s1).toURL(), jsx.getAbsolutePath()).run().copy();
                                }
                            } catch (Exception e) {
                                Throwables.propagate(e);
                            }
                        });
                    }
                } catch (Exception e) {
                    Throwables.propagate(e);
                }
            })
                    .traverse(file);
            final ImmutableMap<String, String> allJsxs = jsxs.build();
            if (!allJsxs.isEmpty()) {
                getLog().info("JsxTransformation.");
                new Jsx.Executable(getLog()).run(new Jsx(JSX_SRC_DIR, JSX_DEST_DIR, jsxBaseDirectory.getAbsolutePath()));
                final Minify jsxMinify = jsxbuilder.build();
                allJsxs.keySet().stream().forEach((k) -> {
                    try {
                        getLog().info("Minification and copy.");

                        final String dest = root + "/" + k;
                        final InputStream stream = jsxMinify.stream(Collections.singletonList("/"+ allJsxs.get(k)));
                        new WriterManager(root, new File(dest).toURI().toURL(), destDir).run().write(stream);
                    } catch (Exception e) {
                        Throwables.propagate(e);
                    }
                });

            }
            getLog().info("Finish");
        } catch (MojoExecutionException | MojoFailureException e) {
            throw e;
        } catch (Exception ex) {
            getLog().error("Error in plugin", ex);
            throw new MojoFailureException("Error ->", ex);
        }


    }

    private InputStream getInputStream(Minify minify, Concatener concatener, ArrayListMultimap<String, String> scripts, String k, Template.Action action) throws Exception {
        switch (action) {
            case minify:
                return minify.stream(scripts.get(k));
            case concat:
                return concatener.stream(scripts.get(k));
            case delete:
                return new ByteArrayInputStream(new byte[0]);
            default:
                throw new IllegalStateException("No action found" + action);
        }
    }

    @VisibleForTesting
    void setRoot(String root) {
        this.root = root;
    }

    @VisibleForTesting
    void setDestDir(String destDir) {
        this.destDir = destDir;
    }

    @VisibleForTesting
    void setProcessor(String processor) {
        this.processor = processor;
    }

    @VisibleForTesting
    void setExtensions(String[] extensions) {
        this.extensions = extensions;
    }

    public void setJsxBaseDirectory(File jsxBaseDirectory) {
        this.jsxBaseDirectory = jsxBaseDirectory;
    }
}

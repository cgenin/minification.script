package net.genin.maven.plugin;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

/**
 * Mojo for minifying js scripts.
 *
 * @goal minify-scripts
 * @phase generate-resources
 */
public class ScriptMojo extends AbstractMojo {
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
            switch (processor) {
                case "BeautifyJs":
                    builder.toBeautifyJs();
                    break;
                case "GoogleClosure":
                    builder.toGoogleClosure();
                    break;
                case "UglifyJs":
                default:
                    builder.toUglifyJs();
            }
            final Minify minify = builder.build();
            getLog().info("Launch ...");
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
                        getLog().info("Copy " + t.url);
                        new WriterManager(root, t.url, destDir).run().copy();
                    } else {
                        getLog().info("Treat " + t.url);
//                      Copy the modified template.
                        new WriterManager(root, t.url, destDir).run().write(t.stream());
                        scripts.keySet().forEach(k -> {
//                          for each scripts
                            try {
//                              Minify and write to destination directory.
                                final String s = root + "/" + k;
                                new WriterManager(root, new File(s).toURI().toURL(), destDir).run().write(minify.stream(scripts.get(k)));

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
            getLog().info("Finish");
        } catch (MojoExecutionException | MojoFailureException e) {
            throw e;
        } catch (Exception ex) {
            getLog().error("Error in plugin", ex);
            throw new MojoFailureException("Error ->", ex);
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
}

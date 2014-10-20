package net.genin.maven.plugin;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.CompactHtmlSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Template Manager for HTML.
 * <p>This class ar used for filtering the template files and re writing <em>script</em> tags.</p>
 */
public abstract class Template {

    /**
     * Url of the file.
     */
    public final URL url;

    /**
     * map for the minified scripts.
     * <P>the key is the minified script and the values the none minified scripts.</P>
     *
     * @return the minify js script and the concatening js sources.
     */
    abstract ArrayListMultimap<String, String> scripts();

    /**
     * Open an stream which contains the resulted template.
     *
     * @return the stream.
     */
    abstract InputStream stream();

    /**
     * Constructor.
     *
     * @param url the url.
     */
    protected Template(URL url) {
        this.url = url;

    }

    /**
     * Handler for treating the different files.
     */
    @FunctionalInterface
    public static interface Handler {
        void handle(Template template);
    }

    /**
     * Builder for processing the templates.
     */
    public static class Builder {

        /**
         * List of authorized extension.
         */
        private final List<String> patterfiles;
        /**
         * Processor for not template files.
         */
        private Handler notTemplate;
        /**
         * Processor for template files.
         */
        private Handler template;

        /**
         * Constructor.
         *
         * @param patterfiles the extensions.
         */
        public Builder(List<String> patterfiles) {
            Preconditions.checkNotNull(patterfiles);
            this.patterfiles = patterfiles;
        }

        /**
         * add treament for none template files.
         *
         * @param handler the treatment.
         * @return the builder.
         */
        public Builder notTemplate(Handler handler) {
            Preconditions.checkNotNull(handler);
            notTemplate = handler;
            return this;
        }

        /**
         * add treatment for the template files.
         *
         * @param handler the treatment.
         * @return the builder.
         */
        public Builder template(Handler handler) {
            Preconditions.checkNotNull(handler);
            template = handler;
            return this;
        }

        /**
         * test if it's an template file.
         *
         * @param f the file.
         * @return true if it is.
         */
        private boolean isTemplate(final File f) {
            final String fileExtension = Files.getFileExtension(f.getAbsolutePath());
            return patterfiles.stream().map(p -> p.equals(fileExtension)).filter(b -> b).count() > 0;

        }

        /**
         * travel the tree files.
         *
         * @param root the root directory URL.
         * @throws Exception
         */
        public void traverse(final URL root) throws Exception {
            Lists.newArrayList(Files.fileTreeTraverser()
                    .breadthFirstTraversal(new File(root.toURI()))).stream()
//                   Not directory
                    .filter(File::isFile)
//                  Call Handlers
                    .forEach(f -> {
                        try {
                            final URL url1 = f.toURI().toURL();

                            if (isTemplate(f)) {
                                MoreObjects.firstNonNull(template, h -> {
                                }).handle(new TemplateScripts(url1));
                            } else {
                                MoreObjects.firstNonNull(notTemplate, h -> {
                                }).handle(new NotTemplate(url1));
                            }

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    });

        }
    }

    /**
     * Factory for creating HtmlCleaner instance.
     *
     * @return the instance.
     */
    public static HtmlCleaner factoryHtmlCleaner() {
        final CleanerProperties cleanerProps = new CleanerProperties();
        cleanerProps.setOmitXmlDeclaration(true);
        cleanerProps.setOmitComments(true);
        return new HtmlCleaner(cleanerProps);
    }

    /**
     * Class for template file.
     */
    private static class TemplateScripts extends Template {
        /**
         * The attribute which contains the relative path of the minified files.
         */
        public static final String DATA_ATTR = "data-script-min";
        /**
         * the minified content.
         */
        private byte[] datas;
        /**
         * the instance of HtmlCleaner.
         */
        private static final HtmlCleaner HTML_CLEANER = factoryHtmlCleaner();

        /**
         * Constructor.
         *
         * @param url url of the file
         */
        private TemplateScripts(URL url) {
            super(url);
        }

        /**
         * Test if the script tag contains the specific attribute.
         *
         * @param tagNode the tag node.
         * @return true if exists.
         */
        private boolean ownAttribute(TagNode tagNode) {
            return tagNode.hasAttribute("data-script-min");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        ArrayListMultimap<String, String> scripts() {
            final ArrayListMultimap<String, String> results = ArrayListMultimap.create();

            try {
                final TagNode clean = HTML_CLEANER.clean(url);
                clean.traverse((parentNode, htmlNode) -> {
                    if (htmlNode instanceof TagNode
                            && isScript((TagNode) htmlNode)
                            && ownAttribute((TagNode) htmlNode)) {

                        results.put(((TagNode) htmlNode).getAttributeByName(DATA_ATTR),
                                ((TagNode) htmlNode).getAttributeByName("src"));
                        parentNode.removeChild(htmlNode);
                    }
                    return true;
                });
                CompactHtmlSerializer serializer =
                        new CompactHtmlSerializer(HTML_CLEANER.getProperties());

                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                serializer.writeToStream(clean, out);
                datas = out.toByteArray();

                if (!results.isEmpty()) {
                    addingMinifyScripts(results, serializer);
                }

            } catch (Exception ex) {
                Throwables.propagate(ex);
            }


            return results;
        }

        /**
         * add the minify tags.
         *
         * @param scripts    the script map.
         * @param serializer the serializer
         * @throws IOException
         */
        private void addingMinifyScripts(final ArrayListMultimap<String, String> scripts, final CompactHtmlSerializer serializer) throws IOException {
            final TagNode cleanForAddingScripts = HTML_CLEANER.clean(stream());
            cleanForAddingScripts.traverse((parentNode, htmlNode) -> {
                if (htmlNode instanceof TagNode
                        && isBody((TagNode) htmlNode)) {
                    scripts.keySet().forEach(
                            k -> {
                                final TagNode script = new TagNode("script");
                                script.addAttribute("type", "application/javascript");
                                script.addAttribute("src", k);
                                ((TagNode) htmlNode).addChild(script);
                            }
                    );

                }
                return true;
            });
            final ByteArrayOutputStream outForAddingScripts = new ByteArrayOutputStream();
            serializer.writeToStream(cleanForAddingScripts, outForAddingScripts);
            datas = outForAddingScripts.toByteArray();
        }

        /**
         * Test if it's body tag.
         *
         * @param htmlNode the tag node.
         * @return true if it's body tag.
         */
        private boolean isBody(TagNode htmlNode) {
            return "body".equals((((TagNode) htmlNode)).getName());
        }

        /**
         * return the modified template.
         *
         * @return the stream.
         */
        @Override
        InputStream stream() {
            return new ByteArrayInputStream(datas);
        }

        /**
         * Test if it's script tag.
         *
         * @param htmlNode the tag node.
         * @return true if it's script tag.
         */
        private boolean isScript(TagNode htmlNode) {
            return "script".equals((((TagNode) htmlNode)).getName());
        }
    }

    /**
     * Class for none template file.
     */
    private static class NotTemplate extends Template {
        /**
         * Constructor
         *
         * @param url url of the file.
         */
        private NotTemplate(final URL url) {
            super(url);
        }

        @Override
        InputStream stream() {
            try {
                return Resources.asByteSource(url).openStream();
            } catch (Exception e) {
                Throwables.propagate(e);
            }
            return null;
        }

        @Override
        ArrayListMultimap<String, String> scripts() {
            return ArrayListMultimap.create();
        }
    }
}

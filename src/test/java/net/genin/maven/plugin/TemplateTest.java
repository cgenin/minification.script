package net.genin.maven.plugin;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import static org.junit.Assert.*;

import com.google.common.collect.Multimap;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * Created by skarb on 19/10/2014.
 */
public class TemplateTest {

    @Test
    public void testTemplate() throws Exception {

        final URL resource = Resources.getResource("template");
        new Template.Builder(Lists.newArrayList("html"))
                .notTemplate(t -> assertFalse(true))
                .template(t -> {
                    final Multimap<String, String> scripts = t.scripts();
                    assertFalse(scripts.isEmpty());
                    assertEquals(1, scripts.size());
                    final Collection<String> actual = scripts.get("/min/test.min.js");
                    assertEquals(1, actual.size());
                    assertEquals("/js/test.js", actual.iterator().next());

                    try {
                        final String s = CharStreams.toString(new InputStreamReader(t.stream(), Charsets.UTF_8));
                        assertEquals("<!DOCTYPE html>\n" +
                                "<html><head lang=\"en\"> \n" +
                                "<meta charset=\"UTF-8\" /> \n" +
                                "<title>TEST</title> </head><body> \n" +
                                "<script type=\"application/javascript\" src=\"/min/test.min.js\"></script></body></html>", s);

                    } catch (IOException e) {
                        Throwables.propagate(e);
                    }

                }).traverse(resource);
    }
    @Test
    public void testNotTemplate() throws Exception {

        final URL resource = Resources.getResource("nottemplate");
        new Template.Builder(Lists.newArrayList("html")).notTemplate(t -> {
            assertTrue(t.url.toString().endsWith("essai.json"));
            assertTrue(t.scripts().isEmpty());

        }).template(t -> assertFalse(true)).traverse(resource);
    }

    @Test
    public void testTemplateWithNoScript() throws Exception {

        final URL resource = Resources.getResource("template2");
        new Template.Builder(Lists.newArrayList("html"))
                .notTemplate(t -> assertFalse(true))
                .template(t -> {
                    final Multimap<String, String> scripts = t.scripts();
                    assertTrue(scripts.isEmpty());

                    try {
                        final String s = CharStreams.toString(new InputStreamReader(t.stream(), Charsets.UTF_8));
                        assertEquals("<!DOCTYPE html>\n" +
                                "<html><head lang=\"en\"> \n" +
                                "<meta charset=\"UTF-8\" /> \n" +
                                "<title>TEST</title>  </head><body> \n" +
                                " \n" +
                                "<script type=\"application/javascript\" src=\"/js/test.js\"></script> \n" +
                                " \n" +
                                "</body></html>", s);

                    } catch (IOException e) {
                        Throwables.propagate(e);
                    }

                }).traverse(resource);
    }

    @Test
    public void testTemplateEmptyScript() throws Exception {

        final URL resource = Resources.getResource("template3");
        new Template.Builder(Lists.newArrayList("html"))
                .notTemplate(t -> assertFalse(true))
                .template(t -> {
                    final Multimap<String, String> scripts = t.scripts();
                    assertTrue(scripts.isEmpty());

                    try {
                        final String s = CharStreams.toString(new InputStreamReader(t.stream(), Charsets.UTF_8));
                        assertEquals("<!DOCTYPE html>\n" +
                                "<html><head lang=\"en\"> \n" +
                                "<meta charset=\"UTF-8\" /> \n" +
                                "<title>TEST</title>  </head><body> \n" +
                                "  \n" +
                                "<h1>TEST</h1> \n" +
                                " \n" +
                                "</body></html>", s);

                    } catch (IOException e) {
                        Throwables.propagate(e);
                    }

                }).traverse(resource);
    }

    @Test
    public void testTemplateWithMany() throws Exception {

        final URL resource = Resources.getResource("template4");
        new Template.Builder(Lists.newArrayList("html"))
                .notTemplate(t -> assertFalse(true))
                .template(t -> {
                    final Multimap<String, String> scripts = t.scripts();
                    assertFalse(scripts.isEmpty());
                    assertEquals(2, scripts.keySet().size());
                    final List<String> actual = Lists.newArrayList(scripts.get("/min/test.min.js"));
                    assertEquals(2, actual.size());
                    assertTrue(actual.contains("/js/test.js"));
                    assertTrue(actual.contains("/js/test2.js"));

                    final List<String> newScript = Lists.newArrayList(scripts.get("/min/new.min.js"));
                    assertEquals(1, newScript.size());
                    assertTrue(newScript.contains("/js/new.js"));

                    try {
                        final String s = CharStreams.toString(new InputStreamReader(t.stream(), Charsets.UTF_8));
                        assertEquals("<!DOCTYPE html>\n" +
                                "<html><head lang=\"en\"> \n" +
                                "<meta charset=\"UTF-8\" /> \n" +
                                "<title>TEST</title> \n" +
                                "<script type=\"application/javascript\" src=\"/js/another.js\"></script> </head><body> \n" +
                                "<script type=\"application/javascript\" src=\"/min/new.min.js\"></script><script type=\"application/javascript\" src=\"/min/test.min.js\"></script></body></html>", s);

                    } catch (IOException e) {
                        Throwables.propagate(e);
                    }

                }).traverse(resource);
    }

    @Test
    public void testTemplateWithDelete() throws Exception {

        final URL resource = Resources.getResource("template5");
        new Template.Builder(Lists.newArrayList("html"))
                .notTemplate(t -> assertFalse(true))
                .template(t -> {
                    final Multimap<String, String> scripts = t.scripts();
                    assertFalse(scripts.isEmpty());
                    assertEquals(1, scripts.keySet().size());

                    final List<String> newScript = Lists.newArrayList(scripts.get("/min/new.min.js"));
                    assertEquals(1, newScript.size());
                    assertTrue(newScript.contains("/js/new.js"));

                    try {
                        final String s = CharStreams.toString(new InputStreamReader(t.stream(), Charsets.UTF_8));
                        assertEquals("<!DOCTYPE html>\n" +
                                "<html><head lang=\"en\"> \n" +
                                "<meta charset=\"UTF-8\" /> \n" +
                                "<title>TEST</title> \n" +
                                "<script type=\"application/javascript\" src=\"/js/another.js\"></script> </head><body> \n" +
                                "<script type=\"application/javascript\" src=\"/min/new.min.js\"></script></body></html>", s);

                    } catch (IOException e) {
                        Throwables.propagate(e);
                    }

                }).traverse(resource);
    }
}

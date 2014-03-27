package org.jnode.plugin.model;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.jnode.nanoxml.XMLElement;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.jnode.plugin.model.XMLConstants.AUTO_START;
import static org.jnode.plugin.model.XMLConstants.CLASS;
import static org.jnode.plugin.model.XMLConstants.EXCLUDE;
import static org.jnode.plugin.model.XMLConstants.EXPORT;
import static org.jnode.plugin.model.XMLConstants.EXTENSION;
import static org.jnode.plugin.model.XMLConstants.EXTENSION_POINT;
import static org.jnode.plugin.model.XMLConstants.ID;
import static org.jnode.plugin.model.XMLConstants.IMPORT;
import static org.jnode.plugin.model.XMLConstants.LIBRARY;
import static org.jnode.plugin.model.XMLConstants.LICENSE_NAME;
import static org.jnode.plugin.model.XMLConstants.LICENSE_URL;
import static org.jnode.plugin.model.XMLConstants.NAME;
import static org.jnode.plugin.model.XMLConstants.PLUGIN;
import static org.jnode.plugin.model.XMLConstants.POINT;
import static org.jnode.plugin.model.XMLConstants.PRIORITY;
import static org.jnode.plugin.model.XMLConstants.PROVIDER_NAME;
import static org.jnode.plugin.model.XMLConstants.PROVIDER_URL;
import static org.jnode.plugin.model.XMLConstants.REQUIRES;
import static org.jnode.plugin.model.XMLConstants.RUNTIME;
import static org.jnode.plugin.model.XMLConstants.SYSTEM;
import static org.jnode.plugin.model.XMLConstants.TYPE;
import static org.jnode.plugin.model.XMLConstants.VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Tests for class {@link org.jnode.plugin.model.PluginDescriptorBuilder}.
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
@RunWith(Theories.class)
public class PluginDescriptorBuilderTest {
    @DataPoint
    public static final MissingValue NULL_VALUE = new MissingValue(null);
    @DataPoint
    public static final MissingValue EMPTY_VALUE = new MissingValue("");
    @DataPoint
    public static final MissingValue BLANK_VALUE = new MissingValue(" ");

    protected static final String DESC_ID = "net.sf.cglib";
    protected static final String DESC_PROVIDER_NAME = "Provider name";
    protected static final String DESC_PROVIDER_URL = "http://url/to/provider";
    protected static final boolean DESC_SYSTEM = true;
    protected static final String DESC_LICENSE_NAME = "apache2.0";
    protected static final String DESC_LICENSE_URL = "http://url/to/license";
    protected static final int DESC_PRIORITY = 1;
    protected static final String DESC_NAME = "cglib classes";
    protected static final String DESC_VERSION = "2.1";
    protected static final boolean DESC_AUTO_START = true;
    protected static final Class<? extends Plugin> DESC_CLASS = MockPlugin.class;
    protected static final String EXT_ALIASES = "aliases";
    protected static final String EXT_FONTS = "fonts";
    protected static final String EXT_ALIAS = "alias";
    protected static final String EXT_FONT = "font";
    protected static final String ATTR_NAME = "name";
    protected static final String ATTR_RESOURCE = "resource";
    protected static final String ATTR_ALIAS_1 = "alias1";
    protected static final String ATTR_ALIAS_2 = "alias2";
    protected static final String ATTR_CLASS = "class";
    protected static final Class<?> ATTR_ALIAS_CLASS_1 = MockClass1.class;
    protected static final Class<?> ATTR_ALIAS_CLASS_2 = MockClass2.class;
    protected static final String ATTR_FONT_1 = "font1";
    protected static final String ATTR_FONT_2 = "font2";
    protected static final String DESC_LIB_NAME = "cglib.jar";
    protected static final String DESC_LIB_TYPE = "resource";
    protected static final String DESC_EXPORT_NAME = "net.sf.cglib.*";
    protected static final String DESC_EXCLUDE_NAME = "some.exclude.*";
    protected static final String EXT_POINT_ID = "lookAndFeels";
    protected static final String EXT_POINT_NAME = "Look and feels";
    protected static final String ANOTHER_PLUGIN = "an.other.plugin";

    // TODO manage more tags/attributes

    private final String rootTagName;

    public PluginDescriptorBuilderTest() {
        this(PLUGIN);
    }

    protected PluginDescriptorBuilderTest(String rootTagName) {
        this.rootTagName = rootTagName;
    }

    //////////////////////////////
    ////// Test normal case //////
    //////////////////////////////

    @Test
    public void testBuildXmlElement() throws Exception {
        PluginDescriptorBuilder builder = createPluginDescriptorBuilder();
        builder.setProviderName(DESC_PROVIDER_NAME);
        builder.setProviderURL(DESC_PROVIDER_URL);
        builder.setLicenseURL(DESC_LICENSE_URL);
        builder.setPluginClass(DESC_CLASS);
        builder.setSystem(DESC_SYSTEM);
        builder.setAutoStart(DESC_AUTO_START);
        builder.setPriority(DESC_PRIORITY);

        PluginDescriptorBuilder.LibraryBuilder library = builder.addRuntimeLibrary(DESC_LIB_NAME, DESC_LIB_TYPE);
        library.addExports(DESC_EXPORT_NAME).addExcludes(DESC_EXCLUDE_NAME);

        PluginDescriptorBuilder.ExtensionBuilder extension = builder.addExtension(EXT_ALIASES, EXT_ALIAS);
        extension.newElement().addAttribute(ATTR_NAME, ATTR_ALIAS_1).addAttribute(ATTR_CLASS, ATTR_ALIAS_CLASS_1);
        extension.newElement().addAttribute(ATTR_NAME, ATTR_ALIAS_2).addAttribute(ATTR_CLASS, ATTR_ALIAS_CLASS_2);

        extension = builder.addExtension(EXT_FONTS, EXT_FONT);
        extension.newElement().addAttribute(ATTR_RESOURCE, ATTR_FONT_1);
        extension.newElement().addAttribute(ATTR_RESOURCE, ATTR_FONT_2);

        builder.addExtensionPoint(EXT_POINT_ID, EXT_POINT_NAME);
        builder.addRequires(ANOTHER_PLUGIN);

        XMLElement xmlElement = builder.buildXmlElement();

        assertNotNull(xmlElement);
        assertEquals(createExpectedXML(), writeToString(xmlElement));
    }

    //////////////////////////////
    ////// Test constructor //////
    //////////////////////////////

    @Theory
    public void testConstructor_missingDescriptorId(MissingValue wrongValue) throws Exception {
        if (!getClass().equals(PluginDescriptorBuilderTest.class)) return;
        try {
            new PluginDescriptorBuilder(wrongValue.toString(), DESC_NAME, DESC_LICENSE_NAME, DESC_VERSION);
            fail("An IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    @Theory
    public void testConstructor_missingDescriptorName(MissingValue wrongValue) throws Exception {
        if (!getClass().equals(PluginDescriptorBuilderTest.class)) return;
        try {
            new PluginDescriptorBuilder(DESC_ID, wrongValue.toString(), DESC_LICENSE_NAME, DESC_VERSION);
            fail("An IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    @Theory
    public void testConstructor_missingLicenseName(MissingValue wrongValue) throws Exception {
        if (!getClass().equals(PluginDescriptorBuilderTest.class)) return;
        try {
            new PluginDescriptorBuilder(DESC_ID, DESC_NAME, wrongValue.toString(), DESC_VERSION);
            fail("An IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    @Theory
    public void testConstructor_missingVersion(MissingValue wrongValue) throws Exception {
        if (!getClass().equals(PluginDescriptorBuilderTest.class)) return;
        try {
            new PluginDescriptorBuilder(DESC_ID, DESC_NAME, DESC_LICENSE_NAME, wrongValue.toString());
            fail("An IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    //////////////////////////////
    ///// Test setPluginClass ////
    //////////////////////////////

    @Test
    public void testSetPluginClass_missingPluginClass() throws Exception {
        if (!getClass().equals(PluginDescriptorBuilderTest.class)) return;

        PluginDescriptorBuilder builder = createPluginDescriptorBuilder();
        try {
            builder.setPluginClass(null);
            fail("An IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    //////////////////////////////
    /// Test addRuntimeLibrary ///
    //////////////////////////////

    @Theory
    public void testAddRuntimeLibrary_missingName(MissingValue wrongValue) throws Exception {
        if (!getClass().equals(PluginDescriptorBuilderTest.class)) return;

        PluginDescriptorBuilder builder = createPluginDescriptorBuilder();
        try {
            builder.addRuntimeLibrary(wrongValue.toString(), DESC_LIB_TYPE);
            fail("An IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    //////////////////////////////
    ////// Test addExports ///////
    //////////////////////////////

    @Test(expected = PluginException.class)
    public void testAddExports_duplicateExports() throws Exception {
        PluginDescriptorBuilder builder = createPluginDescriptorBuilder();
        builder.setProviderName(DESC_PROVIDER_NAME);
        builder.addRuntimeLibrary(DESC_LIB_NAME).addExports(DESC_EXPORT_NAME, DESC_EXPORT_NAME);
    }

    @Test(expected = PluginException.class)
    public void testAddExports_duplicateExports_twoCalls() throws Exception {
        PluginDescriptorBuilder builder = createPluginDescriptorBuilder();
        builder.setProviderName(DESC_PROVIDER_NAME);
        builder.addRuntimeLibrary(DESC_LIB_NAME).addExports(DESC_EXPORT_NAME).addExports(DESC_EXPORT_NAME);
    }

    //////////////////////////////
    ////// Test addExcludes //////
    //////////////////////////////

    @Test(expected = PluginException.class)
    public void testAddExcludes_duplicateExcludes() throws Exception {
        PluginDescriptorBuilder builder = createPluginDescriptorBuilder();
        builder.setProviderName(DESC_PROVIDER_NAME);
        builder.addRuntimeLibrary(DESC_LIB_NAME).addExcludes(DESC_EXCLUDE_NAME, DESC_EXCLUDE_NAME);
    }

    @Test(expected = PluginException.class)
    public void testAddExcludes_duplicateExcludes_twoCalls() throws Exception {
        PluginDescriptorBuilder builder = createPluginDescriptorBuilder();
        builder.setProviderName(DESC_PROVIDER_NAME);
        builder.addRuntimeLibrary(DESC_LIB_NAME).addExcludes(DESC_EXCLUDE_NAME).addExcludes(DESC_EXCLUDE_NAME);
    }

    //////////////////////////////
    ////// Test addExtension /////
    //////////////////////////////

    @Theory
    public void testAddExtension_missingExtensionPoint(MissingValue wrongValue) throws Exception {
        if (!getClass().equals(PluginDescriptorBuilderTest.class)) return;

        PluginDescriptorBuilder builder = createPluginDescriptorBuilder();
        try {
            builder.addExtension(wrongValue.toString(), EXT_FONT);
            fail("An IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    @Theory
    public void testAddExtension_missingTagName(MissingValue wrongValue) throws Exception {
        if (!getClass().equals(PluginDescriptorBuilderTest.class)) return;

        PluginDescriptorBuilder builder = createPluginDescriptorBuilder();
        try {
            builder.addExtension(EXT_FONTS, wrongValue.toString());
            fail("An IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    //////////////////////////////
    ////// Test addAttribute /////
    //////////////////////////////

    @Theory
    public void testAddAttribute_missingAttributeName(MissingValue wrongValue) throws Exception {
        if (!getClass().equals(PluginDescriptorBuilderTest.class)) return;

        PluginDescriptorBuilder builder = createPluginDescriptorBuilder();
        try {
            PluginDescriptorBuilder.ExtensionBuilder extensionBuilder = builder.addExtension(EXT_FONTS, EXT_FONT);
            extensionBuilder.newElement().addAttribute(wrongValue.toString(), ATTR_ALIAS_CLASS_1);
            fail("An IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    //////////////////////////////
    /// Test addExtensionPoint ///
    //////////////////////////////

    @Theory
    public void testAddExtensionPoint_missingExtensionPointId(MissingValue wrongValue) throws Exception {
        if (!getClass().equals(PluginDescriptorBuilderTest.class)) return;

        PluginDescriptorBuilder builder = createPluginDescriptorBuilder();
        try {
            builder.addExtensionPoint(wrongValue.toString(), EXT_POINT_NAME);
            fail("An IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    @Theory
    public void testAddExtensionPoint_missingExtensionPointName(MissingValue wrongValue) throws Exception {
        if (!getClass().equals(PluginDescriptorBuilderTest.class)) return;

        PluginDescriptorBuilder builder = createPluginDescriptorBuilder();
        try {
            builder.addExtensionPoint(EXT_POINT_ID, wrongValue.toString());
            fail("An IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    //////////////////////////////
    ////// Test addRequires //////
    //////////////////////////////

    @Theory
    public void testAddRequires_missingPluginId(MissingValue wrongValue) throws Exception {
        if (!getClass().equals(PluginDescriptorBuilderTest.class)) return;

        PluginDescriptorBuilder builder = createPluginDescriptorBuilder();
        try {
            builder.addRequires(wrongValue.toString());
            fail("An IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    //////////////////////////////
    ////// Test xml validity /////
    //////////////////////////////

    @Test
    public void testXmlTreeIsValid() throws Exception {
        PluginDescriptorBuilder builder = createPluginDescriptorBuilder();
        XMLElement element = builder.buildXmlElement();

        PluginDescriptor descriptor = Factory.parseDescriptor(element);

        // do only a lite verification because parseDescriptor already checks many things
        assertNotNull(descriptor);
        assertEquals(DESC_ID, descriptor.getId());
    }

    //////////////////////////////
    ////// Internal methods //////
    //////////////////////////////

    protected PluginDescriptorBuilder createPluginDescriptorBuilder() {
        return new PluginDescriptorBuilder(DESC_ID, DESC_NAME, DESC_LICENSE_NAME, DESC_VERSION);
    }

    protected String getExpectedRootAttributes() {
        return PROVIDER_URL + "=\"" + DESC_PROVIDER_URL + "\" " +
            ID + "=\"" + DESC_ID + "\" " +
            SYSTEM + "=\"" + DESC_SYSTEM + "\" " +
            LICENSE_NAME + "=\"" + DESC_LICENSE_NAME + "\" " +
            PRIORITY + "=\"" + DESC_PRIORITY + "\" " +
            NAME + "=\"" + DESC_NAME + "\" " +
            AUTO_START + "=\"" + DESC_AUTO_START + "\" " +
            CLASS + "=\"" + DESC_CLASS.getName() + "\" " +
            LICENSE_URL + "=\"" + DESC_LICENSE_URL + "\" " +
            PROVIDER_NAME + "=\"" + DESC_PROVIDER_NAME + "\" " +
            VERSION + "=\"" + DESC_VERSION + "\"";
    }

    private final String createExpectedXML() {
        //TODO add header to written xml ?
//    String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//        "<!DOCTYPE plugin SYSTEM \"jnode.dtd\">\n";
        return "<" + rootTagName + " " + getExpectedRootAttributes() + ">" +
            "<" + RUNTIME + ">" +
            "<" + LIBRARY + " name=\"" + DESC_LIB_NAME + "\" " + TYPE + "=\"" + DESC_LIB_TYPE + "\">" +
            "<" + EXPORT + " name=\"" + DESC_EXPORT_NAME + "\"/>" +
            "<" + EXCLUDE + " name=\"" + DESC_EXCLUDE_NAME + "\"/>" +
            "</" + LIBRARY + ">" +
            "</" + RUNTIME + ">" +
            createExtensionXML(EXT_ALIASES, EXT_ALIAS,
                attributes(ATTR_NAME, ATTR_ALIAS_1, ATTR_CLASS, ATTR_ALIAS_CLASS_1),
                attributes(ATTR_NAME, ATTR_ALIAS_2, ATTR_CLASS, ATTR_ALIAS_CLASS_2)) +
            createExtensionXML(EXT_FONTS, EXT_FONT, attributes(ATTR_RESOURCE, ATTR_FONT_1),
                attributes(ATTR_RESOURCE, ATTR_FONT_2)) +
            "<" + EXTENSION_POINT + " " + ID + "=\"" + EXT_POINT_ID + "\" " + NAME + "=\"" + EXT_POINT_NAME + "\"/>" +
            "<" + REQUIRES + ">" +
            "<" + IMPORT + " " + PLUGIN + "=\"" + ANOTHER_PLUGIN + "\"/>" +
            "</" + REQUIRES + ">" +
            "</" + rootTagName + ">";
    }

    private String createExtensionXML(String point, String alias, Map<String, String>... allAttributes) {
        StringBuilder buffer = new StringBuilder('<' + EXTENSION + ' ' + POINT + "=\"" + point + "\">");
        for (Map<String, String> attributes : allAttributes) {
            buffer.append('<' + alias + ' ');
            boolean first = true;
            for (String attribute : attributes.keySet()) {
                if (!first) {
                    buffer.append(' ');
                }

                buffer.append(attribute).append("=\"").append(attributes.get(attribute)).append('\"');
                first = false;
            }
            buffer.append("/>");
        }
        buffer.append("</" + EXTENSION + '>');
        return buffer.toString();
    }

    private static String writeToString(XMLElement xmlElement) throws IOException {
        StringWriter writer = new StringWriter();
        xmlElement.write(writer);
        return writer.getBuffer().toString();
    }

    private static Map<String, String> attributes(Object... attributes) {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < attributes.length; i += 2) {
            final Object value = attributes[i + 1];
            final String valueStr;
            if (value instanceof Class) {
                valueStr = ((Class) value).getName();
            } else {
                valueStr = (String) value;
            }
            result.put((String) attributes[i], valueStr);
        }
        return result;
    }

    private static class MockClass1 {
    }

    private static class MockClass2 {
    }

    protected static class MissingValue {
        private final String value;

        private MissingValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}

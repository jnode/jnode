package org.jnode.plugin.model;

import org.junit.experimental.theories.Theory;

import static org.jnode.plugin.model.XMLConstants.AUTO_START;
import static org.jnode.plugin.model.XMLConstants.CLASS;
import static org.jnode.plugin.model.XMLConstants.FRAGMENT;
import static org.jnode.plugin.model.XMLConstants.ID;
import static org.jnode.plugin.model.XMLConstants.LICENSE_NAME;
import static org.jnode.plugin.model.XMLConstants.LICENSE_URL;
import static org.jnode.plugin.model.XMLConstants.NAME;
import static org.jnode.plugin.model.XMLConstants.PLUGIN_ID;
import static org.jnode.plugin.model.XMLConstants.PLUGIN_VERSION;
import static org.jnode.plugin.model.XMLConstants.PRIORITY;
import static org.jnode.plugin.model.XMLConstants.PROVIDER_NAME;
import static org.jnode.plugin.model.XMLConstants.PROVIDER_URL;
import static org.jnode.plugin.model.XMLConstants.SYSTEM;
import static org.jnode.plugin.model.XMLConstants.VERSION;
import static org.junit.Assert.fail;

/**
 * Tests for class {@link org.jnode.plugin.model.FragmentDescriptorBuilder}.
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public class FragmentDescriptorBuilderTest extends PluginDescriptorBuilderTest {
    private static final String DESC_PLUGIN_ID = "plugin.id";
    private static final String DESC_PLUGIN_VERSION = "2.3";

    public FragmentDescriptorBuilderTest() {
        super(FRAGMENT);
    }

    @Theory
    public void testBuildXmlElement_missingPluginId(MissingValue wrongValue) throws Exception {
        try {
            new FragmentDescriptorBuilder(DESC_ID, DESC_NAME, DESC_LICENSE_NAME, DESC_VERSION,
                wrongValue.toString(), DESC_PLUGIN_VERSION);
            fail("An IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    @Theory
    public void testBuildXmlElement_missingPluginVersion(MissingValue wrongValue) throws Exception {
        try {
            new FragmentDescriptorBuilder(DESC_ID, DESC_NAME, DESC_LICENSE_NAME, DESC_VERSION,
                DESC_PLUGIN_ID, wrongValue.toString());
            fail("An IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    @Override
    protected FragmentDescriptorBuilder createPluginDescriptorBuilder() {
        return new FragmentDescriptorBuilder(DESC_ID, DESC_NAME, DESC_LICENSE_NAME, DESC_VERSION, DESC_PLUGIN_ID,
            DESC_PLUGIN_VERSION);
    }

    @Override
    protected String getExpectedRootAttributes() {
        return
            PLUGIN_ID + "=\"" + DESC_PLUGIN_ID + "\" " +
                CLASS + "=\"" + DESC_CLASS.getName() + "\" " +
                AUTO_START + "=\"" + DESC_AUTO_START + "\" " +
                PLUGIN_VERSION + "=\"" + DESC_PLUGIN_VERSION + "\" " +
                LICENSE_URL + "=\"" + DESC_LICENSE_URL + "\" " +
                VERSION + "=\"" + DESC_VERSION + "\" " +
                PROVIDER_NAME + "=\"" + DESC_PROVIDER_NAME + "\" " +
                ID + "=\"" + DESC_ID + "\" " +
                PROVIDER_URL + "=\"" + DESC_PROVIDER_URL + "\" " +
                SYSTEM + "=\"" + DESC_SYSTEM + "\" " +
                LICENSE_NAME + "=\"" + DESC_LICENSE_NAME + "\" " +
                PRIORITY + "=\"" + DESC_PRIORITY + "\" " +
                NAME + "=\"" + DESC_NAME + "\"";
    }
}

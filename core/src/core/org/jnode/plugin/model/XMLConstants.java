package org.jnode.plugin.model;

/**
 * Tags and attributes for the XML representation of an {@link org.jnode.plugin.model.PluginDescriptorModel} .
 */
class XMLConstants {
    // plugin tags and attributes
    public static final String PLUGIN = "plugin";
    public static final String ID = "id";
    public static final String LICENSE_NAME = "license-name";
    public static final String LICENSE_URL = "license-url";
    public static final String NAME = "name";
    public static final String PROVIDER_NAME = "provider-name";
    public static final String PROVIDER_URL = "provider-url";
    public static final String VERSION = "version";
    public static final String CLASS = "class";
    public static final String SYSTEM = "system";
    public static final String AUTO_START = "auto-start";
    public static final String PRIORITY = "priority";

    public static final String LIBRARY = "library";
    public static final String TYPE = "type";
    public static final String EXPORT = "export";
    public static final String RUNTIME = "runtime";
    public static final String IMPORT = "import";
    public static final String REQUIRES = "requires";
    public static final String EXCLUDE = "exclude";

    public static final String POINT = "point";
    public static final String EXTENSION = "extension";
    public static final String EXTENSION_POINT = "extension-point";

    // fragment tags and attributes
    public static final String FRAGMENT = "fragment";
    public static final String PLUGIN_ID = "plugin-id";
    public static final String PLUGIN_VERSION = "plugin-version";
}

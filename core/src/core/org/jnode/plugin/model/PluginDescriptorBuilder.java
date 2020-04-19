package org.jnode.plugin.model;

import java.util.Hashtable;
import org.jnode.nanoxml.XMLElement;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginException;

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

/**
 * Class used to build the XML representation of a {@link org.jnode.plugin.model.PluginDescriptorModel}.
 *
 * @see {@link org.jnode.plugin.model.PluginDescriptorBuilder#buildXmlElement()}}
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public class PluginDescriptorBuilder {
    private final XMLElement root;

    public PluginDescriptorBuilder(String id, String name, String licenseName, String version) {
        root = createXMLElement(getRootTagName());
        setRequiredRootAttribute(ID, id);
        setRequiredRootAttribute(NAME, name);
        setRequiredRootAttribute(LICENSE_NAME, licenseName);
        setRequiredRootAttribute(VERSION, version);
    }

    public XMLElement buildXmlElement() {
        return root;
    }

    public final void setProviderName(String providerName) {
        root.setAttribute(PROVIDER_NAME, providerName);
    }


    public final void setProviderURL(String providerURL) {
        root.setAttribute(PROVIDER_URL, providerURL);
    }

    public final void setLicenseURL(String licenseURL) {
        root.setAttribute(LICENSE_URL, licenseURL);
    }

    public final void setPluginClass(Class<? extends Plugin> pluginClass) {
        checkParameterIsNotNull("parameter pluginClass", pluginClass);
        root.setAttribute(CLASS, pluginClass.getName());
    }

    public final void setSystem(boolean system) {
        root.setAttribute(SYSTEM, Boolean.toString(system));
    }

    public final void setAutoStart(boolean autoStart) {
        root.setAttribute(AUTO_START, Boolean.toString(autoStart));
    }

    public final void setPriority(int priority) {
        root.setAttribute(PRIORITY, priority);
    }

    public final LibraryBuilder addRuntimeLibrary(String name) throws PluginException {
        return addRuntimeLibrary(name, null);
    }

    public final LibraryBuilder addRuntimeLibrary(String name, String type) throws PluginException {
        checkRequiredParameter("parameter name", name);

        // Find or create runtime tag
        XMLElement runtime = findElementByTagName(root, RUNTIME, null, true);

        // Find or create library tag
        XMLElement library = findElementByTagName(runtime, LIBRARY, name, true);
        library.setAttribute(NAME, name);
        if (type != null) {
            library.setAttribute(TYPE, type);
        }

        return new LibraryBuilder(library);
    }

    public final ExtensionBuilder addExtension(String extensionPoint, String tagName) {
        checkRequiredParameter("parameter extensionPoint", extensionPoint);
        checkRequiredParameter("parameter tagName", tagName);

        XMLElement extension = null;
        for (XMLElement ext : root.getChildren()) {
            if (ext.getName().equals(EXTENSION)) {
                Object point = ext.getAttribute(POINT);
                if ((point != null) && point.equals(extensionPoint)) {
                    extension = ext;
                    break;
                }
            }
        }

        if (extension == null) {
            extension = createXMLElement(EXTENSION);
            extension.setAttribute(POINT, extensionPoint);
            root.addChild(extension);
        }

        return new ExtensionBuilder(extension, tagName);
    }

    public void addExtensionPoint(String extensionPointId, String extensionPointName) {
        checkRequiredParameter("parameter extensionPointId", extensionPointId);
        checkRequiredParameter("parameter extensionPointName", extensionPointName);

        XMLElement extensionPoint = createXMLElement(EXTENSION_POINT);
        extensionPoint.setAttribute(ID, extensionPointId);
        extensionPoint.setAttribute(NAME, extensionPointName);
        root.addChild(extensionPoint);
    }

    public void addRequires(String pluginId) {
        checkRequiredParameter("parameter pluginId", pluginId);

        XMLElement requires = findElementByTagName(root, REQUIRES, null, true);
        XMLElement imp = createXMLElement(IMPORT);
        imp.setAttribute(PLUGIN, pluginId);
        requires.addChild(imp);
    }

    public static final class ExtensionBuilder {
        private final XMLElement extension;
        private final String tagName;

        private ExtensionBuilder(XMLElement extension, String tagName) {
            this.extension = extension;
            this.tagName = tagName;
        }

        public ExtensionBuilderElement newElement() {
            XMLElement element = createXMLElement(tagName);
            extension.addChild(element);
            return new ExtensionBuilderElement(element);
        }
    }

    public static final class ExtensionBuilderElement {
        private final XMLElement element;

        private ExtensionBuilderElement(XMLElement element) {
            this.element = element;
        }

        public ExtensionBuilderElement addAttribute(String name, String value) {
            element.setAttribute(name, value);
            return this;
        }

        public ExtensionBuilderElement addAttribute(String name, Class<?> value) {
            checkRequiredParameter("parameter name", name);
            return addAttribute(name, (value == null) ? null : value.getName());
        }
    }

    public class LibraryBuilder {
        private final XMLElement library;

        public LibraryBuilder(XMLElement library) {
            this.library = library;
        }

        public LibraryBuilder addExports(String... exportNames) throws PluginException {
            return addElements("export", EXPORT, exportNames);
        }

        public LibraryBuilder addExcludes(String... excludeNames) throws PluginException {
            return addElements("exclude", EXCLUDE, excludeNames);
        }

        private LibraryBuilder addElements(String elementType, String elementTag, String... elementNames)
            throws PluginException {
            for (String elementName : elementNames) {
                XMLElement element = findElementByTagName(library, elementTag, elementName, false);
                if (element != null) {
                    throw new PluginException(
                        "Duplicate " + elementType + "s for library " + library.getAttribute(NAME) + " : " +
                            elementName);
                }

                element = createXMLElement(elementTag);
                element.setAttribute(NAME, elementName);
                library.addChild(element);
            }

            return this;
        }
    }

    protected final void setRequiredRootAttribute(String attributeName, String attributeValue) {
        setRequiredAttribute(root, attributeName, attributeValue);
    }

    protected static final void setRequiredAttribute(XMLElement element, String attributeName, String attributeValue) {
        checkRequiredParameter("attribute " + attributeName, attributeValue);

        if (attributeValue == null) {
            element.removeAttribute(attributeName);
        } else {
            element.setAttribute(attributeName, attributeValue);
        }
    }

    private static void checkRequiredParameter(String parameterName, String parameterValue) {
        checkParameterIsNotNull(parameterName, parameterValue);

        if (parameterValue.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "The required " + parameterName + " is blank or empty (value='" + parameterValue + "')");
        }
    }

    private static void checkParameterIsNotNull(String parameterName, Object parameterValue) {
        if (parameterValue == null) {
            throw new IllegalArgumentException("The required " + parameterName + " is null");
        }
    }

    protected String getRootTagName() {
        return PLUGIN;
    }

    private static XMLElement createXMLElement(String tagName) {
        XMLElement element = new XMLElement(new Hashtable<String, String>(), true, false);
        element.setName(tagName);
        return element;
    }

    private static XMLElement findElementByTagName(XMLElement node, String tagName, String nameAttributeValue,
                                                   boolean create) {
        XMLElement element = null;
        for (XMLElement e : node.getChildren()) {
            if (tagName.equals(e.getName())) {
                if ((nameAttributeValue == null) || nameAttributeValue.equals(e.getAttribute(NAME))) {
                    element = e;
                    break;
                }
            }
        }

        if (create && (element == null)) {
            element = createXMLElement(tagName);
            node.addChild(element);
        }

        return element;
    }
}

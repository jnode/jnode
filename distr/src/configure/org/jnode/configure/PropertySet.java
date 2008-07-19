/*
 * $Id $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jnode.configure;

import java.io.File;
import java.util.LinkedHashMap;

import net.n3.nanoxml.XMLElement;

import org.jnode.configure.adapter.FileAdapter;
import org.jnode.configure.adapter.FileAdapterFactory;

/**
 * A property set denotes a group of properties, typically associated with a
 * file.
 * 
 * @author crawley@jnode.org
 */
public class PropertySet {
    public static class Property {
        private final String name;
        private final PropertyType type;
        private final String description;
        private Value defaultValue;
        private Value value;
        private final XMLElement definingElement;
        private final File definingFile;

        public Property(String name, PropertyType type, String description, Value defaultValue,
                XMLElement definingElement, File definingFile) {
            super();
            this.name = name;
            this.type = type;
            this.description = description;
            this.defaultValue = defaultValue;
            this.definingElement = definingElement;
            this.definingFile = definingFile;
        }

        public Value getValue() {
            return value == null ? defaultValue : value;
        }

        public void setValue(Value value) {
            this.value = value;
        }

        public boolean isSet() {
            return value != null;
        }

        public String getName() {
            return name;
        }

        public PropertyType getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public XMLElement getDefiningElement() {
            return definingElement;
        }

        public File getDefiningFile() {
            return definingFile;
        }

        public boolean hasDefaultValue() {
            return defaultValue != null;
        }

        public Value getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(Value defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    public static class Value {
        private final String token;
        private final String text;

        public Value(String token, String text) {
            super();
            if (token.equals("")) {
                throw new IllegalArgumentException("Empty 'token' string");
            }
            this.token = token;
            this.text = text;
        }

        public String getToken() {
            return token;
        }

        public String getText() {
            return text;
        }

        public String toString() {
            return "'" + token + "'/'" + text + "'";
        }
    }

    private final File file;
    private final File defaultFile;
    private final File templateFile;
    private final char marker;
    private final ConfigureScript script;
    private final FileAdapter adapter;
    private final LinkedHashMap<String, Property> properties =
            new LinkedHashMap<String, Property>();

    public PropertySet(ConfigureScript script, File file, File defaultFile, File templateFile,
            String fileFormat, char marker) throws ConfigureException {
        this.file = file;
        this.defaultFile = defaultFile;
        this.templateFile = templateFile;
        this.marker = marker;
        this.script = script;
        this.adapter = fileFormat == null ? null : FileAdapterFactory.createAdapter(fileFormat);
        if (adapter != null) {
            if (!adapter.isLoadSupported() && defaultFile != null) {
                throw new ConfigureException("A '" + ScriptParser.DEFAULT_FILE +
                        "' attribute cannot be used with " + " format '" + fileFormat +
                        "': the format does not support property loading.");
            }
            if (!adapter.isSaveSupported() && templateFile == null) {
                throw new ConfigureException("A '" + ScriptParser.TEMPLATE_FILE +
                        "' attribute is required with " + " format '" + fileFormat +
                        "': the format does not support property saving.");
            }
        }
    }

    public PropertySet(ConfigureScript script) throws ConfigureException {
        this.file = null;
        this.defaultFile = null;
        this.templateFile = null;
        this.marker = 0;
        this.script = script;
        this.adapter = null;
    }

    public void load(Configure configure) throws ConfigureException {
        adapter.load(this, configure);
    }

    public void save(Configure configure) throws ConfigureException {
        adapter.save(this, configure);
    }

    public File getFile() {
        return file;
    }

    public File getDefaultFile() {
        return defaultFile;
    }

    public File getTemplateFile() {
        return templateFile;
    }

    public char getMarker() {
        return marker;
    }

    public LinkedHashMap<String, Property> getProperties() {
        return properties;
    }

    public void addProperty(String name, PropertyType propType, String description,
            Value defaultValue, XMLElement definingElement, File definingFile)
        throws ConfigureException {
        Property oldProp = script.getProperty(name);
        if (oldProp != null) {
            // FIXME ... alternatively, we could allow properties to be defined
            // in multiple
            // contexts and have them refer to the same value.
            throw new ConfigureException("Property '" + name + "' already declared at line " +
                    oldProp.getDefiningElement().getLineNr() + " of " + oldProp.getDefiningFile());
        }
        Property prop =
                new Property(name, propType, description, defaultValue, definingElement,
                        definingFile);
        properties.put(name, prop);
        script.addProperty(prop);
    }

    public void setProperty(String name, Value value) throws ConfigureException {
        Property property = properties.get(name);
        if (property == null) {
            throw new ConfigureException("Property not declared: '" + name + "'");
        }
        property.setValue(value);
    }

    public Property getProperty(String name) {
        return properties.get(name);
    }
}

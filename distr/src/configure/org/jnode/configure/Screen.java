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

import java.util.ArrayList;
import java.util.List;

import org.jnode.configure.PropertySet.Value;

/**
 * This class represents a 'screen' that sets a number of properties.
 * 
 * @author crawley@jnode.org
 */
public class Screen {
    
    public static class Item {
        private final ConfigureScript script;
        private final String propName;
        private final String text;

        public Item(ConfigureScript script, String propName, String text) {
            this.script = script;
            this.propName = propName;
            this.text = text;
        }

        public ConfigureScript getScript() {
            return script;
        }

        public String getPropName() {
            return propName;
        }

        public String getText() {
            return text;
        }
    }

    private final String title;
    private final String guardProp;
    private final Value valueIs;
    private final Value valueIsNot;
    private final List<Item> items = new ArrayList<Item>();
    
    public Screen(String title, String guardProp, Value valueIs, Value valueIsNot) {
        this.title = title;
        this.guardProp = guardProp;
        this.valueIs = valueIs;
        this.valueIsNot = valueIsNot;
    }

    public String getTitle() {
        return title;
    }

    public String getGuardProp() {
        return guardProp;
    }

    public Value getValueIs() {
        return valueIs;
    }

    public Value getValueIsNot() {
        return valueIsNot;
    }

    public List<Item> getItems() {
        return items;
    }
    
    public void addItem(Item item) {
        items.add(item);
    }

    /**
     * Test to see if this script is executable (yet).  A screen is
     * executable if it has no guard property, or its guard property
     * is set, and has the appropriate value.  Note that we test using
     * the PropertyValue's 'value' attribute, not the 'token' attribute.
     * 
     * @param script this gives us the context for property lookup
     * @return <code>true</true> if the screen is executable, <code>false</code> otherwise.
     */
    public boolean isExecutable(ConfigureScript script) {
        if (guardProp == null) {
            return true;
        }
        PropertySet.Property prop = script.getProperty(guardProp);
        if (!prop.isSet()) {
            return false;
        }
        Value value = prop.getValue();
        if (valueIs != null) {
            return valueIs.getText().equals(value.getText());
        } else if (valueIsNot != null) {
            return !valueIsNot.getText().equals(value.getText());
        } else {
            return true;
        }
    }

    public void execute(Configure configure, ConfigureScript script) throws ConfigureException {
        configure.output(title);
        for (Item item : items) {
        	String text = item.getText();
        	if (text != null) {
        		configure.output(text);
        	}
        	PropertySet.Property prop = script.getProperty(item.getPropName());
            Value value = null;
            do {
                String info = prop.getType().describe(prop.getDefaultValue());
                String input = configure.input(
                        prop.getDescription() + " " + info + ": ");
                if (input == null) {
                	throw new ConfigureException("Unexpected EOF on input");
                }
                value = prop.getType().fromInput(input);
                if (value == null && input.length() == 0 && prop.hasDefaultValue()) {
                	value = prop.getDefaultValue();
                }
            } while (value == null);
            if (value != null) {
                prop.setValue(value);
            }
        }
    }
}

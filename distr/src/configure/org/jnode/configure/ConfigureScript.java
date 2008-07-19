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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jnode.configure.PropertySet.Property;

/**
 * This class provides the in-memory representation corresponding to an XML
 * configure script. The 'execute' method runs the script.
 * 
 * @author crawley@jnode.org
 */
public class ConfigureScript {
    private final File baseDir;
    private final File scriptFile;
    private final ArrayList<PropertySet> propsFiles = new ArrayList<PropertySet>();
    private PropertySet controlProps;
    private final ArrayList<Screen> screens = new ArrayList<Screen>();
    private final HashMap<String, PropertyType> types = new HashMap<String, PropertyType>();
    private final HashMap<String, Property> allProperties = new HashMap<String, Property>();

    public ConfigureScript(File scriptFile) {
        super();
        this.scriptFile = scriptFile;
        this.baseDir = scriptFile.getAbsoluteFile().getParentFile();
    }

    public File getBaseDir() {
        return baseDir;
    }

    public File getScriptFile() {
        return scriptFile;
    }

    public PropertySet getControlProps() {
        return controlProps;
    }

    public void setControlProps(PropertySet controlProps) {
        this.controlProps = controlProps;
    }

    public List<PropertySet> getPropsFiles() {
        return propsFiles;
    }

    public void addPropsFile(PropertySet propsFile) {
        propsFiles.add(propsFile);
    }

    public void addScreen(Screen screen) {
        screens.add(screen);
    }

    public Map<String, PropertyType> getTypes() {
        return types;
    }

    public void addType(PropertyType type) {
        types.put(type.getTypeName(), type);
    }

    /**
     * Lookup a property by name in the script's property namespace.
     * 
     * @param propName the property name
     * @return a property or <code>null</code>
     */
    public Property getProperty(String propName) {
        return allProperties.get(propName);
    }

    /**
     * Add a property to the script's property namespace
     * 
     * @param prop the property to be added.
     */
    public void addProperty(Property prop) {
        allProperties.put(prop.getName(), prop);
    }

    /**
     * Execute this script using the supplied IConfigure to interact with the
     * user. We create a work list of screens, then repeatedly scan the list
     * looking for the first screen that is ready to be executed. When a screen
     * is found, we remove it from the work list and execute it. The process
     * stops when the work list is empty, or none of the remaining screens are
     * executable.
     * 
     * @param configure
     */
    public void execute(Configure configure) throws ConfigureException {
        List<Screen> workList = new LinkedList<Screen>();
        workList.addAll(screens);
        boolean progress;
        do {
            progress = false;
            for (Iterator<Screen> it = workList.iterator(); it.hasNext(); /**/) {
                Screen screen = it.next();
                if (screen.isExecutable(this)) {
                    screen.execute(configure, this);
                    it.remove();
                    progress = true;
                    break;
                }
            }
        } while (!workList.isEmpty() && progress);
    }
}

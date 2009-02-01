/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.test.shell.harness;

import java.util.ArrayList;
import java.util.List;

public class TestSetSpecification {

    private final List<TestSpecification> specs = 
        new ArrayList<TestSpecification>();
    
    private final List<PluginSpecification> plugins = 
        new ArrayList<PluginSpecification>();
    
    private final String title;

    public TestSetSpecification(String title) {
        super();
        this.title = title;
    }

    public List<TestSpecification> getSpecs() {
        return specs;
    }

    public List<PluginSpecification> getPlugins() {
        return plugins;
    }

    public String getTitle() {
        return title;
    }
    
    public void addPluginSpec(PluginSpecification plugin) {
        plugins.add(plugin);
    }
    
    public void addTestSpec(TestSpecification spec) {
        specs.add(spec);
        spec.setTestSet(this);
    }
}

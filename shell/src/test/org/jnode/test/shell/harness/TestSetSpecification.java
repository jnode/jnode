/*
 * $Id$
 *
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
    
    private final List<TestSetSpecification> sets = 
        new ArrayList<TestSetSpecification>();
    
    private final List<PluginSpecification> plugins = 
        new ArrayList<PluginSpecification>();
    
    private final String title;
    private final String base;
    
    private TestSetSpecification parentSet;

    public TestSetSpecification(String title, String base) {
        super();
        this.title = title;
        this.base = base;
    }

    public List<TestSetSpecification> getSets() {
        return sets;
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
    
    public String getBase() {
        return base;
    }

    public void addPluginSpec(PluginSpecification plugin) {
        plugins.add(plugin);
    }
    
    public void addTestSpec(TestSpecification spec) {
        specs.add(spec);
        spec.setTestSet(this);
    }

    public void addTestSetSpecification(TestSetSpecification set) {
        sets.add(set);
        set.setParentSet(this);
    }

    private void setParentSet(TestSetSpecification set) {
        this.parentSet = set;
    }

    public TestSetSpecification getParentSet() {
        return parentSet;
    }
}

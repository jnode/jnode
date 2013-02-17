/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.plugin.model;
//TODO FIX THIS TO NOT BREAK THE BUILD
/*
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jnode.nanoxml.XMLElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginPrerequisite;
import org.jnode.plugin.PluginReference;
import org.junit.Before;
import org.junit.Test;


public class PluginDescriptorModelTest {

    private PluginDescriptorModel model;

    @Before
    public void setUp() throws PluginException {
        XMLElement element = new XMLElement();
        element.parseString("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE plugin SYSTEM \"jnode.dtd\"><plugin id=\"model1\" name=\"model1 name\" version=\"version\" provider-name=\"provider\" license-name=\"lgpl\"><requires><import plugin=\"plug1\"/><import plugin=\"plug2\"/></requires><runtime><library name=\"plugin.jar\"><export name=\"content.*\"/></library></runtime><extension point=\"extension\"><alias name=\"alias\" class=\"class\"/></extension></plugin>");
        model = new PluginDescriptorModel(element);
    }

    @Test
    public void testGetId() {
        assertEquals("model1", model.getId());
    }

    @Test
    public void testGetName() {
        assertEquals("model1 name", model.getName());
    }

    @Test
    public void testGetVersion() {
        assertEquals("version", model.getVersion());
    }

    @Test
    public void testGetProvider() {
        assertEquals("provider", model.getProviderName());
        assertNull(model.getProviderUrl());
    }

    @Test
    public void testGetLicence() {
        assertEquals("lgpl", model.getLicenseName());
        assertNull(model.getLicenseUrl());
    }

    @Test
    public void testGetPrerequisites() {
        PluginPrerequisite[] prerequisites = model.getPrerequisites();
        assertEquals(2, prerequisites.length);

    }

    @Test
    public void testDependencyFound() {
        assertTrue(model.depends("plug1"));
    }

    @Test
    public void testDependencyNotFound() {
        assertFalse(model.depends("plug3"));
    }

    @Test
    public void testGetExtension() {
        Extension[] extensions = model.getExtensions();
        assertEquals(1, extensions.length);
    }

    @Test
    public void testGetRuntime() {
        org.jnode.plugin.Runtime runtime = model.getRuntime();
        assertEquals(1, runtime.getLibraries().length);
    }

    @Test
    public void testHasCustomPluginClass() {
        assertFalse(model.hasCustomPluginClass());
    }

    @Test
    public void testIsAutoStart() {
        assertFalse(model.isAutoStart());
    }

    @Test
    public void testNoPriorityDefined() {
        assertEquals(5, model.getPriority());
    }

    @Test
    public void testToString() {
        assertEquals("model1", model.toString());
        model.getPluginReference();
    }

    @Test
    public void testGetPluginReference() {
        PluginReference reference = model.getPluginReference();
        assertEquals("model1", reference.getId());
        assertEquals("version", reference.getVersion());
    }
}
*/

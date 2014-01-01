/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.test;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jnode.util.NumberUtils;
import org.jnode.vm.VmSystemClassLoader;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.x86.VmX86Architecture32;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;

/**
 * <description>
 * 
 * @author epr
 */
// FIXME
@Ignore
public class TestVmClass {

    private VmClassLoader clc;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        VmSystemClassLoader bib =
                new VmSystemClassLoader(new URL("file:///" + System.getProperty("classes.dir") +
                        "/"), new VmX86Architecture32());
        VmType.initializeForBootImage(bib);
        clc = bib;
    }

    @org.junit.Test
    public void testAssignableFrom() throws Exception {

        testAssignableFrom(Map.class, HashMap.class);
        testAssignableFrom(String.class, Object.class);
        testAssignableFrom(char[].class, Object.class);
        testAssignableFrom(char[].class, char[].class);
        testAssignableFrom(String.class, Comparable.class);
        testAssignableFrom(Comparable.class, String.class);
        testAssignableFrom(Object[].class, String[].class);
    }

    private void testAssignableFrom(Class c1, Class c2) throws Exception {

        System.out.println("test " + c1.getName() + ", " + c2.getName() + " = " +
                c1.isAssignableFrom(c2));

        final VmType vmC1 = clc.loadClass(c1.getName(), true);
        final VmType vmC2 = clc.loadClass(c2.getName(), true);

        System.out.println("\t" + vmC1.getName() + "\t0x" + NumberUtils.hex(vmC1.getAccessFlags()) +
                "\tarray " + vmC1.isArray());
        System.out.println("\t" + vmC2.getName() + "\t0x" + NumberUtils.hex(vmC2.getAccessFlags()) +
                "\tarray " + vmC2.isArray());

        if (c1.isAssignableFrom(c2)) {
            Assert.assertTrue(c1.getName() + " is assignableFrom " + c2.getName(),
                    vmC1.isAssignableFrom(vmC2));
        } else {
            Assert.assertFalse(c1.getName() + " is not assignableFrom " + c2.getName(),
                    vmC1.isAssignableFrom(vmC2));
        }

        if (c2.isAssignableFrom(c1)) {
            Assert.assertTrue(c2.getName() + " is assignableFrom " + c1.getName(),
                    vmC2.isAssignableFrom(vmC1));
        } else {
            Assert.assertFalse(c2.getName() + " is not assignableFrom " + c1.getName(),
                    vmC2.isAssignableFrom(vmC1));
        }
    }

}

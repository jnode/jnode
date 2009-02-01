/*
 * $Id$
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
 
package org.jnode.driver.system.acpi.vm;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.jnode.driver.system.acpi.aml.Aml;
import org.jnode.driver.system.acpi.aml.ParseNode;
import org.jnode.driver.system.pnp.PnP;

/**
 * NameSpace.
 * <p/>
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class NameSpace extends AcpiNamedObject {

    public static final NameSpace rootNameSpace = new NameSpace("\\");
    public static NameSpace currentNameSpace = rootNameSpace;

    private final Map<String, AcpiObject> objects = new HashMap<String, AcpiObject>();

    public NameSpace() {
    }

    public NameSpace(String name) {
        super(name);
    }

    public NameSpace(NameSpace root, String name) {
        super(root, name);
    }

    public static NameSpace getRoot() {
        return rootNameSpace;
    }

    public void put(AcpiObject object) {
        /*
           * if (rootNameSpace.objects==null) { rootNameSpace.objects=new Hashtable();
           * rootNameSpace.put(rootNameSpace); // self put
           */
        objects.put(object.getName(), object);
    }

    public void put(String name, AcpiNamedObject object) {
//      if (rootNameSpace.objects == null) {
//          rootNameSpace.objects = new Hashtable();
//          rootNameSpace.put(rootNameSpace); // self put
//      }
        objects.put(name, object);
        object.setName(name);
    }

    public AcpiObject get(String path) {
        return (AcpiObject) (currentNameSpace.objects.get(path));
    }

    public void remove(String ref) {
        if (objects != null)
            objects.remove(ref);
    }

    public void reset() {
        currentNameSpace = rootNameSpace;
    }

    public void parse(ParseNode root) {
        parse(this, root);
    }

    public void parse(NameSpace origin, ParseNode root) {
        if (root == null)
            return;
        ParseNode op = root.geChild();

        while (op != null) {
            switch (op.getType()) {
                case Aml.AML_SCOPE:
                    Scope scope = new Scope(origin, op.getNameToString());
                    parse(scope, op);
                    break;
                case Aml.AML_DEVICE:
                    Device device = new Device(origin, op.getNameToString());
                    Object address = op.findNameValue("_HID");
                    if (address instanceof String)
                        device.addAddress("_HID", (String) address);
                    else if (address instanceof Integer)
                        device.addAddress("_HID", PnP.eisaIdToString(((Integer) address).intValue()));
                    parse(device, op);
                    break;
            }
            op = op.getNext();
        }
    }

    public void dump(PrintWriter out, String prefix) {
        out.println(toString(prefix));
        for (AcpiObject obj : objects.values()) {
            obj.dump(out, prefix + "   ");
        }
    }

    public void dump(PrintWriter out) {
        dump(out, "");
    }

    public String toString(String prefix) {
        return super.toString(prefix);
    }

    public String toString() {
        return toString("");
    }
}

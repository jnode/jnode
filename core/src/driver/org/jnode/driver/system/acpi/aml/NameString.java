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
 
package org.jnode.driver.system.acpi.aml;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * NameString.
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

public class NameString implements Comparable {

    private String prefix;
    private final List<String> namePath = new ArrayList<String>();

    public NameString() {
        this.prefix = null;
    }

    public NameString(String prefix) {
        this.prefix = prefix;
    }

    public int compareTo(Object o) {
        if (o instanceof NameString) {
            final NameString compared = (NameString) o;
            if (prefix.length() == compared.prefix.length()) {
                if (namePath.size() == compared.namePath.size()) {
                    for (int i = 0; i < namePath.size(); i++) {
                        if ((namePath.get(i)).equals(compared.namePath.get(i))) {
                            return 0;
                        }
                    }
                }
            }
        }
        return -1;
    }

    public boolean equals(Object o) {
        if (o instanceof NameString) {
            NameString compared = (NameString) o;

            if (prefix == null) {
                if (compared.prefix != null)
                    return false;
            } else {
                if (compared.prefix == null)
                    return false;
                if (!prefix.equals(compared.prefix))
                    return false;

            }

            if (namePath.size() == compared.namePath.size()) {
                for (int i = 0; i < namePath.size(); i++) {
                    if (((String) namePath.get(i)).equals(compared.namePath.get(i)))
                        return true;
                }
            }

        }
        return false;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void add(String nameseg) {
        namePath.add(nameseg);
    }

    public void addNamePath(ByteBuffer buffer) {
        StringBuilder nameseg = new StringBuilder(4);
        for (int i = 0; i < 4; i++)
            nameseg.append((char) buffer.get());
        this.add(nameseg.toString());
    }

    public void addDualNamePath(ByteBuffer buffer) {
        //StringBuffer nameseg = new StringBuffer();
        for (int i = 0; i < 2; i++)
            addNamePath(buffer);
    }

    public void addMultiNamePath(ByteBuffer buffer) {
        int count = (buffer.get() & 0xff);
        for (int i = 0; i < count; i++)
            addNamePath(buffer);
    }

    public int getSegCount() {
        return namePath.size();
    }

    public void setNamePath(String namepath) {
        namePath.clear();
        namePath.add(namepath);
    }

    public String getNameseg(int index) {
        return namePath.get(index);
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        if (prefix != null)
            result.append(prefix);
        for (int i = 0; i < namePath.size(); i++) {
            result.append(namePath.get(i));
            if (i < namePath.size() - 1)
                result.append('.');
        }
        return result.toString();

    }
}

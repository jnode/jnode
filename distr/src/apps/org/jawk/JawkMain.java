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

package org.jawk;

import java.io.IOException;

/**
 * Wrapper to call Awk.invoke from a main() method for JNode
 *
 * @author chris boertien
 */
public final class JawkMain {
    public static void main(String... args) {
        if (args.length == 0) {
            throw new RuntimeException();
        }
        int rc = 1;
        try {
            rc = Awk.invoke(args);
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        } catch (ClassNotFoundException e) {
            System.err.println(e.getLocalizedMessage());
        } catch (IllegalArgumentException e) {
            System.err.println(e.getLocalizedMessage());
        } catch (Exception e) {
            System.err.println("Unhandled Exception: " + e.getLocalizedMessage());
        } finally {
            System.exit(rc);
        }
    }
}

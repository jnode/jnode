/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package java.util.prefs;

/**
 * @author Levente S\u00e1ntha
 */
public class NativeFileSystemPreferences {
    /**
     * @see java.util.prefs.FileSystemPreferences#lockFile0(String, int, boolean)
     */
    private static int[] lockFile0(String fileName, int permission, boolean shared){
        //todo implement it
        return new int[]{1, 0};
    }

    /**
     *
     * @see java.util.prefs.FileSystemPreferences#unlockFile0(int)
     */
    private static int unlockFile0(int lockHandle){
        //todo implement it
        return 0;
    }

    /**
     * @see java.util.prefs.FileSystemPreferences#chmod(String, int)
     */
    private static int chmod(String fileName, int permission){
        //todo implement it
        return 0;
    }
}

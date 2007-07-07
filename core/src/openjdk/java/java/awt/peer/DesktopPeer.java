/*
 * Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package java.awt.peer;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.awt.Desktop.Action;

/**
 * The <code>DesktopPeer</code> interface provides methods for the operation
 * of open, edit, print, browse and mail with the given URL or file, by 
 * launching the associated application. 
 * <p>
 * Each platform has an implementation class for this interface.
 * 
 */
public interface DesktopPeer {    
    /**
     * Returns whether the given action is supported on the current platform. 
     * @param action the action type to be tested if it's supported on the 
     *        current platform. 
     * @return <code>true</code> if the given action is supported on 
     *         the current platform; <code>false</code> otherwise.
     */    
    public boolean isSupported(Action action);

    /**
     * Launches the associated application to open the given file. The 
     * associated application is registered to be the default file viewer for 
     * the file type of the given file. 
     * 
     * @param file the given file.
     * @throws IOException If the given file has no associated application, 
     *         or the associated application fails to be launched.          
     */
    public void open(File file) throws IOException;
    
    /**
     * Launches the associated editor and opens the given file for editing. The 
     * associated editor is registered to be the default editor for the file 
     * type of the given file. 
     *
     * @param file the given file.
     * @throws IOException If the given file has no associated editor, or 
     *         the associated application fails to be launched. 
     */
    public void edit(File file) throws IOException;
    
    /**
     * Prints the given file with the native desktop printing facility, using 
     * the associated application's print command.
     *
     * @param file the given file.
     * @throws IOException If the given file has no associated application 
     *         that can be used to print it.
     */
    public void print(File file) throws IOException;
    
    /**
     * Launches the mail composing window of the user default mail client, 
     * filling the message fields including to, cc, etc, with the values 
     * specified by the given mailto URL. 
     * 
     * @param uri represents a mailto URL with specified values of the message.
     *        The syntax of mailto URL is defined by 
     *        <a href="http://www.ietf.org/rfc/rfc2368.txt">RFC2368: The mailto 
     *        URL scheme</a>
     * @throws IOException If the user default mail client is not found, 
     *         or it fails to be launched.
     */
    public void mail(URI mailtoURL) throws IOException;
    
    /**
     * Launches the user default browser to display the given URI. 
     *
     * @param uri the given URI.
     * @throws IOException If the user default browser is not found, 
     *         or it fails to be launched.
     */
    public void browse(URI url) throws IOException;
}

/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.shell.command.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.JarURLConnection;
import java.net.URL;
import java.security.Permission;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

import org.apache.tools.ant.Main;
import org.apache.tools.ant.launch.Locator;
import org.jnode.security.JNodeSecurityManager;
import org.jnode.security.JNodeSecurityManager.SecurityManagerListener;
import org.jnode.shell.help.ParsedArguments;

/**
 * @author Fabien DUMINY (fduminy at users.sourceforge.net)
 *
 */
public class AntCommand
{
    public static void main(String[] args)
    throws Exception {
        makeBuildXml("build.xml");
        
        //final Set<Permission> permissions = new HashSet<Permission>();  
//        SecurityManagerListener l = new SecurityManagerListener()
//        {
//
//            public boolean checkPermission(Permission perm)
//            {
//                //permissions.add(perm);
//                return false; // false : don't check the permission
//            }
//            
//        };
//        
//        ((JNodeSecurityManager) System.getSecurityManager()).setListener(l);
        Main.main(args);
//        ((JNodeSecurityManager) System.getSecurityManager()).setListener(null);

//        System.out.println("permissions:\n");            
//        for(Permission p : permissions)
//        {
//            System.out.println(p);            
//        }
    }
    
    public static void makeBuildXml(String file) throws Exception 
    {
        FileOutputStream fos = new FileOutputStream(new File(file));
        PrintWriter pw = new PrintWriter(fos);
        pw.println("<project name=\"JNode\" default=\"help\" basedir=\".\">");
        pw.println("<target name=\"help\">");
        pw.println("<echo>");
        pw.println("echo task is working");
        pw.println("</echo>");
        pw.println("</target>");
        pw.println("</project>");
        pw.close();
        fos.close();
    }
}

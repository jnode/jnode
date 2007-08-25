/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.jnode.shell.command.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.apache.tools.ant.Main;

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
        File f = new File(file);
        if(!f.exists()){
            System.out.println("build.xml not found, creating a template");
            FileOutputStream fos = new FileOutputStream(f);
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
}

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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarFile;

import org.apache.tools.ant.launch.Launcher;
import org.apache.tools.ant.launch.Locator;
import org.jnode.shell.help.ParsedArguments;

/**
 * TODO implement the command when jar file system will be ready
 * @author Fabien DUMINY (fduminy at users.sourceforge.net)
 *
 */
public class AntCommand
{
//    static final ClassNameArgument ARG_CLASS = new ClassNameArgument("classname", "the class to excute");
//    static final Argument ARG_ARGS = new Argument("arg", "the argument(s) to pass to the class", Argument.MULTI);
//
//    public static Help.Info HELP_INFO = new Help.Info(
//        "java",
//        "Execute the main method of the given java class",
//        new Parameter[]{
//            new Parameter(ARG_CLASS, Parameter.MANDATORY),
//            new Parameter(ARG_ARGS, Parameter.OPTIONAL)
//        }
//    );

    public static void main(String[] args)
    throws Exception {
        //AntCommand.execute(HELP_INFO.parse(args), System.in, System.out, System.err);

        File sourceJar = getClassSource(Launcher.class);
        
        String[] args2 = new String[0];
        Launcher.main(args2);
    }

    /**
     * Execute this command
     */
    public static void execute(
        ParsedArguments cmdLine,
        InputStream in,
        PrintStream out,
        PrintStream err)
        throws Exception {
        
    }
    
    // Locator.getClassSource : 
    public static File getClassSource(Class c) throws IOException {
        System.out.println("class="+c.getName());
        String classResource = c.getName().replace('.', '/') + ".class";
        System.out.println("classResource="+classResource);
        return getResourceSource(c.getClassLoader(), classResource);
    }
    
    // Locator.getResourceSource : 
    public static File getResourceSource(ClassLoader c, String resource) throws IOException {
        if (c == null) {
            c = Locator.class.getClassLoader();
        }
        System.out.println("ClassLoader="+((c != null) ? c.getClass().getName() : ""));
        
        URL url = null;
        if (c == null) {
            url = ClassLoader.getSystemResource(resource);
            System.out.println("url(1)="+url);            
        } else {
            url = c.getResource(resource);
            System.out.println("url(2)="+url);
        }
        
        // Create a URL that refers to an entry in the jar file
        url = new URL("jar:file:/c:/almanac/my.jar!/com/mycompany/MyClass.class");
    
        // Get the jar file
        JarURLConnection conn = (JarURLConnection)url.openConnection();
        JarFile jarfile = conn.getJarFile();
        
        if (url != null) {
            String u = url.toString();
            if (u.startsWith("jar:file:")) {
                int pling = u.indexOf("!");
                String jarName = u.substring(4, pling);
                System.out.println("jarName="+jarName);
                return new File(Locator.fromURI(jarName));
            } else if (u.startsWith("file:")) {
                int tail = u.indexOf(resource);
                String dirName = u.substring(0, tail);
                System.out.println("dirName="+u);
                return new File(Locator.fromURI(dirName));
            }
        }
        return null;
    }    
}

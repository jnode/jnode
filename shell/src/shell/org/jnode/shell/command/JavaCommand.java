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
 
package org.jnode.shell.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.ClassNameArgument;

/**
 * @author epr
 */
public class JavaCommand {

	static final ClassNameArgument ARG_CLASS = new ClassNameArgument("classname", "the class to excute");
	static final Argument ARG_ARGS = new Argument("arg", "the argument(s) to pass to the class", Argument.MULTI);

	public static Help.Info HELP_INFO = new Help.Info(
		"java",
		"Execute the main method of the given java class",
		new Parameter[]{
			new Parameter(ARG_CLASS, Parameter.MANDATORY),
			new Parameter(ARG_ARGS, Parameter.OPTIONAL)
		}
	);

	public static void main(String[] args)
	throws Exception {
		new JavaCommand().execute(HELP_INFO.parse(args), System.in, System.out, System.err);
	}

	/**
	 * Execute this command
	 */
/*	public void execute(
		ParsedArguments cmdLine,
		InputStream in,
		PrintStream out,
		PrintStream err)
		throws Exception {

		Class cls = ARG_CLASS.getClass(cmdLine);
		Method mainMethod = cls.getMethod("main", new Class[] { String[].class });

		String[] clsArgs = ARG_ARGS.getValues(cmdLine);
		if (clsArgs == null) {
			clsArgs = new String[0];
		}

		try {
			mainMethod.invoke(null, new Object[] { clsArgs });
		} catch (InvocationTargetException ex) {
			ex.getTargetException().printStackTrace(err);
		}
	}*/

    public void execute(
		ParsedArguments cmdLine,
		InputStream in,
		PrintStream out,
		PrintStream err)
		throws Exception {

        final ClassLoader parent_cl = Thread.currentThread().getContextClassLoader();
        JCClassLoader cl = new JCClassLoader(parent_cl, new String[]{"./"});

		Class<?> cls = cl.loadClass(ARG_CLASS.getValue(cmdLine));

		Method mainMethod = cls.getMethod("main", new Class[] { String[].class });

		String[] clsArgs = ARG_ARGS.getValues(cmdLine);
		if (clsArgs == null) {
			clsArgs = new String[0];
		}

		try {
			mainMethod.invoke(null, new Object[] { clsArgs });
		} catch (InvocationTargetException ex) {
			ex.getTargetException().printStackTrace(err);
		}
	}

    private static class JCClassLoader extends ClassLoader {
         private String dirs[];

        public JCClassLoader(ClassLoader parent, String[] dir) {
            super(parent);
            this.dirs = dir;
        }

        public Class findClass(String name) throws ClassNotFoundException{
            byte[] b = loadClassData(name);
            return defineClass(name, b, 0, b.length);
        }

        protected URL findResource(String name) {
            try{
                System.out.println("Find res: " + name);
                return findResource(name, dirs);
            }catch(Exception e){

            }
            return null;
        }

        private URL findResource(String name, String[] dirs) throws Exception{

            for(int i = 0; i < dirs.length; i++){
                File d = new File(dirs[i]);
                if(d.isDirectory()){
                    System.out.println("Find res: " + name + " in " + d);
                    return findResource(name, d.list());
                }else{
                    System.out.println("Find res: " + name + " as " + d);
                    if(d.getName().equals(name)){
                        return d.toURL();
                    }
                }
            }

            return null;
        }

         private byte[] loadClassData(String name) throws ClassNotFoundException{
             String fn = name.replace('.','/');
             File f = null;
             for(int i = 0; i < dirs.length; i++){
                 f = new File(dirs[i] + fn + ".class");
                 if(f.exists()) break;
                 f = null;
             }
             if(f == null){
                 throw new ClassNotFoundException(name);
             }else{
                 byte[] data = new byte[(int) f.length()];
                 try{
                     FileInputStream fis = new FileInputStream(f);
                     fis.read(data);
                     return data;
                 }catch(Exception fnfe){
                     throw new ClassNotFoundException(name);
                 }
             }
         }
     }
}


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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ClassNameArgument;
import org.jnode.shell.syntax.StringArgument;

import java.lang.reflect.Modifier;

/**
 * This command runs a Java class by calling its 'public static void
 * main(String[])' method. The class is located by searching the the shell
 * classpath, searching the plugins, and finally looking in the current
 * directory.
 * 
 * @author epr
 * @author crawley@jnode.org
 */
public class JavaCommand extends AbstractCommand {

    private final ClassNameArgument ARG_CLASS = new ClassNameArgument(
            "className", Argument.MANDATORY, "the class to execute");
    private final StringArgument ARG_ARGS = new StringArgument(
            "arg", Argument.OPTIONAL | Argument.MULTIPLE, "the argument(s) to pass to the class");

    public JavaCommand() {
        super("Run a Java class via its 'main' method");
        registerArguments(ARG_ARGS, ARG_CLASS);
    }

    public static void main(String[] args) throws Exception {
        new JavaCommand().execute(args);
    }

    /**
     * Execute the command
     */
    public void execute(CommandLine cmdLine, InputStream in, PrintStream out, PrintStream err)
        throws Exception {

        // Build our classloader
        final ClassLoader parent_cl = Thread.currentThread().getContextClassLoader();
        JCClassLoader cl = new JCClassLoader(parent_cl, new String[]{"./"});

        Method mainMethod = null;
        String className = ARG_CLASS.getValue();
        try {
            // Find (if necessary load) the class to be executed.
            Class<?> cls = cl.loadClass(className);
            // Lookup and check the 'main' method.
            mainMethod = cls.getMethod("main", new Class[]{String[].class});
            if ((mainMethod.getModifiers() & Modifier.STATIC) == 0) {
                err.println("The 'main' method for this class is not static");
                exit(1);
            }
            if ((mainMethod.getModifiers() & Modifier.PUBLIC) == 0) {
                err.println("The 'main' method for this class is not public");
                exit(1);
            }
            String[] mainArgs = ARG_ARGS.isSet() ? ARG_ARGS.getValues() : new String[0];
            mainMethod.invoke(null, new Object[]{mainArgs});
        } catch (ClassNotFoundException ex) {
            err.println("Cannot find the requested class: " + ARG_CLASS.getValue());
            exit(1);
        } catch (NoSuchMethodException ex) {
            err.println("Cannot the 'void main(String[])' method for class " + ARG_CLASS.getValue());
            exit(1);
        } catch (InvocationTargetException ex) {
            // We unwrap and rethrow any exceptions that were thrown by 'invoke'.  It is
            // up to the shell to print a stacktrace ... or not.
            Throwable t = ex.getTargetException();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw (Error) t;
            }
        }
    }

    /**
     * This class loader looks in the supplied list of directories after 
     * checking the parent class loader.
     */
    private static class JCClassLoader extends ClassLoader {
        private String dirs[];

        public JCClassLoader(ClassLoader parent, String[] dir) {
            super(parent);
            this.dirs = dir;
        }

        public Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] b = loadClassData(name);
            return defineClass(name, b, 0, b.length);
        }

        protected URL findResource(String name) {
            try {
                return findResource(name, dirs);
            } catch (MalformedURLException e) {
                return null;
            }
        }

        private URL findResource(String name, String[] dirs) 
            throws MalformedURLException {
            for (int i = 0; i < dirs.length; i++) {
                File d = new File(dirs[i]);
                if (d.isDirectory()) {
                    return findResource(name, d.list());
                } else if (d.getName().equals(name)) {
                    return d.toURI().toURL();
                }
            }
            return null;
        }

        private byte[] loadClassData(String name) throws ClassNotFoundException {
            String fn = name.replace('.', '/');
            File f = null;
            for (int i = 0; i < dirs.length; i++) {
                f = new File(dirs[i] + fn + ".class");
                if (f.exists()) {
                    break;
                }
                f = null;
            }
            if (f == null) {
                throw new ClassNotFoundException(name);
            }
            byte[] data = new byte[(int) f.length()];
            try {
                FileInputStream fis = new FileInputStream(f);
                fis.read(data);
                return data;
            } catch (IOException ex) {
                throw new ClassNotFoundException(name, ex);
            }
        }
    }
}


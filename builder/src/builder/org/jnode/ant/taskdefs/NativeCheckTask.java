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
 
package org.jnode.ant.taskdefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.tools.ant.BuildException;
import org.jnode.vm.VmUtils;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.attrs.Attributes;

/**
 * That ant task will check that native methods are properly implemented
 * for JNode.
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public class NativeCheckTask extends FileSetTask {
    /**
     * potential implementation of the native methods for JNode.
     * key: className (String)
     * value: native methods for the class (List<NativeMethod>)
     */
    private Map<String, List<NativeMethod>> jnodeNativeMethods = new HashMap<String, List<NativeMethod>>();

    /**
     * native methods per class
     * key: className (String)
     * value: native methods for the class (List<NativeMethod>)
     */
    private Map<String, List<NativeMethod>> nativeMethods = new HashMap<String, List<NativeMethod>>();

    private Set<String> missingClasses = new TreeSet<String>();
    private Map<String, List<NativeMethod>> missingMethods = new HashMap<String, List<NativeMethod>>();

    protected void doExecute() throws BuildException {
        // process all classes to find native methods
        // and classes that could potentially implement native methods
        // for JNode
        processFiles();

        // now, check all native methods are properly implemented
        // for JNode
        int nbNativeMethods = 0;
        for (String className : nativeMethods.keySet()) {
            List<NativeMethod> methods = nativeMethods.get(className);
            for (NativeMethod method : methods) {
                nbNativeMethods++;
                checkNativeMethod(className, method);
            }
        }

        // report
        final String INDENT = "    ";

        int nbMissingClasses = missingClasses.size();
        if (!missingClasses.isEmpty()) {
            System.err.println("Missing classes:");
            for (String missingClass : missingClasses) {
                System.err.println(INDENT + missingClass);
            }
        }

        int nbMissingMethods = 0;
        if (!missingMethods.isEmpty()) {
            System.err.println("Missing methods:");
            for (String cls : missingMethods.keySet()) {
                System.err.println(INDENT + " class " + cls);
                for (NativeMethod m : missingMethods.get(cls)) {
                    System.err.println(INDENT + INDENT + m.getName());
                    nbMissingMethods++;
                }
            }
        }

        System.out.println("Found " + nbNativeMethods + " native methods in " + nativeMethods.size() + " classes");
        if ((nbMissingMethods != 0) || (nbMissingClasses != 0)) {
            System.err.println(missingClasses.size() + " missing classes. " + nbMissingMethods + " missing methods");

            String message = "Some native methods are not properly implemented (see errors above)";
            if (failOnError) {
                throw new BuildException(message);
            } else {
                System.err.println("[FAILED] " + message);
            }
        } else {
            System.out.println("[OK] All native methods are properly defined");
        }
    }

    private boolean checkNativeMethod(String className, NativeMethod method) {
        boolean hasError = false;
        String jnodeNativeClass = VmUtils.getNativeClassName(className.replace('/', '.'));

        if (jnodeNativeMethods.containsKey(jnodeNativeClass)) {
            List<NativeMethod> methods = jnodeNativeMethods.get(jnodeNativeClass);
            boolean found = false;
            for (NativeMethod nvMethod : methods) {
                if (method.getName().equals(nvMethod.getName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                List<NativeMethod> methodList = missingMethods.get(jnodeNativeClass);
                if (methodList == null) {
                    methodList = new ArrayList<NativeMethod>();
                    missingMethods.put(jnodeNativeClass, methodList);
                }

                methodList.add(method);
                hasError = true;
            }
        } else {
            missingClasses.add(jnodeNativeClass);
            hasError = true;
        }

        return hasError;
    }

    @Override
    protected void processFile(File file) throws IOException {
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);
            NativeMethodClassVisitor v = getNativeMethods(file, fis);
            if ((v != null) && !v.getNativeMethods().isEmpty()) {
                if (v.couldImplementNativeMethods()) {
                    jnodeNativeMethods.put(v.getClassName(), v.getNativeMethods());
                } else {
                    nativeMethods.put(v.getClassName(), v.getNativeMethods());
                }
            }
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    private NativeMethodClassVisitor getNativeMethods(File file, InputStream inputClass) throws BuildException {
        ClassWriter cw = new ClassWriter(false);
        NativeMethodClassVisitor v = null;
        try {
            ClassReader cr = new ClassReader(inputClass);
            v = new NativeMethodClassVisitor(file, cw);
            cr.accept(v, Attributes.getDefaultAttributes(), true);

            if (v.allowNatives()) {
                // if natives are allowed for that class, no need to check
                // for a pure java implementation
                v = null;
            }
        } catch (Exception ex) {
            System.err.println("Unable to load class in file " + file.getAbsolutePath() + " : " + ex.getMessage());
        }
        return v;
    }

    private static class NativeMethodClassVisitor extends ClassAdapter {
        private String className;
        private boolean couldImplementNativeMethods;
        private boolean allowNatives = false;
        private List<NativeMethod> nativeMethods = new ArrayList<NativeMethod>();

        public NativeMethodClassVisitor(File file, ClassVisitor cv) {
            super(cv);
        }

        @Override
        public void visit(int version,
                          int access,
                          String name,
                          String superName,
                          String[] interfaces,
                          String sourceFile) {
            this.className = name.replace('/', '.');
            this.allowNatives = VmUtils.allowNatives(className, "x86"); //TODO hard coded architecture: change that !
            this.couldImplementNativeMethods = VmUtils.couldImplementNativeMethods(className);

            super.visit(version, access, name, superName, interfaces, sourceFile);
        }

        @Override
        public CodeVisitor visitMethod(int access,
                                       String name,
                                       String desc,
                                       String[] exceptions,
                                       Attribute attrs) {
            if (!allowNatives) {
                // we don't allow native for that class =>
                // we must have a pure java implementation for that method

                if ((couldImplementNativeMethods && isStatic(access)) ||
                    isNative(access)) {
                    nativeMethods.add(new NativeMethod(access, name, desc));
                }
            }

            return null; // we don't to visit inside the method
        }

        public String getClassName() {
            return className;
        }

        public List<NativeMethod> getNativeMethods() {
            return nativeMethods;
        }

        public boolean couldImplementNativeMethods() {
            return couldImplementNativeMethods;
        }

        public boolean allowNatives() {
            return allowNatives;
        }
    }

    private static class NativeMethod {
        private final int access;
        private final String name;
        private final String desc;

        public NativeMethod(int access, String name, String desc) {
            super();
            this.access = access;
            this.name = name;
            this.desc = desc;
        }

        public int getAccess() {
            return access;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public String toString() {
            return name + " " + desc;
        }
    }

    public static boolean isNative(int access) {
        return isSet(access, org.objectweb.asm.Constants.ACC_NATIVE);
    }

    public static boolean isStatic(int access) {
        return isSet(access, org.objectweb.asm.Constants.ACC_STATIC);
    }

    private static boolean isSet(int access, int flag) {
        return ((access & flag) == flag);
    }
}

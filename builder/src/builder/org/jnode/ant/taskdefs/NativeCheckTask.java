/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.jnode.vm.facade.VmUtils;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;
import org.objectweb.asm.attrs.Attributes;

import static org.objectweb.asm.Constants.ACONST_NULL;
import static org.objectweb.asm.Constants.ANEWARRAY;
import static org.objectweb.asm.Constants.ATHROW;
import static org.objectweb.asm.Constants.DCONST_0;
import static org.objectweb.asm.Constants.DRETURN;
import static org.objectweb.asm.Constants.DUP;
import static org.objectweb.asm.Constants.FCONST_0;
import static org.objectweb.asm.Constants.FRETURN;
import static org.objectweb.asm.Constants.ICONST_0;
import static org.objectweb.asm.Constants.INVOKESPECIAL;
import static org.objectweb.asm.Constants.IRETURN;
import static org.objectweb.asm.Constants.LCONST_0;
import static org.objectweb.asm.Constants.LRETURN;
import static org.objectweb.asm.Constants.NEW;
import static org.objectweb.asm.Constants.RETURN;

/**
 * That ant task will check that native methods are properly implemented
 * for JNode.
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public class NativeCheckTask extends Task {
    /**
     * Local files where native methods could be declared.
     */
    private final List<FileSet> declarations = new ArrayList<FileSet>();

    /**
     * Classlib where native methods could be declared.
     */
    private File classlib;

    /**
     * Local files where native methods should be implemented.
     */
    private final List<FileSet> implementations = new ArrayList<FileSet>();

    private boolean failOnError;
    private boolean trace;

    /**
     * potential implementation of the native methods for JNode.
     * key: className (String)
     * value: native methods for the class (List&lt;NativeMethod&gt;)
     */
    private Map<String, List<NativeMethod>> nativeMethodsImplementations = new HashMap<String, List<NativeMethod>>();

    /**
     * native methods per class.
     * key: className (String)
     * value: native methods for the class (List&lt;NativeMethod&gt;)
     */
    private Map<String, List<NativeMethod>> nativeMethods = new HashMap<String, List<NativeMethod>>();

    private Set<String> missingClasses = new TreeSet<String>();
    private Map<String, List<NativeMethod>> missingMethods = new HashMap<String, List<NativeMethod>>();

    public void setClasslib(File classlib) {
        this.classlib = classlib;
    }

    @Override
    public void execute() throws BuildException {
        // find native methods that need to be implemented in pure Java
        log("Searching for declaration of native methods ...", Project.MSG_INFO);
        int count = processFiles(declarations, false);
        count += processClasslib();
        log(count + " declarations of native methods have been found", Project.MSG_INFO);

        // find potential implementations of native methods
        log("Searching for implementations of native methods ...", Project.MSG_INFO);
        count = processFiles(implementations, true);
        log(count + " implementations of native methods have been found", Project.MSG_INFO);

        // now, check all native methods are properly implemented for JNode
        int nbNativeMethods = 0;
        Set<String> usedJNodeNativeClasses = new HashSet<String>();
        for (String className : nativeMethods.keySet()) {
            List<NativeMethod> methods = nativeMethods.get(className);
            for (NativeMethod method : methods) {
                nbNativeMethods++;
                checkNativeMethod(className, method, usedJNodeNativeClasses);
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

        int nbMissingMethods = printMethods(missingMethods, INDENT, "Missing methods:");

        Set<String> unusedJNodeNativeClasses = new TreeSet<String>();
        List<String> ignoredClasses = new ArrayList<String>();
        if (usedJNodeNativeClasses.size() != nativeMethodsImplementations.size()) {
            for (String cls : nativeMethodsImplementations.keySet()) {
                if (!usedJNodeNativeClasses.contains(cls)) {
                    unusedJNodeNativeClasses.add(cls);
                }
            }

            if (!unusedJNodeNativeClasses.isEmpty()) {
                System.err.println("Unused JNode native classes:");
                for (String cls : unusedJNodeNativeClasses) {
                    if ("org.jnode.vm.compiler.ir.NativeTest".equals(cls)) {
                        ignoredClasses.add(cls);
                    } else {
                        System.err.println(INDENT + " class " + cls);
                    }
                }

                if (!ignoredClasses.isEmpty()) {
                    System.err.println(INDENT + " WARNING : These classes were ignored :");
                    for (String cls : ignoredClasses) {
                        System.err.println(INDENT + INDENT + " class " + cls);
                    }
                }
            }
        }

        Map<String, List<NativeMethod>> stubMethods = new HashMap<String, List<NativeMethod>>();
        for (String cls : usedJNodeNativeClasses) {
            for (NativeMethod method : nativeMethodsImplementations.get(cls)) {
                boolean isMissing = false;
                List<NativeMethod> missMethods = missingMethods.get(cls);
                if (missMethods != null) {
                    for (NativeMethod m : missMethods) {
                        if (m.getName().equals(method.getName())) {
                            isMissing = true;
                            break;
                        }
                    }
                }

                if (!isMissing && method.isStub()) {
                    List<NativeMethod> methods = stubMethods.get(cls);
                    if (methods == null) {
                        methods = new ArrayList<NativeMethod>();
                        stubMethods.put(cls, methods);
                    }
                    methods.add(method);
                }
            }
        }
        printMethods(stubMethods, INDENT, "Stub methods:");

        System.out.println("Found " + nbNativeMethods + " native methods in " + nativeMethods.size() + " classes");
        if ((nbMissingMethods != 0) || (nbMissingClasses != 0) || !unusedJNodeNativeClasses.isEmpty()) {
            System.err.println(missingClasses.size() + " missing classes. " + nbMissingMethods + " missing methods");
            System.err.println(
                unusedJNodeNativeClasses.size() + " unused JNode native classes (+" + ignoredClasses.size() +
                    " ignored classes)");
            System.err.println(stubMethods.size() + " stub methods");

            String message = "Some native methods are not properly implemented (see errors above)";
            if (failOnError) {
                throw new BuildException(message);
            } else {
                System.err.println("[FAILED] " + message);
            }
        } else {
            System.out
                .println("[OK] All native methods are properly defined (" + stubMethods.size() + " stub methods)");
        }
    }

    private static int printMethods(Map<String, List<NativeMethod>> methods, String indent, String message) {
        int nbMethods = 0;
        if (!methods.isEmpty()) {
            System.err.println(message);
            for (String cls : methods.keySet()) {
                System.err.println(indent + " class " + cls);
                for (NativeMethod m : methods.get(cls)) {
                    System.err.println(indent + indent + m.getName());
                    nbMethods++;
                }
            }
        }
        return nbMethods;
    }

    private boolean checkNativeMethod(String className, NativeMethod method, Set<String> usedJNodeNativeClasses) {
        boolean hasError = false;
        String jnodeNativeClass = VmUtils.getNativeClassName(className.replace('/', '.'));

        if (nativeMethodsImplementations.containsKey(jnodeNativeClass)) {
            List<NativeMethod> methods = nativeMethodsImplementations.get(jnodeNativeClass);
            boolean found = false;
            for (NativeMethod nvMethod : methods) {
                if (method.getName().equals(nvMethod.getName())) {
                    usedJNodeNativeClasses.add(jnodeNativeClass);
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

    protected int processClasslib() {
        int count = 0;

        JarInputStream jis = null;
        String jar = classlib.getAbsolutePath() + "!";

        try {
            jis = new JarInputStream(new FileInputStream(classlib));
            JarEntry jarEntry;
            while ((jarEntry = jis.getNextJarEntry()) != null) {
                if (!jarEntry.isDirectory() && jarEntry.getName().endsWith(".class")) {
                    byte[] buffer = new byte[(int) jarEntry.getSize()];
                    jis.read(buffer);
                    count += findNativeMethods(jar + jarEntry.getName(), new ByteArrayInputStream(buffer), false);
                }
            }
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        } finally {
            if (jis != null) {
                try {
                    jis.close();
                } catch (IOException e) {
                    throw new BuildException(e);
                }
            }
        }

        return count;
    }

    protected int findNativeMethods(String pathToClass, InputStream inputStream, boolean implementation)
        throws IOException {
        int count = 0;
        if (trace) {
            System.out.println("findNativeMethods in class " + pathToClass);
        }

        try {
            NativeMethodClassVisitor v = getNativeMethods(pathToClass, inputStream, implementation);
            if ((v != null) && !v.getMethods().isEmpty()) {
                if (implementation) {
                    nativeMethodsImplementations.put(v.getClassName(), v.getMethods());
                } else {
                    nativeMethods.put(v.getClassName(), v.getMethods());
                }
                count++;
            }
        } finally {
            inputStream.close();
        }

        return count;
    }

    private NativeMethodClassVisitor getNativeMethods(String file, InputStream inputClass, boolean implementation)
        throws BuildException {
        ClassWriter cw = new ClassWriter(false);
        NativeMethodClassVisitor v = null;
        try {
            ClassReader cr = new ClassReader(inputClass);
            v = new NativeMethodClassVisitor(cw, implementation);
            cr.accept(v, Attributes.getDefaultAttributes(), true);

            if (v.allowNatives()) {
                // if natives are allowed for that class, no need to check
                // for a pure java implementation
                v = null;
            }
        } catch (Exception ex) {
            System.err.println("ERROR : Unable to load class in file " + file + " : " + ex);
        }
        return v;
    }

    private static class NativeMethodClassVisitor extends ClassAdapter {
        private final boolean implementation;
        private String className;
        private boolean couldImplementNativeMethods;
        private boolean allowNatives = false;
        private List<NativeMethod> methods = new ArrayList<NativeMethod>();

        public NativeMethodClassVisitor(ClassVisitor cv, boolean implementation) {
            super(cv);
            this.implementation = implementation;
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
            this.couldImplementNativeMethods = implementation && VmUtils.couldImplementNativeMethods(className);

            super.visit(version, access, name, superName, interfaces, sourceFile);
        }

        @Override
        public CodeVisitor visitMethod(int access,
                                       String name,
                                       String desc,
                                       String[] exceptions,
                                       Attribute attrs) {
            CodeVisitor result = null; // by default, we don't want to visit inside the method

            if (implementation) {
                // we are looking for potential implementation of native methods
                if (couldImplementNativeMethods && isStatic(access)) {
                    NativeMethod method = new NativeMethod(access, name, desc);
                    methods.add(method);

                    result = new EmptyMethodVisitor(method);
                }
            } else {
                // we are looking for declarations of native methods
                if (isNative(access)) {
                    methods.add(new NativeMethod(access, name, desc));
                }
            }

            return result;
        }

        @Override
        public void visitInnerClass(String s, String s2, String s3, int i) {
            super.visitInnerClass(s, s2, s3, i);
        }

        public String getClassName() {
            return className;
        }

        public List<NativeMethod> getMethods() {
            return methods;
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
        private List<String> instructions = new ArrayList<String>();

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

        public boolean isStub() {
//            System.out.println(this.toString() + " -> ");
//            for (String i : instructions) {
//                System.out.println("            - " + i);
//            }

            return !isSpecialMethod() &&
                (instructions.isEmpty() || throwNew(UnsupportedOperationException.class) || returnVoid() ||
                    returnNull() || returnEmptyArray() || returnZero());
        }

        private boolean isSpecialMethod() {
            return name.equals("initIDs"); // this methods are usually empty
        }

        private boolean returnZero() {
            return returnZero(ICONST_0, IRETURN) || returnZero(LCONST_0, LRETURN) || returnZero(FCONST_0, FRETURN) ||
                returnZero(
                    DCONST_0, DRETURN);
        }

        private boolean returnZero(int const0Type, int returnType) {
            return hasInstruction(0, "visitInsn(" + const0Type + ")")
                && hasInstruction(2, "visitInsn(" + returnType + ")");
        }

        private boolean returnEmptyArray() {
            return hasInstruction(0, "visitInsn(" + ICONST_0 + ")")
                && hasInstruction(1, "visitTypeInsn(" + ANEWARRAY + ", java/lang/management/MemoryManagerMXBean)")
                && hasInstruction(2, "visitInsn(" + Constants.ARETURN + ")");
        }

        private boolean returnVoid() {
            return hasInstruction(0, "visitInsn(" + RETURN + ")");
        }

        private boolean returnNull() {
            return hasInstruction(0, "visitInsn(" + ACONST_NULL + ")")
                && hasInstruction(1, "visitInsn(" + Constants.ARETURN + ")");
        }

        private boolean throwNew(Class<? extends Throwable> clazz) {
            String classPath = clazz.getName().replace('.', '/');
            return hasInstruction(0, "visitTypeInsn(" + NEW + ", " + classPath + ")")
                && hasInstruction(1, "visitInsn(" + DUP + ")")
                && hasInstruction(2, "visitMethodInsn(" + INVOKESPECIAL + ", " + classPath + ", <init>, ()V)")
                && hasInstruction(3, "visitInsn(" + ATHROW + ")");
        }

        private boolean hasInstruction(int index, String instruction) {
            return (index < instructions.size()) && instructions.get(index).equals(instruction);
        }

        public void incInstructions(String instruction) {
            instructions.add(instruction);
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

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public void addImplementations(FileSet fs) {
        implementations.add(fs);
    }

    public void addDeclarations(FileSet fs) {
        declarations.add(fs);
    }

    private static boolean isSet(int access, int flag) {
        return ((access & flag) == flag);
    }

    protected int processFiles(List<FileSet> fileSets, boolean implementation) throws BuildException {
        int count = 0;

        final Project project = getProject();
        try {
            for (FileSet fs : fileSets) {
                final String[] files = fs.getDirectoryScanner(project)
                    .getIncludedFiles();
                final File projectDir = fs.getDir(project);
                for (String fname : files) {
                    File classFile = new File(projectDir, fname);
                    count +=
                        findNativeMethods(classFile.getAbsolutePath(), new FileInputStream(classFile), implementation);
                }
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }

        return count;
    }

    private static class EmptyMethodVisitor implements CodeVisitor {
        private final NativeMethod method;

        private EmptyMethodVisitor(NativeMethod method) {
            this.method = method;
        }

        @Override
        public void visitInsn(int i) {
            method.incInstructions("visitInsn(" + i + ")");
        }

        @Override
        public void visitIntInsn(int i, int i2) {
            method.incInstructions("visitIntInsn(" + i + ", " + i2 + ")");
        }

        @Override
        public void visitVarInsn(int i, int i2) {
            method.incInstructions("visitVarInsn(" + i + ", " + i2 + ")");
        }

        @Override
        public void visitTypeInsn(int i, String s) {
            method.incInstructions("visitTypeInsn(" + i + ", " + s + ")");
        }

        @Override
        public void visitFieldInsn(int i, String s, String s2, String s3) {
            method.incInstructions("visitFieldInsn(" + i + ", " + s + ", " + s2 + ", " + s3 + ")");
        }

        @Override
        public void visitMethodInsn(int i, String s, String s2, String s3) {
            method.incInstructions("visitMethodInsn(" + i + ", " + s + ", " + s2 + ", " + s3 + ")");
        }

        @Override
        public void visitJumpInsn(int i, Label label) {
            method.incInstructions("visitJumpInsn(" + i + ", " + label + ")");
        }

        @Override
        public void visitLabel(Label label) {
            method.incInstructions("visitLabel(" + label + ")");
        }

        @Override
        public void visitLdcInsn(Object o) {
            method.incInstructions("visitLdcInsn(" + o + ")");
        }

        @Override
        public void visitIincInsn(int i, int i2) {
            method.incInstructions("visitIincInsn(" + i + ", " + i2 + ")");
        }

        @Override
        public void visitTableSwitchInsn(int i, int i2, Label label, Label[] labels) {
            method.incInstructions("visitTableSwitchInsn(" + i + ", " + i2 + ", " + label + ", " + labels + ")");
        }

        @Override
        public void visitLookupSwitchInsn(Label label, int[] ints, Label[] labels) {
            method.incInstructions("visitLookupSwitchInsn(" + label + ", " + ints + ", " + labels + ")");
        }

        @Override
        public void visitMultiANewArrayInsn(String s, int i) {
            method.incInstructions("visitMultiANewArrayInsn(" + s + ", " + i + ")");
        }

        @Override
        public void visitTryCatchBlock(Label label, Label label2, Label label3, String s) {
            method.incInstructions("visitTryCatchBlock(" + label + ", " + label2 + ", " + label3 + ", " + s + ")");
        }

        @Override
        public void visitMaxs(int i, int i2) {
            method.incInstructions("visitMaxs(" + i + ", " + i2 + ")");
        }

        @Override
        public void visitLocalVariable(String s, String s2, Label label, Label label2, int i) {
            method
                .incInstructions("visitLocalVariable(" + s + ", " + s2 + ", " + label + ", " + label2 + ", " + i + ")");
        }

        @Override
        public void visitLineNumber(int i, Label label) {
            method.incInstructions("visitLineNumber(" + i + ", " + label + ")");
        }

        @Override
        public void visitAttribute(Attribute attribute) {
            method.incInstructions("visitAttribute(" + attribute + ")");
        }
    }
}

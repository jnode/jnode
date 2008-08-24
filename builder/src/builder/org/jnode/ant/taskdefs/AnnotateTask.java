/*
 * $Id: HeaderTask.java 3379 2007-08-04 10:19:57Z lsantha $
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

package org.jnode.ant.taskdefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.BuildException;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.SharedStatics;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.attrs.Annotation;
import org.objectweb.asm.attrs.Attributes;
import org.objectweb.asm.attrs.RuntimeVisibleAnnotations;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * That ant task will add some annotations to some compiled classes
 * mentioned in a property file.
 * For now, it's only necessary to add annotations to some
 * openjdk classes to avoid modifying the original source code.
 *
 * @author Fabien DUMINY (fduminy at jnode dot org)
 */
public class AnnotateTask extends FileSetTask {
    private static final String SHAREDSTATICS_TYPE_DESC = Type.getDescriptor(SharedStatics.class);
    private static final String MAGICPERMISSION_TYPE_DESC = Type.getDescriptor(MagicPermission.class);

    private File annotationFile;
    private String[] classesFiles;

    private String buildStartTime = "";
    private String pattern = "";
    private long startTime = 0;
    private String baseDir;

    private Properties annotations = new Properties();

    protected void doExecute() throws BuildException {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            startTime = format.parse(buildStartTime).getTime();
        } catch (Exception e) {
            throw new BuildException("invalid buildStartTime or pattern", e);
        }

        try {
            if (readProperties()) {
                for (String file : classesFiles) {
                    File classFile = new File(baseDir, file);
                    processFile(classFile);
                }
            }
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        }
    }

    /**
     * Defines the annotation property file where are specified annotations to add
     *
     * @param annotationFile
     */
    public final void setAnnotationFile(File annotationFile) {
        this.annotationFile = annotationFile;
    }

    /**
     * Define the time at which build started
     *
     * @param annotationFile
     */
    public final void setBuildStartTime(String buildStartTime) {
        this.buildStartTime = buildStartTime;
    }

    /**
     * Define the pattern with which buildStartTime is defined
     *
     * @param annotationFile
     */
    public final void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Read the properties file. For now, it simply contains a list of
     * classes that need the SharedStatics annotation.
     *
     * @return
     * @throws BuildException
     */
    private boolean readProperties() throws BuildException {
        readProperties("annotationFile", annotationFile, annotations);
        if (annotations.isEmpty()) {
            System.err.println("WARNING: annotationFile is empty (or doesn't exist)");
            return false;
        }

        classesFiles = (String[]) annotations.keySet().toArray(new String[annotations.size()]);

        // we must sort the classes in reverse order so that
        // classes with longest package name will be used first
        // (that is only necessary for classes whose name is the same
        // but package is different ; typical such class name : "Constants")
        Arrays.sort(classesFiles, Collections.reverseOrder());

        return true;
    }

    /**
     * Generic method that read properties from a given file.
     *
     * @param name
     * @param file
     * @param properties
     * @throws BuildException
     */
    private void readProperties(String name, File file, Properties properties) throws BuildException {
        if (file == null) {
            throw new BuildException(name + " is mandatory");
        }

        if (!file.exists()) {
            return;
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            properties.load(fis);
        } catch (IOException e) {
            throw new BuildException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new BuildException(e);
                }
            }
        }
    }

    /**
     * Get the list of annotations for the given class file.
     *
     * @param classFile list of annotations with ',' as separator. null if no annotation for that class.
     * @return
     */
    private String getAnnotations(File classFile) {
        String annotations = null;
        String classFilePath = classFile.getAbsolutePath();
        for (String f : classesFiles) {
            if (classFilePath.endsWith(f)) {
                annotations = this.annotations.getProperty(f);
                break;
            }
        }

        return annotations;
    }

    /**
     * Actually process a class file (called from parent class)
     */
    @Override
    protected void processFile(File classFile) throws IOException {
        if (classFile.lastModified() < startTime) {
            return;
        }

        String annotations = getAnnotations(classFile);
        if (annotations == null) {
            return;
        }

        File tmpFile = new File(classFile.getParentFile(), classFile.getName() + ".tmp");
        FileInputStream fis = null;
        boolean classIsModified = false;

        try {
            fis = new FileInputStream(classFile);
            classIsModified = addAnnotation(classFile, fis, tmpFile, annotations);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

        if (classIsModified) {
            if (trace) {
                traceClass(classFile, "before");

                traceClass(tmpFile, "after");
            }

            if (!classFile.delete()) {
                throw new IOException("can't delete " + classFile.getAbsolutePath());
            }

            if (!tmpFile.renameTo(classFile)) {
                throw new IOException("can't rename " + tmpFile.getAbsolutePath());
            }
        }
    }

    /**
     * Simple debug method that trace a class file.
     * It can be used to visually check that the annotations has been
     * properly added
     *
     * @param file
     * @throws IOException
     */
    private void traceClass(File file, String message) throws IOException {
        System.out.println("===== (" + message + ") trace for " + file.getAbsolutePath() + " =====");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);

            ClassReader cr = new ClassReader(fis);
            TraceClassVisitor tcv = new TraceClassVisitor(null, new PrintWriter(System.out));
            cr.accept(tcv, Attributes.getDefaultAttributes(), true);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        System.out.println("----- end trace -----");
    }

    /**
     * Add an annotation to a class file
     *
     * @param classFile
     * @param inputClass
     * @param tmpFile
     * @param annotations
     * @return
     * @throws BuildException
     */
    private boolean addAnnotation(File classFile, InputStream inputClass, File tmpFile, String annotations)
        throws BuildException {
        boolean classIsModified = false;
        FileOutputStream outputClass = null;

        ClassWriter cw = new ClassWriter(false);
        try {
            ClassReader cr = new ClassReader(inputClass);

            List<String> annotationTypeDescs = new ArrayList<String>(2);
            if (annotations.contains("SharedStatics")) {
                annotationTypeDescs.add(SHAREDSTATICS_TYPE_DESC);
            }
            if (annotations.contains("MagicPermission")) {
                annotationTypeDescs.add(MAGICPERMISSION_TYPE_DESC);
            }

            MarkerClassVisitor mcv = new MarkerClassVisitor(cw, annotationTypeDescs);
            cr.accept(mcv, Attributes.getDefaultAttributes(), true);

            if (mcv.classIsModified()) {
                System.out.println("adding annotations " + annotations + " to file " + classFile.getName());
                classIsModified = true;

                outputClass = new FileOutputStream(tmpFile);

                byte[] b = cw.toByteArray();
                outputClass.write(b);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new BuildException("Unable to add annotations to file " + classFile.getName(), ex);
        } finally {
            if (outputClass != null) {
                try {
                    outputClass.close();
                } catch (IOException e) {
                    System.err.println("Can't close stream for file " + classFile.getName());
                }

                long timestamp = classFile.lastModified();
                tmpFile.setLastModified(timestamp);
            }
        }

        return classIsModified;
    }

    /**
     * Visitor for a class file that actually do the job of adding annotations in the class.
     *
     * @author fabien
     */
    private static class MarkerClassVisitor extends ClassAdapter {
        private final List<String> annotationTypeDescs;

        private boolean classIsModified = false;

        public MarkerClassVisitor(ClassVisitor cv, List<String> annotationTypeDescs) {
            super(cv);

            this.annotationTypeDescs = annotationTypeDescs;
        }

        @Override
        public void visit(int version, int access, String name,
                          String superName, String[] interfaces, String sourceFile) {
            super.visit(org.objectweb.asm.Constants.V1_5, access,
                name, superName, interfaces, sourceFile);
        }

        @Override
        public void visitAttribute(Attribute attr) {
            if (attr instanceof RuntimeVisibleAnnotations) {
                RuntimeVisibleAnnotations rva = (RuntimeVisibleAnnotations) attr;
                for (Object annotation : rva.annotations) {
                    if (annotation instanceof Annotation) {
                        Annotation ann = (Annotation) annotation;
                        for (String annTypeDesc : annotationTypeDescs) {
                            if (ann.type.equals(annTypeDesc)) {
                                // we have found one of the annotations -> we won't need to add it again !
                                annotationTypeDescs.remove(annTypeDesc);
                                break;
                            }
                        }
                    }
                }
            }

            super.visitAttribute(attr);
        }

        @SuppressWarnings("unchecked")
        public void visitEnd() {
            if (!annotationTypeDescs.isEmpty()) {
                // we have not found the annotation -> we will add it and so modify the class
                classIsModified = true;
                RuntimeVisibleAnnotations attr = new RuntimeVisibleAnnotations();

                for (String annTypeDesc : annotationTypeDescs) {

                    Annotation ann = new Annotation(annTypeDesc);
                    ann.add("name", "");

                    attr.annotations.add(ann);
                }

                cv.visitAttribute(attr);
            }

            super.visitEnd();
        }

        public boolean classIsModified() {
            return classIsModified;
        }
    }
}

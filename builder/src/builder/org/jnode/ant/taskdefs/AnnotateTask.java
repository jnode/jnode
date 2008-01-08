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
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import java.util.StringTokenizer;

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
 * For now, it's only necessary to add SharedStatics annotations to some 
 * openjdk classes to avoid modifying the original source code.
 *   
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
public class AnnotateTask extends FileSetTask {
	private static final String SHAREDSTATICS_TYPE_DESC = Type.getDescriptor(SharedStatics.class);
	private static final String MAGICPERMISSION_TYPE_DESC = Type.getDescriptor(MagicPermission.class);

	private File annotationFile;
	private String[] classesFiles;
	private Properties properties;

	protected void doExecute() throws BuildException {
		if(readProperties(annotationFile))
		{
			processFiles();
		}
	}

	public final File getAnnotationFile() {
		return annotationFile;
	}

	public final void setAnnotationFile(File annotationFile) {
		this.annotationFile = annotationFile;
	}

	/**
	 * Read the properties file. For now, it simply contains a list of
	 * classes that need the SharedStatics annotation.
	 *
	 * @return
	 * @throws BuildException
	 */
	private boolean readProperties(File file) throws BuildException
	{
		if(file == null)
		{
			throw new BuildException("annotationFile is mandatory");
		}

		properties = new Properties();
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(file);
			properties.load(fis);
		} catch (IOException e) {
			throw new BuildException(e);
		}
		finally
		{
			if(fis != null)
			{
				try {
					fis.close();
				} catch (IOException e) {
					throw new BuildException(e);
				}
			}
		}
		if(properties.isEmpty())
		{
			System.err.println("WARNING: annotationFile is empty");
			return false;
		}

		classesFiles = (String[]) properties.keySet().toArray(new String[properties.size()]);

		// we must sort the classes in reverse order so that
		// classes with longest package name will be used first
		// (that is only necessary for classes whose name is the same
		// but package is different ; typical such class name : "Constants")
		Arrays.sort(classesFiles, Collections.reverseOrder());

		return true;
	}

	private String getAnnotations(File file)
	{
		String annotations = null;
		String filePath = file.getAbsolutePath();
		for(String f : classesFiles)
		{
			if(filePath.endsWith(f))
			{
				annotations = properties.getProperty(f);
				break;
			}
		}

		return annotations;
	}

	@Override
	protected void processFile(File file) throws IOException {
		String annotations = getAnnotations(file);
		if(annotations == null)
		{
			return;
		}

		File tmpFile = new File(file.getParentFile(), file.getName()+".tmp");
		FileInputStream fis = null;
		boolean classIsModified = false;

		try
		{
			fis = new FileInputStream(file);
			classIsModified = addAnnotation(file.getName(), fis, tmpFile, annotations);
		}
		finally
		{
			if(fis != null)
			{
				fis.close();
			}
		}

		if(classIsModified)
		{
			if(trace)
			{
				traceClass(file, "before");

				traceClass(tmpFile, "after");
			}

			if(!file.delete())
			{
				throw new IOException("can't delete "+file.getAbsolutePath());
			}

			if(!tmpFile.renameTo(file))
			{
				throw new IOException("can't rename "+tmpFile.getAbsolutePath());
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
	private void traceClass(File file, String message) throws IOException
	{
		System.out.println("===== ("+message+") trace for "+file.getAbsolutePath()+" =====");
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(file);

			ClassReader cr = new ClassReader(fis);
			TraceClassVisitor tcv = new TraceClassVisitor(null, new PrintWriter(System.out));
			cr.accept(tcv, Attributes.getDefaultAttributes(), true);
		}
		finally
		{
			if(fis != null)
			{
				fis.close();
			}
		}
		System.out.println("----- end trace -----");
	}

	private boolean addAnnotation(String fileName, InputStream inputClass, File tmpFile, String annotations) throws BuildException {
		boolean classIsModified = false;
		FileOutputStream outputClass = null;

		ClassWriter cw = new ClassWriter(false);
		try {
			ClassReader cr = new ClassReader(inputClass);

			List<String> annotationTypeDescs = new ArrayList<String>(2);
			if(annotations.contains("SharedStatics"))
			{
				annotationTypeDescs.add(SHAREDSTATICS_TYPE_DESC);
			}
			if(annotations.contains("MagicPermission"))
			{
				annotationTypeDescs.add(MAGICPERMISSION_TYPE_DESC);
			}

			MarkerClassVisitor mcv = new MarkerClassVisitor(cw, annotationTypeDescs);
			cr.accept(mcv, Attributes.getDefaultAttributes(), true);

			if(mcv.classIsModified())
			{
				System.out.println("adding annotations "+annotations+" to file "+fileName);
				classIsModified = true;

				outputClass = new FileOutputStream(tmpFile);

				byte[] b = cw.toByteArray();
				outputClass.write(b);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new BuildException("Unable to add annotations to file "+fileName, ex);
		}
		finally
		{
			if(outputClass != null)
			{
				try {
					outputClass.close();
				} catch (IOException e) {
					System.err.println("Can't close stream for file "+fileName);
				}
			}
		}

		return classIsModified;
	}

	private static class MarkerClassVisitor extends ClassAdapter {
		final private List<String> annotationTypeDescs;

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
			if(attr instanceof RuntimeVisibleAnnotations)
			{
				RuntimeVisibleAnnotations rva = (RuntimeVisibleAnnotations) attr;
				for(Object annotation : rva.annotations)
				{
					if(annotation instanceof Annotation)
					{
						Annotation ann = (Annotation) annotation;
						for(String annTypeDesc : annotationTypeDescs)
						{
							if(ann.type.equals(annTypeDesc))
							{
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
			if(!annotationTypeDescs.isEmpty())
			{
				// we have not found the annotation -> we will add it and so modify the class
				classIsModified = true;
				RuntimeVisibleAnnotations attr = new RuntimeVisibleAnnotations();

				for(String annTypeDesc : annotationTypeDescs)
				{

					Annotation ann = new Annotation(annTypeDesc);
					ann.add("name", "");

					attr.annotations.add(ann);
				}

				cv.visitAttribute(attr);
			}

			super.visitEnd();
		}

		public boolean classIsModified()
		{
			return classIsModified;
		}
	}
}

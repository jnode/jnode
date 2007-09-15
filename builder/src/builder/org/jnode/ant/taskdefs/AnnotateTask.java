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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.jnode.vm.annotation.SharedStatics;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.attrs.Annotation;
import org.objectweb.asm.attrs.Attributes;
import org.objectweb.asm.attrs.RuntimeVisibleAnnotations;
//import org.objectweb.asm.util.AbstractVisitor;
//import org.objectweb.asm.util.CheckClassAdapter;
//import org.objectweb.asm.util.TraceClassVisitor;
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

	private File annotationFile;
	private String[] classesFiles;
	
	protected void doExecute() throws BuildException {
		classesFiles = readProperties(annotationFile);		
		processFiles();
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
	private static String[] readProperties(File file) throws BuildException
	{
		if(file == null)
		{
			throw new BuildException("annotationFile is mandatory");
		}
		
		Properties props = new Properties();
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(file);
			props.load(fis);			
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
		if(props.isEmpty())
		{
			System.err.println("WARNING: annotationFile is empty");
			return new String[0];
		}
		
		String[] classesFiles = new String[props.size()];
		int i = 0;
		for(Object name : props.keySet())
		{
			String className = String.valueOf(name);
			String annotations = props.getProperty(className);
			if("SharedStatics".equals(annotations))
			{
				className = className.replace('.', File.separatorChar);
				className += ".class";
				classesFiles[i++] = className;
			}
		}
		
		// we must sort the classes in reverse order so that 
		// classes with longest package name will be used first
		// (that is only necessary for classes whose name is the same
		// but package is different ; typical such class name : "Constants")
		Arrays.sort(classesFiles, Collections.reverseOrder());
		
		return classesFiles;
	}
	
	private static boolean hasFile(String[] files, File file)
	{
		boolean found = false;
		String filePath = file.getAbsolutePath();
		for(String f : files)
		{
			if(filePath.endsWith(f))
			{
				found = true;
				break;
			}
		}
		
		return found;
	}
	
	@Override
	protected void processFile(File file) throws IOException {
		if(!hasFile(classesFiles, file))
		{
			return;
		}
		
		System.out.println("adding annotation to file "+file.getAbsolutePath());
		File tmpFile = new File(file.getParentFile(), file.getName()+".tmp");
		FileInputStream fis = null;
		FileOutputStream fos = null;
		
		try
		{
			if(trace)
			{
				traceClass(file, "before");
			}
			
			fis = new FileInputStream(file);
			fos = new FileOutputStream(tmpFile);
			addAnnotation(file.getName(), fis, fos);
			
			if(trace)
			{
				traceClass(file, "after");
			}			
		}
		finally
		{
			if(fis != null)
			{
				fis.close();
			}
			if(fos != null)
			{
				fos.close();
			}
		}
		
		if(!file.delete())
		{
			throw new IOException("can't delete "+file.getAbsolutePath());
		}
		
		if(!tmpFile.renameTo(file))
		{
			throw new IOException("can't rename "+tmpFile.getAbsolutePath());
		}
		
		//traceClass(file);
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

	private void addAnnotation(String fileName, InputStream inputClass, OutputStream outputClass) throws BuildException {
		ClassWriter cw = new ClassWriter(false);
		try {
			ClassReader cr = new ClassReader(inputClass);
			cr.accept(new MarkerClassVisitor(cw), 
					Attributes.getDefaultAttributes(), 
					true);			
			byte[] b = cw.toByteArray();
			
			outputClass.write(b);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new BuildException("Unable to load class in file "+fileName, ex);
		}
	}

	private static class MarkerClassVisitor extends ClassAdapter {

		public MarkerClassVisitor(ClassVisitor cv) {
			super(cv);
		}

		@Override
		public void visit(int version, int access, String name,
				String superName, String[] interfaces, String sourceFile) {
			super.visit(org.objectweb.asm.Constants.V1_5, access, 
					name, superName, interfaces, sourceFile);
		}

		@SuppressWarnings("unchecked")
		public void visitEnd() {
			String t = Type.getDescriptor(SharedStatics.class);
			Annotation ann = new Annotation(t);
			ann.add("name", "");

			RuntimeVisibleAnnotations attr = new RuntimeVisibleAnnotations();
			attr.annotations.add(ann);
			cv.visitAttribute(attr);

			super.visitEnd();
		}
	}
}

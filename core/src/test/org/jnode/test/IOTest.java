/*
 * $Id$
 */
package org.jnode.test;

import java.io.File;

/**
 * @author epr
 */
public class IOTest {
	
	public static void main(String[] args) {
		File[] roots = File.listRoots();
		
		System.out.println("FS Roots:");
		for (int i = 0; i < roots.length; i++) {
			System.out.println("[" + i + "]: " + roots[i]);
			printDir(roots[i], 1);
		}
	}
	
	public static void printDir(File dir, int level) {
		final File[] list = dir.listFiles();
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				final File f = list[i];
				if (f.isDirectory()) {
					System.out.println(tabs(level) + "[" + f.getName() + "]");
					printDir(f, level+1);
				} else {
					System.out.println(tabs(level) + f.getName() + " " + f.length());
				}
			}
			System.out.println(tabs(level) + " -- total of " + list.length + " files --");
		} else {
			System.out.println("list == null in (" + dir + ").list");
		}
	}
	
	private static String tabs(int level) {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < level; i++) {
			b.append("  ");
		}
		return b.toString();
	}

}
 
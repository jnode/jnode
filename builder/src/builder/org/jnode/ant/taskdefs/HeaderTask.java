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
 
package org.jnode.ant.taskdefs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class HeaderTask extends Task {

	private final ArrayList<FileSet> fileSets = new ArrayList<FileSet>();

	private File headerFile;
	
	private boolean update = false;
	public void addFileSet(FileSet fs) {
		fileSets.add(fs);
	}
	
	private boolean compareHeader(String[] lines, String[] header) {
		final int linesCnt = lines.length;
		final int hdrCnt = header.length;
		
		for (int i = 0; i < linesCnt; i++) {
			final String line = lines[i];
			if (i < hdrCnt) {
				if (!compareLine(line, header[i])) {
					return false;
				}
			} else {
				String trimmedLine = line.trim();
				if (trimmedLine.startsWith("package")) {
					return true;
				} 
				if (trimmedLine.length() > 0) {
					return false;
				}
			}			
		}
		
		return false;
	}
	
	private boolean compareLine(String line, String hdrLine) {
		if ((line.indexOf('$') >= 0) && (hdrLine.indexOf('$') >= 0)) {
			return true;
		}
		return line.trim().equals(hdrLine.trim());
	}

	public void execute() throws BuildException {
		if (headerFile == null) {
			throw new BuildException("HeaderFile must be set");
		}
		try {
			final String[] header = readFile(headerFile);

			for (FileSet fs : fileSets) {
				final String[] files = fs.getDirectoryScanner(getProject())
						.getIncludedFiles();
				final int fileCount = files.length;
				for (int j = 0; j < fileCount; j++) {
					final String fname = files[j];
					processFile(new File(fs.getDir(getProject()), fname), header);
				}

			}
		} catch (IOException ex) {
			throw new BuildException(ex);
		}
	}

	public final File getHeaderFile() {
		return headerFile;
	}

	public final boolean isUpdate() {
		return update;
	}

	private void processFile(File file, String[] header) throws IOException {
		final String[] inp = readFile(file);
		if (!compareHeader(inp, header)) {
			if (update) {
				log("Updating " + file);
				writeUpdateFile(file, inp, header);
			} else {
				log("Wrong header in " + file);
			}
		}
	}

	private void writeUpdateFile(File file, String[] lines, String[] header) throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(file));
		try {
			final int hdrCnt = header.length;
			for (int i = 0; i < hdrCnt; i++) {
				out.println(header[i]);
			}
			
			final int linesCnt = lines.length;
			boolean write = false;
			for (int i = 0; i < linesCnt; i++) {
				final String line = lines[i];
				String trimmedLine = line.trim();
				if (trimmedLine.startsWith("package")) {
					write = true;
				}
				if (write) {
					out.println(lines[i]);
				}
			}
		} finally {
			out.close();
		}
	}

	
	
	private String[] readFile(File file) throws IOException {
		final BufferedReader in = new BufferedReader(new FileReader(file));
		try {
			final ArrayList<String> lines = new ArrayList<String>();
			String line;
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
			return lines.toArray(new String[lines.size()]);
		} finally {
			in.close();
		}
	}

	public final void setHeaderFile(File headerFile) {
		this.headerFile = headerFile;
	}
	public final void setUpdate(boolean update) {
		this.update = update;
	}
}

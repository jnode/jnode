/*
 * $Id$
 */
package org.jnode.shell.help;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

/**
 * @author qades
 */
public class FileArgument extends Argument {

	public FileArgument(String name, String description, boolean multi) {
		super(name, description, multi);
	}

	public FileArgument(String name, String description) {
		super(name, description, SINGLE);
	}

	// here goes the command line completion

	public File getFile(ParsedArguments args) {
		String value = getValue(args);
		if( value == null )
			return null;
		return new File(value);
	}

	public InputStream getInputStream(ParsedArguments args) throws FileNotFoundException {
		String value = getValue(args);
		if( value == null )
			return null;
		return new FileInputStream(value);
	}

	public OutputStream getOutputStream(ParsedArguments args) throws FileNotFoundException {
		String value = getValue(args);
		if( value == null )
			return null;
		return new FileOutputStream(value);
	}
}

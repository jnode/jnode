/*
 * $Id$
 */
package org.jnode.fs.command;

import java.io.File;

import org.jnode.shell.help.FileArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;

/**
 * Delete a file or a empty directory
 * 
 * @author Guillaume BINET (gbin@users.sourceforge.net)
 */
public class DeleteCommand {

	static final FileArgument ARG_DIR = new FileArgument("file/dir", "delete the file or directory");
	public static Help.Info HELP_INFO =
		new Help.Info(
			"file/dir",
			"the file or directory to delete",
			new Parameter[] { new Parameter(ARG_DIR, Parameter.MANDATORY)});

	public static void main(String[] args) throws Exception {
		final ParsedArguments cmdLine = HELP_INFO.parse(args);
		final File entry = ARG_DIR.getFile(cmdLine);
	    boolean deleteOk=false;
		
		if (!entry.exists()) {
			System.err.println(entry + " does not exist");
		}
		// for this time, delete only empty directory (wait implementation of -r option)
		if(entry.isDirectory()){
			final File[] subFile=entry.listFiles();
			for(int i=0;i<subFile.length;i++){ 
					final String name=subFile[i].getName();
					if(!name.equals(".") && !name.equals("..")){
						System.err.println("Directory is not empty");
						return;
					}				
			}
		}
	
		deleteOk=entry.delete();
	
		if(!deleteOk){
			System.err.println(entry + " does not deleted");
		}
		
	}

}

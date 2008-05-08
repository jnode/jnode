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
 
package org.jnode.shell.command.test;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.CommandSyntaxException;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.StringArgument;
import org.jnode.test.framework.TestManager;

/**
 * This command runs JUnit tests registered with the JNode test framework.
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 * @author crawley@jnode.org
 */
public class SuiteCommand extends AbstractCommand {
    private final FlagArgument FLAG_LIST = 
        new FlagArgument("list", Argument.OPTIONAL, "list the tests");
    
    private final FlagArgument FLAG_RUN = 
        new FlagArgument("run", Argument.OPTIONAL, "run the tests");
    
    private final CategoryArgument ARG_CATEGORY =
        new CategoryArgument("category", Argument.OPTIONAL | Argument.MULTIPLE,
                "test categories to run or list");
    
    public SuiteCommand() {
        super("Run one or more JUnit testcase(s)");
        registerArguments(FLAG_LIST, FLAG_RUN, ARG_CATEGORY);
    }

    public static void main(String[] args) throws Exception {
        new SuiteCommand().execute(args);
    }

    /**
     * Execute this command
     */
    public void execute(CommandLine cmdLine, InputStream in,
            PrintStream out, PrintStream err) {
        TestManager mgr = TestManager.getInstance();
        if (FLAG_LIST.isSet()) {
    		for (Class<? extends Test> test : mgr.getTests()) {
    			out.print(test.getName() + " :");
    			for (String category : mgr.getCategories(test)) {
    				out.print(" ");
    				out.print(category);
    			}
    			out.println();
    		}
    	}
    	else if (FLAG_RUN.isSet()) {
        	String[] categories = ARG_CATEGORY.getValues();
        	if (categories == null) {
        		categories = new String[0];
        	}
        	TestSuite suite = mgr.getTestSuite(Arrays.asList(categories));
        	junit.textui.TestRunner.run(suite);        	
    	}
    }
    
    /**
     * Validate and complete test categories against the categories known to
     * the TestManager.
     */
    private static class CategoryArgument extends StringArgument {

        public CategoryArgument(String label, int flags, String description) {
            super(label, flags, description);        
        }

        public void complete(CompletionInfo completion, String partial) {
            Set<String> availCategories = TestManager.getInstance().getCategories();
        	for (String availCategory : availCategories) {
        		if (availCategory.startsWith(partial)) {
        			completion.addCompletion(availCategory);
        		}
        	}
        }
        
        protected String doAccept(String category) throws CommandSyntaxException {
        	Set<String> availCategories = TestManager.getInstance().getCategories();
        	if (availCategories.contains(category)) {
        	    return category;
        	}
        	else {
        	    throw new CommandSyntaxException("not a recognized JUnit test category");
        	}
        }    
    }
}

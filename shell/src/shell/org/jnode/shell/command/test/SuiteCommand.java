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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.OptionArgument;
import org.jnode.test.framework.TestManager;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class SuiteCommand {
	static private final OptionArgument.Option OPT_LIST = new OptionArgument.Option("list", "list the tests");
	static private final OptionArgument.Option OPT_RUN = new OptionArgument.Option("run", "run the tests for given categories"); 
	static final OptionArgument ARG_ACTION = new OptionArgument(
			"action", "action to do", OPT_LIST, OPT_RUN);
	
    static final CategoryArgument ARG_CATEGORY = new CategoryArgument(
            "category", "a category of test to run", CategoryArgument.MULTI);

	static final Parameter PARAM_ACTION = new Parameter(ARG_ACTION);

	static final Parameter PARAM_CATEGORY = new Parameter(ARG_CATEGORY, Parameter.OPTIONAL);

    public static Help.Info HELP_INFO = new Help.Info("suite",
            "Run one or more JUnit testcase(s)", 
           new Parameter[] 
           { PARAM_ACTION, PARAM_CATEGORY});

    public static void main(String[] args) throws Exception {
        new SuiteCommand().execute(HELP_INFO.parse(args), System.in, System.out,
                System.err);
    }

    /**
     * Execute this command
     */
    public void execute(ParsedArguments cmdLine, InputStream in,
            PrintStream out, PrintStream err) throws Exception {

    	String action = ARG_ACTION.getValue(cmdLine);
    	if(OPT_LIST.getName().equals(action))
    	{
    		TestManager mgr = TestManager.getInstance();
    		for(Class<? extends Test> test : mgr.getTests())
    		{
    			out.print(test.getName()+" :");
    			for(String category : mgr.getCategories(test))
    			{
    				out.print(" ");
    				out.print(category);
    			}
    			out.println();
    		}
    	}
    	else if(OPT_RUN.getName().equals(action))
    	{
        	String[] categories = ARG_CATEGORY.getValues(cmdLine);
        	if (categories == null) {
        		categories = new String[0];
        	}
        	TestSuite suite = TestManager.getInstance().getTestSuite(Arrays.asList(categories));
        	junit.textui.TestRunner.run(suite);        	
    	}
    	else
    	{
    		HELP_INFO.help(null);
    	}
    }
    
    private static class CategoryArgument extends Argument {

        public CategoryArgument(String name, String description, boolean multi) {
            super(name, description, multi);
        }

        public CategoryArgument(String name, String description) {
            super(name, description);
        }

        public void complete(CompletionInfo completion, String partial) {
            Set<String> availCategories = TestManager.getInstance().getCategories();
        	for (String availCategory : availCategories) {
        		if (availCategory.startsWith(partial)) {
        			completion.addCompletion(availCategory);
        		}
        	}
        }
        
        protected boolean isValidValue(String category) {
        	if((category == null) || "".equals(category))
        		return true;
        	
        	Set<String> availCategories = TestManager.getInstance().getCategories();
        	return availCategories.contains(category);
        }    
    }
}

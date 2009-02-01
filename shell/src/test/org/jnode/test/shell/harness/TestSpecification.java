/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.test.shell.harness;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This represents a command test specification for a command or script.
 * 
 * @author crawley@jnode
 */
public class TestSpecification {
    
    public static enum RunMode {
        AS_SCRIPT,
        AS_CLASS,
        AS_ALIAS
    }
    
    private final RunMode runMode;
    private final String command;
    private final List<String> args = new ArrayList<String>();
    private final String scriptContent;
    private final String inputContent;
    private final String outputContent;
    private final String errorContent;
    private final String title;
    private final List<PluginSpecification> plugins = new ArrayList<PluginSpecification>();
    private final int rc;
    private final Map<File, String> fileMap = new HashMap<File, String>();
    private TestSetSpecification testSet;
    
    public TestSpecification(RunMode runMode, String command, String scriptContent2,
            String inputContent2, String outputContent2, String errorContent2,
            String title, int rc) {
        super();
        this.runMode = runMode;
        this.command = command;
        this.scriptContent = scriptContent2;
        this.inputContent = inputContent2;
        this.outputContent = outputContent2;
        this.errorContent = errorContent2;
        this.title = title;
        this.rc = rc;
    }

    public String getOutputContent() {
        return outputContent;
    }

    public String getErrorContent() {
        return errorContent;
    }

    public int getRc() {
        return rc;
    }
    
    public void addArg(String arg) {
        args.add(arg);
    }

    public void addPlugin(PluginSpecification plugin) {
        plugins.add(plugin);
    }

    public void addFile(File file, String content) {
        fileMap.put(file, content);
    }

    public Map<File, String> getFileMap() {
        return fileMap;
    }

    public RunMode getRunMode() {
        return runMode;
    }

    public String getCommand() {
        return command;
    }

    public List<String> getArgs() {
        return args;
    }
    
    public String getScriptContent() {
        return scriptContent;
    }

    public String getInputContent() {
        return inputContent;
    }

    public String getTitle() {
        return title;
    }

    public List<PluginSpecification> getPlugins() {
        return plugins;
    }
    
    public TestSetSpecification getTestSet() {
        return testSet;
    }

    public void setTestSet(TestSetSpecification testSet) {
        this.testSet = testSet;
    }
}

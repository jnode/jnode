/*
 * $Id$
 *
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
import java.util.List;

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
    
    public static class FileSpecification {
        private final File file;
        private final boolean input;
        private final String fileContent;
        
        public FileSpecification(File file, boolean input,
                String fileContent) {
            this.file = file;
            this.input = input;
            this.fileContent = fileContent;
        }

        public File getFile() {
            return file;
        }

        public boolean isInput() {
            return input;
        }

        public String getFileContent() {
            return fileContent;
        }
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
    private final List<FileSpecification> files = new ArrayList<FileSpecification>();
    private TestSetSpecification testSet;
    
    public TestSpecification(RunMode runMode, String command, String scriptContent,
            String inputContent, String outputContent, String errorContent,
            String title, int rc) {
        super();
        this.runMode = runMode;
        this.command = command;
        this.scriptContent = scriptContent;
        this.inputContent = inputContent;
        this.outputContent = outputContent;
        this.errorContent = errorContent;
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

    public void addFile(FileSpecification file) {
        files.add(file);
    }

    public List<FileSpecification> getFiles() {
        return files;
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

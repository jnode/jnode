package org.jnode.test.shell.harness;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * This represents a command test specification for a command or script.
 * 
 * @author crawley@jnode
 */
public class TestSpecification {
    
    public static class PluginSpec {
        public final String pluginId;
        public final String pluginVersion;
        public final String pseudoPluginClassName;
        
        public PluginSpec(String pluginId, String pluginVersion,
                String pseudoPluginClassName) {
            super();
            this.pluginId = pluginId;
            this.pluginVersion = pluginVersion;
            this.pseudoPluginClassName = pseudoPluginClassName;
        }
    }
    
    public static enum RunMode {
        AS_SCRIPT,
        AS_CLASS,
        AS_ALIAS
    }
    
    private final RunMode runMode;
    private final String command;
    private final List<String> args;
    private final String scriptContent;
    private final String inputContent;
    private final String outputContent;
    private final String errorContent;
    private final String title;
    private final List<PluginSpec> requiredPlugins;
    private final int rc;
    private final Map<File, String> fileMap;
    
    public TestSpecification(RunMode runMode, String command, String scriptContent,
            String inputContent, String outputContent, String errorContent,
            String title, int rc, List<String> args, Map<File, String> fileMap,
            List<PluginSpec> requiredPlugins) {
        super();
        this.runMode = runMode;
        this.command = command;
        this.scriptContent = scriptContent;
        this.inputContent = inputContent;
        this.outputContent = outputContent;
        this.errorContent = errorContent;
        this.title = title;
        this.rc = rc;
        this.args = args;
        this.fileMap = fileMap;
        this.requiredPlugins = requiredPlugins;
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

    public List<PluginSpec> getRequiredPlugins() {
        return requiredPlugins;
    }
}

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
    
    public static enum RunMode {
        AS_SCRIPT,
        AS_CLASS,
        AS_ALIAS
    }
    
    private final RunMode runMode;
    private final String command;
    private final List<String> args;
    private final String inputContent;
    private final String outputContent;
    private final String errorContent;
    private final String title;
    private final int rc;
    private final Map<File, String> fileMap;
    
    public TestSpecification(RunMode runMode, String command,
            String inputContent, String outputContent, String errorContent,
            String title, int rc, List<String> args, Map<File, String> fileMap) {
        super();
        this.runMode = runMode;
        this.command = command;
        this.inputContent = inputContent;
        this.outputContent = outputContent;
        this.errorContent = errorContent;
        this.title = title;
        this.rc = rc;
        this.args = args;
        this.fileMap = fileMap;
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

    public String getInputContent() {
        return inputContent;
    }

    public String getTitle() {
        return title;
    }
}

package org.jnode.test.shell.harness;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

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
    
    private RunMode runMode;
    private String command;
    private List<String> args = new ArrayList<String>();
    private String inputContent;
    private String outputContent;
    private String errorContent;
    private String title;
    private int rc;
    private Map<File, String> fileMap = new HashMap<File, String>();
    
    private IXMLElement root;
    
    public TestSpecification() {
        super();
    }

    public TestSpecification load(InputStream in) throws Exception {
        StdXMLReader xr = new StdXMLReader(in);
        IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
        parser.setReader(xr);
        root = (IXMLElement) parser.parse();
        if (!root.getName().equals("testSpec")) {
            throw new TestSpecificationException(
                    "The root element should be 'testSpec' not '" + root.getName() + "'");
        }
        runMode = RunMode.valueOf(extractElementValue("runMode", "AS_CLASS"));
        title = extractElementValue("title");
        command = extractElementValue("command");
        inputContent = extractElementValue("input", "");
        outputContent = extractElementValue("output", "");
        errorContent = extractElementValue("error", "");
        try {
            rc = Integer.parseInt(extractElementValue("error", "0").trim());
        } catch (NumberFormatException ex) {
            throw new TestSpecificationException("'rc' is not an integer");
        }
        IXMLElement elem = root.getFirstChildNamed("args");
        if (elem != null) {
            for (Object obj : elem.getChildren()) {
                if (obj instanceof IXMLElement) {
                    IXMLElement child = (IXMLElement) obj;
                    if (!child.getName().equals("arg")) {
                        throw new TestSpecificationException(
                                "Child elements of 'args' should be 'arg' not '" + root.getName() + "'");
                    }
                    args.add(child.getContent());
                }
            }
        }
        elem = root.getFirstChildNamed("files");
        if (elem != null) {
            for (Object obj : elem.getChildren()) {
                if (obj instanceof IXMLElement) {
                    IXMLElement child = (IXMLElement) obj;
                    if (!child.getName().equals("file")) {
                        throw new TestSpecificationException(
                                "Child elements of 'files' should be 'file' not '" + root.getName() + "'");
                    }
                    String fileName = extractElementValue(child, "name");
                    String content = extractElementValue(child, "content", "");
                    fileMap.put(new File(fileName), content);
                }
            }
        }
        return this;
    }

    private String extractElementValue(IXMLElement parent, String name) throws TestSpecificationException {
        IXMLElement elem = parent.getFirstChildNamed(name);
        if (elem == null) {
            throw new TestSpecificationException(
                    "Element '" + name + "' not found in '" + parent.getName() + "'");
        } else {
            return elem.getContent();
        }
    }

    private String extractElementValue(IXMLElement parent, String name, String dflt) {
        IXMLElement elem = parent.getFirstChildNamed(name);
        return elem == null ? dflt : elem.getContent();
    }

    private String extractElementValue(String name) throws TestSpecificationException {
        return extractElementValue(root, name);
    }

    private String extractElementValue(String name, String dflt) {
        return extractElementValue(root, name, dflt);
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

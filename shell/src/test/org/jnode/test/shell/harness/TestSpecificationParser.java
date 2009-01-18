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

import org.jnode.test.shell.harness.TestSpecification.RunMode;

public class TestSpecificationParser {

    public List<TestSpecification> parse(InputStream in) throws Exception {
        StdXMLReader xr = new StdXMLReader(in);
        IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
        parser.setReader(xr);
        List<TestSpecification> res = new ArrayList<TestSpecification>();
        IXMLElement root = (IXMLElement) parser.parse();
        if (root.getName().equals("testSpec")) {
            res.add(parseTestSpecification(root));
        } else if (root.getName().equals("testSpecs")) {
            for (Object obj : root.getChildren()) {
                if (obj instanceof IXMLElement) {
                    IXMLElement argChild = (IXMLElement) obj;
                    if (!argChild.getName().equals("testSpec")) {
                        throw new TestSpecificationException(
                                "Child elements of 'testSpecs' should be 'testSpec' not '" + 
                                argChild.getName() + "'");
                    }
                    res.add(parseTestSpecification(argChild));
                }
            }
        } else {
            throw new TestSpecificationException(
                    "The root element should be 'testSpec' not '" + root.getName() + "'");
        }
        return res;
    }
    
    private TestSpecification parseTestSpecification(IXMLElement elem) throws TestSpecificationException {
        
        RunMode runMode = RunMode.valueOf(extractElementValue(elem, "runMode", "AS_CLASS"));
        String title = extractElementValue(elem, "title");
        String command = extractElementValue(elem, "command");
        String inputContent = extractElementValue(elem, "input", "");
        String outputContent = extractElementValue(elem, "output", "");
        String errorContent = extractElementValue(elem, "error", "");
        int rc;
        try {
            rc = Integer.parseInt(extractElementValue(elem, "error", "0").trim());
        } catch (NumberFormatException ex) {
            throw new TestSpecificationException("'rc' is not an integer");
        }
        IXMLElement child = elem.getFirstChildNamed("args");
        List<String> args = new ArrayList<String>();
        if (child != null) {
            for (Object obj : child.getChildren()) {
                if (obj instanceof IXMLElement) {
                    IXMLElement argChild = (IXMLElement) obj;
                    if (!argChild.getName().equals("arg")) {
                        throw new TestSpecificationException(
                                "Child elements of 'args' should be 'arg' not '" + argChild.getName() + "'");
                    }
                    args.add(argChild.getContent());
                }
            }
        }
        child = elem.getFirstChildNamed("files");
        Map<File, String> fileMap = new HashMap<File, String>();
        if (child != null) {
            for (Object obj : child.getChildren()) {
                if (obj instanceof IXMLElement) {
                    IXMLElement fileChild = (IXMLElement) obj;
                    if (!fileChild.getName().equals("file")) {
                        throw new TestSpecificationException(
                                "Child elements of 'files' should be 'file' not '" + fileChild.getName() + "'");
                    }
                    String fileName = extractElementValue(fileChild, "name");
                    String content = extractElementValue(fileChild, "content", "");
                    fileMap.put(new File(fileName), content);
                }
            }
        }
        return new TestSpecification(
                runMode, command, inputContent, outputContent, errorContent,
                title, rc, args, fileMap);
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
}

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

import org.jnode.test.shell.harness.TestSpecification.PluginSpec;
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
        String scriptContent = extractElementValue(elem, "script", "");
        String inputContent = extractElementValue(elem, "input", "");
        String outputContent = extractElementValue(elem, "output", "");
        String errorContent = extractElementValue(elem, "error", "");
        int rc;
        try {
            rc = Integer.parseInt(extractElementValue(elem, "rc", "0").trim());
        } catch (NumberFormatException ex) {
            throw new TestSpecificationException("'rc' is not an integer");
        }
        List<String> args = parseArgs(elem.getFirstChildNamed("args"));
        Map<File, String> fileMap = parseFiles(elem.getFirstChildNamed("files"));
        List<PluginSpec> plugins = parsePlugins(elem.getFirstChildNamed("plugins"));
        return new TestSpecification(
                runMode, command, scriptContent, inputContent, outputContent, errorContent,
                title, rc, args, fileMap, plugins);
    }

    private List<PluginSpec> parsePlugins(IXMLElement pluginsElem) throws TestSpecificationException {
        List<PluginSpec> plugins = new ArrayList<PluginSpec>();
        if (pluginsElem != null) {
            for (Object obj : pluginsElem.getChildren()) {
                if (obj instanceof IXMLElement) {
                    IXMLElement child = (IXMLElement) obj;
                    if (!child.getName().equals("plugin")) {
                        throw new TestSpecificationException(
                                "Child elements of 'plugins' should be 'plugin' not '" + child.getName() + "'");
                    }
                    String pluginId = extractElementValue(child, "id");
                    String pluginVersion = extractElementValue(child, "version", "");
                    String pseudoPluginClassName = extractElementValue(child, "class");
                    plugins.add(new PluginSpec(pluginId, pluginVersion, pseudoPluginClassName));
                }
            }
        }
        return plugins;
    }

    private Map<File, String> parseFiles(IXMLElement filesElem) throws TestSpecificationException {
        Map<File, String> fileMap = new HashMap<File, String>();
        if (filesElem != null) {
            for (Object obj : filesElem.getChildren()) {
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
        return fileMap;
    }

    private List<String> parseArgs(IXMLElement argsElem) throws TestSpecificationException {
        List<String> args = new ArrayList<String>();
        if (argsElem != null) {
            for (Object obj : argsElem.getChildren()) {
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
        return args;
    }

    private String extractElementValue(IXMLElement parent, String name) throws TestSpecificationException {
        IXMLElement elem = parent.getFirstChildNamed(name);
        if (elem == null) {
            throw new TestSpecificationException(
                    "Element '" + name + "' not found in '" + parent.getName() + "'");
        } else {
            String res = elem.getContent();
            return (res == null) ? "" : res;
        }
    }

    private String extractElementValue(IXMLElement parent, String name, String dflt) {
        IXMLElement elem = parent.getFirstChildNamed(name);
        return elem == null ? dflt : elem.getContent();
    }
}

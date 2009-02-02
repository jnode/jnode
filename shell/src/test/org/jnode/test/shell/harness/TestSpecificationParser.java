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
import java.io.InputStream;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

import org.jnode.test.shell.harness.TestSpecification.RunMode;

public class TestSpecificationParser {

    public TestSetSpecification parse(InputStream in) throws Exception {
        StdXMLReader xr = new StdXMLReader(in);
        IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
        parser.setReader(xr);
        TestSetSpecification res;
        IXMLElement root = (IXMLElement) parser.parse();
        if (root.getName().equals("testSpec")) {
            res = new TestSetSpecification("");
            res.addTestSpec(parseTestSpecification(root));
        } else if (root.getName().equals("testSpecs")) {
            String title = extractElementValue(root, "title");
            res = new TestSetSpecification(title);
            for (Object obj : root.getChildren()) {
                if (obj instanceof IXMLElement) {
                    IXMLElement argChild = (IXMLElement) obj;
                    String name = argChild.getName();
                    if (name.equals("testSpec")) {
                        res.addTestSpec(parseTestSpecification(argChild));
                    } else if (name.equals("plugin")) {
                        res.addPluginSpec(parsePluginSpecification(argChild));
                    } 
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
        TestSpecification res = new TestSpecification(
                runMode, command, scriptContent, inputContent, outputContent, errorContent,
                title, rc);
        for (Object obj : elem.getChildren()) {
            if (obj instanceof IXMLElement) {
                IXMLElement child = (IXMLElement) obj;
                String name = child.getName();
                if (name.equals("arg")) {
                    res.addArg(child.getContent());
                } else if (name.equals("plugin")) {
                    res.addPlugin(parsePluginSpecification(child));
                } else if (name.equals("file")) {
                    parseFile(child, res);
                }
            }
        }
        return res;
    }

    private PluginSpecification parsePluginSpecification(IXMLElement elem) 
        throws TestSpecificationException {
        String pluginId = extractElementValue(elem, "id");
        String pluginVersion = extractElementValue(elem, "version", "");
        String pseudoPluginClassName = extractElementValue(elem, "class",
                "org.jnode.test.shell.harness.DummyPseudoPlugin");
        return new PluginSpecification(pluginId, pluginVersion, pseudoPluginClassName);
    }

    private void parseFile(IXMLElement elem, TestSpecification res) 
        throws TestSpecificationException {
        String fileName = extractElementValue(elem, "name");
        String content = extractElementValue(elem, "content", "");
        res.addFile(new File(fileName), content);
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

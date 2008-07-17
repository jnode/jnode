/*
 * $Id $
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
package org.jnode.configure;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLException;
import net.n3.nanoxml.XMLParserFactory;

import org.jnode.configure.PropertySet.Property;
import org.jnode.configure.PropertySet.Value;
import org.jnode.configure.Screen.Item;
import org.jnode.configure.adapter.FileAdapter;


/**
 * This class loads an XML configuration script and creates the in-memory
 * representation.
 * 
 * @author crawley@jnode.org
 */
public class ScriptParser {
    public static final String SCRIPT = "configureScript";
    public static final String BASE_DIR = "baseDir";
    public static final String INCLUDE = "include";
    public static final String TYPE = "type";
    public static final String CONTROL_PROPS = "controlProps";
    public static final String PROP_FILE = "propFile";
    public static final String SCRIPT_FILE = "scriptFile";
    public static final String DEFAULT_FILE = "defaultFile";
    public static final String TEMPLATE_FILE = "templateFile";
    public static final String FILE_FORMAT = "fileFormat";
    public static final String MARKER = "marker";
    public static final String DEFAULT_MARKER = "@";
    public static final String VALIDATION_CLASS = "validationClass";
    public static final String SCREEN = "screen";
    public static final String NAME = "name";
    public static final String PATTERN = "pattern";
    public static final String ALT = "alt";
    public static final String VALUE = "value";
    public static final String TOKEN = "token";
    public static final String PROPERTY = "property";
    public static final String PROMPT = "prompt";
    public static final String DESCRIPTION = "description";
    public static final String DEFAULT = "default";
    public static final String TITLE = "title";
    public static final String ITEM = "item";
    public static final String GUARD_PROP = "guardProp";
    public static final String VALUE_IS = "valueIs";
    public static final String VALUE_IS_NOT = "valueIsNot";
    public static final String EMPTY_TOKEN = "emptyToken";
    
    public static class ParseContext {
    	private final File file;
    	private File baseDir;
    	private XMLElement element;
    	
		public ParseContext(File file) {
			super();
			this.file = file;
			this.baseDir = file.getAbsoluteFile().getParentFile();
		}
		
		public XMLElement getImportElement() {
			return element;
		}
		
	    void setElement(XMLElement element) {
			this.element = element;
		}
		
		public File getFile() {
			return file;
		}
		
		public File getBaseDir() {
			return baseDir;
		}

        public void setBaseDir(File baseDir) {
            this.baseDir = baseDir;
        }
    }
    
    private LinkedList<ParseContext> stack = new LinkedList<ParseContext>();
    
    
    public ConfigureScript loadScript(String fileName) throws ConfigureException {
    	final File file = new File(fileName);
    	stack.add(new ParseContext(file));
    	try {
    		final XMLElement root = loadXML(file); 
    		return parseScript(root, file);
    	} finally {
    		stack.removeLast();
    	}
    }

	private XMLElement loadXML(final File file) throws ConfigureException {
		try {
            final FileReader r = new FileReader(file);
            try {
                StdXMLReader xr = new StdXMLReader(r);
                IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
                parser.setReader(xr);
                return (XMLElement) parser.parse();
            } finally {
                r.close();
            }
        } catch (FileNotFoundException ex) {
        	throw new ConfigureException("Cannot open " + file, ex);
        } catch (IOException ex) {
        	throw new ConfigureException("IO error reading " + file, ex);
        } catch (XMLException ex) {
            throw new ConfigureException("XML error reading " + file, ex);
        } catch (Exception ex) {
            throw new ConfigureException("Unexpected error reading " + file, ex);
        }
	}

	private ConfigureScript parseScript(XMLElement root, File scriptFile) 
	    throws ConfigureException {
		ConfigureScript script = new ConfigureScript(scriptFile);
		parseScript(root, script);
		return script;
	}

    private void parseScript(XMLElement root, ConfigureScript script) 
    	throws ConfigureException {
        if (!root.getName().equals(SCRIPT)) {
            error("Root element of a script file should be '" + SCRIPT + "'", root);
        }
        String baseDirName = root.getAttribute(BASE_DIR, "");
        if (baseDirName.length() > 0) {
            File baseDir = new File(stack.getLast().getBaseDir(), baseDirName);
            stack.getLast().setBaseDir(baseDir);
        }
        for (Enumeration<?> en = root.enumerateChildren(); en.hasMoreElements(); /**/ ) {
            XMLElement element = (XMLElement) en.nextElement();
            String elementName = element.getName();
            if (elementName.equals(TYPE)) {
                parseType(element, script);
            } else if (elementName.equals(CONTROL_PROPS)) {
                parseControlProps(element, script);
            } else if (elementName.equals(PROP_FILE)) {
                parsePropsFile(element, script);
            } else if (elementName.equals(SCREEN)) {
                parseScreen(element, script);
            } else if (elementName.equals(INCLUDE)) {
                parseInclude(element, script);
            } else {
                error("Unrecognized element '" + elementName + "'", element);
            }
        }
    }

	public File resolvePath(String fileName) {
		if (fileName == null) {
			return null;
		}
		File res = new File(fileName);
		if (!res.isAbsolute()) {
			res = new File(stack.getLast().getBaseDir(), fileName);
		}
		return res;
	}

    private void parseInclude(XMLElement element, ConfigureScript script) throws ConfigureException {
    	String includeFileName = element.getAttribute(SCRIPT_FILE, null);
    	if (includeFileName == null) {
    		error("A '" + SCRIPT_FILE + "' attribute is required for an '" + INCLUDE + "' element", element);
    	}
    	File includeFile = resolvePath(includeFileName);
    	XMLElement includeRoot = loadXML(includeFile);
    	stack.getLast().setElement(element);
    	stack.add(new ParseContext(includeFile));
    	try {
    		parseScript(includeRoot, script);
    	} finally {
    		stack.removeLast();
    	}
	}

	private void parseType(XMLElement element, ConfigureScript script) 
        throws ConfigureException {
        String name = element.getAttribute(NAME, null);
        String patternString = element.getAttribute(PATTERN, null);
        List<EnumeratedType.Alternate> alternates = 
            new LinkedList<EnumeratedType.Alternate>();
        for (Enumeration<?> en = element.enumerateChildren(); en.hasMoreElements(); /**/) {
            XMLElement child = (XMLElement) en.nextElement();
            if (!child.getName().equals(ALT)) {
                error("A '" + TYPE + "' element can only contain '" + ALT + "' elements", child);
            }
            String value = child.getAttribute(VALUE, null);
            String token = child.getAttribute(TOKEN, value);
            if (value == null) {
                error("A '" + VALUE + "' attribute is required for an '" + ALT + "' element", child);
            }
            if (token.length() == 0) {
            	// An empty token is problematic because and empty input line is 
            	// used to say "use the default value".
            	error("The (specified or implied) value of an '" + ALT + 
                        "' element's '" + TOKEN + "' attribute cannot be empty", child);
            }
            alternates.add(new EnumeratedType.Alternate(token, value));
        }
        PropertyType type = null;
        if (patternString == null) {
            if (alternates.isEmpty()) {
                error("A '" + TYPE + "' element must have a '" + PATTERN + 
                        "' attribute or '" + ALT + "' elements", element);
            } else {
                type = new EnumeratedType(name, alternates);
            }
        } else {
            if (!alternates.isEmpty()) {
                error("A '" + TYPE + "' element cannot have both a '" + PATTERN + 
                        "' attribute and '" + ALT + "' elements", element);
            } else {
            	try {
            		Pattern pattern = Pattern.compile(patternString);
            		String empty = element.getAttribute(EMPTY_TOKEN, null);
            		if (empty == null) {
            	        if (pattern.matcher("").matches()) {
            	        	error("An '" + EMPTY_TOKEN + "' attribute is required because the '" + 
                                    PATTERN + "' attribute matches the empty string", element);
            	        }
            		} else if (empty.length() == 0) {
            			error("The '" + EMPTY_TOKEN + "' attribute must not be an empty string", element);
            		}
            		type = new PatternType(name, pattern, empty);
            	} catch (PatternSyntaxException ex) {
            		error("Invalid '" + PATTERN + "' attribute: " + ex.getDescription(), element);
            	}
            }
        }
        script.addType(type);
    }

    private void parseControlProps(XMLElement element, ConfigureScript script) 
        throws ConfigureException {
    	PropertySet propSet = new PropertySet(script);
    	parseProperties(element, propSet, script);
        script.setControlProps(propSet);
    }

    private void parsePropsFile(XMLElement element, ConfigureScript script) 
        throws ConfigureException {
    	String propFileName = element.getAttribute(NAME, null);
        if (propFileName == null) {
            error("A '" + PROP_FILE + "' element requires a '" + NAME + "' attribute", element);
        }
        File propFile = resolvePath(propFileName);
        String defaultPropFileName = element.getAttribute(DEFAULT_FILE, null);
        File defaultPropFile = resolvePath(defaultPropFileName);
        String fileFormat = element.getAttribute(FILE_FORMAT, FileAdapter.JAVA_PROPERTIES_FORMAT);
        String templateFileName = element.getAttribute(TEMPLATE_FILE, null);
        File templateFile = resolvePath(templateFileName);
        String markerStr = element.getAttribute(MARKER, DEFAULT_MARKER);
        if (markerStr.length() != 1) {
            error("A '" + MARKER + "' attribute must be one character in length", element);
        }
        char marker = markerStr.charAt(0);
        if (marker == '\n' || marker == '\r') {
            error("This marker character won't work", element);
        }
        PropertySet propSet;
        try {
        	propSet = new PropertySet(
        	        script, propFile, defaultPropFile, templateFile, fileFormat, marker);
        } catch (ConfigureException ex) {
        	addStack(ex, element);
        	throw ex;
        }
		parseProperties(element, propSet, script);
        script.addPropsFile(propSet);
    }

    private PropertySet parseProperties(XMLElement element, PropertySet propSet, ConfigureScript script) 
        throws ConfigureException {
        for (Enumeration<?> en = element.enumerateChildren(); en.hasMoreElements(); /**/) {
            XMLElement child = (XMLElement) en.nextElement();
            if (child.getName().equals(PROPERTY)) {
                String name = child.getAttribute(NAME, null);
                if (name == null) {
                    error("A '" + PROPERTY + "' element requires a '" + NAME + "' attribute", child);
                }
                String typeName = child.getAttribute(TYPE, null);
                if (name == null) {
                    error("A '" + PROPERTY + "' element requires a '" + TYPE + "' attribute", child);
                }
                String description = child.getAttribute(DESCRIPTION, null);
                if (name == null) {
                    error("A '" + PROPERTY + "' element requires a '" + DESCRIPTION + "' attribute",  child);
                }
                String defaultValue = child.getAttribute(DEFAULT, null);
                PropertyType type = script.getTypes().get(typeName);
                if (type == null) {
                    error("Use of undeclared type '" + typeName + "'", child);
                }
                Value value = defaultValue == null ? null : type.fromValue(defaultValue);
                try {
                	propSet.addProperty(name, type, description, value, child, stack.getLast().getFile());
                } catch (ConfigureException ex) {
                	addStack(ex, child);
                	throw ex;
                }
            } else {
                error("Expected only '" + PROPERTY + "' elements in this context", element);
            }
        }
        return propSet;
    }

    private void parseScreen(XMLElement element, ConfigureScript script) 
        throws ConfigureException {
        String title = element.getAttribute(TITLE, null);
        if (title == null) {
            error("A '" + SCREEN + "' element requires a '" + TITLE + "' attribute", element);
        }
        String guardPropName = element.getAttribute(GUARD_PROP, null);
        String valueIsStr = element.getAttribute(VALUE_IS, null);
        String valueIsNotStr = element.getAttribute(VALUE_IS_NOT, null);
        
        Value valueIs = null;
        Value valueIsNot = null;
        if (guardPropName != null) {
            Property guardProp = script.getProperty(guardPropName);
            if (guardProp == null) {
                error("A guard property '" + guardPropName + "' not declared", element);
            }
            if (valueIsStr != null && valueIsNotStr != null) {
                error("The '" + VALUE_IS + "' and '" + VALUE_IS_NOT + 
                        "' attributes cannot be used together", element);
            }
            PropertyType type = guardProp.getType();
            if (valueIsStr != null) {
                valueIs = type.fromValue(valueIsStr);
                if (valueIs == null) {
                    error("The string '" + valueIsStr + "' is not a valid " + 
                            type.getTypeName() + " instance", element);
                }
            }
            if (valueIsNotStr != null) {
                valueIsNot = type.fromValue(valueIsNotStr);
                if (valueIsNot == null) {
                    error("The string '" + valueIsNotStr + "' is not a valid " + 
                            type.getTypeName() + " instance", element);
                }
            }
        }
        Screen screen = new Screen(title, guardPropName, valueIs, valueIsNot);
        script.addScreen(screen);
        for (Enumeration<?> en = element.enumerateChildren(); en.hasMoreElements(); /**/ ) {
            XMLElement child = (XMLElement) en.nextElement();
            if (!child.getName().equals(ITEM)) {
                error("Expected an '" + ITEM + "' element", child);
            }
            String propName = child.getAttribute(PROPERTY, null);
            if (propName == null) {
                error("The '" + PROPERTY + "' attribute is required for an '" + 
                        ITEM + "' element", child);
            }
            if (script.getProperty(propName) == null) {
                error("Use of undeclared property '" + propName + "'", child);
            }
            String text = child.getContent();
            screen.addItem(new Item(script, propName, text));
        }
    }
    
    private void addStack(ConfigureException ex, XMLElement element) {
    	stack.getLast().setElement(element);
    	ParseContext[] stackCopy = new ParseContext[stack.size()];
    	stack.toArray(stackCopy);
    	ex.setStack(stackCopy);
    }
    
    private void error(String message, XMLElement element) throws ConfigureException {
    	ConfigureException ex = new ConfigureException(message);
    	addStack(ex, element);
    	throw ex;
    }
}

/*
 * Copyright 1998-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package javax.swing.text.html.parser;

import java.io.PrintStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.BitSet;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.Properties;
import java.net.URL;

/**
 * The representation of an SGML DTD.  DTD describes a document
 * syntax and is used in parsing of HTML documents.  It contains
 * a list of elements and their attributes as well as a list of
 * entities defined in the DTD.
 *
 * @see Element
 * @see AttributeList
 * @see ContentModel
 * @see Parser
 * @author Arthur van Hoff
 */
public
class DTD implements DTDConstants {
    public String name;
    public Vector<Element> elements = new Vector<Element>();
    public Hashtable<String,Element> elementHash
	= new Hashtable<String,Element>();
    public Hashtable<Object,Entity> entityHash
	= new Hashtable<Object,Entity>();
    public final Element pcdata = getElement("#pcdata");
    public final Element html = getElement("html");
    public final Element meta = getElement("meta");
    public final Element base = getElement("base");
    public final Element isindex = getElement("isindex");
    public final Element head = getElement("head");
    public final Element body = getElement("body");
    public final Element applet = getElement("applet");
    public final Element param = getElement("param");
    public final Element p = getElement("p");
    public final Element title = getElement("title");
    final Element style = getElement("style");
    final Element link = getElement("link");
    final Element script = getElement("script");

    public static final int FILE_VERSION = 1;

    /**
     * Creates a new DTD with the specified name.
     * @param name the name, as a <code>String</code> of the new DTD
     */
    protected DTD(String name) {
	this.name = name;
	defEntity("#RE", GENERAL, '\r');
	defEntity("#RS", GENERAL, '\n');
	defEntity("#SPACE", GENERAL, ' ');
	defineElement("unknown", EMPTY, false, true, null, null, null, null);
    }

    /**
     * Gets the name of the DTD.
     * @return the name of the DTD
     */
    public String getName() {
	return name;
    }

    /**
     * Gets an entity by name.
     * @return the <code>Entity</code> corresponding to the 
     *   <code>name</code> <code>String</code>
     */
    public Entity getEntity(String name) {
	return (Entity)entityHash.get(name);
    }

    /**
     * Gets a character entity.
     * @return the <code>Entity</code> corresponding to the
     *    <code>ch</code> character
     */
    public Entity getEntity(int ch) {
	return (Entity)entityHash.get(new Integer(ch));
    }

    /**
     * Returns <code>true</code> if the element is part of the DTD,
     * otherwise returns <code>false</code>.
     *
     * @param  name the requested <code>String</code>
     * @return <code>true</code> if <code>name</code> exists as
     *   part of the DTD, otherwise returns <code>false</code>
     */
    boolean elementExists(String name) {
        return !"unknown".equals(name) && (elementHash.get(name) != null);
    }

    /**
     * Gets an element by name. A new element is
     * created if the element doesn't exist.
     *
     * @param name the requested <code>String</code>
     * @return the <code>Element</code> corresponding to
     *   <code>name</code>, which may be newly created
     */
    public Element getElement(String name) {
	Element e = (Element)elementHash.get(name);
	if (e == null) {
	    e = new Element(name, elements.size());
	    elements.addElement(e);
	    elementHash.put(name, e);
	}
	return e;
    }

    /**
     * Gets an element by index.
     *
     * @param index the requested index
     * @return the <code>Element</code> corresponding to
     *   <code>index</code>
     */
    public Element getElement(int index) {
	return (Element)elements.elementAt(index);
    }

    /**
     * Defines an entity.  If the <code>Entity</code> specified
     * by <code>name</code>, <code>type</code>, and <code>data</code>
     * exists, it is returned; otherwise a new <code>Entity</code>
     * is created and is returned.
     *
     * @param name the name of the <code>Entity</code> as a <code>String</code>
     * @param type the type of the <code>Entity</code>
     * @param data the <code>Entity</code>'s data
     * @return the <code>Entity</code> requested or a new <code>Entity</code>
     *   if not found
     */
    public Entity defineEntity(String name, int type, char data[]) {
	Entity ent = (Entity)entityHash.get(name);
	if (ent == null) {
	    ent = new Entity(name, type, data);
	    entityHash.put(name, ent);
	    if (((type & GENERAL) != 0) && (data.length == 1)) {
		switch (type & ~GENERAL) {
		  case CDATA:
		  case SDATA:
		    entityHash.put(new Integer(data[0]), ent);
		    break;
		}
	    }
	}
	return ent;
    }

    /**
     * Returns the <code>Element</code> which matches the
     * specified parameters.  If one doesn't exist, a new
     * one is created and returned.
     *
     * @param name the name of the <code>Element</code>
     * @param type the type of the <code>Element</code>
     * @param omitStart <code>true</code> if start should be omitted
     * @param omitEnd  <code>true</code> if end should be omitted
     * @param content  the <code>ContentModel</code>
     * @param atts the <code>AttributeList</code> specifying the 
     *    <code>Element</code>
     * @return the <code>Element</code> specified
     */
    public Element defineElement(String name, int type,
		       boolean omitStart, boolean omitEnd, ContentModel content,
		       BitSet exclusions, BitSet inclusions, AttributeList atts) {
	Element e = getElement(name);
	e.type = type;
	e.oStart = omitStart;
	e.oEnd = omitEnd;
	e.content = content;
	e.exclusions = exclusions;
	e.inclusions = inclusions;
	e.atts = atts;
	return e;
    }

    /**
     * Defines attributes for an {@code Element}.
     *
     * @param name the name of the <code>Element</code>
     * @param atts the <code>AttributeList</code> specifying the
     *    <code>Element</code>
     */
    public void defineAttributes(String name, AttributeList atts) {
	Element e = getElement(name);
	e.atts = atts;
    }

    /**
     * Creates and returns a character <code>Entity</code>.
     * @param name the entity's name
     * @return the new character <code>Entity</code>
     */
    public Entity defEntity(String name, int type, int ch) {
	char data[] = {(char)ch};
	return defineEntity(name, type, data);
    }

    /**
     * Creates and returns an <code>Entity</code>.
     * @param name the entity's name
     * @return the new <code>Entity</code>
     */
    protected Entity defEntity(String name, int type, String str) {
	int len = str.length();
	char data[] = new char[len];
	str.getChars(0, len, data, 0);
	return defineEntity(name, type, data);
    }

    /**
     * Creates and returns an <code>Element</code>.
     * @param name the element's name
     * @return the new <code>Element</code>
     */
    protected Element defElement(String name, int type,
		       boolean omitStart, boolean omitEnd, ContentModel content,
		       String[] exclusions, String[] inclusions, AttributeList atts) {
	BitSet excl = null;
	if (exclusions != null && exclusions.length > 0) {
	    excl = new BitSet();
	    for (int i = 0; i < exclusions.length; i++) {
		String str = exclusions[i];
		if (str.length() > 0) {
		    excl.set(getElement(str).getIndex());
		}
	    }
	}
	BitSet incl = null;
	if (inclusions != null && inclusions.length > 0) {
	    incl = new BitSet();
	    for (int i = 0; i < inclusions.length; i++) {
		String str = inclusions[i];
		if (str.length() > 0) {
		    incl.set(getElement(str).getIndex());
		}
	    }
	}
	return defineElement(name, type, omitStart, omitEnd, content, excl, incl, atts);
    }

    /**
     * Creates and returns an <code>AttributeList</code>.
     * @param name the attribute list's name
     * @return the new <code>AttributeList</code>
     */
    protected AttributeList defAttributeList(String name, int type, int modifier, String value, String values, AttributeList atts) {
	Vector vals = null;
	if (values != null) {
	    vals = new Vector();
	    for (StringTokenizer s = new StringTokenizer(values, "|") ; s.hasMoreTokens() ;) {
		String str = s.nextToken();
		if (str.length() > 0) {
		    vals.addElement(str);
		}
	    }
	}
	return new AttributeList(name, type, modifier, value, vals, atts);
    }

    /**
     * Creates and returns a new content model.
     * @param type the type of the new content model
     * @return the new <code>ContentModel</code>
     */
    protected ContentModel defContentModel(int type, Object obj, ContentModel next) {
	return new ContentModel(type, obj, next);
    }

    /**
     * Returns a string representation of this DTD.
     * @return the string representation of this DTD
     */
    public String toString() {
	return name;
    }

    /**
     * The hashtable of DTDs.
     */
    static Hashtable dtdHash = new Hashtable();

  public static void putDTDHash(String name, DTD dtd) {
    dtdHash.put(name, dtd);
  }
    /**
     * Returns a DTD with the specified <code>name</code>.  If
     * a DTD with that name doesn't exist, one is created
     * and returned.  Any uppercase characters in the name
     * are converted to lowercase.
     *
     * @param name the name of the DTD
     * @return the DTD which corresponds to <code>name</code>
     */
    public static DTD getDTD(String name) throws IOException {
	name = name.toLowerCase();
	DTD dtd = (DTD)dtdHash.get(name);
	if (dtd == null)
	  dtd = new DTD(name);

	return dtd;
    }

    /**
     * Recreates a DTD from an archived format.
     * @param in  the <code>DataInputStream</code> to read from
     */
    public void read(DataInputStream in) throws IOException {
	if (in.readInt() != FILE_VERSION) {
	}

	//
	// Read the list of names
	//
	String[] names = new String[in.readShort()];
	for (int i = 0; i < names.length; i++) {
	    names[i] = in.readUTF();
	}


	//
	// Read the entities
	//
	int num = in.readShort();
	for (int i = 0; i < num; i++) {
	    short nameId = in.readShort();
	    int type = in.readByte();
	    String name = in.readUTF();
	    defEntity(names[nameId], type | GENERAL, name);
	}

	// Read the elements
	//
	num = in.readShort();
	for (int i = 0; i < num; i++) {
	    short nameId = in.readShort();
	    int type = in.readByte();
	    byte flags = in.readByte();
	    ContentModel m = readContentModel(in, names);
	    String[] exclusions = readNameArray(in, names);
	    String[] inclusions = readNameArray(in, names);
	    AttributeList atts = readAttributeList(in, names);
	    defElement(names[nameId], type,
		       ((flags & 0x01) != 0), ((flags & 0x02) != 0),
		       m, exclusions, inclusions, atts);
	}
    }

    private ContentModel readContentModel(DataInputStream in, String[] names)
		throws IOException {
	byte flag = in.readByte();
	switch(flag) {
	    case 0:		// null
		return null;
	    case 1: {		// content_c
		int type = in.readByte();
		ContentModel m = readContentModel(in, names);
		ContentModel next = readContentModel(in, names);
		return defContentModel(type, m, next);
	    }
	    case 2: {		// content_e
		int type = in.readByte();
		Element el = getElement(names[in.readShort()]);
		ContentModel next = readContentModel(in, names);
		return defContentModel(type, el, next);
	    }
	default:
		throw new IOException("bad bdtd");
	}
    }

    private String[] readNameArray(DataInputStream in, String[] names)
		throws IOException {
	int num = in.readShort();
	if (num == 0) {
	    return null;
	}
	String[] result = new String[num];
	for (int i = 0; i < num; i++) {
	    result[i] = names[in.readShort()];
	}
	return result;
    }


    private AttributeList readAttributeList(DataInputStream in, String[] names)
		throws IOException  {
	AttributeList result = null;
	for (int num = in.readByte(); num > 0; --num) {
	    short nameId = in.readShort();
	    int type = in.readByte();
	    int modifier = in.readByte();
	    short valueId = in.readShort();
	    String value = (valueId == -1) ? null : names[valueId];
	    Vector values = null;
	    short numValues = in.readShort();
	    if (numValues > 0) {
		values = new Vector(numValues);
		for (int i = 0; i < numValues; i++) {
		    values.addElement(names[in.readShort()]);
		}
	    }
result = new AttributeList(names[nameId], type, modifier, value,
				       values, result);
	    // We reverse the order of the linked list by doing this, but
	    // that order isn't important.
	}
	return result;
    }

}

// Copyright 2001, FreeHEP.
package org.jnode.awt.font.truetype;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;

/**
 * Concrete implementation of the TrueType Font, read from a TTF File.
 *
 * @author Mark Donszelmann
 * @version $Id$
 */
public class TTFFontDataFile extends TTFFontData {

	private static final String mode = "r";

	private String fileName;
	private TTFInput ttf;

	private int sfntMajorVersion;
	private int sfntMinorVersion;
	private int numberOfTables;
	private int searchRange;
	private int entrySelector;
	private int rangeShift;

	public TTFFontDataFile(String name) throws FileNotFoundException, IOException {
		this(new TTFFileInput(new RandomAccessFile(name, mode)));
		fileName = name;
	}

	public TTFFontDataFile(byte[] data) throws IOException {
		this(new TTFMemoryInput(data));
	}

	public TTFFontDataFile(URL url) throws IOException {
		this(new TTFURLInput(url));
	}

	public TTFFontDataFile(TTFInput input) throws IOException {
		ttf = input;

		// read table directory
		ttf.seek(0);

		// read the version : 0x00010000 for version 1.0.
		sfntMajorVersion = ttf.readUShort();
		sfntMinorVersion = ttf.readUShort();
		// read the number of tables
		numberOfTables = ttf.readUShort();

		//(Maximum power of 2 <= numTables) x 16.
		searchRange = ttf.readUShort();
		//Log2(maximum power of 2 <= numTables).
		entrySelector = ttf.readUShort();
		//NumTables x 16-searchRange.
		rangeShift = ttf.readUShort();

		// This is followed at byte 12 by the Table Directory entries. 
		// Entries in the Table Directory must be sorted in ascending 
		// order by tag.

		// read table entries
		for (int i = 0; i < numberOfTables; i++) {
			ttf.seek(12 + i * 16);
			// read TAG  = 4 –byte identifier.
			byte b[] = new byte[4];
			ttf.readFully(b);
			String tag = new String(b);
			// read CheckSum for this table.
			ttf.readLong();
			// read Offset from beginning of TrueType font file.
			int offset = ttf.readLong();
			//read Length of this table.
			int len = ttf.readLong();
			// create a table directoryEntry entry 
			TTFInput tableInput = ttf.createSubInput(offset, len);
			// insert this table as a new table
			newTable(tag, tableInput);
		}
		readAll();
	}

	public int getFontVersion() {
		return sfntMajorVersion;
	}

	public void close() throws IOException {
		super.close();
		ttf.close();
	}

	public void show() {
		super.show();

		System.out.println("Font: " + fileName);
		System.out.println("  sfnt: " + sfntMajorVersion + "." + sfntMinorVersion);
		System.out.println("  numTables: " + numberOfTables);
		System.out.println("  searchRange: " + searchRange);
		System.out.println("  entrySelector: " + entrySelector);
		System.out.println("  rangeShift: " + rangeShift);
	}

}

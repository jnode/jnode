/*
 * $Id$
 */
package org.jnode.fs.fat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.jnode.fs.FSEntry;
import org.jnode.fs.FSEntryIterator;

/**
 * @author gbin
 */
public class FatLfnDirectory extends FatDirectory {

	private HashMap shortNameIndex = new HashMap();
	private HashMap longFileNameIndex = new HashMap();

	/**
	 * @param fs
	 * @param file
	 * @throws IOException
	 */
	public FatLfnDirectory(FatFileSystem fs, FatFile file) throws IOException {
		super(fs, file);
	}

	// for root
	public FatLfnDirectory(FatFileSystem fs, int nrEntries) {
		super(fs, nrEntries);
	}

	public FSEntry addFile(String name) throws IOException {
		name = name.trim();
		String shortName = generateShortNameFor(name);
		FatDirEntry realEntry = new FatDirEntry(this, splitName(shortName), splitExt(shortName));
		LfnEntry entry = new LfnEntry(this, realEntry, name);
		shortNameIndex.put(shortName, entry);
		longFileNameIndex.put(name, entry);
		setDirty();
		flush();
		return entry;
	}

	public FSEntry addDirectory(String name) throws IOException {
		name = name.trim();
		String shortName = generateShortNameFor(name);
		FatDirEntry realEntry = new FatDirEntry(this, splitName(shortName), splitExt(shortName));

		final long parentCluster;
		if (file == null) {
			parentCluster = 0;
		} else {
			parentCluster = file.getStartCluster();
		}

		final int clusterSize = getFatFileSystem().getClusterSize();
		realEntry.setFlags(FatConstants.F_DIRECTORY);
		final FatFile file = realEntry.getFatFile();
		file.setLength(clusterSize);
		final byte[] buf = new byte[clusterSize];
		// Clean the contents of this cluster to avoid reading strange data
		// in the directory.
		file.write(0, buf, 0, buf.length);
		file.getDirectory().initialize(file.getStartCluster(), parentCluster);

		LfnEntry entry = new LfnEntry(this, realEntry, name);
		shortNameIndex.put(shortName, entry);
		longFileNameIndex.put(name, entry);
		setDirty();
		flush();
		return entry;
	}

	public FSEntry getEntry(String name) {
		//System.out.println("Search : " + name);
		name = name.trim();
		FSEntry entry;
		// try first as a long file name
		entry = (LfnEntry)longFileNameIndex.get(name);
		if (entry == null)
			return (LfnEntry)shortNameIndex.get(name.toUpperCase());
		else
			return entry;

	}

	protected synchronized void read(byte[] src) {
		super.read(src);
		readLFN();
	}

	private void readLFN() {
		//System.out.println("Read LFN");
		int i = 0;
		int size = entries.size();

		while (i < size) {
			// jump over empty entries
			while (i < size && entries.get(i) == null) {
				i++;
			}

			if (i >= size) {
				break;
			}

			int offset = i; // beginning of the entry
			//  check when we reach a real entry
			while (entries.get(i) instanceof FatLfnDirEntry) {
            //System.out.println(" Jumped over : " + entries.get(i));
            i++;
				if (i >= size) {
					// This is a cutted entry, forgive it
					break;
				}
			}
			i++;
			if (i >= size) {
            // This is a cutted entry, forgive it
				break;
			}

			LfnEntry current = new LfnEntry(this, entries, offset, i - offset);
         
         if (!current.isDeleted() && current.isValid())   
         {
            shortNameIndex.put(current.getRealEntry().getName(), current);
            longFileNameIndex.put(current.getName(), current);
		      //System.out.println(" Found a real entry : |" + current + "|");
         }
		}

	}
	private void updateLFN() throws IOException{
		//System.out.println("Update LFN");
		Iterator allEntries = shortNameIndex.values().iterator();
		Vector destination = new Vector();

		while (allEntries.hasNext()) {
			LfnEntry currentEntry = (LfnEntry)allEntries.next();
			FatBasicDirEntry[] encoded = currentEntry.compactForm();
			for (int i = 0; i < encoded.length; i++) {
				destination.add(encoded[i]);
			}
		}

		final int size = destination.size();
		if(entries.size()<size){ 
			if (!canChangeSize(size)) {
				throw new IOException("Directory is full");
			}
		}
		
		boolean useAdd=false;
		for (int i = 0; i < size; i++) {
			if(!useAdd){
				try{
					entries.set(i, destination.get(i));
				}catch(ArrayIndexOutOfBoundsException aEx){
					useAdd=true;
				}
			}
			if(useAdd){
				entries.add(i, destination.get(i));
			}
		}
		
		final int entireSize = entries.size();
		for (int i = size; i < entireSize; i++) {
			entries.set(i, null); // remove stale entries
		}

	}

	public void flush() throws IOException {
		//System.out.println("LfnDirectory: Flush");
		updateLFN();
		super.flush();
	}

	public FSEntryIterator iterator() {
		return new FSEntryIterator()
		{
			Iterator it = shortNameIndex.values().iterator();
			
			public boolean hasNext() {
				return it.hasNext();
			}

			public FSEntry next() {
				return (FSEntry) it.next();
			}			
		};
	}

	/*
	 * Its in the DOS manual!(DOS 5: page 72) Valid: A..Z 0..9 _ ^ $ ~ ! # % & - {} () @ ' `
	 * 
	 * Unvalid: spaces/periods,
	 */

	public String generateShortNameFor(String longFullName) {
		int dotIndex = longFullName.lastIndexOf('.');

		String longName;
		String longExt;

		if (dotIndex == -1) // No dot in the name
			{
			longName = longFullName;
			longExt = ""; // so no extension
		} else {
			// split it at the dot
			longName = longFullName.substring(0, dotIndex);
			longExt = longFullName.substring(dotIndex + 1);
		}

		String shortName = longName;
		String shortExt = longExt;

		// make the extension short
		if (shortExt.length() > 3) {
			shortExt = shortExt.substring(0, 3);
		}

		// make the ~n name short
		if (shortName.length() > 8) {
			// trim it
			char[] shortNameChar = shortName.substring(0, 7).toUpperCase().toCharArray();

			// epurate it from alien characters
			loop : for (int i = 0; i < shortNameChar.length; i++) {
				char toTest = shortNameChar[i];
				valid : {
					if (toTest > 255)
						break valid;
					if (toTest == ' ')
						break valid;
					if (toTest >= 'A' && toTest <= 'Z')
						continue loop;
					if (toTest >= '0' && toTest <= '9')
						continue loop;
					if (toTest == '_'
						|| toTest == '^'
						|| toTest == '$'
						|| toTest == '~'
						|| toTest == '!'
						|| toTest == '#'
						|| toTest == '%'
						|| toTest == '&'
						|| toTest == '-'
						|| toTest == '{'
						|| toTest == '}'
						|| toTest == '('
						|| toTest == ')'
						|| toTest == '@'
						|| toTest == '\''
						|| toTest == '`')
						continue loop;

				}
				shortNameChar[i] = '_';

			}

			// name range from "nnnnnn~1" to "~9999999"
			for (int i = 1; i <= 99999999; i++) {
				String tildeStuff = "~" + i;
				int tildeStuffLength = tildeStuff.length();
				//System.out.println("tildeStuff="+tildeStuff);
				System.arraycopy(tildeStuff.toCharArray(), 0, shortNameChar, 7 - tildeStuffLength, tildeStuffLength);
				shortName = new String(shortNameChar);
				if (!shortNameIndex.containsKey(shortName + "." + shortExt))
					break;
			}
		}

		String shortFullName = shortName + "." + shortExt;
		return shortFullName.toUpperCase();
	}

	/**
	 * Remove the entry with the given name from this directory.
	 * 
	 * @param name
	 * @throws IOException
	 */
	public void remove(String name) throws IOException {
		name = name.trim();
		LfnEntry byLongName = (LfnEntry)longFileNameIndex.get(name);

		if (byLongName != null) {
			longFileNameIndex.remove(name);
			shortNameIndex.remove(byLongName.getRealEntry().getName());
			return;
		}

		String uppedName = name.toUpperCase();
		LfnEntry byShortName = (LfnEntry)shortNameIndex.get(uppedName);

		if (byShortName != null) {
			longFileNameIndex.remove(byShortName.getName());
			shortNameIndex.remove(uppedName);
		}
		throw new FileNotFoundException(name);
	}

}

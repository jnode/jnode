// ----------------------------------------
//  Elf ( support ELF format (32bit for i[34]86))
//
//  Copyright (C) 1999  Kiyoka Nishiyama
//
//  $Date$ 
//  $Id$
// ----------------------------------------
package org.jnode.linker;

//
//
// typedef struct elf32_hdr{
//   EI_NIDENT = 16;
//   char	e_ident[EI_NIDENT];      :  e_ident[0...3] = '\x7f' "ELF" であること
//   Elf32_Half	e_type;                  :  ET_EXEC であること
//   Elf32_Half	e_machine;               :  EM_386 であること (将来 EM_486サポートする)
//   Elf32_Word	e_version;
//   Elf32_Addr	e_entry;  /* Entry point */
//   Elf32_Off	e_phoff;
//   Elf32_Off	e_shoff;
//   Elf32_Word	e_flags;
//   Elf32_Half	e_ehsize;
//   Elf32_Half	e_phentsize;
//   Elf32_Half	e_phnum;
//   Elf32_Half	e_shentsize;
//   Elf32_Half	e_shnum;
//   Elf32_Half	e_shstrndx;
// } Elf32_Ehdr;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.jnode.assembler.x86.X86Stream;
import org.jnode.assembler.x86.X86Stream.X86ObjectRef;


public class Elf {
	static final short ET_NONE = 0; /* No file type */
	static final short ET_REL = 1; /* Relocatable file */
	static final short ET_EXEC = 2; /* Executable file */
	static final short ET_DYN = 3; /* Shared object file */
	static final short ET_CORE = 4; /* Core file */
	static final short ET_NUM = 5; /* Number of defined types.  */

	/* These constants define the various ELF target machines */
	static final short EM_386 = 3;
	static final short EM_486 = 6; /* Perhaps disused */

	/* Version constants */
	static final int EV_NONE = 0;
	static final int EV_CURRENT = 1;

	// These constants define the sizes of the various structures
	static final short EHSIZE = 52; // Elf header size
	static final short EHSIZE_EXT = 64; // Elf header size + alignment
	static final short SHENTSIZE = 40; // Section header entry size
	// Align on 4 bytes
	static final short ALIGN = 4;

	static final short EI_MAG0 = 0; //File identification
	static final short EI_MAG1 = 1; //File identification
	static final short EI_MAG2 = 2; //File identification
	static final short EI_MAG3 = 3; //File identification
	static final short EI_CLASS = 4; //File class
	static final short EI_DATA = 5; //Data encoding
	static final short EI_VERSION = 6; //File version
	static final short EI_PAD = 7; //Start of padding bytes
	static final short EI_NIDENT = 16; //Size of e_ident[]

	byte e_ident[] = new byte[EI_NIDENT]; //   :  e_ident[0...3] = '\x7f' "ELF"
	short e_type;
	short e_machine;
	int e_version;
	int e_entry; // Entry point
	int e_phoff;
	int e_shoff;
	int e_flags;
	short e_shentsize;
	short e_shstrndx;

	Vector sections;
	Section symSection;
	Section strSection;
	Section shstrSection;
	Section textSection;
	Section reltextSection;
	Section dataSection;
	Section reldataSection;
	Section bssSection;

	private Elf(short type) {
		e_ident[EI_MAG0] = 0x7f;
		e_ident[EI_MAG1] = (byte) 'E';
		e_ident[EI_MAG2] = (byte) 'L';
		e_ident[EI_MAG3] = (byte) 'F';
		e_ident[EI_CLASS] = 1; // 0=none, 1=32-bit, 2=64-bit
		e_ident[EI_DATA] = 1; // 0=none, 1=lsb, 2=msb
		e_ident[EI_VERSION] = (byte) EV_CURRENT;
		e_type = type;
		e_machine = EM_386;
		e_version = EV_CURRENT;
		e_entry = 0;
		e_phoff = 0;
		e_shoff = 0;
		e_flags = 0;
		e_shstrndx = 0;
		sections = new Vector();
		if (e_type != ET_NONE) {
			// Section 0 should be NULL
			sections.addElement(Section.newNullInstance(this));

			// Add this sections first! Otherwise we cannot create names for the sections
			sections.addElement(shstrSection = Section.newStrTabSection(this));
			e_shstrndx = (short) (sections.size() - 1);

			// Now add a String table
			sections.addElement(strSection = Section.newStrTabSection(this));

			// Now add a Symbol table
			sections.addElement(symSection = Section.newSymTabSection(this));

			// Add a .text section
			sections.addElement(textSection = Section.newTextSection(this));

			// Add a .data section
			sections.addElement(dataSection = Section.newDataSection(this));

			// Add a .bss section
			sections.addElement(bssSection = Section.newBssSection(this));

			// Add a .rel.text section			
			sections.addElement(
				reltextSection =
					Section.newRelTabSection(this, symSection, textSection));

			// Add a .rel.data section			
			sections.addElement(
				reldataSection =
					Section.newRelTabSection(this, symSection, dataSection));
		}
	}

	private Elf() {
		this(ET_NONE);
	}

	public static Elf newRelInstance() {
		return new Elf(ET_REL);
	}

	public static Elf newExeInstance() {
		return new Elf(ET_EXEC);
	}

	public static Elf newFromFile(String filename) throws IOException {
		Elf elf = new Elf();
		elf.load(filename);
		return elf;
	}

	/**
	 * Wrap my contents into an elf object file.
	 * @return Elf
	 */
	public static Elf toElf(X86Stream stream) {
		final Elf elf = Elf.newRelInstance();
		// Store the emitted objects in the text section		
		final Section textSection = elf.getSectionByName(".text");
		final Section relTextSection = elf.getRelTextSection();
		textSection.setBody(stream.getBytes(), 0, stream.getLength());

		// Add all symbols & relocs
		final Collection objectRefs = stream.getObjectRefs();
		System.out.println("Creating " + objectRefs.size() + " symbols");
		int cnt = 0;
		for (Iterator i = objectRefs.iterator(); i.hasNext(); cnt++) {
			/*if ((cnt % 1000) == 0) {
				long end = System.currentTimeMillis();
				System.out.println("At " + cnt + ", it took " + (end-start) + "ms");
				start = end;
			}*/
			final X86ObjectRef ref = (X86ObjectRef) i.next();
			final Object obj = ref.getObject();
			final Symbol sym;
			if (ref.isResolved()) {
				sym = new Symbol(elf, obj.toString(), ref.getOffset(), textSection);
			} else {
				final int[] offsets = ref.getUnresolvedOffsets();
				sym = new Symbol(elf, obj.toString(), 0, null);
				for (int j = 0; j < offsets.length; j++) {
					if (ref.isRelJump()) {
						relTextSection.addPcRelReloc(sym, offsets[j]);
					} else {
						relTextSection.addAbsReloc(sym, offsets[j]);
					}
				}
			}
			if (ref.isPublic()) {
				sym.setBind(Symbol.STB_GLOBAL);
			} else {
				sym.setBind(Symbol.STB_LOCAL);
			}
			elf.addSymbol(sym);
		}
		return elf;
	}

	// --------------------------------------------
	// Header
	// --------------------------------------------
	public boolean isRel() {
		return (e_type == ET_REL);
	}
	public boolean isExe() {
		return (e_type == ET_EXEC);
	}

	public int getEntry() {
		return e_entry;
	}
	public void setEntry(int e) {
		e_entry = e;
	}

	// --------------------------------------------
	// Strings
	// --------------------------------------------
	public String getString(int addr) {
		return strSection.getString(addr);
	}
	public int addString(String name) {
		return strSection.addString(name);
	}
	public String getSHString(int addr) {
		return shstrSection.getString(addr);
	}
	protected Section getSHStrSection() {
		return shstrSection;
	}

	// --------------------------------------------
	// Symbols
	// --------------------------------------------
	public int getNoSymbols() {
		return symSection.getNoSymbols();
	}
	public Symbol getSymbol(int index) {
		return symSection.getSymbol(index);
	}
	public Symbol getSymbolByAddress(int address) {
		return symSection.getSymbolByAddress(address);
	}
	public void addSymbol(Symbol s) {
		symSection.addSymbol(s);
	}
	public int getIndexOfSymbol(Symbol symbol) {
		return symSection.getIndexOfSymbol(symbol);
	}

	// --------------------------------------------
	// Relocs
	// --------------------------------------------
	public Section getRelTextSection() {
		return reltextSection;
	}
	public Section getRelDataSection() {
		return reldataSection;
	}

	// --------------------------------------------
	// Sections
	// --------------------------------------------
	public int getNoSections() {
		return sections.size();
	}
	public Section getSection(int index) {
		return ((index >= 0) && (index < getNoSections()))
			? (Section) sections.elementAt(index)
			: null;
	}
	public Section getSectionByName(String name) {
		for (int i = 1; i < getNoSections(); i++) {
			Section s = getSection(i);
			if (s.getName().equals(name))
				return s;
		}
		return null;
	}
	protected int getSectionIndex(Section s) {
		for (int i = 0; i < getNoSections(); i++) {
			if (s == getSection(i))
				return i;
		}
		return -1;
	}

	// --------------------------------------------
	// Loading & Storing
	// --------------------------------------------

	// Load the ELF data to file
	private void load(String filename) throws IOException {
		RandomAccessFile in;
		int i;
		int e_shnum;
		in = new RandomAccessFile(filename, "r");
		try {	
			// Load the elf header
			LoadUtil.bytes(in, e_ident);
			e_type = LoadUtil.little16(in);
			e_machine = LoadUtil.little16(in);
			e_version = LoadUtil.little32(in);
			e_entry = LoadUtil.little32(in);
			e_phoff = LoadUtil.little32(in);
			e_shoff = LoadUtil.little32(in);
			e_flags = LoadUtil.little32(in);
			/*e_ehsize      =*/
			LoadUtil.little16(in);
			/*e_phentsize   =*/
			LoadUtil.little16(in);
			/*e_phnum       =*/
			LoadUtil.little16(in);
			e_shentsize = LoadUtil.little16(in);
			e_shnum = LoadUtil.little16(in);
			e_shstrndx = LoadUtil.little16(in);
	
			if (!((e_ident[0] == 0x7F)
				&& (e_ident[1] == 'E')
				&& (e_ident[2] == 'L')
				&& (e_ident[3] == 'F'))) {
				throw new IOException("Not Elf Format :" + filename);
			}
	
			if (e_machine != EM_386) {
				throw new IOException("Not Match CPU Type :" + filename);
			}
	
			// Load the program header (ignored for now)
			in.seek(e_phoff);
	
			// Load the section headers
			in.seek(e_shoff);
			for (i = 0; i < e_shnum; i++) {
				Section s = Section.newInstance(this, in);
				sections.addElement(s);
				//System.out.println("Read section " + i);
			}
	
			// Load the sections
			for (i = 0; i < e_shnum; i++) {
				//System.out.println("Read section body " + i);
				Section s = (Section) sections.elementAt(i);
				s.loadBody(in);
				if (i == e_shstrndx)
					shstrSection = s;
				else if (s.isSymTab())
					symSection = s;
				else if (s.isStrTab())
					strSection = s;
			}
		} finally {
			in.close();
		}
	}

	// Store the ELF data from file
	public boolean store(String filename) throws IOException {
		FileOutputStream out;
		int i;
		int ofs = 0;
		out = new FileOutputStream(filename);

		// Prepare to store the sections and initialize their offsets
		ofs = EHSIZE_EXT;

		// Skip section 0, since it is always 0
		for (i = 1; i < getNoSections(); i++) {
			Section s = getSection(i);
			s.setOffset(ofs);
			ofs += s.prepareStoreBody();
			// align
			while ((ofs % ALIGN) != 0)
				ofs++;
		}
		e_shoff = ofs;

		// Now start storing
		ofs = 0;

		// Store the header
		ofs += StoreUtil.bytes(out, e_ident);
		ofs += StoreUtil.little16(out, e_type);
		ofs += StoreUtil.little16(out, e_machine);
		ofs += StoreUtil.little32(out, e_version);
		ofs += StoreUtil.little32(out, e_entry);
		ofs += StoreUtil.little32(out, 0);
		ofs += StoreUtil.little32(out, e_shoff);
		ofs += StoreUtil.little32(out, e_flags);
		ofs += StoreUtil.little16(out, EHSIZE);
		ofs += StoreUtil.little16(out, 0);
		ofs += StoreUtil.little16(out, 0);
		ofs += StoreUtil.little16(out, SHENTSIZE);
		ofs += StoreUtil.little16(out, getNoSections());
		ofs += StoreUtil.little16(out, e_shstrndx);

		// Align till 0x40
		ofs += StoreUtil.little32(out, 0);
		ofs += StoreUtil.little32(out, 0);
		ofs += StoreUtil.little32(out, 0);

		// Just a check
		if (ofs != EHSIZE_EXT) {
			throw new RuntimeException("Strange, invalid header size");
		}

		// Write the section bodies
		// Here again, skip section 0, since it is always empty
		for (i = 1; i < getNoSections(); i++) {
			Section s = getSection(i);
			if (s.getOffset() != ofs) {
				throw new RuntimeException(
					"Strange, offset mismatch in section-body (section="
						+ i
						+ ", ofs="
						+ ofs
						+ ", expected="
						+ s.getOffset());
			}
			ofs += s.storeBody(out);
			// align
			while ((ofs % ALIGN) != 0) {
				ofs += StoreUtil.little8(out, 0);
			}
			System.out.println("store section " + i + ", ofs=" + ofs + ", name=" + s.getName());
		}

		// Write the section headers
		for (i = 0; i < getNoSections(); i++) {
			ofs += getSection(i).store(out);
		}

		out.close();

		return (true);
	}

	public void print() {
		System.out.println("----- Elf Header -----");
		System.out.println("e_type        : " + Integer.toString(e_type, 16));
		System.out.println(
			"e_machine     : " + Integer.toString(e_machine, 16));
		System.out.println(
			"e_version     : " + Integer.toString(e_version, 16));
		System.out.println("e_entry       : " + Integer.toString(e_entry, 16));
		//		System.out.println( "e_phentsize   : " + Integer.toString( e_phentsize,    16));
		//	System.out.println( "e_phnum       : " + Integer.toString( e_phnum,        16));
		System.out.println("e_phoff       : " + Integer.toString(e_phoff, 16));
		System.out.println(
			"e_shnum       : " + Integer.toString(getNoSections(), 16));
		System.out.println("e_shoff       : " + Integer.toString(e_shoff, 16));
		System.out.println(
			"e_shstrndx    : " + Integer.toString(e_shstrndx, 16));
		System.out.println(" ----- BRK ----- ");
		//		System.out.println( " strtab = [" + strtab + "]");
	}

	public static void main(String args[]) throws Exception {
		Elf elf = newFromFile("test.o");
		elf.print();

		System.out.println("\nNow do the store");
		elf = newRelInstance();
		elf.store("test-elf.o");
		elf.print();

		//		System.out.println("\nNow try to read it back again");
		//		elf = newFromFile("test-elf.o");
		//		elf.print();
	}
}

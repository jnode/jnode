// ----------------------------------------
//  Section Information in Segment
//
//  Copyright (C) 1999  Kiyoka Nishiyama
//
//  $Date$ 
//  $Id$
// ----------------------------------------
package org.jnode.linker;

//
// ---- Section Header ---
//typedef struct
//{
//  Elf32_Word	sh_name;		/* Section name (string tbl index) */
//  Elf32_Word	sh_type;		/* Section type */
//  Elf32_Word	sh_flags;		/* Section flags */
//  Elf32_Addr	sh_addr;		/* Section virtual addr at execution */
//  Elf32_Off	sh_offset;		/* Section file offset */
//  Elf32_Word	sh_size;		/* Section size in bytes */
//  Elf32_Word	sh_link;		/* Link to another section */
//  Elf32_Word	sh_info;		/* Additional section information */
//  Elf32_Word	sh_addralign;		/* Section alignment */
//  Elf32_Word	sh_entsize;		/* Entry size if section holds table */
//} Elf32_Shdr;
//

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Vector;


public class Section {
	public static final int SHT_NULL = 0;
	public static final int SHT_PROGBITS = 1;
	public static final int SHT_SYMTAB = 2;
	public static final int SHT_STRTAB = 3;
	public static final int SHT_RELA = 4;
	public static final int SHT_HASH = 5;
	public static final int SHT_DYNAMIC = 6;
	public static final int SHT_NOTE = 7;
	public static final int SHT_NOBITS = 8;
	public static final int SHT_REL = 9;
	public static final int SHT_SHLIB = 10;
	public static final int SHT_DYNSYM = 11;

	public static final int SHF_WRITE = 0x1;
	public static final int SHF_ALLOC = 0x2;
	public static final int SHF_EXECINSTR = 0x4;
	public static final int SHF_MASKPROC = 0xf0000000;

	static String typename[] =
		{
			"NULL",
			"PROGBITS",
			"SYMTAB",
			"STRTAB",
			"RELA",
			"HASH",
			"DYNAMIC",
			"NOTE",
			"NOBITS",
			"REL",
			"SHLIB",
			"DYNSYM",
			"NUM" };
	int sh_name;
	int sh_type;
	int sh_flags;
	int sh_addr;
	int sh_offset;
	int sh_size;
	int sh_link;
	int sh_info;
	int sh_addralign;
	int sh_entsize;
	Elf elf;

	byte[] m_body;
	Vector m_symtab;
	//char[] m_strtab;
	Vector m_reltab;
	private StrTab strTab;

	private Section(Elf elf, int type, String name, int flags) {
		this.elf = elf;
		this.sh_type = type;
		this.sh_flags = flags;
		m_symtab = new Vector();
		m_reltab = new Vector();
		strTab = new StrTab();
		if (name != null) {
			Section shstr = elf.getSHStrSection();
			if (shstr == null) {
				shstr = this;
			}
			sh_name = shstr.addString(name);
			sh_addralign = 1;
		}
	}

	public static Section newNullInstance(Elf elf) {
		return new Section(elf, 0, null, 0);
	}

	public static Section newInstance(Elf elf, RandomAccessFile in)
		throws IOException {
		Section s = new Section(elf, 0, null, 0);
		s.load(in);
		return s;
	}

	public static Section newRelTabSection(
		Elf elf,
		Section symtabSection,
		Section contentSection) {
		Section s =
			new Section(
				elf,
				SHT_REL,
				".rel" + contentSection.getName(),
				SHF_ALLOC);
		s.sh_entsize = 0x08;
		s.sh_link = elf.getSectionIndex(symtabSection);
		s.sh_info = elf.getSectionIndex(contentSection);
		return s;
	}

	public static Section newStrTabSection(Elf elf) {
		return new Section(
			elf,
			SHT_STRTAB,
			(elf.getSHStrSection() == null) ? ".shstrtab" : ".strtab",
			SHF_ALLOC);
	}

	public static Section newSymTabSection(Elf elf) {
		Section s = new Section(elf, SHT_SYMTAB, ".symtab", SHF_ALLOC);
		s.m_symtab.addElement(new Symbol(elf, null, 0, null));
		// First entry must be NULL
		s.sh_entsize = 0x10;
		s.sh_link = elf.getSectionIndex(elf.getSectionByName(".strtab"));
		return s;
	}

	public static Section newTextSection(Elf elf) {
		return new Section(
			elf,
			SHT_PROGBITS,
			".text",
			SHF_ALLOC | SHF_EXECINSTR);
	}

	public static Section newDataSection(Elf elf) {
		return new Section(elf, SHT_PROGBITS, ".data", SHF_WRITE | SHF_ALLOC);
	}

	public static Section newBssSection(Elf elf) {
		return new Section(elf, SHT_NOBITS, ".bss", SHF_WRITE | SHF_ALLOC);
	}

	public String getName() {
		return elf.getSHString(sh_name);
	}
	public int getType() {
		return sh_type;
	}
	public String getTypeName() {
		return typename[sh_type];
	}
	public int getFlags() {
		return sh_flags;
	}
	public void setFlags(int v) {
		sh_flags = v;
	}
	public int getAddr() {
		return sh_addr;
	}
	public int getOffset() {
		return sh_offset;
	}
	public void setOffset(int ofs) {
		sh_offset = ofs;
	}
	public int getSize() {
		return sh_size;
	}
	public int getLink() {
		return sh_link;
	}
	public void setLink(int v) {
		sh_link = v;
	}
	public int getAlign() {
		return sh_addralign;
	}
	public void setAlign(int v) {
		sh_addralign = v;
	}
	public int getEntrySize() {
		return sh_entsize;
	}
	public byte[] getBody() {
		return m_body;
	}
	public void setBody(byte[] v) {
		setBody(v, 0, v.length);
	}
	public void setBody(byte[] v, int start, int length) {
		m_body = new byte[length];
		System.arraycopy(v, start, m_body, 0, length);
		sh_size = m_body.length;
	}
	//	public char[] getStrTab()				{ return m_strtab; }

	public boolean isBss() {
		return (sh_type == SHT_NOBITS);
	}
	public boolean isSymTab() {
		return (sh_type == SHT_SYMTAB);
	}
	public boolean isStrTab() {
		return (sh_type == SHT_STRTAB);
	}
	public boolean isRelTab() {
		return (sh_type == SHT_REL);
	}

	public int getNoRelocs() {
		if (!isRelTab())
			throw new RuntimeException("Only valid to reloc table sections");
		return m_reltab.size();
	}

	public Reloc getReloc(int index) {
		if (!isRelTab())
			throw new RuntimeException("Only valid to reloc table sections");
		return (Reloc) m_reltab.elementAt(index);
	}

	public void addAbsReloc(Symbol symbol, int address) {
		if (!isRelTab()) {
			throw new RuntimeException("Only valid to reloc table sections");
		}
		m_reltab.addElement(Reloc.newAbsInstance(elf, symbol, address));
	}

	public void addPcRelReloc(Symbol symbol, int address) {
		if (!isRelTab()) {
			throw new RuntimeException("Only valid to reloc table sections");
		}
		m_reltab.addElement(Reloc.newPcRelInstance(elf, symbol, address));
	}

	public int getNoSymbols() {
		if (!isSymTab())
			throw new RuntimeException("Only valid to symbol table sections");
		return m_symtab.size();
	}

	public Symbol getSymbol(int index) {
		if (!isSymTab()) {
			throw new RuntimeException("Only valid to symbol table sections");
		}
		return (Symbol) m_symtab.elementAt(index);
	}

	public Symbol getSymbolByAddress(int address) {
		if (!isSymTab())
			throw new RuntimeException("Only valid to symbol table sections");
		int i;
		for (i = 1; i < getNoSymbols(); i++) {
			Symbol s = getSymbol(i);
			if (address == s.getValue());
			{
				return s;
			}
		}
		return null;
	}

	public void addSymbol(Symbol s) {
		m_symtab.addElement(s);
	}

	public int getIndexOfSymbol(Symbol symbol) {
		return m_symtab.indexOf(symbol);
	}

	public String getString(int addr) {
		return strTab.getString(addr);
	}

	/**
	 * Add a string and return its index
	 * @param v
	 */
	public synchronized int addString(String v) {
		if (!isStrTab()) {
			throw new RuntimeException("Only valid for StrTab sections");
		}
		return strTab.addString(v);
	}

	/**
	 * Return the index of a given string, or -1 if not found
	 */
	public synchronized int findString(String v) {
		return strTab.findString(v);
	}

	public void print() {
		System.out.println("  ----- Section Header -----");
		System.out.println(
			"  sh_name       : "
				+ Integer.toString(sh_name, 16)
				+ "("
				+ getName()
				+ ")");
		System.out.println(
			"  sh_type       : "
				+ Integer.toString(sh_type, 16)
				+ "("
				+ typename[sh_type]
				+ ")");
		System.out.println(
			"  sh_flags      : " + Integer.toString(sh_flags, 16));
		System.out.println(
			"  sh_addr       : " + Integer.toString(sh_addr, 16));
		System.out.println(
			"  sh_offset     : " + Integer.toString(sh_offset, 16));
		System.out.println(
			"  sh_size       : " + Integer.toString(sh_size, 16));
		System.out.println(
			"  sh_link       : " + Integer.toString(sh_link, 16));
		System.out.println(
			"  sh_info       : " + Integer.toString(sh_info, 16));
		System.out.println(
			"  sh_addralign  : " + Integer.toString(sh_addralign, 16));
		System.out.println(
			"  sh_entsize    : " + Integer.toString(sh_entsize, 16));
	}

	protected int get_brk() {
		return (sh_addr + sh_size);
	}

	private void load(RandomAccessFile in) throws IOException {
		sh_name = LoadUtil.little32(in);
		sh_type = LoadUtil.little32(in);
		sh_flags = LoadUtil.little32(in);
		sh_addr = LoadUtil.little32(in);
		sh_offset = LoadUtil.little32(in);
		sh_size = LoadUtil.little32(in);
		sh_link = LoadUtil.little32(in);
		sh_info = LoadUtil.little32(in);
		sh_addralign = LoadUtil.little32(in);
		sh_entsize = LoadUtil.little32(in);
	}

	protected int store(OutputStream out) throws IOException {
		int cnt = 0;
		cnt += StoreUtil.little32(out, sh_name);
		cnt += StoreUtil.little32(out, sh_type);
		cnt += StoreUtil.little32(out, sh_flags);
		cnt += StoreUtil.little32(out, sh_addr);
		cnt += StoreUtil.little32(out, sh_offset);
		cnt += StoreUtil.little32(out, sh_size);
		cnt += StoreUtil.little32(out, sh_link);
		cnt += StoreUtil.little32(out, sh_info);
		cnt += StoreUtil.little32(out, sh_addralign);
		cnt += StoreUtil.little32(out, sh_entsize);

		return cnt;
	}

	private void loadSymTab() throws IOException {
		if (!isSymTab())
			throw new RuntimeException("Only valid for symbol table sections");

		ByteArrayInputStream in = new ByteArrayInputStream(m_body);
		int cnt = (sh_size == 0) ? 0 : (sh_size / sh_entsize);
		m_symtab = new Vector();
		for (int i = 0; i < cnt; i++) {
			m_symtab.addElement(new Symbol(elf, in));
		}
	}

	private void storeSymTab() throws IOException {
		if (!isSymTab()) {
			throw new RuntimeException("Only valid for symbol table sections");
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		sh_size = sh_entsize * getNoSymbols();
		for (int i = 0; i < getNoSymbols(); i++) {
			getSymbol(i).store(out);
		}
		m_body = out.toByteArray();
	}

	private void loadRelTab() throws IOException {
		if (!isRelTab()) {
			throw new RuntimeException("Only valid for relocation table sections");
		}

		ByteArrayInputStream in = new ByteArrayInputStream(m_body);
		int cnt = (sh_size == 0) ? 0 : (sh_size / sh_entsize);
		m_reltab = new Vector();
		for (int i = 0; i < cnt; i++) {
			m_reltab.addElement(new Reloc(elf, in));
		}
	}

	private void storeRelTab() throws IOException {
		if (!isRelTab())
			throw new RuntimeException("Only valid for relocation table sections");

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		sh_size = sh_entsize * getNoRelocs();
		for (int i = 0; i < getNoRelocs(); i++) {
			getReloc(i).store(out);
		}
		m_body = out.toByteArray();
	}

	private void loadStrTab() throws IOException {
		if (!isStrTab())
			throw new RuntimeException("Only valid for string table sections");

		strTab = new StrTab(m_body, sh_size);
	}

	private void storeStrTab() throws IOException {
		if (!isStrTab()) {
			throw new RuntimeException("Only valid for string table sections");
		}

		m_body = strTab.toByteArray();
		sh_size = m_body.length;
	}

	protected byte[] loadBody(RandomAccessFile in) throws IOException {
		if (m_body == null) {
			m_body = new byte[sh_size];
			in.seek(sh_offset);
			in.read(m_body);

			if (isSymTab()) {
				loadSymTab();
			} else if (isStrTab()) {
				loadStrTab();
			} else if (isRelTab()) {
				loadRelTab();
			}
		}
		return m_body;
	}

	protected int storeBody(OutputStream out) throws IOException {
		return (m_body != null) ? StoreUtil.bytes(out, m_body) : 0;
	}

	protected int prepareStoreBody() throws IOException {
		if (isSymTab()) {
			storeSymTab();
		} else if (isStrTab()) {
			storeStrTab();
		} else if (isRelTab()) {
			storeRelTab();
		}

		return (m_body != null) ? m_body.length : 0;
	}
}

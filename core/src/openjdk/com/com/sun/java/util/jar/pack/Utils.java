/*
 * Copyright 2003-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.java.util.jar.pack;

import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.util.logging.*;
import java.io.*;

class Utils {
    static final String COM_PREFIX = "com.sun.java.util.jar.pack.";
    static final String METAINF    = "META-INF";

    /*
     * Outputs various diagnostic support information.
     * If >0, print summary comments (e.g., constant pool info).
     * If >1, print unit comments (e.g., processing of classes).
     * If >2, print many comments (e.g., processing of members).
     * If >3, print tons of comments (e.g., processing of references).
     * (installer only)
     */
    static final String DEBUG_VERBOSE = Utils.COM_PREFIX+"verbose";

    /*
     * Disables use of native code, prefers the Java-coded implementation.
     * (installer only)
     */
    static final String DEBUG_DISABLE_NATIVE = COM_PREFIX+"disable.native";

    /*
     * Use the default working TimeZone instead of UTC.
     * Note: This has installer unpacker implications.
     * see: zip.cpp which uses gmtime vs. localtime.
     */
    static final String PACK_DEFAULT_TIMEZONE = COM_PREFIX+"default.timezone";

    /*
     * Property indicating that the unpacker should
     * ignore the transmitted PACK_MODIFICATION_TIME,
     * replacing it by the given value. The value can
     * be a numeric string, representing the number of 
     * mSecs since the epoch (UTC), or the special string
     * {@link #NOW}, meaning the current time (UTC).
     * The default value is the special string {@link #KEEP},
     * which asks the unpacker to preserve all transmitted
     * modification time information.
     * (installer only)
     */
    static final String UNPACK_MODIFICATION_TIME = COM_PREFIX+"unpack.modification.time";

    /*
     * Property indicating that the unpacker strip the
     * Debug Attributes, if they are present, in the pack stream.
     * The default value is false.
     * (installer only)
     */
    static final String UNPACK_STRIP_DEBUG = COM_PREFIX+"unpack.strip.debug";

    /*
     * Remove the input file after unpacking.
     * (installer only)
     */
    static final String UNPACK_REMOVE_PACKFILE = COM_PREFIX+"unpack.remove.packfile";

    /*
     * A possible value for MODIFICATION_TIME
     */
    static final String NOW 				= "now";
    // Other debug options:
    //   com...debug.bands=false      add band IDs to pack file, to verify sync
    //   com...dump.bands=false       dump band contents to local disk
    //   com...no.vary.codings=false  turn off coding variation heuristics
    //   com...no.big.strings=false   turn off "big string" feature

    /*
     * If this property is set to {@link #TRUE}, the packer will preserve
     * the ordering of class files of the original jar in the output archive.
     * The ordering is preserved only for class-files; resource files
     * may be reordered.
     * <p>
     * If the packer is allowed to reorder class files, it can marginally
     * decrease the transmitted size of the archive.
     */
    static final String PACK_KEEP_CLASS_ORDER = COM_PREFIX+"keep.class.order";
    /*
     * This string PACK200 is given as a zip comment on all JAR files
     * produced by this utility.
     */
    static final String PACK_ZIP_ARCHIVE_MARKER_COMMENT = "PACK200";

   // Keep a TLS point to the current Packer or Unpacker.
   // This makes it simpler to supply environmental options
    // to the engine code, especially the native code.
    static final ThreadLocal currentInstance = new ThreadLocal();

    static PropMap currentPropMap() {
	Object obj = currentInstance.get();
	if (obj instanceof PackerImpl)
	    return ((PackerImpl)obj)._props;
	if (obj instanceof UnpackerImpl)
	    return ((UnpackerImpl)obj)._props;
	return null;
    }

    static final boolean nolog
	= Boolean.getBoolean(Utils.COM_PREFIX+"nolog");


    static final Logger log
	= new Logger("java.util.jar.Pack200", null) {
	    public void log(LogRecord record) {
		int verbose = currentPropMap().getInteger(DEBUG_VERBOSE);
		if (verbose > 0) {
		    if (nolog &&
			record.getLevel().intValue() < Level.WARNING.intValue()) {
			System.out.println(record.getMessage());
		    } else {
			super.log(record);
		    }
		}
	    }

	    public void fine(String msg) {
		int verbose = currentPropMap().getInteger(DEBUG_VERBOSE);
		if (verbose > 0) {
			System.out.println(msg);
		}
	    }
	};
    static {
	LogManager.getLogManager().addLogger(log);
    }

    // Returns the Max Version String of this implementation
    static String getVersionString() {
	return "Pack200, Vendor: Sun Microsystems, Version: " + 
	    Constants.JAVA6_PACKAGE_MAJOR_VERSION + "." + 
	    Constants.JAVA6_PACKAGE_MINOR_VERSION;	
    }

    static void markJarFile(JarOutputStream out) throws IOException {
	out.setComment(PACK_ZIP_ARCHIVE_MARKER_COMMENT);
    }

    // -0 mode helper
    static void copyJarFile(JarInputStream in, JarOutputStream out) throws IOException {
	if (in.getManifest() != null) {
	    ZipEntry me = new ZipEntry(JarFile.MANIFEST_NAME);
	    out.putNextEntry(me);
	    in.getManifest().write(out);
	    out.closeEntry();
	}
	byte[] buffer = new byte[1 << 14];
	for (JarEntry je; (je = in.getNextJarEntry()) != null; ) {
	    out.putNextEntry(je);
	    for (int nr; 0 < (nr = in.read(buffer)); ) {
		out.write(buffer, 0, nr);
	    }
	}
	in.close();
	markJarFile(out);  // add PACK200 comment
    }
    static void copyJarFile(JarFile in, JarOutputStream out) throws IOException {
	byte[] buffer = new byte[1 << 14];
	for (Enumeration e = in.entries(); e.hasMoreElements(); ) {
	    JarEntry je = (JarEntry) e.nextElement();
	    out.putNextEntry(je);
	    InputStream ein = in.getInputStream(je);
	    for (int nr; 0 < (nr = ein.read(buffer)); ) {
		out.write(buffer, 0, nr);
	    }
	}
	in.close();
	markJarFile(out);  // add PACK200 comment
    }
    static void copyJarFile(JarInputStream in, OutputStream out) throws IOException {
	// 4947205 : Peformance is slow when using pack-effort=0
	out = new BufferedOutputStream(out); 
	out = new NonCloser(out); // protect from JarOutputStream.close()
	JarOutputStream jout = new JarOutputStream(out);
	copyJarFile(in, jout);
	jout.close();
    }
    static void copyJarFile(JarFile in, OutputStream out) throws IOException {

	// 4947205 : Peformance is slow when using pack-effort=0
	out = new BufferedOutputStream(out); 
	out = new NonCloser(out); // protect from JarOutputStream.close()
	JarOutputStream jout = new JarOutputStream(out);
	copyJarFile(in, jout);
	jout.close();
    }
	// Wrapper to prevent closing of client-supplied stream.
    static private
    class NonCloser extends FilterOutputStream {
	NonCloser(OutputStream out) { super(out); }
	public void close() throws IOException { flush(); }
    }
   static String getJarEntryName(String name) {
	if (name == null)  return null;
	return name.replace(File.separatorChar, '/');
    }

    static String zeString(ZipEntry ze) {
	int store = (ze.getCompressedSize() > 0) ? 
	    (int)( (1.0 - ((double)ze.getCompressedSize()/(double)ze.getSize()))*100 )
	    : 0 ;
	// Follow unzip -lv output
	return (long)ze.getSize() + "\t" + ze.getMethod() 
	    + "\t" + ze.getCompressedSize() + "\t" 
	    + store + "%\t" 
	    + new Date(ze.getTime()) + "\t" 
	    + Long.toHexString(ze.getCrc()) + "\t" 
	    + ze.getName() ;
    }



    static byte[] readMagic(BufferedInputStream in) throws IOException {
	in.mark(4);
	byte[] magic = new byte[4];
	for (int i = 0; i < magic.length; i++) {
	    // read 1 byte at a time, so we always get 4
	    if (1 != in.read(magic, i, 1))
		break;
	}
	in.reset();
	return magic;
    }

    // magic number recognizers
    static boolean isJarMagic(byte[] magic) {
	return (magic[0] == (byte)'P' &&
		magic[1] == (byte)'K' &&
		magic[2] >= 1 &&
		magic[2] <  8 &&
		magic[3] == magic[2] + 1);
    }
    static boolean isPackMagic(byte[] magic) {
	return (magic[0] == (byte)0xCA &&
		magic[1] == (byte)0xFE &&
		magic[2] == (byte)0xD0 &&
		magic[3] == (byte)0x0D);
    }
    static boolean isGZIPMagic(byte[] magic) {
	return (magic[0] == (byte)0x1F &&
		magic[1] == (byte)0x8B &&
		magic[2] == (byte)0x08);
	// fourth byte is variable "flg" field
    }

    private Utils() { } // do not instantiate
}

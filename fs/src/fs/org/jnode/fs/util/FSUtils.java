/**
 * $Id$
 */
package org.jnode.fs.util;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;

/**
 * 
 * @author Fabien DUMINY
 */
public class FSUtils {
	static public String toString(FSEntry entry, boolean deep)
	{
		if(entry == null)
			return "<FSEntry>NULL</FSEntry>";
		
		StringBuffer sb = new StringBuffer(); 
		sb.append("<FSEntry>");
		sb.append(" name="+entry.getName());
		try {
			sb.append(toStringDate(" lastModified=", entry.getLastModified()));
		} catch (IOException e) {
			sb.append(" lastModified=###"+e.getMessage()+"###");
			log.error("error in lastModified", e);
		}
		try {
			sb.append(" isDirty="+entry.isDirty());
		} catch (IOException e1) {
			sb.append(" isDirty=###"+e1.getMessage()+"###");
			log.error("error in isDirty", e1);
		}
		sb.append(" isValid="+entry.isValid());
		
		sb.append(" isFile="+entry.isFile());
		if(deep && entry.isFile())
			try {
				sb.append(toString(entry.getFile()));
			} catch (IOException e2) {
				sb.append(" getFile=###"+e2.getMessage()+"###");
				log.error("error in getFile", e2);
			}			

		sb.append(" isDir="+entry.isDirectory());		
		if(deep && entry.isDirectory())
			try {
				sb.append(toString(entry.getDirectory()));
			} catch (IOException e3) {
				sb.append(" getDirectory=###"+e3.getMessage()+"###");
				log.error("error in getDirectory", e3);
			}
		sb.append("</FSEntry>");
		
		return sb.toString();
	}

	static public String toString(FSDirectory dir) throws IOException
	{
		return toString(dir, false);
	}

	static public String toString(FSDirectory dir, boolean deep) throws IOException
	{
		if(dir == null)
			return "<FSDirectory>NULL</FSDirectory>";
		
		String str = "<FSDirectory>isValid="+dir.isValid()+"</FSDirectory>";
		if(deep)
			str += "\n" + dir.toString(); // also print entry table
		
		return str;
	}
	
	static public String toString(FSFile file)
	{		
		if(file == null)
			return "<FSEntry>NULL</FSEntry>";
		
		StringBuffer sb = new StringBuffer();
		sb.append("<FSFile>");
		sb.append(" isValid"+file.isValid());
		sb.append(" length"+file.getLength());

		sb.append("</FSFile>");
		
		return sb.toString();
	}
	
	static public String toStringDate(String str, long date)
	{
		return toString(str, new Date(date));
	}
	
	static public String toString(String str, Date date)
	{
		return str + dateFormat.format(date);
	}
	static public String toString(byte[] data)
	{
		return toString(data, 0, data.length);
	}
	
	static public String toString(byte[] data, int offset, int length)
	{
		StringBuffer sb = new StringBuffer();
		StringBuffer chars = new StringBuffer(LINE_SIZE);
		
		int l = Math.min(Math.min(length - offset, data.length - offset),
				 MAX_DUMP_SIZE);
		int mod = l % LINE_SIZE;
		if(mod != 0) l += LINE_SIZE - mod;
		
		for(int i = 0 ; i < l ; i++)
		{
			if((i % 16) == 0)
			{
				sb.append(lpad(Integer.toHexString(i), 4)).append(" - ");
				chars.setLength(0); // empty
			}
			
			int idx = offset + i;
			boolean end = (idx >= data.length); 
			if(!end)
			{
				sb.append(lpad(Integer.toHexString(data[idx]),2)).append(" ");
				chars.append((char) data[idx]);
			}
			
			if(((i % 16) == 15) || end) 
			{
				sb.append("   ").append(chars.toString()).append('\n');
			}
		}
		
		return sb.toString();
	}
	
	static public String lpad(String str, int size)
	{
		if(str.length() >= size)
			return str;

		String pad = "";
		int nbBlanks = size - str.length();
		for(int i = 0 ; i < nbBlanks ; i++ )
			pad += " ";
		
		return pad + str;
	}
	
	static public String toStringAsChars(byte[] data, int offset, int length)
	{
		StringBuffer sb = new StringBuffer();
		int l = Math.min(offset + length, data.length);
		for(int i = offset ; i < l ; i++)
		{
			sb.append((char) data[i]);
		}
		
		return sb.toString();
	}
	
	
	static private final int MAX_DUMP_SIZE = 256;
	static private final int LINE_SIZE = 16;
	static protected DateFormat dateFormat = new SimpleDateFormat();
	static private final Logger log = Logger.getLogger(FSUtils.class);	
}

package org.jnode.apps.vmware.disk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.descriptor.AdapterType;
import org.jnode.apps.vmware.disk.handler.ExtentFactory;
import org.jnode.apps.vmware.disk.handler.FileDescriptor;
import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;
import org.jnode.apps.vmware.disk.handler.simple.SimpleExtentFactory;
import org.jnode.apps.vmware.disk.handler.sparse.SparseExtentFactory;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class IOUtils {
	private static final Logger LOG = Logger.getLogger(SparseExtentFactory.class);
		
	private static final String COMMENT     = "#";
	private static final String EQUAL       = "=";
	
	private final static ExtentFactory[] FACTORIES =
	{
		new SparseExtentFactory(),
		new SimpleExtentFactory(),
	};
	
	public static class KeyValue
	{
		private String key;
		private String value;
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
		
	public static String readLine(BufferedReader reader) throws IOException
	{
		String line = null;
		while((line = reader.readLine()) != null)
		{
			LOG.debug("line="+line);
			
			line = line.trim();
			if(!line.isEmpty() && 
			   !line.startsWith(COMMENT))
			{
				return line;
			}			
		}
		
		//throw new IOException("no more lines");
		return null;
	}
	
	public static String removeQuotes(String value)
	{
		// remove enclosing '"'
		return (value == null) ? null : value.substring(1, value.length() - 1);
	}

	public static KeyValue readValue(BufferedReader reader, KeyValue keyValue,
									String wantedKey, boolean removeQuotes) 
						throws IOException
	{
		keyValue = readValue(readLine(reader), keyValue, wantedKey);
		
		if(wantedKey != null)
		{
			while(keyValue.getValue() == null)
			{
				keyValue = readValue(readLine(reader), keyValue, wantedKey);
			}
		}

		keyValue.setValue(removeQuotes ? removeQuotes(keyValue.getValue()) : keyValue.getValue());
		
		return keyValue; 
	}

	private static KeyValue readValue(String line, KeyValue keyValue, 
									String wantedKey) 
				throws IOException
	{
		keyValue = (keyValue == null) ? new KeyValue() : keyValue;

		keyValue.setKey(null);
		keyValue.setValue(null);
		
		if(line == null)
		{
			return keyValue;
		}
		
		int idx = line.indexOf(EQUAL);
		if(idx < 0)
		{
			LOG.debug("err2: tried to read key "+wantedKey+", line="+line);
			return keyValue;
		}
		
		keyValue.setKey(line.substring(0, idx).trim());
		keyValue.setValue(line.substring(idx + 1).trim());
		
		if((wantedKey != null) && !keyValue.getKey().equals(wantedKey))
		{
			throw new IOException("excepted key("+wantedKey+") not found (actual:"+keyValue.getKey()+")");
		}
		
		return keyValue;
	}
	
	public static FileDescriptor readFileDescriptor(File file, boolean isMain) 
						throws IOException, UnsupportedFormatException
	{
		FileDescriptor fileDescriptor = null;
		
		for(ExtentFactory f : FACTORIES)
		{
			try {
				LOG.debug("trying with factory "+f.getClass().getName());
				FileDescriptor fd = f.createFileDescriptor(file, isMain);
				
				// we have found the factory for that format
				fileDescriptor = fd;
				
				break;				
			} catch (UnsupportedFormatException e) {
				// ignore, we will try with the next factory
				LOG.debug(f.getClass().getName()+":"+file+" not supported "+e.getMessage());
			}
		}

		if(fileDescriptor == null)
		{
			throw new UnsupportedFormatException("format not supported for file "+file);
		}
		
		return fileDescriptor;
	}	
	
	public static File getExtentFile(File mainFile, String extentFileName) {
		String path = mainFile.getParentFile().getAbsolutePath(); 
		return new File(path, extentFileName);
	}

	public static Map<String, String> readValuesMap(String lastLine,
											BufferedReader br, 
											boolean removeQuotes,
											String... requiredKeys) throws IOException 
	{
		Map<String, String> values = new HashMap<String, String>();

		KeyValue keyValue = IOUtils.readValue(lastLine, null, null);
		if(keyValue.getValue() == null)
		{
			keyValue = IOUtils.readValue(br, keyValue, null, removeQuotes);
		}
		values.put(keyValue.getKey(), keyValue.getValue());
		
		while((keyValue = IOUtils.readValue(br, keyValue, null, removeQuotes)).getValue() != null)
		{
			values.put(keyValue.getKey(), keyValue.getValue());	
		}
		
		// check required keys
		boolean error = false; 
		StringBuilder sb = new StringBuilder("required keys not found : "); 
		for(String reqKey : requiredKeys)
		{
			if(!values.keySet().contains(reqKey))
			{
				error = true;
				sb.append(reqKey).append(',');
			}
		}
		if(error)
		{
			throw new IOException(sb.toString());
		}
		
		return values;
	}
	
	public static boolean equals(Object o1, Object o2)
	{
		return (o1 == null) ? (o2 == null) : o1.equals(o2);
	}
}

package org.jnode.net.command;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.URLArgument;

public class WgetCommand extends AbstractCommand {

	private URLArgument ARG_SOURCE = new URLArgument("url", Argument.MANDATORY,"source URL.");
	
	InputStream is;
	BufferedOutputStream bos;
	
	public WgetCommand(){
		super("Download content of URL passed as parameter.");
		registerArguments(ARG_SOURCE);
	}
	
	public static void main(String[] args) throws Exception {
        new WgetCommand().execute(args);
    }
	
	@Override
	public void execute(CommandLine commandLine, InputStream in,
			PrintStream out, PrintStream err) throws Exception {
		try {
			URL url = ARG_SOURCE.getValue();
			String filename = getLocalFileName(url);
			out.println("Get file " + filename + " from url " + url.toString());
			get(url,filename);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(is != null){
				is.close();
			}
			if(bos != null){
				bos.close();
			}
		}
	}
	
	/**
	 * Extract file name from url to save it locally.
	 * 
	 * @param url
	 * @return
	 * 
	 * @throws Exception
	 */
	protected String getLocalFileName(URL url) throws Exception {
		String address = url.toString();
		int lastSlashIndex = address.lastIndexOf('/');
		if (lastSlashIndex >= 0 &&
		    lastSlashIndex < address.length() - 1) {
			return address.substring(lastSlashIndex + 1);
		} else {
			throw new Exception("Could not figure out local file name for " + address);
		}
	}
	
	/**
	 * 
	 * @param url
	 * @param localFileName
	 * 
	 * @throws IOException
	 */
	protected void get(URL url, String localFileName) throws IOException {
		is = url.openStream();
		bos = new BufferedOutputStream(new FileOutputStream(localFileName));
		byte[] buffer = new byte[1024];
		int numRead;
		long numWritten = 0;
		while ((numRead = is.read(buffer)) != -1) {
			bos.write(buffer, 0, numRead);
			numWritten += numRead;
		}
		is.close();
	}

}

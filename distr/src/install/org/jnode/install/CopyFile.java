/*
 * $Id$
 */
package org.jnode.install;

import java.io.*;


/**
 * @author Levente S\u00e1ntha
 */
public class CopyFile implements ProgressAware {
    private ProgressSupport progress = new ProgressSupport();
    private boolean progessAware;
    private File source;
    private File destination;

    public CopyFile(boolean progessAware) {
        this.progessAware = progessAware;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public void setDestination(File destination) {
        this.destination = destination;
    }

    public void execute() throws Exception {
        try {
            if(source == null ||destination == null)
                throw new RuntimeException("Source or destination is null.");

            long length = source.length();
            byte[] buf = new byte[128 * 1024];
            FileInputStream fis = new FileInputStream(source);
            FileOutputStream fos = new FileOutputStream(destination);
            int count;
            long status = 0;
            while((count = fis.read(buf)) > -1 ){
                fos.write(buf, 0, count);
                if(progessAware){
                    status += count;
                    int val = (int)(100L * status / length);
                    progress.fireProgressEvent(new ProgressEvent(val));
                }
            }
            fis.close();
            fos.flush();
            fos.close();
        } catch(FileNotFoundException x){
            x.printStackTrace();
        } catch(IOException x){
            x.printStackTrace();
        }
    }

    public void addProgressListener(ProgressListener p) {
        progress.addProgressListener(p);
    }
}

package org.jnode.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jnode.work.Work;
import org.jnode.work.WorkUtils;


/**
 * This is a special implementation of {@link Plugin} for plugins that want 
 * to automatically unzip their content to a given directory 
 * while they are started.
 *  
 * @author fabien
 *
 */
public class AutoUnzipPlugin extends Plugin {
    private boolean startFinished = false;
    
    public AutoUnzipPlugin(PluginDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    protected void startPlugin() throws PluginException {
        startFinished = false;
        WorkUtils.add(new Work("unzip plugin " + getDescriptor().getId()) {
            @Override
            public void execute() {
                copyResources();
            }
        });
    }
    
    public boolean isStartFinished() {
        return startFinished;
    }

    @Override
    protected void stopPlugin() throws PluginException {
        // do nothing for now
        // TODO should we remove the copied files ?
    }
    
    private void copyResources() {
        try {            
            //FIXME shouldn't be hard coded but use java.home system property instead
            final File jnodeHome = new File("/jnode");
            
            // wait the jnode home directory is created
            while (!jnodeHome.exists()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            // create the plugin root directory
            final File pluginRootFile = new File(jnodeHome, getDescriptor().getId());
            pluginRootFile.mkdir();
   
            // copy each plugin's resource to the plugin root directory 
            final PluginDescriptor desc = AutoUnzipPlugin.this.getDescriptor();
            final PluginClassLoader cl = (PluginClassLoader) desc.getPluginClassLoader();
            final String pluginRoot = pluginRootFile.getAbsolutePath() + "/";
            final byte[] buffer = new byte[10240];
            
            for (String resName : cl.getResources()) {
                InputStream input = cl.getResourceAsStream(resName);
                
                try {
                    copy(input, pluginRoot, resName, buffer);
                } catch (SecurityException e) {
                    e.printStackTrace();
                    break;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        } finally {
            startFinished = true;                
        }        
    }
    
    private void copy(InputStream input, String pluginRoot, String name, byte[] buffer) 
        throws SecurityException, IOException {
        File output = new File(pluginRoot + name);
        output.getParentFile().mkdirs();
        
        FileOutputStream fos = null; 
        try {
            fos = new FileOutputStream(output);
            int nbRead;
            
            while ((nbRead = input.read(buffer)) > 0) {
                fos.write(buffer, 0, nbRead);
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
            
            if (input != null) {
                input.close();
            }
        }
    }
}

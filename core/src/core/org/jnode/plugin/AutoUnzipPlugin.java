/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * This is a special implementation of {@link Plugin} for plugins that want
 * to automatically unzip their content to a given directory
 * while they are started.
 *
 * @author fabien
 */
public class AutoUnzipPlugin extends Plugin {
    private boolean startFinished = false;

    public AutoUnzipPlugin(PluginDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    protected void startPlugin() throws PluginException {
        startFinished = false;
        (new Thread() {
            @Override
            public void run() {
                copyResources();
            }
        }).start();
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
        System.out.println("auto unzipping plugin " + getDescriptor().getId());

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
            final ClassLoader cl = desc.getPluginClassLoader();
            final String pluginRoot = pluginRootFile.getAbsolutePath() + '/';
            final byte[] buffer = new byte[10240];

            if (!(cl instanceof PluginClassLoader)) {
                System.err.println("Plugin's ClassLoader doesn't implements PluginClassLoader");
                return;
            }

            for (String resName : ((PluginClassLoader) cl).getResources()) {
                final InputStream input = cl.getResourceAsStream(resName);

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
            System.out.print("plugin " + getDescriptor().getId());
            System.out.println(" has been unzipped to " + pluginRootFile.getAbsolutePath());
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

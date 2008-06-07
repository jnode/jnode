package org.jnode.fs.jifs.files;

import java.nio.ByteBuffer;

import javax.naming.NameNotFoundException;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.jifs.JIFSFile;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginManager;
import org.jnode.plugin.model.PluginDescriptorModel;

/**
 * @author Levente S\u00e1ntha
 */
public class JIFSFpluginJar extends JIFSFile {
    private String id;
    protected ByteBuffer buffer;

    public JIFSFpluginJar() {
        return;
    }

    public JIFSFpluginJar(String id, FSDirectory parent) {
        super(id + ".jar", parent);
        this.id = id;
        refresh();
    }

    public void refresh() {
        try {
            final PluginManager mgr = InitialNaming.lookup(PluginManager.NAME);
            PluginDescriptorModel pdm =
                    (PluginDescriptorModel) mgr.getRegistry().getPluginDescriptor(id);
            if (pdm != null) {

                buffer = pdm.getJarFile().getBuffer();
                isvalid = buffer != null;
            } else {
                isvalid = false;
            }
        } catch (NameNotFoundException e) {
            System.err.println(e);
        }
    }

    public void read(long fileOffset, ByteBuffer destBuf) {
        refresh();
        if (buffer == null) {
            return;
        }
        ByteBuffer buf = buffer;
        buf.position((int) fileOffset);
        buf = (ByteBuffer) buf.slice().limit(destBuf.remaining());
        destBuf.put(buf);
    }

    public long getLength() {
        refresh();
        if (buffer == null) {
            return 0;
        }
        ByteBuffer buf = buffer;
        buf.position(0);
        return buf.remaining();
    }
}

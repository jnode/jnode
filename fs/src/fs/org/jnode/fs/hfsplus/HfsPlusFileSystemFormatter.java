package org.jnode.fs.hfsplus;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.Formatter;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;

public class HfsPlusFileSystemFormatter extends Formatter<HfsPlusFileSystem> {
    
    private HFSPlusParams params;
    
    public HfsPlusFileSystemFormatter(HFSPlusParams params) {
        super(new HfsPlusFileSystemType());
        this.params = params;
    }

    @Override
    public final HfsPlusFileSystem format(final Device device) throws FileSystemException {
        try {
            FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);
            HfsPlusFileSystemType type = fss.getFileSystemType(HfsPlusFileSystemType.ID);
            HfsPlusFileSystem fs = type.create(device, false);
            fs.create(params);
            return fs;
        } catch (NameNotFoundException e) {
            throw new FileSystemException(e);
        }
    }

}

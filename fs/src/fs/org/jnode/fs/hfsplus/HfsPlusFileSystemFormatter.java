package org.jnode.fs.hfsplus;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.Formatter;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;

public class HfsPlusFileSystemFormatter extends Formatter<HfsPlusFileSystem> {

    protected HfsPlusFileSystemFormatter() {
        super(new HfsPlusFileSystemType());
    }

    @Override
    public final HfsPlusFileSystem format(final Device device) throws FileSystemException {
        try {
            FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);
            HfsPlusFileSystemType type = fss.getFileSystemType(HfsPlusFileSystemType.ID);
            HfsPlusFileSystem fs = type.create(device, false);
            fs.create(HfsPlusConstants.OPTIMAL_BLOCK_SIZE);
            return fs;
        } catch (NameNotFoundException e) {
            throw new FileSystemException(e);
        }
    }

}

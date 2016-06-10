package org.jnode.fs.hfsplus.compression;

import org.jnode.fs.hfsplus.HfsPlusFile;
import org.jnode.fs.hfsplus.attributes.AttributeData;

/**
 * A factory interface for creating HFS+ compression algorithm instances.
 *
 * @author Luke Quinane
 */
public interface HfsPlusCompressionFactory {
    /**
     * Creates a new compression algorithm instance.
     *
     * @param file the associated file.
     * @param attributeData the attribute data.
     * @param decmpfsDiskHeader the header for the compressed data.
     * @return the instance.
     */
    HfsPlusCompression createDecompressor(HfsPlusFile file, AttributeData attributeData,
                                          DecmpfsDiskHeader decmpfsDiskHeader);
}

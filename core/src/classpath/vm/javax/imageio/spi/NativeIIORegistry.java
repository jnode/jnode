package javax.imageio.spi;

import org.jnode.imageio.jpeg.JPEGImageReaderSpi;

/**
 *
 */
class NativeIIORegistry {
    private static Object createJPEGImageReaderSpi() {
        return new JPEGImageReaderSpi();
    }
}

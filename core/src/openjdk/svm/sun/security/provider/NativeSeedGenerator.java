package sun.security.provider;

import java.io.IOException;

/**
 *
 */
public class NativeSeedGenerator extends SeedGenerator {
    public NativeSeedGenerator() throws IOException {
        throw new IOException("No native seed generator.");
    }

    @Override
    byte getSeedByte() {
        return 0;
    }
}

/*
 * $
 */
package org.jnode.awt;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.jnode.awt.image.JNodeBufferedImageGraphics;
import org.jnode.awt.image.BufferedImageGraphics2D;

/**
 * @author Levente S\u00e1ntha
 */
public abstract class GraphicsFactory {
    public static GraphicsFactory instance;

    public static GraphicsFactory getInstance() {
        if (instance == null) {
            instance = new NewGraphicsFactory();
        }
        return instance;
    }

    public abstract Graphics2D createGraphics(BufferedImage image);

    public abstract Graphics2D createGraphics(JNodeGenericPeer<?, ?> peer);

    private static class OldGraphicsFactory extends GraphicsFactory {
        public Graphics2D createGraphics(BufferedImage image) {
            return new JNodeBufferedImageGraphics(image);
        }

        public Graphics2D createGraphics(JNodeGenericPeer<?, ?> peer) {
            return new JNodeGraphics(peer);
        }

    }

    private static class NewGraphicsFactory extends GraphicsFactory {
        public Graphics2D createGraphics(BufferedImage image) {
            return new BufferedImageGraphics2D(image);
        }

        public Graphics2D createGraphics(JNodeGenericPeer<?, ?> peer) {
            return new JNodeSurfaceGraphics2D(peer);
        }
    }
}

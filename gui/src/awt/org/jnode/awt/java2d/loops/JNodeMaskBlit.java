/*
 * $
 */
package org.jnode.awt.java2d.loops;

import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.Blit;
import sun.java2d.loops.MaskBlit;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;
import sun.awt.image.BufImgSurfaceData;
import java.lang.ref.WeakReference;
import java.awt.Composite;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

/**
 * Experimental JNode specific implementation of
 * sun.java2d.loops.MaskBlit.
 *
 * @author Levente S\u00e1ntha
 */
public class JNodeMaskBlit extends MaskBlit {
    static {
        GraphicsPrimitive[] primitives = {
                new JNodeMaskBlit(SurfaceType.IntArgb,
                        CompositeType.SrcNoEa,
                        SurfaceType.Any),
                new JNodeMaskBlit(SurfaceType.IntArgb,
                        CompositeType.SrcOverNoEa,
                        SurfaceType.Any),
                new JNodeMaskBlit(SurfaceType.IntArgb,
                        CompositeType.SrcOver,
                        SurfaceType.Any),
                new JNodeMaskBlit(SurfaceType.IntRgb,
                        CompositeType.SrcOver,
                        SurfaceType.Any),
                new JNodeMaskBlit(SurfaceType.IntRgb,
                        CompositeType.SrcNoEa,
                        SurfaceType.Any),
                new JNodeMaskBlit(SurfaceType.IntBgr,
                        CompositeType.SrcOver,
                        SurfaceType.Any),
                new JNodeMaskBlit(SurfaceType.IntBgr,
                        CompositeType.SrcNoEa,
                        SurfaceType.Any),
        };
        GraphicsPrimitiveMgr.register(primitives);
    }

    Blit convertsrc;
    Blit convertdst;
    MaskBlit performop;
    Blit convertresult;

    WeakReference srcTmp;
    WeakReference dstTmp;

    public JNodeMaskBlit(SurfaceType srctype,
                         CompositeType comptype,
                         SurfaceType dsttype) {
        super(srctype, comptype, dsttype);
    }

    @Override
    public GraphicsPrimitive makePrimitive(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        if (CompositeType.Xor.equals(comptype)) {
            throw new InternalError("Cannot construct MaskBlit for " +
                    "XOR mode");
        }

        return new JNodeMaskBlit(srctype, comptype, dsttype);

    }

    public void setPrimitives(Blit srcconverter,
                              Blit dstconverter,
                              GraphicsPrimitive genericop,
                              Blit resconverter) {
        this.convertsrc = srcconverter;
        this.convertdst = dstconverter;
        this.performop = (MaskBlit) genericop;
        this.convertresult = resconverter;
    }

    public synchronized void MaskBlit(SurfaceData srcData,
                                      SurfaceData dstData,
                                      Composite comp,
                                      Region clip,
                                      int srcx, int srcy,
                                      int dstx, int dsty,
                                      int width, int height,
                                      byte mask[], int offset, int scan) {
        if (srcData instanceof sun.awt.image.BufImgSurfaceData || dstData instanceof sun.awt.image.BufImgSurfaceData) {
            BufferedImage sbi = (java.awt.image.BufferedImage) ((BufImgSurfaceData) srcData).getDestination();
            BufferedImage dbi = (java.awt.image.BufferedImage) ((BufImgSurfaceData) dstData).getDestination();
            System.out.println("MaskBlit transfer: \n" +
                    "Source img size: " + sbi.getWidth() + ", " + sbi.getHeight() + "\n" +
                    "Destin img size: " + dbi.getWidth() + ", " + dbi.getHeight() + "\n" +
                    "Params:\n" +
                    "srcx: " + srcx + " srcy: " + srcy + " dstx: " + dstx + " dsty: " + dsty + "\n" +
                    "width: " + width + " height: " + height + " offset: " + offset + " scan: " + scan + "\n" +
                    "mask[]: " + java.util.Arrays.toString(mask) + "\n" +
                    "Composite: " + comp + " clip: " + clip);


            Raster sr = sbi.getRaster();
            if (sbi.getColorModel().isCompatibleRaster(sr)) {
                System.out.println("MaskBlit compatible raster");
                int d_w = dbi.getWidth();
                int d_h = dbi.getHeight();
                if (dstx < 0) dstx = 0;
                if (dsty < 0) dsty = 0;
                if (srcx < 0) srcx = 0;
                if (srcy < 0) srcy = 0;
                if (d_w < width + dstx) width = d_w - dstx;
                if (d_h < height + dsty) height = d_h - dsty;
                if (width > 0 && height > 0) {
                    /*
                    java.awt.image.Raster src = sr.createChild(srcx, srcy, width, height, 0, 0, null);
                    java.awt.image.WritableRaster dst = dbi.getRaster().createWritableChild(dstx, dsty, width, height, 0, 0, null);
                    comp.createContext(sbi.getColorModel(), dbi.getColorModel(), new java.awt.RenderingHints(null)).compose(src, dst,dst);
                    */
                    dbi.getRaster().setDataElements(dstx, dsty, width, height, sr.getDataElements(srcx, srcy, width, height, null));
                }
            } else {
                dbi.setRGB(dstx, dsty, width, height, sbi.getRGB(srcx, srcy, width, height, null, 0, width), 0, width);
            }
        } else {
            System.out.println("Unsupported surface pair: " + srcData + ", " + dstData);
        }
    }
}


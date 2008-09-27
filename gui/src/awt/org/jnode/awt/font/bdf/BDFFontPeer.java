package org.jnode.awt.font.bdf;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.text.CharacterIterator;
import java.util.Locale;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jnode.awt.font.FontManager;
import org.jnode.awt.font.JNodeFontPeer;
import org.jnode.font.bdf.BDFFontContainer;
import org.jnode.font.bdf.BDFGlyph;
import org.jnode.font.bdf.BDFMetrics;
import org.jnode.font.bdf.BDFParser;
import org.jnode.naming.InitialNaming;
import org.jnode.vm.Unsafe;

import sun.font.CoreMetrics;
import sun.font.FontLineMetrics;

/**
 * Specific implementation of {@link JNodeFontPeer} for BDF fonts
 * 
 * @author fabien
 *
 */
public class BDFFontPeer extends JNodeFontPeer {
    private static final Logger log = Logger.getLogger(BDFFontPeer.class);    

    public BDFFontPeer(String name, Map attrs) {
        super(name, attrs);
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#canDisplay(java.awt.Font, char)
     */
    @Override
    public boolean canDisplay(Font font, char c) {        
        BDFFont bdfFont = toBDFFont(font);
        
        //TODO this is a temporary workaround : we should add a method to BDFFont
        BDFGlyph spaceGlyph = bdfFont.getContainer().getGlyph('\u0020');
        BDFGlyph characterGlyph = bdfFont.getContainer().getGlyph(c);
        
        return (c == '\u0020') || ((c != '\u0020') && (characterGlyph != spaceGlyph));
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#canDisplayUpTo(java.awt.Font,
     *      java.text.CharacterIterator, int, int)
     */
    @Override
    public int canDisplayUpTo(Font font, CharacterIterator i, int start,
                              int limit) {
        int upTo = -1;
        
        for (char c = i.setIndex(start); i.getIndex() <= limit; c = i.next()) {
            if (!canDisplay(font, c)) {
                upTo = i.getIndex();
                break;
            }
        }

        return upTo;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#createGlyphVector(java.awt.Font,
     *      java.awt.font.FontRenderContext, java.text.CharacterIterator)
     */
    @Override
    public GlyphVector createGlyphVector(Font font, FontRenderContext frc,
                                         CharacterIterator ci) {
        // TODO implement me
        System.out.println("JNodeFontPeer.createGlyphVector(" +
            "Font,FontRenderContext,CharacterIterator) not implemented");        
        return null;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#createGlyphVector(java.awt.Font,
     *      java.awt.font.FontRenderContext, int[])
     */
    @Override
    public GlyphVector createGlyphVector(Font font, FontRenderContext ctx,
                                         int[] glyphCodes) {
        // TODO implement me
        System.out.println("JNodeFontPeer.createGlyphVector(" +
                "Font,FontRenderContext,int[]) not implemented");        
        return null;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getBaselineFor(java.awt.Font,
     *      char)
     */
    @Override
    public byte getBaselineFor(Font font, char c) {
        System.out.println("JNodeFontPeer.getBaselineFor not implemented"); // TODO implement me        
        return 0;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getFontMetrics(java.awt.Font)
     */
    @Override
    public FontMetrics getFontMetrics(Font font) {
        return toBDFFont(font).getFontMetrics();
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getGlyphName(java.awt.Font, int)
     */
    @Override
    public String getGlyphName(Font font, int glyphIndex) {
        return toBDFFont(font).getContainer().getGlyphs()[glyphIndex].getName();
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getLineMetrics(java.awt.Font,
     *      java.text.CharacterIterator, int, int,
     *      java.awt.font.FontRenderContext)
     */
    @Override
    public LineMetrics getLineMetrics(Font font, CharacterIterator ci,
                                      int begin, int limit, FontRenderContext rc) {
        BDFFont bdfFont = toBDFFont(font);
        BDFMetrics fm = bdfFont.getContainer().getFontMetrics();
        
        float ascent = fm.getAscent();
        float descent = fm.getDescent();
        float leading = fm.getLeading();
        float height = fm.getHeight();
        
        // TODO find these metrics
        int baselineIndex = 0;
        float[] baselineOffsets = new float[]{0f};
        float strikethroughOffset = 0;
        float strikethroughThickness = 0;
        float underlineOffset = 0;
        float underlineThickness = 0;
        float ssOffset = 0;
        //
        
        float italicAngle = getItalicAngle(font);
        CoreMetrics cm = new CoreMetrics(ascent, descent, leading, height, baselineIndex, baselineOffsets,
                        strikethroughOffset, strikethroughThickness, underlineOffset,
                        underlineThickness, ssOffset, italicAngle);
        
        return new FontLineMetrics(limit - begin + 1, cm, rc);
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getMaxCharBounds(java.awt.Font,
     *      java.awt.font.FontRenderContext)
     */
    @Override
    public Rectangle2D getMaxCharBounds(Font font, FontRenderContext rc) {
        System.out.println("JNodeFontPeer.getMaxCharBounds not implemented"); // TODO implement me        
        return null;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getMissingGlyphCode(java.awt.Font)
     */
    @Override
    public int getMissingGlyphCode(Font font) {
        //TODO this is a temporary workaround : we should add a method to BDFFont
        return '\u0020'; // this the char used to replace missing glyphs in BDFFont
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getNumGlyphs(java.awt.Font)
     */
    @Override
    public int getNumGlyphs(Font font) {
        return toBDFFont(font).getContainer().getGlyphs().length;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getPostScriptName(java.awt.Font)
     */
    @Override
    public String getPostScriptName(Font font) {
        return toBDFFont(font).getContainer().getName();
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getStringBounds(java.awt.Font,
     *      java.text.CharacterIterator, int, int,
     *      java.awt.font.FontRenderContext)
     */
    @Override
    public Rectangle2D getStringBounds(Font font, CharacterIterator ci,
                                       int begin, int limit, FontRenderContext frc) {
        BDFFont bdfFont = toBDFFont(font);
        BDFFontContainer container = bdfFont.getContainer();

        double width = 0;
        double height = 0;
        for (char c = ci.setIndex(begin); ci.getIndex() <= limit; c = ci.next()) {
            BDFGlyph g = container.getGlyph(c);
            if (g != null) {
                width += g.getDWidth().width;
                height = Math.max(g.getDWidth().height, height);
            }                
        }
        final Rectangle2D bounds = new Rectangle2D.Double(0, 0, width, height);
        
        if (frc.getTransform() != null) {
            double[] srcPoints =
                    new double[] {bounds.getMinX(), bounds.getMinY(), 
                        bounds.getMinX(), bounds.getMaxY(), 
                        bounds.getMaxX(), bounds.getMaxY(), 
                        bounds.getMaxX(), bounds.getMinY()};
            double[] dstPoints = new double[srcPoints.length]; 
            frc.getTransform().transform(srcPoints, 0, dstPoints, 0, srcPoints.length / 2);
            
            // compute the bounding box of the result
            double minX = dstPoints[0];
            double minY = dstPoints[1];
            double maxX = minX;
            double maxY = minY;
            for (int i = 2; i < dstPoints.length; i += 2) {
                double x = dstPoints[i];
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                
                double y = dstPoints[i + 1];
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
            bounds.setRect(minX, minY, maxX - minX + 1, maxY - minY + 1);
        }
        
        return bounds;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getSubFamilyName(java.awt.Font,
     *      java.util.Locale)
     */
    @Override
    public String getSubFamilyName(Font font, Locale locale) {
        System.out.println("JNodeFontPeer.getSubFamilyName not implemented"); // TODO implement me        
        return null;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#hasUniformLineMetrics(java.awt.Font)
     */
    @Override
    public boolean hasUniformLineMetrics(Font font) {
        System.out.println("JNodeFontPeer.hasUniformLineMetrics not implemented"); // TODO implement me        
        return false;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#layoutGlyphVector(java.awt.Font,
     *      java.awt.font.FontRenderContext, char[], int, int, int)
     */
    @Override
    public GlyphVector layoutGlyphVector(Font font, FontRenderContext frc,
                                         char[] chars, int start, int limit, int flags) {
        System.out.println("JNodeFontPeer.layoutGlyphVector not implemented"); // TODO implement me        
        return null;
    }
    
    /**
     * Convert the given font to a BDFFont.
     * The font given as input might not be an instance of BDFFont 
     * since {@link Font} class is public, not abstract and has a public constructor.
     * If that's the case, then we are trying to find the closest font that we provide.
     *   
     * @param font any instance of {@link Font} (might not be an instance of BDFFont)
     * @return
     */
    private BDFFont toBDFFont(Font font) {
        if (!(font instanceof BDFFont)) {
            // ask the FontManager for a compatible Font that we provide
            try {
                FontManager mgr = InitialNaming.lookup(FontManager.NAME);
                font = mgr.getClosestProvidedFont(font, BDFFontProvider.NAME);
            } catch (NamingException ex) {
                // it should never happen since font peers are created 
                // by the FontManager
                log.error(ex);
            }
        }
        
        if (!(font instanceof BDFFont)) {
            throw new RuntimeException("unable to convert font " + font + " to a BDFFont"); 
        }
        
        return (BDFFont) font;
    }
}

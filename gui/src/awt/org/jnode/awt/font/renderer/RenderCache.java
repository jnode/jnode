/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.awt.font.renderer;

import java.awt.geom.Area;
import org.jnode.awt.font.spi.Glyph;
import org.jnode.awt.font.spi.ShapedGlyph;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class RenderCache {

    /**
     * The current rendering context
     */
    private final RenderContext ctx;

    /**
     * Initialize this instance.
     *
     * @param ctx
     */
    public RenderCache(RenderContext ctx) {
        this.ctx = ctx;
    }

    public final GlyphRenderer getRenderer(Glyph g, double ascent) {
        GlyphRenderer r = (GlyphRenderer) ctx.getObject(g);
        if (r == null) {
            Area area;
            if (g instanceof ShapedGlyph)
                area = new Area(((ShapedGlyph) g).getShape());
            else
                area = new Area(g.getBBox());

            r = new GlyphRenderer(ctx, area, ascent);
            ctx.setObject(g, r);
        }
        return r;
    }

    /**
     * Gets the current rendering context.
     *
     * @return
     */
    public final RenderContext getContext() {
        return ctx;
    }
}

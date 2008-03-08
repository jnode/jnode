/*
 * Copyright 2000 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package sun.awt;

import java.awt.Toolkit;
import java.awt.ScrollPane;
import java.awt.Insets;
import java.awt.Adjustable;
import java.awt.event.MouseWheelEvent;

import sun.awt.DebugHelper;

/*
 * ScrollPaneWheelScroller is a helper class for implmenenting mouse wheel
 * scrolling on a java.awt.ScrollPane.  It contains only static methods.
 * No objects of this class may be instantiated, thus it is declared abstract.
 */
public abstract class ScrollPaneWheelScroller {
    private static final DebugHelper dbg = DebugHelper.create(
                                                 ScrollPaneWheelScroller.class);
    private ScrollPaneWheelScroller() {}
    
    /*
     * Called from ScrollPane.processMouseWheelEvent()
     */
    public static void handleWheelScrolling(ScrollPane sp, MouseWheelEvent e) {
        if (dbg.on) dbg.println("SPWS.hWS(): x="+e.getX()+",y="+e.getY()+
                                " src is " + e.getSource());
        int increment = 0;

        if (sp != null && e.getScrollAmount() != 0) {
            Adjustable adj = getAdjustableToScroll(sp); 
            if (adj != null) {
                increment = getIncrementFromAdjustable(adj, e);
                if (dbg.on) {
                    dbg.println("increment from adjustable(" + adj.getClass() +" : " + increment);
                }
                scrollAdjustable(adj, increment);
            }
        }
    }

    /*
     * Given a ScrollPane, determine which Scrollbar should be scrolled by the
     * mouse wheel, if any.
     */
    public static Adjustable getAdjustableToScroll(ScrollPane sp) {
        int policy = sp.getScrollbarDisplayPolicy();

        // if policy is display always or never, use vert
        if (policy == ScrollPane.SCROLLBARS_ALWAYS ||
            policy == ScrollPane.SCROLLBARS_NEVER) {
            if (dbg.on) dbg.println(
                          "  using vertical scrolling due to scrollbar policy");
            return sp.getVAdjustable();

        }
        else {

            Insets ins = sp.getInsets();
            int vertScrollWidth = sp.getVScrollbarWidth();

            if (dbg.on) {
                dbg.println("insets: l:" + ins.left + " r:" + ins.right +
                 " t:" + ins.top + " b:" + ins.bottom);
                dbg.println("vertScrollWidth = " + vertScrollWidth);
            }

            // Check if scrollbar is showing by examining insets of the
            // ScrollPane
            if (ins.right >= vertScrollWidth) {
                if (dbg.on) dbg.println(
                     "  using vertical scrolling because scrollbar is present");
                return sp.getVAdjustable();
            }
            else {
                int horizScrollHeight = sp.getHScrollbarHeight();
                if (ins.bottom >= horizScrollHeight) {
                    if (dbg.on) dbg.println(
                        "  using horiz scrolling because scrollbar is present");
                    return sp.getHAdjustable();
                }
                else {
                    if (dbg.on) dbg.println(
                            "  using NO scrollbar becsause neither is present");
                    return null;
                }
            }
        }
    }

    /*
     * Given the info in a MouseWheelEvent and an Adjustable to scroll, return
     * the amount by which the Adjustable should be adjusted.  This value may
     * be positive or negative.
     */
    public static int getIncrementFromAdjustable(Adjustable adj,
                                                 MouseWheelEvent e) {
    // ASSUME: adj != null
        int increment = 0;

        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            increment = e.getUnitsToScroll() * adj.getUnitIncrement();
        }
        else if (e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
            increment = adj.getBlockIncrement() * e.getWheelRotation();
        }
        return increment;
    }

    /*
     * Scroll the given Adjustable by the given amount.  Checks the Adjustable's
     * bounds and sets the new value to the Adjustable.
     */
    public static void scrollAdjustable(Adjustable adj, int amount) {
    // ASSUME adj != null
    // ASSUME amount != 0

        int current = adj.getValue();
        int upperLimit = adj.getMaximum() - adj.getVisibleAmount();
        if (dbg.on) dbg.println("  doScrolling by " + amount);

        if (amount > 0 && current < upperLimit) { // still some room to scroll
                                                  // down
            if (current + amount < upperLimit) {
                adj.setValue(current + amount);
                return;
            }
            else {
                adj.setValue(upperLimit);
                return;
            }
        }
        else if (amount < 0 && current > adj.getMinimum()) { // still some room 
                                                             // to scroll up
            if (current + amount > adj.getMinimum()) {
                adj.setValue(current + amount);
                return;
            }
            else {
                adj.setValue(adj.getMinimum());
                return;
            }
        }
    }
}

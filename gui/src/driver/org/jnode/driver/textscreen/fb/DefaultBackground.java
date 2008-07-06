package org.jnode.driver.textscreen.fb;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class DefaultBackground implements Background {
    private final Rectangle bounds = new Rectangle();
    private final Color color; 

    public DefaultBackground(Color color) {
        this.color = color;
    }
    
    public void paint(Graphics ig) {
        ig.setColor(color);
        ig.getClipBounds(bounds);
        ig.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);            
    }
}

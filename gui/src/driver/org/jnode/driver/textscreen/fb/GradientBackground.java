package org.jnode.driver.textscreen.fb;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;


public class GradientBackground implements Background {
    private final Rectangle bounds = new Rectangle();

    public void paint(Graphics ig) {
        ig.getClipBounds(bounds);
        
        float step = (float) 1.0 / (float) bounds.height;
        
        float blue = 0f;
        Color color = new Color(0f, 0f, blue);
        for (int y = 0; y < bounds.height; y++) {
            ig.setColor(color);        
            ig.drawLine(0, y, bounds.width - 1, y);
            
            blue += step;
            color = new Color(0f, 0f, blue);
        }
    }

}

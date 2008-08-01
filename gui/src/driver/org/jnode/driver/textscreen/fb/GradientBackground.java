package org.jnode.driver.textscreen.fb;

import java.awt.Color;
import java.awt.Graphics;


public class GradientBackground implements Background {
    private final int maxX;
    private final int height;
    private final Color[] colors;
    
    GradientBackground(int width, int height) {
        this.maxX = width - 1;
        this.height = height;        
        this.colors = new Color[height];
        
        final float step = (float) 1.0 / (float) height;        
        float blue = 0f;
        Color color = null;
        Color prevColor = null;
        for (int y = 0; y < height; y++) {
            if ((y > 0) && (prevColor.getBlue() == (int) (blue * 255))) {
                color = prevColor;
            } else {
                color = new Color(0f, 0f, blue);
            }
            colors[y] = color;
            
            blue += step;
            prevColor = color;
        }
    }
    
    
    public void paint(Graphics ig) {
        for (int y = 0; y < height; y++) {
            ig.setColor(colors[y]);        
            ig.drawLine(0, y, maxX, y);
        }
    }

}

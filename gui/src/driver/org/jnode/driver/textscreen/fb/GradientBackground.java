package org.jnode.driver.textscreen.fb;

import java.awt.Color;
import java.awt.Graphics;


public class GradientBackground implements Background {
    private final int width;
    private final int height;
    
    GradientBackground(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    
    public void paint(Graphics ig) {
        float step = (float) 1.0 / (float) height;
        
        float blue = 0f;
        Color color = new Color(0f, 0f, blue);
        for (int y = 0; y < height; y++) {
            ig.setColor(color);        
            ig.drawLine(0, y, width - 1, y);
            
            blue += step;
            color = new Color(0f, 0f, blue);
        }
    }

}

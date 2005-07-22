/*
 * $Id$
 */
package org.jnode.driver.textscreen.fb;

import org.jnode.driver.textscreen.ScrollableTextScreen;
import org.jnode.driver.textscreen.TextScreen;


class FbTextScreen implements TextScreen {
    
    private final int width;
    private final int height;
    private final char[] data;
    private final FbTextScreen parent;
    
    public FbTextScreen(int width, int height, FbTextScreen parent) {
        this.width = width;
        this.height = height;
        this.data = new char[width * height];
        this.parent = parent;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#copyContent(int, int, int)
     */
    public final void copyContent(int srcOffset, int destOffset, int length) {
        System.arraycopy(data, srcOffset, data, destOffset, length);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#copyTo(org.jnode.driver.textscreen.TextScreen)
     */
    public void copyTo(TextScreen dst) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#createCompatibleBufferScreen()
     */
    public TextScreen createCompatibleBufferScreen() {
        return new FbTextScreen(getWidth(), getHeight(), this);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#createCompatibleScrollableBufferScreen(int)
     */
    public ScrollableTextScreen createCompatibleScrollableBufferScreen(int height) {
        return new FbScrollableTextScreen(getWidth(), height, this);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#ensureVisible(int)
     */
    public void ensureVisible(int row) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getChar(int)
     */
    public char getChar(int offset) {
        return data[offset];
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getColor(int)
     */
    public int getColor(int offset) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getHeight()
     */
    public final int getHeight() {
        return height;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getOffset(int, int)
     */
    public final int getOffset(int x, int y) {
        return (y * width) + x;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getWidth()
     */
    public final int getWidth() {
        return width;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char, int, int)
     */
    public void set(int offset, char ch, int count, int color) {
        count = Math.min(count, data.length - offset);
        for (int i = count - 1; i >= 0; i--) {
            data[offset + i] = ch;
        }
        // TODO Set color        
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int, int)
     */
    public void set(int offset, char[] ch, int chOfs, int length, int color) {
        length = Math.min(length, data.length - offset);
        for (int i = length - 1; i >= 0; i--) {
            data[offset + i] = ch[chOfs + i];
        }
        // TODO Set color        
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int, int[], int)
     */
    public void set(int offset, char[] ch, int chOfs, int length, int[] colors, int colorsOfs) {
        length = Math.min(length, data.length - offset);
        for (int i = length - 1; i >= 0; i--) {
            data[offset + i] = ch[chOfs + i];
        }
        // TODO Set color        
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#setCursor(int, int)
     */
    public void setCursor(int x, int y) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#setCursorVisible(boolean)
     */
    public void setCursorVisible(boolean visible) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#sync()
     */
    public void sync() {
        // TODO Auto-generated method stub
        
    }
}

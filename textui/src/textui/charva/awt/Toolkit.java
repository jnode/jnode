/*
 * $Id$
 */
package charva.awt;

import java.util.LinkedList;
import java.util.Arrays;

import javax.naming.NameNotFoundException;

import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.ConsoleListener;
import org.jnode.driver.console.ConsoleEvent;
import org.jnode.driver.input.KeyboardAdapter;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.driver.input.PointerEvent;
import org.jnode.driver.input.PointerListener;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.ShellManager;

import charva.awt.event.KeyEvent;
import charva.awt.event.MouseEvent;

/**
 * @author vali
 * @author Levente S\u00e1ntha
 */
public class Toolkit extends AbstractToolkit implements KeyboardListener,
        PointerListener, ConsoleListener {
    private static Toolkit instance = null;

    private int[] colorpairs = new int[256];

    private LinkedList keyQueue = new LinkedList();

    private final TextConsole console;

    //private final int iniCurX;
    //private final int iniCurY;
    private int curX;

    private int curY;

    private int leftClip;

    private int rightClip;

    private int topClip;

    private int bottomClip;

    private boolean registered;

    private ConsoleManager conMan;

    // mouse state
    private int prevMouseX = 0;

    private int prevMouseY = 0;

    boolean leftWasPressed = false;

    boolean middleWasPressed = false;

    boolean rightWasPressed = false;

    // end of mouse state

    private final boolean useBuffer = false;

    protected Toolkit() {
        throw new RuntimeException("Illegal use of constructor");
    }

    public Toolkit(TextConsole console, ConsoleManager conMan) {
        this._evtQueue = EventQueue.getInstance();

        /*
         * If the terminal is capable of handling colors, initialize the color
         * capability and the first color-pair (the default foreground and
         * background colors are white-on-black, but can be modified by setting
         * the static variables _defaultForeground and _defaultBackground.
         */
        if (this.hasColors() && Toolkit.isColorEnabled) {
            startColors();
            _colorPairs.add(new ColorPair(_defaultForeground,
                    _defaultBackground));
        }
        this.conMan = conMan;
        //if (!useBuffer) {
        this.console = console;
        //        } else {
        //            BufferScreen bs = new BufferScreen(console.getWidth(), console
        //                    .getHeight());
        //            try {
        //                buffer = new RawTextConsole(conMan, console.getConsoleName()
        //                        + "_buffer", bs);
        //            } catch (ConsoleException e) {
        //                e.printStackTrace();
        //            }
        //            buffer.setCursorVisible(false);
        //        }
        //this.console = console;
        initConsole(console);
        register();
    }

    /**
     * This static method instantiates a Toolkit object if it does not already
     * exist; and returns a reference to the instantiated Toolkit.
     */
    //    public static void pause() {
    //        try {
    //            Thread.sleep( 500 );
    //        }
    //        catch( InterruptedException e ) {
    //            e.printStackTrace();
    //        }
    //    }
    public void unregister() {
        if (registered) {
            conMan.unregisterConsole(console);
            registered = false;
        }
    }

    public void register() {
        if (registered) {
            return;
        }
        registered = true;
        conMan.registerConsole(console);
        System.out.println("Registering charva Toolkit with consoleManager.");
        //        pause();
        conMan.focus(console);

        console.clear();
    }

    public static void setDefaultToolkit(Toolkit tk) {
        instance = tk;
    }

    public static synchronized Toolkit getDefaultToolkit() {
        if (instance == null) {
            instance = createInstance();
        }
        return instance;
    }

    private static Toolkit createInstance() {
        try {
            final ShellManager sm = InitialNaming.lookup(ShellManager.NAME);
            final ConsoleManager conMgr = sm.getCurrentShell().getConsole().getManager();
            final TextConsole console = (TextConsole) conMgr.createConsole(
            		"charva", 
            		ConsoleManager.CreateOptions.TEXT |
                            ConsoleManager.CreateOptions.STACKED |
                            ConsoleManager.CreateOptions.NO_LINE_EDITTING);
            console.addKeyboardListener(new KeyboardAdapter() {
                public void keyPressed(KeyboardEvent event) {
                    if (event.isControlDown() && event.getKeyChar() == 'z') {
                        System.err.println("got ctrl-z, unregistering Toolkit.");
                        //maybe this will help to debug the finite-sized text
                        // area bug.
                        getDefaultToolkit().unregister();
                        event.consume();
                    }
                }
            });
            return new Toolkit(console, conMgr);
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void initConsole(TextConsole cons) {
//        cons.setCursorVisible(false); //This may have to be commented out if charva items lose their cursor.
        cons.setCursorVisible( true );//Keep this so TextComponents have a cursor.
        cons.addKeyboardListener(this);
        cons.addPointerListener(this);
        curX = /* iniCurX = */console.getCursorX();
        curY = /* iniCurY = */console.getCursorY();
        resetClipRect();
    }

    public synchronized void waitTillFinished() {
        while (isKeyboardReaderRunning()) {
            try {
                instance.wait();
            } catch (InterruptedException ie) {
                //ignore
            }
        }
        close();
    }

    public void consoleClosed(ConsoleEvent event) {
        synchronized(Toolkit.class){
            instance = null;
        }
    }

    public void close() {
        unregister();
        synchronized(Toolkit.class){
            instance = null;
        }
        //this could kill threads, etc. But let's save 'em for if this instance
        // comes up again.
        //        System.err.println( "In close(), calling clear" );
        //        pause();
        //        clear();
        //        System.err.println( "Stopping SyncQueue" );
        //        pause();
        //        SyncQueue.getInstance().stop();
        //        System.err.println( "Stopping KeyboardReader" );
        //        pause();
        //        setKeyboardReaderRunning( false );
        //        System.err.println( "Skipping KeyQueue.Notify all" );
        //        pause();
        //// keyQueue.notifyAll();
        //        System.err.println( "Closing console: " + console.getConsoleName() +
        // ": " + console.getClass().getName() );
        //        pause();
        //
        //        System.err.println( "Unregistering" );
        //        unregister();
        //        System.err.println( "Returning from close()" );
        //        pause();
    }

    public void clear() {
        console.clear();
    }

    public void setCursor(int x_, int y_) {
        curX = x_;
        curY = y_;
        console.setCursor(x_, y_);
    }

    public void addChar(int chr, int attrib, int colorpair_) {
        putCharWithClip(chr, attrib, colorpair_);
    }

    public void addVerticalLine(int length, int attrib, int colorpair) {
        int x = curX;
        for (int ln = curY + length, i = curY; i < ln; i++) {
            setCursor(x, i);
            putCharWithClip(ACS_VLINE, attrib, colorpair);
        }
    }

    public void setCursorVisible(boolean visible) {
        console.setCursorVisible(visible);
    }

    public void addString(String str_, int attrib_, int colorpair_) {
        if (str_ == null) {
            return;
        }

        putCharsWithClip(str_.toCharArray(), attrib_, colorpair_);
    }

    public void drawBoxNative(int left_, int top_, int right_, int bottom_,
            int colorpair_) {
        setCharWithClip(left_, top_, ACS_ULCORNER, colorpair_);
        setCharsWithClip(left_ + 1, top_, ACS_HLINE, colorpair_, right_ - left_ - 1);

        setCharWithClip(left_, bottom_, ACS_LLCORNER, colorpair_);
        setCharsWithClip(left_ + 1, bottom_, ACS_HLINE, colorpair_, right_ - left_ - 1);

        setCharWithClip(right_, top_, ACS_URCORNER, colorpair_);
        for (int j = top_ + 1; j < bottom_; j++) {
            setCharWithClip(left_, j, ACS_VLINE, colorpair_);

        }
        setCharWithClip(right_, bottom_, ACS_LRCORNER, colorpair_);
        for (int j = top_ + 1; j < bottom_; j++) {
            setCharWithClip(right_, j, ACS_VLINE, colorpair_);
        }
    }

    public void blankBoxNative(int left_, int top_, int right_, int bottom_,
            int colorpair_) {
        int width = right_ - left_;
        for (int j = top_; j < bottom_; j++) {
            setCharsWithClip(left_, j, ' ', colorpair_, width);
        }
    }

    public void setClipRectNative(int left_, int top_, int right_, int bottom_) {
        leftClip = left_;
        rightClip = right_;
        topClip = top_;
        bottomClip = bottom_;
    }

    public void resetClipRect() {
        leftClip = 0;
        rightClip = console.getWidth();
        topClip = 0;
        bottomClip = console.getHeight();
    }

    public void beep() {
        //        SpeakerUtils.beep();
    }

    public int getScreenRows() {
        return console.getHeight();
    }

    public int getScreenColumns() {
        return console.getWidth();
    }

    public boolean hasColors() {
        return true;
    }

    public int getMaxColorPairs() {
        return 256;
    }

    public void startColors() {
    }

    public void sync() {
        if (useBuffer) {
            //buffer.copyTo(console);
        }
    }

    public void initColorPair(int index, int fgnd_, int bgnd_)
            throws TerminfoCapabilityException {
        colorpairs[index] = ((0xF & bgnd_) << 4) | (0xF & fgnd_);
    }

    public String getTtyName() {
        return console.getConsoleName();
    }

    protected Object readKey() {
        synchronized (keyQueue) {
            while (keyQueue.isEmpty() && isKeyboardReaderRunning()) {
                try {
                    keyQueue.wait();
                } catch (InterruptedException ie) {

                }
            }
            if (isKeyboardReaderRunning()) {
                return keyQueue.removeFirst();
            } else {
                return null;
            }
        }
    }

    protected int getx() {
        return console.getCursorX();
    }

    protected int gety() {
        return console.getCursorY();
    }

    public void keyPressed(KeyboardEvent event) {
        synchronized (keyQueue) {
            int key_code = event.getKeyCode();
            int key_char = event.getKeyChar();
            int key;
            switch (key_code) {
            case java.awt.event.KeyEvent.VK_LEFT:
                key = KeyEvent.VK_LEFT;
                break;
            case java.awt.event.KeyEvent.VK_RIGHT:
                key = KeyEvent.VK_RIGHT;
                break;
            case java.awt.event.KeyEvent.VK_UP:
                key = KeyEvent.VK_UP;
                break;
            case java.awt.event.KeyEvent.VK_DOWN:
                key = KeyEvent.VK_DOWN;
                break;
            case java.awt.event.KeyEvent.VK_PAGE_DOWN:
                key = KeyEvent.VK_PAGE_DOWN;
                break;
            case java.awt.event.KeyEvent.VK_PAGE_UP:
                key = KeyEvent.VK_PAGE_UP;
                break;

            case java.awt.event.KeyEvent.VK_HOME:
                key = KeyEvent.VK_HOME;
                break;

            case java.awt.event.KeyEvent.VK_END:
                key = KeyEvent.VK_END;
                break;

            case java.awt.event.KeyEvent.VK_BACK_SPACE:
                key = KeyEvent.VK_BACK_SPACE;
                break;

            case java.awt.event.KeyEvent.VK_INSERT:
                key = KeyEvent.VK_INSERT;
                break;

            case java.awt.event.KeyEvent.VK_DELETE:
                key = KeyEvent.VK_DELETE;
                break;

            case java.awt.event.KeyEvent.VK_ENTER:
                key = KeyEvent.VK_ENTER;
                break;

            case java.awt.event.KeyEvent.VK_ESCAPE:
                key = KeyEvent.VK_ESCAPE;
                break;

            case java.awt.event.KeyEvent.VK_F1:
            case java.awt.event.KeyEvent.VK_F2:
            case java.awt.event.KeyEvent.VK_F3:
            case java.awt.event.KeyEvent.VK_F4:
            case java.awt.event.KeyEvent.VK_F5:
            case java.awt.event.KeyEvent.VK_F6:
            case java.awt.event.KeyEvent.VK_F7:
            case java.awt.event.KeyEvent.VK_F8:
            case java.awt.event.KeyEvent.VK_F9:
            case java.awt.event.KeyEvent.VK_F10:
            case java.awt.event.KeyEvent.VK_F11:
            case java.awt.event.KeyEvent.VK_F12:
            case java.awt.event.KeyEvent.VK_F13:
            case java.awt.event.KeyEvent.VK_F14:
            case java.awt.event.KeyEvent.VK_F15:
            case java.awt.event.KeyEvent.VK_F16:
            case java.awt.event.KeyEvent.VK_F17:
            case java.awt.event.KeyEvent.VK_F18:
            case java.awt.event.KeyEvent.VK_F19:
            case java.awt.event.KeyEvent.VK_F20:
                key = KeyEvent.VK_F1 + key_code - java.awt.event.KeyEvent.VK_F1;
                break;
            default:
                key = key_char;
            }

            keyQueue.add(new Integer(key));
            keyQueue.notifyAll();
        }
    }

    public void keyReleased(KeyboardEvent event) {

    }

    private void setCharWithClip(int x, int y, int ch, int cp) {
        if (x >= leftClip && x <= rightClip && y >= topClip && y <= bottomClip) {
            if (console != null) {
                console.setChar(x, y, (char) ch, colorpairs[cp]);
            }
        }
    }

    private void setCharsWithClip(int x, int y, int ch, int cp, int nbCopy) {
        if (x >= leftClip && x <= rightClip && y >= topClip && y <= bottomClip) {
            if (console != null) {
                char c = (char) ch;
                int color = colorpairs[cp];
                int xmax = Math.min(x + nbCopy, rightClip + 1);
                int ln = xmax - x;
                if(ln > 0){
                    char[] chars = new char[ln];
                    Arrays.fill(chars, c);
                    console.setChar(x, y, chars, color);
                }
            }
        }
    }

    private void putCharWithClip(int ch, int att, int cp) {
        if (curY >= topClip && curY <= bottomClip && curX >= leftClip
                && curX <= rightClip) {
            console.putChar((char) ch, computeColor(att, cp));
            curX++;
        }
    }

    private void putCharsWithClip(char[] chars, int att, int cp) {
        if (curY >= topClip && curY <= bottomClip && curX >= leftClip
                && curX <= rightClip) {
            int xmax = Math.min(curX + chars.length, rightClip + 1);
            int cx = console.getCursorX();
            int cy = console.getCursorY();
            int length = xmax - curX;
            if(length > 0){
                console.setChar(cx, cy, chars, 0, length, computeColor(att, cp));
                console.setCursor(cx + length, cy);
                curX += length;
            }
        }
    }

    private int computeColor(int att, int cp) {
        int c = colorpairs[cp];

        if ((att & A_REVERSE) != 0) {
            c = ((c << 4) & 0xF0) | ((c >> 4) & 0xF);
        }

        if ((att & A_UNDERLINE) != 0) {
            c = c | 8;
        }

        if ((att & A_BOLD) != 0) {
            c = c | 8;
        }

        int bu = A_BOLD | A_UNDERLINE;
        if ((att & bu) == bu) {
            c = c ^ 0xFF;
        }

        return c;
    }

    /**
     *  
     */
    public void pointerStateChanged(PointerEvent event) {
        synchronized (keyQueue) {
            int x = prevMouseX;
            int y = prevMouseY;

            // compute the new mouse position
            if (event.isRelative()) {
                //System.err.println("relative x="+event.getX()+"
                // y="+event.getY());
                x += event.getX();
                y += event.getY();
            } else if (event.isAbsolute()) {
                //System.err.println("absolute x="+event.getX()+"
                // y="+event.getY());
                x = event.getX();
                y = event.getY();
            }
            //System.err.println("x="+event.getX()+" y="+event.getY());

            int button = -1;
            int modifiers = 0;
            if (leftWasPressed && !event.isLeftButtonPressed()) {
                button = MouseEvent.BUTTON1;
                modifiers = MouseEvent.MOUSE_RELEASED;
            } else if (!leftWasPressed && event.isLeftButtonPressed()) {
                button = MouseEvent.BUTTON1;
                modifiers = MouseEvent.MOUSE_PRESSED;
            } else if (middleWasPressed && !event.isMiddleButtonPressed()) {
                button = MouseEvent.BUTTON2;
                modifiers = MouseEvent.MOUSE_RELEASED;
            } else if (!middleWasPressed && event.isMiddleButtonPressed()) {
                button = MouseEvent.BUTTON2;
                modifiers = MouseEvent.MOUSE_PRESSED;
            } else if (rightWasPressed && !event.isRightButtonPressed()) {
                button = MouseEvent.BUTTON3;
                modifiers = MouseEvent.MOUSE_RELEASED;
            } else if (!rightWasPressed && event.isRightButtonPressed()) {
                button = MouseEvent.BUTTON3;
                modifiers = MouseEvent.MOUSE_PRESSED;
            }

            // save the current mouse state
            leftWasPressed = event.isLeftButtonPressed();
            middleWasPressed = event.isMiddleButtonPressed();
            rightWasPressed = event.isRightButtonPressed();
            prevMouseX = x;
            prevMouseY = y;

            Component component = getComponentAt(x, y);
            if (component != null) {
                MouseEvent me = new MouseEvent(component, modifiers, x, y, 0,
                        button);
                keyQueue.add(me);
                keyQueue.notifyAll();
            }
        }
    }

    //NOT USED
    //	public void redrawWin() {
    //	}
    //	public String getStringCapability(String capname_)
    //			throws TerminfoCapabilityException {
    //		return null;
    //	}
    //	public int getNumericCapability(String capname_)
    //			throws TerminfoCapabilityException {
    //		return 0;
    //	}
    //NOT USED
    //	public boolean getBooleanCapability(String capname_)
    //			throws TerminfoCapabilityException {
    //		return false;
    //	}
    //NOT USED
    //	public void putp(String str_) {
    //	}
    //NOT USED
    //	public void print(String str_) throws TerminfoCapabilityException {
    //	}
    //NOT USED
    //	public void addHorizontalLine(int length_, int attrib_, int colorpair) {
    //	}
}

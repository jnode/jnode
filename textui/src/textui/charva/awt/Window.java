/* class Window
 *
 * Copyright (C) 2001-2003  R M Pitman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package charva.awt;

import java.util.Enumeration;
import java.util.Vector;

import charva.awt.event.AWTEvent;
import charva.awt.event.AdjustmentEvent;
import charva.awt.event.GarbageCollectionEvent;
import charva.awt.event.InvocationEvent;
import charva.awt.event.PaintEvent;
import charva.awt.event.ScrollEvent;
import charva.awt.event.SyncEvent;
import charva.awt.event.WindowEvent;
import charva.awt.event.WindowListener;

/**
 * The Window class represents a "toplevel" window with no decorative frame.
 * The window is initially invisible; you must use the show() method to make it
 * visible.
 */
public class Window extends Container implements Runnable {

    public Window(Window owner_) {
        _owner = owner_;
        init();
    }

    public Window(Frame owner_) {
        _owner = owner_;
        init();
    }

    private void init() {
        _term = Toolkit.getDefaultToolkit();
        super._layoutMgr = new BorderLayout();
        _visible = false;

        // The window inherits the colors of its parent if there is one,
        // otherwise use the default colors as set in charva.awt.Toolkit.
        if (_owner != null) {
            super.setForeground(_owner.getForeground());
            super.setBackground(_owner.getBackground());
        } else {
            super.setForeground(Toolkit.getDefaultForeground());
            super.setBackground(Toolkit.getDefaultBackground());
        }
    }

    /**
     * Return the Window that is the "owner" of this Window.
     */
    public Window getOwner() {
        return _owner;
    }

    /**
     * Register a WindowListener object for this window.
     */
    public void addWindowListener(WindowListener listener_) {
        if (_windowListeners == null) _windowListeners = new Vector<WindowListener>();

        _windowListeners.add(listener_);
    }

    /**
     * Process window events occurring on this window by dispatching them to
     * any registered WindowListener objects.
     */
    protected void processWindowEvent(WindowEvent evt_) {
        if (_windowListeners == null) return;

        Enumeration<WindowListener> e = _windowListeners.elements();
        while (e.hasMoreElements()) {
            WindowListener wl = (WindowListener) e.nextElement();
            switch (evt_.getID()) {

            case AWTEvent.WINDOW_CLOSING:
                wl.windowClosing(evt_);
                break;

            case AWTEvent.WINDOW_OPENED:
                wl.windowOpened(evt_);
                break;
            }
        }
    }

    /**
     * Returns true if this Window is currently displayed.
     */
    public boolean isDisplayed() {
        return _term.isWindowDisplayed(this);
    }

    /**
     * Causes this Window to be sized to fit the preferred sizes and layouts of
     * its contained components.
     */
    public void pack() {
        setSize(minimumSize());
        super.doLayout(); // call method in Container superclass
    }

    /**
     * Lay out the contained components, draw the window and its contained
     * components, and then read input events off the EventQueue and send them
     * to the component that has the input focus.
     */
    public void show() {

        if (_visible) return; // This window is already visible.

        _visible = true;
        _term.addWindow(this);

        /*
         * Start the keyboard-reader thread _after_ the first window has been
         * displayed, otherwise we get a NoSuchElement exception if the user
         * starts typing before the first window is displayed, because the
         * _windowList is empty.
         */
        _term.startKeyboardReader();

        /*
         * call doLayout in Container superclass. This will lay out all of the
         * contained components (i.e. children) and their descendants, and in
         * the process will set the valid flag of all descendants.
         */
        super.doLayout(); // call method in Container superclass

        this.adjustLocation(); // ensure it fits inside the screen

        this.draw(Toolkit.getDefaultToolkit());

        /*
         * Rather than call Toolkit.sync() directly here, we put a SyncEvent
         * onto the SyncQueue. The SyncThread will read it off the SyncQueue
         * and then sleep for 50msec before putting the SyncEvent onto the
         * EventQueue, from which it will be picked up by the active Window
         * (i.e. an instance of this class). The active Window will then call
         * Toolkit.sync() directly. This slight delay speeds up the case where
         * a window opens and then immediately opens a new window (and
         * another...).
         */
        SyncQueue.getInstance().postEvent(new SyncEvent(this));

        if (_dispatchThreadRunning)
            run();
        else {
            _dispatchThreadRunning = true;
            //System.err.println( "Created window dispatch thread." );
            Thread dispatchThread = new Thread(this);
            dispatchThread.setName("Window event dispatcher");
            dispatchThread.start();

            /*
             * If "charva.script.playback" is defined, we start up a thread for
             * playing back the script. Keys from both the script and the
             * keyboard will cause "fireKeystroke()" to be invoked. The
             * playback thread is started _after_ "addWindow()" is called for
             * the first time, to make sure that _windowList is non-empty when
             * the playback thread calls "fireKeystroke()".
             */
            //startPlayback();

        }
    }

    public void run() {
        /*
         * Loop and process input events until the window closes.
         */
        try {
            EventQueue evtQueue = _term.getSystemEventQueue();
            for (_windowClosed = false; _windowClosed != true;) {

                java.util.EventObject evt = evtQueue.getNextEvent();

                /*
                 * The event object should always be an AWTEvent. If not, we
                 * will get a ClassCastException.
                 */
                processEvent((AWTEvent) evt);
            } // end FOR loop
        } catch (Exception e) {
            System.err.println( "Exception in Window.run" );
            e.printStackTrace();
//            System.exit(1);
            System.err.println( "Exiting method (not VM)" );
        }
    }

    /**
     * Process an event off the event queue. This method can be extended by
     * subclasses of Window to deal with application-specific events.
     */
    protected void processEvent(AWTEvent evt_) {
        Object source = evt_.getSource();

        if (evt_ instanceof AdjustmentEvent){
            ((Adjustable) source)
                    .processAdjustmentEvent((AdjustmentEvent) evt_);
        }

        else if (evt_ instanceof ScrollEvent) {
            processScrollEvent((ScrollEvent) evt_);
        }

        else if (evt_ instanceof PaintEvent) {

            processPaintEvent((PaintEvent) evt_);
        }

        else if (evt_ instanceof SyncEvent) {
            _term.sync();
        }

        else if (evt_ instanceof WindowEvent) {
            WindowEvent we = (WindowEvent) evt_;
            we.getWindow().processWindowEvent(we);

            /*
             * Now, having given the WindowListener objects a chance to process
             * the WindowEvent, we must check if it was a WINDOW_CLOSING event
             * sent to this window.
             */
            if (we.getID() == AWTEvent.WINDOW_CLOSING) {

                we.getWindow()._windowClosed = true;

                /*
                 * Remove this window from the list of those displayed, and
                 * blank out the screen area where the window was displayed.
                 */
                _term.removeWindow(we.getWindow());
                _term.blankBox(_origin, _size);

                /*
                 * Now redraw all of the windows, from the bottom to the top.
                 */
                Vector<Window> winlist = _term.getWindowList();
                Window window = null;
                synchronized (winlist) {
                    for (int i = 0; i < winlist.size(); i++) {
                        window = (Window) winlist.elementAt(i);
                        window.draw(Toolkit.getDefaultToolkit());
                    }
                    if (window != null) // (there may be no windows left)
                            window.requestFocus();
                }

                /*
                 * Put a SyncEvent onto the SyncQueue. The SyncThread will
                 * sleep for 50 msec before putting it onto the EventQueue,
                 * from which it will be picked up by the active Window (i.e.
                 * an instance of this class), which will then call
                 * Toolkit.sync() directly. This is done to avoid calling
                 * sync() after the close of a window and again after the
                 * display of a new window which might be displayed immediately
                 * afterwards.
                 */
                if(window != null)
                    SyncQueue.getInstance().postEvent(new SyncEvent(window));
            }
        } // end if WindowEvent

        else if (evt_ instanceof GarbageCollectionEvent) {
            SyncQueue.getInstance().postEvent(evt_);
        }

        else if (evt_ instanceof InvocationEvent) {
            ((InvocationEvent) evt_).dispatch();
        }

        else {
            /*
             * It is a KeyEvent, MouseEvent, ActionEvent, ItemEvent, FocusEvent
             * or a custom type of event.
             */
            //System.err.println(evt_);
            ((Component) source).processEvent(evt_);
        }
    }

    protected void processScrollEvent(ScrollEvent event) {
        Scrollable source = (Scrollable) event.getSource();
        source.processScrollEvent(event);
        requestFocus();
        super.requestSync();
    }

    protected void processPaintEvent(PaintEvent  event) {
        Component source = (Component) event.getSource();
        if (!source.isTotallyObscured()) {
            processPaintEvent2(source);
        }
    }

    private void processPaintEvent2(Component source) {
        /*
         * Unless the affected component is totally obscured by windows
         * that are stacked above it, we must redraw its window and all the
         * windows above it.
         */
        if (!((Component) source).isTotallyObscured()) {

            Vector<Window> windowlist = _term.getWindowList();
            synchronized (windowlist) {

                /*
                 * We have to draw the window rather than just the affected
                 * component, because setVisible(false) may have been set
                 * on the component.
                 */
                Window ancestor = ((Component) source).getAncestorWindow();
                ancestor.draw(Toolkit.getDefaultToolkit());

                /*
                 * Ignore windows that are underneath the window that
                 * contains the component that generated the PaintEvent.
                 */
                Window w = null;
                int i;
                for (i = 0; i < windowlist.size(); i++) {
                    w = (Window) windowlist.elementAt(i);
                    if (w == ancestor) break;
                }

                /*
                 * Redraw all the windows _above_ the one that generated
                 * the PaintEvent.
                 */
                for (; i < windowlist.size(); i++) {
                    w = (Window) windowlist.elementAt(i);
                    w.draw(Toolkit.getDefaultToolkit());
                }
            }

            super.requestSync();
        }
    }

    /**
     * Hide this window and all of its contained components. This is done by
     * putting a WINDOW_CLOSING event onto the queue.
     */
    public void hide() {

        if (!_visible) {
            System.err.println("Trying to hide window " + this
                    + " that is already hidden!");
            return; // This window is already hidden.
        }
        _term.removeWindow( this );
        _visible = false;
        WindowEvent we = new WindowEvent(this, AWTEvent.WINDOW_CLOSING);
        _term.getSystemEventQueue().postEvent(we);
    }

    /**
     * Draw all the components in this window, and request the keyboard focus.
     * @param toolkit
     */
    public void draw(Toolkit toolkit) {
        super.draw(toolkit);
        requestFocus();
    }

    /**
     * Overrides the method in the Component superclass, because a Window has
     * no parent container. Note that we return a COPY of the origin, not a
     * reference to it, so that the caller cannot modify our location via the
     * returned value.
     */
    public Point getLocationOnScreen() {
        return new Point(_origin);
    }

    /**
     * A Window component will not receive input focus during keyboard focus
     * traversal using Tab and Shift-Tab.
     */
    public boolean isFocusTraversable() {
        return false;
    }

    /**
     * Adjust the position of the window so that it fits inside the screen.
     */
    public void adjustLocation() {
        int bottom = _origin.y + getHeight();
        if (bottom > _term.getScreenRows())
                _origin.y -= bottom - _term.getScreenRows();

        if (_origin.y < 0) _origin.y = 0;

        int right = _origin.x + getWidth();
        if (right > _term.getScreenColumns())
                _origin.x -= right - _term.getScreenColumns();

        if (_origin.x < 0) _origin.x = 0;
    }

    public void debug(int level_) {
        System.err.println("Window origin=" + _origin + " size=" + _size);
        super.debug(1);
    }

    @SuppressWarnings("unused")
    private void startPlayback() {
//        System.err.println("Playback disabled (awaiting security exception fix..)");
        return;
//        String scriptfilename = null;
//        BufferedReader scriptReader = null;
//        if ((scriptfilename = System.getProperty("charva.script.playback")) == null)
//                return;
//
//        try {
//            scriptReader = new BufferedReader(new FileReader(scriptfilename));
//        } catch (FileNotFoundException ef) {
//            System.err.println("Cannot open script file \"" + scriptfilename
//                    + "\" for reading");
//            return;
//        }
//
//        PlaybackThread thr = new PlaybackThread(scriptReader);
//        thr.setDaemon(true);
//        thr.setName("playback thread");
//        thr.start();
    }

    //====================================================================
    // INSTANCE VARIABLES

    private Window _owner;

    protected Toolkit _term;

    private boolean _windowClosed = false;

    private Vector<WindowListener> _windowListeners = null;

    private static boolean _dispatchThreadRunning = false;

}

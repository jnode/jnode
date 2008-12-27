/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.awt.swingpeers;

import java.awt.AWTError;
import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.Shape;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.event.InvocationEvent;
import java.awt.image.BufferedImage;
import java.awt.peer.ButtonPeer;
import java.awt.peer.CanvasPeer;
import java.awt.peer.CheckboxMenuItemPeer;
import java.awt.peer.CheckboxPeer;
import java.awt.peer.ChoicePeer;
import java.awt.peer.ComponentPeer;
import java.awt.peer.DialogPeer;
import java.awt.peer.FileDialogPeer;
import java.awt.peer.FramePeer;
import java.awt.peer.LabelPeer;
import java.awt.peer.LightweightPeer;
import java.awt.peer.ListPeer;
import java.awt.peer.MenuBarPeer;
import java.awt.peer.MenuItemPeer;
import java.awt.peer.MenuPeer;
import java.awt.peer.PanelPeer;
import java.awt.peer.PopupMenuPeer;
import java.awt.peer.ScrollPanePeer;
import java.awt.peer.ScrollbarPeer;
import java.awt.peer.TextAreaPeer;
import java.awt.peer.TextFieldPeer;
import java.awt.peer.WindowPeer;
import java.beans.PropertyVetoException;
import java.util.WeakHashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import org.jnode.awt.JNodeAwtContext;
import org.jnode.awt.JNodeToolkit;
import org.jnode.vm.annotation.SharedStatics;
import sun.awt.AWTAutoShutdown;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

/**
 * AWT toolkit implemented entirely with JFC peers, thus allowing a lightweight
 * simulation of the operating system desktop.
 *
 * @author Levente S\u00e1ntha
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@SharedStatics
public final class SwingToolkit extends JNodeToolkit {

    /**
     * An empty border
     */
    static final Border EMPTY_BORDER = new EmptyBorder(0, 0, 0, 0);

    /**
     * My repaint manager.  Only valid between onInitialize and onClose
     */
    private RepaintManager repaintManager;

    public static void add(Component component, JComponent peer) {
        final ISwingContainerPeer containerPeer = getContainerPeer(component);
        if (containerPeer != null) {
            containerPeer.addAWTComponent(component, peer);
        }
    }

    /**
     * Copies the generic component properties from the AWT component into the
     * peer.
     *
     * @param awtComponent
     * @param peer
     */
    static void copyAwtProperties(Component awtComponent, Component peer) {
        Color c;
        Font f;
        if ((c = awtComponent.getForeground()) != null) {
            peer.setForeground(c);
        }
        if ((c = awtComponent.getBackground()) != null) {
            peer.setBackground(c);
        }
        if ((f = awtComponent.getFont()) != null) {
            peer.setFont(f);
        }
    }

    @SuppressWarnings("deprecation")
    private static ISwingContainerPeer getContainerPeer(Component component) {
        final Component parent = component.getParent();
        if (parent == null) {
            return null;
        } else {
            final ComponentPeer parentPeer = parent.getPeer();
            if (parentPeer instanceof ISwingContainerPeer) {
                return (ISwingContainerPeer) parentPeer;
            } else {
                return getContainerPeer(parent);
            }
        }
    }

    /**
     * Paint all the lightweight children of the given container.
     *
     * @param awtContainer
     * @param g
     */
    public static void paintLightWeightChildren(Container awtContainer,
                                                Graphics g, int dx, int dy) {
        final Shape oldClip = g.getClip();
        try {
            final Component[] comps = awtContainer.getComponents();
            final int cnt = comps.length;
            for (int i = 0; i < cnt; i++) {
                final Component child = comps[i];
                if (child.isVisible() && child.isLightweight()) {
                    final int x = child.getX() - dx;
                    final int y = child.getY() - dy;
                    final int width = child.getWidth();
                    final int height = child.getHeight();
                    g.setClip(x, y, width, height);
                    g.translate(x, y);
                    try {
                        child.paint(g);
                    } finally {
                        g.translate(-x, -y);
                    }
                }
            }
        } finally {
            g.setClip(oldClip);
        }
    }

    private DesktopFrame desktopFrame;

    /**
     * Initialize this instance.
     */
    public SwingToolkit() {
    }

    // Peers

    protected ButtonPeer createButton(Button target) {
        return new SwingButtonPeer(this, target);
    }

    protected CanvasPeer createCanvas(Canvas target) {
        // return super.createCanvas( target );
        return new SwingCanvasPeer(this, target);
    }

    protected CheckboxPeer createCheckbox(Checkbox target) {
        return new SwingCheckboxPeer(this, target);
    }

    protected CheckboxMenuItemPeer createCheckboxMenuItem(
        CheckboxMenuItem target) {
        return new SwingCheckboxMenuItemPeer(this, target);
    }

    protected ChoicePeer createChoice(Choice target) {
        return new SwingChoicePeer(this, target);
    }

    protected LightweightPeer createComponent(Component target) {
        //todo investigate the idea used in super for applying here
        if (target instanceof Container)
            return new SwingLightweightContainerPeer(this, (Container) target);
        else
            return new SwingLightweightPeer(this, target);
    }

    protected DialogPeer createDialog(Dialog target) {
        return new SwingDialogPeer(this, target);
    }

    public DragSourceContextPeer createDragSourceContextPeer(
        DragGestureEvent dge) {
        return null;
    }

    protected FileDialogPeer createFileDialog(FileDialog target) {
        return null;
    }

    protected FramePeer createFrame(final Frame target) {
        final FramePeer[] ret = new FramePeer[1];

        Runnable run = new Runnable() {
            public void run() {

                if (!isGuiActive()) {
                    //throw new AWTError("AWT is currently not available");
                    initGui();
                }
                if (target instanceof DesktopFrame) {
                    setTop(target);
                    log.debug("createFrame:desktopFramePeer(" + target + ")");
                    // Only desktop is real frame
                    //return new DesktopFramePeer(SwingToolkit.this, (DesktopFrame) target);
                    synchronized (ret) {
                        ret[0] = new DesktopFramePeer(SwingToolkit.this, (DesktopFrame) target);
                        try {
                            Method method = AWTAutoShutdown.class.getDeclaredMethod("registerPeer",
                                Object.class, Object.class);
                            method.setAccessible(true);
                            method.invoke(AWTAutoShutdown.getInstance(), target, ret[0]);
                        } catch (Exception x) {
                            x.printStackTrace();
                        }
                    }
                } else /*if (target instanceof JFrame) */ {
                    if (!isGuiActive()) {
                        throw new AWTError("Gui is not active");
                    }
                    log.debug("createFrame:normal(" + target + ")");
                    // Other frames are emulated
                    //return new SwingFramePeer(SwingToolkit.this, target);
                    synchronized (ret) {
                        ret[0] = new SwingFramePeer(SwingToolkit.this, target);
                    }
                } /*else {
            if (!isGuiActive()) {
                throw new AWTError("Gui is not active");
            }
            log.debug("createFrame:normal(" + target + ")");
            // Other frames are emulated
            return new SwingJFramePeer(this, target);
        }   */
            }
        };

        //peer frames should be created in the same app context where the desktop is
        //todo refactor this into a generic inter-appcontext invoke and wait
        AppContext ac = SunToolkit.targetToAppContext(target);
        if (ac != null) {
            EventQueue eq = (EventQueue) ac.get(sun.awt.AppContext.EVENT_QUEUE_KEY);
            if (eq != null) {
                try {
                    Method met = EventQueue.class.getDeclaredMethod("initDispatchThread");
                    met.setAccessible(true);
                    met.invoke(eq);
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        }

        // invoke and wait --
        EventQueue eq = getMainEventQueue();

        if (eq == getSystemEventQueueImpl()) {
            run.run();
            synchronized (ret) {
                return ret[0];
            }
        }

        try {
            Field field = EventQueue.class.getField("dispatchThread");
            field.setAccessible(true);
            Thread edt = (Thread) field.get(eq);
            if (Thread.currentThread() == edt) {
                run.run();
                synchronized (ret) {
                    return ret[0];
                }
            }
        } catch (Exception x) {
            throw new RuntimeException(x);
        }

        class AWTInvocationLock {
        }
        Object lock = new AWTInvocationLock();

        InvocationEvent event = new InvocationEvent(Toolkit.getDefaultToolkit(), run, lock, true);

        try {
            synchronized (lock) {
                eq.postEvent(event);
                lock.wait();
            }
        } catch (Exception x) {
            throw new RuntimeException(x);
        }

        Throwable eventThrowable = event.getThrowable();
        if (eventThrowable != null) {
            throw new RuntimeException(eventThrowable);
        }

        // --invoke and wait

        synchronized (ret) {
            return ret[0];
        }
    }

    protected LabelPeer createLabel(Label target) {
        return new SwingLabelPeer(this, target);
    }

    protected ListPeer createList(java.awt.List target) {
        return new SwingListPeer(this, target);
    }

    protected MenuPeer createMenu(Menu target) {
        return new SwingMenuPeer(this, target);
    }

    protected MenuBarPeer createMenuBar(MenuBar target) {
        return new SwingMenuBarPeer(this, target);
    }

    protected MenuItemPeer createMenuItem(MenuItem target) {
        return new SwingMenuItemPeer(this, target);
    }

    protected PanelPeer createPanel(Panel target) {
        return new SwingPanelPeer(this, target);
    }

    protected PopupMenuPeer createPopupMenu(PopupMenu target) {
        return new SwingPopupMenuPeer(this, target);
    }

    protected ScrollbarPeer createScrollbar(Scrollbar target) {
        return new SwingScrollbarPeer(this, target);
    }

    protected ScrollPanePeer createScrollPane(ScrollPane target) {
        return new SwingScrollPanePeer(this, target);
    }

    protected TextAreaPeer createTextArea(TextArea target) {
        return new SwingTextAreaPeer(this, target);
    }

    protected TextFieldPeer createTextField(TextField target) {
        return new SwingTextFieldPeer(this, target);
    }

    protected WindowPeer createWindow(Window target) {
        return new SwingWindowPeer(this, target);
    }

    public JNodeAwtContext getAwtContext() {
        return desktopFrame;
    }

    /**
     * @see org.jnode.awt.JNodeToolkit#refresh()
     */
    protected void refresh() {
        log.info("Refresh");
        final JNodeAwtContext ctx = getAwtContext();
        if (ctx == null) {
            log.info("Refresh: no AWT context");
            return;
        }
        final Container root = ctx.getAwtRoot();
        if (root != null) {
            root.repaint();
        } else {
            log.info("Refresh: no AWT root");
        }
    }

    public Component getTopComponentAt(int x, int y) {
        if (desktopFrame == null) {
            //no AWT yet, drop the event
            return null;
        }
        Component comp = desktopFrame.getComponentAt(x, y);
        if (comp instanceof SwingBaseWindow) {
            SwingBaseWindow base = (SwingBaseWindow) comp;
            if (base.isShowing()) {
                Window w = base.getAWTComponent();
                if (w instanceof Frame) {
                    MenuBar mb = ((Frame) w).getMenuBar();
                    if (mb != null) {
                        JMenuBar jmb = ((SwingMenuBarPeer) mb.getPeer()).jComponent;
                        Point p = new Point(x, y);
                        SwingUtilities.convertPointFromScreen(p, jmb);
                        comp = SwingUtilities.getDeepestComponentAt(jmb, p.x, p.y);
                        if (comp != null && (comp != jmb || jmb.contains(p.x, p.y))) {
                            return comp;
                        }
                    }
                }
                Point p = new Point(x, y);
                SwingUtilities.convertPointFromScreen(p, w);
                comp = SwingUtilities.getDeepestComponentAt(w, p.x, p.y);
                if (comp == w) {
                    p = new Point(x, y);
                    SwingUtilities.convertPointFromScreen(p, base);
                    comp = SwingUtilities.getDeepestComponentAt(base, p.x, p.y);
                }
            }
        } else {
            comp = super.getTopComponentAt(x, y);
            SwingBaseWindow window = (SwingBaseWindow) SwingUtilities.getAncestorOfClass(SwingBaseWindow.class, comp);
            if (window != null) {
                Rectangle r = window.getBounds();
                Insets ins = window.getSwingPeer().getInsets();
                r.x = r.x + ins.left;
                r.y = r.y + ins.top;
                r.width = r.width - ins.left - ins.right;
                r.height = r.height - ins.top - ins.bottom;
                if (r.contains(x, y)) {
                    Component c = window.getAWTComponent().findComponentAt(x - r.x + ins.left, y - r.y + ins.top);
                    if (c != null) {
                        comp = c;
                    }
                }
            }
        }
        return comp;
    }

    public void activateWindow(Component comp) {
        if (comp == null) return;

        Window w = SwingUtilities.getWindowAncestor(comp);
        if (w == null) return;

        WindowPeer p = (WindowPeer) w.getPeer();
        if (p instanceof SwingBaseWindowPeer) {
            JInternalFrame f = (JInternalFrame) ((SwingBaseWindowPeer) p).peerComponent;
            if (f.isShowing() && !f.isSelected()) {
                try {
                    f.setSelected(true);
                } catch (PropertyVetoException pve) {
                    //ignore
                }
            }
        }
    }

    /**
     * @see org.jnode.awt.JNodeToolkit#onClose()
     */
    protected void onClose() {
        log.debug("onClose");
        // Stop the repaint manager
        if (repaintManager != null) {
            //repaintManager.shutdown();
            repaintManager = null;
        }

        // sometime when the start of wat has failed, desktopFrame can be null
        // so, we must check it is not null
        if (desktopFrame != null) {
            // Close the desktop
            desktopFrame.dispose();
            desktopFrame = null;
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////
    // Private

    final void onDisposeFrame(SwingBaseWindowPeer windowPeer) {
        // Nothing to do
    }

    /**
     * @see org.jnode.awt.JNodeToolkit#onInitialize()
     */
    protected void onInitialize() {
        log.debug("onInitialize");

        // Set the repaint manager

        RepaintManager.setCurrentManager(repaintManager = new RepaintManager() {
            private WeakHashMap<Component, BufferedImage> bufferMap = new WeakHashMap<Component, BufferedImage>();
            private final Component DEFA_KEY = new Component() {
            };

            @Override
            public Image getOffscreenBuffer(Component c, int proposedWidth, int proposedHeight) {
                BufferedImage buffer = bufferMap.get(c);
                if (buffer == null || buffer.getWidth() < proposedWidth || buffer.getHeight() < proposedHeight) {
                    buffer = new BufferedImage(proposedWidth, proposedHeight, BufferedImage.TYPE_INT_ARGB);
                    bufferMap.put(c, buffer);
                }

                return buffer;
            }
        });

        try {
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception x) {
            log.warn("Look And Feel not found: ", x);
        }
        // Create the desktop
        desktopFrame = new DesktopFrame(getScreenSize());
        desktopFrame.show();
    }

    protected void onResize() {
        //nothing to do here yet
    }

    public boolean isWindow(Component comp) {
        return comp instanceof SwingWindow;
    }

    /**
     * Sets the source of the event to the given component.
     */
    static <T extends AWTEvent> T convertEvent(T event, Component awtComponent) {
        event.setSource(awtComponent);
        return event;
    }

    /**
     * Run the runnable now, if the current thread is the event dispatch thread,
     * otherwise invoke it on the event thread.
     */
    public static void invokeNowOrLater(final Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    /*
     * Modality
     */
    public boolean isModalityTypeSupported(Dialog.ModalityType modalityType) {
        //todo implement it
        return false;
    }

}

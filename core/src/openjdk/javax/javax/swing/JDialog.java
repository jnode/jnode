/*
 * Copyright 1997-2006 Sun Microsystems, Inc.  All Rights Reserved.
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
package javax.swing;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.Vector;
import java.io.Serializable;
import javax.accessibility.*;
import java.applet.Applet;

/** 
 * The main class for creating a dialog window. You can use this class
 * to create a custom dialog, or invoke the many class methods
 * in {@link JOptionPane} to create a variety of standard dialogs.
 * For information about creating dialogs, see
 * <em>The Java Tutorial</em> section
 * <a
 href="http://java.sun.com/docs/books/tutorial/uiswing/components/dialog.html">How
 * to Make Dialogs</a>.
 *
 * <p>
 *
 * The <code>JDialog</code> component contains a <code>JRootPane</code>
 * as its only child.
 * The <code>contentPane</code> should be the parent of any children of the
 * <code>JDialog</code>. 
 * As a convenience <code>add</code> and its variants, <code>remove</code> and
 * <code>setLayout</code> have been overridden to forward to the
 * <code>contentPane</code> as necessary. This means you can write:
 * <pre>
 *       dialog.add(child);
 * </pre>
 * And the child will be added to the contentPane.
 * The <code>contentPane</code> is always non-<code>null</code>.
 * Attempting to set it to <code>null</code> generates an exception.
 * The default <code>contentPane</code> has a <code>BorderLayout</code>
 * manager set on it. 
 * Refer to {@link javax.swing.RootPaneContainer}
 * for details on adding, removing and setting the <code>LayoutManager</code>
 * of a <code>JDialog</code>.
 * <p>
 * Please see the <code>JRootPane</code> documentation for a complete 
 * description of the <code>contentPane</code>, <code>glassPane</code>, 
 * and <code>layeredPane</code> components.
 * <p>
 * In a multi-screen environment, you can create a <code>JDialog</code>
 * on a different screen device than its owner.  See {@link java.awt.Frame} for
 * more information.
 * <p>
 * <strong>Warning:</strong> Swing is not thread safe. For more
 * information see <a
 * href="package-summary.html#threading">Swing's Threading
 * Policy</a>.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @see JOptionPane
 * @see JRootPane
 * @see javax.swing.RootPaneContainer
 *
 * @beaninfo
 *      attribute: isContainer true
 *      attribute: containerDelegate getContentPane
 *    description: A toplevel window for creating dialog boxes.
 *
 * @author David Kloba
 * @author James Gosling
 * @author Scott Violet
 */
public class JDialog extends Dialog implements WindowConstants,
                                               Accessible,
                                               RootPaneContainer,
                               TransferHandler.HasGetTransferHandler
{
    /**
     * Key into the AppContext, used to check if should provide decorations
     * by default.
     */
    private static final Object defaultLookAndFeelDecoratedKey = 
    	    new StringBuffer("JDialog.defaultLookAndFeelDecorated");

    private int defaultCloseOperation = HIDE_ON_CLOSE;
    
    /**
     * @see #getRootPane
     * @see #setRootPane
     */
    protected JRootPane rootPane;

    /**
     * If true then calls to <code>add</code> and <code>setLayout</code>
     * will be forwarded to the <code>contentPane</code>. This is initially
     * false, but is set to true when the <code>JDialog</code> is constructed.
     *
     * @see #isRootPaneCheckingEnabled
     * @see #setRootPaneCheckingEnabled
     * @see javax.swing.RootPaneContainer
     */
    protected boolean rootPaneCheckingEnabled = false;

    /**
     * The <code>TransferHandler</code> for this dialog.
     */
    private TransferHandler transferHandler;

    /**
     * Creates a modeless dialog without a title and without a specified
     * <code>Frame</code> owner.  A shared, hidden frame will be
     * set as the owner of the dialog.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.     
     * <p>
     * NOTE: This constructor does not allow you to create an unowned
     * <code>JDialog</code>. To create an unowned <code>JDialog</code>
     * you must use either the <code>JDialog(Window)</code> or
     * <code>JDialog(Dialog)</code> constructor with an argument of
     * <code>null</code>.
     * 
     * @exception HeadlessException if <code>GraphicsEnvironment.isHeadless()</code>
     *     returns <code>true</code>.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     */
    public JDialog() {
        this((Frame)null, false);
    }

    /**
     * Creates a modeless dialog without a title with the
     * specified <code>Frame</code> as its owner.  If <code>owner</code>
     * is <code>null</code>, a shared, hidden frame will be set as the
     * owner of the dialog.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     * <p>
     * NOTE: This constructor does not allow you to create an unowned
     * <code>JDialog</code>. To create an unowned <code>JDialog</code>
     * you must use either the <code>JDialog(Window)</code> or
     * <code>JDialog(Dialog)</code> constructor with an argument of
     * <code>null</code>.
     *
     * @param owner the <code>Frame</code> from which the dialog is displayed
     * @exception HeadlessException if <code>GraphicsEnvironment.isHeadless()</code>
     *     returns <code>true</code>.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     */
    public JDialog(Frame owner) {
        this(owner, false);
    }

    /**
     * Creates a dialog with the specified owner <code>Frame</code>, modality
     * and an empty title. If <code>owner</code> is <code>null</code>, 
     * a shared, hidden frame will be set as the owner of the dialog.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.     
     * <p>
     * NOTE: This constructor does not allow you to create an unowned
     * <code>JDialog</code>. To create an unowned <code>JDialog</code>
     * you must use either the <code>JDialog(Window)</code> or
     * <code>JDialog(Dialog)</code> constructor with an argument of
     * <code>null</code>.
     *
     * @param owner the <code>Frame</code> from which the dialog is displayed
     * @param modal specifies whether dialog blocks user input to other top-level 
     *     windows when shown. If <code>true</code>, the modality type property is set to 
     *     <code>DEFAULT_MODALITY_TYPE</code>, otherwise the dialog is modeless.     
     * @exception HeadlessException if <code>GraphicsEnvironment.isHeadless()</code>
     *     returns <code>true</code>.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     */
    public JDialog(Frame owner, boolean modal) {
        this(owner, null, modal);
    }

    /**
     * Creates a modeless dialog with the specified title and
     * with the specified owner frame.  If <code>owner</code>
     * is <code>null</code>, a shared, hidden frame will be set as the
     * owner of the dialog.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.     
     * <p>
     * NOTE: This constructor does not allow you to create an unowned
     * <code>JDialog</code>. To create an unowned <code>JDialog</code>
     * you must use either the <code>JDialog(Window)</code> or
     * <code>JDialog(Dialog)</code> constructor with an argument of
     * <code>null</code>.
     *
     * @param owner the <code>Frame</code> from which the dialog is displayed
     * @param title  the <code>String</code> to display in the dialog's
     *			title bar
     * @exception HeadlessException if <code>GraphicsEnvironment.isHeadless()</code>
     *     returns <code>true</code>.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     */
    public JDialog(Frame owner, String title) {
        this(owner, title, false);     
    }

    /**
     * Creates a dialog with the specified title, owner <code>Frame</code>
     * and modality. If <code>owner</code> is <code>null</code>, 
     * a shared, hidden frame will be set as the owner of this dialog.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.     
     * <p>
     * NOTE: Any popup components (<code>JComboBox</code>,
     * <code>JPopupMenu</code>, <code>JMenuBar</code>)
     * created within a modal dialog will be forced to be lightweight.
     * <p>
     * NOTE: This constructor does not allow you to create an unowned
     * <code>JDialog</code>. To create an unowned <code>JDialog</code>
     * you must use either the <code>JDialog(Window)</code> or
     * <code>JDialog(Dialog)</code> constructor with an argument of
     * <code>null</code>.
     *
     * @param owner the <code>Frame</code> from which the dialog is displayed
     * @param title  the <code>String</code> to display in the dialog's
     *     title bar
     * @param modal specifies whether dialog blocks user input to other top-level 
     *     windows when shown. If <code>true</code>, the modality type property is set to 
     *     <code>DEFAULT_MODALITY_TYPE</code> otherwise the dialog is modeless 
     * @exception HeadlessException if <code>GraphicsEnvironment.isHeadless()</code>
     *     returns <code>true</code>.
     * 
     * @see java.awt.Dialog.ModalityType 
     * @see java.awt.Dialog.ModalityType#MODELESS 
     * @see java.awt.Dialog#DEFAULT_MODALITY_TYPE 
     * @see java.awt.Dialog#setModal 
     * @see java.awt.Dialog#setModalityType 
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     */
    public JDialog(Frame owner, String title, boolean modal) {
        super(owner == null? SwingUtilities.getSharedOwnerFrame() : owner, 
              title, modal);
 	if (owner == null) {
	    WindowListener ownerShutdownListener =
		(WindowListener)SwingUtilities.getSharedOwnerFrameShutdownListener();
 	    addWindowListener(ownerShutdownListener);
 	}
        dialogInit();
    }

    /**
     * Creates a dialog with the specified title, 
     * owner <code>Frame</code>, modality and <code>GraphicsConfiguration</code>.
     * If <code>owner</code> is <code>null</code>, 
     * a shared, hidden frame will be set as the owner of this dialog.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.     
     * <p>
     * NOTE: Any popup components (<code>JComboBox</code>,
     * <code>JPopupMenu</code>, <code>JMenuBar</code>)
     * created within a modal dialog will be forced to be lightweight.
     * <p>
     * NOTE: This constructor does not allow you to create an unowned
     * <code>JDialog</code>. To create an unowned <code>JDialog</code>
     * you must use either the <code>JDialog(Window)</code> or
     * <code>JDialog(Dialog)</code> constructor with an argument of
     * <code>null</code>.
     *
     * @param owner the <code>Frame</code> from which the dialog is displayed
     * @param title  the <code>String</code> to display in the dialog's
     *     title bar
     * @param modal specifies whether dialog blocks user input to other top-level 
     *     windows when shown. If <code>true</code>, the modality type property is set to 
     *     <code>DEFAULT_MODALITY_TYPE</code>, otherwise the dialog is modeless. 
     * @param gc the <code>GraphicsConfiguration</code> 
     *     of the target screen device.  If <code>gc</code> is 
     *     <code>null</code>, the same
     *     <code>GraphicsConfiguration</code> as the owning Frame is used.    
     * @exception HeadlessException if <code>GraphicsEnvironment.isHeadless()</code>
     *     returns <code>true</code>.       
     * @see java.awt.Dialog.ModalityType 
     * @see java.awt.Dialog.ModalityType#MODELESS 
     * @see java.awt.Dialog#DEFAULT_MODALITY_TYPE 
     * @see java.awt.Dialog#setModal 
     * @see java.awt.Dialog#setModalityType 
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     * @since 1.4
     */
    public JDialog(Frame owner, String title, boolean modal,
                   GraphicsConfiguration gc) {
        super(owner == null? SwingUtilities.getSharedOwnerFrame() : owner, 
              title, modal, gc);
 	if (owner == null) {
	    WindowListener ownerShutdownListener =
		(WindowListener)SwingUtilities.getSharedOwnerFrameShutdownListener();
 	    addWindowListener(ownerShutdownListener);
 	}
        dialogInit();
    }

    /**
     * Creates a modeless dialog without a title with the
     * specified <code>Dialog</code> as its owner.
     * <p>
     * This constructor sets the component's locale property to the value 
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @param owner the owner <code>Dialog</code> from which the dialog is displayed
     *     or <code>null</code> if this dialog has no owner
     * @exception HeadlessException <code>if GraphicsEnvironment.isHeadless()</code>
     *     returns <code>true</code>.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     */
    public JDialog(Dialog owner) {
        this(owner, false);
    }

    /**
     * Creates a dialog with the specified owner <code>Dialog</code> and modality.
     * <p>
     * This constructor sets the component's locale property to the value 
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @param owner the owner <code>Dialog</code> from which the dialog is displayed
     *     or <code>null</code> if this dialog has no owner
     * @param modal specifies whether dialog blocks user input to other top-level 
     *     windows when shown. If <code>true</code>, the modality type property is set to 
     *     <code>DEFAULT_MODALITY_TYPE</code>, otherwise the dialog is modeless.  
     * @exception HeadlessException if <code>GraphicsEnvironment.isHeadless()</code>
     *     returns <code>true</code>.
     * @see java.awt.Dialog.ModalityType 
     * @see java.awt.Dialog.ModalityType#MODELESS 
     * @see java.awt.Dialog#DEFAULT_MODALITY_TYPE 
     * @see java.awt.Dialog#setModal 
     * @see java.awt.Dialog#setModalityType 
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     */
    public JDialog(Dialog owner, boolean modal) {
        this(owner, null, modal);
    }

    /**
     * Creates a modeless dialog with the specified title and
     * with the specified owner dialog.
     * <p>
     * This constructor sets the component's locale property to the value 
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @param owner the owner <code>Dialog</code> from which the dialog is displayed
     *     or <code>null</code> if this dialog has no owner
     * @param title  the <code>String</code> to display in the dialog's
     *			title bar
     * @exception HeadlessException if <code>GraphicsEnvironment.isHeadless()</code>
     *     returns <code>true</code>.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     */
    public JDialog(Dialog owner, String title) {
        this(owner, title, false);     
    }

    /**
     * Creates a dialog with the specified title, modality 
     * and the specified owner <code>Dialog</code>. 
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.     
     *
     * @param owner the owner <code>Dialog</code> from which the dialog is displayed
     *     or <code>null</code> if this dialog has no owner
     * @param title  the <code>String</code> to display in the dialog's
     *	   title bar
     * @param modal specifies whether dialog blocks user input to other top-level 
     *     windows when shown. If <code>true</code>, the modality type property is set to 
     *     <code>DEFAULT_MODALITY_TYPE</code>, otherwise the dialog is modeless 
     * @exception HeadlessException if <code>GraphicsEnvironment.isHeadless()</code>
     *     returns <code>true</code>.
     * @see java.awt.Dialog.ModalityType 
     * @see java.awt.Dialog.ModalityType#MODELESS 
     * @see java.awt.Dialog#DEFAULT_MODALITY_TYPE 
     * @see java.awt.Dialog#setModal 
     * @see java.awt.Dialog#setModalityType 
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     */
    public JDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
        dialogInit();
    }

    /**
     * Creates a dialog with the specified title, owner <code>Dialog</code>, 
     * modality and <code>GraphicsConfiguration</code>.
     * 
     * <p>
     * NOTE: Any popup components (<code>JComboBox</code>,
     * <code>JPopupMenu</code>, <code>JMenuBar</code>)
     * created within a modal dialog will be forced to be lightweight.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.     
     *
     * @param owner the owner <code>Dialog</code> from which the dialog is displayed
     *     or <code>null</code> if this dialog has no owner
     * @param title  the <code>String</code> to display in the dialog's
     *     title bar
     * @param modal specifies whether dialog blocks user input to other top-level 
     *     windows when shown. If <code>true</code>, the modality type property is set to 
     *     <code>DEFAULT_MODALITY_TYPE</code>, otherwise the dialog is modeless 
     * @param gc the <code>GraphicsConfiguration</code> 
     *     of the target screen device.  If <code>gc</code> is 
     *     <code>null</code>, the same
     *     <code>GraphicsConfiguration</code> as the owning Dialog is used.    
     * @exception HeadlessException if <code>GraphicsEnvironment.isHeadless()</code> 
     *     returns <code>true</code>.
     * @see java.awt.Dialog.ModalityType 
     * @see java.awt.Dialog.ModalityType#MODELESS 
     * @see java.awt.Dialog#DEFAULT_MODALITY_TYPE 
     * @see java.awt.Dialog#setModal 
     * @see java.awt.Dialog#setModalityType 
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     * @since 1.4
     */
    public JDialog(Dialog owner, String title, boolean modal,
                   GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
        dialogInit();
    }
    
    /**
     * Creates a modeless dialog with the specified owner <code>Window</code> and
     * an empty title.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @param owner the <code>Window</code> from which the dialog is displayed or
     *     <code>null</code> if this dialog has no owner
     * @exception HeadlessException when
     *    <code>GraphicsEnvironment.isHeadless()</code> returns <code>true</code>
     *
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     * 
     * @since 1.6
     */
    public JDialog(Window owner) {
        this(owner, Dialog.ModalityType.MODELESS);
    }
    
    /**
     * Creates a dialog with the specified owner <code>Window</code>, modality
     * and an empty title.
     * <p>
     * This constructor sets the component's locale property to the value 
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @param owner the <code>Window</code> from which the dialog is displayed or
     *     <code>null</code> if this dialog has no owner
     * @param modalityType specifies whether dialog blocks input to other
     *     windows when shown. <code>null</code> value and unsupported modality
     *     types are equivalent to <code>MODELESS</code>
     * @exception HeadlessException when
     *    <code>GraphicsEnvironment.isHeadless()</code> returns <code>true</code>
     *
     * @see java.awt.Dialog.ModalityType
     * @see java.awt.Dialog#setModal
     * @see java.awt.Dialog#setModalityType
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     * 
     * @since 1.6
     */
    public JDialog(Window owner, ModalityType modalityType) {
        this(owner, null, modalityType);
    }
    
    /**
     * Creates a modeless dialog with the specified title and owner
     * <code>Window</code>.
     * <p>
     * This constructor sets the component's locale property to the value 
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @param owner the <code>Window</code> from which the dialog is displayed or
     *     <code>null</code> if this dialog has no owner
     * @param title the <code>String</code> to display in the dialog's
     *     title bar or <code>null</code> if the dialog has no title
     * @exception java.awt.HeadlessException when
     *     <code>GraphicsEnvironment.isHeadless()</code> returns <code>true</code>
     *
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     * 
     * @since 1.6
     */
    public JDialog(Window owner, String title) {
        this(owner, title, Dialog.ModalityType.MODELESS);     
    }
    
    /**
     * Creates a dialog with the specified title, owner <code>Window</code> and
     * modality.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.     
     *
     * @param owner the <code>Window</code> from which the dialog is displayed or
     *     <code>null</code> if this dialog has no owner
     * @param title the <code>String</code> to display in the dialog's
     *     title bar or <code>null</code> if the dialog has no title
     * @param modalityType specifies whether dialog blocks input to other
     *     windows when shown. <code>null</code> value and unsupported modality
     *     types are equivalent to <code>MODELESS</code>
     * @exception java.awt.HeadlessException when
     *     <code>GraphicsEnvironment.isHeadless()</code> returns <code>true</code>
     *
     * @see java.awt.Dialog.ModalityType
     * @see java.awt.Dialog#setModal
     * @see java.awt.Dialog#setModalityType
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     * 
     * @since 1.6
     */
    public JDialog(Window owner, String title, Dialog.ModalityType modalityType) {
        super(owner, title, modalityType);
        dialogInit();
    }
    
    /**
     * Creates a dialog with the specified title, owner <code>Window</code>,
     * modality and <code>GraphicsConfiguration</code>.
     * <p>
     * NOTE: Any popup components (<code>JComboBox</code>,
     * <code>JPopupMenu</code>, <code>JMenuBar</code>)
     * created within a modal dialog will be forced to be lightweight.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.     
     *
     * @param owner the <code>Window</code> from which the dialog is displayed or
     *     <code>null</code> if this dialog has no owner
     * @param title the <code>String</code> to display in the dialog's
     *     title bar or <code>null</code> if the dialog has no title
     * @param modalityType specifies whether dialog blocks input to other
     *     windows when shown. <code>null</code> value and unsupported modality
     *     types are equivalent to <code>MODELESS</code>
     * @param gc the <code>GraphicsConfiguration</code> of the target screen device;
     *     if <code>null</code>, the <code>GraphicsConfiguration</code> from the owning
     *     window is used; if <code>owner</code> is also <code>null</code>, the
     *     system default <code>GraphicsConfiguration</code> is assumed
     * @exception java.awt.HeadlessException when
     *    <code>GraphicsEnvironment.isHeadless()</code> returns <code>true</code>
     *
     * @see java.awt.Dialog.ModalityType
     * @see java.awt.Dialog#setModal
     * @see java.awt.Dialog#setModalityType
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     *
     * @since 1.6
     */
    public JDialog(Window owner, String title, Dialog.ModalityType modalityType,
                   GraphicsConfiguration gc) {
        super(owner, title, modalityType, gc);
        dialogInit();
    }    

    /**
     * Called by the constructors to init the <code>JDialog</code> properly.
     */
    protected void dialogInit() {
        enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);
        setLocale( JComponent.getDefaultLocale() );
        setRootPane(createRootPane());
        setRootPaneCheckingEnabled(true);
        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations = 
            UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
                setUndecorated(true);
                getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
            }
        }
        sun.awt.SunToolkit.checkAndSetPolicy(this, true);
    }

    /**
     * Called by the constructor methods to create the default
     * <code>rootPane</code>.
     */
    protected JRootPane createRootPane() {
        JRootPane rp = new JRootPane();
        // NOTE: this uses setOpaque vs LookAndFeel.installProperty as there
        // is NO reason for the RootPane not to be opaque. For painting to
        // work the contentPane must be opaque, therefor the RootPane can
        // also be opaque.
        rp.setOpaque(true);
        return rp;
    }

    /**
     * Handles window events depending on the state of the
     * <code>defaultCloseOperation</code> property.
     *
     * @see #setDefaultCloseOperation
     */
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            switch(defaultCloseOperation) {
              case HIDE_ON_CLOSE:
                 setVisible(false);
                 break;
              case DISPOSE_ON_CLOSE:
                 dispose();
                 break;
              case DO_NOTHING_ON_CLOSE:
                 default: 
                 break;
            }
        }
    }
 

    /**                   
     * Sets the operation that will happen by default when
     * the user initiates a "close" on this dialog.
     * You must specify one of the following choices:
     * <p>
     * <ul>
     * <li><code>DO_NOTHING_ON_CLOSE</code>
     * (defined in <code>WindowConstants</code>):
     * Don't do anything; require the
     * program to handle the operation in the <code>windowClosing</code>
     * method of a registered <code>WindowListener</code> object.
     *
     * <li><code>HIDE_ON_CLOSE</code>
     * (defined in <code>WindowConstants</code>):
     * Automatically hide the dialog after
     * invoking any registered <code>WindowListener</code>
     * objects.
     *
     * <li><code>DISPOSE_ON_CLOSE</code>
     * (defined in <code>WindowConstants</code>):
     * Automatically hide and dispose the 
     * dialog after invoking any registered <code>WindowListener</code>
     * objects.
     * </ul>
     * <p>
     * The value is set to <code>HIDE_ON_CLOSE</code> by default. Changes
     * to the value of this property cause the firing of a property
     * change event, with property name "defaultCloseOperation".
     * <p>
     * <b>Note</b>: When the last displayable window within the
     * Java virtual machine (VM) is disposed of, the VM may
     * terminate.  See <a href="../../java/awt/doc-files/AWTThreadIssues.html">
     * AWT Threading Issues</a> for more information.
     *
     * @param operation the operation which should be performed when the
     *        user closes the dialog
     * @throws IllegalArgumentException if defaultCloseOperation value 
     *         isn't one of the above valid values
     * @see #addWindowListener
     * @see #getDefaultCloseOperation
     * @see WindowConstants
     *
     * @beaninfo
     *   preferred: true
     *       bound: true
     *        enum: DO_NOTHING_ON_CLOSE WindowConstants.DO_NOTHING_ON_CLOSE
     *              HIDE_ON_CLOSE       WindowConstants.HIDE_ON_CLOSE
     *              DISPOSE_ON_CLOSE    WindowConstants.DISPOSE_ON_CLOSE
     * description: The dialog's default close operation.
     */
    public void setDefaultCloseOperation(int operation) {
        if (operation != DO_NOTHING_ON_CLOSE &&
            operation != HIDE_ON_CLOSE &&
            operation != DISPOSE_ON_CLOSE) {
            throw new IllegalArgumentException("defaultCloseOperation must be one of: DO_NOTHING_ON_CLOSE, HIDE_ON_CLOSE, or DISPOSE_ON_CLOSE");
        }

        int oldValue = this.defaultCloseOperation;
        this.defaultCloseOperation = operation;
        firePropertyChange("defaultCloseOperation", oldValue, operation);
    }

   /**
    * Returns the operation which occurs when the user
    * initiates a "close" on this dialog.
    *
    * @return an integer indicating the window-close operation
    * @see #setDefaultCloseOperation
    */
    public int getDefaultCloseOperation() {
        return defaultCloseOperation;
    }

    /**
     * Sets the {@code transferHandler} property, which is a mechanism to
     * support transfer of data into this component. Use {@code null}
     * if the component does not support data transfer operations.
     * <p>
     * If the system property {@code suppressSwingDropSupport} is {@code false}
     * (the default) and the current drop target on this component is either
     * {@code null} or not a user-set drop target, this method will change the
     * drop target as follows: If {@code newHandler} is {@code null} it will
     * clear the drop target. If not {@code null} it will install a new
     * {@code DropTarget}.
     * <p>
     * Note: When used with {@code JDialog}, {@code TransferHandler} only
     * provides data import capability, as the data export related methods
     * are currently typed to {@code JComponent}.
     * <p>
     * Please see
     * <a href="http://java.sun.com/docs/books/tutorial/uiswing/misc/dnd.html">
     * How to Use Drag and Drop and Data Transfer</a>, a section in
     * <em>The Java Tutorial</em>, for more information.
     * 
     * @param newHandler the new {@code TransferHandler}
     *
     * @see TransferHandler
     * @see #getTransferHandler
     * @see java.awt.Component#setDropTarget
     * @since 1.6
     *
     * @beaninfo
     *        bound: true
     *       hidden: true
     *  description: Mechanism for transfer of data into the component
     */
    public void setTransferHandler(TransferHandler newHandler) {
        TransferHandler oldHandler = transferHandler;
        transferHandler = newHandler;
        SwingUtilities.installSwingDropTargetAsNecessary(this, transferHandler);
        firePropertyChange("transferHandler", oldHandler, newHandler);
    }

    /**
     * Gets the <code>transferHandler</code> property.
     *
     * @return the value of the <code>transferHandler</code> property
     *
     * @see TransferHandler
     * @see #setTransferHandler
     * @since 1.6
     */
    public TransferHandler getTransferHandler() {
        return transferHandler;
    }

    /** 
     * Calls <code>paint(g)</code>.  This method was overridden to 
     * prevent an unnecessary call to clear the background.
     *
     * @param g  the <code>Graphics</code> context in which to paint
     */
    public void update(Graphics g) {
        paint(g);
    }

   /**
    * Sets the menubar for this dialog.
    *
    * @param menu the menubar being placed in the dialog
    *
    * @see #getJMenuBar
    *
    * @beaninfo
    *      hidden: true
    * description: The menubar for accessing pulldown menus from this dialog.
    */
    public void setJMenuBar(JMenuBar menu) {
        getRootPane().setMenuBar(menu);
    }

   /**
    * Returns the menubar set on this dialog.
    *
    * @see #setJMenuBar
    */
    public JMenuBar getJMenuBar() { 
        return getRootPane().getMenuBar(); 
    }


    /**
     * Returns whether calls to <code>add</code> and 
     * <code>setLayout</code> are forwarded to the <code>contentPane</code>.
     *
     * @return true if <code>add</code> and <code>setLayout</code> 
     *         are fowarded; false otherwise
     *
     * @see #addImpl
     * @see #setLayout
     * @see #setRootPaneCheckingEnabled
     * @see javax.swing.RootPaneContainer
     */
    protected boolean isRootPaneCheckingEnabled() {
        return rootPaneCheckingEnabled;
    }


    /**
     * Sets whether calls to <code>add</code> and 
     * <code>setLayout</code> are forwarded to the <code>contentPane</code>.
     * 
     * @param enabled  true if <code>add</code> and <code>setLayout</code>
     *        are forwarded, false if they should operate directly on the
     *        <code>JDialog</code>.
     *
     * @see #addImpl
     * @see #setLayout
     * @see #isRootPaneCheckingEnabled
     * @see javax.swing.RootPaneContainer
     * @beaninfo
     *      hidden: true
     * description: Whether the add and setLayout methods are forwarded
     */
    protected void setRootPaneCheckingEnabled(boolean enabled) {
        rootPaneCheckingEnabled = enabled;
    }

    /**
     * Adds the specified child <code>Component</code>.
     * This method is overridden to conditionally forward calls to the
     * <code>contentPane</code>.
     * By default, children are added to the <code>contentPane</code> instead
     * of the frame, refer to {@link javax.swing.RootPaneContainer} for
     * details.
     * 
     * @param comp the component to be enhanced
     * @param constraints the constraints to be respected
     * @param index the index
     * @exception IllegalArgumentException if <code>index</code> is invalid
     * @exception IllegalArgumentException if adding the container's parent
     *			to itself
     * @exception IllegalArgumentException if adding a window to a container
     * 
     * @see #setRootPaneCheckingEnabled
     * @see javax.swing.RootPaneContainer
     */
    protected void addImpl(Component comp, Object constraints, int index) 
    {
        if(isRootPaneCheckingEnabled()) {
            getContentPane().add(comp, constraints, index);
        }
        else {
            super.addImpl(comp, constraints, index);
        }
    }

    /** 
     * Removes the specified component from the container. If
     * <code>comp</code> is not the <code>rootPane</code>, this will forward
     * the call to the <code>contentPane</code>. This will do nothing if
     * <code>comp</code> is not a child of the <code>JDialog</code> or
     * <code>contentPane</code>.
     *
     * @param comp the component to be removed
     * @throws NullPointerException if <code>comp</code> is null
     * @see #add
     * @see javax.swing.RootPaneContainer
     */
    public void remove(Component comp) {
	if (comp == rootPane) {
	    super.remove(comp);
	} else {
	    getContentPane().remove(comp);
	}
    }


    /**
     * Sets the <code>LayoutManager</code>.
     * Overridden to conditionally forward the call to the
     * <code>contentPane</code>.
     * Refer to {@link javax.swing.RootPaneContainer} for
     * more information.
     *
     * @param manager the <code>LayoutManager</code>
     * @see #setRootPaneCheckingEnabled
     * @see javax.swing.RootPaneContainer
     */
    public void setLayout(LayoutManager manager) {
        if(isRootPaneCheckingEnabled()) {
            getContentPane().setLayout(manager);
        }
        else {
            super.setLayout(manager);
        }
    }


    /**
     * Returns the <code>rootPane</code> object for this dialog.
     *
     * @see #setRootPane
     * @see RootPaneContainer#getRootPane
     */
    public JRootPane getRootPane() { 
        return rootPane; 
    }


    /**
     * Sets the <code>rootPane</code> property. 
     * This method is called by the constructor.
     *
     * @param root the <code>rootPane</code> object for this dialog
     *
     * @see #getRootPane
     *
     * @beaninfo
     *   hidden: true
     * description: the RootPane object for this dialog.
     */
    protected void setRootPane(JRootPane root) {
        if(rootPane != null) {
            remove(rootPane);
        }
        rootPane = root;
        if(rootPane != null) {
            boolean checkingEnabled = isRootPaneCheckingEnabled();
            try {
                setRootPaneCheckingEnabled(false);
                add(rootPane, BorderLayout.CENTER);
            }
            finally {
                setRootPaneCheckingEnabled(checkingEnabled);
            }
        }
    }


    /**
     * Returns the <code>contentPane</code> object for this dialog.
     *
     * @return the <code>contentPane</code> property
     *
     * @see #setContentPane
     * @see RootPaneContainer#getContentPane
     */
    public Container getContentPane() { 
        return getRootPane().getContentPane(); 
    }


   /**
     * Sets the <code>contentPane</code> property. 
     * This method is called by the constructor.
     * <p>
     * Swing's painting architecture requires an opaque <code>JComponent</code>
     * in the containment hiearchy. This is typically provided by the
     * content pane. If you replace the content pane it is recommended you
     * replace it with an opaque <code>JComponent</code>.
     * @see JRootPane
     *
     * @param contentPane the <code>contentPane</code> object for this dialog
     *
     * @exception java.awt.IllegalComponentStateException (a runtime
     *            exception) if the content pane parameter is <code>null</code>
     * @see #getContentPane
     * @see RootPaneContainer#setContentPane
     *
     * @beaninfo
     *     hidden: true
     *     description: The client area of the dialog where child 
     *                  components are normally inserted.
     */
    public void setContentPane(Container contentPane) {
        getRootPane().setContentPane(contentPane);
    }

    /**
     * Returns the <code>layeredPane</code> object for this dialog.
     *
     * @return the <code>layeredPane</code> property
     *
     * @see #setLayeredPane
     * @see RootPaneContainer#getLayeredPane
     */
    public JLayeredPane getLayeredPane() { 
        return getRootPane().getLayeredPane(); 
    }

    /**
     * Sets the <code>layeredPane</code> property.  
     * This method is called by the constructor.
     *
     * @param layeredPane the new <code>layeredPane</code> property
     *
     * @exception java.awt.IllegalComponentStateException (a runtime
     *            exception) if the layered pane parameter is null
     * @see #getLayeredPane
     * @see RootPaneContainer#setLayeredPane
     *
     * @beaninfo
     *     hidden: true
     *     description: The pane which holds the various dialog layers.
     */
    public void setLayeredPane(JLayeredPane layeredPane) {
        getRootPane().setLayeredPane(layeredPane);
    }

    /**
     * Returns the <code>glassPane</code> object for this dialog.
     *
     * @return the <code>glassPane</code> property
     *
     * @see #setGlassPane
     * @see RootPaneContainer#getGlassPane
     */
    public Component getGlassPane() { 
        return getRootPane().getGlassPane(); 
    }

    /**
     * Sets the <code>glassPane</code> property. 
     * This method is called by the constructor.
     *
     * @param glassPane the <code>glassPane</code> object for this dialog
     * @see #getGlassPane
     * @see RootPaneContainer#setGlassPane
     *
     * @beaninfo
     *     hidden: true
     *     description: A transparent pane used for menu rendering.
     */
    public void setGlassPane(Component glassPane) {
        getRootPane().setGlassPane(glassPane);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.6
     */
    public Graphics getGraphics() {
        JComponent.getGraphicsInvoked(this);
        return super.getGraphics();
    }

    /**
     * Repaints the specified rectangle of this component within
     * <code>time</code> milliseconds.  Refer to <code>RepaintManager</code>
     * for details on how the repaint is handled.
     * 
     * @param     time   maximum time in milliseconds before update
     * @param     x    the <i>x</i> coordinate
     * @param     y    the <i>y</i> coordinate
     * @param     width    the width
     * @param     height   the height
     * @see       RepaintManager
     * @since     1.6
     */
    public void repaint(long time, int x, int y, int width, int height) {
        if (RepaintManager.HANDLE_TOP_LEVEL_PAINT) {
            RepaintManager.currentManager(this).addDirtyRegion(
                              this, x, y, width, height);
        }
        else {
            super.repaint(time, x, y, width, height);
        }
    }

    /**
     * Provides a hint as to whether or not newly created <code>JDialog</code>s
     * should have their Window decorations (such as borders, widgets to
     * close the window, title...) provided by the current look
     * and feel. If <code>defaultLookAndFeelDecorated</code> is true,
     * the current <code>LookAndFeel</code> supports providing window
     * decorations, and the current window manager supports undecorated
     * windows, then newly created <code>JDialog</code>s will have their
     * Window decorations provided by the current <code>LookAndFeel</code>.
     * Otherwise, newly created <code>JDialog</code>s will have their
     * Window decorations provided by the current window manager.
     * <p>
     * You can get the same effect on a single JDialog by doing the following:
     * <pre>
     *    JDialog dialog = new JDialog();
     *    dialog.setUndecorated(true);
     *    dialog.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
     * </pre>
     *
     * @param defaultLookAndFeelDecorated A hint as to whether or not current
     *        look and feel should provide window decorations
     * @see javax.swing.LookAndFeel#getSupportsWindowDecorations
     * @since 1.4
     */
    public static void setDefaultLookAndFeelDecorated(boolean defaultLookAndFeelDecorated) {
        if (defaultLookAndFeelDecorated) {
            SwingUtilities.appContextPut(defaultLookAndFeelDecoratedKey, Boolean.TRUE);
        } else {
            SwingUtilities.appContextPut(defaultLookAndFeelDecoratedKey, Boolean.FALSE);
        }
    }

    /**
     * Returns true if newly created <code>JDialog</code>s should have their
     * Window decorations provided by the current look and feel. This is only
     * a hint, as certain look and feels may not support this feature.
     *
     * @return true if look and feel should provide Window decorations.
     * @since 1.4
     */
    public static boolean isDefaultLookAndFeelDecorated() {
        Boolean defaultLookAndFeelDecorated = 
            (Boolean) SwingUtilities.appContextGet(defaultLookAndFeelDecoratedKey);
        if (defaultLookAndFeelDecorated == null) {
            defaultLookAndFeelDecorated = Boolean.FALSE;
        }
        return defaultLookAndFeelDecorated.booleanValue();
    }

    /**
     * Returns a string representation of this <code>JDialog</code>.
     * This method 
     * is intended to be used only for debugging purposes, and the 
     * content and format of the returned string may vary between      
     * implementations. The returned string may be empty but may not 
     * be <code>null</code>.
     * 
     * @return  a string representation of this <code>JDialog</code>.
     */
    protected String paramString() {
        String defaultCloseOperationString;
        if (defaultCloseOperation == HIDE_ON_CLOSE) {
            defaultCloseOperationString = "HIDE_ON_CLOSE";
        } else if (defaultCloseOperation == DISPOSE_ON_CLOSE) {
            defaultCloseOperationString = "DISPOSE_ON_CLOSE";
        } else if (defaultCloseOperation == DO_NOTHING_ON_CLOSE) {
            defaultCloseOperationString = "DO_NOTHING_ON_CLOSE";
        } else defaultCloseOperationString = "";
	String rootPaneString = (rootPane != null ?
				 rootPane.toString() : "");
	String rootPaneCheckingEnabledString = (rootPaneCheckingEnabled ?
						"true" : "false");

	return super.paramString() +
	",defaultCloseOperation=" + defaultCloseOperationString +
	",rootPane=" + rootPaneString +
	",rootPaneCheckingEnabled=" + rootPaneCheckingEnabledString;
    }


/////////////////
// Accessibility support
////////////////

    protected AccessibleContext accessibleContext = null;

    /**
     * Gets the AccessibleContext associated with this JDialog. 
     * For JDialogs, the AccessibleContext takes the form of an 
     * AccessibleJDialog. 
     * A new AccessibleJDialog instance is created if necessary.
     *
     * @return an AccessibleJDialog that serves as the 
     *         AccessibleContext of this JDialog
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJDialog();
        }
        return accessibleContext;
    }

    /**
     * This class implements accessibility support for the 
     * <code>JDialog</code> class.  It provides an implementation of the 
     * Java Accessibility API appropriate to dialog user-interface 
     * elements.
     */
    protected class AccessibleJDialog extends AccessibleAWTDialog { 
        
        // AccessibleContext methods
        //
        /**
         * Get the accessible name of this object.  
         *
         * @return the localized name of the object -- can be null if this 
         * object does not have a name
         */
        public String getAccessibleName() {
            if (accessibleName != null) {
                return accessibleName;
            } else {
                if (getTitle() == null) {
                    return super.getAccessibleName();
                } else {
                    return getTitle();
                }
            }
        }

        /**
         * Get the state of this object.
         *
         * @return an instance of AccessibleStateSet containing the current 
         * state set of the object
         * @see AccessibleState
         */
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();

            if (isResizable()) {
                states.add(AccessibleState.RESIZABLE);
            }
            if (getFocusOwner() != null) {
                states.add(AccessibleState.ACTIVE);
            }
            if (isModal()) {
                states.add(AccessibleState.MODAL);
            }
            return states;
        }

    } // inner class AccessibleJDialog
}

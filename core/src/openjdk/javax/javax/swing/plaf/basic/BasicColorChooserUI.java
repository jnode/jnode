/*
 * Copyright 1997-2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.swing.plaf.basic;

import javax.swing.*;
import javax.swing.colorchooser.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

import sun.swing.DefaultLookup;
import sun.swing.UIAction;

/**
 * Provides the basic look and feel for a JColorChooser.
 * <p>
 * @author Tom Santos
 * @author Steve Wilson
 */

public class BasicColorChooserUI extends ColorChooserUI
{
    /**
     * JColorChooser this BasicColorChooserUI is installed on.
     *
     * @since 1.5
     */
    protected JColorChooser chooser;

    JTabbedPane tabbedPane;
    JPanel singlePanel;

    JPanel previewPanelHolder;
    JComponent previewPanel;
    boolean isMultiPanel = false;
    private static TransferHandler defaultTransferHandler = new ColorTransferHandler();

    protected AbstractColorChooserPanel[] defaultChoosers;

    protected ChangeListener previewListener;
    protected PropertyChangeListener propertyChangeListener;
    private Handler handler;

    public static ComponentUI createUI(JComponent c) {
	return new BasicColorChooserUI();
    }

    protected AbstractColorChooserPanel[] createDefaultChoosers() {
        AbstractColorChooserPanel[] panels = ColorChooserComponentFactory.getDefaultChooserPanels();
	return panels;
    }

    protected void uninstallDefaultChoosers() {
        AbstractColorChooserPanel[] choosers = chooser.getChooserPanels();
        for( int i = 0 ; i < choosers.length; i++) {
       	    chooser.removeChooserPanel( choosers[i] );
	}
    }

    public void installUI( JComponent c ) {
        chooser = (JColorChooser)c;

        super.installUI( c );

	installDefaults();
	installListeners();

	tabbedPane = new JTabbedPane();
        tabbedPane.setName("ColorChooser.tabPane");
        tabbedPane.setInheritsPopupMenu(true);
	singlePanel = new JPanel(new CenterLayout());
        singlePanel.setName("ColorChooser.panel");
        singlePanel.setInheritsPopupMenu(true);

	chooser.setLayout( new BorderLayout() );

	defaultChoosers = createDefaultChoosers();
	chooser.setChooserPanels(defaultChoosers);

	previewPanelHolder = new JPanel(new CenterLayout());
        previewPanelHolder.setName("ColorChooser.previewPanelHolder");

        if (DefaultLookup.getBoolean(chooser, this,
                                  "ColorChooser.showPreviewPanelText", true)) {
            String previewString = UIManager.getString(
                "ColorChooser.previewText", chooser.getLocale());
            previewPanelHolder.setBorder(new TitledBorder(previewString));
        }
        previewPanelHolder.setInheritsPopupMenu(true);
	chooser.add(previewPanelHolder, BorderLayout.SOUTH);

	installPreviewPanel();
	chooser.applyComponentOrientation(c.getComponentOrientation());
    }

    public void uninstallUI( JComponent c ) {
	chooser.remove(tabbedPane);
	chooser.remove(singlePanel);
	chooser.remove(previewPanelHolder);

        uninstallDefaultChoosers();
	uninstallListeners();
	uninstallDefaults();

	previewPanelHolder.remove(previewPanel);
	if (previewPanel instanceof UIResource) {
	    chooser.setPreviewPanel(null);
	}

	previewPanelHolder = null;
	previewPanel = null;
	defaultChoosers = null;
	chooser = null;
	tabbedPane = null;

        handler = null;
    }

    protected void installPreviewPanel() {
	if (previewPanel != null) {
	    previewPanelHolder.remove(previewPanel);
	    previewPanel.removeMouseListener(getHandler());
	}

	previewPanel = chooser.getPreviewPanel();
        Dimension layoutSize = new Dimension(); // fix for bug 4759306
        if (previewPanel != null) {
            layoutSize = new BorderLayout().minimumLayoutSize(previewPanel);
            if ((previewPanelHolder != null) && (chooser != null) &&
            (layoutSize.getWidth() + layoutSize.getHeight() == 0)) {
              chooser.remove(previewPanelHolder);
              return;
            }
        }
	if (previewPanel == null || previewPanel instanceof UIResource) { 
	  previewPanel = ColorChooserComponentFactory.getPreviewPanel(); // get from table?
	    chooser.setPreviewPanel(previewPanel);
	}
	previewPanel.setForeground(chooser.getColor());
	previewPanelHolder.add(previewPanel);
	previewPanel.addMouseListener(getHandler());
        previewPanel.setInheritsPopupMenu(true);
    }

    protected void installDefaults() {
        LookAndFeel.installColorsAndFont(chooser, "ColorChooser.background", 
                                              "ColorChooser.foreground",
                                              "ColorChooser.font");
        LookAndFeel.installProperty(chooser, "opaque", Boolean.TRUE);
	TransferHandler th = chooser.getTransferHandler();
	if (th == null || th instanceof UIResource) {
	    chooser.setTransferHandler(defaultTransferHandler);
	}
    }

    protected void uninstallDefaults() {
	if (chooser.getTransferHandler() instanceof UIResource) {
	    chooser.setTransferHandler(null);
	}
    }

    
    protected void installListeners() {
        propertyChangeListener = createPropertyChangeListener();
	chooser.addPropertyChangeListener( propertyChangeListener );

	previewListener = getHandler();
	chooser.getSelectionModel().addChangeListener(previewListener);
    }

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }

    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }
  
    protected void uninstallListeners() {
	chooser.removePropertyChangeListener( propertyChangeListener );
	chooser.getSelectionModel().removeChangeListener(previewListener);
	previewPanel.removeMouseListener(getHandler());
    }


    private class Handler implements ChangeListener, MouseListener,
            PropertyChangeListener {
        //
        // ChangeListener
        //
        public void stateChanged(ChangeEvent evt) {
            ColorSelectionModel model = (ColorSelectionModel)evt.getSource();
            if (previewPanel != null) {
                previewPanel.setForeground(model.getSelectedColor());
                previewPanel.repaint();
            }
        }

        //
        // MouseListener
        public void mousePressed(MouseEvent evt) {
            if (chooser.getDragEnabled()) {
                TransferHandler th = chooser.getTransferHandler();
                th.exportAsDrag(chooser, evt, TransferHandler.COPY);
            }
        }
        public void mouseReleased(MouseEvent evt) {}
        public void mouseClicked(MouseEvent evt) {}
        public void mouseEntered(MouseEvent evt) {}
        public void mouseExited(MouseEvent evt) {}

        //
        // PropertyChangeListener
        //
        public void propertyChange(PropertyChangeEvent evt) {
            String prop = evt.getPropertyName();

            if (prop == JColorChooser.CHOOSER_PANELS_PROPERTY) {
                AbstractColorChooserPanel[] oldPanels =
                    (AbstractColorChooserPanel[])evt.getOldValue();
                AbstractColorChooserPanel[] newPanels =
                    (AbstractColorChooserPanel[])evt.getNewValue();

                for (int i = 0; i < oldPanels.length; i++) {  // remove old panels                  
                   Container wrapper = oldPanels[i].getParent();
                    if (wrapper != null) {
                      Container parent = wrapper.getParent();
                      if (parent != null)
                          parent.remove(wrapper);  // remove from hierarchy
                      oldPanels[i].uninstallChooserPanel(chooser); // uninstall
                    }
                }

                int numNewPanels = newPanels.length;
                if (numNewPanels == 0) {  // removed all panels and added none
                    chooser.remove(tabbedPane);
                    return;
                } 
                else if (numNewPanels == 1) {  // one panel case
                    chooser.remove(tabbedPane);
                    JPanel centerWrapper = new JPanel( new CenterLayout() );
                    centerWrapper.setInheritsPopupMenu(true);
                    centerWrapper.add(newPanels[0]);
                    singlePanel.add(centerWrapper, BorderLayout.CENTER);
                    chooser.add(singlePanel);
                }
                else {   // multi-panel case
                    if ( oldPanels.length < 2 ) {// moving from single to multiple
                        chooser.remove(singlePanel);
                        chooser.add(tabbedPane, BorderLayout.CENTER);
                    }

                    for (int i = 0; i < newPanels.length; i++) {            
                        JPanel centerWrapper = new JPanel( new CenterLayout() );
                        centerWrapper.setInheritsPopupMenu(true);
                        String name = newPanels[i].getDisplayName();
                        int mnemonic = newPanels[i].getMnemonic();
                        centerWrapper.add(newPanels[i]);
                        tabbedPane.addTab(name, centerWrapper);
                        if (mnemonic > 0) {
                            tabbedPane.setMnemonicAt(i, mnemonic);
                            tabbedPane.setDisplayedMnemonicIndexAt(
                                  i, newPanels[i].getDisplayedMnemonicIndex());
                        }
                    }
                }
                chooser.applyComponentOrientation(chooser.getComponentOrientation());
                for (int i = 0; i < newPanels.length; i++) {
                    newPanels[i].installChooserPanel(chooser);
                }
            }

            if (prop == JColorChooser.PREVIEW_PANEL_PROPERTY) {
                if (evt.getNewValue() != previewPanel) {
                    installPreviewPanel();
                }
            }
            if (prop == "componentOrientation") {
                ComponentOrientation o =
                    (ComponentOrientation)evt.getNewValue();
                JColorChooser cc = (JColorChooser)evt.getSource();
                if (o != (ComponentOrientation)evt.getOldValue()) {
                    cc.applyComponentOrientation(o);
                    cc.updateUI();
                }
            }
        }
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of <Foo>.
     */
    public class PropertyHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            getHandler().propertyChange(e);
        }
    }

    static class ColorTransferHandler extends TransferHandler implements UIResource {

	ColorTransferHandler() {
	    super("color");
	}
    }
    
}

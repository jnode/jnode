/* BasicTextUI.java --
   Copyright (C) 2002, 2003, 2004, 2005, 2006  Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.InputMapUIResource;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.UIResource;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * The abstract base class from which the UI classes for Swings text
 * components are derived. This provides most of the functionality for
 * the UI classes.
 *
 * @author original author unknown
 * @author Roman Kennke (roman@kennke.org)
 */
public abstract class BasicTextUI extends TextUI
  implements ViewFactory
{
  /**
   * A {@link DefaultCaret} that implements {@link UIResource}.
   */
  public static class BasicCaret extends DefaultCaret implements UIResource
  {
    public BasicCaret()
    {
      // Nothing to do here.
    }
  }

  /**
   * A {@link DefaultHighlighter} that implements {@link UIResource}.
   */
  public static class BasicHighlighter extends DefaultHighlighter
    implements UIResource
  {
    public BasicHighlighter()
    {
      // Nothing to do here.
    }
  }

  /**
   * This view forms the root of the View hierarchy. However, it delegates
   * most calls to another View which is the real root of the hierarchy.
   * The purpose is to make sure that all Views in the hierarchy, including
   * the (real) root have a well-defined parent to which they can delegate
   * calls like {@link #preferenceChanged}, {@link #getViewFactory} and
   * {@link #getContainer}.
   */
  private class RootView extends View
  {
    /** The real root view. */
    private View view;

    /**
     * Creates a new RootView.
     */
    public RootView()
    {
      super(null);
    }

    /**
     * Returns the ViewFactory for this RootView. If the current EditorKit
     * provides a ViewFactory, this is used. Otherwise the TextUI itself
     * is returned as a ViewFactory.
     *
     * @return the ViewFactory for this RootView
     */
    public ViewFactory getViewFactory()
    {
      ViewFactory factory = null;
      EditorKit editorKit = BasicTextUI.this.getEditorKit(getComponent());
      factory = editorKit.getViewFactory();
      if (factory == null)
	factory = BasicTextUI.this;
      return factory;
    }

    /**
     * Indicates that the preferences of one of the child view has changed.
     * This calls revalidate on the text component.
     *
     * @param v the child view which's preference has changed
     * @param width <code>true</code> if the width preference has changed
     * @param height <code>true</code> if the height preference has changed
     */
    public void preferenceChanged(View v, boolean width, boolean height)
    {
      textComponent.revalidate();
    }

    /**
     * Sets the real root view.
     *
     * @param v the root view to set
     */
    public void setView(View v)
    {
      if (view != null)
        view.setParent(null);
      
      if (v != null)
        v.setParent(this);

      view = v;
    }

    /**
     * Returns the real root view, regardless of the index.
     *
     * @param index not used here
     *
     * @return the real root view, regardless of the index.
     */
    public View getView(int index)
    {
      return view;
    }

    /**
     * Returns <code>1</code> since the RootView always contains one
     * child, that is the real root of the View hierarchy.
     *
     * @return <code>1</code> since the RootView always contains one
     *         child, that is the real root of the View hierarchy
     */
    public int getViewCount()
    {
      int count = 0;
      if (view != null)
        count = 1;
      return count;
    }

    /**
     * Returns the <code>Container</code> that contains this view. This
     * normally will be the text component that is managed by this TextUI.
     *
     * @return the <code>Container</code> that contains this view
     */
    public Container getContainer()
    {
      return textComponent;
    }

    /**
     * Returns the preferred span along the specified <code>axis</code>.
     * This is delegated to the real root view.
     *
     * @param axis the axis for which the preferred span is queried
     *
     * @return the preferred span along the axis
     */
    public float getPreferredSpan(int axis)
    {
      if (view != null)
	return view.getPreferredSpan(axis);

      return Integer.MAX_VALUE;
    }

    public void setSize(float w, float h)
    {
      if (view != null)
        view.setSize(w, h);
    }

    /**
     * Paints the view. This is delegated to the real root view.
     *
     * @param g the <code>Graphics</code> context to paint to
     * @param s the allocation for the View
     */
    public void paint(Graphics g, Shape s)
    {
      if (view != null)
        {
          Rectangle b = s.getBounds();
          view.setSize(b.width, b.height);
        view.paint(g, s);
    }
    }


    /**
     * Maps a position in the document into the coordinate space of the View.
     * The output rectangle usually reflects the font height but has a width
     * of zero.
     *
     * This is delegated to the real root view.
     *
     * @param position the position of the character in the model
     * @param a the area that is occupied by the view
     * @param bias either {@link Position.Bias#Forward} or
     *        {@link Position.Bias#Backward} depending on the preferred
     *        direction bias. If <code>null</code> this defaults to
     *        <code>Position.Bias.Forward</code>
     *
     * @return a rectangle that gives the location of the document position
     *         inside the view coordinate space
     *
     * @throws BadLocationException if <code>pos</code> is invalid
     * @throws IllegalArgumentException if b is not one of the above listed
     *         valid values
     */
    public Shape modelToView(int position, Shape a, Position.Bias bias)
      throws BadLocationException
    {
      return view.modelToView(position, a, bias);
    }

    /**
     * Maps coordinates from the <code>View</code>'s space into a position
     * in the document model.
     *
     * @param x the x coordinate in the view space
     * @param y the y coordinate in the view space
     * @param a the allocation of this <code>View</code>
     * @param b the bias to use
     *
     * @return the position in the document that corresponds to the screen
     *         coordinates <code>x, y</code>
     */
    public int viewToModel(float x, float y, Shape a, Position.Bias[] b)
    {
      return view.viewToModel(x, y, a, b);
    }

    /**
     * Notification about text insertions. These are forwarded to the
     * real root view.
     *
     * @param ev the DocumentEvent describing the change
     * @param shape the current allocation of the view's display
     * @param vf the ViewFactory to use for creating new Views
     */
    public void insertUpdate(DocumentEvent ev, Shape shape, ViewFactory vf)
    {
      view.insertUpdate(ev, shape, vf);
    }

    /**
     * Notification about text removals. These are forwarded to the
     * real root view.
     *
     * @param ev the DocumentEvent describing the change
     * @param shape the current allocation of the view's display
     * @param vf the ViewFactory to use for creating new Views
     */
    public void removeUpdate(DocumentEvent ev, Shape shape, ViewFactory vf)
    {
      view.removeUpdate(ev, shape, vf);
    }

    /**
     * Notification about text changes. These are forwarded to the
     * real root view.
     *
     * @param ev the DocumentEvent describing the change
     * @param shape the current allocation of the view's display
     * @param vf the ViewFactory to use for creating new Views
     */
    public void changedUpdate(DocumentEvent ev, Shape shape, ViewFactory vf)
    {
      view.changedUpdate(ev, shape, vf);
    }

    /**
     * Returns the document position that is (visually) nearest to the given
     * document position <code>pos</code> in the given direction <code>d</code>.
     *
     * @param pos the document position
     * @param b the bias for <code>pos</code>
     * @param a the allocation for the view
     * @param d the direction, must be either {@link SwingConstants#NORTH},
     *        {@link SwingConstants#SOUTH}, {@link SwingConstants#WEST} or
     *        {@link SwingConstants#EAST}
     * @param biasRet an array of {@link Position.Bias} that can hold at least
     *        one element, which is filled with the bias of the return position
     *        on method exit
     *
     * @return the document position that is (visually) nearest to the given
     *         document position <code>pos</code> in the given direction
     *         <code>d</code>
     *
     * @throws BadLocationException if <code>pos</code> is not a valid offset in
     *         the document model
     */
    public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a,
                                         int d, Position.Bias[] biasRet)
      throws BadLocationException
    {
      return view.getNextVisualPositionFrom(pos, b, a, d, biasRet);
    }

    /**
     * Returns the startOffset of this view, which is always the beginning
     * of the document.
     *
     * @return the startOffset of this view
     */
    public int getStartOffset()
    {
      return 0;
    }

    /**
     * Returns the endOffset of this view, which is always the end
     * of the document.
     *
     * @return the endOffset of this view
     */
    public int getEndOffset()
    {
      return getDocument().getLength();
    }

    /**
     * Returns the document associated with this view.
     *
     * @return the document associated with this view
     */
    public Document getDocument()
    {
      return textComponent.getDocument();
    }

    /**
     * Returns the attributes, which is null for the RootView.
     */
    public AttributeSet getAttributes()
    {
      return null;
    }
  }

  /**
   * Receives notifications when properties of the text component change.
   */
  private class PropertyChangeHandler implements PropertyChangeListener
  {
    /**
     * Notifies when a property of the text component changes.
     *
     * @param event the PropertyChangeEvent describing the change
     */
    public void propertyChange(PropertyChangeEvent event)
    {
      if (event.getPropertyName().equals("document"))
        {
          // Document changed.
          Object oldValue = event.getOldValue();
          if (oldValue != null)
            {
              Document oldDoc = (Document) oldValue;
              oldDoc.removeDocumentListener(documentHandler);
            }
          Object newValue = event.getNewValue();
          if (newValue != null)
            {
              Document newDoc = (Document) newValue;
              newDoc.addDocumentListener(documentHandler);
            }
	      modelChanged();
        }

      BasicTextUI.this.propertyChange(event);
    }
  }

  /**
   * Listens for changes on the underlying model and forwards notifications
   * to the View. This also updates the caret position of the text component.
   *
   * TODO: Maybe this should somehow be handled through EditorKits
   */
  class DocumentHandler implements DocumentListener
  {
    /**
     * Notification about a document change event.
     *
     * @param ev the DocumentEvent describing the change
     */
    public void changedUpdate(DocumentEvent ev)
    {
      // Updates are forwarded to the View even if 'getVisibleEditorRect'
      // method returns null. This means the View classes have to be
      // aware of that possibility.
      rootView.changedUpdate(ev, getVisibleEditorRect(),
                             rootView.getViewFactory());
    }

    /**
     * Notification about a document insert event.
     *
     * @param ev the DocumentEvent describing the insertion
     */
    public void insertUpdate(DocumentEvent ev)
    {
      // Updates are forwarded to the View even if 'getVisibleEditorRect'
      // method returns null. This means the View classes have to be
      // aware of that possibility.
      rootView.insertUpdate(ev, getVisibleEditorRect(),
                            rootView.getViewFactory());
    }

    /**
     * Notification about a document removal event.
     *
     * @param ev the DocumentEvent describing the removal
     */
    public void removeUpdate(DocumentEvent ev)
    {
      // Updates are forwarded to the View even if 'getVisibleEditorRect'
      // method returns null. This means the View classes have to be
      // aware of that possibility.
      rootView.removeUpdate(ev, getVisibleEditorRect(),
                            rootView.getViewFactory());
    }
  }

  /**
   * The EditorKit used by this TextUI.
   */
  // FIXME: should probably be non-static.
  static EditorKit kit = new DefaultEditorKit();

  /**
   * The root view.
   */
  RootView rootView = new RootView();

  /**
   * The text component that we handle.
   */
  JTextComponent textComponent;

  /**
   * Receives notification when the model changes.
   */
  private PropertyChangeHandler updateHandler = new PropertyChangeHandler();

  /** The DocumentEvent handler. */
  DocumentHandler documentHandler = new DocumentHandler();

  /**
   * Creates a new <code>BasicTextUI</code> instance.
   */
  public BasicTextUI()
  {
    // Nothing to do here.
  }

  /**
   * Creates a {@link Caret} that should be installed into the text component.
   *
   * @return a caret that should be installed into the text component
   */
  protected Caret createCaret()
  {
    return new BasicCaret();
  }

  /**
   * Creates a {@link Highlighter} that should be installed into the text
   * component.
   *
   * @return a <code>Highlighter</code> for the text component
   */
  protected Highlighter createHighlighter()
  {
    return new BasicHighlighter();
  }

  /**
   * The text component that is managed by this UI.
   *
   * @return the text component that is managed by this UI
   */
  protected final JTextComponent getComponent()
  {
    return textComponent;
  }

  /**
   * Installs this UI on the text component.
   *
   * @param c the text component on which to install the UI
   */
  public void installUI(final JComponent c)
  {
    textComponent = (JTextComponent) c;
    installDefaults();
    textComponent.addPropertyChangeListener(updateHandler);
    Document doc = textComponent.getDocument();
    if (doc == null)
      {
	doc = getEditorKit(textComponent).createDefaultDocument();
	textComponent.setDocument(doc);
      }
    else
      {
        doc.addDocumentListener(documentHandler);
    modelChanged();
      }

    installListeners();
    installKeyboardActions();
  }

  /**
   * Installs UI defaults on the text components.
   */
  protected void installDefaults()
  {
    String prefix = getPropertyPrefix();
    // Install the standard properties.
    LookAndFeel.installColorsAndFont(textComponent, prefix + ".background",
                                     prefix + ".foreground", prefix + ".font");
    LookAndFeel.installBorder(textComponent, prefix + ".border");
    textComponent.setMargin(UIManager.getInsets(prefix + ".margin"));

    // Some additional text component only properties.
    Color color = textComponent.getCaretColor();
    if (color == null || color instanceof UIResource)
      {
        color = UIManager.getColor(prefix + ".caretForeground");
        textComponent.setCaretColor(color);
      }

    // Fetch the colors for enabled/disabled text components.
    color = textComponent.getDisabledTextColor();
    if (color == null || color instanceof UIResource)
      {
        color = UIManager.getColor(prefix + ".inactiveBackground");
        textComponent.setDisabledTextColor(color);
      }
    color = textComponent.getSelectedTextColor();
    if (color == null || color instanceof UIResource)
      {
        color = UIManager.getColor(prefix  + ".selectionForeground");
        textComponent.setSelectedTextColor(color);
      }
    color = textComponent.getSelectionColor();
    if (color == null || color instanceof UIResource)
      {
        color = UIManager.getColor(prefix  + ".selectionBackground");
        textComponent.setSelectionColor(color);    
      }

    Insets margin = textComponent.getMargin();
    if (margin == null || margin instanceof UIResource)
      {
        margin = UIManager.getInsets(prefix + ".margin");
        textComponent.setMargin(margin);
      }

    Caret caret = textComponent.getCaret();
    if (caret == null || caret instanceof UIResource)
      {
        caret = createCaret();
        textComponent.setCaret(caret);
        caret.setBlinkRate(UIManager.getInt(prefix + ".caretBlinkRate"));
      }

    Highlighter highlighter = textComponent.getHighlighter();
    if (highlighter == null || highlighter instanceof UIResource)
      textComponent.setHighlighter(createHighlighter());

  }

  /**
   * This FocusListener triggers repaints on focus shift.
   */
  private FocusListener focuslistener = new FocusListener() {
      public void focusGained(FocusEvent e) 
      {
        textComponent.repaint();
      }
      public void focusLost(FocusEvent e)
      {
        textComponent.repaint();
        
        // Integrates Swing text components with the system clipboard:
        // The idea is that if one wants to copy text around X11-style
        // (select text and middle-click in the target component) the focus
        // will move to the new component which gives the old focus owner the
        // possibility to paste its selection into the clipboard.
        if (!e.isTemporary()
            && textComponent.getSelectionStart()
               != textComponent.getSelectionEnd())
          {
            SecurityManager sm = System.getSecurityManager();
            try
              {
                if (sm != null)
                  sm.checkSystemClipboardAccess();
                
                Clipboard cb = Toolkit.getDefaultToolkit().getSystemSelection();
                if (cb != null)
                  {
                    StringSelection selection = new StringSelection(
                        textComponent.getSelectedText());
                    cb.setContents(selection, selection);
                  }
              }
            catch (SecurityException se)
              {
                // Not allowed to access the clipboard: Ignore and
                // do not access it.
              }
            catch (HeadlessException he)
              {
                // There is no AWT: Ignore and do not access the
                // clipboard.
              }
            catch (IllegalStateException ise)
            {
                // Clipboard is currently unavaible.
            }
          }
      }
    };

  /**
   * Install all listeners on the text component.
   */
  protected void installListeners()
  {
    textComponent.addFocusListener(focuslistener);
  }

  /**
   * Returns the name of the keymap for this type of TextUI.
   * 
   * This is implemented so that the classname of this TextUI
   * without the package prefix is returned. This way subclasses
   * don't have to override this method.
   * 
   * @return the name of the keymap for this TextUI
   */
  protected String getKeymapName()
  {
    String fullClassName = getClass().getName();
    int index = fullClassName.lastIndexOf('.');
    String className = fullClassName.substring(index + 1);
    return className;
  }

  /**
   * Creates the {@link Keymap} that is installed on the text component.
   *
   * @return the {@link Keymap} that is installed on the text component
   */
  protected Keymap createKeymap()
  {
    String keymapName = getKeymapName();
    Keymap keymap = JTextComponent.getKeymap(keymapName);
    if (keymap == null)
      {
        Keymap parentMap =
          JTextComponent.getKeymap(JTextComponent.DEFAULT_KEYMAP);
        keymap = JTextComponent.addKeymap(keymapName, parentMap);
        Object val = UIManager.get(getPropertyPrefix() + ".keyBindings");
        if (val != null && val instanceof JTextComponent.KeyBinding[])
          {
            JTextComponent.KeyBinding[] bindings =
              (JTextComponent.KeyBinding[]) val;
            JTextComponent.loadKeymap(keymap, bindings,
                                      getComponent().getActions());
          }
      }
    return keymap;
  }

  /**
   * Installs the keyboard actions on the text components.
   */
  protected void installKeyboardActions()
  {    
    // This is only there for backwards compatibility.
    textComponent.setKeymap(createKeymap());

    // load any bindings for the newer InputMap / ActionMap interface
    SwingUtilities.replaceUIInputMap(textComponent, JComponent.WHEN_FOCUSED,
                                     getInputMap());
    SwingUtilities.replaceUIActionMap(textComponent, getActionMap());
  }
    
  /**
   * Creates an ActionMap to be installed on the text component.
   * 
   * @return an ActionMap to be installed on the text component
   */
  private ActionMap getActionMap()
      {
    // Note: There are no .actionMap entries in the standard L&Fs. However,
    // with the RI it is possible to install action maps via such keys, so
    // we must load them too. It can be observed that when there is no
    // .actionMap entry in the UIManager, one gets installed after a text
    // component of that type has been loaded.
    String prefix = getPropertyPrefix();
    String amName = prefix + ".actionMap";
    ActionMap am = (ActionMap) UIManager.get(amName);
    if (am == null)
      {
        am = createActionMap();
        UIManager.put(amName, am);
      }
    
    ActionMap map = new ActionMapUIResource();
    map.setParent(am);

    return map;
  }
  
  /**
   * Creates a default ActionMap for text components that have no UI default
   * for this (the standard for the built-in L&Fs). The ActionMap is copied
   * from the text component's getActions() method.
   * 
   * @returna default ActionMap
   */
  private ActionMap createActionMap()
  {
    ActionMap am = new ActionMapUIResource();
    Action[] actions = textComponent.getActions();
    for (int i = actions.length - 1; i >= 0; i--)
      {
        Action action = actions[i];
        am.put(action.getValue(Action.NAME), action);
      }
    // Add TransferHandler's actions here. They don't seem to be in the
    // JTextComponent's default actions, and I can't make up a better place
    // to add them.
    Action copyAction = TransferHandler.getCopyAction();
    am.put(copyAction.getValue(Action.NAME), copyAction);
    Action cutAction = TransferHandler.getCutAction();
    am.put(cutAction.getValue(Action.NAME), cutAction);
    Action pasteAction = TransferHandler.getPasteAction();
    am.put(pasteAction.getValue(Action.NAME), pasteAction);

    return am;
  }

  /**
   * Gets the input map for the specified <code>condition</code>.
   *
   * @return the InputMap for the specified condition
   */
  private InputMap getInputMap()
  {
    InputMap im = new InputMapUIResource();
    String prefix = getPropertyPrefix();
    InputMap shared =
      (InputMap) SharedUIDefaults.get(prefix + ".focusInputMap");
    if (shared != null)
      im.setParent(shared);
    return im;
  }

  /**
   * Uninstalls this TextUI from the text component.
   *
   * @param component the text component to uninstall the UI from
   */
  public void uninstallUI(final JComponent component)
  {
    super.uninstallUI(component);
    rootView.setView(null);

    uninstallDefaults();
    uninstallListeners();
    uninstallKeyboardActions();

    textComponent = null;
  }

  /**
   * Uninstalls all default properties that have previously been installed by
   * this UI.
   */
  protected void uninstallDefaults()
  {
    // Do nothing here.
  }

  /**
   * Uninstalls all listeners that have previously been installed by
   * this UI.
   */
  protected void uninstallListeners()
  {
    textComponent.removeFocusListener(focuslistener);
    textComponent.getDocument().removeDocumentListener(documentHandler);
  }

  /**
   * Uninstalls all keyboard actions that have previously been installed by
   * this UI.
   */
  protected void uninstallKeyboardActions()
  {
    SwingUtilities.replaceUIInputMap(textComponent, JComponent.WHEN_FOCUSED, 
                                     null);
    SwingUtilities.replaceUIActionMap(textComponent, null);
  }

  /**
   * Returns the property prefix by which the text component's UIDefaults
   * are looked up.
   *
   * @return the property prefix by which the text component's UIDefaults
   *     are looked up
   */
  protected abstract String getPropertyPrefix();

  /**
   * Returns the preferred size of the text component.
   *
   * @param c not used here
   *
   * @return the preferred size of the text component
   */
  public Dimension getPreferredSize(JComponent c)
  {
    Dimension d = c.getSize();
    Insets i = c.getInsets();
    if (d.width > (i.left + i.right) && d.height > (i.top + i.bottom))
      {
        rootView.setSize(d.width - i.left - i.right,
                         d.height - i.top - i.bottom);
      }
    float w = rootView.getPreferredSpan(View.X_AXIS);
    float h = rootView.getPreferredSpan(View.Y_AXIS);

    Dimension size =  new Dimension((int) w + i.left + i.right,
                         (int) h + i.top + i.bottom);
    return size;
  }

  /**
   * Returns the maximum size for text components that use this UI.
   *
   * This returns (Integer.MAX_VALUE, Integer.MAX_VALUE).
   *
   * @param c not used here
   *
   * @return the maximum size for text components that use this UI
   */
  public Dimension getMaximumSize(JComponent c)
  {
    // Sun's implementation returns Integer.MAX_VALUE here, so do we.
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  /**
   * Returns the minimum size for text components. This returns the size
   * of the component's insets.
   *
   * @return the minimum size for text components
   */
  public Dimension getMinimumSize(JComponent c)
  {
    Insets i = c.getInsets();
    return new Dimension(i.left + i.right, i.top + i.bottom);
  }

  /**
   * Paints the text component. This acquires a read lock on the model and then
   * calls {@link #paintSafely(Graphics)} in order to actually perform the
   * painting.
   *
   * @param g the <code>Graphics</code> context to paint to
   * @param c not used here
   */
  public final void paint(Graphics g, JComponent c)
  {
    try
      {
        Document doc = textComponent.getDocument();
        if (doc instanceof AbstractDocument)
          {
            AbstractDocument aDoc = (AbstractDocument) doc;
            aDoc.readLock();
          }
        
    paintSafely(g);
  }
    finally
      {
        Document doc = textComponent.getDocument();
        if (doc instanceof AbstractDocument)
          {
            AbstractDocument aDoc = (AbstractDocument) doc;
            aDoc.readUnlock();
          }
      }
  }

  /**
   * This paints the text component while beeing sure that the model is not
   * modified while painting.
   *
   * The following is performed in this order:
   * <ol>
   * <li>If the text component is opaque, the background is painted by
   * calling {@link #paintBackground(Graphics)}.</li>
   * <li>If there is a highlighter, the highlighter is painted.</li>
   * <li>The view hierarchy is painted.</li>
   * <li>The Caret is painter.</li>
   * </ol>
   *
   * @param g the <code>Graphics</code> context to paint to
   */
  protected void paintSafely(Graphics g)
  {
    Caret caret = textComponent.getCaret();
    Highlighter highlighter = textComponent.getHighlighter();
    
    if (textComponent.isOpaque())
      paintBackground(g);
    
    // Try painting with the highlighter without checking whether there
    // is a selection because a highlighter can be used to do more than
    // marking selected text.
    if (highlighter != null)
      {
        // Handle restoring of the color here to prevent
        // drawing problems when the Highlighter implementor
        // forgets to restore it.
        Color oldColor = g.getColor();
      highlighter.paint(g);
        g.setColor(oldColor);
      }
      

    rootView.paint(g, getVisibleEditorRect());

    if (caret != null && textComponent.hasFocus())
      caret.paint(g);
  }

  /**
   * Paints the background of the text component.
   *
   * @param g the <code>Graphics</code> context to paint to
   */
  protected void paintBackground(Graphics g)
  {
    Color old = g.getColor();
    g.setColor(textComponent.getBackground());
    g.fillRect(0, 0, textComponent.getWidth(), textComponent.getHeight());
    g.setColor(old);
  }

  /**
   * Overridden for better control over background painting. This now simply
   * calls {@link #paint} and this delegates the background painting to
   * {@link #paintBackground}.
   *
   * @param g the graphics to use
   * @param c the component to be painted
   */
  public void update(Graphics g, JComponent c)
  {
    paint(g, c);
  }

  /**
   * Marks the specified range inside the text component's model as
   * damaged and queues a repaint request.
   *
   * @param t the text component
   * @param p0 the start location inside the document model of the range that
   *        is damaged
   * @param p1 the end location inside the document model of the range that
   *        is damaged
   */
  public void damageRange(JTextComponent t, int p0, int p1)
  {
    damageRange(t, p0, p1, Position.Bias.Forward, Position.Bias.Backward);
  }

  /**
   * Marks the specified range inside the text component's model as
   * damaged and queues a repaint request. This variant of this method
   * allows a {@link Position.Bias} object to be specified for the start
   * and end location of the range.
   *
   * @param t the text component
   * @param p0 the start location inside the document model of the range that
   *        is damaged
   * @param p1 the end location inside the document model of the range that
   *        is damaged
   * @param firstBias the bias for the start location
   * @param secondBias the bias for the end location
   */
  public void damageRange(JTextComponent t, int p0, int p1,
                          Position.Bias firstBias, Position.Bias secondBias)
  {
    Rectangle alloc = getVisibleEditorRect();
    if (alloc != null)
      {
        Document doc = t.getDocument();
    
        // Acquire lock here to avoid structural changes in between.
        if (doc instanceof AbstractDocument)
          ((AbstractDocument) doc).readLock();
    try
      {
            rootView.setSize(alloc.width, alloc.height);
            Shape damage = rootView.modelToView(p0, firstBias, p1, secondBias,
                                                alloc);
            Rectangle r = damage instanceof Rectangle ? (Rectangle) damage
                                                      : damage.getBounds();
            textComponent.repaint(r.x, r.y, r.width, r.height);
          }
        catch (BadLocationException ex)
          {
            // Lets ignore this as it causes no serious problems.
            // For debugging, comment this out.
            // ex.printStackTrace();
          }
        finally
          {
            // Release lock.
            if (doc instanceof AbstractDocument)
              ((AbstractDocument) doc).readUnlock();
      }
      }
  }

  /**
   * Returns the {@link EditorKit} used for the text component that is managed
   * by this UI.
   *
   * @param t the text component
   *
   * @return the {@link EditorKit} used for the text component that is managed
   *         by this UI
   */
  public EditorKit getEditorKit(JTextComponent t)
  {
    return kit;
  }

  /**
   * Gets the next position inside the document model that is visible on
   * screen, starting from <code>pos</code>.
   *
   * @param t the text component
   * @param pos the start positionn
   * @param b the bias for pos
   * @param direction the search direction
   * @param biasRet filled by the method to indicate the bias of the return
   *        value
   *
   * @return the next position inside the document model that is visible on
   *         screen
   */
  public int getNextVisualPositionFrom(JTextComponent t, int pos,
                                       Position.Bias b, int direction,
                                       Position.Bias[] biasRet)
    throws BadLocationException
  {
    // A comment in the spec of NavigationFilter.getNextVisualPositionFrom()
    // suggests that this method should be implemented by forwarding the call
    // the root view.
    return rootView.getNextVisualPositionFrom(pos, b,
                                              getVisibleEditorRect(),
                                              direction, biasRet);
  }

  /**
   * Returns the root {@link View} of a text component.
   *
   * @return the root {@link View} of a text component
   */
  public View getRootView(JTextComponent t)
  {
    return rootView;
  }

  /**
   * Maps a position in the document into the coordinate space of the View.
   * The output rectangle usually reflects the font height but has a width
   * of zero. A bias of {@link Position.Bias#Forward} is used in this method.
   *
   * @param t the text component
   * @param pos the position of the character in the model
   *
   * @return a rectangle that gives the location of the document position
   *         inside the view coordinate space
   *
   * @throws BadLocationException if <code>pos</code> is invalid
   * @throws IllegalArgumentException if b is not one of the above listed
   *         valid values
   */
  public Rectangle modelToView(JTextComponent t, int pos)
    throws BadLocationException
  {
    return modelToView(t, pos, Position.Bias.Forward);
  }

  /**
   * Maps a position in the document into the coordinate space of the View.
   * The output rectangle usually reflects the font height but has a width
   * of zero.
   *
   * @param t the text component
   * @param pos the position of the character in the model
   * @param bias either {@link Position.Bias#Forward} or
   *        {@link Position.Bias#Backward} depending on the preferred
   *        direction bias. If <code>null</code> this defaults to
   *        <code>Position.Bias.Forward</code>
   *
   * @return a rectangle that gives the location of the document position
   *         inside the view coordinate space
   *
   * @throws BadLocationException if <code>pos</code> is invalid
   * @throws IllegalArgumentException if b is not one of the above listed
   *         valid values
   */
  public Rectangle modelToView(JTextComponent t, int pos, Position.Bias bias)
    throws BadLocationException
  {
    // We need to read-lock here because we depend on the document
    // structure not beeing changed in between.
    Document doc = textComponent.getDocument();
    if (doc instanceof AbstractDocument)
      ((AbstractDocument) doc).readLock();
    Rectangle rect = null;
    try
      {
    Rectangle r = getVisibleEditorRect();
        if (r != null)
          {
            rootView.setSize(r.width, r.height);
            Shape s = rootView.modelToView(pos, r, bias);
            if (s != null)
              rect = s.getBounds();
          }
      }
    finally
      {
        if (doc instanceof AbstractDocument)
          ((AbstractDocument) doc).readUnlock();
      }
    return rect;
  }

  /**
   * Maps a point in the <code>View</code> coordinate space to a position
   * inside a document model.
   *
   * @param t the text component
   * @param pt the point to be mapped
   *
   * @return the position inside the document model that corresponds to
   *     <code>pt</code>
   */
  public int viewToModel(JTextComponent t, Point pt)
  {
    return viewToModel(t, pt, new Position.Bias[1]);
  }

  /**
   * Maps a point in the <code>View</code> coordinate space to a position
   * inside a document model.
   *
   * @param t the text component
   * @param pt the point to be mapped
   * @param biasReturn filled in by the method to indicate the bias of the
   *        return value
   *
   * @return the position inside the document model that corresponds to
   *     <code>pt</code>
   */
  public int viewToModel(JTextComponent t, Point pt, Position.Bias[] biasReturn)
  {
    return rootView.viewToModel(pt.x, pt.y, getVisibleEditorRect(), biasReturn);
  }

  /**
   * Creates a {@link View} for the specified {@link Element}.
   *
   * @param elem the <code>Element</code> to create a <code>View</code> for
   *
   * @see ViewFactory
   */
  public View create(Element elem)
  {
    // Subclasses have to implement this to get this functionality.
    return null;
  }

  /**
   * Creates a {@link View} for the specified {@link Element}.
   *
   * @param elem the <code>Element</code> to create a <code>View</code> for
   * @param p0 the start offset
   * @param p1 the end offset
   *
   * @see ViewFactory
   */
  public View create(Element elem, int p0, int p1)
  {
    // Subclasses have to implement this to get this functionality.
    return null;
  }

  /**
   * Returns the allocation to give the root view.
   *
   * @return the allocation to give the root view
   *
   * @specnote The allocation has nothing to do with visibility. According
   *           to the specs the naming of this method is unfortunate and
   *           has historical reasons
   */
  protected Rectangle getVisibleEditorRect()
  {
    int width = textComponent.getWidth();
    int height = textComponent.getHeight();

    // Return null if the component has no valid size.
    if (width <= 0 || height <= 0)
      return null;
	
    Insets insets = textComponent.getInsets();
    return new Rectangle(insets.left, insets.top,
			 width - insets.left - insets.right,
			 height - insets.top - insets.bottom);
  }

  /**
   * Sets the root view for the text component.
   *
   * @param view the <code>View</code> to be set as root view
   */
  protected final void setView(View view)
  {
    rootView.setView(view);
    textComponent.revalidate();
    textComponent.repaint();
  }

  /**
   * Indicates that the model of a text component has changed. This
   * triggers a rebuild of the view hierarchy.
   */
  protected void modelChanged()
  {
    if (textComponent == null || rootView == null) 
      return;
    ViewFactory factory = rootView.getViewFactory();
    if (factory == null) 
      return;
    Document doc = textComponent.getDocument();
    if (doc == null)
      return;
    Element elem = doc.getDefaultRootElement();
    if (elem == null)
      return;
    View view = factory.create(elem);
    setView(view);
  }

  /**
   * Receives notification whenever one of the text component's bound
   * properties changes. This default implementation does nothing.
   * It is a hook that enables subclasses to react to property changes
   * on the text component.
   *
   * @param ev the property change event
   */
  protected void propertyChange(PropertyChangeEvent ev)
  {
    // The default implementation does nothing.
  }
}

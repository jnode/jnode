/* JSpinner.java --
   Copyright (C) 2004, 2005, 2006  Free Software Foundation, Inc.

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


package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.SpinnerUI;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import javax.swing.text.AttributeSet;
import javax.accessibility.AccessibleValue;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleText;
import javax.accessibility.AccessibleEditableText;
import javax.accessibility.AccessibleRole;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;

/**
 * A <code>JSpinner</code> is a component that displays a single value from
 * a sequence of values, and provides a convenient means for selecting the
 * previous and next values in the sequence.  Typically the spinner displays
 * a numeric value, but it is possible to display dates or arbitrary items
 * from a list.
 *
 * @author Ka-Hing Cheung
 * 
 * @since 1.4
 */
public class JSpinner extends JComponent
{
  /**
   * The base class for the editor used by the {@link JSpinner} component.  
   * The editor is in fact a panel containing a {@link JFormattedTextField}
   * component.
   */
  public static class DefaultEditor 
    extends JPanel 
    implements ChangeListener, PropertyChangeListener, LayoutManager
  {
    /** The spinner that the editor is allocated to. */
    private JSpinner spinner;

    /** The JFormattedTextField that backs the editor. */
    JFormattedTextField ftf;

    /**
     * For compatability with Sun's JDK 1.4.2 rev. 5
     */
    private static final long serialVersionUID = -5317788736173368172L;

    /**
     * Creates a new <code>DefaultEditor</code> object.  The editor is 
     * registered with the spinner as a {@link ChangeListener} here.
     *
     * @param spinner the <code>JSpinner</code> associated with this editor
     */
    public DefaultEditor(JSpinner spinner)
    {
      super();
      setLayout(this);
      this.spinner = spinner;
      ftf = new JFormattedTextField();
      add(ftf);
      ftf.setValue(spinner.getValue());
      ftf.addPropertyChangeListener(this);
      if (getComponentOrientation().isLeftToRight())
	ftf.setHorizontalAlignment(JTextField.RIGHT);
      else
	ftf.setHorizontalAlignment(JTextField.LEFT);
      spinner.addChangeListener(this);
    }

    /**
     * Returns the <code>JSpinner</code> component that the editor is assigned
     * to.
     * 
     * @return The spinner that the editor is assigned to.
     */
    public JSpinner getSpinner()
    {
      return spinner;
    }
    
    /**
     * DOCUMENT ME!
     */
    public void commitEdit() throws ParseException
    {
      // TODO: Implement this properly.
    }

    /**
     * Removes the editor from the {@link ChangeListener} list maintained by
     * the specified <code>spinner</code>.
     *
     * @param spinner  the spinner (<code>null</code> not permitted).
     */
    public void dismiss(JSpinner spinner)
    {
      spinner.removeChangeListener(this);
    }

    /**
     * Returns the text field used to display and edit the current value in 
     * the spinner.
     *
     * @return The text field.
     */
    public JFormattedTextField getTextField()
    {
      return ftf;
    }
    
    /**
     * Sets the bounds for the child components in this container.  In this
     * case, the text field is the only component to be laid out.
     *
     * @param parent the parent container.
     */
    public void layoutContainer(Container parent)
    {
      Insets insets = getInsets();
      Dimension size = getSize();
      ftf.setBounds(insets.left, insets.top,
                    size.width - insets.left - insets.right,
                    size.height - insets.top - insets.bottom);
    }
    
    /**
     * Calculates the minimum size for this component.  In this case, the
     * text field is the only subcomponent, so the return value is the minimum
     * size of the text field plus the insets of this component.
     *
     * @param parent  the parent container.
     *
     * @return The minimum size.
     */
    public Dimension minimumLayoutSize(Container parent)
    {
      Insets insets = getInsets();
      Dimension minSize = ftf.getMinimumSize();
      return new Dimension(minSize.width + insets.left + insets.right,
                            minSize.height + insets.top + insets.bottom);
    }
    
    /**
     * Calculates the preferred size for this component.  In this case, the
     * text field is the only subcomponent, so the return value is the 
     * preferred size of the text field plus the insets of this component.
     *
     * @param parent  the parent container.
     *
     * @return The preferred size.
     */
    public Dimension preferredLayoutSize(Container parent)
    {
      Insets insets = getInsets();
      Dimension prefSize = ftf.getPreferredSize();
      return new Dimension(prefSize.width + insets.left + insets.right,
                            prefSize.height + insets.top + insets.bottom);
    }
    
    /**
     * Receives notification of property changes.  If the text field's 'value' 
     * property changes, the spinner's model is updated accordingly.
     *
     * @param event the event.
     */
    public void propertyChange(PropertyChangeEvent event)
    {
      if (event.getSource() == ftf) 
        {
          if (event.getPropertyName().equals("value"))
            spinner.getModel().setValue(event.getNewValue());
        }
    }
    
    /**
     * Receives notification of changes in the state of the {@link JSpinner}
     * that the editor belongs to - the content of the text field is updated
     * accordingly.  
     *
     * @param event  the change event.
     */
    public void stateChanged(ChangeEvent event)
    {
      ftf.setValue(spinner.getValue());
    }
    
    /**
     * This method does nothing.  It is required by the {@link LayoutManager}
     * interface, but since this component has a single child, there is no
     * need to use this method.
     * 
     * @param child  the child component to remove.
     */
    public void removeLayoutComponent(Component child)
    {
      // Nothing to do here.
    }

    /**
     * This method does nothing.  It is required by the {@link LayoutManager}
     * interface, but since this component has a single child, there is no
     * need to use this method.
     *
     * @param name  the name.
     * @param child  the child component to add.
     */
    public void addLayoutComponent(String name, Component child)
    {
      // Nothing to do here.
    }
  }

  /**
   * A panel containing a {@link JFormattedTextField} that is configured for
   * displaying and editing numbers.  The panel is used as a subcomponent of
   * a {@link JSpinner}.
   * 
   * @see JSpinner#createEditor(SpinnerModel)
   */
  public static class NumberEditor extends DefaultEditor
  {
    /**
     * For compatability with Sun's JDK
     */
    private static final long serialVersionUID = 3791956183098282942L;

    /**
     * Creates a new <code>NumberEditor</code> object for the specified 
     * <code>spinner</code>.  The editor is registered with the spinner as a 
     * {@link ChangeListener}.
     *
     * @param spinner the component the editor will be used with.
     */
    public NumberEditor(JSpinner spinner)
    {
      super(spinner);
      NumberEditorFormatter nef = new NumberEditorFormatter();
      nef.setMinimum(getModel().getMinimum());
      nef.setMaximum(getModel().getMaximum());
      ftf.setFormatterFactory(new DefaultFormatterFactory(nef));
    }

    /**
     * Creates a new <code>NumberEditor</code> object.
     *
     * @param spinner  the spinner.
     * @param decimalFormatPattern  the number format pattern.
     */
    public NumberEditor(JSpinner spinner, String decimalFormatPattern)
    {
      super(spinner);
      NumberEditorFormatter nef 
          = new NumberEditorFormatter(decimalFormatPattern);
      nef.setMinimum(getModel().getMinimum());
      nef.setMaximum(getModel().getMaximum());
      ftf.setFormatterFactory(new DefaultFormatterFactory(nef));
    }

    /**
     * Returns the format used by the text field.
     *
     * @return The format used by the text field.
     */
    public DecimalFormat getFormat()
    {
      NumberFormatter formatter = (NumberFormatter) ftf.getFormatter();
      return (DecimalFormat) formatter.getFormat();
    }

    /**
     * Returns the model used by the editor's {@link JSpinner} component,
     * cast to a {@link SpinnerNumberModel}.
     * 
     * @return The model.
     */
    public SpinnerNumberModel getModel()
    {
      return (SpinnerNumberModel) getSpinner().getModel();
    }
  }

  static class NumberEditorFormatter 
    extends NumberFormatter
  {
    public NumberEditorFormatter() 
    {
      super(NumberFormat.getInstance());
    }
    public NumberEditorFormatter(String decimalFormatPattern)
    {
      super(new DecimalFormat(decimalFormatPattern));
    }
  }

  /**
   * A <code>JSpinner</code> editor used for the {@link SpinnerListModel}.
   * This editor uses a <code>JFormattedTextField</code> to edit the values
   * of the spinner.
   *
   * @author Roman Kennke (kennke@aicas.com)
   */
  public static class ListEditor extends DefaultEditor
  {
    /**
     * Creates a new instance of <code>ListEditor</code>.
     *
     * @param spinner the spinner for which this editor is used
     */
    public ListEditor(JSpinner spinner)
    {
      super(spinner);
    }

    /**
     * Returns the spinner's model cast as a {@link SpinnerListModel}.
     * 
     * @return The spinner's model.
     */
    public SpinnerListModel getModel()
    {
      return (SpinnerListModel) getSpinner().getModel();
    }
  }

  /**
   * An editor class for a <code>JSpinner</code> that is used
   * for displaying and editing dates (e.g. that uses
   * <code>SpinnerDateModel</code> as model).
   *
   * The editor uses a {@link JTextField} with the value
   * displayed by a {@link DateFormatter} instance.
   */
  public static class DateEditor extends DefaultEditor
  {

    /** The serialVersionUID. */
    private static final long serialVersionUID = -4279356973770397815L;

    /**
     * Creates a new instance of DateEditor for the specified
     * <code>JSpinner</code>.
     *
     * @param spinner the <code>JSpinner</code> for which to
     *     create a <code>DateEditor</code> instance
     */
    public DateEditor(JSpinner spinner)
    {
      super(spinner);
      DateEditorFormatter nef = new DateEditorFormatter();
      nef.setMinimum(getModel().getStart());
      nef.setMaximum(getModel().getEnd());
      ftf.setFormatterFactory(new DefaultFormatterFactory(nef));
    }

    /**
     * Creates a new instance of DateEditor for the specified
     * <code>JSpinner</code> using the specified date format
     * pattern.
     *
     * @param spinner the <code>JSpinner</code> for which to
     *     create a <code>DateEditor</code> instance
     * @param dateFormatPattern the date format to use
     *
     * @see SimpleDateFormat#SimpleDateFormat(String)
     */
    public DateEditor(JSpinner spinner, String dateFormatPattern)
    {
      super(spinner);
      DateEditorFormatter nef = new DateEditorFormatter(dateFormatPattern);
      nef.setMinimum(getModel().getStart());
      nef.setMaximum(getModel().getEnd());
      ftf.setFormatterFactory(new DefaultFormatterFactory(nef));
    }

    /**
     * Returns the <code>SimpleDateFormat</code> instance that is used to
     * format the date value.
     *
     * @return the <code>SimpleDateFormat</code> instance that is used to
     *     format the date value
     */
    public SimpleDateFormat getFormat()
    {
      DateFormatter formatter = (DateFormatter) ftf.getFormatter();
      return (SimpleDateFormat) formatter.getFormat();
    }

    /**
     * Returns the {@link SpinnerDateModel} that is edited by this editor.
     *
     * @return the <code>SpinnerDateModel</code> that is edited by this editor
     */
    public SpinnerDateModel getModel()
    {
      return (SpinnerDateModel) getSpinner().getModel();
    }
  }

  static class DateEditorFormatter 
    extends DateFormatter
  {
    public DateEditorFormatter() 
    {
      super(DateFormat.getInstance());
    }
    public DateEditorFormatter(String dateFormatPattern)
    {
      super(new SimpleDateFormat(dateFormatPattern));
    }
  }

  /** 
   * A listener that forwards {@link ChangeEvent} notifications from the model
   * to the {@link JSpinner}'s listeners. 
   */
  class ModelListener implements ChangeListener
    {
    /**
     * Creates a new listener.
     */
    public ModelListener()
    {
      // nothing to do here
    }
    
    /**
     * Receives notification from the model that its state has changed.
     * 
     * @param event  the event (ignored).
     */
    public void stateChanged(ChangeEvent event)
      {
	fireStateChanged();
      }
  }

  /** 
   * The model that defines the current value and permitted values for the 
   * spinner. 
   */
  private SpinnerModel model;

  /** The current editor. */
  private JComponent editor;

  private static final long serialVersionUID = 3412663575706551720L;

  /**
   * Creates a new <code>JSpinner</code> with default instance of 
   * {@link SpinnerNumberModel} (that is, a model with value 0, step size 1, 
   * and no upper or lower limit).
   *
   * @see javax.swing.SpinnerNumberModel
   */
  public JSpinner()
  {
    this(new SpinnerNumberModel());
  }

  /**
   * Creates a new <code>JSpinner with the specified model.  The 
   * {@link #createEditor(SpinnerModel)} method is used to create an editor
   * that is suitable for the model.
   *
   * @param model the model (<code>null</code> not permitted).
   * 
   * @throws NullPointerException if <code>model</code> is <code>null</code>.
   */
  public JSpinner(SpinnerModel model)
  {
    this.model = model;
    this.editor = createEditor(model);
    model.addChangeListener(new ModelListener());
    updateUI();
  }

  /**
   * If the editor is <code>JSpinner.DefaultEditor</code>, then forwards the
   * call to it, otherwise do nothing.
   *
   * @throws ParseException DOCUMENT ME!
   */
  public void commitEdit() throws ParseException
  {
    if (editor instanceof DefaultEditor)
      ((DefaultEditor) editor).commitEdit();
  }

  /**
   * Gets the current editor
   *
   * @return the current editor
   *
   * @see #setEditor
   */
  public JComponent getEditor()
  {
    return editor;
  }

  /**
   * Changes the current editor to the new editor. The old editor is
   * removed from the spinner's {@link ChangeEvent} list.
   *
   * @param editor the new editor (<code>null</code> not permitted.
   *
   * @throws IllegalArgumentException if <code>editor</code> is 
   *                                  <code>null</code>.
   *
   * @see #getEditor
   */
  public void setEditor(JComponent editor)
  {
    if (editor == null)
      throw new IllegalArgumentException("editor may not be null");

    JComponent oldEditor = this.editor;
    if (oldEditor instanceof DefaultEditor)
      ((DefaultEditor) oldEditor).dismiss(this);
    else if (oldEditor instanceof ChangeListener)
      removeChangeListener((ChangeListener) oldEditor);

    this.editor = editor;
    firePropertyChange("editor", oldEditor, editor);
  }

  /**
   * Returns the model used by the {@link JSpinner} component.
   *
   * @return The model.
   *
   * @see #setModel(SpinnerModel)
   */
  public SpinnerModel getModel()
  {
    return model;
  }

  /**
   * Sets a new underlying model.
   *
   * @param newModel the new model to set
   *
   * @exception IllegalArgumentException if newModel is <code>null</code>
   */
  public void setModel(SpinnerModel newModel)
  {
    if (newModel == null)
      throw new IllegalArgumentException();
    
    if (model == newModel)
      return;

    SpinnerModel oldModel = model;
    model = newModel;
    firePropertyChange("model", oldModel, newModel);
      setEditor(createEditor(model));
  }

  /**
   * Gets the next value without changing the current value.
   *
   * @return the next value
   *
   * @see javax.swing.SpinnerModel#getNextValue
   */
  public Object getNextValue()
  {
    return model.getNextValue();
  }

  /**
   * Gets the previous value without changing the current value.
   *
   * @return the previous value
   *
   * @see javax.swing.SpinnerModel#getPreviousValue
   */
  public Object getPreviousValue()
  {
    return model.getPreviousValue();
  }

  /**
   * Gets the <code>SpinnerUI</code> that handles this spinner
   *
   * @return the <code>SpinnerUI</code>
   */
  public SpinnerUI getUI()
  {
    return (SpinnerUI) ui;
  }

  /**
   * Gets the current value of the spinner, according to the underly model,
   * not the UI.
   *
   * @return the current value
   *
   * @see javax.swing.SpinnerModel#getValue
   */
  public Object getValue()
  {
    return model.getValue();
  }

  /**
   * Sets the value in the model.
   *
   * @param value the new value.
   */
  public void setValue(Object value)
  {
    model.setValue(value);
  }

  /**
   * Returns the ID that identifies which look and feel class will be
   * the UI delegate for this spinner.
   *
   * @return <code>"SpinnerUI"</code>.
   */
  public String getUIClassID()
  {
    return "SpinnerUI";
  }

  /**
   * This method resets the spinner's UI delegate to the default UI for the
   * current look and feel.
   */
  public void updateUI()
  {
    setUI((SpinnerUI) UIManager.getUI(this));
  }

  /**
   * Sets the UI delegate for the component.
   *
   * @param ui The spinner's UI delegate.
   */
  public void setUI(SpinnerUI ui)
  {
    super.setUI(ui);
  }

  /**
   * Adds a <code>ChangeListener</code>
   *
   * @param listener the listener to add
   */
  public void addChangeListener(ChangeListener listener)
  {
    listenerList.add(ChangeListener.class, listener);
  }

  /**
   * Remove a particular listener
   *
   * @param listener the listener to remove
   */
  public void removeChangeListener(ChangeListener listener)
  {
    listenerList.remove(ChangeListener.class, listener);
  }

  /**
   * Gets all the <code>ChangeListener</code>s
   *
   * @return all the <code>ChangeListener</code>s
   */
  public ChangeListener[] getChangeListeners()
  {
    return (ChangeListener[]) listenerList.getListeners(ChangeListener.class);
  }

  /**
   * Fires a <code>ChangeEvent</code> to all the <code>ChangeListener</code>s
   * added to this <code>JSpinner</code>
   */
  protected void fireStateChanged()
  {
    ChangeEvent evt = new ChangeEvent(this);
    ChangeListener[] listeners = getChangeListeners();

    for (int i = 0; i < listeners.length; ++i)
      listeners[i].stateChanged(evt);
  }

  /**
   * Creates an editor that is appropriate for the specified <code>model</code>.
   *
   * @param model the model.
   *
   * @return The editor.
   */
  protected JComponent createEditor(SpinnerModel model)
  {
    if (model instanceof SpinnerDateModel)
      return new DateEditor(this);
    else if (model instanceof SpinnerNumberModel)
      return new NumberEditor(this);
    else if (model instanceof SpinnerListModel)
      return new ListEditor(this);
    else
      return new DefaultEditor(this);
  }

    //jnode + opnjdk
    
        /**
     * <code>AccessibleJSpinner</code> implements accessibility
     * support for the <code>JSpinner</code> class.
     * @since 1.5
     */
    protected class AccessibleJSpinner extends AccessibleJComponent
        implements AccessibleValue, AccessibleAction, AccessibleText,
                AccessibleEditableText, ChangeListener {

	private Object oldModelValue = null;

	/**
	 * AccessibleJSpinner constructor
	 */
	protected AccessibleJSpinner() {
	    // model is guaranteed to be non-null
	    oldModelValue = model.getValue();
	    JSpinner.this.addChangeListener(this);
	}

	/**
         * Invoked when the target of the listener has changed its state.
         *
         * @param e  a <code>ChangeEvent</code> object. Must not be null.
	 * @throws NullPointerException if the parameter is null.
	 */
	public void stateChanged(ChangeEvent e) {
	    if (e == null) {
		throw new NullPointerException();
	    }
	    Object newModelValue = model.getValue();
	    firePropertyChange(ACCESSIBLE_VALUE_PROPERTY,
			       oldModelValue,
			       newModelValue);
	    firePropertyChange(ACCESSIBLE_TEXT_PROPERTY,
			       null,
			       0); // entire text may have changed

	    oldModelValue = newModelValue;
	}

	/* ===== Begin AccessibleContext methods ===== */

	/**
	 * Gets the role of this object.  The role of the object is the generic
	 * purpose or use of the class of this object.  For example, the role
	 * of a push button is AccessibleRole.PUSH_BUTTON.  The roles in
	 * AccessibleRole are provided so component developers can pick from
	 * a set of predefined roles.  This enables assistive technologies to
	 * provide a consistent interface to various tweaked subclasses of
	 * components (e.g., use AccessibleRole.PUSH_BUTTON for all components
	 * that act like a push button) as well as distinguish between sublasses
	 * that behave differently (e.g., AccessibleRole.CHECK_BOX for check boxes
	 * and AccessibleRole.RADIO_BUTTON for radio buttons).
	 * <p>Note that the AccessibleRole class is also extensible, so
	 * custom component developers can define their own AccessibleRole's
	 * if the set of predefined roles is inadequate.
	 *
	 * @return an instance of AccessibleRole describing the role of the object
	 * @see javax.accessibility.AccessibleRole
	 */
	public AccessibleRole getAccessibleRole() {
	    return AccessibleRole.SPIN_BOX;
	}

	/**
	 * Returns the number of accessible children of the object.
	 *
	 * @return the number of accessible children of the object.
	 */
	public int getAccessibleChildrenCount() {
	    // the JSpinner has one child, the editor
	    if (editor.getAccessibleContext() != null) {
		return 1;
	    }
	    return 0;
	}

	/**
	 * Returns the specified Accessible child of the object.  The Accessible
	 * children of an Accessible object are zero-based, so the first child
	 * of an Accessible child is at index 0, the second child is at index 1,
	 * and so on.
	 *
	 * @param i zero-based index of child
	 * @return the Accessible child of the object
	 * @see #getAccessibleChildrenCount
	 */
	public Accessible getAccessibleChild(int i) {
	    // the JSpinner has one child, the editor
	    if (i != 0) {
		return null;
	    }
	    if (editor.getAccessibleContext() != null) {
		return (Accessible)editor;
	    }
	    return null;
	}

	/* ===== End AccessibleContext methods ===== */

	/**
	 * Gets the AccessibleAction associated with this object that supports
	 * one or more actions.
	 *
	 * @return AccessibleAction if supported by object; else return null
	 * @see AccessibleAction
	 */
	public AccessibleAction getAccessibleAction() {
	    return this;
	}

	/**
	 * Gets the AccessibleText associated with this object presenting
	 * text on the display.
	 *
	 * @return AccessibleText if supported by object; else return null
	 * @see AccessibleText
	 */
	public AccessibleText getAccessibleText() {
	    return this;
	}

	/*
	 * Returns the AccessibleContext for the JSpinner editor
	 */
	private AccessibleContext getEditorAccessibleContext() {
	    if (editor instanceof DefaultEditor) {
		JTextField textField = ((DefaultEditor)editor).getTextField();
		if (textField != null) {
		    return textField.getAccessibleContext();
		}
	    } else if (editor instanceof Accessible) {
		return ((Accessible)editor).getAccessibleContext();
	    }
	    return null;
	}

	/*
	 * Returns the AccessibleText for the JSpinner editor
	 */
	private AccessibleText getEditorAccessibleText() {
	    AccessibleContext ac = getEditorAccessibleContext();
	    if (ac != null) {
		return ac.getAccessibleText();
	    }
	    return null;
	}

	/*
	 * Returns the AccessibleEditableText for the JSpinner editor
	 */
	private AccessibleEditableText getEditorAccessibleEditableText() {
	    AccessibleText at = getEditorAccessibleText();
	    if (at instanceof AccessibleEditableText) {
		return (AccessibleEditableText)at;
	    }
	    return null;
	}

	/**
	 * Gets the AccessibleValue associated with this object.
	 *
	 * @return AccessibleValue if supported by object; else return null
	 * @see AccessibleValue
	 *
	 */
	public AccessibleValue getAccessibleValue() {
	    return this;
	}

	/* ===== Begin AccessibleValue impl ===== */

	/**
	 * Get the value of this object as a Number.  If the value has not been
	 * set, the return value will be null.
	 *
	 * @return value of the object
	 * @see #setCurrentAccessibleValue
	 */
	public Number getCurrentAccessibleValue() {
	    Object o = model.getValue();
	    if (o instanceof Number) {
		return (Number)o;
	    }
	    return null;
	}

	/**
	 * Set the value of this object as a Number.
	 *
	 * @param n the value to set for this object
	 * @return true if the value was set; else False
	 * @see #getCurrentAccessibleValue
	 */
	public boolean setCurrentAccessibleValue(Number n) {
	    // try to set the new value
	    try {
		model.setValue(n);
		return true;
	    } catch (IllegalArgumentException iae) {
		// SpinnerModel didn't like new value
	    }
	    return false;
	}

	/**
	 * Get the minimum value of this object as a Number.
	 *
	 * @return Minimum value of the object; null if this object does not
	 * have a minimum value
	 * @see #getMaximumAccessibleValue
	 */
	public Number getMinimumAccessibleValue() {
	    if (model instanceof SpinnerNumberModel) {
		SpinnerNumberModel numberModel = (SpinnerNumberModel)model;
		Object o = numberModel.getMinimum();
		if (o instanceof Number) {
		    return (Number)o;
		}
	    }
	    return null;
	}

	/**
	 * Get the maximum value of this object as a Number.
	 *
	 * @return Maximum value of the object; null if this object does not
	 * have a maximum value
	 * @see #getMinimumAccessibleValue
	 */
	public Number getMaximumAccessibleValue() {
	    if (model instanceof SpinnerNumberModel) {
		SpinnerNumberModel numberModel = (SpinnerNumberModel)model;
		Object o = numberModel.getMaximum();
		if (o instanceof Number) {
		    return (Number)o;
		}
	    }
	    return null;
	}

	/* ===== End AccessibleValue impl ===== */

	/* ===== Begin AccessibleAction impl ===== */

	/**
	 * Returns the number of accessible actions available in this object
	 * If there are more than one, the first one is considered the "default"
	 * action of the object.
	 *
	 * Two actions are supported: AccessibleAction.INCREMENT which
	 * increments the spinner value and AccessibleAction.DECREMENT
	 * which decrements the spinner value
	 *
	 * @return the zero-based number of Actions in this object
	 */
	public int getAccessibleActionCount() {
	    return 2;
	}

	/**
	 * Returns a description of the specified action of the object.
	 *
	 * @param i zero-based index of the actions
	 * @return a String description of the action
	 * @see #getAccessibleActionCount
	 */
	public String getAccessibleActionDescription(int i) {
	    if (i == 0) {
		return AccessibleAction.INCREMENT;
	    } else if (i == 1) {
		return AccessibleAction.DECREMENT;
	    }
	    return null;
	}

	/**
	 * Performs the specified Action on the object
	 *
	 * @param i zero-based index of actions. The first action
	 * (index 0) is AccessibleAction.INCREMENT and the second
	 * action (index 1) is AccessibleAction.DECREMENT.
	 * @return true if the action was performed; otherwise false.
	 * @see #getAccessibleActionCount
	 */
	public boolean doAccessibleAction(int i) {
	    if (i < 0 || i > 1) {
		return false;
	    }
	    Object o = null;
	    if (i == 0) {
		o = getNextValue(); // AccessibleAction.INCREMENT
	    } else {
		o = getPreviousValue();	// AccessibleAction.DECREMENT
	    }
	    // try to set the new value
	    try {
		model.setValue(o);
		return true;
	    } catch (IllegalArgumentException iae) {
		// SpinnerModel didn't like new value
	    }
	    return false;
	}

	/* ===== End AccessibleAction impl ===== */

	/* ===== Begin AccessibleText impl ===== */

	/*
	 * Returns whether source and destination components have the
	 * same window ancestor
	 */
	private boolean sameWindowAncestor(Component src, Component dest) {
	    if (src == null || dest == null) {
		return false;
	    }
	    return SwingUtilities.getWindowAncestor(src) ==
		SwingUtilities.getWindowAncestor(dest);
	}

	/**
	 * Given a point in local coordinates, return the zero-based index
	 * of the character under that Point.  If the point is invalid,
	 * this method returns -1.
	 *
	 * @param p the Point in local coordinates
	 * @return the zero-based index of the character under Point p; if
	 * Point is invalid return -1.
	 */
	public int getIndexAtPoint(Point p) {
	    AccessibleText at = getEditorAccessibleText();
	    if (at != null && sameWindowAncestor(JSpinner.this, editor)) {
		// convert point from the JSpinner bounds (source) to
		// editor bounds (destination)
		Point editorPoint = SwingUtilities.convertPoint(JSpinner.this,
								p,
								editor);
		if (editorPoint != null) {
		    return at.getIndexAtPoint(editorPoint);
		}
	    }
	    return -1;
	}

	/**
	 * Determines the bounding box of the character at the given
	 * index into the string.  The bounds are returned in local
	 * coordinates.  If the index is invalid an empty rectangle is
	 * returned.
	 *
	 * @param i the index into the String
	 * @return the screen coordinates of the character's bounding box,
	 * if index is invalid return an empty rectangle.
	 */
	public Rectangle getCharacterBounds(int i) {
	    AccessibleText at = getEditorAccessibleText();
	    if (at != null ) {
		Rectangle editorRect = at.getCharacterBounds(i);
		if (editorRect != null &&
		    sameWindowAncestor(JSpinner.this, editor)) {
		    // return rectangle in the the JSpinner bounds
		    return SwingUtilities.convertRectangle(editor,
							   editorRect,
							   JSpinner.this);
		}
	    }
	    return null;
	}

	/**
	 * Returns the number of characters (valid indicies)
	 *
	 * @return the number of characters
	 */
	public int getCharCount() {
	    AccessibleText at = getEditorAccessibleText();
	    if (at != null) {
		return at.getCharCount();
	    }
	    return -1;
	}

	/**
	 * Returns the zero-based offset of the caret.
	 *
	 * Note: That to the right of the caret will have the same index
	 * value as the offset (the caret is between two characters).
	 * @return the zero-based offset of the caret.
	 */
	public int getCaretPosition() {
	    AccessibleText at = getEditorAccessibleText();
	    if (at != null) {
		return at.getCaretPosition();
	    }
	    return -1;
	}

	/**
	 * Returns the String at a given index.
	 *
	 * @param part the CHARACTER, WORD, or SENTENCE to retrieve
	 * @param index an index within the text
	 * @return the letter, word, or sentence
	 */
	public String getAtIndex(int part, int index) {
	    AccessibleText at = getEditorAccessibleText();
	    if (at != null) {
		return at.getAtIndex(part, index);
	    }
	    return null;
	}

	/**
	 * Returns the String after a given index.
	 *
	 * @param part the CHARACTER, WORD, or SENTENCE to retrieve
	 * @param index an index within the text
	 * @return the letter, word, or sentence
	 */
	public String getAfterIndex(int part, int index) {
	    AccessibleText at = getEditorAccessibleText();
	    if (at != null) {
		return at.getAfterIndex(part, index);
	    }
	    return null;
	}

	/**
	 * Returns the String before a given index.
	 *
	 * @param part the CHARACTER, WORD, or SENTENCE to retrieve
	 * @param index an index within the text
	 * @return the letter, word, or sentence
	 */
	public String getBeforeIndex(int part, int index) {
	    AccessibleText at = getEditorAccessibleText();
	    if (at != null) {
		return at.getBeforeIndex(part, index);
	    }
	    return null;
	}

	/**
	 * Returns the AttributeSet for a given character at a given index
	 *
	 * @param i the zero-based index into the text
	 * @return the AttributeSet of the character
	 */
	public AttributeSet getCharacterAttribute(int i) {
	    AccessibleText at = getEditorAccessibleText();
	    if (at != null) {
		return at.getCharacterAttribute(i);
	    }
	    return null;
	}

	/**
	 * Returns the start offset within the selected text.
	 * If there is no selection, but there is
	 * a caret, the start and end offsets will be the same.
	 *
	 * @return the index into the text of the start of the selection
	 */
	public int getSelectionStart() {
	    AccessibleText at = getEditorAccessibleText();
	    if (at != null) {
		return at.getSelectionStart();
	    }
	    return -1;
	}

	/**
	 * Returns the end offset within the selected text.
	 * If there is no selection, but there is
	 * a caret, the start and end offsets will be the same.
	 *
	 * @return the index into teh text of the end of the selection
	 */
	public int getSelectionEnd() {
	    AccessibleText at = getEditorAccessibleText();
	    if (at != null) {
		return at.getSelectionEnd();
	    }
	    return -1;
	}

	/**
	 * Returns the portion of the text that is selected.
	 *
	 * @return the String portion of the text that is selected
	 */
	public String getSelectedText() {
	    AccessibleText at = getEditorAccessibleText();
	    if (at != null) {
		return at.getSelectedText();
	    }
	    return null;
	}

	/* ===== End AccessibleText impl ===== */


	/* ===== Begin AccessibleEditableText impl ===== */

	/**
	 * Sets the text contents to the specified string.
	 *
	 * @param s the string to set the text contents
	 */
	public void setTextContents(String s) {
	    AccessibleEditableText at = getEditorAccessibleEditableText();
	    if (at != null) {
		at.setTextContents(s);
	    }
	}

	/**
	 * Inserts the specified string at the given index/
	 *
	 * @param index the index in the text where the string will
	 * be inserted
	 * @param s the string to insert in the text
	 */
	public void insertTextAtIndex(int index, String s) {
	    AccessibleEditableText at = getEditorAccessibleEditableText();
	    if (at != null) {
		at.insertTextAtIndex(index, s);
	    }
	}

	/**
	 * Returns the text string between two indices.
	 *
	 * @param startIndex the starting index in the text
	 * @param endIndex the ending index in the text
	 * @return the text string between the indices
	 */
	public String getTextRange(int startIndex, int endIndex) {
	    AccessibleEditableText at = getEditorAccessibleEditableText();
	    if (at != null) {
		return at.getTextRange(startIndex, endIndex);
	    }
	    return null;
	}

	/**
	 * Deletes the text between two indices
	 *
	 * @param startIndex the starting index in the text
	 * @param endIndex the ending index in the text
	 */
	public void delete(int startIndex, int endIndex) {
	    AccessibleEditableText at = getEditorAccessibleEditableText();
	    if (at != null) {
		at.delete(startIndex, endIndex);
	    }
	}

	/**
	 * Cuts the text between two indices into the system clipboard.
	 *
	 * @param startIndex the starting index in the text
	 * @param endIndex the ending index in the text
	 */
	public void cut(int startIndex, int endIndex) {
	    AccessibleEditableText at = getEditorAccessibleEditableText();
	    if (at != null) {
		at.cut(startIndex, endIndex);
	    }
	}

	/**
	 * Pastes the text from the system clipboard into the text
	 * starting at the specified index.
	 *
	 * @param startIndex the starting index in the text
	 */
	public void paste(int startIndex) {
	    AccessibleEditableText at = getEditorAccessibleEditableText();
	    if (at != null) {
		at.paste(startIndex);
	    }
	}

	/**
	 * Replaces the text between two indices with the specified
	 * string.
	 *
	 * @param startIndex the starting index in the text
	 * @param endIndex the ending index in the text
	 * @param s the string to replace the text between two indices
	 */
	public void replaceText(int startIndex, int endIndex, String s) {
	    AccessibleEditableText at = getEditorAccessibleEditableText();
	    if (at != null) {
		at.replaceText(startIndex, endIndex, s);
	    }
	}

	/**
	 * Selects the text between two indices.
	 *
	 * @param startIndex the starting index in the text
	 * @param endIndex the ending index in the text
	 */
	public void selectText(int startIndex, int endIndex) {
	    AccessibleEditableText at = getEditorAccessibleEditableText();
	    if (at != null) {
		at.selectText(startIndex, endIndex);
	    }
	}

	/**
	 * Sets attributes for the text between two indices.
	 *
	 * @param startIndex the starting index in the text
	 * @param endIndex the ending index in the text
	 * @param as the attribute set
	 * @see AttributeSet
	 */
	public void setAttributes(int startIndex, int endIndex, AttributeSet as) {
	    AccessibleEditableText at = getEditorAccessibleEditableText();
	    if (at != null) {
		at.setAttributes(startIndex, endIndex, as);
	    }
	}
    }  /* End AccessibleJSpinner */
}

/* class JOptionPane
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

package charvax.swing;

import charva.awt.BorderLayout;
import charva.awt.Component;
import charva.awt.Dimension;
import charva.awt.FlowLayout;
import charva.awt.Point;
import charva.awt.Toolkit;
import charva.awt.event.ActionEvent;
import charva.awt.event.ActionListener;
import charva.awt.event.KeyEvent;
import charva.awt.event.KeyListener;
import charvax.swing.border.EmptyBorder;

/**
 * <p>JOptionPane makes it easy to pop up a standard dialog box that prompts
 * the user for information or displays some information.</p>
 *
 * <p>The labels of the option-buttons displayed within the popup dialog can be
 * customized by changing static variables in the JOptionPane class.
 * Similarly, "accelerator keys" can be set to have the same effect
 * as the option-buttons. For example, the following code would set
 * the label of the OK button to "OK (F1)", and set the F1 key to 
 * have the same effect as the OK button:</p>
 * <pre>
 *	JOptionPane.OK_LABEL = "OK (F1)";
 *	JOptionPane.OK_ACCELERATOR = KeyEvent.VK_F1;
 * </pre>
 * <p>Note that after the buttons have been customized, they stay customized
 * for all future invocations of the JOptionPane "showXXXDialog()" methods.</p>
 *
 * <p>The parameters to these methods follow consistent patterns:</p>
 * <blockquote>
 * <dl>
 *   <dt>parentComponent
 *       <dd>Defines the Component that is to be the parent of this dialog 
 * box. It is used in two ways: its screen coordinates are 
 * used in the placement of the dialog box, and the dialog box inherits its
 * foreground and background colors from <code>parentComponent</code> (unless
 * the the JOptionPane's colors have been set explicitly). In general, the 
 * dialog box centered on top of <code>parentComponent</code>. This parameter 
 * may be null, in which case a default Frame is used as the parent, and 
 * the dialog will be centered on the screen.<p>
 *
 *   <dt>message
 *       <dd>A descriptive message to be placed in the dialog box. In the
 * most common usage, message is just a String or String constant. 
 * However, the type of this parameter is actually Object. Its 
 * interpretation depends on its type: 
 *
 * <blockquote>
 * <dl>
 *   <dt>String
 *       <dd>The string is displayed as a message on one line.
 *   <dt>Object[]
 *       <dd>An array of Objects is interpreted as a series of messages 
 * arranged in a vertical stack. Each Object is converted to a String
 * using its toString() method. The result is wrapped in a JLabel and displayed. 
 * </dl>
 * </blockquote>
 * <p>
 *   <dt>messageType
 *       <dd>Defines the style of the message. The possible values are:
 * <ul>
 * <li><code>ERROR_MESSAGE</code>
 * <li><code>INFORMATION_MESSAGE</code>
 * <li><code>WARNING_MESSAGE</code>
 * <li><code>QUESTION_MESSAGE</code>
 * <li><code>PLAIN_MESSAGE </code>
 * </ul><p>
 *   <dt>optionType
 *       <dd>Defines the set of option buttons that appear at the bottom of the dialog box:
 * <ul>
 * <li><code>DEFAULT_OPTION</code>
 * <li><code>YES_NO_OPTION</code>
 * <li><code>YES_NO_CANCEL_OPTION</code>
 * <li><code>OK_CANCEL_OPTION</code>
 * </ul>
 *       You aren't limited to this set of option buttons. You can 
 * provide any buttons you want using the <code>options</code> parameter.<p>
 *
 *   <dt>options
 *       <dd>A more detailed description of the set of option buttons that 
 * will appear at the bottom of the dialog box. The usual value for the 
 * options parameter is an array of Strings. But the parameter type is 
 * an array of Objects. A button is created for each object depending on 
 * its type:<p>
 * <dl>
 *   <dt>Component
 *       <dd>The component is added to the button row directly. 
 *   <dt>other
 *       <dd>The Object is converted to a string using its toString method 
 * and the result is used to label a JButton. 
 * </dl>
 * <p>
 *   <dt>icon
 *       <dd>This parameter is used in javax.Swing to specify a decorative 
 * icon to be placed in the dialog box. It is ignored by Charva.<p>
 *   <dt>title
 *       <dd>The title for the dialog box. <p>
 *   <dt>initialValue
 *       <dd>The default selection (input value). <p>
 * </dl>
 * </blockquote>
 */
public class JOptionPane
{
    /** Creates a JOptionPane with a test message */
    public JOptionPane() {
	this("This is a test message", PLAIN_MESSAGE, DEFAULT_OPTION, null, null, null);
    }

    /**
     * Creates an instance of JOptionPane to display the specified message.
     * @param message_ the message to display. It can be a String 
     * or an array of Strings. If it is an array of Strings, they are
     * stacked vertically.
     */
    public JOptionPane(Object message_) {
	this(message_, PLAIN_MESSAGE, DEFAULT_OPTION, null, null, null);
    }

    /**
     * Creates an instance of JOptionPane to display the specified message.
     * @param message_ the message to display. It can be a String 
     * or an array of Strings.
     * @param messagetype_ is ignored. It is used in javax.swing to
     * determine which icon to display.
     */
    public JOptionPane(Object message_, int messagetype_) {
	this(message_, messagetype_, DEFAULT_OPTION, null, null, null);
    }

    /**
     * Creates an instance of JOptionPane to display the specified message.
     * @param message_ the message to display. It can be a String 
     * or an array of Strings.
     * @param messagetype_ is ignored. It is used in javax.swing to
     * determine which icon to display.
     * @param optiontype_ determines which option-buttons to display. Allowed
     * values are: DEFAULT_OPTION, YES_NO_OPTION, YES_NO_CANCEL_OPTION, and
     * OK_CANCEL_OPTION.
     */
    public JOptionPane(Object message_, int messagetype_, int optiontype_) {
	this(message_, messagetype_, optiontype_, null, null, null);
    }

    /**
     * Creates an instance of JOptionPane to display the specified message.
     * @param message_ the message to display. It can be a String 
     * or an array of Strings.
     * @param messageType_ is ignored. It is used in javax.swing to
     * determine which icon to display.
     * @param optionType_ determines which option-buttons to display. Allowed
     * values are: DEFAULT_OPTION, YES_NO_OPTION, YES_NO_CANCEL_OPTION, and
     * OK_CANCEL_OPTION.
     * @param icon_ the Icon image to display (not used by Charva; it is here
     * for compatibility with Swing).
     * @param options_ the choices the user can select
     * @param initialValue_ the choice that is initially selected; if null, 
     * then nothing will be initially selected; only meaningful if "options"
     * is used
     */
    public JOptionPane(Object message_, int messageType_, int optionType_, 
	Object icon_, Object[] options_, Object initialValue_) 
    {
	_message = message_;
	_messagetype = messageType_;
	_optiontype = optionType_;
	_options = options_;
	_initialValue = initialValue_;
    }

    /** If newvalue_ is true, a JTextField will be displayed for
     * the user to provide text input.
     */
    public void setWantsInput(boolean newvalue_) {
	_wantsInput = newvalue_;
    }

    /** Returns the value of _wantsInput.
     */
    public boolean getWantsInput() {
	return _wantsInput;
    }

    /** Sets the default input value that is displayed to the user.
     * Only used if <code>_wantsInput</code> is true.
     */
    public void setInitialSelectionValue(Object value_) {
	_inputValue = (String) value_;
    }

    /** Sets the initial value of the text field for the user to modify.
     */
    public void setInputValue(Object value_) {
	_inputValue = (String) value_;
    }

    /** Returns the value the user has input, (relevant only if 
     * _wantsInput is true).
     */
    public Object getInputValue() {
	return _inputValue;
    }

    /** Sets the options this pane displays in the button-panel at
     * the bottom. If an element in 
     * newOptions is an instance of a subclass of AbstractButton (for
     * example, a JButton), it is added directly to the pane, 
     * otherwise a button is created for the element. The advantage
     * of adding a button rather than a string is that a mnemonic 
     * can be set for the button.
     */
    public void setOptions(Object[] newOptions_) {
	_options = newOptions_;
    }

    /** Returns the choices the user can make.
     */
    public Object[] getOptions() {
	return _options;
    }

    /** Sets the initial value that is to be enabled -- the Component
     * that has the focus when the pane is initially displayed.
     * (NOT IMPLEMENTED YET).
     */
    public void setInitialValue(Object initialValue_) {
    }

    /** Returns the initial value that is to be enabled -- the Component
     * that has the focus when the pane is initially displayed.
     * (NOT IMPLEMENTED YET).
     */
    public Object getInitialValue() {
	return null;
    }

    /**
     * Creates and returns a new JDialog for displaying the required 
     * message. The dialog inherits the foreground and background colors
     * of the <code>owner_</code> component and is centered on it.
     * If <code>owner_</code> is null, default (black/white) colors are used.
     */
    public JDialog createDialog(Component owner_, String title_) {

	/* This is a non-static inner class used for a popup dialog.
	 */
	JOptionPane.Popup dlg = null;

	dlg = this.new Popup(owner_, _message, title_);
	if (owner_ != null)
	    dlg.setLocationRelativeTo(owner_);

	return dlg;
    }

    /**
     * Brings up a dialog where the number of choices is dependent on the
     * value of the optiontype_ parameter.
     * @param parent_ Determines the frame in which the dialog is displayed.
     * @param message_ the String to display.
     * @param title_ the title of the dialog.
     * @param optiontype_ must be YES_NO_OPTION or YES_NO_CANCEL_OPTION.
     */
    public static int showConfirmDialog(Component parent_,
	    Object message_, String title_, int optiontype_) {

	JOptionPane pane = new JOptionPane(message_, PLAIN_MESSAGE, optiontype_);
	Popup dialog = (Popup) pane.createDialog(parent_, title_);
	dialog.show();
	return ((Integer) pane.getValue()).intValue();
    }

    /**
     * Brings up a dialog that allows the user to input a value.
     * @param parent_ Determines the frame in which the dialog is displayed.
     * @param message_ the String to display.
     * @param title_ the title of the dialog.
     * @param messagetype_ is ignored (it is used in javax.swing to determine
     * which icon to display).
     */
    public static String showInputDialog(Component parent_,
	    Object message_, String title_, int messagetype_) {

	JOptionPane pane = new JOptionPane(message_, 
		messagetype_, OK_CANCEL_OPTION);
	pane._wantsInput = true;
	Popup dialog = (Popup) pane.createDialog(parent_, title_);
	dialog.show();
	int option = ((Integer) pane.getValue()).intValue();
	if (option == CANCEL_OPTION)
	    return null;
	else
	    return (String) pane.getInputValue();
    }

    /**
     * Brings up a confirmation dialog titled "Confirm"
     */
    public static void showMessageDialog(Component parent_, Object message_) {
	showMessageDialog(parent_, message_, "Confirm", DEFAULT_OPTION);
    }

    /**
     * Brings up a confirmation dialog with the specified title.  The
     * msgtype parameter is ignored (it is used in the javax.swing package to
     * specify an icon to display).
     */
    public static void showMessageDialog(Component parent_,
	    Object message_, String title_, int msgtype_) {

	JOptionPane pane = new JOptionPane(message_, msgtype_, DEFAULT_OPTION);
	JDialog dialog = pane.createDialog(parent_, title_);
	dialog.show();
    }

    /** Returns the option the user has selected.
     */
    public Object getValue() {
	return _value;
    }

    // INSTANCE VARIABLES
    protected Object _message;
    protected int _messagetype;

    /** Determines which option buttons to display (unless an array
     * of options is explicitly specified with <code>setOptions()</code>).
     */
    protected int _optiontype;

    /** If true, an TextField will be displayed for the user to provide
     * input.
     */
    protected boolean _wantsInput = false;

    protected String _inputValue = "";

    /** Array of options to display to the user in the bottom button-panel.
     * The objects in this array can be any combination of Strings or 
     * components which are subclasses of AbstractButton.
     * Buttons are just added to the bottom button-panel; Strings are
     * wrapped in a JButton which is then added to the button-panel.
     */
    protected Object[] _options;

    /** Option that should be initially selected in <code>_options</code>.
     */
    protected Object _initialValue;

    /** The currently selected option.
     */
    protected Object _value;

    // Message types
    public static final int ERROR_MESSAGE = 100;
    public static final int INFORMATION_MESSAGE = 101;
    public static final int WARNING_MESSAGE = 102;
    public static final int QUESTION_MESSAGE = 103;
    public static final int PLAIN_MESSAGE = 104;

    // Option types
    public static final int DEFAULT_OPTION = 200;
    public static final int YES_NO_OPTION = 201;
    public static final int YES_NO_CANCEL_OPTION = 202;
    public static final int OK_CANCEL_OPTION = 203;

    // Return values
    public static final int YES_OPTION = 300;
    public static final int NO_OPTION = 301;
    public static final int CANCEL_OPTION = 302;
    public static final int OK_OPTION = 303;
    public static final int CLOSED_OPTION = 304;

    // Label values - can be changed to customize appearance
    public static String YES_LABEL = "Yes";
    public static String NO_LABEL = "No";
    public static String CANCEL_LABEL = "Cancel";
    public static String OK_LABEL = "OK";

    // Accelerator keystrokes - can be customized.
    public static int YES_ACCELERATOR = -1;
    public static int NO_ACCELERATOR = -1;
    public static int CANCEL_ACCELERATOR = -1;
    public static int OK_ACCELERATOR = -1;

    //====================================================================

    /** This is a non-static inner class used for the popup dialog that 
     * JOptionPane creates.
     */
    private class Popup
	extends JDialog
	implements ActionListener, KeyListener
    {
	/** Constructor
	 */
	Popup(Component owner_, Object message_, String title_) {

	    super();

	    // The window inherits the colors of its parent if there is one,
	    // otherwise the default colors are used.
	    if (owner_ != null) {
		super.setForeground(owner_.getForeground());
		super.setBackground(owner_.getBackground());
	    }
	    else {
		super.setForeground(Toolkit.getDefaultForeground());
		super.setBackground(Toolkit.getDefaultBackground());
	    }
	    setTitle(title_);

	    _ownerComponent = owner_;
	    setLayout(new BorderLayout());

	    JPanel northpan = new JPanel();
	    northpan.setBorder(new EmptyBorder(2,2,1,2));

	    JPanel messagepanel = new JPanel();
	    if (message_ instanceof String) {
		messagepanel.add(new JLabel((String) message_));
	    }
	    else if (message_ instanceof Object[]) {
		messagepanel.setLayout(
			new BoxLayout(getOwner(), BoxLayout.Y_AXIS));

		Object[] objects = (Object[]) message_;
		for (int i=0; i<objects.length; i++) {
		    messagepanel.add(new JLabel(objects[i].toString()));
		}
	    }
	    else {
		throw new IllegalArgumentException("illegal message type " +
		       message_.getClass().getName());
	    }

	    northpan.add(messagepanel);
	    add(northpan, BorderLayout.NORTH);

	    if (_wantsInput) {
		JPanel centerpan = new JPanel();
		centerpan.setBorder(new EmptyBorder(1, 1, 1, 1));
		_inputField = new JTextField(_inputValue, 20);
		_inputField.addActionListener(this);
		centerpan.add(_inputField);
		add(centerpan, BorderLayout.CENTER);
	    }

	    JPanel southpan = new JPanel();
	    southpan.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
	
	    if (_options != null) {
		// Option buttons were explicitly specified.
		for (int i=0; i<_options.length; i++) {
		    AbstractButton button = null;
		    if (_options[i] instanceof String)
			button = new JButton((String) _options[i]);
		    else if (_options[i] instanceof AbstractButton)
			button = (AbstractButton) _options[i];
		    button.addActionListener(this);
		    southpan.add(button);
		}
	    }
	    else {
		// Decide which option buttons to display, based on the
		// value of _optiontype.
		if (_optiontype == DEFAULT_OPTION || 
			_optiontype == OK_CANCEL_OPTION) {

		    _okButton = new JButton("OK");
		    _okButton.setText(OK_LABEL);
		    _okButton.addActionListener(this);
		    southpan.add(_okButton);
		}
		if (_optiontype == YES_NO_OPTION ||
			_optiontype == YES_NO_CANCEL_OPTION) {

		    _yesButton = new JButton("Yes");
		    _yesButton.setText(YES_LABEL);
		    _yesButton.addActionListener(this);
		    southpan.add(_yesButton);
		}
		if (_optiontype == YES_NO_OPTION ||
			_optiontype == YES_NO_CANCEL_OPTION) {

		    _noButton = new JButton("No");
		    _noButton.setText(NO_LABEL);
		    _noButton.addActionListener(this);
		    southpan.add(_noButton);
		}
		if (_optiontype == YES_NO_CANCEL_OPTION ||
			_optiontype == OK_CANCEL_OPTION) {

		    _cancelButton = new JButton("Cancel");
		    _cancelButton.setText(CANCEL_LABEL);
		    _cancelButton.addActionListener(this);
		    southpan.add(_cancelButton);
		}
	    }
	    add(southpan, BorderLayout.SOUTH);

	    setSize(Toolkit.getDefaultToolkit().getScreenSize());
	    pack();
	    Dimension msgsize = messagepanel.getSize();
	    setSize(msgsize.width+8, msgsize.height+9);
	    pack();

	    /* Center the dialog over its parent component.
	     */
	    Dimension ourSize = getSize();
	    if (_ownerComponent != null) {
		Point ownerOrigin = _ownerComponent.getLocationOnScreen();
		Dimension ownerSize = _ownerComponent.getSize();
		Point ownerCenter = ownerOrigin.addOffset(
		    ownerSize.width/2, ownerSize.height/2);
		setLocation(ownerCenter.addOffset(
		    -ourSize.width/2, -ourSize.height/2));
	    }
	    else {
		/* The parent component was not specified. Center this
		 * dialog box in the middle of the screen.
		 */
		Dimension screensize =
		    Toolkit.getDefaultToolkit().getScreenSize();
		Point screenCenter = new Point(screensize.width/2,
		    screensize.height/2);
		setLocation(screenCenter.addOffset(
		    -ourSize.width/2, -ourSize.height/2));
	    }

	    // Add a KeyListener in case one or more accelerators were set.
	    addKeyListener(this);
	}

	/** Gets called when the user presses an option-button (or
	 * if ENTER is pressed while focus is in the TextField).
	 */
	public void actionPerformed(ActionEvent e_) {
	    if (_wantsInput)
		_inputValue = _inputField.getText();

	    if (_options != null) {
		// Options were specified explicitly.
		// So ignore ENTER if pressed while focus is in textfield.
		if (e_.getSource() == _inputField)
		    return;

		AbstractButton source = (AbstractButton) e_.getSource();
		for (int i=0; i < _options.length; i++) {
		    if (_options[i] instanceof String && 
			    source.getText().equals(_options[i]) ) {

			_value = _options[i];
			break;
		    }
		    else if (source == _options[i]) {
			_value = source;
			break;
		    }
		}
	    }
	    else {
		// Options were not specified explicitly.
		Object source = e_.getSource();
		if ( source == _okButton || source == _inputField) {
		    _value = new Integer(JOptionPane.OK_OPTION);
		}
		else if (source == _yesButton) {
		    _value = new Integer(JOptionPane.YES_OPTION);
		}
		else if (source == _noButton) {
		    _value = new Integer(JOptionPane.NO_OPTION);
		}
		else if (source == _cancelButton) {
		    _value = new Integer(JOptionPane.CANCEL_OPTION);
		}
	    }

	    hide();
	}

	public void keyPressed(KeyEvent e_) {
	    int key = e_.getKeyCode();
	    if (key == OK_ACCELERATOR &&
		    (_optiontype == DEFAULT_OPTION || 
		    _optiontype == OK_CANCEL_OPTION)) {

		_value = new Integer(JOptionPane.OK_OPTION);
		_inputValue = _inputField.getText();
		hide();
	    }
	    else if (key == YES_ACCELERATOR &&
		    (_optiontype == YES_NO_OPTION ||
		    _optiontype == YES_NO_CANCEL_OPTION)) {
		_value = new Integer(JOptionPane.YES_OPTION);
		_inputValue = _inputField.getText();
		hide();
	    }
	    else if (key == NO_ACCELERATOR &&
		    (_optiontype == YES_NO_OPTION ||
		    _optiontype == YES_NO_CANCEL_OPTION)) {
		_value = new Integer(JOptionPane.NO_OPTION);
		hide();
	    }
	    else if (key == CANCEL_ACCELERATOR &&
		    (_optiontype == YES_NO_CANCEL_OPTION ||
		    _optiontype == OK_CANCEL_OPTION)) {
		_value = new Integer(JOptionPane.CANCEL_OPTION);
		hide();
	    }
	}

	public void keyTyped(KeyEvent e_) {
	}

	public void keyReleased(KeyEvent e_) { }

	private Component _ownerComponent;
	private JButton _okButton;
	private JButton _yesButton;
	private JButton _noButton;
	private JButton _cancelButton;
	private JTextField _inputField = new JTextField(20);
    }
}

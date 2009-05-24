/* class JFileChooser
 *
 * Copyright (C) 2003  R M Pitman
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

/*
 * Modified Jul 14, 2003 by Tadpole Computer, Inc.
 * Modifications Copyright 2003 by Tadpole Computer, Inc.
 *
 * Modifications are hereby licensed to all parties at no charge under
 * the same terms as the original.
 *
 * Fixed bug to allow save dialog to work when files do not exist.
 * Added setSelectedFile method.  Fixed fileSelectionMode to mean
 * that when FILES_ONLY, entry of a directory name in the textfield now
 * causes the appropriate setCurrentDirectory() call.
 */

package charvax.swing;

import java.io.File;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import charva.awt.BorderLayout;
import charva.awt.Component;
import charva.awt.Dialog;
import charva.awt.Dimension;
import charva.awt.FlowLayout;
import charva.awt.Insets;
import charva.awt.Point;
import charva.awt.Toolkit;
import charva.awt.event.ActionEvent;
import charva.awt.event.ActionListener;
import charva.awt.event.EventListener;
import charva.awt.event.KeyEvent;
import charva.awt.event.KeyListener;
import charvax.swing.border.EmptyBorder;
import charvax.swing.border.TitledBorder;
import charvax.swing.event.ListSelectionEvent;
import charvax.swing.event.ListSelectionListener;
import charvax.swing.filechooser.FileFilter;

/**
 * <p>The JFileChooser class displays a dialog from which the user can choose
 * a file.  The dialog is always modal (i.e. the user cannot interact
 * with any other windows until he closes the dialog).</p>
 *
 * <p>The dialog is displayed by calling its showDialog() method, which blocks 
 * until the dialog is closed (by the user pressing the Approve or Cancel 
 * buttons, or by pressing ENTER while the focus is in the "Filename" field).
 * After the dialog has been closed, the program can find out what 
 * File was selected by calling the getSelectedFile() method.</p>
 *
 * The labels of the buttons that are displayed in the JFileChooser
 * can be customized by changing the following static variables:
 * <ul>
 * <li> <code>PARENT_DIRECTORY_LABEL</code>
 * <li> <code>NEW_DIRECTORY_LABEL</code>
 * <li> <code>APPROVE_LABEL</code>
 * <li> <code>CANCEL_LABEL</code>
 * </ul>
 * <p>"Accelerator keys" can also be set for the buttons. For example,
 * to set the F1 key to have the same effect as pressing the CANCEL
 * button, call the following code before using the JFileChooser:</p>
 * <pre>
 *	JFileChooser.CANCEL_LABEL = "Cancel (F1)";
 *	JFileChooser.CANCEL_ACCELERATOR = KeyEvent.VK_F1;
 * </pre>
 * Note that after the buttons have been customized, they stay customized
 * for all future invocations of JFileChooser (until they are re-customized
 * to some other value).
 */
public class JFileChooser
    extends JComponent
{
    /**
     * Constructs a JFileChooser pointing to the user's home directory.
     */
    public JFileChooser()
    {
	this((File) null);
    }

    /**
     * Constructs a JFileChooser pointing to the specified directory.
     * Passing in a null parameter causes the JFileChooser to point to 
     * the user's home directory.
     */
    public JFileChooser(File currentDirectory_) 
    {
	setCurrentDirectory(currentDirectory_);
    }

    /**
     * Constructs a JFileChooser with the specified pathname. Passing a value
     * of <code>null</code> causes the file chooser to point to the user's
     * home directory.
     */
    public JFileChooser(String currentDirectoryPath_)
    {
	if (currentDirectoryPath_ == null)
	    setCurrentDirectory(null);
	else
	    setCurrentDirectory(new File(currentDirectoryPath_));
    }


    /** Set the current directory. Passing a parameter of <code>null</code>
     * cause the JFileChooser to point to the user's home directory.
     */
    public void setCurrentDirectory(File dir_) {
	if (dir_ == null) {
        dir_ = new File(System.getProperty("user.home"));
	}

	if (dir_.isDirectory() == false) {
	    throw new IllegalArgumentException("not a directory");
	}
	_currentDirectory = dir_;
	_location = dir_.getAbsolutePath();
    }

    /**
     * Returns the currently displayed directory.
     */
    public File getCurrentDirectory() { 
	return _currentDirectory; 
    }

    /**
     * Get the File selected by the user.  If the user pressed Cancel, 
     * the return value is null.
     */
    public File getSelectedFile() {
	if (_cancelWasPressed)
	    return null;

	return new File(_location);
    }

    public void setSelectedFile(File file_) {
	if (!file_.isAbsolute()) {
	    file_ = new File(_currentDirectory, file_.getPath());
	}

	File parent = file_.getParentFile();

	if (!file_.isDirectory() && (parent != null)) {
	    _currentDirectory = parent;
	    _location = file_.getAbsolutePath();
	} else if (file_.isDirectory()) {
	    _currentDirectory = file_;
	    _location = file_.getAbsolutePath();
	}
	fireFileChooserEvent();
    }

    /**
     * Pops up a custom file chooser dialog with a custom approve button.
     * @param parent_ the parent component of the dialog; can be
     * <code>null</code>. 
     * @param approveButtonText_ the custom text string to display in the 
     * Approve button.
     * @return the return state of the file chooser on popdown:
     * <ul><li>JFileChooser.CANCEL_OPTION
     * <li>JFileChooser.APPROVE_OPTION
     * <li>JFileChooser.ERROR_OPTION</ul>
     */
    public int showDialog(Component parent_, String approveButtonText_)
    {
	_approveButtonText = approveButtonText_;
	JDialog chooserDialog = new ChooserDialog(parent_);
	chooserDialog.setLocationRelativeTo(parent_);
	chooserDialog.show();
	if (_cancelWasPressed)
	    return CANCEL_OPTION;
	else
	    return APPROVE_OPTION;
    }

    /**
     * Pops up a "Save File" file chooser dialog; this is a convenience 
     * method and is equivalent to showDialog(Component, "Save").
     * @return the return state of the file chooser on popdown:
     * <ul><li>JFileChooser.CANCEL_OPTION
     * <li>JFileChooser.APPROVE_OPTION
     * <li>JFileChooser.ERROR_OPTION</ul>
     */
    public int showSaveDialog(Component parent_)
    {
	return showDialog(parent_, "Save");
    }

    /**
     * Pops up a "Open File" file chooser dialog; this is a convenience 
     * method and is equivalent to showDialog(Component, "Open").
     * @return the return state of the file chooser on popdown:
     * <ul>
     * <li>JFileChooser.CANCEL_OPTION
     * <li>JFileChooser.APPROVE_OPTION
     * <li>JFileChooser.ERROR_OPTION
     * </ul>
     */
    public int showOpenDialog(Component parent_)
    {
	return showDialog(parent_, "Open");
    }

    /**
     * Sets the <code>JFileChooser</code> to allow the user to select
     * files only directories only, or files and directories. The default
     * is JFileChooser.FILES_ONLY.
     */
    public void setFileSelectionMode(int mode_)
    {
	if (mode_ < FILES_ONLY || mode_ > FILES_AND_DIRECTORIES)
	    throw new IllegalArgumentException("invalid file selection mode");

	_fileSelectionMode = mode_;
    }

    /** Returns the current file-selection mode.
     * @return the file-selection mode, one of the following:<p>
     * <ul>
     * <li><code>JFileChooser.FILES_ONLY</code>
     * <li><code>JFileChooser.DIRECTORIES_ONLY</code>
     * <li><code>JFileChooser.FILES_AND_DIRECTORIES</code>
     * </ul>
     */
    public int getFileSelectionMode() { 
	return _fileSelectionMode; 
    }

    public void setDialogTitle(String title_) {
	_title = title_;
    }

    /**
     * Sets the current file filter. The file filter is used by the
     * file chooser to filter out files from the user's view.
     */
    public void setFileFilter(FileFilter filter_)
    {
	_fileFilter = filter_;
    }

    /**
     * Returns the currently selected file filter.
     */
    public FileFilter getFileFilter()
    {
	return _fileFilter;
    }

    public void debug(int level_) {
	System.err.println("JFileChooser origin=" + _origin + 
		" title=" + _title );
    }

    /** Required to implement abstract method of JComponent (never used).
     */
    public Dimension minimumSize() {
	return null;
    }

    /** Required to implement abstract method of JComponent (never used).
     */
    public Dimension getSize() {
	return null;
    }

    /** Required to implement abstract method of JComponent (never used).
     */
    public int getHeight() {
	return 0;
    }

    /** Required to implement abstract method of JComponent (never used).
     */
    public int getWidth() {
	return 0;
    }

    protected void addFileChooserListener(FileChooserListener l)
    {
	_filelisteners.addElement(l);
    }

    protected void fireFileChooserEvent()
    {
	Enumeration<FileChooserListener> e = _filelisteners.elements();
	while (e.hasMoreElements()) {
	    FileChooserListener l = (FileChooserListener) e.nextElement();
	    l.fileChanged(new FileChooserEvent(this));
	}
    }

    //====================================================================
    // INSTANCE VARIABLES
    protected String _title;
    protected String _approveButtonText = "Open File";

    /** The current directory shown in the dialog.
     */
    protected File _currentDirectory = null;
    protected JFileChooser.DirList _dirList = this.new DirList();
    protected String _location = "";
    protected boolean _cancelWasPressed = true;
    protected int _fileSelectionMode = FILES_ONLY;
    protected FileFilter _fileFilter = null;
    protected Vector<FileChooserListener> _filelisteners = new Vector<FileChooserListener>();

    protected static final int _COLS = 50;
    protected static final int _ROWS = 20;

    public static final int FILES_ONLY = 200;
    public static final int DIRECTORIES_ONLY = 201;
    public static final int FILES_AND_DIRECTORIES = 202;

    public static final int CANCEL_OPTION = 300;
    public static final int APPROVE_OPTION = 301;
    public static final int ERROR_OPTION = 302;

    // Default button labels - can be customized.
    public static String CANCEL_LABEL = "Cancel";
    public static String APPROVE_LABEL = "Approve";
    public static String PARENT_DIRECTORY_LABEL = "Parent Directory";
    public static String NEW_DIRECTORY_LABEL = "New Directory";

    // Button accelerators (disabled by default).
    public static int CANCEL_ACCELERATOR = -1;
    public static int APPROVE_ACCELERATOR = -1;
    public static int PARENT_DIRECTORY_ACCELERATOR = -1;
    public static int NEW_DIRECTORY_ACCELERATOR = -1;

    /*====================================================================
     * This is a nonstatic inner class used by JFileChooser to display
     * a popup dialog.
     */
    private class ChooserDialog
	extends JDialog
	implements ActionListener, ListSelectionListener, KeyListener,
		   FileChooserListener
    {
	ChooserDialog(Component parent_) {
	    setTitle(_title);
	    setSize(_COLS, _ROWS);

	    // Inherit colors from the parent component unless they have
	    // been set already.
	    if (JFileChooser.this.getForeground() == null)
		setForeground(parent_.getForeground());
	    else
		setForeground(JFileChooser.this.getForeground());

	    if (JFileChooser.this.getBackground() == null)
		setBackground(parent_.getBackground());
	    else
		setBackground(JFileChooser.this.getBackground());

	    /* Insert the directory list in the west.
	     */
	    _dirList.setVisibleRowCount(12);
	    _dirList.setColumns(45);
	    _dirList.addListSelectionListener(this);
	    displayCurrentDirectory();

	    _scrollPane = new JScrollPane(_dirList);
	    _scrollPane.setViewportBorder(new TitledBorder("Files"));
	    add(_scrollPane, BorderLayout.WEST);

	    /* Insert a north panel that contains the Parent and New buttons.
	     */
	    JPanel toppanel = new JPanel();
	    toppanel.setBorder(new EmptyBorder(0,1,0,1));
	    toppanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 0));
	    toppanel.add(_parentButton);
	    _parentButton.setText(PARENT_DIRECTORY_LABEL);
	    _parentButton.addActionListener(this);

	    toppanel.add(_newButton);
	    _newButton.setText(NEW_DIRECTORY_LABEL);
	    _newButton.addActionListener(this);
	    add(toppanel, BorderLayout.NORTH);

	    /* Insert a panel in the south for the textfield and the
	     * Approve and Cancel buttons.
	     */
	    JPanel southpanel = new JPanel();
	    southpanel.setLayout(new BorderLayout());

	    JPanel topsouth = new JPanel();
	    topsouth.add(new JLabel("Pathname:"));
	    topsouth.add(_locationField);
	    _locationField.setText(_location);
	    _locationField.setActionCommand("locationField");
	    _locationField.addActionListener(this);
	    southpanel.add(topsouth, BorderLayout.NORTH);

	    JPanel bottomsouth = new JPanel();
	    bottomsouth.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 0));
	    bottomsouth.setBorder(new EmptyBorder(1,1,0,1));
	    bottomsouth.add(_approveButton);
	    bottomsouth.add(_cancelButton);
	    _approveButton.setText(_approveButtonText);
	    _cancelButton.setText(CANCEL_LABEL);
	    _approveButton.addActionListener(this);
	    _cancelButton.addActionListener(this);
	    southpanel.add(bottomsouth, BorderLayout.SOUTH);

	    add(southpanel, BorderLayout.SOUTH);

	    pack();
	    Insets insets = getInsets();
	    _dirList.setColumns(getWidth() - insets.left - insets.right - 2);

	    addKeyListener(this);
	    addFileChooserListener(this);
	}

	/** Implements the ActionListener interface. Handles button-presses,
	 * and the ENTER keystroke in the Pathname field.
	 */
	public void actionPerformed(ActionEvent e_) {
	    Object source = e_.getSource();
	    if (source == _parentButton) {
		_doParentDirectoryAction();
	    }
	    else if (source == _newButton) {
		_doNewDirectoryAction();
	    }
	    else if (source == _approveButton) {
		_doApproveAction();
	    }
	    else if (source == _cancelButton) {
		_doCancelAction();
	    }
	    else if (source == _locationField) {
		_doApproveAction();
	    }
	}

	/** Implements the KeyListener interface
	 */
	public void keyPressed(KeyEvent e_) {
	    int key = e_.getKeyCode();
	    if (key == PARENT_DIRECTORY_ACCELERATOR) {
		_doParentDirectoryAction(); 
	    }
	    else if (key == NEW_DIRECTORY_ACCELERATOR) {
		_doNewDirectoryAction();
	    }
	    else if (key == APPROVE_ACCELERATOR) {
		_doApproveAction(); 
	    }
	    else if (key == CANCEL_ACCELERATOR) {
		_doCancelAction(); 
	    }
	}

	/** Implements the KeyListener interface
	 */
	public void keyTyped(KeyEvent e_) {
	}

	/** Implements KeyListener interface; is never called.
	 */
	public void keyReleased(KeyEvent e_) { }

	/** Implements the ListSelectionListener interface.
	 */
	public void valueChanged(ListSelectionEvent e_) {
	    String listitem = (String) _dirList.getSelectedValue();
	    if (listitem == null) {
		// The selection is empty; so there must have been a 
		// file selected, but it has just been deselected.
		_locationField.setText(_currentDirectory.getAbsolutePath());
		return;
	    }

	    /* Strip the trailing "/"
	     */
	    if (listitem.endsWith("/"))
		listitem = listitem.substring(0, listitem.length()-1);

	    File file = new File(_currentDirectory, listitem);
	    if (file.canRead() == false) {
		String[] msgs = {
		    "File or directory not readable:",
		    file.getAbsolutePath() };
		JOptionPane.showMessageDialog(
			this, msgs, "Error", JOptionPane.ERROR_MESSAGE);
		return;
	    }
	    else if (file.isDirectory() == false) {
		if (_fileSelectionMode == DIRECTORIES_ONLY) {
		    String[] msgs = {
			"Not a directory:",
			file.getAbsolutePath() };
		    JOptionPane.showMessageDialog(
			    this, msgs, "Error", JOptionPane.ERROR_MESSAGE);
		}
		else {
		    _locationField.setText(file.getAbsolutePath());
		}
		return;
	    }

	    // The selected file is a directory.
	    setCurrentDirectory(file);
	    displayCurrentDirectory();
	    repaint();

	    _scrollPane.getViewport().setViewPosition(new Point(0,0));
	    _scrollPane.repaint();

	    /* If the newly selected directory is a root directory,
	     * don't allow the Parent button to be pressed.
	     */
	    if (_isRoot(_currentDirectory) == false)
		_parentButton.setEnabled(true);
	}

	/** Implements the FileChooserListener interface.
	 */
	public void fileChanged(FileChooserEvent e)
	{
	    displayCurrentDirectory();
	    repaint();
	}

	private void _doNewDirectoryAction() {
	    JFileChooser.NewDirDialog dlg = 
		new NewDirDialog(this, _currentDirectory);

	    dlg.setLocation(getLocation().addOffset(2, 2));
	    dlg.show();
	    File newdir = dlg.getDirectory();
	    if (newdir != null)
		setCurrentDirectory(newdir);
	    	displayCurrentDirectory();
	    	repaint();
	}

	private void _doParentDirectoryAction() {
	    if (_isRoot(_currentDirectory)) {
		// We are already in a root directory.  Display the 
		// filesystem roots in the listbox. The list of 
		// root directories is system-dependent; on Windows it 
		// would be A:, B:, C: etc.  On Unix it would be "/".
	    	File[] roots = File.listRoots();
	    	for (int i=0; i<roots.length; i++) {
	    	    DefaultListModel listModel = 
		    	(DefaultListModel) _dirList.getModel();
	    	    listModel.addElement(roots[i].getAbsolutePath());
	    	}
	    	_location = "";
	    }
	    else {
		File parent = _currentDirectory.getParentFile();
		if (_isRoot(parent)) {
		    _parentButton.setEnabled(false);
		    _dirList.requestFocus();
		}
		setCurrentDirectory(parent);
	    	displayCurrentDirectory();
	    	repaint();
	    }
	}

	private void _doApproveAction() {
	    File file = new File(_locationField.getText());
	    String errmsg = null;

	    if (_fileSelectionMode == DIRECTORIES_ONLY && 
		file.isDirectory() == false) {

		errmsg = "Entry is not a directory: ";
	    }
	    else if (_fileSelectionMode == FILES_ONLY &&
		file.isDirectory()) {

		setCurrentDirectory(file);
		displayCurrentDirectory();
		repaint();
		return;
	    }

	    if (errmsg != null) {
		String[] msgs = { errmsg, _locationField.getText() };
		JOptionPane.showMessageDialog(
		    this, msgs, "Error", JOptionPane.ERROR_MESSAGE);
		return;
	    }

	    _cancelWasPressed = false;
	    _location = _locationField.getText();
	    hide();
	}

	private void _doCancelAction() {
	    _cancelWasPressed = true;
	    hide();
	}

	/** Returns true if the specified file is a root directory.
	 */
	private boolean _isRoot(File dir_) {
	    String dirname = dir_.getAbsolutePath();

	    File[] roots = File.listRoots();
	    for (int i=0; i<roots.length; i++) {
		if (roots[i].getAbsolutePath().equals(dirname))
		    return true;
	    }
	    return false;
	}


	/**
	 * Causes the JFileChooser to scan its file list for the current
	 * directory, using the currently selected file filter if applicable.
	 * Note that this method does not cause the file chooser to be redrawn.
	 */
	private void displayCurrentDirectory()
	{
	    /* Clear the list of Files in the current dir
	     */
	    _dirList.clear();

	    DefaultListModel listModel = (DefaultListModel) _dirList.getModel();

	    /* Add all the current directory's children into the list.
	     */
	    File[] files = _currentDirectory.listFiles();

	    /* Define and instantiate an anonymous class that implements
	     * the Comparator interface. This will be used by the TreeSet
	     * to keep the filenames in lexicographical order.
	     */
	    Comparator<String> fileSorter = new Comparator<String>() {
		public int compare(String file1, String file2) {
		    return file1.compareTo(file2);
		}
	    };

	    TreeSet<String> dirs = new TreeSet<String>(fileSorter);
	    int numEntries = 0;
	    for (int i=0; i<files.length; i++) {
		if (files[i].isDirectory()) {
		    dirs.add(files[i].getName() + "/");
		}
		else if ((_fileSelectionMode != DIRECTORIES_ONLY) &&
			 (_fileFilter == null ||
			  _fileFilter.accept(files[i]))) {
		    /* This is a regular file, and either there is no
		     * file filter or the file is accepted by the
		     * filter.
		     */
		    dirs.add(files[i].getName());
		}
		numEntries++;
	    }

	    /* Copy the filenames from the TreeSet to the JList widget
	     */
	    Iterator<String> iter = dirs.iterator();
	    while (iter.hasNext()) {
		listModel.addElement(iter.next());
	    }
	    _locationField.setText(_location);
	}

	private JScrollPane _scrollPane;
	protected JButton _cancelButton = new JButton("Cancel");
	protected JButton _approveButton = new JButton("Open");
	protected JButton _parentButton = new JButton("Parent Directory");
	protected JButton _newButton = new JButton("New Directory");
	private JTextField _locationField = new JTextField(35);
    }

    /*====================================================================
     * This is a non-static inner class used by the JFileChooser 
     * to implement a sorted list of directory names. The user can
     * find a directory quickly by entering the first few characters
     * of the directory name.
     */
    private class DirList
	extends JList
    {
	DirList() {
	    super();
	    setVisibleRowCount(10);
	    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	/** Clears the list of files displayed by this JList. 
	 * Does not generate any ListSelectionEvents. (?)
	 */
	void clear() {
	    // Clear the selection model without notifying any 
	    // ListSelectionListeners.
	    int min = getSelectionModel().getMinSelectionIndex();
	    if (min != -1) {
	    	int max = getSelectionModel().getMaxSelectionIndex();
		getSelectionModel().removeIndexInterval(min, max);
	    }

	    // Clear the contents of the data model.
	    ((DefaultListModel) getModel()).clear();
	    _currentRow = 0;
	    _matchbuf.setLength(0);
	}

	/** Overrides corresponding method in JList, and allows the
	 * user to find a directory quickly by typing the first few letters
	 * of the filename.
	 */
	public void processKeyEvent(KeyEvent evt_)
	{
	    int key = evt_.getKeyCode();
	    ListModel listmodel = super.getModel();
	    if (listmodel.getSize() > 0 && 
		    (key == KeyEvent.VK_BACK_SPACE || 
			(key > ' ' && key < 255))) {

		if (key == KeyEvent.VK_BACK_SPACE) {
		    if (_matchbuf.length() > 0)	    // truncate
			_matchbuf.setLength(_matchbuf.length() - 1);
		    else
			Toolkit.getDefaultToolkit().beep();
		}
		else
		    _matchbuf.append((char) key);

		/* Scan through the items in the list until we get
		 * to an item that is lexicographically greater than the
		 * pattern we have typed.
		 */
		String matchstring = _matchbuf.toString();
		int i;
		for (i=0; i<listmodel.getSize(); i++) {
		    if (matchstring.compareTo(listmodel.getElementAt(i).toString()) <= 0) {
			break;
		    }
		}
		if (i == listmodel.getSize())
		    i--;    // the loop completed.

		String item = (String) listmodel.getElementAt(i);
		if ( ! item.startsWith(matchstring))
		    Toolkit.getDefaultToolkit().beep();

		_currentRow = i;
		super.ensureIndexIsVisible(i);
	    }
	    super.processKeyEvent(evt_);
	}

	// INSTANCE VARIABLE
	private StringBuffer _matchbuf = new StringBuffer();
    }

    /*====================================================================
     * This is a non-static inner class used by the JFileChooser 
     * to get the name of the new directory to create.
     */
    private class NewDirDialog
	extends JDialog
	implements ActionListener
    {
	NewDirDialog(Dialog owner_, File parent_) {
	    super(owner_);

	    setTitle("Enter the new directory name");
	    _parentFile = parent_;
	    setSize(60, 10);

	    JPanel midpan = new JPanel();
	    midpan.setBorder(new EmptyBorder(2,2,2,2));
	    midpan.add(new JLabel("Directory name:"));
	    _dirnameField = new JTextField(35);
	    _dirnameField.setActionCommand("dirname");
	    _dirnameField.addActionListener(this);
	    midpan.add(_dirnameField);
	    add(midpan, BorderLayout.CENTER);

	    _okButton = new JButton("OK");
	    _okButton.addActionListener(this);
	    _cancelButton = new JButton("Cancel");
	    _cancelButton.addActionListener(this);
	    JPanel southpan = new JPanel();
	    southpan.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 1));
	    southpan.add(_okButton);
	    southpan.add(_cancelButton);
	    add(southpan, BorderLayout.SOUTH);
	    pack();
	}

	public void actionPerformed(ActionEvent e_) {
	    String cmd = e_.getActionCommand();
	    if (cmd.equals("OK") || cmd.equals("dirname")) {
		if (_parentFile.canWrite() == false) {
		    String[] msgs = {"Permission denied"};
		    JOptionPane.showMessageDialog(
			    this, msgs, "Error", JOptionPane.ERROR_MESSAGE);
		    return;
		}

		File newdir = new File(_parentFile, _dirnameField.getText());
		boolean ok = newdir.mkdir();
		if (ok == false) {
		    String[] msgs = {"Invalid directory"};
		    JOptionPane.showMessageDialog(
			    this, msgs, "Error", JOptionPane.ERROR_MESSAGE);
		}
		else {
		    _directory = newdir;
		    hide();
		}
	    }
	    else if (cmd.equals("Cancel")) {
		_directory = null;
		hide();
	    }
	}

	File getDirectory() { return _directory; }

	private File _parentFile;
	private JButton _okButton;
	private JButton _cancelButton;
	private JTextField _dirnameField;
	private File _directory = null;

    }

    // end of inner class NewDirDialog

    private interface FileChooserListener extends EventListener
    {
	public void fileChanged(FileChooserEvent e);
    }

    private class FileChooserEvent extends java.util.EventObject
    {
        private static final long serialVersionUID = 1L;

    public FileChooserEvent(Object source_)
	{
	    super(source_);
	}
    }
}

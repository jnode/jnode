/* class Tutorial
 *
 * R Pitman, 2003
 * Last updated: 20 June, 2003.
 *
 * This class performs a general test of the functionality of the
 * CHARVA library.
 */

package charva.awt;

import charva.awt.event.ActionEvent;
import charva.awt.event.ActionListener;
import charva.awt.event.FocusEvent;
import charva.awt.event.FocusListener;
import charva.awt.event.ItemEvent;
import charva.awt.event.ItemListener;
import charva.awt.event.KeyEvent;
import charva.awt.event.KeyListener;
import charva.awt.util.CapsTextField;
import charvax.swing.BoxLayout;
import charvax.swing.ButtonGroup;
import charvax.swing.DefaultListModel;
import charvax.swing.JButton;
import charvax.swing.JCheckBox;
import charvax.swing.JComboBox;
import charvax.swing.JDialog;
import charvax.swing.JFileChooser;
import charvax.swing.JFrame;
import charvax.swing.JLabel;
import charvax.swing.JList;
import charvax.swing.JMenu;
import charvax.swing.JMenuBar;
import charvax.swing.JMenuItem;
import charvax.swing.JOptionPane;
import charvax.swing.JPanel;
import charvax.swing.JPasswordField;
import charvax.swing.JProgressBar;
import charvax.swing.JRadioButton;
import charvax.swing.JScrollPane;
import charvax.swing.JTabbedPane;
import charvax.swing.JTable;
import charvax.swing.JTextArea;
import charvax.swing.JTextField;
import charvax.swing.ListSelectionModel;
import charvax.swing.SwingUtilities;
import charvax.swing.border.EmptyBorder;
import charvax.swing.border.LineBorder;
import charvax.swing.border.TitledBorder;
import charvax.swing.event.ListDataEvent;
import charvax.swing.event.ListDataListener;
import charvax.swing.event.ListSelectionEvent;
import charvax.swing.event.ListSelectionListener;

public class Tutorial extends JFrame implements ActionListener {

    //private JTextField tf;

    public Tutorial() {
        super("Charva Demo - copyright R Pitman, 2003");
        setForeground(Color.green);
        setBackground(Color.black);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        JMenuBar menubar = new JMenuBar();
        JMenu jMenuFile = new JMenu("File");
        jMenuFile.setMnemonic('F');

        JMenuItem jMenuItemFileChooser = new JMenuItem("JFileChooser", 'F');
        jMenuItemFileChooser.addActionListener(this);
        jMenuFile.add(jMenuItemFileChooser);

        JMenuItem jMenuItemCustomFileChooser = new JMenuItem(
                "custom FileChooser", 'c');
        jMenuItemCustomFileChooser.addActionListener(this);
        jMenuFile.add(jMenuItemCustomFileChooser);

        jMenuFile.addSeparator();

        JMenuItem jMenuItemFileExit = new JMenuItem("Exit", 'x');
        jMenuItemFileExit.addActionListener(this);
        jMenuFile.add(jMenuItemFileExit);

        JMenu jMenuLayout = new JMenu("Layouts");
        jMenuLayout.setMnemonic('L');
        JMenuItem jMenuItemLayoutNull = new JMenuItem("Null Layout");
        jMenuItemLayoutNull.setMnemonic('N');
        jMenuItemLayoutNull.addActionListener(this);
        jMenuLayout.add(jMenuItemLayoutNull);

        jMenuLayout.addSeparator();

        JMenuItem jMenuItemLayoutMisc = new JMenuItem("Miscellaneous Layouts");
        jMenuItemLayoutMisc.setMnemonic('M');
        jMenuItemLayoutMisc.addActionListener(this);
        jMenuLayout.add(jMenuItemLayoutMisc);

        JMenuItem jMenuItemLayoutColor = new JMenuItem("Layouts in Color");
        jMenuItemLayoutColor.setMnemonic('C');
        jMenuItemLayoutColor.addActionListener(this);
        jMenuLayout.add(jMenuItemLayoutColor);

        JMenuItem jMenuItemLayoutGBL = new JMenuItem("GridBagLayout");
        jMenuItemLayoutGBL.setMnemonic('G');
        jMenuItemLayoutGBL.addActionListener(this);
        jMenuLayout.add(jMenuItemLayoutGBL);

        JMenu jMenuContainers = new JMenu("Containers");
        jMenuContainers.setMnemonic('C');

        JMenuItem jMenuItemContainerJTabbedPane = new JMenuItem("JTabbedPane");
        jMenuItemContainerJTabbedPane.setMnemonic('T');
        jMenuItemContainerJTabbedPane.addActionListener(this);
        jMenuContainers.add(jMenuItemContainerJTabbedPane);

        JMenu jMenuItemContainerJOptionPane = new JMenu("JOptionPane...");
        jMenuItemContainerJOptionPane.setMnemonic('O');
        jMenuContainers.add(jMenuItemContainerJOptionPane);

        JMenuItem jMenuItemShowMessageDialog = new JMenuItem(
                "showMessageDialog");
        jMenuItemShowMessageDialog.addActionListener(this);
        jMenuItemContainerJOptionPane.add(jMenuItemShowMessageDialog);

        JMenuItem jMenuItemShowConfirmDialog = new JMenuItem(
                "showConfirmDialog");
        jMenuItemShowConfirmDialog.addActionListener(this);
        jMenuItemContainerJOptionPane.add(jMenuItemShowConfirmDialog);

        JMenuItem jMenuItemShowInputDialog = new JMenuItem("showInputDialog");
        jMenuItemShowInputDialog.addActionListener(this);
        jMenuItemContainerJOptionPane.add(jMenuItemShowInputDialog);

        JMenuItem jMenuItemShowCustomInputDialog = new JMenuItem(
                "show Custom InputDialog");
        jMenuItemShowCustomInputDialog.addActionListener(this);
        jMenuItemContainerJOptionPane.add(jMenuItemShowCustomInputDialog);

        JMenu jMenuWidgets = new JMenu("Widgets");
        jMenuWidgets.setMnemonic('W');

        JMenuItem jMenuItemWidgetText = new JMenuItem("Text components");
        jMenuItemWidgetText.setMnemonic('T');
        jMenuItemWidgetText.addActionListener(this);
        jMenuWidgets.add(jMenuItemWidgetText);

        JMenuItem jMenuItemWidgetSelection = new JMenuItem(
                "Selection components");
        jMenuItemWidgetSelection.setMnemonic('S');
        jMenuItemWidgetSelection.addActionListener(this);
        jMenuWidgets.add(jMenuItemWidgetSelection);

        JMenuItem jMenuItemWidgetButtons = new JMenuItem("Buttons");
        jMenuItemWidgetButtons.setMnemonic('B');
        jMenuItemWidgetButtons.addActionListener(this);
        jMenuWidgets.add(jMenuItemWidgetButtons);

        JMenuItem jMenuItemWidgetJTable = new JMenuItem("JTable");
        jMenuItemWidgetJTable.setMnemonic('J');
        jMenuItemWidgetJTable.addActionListener(this);
        jMenuWidgets.add(jMenuItemWidgetJTable);

        JMenu jMenuEvents = new JMenu("Events");
        jMenuEvents.setMnemonic('E');

        JMenuItem jMenuItemKeyEvents = new JMenuItem("KeyEvents");
        jMenuItemKeyEvents.setMnemonic('K');
        jMenuItemKeyEvents.addActionListener(this);
        jMenuEvents.add(jMenuItemKeyEvents);

        JMenuItem jMenuItemFocusEvents = new JMenuItem("FocusEvents");
        jMenuItemFocusEvents.setMnemonic('F');
        jMenuItemFocusEvents.addActionListener(this);
        jMenuEvents.add(jMenuItemFocusEvents);

        JMenu jMenuThreads = new JMenu("Threads");
        jMenuThreads.setMnemonic('T');

        JMenuItem jMenuItemProgressBar = new JMenuItem("JProgressBar");
        jMenuItemProgressBar.setMnemonic('P');
        jMenuItemProgressBar.addActionListener(this);
        jMenuThreads.add(jMenuItemProgressBar);

        menubar.add(jMenuFile);
        menubar.add(jMenuLayout);
        menubar.add(jMenuContainers);
        menubar.add(jMenuWidgets);
        menubar.add(jMenuEvents);
        menubar.add(jMenuThreads);

        setJMenuBar(menubar);

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.add(new JLabel(
                "Use LEFT and RIGHT cursor keys to select a menu."));
        labelPanel.add(new JLabel("Use ENTER to invoke a menu or menu-item."));
        labelPanel.add(new JLabel("(You can also use the "
                + "underlined \"mnemonic key\" to invoke a menu.)"));
        labelPanel.add(new JLabel("Use BACKSPACE or ESC to dismiss a menu."));
        contentPane.add(labelPanel, BorderLayout.SOUTH);

        setLocation(0, 0);
        Toolkit tk = Toolkit.getDefaultToolkit();
        setSize(tk.getScreenColumns(), tk.getScreenRows());
        validate();
    }

    public static void main(String[] args) throws Exception {
        Tutorial testwin = new Tutorial();

        /*So we have explicit control over the Toolkit instance.*/
        Toolkit.getDefaultToolkit().register();
        testwin.show();
        Toolkit.getDefaultToolkit().waitTillFinished();
    }

    public void actionPerformed(ActionEvent ae_) {
        String actionCommand = ae_.getActionCommand();
        if (actionCommand.equals("Exit")) {
            hide();
            Toolkit.getDefaultToolkit().close();
            System.err.println( "-->"+getClass().getName()+" closed.  Press ctrl-c if necessary to return to the prompt." );
            //System.gc(); // so that HPROF reports only live objects.
            //System.exit(0);
        } else if (actionCommand.equals("JFileChooser")) {
            testFileChooser();
        } else if (actionCommand.equals("custom FileChooser")) {
            //	    JFileChooser.CANCEL_LABEL = "Cancel (F4)";
            //	    JFileChooser.CANCEL_ACCELERATOR = KeyEvent.VK_F4;
            //	    (new JFileChooser()).show();
            JOptionPane.showMessageDialog(this,
                    "This test has been (temporarily) disabled", "Information",
                    JOptionPane.PLAIN_MESSAGE);
        } else if (actionCommand.equals("Null Layout")) {
            JDialog dlg = new NullLayoutTest(this);
            dlg.show();
        } else if (actionCommand.equals("Miscellaneous Layouts")) {
            JDialog dlg = new LayoutTest(this);
            dlg.show();
        } else if (actionCommand.equals("Layouts in Color")) {
            if (!Toolkit.getDefaultToolkit().hasColors()) {
                JOptionPane.showMessageDialog(this,
                        "This terminal does not have color capability!",
                        "Error", JOptionPane.PLAIN_MESSAGE);
                return;
            }
            JDialog dlg = new ColorLayoutTest(this);
            dlg.setLocationRelativeTo(this);
            dlg.show();
        } else if (actionCommand.equals("GridBagLayout")) {
            JDialog dlg = new GridBagLayoutTest(this);
            dlg.setLocationRelativeTo(this);
            dlg.show();
        } else if (actionCommand.equals("JTabbedPane")) {
            JDialog dlg = new JTabbedPaneTest(this);
            dlg.show();
        } else if (actionCommand.equals("showMessageDialog")) {
            JOptionPane.showMessageDialog(this,
                    "This is an example of a Message Dialog "
                            + "with a single message string",
                    "This is the title", JOptionPane.PLAIN_MESSAGE);
        } else if (actionCommand.equals("showConfirmDialog")) {
            showConfirmDialog();
        } else if (actionCommand.equals("showInputDialog")) {
            showInputDialog();
        } else if (actionCommand.equals("show Custom InputDialog")) {
            showCustomInputDialog(this);
        } else if (actionCommand.equals("Text components")) {
            TextWidgetTest dlg = new TextWidgetTest(this);
            dlg.show();
        } else if (actionCommand.equals("Selection components")) {
            SelectionTest dlg = new SelectionTest(this);
            dlg.show();
        } else if (actionCommand.equals("Buttons")) {
            (new ButtonTest(this)).show();
        } else if (actionCommand.equals("JTable")) {
            JTableTest dlg = new JTableTest(this);
            dlg.setLocationRelativeTo(this);
            dlg.show();
        } else if (actionCommand.equals("KeyEvents")) {
            KeyEventTest dlg = new KeyEventTest(this);
            dlg.setLocationRelativeTo(this);
            dlg.show();
        } else if (actionCommand.equals("FocusEvents")) {
            FocusEventTest dlg = new FocusEventTest(this);
            dlg.setLocationRelativeTo(this);
            dlg.show();
        } else if (actionCommand.equals("JProgressBar")) {
            ProgressBarTest dlg = new ProgressBarTest(this);
            dlg.setLocationRelativeTo(this);
            dlg.show();
        } else {
            JOptionPane.showMessageDialog(this, "Menu item \"" + actionCommand
                    + "\" not implemented yet", "Error",
                    JOptionPane.PLAIN_MESSAGE);
        }
        // Trigger garbage-collection after every menu action.
        Toolkit.getDefaultToolkit().triggerGarbageCollection(this);
    }

    /**
     * Demonstrate the JFileChooser.
     */
    private void testFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("A File Chooser");
        chooser.setForeground(Color.white);
        chooser.setBackground(Color.blue);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        //  Uncomment this section of code to apply a FileFilter that masks out
        // all
        //  files whose names do not end with ".java".
        //	/* Construct an anonymous inner class that extends the abstract
        //	 * FileFilter class.
        //	 */
        //	charvax.swing.filechooser.FileFilter filter =
        //		new charvax.swing.filechooser.FileFilter() {
        //	    public boolean accept(File file_) {
        //		String pathname = file_.getAbsolutePath();
        //		return (pathname.endsWith(".java"));
        //	    }
        //	};
        //	chooser.setFileFilter(filter);

        if (chooser.showDialog(this, "Open File") == JFileChooser.APPROVE_OPTION) {

            String msgs[] = { "The selected file was:",
                    chooser.getSelectedFile().getAbsolutePath()};
            JOptionPane.showMessageDialog(this, msgs,
                    "Results of JFileChooser", JOptionPane.PLAIN_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "The CANCEL button was selected",
                    "Results of JFileChooser", JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * Demonstrate the JOptionPane.showConfirmDialog() method.
     */
    private void showConfirmDialog() {
        String[] messages = { "This is an example of a Confirm Dialog",
                "that displays an array of Strings"};

        int option = JOptionPane.showConfirmDialog(this, messages,
                "Select an Option", JOptionPane.YES_NO_CANCEL_OPTION);
        String result = "";
        if (option == JOptionPane.YES_OPTION)
            result = "User selected YES option";
        else if (option == JOptionPane.NO_OPTION)
            result = "User selected NO option";
        else
            result = "User selected Cancel option";
        JOptionPane.showMessageDialog(this, result,
                "Result of showConfirmDialog", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Demonstrate the JOptionPane.showInputDialog() method.
     */
    private void showInputDialog() {
        String[] messages = { "This is an example of an Input Dialog",
                "that displays an array of Strings"};
        String result = JOptionPane.showInputDialog(this, messages,
                "Input a value", JOptionPane.QUESTION_MESSAGE);
        String msg = null;
        if (result == null)
            msg = "User selected Cancel option";
        else
            msg = "User entered \"" + result + "\"";
        JOptionPane.showMessageDialog(this, msg, "Result of showInputDialog",
                JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Demonstrate how to customize the JOptionPane.
     */
    private void showCustomInputDialog(Component parent_) {

        String[] results = { "", ""};
        String selected_option = null;

        String[] message = { "This shows how to create and use",
                "a JOptionPane directly, without using",
                "the convenience methods"};
        JOptionPane pane = new JOptionPane(message);

        // Make the dialog display a JTextField for user input.
        pane.setWantsInput(true);

        // Set the initial input value displayed to the user.
        pane.setInitialSelectionValue("default input value");

        // Provide customized button labels.
        String[] options = { "Option 1", "Option 2", "Option 3", "Option 4"};
        pane.setOptions(options);

        // Make "Option 2" the default button.
        pane.setInitialValue("Option 2");

        JDialog dialog = pane.createDialog(parent_, "Custom JOptionPane");
        dialog.show();
        Object selectedValue = pane.getValue();
        System.err.println("Selected value is " + selectedValue);
        results[ 0] = "The input value is \"" + (String) pane.getInputValue()
                + "\"";

        // If there is NOT an array of option buttons:
        // (In this case, there is).
        if (pane.getOptions() == null) {
            int option = ((Integer) selectedValue).intValue();
            switch (option) {
            case JOptionPane.YES_OPTION:
                selected_option = "YES";
                break;
            case JOptionPane.OK_OPTION:
                selected_option = "OK";
                break;
            case JOptionPane.NO_OPTION:
                selected_option = "NO";
                break;
            case JOptionPane.CANCEL_OPTION:
                selected_option = "CANCEL";
                break;
            }
        } else {
            // If there IS an array of option buttons:
            for (int i = 0; i < options.length; i++) {
                if (options[ i].equals(selectedValue)) {
                    selected_option = options[ i];
                    break;
                }
            } // end for
        }
        results[ 1] = "The selected option is \"" + selected_option + "\"";

        /*
         * Change the (static) labels and accelerators in the JOptionPane. Note
         * that the buttons stay customized for future invocations of the
         * JOptionPane methods, until they are customized again.
         * JOptionPane.OK_ACCELERATOR = KeyEvent.VK_F5;
         * JOptionPane.YES_ACCELERATOR = KeyEvent.VK_F6;
         * JOptionPane.NO_ACCELERATOR = KeyEvent.VK_F7;
         * JOptionPane.CANCEL_ACCELERATOR = KeyEvent.VK_F8;
         */

        JOptionPane.showMessageDialog(this, results,
                "Result of Customized JOptionPane", JOptionPane.PLAIN_MESSAGE);
    }

}

/**
 * This class demonstrates how to lay out components manually, by setting the
 * LayoutManager to "null". In Charva, you can set a component's position
 * within its container with "setLocation()"; but in Swing, you have to use
 * "setBounds()". Also, of course, in Charva the units are rows and columns
 * whereas in Swing they are pixels.
 */

class NullLayoutTest extends JDialog implements ActionListener {

    public NullLayoutTest(Frame owner_) {
        super(owner_, "Null Layout Test");
        setLocation(3, 3);
        setSize(60, 20);
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        JLabel label0 = new JLabel(
                "Demonstrates how to lay components out manually");
        contentPane.add(label0);
        label0.setLocation(2, 2);

        JPanel panel1 = new JPanel();
        panel1.setLayout(null);
        contentPane.add(panel1);
        panel1.setLocation(2, 3);
        panel1.setSize(40, 6);
        panel1.setBorder(new TitledBorder("Panel1"));

        JLabel label1 = new JLabel("Label 1:");
        panel1.add(label1);
        label1.setLocation(1, 2);

        JTextField textfield1 = new JTextField("Text Field 1");
        panel1.add(textfield1);
        textfield1.setLocation(11, 2);

        JPanel panel2 = new JPanel();
        panel2.setLayout(null);
        contentPane.add(panel2);
        panel2.setLocation(2, 10);
        panel2.setSize(40, 6);
        panel2.setBorder(new TitledBorder("Panel2"));

        JLabel label2 = new JLabel("Label 2:");
        panel2.add(label2);
        label2.setLocation(1, 2);

        JTextField textfield2 = new JTextField("Text Field 2");
        panel2.add(textfield2);
        textfield2.setLocation(11, 2);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        contentPane.add(okButton);
        okButton.setLocation(25, 17);
        okButton.setMnemonic(0x18); // CTRL-X
    }

    public void actionPerformed(ActionEvent ae_) {
        if (ae_.getActionCommand().equals("OK")) {
            hide();
        }
    }
}

/**
 * This class demonstrates how to use the BorderLayout (which is the default
 * layout for JFrame and JDialog), the BoxLayout and the FlowLayout (which is
 * the default layout for JPanel).
 */

class LayoutTest extends JDialog implements ActionListener {

    public LayoutTest(Frame owner_) {
        super(owner_, "Miscellaneous Layout Test");
        setLocation(3, 3);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout()); // default layout for
                                                   // JDialog

        JPanel toppan = new JPanel();
        toppan.setBorder(new TitledBorder("North Panel"));
        toppan.add(new JLabel("north panel uses FlowLayout"));
        contentPane.add(toppan, BorderLayout.NORTH);

        JPanel westpan = new JPanel();
        westpan.setBorder(new TitledBorder("West Panel"));
        westpan.setLayout(new BoxLayout(westpan, BoxLayout.Y_AXIS));
        westpan.add(new JLabel("west panel uses BoxLayout"));
        westpan.add(new JTextField("JTextField #1."));
        westpan.add(new JTextField("JTextField #2."));
        westpan.add(new JTextField("JTextField #3."));
        westpan.add(new JTextField("JTextField #4."));
        westpan.add(new JTextField("JTextField #5."));
        contentPane.add(westpan, BorderLayout.WEST);

        JPanel eastpan = new JPanel();
        eastpan.setBorder(new TitledBorder("East Panel"));
        eastpan.add(new JTextField("A JTextField"));
        contentPane.add(eastpan, BorderLayout.EAST);

        JPanel centerpan = new JPanel();
        centerpan.setLayout(new BorderLayout());
        centerpan.setBorder(new TitledBorder("Center Panel"));
        centerpan.add(new JLabel("A label in the center"), BorderLayout.CENTER);
        contentPane.add(centerpan, BorderLayout.CENTER);

        JPanel southpan = new JPanel();
        southpan.setBorder(new TitledBorder("South Panel"));
        southpan.add(new JLabel("A label in the south: "));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        southpan.add(okButton);
        contentPane.add(southpan, BorderLayout.SOUTH);
        okButton.setMnemonic(KeyEvent.VK_F10);
        pack();
    }

    public void actionPerformed(ActionEvent ae_) {
        if (ae_.getActionCommand().equals("OK")) {
            hide();
        }
    }
}

/**
 * This class is based on the MiscellaneousLayoutTest but it demonstrates how
 * to set the foreground and background colors of dialogs and components.
 */

class ColorLayoutTest extends JDialog implements ActionListener {

    public ColorLayoutTest(Frame owner_) {
        super(owner_,
                "Layout Test in Color (yellow foreground, green background)");
        setLocation(3, 3);
        setForeground(Color.yellow);
        setBackground(Color.green);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout()); // default layout for
                                                   // JDialog

        JPanel toppan = new JPanel();
        toppan.setBorder(new TitledBorder(
                "North Panel (inherits green background)"));
        toppan.setForeground(Color.blue);
        contentPane.add(toppan, BorderLayout.NORTH);

        JRadioButton button1 = new JRadioButton("A JRadioButton...  ");
        JRadioButton button2 = new JRadioButton("And another JRadioButton");
        ButtonGroup buttons = new ButtonGroup();
        buttons.add(button1);
        buttons.add(button2);
        button1.setSelected(true);
        toppan.add(button1);
        toppan.add(button2);

        JPanel westpan = new JPanel();
        westpan.setBorder(new TitledBorder("West Panel"));
        westpan.setLayout(new BoxLayout(westpan, BoxLayout.Y_AXIS));
        JLabel label1 = new JLabel("Magenta label in west panel");
        westpan.add(label1);
        label1.setForeground(Color.magenta);
        JTextField textfield1 = new JTextField(
                "Cyan JTextField, red background");
        textfield1.setForeground(Color.cyan);
        textfield1.setBackground(Color.red);
        westpan.add(textfield1);

        JTextField whiteTextField = new JTextField("White JTextField");
        whiteTextField.setForeground(Color.white);
        westpan.add(whiteTextField);
        JTextField blueTextField = new JTextField("Blue JTextField");
        blueTextField.setForeground(Color.blue);
        westpan.add(blueTextField);
        JTextField yellowTextField = new JTextField("Yellow JTextField");
        yellowTextField.setForeground(Color.yellow);
        westpan.add(yellowTextField);
        JTextField blackTextField = new JTextField("Black JTextField");
        blackTextField.setForeground(Color.black);
        westpan.add(blackTextField);
        contentPane.add(westpan, BorderLayout.WEST);

        JPanel eastpan = new JPanel();
        eastpan.setForeground(Color.black);
        eastpan.setBorder(new TitledBorder("East Panel"));
        eastpan.add(new JTextField("A JTextField"));
        contentPane.add(eastpan, BorderLayout.EAST);

        JPanel centerpan = new JPanel();
        centerpan.setForeground(Color.white);
        centerpan.setBackground(Color.black);
        centerpan.setLayout(new BorderLayout());
        LineBorder centerpan_lineborder = new LineBorder(Color.green);
        TitledBorder centerpan_titledborder = new TitledBorder(
                centerpan_lineborder, "Green border, yellow title", 0, 0, null,
                Color.yellow);
        centerpan.setBorder(centerpan_titledborder);
        centerpan.add(new JLabel("A white label in the center"),
                BorderLayout.CENTER);
        contentPane.add(centerpan, BorderLayout.CENTER);

        JPanel southpan = new JPanel();
        southpan.setBorder(new TitledBorder("South Panel (white foreground)"));
        southpan.setBackground(Color.blue);
        southpan.setForeground(Color.white);
        JLabel labelsouth = new JLabel("A green label in the south panel ");
        labelsouth.setForeground(Color.green);
        southpan.add(labelsouth);
        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        southpan.add(okButton);
        contentPane.add(southpan, BorderLayout.SOUTH);
        okButton.setMnemonic(KeyEvent.VK_F10);
        pack();
    }

    public void actionPerformed(ActionEvent ae_) {
        if (ae_.getActionCommand().equals("OK")) {
            hide();
        }
    }
}

/**
 * This class demonstrates how to use the GridBagLayout.
 */

class GridBagLayoutTest extends JDialog implements ActionListener,
        ListSelectionListener {

    private JTextField lastnameField = new JTextField(25);

    private JTextField initialsField = new JTextField(5);

    private JTextField address1Field = new JTextField(20);

    private JTextField address2Field = new JTextField(20);

    private JTextField cityField = new JTextField(20);

    private JTextField postcodeField = new JTextField(8);

    private JTextField stateField = new JTextField(15);

    public GridBagLayoutTest(Frame owner_) {
        super(owner_, "GridBagLayout Test");
        Container contentPane = getContentPane();

        JPanel centerpan = new JPanel();
        centerpan.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        centerpan.add(new JLabel("Last name: "), gbc);

        gbc.gridy = 1;
        centerpan.add(new JLabel("Address line 1: "), gbc);

        gbc.gridy = 2;
        centerpan.add(new JLabel("Address line 2: "), gbc);

        gbc.gridy = 3;
        centerpan.add(new JLabel("City: "), gbc);

        gbc.gridy = 4;
        centerpan.add(new JLabel("Postal code: "), gbc);

        gbc.gridy = 5;
        centerpan.add(new JLabel("State: "), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        centerpan.add(lastnameField, gbc);

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        centerpan.add(new JLabel(" Initials: "), gbc);

        gbc.gridx = 3;
        gbc.anchor = GridBagConstraints.WEST;
        centerpan.add(initialsField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        centerpan.add(address1Field, gbc);

        gbc.gridy = 2;
        centerpan.add(address2Field, gbc);

        gbc.gridy = 3;
        centerpan.add(cityField, gbc);

        gbc.gridy = 4;
        centerpan.add(postcodeField, gbc);

        gbc.gridy = 5;
        centerpan.add(stateField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 6;
        gbc.insets = new Insets(1, 1, 1, 1);
        String[] countries = { "Portugal", "Spain", "France", "Italy",
                "Germany", "Poland", "Austria", "Belgium", "Denmark", "Norway",
                "Sweden"};
        _countryList = new JList(countries);
        _countryList.setVisibleRowCount(6);
        _countryList.setColumns(12);
        _countryList.addListSelectionListener(this);
        JScrollPane scrollpane = new JScrollPane(_countryList);
        TitledBorder viewportBorder = new TitledBorder("Countries");
        scrollpane.setViewportBorder(viewportBorder);
        centerpan.add(scrollpane, gbc);

        JPanel southpan = new JPanel();
        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        southpan.add(okButton);

        contentPane.add(centerpan, BorderLayout.CENTER);
        contentPane.add(southpan, BorderLayout.SOUTH);
        pack();
    }

    public void actionPerformed(ActionEvent ae_) {
        if (ae_.getActionCommand().equals("OK")) {
            hide();
        }
    }

    /**
     * This method implements the ListSelectionListener interface, and is
     * called when an item is selected or deselected in the JList.
     */
    public void valueChanged(ListSelectionEvent e_) {
        _countryList.repaint();
    }

    private JList _countryList;
}

/**
 * This class demonstrates how to use the JTabbedPane.
 */

class JTabbedPaneTest extends JDialog implements ActionListener, KeyListener {

    JTabbedPaneTest(Frame owner_) {
        super(owner_, "JTabbedPane Test");
        _insets = new Insets(2, 3, 2, 3);
        Container contentPane = getContentPane();

        JPanel toppan = new JPanel();
        toppan.setBorder(new EmptyBorder(1, 1, 1, 1));
        toppan.add(new JLabel(
                "Press the F5, F6 and F7 keys to switch between panes"));

        JPanel centerpan = new JPanel();
        centerpan.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 1));

        _tabpane = new JTabbedPane();
        _tabpane.addTab("General", null, new GeneralPane(), "F5");
        _tabpane.addTab("Device Manager", null, new DevicePane(), "F6");
        _tabpane.addTab("Performance", null, new PerformancePane(), "F7");
        addKeyListener(this);
        centerpan.add(_tabpane);

        _okButton = new JButton("OK");
        _okButton.addActionListener(this);

        contentPane.add(toppan, BorderLayout.NORTH);
        contentPane.add(centerpan, BorderLayout.CENTER);
        contentPane.add(_okButton, BorderLayout.SOUTH);

        pack();
    }

    public void actionPerformed(ActionEvent e_) {
        hide();
    }

    /**
     * This method implements the KeyListener interface and handles the
     * interactive selection of tabs.
     */
    public void keyPressed(KeyEvent evt_) {
        int key = evt_.getKeyCode();
        if (key == KeyEvent.VK_F5)
            _tabpane.setSelectedIndex(0);
        else if (key == KeyEvent.VK_F6)
            _tabpane.setSelectedIndex(1);
        else if (key == KeyEvent.VK_F7) _tabpane.setSelectedIndex(2);
    }

    public void keyTyped(KeyEvent evt_) {
    }

    public void keyReleased(KeyEvent evt_) {
    }

    class GeneralPane extends JPanel {

        public GeneralPane() {
            setLayout(new BorderLayout());

            JPanel northpan = new JPanel();
            northpan.setLayout(new BoxLayout(northpan, BoxLayout.Y_AXIS));
            northpan.setBorder(new TitledBorder("System"));
            northpan.add(new JLabel("Red Hat Linux 9.0"));

            JPanel centerpan = new JPanel();
            centerpan.setLayout(new BoxLayout(centerpan, BoxLayout.Y_AXIS));
            centerpan.setBorder(new TitledBorder("Registered to"));
            centerpan.add(new JLabel("Rob Pitman"));
            centerpan.add(new JLabel("8 Pickwood Road"));
            centerpan.add(new JLabel("Centurion, South Africa"));

            JPanel southpan = new JPanel();
            southpan.setLayout(new BoxLayout(southpan, BoxLayout.Y_AXIS));
            southpan.setBorder(new TitledBorder("Computer"));
            southpan.add(new JLabel("GenuineIntel"));
            southpan.add(new JLabel("x86 Family 15 Model 1 Stepping 2"));
            southpan.add(new JLabel("256 MB RAM"));

            add(northpan, BorderLayout.NORTH);
            add(centerpan, BorderLayout.CENTER);
            add(southpan, BorderLayout.SOUTH);
            pack();
        }
    }

    class DevicePane extends JPanel {

        public DevicePane() {
            setLayout(new BorderLayout());

            JPanel northpan = new JPanel();
            JRadioButton button1 = new JRadioButton("View devices by type");
            JRadioButton button2 = new JRadioButton(
                    "View devices by connection");
            ButtonGroup buttons = new ButtonGroup();
            buttons.add(button1);
            buttons.add(button2);
            button1.setSelected(true);
            northpan.add(button1);
            northpan.add(button2);

            JPanel centerpan = new JPanel();
            String[] devices = { "Computer", "CD-ROM", "Disk drives",
                    "Display adapters", "Floppy disk controllers",
                    "Imaging devices", "Keyboard", "Modem", "Monitors", "Mouse"};
            JList deviceList = new JList(devices);
            deviceList.setBorder(new TitledBorder("Devices"));
            centerpan.add(deviceList);

            JPanel southpan = new JPanel();
            southpan.add(new JButton("Properties"));
            southpan.add(new JButton("Refresh"));
            southpan.add(new JButton("Remove"));
            southpan.add(new JButton("Print..."));

            add(northpan, BorderLayout.NORTH);
            add(centerpan, BorderLayout.CENTER);
            add(southpan, BorderLayout.SOUTH);
            pack();
        }
    }

    class PerformancePane extends JPanel {

        public PerformancePane() {
            setLayout(new BorderLayout());

            JPanel centerpan = new JPanel();
            centerpan.setBorder(new TitledBorder("Performance Status"));
            centerpan.setLayout(new BoxLayout(centerpan, BoxLayout.Y_AXIS));
            centerpan.add(new JLabel("Memory:           256.0 MB of RAM"));
            centerpan.add(new JLabel("System Resources: 50% free"));
            centerpan.add(new JLabel("File System:      32 bit"));
            centerpan.add(new JLabel("Virtual Memory:   32 bit"));
            centerpan.add(new JLabel("Disk Compression: Not Installed"));
            centerpan.add(new JLabel(
                    "Your system is configured for optimum performance"));

            JPanel southpan = new JPanel();
            southpan.setBorder(new TitledBorder("Advanced Settings"));
            southpan.add(new JButton("File System..."));
            southpan.add(new JButton("Graphics..."));
            southpan.add(new JButton("Virtual Memory..."));

            add(centerpan, BorderLayout.CENTER);
            add(southpan, BorderLayout.SOUTH);
            pack();
        }
    }

    private JButton _okButton;

    private JTabbedPane _tabpane;
}

/**
 * This class demonstrates how to use the JTextField, the JPasswordField and
 * the JTextArea.
 */

class TextWidgetTest extends JDialog implements ActionListener {

    TextWidgetTest(Frame owner_) {
        super(owner_, "Text Widget Test");
        Container contentPane = getContentPane();

        JPanel centerpan = new JPanel();
        centerpan.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        centerpan.add(new TextFieldPanel(), gbc);

        gbc.gridx = 1;
        centerpan.add(new PasswordFieldPanel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        centerpan.add(new TextAreaPanel(), gbc);

        JPanel southpan = new JPanel();
        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        southpan.add(okButton);

        contentPane.add(centerpan, BorderLayout.CENTER);
        contentPane.add(southpan, BorderLayout.SOUTH);
        pack();
    }

    public void actionPerformed(ActionEvent e_) {
        if (e_.getActionCommand().equals("OK")) hide();
    }

    /**
     * An inner class to display a JTextField.
     */
    class TextFieldPanel extends JPanel implements ItemListener {

        private JCheckBox _enabledCb;

        private JCheckBox _visibleCb;

        private JTextField _textfield;

        TextFieldPanel() {
            setLayout(new BorderLayout());
            setBorder(new TitledBorder("JTextField"));

            JPanel northpan = new JPanel();
            _enabledCb = new JCheckBox("Enabled");
            _enabledCb.setSelected(true);
            _enabledCb.addItemListener(this);
            northpan.add(_enabledCb);
            _visibleCb = new JCheckBox("Visible");
            _visibleCb.setSelected(true);
            _visibleCb.addItemListener(this);
            northpan.add(_visibleCb);

            JPanel southpan = new JPanel();
            _textfield = new JTextField("This is some text.....");
            _textfield.setBorder(new EmptyBorder(1, 1, 1, 1));
            southpan.add(_textfield);

            add(northpan, BorderLayout.NORTH);
            add(southpan, BorderLayout.SOUTH);
        }

        public void itemStateChanged(ItemEvent e_) {
            if (e_.getSource() == _enabledCb) {
                _textfield.setEnabled(_enabledCb.isSelected());
            } else {
                _textfield.setVisible(_visibleCb.isSelected());
            }
        }
    }

    /**
     * An inner class to display a JPasswordField.
     */
    class PasswordFieldPanel extends JPanel implements ItemListener {

        private JCheckBox _enabledCb;

        private JCheckBox _visibleCb;

        private JPasswordField _textfield;

        PasswordFieldPanel() {
            setLayout(new BorderLayout());
            setBorder(new TitledBorder("JPasswordField"));

            JPanel northpan = new JPanel();
            _enabledCb = new JCheckBox("Enabled");
            _enabledCb.setSelected(true);
            _enabledCb.addItemListener(this);
            northpan.add(_enabledCb);
            _visibleCb = new JCheckBox("Visible");
            _visibleCb.setSelected(true);
            _visibleCb.addItemListener(this);
            northpan.add(_visibleCb);

            JPanel southpan = new JPanel();
            _textfield = new JPasswordField("This is some text.....");
            _textfield.setBorder(new EmptyBorder(1, 1, 1, 1));
            southpan.add(_textfield);

            add(northpan, BorderLayout.NORTH);
            add(southpan, BorderLayout.SOUTH);
        }

        public void itemStateChanged(ItemEvent e_) {
            if (e_.getSource() == _enabledCb) {
                _textfield.setEnabled(_enabledCb.isSelected());
            } else {
                _textfield.setVisible(_visibleCb.isSelected());
            }
        }
    }

    /**
     * An inner class to display a JTextArea.
     */
    class TextAreaPanel extends JPanel implements ItemListener {

        private JCheckBox _linewrap;

        private JCheckBox _linewrapstyle;

        private JTextArea _textarea;

        TextAreaPanel() {
            setLayout(new BorderLayout());
            setBorder(new TitledBorder("JTextArea in a JScrollPane"));

            JPanel northpan = new JPanel();
            _linewrap = new JCheckBox("Line Wrap ");
            _linewrap.setSelected(false);
            _linewrap.addItemListener(this);
            northpan.add(_linewrap);
            _linewrapstyle = new JCheckBox("Line Wrap Style = Word");
            _linewrapstyle.setSelected(false);
            _linewrapstyle.setEnabled(false);
            _linewrapstyle.addItemListener(this);
            northpan.add(_linewrapstyle);

            _textarea = new JTextArea("Contents of the JTextArea...", 8, 50);
            JScrollPane scrollpane = new JScrollPane(_textarea);
            scrollpane.setViewportBorder(new TitledBorder("Text Area"));

            add(northpan, BorderLayout.NORTH);
            add(scrollpane, BorderLayout.SOUTH);
        }

        public void itemStateChanged(ItemEvent e_) {
            Component source = (Component) e_.getSource();
            if (source == _linewrap) {
                _textarea.setLineWrap(_linewrap.isSelected());
                if (_textarea.getLineWrap() == false)
                        _linewrapstyle.setSelected(false);
                _linewrapstyle.setEnabled(_textarea.getLineWrap());
            } else {
                _textarea.setWrapStyleWord(_linewrapstyle.isSelected());
            }
        }
    }
}

/**
 * This class demonstrates how to use the JComboBox and the JList components.
 */

class SelectionTest extends JDialog implements ActionListener {

    SelectionTest(Frame owner_) {
        super(owner_, "JComboBox and JList");
        Container contentPane = getContentPane();

        JPanel northpan = new ComboBoxPanel();

        JPanel centerpan = new JListPanel();

        JPanel southpan = new JPanel();
        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        southpan.add(okButton);

        contentPane.add(northpan, BorderLayout.NORTH);
        contentPane.add(centerpan, BorderLayout.CENTER);
        contentPane.add(southpan, BorderLayout.SOUTH);
        pack();
    }

    public void actionPerformed(ActionEvent e_) {
        if (e_.getActionCommand().equals("OK")) {
            hide();
        }
    }

    /**
     * An inner class that displays a JComboBox.
     */
    class ComboBoxPanel extends JPanel implements ItemListener {

        private JComboBox _comboBox;

        private JTextField _comboBoxSelection;

        ComboBoxPanel() {
            setLayout(new BorderLayout());
            setBorder(new TitledBorder("JComboBox"));

            add(new JLabel("Press ENTER to pop up the JComboBox"),
                    BorderLayout.NORTH);

            String[] colors = { "Red", "Blue", "Green", "Magenta", "Mauve",
                    "Orange", "Black", "White", "Brown"};
            _comboBox = new JComboBox(colors);
            _comboBox.setMaximumRowCount(5);
            _comboBox.addItemListener(this);
            add(_comboBox, BorderLayout.CENTER);

            JPanel southpan = new JPanel();
            southpan.add(new JLabel(" Selected item is: "));
            _comboBoxSelection = new JTextField(15);
            _comboBoxSelection.setEnabled(false);
            southpan.add(_comboBoxSelection);
            add(southpan, BorderLayout.SOUTH);
        }

        public void itemStateChanged(ItemEvent e_) {
            _comboBoxSelection.setText((String) _comboBox.getSelectedItem());
        }
    }

    /**
     * An inner class that displays a JList.
     */
    class JListPanel extends JPanel implements ListSelectionListener,
            ListDataListener, ItemListener, ActionListener {

        private JCheckBox _selectionMode;

        private JList _vehicleList;

        private JTextField _listSelection;

        private JButton _deleteButton;

        JListPanel() {
            setBorder(new TitledBorder("JList"));
            setLayout(new BorderLayout());

            add(new JLabel("Use UP, DOWN, PGUP, PGDN, HOME & END to navigate"),
                    BorderLayout.NORTH);

            String[] vehicles = { "Volkswagen", "Rolls-Royce", "Toyota",
                    "Chrysler", "Mercedes Benz", "Bentley", "Bugatti",
                    "Maserati", "Porsche"};
            DefaultListModel model = new DefaultListModel();
            int columns = 0;
            for (int i = 0; i < vehicles.length; i++) {
                model.addElement(vehicles[ i]);
                if (vehicles[ i].length() > columns)
                        columns = vehicles[ i].length();
            }
            model.addListDataListener(this);

            _vehicleList = new JList(model);
            _vehicleList.setVisibleRowCount(5);
            _vehicleList.setColumns(columns);
            _vehicleList.addListSelectionListener(this);
            JScrollPane scrollpane = new JScrollPane(_vehicleList);
            scrollpane.setViewportBorder(new TitledBorder("Vehicles"));
            add(scrollpane, BorderLayout.WEST);

            _selectionMode = new JCheckBox("Selection Mode = Multiple");
            _selectionMode.addItemListener(this);

            _deleteButton = new JButton("Delete selected item(s)");
            _deleteButton.setActionCommand("Delete");
            _deleteButton.addActionListener(this);

            JPanel eastpan = new JPanel();
            eastpan.setLayout(new BoxLayout(eastpan, BoxLayout.Y_AXIS));
            eastpan.add(new JLabel(""));
            eastpan.add(_selectionMode);
            eastpan.add(new JLabel(""));
            eastpan.add(_deleteButton);
            add(eastpan, BorderLayout.EAST);

            JPanel southpan = new JPanel();
            southpan.add(new JLabel("Selected item(s):"));

            _listSelection = new JTextField(30);
            _listSelection.setEnabled(false);
            southpan.add(_listSelection);
            add(southpan, BorderLayout.SOUTH);
            pack();
        }

        /**
         * This method implements the ListSelectionListener interface, and is
         * called when an item is selected or deselected in the JList.
         */
        public void valueChanged(ListSelectionEvent e_) {
            Object[] items = _vehicleList.getSelectedValues();
            String s = "";
            for (int i = 0; i < items.length; i++) {
                if (i != 0) s += ",";
                s += (String) items[ i];
            }
            _listSelection.setText(s);
        }

        public void actionPerformed(ActionEvent e_) {
            String cmd = e_.getActionCommand();
            if (cmd.equals("Delete")) {
                int[] indices = _vehicleList.getSelectedIndices();
                if (indices.length == 0) return; // there is no selected item

                DefaultListModel model = (DefaultListModel) _vehicleList
                        .getModel();

                // We must remove the last elements first, otherwise
                // (if we remove an element with a low index), the
                // higher indices will be invalid.
                for (int i = indices.length - 1; i >= 0; i--) {
                    model.removeElementAt(indices[ i]);
                }

                // Having deleted some elements from the list, we must
                // ensure that:
                // (a) the first index inside the visible area is >= 0
                // (b) the "current row" is inside the visible area.
                // What constitutes the "current row" after a deletion is
                // debatable; we will assume that the last index to be
                // deleted is a close approximation.
                _vehicleList.ensureIndexIsVisible(indices[ 0]);
            }
        }

        /**
         * This method implements the ListDataListener interface, and is called
         * when an item is added to or removed from the list, or the value of
         * an item in the list changes.
         */
        public void contentsChanged(ListDataEvent e_) {
            _vehicleList
                    .removeSelectionInterval(e_.getIndex0(), e_.getIndex1());
            _vehicleList.repaint();
        }

        /**
         * These methods are defined for compatibilty with Swing, but are not
         * used in CHARVA.
         */
        public void intervalAdded(ListDataEvent e_) {
        }

        public void intervalRemoved(ListDataEvent e_) {
        }

        /**
         * This method implements the ItemListener interface, and is called
         * when the SelectionMode checkbox is changed.
         */
        public void itemStateChanged(ItemEvent e_) {
            if (e_.getSource() == _selectionMode) {
                if (_selectionMode.isSelected()) {
                    _vehicleList
                            .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                } else {
                    _vehicleList
                            .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                }
            }
        }
    }
}

/**
 * This class demonstrates how to use the various types of Buttons.
 */

class ButtonTest extends JDialog implements ActionListener, KeyListener,
        ItemListener {

    private ButtonGroup _buttons = new ButtonGroup();

    private JRadioButton _strawberry = new JRadioButton("Strawberry");

    private JRadioButton _chocolate = new JRadioButton("Chocolate");

    private JRadioButton _vanilla = new JRadioButton("Vanilla");

    private JRadioButton _pistachio = new JRadioButton("Pistachio");

    private JRadioButton _lime = new JRadioButton("Lime");

    private JTextField _selectedFlavor = new JTextField(15);

    private JCheckBox _nutTopping = new JCheckBox("Nuts ");

    private JCheckBox _syrupTopping = new JCheckBox("Syrup ");

    private JCheckBox _candyTopping = new JCheckBox("Candy ");

    private JCheckBox _waferTopping = new JCheckBox("Wafer ");

    ButtonTest(Frame owner_) {
        super(owner_, "Button Test");
        Container contentPane = getContentPane();

        contentPane.add(makeNorthPanel(), BorderLayout.NORTH);

        contentPane.add(makeCenterPanel(), BorderLayout.CENTER);

        JPanel southpan = new JPanel();
        JButton okButton = new JButton("OK (F9)");
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);
        southpan.add(okButton);

        contentPane.add(southpan, BorderLayout.SOUTH);

        /*
         * Add a KeyListener for this entire window; any key pressed on any
         * component in this window will cause keyPressed (or keyTyped) to be
         * called.
         */
        addKeyListener(this);
        pack();
    }

    /**
     * Implements the ActionListener interface.
     */
    public void actionPerformed(ActionEvent e_) {
        String cmd = e_.getActionCommand();
        if (cmd.equals("OK")) {
            hide();
        }
    }

    /**
     * Implements ItemListener interface
     */
    public void itemStateChanged(ItemEvent e_) {

        int statechange = e_.getStateChange();
        Component source = (Component) e_.getSource();
        if (statechange == ItemEvent.SELECTED) {
            JRadioButton button = (JRadioButton) source;
            _selectedFlavor.setText(button.getText());
        }
    }

    public void keyPressed(KeyEvent e_) {
        int key = e_.getKeyCode();
        Object src = e_.getSource();

        if (key == KeyEvent.VK_F9) {
            /*
             * Consume the event so it doesn't get processed further by the
             * component that generated it.
             */
            e_.consume();
            hide();
            return;
        }

        if (key == KeyEvent.VK_UP) {
            if (src == _chocolate)
                _strawberry.requestFocus();
            else if (src == _vanilla)
                _chocolate.requestFocus();
            else if (src == _pistachio)
                _vanilla.requestFocus();
            else if (src == _lime) _pistachio.requestFocus();
            e_.consume();
            repaint();
        } else if (key == KeyEvent.VK_DOWN) {
            if (src == _strawberry)
                _chocolate.requestFocus();
            else if (src == _chocolate)
                _vanilla.requestFocus();
            else if (src == _vanilla)
                _pistachio.requestFocus();
            else if (src == _pistachio)
                _lime.requestFocus();
            else if (src == _lime) _nutTopping.requestFocus();
            e_.consume();
            repaint();
        }
    }

    public void keyTyped(KeyEvent e_) {
    }

    public void keyReleased(KeyEvent e_) {
    }

    private JPanel makeNorthPanel() {
        JPanel northpan = new JPanel();
        northpan.setBorder(new TitledBorder("Select a flavor"));
        northpan.setLayout(new BoxLayout(northpan, BoxLayout.Y_AXIS));

        northpan.add(_strawberry);
        northpan.add(_chocolate);
        northpan.add(_vanilla);
        northpan.add(_pistachio);
        northpan.add(_lime);

        _strawberry.addItemListener(this);
        _strawberry.setActionCommand("Strawberry");
        _chocolate.addItemListener(this);
        _chocolate.setActionCommand("Chocolate");
        _vanilla.addItemListener(this);
        _vanilla.setActionCommand("Vanilla");
        _pistachio.addItemListener(this);
        _pistachio.setActionCommand("Pistachio");
        _lime.addItemListener(this);
        _lime.setActionCommand("Lime");

        JPanel panel = new JPanel();
        panel.add(new JLabel("Selected flavor: "));
        panel.add(_selectedFlavor);
        _selectedFlavor.setEnabled(false);
        panel.setBorder(new EmptyBorder(1, 1, 1, 1));
        northpan.add(panel);

        _buttons.add(_strawberry);
        _strawberry.setSelected(true); // select one button in the group
        _buttons.add(_chocolate);
        _buttons.add(_vanilla);
        _buttons.add(_pistachio);
        _buttons.add(_lime);

        return northpan;
    }

    private JPanel makeCenterPanel() {
        JPanel centerpan = new JPanel();
        centerpan.setBorder(new TitledBorder("Select one or more toppings"));
        centerpan.add(_nutTopping);
        centerpan.add(_syrupTopping);
        centerpan.add(_candyTopping);
        centerpan.add(_waferTopping);

        return centerpan;
    }
}

/**
 * This class demonstrates how to use the JTable component in a JScrollPane.
 */

class JTableTest extends JDialog implements ActionListener, ItemListener {

    private JTable _table;

    private JTextField _selectedColumns = new JTextField(10);

    private JTextField _selectedRows = new JTextField(10);

    private JCheckBox _checkBoxAllowRowSelection = new JCheckBox(
            "Allow row selection");

    private JCheckBox _checkBoxAllowColumnSelection = new JCheckBox(
            "Allow column selection");

    private JCheckBox _checkBoxAllowMultipleSelection = new JCheckBox(
            "Allow multiple selection");

    private JButton _okButton;

    //private JScrollBar _scrollbar;

    JTableTest(Frame owner_) {
        super(owner_, "JTable in a JScrollPane");
        _insets = new Insets(3, 3, 3, 3);
        Container contentPane = getContentPane();

        JPanel northpan = new JPanel();
        northpan.setBorder(new EmptyBorder(1, 1, 1, 1));
        northpan.add(new JLabel("Press ENTER to select/deselect columns/rows"));
        contentPane.add(northpan, BorderLayout.NORTH);

        contentPane.add(makeCenterPanel(), BorderLayout.CENTER);

        contentPane.add(makeEastPanel(), BorderLayout.EAST);

        _okButton = new JButton("OK");
        _okButton.addActionListener(this);
        contentPane.add(_okButton, BorderLayout.SOUTH);

        pack();
    }

    public void actionPerformed(ActionEvent e_) {
        hide();
    }

    public void itemStateChanged(ItemEvent e_) {
        Object source = e_.getSource();
        if (source == _checkBoxAllowRowSelection) {
            boolean allowed = _checkBoxAllowRowSelection.isSelected();
            _table.setRowSelectionAllowed(allowed);
        } else if (source == _checkBoxAllowColumnSelection) {
            boolean allowed = _checkBoxAllowColumnSelection.isSelected();
            _table.setColumnSelectionAllowed(allowed);
        } else if (source == _checkBoxAllowMultipleSelection) {
            boolean allowed = _checkBoxAllowMultipleSelection.isSelected();
            if (allowed)
                _table
                        .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            else
                _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
    }

    private JPanel makeCenterPanel() {
        JPanel centerpan = new JPanel();
        centerpan.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 1));
        String[] headings = { "Name", "Color", "Composition", "Mass", "Radius",
                "Orbit"};
        String[][] data = {
                { "Mars", "Red", "Dust", "1.5e10", "2.7e6", "Elliptical"},
                { "Pluto", "Blue", "Rock", "2.3e11", "2.9e7", "Circular"},
                { "Luna", "Green", "Cheese", "1.3e5", "2.3e12", "Square"},
                { "Venus", "White", "Gas", "4.3e5", "2.3e12",
                        "A funny irregular shape whose name is longer than the table width"},
                { "Jupiter", "Black", "Marshmallow", "4.3e6", "2.3e12",
                        "Zigzag"},
                { "Neptune", "Purple", "Gas", "1.2e6", "2.4e2", "Elliptical"},
                { "Saturn", "Yellow", "Gas", "1.1e7", "1.4e6", "Circular"}};

        /*
         * The following inner class overrides the processKeyEvent() method of
         * JTable, so that we can display the selected rows and columns.
         */
        _table = new JTable(data, headings) {

            /*
             * Gets called when the user presses a key in the JTable.
             */
            public void processKeyEvent(KeyEvent e_) {
                super.processKeyEvent(e_);
                if (e_.getKeyCode() != KeyEvent.VK_ENTER) return;

                int[] rows = getSelectedRows();
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < rows.length; i++) {
                    buf.append(rows[ i]);
                    buf.append(' ');
                }
                _selectedRows.setText(buf.toString());

                int[] columns = getSelectedColumns();
                buf = new StringBuffer();
                for (int i = 0; i < columns.length; i++) {
                    buf.append(columns[ i]);
                    buf.append(' ');
                }
                _selectedColumns.setText(buf.toString());
            }
        };
        _table.setPreferredScrollableViewportSize(new Dimension(30, 5));
        //_table.setValueAt("Yellow", 5, 2);
        //_table.setValueAt("Red", 7, 4);
        //_table.setValueAt("Magenta", 1, 5);
        JScrollPane scrollPane = new JScrollPane(_table);
        TitledBorder border = new TitledBorder(new LineBorder(Color.cyan));
        border.setTitle("The Heavenly Bodies");
        scrollPane.setViewportBorder(border);
        //	scrollPane.setSize(25, 6);
        centerpan.add(scrollPane);

        return centerpan;
    }

    private JPanel makeEastPanel() {
        JPanel eastpan = new JPanel();
        eastpan.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 2;
        eastpan.add(_checkBoxAllowRowSelection, gbc);
        _checkBoxAllowRowSelection.addItemListener(this);
        _checkBoxAllowRowSelection.setSelected(true);

        gbc.gridy = 1;
        eastpan.add(_checkBoxAllowColumnSelection, gbc);
        _checkBoxAllowColumnSelection.addItemListener(this);
        _checkBoxAllowColumnSelection.setSelected(true);

        gbc.gridy = 2;
        eastpan.add(_checkBoxAllowMultipleSelection, gbc);
        _checkBoxAllowMultipleSelection.addItemListener(this);
        _checkBoxAllowMultipleSelection.setSelected(false);

        gbc.gridy = 3;
        gbc.gridwidth = 1;
        eastpan.add(new JLabel(""), gbc);

        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridy = 4;
        eastpan.add(new JLabel("selected columns: "), gbc);

        gbc.gridy = 5;
        eastpan.add(new JLabel("selected rows: "), gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        _selectedColumns.setEnabled(false);
        eastpan.add(_selectedColumns, gbc);

        gbc.gridy = 5;
        _selectedRows.setEnabled(false);
        eastpan.add(_selectedRows, gbc);

        return eastpan;
    }
}

/**
 * This class demonstrates how to listen for KeyEvents generated by a
 * component, and modify the component's default reaction to such KeyEvents.
 */

class KeyEventTest extends JDialog implements ActionListener, KeyListener {

    private JCheckBox _checkBox1 = new JCheckBox("System ON");

    private JCheckBox _checkBox2 = new JCheckBox("Alarm ON");

    private JCheckBox _checkBox3 = new JCheckBox("System Armed");

    private charva.awt.util.CapsTextField _capsField;

    KeyEventTest(Frame owner_) {
        super(owner_, "KeyEvent Test");
        Container contentPane = getContentPane();

        contentPane.add(makeNorthPanel(), BorderLayout.NORTH);

        contentPane.add(makeCenterPanel(), BorderLayout.CENTER);

        JPanel southpan = new JPanel();
        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        southpan.add(okButton);

        contentPane.add(southpan, BorderLayout.SOUTH);

        pack();
    }

    /**
     * Implements the ActionListener interface
     */
    public void actionPerformed(ActionEvent e_) {
        String cmd = e_.getActionCommand();
        if (cmd.equals("OK")) hide();
    }

    /**
     * Implements the KeyListener interface
     */
    public void keyPressed(KeyEvent e_) {
        Component src = (Component) e_.getSource();
        int key = e_.getKeyCode();
        if (key == KeyEvent.VK_DOWN
                && (src == _checkBox1 || src == _checkBox2 || src == _checkBox3)) {

            /*
             * Move the keyboard input focus to the textfield below.
             */
            _capsField.requestFocus();

            /*
             * "Consume" the keystroke so that it is not interpreted further by
             * the JCheckBox which generated it.
             */
            e_.consume();

            /*
             * Repaint the dialog-box to update the cursor position.
             */
            repaint();
        }
    }

    /**
     * Implements the KeyListener interface
     */
    public void keyTyped(KeyEvent e_) {
    }

    public void keyReleased(KeyEvent e_) {
    }

    private JPanel makeNorthPanel() {
        JPanel northpan = new JPanel();
        northpan.setBorder(new TitledBorder("A set of JCheckBoxes"));
        northpan.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(1, 1, 1, 1);
        JLabel label = new JLabel(
                "Press CURSOR-DOWN to move to the text-field below");
        northpan.add(label, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        northpan.add(_checkBox1, gbc);
        _checkBox1.addKeyListener(this);

        gbc.gridx = 1;
        northpan.add(_checkBox2, gbc);
        _checkBox2.addKeyListener(this);

        gbc.gridx = 2;
        northpan.add(_checkBox3, gbc);
        _checkBox3.addKeyListener(this);

        return northpan;
    }

    private JPanel makeCenterPanel() {
        JPanel centerpan = new JPanel();
        centerpan.setBorder(new TitledBorder(
                "A Text Field that converts to uppercase"));
        centerpan.setLayout(new BorderLayout());

        JLabel label1 = new JLabel(
                "The CapsTextField is a subclass of JTextField");
        label1.setBorder(new EmptyBorder(1, 1, 0, 1));
        centerpan.add(label1, BorderLayout.NORTH);

        JLabel label2 = new JLabel(
                "that overrides the processKeyEvent() method");
        label2.setBorder(new EmptyBorder(0, 1, 1, 1));
        centerpan.add(label2, BorderLayout.CENTER);

        JPanel southpan = new JPanel();
        southpan.add(new JLabel("CapsTextField: "));
        _capsField = new CapsTextField(
                "THIS FIELD AUTOMATICALLY CONVERTS TO UPPERCASE");
        southpan.add(_capsField);
        centerpan.add(southpan, BorderLayout.SOUTH);

        return centerpan;
    }
}

/**
 * This class demonstrates how to listen for FocusEvents which are generated
 * when a component gains or loses the keyboard input focus.
 */

class FocusEventTest extends JDialog implements FocusListener, ActionListener {

    private JTextField _floatField = new JTextField(15);

    private JTextField _focusLostBy = new JTextField(15);

    private JTextField _focusGainedBy = new JTextField(15);

    private JButton _okButton = new JButton("OK");

    FocusEventTest(Frame owner_) {
        super(owner_, "FocusEvent Test");
        Container contentPane = getContentPane();

        contentPane.add(makeNorthPanel(), BorderLayout.NORTH);

        contentPane.add(makeCenterPanel(), BorderLayout.CENTER);

        JPanel southpan = new JPanel();
        _okButton.addActionListener(this);
        _okButton.addFocusListener(this);
        southpan.add(_okButton);
        contentPane.add(southpan, BorderLayout.SOUTH);

        pack();
    }

    public void actionPerformed(ActionEvent e_) {
        if (e_.getActionCommand().equals("OK")) {
            hide();
        }
    }

    public void focusGained(FocusEvent e_) {
        Object src = e_.getSource();
        if (src == _floatField) {
            _focusGainedBy.setText("_floatField");
        } else if (src == _okButton) {
            _focusGainedBy.setText("_okButton");
        }
    }

    public void focusLost(FocusEvent e_) {
        Object src = e_.getSource();
        if (src == _floatField) {
            _focusLostBy.setText("_floatField");

            try {
                Float.parseFloat(_floatField.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Must be a valid floating-point number", "Error",
                        JOptionPane.PLAIN_MESSAGE);
                _floatField.requestFocus();
            }
        } else if (src == _okButton) {
            _focusLostBy.setText("_okButton");
        }
    }

    private JPanel makeNorthPanel() {
        JPanel northpan = new JPanel();
        northpan.setBorder(new TitledBorder("A floating-point input field"));
        northpan.setLayout(new BorderLayout());

        JLabel label1 = new JLabel("Enter a non-numeric value, and then");
        label1.setBorder(new EmptyBorder(1, 1, 0, 1));
        northpan.add(label1, BorderLayout.NORTH);

        JLabel label2 = new JLabel("try pressing TAB");
        label2.setBorder(new EmptyBorder(0, 1, 1, 1));
        northpan.add(label2, BorderLayout.CENTER);

        northpan.add(_floatField, BorderLayout.SOUTH);
        _floatField.addFocusListener(this);

        return northpan;
    }

    private JPanel makeCenterPanel() {
        JPanel centerpan = new JPanel();
        centerpan.setBorder(new TitledBorder("Status"));
        centerpan.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.EAST;
        centerpan.add(new JLabel("Focus lost by: "), gbc);

        gbc.gridy = 1;
        centerpan.add(new JLabel("Focus gained by: "), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        centerpan.add(_focusLostBy, gbc);
        _focusLostBy.setEnabled(false);

        gbc.gridy = 1;
        centerpan.add(_focusGainedBy, gbc);
        _focusGainedBy.setEnabled(false);

        return centerpan;
    }
}

//========================================================================
/**
 * This class demonstrates how to run a long-duration task in a separate thread
 * and display the task's progress in a JProgressBar component.
 */

class ProgressBarTest extends JDialog implements ActionListener {

    private JProgressBar _progressBar = new JProgressBar();

    private Thread _taskThread;

    ProgressBarTest(Frame owner_) {
        super(owner_, "JProgressBar Test");
        Container contentPane = getContentPane();

        contentPane.add(makeNorthPanel(), BorderLayout.NORTH);

        contentPane.add(makeCenterPanel(), BorderLayout.CENTER);

        JPanel southpan = new JPanel();
        JButton startButton = new JButton("Start Task");
        startButton.addActionListener(this);
        southpan.add(startButton);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        southpan.add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        southpan.add(cancelButton);

        contentPane.add(southpan, BorderLayout.SOUTH);

        pack();
    }

    /**
     * Implements the ActionListener interface
     */
    public void actionPerformed(ActionEvent e_) {
        String cmd = e_.getActionCommand();
        if (cmd.equals("Start Task")) {
            if (_taskThread != null && _taskThread.isAlive()) {
                JOptionPane.showMessageDialog(this,
                        "The task is already running", "Error",
                        JOptionPane.PLAIN_MESSAGE);
            } else {
                _taskThread = new TaskThread();
                _taskThread.start();
            }
        } else if (cmd.equals("OK")) {
            if (_taskThread != null && _taskThread.isAlive()) {
                JOptionPane.showMessageDialog(this,
                        "The task is still running", "Error",
                        JOptionPane.PLAIN_MESSAGE);
            } else
                hide();
        } else if (cmd.equals("Cancel")) {
            if (_taskThread != null && _taskThread.isAlive()) {
                _taskThread.interrupt();
            }
            hide();
        }
    }

    private JPanel makeNorthPanel() {
        JPanel northpan = new JPanel();
        northpan.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(1, 1, 1, 1);
        JLabel label = new JLabel(
                "Press START TASK to run a long task in a separate thread.");
        northpan.add(label, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 1, 0, 1);
        label = new JLabel(
                "While the task is running, press TAB and then enter some");
        northpan.add(label, gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 1, 0, 1);
        label = new JLabel(
                "text in the TextField to verify that the user interface");
        northpan.add(label, gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 1, 1, 1);
        label = new JLabel("is still responsive.");
        northpan.add(label, gbc);

        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 1, 0, 1);
        label = new JLabel(
                "The progress bar will start in indeterminate mode, indicating");
        northpan.add(label, gbc);

        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 1, 0, 1);
        label = new JLabel(
                "that the task duration is initially unknown; then it will");
        northpan.add(label, gbc);

        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 1, 1, 1);
        label = new JLabel("change to determinate mode.");
        northpan.add(label, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        northpan.add(new JLabel("Enter some text here:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        northpan.add(new JTextField(20), gbc);
        gbc.insets = new Insets(0, 1, 1, 1);

        return northpan;
    }

    private JPanel makeCenterPanel() {
        JPanel centerpan = new JPanel();
        centerpan.setBorder(new TitledBorder("Task Progress"));
        centerpan.setLayout(new BorderLayout());

        _progressBar.setStringPainted(true);
        centerpan.add(_progressBar, BorderLayout.CENTER);

        return centerpan;
    }

    /**
     * A nonstatic inner class that pretends to perform a time-consuming task.
     */
    private class TaskThread extends Thread {

        /** Constructor */
        private TaskThread() {
        }

        /**
         * Pretend to do a task that takes a long time. Twice per second, wake
         * up and update the progress bar. Note that since this thread is not
         * the event-dispatching thread, we cannot manipulate the screen
         * components directly; instead, we must call the static method
         * "invokeLater()" of the SwingUtilities class, which will cause the
         * event-dispatching thread to update the progress bar. See "Core Java,
         * Volume II" by Horstmann and Cornell, chapter 1; Also see
         * http://java.sun.com/docs/books/tutorial/uiswing/overview/threads.html
         */
        public void run() {

            try {
                // Initially, set the progressbar to indeterminate mode
                // for 5 seconds (i.e. pretend we don't initially know
                // the duration of the task).
                _progressBar.setIndeterminate(true);
                Thread.sleep(5000L);
                _progressBar.setIndeterminate(false);

                for (int percent = 0; percent <= 100; percent += 2) {

                    Thread.sleep(500L);
                    Runnable updater = new ProgressBarUpdater(percent);
                    SwingUtilities.invokeLater(updater);
                }
            } catch (InterruptedException e) {
                System.err.println("TaskThread was interrupted");
                return;
            }
        }
    }

    /**
     * This is a nonstatic inner class that implements the Runnable interface;
     * instances of this can be passed to the SwingUtilities.invokeLater()
     * method. A shortcut method of invoking code in the event-dispatch thread,
     * involving the use of anonymous inner classes, is shown in "Core Java
     * Volume II" by Horstmann and Cornell, chapter 1, in the "Threads and
     * Swing" subsection.
     */
    private class ProgressBarUpdater implements Runnable {

        private int _percent;

        private ProgressBarUpdater(int percent_) {
            _percent = percent_;
        }

        public void run() {
            String str = Integer.toString(_percent) + "%";
            _progressBar.setString(str);
            _progressBar.setValue(_percent);
        }
    }
}

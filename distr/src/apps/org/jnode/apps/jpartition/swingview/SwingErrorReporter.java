package org.jnode.apps.jpartition.swingview;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.jnode.apps.jpartition.ErrorReporter;

public class SwingErrorReporter extends ErrorReporter {
    @Override
    protected void displayError(Object source, String message) {
        Component parent = (source instanceof Component) ? (Component) source : null;
        JOptionPane.showMessageDialog(parent, "an error happened : " + message +
                "\nSee logs for details", "error", JOptionPane.ERROR_MESSAGE);
    }
}

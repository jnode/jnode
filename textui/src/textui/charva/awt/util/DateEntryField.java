/* class DateEntryField
 *
 * Copyright (C) 2001  R M Pitman
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

package charva.awt.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import charva.awt.Component;
import charva.awt.Dialog;
import charva.awt.FlowLayout;
import charva.awt.Frame;
import charva.awt.Toolkit;
import charva.awt.event.FocusEvent;
import charva.awt.event.FocusListener;
import charva.awt.event.KeyEvent;
import charva.awt.event.KeyListener;
import charvax.swing.JLabel;
import charvax.swing.JOptionPane;
import charvax.swing.JPanel;
import charvax.swing.JTextField;

/**
 * This class displays a date entry field in the format CCYY/MM/DD and allows
 * the user to edit the contents of the three subfields. It intercepts each
 * keystroke and causes a beep if a non-numeric key is typed.
 */
public class DateEntryField extends JPanel implements KeyListener,
        FocusListener {

    public DateEntryField(Frame owner_) {
        this(owner_, TimeZone.getDefault());
    }

    public DateEntryField(Frame owner_, TimeZone zone_) {
        //_owner = owner_;
        init(zone_);
    }

    public DateEntryField(Dialog owner_) {
        this(owner_, TimeZone.getDefault());
    }

    public DateEntryField(Dialog owner_, TimeZone zone_) {
        //_owner = owner_;
        init(zone_);
    }

    private void init(TimeZone zone_) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        _cal = new GregorianCalendar(zone_);

        add(_yearField);
        _yearField.addKeyListener(this);
        _yearField.addFocusListener(this);
        add(new JLabel("/"));
        add(_monthField);
        _monthField.addKeyListener(this);
        _monthField.addFocusListener(this);
        add(new JLabel("/"));
        add(_dayField);
        _dayField.addKeyListener(this);
        _dayField.addFocusListener(this);
    }

    public void keyPressed(KeyEvent e_) {
        /*
         * Allow non-printing keys to be processed by the superclass.
         */
        return; // pass the event on to its destination.
    }

    /**
     * This is never called.
     */
    public void keyReleased(KeyEvent e_) {
    }

    public void keyTyped(KeyEvent e_) {
        int key = e_.getKeyCode();

        /*
         * If the key is non-numeric, ring the bell and "consume" the keystroke
         * so that it doesn't get echoed.
         */
        if (key < '0' || key > '9') {
            Toolkit.getDefaultToolkit().beep();
            e_.consume();
        }

        Component source = (Component) e_.getSource();
        if (source == _yearField && _yearField.getText().length() < 4) return;

        if (source == _monthField && _monthField.getText().length() < 2)
                return;

        if (source == _dayField && _dayField.getText().length() < 2) return;

        Toolkit.getDefaultToolkit().beep();
        e_.consume();
    }

    public void focusGained(FocusEvent fe_) {
    }

    public void focusLost(FocusEvent e_) {
        Component source = (Component) e_.getSource();

        /*
         * Get the absolute origin of this component.
         */
        /* Point origin = */getLocationOnScreen();

        try {
            if (source == _yearField) {
                if (_yearField.getText().equals("")) {
                    String[] msgs = { "A valid year must be ",
                            "entered in this field "};

                    JOptionPane.showMessageDialog(_yearField, msgs, "Error",
                            JOptionPane.ERROR_MESSAGE);

                    _yearField.requestFocus();
                } else {
                    int year = Integer.parseInt(_yearField.getText());
                    _cal.set(Calendar.YEAR, year);
                }
            }

            else if (source == _monthField) {

                if (_monthField.getText().equals("") == false) {
                    int month = Integer.parseInt(_monthField.getText());
                    if (month >= 1 && month <= 12) {
                        _cal.clear(Calendar.DAY_OF_MONTH);
                        _cal.set(Calendar.MONTH, month - 1);
                        return;
                    }
                }
                String[] msgs = { "The month must be", "between 1 and 12"};

                JOptionPane.showMessageDialog(_monthField, msgs, "Error",
                        JOptionPane.ERROR_MESSAGE);

                _monthField.requestFocus();
            }

            else if (source == _dayField) {
                int maxday = _cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                if (_dayField.getText().equals("") == false) {
                    int day = Integer.parseInt(_dayField.getText());

                    /*
                     * By the time we get here, the year and month fields have
                     * been filled in already, so we can check whether the day
                     * is valid.
                     */
                    if (day >= 1 && day <= maxday) {
                        _cal.set(Calendar.DAY_OF_MONTH, day);
                        return;
                    }
                }
                String[] msgs = { "The day must be", "between 1 and " + maxday};
                JOptionPane.showMessageDialog(_dayField, msgs, "Error",
                        JOptionPane.ERROR_MESSAGE);

                _dayField.requestFocus();
            }
        } catch (NumberFormatException e) {
            // should never happen because we are trapping non-numeric
            // keystrokes.
            System.err.println("Number format exception");
        }
    }

    /**
     * Set the fields to the current time.
     */
    public void set() {
        int year = _cal.get(Calendar.YEAR);
        _yearField.setText(Integer.toString(year));

        int month = _cal.get(Calendar.MONTH) + 1;
        _monthField.setText(Integer.toString(month));

        int day = _cal.get(Calendar.DAY_OF_MONTH);
        _dayField.setText(Integer.toString(day));
    }

    /**
     * Set the year field
     */
    public void setYear(int year_) {
        _yearField.setText(Integer.toString(year_));
        _cal.set(Calendar.YEAR, year_);
    }

    /**
     * Set the month field
     */
    public void setMonth(int month_) {
        _monthField.setText(Integer.toString(month_));
        _cal.set(Calendar.MONTH, month_ - 1);
    }

    /**
     */
    public void setDay(int day_) {
        _dayField.setText(Integer.toString(day_));
        _cal.set(Calendar.DAY_OF_MONTH, day_);
    }

    /**
     * Get the year value.
     */
    public int getYear() {
        return Integer.parseInt(_yearField.getText());
    }

    /**
     * Get the month value (between 1 and 12)
     */
    public int getMonth() {
        return Integer.parseInt(_monthField.getText());
    }

    /**
     * Get the day-of-month value.
     */
    public int getDay() {
        return Integer.parseInt(_dayField.getText());
    }

    private JTextField _yearField = new JTextField(4);

    private JTextField _monthField = new JTextField(2);

    private JTextField _dayField = new JTextField(2);

    private GregorianCalendar _cal;
    //private final Window _owner;
}

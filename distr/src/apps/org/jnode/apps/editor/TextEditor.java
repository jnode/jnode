/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.apps.editor;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.textscreen.TextScreenConsoleManager;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.ShellManager;

/**
 * @author Levente S\u00e1ntha
 */
public class TextEditor implements KeyboardListener {
    private int cx, cy, sh, sw, cxm, cym, fx, fy;
    private List<StringBuilder> ls;
    private TextConsole console;
    private File file;
    private boolean ro = false;

    public static void main(String[] argv) throws Exception {
        if (argv.length == 0) {
            System.out.println("No file specified");
            return;
        }

        ShellManager sm = InitialNaming.lookup(ShellManager.NAME);
        TextScreenConsoleManager manager = (TextScreenConsoleManager) sm.getCurrentShell().getConsole().getManager();
        TextConsole console = manager.createConsole(
            "editor",
            (ConsoleManager.CreateOptions.TEXT |
                ConsoleManager.CreateOptions.STACKED |
                ConsoleManager.CreateOptions.NO_LINE_EDITTING |
                ConsoleManager.CreateOptions.NO_SYSTEM_OUT_ERR));
        try {
            manager.focus(console);

            TextEditor te = new TextEditor(console);
            File f = new File(argv[0]);
            te.ro = argv.length == 2 && "ro".equals(argv[1]);
            te.loadFile(f);
        } catch (Exception e) {
            manager.unregisterConsole(console);
            throw e;
        }
    }

    public TextEditor(TextConsole console) {
        this.console = console;
        console.addKeyboardListener(this);
        this.console.setCursorVisible(true);
        cx = 0;
        cy = 0;
        sh = console.getHeight();
        sw = console.getWidth();
        cxm = sw - 1;
        cym = sh - 1 - 1;
        fx = 0;
        fy = 0;
        ls = new ArrayList<StringBuilder>();
    }

    public void loadFile(File f) throws IOException {
        ls.clear();
        if (f.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(f));
            int c;
            StringBuilder sb = new StringBuilder();
            while ((c = br.read()) != -1) {
                if (c == '\n') {
                    sb.append((char) c);
                    ls.add(sb);
                    sb = new StringBuilder();
                } else {
                    sb.append((char) c);
                }
            }
            if (sb.length() > 0)
                ls.add(sb);
        }

        file = f;
        updateScreen();
    }

    private String message;

    private void saveFile() {
        if (file == null) {
            System.out.println("No file.");
            return;
        }
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                try {
                    StringBuilder buf = new StringBuilder();
                    FileWriter fw = new FileWriter(file);

                    for (StringBuilder sb : ls)
                        buf.append(sb);

                    fw.write(buf.toString());
                    fw.flush();
                    fw.close();
                    message = "Saved " + file;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }


    private void updateScreen() {
        char[] data = new char[sw * (cym + 1 + 1)];
        int k = 0;
        StringBuilder e = new StringBuilder();
        List<StringBuilder> rl = new ArrayList<StringBuilder>();
        for (int i = 0; i < cym + 1; i++) {
            StringBuilder l = (fy + i < ls.size()) ? ls.get(fy + i) : e;
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < l.length(); j++) {
                char c = l.charAt(j);
                if (c == '\t') {
                    sb.append(' ');
                    sb.append(' ');
                    sb.append(' ');
                    sb.append(' ');
                } else if (c == '\n') {
                    sb.append(' ');
                } else {
                    sb.append(c);
                }
            }
            rl.add(sb);
        }

        int tab_count = 0;
        if (ls.size() > 0) {
            CharSequence l = ls.get(oy()).subSequence(0, fx + cx);
            for (int i = 0; i < l.length(); i++) {
                if (l.charAt(i) == '\t') tab_count++;
            }
        }

        int tx = fx + cx + 3 * tab_count;
        int cx2 = cx + 3 * tab_count;
        int fx2 = fx;
        if (tx >= sw) {
            cx2 = sw - 1;
            fx2 = tx - cx2;
        }

        for (int i = 0; i < cym + 1; i++) {
            StringBuilder l = rl.get(i);
            for (int j = 0; j < sw; j++) {
                char c = (fx2 + j < l.length()) ? l.charAt(fx2 + j) : ' ';
                data[k++] = c;
            }
        }

        if (message != null) {
            e.append(message);
            message = null;
        } else {
            e.append(ro ? "VIEW: " : "EDIT: ").append(file.getName()).
                append(" [").append(fx2 + cx2 + 1).append(",").append(oy() + 1).append("]");
        }
        for (int j = 0; j < sw; j++) {
            data[k++] = (j < e.length()) ? e.charAt(j) : ' ';
        }
        console.setChar(0, 0, data, 7);
        console.setCursor(cx2, cy);
    }

    public void keyPressed(KeyboardEvent e) {
        int k = e.getKeyCode();
        if (ro ^ e.isControlDown()) {
            switch (k) {
                case KeyEvent.VK_UP: {
                    if (fy > 0) fy--;
                    break;
                }
                case KeyEvent.VK_DOWN: {
                    if (fy < my() - cym) fy++;
                    break;
                }
                case KeyEvent.VK_LEFT: {
                    if (fx > 0) fx--;
                    break;
                }
                case KeyEvent.VK_RIGHT: {
                    if (fx < mx()) fx++;
                    break;
                }
                case KeyEvent.VK_HOME: {
                    cx = fx = cy = fy = 0;
                    break;
                }
                case KeyEvent.VK_END: {
                    if (my() < cym) cy = my();
                    else {
                        cy = cym;
                        fy = my() - cy;
                    }
                    end();
                    break;
                }
                case KeyEvent.VK_Y: {
                    if (ro) break;
                    if (oy() >= 0 && oy() <= my()) {
                        ls.remove(oy());
                        if (cy > 0 && oy() == my() + 1) cy--;
                        else if (fy > 0) fy--;
                    }
                    break;
                }
                case KeyEvent.VK_S:
                case KeyEvent.VK_F2: {
                    if (ro) break;
                    saveFile();
                    break;
                }
                case KeyEvent.VK_Q:
                case KeyEvent.VK_F10: {
                    console.getManager().unregisterConsole(console);
                    break;
                }

                //same as without control key down
                case KeyEvent.VK_PAGE_UP: {
                    if (fy > cym) fy -= cym;
                    else fy = 0;
                    break;
                }
                case KeyEvent.VK_PAGE_DOWN: {
                    if (my() - fy > cym) fy += cym;
                    break;
                }

            }
        } else {
        out:
            switch (k) {
                case KeyEvent.VK_UP: {
                    cursorUp();
                    break;
                }
                case KeyEvent.VK_DOWN: {
                    cursorDown();
                    break;
                }
                case KeyEvent.VK_LEFT: {
                    if (cx > 0) cx--;
                    else if (fx > 0) fx--;
                    else if (cursorUp()) end();
                    break;
                }
                case KeyEvent.VK_RIGHT: {
                    if (ox() < mx()) {
                        if (cx < cxm) cx++;
                        else fx++;
                    } else if (cursorDown()) {
                        cx = 0;
                        fx = 0;
                    }
                    break;
                }
                case KeyEvent.VK_HOME: {
                    cx = fx = 0;
                    break;
                }
                case KeyEvent.VK_END: {
                    end();
                    break;
                }

                //same as with control key down
                case KeyEvent.VK_PAGE_UP: {
                    if (fy > cym) fy -= cym;
                    else fy = 0;
                    break;
                }
                case KeyEvent.VK_PAGE_DOWN: {
                    if (my() - fy > cym) fy += cym;
                    break;
                }
                default:
                    if (ro) break;

                    char c = e.getKeyChar();
                    if (c == KeyEvent.CHAR_UNDEFINED && k != KeyEvent.VK_DELETE) return;
                    if (cy == ls.size()) ls.add(new StringBuilder());
                    StringBuilder l = ls.get(oy());
                    switch (k) {
                        case KeyEvent.VK_ENTER: {
                            c = '\n';
                            ls.add(oy() + 1, new StringBuilder());
                            break;
                        }
                        case KeyEvent.VK_DELETE: {
                            if (cx == mx()) {
                                if (cy < my()) {
                                    l.deleteCharAt(l.length() - 1).append(ls.remove(oy() + 1));
                                }
                            } else if (cx < mx())
                                l.deleteCharAt(ox());
                            break out;
                        }
                        case KeyEvent.VK_BACK_SPACE: {
                            if (ox() == 0) {
                                if (cy > 0) {
                                    StringBuilder l2 = ls.get(oy() - 1);
                                    cx = l2.length() - 1;
                                    l2.deleteCharAt(l2.length() - 1).append(ls.remove(oy()));
                                    cy--;
                                } else if (fy > 0) {
                                    StringBuilder l2 = ls.get(oy() - 1);
                                    cx = l2.length() - 1;
                                    l2.deleteCharAt(l2.length() - 1).append(ls.remove(oy()));
                                    fy--;
                                }
                            } else if (cx > 0) {
                                cx--;
                                l.deleteCharAt(ox());
                            } else {
                                fx--;
                                l.deleteCharAt(ox());
                            }
                            break out;
                        }

                    }
                    if (cx == l.length()) l.append(c);
                    else l.insert(ox(), c);
                    if (c == '\n') {
                        if (cy < cym) cy++;
                        else fy++;
                        if (cx < l.length() + 1) {
                            StringBuilder l2 = ls.get(oy());
                            l2.append(l, ox() + 1, l.length());
                            l.delete(ox() + 1, l.length());
                        }
                        cx = fx = 0;
                    } else {
                        if (cx == cxm) fx++;
                        else cx++;
                    }
            }
        }
        if (oy() > cym && cy < 0) {
            cy = cym;
            fy = my() - cy;
        }
        if (oy() > my()) {
            fy -= oy() - my();
            fy = fy < 0 ? 0 : fy;
        }
        if (ox() > mx()) {
            if (mx() > cxm) {
                cx = cxm;
                fx = mx() - cx;
            } else {
                cx = mx();
                fx = 0;
            }
        }
        e.consume();
        updateScreen();
    }

    private void end() {
        if (mx() < cxm)
            cx = mx();
        else {
            cx = cxm;
            fx = mx() - cx;
        }
    }

    private boolean cursorUp() {
        boolean ret;
        if (ret = (cy > 0))
            cy--;
        else if (ret = (fy > 0))
            fy--;
        return ret;
    }

    private boolean cursorDown() {
        boolean ret;
        if (ret = (oy() < my()))
            if (cy < cym) cy++;
            else fy++;
        return ret;
    }

    private int oy() {
        return fy + cy;
    }

    private int ox() {
        return fx + cx;
    }

    private int my() {
        return ls.size() - 1;
    }

    private int mx() {
        return ls.size() == 0 ? 0 : ls.get(oy()).length() - (oy() == my() ? 0 : 1);
    }

    public void keyReleased(KeyboardEvent event) {
    }
}

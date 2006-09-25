package org.jnode.apps.editor;

import org.jnode.driver.input.KeyboardListener;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.textscreen.TextScreenConsoleManager;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.ShellManager;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.awt.event.KeyEvent;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author Levente S\u00e1ntha
 */
public class TextEditor implements KeyboardListener {
    private int cx, cy, sh, sw, cxm, cym, fx, fy;
    private List<StringBuilder> ls;
    private TextConsole console;
    private File file;

    public static void main(String[] argv) throws Exception {
        if(argv.length == 0){
            System.out.println("No file specified");
            return;
        }

        ShellManager sm = InitialNaming.lookup(ShellManager.NAME);
        TextScreenConsoleManager manager = (TextScreenConsoleManager) sm.getCurrentShell().getConsole().getManager();
        TextConsole console = manager.createConsole("editor",
                    ConsoleManager.CreateOptions.TEXT |ConsoleManager.CreateOptions.NO_SYSTEM_OUT_ERR_IN);
        manager.focus(console);

        TextEditor te = new TextEditor(console);
        File f = new File(argv[0]);
        te.loadFile(f);
    }

    public TextEditor(TextConsole first) {
        this.console = first;
        first.addKeyboardListener(this);
        console.setCursorVisible(true);
        cx = 0;
        cy = 0;
        sh = first.getHeight();
        sw = first.getWidth();
        cxm = sw - 1;
        cym = sh - 1;
        fx = 0;
        fy = 0;
        ls = new ArrayList<StringBuilder>();
    }

    public void loadFile(File f)  throws IOException {
        ls.clear();
        if(f.exists()){
            BufferedReader br = new BufferedReader(new FileReader(f));
            int c;
            StringBuilder sb = new StringBuilder();
            while((c = br.read()) != -1){
                if(c == '\n'){
                    sb.append((char) c);
                    ls.add(sb);
                    sb = new StringBuilder();
                } else {
                    sb.append((char) c);
                }
            }
            if(sb.length() > 0)
                ls.add(sb);
        }

        file = f;
        updateScreen();
    }

    private void saveFile() {
        if(file == null) {
            System.out.println("No file.");
            return;
        }
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    StringBuilder buf = new StringBuilder();
                    FileWriter fw = new FileWriter(file);
                    for(StringBuilder sb : ls)
                        buf.append(sb);
                    fw.write(buf.toString());
                    fw.flush();
                    fw.close();
                    System.out.println("Saved " + file);
                } catch(Exception e){
                   e.printStackTrace();
                }
                return null;
            }
        });
    }

    private void updateScreen(){
        char[] data = new char[sw * sh];
        int k = 0;
        StringBuilder e = new StringBuilder();
        for(int i = 0; i < sh; i ++){
            StringBuilder l = (fy + i < ls.size()) ? ls.get(fy + i) : e;
            for(int j = 0; j < sw; j++){
                char c = (fx + j < l.length()) ? l.charAt(fx + j) : ' ';
                data[k++] = (c == '\n' || c == '\t') ? ' ' : c;
            }
        }
        console.setChar(0, 0, data, 7);
        console.setCursor(cx, cy);
    }

    public void keyPressed(KeyboardEvent e) {
        int k = e.getKeyCode();
        if(e.isControlDown())
            switch (k) {
                case KeyEvent.VK_UP: {if(fy > 0) fy --; break;}
                case KeyEvent.VK_DOWN: {if(fy < my() - cym) fy ++; break;}
                case KeyEvent.VK_LEFT: {if(fx > 0) fx --; break;}
                case KeyEvent.VK_RIGHT: {if(fx < mx()) fx ++; break;}
                case KeyEvent.VK_HOME: {cx = fx = cy = fy = 0; break;}
                case KeyEvent.VK_END: { if(my() < cym) cy = my(); else {cy = cym; fy = my() - cy;}; end(); break;}
                case KeyEvent.VK_Y: { ls.remove(oy()); if(oy() > my()) cy --; break;}
                case KeyEvent.VK_S: { saveFile(); break;}
                case KeyEvent.VK_Q: { console.getManager().unregisterConsole(console); break;}

            }
        else
        out: switch (k) {
                case KeyEvent.VK_UP: { cursorUp(); break;}
                case KeyEvent.VK_DOWN: {cursorDown();break;}
                case KeyEvent.VK_LEFT: {if(cx > 0) cx--; else if(fx > 0) fx --; else if(cursorUp()) end(); break;}
                case KeyEvent.VK_RIGHT: {if(ox() < mx()) if(cx < cxm) cx++; else fx ++; else if(cursorDown()) {cx = 0; fx = 0; } break;}
                case KeyEvent.VK_HOME: {cx = fx = 0; break;}
                case KeyEvent.VK_END: { end(); break;}
                case KeyEvent.VK_PAGE_UP: {if(fy > cym) fy -= cym; else fy = 0; break;}
                case KeyEvent.VK_PAGE_DOWN: {if(my() - fy > cym) fy +=cym; break;}
                default:
                    char c = e.getKeyChar();
                    if(c == 0 && k != KeyEvent.VK_DELETE) return;
                    if(cy == ls.size()) ls.add(new StringBuilder());
                    StringBuilder l = ls.get(oy());
                    switch(k){
                        case KeyEvent.VK_ENTER: {c = '\n';ls.add(oy() + 1, new StringBuilder()); break;}
                        case KeyEvent.VK_DELETE: {
                            if(cx == mx()) {
                                if(cy < my()){
                                    l.deleteCharAt(l.length() - 1).append(ls.remove(oy() + 1));
                                }
                            } else if(cx < mx())
                                l.deleteCharAt(ox());
                            break out;
                        }
                        case KeyEvent.VK_BACK_SPACE: {
                            if(ox() == 0) {
                                if(cy > 0){
                                    StringBuilder l2 = ls.get(oy() - 1);
                                    cx = l2.length() - 1;
                                    l2.deleteCharAt(l2.length() - 1).append(ls.remove(oy()));
                                    cy --;
                                }
                            } else if(cx > 0){
                                cx --;
                                l.deleteCharAt(ox());
                            } else {
                                fx --;
                                l.deleteCharAt(ox());
                            }
                            break out;
                        }

                    }
                    if(cx == l.length()) l.append(c); else l.insert(ox(), c);
                    if(c == '\n' ) {
                        if(cy < cym) cy ++; else fy ++;
                        if(cx < l.length() + 1){
                            StringBuilder l2 = ls.get(oy());
                            l2.append(l, ox() + 1, l.length());
                            l.delete(ox() + 1, l.length());
                        }
                        cx = fx = 0;
                    } else {
                        if(cx == cxm) fx ++; else cx ++;
                    }
        }
        if(oy() > cym && cy < 0){
            cy = cym;
            fy = my() - cy;
        }
        if(oy() > my()){
            fy -= oy() - my();
        }
        if(ox() > mx()) {
            if(mx() > cxm){
                cx = cxm;
                fx = mx() - cx;
            } else {
                cx = mx();
                fx = 0;
            }
        }
        updateScreen();
    }

    private void end() {
        if(mx() < cxm)
            cx = mx();
        else {
            cx = cxm; fx = mx() - cx;
        }
    }

    private boolean cursorUp() {
        boolean ret;
        if(ret = (cy > 0))
            cy--;
        else if(ret = (fy > 0))
            fy--;
        return ret;
    }

    private boolean cursorDown() {
        boolean ret;
        if(ret = (oy() < my()))
            if(cy < cym) cy++;
            else fy++;
        return ret;
    }

    private int oy() {
        return fy + cy;
    }

    private int ox() {return fx + cx;}

    private int my() {return ls.size() - 1;}

    private int mx() {return ls.size() == 0 ? 0 : ls.get(oy()).length() - ( oy() == my() ? 0 : 1);}

    public void keyReleased(KeyboardEvent event) {}
}

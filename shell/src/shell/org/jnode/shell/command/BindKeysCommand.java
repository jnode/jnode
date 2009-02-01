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
 
package org.jnode.shell.command;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jnode.driver.console.Console;
import org.jnode.driver.console.KeyEventBindings;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.VirtualKey;
import org.jnode.driver.console.textscreen.ConsoleKeyEventBindings;
import org.jnode.driver.console.textscreen.KeyboardReaderAction;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.CommandSyntaxException;
import org.jnode.shell.syntax.EnumArgument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * This command allows the user to examine and change JNode's key bindings.
 * 
 * @author crawley@jnode.org
 */
public class BindKeysCommand extends AbstractCommand {
    
    private static final Map<String, Integer> VK_NAME_MAP = 
        new HashMap<String, Integer>();
    private static final Map<Integer, String> VK_MAP = 
        new HashMap<Integer, String>();
    private static final Map<String, Integer> MODIFIER_NAME_MAP = 
        new HashMap<String, Integer>();
    private static final Map<Integer, String> MODIFIER_MAP = 
        new HashMap<Integer, String>();
    
    static {
        // This is the best way I can think of to enumerate all of the VK_ codes
        // defined by the KeyEvent class.
        for (Field field : KeyEvent.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && 
                    field.getName().startsWith("VK_")) {
                try {
                    Integer vk = (Integer) field.get(null);
                    String name = constCase(KeyEvent.getKeyText(vk.intValue()));
                    VK_NAME_MAP.put(name, vk);
                    VK_MAP.put(vk, name);
                } catch (IllegalAccessException ex) {
                    // This cannot happen.  But if it does we'll just ignore
                    // the virtual key constant that caused it.
                }
            }
        }
        // Now do the same for the modifiers.  Note that we map the names to the 'new' 
        // modifier mask values; see KeyEvent javadoc ...
        initModifier("AWT.shift", "Shift", KeyEvent.SHIFT_DOWN_MASK);
        initModifier("AWT.control", "Ctrl", KeyEvent.CTRL_DOWN_MASK);
        initModifier("AWT.alt", "Alt", KeyEvent.ALT_DOWN_MASK);
        initModifier("AWT.meta", "Meta", KeyEvent.META_DOWN_MASK);
        initModifier("AWT.button1", "Button 1", KeyEvent.BUTTON1_DOWN_MASK);
        initModifier("AWT.button2", "Button 2", KeyEvent.BUTTON2_DOWN_MASK);
        initModifier("AWT.button3", "Button 3", KeyEvent.BUTTON3_DOWN_MASK);
        initModifier("AWT.altGraph", "Alt Graph", KeyEvent.ALT_GRAPH_DOWN_MASK);
    }
    
    private static void initModifier(String propName, String dflt, int modifier) {
        String name = constCase(Toolkit.getProperty(propName, dflt));
        MODIFIER_NAME_MAP.put(name,  modifier);
        MODIFIER_MAP.put(modifier, name);
    }
    
    private static final String[] ASCII_NAMES = new String[] {
        "NUL", "SOH", "STC", "ETX", "EOT", "ENQ", "ACK", "BEL", 
        "BS", "HT", "NL", "VT", "FF", "CR", "SO", "SI", 
        "DLE", "DC1", "DC2", "DC3", "DC4", "NAK", "SYN", "ETB",
        "CAN", "EM", "SUB", "ESC", "FS", "GS", "RS", "US", "SP"
    };
    
    private static class ActionArgument extends EnumArgument<KeyboardReaderAction> {
        public ActionArgument(String label, int flags, String description) {
            super(label, flags, KeyboardReaderAction.class, description);
        }

        @Override
        protected String argumentKind() {
            return "keyboard reader action";
        }
    }
    
    private static class VirtualKeyArgument extends Argument<VirtualKey> {

        protected VirtualKeyArgument(String label, int flags, String description) {
            super(label, flags, new VirtualKey[0], description);
        }

        @Override
        protected String argumentKind() {
            return "VK spec";
        }

        @Override
        protected VirtualKey doAccept(Token value) throws CommandSyntaxException {
            String str = value.text;
            String[] parts = str.split("\\+");
            int modifiers = 0;
            for (int i = 0; i < parts.length - 1; i++) {
                Integer m = MODIFIER_NAME_MAP.get(constCase(parts[i]));
                if (m == null) {
                    throw new CommandSyntaxException(parts[i] + "' is an unknown modifier");
                }
                modifiers |= m;
            }
            Integer vk = VK_NAME_MAP.get(constCase(parts[parts.length - 1]));
            if (vk == null) {
                throw new CommandSyntaxException(parts[parts.length - 1] + "' is an unknown virtual key name");
            }
            return new VirtualKey(vk, modifiers);
        }
    }
    
    private static class CharacterArgument extends Argument<Character> {

        protected CharacterArgument(String label, int flags, String description) {
            super(label, flags, new Character[0], description);
        }

        @Override
        protected String argumentKind() {
            return "character";
        }

        @Override
        protected Character doAccept(Token value) throws CommandSyntaxException {
            String str = value.text;
            String upper = str.toUpperCase();
            for (int i = 0; i < ASCII_NAMES.length; i++) {
                if (ASCII_NAMES[i].equals(upper)) {
                    return (char) i;
                }
            }
            if (upper.equals("DEL")) {
                return '\177';
            }
            if (str.length() == 3 && str.charAt(0) == '\'' && str.charAt(2) == '\'') {
                return str.charAt(1);
            }
            if (str.startsWith("0x") || str.startsWith("0X")) {
                int ch = Integer.parseInt(str.substring(2), 16);
                return (char) ch;
            }
            throw new CommandSyntaxException("invalid character");
        }
    }

    private final FlagArgument FLAG_RESET = 
        new FlagArgument("reset", Argument.OPTIONAL,
                "reset the bindings to the default values");

    private final FlagArgument FLAG_ADD = 
        new FlagArgument("add", Argument.OPTIONAL, "add bindings");

    private final FlagArgument FLAG_REMOVE = 
        new FlagArgument("remove", Argument.OPTIONAL, "remove bindings");
    
    private final ActionArgument ARG_ACTION = 
        new ActionArgument("action", Argument.OPTIONAL, "an keyboard reader action");
    
    private final VirtualKeyArgument ARG_VK_SPEC = 
        new VirtualKeyArgument("vkSpec", Argument.OPTIONAL + Argument.MULTIPLE,
                "a virtual key specification");
    
    private final CharacterArgument ARG_CHARACTER = 
        new CharacterArgument("character", Argument.OPTIONAL + Argument.MULTIPLE, 
                "a character");
    
    private PrintWriter out;
    private PrintWriter err;
    

    public BindKeysCommand() {
        super("display or change the keyboard bindings");
        registerArguments(FLAG_RESET, FLAG_ADD, FLAG_REMOVE, 
                ARG_ACTION, ARG_VK_SPEC, ARG_CHARACTER);
    }

    private static String constCase(String keyText) {
        StringBuilder sb = new StringBuilder(keyText);
        int len = keyText.length();
        for (int i = 0; i < len; i++) {
            char ch = sb.charAt(i);
            if (ch == ' ') {
                sb.setCharAt(i, '_');
            } else {
                sb.setCharAt(i, Character.toUpperCase(ch));
            }
        }
        return sb.toString();
    }

    @Override
    public void execute() throws Exception {
        out = getOutput().getPrintWriter();
        err = getError().getPrintWriter();
        Console console = ShellUtils.getCurrentShell().getConsole();
        if (!(console instanceof TextConsole)) {
            err.println("The current console is not a TextConsole.");
        }
        TextConsole textConsole = (TextConsole) console;
        if (FLAG_RESET.isSet()) {
            resetBindings(textConsole);
        } else if (FLAG_ADD.isSet()) {
            addBindings(textConsole);
        } else if (FLAG_REMOVE.isSet()) {
            removeBindings(textConsole);
        } else {
            displayBindings(textConsole);
        }
    }

    /**
     * Remove bindings for an action.
     * 
     * @param console the console whose bindings are to be changed
     */
    private void removeBindings(TextConsole console) {
        ConsoleKeyEventBindings bindings = console.getKeyEventBindings();
        // This throws an unchecked exception if the action is not supplied.  It signals
        // a bug in the command syntax and should be allowed to propagate to the shell.
        KeyboardReaderAction action = ARG_ACTION.getValue();
        
        if (ARG_VK_SPEC.isSet() || ARG_CHARACTER.isSet()) {
            // If virtual key names were supplied, remove only those bindings.
            if (ARG_VK_SPEC.isSet()) {
                for (VirtualKey vk : ARG_VK_SPEC.getValues()) {
                    bindings.unsetVKAction(vk);
                }
            } 
            if (ARG_CHARACTER.isSet()) {
                for (char ch : ARG_CHARACTER.getValues()) {
                    bindings.unsetCharAction(ch);
                }
            } 
        } else {
            // Otherwise remove all bindings for the action.
            int count = 0;
            List<Character> chars = buildCharMap(bindings).get(action);
            if (chars != null) {
                for (char ch : chars) {
                    bindings.unsetCharAction(ch);
                    count++;
                }
            }
            List<VirtualKey> vks = buildVKMap(bindings).get(action);
            if (vks != null) {
                for (VirtualKey vk : vks) {
                    bindings.unsetVKAction(vk);
                    count++;
                }
            }
            if (count == 0) {
                err.println("There are no bindings for action '" + action + "'");
                exit(1);
            }
        }
        console.setKeyEventBindings(bindings);
        out.println("Updated the current console's key bindings for action '" + action + "'.");
    }

    private void addBindings(TextConsole console) {
        ConsoleKeyEventBindings bindings = console.getKeyEventBindings();
        // This throws an unchecked exception if the action is not supplied.  It signals
        // a bug in the command syntax and should be allowed to propagate to the shell.
        KeyboardReaderAction action = ARG_ACTION.getValue();

        int count = 0;
        if (ARG_VK_SPEC.isSet()) {
            for (VirtualKey vk : ARG_VK_SPEC.getValues()) {
                bindings.setVKAction(vk, action);
                count++;
            }
        } 
        if (ARG_CHARACTER.isSet()) {
            for (char ch : ARG_CHARACTER.getValues()) {
                bindings.setCharAction(ch, action);
                count++;
            }
        }
        if (count > 0) {
            console.setKeyEventBindings(bindings);
            out.println("Updated the current console's key bindings for action '" + action + "'.");
        }
    }

    private void resetBindings(TextConsole console) {
        console.setKeyEventBindings(ConsoleKeyEventBindings.createDefault());
        out.println("Reset the current console's key bindings.");
    }

    private void displayBindings(TextConsole console) {
        ConsoleKeyEventBindings bindings = console.getKeyEventBindings();
        
        Map<KeyboardReaderAction, List<Character>> charMap = buildCharMap(bindings);
        Map<KeyboardReaderAction, List<VirtualKey>> vkMap = buildVKMap(bindings);
        
        for (KeyboardReaderAction action : KeyboardReaderAction.values()) {
            List<Character> chars = charMap.get(action);
            List<VirtualKey> vks = vkMap.get(action);
            if (chars == null && vks == null && 
                    (action == KeyboardReaderAction.getDefaultCharAction() ||
                     action == KeyboardReaderAction.getDefaultVKAction())) {
                continue;
            }
            StringBuilder sb = new StringBuilder(40);
            sb.append(describe(action)).append(" : ");
            if (chars == null && vks == null) {
                sb.append("not bound");
            } else {
                boolean first = true;
                if (chars != null) {
                    for (char ch : chars) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append(", ");
                        }
                        sb.append(describe(ch));
                    }
                }
                if (vks != null) {
                    for (VirtualKey vk : vks) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append(", ");
                        }
                        sb.append(describe(vk));
                    }
                }
            }
            out.println(sb);
        }
    }
    
    /**
     * Build a map from actions to the virtual keys that map to them.
     * @param bindings
     * @return the map
     */
    private Map<KeyboardReaderAction, List<VirtualKey>> buildVKMap(
            KeyEventBindings<KeyboardReaderAction> bindings) {
        VirtualKey[] boundKeys = bindings.getBoundVKs();
        Map<KeyboardReaderAction, List<VirtualKey>> vkMap =
            new HashMap<KeyboardReaderAction, List<VirtualKey>>();
        for (VirtualKey vk : boundKeys) {
            KeyboardReaderAction action = bindings.getVKAction(vk);
            List<VirtualKey> list = vkMap.get(action);
            if (list == null) {
                list = new ArrayList<VirtualKey>();
                vkMap.put(action, list);
            }
            list.add(vk);
        }
        return vkMap;
    }

    /**
     * Build a map from actions to the characters that map to them.
     * @param bindings
     * @return the map.
     */
    private Map<KeyboardReaderAction, List<Character>> buildCharMap(
            KeyEventBindings<KeyboardReaderAction> bindings) {
        char[] boundChars = bindings.getBoundChars();
        Map<KeyboardReaderAction, List<Character>> charMap = 
            new HashMap<KeyboardReaderAction, List<Character>>();
        for (char ch : boundChars) {
            KeyboardReaderAction action = bindings.getCharAction(ch);
            List<Character> list = charMap.get(action);
            if (list == null) {
                list = new ArrayList<Character>();
                charMap.put(action, list);
            }
            list.add(ch);
        }
        return charMap;
    }

    private String describe(KeyboardReaderAction action) {
        return action.toString();
    }

    private String describe(char ch) {
        StringBuilder sb = new StringBuilder();
        if (ch < 0x1f) {
            sb.append(ASCII_NAMES[ch]);
        } else if (ch == ' ') {
            sb.append("SP");
        } else if (ch == '\177') {
            sb.append("DEL");
        } else if (ch < '\177') {
            sb.append('\'').append(ch).append('\'');
        } else {
            sb.append('\'').append(ch).append("' (0x" + Integer.toHexString(ch)).append(')');
        }
        return sb.toString();
    }

    private String describe(VirtualKey vk) {
        int modifiers = vk.getModifiers();
        StringBuilder sb = new StringBuilder();
        // We don't use the KeyEvent.getKeyModifierText method because it
        // expects the 'old' mask values and conflates some of the masks.
        if ((modifiers & KeyEvent.SHIFT_DOWN_MASK) != 0) {
            sb.append(MODIFIER_MAP.get(KeyEvent.SHIFT_DOWN_MASK)).append('+');
        }
        if ((modifiers & KeyEvent.CTRL_DOWN_MASK) != 0) {
            sb.append(MODIFIER_MAP.get(KeyEvent.CTRL_DOWN_MASK)).append('+');
        }
        if ((modifiers & KeyEvent.ALT_DOWN_MASK) != 0) {
            sb.append(MODIFIER_MAP.get(KeyEvent.ALT_DOWN_MASK)).append('+');
        }
        if ((modifiers & KeyEvent.META_DOWN_MASK) != 0) {
            sb.append(MODIFIER_MAP.get(KeyEvent.META_DOWN_MASK)).append('+');
        }
        if ((modifiers & KeyEvent.BUTTON1_DOWN_MASK) != 0) {
            sb.append(MODIFIER_MAP.get(KeyEvent.BUTTON1_DOWN_MASK)).append('+');
        }
        if ((modifiers & KeyEvent.BUTTON2_DOWN_MASK) != 0) {
            sb.append(MODIFIER_MAP.get(KeyEvent.BUTTON2_DOWN_MASK)).append('+');
        }
        if ((modifiers & KeyEvent.BUTTON3_DOWN_MASK) != 0) {
            sb.append(MODIFIER_MAP.get(KeyEvent.BUTTON3_DOWN_MASK)).append('+');
        }
        if ((modifiers & KeyEvent.ALT_GRAPH_DOWN_MASK) != 0) {
            sb.append(MODIFIER_MAP.get(KeyEvent.ALT_GRAPH_DOWN_MASK)).append('+');
        }
        sb.append("VK_");
        sb.append(VK_MAP.get(vk.getVKCode()));
        return sb.toString();
    }
}

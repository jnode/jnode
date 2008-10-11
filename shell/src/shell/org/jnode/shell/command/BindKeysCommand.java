/*
 * $Id: AliasCommand.java 4198 2008-06-05 10:54:46Z crawley $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

import java.awt.event.KeyEvent;
import java.io.PrintWriter;
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
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.EnumArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * This command allows the user to examine and change JNode's key bindings.
 * 
 * @author crawley@jnode.org
 */
public class BindKeysCommand extends AbstractCommand {
    
    private static final String[] ASCII_NAMES = new String[] {
        "NUL", "SOH", "STC", "ETX", "EOT", "ENQ", "ACK", "BEL", 
        "BS", "HT", "NL", "VT", "FF", "CR", "SO", "SI", 
        "DLE", "DC1", "DC2", "DC3", "DC4", "NAK", "SYN", "ETB",
        "CAN", "EM", "SUB", "ESC", "FS", "GS", "RS", "US"
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

    private final FlagArgument FLAG_RESET = 
        new FlagArgument("reset", Argument.OPTIONAL, "reset the bindings to the default values");

    private final FlagArgument FLAG_ADD = 
        new FlagArgument("add", Argument.OPTIONAL, "add bindings");

    private final FlagArgument FLAG_REMOVE = 
        new FlagArgument("remove", Argument.OPTIONAL, "remove bindings");
    
    private final ActionArgument ARG_ACTION = 
        new ActionArgument("action", Argument.OPTIONAL, "an keyboard reader action");
    
    private final StringArgument ARG_VK_NAME = 
        new StringArgument("vkName", Argument.OPTIONAL + Argument.MULTIPLE, "a virtual key specification");
    
    private final StringArgument ARG_CHAR_NAME = 
        new StringArgument("charName", Argument.OPTIONAL + Argument.MULTIPLE, "a character");
    
    private PrintWriter out;
    private PrintWriter err;
    

    public BindKeysCommand() {
        super("display or change the keyboard bindings");
        registerArguments(FLAG_RESET, FLAG_ADD, FLAG_REMOVE, 
                ARG_ACTION, ARG_VK_NAME, ARG_CHAR_NAME);
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
        
        if (ARG_CHAR_NAME.isSet() || ARG_VK_NAME.isSet()) {
            // If character or virtual key names were supplied, remove only those bindings.
            if (ARG_CHAR_NAME.isSet()) {
                for (String charName : ARG_CHAR_NAME.getValues()) {
                    char ch = getCharacter(charName);
                    if (ch != KeyEvent.CHAR_UNDEFINED) {
                        bindings.unsetCharAction(ch);
                    } else {
                        err.println("Cannot translate character name '" + charName + "'");
                        exit(1);
                    }
                }
            } 
            if (ARG_VK_NAME.isSet()) {
                for (String vkName : ARG_VK_NAME.getValues()) {
                    VirtualKey vk = getVirtualKey(vkName);
                    if (vk != null) {
                        bindings.unsetVKAction(vk);
                    } else {
                        err.println("Cannot translate virtual key name '" + vkName + "'");
                        exit(1);
                    }
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

    private char getCharacter(String charName) {
        // TODO Auto-generated method stub
        return KeyEvent.CHAR_UNDEFINED;
    }

    private VirtualKey getVirtualKey(String vkName) {
        // TODO Auto-generated method stub
        return null;
    }

    private void addBindings(TextConsole console) {
        ConsoleKeyEventBindings bindings = console.getKeyEventBindings();
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
    
    private KeyboardReaderAction getAction(String name) {
        return KeyboardReaderAction.valueOf(name);
    }

    private String describe(char ch) {
        StringBuilder sb = new StringBuilder();
        if (ch < 0x1f) {
            sb.append("CTRL-" + (char)(ch + 0x40));
            sb.append(" (").append(ASCII_NAMES[ch]).append(")");
        } else if (ch == ' ') {
            sb.append("SPACE");
        } else if (ch == '\177') {
            sb.append("DEL");
        } else if (ch < '\177') {
            sb.append(ch);
        } else {
            sb.append(ch).append("(0x" + Integer.toHexString(ch)).append(')');
        }
        return sb.toString();
    }

    private String describe(VirtualKey vk) {
        if (vk.getModifiers() != 0) {
            return (KeyEvent.getKeyModifiersText(vk.getModifiers()) + " " +
                    KeyEvent.getKeyText(vk.getVKCode()));
        } else {
            return KeyEvent.getKeyText(vk.getVKCode());
        }
    }
}

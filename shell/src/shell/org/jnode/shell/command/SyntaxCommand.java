/*
 * $Id: SetCommand.java 3912 2008-03-30 02:24:36Z crawley $
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import org.jnode.nanoxml.XMLElement;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.syntax.AliasArgument;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.Syntax;
import org.jnode.shell.syntax.SyntaxArgumentMissingException;
import org.jnode.shell.syntax.SyntaxBundle;
import org.jnode.shell.syntax.SyntaxManager;
import org.jnode.shell.syntax.SyntaxSpecLoader;
import org.jnode.shell.syntax.XMLSyntaxSpecAdapter;


/**
 * Shell command to manage command syntaxes
 * 
 * @author crawley@jnode.org
 */
public class SyntaxCommand extends AbstractCommand {
    
    private final AliasArgument ARG_ALIAS = 
        new AliasArgument("alias", Argument.OPTIONAL, "the target alias");
    private FileArgument ARG_FILE = 
        new FileArgument("file", Argument.OPTIONAL, "if set, load the syntax from this file");
    private FlagArgument ARG_REMOVE =
        new FlagArgument("remove", Argument.OPTIONAL, "if set, remove the syntaxes for the target alias");
    private FlagArgument ARG_DUMP =
        new FlagArgument("dump", Argument.OPTIONAL, "if set, show the syntaxes for the target alias");
    private FlagArgument ARG_DUMP_ALL =
        new FlagArgument("dumpAll", Argument.OPTIONAL, "if set, show the syntaxes for all aliases");
    
    public SyntaxCommand() {
        super("manages syntaxes for commands using the 'new' syntax mechanisms");
        registerArguments(ARG_ALIAS, ARG_FILE, ARG_REMOVE, ARG_DUMP, ARG_DUMP_ALL);
    }

    public static void main(String[] args) throws Exception {
        new SyntaxCommand().execute(args);
    }

    public void execute()  throws Exception {
        PrintWriter out = getOutput().getPrintWriter();
        PrintWriter err = getError().getPrintWriter();
        SyntaxManager synMgr = ShellUtils.getCurrentSyntaxManager();
        if (ARG_DUMP_ALL.isSet()) {
            for (String alias : synMgr.getKeys()) {
                SyntaxBundle bundle = synMgr.getSyntaxBundle(alias);
                dumpSyntax(alias, bundle, out);
            }
        } else {
            String alias;
            if (ARG_DUMP.isSet()) {
                alias = getAlias();
                SyntaxBundle bundle = synMgr.getSyntaxBundle(alias);
                if (bundle == null) {
                    err.println("No syntax for alias '" + alias + "'");
                } else {
                    dumpSyntax(alias, bundle, out);
                }
            } else if (ARG_FILE.isSet()) {
                alias = getAlias();
                File file = ARG_FILE.getValue();
                XMLElement xml = new XMLElement();
                FileReader reader = null;
                try {
                    reader = new FileReader(file);
                    xml.parseFromReader(new BufferedReader(reader));
                    xml.setAttribute("alias", alias);
                    SyntaxBundle bundle = 
                        new SyntaxSpecLoader().loadSyntax(new XMLSyntaxSpecAdapter(xml));
                    synMgr.add(bundle);
                } catch (IOException ex) {
                    err.println("Cannot read file '" + file + "': " + ex.getMessage());
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            } else if (ARG_REMOVE.isSet()) {
                alias = getAlias();
                synMgr.remove(alias);
            } else {
                for (String key : synMgr.getKeys()) {
                    out.println(key);
                }
            }
        }
    }
    
    private String getAlias() throws SyntaxArgumentMissingException {
        if (!ARG_ALIAS.isSet()) {
            throw new SyntaxArgumentMissingException(
                    "an alias argument is required for this syntax", ARG_ALIAS);
        }
        return ARG_ALIAS.getValue();
    }

    private void dumpSyntax(String alias, SyntaxBundle bundle, PrintWriter out) 
        throws IOException {
        XMLElement element = new XMLElement(new Hashtable<String, Object>(), false, false);
        element.setName("syntax");
        element.setAttribute("alias", alias);
        String description = bundle.getDescription();
        if (description != null) {
            element.setAttribute("description", description);
        }
        Syntax[] syntaxes = bundle.getSyntaxes();
        for (Syntax syntax : syntaxes) {
            element.addChild(syntax.toXML());
        }
        element.write(out);
        out.println();
    }
    
}

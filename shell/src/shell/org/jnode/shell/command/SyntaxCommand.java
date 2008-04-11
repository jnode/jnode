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
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;

import nanoxml.XMLElement;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.syntax.Syntax;
import org.jnode.shell.syntax.AliasArgument;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.SyntaxArgumentMissingException;
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

    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) 
    throws Exception {
        SyntaxManager synMgr = ShellUtils.getCurrentSyntaxManager();

        if (ARG_DUMP_ALL.isSet()) {
            for (String alias : synMgr.getKeys()) {
                Syntax syntax = synMgr.getSyntax(alias);
                dumpSyntax(alias, syntax, out);
            }
        }
        else {
            if (!ARG_ALIAS.isSet()) {
                throw new SyntaxArgumentMissingException(
                        "alias argument required for these flags", ARG_ALIAS);
            }
            String alias = ARG_ALIAS.getValue();
            if (ARG_DUMP.isSet()) {
                Syntax syntax = synMgr.getSyntax(alias);
                if (syntax == null) {
                    err.println("No syntax for alias '" + alias + "'");
                }
                else {
                    dumpSyntax(alias, syntax, out);
                }
            }
            else if (ARG_FILE.isSet()) {
                File file = ARG_FILE.getValue();
                XMLElement xml = new XMLElement();
                FileReader reader = null;
                try {
                    reader = new FileReader(file);
                    xml.parseFromReader(new BufferedReader(reader));
                    Syntax syntax = new SyntaxSpecLoader().loadSyntax(new XMLSyntaxSpecAdapter(xml));
                    synMgr.add(alias, syntax);
                }
                catch (IOException ex) {
                    err.println("Cannot read file '" + file + "': " + ex.getMessage());
                }
                finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }
            else if (ARG_REMOVE.isSet()) {
                synMgr.remove(alias);
            }
            else {
                for (String key : synMgr.getKeys()) {
                    out.println(key);
                }
            }
        }
    }

    private void dumpSyntax(String alias, Syntax syntax, PrintStream out) 
    throws IOException {
        XMLElement element = syntax.toXML();
        element.setName("syntax");
        element.setAttribute("alias", alias);
        Writer writer = new OutputStreamWriter(out);
        element.write(writer);
        writer.flush();
        out.println();
    }
    
}

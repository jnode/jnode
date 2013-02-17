/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.command.system;

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
    
    private static final String help_alias = "the target alias";
    private static final String help_file = "if set, load the syntax from this file";
    private static final String help_remove = "if set, remove the syntaxes for the target alias";
    private static final String help_dump = "if set, show the syntaxes for the target alias";
    private static final String help_dump_all = "if set, show the syntaxes for all aliases";
    private static final String help_super = "Manage syntaxes for commands using the 'new' syntax mechanism";
    private static final String err_no_alias = "No syntax for alias '%s'%n";
    private static final String err_file_read = "Cannot read file '%s': %s%n";
    private static final String ex_syntax_alias = "An alias argument is required for this syntax";
    
    private final AliasArgument argAlias;
    private FileArgument argFile;
    private FlagArgument argRemove;
    private FlagArgument argDump;
    private FlagArgument argDumpAll;
    
    public SyntaxCommand() {
        super(help_super);
        argAlias   = new AliasArgument("alias", Argument.OPTIONAL, help_alias);
        argFile    = new FileArgument("file", Argument.OPTIONAL, help_file);
        argRemove  = new FlagArgument("remove", Argument.OPTIONAL, help_remove);
        argDump    = new FlagArgument("dump", Argument.OPTIONAL, help_dump);
        argDumpAll = new FlagArgument("dumpAll", Argument.OPTIONAL, help_dump_all);
        registerArguments(argAlias, argFile, argRemove, argDump, argDumpAll);
    }

    public static void main(String[] args) throws Exception {
        new SyntaxCommand().execute(args);
    }
    
    public void execute()  throws Exception {
        PrintWriter out = getOutput().getPrintWriter();
        PrintWriter err = getError().getPrintWriter();
        SyntaxManager synMgr = ShellUtils.getCurrentSyntaxManager();
        if (argDumpAll.isSet()) {
            for (String alias : synMgr.getKeys()) {
                SyntaxBundle bundle = synMgr.getSyntaxBundle(alias);
                dumpSyntax(alias, bundle, out);
            }
        } else {
            String alias;
            if (argDump.isSet()) {
                alias = getAlias();
                SyntaxBundle bundle = synMgr.getSyntaxBundle(alias);
                if (bundle == null) {
                    err.format(err_no_alias, alias);
                } else {
                    dumpSyntax(alias, bundle, out);
                }
            } else if (argFile.isSet()) {
                alias = getAlias();
                File file = argFile.getValue();
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
                    err.format(err_file_read, alias, ex.getLocalizedMessage());
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            } else if (argRemove.isSet()) {
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
        if (!argAlias.isSet()) {
            throw new SyntaxArgumentMissingException(ex_syntax_alias, argAlias);
        }
        return argAlias.getValue();
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

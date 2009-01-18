/*
 * $Id: NameSpace.java 4564 2008-09-18 22:01:10Z fduminy $
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
package org.jnode.emu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.naming.NamingException;

import org.jnode.emu.naming.BasicNameSpace;
import org.jnode.emu.plugin.model.DummyExtensionPoint;
import org.jnode.naming.InitialNaming;
import org.jnode.nanoxml.XMLElement;
import org.jnode.shell.ShellManager;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.def.DefaultAliasManager;
import org.jnode.shell.def.DefaultShellManager;
import org.jnode.shell.help.HelpFactory;
import org.jnode.shell.help.def.DefaultHelpFactory;
import org.jnode.shell.syntax.DefaultSyntaxManager;
import org.jnode.shell.syntax.SyntaxBundle;
import org.jnode.shell.syntax.SyntaxManager;
import org.jnode.shell.syntax.SyntaxSpecAdapter;
import org.jnode.shell.syntax.SyntaxSpecLoader;
import org.jnode.shell.syntax.XMLSyntaxSpecAdapter;

/**
 * This class is the core of a light-weight JNode emulator that allows (some) JNode
 * applications to be run using a classic JVM in the context of a JNode development sandbox.
 * 
 * @author Levente S\u00e1ntha
 * @author Stephen Crawley
 */
public abstract class Emu {
    private static final String[] ALL_PROJECTS = new String[]{
        "core", "distr", "fs", "gui", "net", "shell", "sound", "textui"
    };

    private static final String[] PLUGIN_NAMES = new String[]{
        "org.jnode.shell.command",
        "org.jnode.shell.command.driver.console",
        "org.jnode.apps.editor",
        "org.jnode.apps.edit",
        "org.jnode.apps.console",
    };

    /**
     * Initialize a minimal subset of JNode services to allow us to run JNode commands.
     *
     * @param root the notional JNode sandbox root directory or <code>null</code>.
     * @throws EmuException
     */
    public static void initEnv(File root) throws EmuException {
        if (true) {
            if (root == null) {
                root = new File("").getAbsoluteFile();
                System.err.println("Assuming that the JNode root is '" + root + "'");
            }
            InitialNaming.setNameSpace(new BasicNameSpace());

            try {
                InitialNaming.bind(DeviceManager.NAME, DeviceManager.INSTANCE);
                AliasManager aliasMgr =
                    new DefaultAliasManager(new DummyExtensionPoint()).createAliasManager();
                SyntaxManager syntaxMgr =
                    new DefaultSyntaxManager(new DummyExtensionPoint()).createSyntaxManager();
                for (String pluginName : PLUGIN_NAMES) {
                    configurePluginCommands(root, pluginName, aliasMgr, syntaxMgr);
                }
                System.setProperty("jnode.invoker", "thread");
                System.setProperty("jnode.interpreter", "redirecting");
                System.setProperty("jnode.debug", "true");
                InitialNaming.bind(AliasManager.NAME, aliasMgr);
                InitialNaming.bind(ShellManager.NAME, new DefaultShellManager());
                InitialNaming.bind(SyntaxManager.NAME, syntaxMgr);
                InitialNaming.bind(HelpFactory.NAME, new DefaultHelpFactory());
            } catch (NamingException ex) {
                throw new EmuException("Problem setting up InitialNaming bindings", ex);
            }
        }
    }

    /**
     * Configure any command classes specified by a given plugin's descriptor
     *
     * @param root       the root directory for the JNode sandbox.
     * @param pluginName the plugin to be processed
     * @param aliasMgr   the alias manager to be populated
     * @param syntaxMgr  the syntax manager to be populated
     * @throws EmuException
     */
    private static void configurePluginCommands(File root, String pluginName, AliasManager aliasMgr,
                                                SyntaxManager syntaxMgr) throws EmuException {
        XMLElement pluginDescriptor = loadPluginDescriptor(root, pluginName);
        extractAliases(pluginDescriptor, aliasMgr);
        extractSyntaxBundles(pluginDescriptor, syntaxMgr);
    }

    /**
     * Populate the supplied syntax manager with syntax entries from a plugin descriptor.
     *
     * @param pluginDescriptor the plugin descriptor's root XML element
     * @param syntaxMgr        the syntax manager to be populated.
     * @throws EmuException
     */
    private static void extractSyntaxBundles(XMLElement pluginDescriptor, SyntaxManager syntaxMgr)
        throws EmuException {
        XMLElement syntaxesDescriptor = findExtension(pluginDescriptor, SyntaxManager.SYNTAXES_EP_NAME);
        if (syntaxesDescriptor == null) {
            return;
        }
        SyntaxSpecLoader loader = new SyntaxSpecLoader();
        for (XMLElement syntaxDescriptor : syntaxesDescriptor.getChildren()) {
            if (!syntaxDescriptor.getName().equals("syntax")) {
                continue;
            }
            SyntaxSpecAdapter adaptedElement = new XMLSyntaxSpecAdapter(syntaxDescriptor);
            try {
                SyntaxBundle bundle = loader.loadSyntax(adaptedElement);
                if (bundle != null) {
                    syntaxMgr.add(bundle);
                }
            } catch (Exception ex) {
                throw new EmuException("problem in syntax", ex);
            }
        }
    }

    /**
     * Populate the supplied alias manager with aliases from a plugin descriptor.
     *
     * @param pluginDescriptor the plugin descriptor's root XML element
     * @param aliasMgr         the alias manager to be populated.
     * @throws EmuException
     */
    private static void extractAliases(XMLElement pluginDescriptor, AliasManager aliasMgr) {
        XMLElement aliasesDescriptor = findExtension(pluginDescriptor, AliasManager.ALIASES_EP_NAME);
        if (aliasesDescriptor == null) {
            return;
        }
        for (XMLElement aliasDescriptor : aliasesDescriptor.getChildren()) {
            if (aliasDescriptor.getName().equals("alias")) {
                String alias = aliasDescriptor.getStringAttribute("name");
                String className = aliasDescriptor.getStringAttribute("class");
                aliasMgr.add(alias, className);
            }
        }
    }

    /**
     * Locate the descriptor for a given extension point.
     *
     * @param pluginDescriptor the plugin descriptor
     * @param epName           the extension point's name
     * @return
     */
    private static XMLElement findExtension(XMLElement pluginDescriptor, String epName) {
        for (XMLElement child : pluginDescriptor.getChildren()) {
            if (child.getName().equals("extension") &&
                epName.equals(child.getStringAttribute("point"))) {
                return child;
            }
        }
        return null;
    }

    /**
     * Locate and load a plugin descriptor.  We search the "descriptors" directory of
     * each of the projects listed in ALL_PROJECTS
     *
     * @param root       the notional root directory for the user's JNode sandbox.
     * @param pluginName the name of the plugin we're trying to locate
     * @return the loaded plugin descriptor or <code>null</code>
     * @throws EmuException
     */
    private static XMLElement loadPluginDescriptor(File root, String pluginName)
        throws EmuException {
        File file = null;
        for (String projectName : ALL_PROJECTS) {
            file = new File(new File(new File(root, projectName), "descriptors"), pluginName + ".xml");
            if (file.exists()) {
                break;
            }
        }
        if (!file.exists()) {
            throw new EmuException("Cannot find plugin descriptor file for '" +
                pluginName + "': " + file.getAbsolutePath());
        }
        BufferedReader br = null;
        try {
            XMLElement elem = new XMLElement();
            br = new BufferedReader(new FileReader(file));
            elem.parseFromReader(br);
            if (!elem.getName().equals("plugin")) {
                throw new EmuException("File does not contain a 'plugin' descriptor: " + file);
            }
            return elem;
        } catch (IOException ex) {
            throw new EmuException("Problem reading / parsing plugin descriptor file " + file, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }
}

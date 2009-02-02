/*
 * $Id$
 *
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
 
package org.jnode.build.documentation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jnode.build.AbstractPluginTask;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.Library;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginPrerequisite;

/**
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class PluginDocumentationTask extends AbstractPluginTask {

    private final LinkedList<FileSet> descriptorSets = new LinkedList<FileSet>();

    private File destdir;

    private boolean tree = false;

    private final SortedMap<String, PluginData> descriptors = new TreeMap<String, PluginData>();

    private static final String DOCHEADER = "JNode plugin documentation";

    private static final String PLUGINS_SUBDIR = "plugins";

    private static final String EXT = ".html";

    private static final String ALL_FRAME = "allFrame";

    private static final String ALL_FILE = "all-frame" + EXT;

    private static final String OVERVIEW_SUMMARY_FILE = "overview-summary" + EXT;

    private static final String OVERVIEW_SUMMARY_FRAME = "overviewSummary";

    private static final String OVERVIEW_PACKAGE_FILE = "overview-package" + EXT;

    private static final String OVERVIEW_LICENSE_FILE = "overview-license" + EXT;

    private static final String OVERVIEW_TREE_FILE = "overview-tree" + EXT;

    private static final String OVERVIEW_TREE_DOTFILE = "overview-tree.dot";
    private static final String OVERVIEW_TREE_PNGFILE = "overview-tree.png";

    private static final String CSS_FILE = "index.css";

    private static final String INDEX = "index" + EXT;

    private static final ToolbarEntry[] TOOLBAR_ENTRIES = {
        new ToolbarEntry("Overview", OVERVIEW_SUMMARY_FILE),
        new ToolbarEntry("Java packages", OVERVIEW_PACKAGE_FILE),
        new ToolbarEntry("Licenses", OVERVIEW_LICENSE_FILE),
        new ToolbarEntry("Tree", OVERVIEW_TREE_FILE)};

    private static final LicenseEntry[] KNOWN_LICENSES = {
        new LicenseEntry("bsd",
            "http://opensource.org/licenses/bsd-license.php"),
        new LicenseEntry("gpl",
            "http://opensource.org/licenses/gpl-license.php"),
        new LicenseEntry("lgpl",
            "http://opensource.org/licenses/lgpl-license.php"),
        new LicenseEntry("cpl", "http://opensource.org/licenses/cpl1.0.php"),
        new LicenseEntry("classpath",
            "http://www.gnu.org/software/classpath/license.html"),
        new LicenseEntry("apache",
            "http://opensource.org/licenses/apachepl.php"),
        new LicenseEntry("apache2.0",
            "http://www.apache.org/licenses/LICENSE-2.0"),
        new LicenseEntry("zlib",
            "http://www.opensource.org/licenses/zlib-license.html")};

    public FileSet createDescriptors() {
        final FileSet fs = new FileSet();
        descriptorSets.add(fs);

        return fs;
    }

    /**
     * Get a list of all included descriptor files.
     *
     * @return
     */
    protected File[] getDescriptorFiles() {
        final List<File> files = new ArrayList<File>();
        for (FileSet fs : descriptorSets) {
            final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            final String[] filesNames = ds.getIncludedFiles();

            for (String filename : filesNames) {
                files.add(new File(ds.getBasedir(), filename));
            }
        }

        return files.toArray(new File[files.size()]);
    }

    /**
     * @throws org.apache.tools.ant.BuildException
     *
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        descriptors.clear();
        if (descriptorSets.isEmpty()) {
            throw new BuildException("At at least 1 descriptorset element");
        }

        if (destdir == null) {
            throw new BuildException("The todir attribute must be set");
        }

        if (getPluginDir() == null) {
            throw new BuildException("The pluginDir attribute must be set");
        }

        if (!destdir.exists()) {
            destdir.mkdirs();
        } else if (!destdir.isDirectory()) {
            throw new BuildException("destdir must be a directory");
        }

        try {
            // Load the plugin data
            for (File descrFile : getDescriptorFiles()) {
                loadPluginData(descriptors, descrFile);
            }

            // Write the plugin documentation
            for (PluginData data : descriptors.values()) {
                writePluginDocumentation(data);
            }

            // Write the index files
            writeCSS();
            writeAllFrame(descriptors, DOCHEADER);
            writeOverviewSummary(descriptors, DOCHEADER);
            writeOverviewPackage(descriptors, DOCHEADER);
            writeOverviewLicense(descriptors, DOCHEADER);
            if (isTree()) {
                writeOverviewTree(descriptors, DOCHEADER);
            }
            writeIndex(descriptors, DOCHEADER);
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }

    private void writeCSS() throws IOException {
        final File file = new File(getDestdir(), CSS_FILE);
        final PrintWriter out = new PrintWriter(new FileWriter(file));
        try {
            out.println("body { background-color: #FFFFFF }");
            out
                .println(".frameItem { font-size:  80%; font-family: Verdana, Arial, sans-serif }");
            out.println(".summaryTable { border: 1px solid; }");
            out
                .println(".summaryTableHdr { background: #CCCCFF; font-size: 120%; font-weight: bold; }");
            out
                .println(".toolbar { background: #EEEEFF; font-size: 110%; font-weight: bold; }");
            out.println(".toolbarItem:link { text-decoration: none; }");
            out.println(".toolbarItem:visited { text-decoration: none; }");
        } finally {
            out.close();
        }
    }

    private void writeIndex(Map<String, PluginData> descriptors, String title)
        throws IOException {
        final File file = new File(getDestdir(), INDEX);
        final PrintWriter out = new PrintWriter(new FileWriter(file));
        try {
            out.println("<html><head>");
            out.println("<title>" + title + "</title>");
            out.println("</head>");

            out.println("<frameset cols='20%,80%' title=''>");
            out.println("<frame src='" + ALL_FILE + "' name='" + ALL_FRAME
                + "' scrolling='yes'>");
            out.println("<frame src='" + OVERVIEW_SUMMARY_FILE + "' name='"
                + OVERVIEW_SUMMARY_FRAME + "' scrolling='yes'>");
            out.println("</frameset>");

            out.println("</body></html>");
        } finally {
            out.close();
        }
    }

    private void writeOverviewSummary(Map<String, PluginData> descriptors,
                                      String title) throws IOException {
        final File file = new File(getDestdir(), OVERVIEW_SUMMARY_FILE);
        final PrintWriter out = new PrintWriter(new FileWriter(file));
        try {
            out.println("<html><head>");
            out.println("<title>" + title + "</title>");
            out.println("<link rel='stylesheet' TYPE='text/css' href='"
                + CSS_FILE + "'>");
            out.println("</head><body>");

            addToolbar(out, "");

            addSummaryTableHdr(out, "Plugins");
            for (PluginData data : descriptors.values()) {
                final String link = "<a href='" + data.getHtmlFile() + "'>"
                    + data.getDescriptor().getId() + "</a>";
                addTableRow(out, link, data.getDescriptor().getName());
            }
            endSummaryTableHdr(out);
            addFooter(out);
            out.println("</body></html>");
        } finally {
            out.close();
        }
    }

    private void writeOverviewPackage(Map<String, PluginData> descriptors,
                                      String title) throws IOException {
        final File file = new File(getDestdir(), OVERVIEW_PACKAGE_FILE);
        final PrintWriter out = new PrintWriter(new FileWriter(file));
        try {
            out.println("<html><head>");
            out.println("<title>" + title + "</title>");
            out.println("<link rel='stylesheet' TYPE='text/css' href='"
                + CSS_FILE + "'>");
            out.println("</head><body>");

            addToolbar(out, "");

            // Build the packages list'
            final List<PackageData> pkgs = new ArrayList<PackageData>();
            for (PluginData data : descriptors.values()) {
                if (data.getDescriptor().getRuntime() != null) {
                    for (Library lib : data.getDescriptor().getRuntime()
                        .getLibraries()) {
                        for (String exp : lib.getExports()) {
                            if (!exp.equals("*")) {
                                if (exp.endsWith(".*")) {
                                    exp = exp.substring(0, exp.length() - 2);
                                }
                                pkgs.add(new PackageData(exp, data));
                            }
                        }
                    }
                }
            }
            Collections.sort(pkgs);

            addSummaryTableHdr(out, "Java packages");
            for (PackageData pkg : pkgs) {
                final PluginData data = pkg.getPlugin();
                final String link = "<a href='" + data.getHtmlFile() + "'>"
                    + data.getDescriptor().getId() + "</a>";
                addTableRow(out, pkg.getPackageName(), link);
            }
            endSummaryTableHdr(out);
            addFooter(out);

            out.println("</body></html>");
        } finally {
            out.close();
        }
    }

    /**
     * Generate an overview package that list all license and the plugins that
     * use that.
     *
     * @param descriptors
     * @param title
     * @throws IOException
     */
    private void writeOverviewLicense(Map<String, PluginData> descriptors,
                                      String title) throws IOException {
        final File file = new File(getDestdir(), OVERVIEW_LICENSE_FILE);
        final PrintWriter out = new PrintWriter(new FileWriter(file));
        try {
            out.println("<html><head>");
            out.println("<title>" + title + "</title>");
            out.println("<link rel='stylesheet' TYPE='text/css' href='"
                + CSS_FILE + "'>");
            out.println("</head><body>");

            addToolbar(out, "");

            // Build the packages list'
            final SortedMap<LicenseEntry, List<PluginData>> lics = new TreeMap<LicenseEntry, List<PluginData>>();
            for (PluginData data : descriptors.values()) {
                final LicenseEntry lic = findLicense(data.getDescriptor());
                List<PluginData> list = lics.get(lic);
                if (list == null) {
                    list = new ArrayList<PluginData>();
                    lics.put(lic, list);
                }
                list.add(data);
            }

            addSummaryTableHdr(out, "Licenses");
            for (Map.Entry<LicenseEntry, List<PluginData>> entry : lics.entrySet()) {
                final StringBuilder sb = new StringBuilder();
                for (PluginData data : entry.getValue()) {
                    final String link = "<a href='" + data.getHtmlFile() + "'>"
                        + data.getDescriptor().getId() + "</a><br/>";
                    sb.append(link);
                }
                addTableRow(out, entry.getKey().toHtmlString(), sb.toString());
            }
            endSummaryTableHdr(out);
            addFooter(out);

            out.println("</body></html>");
        } finally {
            out.close();
        }
    }

    /**
     * Generate an overview package that list all license and the plugins that
     * use that.
     *
     * @param descriptors
     * @param title
     * @throws IOException
     */
    private void writeOverviewTree(Map<String, PluginData> descriptors,
                                   String title) throws IOException {
        final File file = new File(getDestdir(), OVERVIEW_TREE_FILE);
        final File dotFile = new File(getDestdir(), OVERVIEW_TREE_DOTFILE);
        final File pngFile = new File(getDestdir(), OVERVIEW_TREE_PNGFILE);
        final PrintWriter out = new PrintWriter(new FileWriter(file));
        try {
            out.println("<html><head>");
            out.println("<title>" + title + "</title>");
            out.println("<link rel='stylesheet' TYPE='text/css' href='"
                + CSS_FILE + "'>");
            out.println("</head><body>");

            addToolbar(out, "");

            // Build dot file
            final DotBuilder dot = new DotBuilder(dotFile, pngFile);
            for (PluginData data : descriptors.values()) {
                dot.add(data);
            }
            dot.close();

            addSummaryTableHdr(out, "Tree");
            addTableRow(out, "<img src='" + OVERVIEW_TREE_PNGFILE + "'>");
            endSummaryTableHdr(out);
            addFooter(out);

            out.println("</body></html>");
        } finally {
            out.close();
        }
    }

    private void writeAllFrame(Map<String, PluginData> descriptors, String title)
        throws IOException {
        final File file = new File(getDestdir(), ALL_FILE);
        final PrintWriter out = new PrintWriter(new FileWriter(file));
        try {
            out.println("<html><head>");
            out.println("<title>" + title + "</title>");
            out.println("<link rel='stylesheet' TYPE='text/css' href='"
                + CSS_FILE + "'>");
            out.println("</head><body>");

            out.println("<table border='0' width='100%'><tr><td nowrap>");
            out.println("<b>All plugins</a>");
            out.println("</td></tr></table><p/>");

            out.println("<table border='0' width='100%'><tr><td nowrap>");
            for (PluginData data : descriptors.values()) {
                out.println("<a class='frameItem' href='" + data.getHtmlFile()
                    + "' target='" + OVERVIEW_SUMMARY_FRAME + "'>");
                out.println(data.getDescriptor().getId());
                out.println("</a><br/>");
            }
            out.println("</td></tr></table>");

            out.println("</body></html>");
        } finally {
            out.close();
        }
    }

    private void loadPluginData(Map<String, PluginData> descriptors,
                                File descrFile) {
        final PluginDescriptor descr = readDescriptor(descrFile);
        final String fullId = descr.getId() + "_" + descr.getVersion();

        if (descriptors.containsKey(fullId)) {
            final PluginData otherData = descriptors.get(fullId);
            throw new BuildException("Same id(" + fullId + ") for 2 plugins: "
                + otherData.getDescriptorFile() + ", " + descrFile);
        }

        // Create & store plugin data
        final PluginData data = new PluginData(descrFile, fullId);
        data.setDescriptor(descr);
        final String fname = PLUGINS_SUBDIR + "/" + descr.getId() + EXT;
        data.setHtmlFile(fname);

        descriptors.put(fullId, data);
    }

    /**
     * Build the documentation for the plugin
     *
     * @param data the plugin's data
     * @throws BuildException
     * @throws IOException
     * @throws SecurityException
     */
    protected void writePluginDocumentation(PluginData data)
        throws BuildException, IOException {

        final File file = new File(getDestdir(), data.getHtmlFile());
        file.getParentFile().mkdirs();
        final PrintWriter out = new PrintWriter(new FileWriter(file));
        try {
            final PluginDescriptor descr = data.getDescriptor();

            out.println("<html><head>");
            out.println("<title>" + descr.getId() + "</title>");
            out.println("<link rel='stylesheet' TYPE='text/css' href='../"
                + CSS_FILE + "'>");
            out.println("</head><body>");

            addToolbar(out, "../");

            final String title;
            if (descr.isFragment()) {
                title = "Fragment summary";
            } else {
                title = "Plugin summary";
            }

            addSummaryTableHdr(out, title);
            addTableRow(out, "Id", descr.getId());
            addTableRow(out, "Name", descr.getName());
            addTableRow(out, "Version", descr.getVersion());
            addTableRow(out, "Provider", formatProvider(descr));
            addTableRow(out, "License", formatLicense(descr));
            addTableRow(out, "Plugin class",
                descr.hasCustomPluginClass() ? descr
                    .getCustomPluginClassName() : "-");
            addTableRow(out, "Flags", formatFlags(descr));
            endSummaryTableHdr(out);

            if (descr.getPrerequisites().length > 0) {
                addSummaryTableHdr(out, "Requires");
                for (PluginPrerequisite prereq : descr.getPrerequisites()) {
                    final String href = prereq.getPluginId() + EXT;
                    final PluginData prereqData = getPluginData(prereq
                        .getPluginId());
                    final String name = (prereqData != null) ? prereqData
                        .getDescriptor().getName() : "?";
                    addTableRow(out, "<a href='" + href + "'>"
                        + prereq.getPluginId() + "</a>", name);
                }
                endSummaryTableHdr(out);
            }

            final List<PluginData> requiredBy = getRequiredBy(descr.getId());
            if (!requiredBy.isEmpty()) {
                addSummaryTableHdr(out, "Required by");
                for (PluginData reqBy : requiredBy) {
                    final String id = reqBy.getDescriptor().getId();
                    final String href = id + EXT;
                    final String name = reqBy.getDescriptor().getName();
                    addTableRow(out, "<a href='" + href + "'>" + id + "</a>",
                        name);
                }
                endSummaryTableHdr(out);
            }

            if (descr.getRuntime() != null) {
                final String lib = "Library";
                final String exports = "Exports packages";

                addSummaryTableHdr(out, "Libraries");
                for (Library library : descr.getRuntime().getLibraries()) {
                    final String libName = library.getName();
                    final StringBuilder sb = new StringBuilder();

                    for (String export : library.getExports()) {
                        sb.append(export);
                        sb.append("<br/>");
                    }
                    addTableRow(out, libName, sb.toString());
                }
                endSummaryTableHdr(out);
            }

            final ExtensionPoint[] epList = descr.getExtensionPoints();
            if ((epList != null) && (epList.length > 0)) {
                addSummaryTableHdr(out, "Extension points");
                for (ExtensionPoint ep : epList) {
                    addTableRow(out, ep.getSimpleIdentifier(), ep.getName());
                }
                endSummaryTableHdr(out);
            }

            final Extension[] extList = descr.getExtensions();
            if ((extList != null) && (extList.length > 0)) {
                addSummaryTableHdr(out, "Extensions");
                for (Extension ext : extList) {
                    final StringBuilder sb = new StringBuilder();

                    for (ConfigurationElement cfg : ext
                        .getConfigurationElements()) {
                        format(cfg, sb, "");
                        sb.append("<br/>");
                    }
                    addTableRow(out, format(ext), sb.toString());
                }
                endSummaryTableHdr(out);
            }

            addFooter(out);
            out.println("</body></html>");
        } finally {
            out.close();
        }
    }

    private void format(ConfigurationElement cfg, StringBuilder out,
                        String indent) {
        final ConfigurationElement[] children = cfg.getElements();
        final boolean hasChildren = ((children != null) && (children.length > 0));

        out.append(indent);
        out.append("&lt;");
        out.append(cfg.getName());

        for (String key : cfg.attributeNames()) {
            out.append(' ');
            out.append(key);
            out.append("='<i>");
            out.append(cfg.getAttribute(key));
            out.append("</i>\'");
        }

        if (hasChildren) {
            out.append("&gt;");
            for (ConfigurationElement child : children) {
                format(child, out, indent + "&nbs&nbsp;");
                out.append("<br/>");
            }
        } else {
            out.append("/&gt;");
        }
    }

    private String format(Extension ext) {
        final PluginData epPlugin = getPluginData(ext
            .getExtensionPointPluginId());
        if (epPlugin != null) {
            final String href = ext.getExtensionPointPluginId() + EXT;
            return "<a href='" + href + "'>"
                + ext.getExtensionPointUniqueIdentifier() + "</a>";
        } else {
            return ext.getExtensionPointUniqueIdentifier();
        }
    }

    private String formatFlags(PluginDescriptor descr) {
        final StringBuilder sb = new StringBuilder();
        if (descr.isSystemPlugin()) {
            sb.append("system");
        }
        if (descr.isAutoStart()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("auto-start");
        }
        if (sb.length() > 0) {
            return sb.toString();
        } else {
            return "-";
        }
    }

    private LicenseEntry findLicense(PluginDescriptor descr) {
        final String name = descr.getLicenseName();
        final String url = descr.getLicenseUrl();

        if ((name != null) && (url != null)) {
            return new LicenseEntry(name, url);
        } else if (name != null) {
            for (LicenseEntry lic : KNOWN_LICENSES) {
                if (lic.getName().equalsIgnoreCase(name)) {
                    return lic;
                }
            }
            return new LicenseEntry(name, null);
        } else {
            return LicenseEntry.UNKNOWN;
        }

    }

    private String formatLicense(PluginDescriptor descr) {
        return findLicense(descr).toHtmlString();
    }

    private String formatProvider(PluginDescriptor descr) {
        final String name = descr.getProviderName();
        final String url = descr.getProviderUrl();

        if ((name != null) && (url != null)) {
            return "<a href='" + url + "'>" + name + "</a>";
        } else if (name != null) {
            return name;
        } else {
            return "-";
        }
    }

    private void addSummaryTableHdr(PrintWriter out, String title) {
        out
            .println("<table class='summaryTable' border='1' cellpadding='3' cellspacing='0' width='100%'>");
        out.println("<tr><td class='summaryTableHdr' colspan='2'>");
        out.println(title);
        out.println("</td></tr>");
    }

    private void addToolbar(PrintWriter out, String base) {
        out
            .println("<table class='toolbar' border='0' cellpadding='3' cellspacing='0' width='100%'>");
        out.println("<tr>");
        for (ToolbarEntry tbe : TOOLBAR_ENTRIES) {
            if (!tbe.getHref().equals(OVERVIEW_TREE_FILE) || isTree()) {
                out.print("<td nowrap>");
                out.print("<a href='" + base + tbe.getHref()
                    + "' class='toolbarItem'>");
                out.print(tbe.getTitle());
                out.println("</a></td>");
            }
        }
        out.println("</td></tr></table>");
        out.println("<p/>");
    }

    private void addFooter(PrintWriter out) {
        out.println("<hr>");
        out.println("<font size='-1'>");
        out.println("This file is generated on " + new Date());
        out.println("<p/>");
        out
            .println("For more info visit <a href='http://jnode.org' target='_top'>http://jnode.org</a>");
        out.println("</font>");
    }

    private void endSummaryTableHdr(PrintWriter out) {
        out.println("</table>");
        out.println("<p/>");
    }

    private void addTableRow(PrintWriter out, String... cols) {
        out.println("<tr>");
        for (String col : cols) {
            out.println("<td valign='top'>");
            out.println(col);
            out.println("</td>");
        }
        out.println("</tr>");
    }

    /**
     * @return Returns the destdir.
     */
    public final File getDestdir() {
        return this.destdir;
    }

    /**
     * @param destdir The destdir to set.
     */
    public final void setDestdir(File destdir) {
        this.destdir = destdir;
    }

    private PluginData getPluginData(String id) {
        for (PluginData data : descriptors.values()) {
            if (data.getDescriptor().getId().equals(id)) {
                return data;
            }
        }
        return null;
    }

    /**
     * Gets a list of all plugins that are require the given plugin id.
     *
     * @param id
     * @return
     */
    private List<PluginData> getRequiredBy(String id) {
        final ArrayList<PluginData> list = new ArrayList<PluginData>();
        for (PluginData data : descriptors.values()) {
            final PluginPrerequisite[] reqs = data.getDescriptor()
                .getPrerequisites();
            if ((reqs != null) && (reqs.length > 0)) {
                for (PluginPrerequisite req : reqs) {
                    if (req.getPluginId().equals(id)) {
                        list.add(data);
                        break;
                    }
                }
            }
        }
        return list;
    }

    /**
     * @return Returns the tree.
     */
    public final boolean isTree() {
        return tree;
    }

    /**
     * @param tree The tree to set.
     */
    public final void setTree(boolean tree) {
        this.tree = tree;
    }
}

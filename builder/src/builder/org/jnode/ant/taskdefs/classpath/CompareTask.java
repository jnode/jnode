/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.ant.taskdefs.classpath;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;

/**
 * Task used to compare the latest classpath version against the latest jnode
 * version of classpath.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CompareTask extends Task {

    private BaseDirs vmDirs;

    private BaseDirs classpathDirs;

    private BaseDirs vmSpecificDirs;

    private File destDir;

    private String vmSpecificTag = "@vm-specific";

    private String classpathBugfixTag = "@classpath-bugfix";

    private static final int NO_CHANGE = 0x01;

    private static final int NEEDS_MERGE = 0x02;

    private static final int FLAGS_MASK = 0xFF00;

    private static final int FLAG_NATIVE = 0x0100;

    private static final int FLAG_VM_SPECIFIC = 0x0200;

    private static final int FLAG_CLASSPATH_BUGFIX = 0x0400;

    private static final int FLAG_JNODE = 0x0800;

    private static final int FLAG_TARGET_DIFF = 0x1000;

    public void execute() {
        if (destDir == null) {
            throw new BuildException("The destdir attribute must be set");
        }
        final Map<String, SourceFile> vmFiles = vmDirs
                .scanJavaFiles(getProject());
        final Map<String, SourceFile> classpathFiles = classpathDirs
                .scanJavaFiles(getProject());
        final Map<String, SourceFile> vmSpecificFiles = vmSpecificDirs
                .scanJavaFiles(getProject());
        final TreeSet<String> allFiles = new TreeSet<String>();
        final Map<String, String> packageDiffs = new TreeMap<String, String>();
        allFiles.addAll(vmFiles.keySet());
        allFiles.addAll(classpathFiles.keySet());

        try {
            destDir.mkdirs();
            final File outFile = new File(destDir, "classpath-compare.html");
            final PrintWriter out = new PrintWriter(new FileWriter(outFile));
            reportHeader(out);
            int missingInCp = 0;
            int missingInVm = 0;
            int needsMerge = 0;
            int diffVmSpecific = 0;
            int diffClasspathBugfix = 0;
            int diffNative = 0;
            int diffJNode = 0;
            int vmSpecific = 0;

            for (String name : allFiles) {
                SourceFile cpFile = classpathFiles.get(name);
                final SourceFile vmFile = vmFiles.get(name);
                final SourceFile vmSpecificFile = vmSpecificFiles
                        .get(name);

                if (vmSpecificFile != null) {
                    // File is found as vm specific source
                    reportVmSpecific(out, vmSpecificFile.getReportName(),
                            "vm-specific");
                    vmSpecific++;
                } else if (vmFile == null) {
                    // file is not found as vmspecific source, nor as vm source
                    if (!cpFile.isIgnoreMissing()) {
                        reportMissing(out, cpFile.getReportName(), "classpath",
                                getFlags(cpFile));
                        missingInCp++;
                    }
                } else if (cpFile == null) {
                    // File is not found in classpath sources
                    reportMissing(out, vmFile.getReportName(), "vm", 0);
                    missingInVm++;
                } else {
                    // We have both the classpath version and the vm version.
                    cpFile = cpFile.getBestFileForTarget(vmFile.getTarget());

                    final String diffFileName = vmFile.getReportName() + ".diff";
                    int rc = runDiff(vmFile, cpFile, diffFileName, packageDiffs);
                    switch (rc & ~FLAGS_MASK) {
                    case NO_CHANGE:
                        break;
                    case NEEDS_MERGE:
                        reportNeedsMerge(out, vmFile.getReportName(), vmFile
                                .getTarget(), diffFileName, rc & FLAGS_MASK);
                        needsMerge++;
                        break;
                    default:
                        throw new RuntimeException("Invalid rc " + rc);
                    }
                    if ((rc & FLAG_VM_SPECIFIC) != 0) {
                        diffVmSpecific++;
                    }
                    if ((rc & FLAG_CLASSPATH_BUGFIX) != 0) {
                        diffClasspathBugfix++;
                    }
                    if ((rc & FLAG_NATIVE) != 0) {
                        diffNative++;
                    }
                    if ((rc & FLAG_JNODE) != 0) {
                        diffJNode++;
                    }
                    // Let's compare them
                }
            }

            // Package diffs
            for (Map.Entry<String, String> entry : packageDiffs.entrySet()) {
                final String pkg = entry.getKey();
                final String diff = entry.getValue();
                final String diffFileName = pkg + ".pkgdiff";
                processPackageDiff(diffFileName, pkg, diff);
                reportPackageDiff(out, pkg, diffFileName, getFlags(diff));
            }

            out.println("</table><p/>");

            // Summary
            out.println("<a name='summary'/><h2>Summary</h2>");
            if (missingInCp > 0) {
                out.println("Found " + missingInCp
                        + " files missing in classpath</br>");
                log("Found " + missingInCp + " files missing in classpath");
            }
            if (missingInVm > 0) {
                out.println("Found " + missingInVm
                        + " files missing in vm<br/>");
                log("Found " + missingInVm + " files missing in vm");
            }
            if (needsMerge > 0) {
                out.println("Found " + needsMerge
                        + " files that needs merging<br/>");
                log("Found " + needsMerge + " files that needs merging");
            }
            if (diffVmSpecific > 0) {
                out.println("Found " + diffVmSpecific
                        + " VM specific differences<br/>");
                log("Found " + diffVmSpecific + " VM specific differences");
            }
            if (vmSpecific > 0) {
                out.println("Found " + vmSpecific + " VM specific files<br/>");
                log("Found " + vmSpecific + " VM specific files");
            }
            if (diffClasspathBugfix > 0) {
                out.println("Found " + diffClasspathBugfix
                        + " local classpath bugfixes<br/>");
                log("Found " + diffClasspathBugfix
                        + " local classpath bugfixes");
            }
            if (diffNative > 0) {
                out.println("Found " + diffNative
                        + " changes with native in it<br/>");
                log("Found " + diffNative + " changes with native in it");
            }
            if (diffJNode > 0) {
                out.println("Found " + diffJNode
                        + " changes with JNode in it<br/>");
                log("Found " + diffJNode + " changes with JNode in it");
            }

            reportFooter(out);
            out.flush();
            out.close();
        } catch (IOException ex) {
            throw new BuildException(ex);
        } catch (InterruptedException ex) {
            throw new BuildException(ex);
        }
    }

    protected int runDiff(SourceFile vmFile, SourceFile cpFile,
            String diffFileName, Map<String, String> packageDiffs)
            throws IOException, InterruptedException {
        final String[] cmd = { "diff", "-b", // Ignore white space change
                "-au", "-I", ".*$" + "Id:.*$.*", // Avoid cvs keyword
                                                    // expansion in this string
                vmFile.getFileName(), cpFile.getFile().getAbsolutePath() };
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ByteArrayOutputStream err = new ByteArrayOutputStream();
        final PumpStreamHandler streamHandler = new PumpStreamHandler(out, err);
        final Execute exe = new Execute(streamHandler);
        exe.setCommandline(cmd);
        exe.setWorkingDirectory(vmFile.getBaseDir());
        final int rc = exe.execute();
        if (rc != 0) {
            File diffFile = new File(destDir, diffFileName);
            FileOutputStream os = new FileOutputStream(diffFile);
            try {
                final byte[] diff = out.toByteArray();
                os.write(diff);
                os.flush();

                final String diffStr = new String(diff);
                final String pkg = vmFile.getPackageName();
                String pkgDiff;
                if (packageDiffs.containsKey(pkg)) {
                    pkgDiff = packageDiffs.get(pkg);
                    pkgDiff = pkgDiff + "diff\n" + diffStr;
                } else {
                    pkgDiff = diffStr;
                }
                packageDiffs.put(pkg, pkgDiff);

                int flags = getFlags(diffStr);
                if (!vmFile.getTarget().equals(cpFile.getTarget())) {
                    flags |= FLAG_TARGET_DIFF;
                }
                return flags | NEEDS_MERGE;
            } finally {
                os.close();
            }
        } else {
            return NO_CHANGE;
        }
    }

    private void processPackageDiff(String diffFileName, String pkg, String diff)
            throws IOException {
        File diffFile = new File(destDir, diffFileName);
        FileWriter os = new FileWriter(diffFile);
        try {
            os.write(diff);
            os.flush();
        } finally {
            os.close();
        }
    }

    protected void reportHeader(PrintWriter out) {
        out.println("<html>");
        out.println("<title>Classpath compare</title>");
        out.println("<style type='text/css'>");
        out.println(".classpath-only     { background-color: #FFFFAA; }");
        out.println(".vm-only            { background-color: #CCCCFF; }");
        out.println(".needsmerge         { background-color: #FF9090; }");
        out.println(".vm-specific        { background-color: #119911; }");
        out.println(".vm-specific-source { background-color: #22FF22; }");
        out.println(".classpath-bugfix   { background-color: #CCFFCC; }");
        out.println("</style>");
        out.println("<body>");
        out.println("<h1>Classpath compare results</h1>");
        out.println("Created at " + new Date());
        out.println("<table border='1' width='100%' style='border: solid 1'>");
        out.println("<tr>");
        out.println("<th align='left'>Class</th>");
        out.println("<th align='left'>Target</th>");
        out.println("<th align='left'>Merge status</th>");
        out.println("</tr>");
        out.flush();
    }

    protected void reportMissing(PrintWriter out, String fname,
            String existsIn, int flags) {
        out.println("<tr class='" + existsIn + "-only'>");
        out.println("<td>" + fname + "</td>");
        out.println("<td>&nbsp;</td>");
        out.println("<td>Exists only in " + existsIn);
        reportFlags(out, flags);
        out.println("</td>");
        out.println("</tr>");
        out.flush();
    }

    protected void reportVmSpecific(PrintWriter out, String fname,
            String existsIn) {
        out.println("<tr class='vm-specific-source'>");
        out.println("<td>" + fname + "</td>");
        out.println("<td>&nbsp;</td>");
        out.println("<td>VM specific source");
        out.println("</td>");
        out.println("</tr>");
        out.flush();
    }

    protected void reportNeedsMerge(PrintWriter out, String fname,
            String target, String diffFileName, int flags) {
        out.println("<tr class='" + flagsToStyleClass(flags) + "'>");
        out.println("<td>" + fname + "</td>");
        if (target.equals(TargetedFileSet.DEFAULT_TARGET)) {
            target = "&nbsp;";
        }
        out.println("<td>" + target + "</td>");
        out.println("<td><a href='" + diffFileName + "'>Diff</a>");
        reportFlags(out, flags);
        out.println("</td>");
        out.println("</tr>");
        out.flush();
    }

    protected void reportPackageDiff(PrintWriter out, String pkg,
            String diffFileName, int flags) {
        out.println("<tr class='needsmerge'>");
        out.println("<td>" + pkg + "</td>");
        out.println("<td>&nbsp;</td>");
        out.println("<td><a href='" + diffFileName + "'>diff</a>");
        reportFlags(out, flags);
        out.println("</td>");
        out.println("</tr>");
        out.flush();
    }

    protected void reportFooter(PrintWriter out) {
        out.println("</body></html>");
        out.flush();
    }

    protected String flagsToStyleClass(int flags) {
        if ((flags & FLAG_VM_SPECIFIC) != 0) {
            return "vm-specific";
        } else if ((flags & FLAG_CLASSPATH_BUGFIX) != 0) {
            return "classpath-bugfix";
        } else {
            return "needsmerge";
        }
    }

    protected void reportFlags(PrintWriter out, int flags) {
        final StringBuffer b = new StringBuffer();
        if ((flags & FLAG_TARGET_DIFF) != 0) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append("different target");
        }
        if ((flags & FLAG_VM_SPECIFIC) != 0) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append("vm-specific");
        }
        if ((flags & FLAG_CLASSPATH_BUGFIX) != 0) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append("cp-bugfix");
        }
        if ((flags & FLAG_NATIVE) != 0) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append("native");
        }
        if ((flags & FLAG_JNODE) != 0) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append("jnode");
        }

        if (b.length() > 0) {
            out.println(" <i>(");
            out.println(b.toString());
            out.println(")</i>");
        }
    }

    protected int getFlags(String code) {
        int flags = 0;
        if (code.indexOf("native") >= 0) {
            flags |= FLAG_NATIVE;
        }
        if (code.toLowerCase().indexOf("jnode") >= 0) {
            flags |= FLAG_JNODE;
        }
        if (code.indexOf(vmSpecificTag) >= 0) {
            flags |= FLAG_VM_SPECIFIC;
        }
        if (code.indexOf(classpathBugfixTag) >= 0) {
            flags |= FLAG_CLASSPATH_BUGFIX;
        }
        return flags;
    }

    protected int getFlags(SourceFile file) throws IOException {
        final FileReader fr = new FileReader(file.getFile());
        try {
            final BufferedReader in = new BufferedReader(fr);
            final StringBuffer b = new StringBuffer();
            String line;
            while ((line = in.readLine()) != null) {
                b.append(line);
                b.append('\n');
            }
            return getFlags(b.toString());
        } finally {
            fr.close();
        }
    }

    public BaseDirs createVmsources() {
        if (vmDirs == null) {
            vmDirs = new BaseDirs();
        }
        return vmDirs;
    }

    public BaseDirs createVmspecificsources() {
        if (vmSpecificDirs == null) {
            vmSpecificDirs = new BaseDirs();
        }
        return vmSpecificDirs;
    }

    public BaseDirs createClasspathsources() {
        if (classpathDirs == null) {
            classpathDirs = new BaseDirs();
        }
        return classpathDirs;
    }

    /**
     * @return Returns the destDir.
     */
    public final File getDestDir() {
        return this.destDir;
    }

    /**
     * @param destDir
     *            The destDir to set.
     */
    public final void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    /**
     * @param classpathBugfixTag
     *            The classpathBugfixTag to set.
     */
    public final void setClasspathBugfixTag(String classpathBugfixTag) {
        this.classpathBugfixTag = classpathBugfixTag;
    }

    /**
     * @param vmSpecificTag
     *            The vmSpecificTag to set.
     */
    public final void setVmSpecificTag(String vmSpecificTag) {
        this.vmSpecificTag = vmSpecificTag;
    }
}

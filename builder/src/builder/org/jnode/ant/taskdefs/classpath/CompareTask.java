/*
 * $Id$
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

    private String type;

    private String vmSpecificTag = "@vm-specific";

    // represent an unsubmitted classpath bugfix 
    // (if not matching the tag 'classpathBugIDTag')
    private String classpathBugfixTag = "@classpath-bugfix";
    
    private String classpathBugfixEndTag = "@classpath-bugfix-end";

    // represent a submitted classpath bugfix (followed by an ID) 
    // this tag is immediately followed by the classpath bugzilla's identifier
    private String classpathBugIDTag = "@classpath-bugfix-";
    
    // url of Classpath Bugzilla
    private final String CP_BUGZILLA_URL = "http://gcc.gnu.org/bugzilla/show_bug.cgi?id=";
    
    private String jnodeMail = "@jnode.org";
    
    private static final int NO_CHANGE = 0x01;

    private static final int NEEDS_MERGE = 0x02;

    private static final int FLAGS_MASK = 0xFF00;

    private static final int FLAG_NATIVE = 0x0100;

    private static final int FLAG_VM_SPECIFIC = 0x0200;

    private static final int FLAG_CLASSPATH_BUGFIX = 0x0400;

    private static final int FLAG_JNODE = 0x0800;

    private static final int FLAG_TARGET_DIFF = 0x1000;
    
    private static final int FLAG_UNSUBMITTED_CLASSPATH_BUGFIX = 0x2000;

    public void execute() {
        if (destDir == null)
            throw new BuildException("The destdir attribute must be set");

        if (type == null)
            throw new BuildException("The type attribute must be set");


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
            final File outBugsFile = new File(destDir, "bugfix.html");
            final PrintWriter outBugs = new PrintWriter(new FileWriter(outBugsFile));
            reportHeader(outBugs, "Class", "Target", "Classpath bugs");
            
            final File outFile = new File(destDir, "index.html");
            final PrintWriter out = new PrintWriter(new FileWriter(outFile));
            reportHeader(out, "Class", "Target", "Merge status");
            
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
                        reportMissing(out, cpFile.getReportName(), type,
                                getFlags(cpFile));
                        missingInVm++;
                    }
                } else if (cpFile == null) {
                    // File is not found in classpath sources
                    reportMissing(out, vmFile.getReportName(), "vm", new Flags());
                    missingInCp++;
                } else {
                    // We have both the classpath version and the vm version.
                    cpFile = cpFile.getBestFileForTarget(vmFile.getTarget());

                    // Let's compare them                    
                    final String diffFileName = vmFile.getReportName() + ".diff";
                    Flags rc = runDiff(vmFile, cpFile, diffFileName, packageDiffs);
                    switch (rc.asInt() & ~FLAGS_MASK) {
                    case NO_CHANGE:
                        break;
                    case NEEDS_MERGE:
                        reportNeedsMerge(out, vmFile.getReportName(), vmFile
                                .getTarget(), diffFileName, rc.mask(FLAGS_MASK));
                        needsMerge++;
                        break;
                    default:
                        throw new RuntimeException("Invalid rc " + rc);
                    }
                    if (rc.isSet(FLAG_VM_SPECIFIC)) {
                        diffVmSpecific++;
                    }
                    if (rc.isSet(FLAG_CLASSPATH_BUGFIX)) {
                        diffClasspathBugfix++;
                    }
                    if (rc.isSet(FLAG_NATIVE)) {
                        diffNative++;
                    }
                    if (rc.isSet(FLAG_JNODE)) {
                        diffJNode++;
                    }
                    
                    reportClasspathBugs(outBugs, vmFile.getReportName(), vmFile
                            .getTarget(), rc);
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
                        + " files missing in " + type + "</br>");
                log("Found " + missingInCp + " files missing in " + type);
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
                        + " local <a href=\"bugfix.html\">classpath bugfixes</a><br/>");
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
            
            reportFooter(outBugs);
            outBugs.flush();
            outBugs.close();            
        } catch (IOException ex) {
            throw new BuildException(ex);
        } catch (InterruptedException ex) {
            throw new BuildException(ex);
        }
    }

    protected Flags runDiff(SourceFile vmFile, SourceFile cpFile,
            String diffFileName, Map<String, String> packageDiffs)
            throws IOException, InterruptedException {
        final String[] cmd = { "diff", 
                "-b", // Ignore white space change
                //"-E", // Ignore changes due to tab expansion
                //"-w", // Ignore all white space change
                //"-B", // Ignore changes whose lines are all blank
                //"-N", // Treat absent files as empty
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
        if ((rc != 0) && (out.size() > 0)) {
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

                Flags flags = getFlags(diffStr);
                if (!vmFile.getTarget().equals(cpFile.getTarget())) {
                    flags.set(FLAG_TARGET_DIFF);
                }
                flags.set(NEEDS_MERGE);
                return flags;
            } finally {
                os.close();
            }
        } else {
            return new Flags(NO_CHANGE);
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

    protected void reportHeader(PrintWriter out, String... headers) {
        String capital_type = type.substring(0,1).toUpperCase() + type.substring(1);
        out.println("<html>");
        out.println("<title>" + capital_type + " compare</title>");
        out.println("<style type='text/css'>");
        out.println("." + type + "-only         { background-color: #FFFFAA; }");
        out.println(".vm-only                { background-color: #CCCCFF; }");
        out.println(".needsmerge             { background-color: #FF9090; }");
        out.println(".vm-specific            { background-color: #119911; }");
        out.println(".vm-specific-source     { background-color: #22FF22; }");
        out.println(".classpath-bugfix       { background-color: #CCFFCC; }");
        out.println("</style>");
        out.println("<body>");
        out.println("<h1>" + capital_type + " compare results</h1>");
        out.println("Created at " + new Date());
        out.println("<table border='1' width='100%' style='border: solid 1'>");
        out.println("<tr>");
        for(String header : headers)
        {
            out.println("<th align='left'>"+header+"</th>");
        }
        out.println("</tr>");
        out.flush();
    }

    protected void reportMissing(PrintWriter out, String fname,
            String existsIn, Flags flags) {
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
            String target, String diffFileName, Flags flags) {
        out.println("<tr class='" + flagsToStyleClass(flags.asInt()) + "'>");
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

    protected void reportClasspathBugs(PrintWriter out, String fname, String target, 
            Flags flags) 
    {
        String[] cpBugIDs = flags.getBugIDs();
        if(cpBugIDs.length == 0) return; // no bug in this file
        
        out.println("<tr class='" + flagsToStyleClass(flags.asInt()) + "'>");
        out.println("<td>" + fname + "</td>");
        if (target.equals(TargetedFileSet.DEFAULT_TARGET)) {
            target = "&nbsp;";
        }
        out.println("<td>" + target + "</td>");
        out.println("<td>");
        int i = 0;
        for(String bugID : cpBugIDs)
        {
            if(i > 0) out.println(",");
            out.println("<a href='" + CP_BUGZILLA_URL + bugID+"'>"+bugID+"</a>");
            i++;
        }
        
        out.println("</td>");
        out.println("</tr>");
        out.flush();
    }
    
    protected void reportPackageDiff(PrintWriter out, String pkg,
            String diffFileName, Flags flags) {
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

    protected void reportFlags(PrintWriter out, Flags flags) {
        final StringBuffer b = new StringBuffer();
        if (flags.isSet(FLAG_TARGET_DIFF)) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append("different target");
        }
        if (flags.isSet(FLAG_VM_SPECIFIC)) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append("vm-specific");
        }
        if (flags.isSet(FLAG_CLASSPATH_BUGFIX)) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append("cp-bugfix");
        }
        if (flags.isSet(FLAG_NATIVE)) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append("native");
        }
        if (flags.isSet(FLAG_JNODE)) {
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

    protected Flags getFlags(String code) 
    {
        final Flags flags = new Flags();
        getFlags(code, flags);
        return flags;
    }
    
    protected void getFlags(String code, final Flags flags) {
        final int nbChars = code.length(); 
        for(int i = 0 ; i < nbChars ; i++)
        {
            if(matchTag(code, i, "native")) {
                flags.set(FLAG_NATIVE);
                i += "native".length();
                continue;
            }
            
            if (matchTag(code, i, "jnode")) {
                // exclude the case where there is jnode mails ("@jnode.org")
                if((i == 0) || !matchTag(code, i - 1, jnodeMail))
                {                    
                    flags.set(FLAG_JNODE);                
                }
                i += "jnode".length();
                continue;
            }
            if (matchTag(code, i, vmSpecificTag)) {
                flags.set(FLAG_VM_SPECIFIC);
                i += vmSpecificTag.length();
                continue;
            }

            if ( matchTag(code, i, classpathBugfixTag) &&
                !matchTag(code, i, classpathBugfixEndTag) ) 
            {
                flags.set(FLAG_CLASSPATH_BUGFIX);
                
                // has this bugfix been submitted ?
                if( matchTag(code, i, classpathBugIDTag) )
                {
                    // bugfix has been submitted 
                    int startID = i + classpathBugIDTag.length();
                    int endID = startID;
                    while((endID < code.length()) && 
                          Character.isDigit(code.charAt(endID)) ) 
                    { 
                        endID++; 
                    }
                    
                    String id = code.substring(startID, endID);
                    flags.addBugID(id);
                }
                else
                {                    
                    // bugfix hasn't been submitted
                    flags.set(FLAG_UNSUBMITTED_CLASSPATH_BUGFIX);                
                }
                i += classpathBugfixTag.length();
                continue;
            }
        }
    }
    
    private boolean matchTag(String code, int startIndex, String tag)
    {
        int endIndex = startIndex+tag.length();
        if(endIndex > code.length()) return false;
        
        return code.substring(startIndex, endIndex).equals(tag);
    }

    protected Flags getFlags(SourceFile file) throws IOException {
        final FileReader fr = new FileReader(file.getFile());
        try {
            final BufferedReader in = new BufferedReader(fr);
            //final StringBuffer b = new StringBuffer();
            String line;
            final Flags flags = new Flags();
            while ((line = in.readLine()) != null) {
                getFlags(line.toString(), flags);
                //b.append(line);
                //b.append('\n');
            }
            return flags;
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
     * Returns the type.
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * The type of the comparesion.
     * @param type of the comparesion
     */
    public void setType(String type) {
        this.type = type;
    }
}

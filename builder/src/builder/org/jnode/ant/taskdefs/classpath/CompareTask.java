/*
 * $Id$
 */
package org.jnode.ant.taskdefs.classpath;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;
import org.apache.tools.ant.types.FileSet;


/**
 * Task used to compare the latest classpath version against the latest
 * jnode version of classpath.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CompareTask extends Task {

    private BaseDirs vmDirs;
    private BaseDirs classpathDirs;
    private File destDir;
    
    
    public void execute() {
        if (destDir == null) {
            throw new BuildException("The destdir attribute must be set");
        }
        final Map vmFiles = scanJavaFiles(vmDirs);
        final Map classpathFiles = scanJavaFiles(classpathDirs);
        final TreeSet allFiles = new TreeSet();
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
            
            for (Iterator i = allFiles.iterator(); i.hasNext(); ) {
                final String name = (String)i.next();
                final JavaFile cpFile = (JavaFile)classpathFiles.get(name);
                final JavaFile vmFile = (JavaFile)vmFiles.get(name);
                
                if (!vmFiles.containsKey(name)) {
                    reportMissing(out, cpFile.getClassName(), "classpath");
                    missingInCp++;
                } else if (!classpathFiles.containsKey(name)) {
                    reportMissing(out, vmFile.getClassName(), "vm");
                    missingInVm++;
                } else {
                    
                    final String diffFileName = vmFile.getClassName() + ".diff";
                    boolean different = runDiff(vmFile, cpFile, diffFileName);
                    if (different) {
                        // They are not equal
                        reportNeedsMerge(out, vmFile.getClassName(), diffFileName);
                        needsMerge++;
                    }
                    // Let's compare them
                }
            }
            
            reportFooter(out);
            out.flush();
            out.close();
            
            if (missingInCp > 0) {
            	log("Found " + missingInCp + " files missing in classpath");
            }
            if (missingInVm > 0) {
            	log("Found " + missingInVm + " files missing in vm");
            }
            if (needsMerge > 0) {
            	log("Found " + needsMerge + " files that needs merging");
			}            
        } catch (IOException ex) {
            throw new BuildException(ex);
        } catch (InterruptedException ex) {
            throw new BuildException(ex);
        }
    }
    
    protected boolean runDiff(JavaFile vmFile, JavaFile cpFile, String diffFileName) throws IOException, InterruptedException {
        final String[] cmd = {
              "diff",
              "-au",
              vmFile.getFileName(),
              cpFile.getFile().getAbsolutePath()
        };
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
                os.write(out.toByteArray());
                os.flush();
            } finally {
                os.close();
            }
        }
        return (rc != 0);
    }
    
    
    protected void reportHeader(PrintWriter out) {
        out.println("<html>");
        out.println("<title>Classpath compare</title>");
        out.println("<style type='text/css'>");
        out.println(".classpath-only { background-color: #FFFFAA; }");
        out.println(".vm-only        { background-color: #CCCCFF; }");
        out.println(".needsmerge     { background-color: #FF9090; }");
        out.println("</style>");
        out.println("<body>");
        out.println("<h1>Classpath compare results</h1>");
        out.println("Created at " + new Date());
        out.println("<table border='1' width='100%' style='border: solid 1'>");        
        out.println("<tr>");
        out.println("<th align='left'>Class</th>");
        out.println("<th align='left'>Merge status</th>");
        out.println("</tr>");
    }
    
    protected void reportMissing(PrintWriter out, String fname, String existsIn) {
        out.println("<tr class='" + existsIn + "-only'>");
        out.println("<td>" + fname + "</td>");
        out.println("<td>Exists only in " + existsIn + "</td>");
        out.println("</tr>");
    }
    
    protected void reportNeedsMerge(PrintWriter out, String fname, String diffFileName) {
        out.println("<tr class='needsmerge'>");
        out.println("<td>" + fname + "</td>");
        out.println("<td><a href='" + diffFileName + "'>Diff</a></td>");
        out.println("</tr>");
    }
    
    protected void reportFooter(PrintWriter out) {
        out.println("</table>");        
        out.println("</body></html>");
    }
    
    
    public BaseDirs createVmsources() {
        if (vmDirs == null) {
            vmDirs = new BaseDirs();
        }
        return vmDirs;
    }
    
    public BaseDirs createClasspathsources() {
        if (classpathDirs == null) {
            classpathDirs = new BaseDirs();
        }
        return classpathDirs;
    }
    
    private Map scanJavaFiles(BaseDirs baseDirs) {
        TreeMap map = new TreeMap();
        for (Iterator i = baseDirs.getFileSets().iterator(); i.hasNext(); ) {
            final FileSet fs = (FileSet)i.next();

            final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            final String[] fNames = ds.getIncludedFiles();
            for (int j = 0; j < fNames.length; j++) {
                final String fName = fNames[j];
                map.put(fName, new JavaFile(ds.getBasedir(), fName));
            }
        }
        return map;
    }
    
    public static class BaseDirs {
        
        private final ArrayList fileSets = new ArrayList();

        public List getFileSets() {
            return fileSets;
        }
        
        public void addFileset(FileSet fs) {
            fileSets.add(fs);
        }
    }    
    
    /**
     * @return Returns the destDir.
     */
    public final File getDestDir() {
        return this.destDir;
    }
    /**
     * @param destDir The destDir to set.
     */
    public final void setDestDir(File destDir) {
        this.destDir = destDir;
    }
}

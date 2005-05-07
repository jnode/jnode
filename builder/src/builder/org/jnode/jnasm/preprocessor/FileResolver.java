/**
 * $Id$  
 */
package org.jnode.jnasm.preprocessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class FileResolver {
    private final List<File> directoryList;

    public FileResolver(List<File> directoryList) {
        this.directoryList = directoryList;
    }

    public File resolveFile(String fileName) throws FileNotFoundException {
        if (directoryList == null){
            File file = new File(fileName);
            if(file.exists()) {
                System.out.println("Resolved: " + file);
                return file;
            } else {
                throw new FileNotFoundException(fileName);
            }
        } else {
            File resolved;
            for (File directory : directoryList) {
                resolved = new File(directory, fileName);
                if (resolved.exists()){
                    System.out.println("Resolved: " + resolved);
                    return resolved;
                }
            }
        }
        throw new FileNotFoundException(fileName);
    }
}

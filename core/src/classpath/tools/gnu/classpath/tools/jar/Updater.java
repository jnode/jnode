/* Updater.java - action to update a jar file
 Copyright (C) 2006 Free Software Foundation, Inc.

 This file is part of GNU Classpath.

 GNU Classpath is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.

 GNU Classpath is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with GNU Classpath; see the file COPYING.  If not, write to the
 Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version. */


package gnu.classpath.tools.jar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class Updater
    extends Creator
{
  JarFile inputJar;

  protected Manifest createManifest(Main parameters) throws IOException
  {
    Manifest result = inputJar.getManifest();
    if (result == null)
      return super.createManifest(parameters);
    if (parameters.manifestFile != null)
      result.read(new FileInputStream(parameters.manifestFile));
    return result;
  }

  public void run(Main parameters) throws IOException
  {
    // Set this early so that createManifest can use it.
    inputJar = new JarFile(parameters.archiveFile);

    // Write all the new entries to a temporary file.
    File tmpFile = File.createTempFile("jarcopy", null);
    OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile));
    writeCommandLineEntries(parameters, os);

    // Now read the old file and copy extra entries to the new file.
    Enumeration e = inputJar.entries();
    while (e.hasMoreElements())
      {
        ZipEntry entry = (ZipEntry) e.nextElement();
        if (writtenItems.contains(entry.getName()))
          continue;
        writeFile(entry.isDirectory(), inputJar.getInputStream(entry),
                  entry.getName(), parameters.verbose);
      }

    close();
    tmpFile.renameTo(parameters.archiveFile);
  }
}

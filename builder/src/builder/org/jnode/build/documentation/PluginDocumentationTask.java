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
 
package org.jnode.build.documentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.net.MalformedURLException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jnode.build.AbstractPluginTask;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.Library;
import org.jnode.plugin.PluginPrerequisite;
import org.jnode.plugin.model.PluginDescriptorModel;

import com.lowagie.text.*;
import com.lowagie.text.html.HtmlWriter;

/**
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class PluginDocumentationTask extends AbstractPluginTask
{
  private LinkedList<FileSet> descriptorSets = new LinkedList<FileSet>();
  private File todir;
  private final static String DOCHEADER = "JNode plugin documentation";
  private final String ext = ".html";

  public FileSet createDescriptors()
  {
    final FileSet fs = new FileSet();
    descriptorSets.add(fs);

    return fs;
  }

  protected File[] getDescriptorFiles()
  {
    List<File> files = new ArrayList<File>();
    for (FileSet fs : descriptorSets)
    {
      final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
      final String[] filesNames = ds.getIncludedFiles();

      for (String filename : filesNames)
      {
        files.add(new File(ds.getBasedir(), filename));
      }
    }

    return (File[]) files.toArray(new File[files.size()]);
  }


  /**
   * @return The destination directory
   */
  public final File getTodir()
  {
    return this.todir;
  }

  /**
   * @param todir
   */
  public final void setTodir(File todir)
  {
    this.todir = todir;
  }

  /**
   * @throws org.apache.tools.ant.BuildException
   *
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() throws BuildException
  {
    if (descriptorSets.isEmpty())
    {
      throw new BuildException("At at least 1 descriptorset element");
    }

    if (todir == null)
    {
      throw new BuildException("The todir attribute must be set");
    }

    if (getPluginDir() == null)
    {
      throw new BuildException("The pluginDir attribute must be set");
    }

    if (!todir.exists())
    {
      todir.mkdirs();
    }

    else if (!todir.isDirectory())
    {
      throw new BuildException("todir must be a directory");
    }

    Document document = new Document();
    Table table = null;

    try
    {
      File file = new File(todir,"index"+ext);
      HtmlWriter.getInstance(document, new FileOutputStream(file));

      document.open();
      Cell cell;

      table = new Table(1);
      table.setBorderWidth(1);
      table.setPadding(0);
      table.setSpacing(0);
      table.setAlignment("left");
      table.setWidth(100);

      cell = new Cell(DOCHEADER);
      cell.setHeader(true);
      table.addCell(cell);
      table.endHeaders();
    }
    catch (FileNotFoundException e)
    {
      throw new BuildException(e.getMessage());
    }
    catch (BadElementException e)
    {
      throw new BuildException(e.getMessage());
    }
    catch (DocumentException e)
    {
      throw new BuildException(e.getMessage());
    }

    Map<String, File> descriptors = new HashMap<String, File>();

    File[] files = getDescriptorFiles();

    for (File descriptor : files)
    {
      buildPluginDocumentation(descriptors, descriptor, table);
    }

    try
    {
      document.add(table);
    }
    catch (DocumentException e)
    {
      throw new BuildException(e.getMessage());
    }
    document.close();
  }

/**
 * Vuild the documentation for the plugin
 * @param descriptors a map of all the descriptors
 * @param descriptor the descriptor
 * @param index index file
 * @throws BuildException
 */
  protected void buildPluginDocumentation(Map<String, File> descriptors, File descriptor, Table index) throws BuildException
  {
    final PluginDescriptorModel pluginDescriptorModel = readDescriptor(descriptor);

    final String fullId = pluginDescriptorModel.getId() + "_" + pluginDescriptorModel.getVersion();
    if (descriptors.containsKey(fullId))
    {
      File otherDesc = descriptors.get(fullId);
      throw new BuildException("Same id(" + fullId + ") for 2 plugins: " + otherDesc + ", " + descriptor);
    }

    descriptors.put(fullId, descriptor);

    Document document = new Document();

    try
    {
      File file = new File(todir,pluginDescriptorModel.getId()+ext);

      HtmlWriter.getInstance(document, new FileOutputStream(file));

      document.open();
      Table table = new Table(3);
      Cell cell;

      table.setBorderWidth(1);
      table.setPadding(0);
      table.setSpacing(0);
      table.setAlignment("left");
      table.setWidth(100);

      Anchor anchor = new Anchor(pluginDescriptorModel.getId());
      anchor.setName("LINK");
      anchor.setReference(file.toURL().toString());
      index.addCell(new Cell(anchor));

      cell = new Cell(DOCHEADER+" for "+pluginDescriptorModel.getId());
      cell.setHeader(true);
      cell.setColspan(3);
      table.addCell(cell);
      table.endHeaders();

      table.addCell("Name");
      cell = new Cell(pluginDescriptorModel.getName());
      cell.setColspan(2);
      table.addCell(cell);

      table.addCell("Version");
      cell = new Cell(pluginDescriptorModel.getVersion());
      cell.setColspan(2);
      table.addCell(cell);

      table.addCell("Provider");
      cell = new Cell(pluginDescriptorModel.getProviderName());
      cell.setColspan(2);
      table.addCell(cell);

      if (pluginDescriptorModel.getPrerequisites() != null && pluginDescriptorModel.getPrerequisites().length != 0)
      {
        cell = new Cell("Requires");
        cell.setRowspan(pluginDescriptorModel.getPrerequisites().length);
        cell.setVerticalAlignment("top");
        table.addCell(cell);

        File prerequisites;
        for (PluginPrerequisite pluginPrerequisite : pluginDescriptorModel.getPrerequisites())
        {
          anchor = new Anchor(pluginPrerequisite.getPluginId());
          anchor.setName("LINK");
          prerequisites = new File(todir, pluginPrerequisite.getPluginId()+ext);
          anchor.setReference(prerequisites.toURL().toString());
          cell = new Cell(anchor);
          cell.setColspan(2);
          table.addCell(cell);
        }
      }

      if (pluginDescriptorModel.getRuntime() != null)
      {
        final String lib ="Library";
        final String exports ="Exports packages";

        for (Library library : pluginDescriptorModel.getRuntime().getLibraries())
        {
          table.addCell(lib);
          cell = new Cell(library.getName());
          cell.setColspan(2);
          table.addCell(cell);

          cell = new Cell(exports);
          cell.setRowspan(library.getExports().length);
          cell.setVerticalAlignment("top");
          table.addCell(cell);
          for (String export : library.getExports())
          {
            cell = new Cell(export);
            cell.setColspan(2);
            table.addCell(cell);
          }
        }
      }

      if (pluginDescriptorModel.getExtensions() != null)
      {
        final String extens ="Extension";
        final String premis =" ";
        for (Extension extension : pluginDescriptorModel.getExtensions())
        {
          table.addCell(extens);
          cell = new Cell(extension.getExtensionPointUniqueIdentifier());
          cell.setColspan(2);
          table.addCell(cell);

          cell = new Cell(premis);
          cell.setRowspan(extension.getConfigurationElements().length);
          cell.setColspan(1);
          table.addCell(cell);
          String name, actions;

          for (ConfigurationElement configurationElement : extension.getConfigurationElements())
          {
            name = configurationElement.getAttribute("name");
            actions = configurationElement.getAttribute("actions");
            cell = new Cell(configurationElement.getAttribute("class")+(name!=null?" name: \""+name+"\"":"")+(actions!=null?" actions: \""+actions+"\"":""));
            cell.setColspan(2);
            table.addCell(cell);
          }
        }
      }

      document.add(table);

      document.close();
    }
    catch (FileNotFoundException e)
    {
      throw new BuildException(e.getMessage());
    }
    catch (DocumentException e)
    {
      throw new BuildException(e.getMessage());
    }
    catch (MalformedURLException e)
    {
      throw new BuildException(e.getMessage());
    }
  }
}

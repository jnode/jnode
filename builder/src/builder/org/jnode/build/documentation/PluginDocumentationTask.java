package org.jnode.build.documentation;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;
import org.jnode.build.AbstractPluginTask;
import org.jnode.plugin.model.PluginDescriptorModel;

import java.io.File;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mh
 * Date: 20-04-2005
 * Time: 14:07:43
 * To change this template use File | Settings | File Templates.
 */
public class PluginDocumentationTask extends AbstractPluginTask
{
  private LinkedList<FileSet> descriptorSets = new LinkedList<FileSet>();
  private File todir;

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

    Map<String, File> descriptors = new HashMap<String, File>();

    File[] files = getDescriptorFiles();

    for (File descriptor : files)
    {
      buildPluginDocumentation(descriptors, descriptor);
    }
  }

  protected void buildPluginDocumentation(Map<String, File> descriptors, File descriptor) throws BuildException
  {
    final PluginDescriptorModel pluginDescriptorModel = readDescriptor(descriptor);

    final String fullId = pluginDescriptorModel.getId() + "_" + pluginDescriptorModel.getVersion();
    if (descriptors.containsKey(fullId))
    {
      File otherDesc = descriptors.get(fullId);
      throw new BuildException("Same id(" + fullId + ") for 2 plugins: " + otherDesc + ", " + descriptor);
    }

    descriptors.put(fullId, descriptor);


//    throw new BuildException(pluginDescriptorModel.toString());
  }
}

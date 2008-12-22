/*

JTestServer is a client/server framework for testing any JVM implementation.
 
Copyright (C) 2008  Fabien DUMINY (fduminy@jnode.org)

JTestServer is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

JTestServer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package org.jtestserver.client.utils;

import gnu.testlet.runner.Filter;
import gnu.testlet.runner.Filter.LineProcessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jtestserver.client.Config;

/**
 * Utility class used to read from a text file and also to write it.
 *  
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class TestListRW {
    /**
     * My logger
     */
    private static final Logger LOGGER = Logger.getLogger(TestListRW.class.getName());
    
    /**
     * Configuration used to filter the list of lines.
     */
    private final Config config;
    
    /**
     * Create an instance from the given configuration.
     * @param config
     */
    public TestListRW(Config config) {
        this.config = config;
    }
    
    /**
     * Read the mauve tests list but don't take lines containing '[' 
     * and also apply additional filters specified in configuration. 
     * @return
     * @throws IOException
     */
    public List<String> readCompleteList() throws IOException {
        final List<String> list = new ArrayList<String>();
        Filter.readTestList(new LineProcessor() {

            @Override
            public void processLine(StringBuffer buf) {
                String line = buf.toString();
                //if (!line.contains("[")) {
                if (!line.contains("[") && acceptTest(line)) {
                    list.add(line);
                }
            }
            
        });
        return list;
    }
    
    /**
     * Read a list from the given file but don't take lines starting with '#' (comments) 
     * and also apply additional filters specified in configuration.
     * @param file from which to read the list.
     * @return
     * @throws IOException
     */
    public List<String> readList(File file) throws IOException {
        List<String> list = new ArrayList<String>();
        
        InputStream in = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String test = null;
        while ((test = reader.readLine()) != null) {
            if (!test.startsWith("#") && acceptTest(test)) {
                list.add(test);
            }
        }
        LOGGER.info("read " + list.size() + " lines from " + file.getAbsolutePath());
        
        return list;
    }
    
    /**
     * Check if a test should be kept or not according to the filters specified 
     * in the configuration.
     * @param test name of the test top filter.
     * @return true if the test should be kept.
     */
    public boolean acceptTest(String test) {
        boolean accept = true;
        
        for (String exclude : config.getExcludingFilters()) {
            if (test.contains(exclude)) {
                accept = false;
                break;
            }
        }
        
        return accept;
    }

    /**
     * Write a list to the given file.
     * @param file to which the list is written.
     * @param list the list to write.
     * @throws IOException
     */
    public void writeList(File file, List<String> list) throws IOException {
        OutputStream out = new FileOutputStream(file);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

        for (String line : list) {
            writer.append(line).append('\n');
        }
        writer.close();
        LOGGER.info("wrote " + list.size() + " lines to " + file.getAbsolutePath());
    }
}

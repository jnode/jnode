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
package org.jtestserver.client;

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

public class TestListRW {
    private static final Logger LOGGER = Logger.getLogger(TestListRW.class.getName());
    
    private final Config config;
    
    public TestListRW(Config config) {
        this.config = config;
    }
    
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

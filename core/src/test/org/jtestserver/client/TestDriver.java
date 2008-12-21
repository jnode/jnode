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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jtestserver.client.process.ServerProcess;
import org.jtestserver.client.utils.TestListRW;
import org.jtestserver.client.utils.WatchDog;
import org.jtestserver.common.Status;
import org.jtestserver.common.protocol.Protocol;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.TimeoutException;
import org.jtestserver.common.protocol.UDPProtocol;
import org.jtestserver.tests.AllTests;

public class TestDriver {
    private static final Logger LOGGER = Logger.getLogger(TestDriver.class.getName());
    
    public static void main(String[] args) {
        File configDir = AllTests.CONFIG_DIRECTORY;
        //File configDir = new File(".");
        
        try {
            TestDriver testDriver = createUDPTestDriver(configDir);
            
            if ((args.length > 0) && "kill".equals(args[0])) {
                testDriver.killRunningServers();
            } else {
                testDriver.start();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "protocol error", e);
        } catch (ProtocolException e) {
            LOGGER.log(Level.SEVERE, "I/O error", e);
        }
    }
    
    private static TestDriver createUDPTestDriver(File configDir) throws ProtocolException, IOException {         
        Config config = new ConfigReader().read(configDir);
        InetAddress serverAddress = InetAddress.getByName(config.getServerName());
        int serverPort = config.getServerPort();
        UDPProtocol protocol = UDPProtocol.createClient(serverAddress, serverPort);
        protocol.setTimeout(config.getClientTimeout());
        
        ServerProcess process = config.getVMConfig().createServerProcess();
        return new TestDriver(config, protocol, process);
    }
    
    private final TestClient client;
    private final ServerProcess process;
    private final List<String> tests = new ArrayList<String>();
    private final Config config;
    private final TestListRW testListRW;
    private final WatchDog watchDog;
    
    private TestDriver(Config config, Protocol protocol, ServerProcess process) {
        this.config = config;
        client = new DefaultTestClient(protocol);
        this.process = process;
        testListRW = new TestListRW(config);
        watchDog = new WatchDog(process, config) {

            @Override
            protected void processDead() {
                LOGGER.warning("process is dead. restarting it.");
                try {
                    TestDriver.this.start();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "error while restarting", e);
                } catch (ProtocolException e) {
                    LOGGER.log(Level.SEVERE, "error while restarting", e);
                }
            }
        };
    }
    
    public void killRunningServers() throws ProtocolException {
        LOGGER.info("killing running servers");
        
        try {
            // kill server that might still be running
            client.shutdown();
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "unexpected error", t);
        }
        
        boolean killed = false;
        while (!killed) {
            try {
                client.getStatus();
            } catch (TimeoutException e) {
                LOGGER.log(Level.SEVERE, "a timeout happened", e);
                killed = true;
            }
        }

        // stop the watch dog before actually stop the process
        watchDog.stopWatching();
        try {
            process.stop();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "and error happened while stopping", e);
        }
    }
    
    public void start() throws IOException, ProtocolException {
        killRunningServers();
        
        process.start();
        watchDog.startWatching();
 
        final File workingFile = new File(config.getWorkDir(), "working-tests.txt");
        final File crashingFile = new File(config.getWorkDir(), "crashing-tests.txt");
        
        List<String> workingList = new ArrayList<String>();
        List<String> crashingList = new ArrayList<String>();
        
        LOGGER.info("running list of working tests");
        runTests(workingFile, true, workingList, crashingList);
        
        LOGGER.info("running list of crashing tests");
        runTests(crashingFile, false, workingList, crashingList);
        
        LOGGER.info("writing crashing & working tests lists");        
        testListRW.writeList(workingFile, workingList);
        testListRW.writeList(crashingFile, crashingList);
        
        watchDog.stopWatching();
        killRunningServers();
    }
    
    private void runTests(File listFile, boolean useCompleteListAsDefault,
            List<String> workingList, List<String> crashingList)
        throws ProtocolException, IOException {
        final List<String> list;
        if (listFile.exists() && !config.isForceUseMauveList()) {
            list = testListRW.readList(listFile);
        } else {
            if (useCompleteListAsDefault || config.isForceUseMauveList()) {
                // not yet a list of working/crashing tests => starts with the
                // default one
                list = testListRW.readCompleteList();
            } else {
                list = new ArrayList<String>();
            }
        }

        for (String test : list) {
            boolean working = false;
            LOGGER.info("launching test " + test);

            try {
                Status status = client.runMauveTest(test);
                LOGGER.info(((status == null) ? "null" : status.toString()) + ": " + test);

                working = true;
            } catch (TimeoutException e) {
                LOGGER.log(Level.SEVERE, "a timeout happened", e);
            } finally {
                if (working) {
                    workingList.add(test);
                } else {
                    crashingList.add(test);
                }
            }
        }
    }
    
}

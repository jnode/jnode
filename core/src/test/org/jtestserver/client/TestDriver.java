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

import gnu.testlet.runner.ClassResult;
import gnu.testlet.runner.HTMLGenerator;
import gnu.testlet.runner.PackageResult;
import gnu.testlet.runner.RunResult;
import gnu.testlet.runner.TestResult;
import gnu.testlet.runner.XMLReportParser;
import gnu.testlet.runner.XMLReportWriter;
import gnu.testlet.runner.compare.ComparisonWriter;
import gnu.testlet.runner.compare.HTMLComparisonWriter;
import gnu.testlet.runner.compare.ReportComparator;
import gnu.testlet.runner.compare.RunComparison;
import gnu.testlet.runner.compare.TextComparisonWriter;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.nanoxml.XMLParseException;

import org.jtestserver.client.process.ServerProcess;
import org.jtestserver.client.utils.ConfigurationUtils;
import org.jtestserver.client.utils.TestListRW;
import org.jtestserver.client.utils.WatchDog;
import org.jtestserver.common.protocol.Client;
import org.jtestserver.common.protocol.Protocol;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.TimeoutException;
import org.jtestserver.common.protocol.udp.UDPProtocol;

public class TestDriver {
    private static final Logger LOGGER = Logger.getLogger(TestDriver.class.getName());
    
    public static void main(String[] args) {
        ConfigurationUtils.init();
        TestDriver testDriver = null;
        
        try {
            testDriver = createUDPTestDriver();
            
            if ((args.length > 0) && "kill".equals(args[0])) {
                testDriver.killRunningServers();
            } else {
                testDriver.start();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "protocol error", e);
            
            if (testDriver != null) {
                try {
                    testDriver.killRunningServers();
                } catch (ProtocolException e1) {
                    LOGGER.log(Level.SEVERE, "protocol error", e1);
                }
            }
        } catch (ProtocolException e) {
            LOGGER.log(Level.SEVERE, "I/O error", e);
            
            if (testDriver != null) {
                try {
                    testDriver.killRunningServers();
                } catch (ProtocolException e1) {
                    LOGGER.log(Level.SEVERE, "protocol error", e1);
                }
            }
        }
    }
    
    private static TestDriver createUDPTestDriver() throws ProtocolException, IOException {         
        Config config = new ConfigReader().read(ConfigurationUtils.getConfigurationFile());
        InetAddress serverAddress = InetAddress.getByName(config.getServerName());
        int serverPort = config.getServerPort();        
        Protocol<?> protocol = new UDPProtocol(); //TODO create protocol from a config parameter
        
        Client<?, ?> client = protocol.createClient(serverAddress, serverPort);
        client.setTimeout(config.getClientTimeout());
        
        ServerProcess process = config.getVMConfig().createServerProcess();
        return new TestDriver(config, client, process);
    }
    
    private final TestClient client;
    private final ServerProcess process;
    private final List<String> tests = new ArrayList<String>();
    private final Config config;
    private final TestListRW testListRW;
    private final WatchDog watchDog;
    
    private TestDriver(Config config, Client<?, ?> client, ServerProcess process) {
        this.config = config;
        this.client = new DefaultTestClient(client);
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
        //killRunningServers();
        
        process.start();
        watchDog.startWatching();
 
        Run latestRun = Run.getLatest(config);
        Run newRun = Run.create(config);
        
        List<String> workingList = new ArrayList<String>();
        List<String> crashingList = new ArrayList<String>();
        
        LOGGER.info("running list of working tests");
        File workingTests = (latestRun == null) ? null : latestRun.getWorkingTests();
        RunResult runResult = runTests(workingTests, true, workingList, crashingList, newRun.getTimestampString());
        
        LOGGER.info("running list of crashing tests");
        File crashingTests = (latestRun == null) ? null : latestRun.getCrashingTests();
        RunResult rr = runTests(crashingTests, false, workingList, crashingList, 
                newRun.getTimestampString());
        mergeResults(runResult, rr);
        
        LOGGER.info("writing crashing & working tests lists");
        testListRW.writeList(newRun.getWorkingTests(), workingList);
        testListRW.writeList(newRun.getCrashingTests(), crashingList);
        
        writeReports(runResult, newRun.getReportXml());
        
        compareRuns(latestRun, newRun, runResult);
        
        watchDog.stopWatching();
        killRunningServers();
    }
    
    private void compareRuns(Run latestRun, Run newRun, RunResult newRunResult) throws XMLParseException, IOException {
        RunResult latestRunResult = new XMLReportParser().parse(latestRun.getReportXml());
        
        ReportComparator comparator = new ReportComparator(latestRunResult, newRunResult);
        RunComparison comparison = comparator.compare();
        
        // write comparison in html format
        ComparisonWriter writer = new HTMLComparisonWriter();
        writer.write(comparison, new File(newRun.getReportXml().getParentFile(), "comparison.html"));
        
        // write comparison in text format
        writer = new TextComparisonWriter();
        writer.write(comparison, new File(newRun.getReportXml().getParentFile(), "comparison.txt"));
    }
    
    private void writeReports(RunResult result, File reportXml) throws IOException {
        XMLReportWriter rw = new XMLReportWriter(false);
        rw.write(result, reportXml);
        
        HTMLGenerator.createReport(result, reportXml.getParentFile());
    }
    
    private RunResult runTests(File listFile, boolean useCompleteListAsDefault,
            List<String> workingList, List<String> crashingList, String timestamp)
        throws ProtocolException, IOException {
        final List<String> list;
        if ((listFile != null) && listFile.exists() && !config.isForceUseMauveList()) {
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

        RunResult result = new RunResult(timestamp);
        int i = 0;
        for (String test : list) {
            if (i++ > 100) { // TODO for debug only, remove that
                break;
            }
            
            boolean working = false;
            LOGGER.info("launching test " + test);

            try {
                RunResult delta = client.runMauveTest(test);
                mergeResults(result, delta);
                
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
        
        return result;
    }
    
    private void mergeResults(RunResult targetResult, RunResult result) {
        for (Iterator<?> itPackage = result.getPackageIterator(); itPackage.hasNext(); ) {
            PackageResult pkg = (PackageResult) itPackage.next();
            
            PackageResult pr = targetResult.getPackageResult(pkg.getName());
            if (pr == null) {
                pr = pkg;
                targetResult.add(pkg);
            } else {            
                for (Iterator<?> itClass = pkg.getClassIterator(); itClass.hasNext(); ) {
                    ClassResult cls = (ClassResult) itClass.next();
                    
                    ClassResult cr = pr.getClassResult(cls.getName());
                    if (cr == null) {
                        cr = cls;
                        pr.add(cls);
                    } else {                                    
                        for (Iterator<?> itTest = cls.getTestIterator(); itTest.hasNext(); ) {
                            TestResult test = (TestResult) itTest.next();
                            cr.add(test);
                        }
                    }
                }
            }
        }
    }
    
}

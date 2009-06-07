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
import org.jtestserver.client.router.MultipleClientTestRouter;
import org.jtestserver.client.router.TestRouter;
import org.jtestserver.client.router.TestRouterResult;
import org.jtestserver.client.utils.ConfigurationUtils;
import org.jtestserver.client.utils.TestListRW;
import org.jtestserver.common.protocol.Client;
import org.jtestserver.common.protocol.Protocol;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.udp.UDPProtocol;

public class TestDriver {
    private static final Logger LOGGER = Logger.getLogger(TestDriver.class.getName());
    
    public static void main(String[] args) {
        ConfigurationUtils.init();
        TestDriver testDriver = null;
        
        try {
            testDriver = createUDPTestDriver();
            
            testDriver.start();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "protocol error", e);
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
    
    private final Config config;
    private final TestListRW testListRW;
    private final TestRouter instance;
    private final String processClassName;
    
    private TestDriver(Config config, Client<?, ?> client, ServerProcess process) {
        this.config = config;
        testListRW = new TestListRW(config);
        //instance = new SingleClientTestRouter(config, client, process);
        instance = new MultipleClientTestRouter(config, client, process);
        processClassName = process.getClass().getName();
    }
    
    public void start() throws Exception {
        instance.start();
 
        try {
            Run latestRun = Run.getLatest(config);
            Run newRun = Run.create(config);
            
            List<String> workingList = new ArrayList<String>();
            List<String> crashingList = new ArrayList<String>();
            RunResult runResult;
            
            if (latestRun == null) {
                LOGGER.info("running list of all tests");
                runResult = runTests(null, true, workingList, crashingList, newRun.getTimestampString());
                runResult.setSystemProperty("jtestserver.process", processClassName);

            } else {
                LOGGER.info("running list of working tests");
                File workingTests = latestRun.getWorkingTests();
                runResult = runTests(workingTests, true, workingList, crashingList, newRun.getTimestampString());
                runResult.setSystemProperty("jtestserver.process", processClassName);
                
                LOGGER.info("running list of crashing tests");
                File crashingTests = latestRun.getCrashingTests();
                RunResult rr = runTests(crashingTests, false, workingList, crashingList, 
                        newRun.getTimestampString());
                mergeResults(runResult, rr);
            }
            
            LOGGER.info("writing crashing & working tests lists");
            testListRW.writeList(newRun.getWorkingTests(), workingList);
            testListRW.writeList(newRun.getCrashingTests(), crashingList);
            
            writeReports(runResult, newRun.getReportXml());
            
            compareRuns(latestRun, newRun, runResult);
        } finally {        
            instance.stop();
        }
    }
    
    private void compareRuns(Run latestRun, Run newRun, RunResult newRunResult) throws XMLParseException, IOException {
        if ((latestRun != null) && latestRun.getReportXml().exists()) {
            // there was a previous run, let do the comparison !
            
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
    }
    
    private void writeReports(RunResult result, File reportXml) throws IOException {
        XMLReportWriter rw = new XMLReportWriter(false);
        rw.write(result, reportXml);
        
        HTMLGenerator.createReport(result, reportXml.getParentFile());
    }
    
    private RunResult runTests(File listFile, boolean useCompleteListAsDefault,
            List<String> workingList, List<String> crashingList, String timestamp)
        throws Exception {
        final List<String> list;
        if ((listFile != null) && listFile.exists()) {
            list = testListRW.readList(listFile);
        } else {
            if (useCompleteListAsDefault) {
                // not yet a list of working/crashing tests => starts with the
                // default one
                list = testListRW.readCompleteList();
            } else {
                list = new ArrayList<String>();
            }
        }

        int i = 0; // TODO for debug only, remove that
        for (String test : list) {
            if (i++ > 100) { // TODO for debug only, remove that
                break;
            }
            
            LOGGER.info("adding test " + test);            
            instance.addTest(test);
        }

        RunResult result = new RunResult(timestamp);
        boolean firstTest = true;
        while (instance.hasPendingTests()) {
            boolean working = false;
            RunResult delta = null;
            String test = null;
            
            try {
                LOGGER.info("getting a result");
                TestRouterResult runnerResult = instance.getResult(); 
                delta = runnerResult.getRunResult();
                test = runnerResult.getTest();
                LOGGER.info("got a result for " + test);
                mergeResults(result, delta);
                
                working = true;
            } finally {
                if (working) {
                    workingList.add(test);
                } else {
                    crashingList.add(test);
                }
                
                if (firstTest && (delta != null)) {
                    for (String name : delta.getSystemPropertyNames()) {
                        result.setSystemProperty(name, delta.getSystemProperty(name));
                    }
                    
                    firstTest = false;
                }
            }
        }
        
        return result;
    }
    
    private void mergeResults(RunResult target, RunResult source) {
        for (Iterator<?> itSourcePackage = source.getPackageIterator(); itSourcePackage.hasNext(); ) {
            PackageResult sourcePackage = (PackageResult) itSourcePackage.next();
            
            PackageResult targetPackage = target.getPackageResult(sourcePackage.getName());
            if (targetPackage == null) {
                target.add(sourcePackage);
            } else {            
                for (Iterator<?> itSourceClass = sourcePackage.getClassIterator(); itSourceClass.hasNext(); ) {
                    ClassResult sourceClass = (ClassResult) itSourceClass.next();
                    
                    ClassResult targetClass = targetPackage.getClassResult(sourceClass.getName());
                    if (targetClass == null) {
                        targetPackage.add(sourceClass);
                    } else {                                    
                        for (Iterator<?> itSourceTest = sourceClass.getTestIterator(); itSourceTest.hasNext(); ) {
                            TestResult sourceTest = (TestResult) itSourceTest.next();
                            
                            boolean hasTest = false;
                            for (Iterator<?> it = targetClass.getTestIterator(); it.hasNext(); ) {
                                TestResult tr = (TestResult) it.next();
                                if (tr.getName().equals(sourceTest.getName())) {
                                    hasTest = true;
                                    break;
                                }
                            }
                            
                            if (!hasTest) {
                                targetClass.add(sourceTest);
                            }
                            
                        }
                    }
                }
            }
        }
    }
    
}

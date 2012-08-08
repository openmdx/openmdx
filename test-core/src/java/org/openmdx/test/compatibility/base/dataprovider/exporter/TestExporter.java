/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestExporter.java,v 1.11 2007/02/02 14:42:31 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/02/02 14:42:31 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.test.compatibility.base.dataprovider.exporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefPackage;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.ElementNameQualifier;
import org.custommonkey.xmlunit.ElementQualifier;
import org.custommonkey.xmlunit.XMLTestCase;
import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.view.Manager_1;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1ConnectionFactoryImpl;
import org.openmdx.compatibility.base.application.container.LightweightContainer_1;
import org.openmdx.compatibility.base.application.container.SimpleServiceLocator;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.QualityOfService;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.exporter.KeepCauseErrorHandler;
import org.openmdx.compatibility.base.dataprovider.exporter.ProviderTraverser;
import org.openmdx.compatibility.base.dataprovider.exporter.SystemPrintHandler;
import org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler;
import org.openmdx.compatibility.base.dataprovider.exporter.Traverser;
import org.openmdx.compatibility.base.dataprovider.exporter.XMLExportHandler;
import org.openmdx.compatibility.base.dataprovider.exporter.XmlContentHandler;
import org.openmdx.compatibility.base.dataprovider.layer.model.StopWatch_1;
import org.openmdx.compatibility.base.dataprovider.transport.adapter.Provider_1;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Provider_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.delegation.Connection_1;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.time.TreeStopWatch;
import org.openmdx.deployment1.accessor.basic.DeploymentConfiguration_1;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.accessor.basic.spi.Model_1;
import org.openmdx.test.compatibility.base.dataprovider.exporter.ExporterTestSetup;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.openmdx.base.jmi.BasePackage;
import org.openmdx.test.compatibility.state1.classic.jmi.ClassicPackage;
import org.openmdx.test.compatibility.state1.classic.cci.Segment;

/**
 * 
 */
public class TestExporter extends XMLTestCase {
  
    /**
     * referenceFilters is an additional argument of the ProviderTraverser's constructors
     */
    static private List REFERENCE_FILTERS = Collections.EMPTY_LIST; // TODO verify its value

    /**
     * attributeFilters is an additional argument of the ProviderTraverser's constructors
     */
    static private Map ATTRIBUTE_FILTERS = Collections.EMPTY_MAP; // TODO Verfy its value

    static private String[] _args;
    static private boolean _doSetup = true;

    static private String EXPORT_SEGMENT_ID = "ExportTest"; 

    private static final String STATE_ID_1 = "export1";
    private static final String STATE_ID_2 = "export2";
    //private static final String _roledId1 = "exportRoleId1";
    //private static final String _roledId2 = "exportRoleId2";
    //private static final String _roledId3 = "exportRoleId3";
    //private static final String _roledId4 = "exportRoleId4";
    //private static final String _roleTypeId1 = "aRole1";
    //private static final String _roleTypeId2 = "aRole2";

    private static final String _coreIdBase = "xbase";
//  private static final String _noRoleIdBase = "xNoR";
    //private static final String _roleTypeIdX = "xRole";

    private static final short TEST_WITH_ROLES = 1;
    private static final short TEST_WITH_STATES = 2;
    private static final short TEST_WITH_ROLESANDSTATES = 3;
    private static final short TEST_PLAIN = 4;
    private static final short TEST_UNDEF = 22;

    
    static private boolean deployed = false;
    static private final Path[] PROVIDER_DEPLOYMENT_UNITS = new Path[]{
	    new Path("xri:@openmdx:org.openmdx.deployment1/provider/org:openmdx/segment/org:openmdx:test/configuration/junit/domain/apps/deploymentUnit/teststate")
	};
    
    static private final Path[] CONNECTOR_DEPLOYMENT_UNITS = new Path[]{
        new Path("xri:@openmdx:org.openmdx.deployment1/provider/org:openmdx/segment/org:openmdx:test/configuration/junit/domain/apps/deploymentUnit/connectors")
    };
    
    static private final Path ROOT_PROVIDER_PATH = new Path("xri:@openmdx:org.openmdx.test.compatibility.state1/provider");
    
    Path _sourceSegmentPath = null;

    private ServiceHeader serviceHeader;
    private RefPackage_1_0 rootPkgForSource;
//  private RefPackage rootPkgForTarget;
    private Dataprovider_1_0 sourceDataprovider;
//  private Dataprovider_1_0 targetDataprovider;
//  private Provider targetProvider;
    private Segment sourceSegment;

    public TestExporter(String name) {
        super(name);
    }

    public static void main(String[] args) {
        try {
            _args = args;
            junit.textui.TestRunner.run(suite());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        TestSuite suite = null;
        
        if (_args != null ) {
	        for (int i = 0; i < _args.length; i++) {
	            if ("-t".equals(_args[i]) && _args.length > i + 1) {
	                if (suite == null) {
	                    suite = new TestSuite();
	                }
	
	                suite.addTest(new TestExporter(_args[i + 1]));
	            }
	            
	            if ("--noSetup".equals(_args[i])) {
	                _doSetup = false;
	            }
	        }
        }

        if (suite == null) {
            suite = new TestSuite(TestExporter.class);
        }

        return suite;
    }

    protected void setUp() throws Exception {
        AppLog.info("testName: "+ getName());
        
        _sourceSegmentPath = ROOT_PROVIDER_PATH.getChild(getName().substring(4, getName().length())).getChild("segment").getChild(EXPORT_SEGMENT_ID);

        try {
            if(! deployed){
                System.out.println("Deploying...");
                DeploymentConfiguration_1.createInstance(
                    new String[]{
                        "xri:+resource/org/openmdx/test/deployment.configuration.xml",
                        "xri:+resource/org/openmdx/test/compatibility/base/dataprovider/exporter/deployment.configuration.xml"
                    }
                );
                new LightweightContainer_1(
                    "TestExport",
                    CONNECTOR_DEPLOYMENT_UNITS,
                    PROVIDER_DEPLOYMENT_UNITS
                );
                deployed = true;
           }

            List outStreams = new ArrayList();
            outStreams.add(System.out);
            TreeStopWatch TSW = new TreeStopWatch();
            StopWatch_1.setStopWatch(TSW);
            // TSW.setStopReports(outStreams, true);
            
            this.serviceHeader = new ServiceHeader("test",null, false, new QualityOfService());
            sourceDataprovider = Dataprovider_1ConnectionFactoryImpl.createGenericConnection(
                SimpleServiceLocator.getInitialContext().lookup("org/openmdx/test/managing/explorer")
            );
            AppLog.info("sourceDataprovider is set up");
            
            Provider_1_0 sourceProviderLayer = new Provider_1(new RequestCollection(this.serviceHeader, sourceDataprovider), false);
            ObjectFactory_1_0 sourceManager = new Manager_1(new Connection_1(sourceProviderLayer, false));
            rootPkgForSource = new RefRootPackage_1(sourceManager, null, null);
            /* basePackage sourceGenericPackage = (basePackage) */ rootPkgForSource.refPackage("org:openmdx:base");
//            exportSegmentPath = new Path(
//                sourceGenericPackage.getProviderClass().getProvider(SOURCE_ROOT_PATH).getSegment(EXPORT_SEGMENT_ID);
            ClassicPackage pkg = (ClassicPackage)rootPkgForSource.refPackage("org:openmdx:test:compatibility:state1");
            PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(
                rootPkgForSource.refObject("org:openmdx:test:compatibility:state1")
            );
            persistenceManager.currentTransaction().begin();
    		sourceSegment = pkg.getSegment().getSegment(
    				new Path(_sourceSegmentPath)
    		);
            persistenceManager.currentTransaction().commit();

        } catch (ServiceException se) {
            se.printStackTrace(System.out);
            throw se;
        }
    }


    //---------------------------------------------------------------------------  
    protected void tearDown(
    ) {
      try {
          if (StopWatch_1.instance() instanceof TreeStopWatch ) {
              TreeStopWatch TSW = (TreeStopWatch) StopWatch_1.instance();
              TSW.printOut(this.getName(), new PrintWriter(System.out, true));
          }
                    
      }
      catch(Exception e) {
        System.out.println("error in deactivating");
      }
    }

    /**
     * setup the source DB
     * <p>
     * objectMap and stateMap get filled with the expected objects.
     * 
     * @param prepareTest   test for which to prepare
     * @throws Exception
     */
    protected void setUpSourceDB(
        short[] prepareTest,
        Map objectMap,
        Map stateMap
    ) throws Exception {
        StopWatch_1.instance().startTimer("setup_source");
        ClassicPackage statePackage = (ClassicPackage) 
            rootPkgForSource.refPackage("org:openmdx:test:compatibility:state1");
        
        Segment sourceModelSegment = createSourceSegment(statePackage);
        
        // first tear down the states in the db for making sure that the 
        // sequence in which the tests are executed doesn't matter.
        ExporterTestSetup.tearDownSourceDBPlain(
            statePackage,
            sourceModelSegment,
            _coreIdBase,
            10
        );
        
        ExporterTestSetup.tearDownSourceDBWithStates(
            statePackage, 
            sourceModelSegment, 
            STATE_ID_1, 
            STATE_ID_2, 
            stateMap, 
            objectMap
        );
        
        
        
        Arrays.sort(prepareTest);
        
        if (Arrays.binarySearch(prepareTest, TEST_PLAIN) >= 0) {
            ExporterTestSetup.setUpSourceDBPlain(
                statePackage, 
                sourceModelSegment,
                _coreIdBase, 
                objectMap,
                10
            );
        }
        /*
        if (Arrays.binarySearch(prepareTest, TEST_WITH_ROLES) >= 0) {
            TestSetup.setUpSourceDBWithRoles(
                statePackage, 
                sourceModelSegment, 
                _noRoleIdBase, 
                _coreIdBase, 
                _roleTypeIdX, 
                objectMap
            );
        }
        */


        if (Arrays.binarySearch(prepareTest, TEST_WITH_STATES) >= 0) {
            ExporterTestSetup.setUpSourceDBWithStates(
                statePackage, 
                sourceModelSegment, 
                STATE_ID_1, 
                STATE_ID_2, 
                stateMap, 
                objectMap
            );
        }
        
        /*
        if (Arrays.binarySearch(prepareTest, TEST_WITH_ROLESANDSTATES) >= 0) {
            TestSetup.setUpSourceDBWithRoleAndStates(
                statePackage,
                sourceModelSegment,
                _roledId1,
                _roledId2,
                _roledId3,
                _roledId4,
                _roleTypeId1,
                _roleTypeId2,
                stateMap,
                objectMap
            );
        }
        */

        StopWatch_1.instance().stopTimer("setup_source");
        
        StopWatch_1.instance().startTimer("check_source");
        // mainly for testing the check-routine (works only if the db routines are activated)
        ExporterTestHelper.checkDBForValidStates(sourceDataprovider, stateMap, new HashMap());
        ExporterTestHelper.checkDBForObjects(
            sourceDataprovider, 
            objectMap, 
            new HashMap(), 
            true, 
            prepareTest.length == 1 // the order is only correct for single tests
            && this.getName().equals("Jdbc")
        );
        StopWatch_1.instance().stopTimer("check_source");
    }
    
//  private short getTransactionBehavior() {
//      short behavior = TraversalHandler.NO_TRANSACTION;  // default
//       
//      if (Arrays.asList(_args).contains("--oneTransaction")) {
//          behavior = TraversalHandler.ONE_TRANSACTION;
//      }
//      else if (Arrays.asList(_args).contains("--objectTransaction")) {
//          behavior = TraversalHandler.OBJECT_TRANSACTION;
//      }
//      else if (Arrays.asList(_args).contains("--toplevelTransaction")) {
//          behavior = TraversalHandler.TOP_LEVEL_TRANSACTION;
//      }
//
//      return behavior;
//  }
        

    private Segment createSourceSegment(
        ClassicPackage statePackage
    ) {
        PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(
            rootPkgForSource.refObject("org:openmdx:test:compatibility:state1")
        );
        persistenceManager.currentTransaction().begin();
        //Segment sourceModelSegment = statePackage.getSegmentClass().createSegment();
        //sourceProvider.addSegment(EXPORT_SEGMENT_ID, sourceModelSegment);
        sourceSegment = statePackage.getSegment().getSegment(_sourceSegmentPath);
        persistenceManager.currentTransaction().commit();
        
        return sourceSegment;
    }

    


 
    protected Traverser setupTraverser(
        Model_1_0 model, 
        TraversalHandler th
    ) throws Exception {
        ArrayList startPoints = new ArrayList();
        
        startPoints.add(new Path(_sourceSegmentPath));

        ProviderTraverser traverser = new ProviderTraverser(
            this.serviceHeader,
            sourceDataprovider, 
            model, 
            startPoints,
            REFERENCE_FILTERS,
            ATTRIBUTE_FILTERS
        );

        traverser.setTraversalHandler(th);

        return traverser;

    }

    
    public void testJdbc() throws Throwable {
        doTestExportPlain();
        doTestExportStates();
    }
    
    public void testNone() throws Throwable {
        doTestExportPlain();
        doTestExportStates();
    }
    
    
    /**
     * "Test" for preparing the DB. Sets the DB up and tests that everything 
     * is set up correctly. Can be used for testing the Replicator program 
     * on defined DB state.
     * <p>
     * accepts options: 
     * --roles setup db with roles
     * --states  setup db with states
     * --rolesAndStates  setup db with states and roles
     * Default: setup db with all configurations
     * 
     * --sourceDB  setup source db 
     * --targetDB  setup target db
     * Default: setup both db
     * 
     * Source and Target DB initialization are not the same (obviously, otherwise
     * there would be nothing to replicate).
     *
     */
    public void doTestSetupDB() throws Throwable {
        try {
            // this is an interactive test for preparing the db, give some feedback
            System.out.println("*** testSetupDB ***");
            AppLog.info("*** testSetupDB ***");
            
            if (_args != null ) {
//	            boolean setSourceDB = false;
//	            boolean setTargetDB = false;
//	            boolean setBothDB = false;
	
	            short[] setupConfig = new short[3];
	            if (Arrays.asList(_args).contains("--roles")) {
	                setupConfig[0] = TEST_WITH_ROLES;
	            } else {
	                setupConfig[0] = TEST_UNDEF;
	            }
	
	            if (Arrays.asList(_args).contains("--states")) {
	                setupConfig[1] = TEST_WITH_STATES;
	            } else {
	                setupConfig[1] = TEST_UNDEF;
	            }
	
	            if (Arrays.asList(_args).contains("--rolesAndStates")) {
	                setupConfig[2] = TEST_WITH_ROLESANDSTATES;
	            } else {
	                setupConfig[2] = TEST_UNDEF;
	            }
	
	            if (setupConfig[0] == TEST_UNDEF && 
	                setupConfig[1] == TEST_UNDEF && 
	                setupConfig[2] == TEST_UNDEF
	            ) {
	                setupConfig[0] = TEST_WITH_ROLES;
	                setupConfig[1] = TEST_WITH_STATES;
	                setupConfig[2] = TEST_WITH_ROLESANDSTATES;
	            }
	
	
	            HashMap objectMap = new HashMap();
	            HashMap stateMap = new HashMap();
	
	            Arrays.sort(setupConfig);
	
	            System.out.println("testSetupDB: setup sourceDB with " + setupConfig[0] + ", " + setupConfig[1] + ", " + setupConfig[2]);
	            AppLog.info("testSetupDB: setup sourceDB with " + setupConfig[0] + ", " + setupConfig[1] + ", " + setupConfig[2]);
	            setUpSourceDB(setupConfig, objectMap, stateMap);
            }
        } catch (ServiceException se) {
            se.printStackTrace(System.out);
            throw se;
        }

    }
    
    
    public void doTestExportRoles() throws Throwable {
      //runXMLExporter(new short[] {TEST_WITH_ROLES}, "roles.xml");
        
    }
    public void doTestExportRolesAndStates() throws Throwable {
        //runXMLExporter(new short[] {TEST_WITH_ROLESANDSTATES}, "rolesAndStates.xml");
    }
    
    
    public void doTestExportStates() throws Throwable {
        String baseName = "states";
        String fileName = getName() + "_" + baseName;
        String correctFileName = getName() + "_" + "states_correct.xml";

        File outFile = File.createTempFile(fileName, ".xml");
        String outFilePath = outFile.getCanonicalPath();
        
        runXMLExporter(new short[] {TEST_WITH_STATES}, outFile);

        assertTrue(
            "exported file differs from its correct (" + outFilePath + ", " + correctFileName + ")", 
            compareXMLExportFiles(outFilePath, correctFileName)
        );

        // if we get here, the tmp file can be deleted because it is not needed
        // for comparing to the reference file
        outFile.deleteOnExit();
    }
    
    
    public void doTestExportPlain() throws Throwable {
        // add the test name (jdbc or none) to the exported files to keep the 
        // two versions. Otherwise the later test overwrites the first result.
        String baseName = "plain";
        String fileName = getName() + "_" + baseName;
        String correctFileName = getName() + "_" + "plain_correct.xml";
        
        File outFile = File.createTempFile(fileName, ".xml");
        String outFilePath = outFile.getCanonicalPath();
 
        runXMLExporter(new short[] {TEST_PLAIN}, outFile);
        
        assertTrue(
            "exported file differs from its correct (" + outFilePath + ", " + correctFileName + ")", 
            compareXMLExportFiles(outFilePath, correctFileName)
        );
        
        // if we get here, the tmp file can be deleted because it is not needed
        // for comparing to the reference file
        outFile.deleteOnExit();
    }
    
    /*public void testExportAll() throws Throwable {
        if (StopWatch_1.instance() instanceof TreeStopWatch) {
            ((TreeStopWatch) StopWatch_1.instance()).switchTest("ExportAll");
        }

        String fileName = new String("all.xml");
        String correctFileName = new String("impl/ch/omex/test/spice/dataprovider/exporter/all_correct.xml");
        runXMLExporter(new short[] {TEST_PLAIN, TEST_WITH_STATES, TEST_WITH_ROLES, TEST_WITH_ROLESANDSTATES}, fileName);
        
        assertTrue(
            "exported file differs from its correct (" + fileName + ", " + correctFileName + ")", 
            compareExportFiles(
                fileName,
                correctFileName
    
            )
        );
    }
    */

    /**
     * check if two elements qualify for comparison based on the default 
     * (element name) and the id.
     */
    static class ElementIDQualifier implements ElementQualifier {
        ElementNameQualifier defaultElementQualifier = new ElementNameQualifier();
        
        public boolean qualifyForComparison(Element control, Element test) {
            boolean qualifies = false;
            //System.out.println("qualifyForComparisone controlName: " + control.getLocalName() + " testName: " + test.getLocalName());
            
            // First try it with the default ElementNameQualifier
            qualifies = defaultElementQualifier.qualifyForComparison(control, test);
            
            // it may be that this is a bit to generous; check for the ids
            if (qualifies) {
                if (control.getLocalName().equals("org.openmdx.test.compatibility.state1.RoleClass") ||
                    control.getLocalName().equals("org.openmdx.test.compatibility.state1.RoleClassB") ||
                    control.getLocalName().equals("org.openmdx.test.compatibility.state1.RoleClassD")
                ) {
                    String idControl = control.getAttribute("id");
                    String idTest = test.getAttribute("id");
                    
                    qualifies = idControl.equals(idTest);
                    
                    // System.out.println("qualifyForComparisone controlId: " + idControl + " idTest: " + idTest);
                }
            }
            //System.out.println(" result " + qualifies);
               
            return qualifies;
        }
    }
    
    /**
     * avoid comparison of modification and creation date.
     */
    static class IgnoreDateDifferenceListener implements DifferenceListener {
        public int differenceFound(Difference diff) {
            int result = RETURN_ACCEPT_DIFFERENCE;
            Node controlNode = diff.getControlNodeDetail().getNode();
            Node testNode = diff.getTestNodeDetail().getNode();
            
            String controlParent = controlNode.getParentNode() == null ? null : controlNode.getParentNode().getLocalName();
            String testParent = testNode.getParentNode() == null ? null : testNode.getParentNode().getLocalName();
            
			if ((controlParent != null && (controlParent.equals("modifiedAt") || controlParent.equals("createdAt"))) ||
			    (testParent != null && (testParent.equals("modifiedAt") || testParent.equals("createdAt")))
			) {
			    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
			}
            
			// System.out.println("controlparentName " + controlParent + " testName " + testParent + " result " + result);
            // System.out.println("controlValue " + diff.getControlNodeDetail().getNode().getNodeValue() + " testValue " + diff.getTestNodeDetail().getNode().getNodeValue());
            // System.out.println("control " + diff.getControlNodeDetail().toString());
            return result;
        }
        
        public void skippedComparison(Node control, Node test) {
            // nothing
        }
    }
    
    /**
     * Compare the two files; they are equal if they differ only in the dates
     * contained (createdAt, ...) 
     * 
     * @param newFile
     * @param correctFile
     * @throws Exception
     */
    protected boolean compareXMLExportFiles(
        String newFile,
        String correctFile
    ) throws Exception {
        URL correctFileLocation = new URL("xri:+resource/org/openmdx/test/compatibility/base/dataprovider/exporter/");
//      String correctLine = null;
//      String resultLine = null;
                
        Reader fr = new FileReader(newFile);
        Reader correct = new InputStreamReader(
            new URL(correctFileLocation, correctFile).openStream()
        );
        
        Diff diff = new Diff(fr, correct);
        diff.overrideElementQualifier(new ElementIDQualifier());
        diff.overrideDifferenceListener(new IgnoreDateDifferenceListener());
        
        boolean similar = diff.similar();
        if (!similar) {
            System.out.println("diffs: " + diff.toString());
        }
        return similar;
        
    }

    
    /**
     * 
     * @param tests
     * @param outFileName
     * @throws Throwable
     */
    protected void runXMLExporter(
        short[] tests,
        File outFile
    ) throws Throwable {
        PrintStream pstream = null;
        KeepCauseErrorHandler kceh = new KeepCauseErrorHandler();

        try {

            AppLog.info("*** test XMLExportHandler ***");

            HashMap objectMap = new HashMap();
            HashMap stateMap = new HashMap();
            
            if (_doSetup) {
                setUpSourceDB(tests , objectMap, stateMap);
                // for export of provider; otherwise there would be to many objects
                // teardownTargetDB((state1Package) rootPkgForTarget.refPackage(state1Package.class.getName()));
            }

            Model_1 model = ExporterTestSetup.getModel();
            
            pstream = new PrintStream(new FileOutputStream(outFile), true);

            XmlContentHandler ch = setupContentHandler(pstream);

            XMLExportHandler xh =
                new XMLExportHandler(
                    model,
                    "http://www.w3.org/2001/XMLSchema-instance",
                    "./impl/ch/omex/test/state1/xmi/state1.xsd");

            xh.setContentHandler(ch);
            
            SystemPrintHandler sph = new SystemPrintHandler(xh);
            
            ArrayList startPoints = new ArrayList();
            Arrays.sort(tests);
            
            if (Arrays.binarySearch(tests, TEST_WITH_ROLES) >= 0 
                || Arrays.binarySearch(tests, TEST_WITH_ROLESANDSTATES) >= 0
                || Arrays.binarySearch(tests, TEST_WITH_STATES) >= 0
                || Arrays.binarySearch(tests, TEST_PLAIN) >= 0
            ) {
                startPoints.add(new Path(_sourceSegmentPath));
            }
            if (Arrays.binarySearch(tests, TEST_PLAIN) >= 0 ) {
                // export starting from reference (within provider exported before,
                // may later on lead to an exception)
                startPoints.add(_sourceSegmentPath.getChild("roleNoRole"));
            }

            ProviderTraverser traverser = new ProviderTraverser(
                this.serviceHeader,
                sourceDataprovider, 
                model, 
                startPoints,
                REFERENCE_FILTERS,
                ATTRIBUTE_FILTERS
            );
            traverser.setTraversalHandler(sph);
            
            traverser.setErrorHandler(kceh);

            traverser.traverse();
        } catch (ServiceException se) {
            se.printStackTrace(System.out);
            System.out.println("-- caused by: ");
            kceh.getCauseException().printStackTrace(System.out);
            throw se;
        } finally {
            if (pstream != null) {
                pstream.close();
            }
        }
    }


    protected XmlContentHandler setupContentHandler(
        PrintStream target
    ) throws Exception {        
        XmlContentHandler contentHandler = new XmlContentHandler(target);
          
        contentHandler.setAutoCollation(true);
        contentHandler.setEncoding("UTF-8");
        contentHandler.setIndentation(true);
        contentHandler.setIndentationLength(4);
          
        return contentHandler;
    }

}

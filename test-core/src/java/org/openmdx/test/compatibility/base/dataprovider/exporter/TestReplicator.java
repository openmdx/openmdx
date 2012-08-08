/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestReplicator.java,v 1.19 2007/02/02 14:42:31 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.19 $
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
 *   the documentation and/or other materials provided with the
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.test.compatibility.base.dataprovider.exporter;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.Transaction;
import javax.jmi.reflect.RefPackage;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.view.Manager_1;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1ConnectionFactoryImpl;
import org.openmdx.compatibility.base.application.container.SimpleServiceLocator;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.QualityOfService;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.exporter.ProviderTraverser;
import org.openmdx.compatibility.base.dataprovider.exporter.ReplicateHandler;
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
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.accessor.basic.spi.Model_1;
import org.openmdx.test.compatibility.base.dataprovider.exporter.ExporterTestSetup;

import org.openmdx.base.jmi.BasePackage;
import org.openmdx.test.compatibility.state1.classic.jmi.ClassicPackage;
import org.openmdx.base.cci.Provider;
import org.openmdx.test.compatibility.state1.classic.cci.Segment;

/**
 * 
 */
public class TestReplicator extends TestCase {

    /**
     * referenceFilters is an additional argument of the ProviderTraverser's constructors
     */
    static private List REFERENCE_FILTERS = Collections.EMPTY_LIST; // TODO verify its value

    /**
     * attributeFilters is an additional argument of the ProviderTraverser's constructors
     */
    static private Map ATTRIBUTE_FILTERS = Collections.EMPTY_MAP; // TODO Verfy its value
  
  /**
   * Helper class which provides public access to getClosestRoleStereotypeHierarchy
   * for single testing of this method.
   * @author anyff
   *
   * To change this generated comment go to 
   * Window>Preferences>Java>Code Generation>Code Template
   */
  static class TestProviderTraverser extends ProviderTraverser {
  
    public TestProviderTraverser(
      ServiceHeader header,
      Dataprovider_1_0 provider,
      Model_1_0 model,
      List sourcePaths
    ) throws NullPointerException{
      
      super(header, provider, model, sourcePaths, REFERENCE_FILTERS, ATTRIBUTE_FILTERS);
      
      _model = model;
    }
    
    /**
     * public access to method getClosestRoleStereotypeHierarchy() for testing.
     * 
     * @param className
     * @throws ServiceException
     */
    public List testGetClosestRoleStereotypeHierarchy(
        String className
    ) throws ServiceException {
      return getClosestRoleStereotypeHierarchy(
        _model.getDereferencedType(className),
        new ArrayList(),
        Integer.MAX_VALUE
      );
    }
    
    Model_1_0 _model;
  }


    static private String[] _args;
    static private boolean _doSetup = true;

    static private String SOURCE_SEGMENT_ID = "Source";
    static private String TARGET_SEGMENT_ID = "Target";
    
//    static private String EXPORT_SEGMENT_ID = "Export";

    private static final String STATE_ID_1 = "aState1";
    private static final String STATE_ID_2 = "aState2";
    private static final String _roledId1 = "exportRoleId1";
    private static final String _roledId2 = "exportRoleId2";
    private static final String _roledId3 = "exportRoleId3";
    private static final String _roledId4 = "exportRoleId4";
    private static final String _roleTypeId1 = "aRole1";
    private static final String _roleTypeId2 = "aRole2";

    private static final String _coreIdBase = "xbase";
    private static final String _noRoleIdBase = "xNoR";
    private static final String _roleTypeIdX = "xRole";

    private static final short TEST_WITH_ROLES = 1;
    private static final short TEST_WITH_STATES = 2;
    private static final short TEST_WITH_ROLESANDSTATES = 3;
    private static final short TEST_PLAIN = 4;
    private static final short TEST_UNDEF = 22;

    static final Path ROOT_PROVIDER_PATH = new Path("xri:@openmdx:org.openmdx.test.compatibility.state1/provider");
    static final Path SOURCE_ROOT_PATH = ROOT_PROVIDER_PATH.getChild("TestSource");
    static final Path TARGET_ROOT_PATH = ROOT_PROVIDER_PATH.getChild("TestTarget");
    static private final Path SOURCE_PATH = SOURCE_ROOT_PATH.getChild("segment").getChild(SOURCE_SEGMENT_ID);
    static private final Path TARGET_PATH = TARGET_ROOT_PATH.getChild("segment").getChild(TARGET_SEGMENT_ID);

    private ServiceHeader serviceHeader;
    private RefPackage rootPkgForSource;
    private RefPackage rootPkgForTarget;
    private Dataprovider_1_0 sourceDataprovider;
    private Dataprovider_1_0 targetDataprovider;
    private Provider targetProvider;
    private Provider sourceProvider;

    public TestReplicator(String name) {
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

        for (int i = 0; i < _args.length; i++) {
            if ("-t".equals(_args[i]) && _args.length > i + 1) {
                if (suite == null) {
                    suite = new TestSuite();
                }

                suite.addTest(new TestReplicator(_args[i + 1]));
            }
            
            if ("--noSetup".equals(_args[i])) {
                _doSetup = false;
            }
        }

        if (suite == null) {
            suite = new TestSuite(TestReplicator.class);
        }

        return suite;
    }

    protected void setUp() throws Exception {

        try {
            List outStreams = new ArrayList();
            outStreams.add(System.out);
            TreeStopWatch TSW = new TreeStopWatch();
            StopWatch_1.setStopWatch(TSW);
            // TSW.setStopReports(outStreams, true);
            
            this.serviceHeader = new ServiceHeader("test",null, false, new QualityOfService());
            sourceDataprovider = Dataprovider_1ConnectionFactoryImpl.createGenericConnection(
                SimpleServiceLocator.getInitialContext().lookup("ch/omex/dataprovider-1/NoOrNewTransaction/access")
            );
            Provider_1_0 sourceProviderLayer = new Provider_1(new RequestCollection(this.serviceHeader, sourceDataprovider), false);
            ObjectFactory_1_0 sourceManager = new Manager_1(new Connection_1(sourceProviderLayer, false));
            rootPkgForSource = new RefRootPackage_1(sourceManager, null, null);
            BasePackage sourceGenericPackage = (BasePackage) rootPkgForSource.refPackage("org:openmdx:base");
            sourceProvider = sourceGenericPackage.getProvider().getProvider(SOURCE_ROOT_PATH);

            targetDataprovider = sourceDataprovider;
            Provider_1_0 targetProviderLayer = new Provider_1(new RequestCollection(this.serviceHeader, sourceDataprovider), false);
            ObjectFactory_1_0 targetManager = new Manager_1(new Connection_1(targetProviderLayer, false));
            rootPkgForTarget = new RefRootPackage_1(targetManager, null, null);
            BasePackage targetGenericPackage = (BasePackage) rootPkgForTarget.refPackage("org:openmdx:base");
            targetProvider = targetGenericPackage.getProvider().getProvider(TARGET_ROOT_PATH);

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
        
        teardownSourceDB(statePackage);
        
        Segment sourceModelSegment = createSourceSegment(statePackage);
        
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


        if (Arrays.binarySearch(prepareTest, TEST_WITH_ROLES) >= 0) {
            ExporterTestSetup.setUpSourceDBWithRoles(
                statePackage, 
                sourceModelSegment, 
                _noRoleIdBase, 
                _coreIdBase, 
                _roleTypeIdX, 
                objectMap
            );
        }

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

        if (Arrays.binarySearch(prepareTest, TEST_WITH_ROLESANDSTATES) >= 0) {
            ExporterTestSetup.setUpSourceDBWithRoleAndStates(
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
        );
        StopWatch_1.instance().stopTimer("check_source");
    }
    
    private short getTransactionBehavior() {
        short behavior = TraversalHandler.NO_TRANSACTION;  // default
        
        if (Arrays.asList(_args).contains("--oneTransaction")) {
            behavior = TraversalHandler.ONE_TRANSACTION;
        }
        else if (Arrays.asList(_args).contains("--objectTransaction")) {
            behavior = TraversalHandler.OBJECT_TRANSACTION;
        }
        else if (Arrays.asList(_args).contains("--toplevelTransaction")) {
            behavior = TraversalHandler.TOP_LEVEL_TRANSACTION;
        }

        return behavior;
    }
        

    private Segment createSourceSegment(
        ClassicPackage statePackage
    ) {
        Transaction unitOfWork = JDOHelper.getPersistenceManager(targetProvider).currentTransaction();
        unitOfWork.begin();
        Segment sourceModelSegment = statePackage.getSegment().createSegment();
        sourceProvider.addSegment(SOURCE_SEGMENT_ID, sourceModelSegment);
        unitOfWork.commit();
        return sourceModelSegment;
    }

    private void teardownSourceDB(
        ClassicPackage statePackage
    ) {
        Transaction unitOfWork = JDOHelper.getPersistenceManager(targetProvider).currentTransaction();
        try {
            unitOfWork.begin();
            Segment sourceSegment = (Segment) sourceProvider.getSegment(SOURCE_SEGMENT_ID);
            sourceSegment.refDelete();
            unitOfWork.commit();
        } catch (JmiServiceException jse) {

            if (jse.getExceptionCode() != BasicException.Code.ASSERTION_FAILURE
                && jse.getExceptionCode() != BasicException.Code.NOT_FOUND
                && jse.getExceptionCode() != BasicException.Code.ABORT
            ) {
                throw jse;
            } else {
                
                //unitOfWork.rollback();
            }
        }
    }

    private Segment createTargetSegment(
        ClassicPackage statePackage
    ) {
        Transaction unitOfWork = JDOHelper.getPersistenceManager(targetProvider).currentTransaction();
        unitOfWork.begin();
        Segment targetModelSegment = statePackage.getSegment().createSegment();
        targetProvider.addSegment(TARGET_SEGMENT_ID, targetModelSegment);
        unitOfWork.commit();
        return targetModelSegment;
    }

    private void teardownTargetDB(
        ClassicPackage statePackage
    ) {
        Transaction unitOfWork = JDOHelper.getPersistenceManager(targetProvider).currentTransaction();
        try {
            unitOfWork.begin();
            Segment targetSegment = (Segment) targetProvider.getSegment(TARGET_SEGMENT_ID);
            targetSegment.refDelete();
            unitOfWork.commit();
        } catch (JmiServiceException jse) {

            if (jse.getExceptionCode() != BasicException.Code.ASSERTION_FAILURE
                && jse.getExceptionCode() != BasicException.Code.NOT_FOUND
                && jse.getExceptionCode() != BasicException.Code.ABORT
            ) {
                throw jse;
            } else {
                //unitOfWork.rollback();
            }
        }
    }

    /**
     * setup the target DB
     * 
     * @param pkg
     * @throws Exception
     */
    protected void setUpTargetDB(
        short[] prepareTest
    ) throws Exception {
        StopWatch_1.instance().startTimer("setup_target");

        ClassicPackage statePackage = (ClassicPackage) rootPkgForTarget.refPackage("org:openmdx:test:compatibility:state1");
        teardownTargetDB(statePackage);
        Segment targetModelSegment = createTargetSegment(statePackage);

        HashMap objectMapInternal = new HashMap();
        HashMap stateMapInternal = new HashMap();

        if (Arrays.binarySearch(prepareTest, TEST_WITH_ROLES) >= 0) {
            ExporterTestSetup.setUpTargetDBWithRoles(statePackage, targetModelSegment, _noRoleIdBase, _coreIdBase, _roleTypeIdX, objectMapInternal);
        }

        if (Arrays.binarySearch(prepareTest, TEST_WITH_STATES) >= 0) {
            ExporterTestSetup.setUpTargetDBWithStates(
                statePackage,
                targetModelSegment,
                STATE_ID_1,
                STATE_ID_2,
                stateMapInternal,
                objectMapInternal);
        }

        if (Arrays.binarySearch(prepareTest, TEST_WITH_ROLESANDSTATES) >= 0) {
            ExporterTestSetup.setUpTargetDBWithRoleAndStates(
                statePackage,
                targetModelSegment,
                _roledId1,
                _roledId2,
                _roledId3,
                _roleTypeId1,
                stateMapInternal,
                objectMapInternal);
        }
        StopWatch_1.instance().stopTimer("setup_target");


        // check that the db is as expected
        StopWatch_1.instance().startTimer("check_target");
        ExporterTestHelper.checkDBForValidStates(targetDataprovider, stateMapInternal, new HashMap());
        ExporterTestHelper.checkDBForObjects(
            targetDataprovider, 
            objectMapInternal, 
            new HashMap(), 
            true,
            prepareTest.length == 1 // the order is only correct for single tests);
        );
        
        StopWatch_1.instance().stopTimer("check_target");
    }

    /**
     * 
     */
    protected ReplicateHandler setupReplicateHandler(
        Model_1 model,
        short transactionBehavior
    ) throws Exception {
        Map pathMap = new HashMap();

        pathMap.put(new Path(SOURCE_PATH), new Path(TARGET_PATH));

        ReplicateHandler sh = new ReplicateHandler(this.serviceHeader, targetDataprovider, model, pathMap);

        sh.setTransactionBehavior(transactionBehavior);
        return sh;
    }

    protected Traverser setupTraverser(
        Model_1_0 model, 
        TraversalHandler th
    ) throws Exception {
        ArrayList startPoints = new ArrayList();
        
        startPoints.add(new Path(SOURCE_PATH));

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
    public void testSetupDB() throws Throwable {
        try {
            // this is an interactive test for preparing the db, give some feedback
            System.out.println("*** testSetupDB ***");

            boolean setSourceDB = false;
            boolean setTargetDB = false;
            boolean setBothDB = false;

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

            if (setupConfig[0] == TEST_UNDEF && setupConfig[1] == TEST_UNDEF && setupConfig[2] == TEST_UNDEF) {
                setupConfig[0] = TEST_WITH_ROLES;
                setupConfig[1] = TEST_WITH_STATES;
                setupConfig[2] = TEST_WITH_ROLESANDSTATES;
            }

            setSourceDB = Arrays.asList(_args).contains("--sourceDB");
            setTargetDB = Arrays.asList(_args).contains("--targetDB");
            setBothDB = !setSourceDB && !setTargetDB;

            HashMap objectMap = new HashMap();
            HashMap stateMap = new HashMap();

            Arrays.sort(setupConfig);

            if (setBothDB || setSourceDB) {
                System.out.println("testSetupDB: setup sourceDB with " + setupConfig[0] + ", " + setupConfig[1] + ", " + setupConfig[2]);

                setUpSourceDB(setupConfig, objectMap, stateMap);
            }

            if (setBothDB || setTargetDB) {
                System.out.println("testSetupDB: setup targetDB with " + setupConfig[0] + ", " + setupConfig[1] + ", " + setupConfig[2]);

                setUpTargetDB(setupConfig);
            }
        } catch (ServiceException se) {
            se.printStackTrace(System.out);
            throw se;
        }

    }
    
    public void testReplicateAll() throws Throwable {
        AppLog.trace("*** testReplicateAll ***");
        
        if (StopWatch_1.instance() instanceof TreeStopWatch) {
            ((TreeStopWatch) StopWatch_1.instance()).switchTest("ReplicateAll");
        }

        HashMap objectMap = new HashMap();
        HashMap stateMap = new HashMap();

        // must be setup in any case because objectMap and stateMap is needed
        if (_doSetup) {
            setUpSourceDB(new short[] { TEST_WITH_STATES, TEST_WITH_ROLES, TEST_WITH_ROLESANDSTATES }, objectMap, stateMap);
            setUpTargetDB(new short[] { TEST_WITH_STATES, TEST_WITH_ROLES, TEST_WITH_ROLESANDSTATES });
        }
        runReplicator(objectMap, stateMap);
    }
        

    public void testReplicateStates() throws Throwable {
        AppLog.trace("*** testReplicateStates ***");
        
        if (StopWatch_1.instance() instanceof TreeStopWatch) {
            ((TreeStopWatch) StopWatch_1.instance()).switchTest("ReplicateStates");
        }

        HashMap objectMap = new HashMap();
        HashMap stateMap = new HashMap();

        // must be setup in any case because objectMap and stateMap is needed
        if (_doSetup) {
            setUpSourceDB(new short[] { TEST_WITH_STATES }, objectMap, stateMap);
            setUpTargetDB(new short[] { TEST_WITH_STATES });
        }
        runReplicator(objectMap, stateMap);
    }

    public void testReplicateRoles() throws Throwable {
        AppLog.trace("*** testReplicateRoles ***");

        if (StopWatch_1.instance() instanceof TreeStopWatch) {
            ((TreeStopWatch) StopWatch_1.instance()).switchTest("ReplicateRoles");
        }


        HashMap objectMap = new HashMap();
        HashMap stateMap = new HashMap();

        if (_doSetup) {
            setUpSourceDB(new short[] { TEST_WITH_ROLES }, objectMap, stateMap);
            setUpTargetDB(new short[] { TEST_WITH_ROLES });
        }
        runReplicator(objectMap, stateMap);
    }

    public void testReplicateRolesAndStates() throws Throwable {
        AppLog.trace("*** testReplicateRolesAndStates ***");

        if (StopWatch_1.instance() instanceof TreeStopWatch) {
            ((TreeStopWatch) StopWatch_1.instance()).switchTest("ReplicateRolesAndStates");
        }

        HashMap objectMap = new HashMap();
        HashMap stateMap = new HashMap();
        
        if (_doSetup) {
            setUpSourceDB(new short[] { TEST_WITH_ROLESANDSTATES }, objectMap, stateMap);
            setUpTargetDB(new short[] { TEST_WITH_ROLESANDSTATES });
        }
        
        runReplicator(objectMap, stateMap);

    }
    
    protected void runReplicator(
        Map objectMap,
        Map stateMap
    ) throws Exception {
        try {
            StopWatch_1.instance().startTimer("replicate");
            Model_1 model = ExporterTestSetup.getModel();
    
            ReplicateHandler sh = setupReplicateHandler(model, getTransactionBehavior());
    
            SystemPrintHandler sph = new SystemPrintHandler(sh);
    
            Traverser traverser = setupTraverser(model, sph);
            traverser.traverse();
            StopWatch_1.instance().stopTimer("replicate");
    
            StopWatch_1.instance().startTimer("check_replicate");
            // check if target DB has the correct objects, states and roles
            HashMap pathMap = new HashMap();
            pathMap.put(SOURCE_PATH, TARGET_PATH);
            ExporterTestHelper.checkDBForObjects(
                targetDataprovider, 
                objectMap, 
                pathMap, 
                true,
                true
            );
            ExporterTestHelper.checkDBForValidStates(sourceDataprovider, stateMap, pathMap);
            StopWatch_1.instance().stopTimer("check_replicate");
    
        } catch (JmiServiceException je) {
            je.printStackTrace(System.out);
            throw je;
        } catch (ServiceException se) {
            se.printStackTrace(System.out);
            throw se;
        }
    }
        

    //---------------------------------------------------------------------------
    public void testExportRoles() throws Throwable {
        runXMLExporter(new short[] {TEST_WITH_ROLES}, "roles.xml");
    }
    public void testExportRolesAndStates() throws Throwable {
        runXMLExporter(new short[] {TEST_WITH_ROLESANDSTATES}, "rolesAndStates.xml");
    }
    public void testExportStates() throws Throwable {
        runXMLExporter(new short[] {TEST_WITH_STATES}, "states.xml");
    }
    public void testExportPlain() throws Throwable {
        runXMLExporter(new short[] {TEST_PLAIN}, "plain.xml");
    }
    public void testExportAll() throws Throwable {
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

    /**
     * Compare the two files; they are equal if they differ only in the dates
     * contained (createdAt, ...) 
     * 
     * @param newFile
     * @param correctFile
     * @throws Exception
     */
    protected boolean compareExportFiles(
        String newFile,
        String correctFile
    ) throws Exception {
        String correctLine = null;
        String resultLine = null;
        
        ArrayList dateEntries = new ArrayList();
        dateEntries.add("<object_modifiedAt>");
        dateEntries.add("<object_createdAt>");
        dateEntries.add("<modifiedAt>");
        dateEntries.add("<createdAt>");
        
        FileReader fr = new FileReader(newFile);
        BufferedReader resultReader = new BufferedReader(fr);
        
        FileReader correct = new FileReader(correctFile);
        BufferedReader correctReader = new BufferedReader(correct);
        
        correctLine = correctReader.readLine();
        resultLine = resultReader.readLine();
        while ((correctLine != null) && (resultLine != null)) {
            if (!correctLine.equals(resultLine)) {
                
                boolean equal = false;
                for (Iterator d = dateEntries.iterator(); d.hasNext() && !equal; ) {
                    String dateEntry = (String) d.next();
                    
                    equal = 
                        correctLine.indexOf(dateEntry) == resultLine.indexOf(dateEntry) 
                        && correctLine.indexOf(dateEntry) >= 0;
                }
                if (!equal) {
                   System.out.println("differing lines:");
                   System.out.println("correct: " + correctLine);
                   System.out.println("new:     " + resultLine);
                   return false;
                }
            }
            correctLine = correctReader.readLine();
            resultLine = resultReader.readLine();
        }
        if (correctLine != resultLine) { // both null is ok
            if (resultLine != null) {
                System.out.println("longer file: " + newFile);
                System.out.println("remaining line: " + resultLine);
            }
            else {
                System.out.println("longer file: " + correctFile);
                System.out.println("remaining line: " + correctLine);
            }
                
            System.out.println("one file is shorter");
            return false;
        }
        return true;
    }   
    


    protected void printHierarchy(
        String start,
        int expectedHierarchySize,
        TestProviderTraverser traverser
    ) throws ServiceException {
        System.out.println("Start: " + start);
                
        List hierarchy = traverser.testGetClosestRoleStereotypeHierarchy(start);
        int hierarchySize = 0;
        if (hierarchy != null) {
            for (Iterator h = hierarchy.iterator(); h.hasNext(); ) {
                ModelElement_1_0 superClass = (ModelElement_1_0) h.next();
                
                System.out.println("super: " + superClass.getValues("qualifiedName"));
            }
            hierarchySize = hierarchy.size();
            
        }
        else {
          System.out.println("hierarchy is empty");
        }
        
        assertEquals("hierarchy size not as expected", expectedHierarchySize, hierarchySize);
        
    }
            
    public void testGetClosestRoleStereotypeHierarchy(
    ) throws Throwable {
      PrintStream pstream = null;
      try {

          AppLog.trace("*** testRoleHierarchy ***");

          Model_1 model = ExporterTestSetup.getModel();
                    
          List sourcePaths = new ArrayList();
          sourcePaths.add("dummy");
          // reduced setup!!!
          TestProviderTraverser traverser = new TestProviderTraverser(
              this.serviceHeader,
              sourceDataprovider, 
              model, 
              sourcePaths
          );
          
            
          printHierarchy("ch:omex:test:state1:RoleClassB", 0, traverser);
          
          printHierarchy("ch:omex:test:state1:RoleClassRoleAExtension", 2, traverser);
          
          printHierarchy("ch:omex:test:state1:RoleClassRoleARoleA", 1, traverser);
          
          printHierarchy("ch:omex:test:state1:RoleClassFree", 0, traverser);
          
          printHierarchy("ch:omex:test:state1:RoleNoRole", 0, traverser);

      } catch (ServiceException se) {
          se.printStackTrace(System.out);
          throw se;
      } finally {
          if (pstream != null) {
              pstream.close();
          }
      }
  }
    
    protected void runXMLExporter(
        short[] tests,
        String outFileName
    ) throws Throwable {
        PrintStream pstream = null;
        try {

            AppLog.trace("*** test XMLExportHandler ***");

            HashMap objectMap = new HashMap();
            HashMap stateMap = new HashMap();
            
            if (_doSetup) {
                setUpSourceDB(tests , objectMap, stateMap);
                // for export of provider; otherwise there would be to many objects
                // teardownTargetDB((ClassicPackage) rootPkgForTarget.refPackage("org:openmdx:test:compatibility:state1"));
            }

            Model_1 model = ExporterTestSetup.getModel();
            pstream = new PrintStream(new FileOutputStream(outFileName), true);

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
                startPoints.add(new Path(SOURCE_PATH));
            }
            if (Arrays.binarySearch(tests, TEST_PLAIN) >= 0 ) {
                // export starting from reference (within provider exported before,
                // may later on lead to an exception)
                startPoints.add(SOURCE_PATH.getChild("roleNoRole"));
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

            traverser.traverse();
        } catch (ServiceException se) {
                  se.printStackTrace(System.out);
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

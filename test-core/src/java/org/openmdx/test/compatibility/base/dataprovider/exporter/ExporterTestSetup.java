/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ExporterTestSetup.java,v 1.6 2008/11/16 00:46:12 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/16 00:46:12 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.test.compatibility.base.dataprovider.exporter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.view.Manager_1;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1ConnectionFactoryImpl;
import org.openmdx.compatibility.base.application.container.SimpleServiceLocator;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.QualityOfService;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.transport.adapter.Provider_1;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Provider_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.delegation.Connection_1;
import org.openmdx.model1.accessor.basic.spi.Model_1;

import org.openmdx.base.jmi.BasePackage;
import org.openmdx.test.compatibility.state1.classic.jmi.ClassicPackage;
//#if defined(OPENMDX1)
import org.openmdx.base.cci.Provider;
import org.openmdx.base.cci.ProviderClass;
import org.openmdx.test.compatibility.state1.classic.cci.RoleAContained;
import org.openmdx.test.compatibility.state1.classic.cci.RoleAContainedClass;
import org.openmdx.test.compatibility.state1.classic.cci.RoleClass;
import org.openmdx.test.compatibility.state1.classic.cci.RoleClassB;
import org.openmdx.test.compatibility.state1.classic.cci.RoleClassBClass;
import org.openmdx.test.compatibility.state1.classic.cci.RoleClassClass;
import org.openmdx.test.compatibility.state1.classic.cci.RoleClassD;
import org.openmdx.test.compatibility.state1.classic.cci.RoleClassDClass;
import org.openmdx.test.compatibility.state1.classic.cci.RoleClassRoleA;
import org.openmdx.test.compatibility.state1.classic.cci.RoleClassRoleAClass;
import org.openmdx.test.compatibility.state1.classic.cci.RoleClassRoleARoleType;
import org.openmdx.test.compatibility.state1.classic.cci.RoleClassRoleARoleTypeClass;
import org.openmdx.test.compatibility.state1.classic.cci.RoleClassStateCRoleType;
import org.openmdx.test.compatibility.state1.classic.cci.RoleClassStateCRoleTypeClass;
import org.openmdx.test.compatibility.state1.classic.cci.RoleNoRole;
import org.openmdx.test.compatibility.state1.classic.cci.RoleNoRoleClass;
import org.openmdx.test.compatibility.state1.classic.cci.Segment;
import org.openmdx.test.compatibility.state1.classic.cci.StateA;
import org.openmdx.test.compatibility.state1.classic.cci.StateADerived;
import org.openmdx.test.compatibility.state1.classic.cci.StateADerivedClass;
import org.openmdx.test.compatibility.state1.classic.cci.StateC;
import org.openmdx.test.compatibility.state1.classic.cci.StateCClass;
import org.openmdx.test.compatibility.state1.classic.cci.StateCRole;
import org.openmdx.test.compatibility.state1.classic.cci.StateCRoleClass;
import org.openmdx.test.compatibility.state1.classic.cci.StateCRoleDepend;
import org.openmdx.test.compatibility.state1.classic.cci.StateCRoleDependClass;
import org.openmdx.test.compatibility.state1.classic.cci.StateD;
import org.openmdx.test.compatibility.state1.classic.cci.StateDClass;
//#else
//import java.net.URI;
//import java.net.URISyntaxException;
//import org.openmdx.base.jmi.Provider;
//import org.openmdx.base.jmi.ProviderClass;
//import org.openmdx.test.compatibility.state1.classic.jmi.RoleAContained;
//import org.openmdx.test.compatibility.state1.classic.jmi.RoleAContainedClass;
//import org.openmdx.test.compatibility.state1.classic.jmi.RoleClass;
//import org.openmdx.test.compatibility.state1.classic.jmi.RoleClassB;
//import org.openmdx.test.compatibility.state1.classic.jmi.RoleClassBClass;
//import org.openmdx.test.compatibility.state1.classic.jmi.RoleClassClass;
//import org.openmdx.test.compatibility.state1.classic.jmi.RoleClassD;
//import org.openmdx.test.compatibility.state1.classic.jmi.RoleClassDClass;
//import org.openmdx.test.compatibility.state1.classic.jmi.RoleClassRoleA;
//import org.openmdx.test.compatibility.state1.classic.jmi.RoleClassRoleAClass;
//import org.openmdx.test.compatibility.state1.classic.jmi.RoleClassRoleARoleType;
//import org.openmdx.test.compatibility.state1.classic.jmi.RoleClassRoleARoleTypeClass;
//import org.openmdx.test.compatibility.state1.classic.jmi.RoleClassStateCRoleType;
//import org.openmdx.test.compatibility.state1.classic.jmi.RoleClassStateCRoleTypeClass;
//import org.openmdx.test.compatibility.state1.classic.jmi.RoleNoRole;
//import org.openmdx.test.compatibility.state1.classic.jmi.RoleNoRoleClass;
//import org.openmdx.test.compatibility.state1.classic.jmi.Segment;
//import org.openmdx.test.compatibility.state1.classic.jmi.StateA;
//import org.openmdx.test.compatibility.state1.classic.jmi.StateADerived;
//import org.openmdx.test.compatibility.state1.classic.jmi.StateADerivedClass;
//import org.openmdx.test.compatibility.state1.classic.jmi.StateC;
//import org.openmdx.test.compatibility.state1.classic.jmi.StateCClass;
//import org.openmdx.test.compatibility.state1.classic.jmi.StateCRole;
//import org.openmdx.test.compatibility.state1.classic.jmi.StateCRoleClass;
//import org.openmdx.test.compatibility.state1.classic.jmi.StateCRoleDepend;
//import org.openmdx.test.compatibility.state1.classic.jmi.StateCRoleDependClass;
//import org.openmdx.test.compatibility.state1.classic.jmi.StateD;
//import org.openmdx.test.compatibility.state1.classic.jmi.StateDClass;
//#endif


/**
 * Setup data in Dataprovider.
 * 
 * @author anyff
 */
public class ExporterTestSetup {
  
    
    static public void tearDownSourceDBPlain(
        ClassicPackage pkg,
        Segment exportSegment,
        String coreBaseId,
        int  num
    ) throws Exception {
        PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(exportSegment);

        String roleNoRoleId1 = coreBaseId + "_rnr1";
        String roleNoRoleId2 = coreBaseId + "_rnr2";
        String[] rc_id = new String[num];
        String[] rcb_id = new String[num];

        for (int i = 0; i < num; i++) {
            String classId = coreBaseId + "_rc_" + String.valueOf(i);
            rc_id[i] = classId;
            String classBId = coreBaseId + "_rcb_" + String.valueOf(i);
            rcb_id[i] = classBId;
        }

        // first remove the objects which will be added later
        try {
            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            exportSegment.removeRoleNoRole(roleNoRoleId1);
            exportSegment.removeRoleNoRole(roleNoRoleId2);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();
            
            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            for (int i = 0; i < num; i++) {
                exportSegment.removeRoleAbstractRoot(rc_id[i]);
                exportSegment.removeRoleAbstractRoot(rcb_id[i]);
            }
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();
        }
        catch (JmiServiceException jse) {
            //
            // ignore not found exception
            //
            if (
                persistenceManager.currentTransaction().isActive() // pkg.refUnitOfWork().isActive()
            ) {
                persistenceManager.currentTransaction().rollback(); // pkg.refRollback();
            }
        }
    }
    
    /**
   * Set up the DB without roles or states.
   * <p>
   * Fast setup for testing export starting at a reference or a segment.  
   * <p>
   * Set up RoleClass and RoleClassB to their own segment, to test export of 
   * entire segment; set up RoleNoRole to test export starting at reference. 
   * <p>
   * The two segments exportSegment and refSegment may be the same, in which 
   * case certain objects get exported twice.
   * 
   * @param pkg
   * @param exportSegment     segment which will be exported entirely
   * @param refSegment        segment to export references from
   * @param roleNoRoleBaseId  
   * @param coreBaseId
   * @param roleTypeId
   * @param objectMap
   * @throws Exception
   */
  static public void setUpSourceDBPlain(
      ClassicPackage pkg,
      Segment exportSegment,
      String coreBaseId,
      Map objectMap,
      int  num
  ) throws Exception {
      // teardwon already executed.
      	
      DataproviderObject dpo = null;
      // define the id's
      String roleNoRoleId1 = coreBaseId + "_rnr1";
      String roleNoRoleId2 = coreBaseId + "_rnr2";
      String[] rc_id = new String[num];
      String[] rcb_id = new String[num];

      for (int i = 0; i < num; i++) {
          String classId = coreBaseId + "_rc_" + String.valueOf(i);
          rc_id[i] = classId;
          String classBId = coreBaseId + "_rcb_" + String.valueOf(i);
          rcb_id[i] = classBId;
      }

      
      // first the classes
      RoleClassBClass roleClassBClass = pkg.getRoleClassB(); // pkg.getRoleClassBClass();
      RoleClassClass roleClassClass = pkg.getRoleClass(); // pkg.getRoleClassClass();
      RoleNoRoleClass roleNoRoleClass = pkg.getRoleNoRole(); // pkg.getRoleNoRoleClass();
      RoleClassDClass roleClassDClass = pkg.getRoleClassD(); // pkg.getRoleClassDClass();
      PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(exportSegment);

      persistenceManager.currentTransaction().begin(); // pkg.refBegin();
      
      RoleNoRole roleNoRole1 = roleNoRoleClass.createRoleNoRole();
      roleNoRole1.setName(roleNoRoleId1);
      roleNoRole1.setAaaa(new String[] { "‰ƒˆ÷¸‹Ë»È…‡¿{}$£[]!'^~?'@¶|#<>&\"\\/" });
      exportSegment.addRoleNoRole(roleNoRoleId1, roleNoRole1);
            
      roleNoRoleId2 = coreBaseId + "_rnr2";
      RoleNoRole roleNoRole2 = roleNoRoleClass.createRoleNoRole();
      roleNoRole2.setName(roleNoRoleId2);
      roleNoRole2.setAaaa(new String[] { "‰ƒˆ÷¸‹Ë»È…‡¿{}$£[]!'^~?'@¶|#<>&\"\\/" });
      exportSegment.addRoleNoRole(roleNoRoleId2, roleNoRole2);
      persistenceManager.currentTransaction().commit(); // pkg.refCommit();
      
      ArrayList objects  = new ArrayList();      
      dpo = ExporterTestHelper.toDataproviderObject(roleNoRole1, getModel()); 
      objects.add(dpo);
      dpo = ExporterTestHelper.toDataproviderObject(roleNoRole2, getModel()); 
      objects.add(dpo);
      mapAdd(objectMap, dpo.path().getParent(), objects);

      ArrayList rNr1Objects = new ArrayList();
      ArrayList rNr2Objects = new ArrayList();
      ArrayList rcObjects = new ArrayList();
      ArrayList rcBObjects = new ArrayList();

      for (int i = 0; i < num; i++) {
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        String classId = rc_id[i];
        RoleClass roleClass = roleClassClass.createRoleClass();
        roleClass.setName(classId);
        roleClass.setRcName("roleClass");
        exportSegment.addRoleAbstractRoot(classId, roleClass);

        String classBId = rcb_id[i];
        RoleClassB roleClassB = roleClassBClass.createRoleClassB();
        roleClassB.setName(classId);
        roleClassB.setRcbName("roleClassB");
        exportSegment.addRoleAbstractRoot(classBId, roleClassB);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
                
        dpo = ExporterTestHelper.toDataproviderObject(roleClass, getModel()); 
        rcObjects.add(dpo);
        
        dpo = ExporterTestHelper.toDataproviderObject(roleClassB, getModel()); 
        rcBObjects.add(dpo);
        /* are this roles ?????*/
        for (int j = 1; j < 3; j++) {
          persistenceManager.currentTransaction().begin(); // pkg.refBegin();
          String classDId = coreBaseId + "_rcd" + String.valueOf(j) + "_" + String.valueOf(i);
          RoleClassD roleClassD = roleClassDClass.createRoleClassD();
          roleClassD.setRcdName(classDId);
          (j==1?roleNoRole1:roleNoRole2).addRoleClassD(classDId, roleClassD);
          persistenceManager.currentTransaction().commit(); // pkg.refCommit();
          
          dpo = ExporterTestHelper.toDataproviderObject(roleClassD, getModel()); 
          if (j==1) {
            rNr1Objects.add(dpo);
          }
          else {
            rNr2Objects.add(dpo);
          }
        } 
        
      }
        
      dpo = (DataproviderObject)rcObjects.get(0);
      mapAdd(objectMap, dpo.path().getParent(), rcObjects);
      dpo = (DataproviderObject)rcBObjects.get(0);
      mapAdd(objectMap, dpo.path().getParent(), rcBObjects);
      // dpo = (DataproviderObject)rNr1Objects.get(0);
      // mapAdd(objectMap, dpo.path().getParent(), rNr1Objects);
      // dpo = (DataproviderObject)rNr2Objects.get(0);
      // mapAdd(objectMap, dpo.path().getParent(), rNr2Objects);
  }
  
  
  
    /**
     * Set up the DB for testing Roles. 
     * <p>
     * Set up a RoleNoRole objects. Set up a RoleClassRoleA role which takes 
     * the RoleNoRole objects as a group. Like that the boundaries of the
     * synchronisation set can be tested.
     * 
     * objects created:
     * RoleType1   = RoleClassRoleARoleType(roleTypeId1)
     * RoleType2   = RoleClassRoleARoleType(roleTypeId2)
     * roleNoRole1 = RoleNoRole(noRoleId1),
     * roleNoRole2 = RoleNoRole(noRoleId2),
     * core1       = RoleClass(core1Id),
     * core2       = RoleClass(core2Id),
     * core1role1  = RoleClassRoleA(core1, roleType1),
     * core2role1  = RoleClassRoleA(core2, roleType1),
     * core1role2  = RoleClassRoleA(core1, roleType2)
     * 
     * core1role1.addToGroup(roleNoRole1)
     * core1role1.addToGroup(roleNoRole2);
     * core1role2.addToGroup(roleNoRole2)
     * 
     * 
     * @param pkg
     * @param modelSegment
     * @param id1
     * @param id2
     * @param roleTypeId
     * @param objectMap containing the objects and their states
     * @throws Exception
     */
    static public void setUpSourceDBWithRoles(
        ClassicPackage pkg,
        Segment modelSegment,
        String roleNoRoleBaseId,
        String coreBaseId,
        String roleTypeId,
        Map objectMap
    ) throws Exception {
        // first the classes
        RoleClassClass roleClassClass = pkg.getRoleClass(); // pkg.getRoleClassClass();
        RoleClassRoleARoleTypeClass roleARoleTypeClass = pkg.getRoleClassRoleARoleType(); // pkg.getRoleClassRoleARoleTypeClass();
        RoleClassRoleAClass roleAClass = pkg.getRoleClassRoleA(); // pkg.getRoleClassRoleAClass();
        RoleNoRoleClass roleNoRoleClass = pkg.getRoleNoRole(); // pkg.getRoleNoRoleClass();
        PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(modelSegment);
    
        String core1Id = coreBaseId + "_c1";
        String core2Id = coreBaseId + "_c2";
        String roleAContainedId = coreBaseId + "_rc1";
        String noRoleId1 = roleNoRoleBaseId + "_nr1";
        String noRoleId2 = roleNoRoleBaseId + "_nr2";
        String roleTypeId1 = roleTypeId + "_rt1";
        String roleTypeId2 = roleTypeId + "_rt2";
    
        // create role type
        RoleClassRoleARoleType roleType1 = roleARoleTypeClass.createRoleClassRoleARoleType();
        RoleClassRoleARoleType roleType2 = roleARoleTypeClass.createRoleClassRoleARoleType();
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addAbstractRootRoleType(roleTypeId1, roleType1);
        modelSegment.addAbstractRootRoleType(roleTypeId2, roleType2);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        RoleNoRole roleNoRole1 = roleNoRoleClass.createRoleNoRole("noRole 1111");
        roleNoRole1.setAaaa(new String[] { "‰‰‰‰0000" });
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addRoleNoRole(noRoleId1, roleNoRole1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject dpo1 = ExporterTestHelper.toDataproviderObject(roleNoRole1, getModel());
    
        RoleNoRole roleNoRole2 = roleNoRoleClass.createRoleNoRole("noRole 2222");
        roleNoRole2.setAaaa(new String[] { "‰‰‰‰1111" });
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addRoleNoRole(noRoleId2, roleNoRole2);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject dpo2 = ExporterTestHelper.toDataproviderObject(roleNoRole2, getModel());
    
        // create cores     
        RoleClass core1 = roleClassClass.createRoleClass("name value 1", "rcName value 1");
        core1.setAaaa(new String[] { "‰‰‰‰1111" });
        core1.setBbbb(new String[] { "bbbb1111" });
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addRoleAbstractRoot(core1Id, core1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject dpo3 = ExporterTestHelper.toDataproviderObject(core1, getModel());
    
        RoleClass core2 = roleClassClass.createRoleClass("name value 2", "rcName value 2");
        core2.setAaaa(new String[] { "‰‰‰‰2222" });
        core2.setBbbb(new String[] { "‰‰‰‰2222" });
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addRoleAbstractRoot(core2Id, core2);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject dpo4 = ExporterTestHelper.toDataproviderObject(core2, getModel());
    
        // add a role to core1 and core2
    
        // for RoleClassRoleA the qualifying attribute is rcraName, ident or id
        String rcraName1 = "rcraName1";
        String ident1 = "ident1";
    
        RoleClassRoleA roleA1 = roleAClass.extendRoleClass(core1, rcraName1);
        roleA1.setDddd(new String[] { "dddd1111" });
        roleA1.setIdent(ident1);
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        core1.addRole(roleTypeId1, roleA1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        String rcraName2 = "rcraName2";
        String ident2 = "ident2";
    
        RoleClassRoleA roleA2 = roleAClass.extendRoleClass(core2, rcraName2);
        roleA2.setDddd(new String[] { "dddd1111" });
        roleA2.setIdent(ident2);
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        core2.addRole(roleTypeId1, roleA2);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
        
        RoleAContainedClass roleAContainedClass = pkg.getRoleAContained(); // pkg.getRoleAContainedClass();
        RoleAContained roleAContained = roleAContainedClass.createRoleAContained("roleAContained");
        List values = new ArrayList();
        values.add(new Integer(1));
        values.add(new Integer(2));
        values.add(new Integer(3));
        values.add(new Integer(4));
        
        roleAContained.setNumber(values);
        
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        roleA2.addRoleAContained(roleAContainedId, roleAContained);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
        DataproviderObject dpo8 = ExporterTestHelper.toDataproviderObject(roleAContained, getModel());

    
        // add a second role to core1
        RoleClassRoleA role2A1 = roleAClass.extendRoleClass(core1, "rcraName3");
        role2A1.setDddd(new String[] { "dddd2222" });
        role2A1.setIdent("ident3");
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        core1.addRole(roleTypeId2, role2A1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject dpo7 = ExporterTestHelper.toDataproviderObject(role2A1, getModel());
    
        // add member to groups
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        roleA1.addGroup(roleNoRole1);
        roleA1.addGroup(roleNoRole2);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject dpo5 = ExporterTestHelper.toDataproviderObject(roleA1, getModel());
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        roleA2.addGroup(roleNoRole1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject dpo6 = ExporterTestHelper.toDataproviderObject(roleA2, getModel());
    
        // add objects to objectMap   
        ArrayList objects = new ArrayList();
    
        objects.add(dpo1);
        objects.add(dpo2);
        mapAdd(objectMap, dpo1.path().getParent(), objects);
    
        objects = new ArrayList();
        dpo3.values("object_hasRole").add(roleTypeId1);
        dpo3.values("object_hasRole").add(roleTypeId2);
        objects.add(dpo3);
        dpo4.values("object_hasRole").add(roleTypeId1);
        objects.add(dpo4);
        mapAdd(objectMap, dpo3.path().getParent(), objects);
        
        objects = new ArrayList();
        objects.add(dpo8);
        mapAdd(objectMap, dpo8.path().getParent(), objects);
    
        // the roles:
        objects = new ArrayList();
        objects.add(dpo5);
        mapAdd(objectMap, dpo5.path(), objects);
    
        objects = new ArrayList();
        objects.add(dpo6);
        mapAdd(objectMap, dpo6.path(), objects);
    
        objects = new ArrayList();
        objects.add(dpo7);
        mapAdd(objectMap, dpo7.path(), objects);
    }
    
    
    static public void tearDownSourceDBWithStates(
        ClassicPackage pkg,
        Segment modelSegment,
        String id1,
        String id2,
        Map stateMap,
        Map objectMap
    ) throws Exception {        
//      StateADerivedClass stateADerivedClass = pkg.getStateADerivedClass();
        PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(modelSegment);
        
        String d1id = id1 + "d1";
        String d2id = id1 + "d2";
        String d3id = id1 + "d3";
            
        // first remove the entries which will be created later on
        try {
            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            StateA a1 = modelSegment.getStateA(id1);
            a1.removeStateD(d1id);
            a1.removeStateD(d2id);
            a1.removeStateD(d3id);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();
            
            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            modelSegment.removeStateA(id1);
            modelSegment.removeStateA(id2);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();
        }
		catch (JmiServiceException jse) {
            //
            // ignore not found exception
            //
            if (
                persistenceManager.currentTransaction().isActive() // pkg.refUnitOfWork().isActive()
            ) {
                persistenceManager.currentTransaction().rollback(); // pkg.refRollback();
            }
        }
    }

    
    /**
     * Prepare DB for testing export.
     * 
     * @param objectMap : map containing the currently valid objects
     * @param statesMap: map containing all the states for a certain object
     * 
     */
    static public void setUpSourceDBWithStates(
        ClassicPackage pkg,
        Segment modelSegment,
        String id1,
        String id2,
        Map stateMap,
        Map objectMap
    ) throws Exception {        
        StateADerivedClass stateADerivedClass = pkg.getStateADerived(); // pkg.getStateADerivedClass();
        StateDClass stateDClass = pkg.getStateD(); //pkg.getStateDClass();
        PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(modelSegment);
        
        String d1id = id1 + "d1";
        String d2id = id1 + "d2";
        String d3id = id1 + "d3";
        
        // first remove the entries which will be created later on
        try {
            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            StateA a1 = modelSegment.getStateA(id1);
            a1.removeStateD(d1id);
            a1.removeStateD(d2id);
            a1.removeStateD(d3id);
            
            modelSegment.removeStateA(id1);
            modelSegment.removeStateA(id2);
           persistenceManager.currentTransaction().commit(); // pkg.refCommit();
        }
		catch (JmiServiceException jse) {
            //
            // ignore not found exception
            //
            if (
                persistenceManager.currentTransaction().isActive() // pkg.refUnitOfWork().isActive()
            ) {
                persistenceManager.currentTransaction().rollback(); // pkg.refRollback();
            }
        }
        
        // Create StateADerived instance a1
        StateADerived a1 = stateADerivedClass.createStateADerived();
        a1.setStateAttr("state_0");
        a1.setValue(new String[] { "A", id1 });
        a1.setStateADerived("1_derived_0");
        a1.setValueADerived(new String[] { "11AA", id1 });
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addStateA(id1, a1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        // copy initial state of a1 into a DataproviderObject
        DataproviderObject a1_ = ExporterTestHelper.toDataproviderObject(a1, getModel());
    
        // first update of a1
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        a1.setObject_validFrom(ExporterTestHelper.asDate("01.02.2002"));
        a1.setObject_validTo(ExporterTestHelper.asDate("01.10.2002"));
        a1.setStateADerived("1_derived_1");
        a1.setValueADerived(new String[] { "11BB" });
        a1.setValue(new String[] { "BB" });
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        // second update of a1
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        a1.setObject_validFrom(ExporterTestHelper.asDate("01.07.2002"));
        a1.setObject_validTo(ExporterTestHelper.asDate("01.11.2002"));
        a1.setStateADerived("1_derived_2");
        a1.setValueADerived(new String[] { "11CC", id1 });
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        ExporterTestHelper.showValidStates(modelSegment.getStateA(id1));
    
        // create DataproviderObjects for all valid states, based on the inital state
        DataproviderObject a1_s1 = ExporterTestHelper.createState(a1_, null, "01.02.2002");
        DataproviderObject a1_s2 = ExporterTestHelper.createState(a1_, "01.02.2002", "01.07.2002");
        DataproviderObject a1_s3 = ExporterTestHelper.createState(a1_, "01.07.2002", "01.10.2002");
        DataproviderObject a1_s4 = ExporterTestHelper.createState(a1_, "01.10.2002", "01.11.2002");
        DataproviderObject a1_s5 = ExporterTestHelper.createState(a1_, "01.11.2002", null);
    
        // adjust the values for each state
        a1_s2.clearValues("stateADerived").add("1_derived_1");
        a1_s2.clearValues("valueADerived").add("11BB");
        a1_s2.clearValues("value").add("BB");
    
        a1_s3.clearValues("stateADerived").add("1_derived_2");
        a1_s3.clearValues("valueADerived").add("11CC");
        a1_s3.clearValues("value").add("BB");
    
        a1_s4.clearValues("stateADerived").add("1_derived_2");
        a1_s4.clearValues("valueADerived").add("11CC");
    
        ArrayList states = new ArrayList();
        states.add(a1_s1);
        states.add(a1_s2);
        states.add(a1_s3);
        states.add(a1_s4);
        states.add(a1_s5);
    
        // add all states 
        stateMap.put(a1_.path(), states);
    
        // add the currently valid states 
        ArrayList objects = new ArrayList();
        objects.add(a1_s5);
        mapAdd(objectMap, a1_s5.path().getParent(), objects);
    
        StateADerived stateA2 = stateADerivedClass.createStateADerived();
        stateA2.setObject_validFrom(ExporterTestHelper.asDate("01.02.2002"));
        stateA2.setStateAttr("state_0");
        stateA2.setValue(new String[] { "A", id2 });
        stateA2.setStateADerived("2_derived_0");
        stateA2.setValueADerived(new String[] { "22AA", id2 });
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addStateA(id2, stateA2);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject a2_ = ExporterTestHelper.toDataproviderObject(stateA2, getModel());
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        stateA2.setObject_validFrom(ExporterTestHelper.asDate("01.08.2002"));
        stateA2.setStateADerived("2_derived_1");
        stateA2.setValueADerived(new String[] { "22BB" });
        stateA2.setValue(new String[] { "BB" });
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        stateA2.setObject_validFrom(ExporterTestHelper.asDate("01.04.2002"));
        stateA2.setObject_validTo(ExporterTestHelper.asDate("01.09.2002"));
        stateA2.setStateADerived("2_derived_2");
        stateA2.setValueADerived(new String[] { "22CC", id2 });
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        ExporterTestHelper.showValidStates(modelSegment.getStateA(id2));
    
        DataproviderObject a2_s1 = ExporterTestHelper.createState(a2_, "01.02.2002", "01.04.2002");
        DataproviderObject a2_s2 = ExporterTestHelper.createState(a2_, "01.04.2002", "01.08.2002");
        DataproviderObject a2_s3 = ExporterTestHelper.createState(a2_, "01.08.2002", "01.09.2002");
        DataproviderObject a2_s4 = ExporterTestHelper.createState(a2_, "01.09.2002", null);
    
        a2_s2.clearValues("stateADerived").add("2_derived_2");
        a2_s2.clearValues("valueADerived").addAll(Arrays.asList(new String[] { "22CC", id2 }));
    
        a2_s3.clearValues("stateADerived").add("2_derived_2");
        a2_s3.clearValues("valueADerived").addAll(Arrays.asList(new String[] { "22CC", id2 }));
        a2_s3.clearValues("value").add("BB");
    
        a2_s4.clearValues("stateADerived").add("2_derived_1");
        a2_s4.clearValues("valueADerived").addAll(Arrays.asList(new String[] { "22BB" }));
        a2_s4.clearValues("value").add("BB");
    
        states = new ArrayList();
        states.add(a2_s1);
        states.add(a2_s2);
        states.add(a2_s3);
        states.add(a2_s4);
        stateMap.put(a2_.path(), states);
    
        objects = new ArrayList();
        objects.add(a2_s4);
        mapAdd(objectMap, a2_s4.path().getParent(), objects);
    
        // Create a StateD instance
        StateD stateD1 = stateDClass.createStateD();
        stateD1.setStateAttr(new String("state_0"));
        stateD1.setAnyURIVal(newURI("ch:omex:testRole1/ch"));
        stateD1.setBooleanVal(new Boolean(true));
        stateD1.setDateVal(xmlDatatypeFactory().newXMLGregorianCalendar("2003-01-07"));
        stateD1.setDateTimeVal(ExporterTestHelper.asDate("01.07.2003"));
        stateD1.setDecimalVal(new BigDecimal(12));
        stateD1.setDurationVal(xmlDatatypeFactory().newDurationYearMonth(true, 2004, 11));
        stateD1.setIntegerVal(new Integer(12));
        stateD1.setLongVal(new Long(200000));
        stateD1.setShortVal(new Short((short) 200));
        
        // TODO check setup, try setting different values for second states.
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        a1.addStateD(d1id, stateD1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
        DataproviderObject d1dpo1 = ExporterTestHelper.toDataproviderObject(stateD1, getModel());
    
        // add a second state of stateD1
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        stateD1.setStateAttr(new String("state_1"));
        stateD1.setLongVal(new Long(300000));

        stateD1.setObject_validFrom(ExporterTestHelper.asDate("01.04.2002"));
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
        DataproviderObject d1dpo2 = ExporterTestHelper.toDataproviderObject(stateD1, getModel());
     
        // one full
        stateD1 = stateDClass.createStateD();
        stateD1.setStateAttr(new String("state_0"));
//        stateD1.getLongValues().values().addAll(
//                Arrays.asList(new Long[]{new Long(1)})
//            );

// versuch den wert anders zu setzen        Collection col = new ArrayList();
//        col.addAll(stateD1.getLongValues().values());
//        stateD1.;

        stateD1.setAnyURIVal(newURI("ch:omex:testRole1/ch"));
        stateD1.setBooleanVal(new Boolean(true));
        stateD1.setDateVal(xmlDatatypeFactory().newXMLGregorianCalendar("2003-01-07"));
        stateD1.setDateTimeVal(ExporterTestHelper.asDate("01.07.2003"));
        stateD1.setDecimalVal(new BigDecimal(12));
        stateD1.setDurationVal(xmlDatatypeFactory().newDurationYearMonth(true, 2004, 11));
        stateD1.setIntegerVal(new Integer(12));
        stateD1.setLongVal(new Long(200000));
        stateD1.setShortVal(new Short((short) 200));
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        a1.addStateD(d2id, stateD1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
        
        DataproviderObject d2dpo1 = ExporterTestHelper.toDataproviderObject(stateD1, getModel());
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        // one empty
        stateD1 = stateDClass.createStateD();
        
        a1.addStateD(d3id, stateD1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject d3dpo1 = ExporterTestHelper.toDataproviderObject(stateD1, getModel());
    
        states = new ArrayList();
        states.add(ExporterTestHelper.createExpected(d1dpo1, null, "01.04.2002"));
        states.add(ExporterTestHelper.createExpected(d1dpo2, "01.04.2002", null));
        stateMap.put(d1dpo1.path(), states);
    
        objects = new ArrayList();
        objects.add(ExporterTestHelper.createExpected(d1dpo2, "01.04.2002", null));
        objects.add(d2dpo1);
        objects.add(d3dpo1);
        mapAdd(objectMap, d3dpo1.path().getParent(), objects);
    
    }
    /**
     * setup target db for synchronizing with source db which was setup with
     * setUpDBWithRoles().
     * <p>
     * Setup objects which exist only in the target, those must be deleted during
     * synchronisation. Leave out some objects which exist in the source, those 
     * should be created in the target.
     * <p>
     * objects created:
     * 
     * RoleType1   = RoleClassRoleARoleType(roleTypeId1)
     * RoleType3   = RoleClassRoleARoleType(roleTypeId3)  // target only
     * roleNoRole1 = RoleNoRole(noRoleId1),
     * roleNoRole3 = RoleNoRole(noRoleId3),               // target only
     * core1       = RoleClass(core1Id),
     * core3       = RoleClass(core3Id),                  // target only
     * core1role1  = RoleClassRoleA(core1, roleType1),
     * core1role3  = RoleClassRoleA(core1, roleType3),    // target only
     * core3role1  = RoleClassRoleA(core3, roleType1)   
     * 
     * core1role1.addToGroup(roleNoRole1);   
     * core1role1.addToGroup(roleNoRole3);      // target only roleNoRole3
     * core1role3.addToGroup(roleNoRole1);      // target only core1role3
     * 
     * objects in source but not in target (which means they don't get created here)
     * RoleType2   = RoleClassRoleARoleType(roleTypeId2)
     * 
     * roleNoRole2 = RoleNoRole(noRoleId2),               
     * 
     * core2       = RoleClass(core2Id)
     * core2role1  = RoleClassRoleA(core2, roleType1),
     * core1role1.addToGroup(roleNoRole2)
     * core1role2.addToGroup(roleNoRole2)
     */
    static public void setUpTargetDBWithRoles(
        ClassicPackage pkg,
        Segment modelSegment,
        String roleNoRoleBaseId,
        String coreBaseId,
        String roleTypeId,
        Map objectMap
    ) throws Exception {
        // first the classes
        RoleClassClass roleClassClass = pkg.getRoleClass(); // pkg.getRoleClassClass();
        RoleClassRoleARoleTypeClass roleARoleTypeClass = pkg.getRoleClassRoleARoleType(); // pkg.getRoleClassRoleARoleTypeClass();
        RoleClassRoleAClass roleAClass = pkg.getRoleClassRoleA(); // pkg.getRoleClassRoleAClass();
        RoleNoRoleClass roleNoRoleClass = pkg.getRoleNoRole(); // pkg.getRoleNoRoleClass();
        PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(modelSegment);
    
        String core1Id = coreBaseId + "_c1";
        String core3Id = coreBaseId + "_c3";
        String noRoleId1 = roleNoRoleBaseId + "_nr1";
        String noRoleId3 = roleNoRoleBaseId + "_nr3";
        String roleTypeId1 = roleTypeId + "_rt1";
        String roleTypeId3 = roleTypeId + "_rt3";
    
        // create role type 
    
        RoleClassRoleARoleType roleType1 = roleARoleTypeClass.createRoleClassRoleARoleType();
        RoleClassRoleARoleType roleType3 = roleARoleTypeClass.createRoleClassRoleARoleType();
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addAbstractRootRoleType(roleTypeId1, roleType1);
        modelSegment.addAbstractRootRoleType(roleTypeId3, roleType3);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        RoleNoRole roleNoRole1 = roleNoRoleClass.createRoleNoRole("noRole 1111");
        roleNoRole1.setAaaa(new String[] { "‰‰‰‰0000" });
        roleNoRole1.setAaaa(new String[] { "targetOnly 6666" }); // target only
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addRoleNoRole(noRoleId1, roleNoRole1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject dpo1 = ExporterTestHelper.toDataproviderObject(roleNoRole1, getModel());
    
        RoleNoRole roleNoRole3 = // target only
        roleNoRoleClass.createRoleNoRole("noRole 3333"); // ...
        roleNoRole3.setAaaa(new String[] { "targetOnly 6666" }); // ...
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addRoleNoRole(noRoleId3, roleNoRole3);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject dpo2 = ExporterTestHelper.toDataproviderObject(roleNoRole3, getModel());
    
        // create cores     
        RoleClass core1 = roleClassClass.createRoleClass("name value 1", "rcName value 1");
        core1.setAaaa(new String[] { "‰‰‰‰1111" });
        core1.setBbbb(new String[] { "target diff 1111" }); // target diff
        core1.setAaaa(new String[] { "‰‰‰‰1111", "targetOnly 6666" });
        // target only
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addRoleAbstractRoot(core1Id, core1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject dpo3 = ExporterTestHelper.toDataproviderObject(core1, getModel());
    
        RoleClass core3 = // target only
        roleClassClass.createRoleClass("name value 3", "rcName value 3");
        core3.setAaaa(new String[] { "targetOnly 6666" });
        core3.setBbbb(new String[] { "targetOnly 6666" });
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addRoleAbstractRoot(core3Id, core3);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject dpo4 = ExporterTestHelper.toDataproviderObject(core3, getModel());
    
        // add a role to core1 and core2
    
        // for RoleClassRoleA the qualifying attribute is rcraName, ident or id
        String rcraName1 = "rcraName1";
        String ident1 = "ident1";
    
        RoleClassRoleA core1roleA1 = roleAClass.extendRoleClass(core1, rcraName1);
        core1roleA1.setDddd(new String[] { "dddd1111" });
        core1roleA1.setIdent(ident1);
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        core1.addRole(roleTypeId1, core1roleA1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        // must select values which do not collide with names existing in the
        // source (those are all qualifying attributes)
        String rcraName3 = "rcraName33";
        String ident3 = "ident33";
    
        RoleClassRoleA core3roleA1 = roleAClass.extendRoleClass(core3, rcraName3);
        core3roleA1.setDddd(new String[] { "dddd1111" });
        core3roleA1.setIdent(ident3);
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        core3.addRole(roleTypeId1, core3roleA1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        // add a third role to core1
        RoleClassRoleA core1roleA3 = roleAClass.extendRoleClass(core1, "rcraName44");
        core1roleA3.setDddd(new String[] { "dddd3333" });
        core1roleA3.setIdent("ident44");
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        core1.addRole(roleTypeId3, core1roleA3);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject dpo7 = ExporterTestHelper.toDataproviderObject(core3roleA1, getModel());
    
        // add member to groups
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        core1roleA1.addGroup(roleNoRole1);
        core1roleA1.addGroup(roleNoRole3);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject dpo5 = ExporterTestHelper.toDataproviderObject(core1roleA1, getModel());
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        core1roleA3.addGroup(roleNoRole1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject dpo6 = ExporterTestHelper.toDataproviderObject(core1roleA3, getModel());
    
        // add objects to objectMap
        ArrayList objects = new ArrayList();
    
        objects.add(dpo1);
        objects.add(dpo2);
        mapAdd(objectMap, dpo1.path().getParent(), objects);
    
        objects = new ArrayList();
        dpo3.values("object_hasRole").add(roleTypeId1);
        dpo3.values("object_hasRole").add(roleTypeId3);
        objects.add(dpo3);
        dpo4.values("object_hasRole").add(roleTypeId1);
        objects.add(dpo4);
    
        mapAdd(objectMap, dpo3.path().getParent(), objects);
    
        // the roles:
        objects = new ArrayList();
        objects.add(dpo5);
        mapAdd(objectMap, dpo5.path(), objects);
    
        objects = new ArrayList();
        objects.add(dpo6);
        mapAdd(objectMap, dpo6.path(), objects);
    
        objects = new ArrayList();
        objects.add(dpo7);
        mapAdd(objectMap, dpo7.path(), objects);
    
    }
    /**
     * Prepare target DB for testing synchronization.
     * <p>
     * The target objects are setup to test the behaviour of the synchronizer.
     * The states differ from the source states in the following manner:
     * <ul>
     * <li> a1 differs entirely from the source state id1, thus should be replaced entirely. </li>
     * <li> a2 is exactly the same as in source db, thus should remain untouched.</li>
     * <li> a3 exists only in the target provider, thus should be deleted.</li>
     * <li> d1 is exactly the same as in source db, thus should remain untouched. </li>
     * <li> d2 does not exist in target db, thus should be created. </li>
     * <li> d3 as d2, but an entirely empty object </li>
     * </ul>
     * 
     * @param objectMap : map containing the currently valid objects
     * @param statesMap: map containing all the states for a certain object
     */
    static public void setUpTargetDBWithStates(
        ClassicPackage pkg,
        Segment modelSegment,
        String id1,
        String id2,
        Map stateMap,
        Map objectMap
    ) throws Exception {
    
        StateADerivedClass stateADerivedClass = pkg.getStateADerived(); // pkg.getStateADerivedClass();
        StateDClass stateDClass = pkg.getStateD(); // pkg.getStateDClass(); 
        PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(modelSegment);
    
        // Create StateADerived instance a1, 
        // set a valid_from to further distinguish from source to see what happens
        StateADerived a1 = stateADerivedClass.createStateADerived();
        a1.setObject_validFrom(ExporterTestHelper.asDate("01.01.2000"));
        a1.setStateAttr("state_0");
        a1.setValue(new String[] { "A", id1 });
        a1.setStateADerived("1_derived_0");
        a1.setValueADerived(new String[] { "11AA", id1 });
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addStateA(id1, a1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        // copy initial state of a1 into a DataproviderObject
        DataproviderObject a1_ = ExporterTestHelper.toDataproviderObject(a1, getModel());
    
        // first update of a1
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        a1.setObject_validFrom(ExporterTestHelper.asDate("01.02.2010"));
        a1.setObject_validTo(ExporterTestHelper.asDate("01.10.2010"));
        a1.setStateADerived("1_derived_1");
        a1.setValueADerived(new String[] { "11BB" });
        a1.setValue(new String[] { "BB" });
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        // second update of a1
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        a1.setObject_validFrom(ExporterTestHelper.asDate("01.07.2010"));
        a1.setObject_validTo(ExporterTestHelper.asDate("01.11.2010"));
        a1.setStateADerived("1_derived_2");
        a1.setValueADerived(new String[] { "11CC", id1 });
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        ExporterTestHelper.showValidStates(modelSegment.getStateA(id1));
    
        // create DataproviderObjects for all valid states, based on the inital state
        DataproviderObject a1_s1 = ExporterTestHelper.createState(a1_, "01.01.2000", "01.02.2010");
        DataproviderObject a1_s2 = ExporterTestHelper.createState(a1_, "01.02.2010", "01.07.2010");
        DataproviderObject a1_s3 = ExporterTestHelper.createState(a1_, "01.07.2010", "01.10.2010");
        DataproviderObject a1_s4 = ExporterTestHelper.createState(a1_, "01.10.2010", "01.11.2010");
        DataproviderObject a1_s5 = ExporterTestHelper.createState(a1_, "01.11.2010", null);
    
        // adjust the values for each state
        a1_s2.clearValues("stateADerived").add("1_derived_1");
        a1_s2.clearValues("valueADerived").add("11BB");
        a1_s2.clearValues("value").add("BB");
    
        a1_s3.clearValues("stateADerived").add("1_derived_2");
        a1_s3.clearValues("valueADerived").add("11CC");
        a1_s3.clearValues("value").add("BB");
    
        a1_s4.clearValues("stateADerived").add("1_derived_2");
        a1_s4.clearValues("valueADerived").add("11CC");
    
        ArrayList states = new ArrayList();
        states.add(a1_s1);
        states.add(a1_s2);
        states.add(a1_s3);
        states.add(a1_s4);
        states.add(a1_s5);
    
        // add all states 
        stateMap.put(a1_.path(), states);
    
        // add the currently valid states 
        ArrayList objects = new ArrayList();
        objects.add(a1_s1); 
        mapAdd(objectMap, a1_s1.path().getParent(), objects);
    
        // Create StateADerived instance a2 with all states like in source
        StateADerived a2 = stateADerivedClass.createStateADerived();
        a2.setObject_validFrom(ExporterTestHelper.asDate("01.02.2002"));
        a2.setStateAttr("state_0");
        a2.setValue(new String[] { "A", id2 });
        a2.setStateADerived("2_derived_0");
        a2.setValueADerived(new String[] { "22AA", id2 });
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addStateA(id2, a2);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject a2_ = ExporterTestHelper.toDataproviderObject(a2, getModel());
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        a2.setObject_validFrom(ExporterTestHelper.asDate("01.08.2002"));
        a2.setStateADerived("2_derived_1");
        a2.setValueADerived(new String[] { "22BB" });
        a2.setValue(new String[] { "BB" });
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        a2.setObject_validFrom(ExporterTestHelper.asDate("01.04.2002"));
        a2.setObject_validTo(ExporterTestHelper.asDate("01.09.2002"));
        a2.setStateADerived("2_derived_2");
        a2.setValueADerived(new String[] { "22CC", id2 });
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        ExporterTestHelper.showValidStates(modelSegment.getStateA(id2));
    
        DataproviderObject a2_s1 = ExporterTestHelper.createState(a2_, "01.02.2002", "01.04.2002");
        DataproviderObject a2_s2 = ExporterTestHelper.createState(a2_, "01.04.2002", "01.08.2002");
        DataproviderObject a2_s3 = ExporterTestHelper.createState(a2_, "01.08.2002", "01.09.2002");
        DataproviderObject a2_s4 = ExporterTestHelper.createState(a2_, "01.09.2002", null);
    
        a2_s2.clearValues("stateADerived").add("2_derived_2");
        a2_s2.clearValues("valueADerived").addAll(Arrays.asList(new String[] { "22CC", id2 }));
    
        a2_s3.clearValues("stateADerived").add("2_derived_2");
        a2_s3.clearValues("valueADerived").addAll(Arrays.asList(new String[] { "22CC", id2 }));
        a2_s3.clearValues("value").add("BB");
    
        a2_s4.clearValues("stateADerived").add("2_derived_1");
        a2_s4.clearValues("valueADerived").addAll(Arrays.asList(new String[] { "22BB" }));
        a2_s4.clearValues("value").add("BB");
    
        states = new ArrayList();
        states.add(a2_s1);
        states.add(a2_s2);
        states.add(a2_s3);
        states.add(a2_s4);
        stateMap.put(a2_.path(), states);
    
        objects = new ArrayList();
        objects.add(a2_s4);
        mapAdd(objectMap, a2_s4.path().getParent(), objects);
    
        // Create a3, this instance has to be removed after synchronisation!
        StateADerived a3 = stateADerivedClass.createStateADerived();
        a3.setObject_validFrom(ExporterTestHelper.asDate("01.04.2002"));
        a3.setObject_validFrom(ExporterTestHelper.asDate("01.05.2002"));
        a3.setStateAttr("state_0");
        a3.setValue(new String[] { "target_only" });
        a3.setStateADerived("target_only");
        a3.setValueADerived(new String[] { "target_only", id2 });
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addStateA("aState3", a3);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject a3_ = ExporterTestHelper.toDataproviderObject(a3, getModel());
    
        states = new ArrayList();
        states.add(a3_);
        stateMap.put(a3_.path(), states);
    
        objects = new ArrayList();
        objects.add(a3_);
        mapAdd(objectMap, a3_.path().getParent(), objects);
    
        // Create a StateD instance
        StateD stateD1 = stateDClass.createStateD();
        String d1id = id1 + "d1";
        stateD1.setStateAttr(new String("state_0"));
        stateD1.setAnyURIVal(newURI("ch:omex:testRole1/ch"));
        stateD1.setBooleanVal(new Boolean(true));
        stateD1.setDateVal(xmlDatatypeFactory().newXMLGregorianCalendar("2003-01-07"));
        stateD1.setDateTimeVal(ExporterTestHelper.asDate("01.07.2003"));
        stateD1.setDecimalVal(new BigDecimal(12));
        stateD1.setDurationVal(xmlDatatypeFactory().newDurationYearMonth(true, 2004, 11));
        stateD1.setIntegerVal(new Integer(12));
        stateD1.setLongVal(new Long(200000));
        stateD1.setShortVal(new Short((short) 200));
        stateD1.getLongValues().values().addAll(
            Arrays.asList(new Long[]{new Long(2)})
        );
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        a1.addStateD(d1id, stateD1);
        DataproviderObject d1dpo1 = ExporterTestHelper.toDataproviderObject(stateD1, getModel());
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        // add a second state of stateD1
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        stateD1.setStateAttr(new String("state_1"));
        stateD1.getLongValues().values().addAll(
            Arrays.asList(new Long[]{new Long(2)})
        );
        stateD1.setObject_validFrom(ExporterTestHelper.asDate("01.04.2002"));
        DataproviderObject d1dpo2 = ExporterTestHelper.toDataproviderObject(stateD1, getModel());
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        // one full
        stateD1 = stateDClass.createStateD();
        String d2id = id1 + "d2";
        stateD1.setStateAttr(new String("state_0"));
        stateD1.getLongValues().values().addAll(
            Arrays.asList(new Long[]{new Long(2)})
        );
 
        stateD1.setAnyURIVal(newURI("ch:omex:testRole1/ch"));
        stateD1.setBooleanVal(new Boolean(true));
        stateD1.setDateVal(xmlDatatypeFactory().newXMLGregorianCalendar("2003-01-07"));
        stateD1.setDateTimeVal(ExporterTestHelper.asDate("01.07.2003"));
        stateD1.setDecimalVal(new BigDecimal(12));
        stateD1.setDurationVal(xmlDatatypeFactory().newDurationYearMonth(true, 2004, 11));
        stateD1.setIntegerVal(new Integer(12));
        stateD1.setLongVal(new Long(200000));
        stateD1.setShortVal(new Short((short) 200));
    
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        a1.addStateD(d2id, stateD1);
    
        DataproviderObject d2dpo1 = ExporterTestHelper.toDataproviderObject(stateD1, getModel());
    
        // one empty
        stateD1 = stateDClass.createStateD();
        String d3id = id1 + "d3";
        a1.addStateD(d3id, stateD1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
    
        DataproviderObject d3dpo1 = ExporterTestHelper.toDataproviderObject(stateD1, getModel());
    
        states = new ArrayList();
        states.add(ExporterTestHelper.createExpected(d1dpo1, null, "01.04.2002"));
        states.add(ExporterTestHelper.createExpected(d1dpo2, "01.04.2002", null));
        stateMap.put(d1dpo1.path(), states);
    
        objects = new ArrayList();
        objects.add(ExporterTestHelper.createExpected(d1dpo2, "01.04.2002", null));
        objects.add(d2dpo1);
        objects.add(d3dpo1);
        mapAdd(objectMap, d3dpo1.path().getParent(), objects);
    
    }

//#if defined(OPENMDX1)
	private static final String newURI(
		String value
    ){
    	return new String(value);
    }
//#else
//	private static final URI newURI(
//		String value
//  ) throws URISyntaxException{
//    	return new URI(value);
//  }
//#endif

    /**
     * Add the list to the map, if the list is already contained, just add the
     * new entries.
     * 
     * @param map
     * @param key
     * @param list
     */
    private static Model_1 _model;

    public static Model_1 getModel() throws ServiceException {
        if (_model == null) {
            createModel();
        }
        return _model;
    }

    /**
     * create the model with default models.
     * 
     * @throws ServiceException
     */
    private static void createModel() throws ServiceException {
        _model = new Model_1();
        _model.addModels(
            Arrays.asList(
                new String[] { 
                    "org:w3c", 
                    "org:openmdx:base", 
                    "org:openmdx:test:compatibility:state1"
                }
            )
        );
    }
    

    static private void mapAdd(Map map, Object key, List list) {
        if (map.containsKey(key)) {
            List mapEntry = (List) map.get(key);
            mapEntry.addAll(list);
        } else {
            map.put(key, list);
        }
    }
    

    /**
     * Set up the DB for testing Roles and States. 
     * <p>
     * Add the valid states to the objectMap, which contains for each reference
     * all the valid states as a DataproviderObject in a list. Roles of an 
     * object must be treated separately, with their own reference.
     * 
     * @param pkg
     * @param modelSegment
     * @param id1
     * @param id2
     * @param roleTypeId
     * @param stateMap  contains the valid states of an object
     * @param objectMap contains the (current) objects 
     * @throws Exception
     */
    static public void setUpSourceDBWithRoleAndStates(
        ClassicPackage pkg,
        Segment modelSegment,
        String id1,
        String id2,
        String id3,
        String id4,
        String roleTypeId1,
        String roleTypeId2,
        Map stateMap,
        Map objectMap
    ) throws Exception {

        StateCClass stateCClass = pkg.getStateC(); // pkg.getStateCClass();
        StateCRoleClass stateCRoleClass = pkg.getStateCRole(); // pkg.getStateCRoleClass();
        StateCRoleDependClass stateCRoleDependClass = pkg.getStateCRoleDepend(); // pkg.getStateCRoleDependClass();
        RoleClassStateCRoleTypeClass stateCRoleTypeClass = pkg.getRoleClassStateCRoleType(); // pkg.getRoleClassStateCRoleTypeClass();
        PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(modelSegment);

        // first create role type
        RoleClassStateCRoleType role1 = stateCRoleTypeClass.createRoleClassStateCRoleType();       

        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addStateCRoleType(roleTypeId1, role1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();
        
        RoleClassStateCRoleType role2 = stateCRoleTypeClass.createRoleClassStateCRoleType();
        
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        modelSegment.addStateCRoleType(roleTypeId2, role2);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();


        {
            // create a base object
            StateC c1 = stateCClass.createStateC(id1);
            c1.setObject_validFrom(ExporterTestHelper.asDate("01.01.2001"));
            c1.setValue(new String[] { "state1" });

            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            modelSegment.addStateC(id1, c1);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();

            DataproviderObject c1_ = ExporterTestHelper.toDataproviderObject(c1, getModel());

            // add  a state
            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            c1.setValue(new String[] { "state2" });
            c1.setObject_validFrom(ExporterTestHelper.asDate("01.09.2001"));
            c1.setObject_validTo(ExporterTestHelper.asDate("01.09.2002"));
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();

            // add a role
            StateCRole cRole = stateCRoleClass.extendRole(c1, "state3", "role1");

            cRole.setNum(new String[] { "first ƒ" });
            cRole.setObject_validFrom(ExporterTestHelper.asDate("01.11.2001"));
            cRole.setObject_validTo(ExporterTestHelper.asDate("01.12.2001"));

            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            c1.addRole(roleTypeId1, cRole);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();

            DataproviderObject dpo4 = ExporterTestHelper.toDataproviderObject(c1, getModel());

            // add a second role (leaping over the last states end)
            cRole = stateCRoleClass.extendRole(c1, "state4", "role2");
            cRole.setNum(new String[] { "second ƒ" });
            cRole.setObject_validFrom(ExporterTestHelper.asDate("01.08.2002"));
            cRole.setObject_validTo(ExporterTestHelper.asDate("01.11.2002"));

            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            c1.addRole(roleTypeId1, cRole);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();

            DataproviderObject dpo6 = ExporterTestHelper.toDataproviderObject(c1, getModel());

            // setup valid states array with all the states produced in the correct order  
            ArrayList validStates = new ArrayList();

            DataproviderObject c1_s1 = ExporterTestHelper.createState(c1_, "01.01.2001", "01.09.2001");
            DataproviderObject c1_s2 = ExporterTestHelper.createState(c1_, "01.09.2001", "01.11.2001");
            DataproviderObject c1_s3 = ExporterTestHelper.createState(c1_, "01.11.2001", "01.12.2001");
            DataproviderObject c1_s4 = ExporterTestHelper.createState(c1_, "01.12.2001", "01.08.2002");
            DataproviderObject c1_s5 = ExporterTestHelper.createState(c1_, "01.08.2002", "01.09.2002");
            DataproviderObject c1_s6 = ExporterTestHelper.createState(c1_, "01.09.2002", "01.11.2002");
            DataproviderObject c1_s7 = ExporterTestHelper.createState(c1_, "01.11.2002", null);

            c1_s2.clearValues("value").addAll(Arrays.asList(new String[] { "state2" }));
            c1_s3.clearValues("value").addAll(Arrays.asList(new String[] { "state2" }));
            c1_s3.clearValues("object_hasRole").set(0, roleTypeId1);
            c1_s4.clearValues("value").addAll(Arrays.asList(new String[] { "state2" }));
            c1_s5.clearValues("object_hasRole").set(0, roleTypeId1);
            c1_s5.clearValues("value").addAll(Arrays.asList(new String[] { "state2" }));
            c1_s6.clearValues("object_hasRole").set(0, roleTypeId1);

            validStates.add(c1_s1);
            validStates.add(c1_s2);
            validStates.add(c1_s3);
            validStates.add(c1_s4);
            validStates.add(c1_s5);
            validStates.add(c1_s6);
            validStates.add(c1_s7);

            stateMap.put(c1_.path(), validStates);

            ExporterTestHelper.showValidStates(modelSegment.getStateC(id1));

            ArrayList roleStates = new ArrayList();
            roleStates.add(dpo4);
            roleStates.add(dpo6);
            stateMap.put(dpo4.path().add("role").add(roleTypeId1), roleStates);

            // prepare object map
            ArrayList objects = new ArrayList();
            objects.add(c1_s7);
            mapAdd(objectMap, c1_.path().getParent(), objects);
        }
        {
            // create a base object
            StateC c1 = stateCClass.createStateC(id2);
            c1.setObject_validFrom(ExporterTestHelper.asDate("01.01.2000"));
            c1.setValue(new String[] { "state1" });

            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            modelSegment.addStateC(id2, c1);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();
            DataproviderObject c1_ = ExporterTestHelper.toDataproviderObject(c1, getModel());

            // add  a state
            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            c1.setValue(new String[] { "state2" });
            c1.setObject_validFrom(ExporterTestHelper.asDate("01.06.2000"));
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();

            // add a role
            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            StateCRole cRole = stateCRoleClass.extendRole(c1, "state5", "role3");
            cRole.setNum(new String[] { "first" });
            cRole.setObject_validFrom(ExporterTestHelper.asDate("01.07.2000"));

            c1.addRole(roleTypeId1, cRole);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();

            DataproviderObject dpo4 = ExporterTestHelper.toDataproviderObject(c1, getModel());
            dpo4.values("object_inRole").add(roleTypeId1);

            // setup valid states array with all the states produced in the correct order  
            ArrayList validStates = new ArrayList();
            DataproviderObject c1_s1 = ExporterTestHelper.createState(c1_, "01.01.2000", "01.06.2000");
            DataproviderObject c1_s2 = ExporterTestHelper.createState(c1_, "01.06.2000", "01.07.2000");
            DataproviderObject c1_s3 = ExporterTestHelper.createState(c1_, "01.07.2000", null);

            c1_s2.clearValues("value").addAll(Arrays.asList(new String[] { "state2" }));
            c1_s3.clearValues("value").addAll(Arrays.asList(new String[] { "state2" }));
            c1_s3.clearValues("object_hasRole").set(0, roleTypeId1);
            validStates.add(c1_s1);
            validStates.add(c1_s2);
            validStates.add(c1_s3);
            stateMap.put(c1_.path(), validStates);

            ArrayList roleStates = new ArrayList();
            roleStates.add(dpo4);
            stateMap.put(dpo4.path().add("role").add(roleTypeId1), roleStates);

            // Prepare object map
            ArrayList objects = new ArrayList();
            objects.add(c1_s3);
            mapAdd(objectMap, c1_.path().getParent(), objects);
        }

        {
            // create a base object 
            StateC c1 = stateCClass.createStateC(id3);
            c1.setObject_validFrom(ExporterTestHelper.asDate("01.01.2010"));
            c1.setObject_validTo(ExporterTestHelper.asDate("01.01.2020"));
            c1.setValue(new String[] { "state1" });

            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            modelSegment.addStateC(id3, c1);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();

            TimedAccess access = getModelSegmentAtDate("01.01.2012", "Source");
            Segment futureModelSegment = access.segment;

            c1 = futureModelSegment.getStateC(id3);

            DataproviderObject c1_ = ExporterTestHelper.toDataproviderObject(c1, getModel());

            // add a role
            // both attributes are changed: "stateAttr" of core and "name" of role
            StateCRole cRole = stateCRoleClass.extendRole(c1, "state33", "role33");
            // must be deleted in target! cRole.addNum("first");
            cRole.setObject_validFrom(ExporterTestHelper.asDate("01.07.2011"));
            cRole.setObject_validTo(ExporterTestHelper.asDate("01.07.2013"));

            access.getTransaction().begin(); // access.statePackage.refBegin();
            c1.addRole(roleTypeId1, cRole);
            access.getTransaction().commit(); // access.statePackage.refCommit();

            DataproviderObject dpo2 = ExporterTestHelper.toDataproviderObject(cRole, getModel());

            // setup valid states array with all the states produced in the correct order  
            ArrayList validStates = new ArrayList();

            DataproviderObject c1_s1 = ExporterTestHelper.createState(c1_, "01.01.2010", "01.07.2011");
            DataproviderObject c1_s2 = ExporterTestHelper.createState(c1_, "01.07.2011", "01.07.2013");
            DataproviderObject c1_s3 = ExporterTestHelper.createState(c1_, "01.07.2013", null);

            c1_s2.clearValues("object_hasRole").set(0, roleTypeId1);
            c1_s3.clearValues("object_hasRole").set(0, roleTypeId1);
            validStates.add(c1_s1);
            validStates.add(c1_s2);
            validStates.add(c1_s3);
            stateMap.put(c1_.path(), validStates);

            ArrayList roleStates = new ArrayList();
            roleStates.add(dpo2);
            stateMap.put(dpo2.path().add("role").add(roleTypeId1), roleStates);

            // Prepare object map
            // no valid current objects
            //            ArrayList objects = new ArrayList();
            //            objects.add(TestHelper.setValidity(dpo3, "01.07.2000", null));
            //            mapAdd(objectMap, dpo1.path().getParent(), objects);

        }
        
        {
            // create a base object 
            StateC c1 = stateCClass.createStateC(id4);
            c1.setObject_validFrom(ExporterTestHelper.asDate("01.01.1910"));
            c1.setObject_validTo(ExporterTestHelper.asDate("01.01.1990"));
            c1.setValue(new String[] { "state1" });

            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            modelSegment.addStateC(id4, c1);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();

            TimedAccess access = getModelSegmentAtDate("01.12.1916", "Source");
            Segment pastModelSegment = access.segment;

            c1 = pastModelSegment.getStateC(id4);

            DataproviderObject c1_ = ExporterTestHelper.toDataproviderObject(c1, getModel());

            // add a role
            // both attributes are changed: "stateAttr" of core and "name" of role
            StateCRole cRole = stateCRoleClass.extendRole(c1, "state44", "role44");
            cRole.setNum(new String[] {"first"});
            cRole.setObject_validFrom(ExporterTestHelper.asDate("01.03.1910"));
            cRole.setObject_validTo(ExporterTestHelper.asDate("01.03.1970"));

            access.getTransaction().begin(); // access.statePackage.refBegin();
            c1.addRole(roleTypeId1, cRole);
            access.getTransaction().commit(); // access.statePackage.refCommit();

            DataproviderObject dpo1 = ExporterTestHelper.toDataproviderObject(cRole, getModel());
            
            // add second role
            cRole = stateCRoleClass.extendRole(c1, "state55", "role55");
            cRole.setNum(new String[] {"first"});
            cRole.setObject_validFrom(ExporterTestHelper.asDate("01.11.1912"));
            cRole.setObject_validTo(ExporterTestHelper.asDate("01.11.1922"));

            access.getTransaction().begin(); // access.statePackage.refBegin();
            c1.addRole(roleTypeId2, cRole);
            access.getTransaction().commit(); // access.statePackage.refCommit();
            
            DataproviderObject dpo2 = ExporterTestHelper.toDataproviderObject(cRole, getModel());
            
            // add object to role
            StateCRoleDepend stateCRoleDepend = stateCRoleDependClass.createStateCRoleDepend();
            
            stateCRoleDepend.setName("dependend from " + cRole.getName());
            stateCRoleDepend.setObject_validFrom(ExporterTestHelper.asDate("01.01.1913"));
            stateCRoleDepend.setObject_validTo(ExporterTestHelper.asDate("01.10.1921"));
            
            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            cRole.addStateCRoleDepend("roleDep1", stateCRoleDepend);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();
            
            StateCRole role = (StateCRole)c1.getRole(roleTypeId2);
            stateCRoleDepend = role.getStateCRoleDepend("roleDep1");
            
            DataproviderObject roleDepDpo1 = 
                ExporterTestHelper.toDataproviderObject(stateCRoleDepend, getModel());
            
            // add a second state to this object
            access.getTransaction().begin(); // access.statePackage.refBegin();
            stateCRoleDepend.setName("dependend from " + cRole.getName() + ". Update.");
            stateCRoleDepend.setObject_validFrom(ExporterTestHelper.asDate("01.01.1915"));
            // TODO using 01.10.1921 here leads to endless loop in state plugin !?
            stateCRoleDepend.setObject_validTo(ExporterTestHelper.asDate("01.09.1921"));
            access.getTransaction().commit(); // access.statePackage.refCommit();
            DataproviderObject roleDepDpo2 = 
                ExporterTestHelper.toDataproviderObject(stateCRoleDepend, getModel());


            // add another object to role
            stateCRoleDepend = stateCRoleDependClass.createStateCRoleDepend();
            
            stateCRoleDepend.setName("dependend from " + cRole.getName() + ", second object.");
            stateCRoleDepend.setObject_validFrom(ExporterTestHelper.asDate("01.01.1912"));
            stateCRoleDepend.setObject_validTo(ExporterTestHelper.asDate("01.10.1922"));
            
            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            cRole.addStateCRoleDepend("roleDep2", stateCRoleDepend);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();
            
            role = (StateCRole)c1.getRole(roleTypeId2);
            stateCRoleDepend = role.getStateCRoleDepend("roleDep2");
            
            DataproviderObject roleDepDpo3 = 
                ExporterTestHelper.toDataproviderObject(stateCRoleDepend, getModel());
            

            // setup valid states array with all the states produced in the correct order  
            ArrayList validStates = new ArrayList();

            DataproviderObject c1_s1 = ExporterTestHelper.createState(c1_, "01.01.1910", "01.03.1910");
            DataproviderObject c1_s2 = ExporterTestHelper.createState(c1_, "01.03.1910", "01.11.1912");
            DataproviderObject c1_s3 = ExporterTestHelper.createState(c1_, "01.11.1912", "01.11.1922");
            DataproviderObject c1_s6 = ExporterTestHelper.createState(c1_, "01.11.1922", "01.03.1970");
            DataproviderObject c1_s7 = ExporterTestHelper.createState(c1_, "01.03.1970", "01.01.1990");
            
            c1_s2.clearValues("object_hasRole").set(0, roleTypeId1);
            c1_s3.clearValues("object_hasRole").set(0, roleTypeId2);
            c1_s3.values("object_hasRole").set(1, roleTypeId1);
//            c1_s4.clearValues("object_hasRole").set(0, roleTypeId1);
//            c1_s4.clearValues("object_hasRole").add(roleTypeId2);
//            c1_s5.clearValues("object_hasRole").set(0, roleTypeId1);
//            c1_s5.clearValues("object_hasRole").add(roleTypeId2);
            c1_s6.clearValues("object_hasRole").set(0, roleTypeId1);
            validStates.add(c1_s1);
            validStates.add(c1_s2);
            validStates.add(c1_s3);
//            validStates.add(c1_s4);
//            validStates.add(c1_s5);
            validStates.add(c1_s6);
            validStates.add(c1_s7);

            stateMap.put(c1_.path(), validStates);

            ArrayList roleStates = new ArrayList();
            roleStates.add(dpo1);
            stateMap.put(dpo1.path().add("role").add(roleTypeId1), roleStates);

            roleStates = new ArrayList();
            roleStates.add(dpo2);
            stateMap.put(dpo2.path().add("role").add(roleTypeId2), roleStates);
            
            ArrayList roleDependendStates = new ArrayList();
            
            DataproviderObject rdop_1 = ExporterTestHelper.createState(roleDepDpo1, "01.01.1913", "01.01.1915");
            DataproviderObject rdop_2 = ExporterTestHelper.createState(roleDepDpo2, "01.01.1915", "01.09.1921");
            DataproviderObject rdop_3 = ExporterTestHelper.createState(roleDepDpo1, "01.09.1921", "01.10.1921");
            
            roleDependendStates.add(rdop_1);
            roleDependendStates.add(rdop_2);
            roleDependendStates.add(rdop_3);
            stateMap.put(roleDepDpo1.path(), roleDependendStates);
            
            roleDependendStates = new ArrayList();
            DataproviderObject rdop_4 = ExporterTestHelper.createState(roleDepDpo3, "01.01.1912", "01.10.1922");
            roleDependendStates.add(rdop_4);
            stateMap.put(roleDepDpo3.path(), roleDependendStates);
            
        }


    }

    /**
     * Set up target DB for testing synchronization with Roles and States. 
     * <p>
     * Add the valid states to the objectMap, which contains for each reference
     * all the valid states as a DataproviderObject in a list. Roles of an 
     * object must be treated separately, with their own reference.
     * 
     * @param pkg
     * @param modelSegment
     * @param id1
     * @param id2
     * @param roleTypeId
     * @param stateMap  contains the valid states of an object
     * @param objectMap contains the (current) objects 
     * @throws Exception
     */
    static public void setUpTargetDBWithRoleAndStates(
        ClassicPackage pkg,
        Segment modelSegment,
        String id1,
        String id2,
        String id3,
        String roleTypeId,
        Map stateMap,
        Map objectMap
    ) throws Exception {

        StateCClass stateCClass = pkg.getStateC(); // pkg.getStateCClass();
        StateCRoleClass stateCRoleClass = pkg.getStateCRole(); // pkg.getStateCRoleClass();
        RoleClassStateCRoleTypeClass stateCRoleTypeClass = pkg.getRoleClassStateCRoleType(); // pkg.getRoleClassStateCRoleTypeClass();
        PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(modelSegment);
        
        persistenceManager.currentTransaction().begin(); // pkg.refBegin();
        RoleClassStateCRoleType role1 = stateCRoleTypeClass.createRoleClassStateCRoleType();
        modelSegment.addStateCRoleType(roleTypeId, role1);
        persistenceManager.currentTransaction().commit(); // pkg.refCommit();

        {
            // create a base object (leave it as in the target provider)
            StateC c1 = stateCClass.createStateC(id1);
            c1.setObject_validFrom(ExporterTestHelper.asDate("01.01.2001"));
            c1.setValue(new String[] { "state1" });

            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            modelSegment.addStateC(id1, c1);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();

            DataproviderObject c1_ = ExporterTestHelper.toDataproviderObject(c1, getModel());

            // add  a state
            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            c1.setValue(new String[] { "state2" });
            c1.setObject_validFrom(ExporterTestHelper.asDate("01.09.2001"));
            c1.setObject_validTo(ExporterTestHelper.asDate("01.09.2002"));
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();

            // leave out the source role at 1.11.2001 - 1.12.2001, instead add 
            // one at 01.01.2002 - 01.03.2002
            StateCRole cRole = stateCRoleClass.extendRole(c1, "state3", "role1");

            cRole.setNum(new String[] { "first" });
            cRole.setObject_validFrom(ExporterTestHelper.asDate("01.01.2002"));
            cRole.setObject_validTo(ExporterTestHelper.asDate("01.03.2002"));

            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            c1.addRole(roleTypeId, cRole);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();

            DataproviderObject dpo4 = ExporterTestHelper.toDataproviderObject(c1, getModel());

            // add a second role, but with different start and end times 
            // compared to source entries
            cRole = stateCRoleClass.extendRole(c1, "state4", "role2");
            cRole.setNum(new String[] { "second" });
            cRole.setObject_validFrom(ExporterTestHelper.asDate("01.07.2002"));
            cRole.setObject_validTo(ExporterTestHelper.asDate("01.10.2002"));

            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            c1.addRole(roleTypeId, cRole);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();

            DataproviderObject dpo6 = ExporterTestHelper.toDataproviderObject(c1, getModel());

            // setup valid states array with all the states produced in the correct order  
            ArrayList validStates = new ArrayList();

            DataproviderObject c1_s1 = ExporterTestHelper.createState(c1_, "01.01.2001", "01.09.2001");
            DataproviderObject c1_s2 = ExporterTestHelper.createState(c1_, "01.09.2001", "01.01.2002");
            DataproviderObject c1_s3 = ExporterTestHelper.createState(c1_, "01.01.2002", "01.03.2002");
            DataproviderObject c1_s4 = ExporterTestHelper.createState(c1_, "01.03.2002", "01.07.2002");
            DataproviderObject c1_s5 = ExporterTestHelper.createState(c1_, "01.07.2002", "01.09.2002");
            DataproviderObject c1_s6 = ExporterTestHelper.createState(c1_, "01.09.2002", "01.10.2002");
            DataproviderObject c1_s7 = ExporterTestHelper.createState(c1_, "01.10.2002", null);

            c1_s2.clearValues("value").addAll(Arrays.asList(new String[] { "state2" }));
            c1_s3.clearValues("value").addAll(Arrays.asList(new String[] { "state2" }));
            c1_s3.clearValues("object_hasRole").set(0, roleTypeId);
            c1_s4.clearValues("value").addAll(Arrays.asList(new String[] { "state2" }));
            c1_s5.clearValues("object_hasRole").set(0, roleTypeId);
            c1_s5.clearValues("value").addAll(Arrays.asList(new String[] { "state2" }));
            c1_s6.clearValues("object_hasRole").set(0, roleTypeId);

            validStates.add(c1_s1);
            validStates.add(c1_s2);
            validStates.add(c1_s3);
            validStates.add(c1_s4);
            validStates.add(c1_s5);
            validStates.add(c1_s6);
            validStates.add(c1_s7);

            stateMap.put(c1_.path(), validStates);

            ExporterTestHelper.showValidStates(modelSegment.getStateC(id1));

            ArrayList roleStates = new ArrayList();
            roleStates.add(dpo4);
            roleStates.add(dpo6);
            stateMap.put(dpo4.path().add("role").add(roleTypeId), roleStates);

            // prepare object map
            ArrayList objects = new ArrayList();
            objects.add(c1_s7);
            mapAdd(objectMap, c1_.path().getParent(), objects);

        }
        {
            // create object and states/roles exactly like in source
            // create a base object
            StateC c1 = stateCClass.createStateC(id2);
            c1.setObject_validFrom(ExporterTestHelper.asDate("01.01.2000"));
            c1.setValue(new String[] { "state1" });

            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            modelSegment.addStateC(id2, c1);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();
            DataproviderObject c1_ = ExporterTestHelper.toDataproviderObject(c1, getModel());

            // add  a state
            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            c1.setValue(new String[] { "state2" });
            c1.setObject_validFrom(ExporterTestHelper.asDate("01.06.2000"));
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();

            // add a role
            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            StateCRole cRole = stateCRoleClass.extendRole(c1, "state5", "role3");
            cRole.setNum(new String[] { "first" });
            cRole.setObject_validFrom(ExporterTestHelper.asDate("01.07.2000"));

            c1.addRole(roleTypeId, cRole);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();

            DataproviderObject dpo4 = ExporterTestHelper.toDataproviderObject(c1, getModel());
            dpo4.values("object_inRole").add(roleTypeId);

            // setup valid states array with all the states produced in the correct order  
            ArrayList validStates = new ArrayList();
            DataproviderObject c1_s1 = ExporterTestHelper.createState(c1_, "01.01.2000", "01.06.2000");
            DataproviderObject c1_s2 = ExporterTestHelper.createState(c1_, "01.06.2000", "01.07.2000");
            DataproviderObject c1_s3 = ExporterTestHelper.createState(c1_, "01.07.2000", null);

            c1_s2.clearValues("value").addAll(Arrays.asList(new String[] { "state2" }));
            c1_s3.clearValues("value").addAll(Arrays.asList(new String[] { "state2" }));
            c1_s3.clearValues("object_hasRole").set(0, roleTypeId);
            validStates.add(c1_s1);
            validStates.add(c1_s2);
            validStates.add(c1_s3);
            stateMap.put(c1_.path(), validStates);

            ArrayList roleStates = new ArrayList();
            roleStates.add(dpo4);
            stateMap.put(dpo4.path().add("role").add(roleTypeId), roleStates);

            // Prepare object map
            ArrayList objects = new ArrayList();
            objects.add(c1_s3);
            mapAdd(objectMap, c1_.path().getParent(), objects);

        }

        {
            // create object as in source and change just the roles values
            // create a base object 
            StateC c1 = stateCClass.createStateC(id3);
            c1.setObject_validFrom(ExporterTestHelper.asDate("01.01.2010"));
            c1.setObject_validTo(ExporterTestHelper.asDate("01.01.2020"));
            c1.setValue(new String[] { "state1" });

            persistenceManager.currentTransaction().begin(); // pkg.refBegin();
            modelSegment.addStateC(id3, c1);
            persistenceManager.currentTransaction().commit(); // pkg.refCommit();

            TimedAccess access = getModelSegmentAtDate("01.01.2012", "Target");
            Segment futureModelSegment = access.segment;

            c1 = futureModelSegment.getStateC(id3);

            DataproviderObject c1_ = ExporterTestHelper.toDataproviderObject(c1, getModel());

            // add a role
            // both attributes are changed: "stateAttr" of core and "name" of role
            StateCRole cRole = stateCRoleClass.extendRole(c1, "state33", "role33");
            cRole.setNum(new String[]{"first"});
            cRole.setObject_validFrom(ExporterTestHelper.asDate("01.07.2011"));
            cRole.setObject_validTo(ExporterTestHelper.asDate("01.07.2013"));

            access.getTransaction().begin(); // access.statePackage.refBegin();
            c1.addRole(roleTypeId, cRole);
            access.getTransaction().commit(); // access.statePackage.refCommit();

            DataproviderObject dpo2 = ExporterTestHelper.toDataproviderObject(cRole, getModel());

            // setup valid states array with all the states produced in the correct order  
            ArrayList validStates = new ArrayList();

            DataproviderObject c1_s1 = ExporterTestHelper.createState(c1_, "01.01.2010", "01.07.2011");
            DataproviderObject c1_s2 = ExporterTestHelper.createState(c1_, "01.07.2011", "01.07.2013");
            DataproviderObject c1_s3 = ExporterTestHelper.createState(c1_, "01.07.2013", null);

            c1_s2.clearValues("object_hasRole").set(0, roleTypeId);
            c1_s3.clearValues("object_hasRole").set(0, roleTypeId);
            validStates.add(c1_s1);
            validStates.add(c1_s2);
            validStates.add(c1_s3);
            stateMap.put(c1_.path(), validStates);

            ArrayList roleStates = new ArrayList();
            roleStates.add(dpo2);
            stateMap.put(dpo2.path().add("role").add(roleTypeId), roleStates);

        }

    }
    protected static TimedAccess getModelSegmentAtDate(
        String requestedFor,
        String segmentID
    ) throws Exception {

        ServiceHeader serviceHeader =
            new ServiceHeader("test", null, false, new QualityOfService(), null, ExporterTestHelper.dateAsString(requestedFor));

        try {
            Object providerPath = null;
            if (segmentID.equals("Source")) {
                providerPath = TestReplicator.SOURCE_ROOT_PATH;
            }
            else {
                providerPath = TestReplicator.TARGET_ROOT_PATH;
            }
            
            Dataprovider_1_0 Dataprovider = Dataprovider_1ConnectionFactoryImpl.createGenericConnection(
                SimpleServiceLocator.getInitialContext().lookup("ch/omex/dataprovider-1/NoOrNewTransaction/access")
            );
            Provider_1_0 providerLayer = new Provider_1(new RequestCollection(serviceHeader, Dataprovider), false);
            ObjectFactory_1_0 manager = new Manager_1(new Connection_1(providerLayer, false));
            RefRootPackage_1 rootPkg = new RefRootPackage_1(manager, null, null);
            BasePackage genericPkg = (BasePackage) rootPkg.refPackage("org:openmdx:base");
            ClassicPackage statePackage = (ClassicPackage) rootPkg.refPackage("org:openmdx:compatibility:state1");
            ProviderClass providerClass = genericPkg.getProvider(); // genericPkg.getProviderClass(); 
            Provider provider = (Provider) providerClass.getProvider(providerPath);
            return new TimedAccess(
                statePackage,
                (Segment) provider.getSegment(segmentID)
            );
        } catch (ServiceException se) {
            se.printStackTrace(System.out);
            throw se;
        }

    }

    /**
     * A lazy initialized DatatypeFactory instance
     */
    private static DatatypeFactory datatypeFactory = null;

    /**
     * @return a Datatype Factory Instance
     */
    protected static synchronized DatatypeFactory xmlDatatypeFactory(
    ){
      if(datatypeFactory == null) try {
        datatypeFactory = DatatypeFactory.newInstance();
      } catch (DatatypeConfigurationException e) {
        throw new RuntimeServiceException(e);
      }
      return datatypeFactory;
    }

    /**
     *
     */
    static class TimedAccess {
        
        TimedAccess(
            ClassicPackage statePackage,
            Segment segment
        ){
            this.statePackage = statePackage;
            this.segment = segment;
            this.persistenceManager = JDOHelper.getPersistenceManager(segment);
        }
            
        public final ClassicPackage statePackage;
        public final Segment segment;
        private final PersistenceManager persistenceManager;

        public Transaction getTransaction(
        ){
            return this.persistenceManager.currentTransaction();
        }
        
    }

}

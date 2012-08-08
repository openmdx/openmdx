/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestAudit_1.java,v 1.24 2009/06/09 16:30:32 hburger Exp $
 * Description: Unit test for audit1
 * Revision:    $Revision: 1.24 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 16:30:32 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.openmdx.test.compatibility.audit1;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefPackage;
import javax.xml.datatype.DatatypeFactory;

import org.junit.Before;
import org.junit.Test;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.jmi1.BasePackage;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.jmi1.ProviderClass;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.compatibility.audit1.cci2.UnitOfWorkQuery;
import org.openmdx.compatibility.audit1.jmi1.Audit1Package;
import org.openmdx.compatibility.audit1.jmi1.Auditable;
import org.openmdx.compatibility.audit1.jmi1.Involved;
import org.openmdx.compatibility.audit1.jmi1.UnitOfWork;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.test.app1.jmi1.App1Package;
import org.openmdx.test.app1.jmi1.Person;
import org.openmdx.test.app1.jmi1.PersonClass;
import org.openmdx.test.app1.jmi1.Segment;
import org.openmdx.test.app1.jmi1.SegmentClass;

/**
 * Unit test for audit1
 */
public class TestAudit_1 {

    /**
     * A Datatype Factory Instance
     */
    protected DatatypeFactory xmlDatatypeFactory;

    @Before
    protected void setUp(
    ) throws Exception {
        if(xmlDatatypeFactory == null) xmlDatatypeFactory = DatatypeFactory.newInstance();
        //
        // TODO :
        //
//      try {
//        if(! deployed){
//          System.out.println("Deploying...");
//          DeploymentConfiguration_1.createInstance(
//              new String[]{
//                  "xri:+resource/org/openmdx/test/deployment.configuration.xml",
//                  "xri:+resource/org/openmdx/test/compatibility/audit1/deployment.configuration.xml"
//              }
//          );
//          new LightweightContainer_1(
//              "testapp",
//              CONNECTORS,
//              DEPLOYMENT_UNITS
//          );
//          deployed = true;
//        }
//        System.out.println(">>>> **** Start Test: " + this.getName());
//        AppLog.info("Start Test", this.getName());
  //
//        Dataprovider_1_0Connection remoteConnection = Dataprovider_1ConnectionFactoryImpl.createGenericConnection(
//            SimpleServiceLocator.getInitialContext().lookup("org/openmdx/test/managing/explorer")
//        );
  //
//        // intercept webservice transport for testing
//        Dataprovider_1_0Connection connection = remoteConnection;
////        Dataprovider_1_0Connection connection =
//////          new Dataprovider_1_0ConnectionImpl("http://localhost:8080/dataproviders/junits");
////          new Dataprovider_1_0ConnectionImpl(remoteConnection);            
  //
//        RequestCollection channel = new RequestCollection(
//          new ServiceHeader(),
//          connection
//        );
  //
//        // get a manager which delegates to explorer 
//        Provider_1_0 provider = new Provider_1(
//          channel,
//          false
//        );
//        AbstractTestAudit_1.manager = new Manager_1(
//          new Connection_1(
//            provider,
//            false
//          )
//        );
  //
//        // get initial data from resource
//        new XmlImporter(
//          new ServiceHeader(),
//          connection
//        ).process(
//          new String[]{
//            "xri:+resource/org/openmdx/test/compatibility/audit1/appData.xml",
//            "xri:+resource/org/openmdx/test/compatibility/audit1/auditData.xml"
//          }
//        );
//      }
//      catch (ServiceException e) {
//        throw e.log();
//      }
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings({
        "unchecked", "deprecation"
    })
    private void showUnitsOfWork(

      org.openmdx.compatibility.audit1.jmi1.Segment auditSegment



    ) {
      Collection unitsOfWork = auditSegment.getUnitOfWork();
      FilterProperty[] uowFilter = new FilterProperty[]{
        new FilterProperty(
          Quantors.THERE_EXISTS,
          "involved",
          FilterOperators.IS_LIKE,
          "xri:@openmdx:org.openmdx.test.app1/provider/Auditing/segment/Standard/person/%"
        )
      };

      // retrieve all units of work where persons and their dependent objects 
      // are involved
//      for(
//        Iterator i = ((org.openmdx.base.collection.Container)unitsOfWork).subSet(uowFilter).iterator();
//        i.hasNext();
//      ) {
//        UnitOfWork uow = (UnitOfWork)i.next();
//        System.out.println("unit of work: " + uow.toString());
//        for(
//          Iterator j = uow.getInvolved().iterator();
//          j.hasNext();
//        ) {
//          try {
//            Involved involved = (Involved)j.next();
//            System.out.println("involved=" + involved.toString());
//            Person beforeImage = (Person)involved.getView("BeforeImage");
//            Person afterImage = (Person)involved.getView("AfterImage");
//            System.out.println("  involved");
//            System.out.println("    toString=" + involved.toString());
//            System.out.println("    id=" + involved.refMofId());
//            System.out.println("    unitOfWorkId=" + involved.getUnitOfWorkId());
//            System.out.println("    modifiedFeature=" + involved.getModifiedFeature());
//            System.out.println("    taskId=" + involved.getTaskId());
//            System.out.println("    beforeImage");
//            System.out.println("      lastName=" + beforeImage.getLastName());
//            System.out.println("      givenName=" + beforeImage.getGivenName());
//            System.out.println("      birthDate=" + beforeImage.getBirthdate());
//            System.out.println("    afterImage");
//            System.out.println("      lastName=" + afterImage.getLastName());
//            System.out.println("      givenName=" + afterImage.getGivenName());
//            System.out.println("      birthDate=" + afterImage.getBirthdate());
//
//            // write delta attributes
//            for(
//              Iterator k = involved.getModifiedFeature().iterator();
//              k.hasNext();
//            ) {
//              String modifiedFeature = (String)k.next();
//              Object beforeValue = beforeImage.refGetValue(modifiedFeature);
//              Object afterValue = afterImage.refGetValue(modifiedFeature);
//              System.out.println("    " + modifiedFeature);
//              System.out.println("      before=" + beforeValue);
//              System.out.println("      after=" + afterValue);
//            }
//          }
//          catch(RuntimeException e) {
//            if(BasicException.toExceptionStack(e).getExceptionCode() != BasicException.Code.NOT_FOUND) {
//              throw e;
//            }
//            System.out.println("NOT_FOUND when reading involved. Auditable probably removed.");
//            break;
//          }
//          catch(Exception e) {
//            System.out.println("Exception when reading involved. Auditable probably removed.");
//            break;
//          }
//        }
//      }
    }

    //---------------------------------------------------------------------------
    @SuppressWarnings("deprecation")
    @Test
    public void testProvider(
    ) throws Exception {

      try {
        System.out.println("getting root package...");
        RefPackage rootPkg = null; // TODO new RefRootPackage_1(
//          manager,
//          (java.util.Map)null, // impls
//          (java.lang.Object)null // context
//        );
        BasePackage genericPkg = (BasePackage)rootPkg.refPackage("org:openmdx:base");
        Audit1Package audit1Pkg = (Audit1Package)rootPkg.refPackage("org:openmdx:compatibility:audit1");
        App1Package app1Pkg = (App1Package)rootPkg.refPackage("org:openmdx:test:app1");

        // classes
        SegmentClass segmentClass = app1Pkg.getSegment();
        PersonClass personClass = app1Pkg.getPerson();

        // segment
        Segment segment = segmentClass.getSegment(
          ROOT_SEGMENT_APP1
        );
        //
        // JDO Persistence Manager
        //
        PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(segment);

        // create some Persons
        Person person = null;
        persistenceManager.currentTransaction().begin(); // app1Pkg.refBegin();        
        for(
          int i = 0;
          i < N_PERSONS;
          i++
        ) {
          person = personClass.createPerson();
          person.setForeignId("F" + i);
          person.setBirthdate(xmlDatatypeFactory.newXMLGregorianCalendar("1960-01-01"));
          person.setBirthdateAsDateTime(new Date(System.currentTimeMillis()));
          /* Date d = */ person.getBirthdateAsDateTime();
          person.setLastName("Muster" + i);
          person.setSalutation("Herr");
          person.setSex((short)0);
          person.getGivenName().add("Hans");
          person.getGivenName().add("Heiri");
          person.setGivenName(new String[]{"Hans", "Heiri"});
          segment.addPerson("000" + i, person);
        }
        persistenceManager.currentTransaction().commit(); // app1Pkg.refCommit();        

        // do N_UPDATES. For each update there should be one more involved
        for(
          int count = 0;
          count < N_UPDATES;
          count++
        ) {
          System.out.println();
          System.out.println("doing update " + count);

          // modify the persons to provoke auditing
          for(
            int i = 0;
            i < N_PERSONS;
            i++
          ) {
            person = segment.getPerson("000" + i);

            // force 10 objects to be in the same unit of work
            if(i % 10 == 0) {
              persistenceManager.currentTransaction().begin(); // app1Pkg.refBegin();
            }
            List<String> givenNames = person.getGivenName();
            String givenName = "Jean" + count;
            if(givenNames.isEmpty()){
                givenNames.add(givenName);
            } else {
                givenNames.set(0, givenName);
            }
            person.setSex((short)count);
            person.getMemberOfGroup().add("group " + count);
            if(i % 10 == 9) {
              persistenceManager.currentTransaction().commit(); // app1Pkg.refCommit();
            }
          }
          if(
            persistenceManager.currentTransaction().isActive() // app1Pkg.refUnitOfWork().isActive()            
          ) {
            persistenceManager.currentTransaction().commit(); // app1Pkg.refCommit();
          }

          // get audit of the modified objects
          for(
            int i = 0;
            i < N_PERSONS;
            i++
          ) {
            person = segment.getPerson("000" + i);
            System.out.println("audit for person " + person.refMofId());
            Auditable auditable = null; // TODO (Auditable)person.getContext("Audit");
            persistenceManager.refresh(auditable); // auditable.refRefresh();
            org.w3c.cci2.Container<Involved> involveds = auditable.getInvolved(); 
            for(Involved involved  : involveds){
              Person beforeImage = (Person)involved.getView("BeforeImage");
              Person afterImage = (Person)involved.getView("AfterImage");
              System.out.println("  involved");
              System.out.println("    id=" + involved.refMofId());
              System.out.println("    unitOfWorkId=" + involved.getUnitOfWorkId());
              System.out.println("    modifiedFeature=" + involved.getModifiedFeature());
              System.out.println("    taskId=" + involved.getTaskId());
              System.out.println("    beforeImage");
              System.out.println("      lastName=" + beforeImage.getLastName());
              System.out.println("      givenName=" + beforeImage.getGivenName());
              System.out.println("      birthDate=" + beforeImage.getBirthdate());
              System.out.println("    afterImage");
              System.out.println("      lastName=" + afterImage.getLastName());
              System.out.println("      givenName=" + afterImage.getGivenName());
              System.out.println("      birthDate=" + afterImage.getBirthdate());
            }
          }
        }

        ProviderClass auditProviderClass = genericPkg.getProvider();
        Provider auditProvider = auditProviderClass.getProvider(
          ROOT_PROVIDER_AUDIT
        );

        org.openmdx.compatibility.audit1.jmi1.Segment auditSegment = (org.openmdx.compatibility.audit1.jmi1.Segment)



          auditProvider.getSegment("Standard");
        // to test get all units of work with empty filter
        Collection<UnitOfWork> unitsOfWork = auditSegment.getUnitOfWork();
        UnitOfWorkQuery uowFilterJmi = audit1Pkg.createUnitOfWorkQuery();
//        org.openmdx.base.collection.Container<UnitOfWork> container = (org.openmdx.base.collection.Container<UnitOfWork>) unitsOfWork;
//        container = container.subSet(uowFilterJmi);
//        for(UnitOfWork uow : container) {
//          // Is there something to do?
//        }

        // remove persons
        persistenceManager.currentTransaction().begin(); // app1Pkg.refBegin();        
        for(
          int i = 0;
          i < N_PERSONS;
          i++
        ) {
          segment.getPerson("000" + i).refDelete();
        }
        persistenceManager.currentTransaction().commit(); // app1Pkg.refCommit();        

        // get units of work (in this case last AfterImage should be empty)
        this.showUnitsOfWork(auditSegment);

      }
      catch(RuntimeServiceException e) {
        SysLog.error("exception", e);
        throw e;
      } catch(JmiServiceException e) {
        SysLog.error("exception", e);
        throw e;
      }
    }

    //---------------------------------------------------------------------------
    // Variables    
    //---------------------------------------------------------------------------    

//  static private boolean deployed = false;

    static private int N_PERSONS = 2;
    static private int N_UPDATES = 2;

  //  static private ObjectFactory_1_0 manager = null;
    static private Path ROOT_SEGMENT_APP1 = new Path("xri:@openmdx:org.openmdx.test.app1/provider/Auditing/segment/Standard");
    static private Path ROOT_PROVIDER_AUDIT = new Path("xri:@openmdx:org.openmdx.compatibility.audit1/provider/Audit");
    static private Path[] CONNECTORS = new Path[] {
      new Path("xri:@openmdx:org.openmdx.deployment1/provider/org:openmdx/segment/org:openmdx:test/configuration/junit/domain/apps/deploymentUnit/connectors")
    };
    static private Path[] DEPLOYMENT_UNITS = new Path[] {
        new Path("xri:@openmdx:org.openmdx.deployment1/provider/org:openmdx/segment/org:openmdx:test/configuration/junit/domain/apps/deploymentUnit/testaudit")
    };

}

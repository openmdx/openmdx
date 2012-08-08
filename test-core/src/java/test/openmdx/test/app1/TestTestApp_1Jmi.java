/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestTestApp_1Jmi.java,v 1.18 2009/06/03 15:49:30 hburger Exp $
 * Description: Unit test for model app1
 * Revision:    $Revision: 1.18 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/03 15:49:30 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package test.openmdx.test.app1;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.jdo.JDOException;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jmi.reflect.InvalidObjectException;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.resource.ResourceException;
import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.RequestCollection;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.deployment.Deployment_1;
import org.openmdx.application.dataprovider.importer.XmlImporter;
import org.openmdx.application.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.spi.DelegatingRefObject_1_0;
import org.openmdx.base.accessor.jmi.spi.Jmi1Object_1_0;
import org.openmdx.base.accessor.jmi.spi.RefMetaObject_1;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.Directions;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.datastore1.jmi1.Datastore1Package;
import org.openmdx.compatibility.datastore1.jmi1.QueryFilter;
import org.openmdx.generic1.jmi1.BooleanProperty;
import org.openmdx.generic1.jmi1.Generic1Package;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.test.app1.cci2.CycleMember1Query;
import org.openmdx.test.app1.cci2.InvoiceHasInvoicePosition;
import org.openmdx.test.app1.cci2.InvoicePositionQuery;
import org.openmdx.test.app1.cci2.PersonQuery;
import org.openmdx.test.app1.cci2.SegmentContainsInvoice;
import org.openmdx.test.app1.cci2.SegmentHasAddress;
import org.openmdx.test.app1.cci2.SegmentHasPerson;
import org.openmdx.test.app1.jmi1.Address;
import org.openmdx.test.app1.jmi1.AddressFormat;
import org.openmdx.test.app1.jmi1.AddressFormatAsResult;
import org.openmdx.test.app1.jmi1.App1Package;
import org.openmdx.test.app1.jmi1.CanNotFormatNameException;
import org.openmdx.test.app1.jmi1.CycleMember1;
import org.openmdx.test.app1.jmi1.CycleMember1Class;
import org.openmdx.test.app1.jmi1.CycleMember2;
import org.openmdx.test.app1.jmi1.CycleMember2Class;
import org.openmdx.test.app1.jmi1.Document;
import org.openmdx.test.app1.jmi1.DocumentClass;
import org.openmdx.test.app1.jmi1.EmailAddress;
import org.openmdx.test.app1.jmi1.EmailAddressClass;
import org.openmdx.test.app1.jmi1.EmailAddressSendMessageTemplateResult;
import org.openmdx.test.app1.jmi1.InternationalPostalAddress;
import org.openmdx.test.app1.jmi1.InternationalPostalAddressClass;
import org.openmdx.test.app1.jmi1.Invoice;
import org.openmdx.test.app1.jmi1.InvoiceClass;
import org.openmdx.test.app1.jmi1.InvoicePosition;
import org.openmdx.test.app1.jmi1.InvoicePositionClass;
import org.openmdx.test.app1.jmi1.MessageTemplate;
import org.openmdx.test.app1.jmi1.MessageTemplateClass;
import org.openmdx.test.app1.jmi1.NameFormat;
import org.openmdx.test.app1.jmi1.Person;
import org.openmdx.test.app1.jmi1.PersonClass;
import org.openmdx.test.app1.jmi1.PersonDateOpResult;
import org.openmdx.test.app1.jmi1.PersonFormatNameAsResult;
import org.openmdx.test.app1.jmi1.PersonGroup;
import org.openmdx.test.app1.jmi1.PersonGroupClass;
import org.openmdx.test.app1.jmi1.PostalAddress;
import org.openmdx.test.app1.jmi1.Product;
import org.openmdx.test.app1.jmi1.Segment;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.SparseArray;
import org.w3c.cci2.StringTypePredicate;
import org.w3c.spi2.Datatypes;

/**
 * AbstractTestApp_1Jmi
 */
public class TestTestApp_1Jmi extends TestCase {

    //---------------------------------------------------------------------------  

    //---------------------------------------------------------------------------  
    /**
     * For command-line invocations
     */
    public static void main(
        String[] args
    ){
        TestRunner.run(suite());
    }

    //---------------------------------------------------------------------------  
    /**
     * Prepare the test suite
     * 
     * @return the test suite
     */
    public static Test suite(
    ) {
        TestSuite suite = new TestSuite("App1");
        TestContext context = new TestContext(
            suite.getName()
        );
        suite.addTest(new TestGateway("resetSegment", context));
        TestSuite threaded = new SequentialThreads("1st");
        threaded.addTest(new TestEntityProvider("testPackageAcquisition", context));
        threaded.addTest(new TestEntityProvider("testMain", context));
//      threaded.addTest(new TestEntityProvider("testInMemoryProvider", context));
//      threaded.addTest(new TestEntityProvider("testSerialization", context));
        threaded.addTest(new TestEntityProvider("removeSegment", context));
        suite.addTest(threaded);
        suite.addTest(new TestGateway("resetSegment", context));
        threaded = new SequentialThreads("2nd");
        threaded.addTest(new TestEntityProvider("testMain", context));
        suite.addTest(threaded);
//      suite.addTest(new TestGateway("testCR10006272", context));
        suite.addTest(new TestGateway("testTearDown", context));
        return suite;
    }

    /**
     * SequentialThreads
     */
    static class SequentialThreads extends TestSuite {

        /**
         * Constructor 
         *
         */
        SequentialThreads() {
            super();
        }

        /**
         * Constructor 
         *
         * @param name
         */
        SequentialThreads(String name) {
            super(name);
        }

        /* (non-Javadoc)
         * @see junit.framework.TestSuite#runTest(junit.framework.Test, junit.framework.TestResult)
         */
        @Override
        public void runTest(
            final Test test, 
            final TestResult result
        ) {
            Thread thread = new Thread(
                new Runnable(){
                    public void run() {
                        test.run(result);
                    }
                }
            );

            System.out.println("Starting Thread " + thread.getId());
            thread.start();
            try {
                thread.join();
                System.out.println("Thread " + thread.getId() + " terminated");
            } catch (InterruptedException exception) {
                result.addError(test, exception);
            }
            
        }
        
    }
    
    
    //---------------------------------------------------------------------------
    static class ReadModels
    implements Runnable {

        public void run(
        ) {
            try {
                Model_1_0 model = Model_1Factory.getModel();
                for(
                    int i = 0; i < 5000000;
                    i++
                ) {
                    model.getElement("org:openmdx:base:BasicObject");
                }
            }
            catch(ServiceException e) {
                System.out.println("ReadModels catched Exception. Terminating");
                System.out.println(e);
            }
            System.out.println("ReadModels terminated");
        }
    }

    //---------------------------------------------------------------------------
    public static class TestGateway extends TestCase {
        
        /**
         * Constructor 
         *
         * @param name
         * @param context
         */
        TestGateway(
            String name, 
            TestContext context
        ) {
            super(name);
            this.context = context;
        }

        private final TestContext context;
        /**
         * For CR10006272
         */
        private RequestCollection channel;
        
        public void resetSegment(
        ) throws Exception {
            System.out.println(">>>> **** Start Test: " + this.getName());
            SysLog.info("Start Test", this.getName());

            this.channel = new RequestCollection(
                new ServiceHeader(),
                this.context.getDataproviderConnection()
            );

            // remove segment and contained objects
            Path segmentPath = new Path("xri:@openmdx:org.openmdx.test.app1/provider/" + PROVIDER_NAME + "/segment/" + SEGMENT_NAME);
            try {
                channel.addGetRequest(
                    segmentPath,
                    AttributeSelectors.ALL_ATTRIBUTES,
                    null
                );
                channel.beginUnitOfWork(true);
                channel.addRemoveRequest(segmentPath);
                channel.endUnitOfWork();
            }
            catch(ServiceException exception) {
                switch (exception.getExceptionCode()){
                    case BasicException.Code.NOT_FOUND:
                    case BasicException.Code.NOT_SUPPORTED:
                        break;
                    default:
                        throw exception;
                }
            }
            DataproviderObject segment = new DataproviderObject(segmentPath);
            segment.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:test:app1:Segment");
            channel.beginUnitOfWork(true);
            channel.addCreateRequest(segment);
            channel.endUnitOfWork();

            // get initial data from resource (segment, etc.)
            new XmlImporter(
                new ServiceHeader(),
                this.context.getDataproviderConnection(),
                true, // transactional
                true // split units of work 
            ).process(
                new String[]{"xri:+resource/org/openmdx/test/test/app1/data.xml"}
            );

            File scratchFile = this.context.getScratchFile();
            
            if(scratchFile == null) scratchFile = File.createTempFile(getClass().getName(), null);
            System.out.println ("Scratch File = " + scratchFile);

        }
    
        public void testCR10006272() throws ServiceException{
            Path personPath = new Path("xri:@openmdx:org.openmdx.test.app1/provider/" + PROVIDER_NAME + "/segment/" + SEGMENT_NAME + "/person");
            List<?> findReply = channel.addFindRequest(
                personPath,
                null, // attributeFilter, 
                AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                null, // attributeSpecifier, 
                0, // position
                1, // size
                Directions.ASCENDING
            );
            assertFalse("CR10006272", findReply.isEmpty());
            assertNotNull("CR10006272", findReply.get(0));
        }

        public void testTearDown(
        ) throws Exception {
            this.context.close();
            System.out.println("<<<< **** End Test: " + this.getName());
            SysLog.info("End test",this.getName());
        }

    }
    
   public static class TestEntityProvider extends TestCase {
        
        /**
         * Constructor 
         *
         * @param name
         */
        public TestEntityProvider(
            String name,
            TestContext context
        ) {
            super(name);
            this.context = context;
        }

        private final TestContext context;
        
        protected long id;
        
        protected String nextId(){
            return "ID" + this.id++;
        }

        public void testPackageAcquisition() throws ServiceException{
            QueryFilter instance = this.context.getPersistenceManager().newInstance(QueryFilter.class);
            Datastore1Package datastore1Package = (Datastore1Package)instance.refImmediatePackage();
            assertEquals("MOF ID", "org:openmdx:compatibility:datastore1:datastore1", datastore1Package.refMofId());
        }

        @SuppressWarnings("deprecation")
        public void testMain(
        ) throws ServiceException, IOException, RefException{
            this.id = 500000l;
            PersistenceManager persistenceManager = this.context.getPersistenceManager();
            System.out.println("getting root package...");
//          Authority app1 = (Authority) persistenceManager.getObjectById(new Path(App1Package.AUTHORITY_XRI));
            Authority app1 = (Authority) persistenceManager.getObjectById(Authority.class, App1Package.AUTHORITY_XRI);
            RefPackage rootPkg = app1.refOutermostPackage();
            App1Package app1Package = (App1Package) (
                    (RefObject)persistenceManager.newInstance(Segment.class)
            ).refImmediatePackage();
            
            Generic1Package generic1Package = (Generic1Package) rootPkg.refPackage("org:openmdx:generic1");

            // test thread-safety
//          Thread r1 = new Thread(new ReadModels());
//          Thread r2 = new Thread(new ReadModels());
//          Thread r3 = new Thread(new ReadModels());
//          Thread r4 = new Thread(new ReadModels());
//          Thread r5 = new Thread(new ReadModels());
//          Thread w1 = new Thread(new UpdateModels());
//          Thread w2 = new Thread(new UpdateModels());
//          Thread w3 = new Thread(new UpdateModels());
////        // start
//          r1.start();
//          r2.start();
//          r3.start();
//          r4.start();
//          r5.start();
//          w1.start();
//          w2.start();
//          w3.start();
//          // join
//          r1.join();
//          r2.join();
//          r3.join();
//          r4.join();
//          r5.join();
//          w1.join();
//          w2.join();
//          w3.join();

            // END test model functions
            // BEGIN test model functions
            Model_1_0 model = ((RefRootPackage_1)rootPkg).refModel();
            rootPkg.refPackage(
                "test:openmdx:state2"
            );
            PersonClass personClass = app1Package.getPerson();
            InternationalPostalAddressClass postalAddressClass = app1Package.getInternationalPostalAddress();
            EmailAddressClass emailAddressClass = app1Package.getEmailAddress();
            CycleMember1Class cycleMember1Class = app1Package.getCycleMember1();
            CycleMember2Class cycleMember2Class = app1Package.getCycleMember2();
            MessageTemplateClass messageTemplateClass = app1Package.getMessageTemplate();
            DocumentClass documentClass = app1Package.getDocument();
            PersonGroupClass personGroupClass = app1Package.getPersonGroup();

            // segment
            Provider provider = app1.getProvider(false, PROVIDER_NAME);
            Segment segment = (Segment) provider.getSegment(SEGMENT_NAME);
            long startedAt = 0;

            /**
             * Test Invoice
             */
            InvoiceClass invoiceClass = app1Package.getInvoice();
            InvoicePositionClass invoicePositionClass = app1Package.getInvoicePosition();

            BooleanProperty booleanProperty = generic1Package.getBooleanProperty().createBooleanProperty();

            booleanProperty.setDescription("A SparseArray Of Flags");
            booleanProperty.getBooleanValue().put(0, Boolean.TRUE);
            
            Invoice invoice = invoiceClass.createInvoice();        
            invoice.setDescription("this is an invoice for PG0");
            invoice.setProductGroupId("PG0");
            assertNull("CR0003551", refGetPath(invoice));
            SegmentContainsInvoice.Invoice<Invoice> invoices = segment.getInvoice();
            RefContainer refInvoices = (RefContainer) invoices;
            invoice.addProperty(false, "flag", booleanProperty);

            persistenceManager.currentTransaction().begin();
            
            refInvoices.refAdd(RefContainer.REASSIGNABLE, nextId(), invoice);
            assertNotNull("CR0003551", refGetPath(invoice));
            for(int i = 0; i < 10; i++) {
                InvoicePosition invoicePosition = invoicePositionClass.createInvoicePosition();
                invoicePosition.setDescription("this is invoice position for P" + i);
                invoicePosition.setProductId("P" + i);
                assertNull("CR0003551", refGetPath(invoicePosition));
                invoice.addInvoicePosition(false, nextId(), invoicePosition);
                assertNotNull("CR0003551", refGetPath(invoicePosition));
            }
            persistenceManager.currentTransaction().commit();
            
            synchronized(this) {
                try {
                    System.out.println("Wait 5 sec");
                    Thread.yield();
                    wait(5000);
                    System.out.println("...done");
                } catch (InterruptedException exception) {
                    System.out.println("...interrupted");
                }
            }
            
            {
                System.out.println("Invoice instanceof " + Arrays.toString(invoice.getClass().getInterfaces()));
                Path flagId = ((Path) JDOHelper.getObjectId(invoice)).getDescendant(
                    "property",
                    "flag"
                );
                BooleanProperty flag = (BooleanProperty) persistenceManager.getObjectById(flagId);
                assertNotNull("Flag", flag);
                SparseArray<Boolean> flags = flag.getBooleanValue();
                assertNotNull("flags", flags);
                assertEquals("Flag[0]", Boolean.TRUE, flags.get(0));
                PersistenceManager m = JDOHelper.getPersistenceManager(flag);
                assertSame(
                    "Class with root parent", 
                    persistenceManager, 
                    m
                );
                assertTrue("Implementation detail", flag instanceof DelegatingRefObject_1_0);
                DelegatingRefObject_1_0 entity = (DelegatingRefObject_1_0) flag;
                assertNotSame("Made persistent", entity.openmdxjdoGetDelegate(), entity.openmdxjdoGetDataObject());
            }
            {
                //
                // Read via extent
                //
                InvoicePositionQuery invoicePositionQuery = app1Package.createInvoicePositionQuery();
                invoicePositionQuery.identity().like(
                    segment.refGetPath().getDescendant("invoice",":*","invoicePosition","%").toResourcePattern()
                );
                List<InvoicePosition> invoicePositions = segment.getExtent(invoicePositionQuery);
                assertTrue("Invoice Positions: Second Last", invoicePositions.listIterator(9).hasNext());
                assertFalse("Invoice Positions: Last", invoicePositions.listIterator(10).hasNext());
                assertEquals("Invoice Positions: Size", 10,invoicePositions.size());
            }
            {
                //
                // Read via Query
                //
                Query query = persistenceManager.newQuery(InvoicePosition.class);
                InvoicePositionQuery invoicePositionQuery = (InvoicePositionQuery) query;
                invoicePositionQuery.identity().like(
                    segment.refGetPath().getDescendant("invoice",":*","invoicePosition","%").toResourcePattern()
                );
                query.setCandidates(segment.getExtent());                
                List<InvoicePosition> invoicePositions = (List<InvoicePosition>) query.execute();
                assertTrue("Invoice Positions: Second Last", invoicePositions.listIterator(9).hasNext());
                assertFalse("Invoice Positions: Second Last", invoicePositions.listIterator(10).hasNext());
                assertEquals("Invoice Positions: Size", 10,invoicePositions.size());
            }
            {
                InvoicePositionQuery invoicePositionQuery = app1Package.createInvoicePositionQuery();
                // get products without price. price is an expensive derived
                // atttribute. Therefore this iteration should be much faster
                // than the next one
                startedAt = System.currentTimeMillis();
                persistenceManager.refresh(invoice);
                InvoiceHasInvoicePosition.InvoicePosition<InvoicePosition> allInvoicePositions = invoice.getInvoicePosition();
                Collection<InvoicePosition> someInvoicePositions = invoicePositionQuery == null ?
                    allInvoicePositions :
                        allInvoicePositions.getAll(invoicePositionQuery);
                for(InvoicePosition invoicePosition : someInvoicePositions) {
                    Product product = invoicePosition.getProduct();
                    product.getDescription();
//                  System.out.println("product[" + i + "]");
//                  System.out.println("  description=" + product.getDescription());
                }
                System.out.println("time for retrieving 10 invoice positions (without price)=" + (System.currentTimeMillis() - startedAt));

                startedAt = System.currentTimeMillis();
                persistenceManager.refresh(invoice);
                allInvoicePositions  = invoice.getInvoicePosition();
                for(InvoicePosition invoicePosition : allInvoicePositions){
                    Product product = invoicePosition.getProduct();
                    System.out.println("Product " + product.getDescription() + " costs " + product.getPrice());
//                  System.out.println("product[" + i + "]");
//                  System.out.println("  description=" + product.getDescription());
//                  System.out.println("  price=" + product.getPrice());
                }
                System.out.println("time for retrieving 10 invoice positions (with price)=" + (System.currentTimeMillis() - startedAt));
            }

            /**
             * Test Address
             */
            // get AddressFormat
            Collection<AddressFormat> addressFormats = segment.getAddressFormat();
            for(AddressFormat addressFormat : addressFormats) {
                System.out.println("addressFormat=" + addressFormat);
            }

            // get NameFormat
            Collection<NameFormat> nameFormats = segment.getNameFormat();
            for(NameFormat nameFormat: nameFormats) {
                System.out.println("nameFormat=" + nameFormat);
            }

            // reference nameFormat has multiplicity 1..1. Test for NOT_FOUND exception
            // TODO
//          try {
//          segment.getNameFormat(false, "unknown");
////        fail("NOT_FOUND expected"); // TODO: make NOT_FOUND exception to be thrown
//          } catch(JmiServiceException e) {
//          assertEquals("NOT_FOUND expected", BasicException.Code.NOT_FOUND, e.getExceptionCode());
//          }
//          // reference addressFormat has multiplicity 0..1. Test for null
//          assertNull("addressFormat must be null", segment.getAddressFormat("unknown"));


            // modify feature 
            for(NameFormat nameFormat: nameFormats) try {
                persistenceManager.currentTransaction().begin();
                nameFormat.refSetValue(
                    "description",
                    "modified description"
                );
                persistenceManager.currentTransaction().commit();
                fail("all attributes are non changeable --> object can not be updated");
            } catch(JDOFatalDataStoreException e) {
                System.out.println("all attributes are non changeable --> object can not be updated");
            }

            try {
                NameFormat nameFormat = (NameFormat) persistenceManager.newInstance(NameFormat.class);
                persistenceManager.currentTransaction().begin();
                nameFormat.setDescription(
                    "a description"
                );
                segment.getNameFormat().add(nameFormat);
                persistenceManager.currentTransaction().commit();
                fail("constraint isFrozen --> object can not be updated");
            } catch(JDOFatalDataStoreException e) {
                if(persistenceManager.currentTransaction().isActive()){
                    persistenceManager.currentTransaction().rollback();
                }
                System.out.println("constraint isFrozen --> object can not be updated");
            }


            InternationalPostalAddress postalAddress = null;
            EmailAddress emailAddress = null;
            assertEquals("Initial address count", 0, segment.getAddress().size());
            for(
                    int i = 0;
                    i < 4;
                    i++
            ){
                System.out.println(
                    new String[]{
                        "Rollback address addition",
                        "Clear persistent address collection",
                        "Clear transient address collection",
                        "Commit address addition"
                    }[i]
                );
                persistenceManager.currentTransaction().begin();
                postalAddress = postalAddressClass.createInternationalPostalAddress();
                postalAddress.setCountry("Switzerland");
                postalAddress.setCity("Zurich");
                postalAddress.setHouseNumber("57");
                postalAddress.setPostalCode("8005");
                postalAddress.setStreet("Bahnhofstr.");
                postalAddress.setAddressLine("Familie", "Muster");
                segment.addAddress(false, "0001", postalAddress);

                // create a EmailAddress
                emailAddress = emailAddressClass.createEmailAddress();
                emailAddress.setAddress("hans.muster@app1.ch");
                segment.addAddress(false, "0002", emailAddress);
                assertEquals(
                    "Transient added address count",
                    2,
                    segment.getAddress().size()
                );
                switch(i){
                    case 0:
                        persistenceManager.currentTransaction().rollback();
                        assertEquals(
                            "Rolled back address count",
                            0,
                            segment.getAddress().size()
                        );
                        break;
                    case 1:
                        persistenceManager.currentTransaction().commit();
                        assertEquals(
                            "Commited address count",
                            2,
                            segment.getAddress().size()
                        );
                        persistenceManager.currentTransaction().begin();
                        segment.getAddress().clear();
                        assertEquals(
                            "Transient cleared address count",
                            0,
                            segment.getAddress().size()
                        );
                        persistenceManager.currentTransaction().commit();
                        assertEquals(
                            "Cleared persistent address count",
                            0,
                            segment.getAddress().size()
                        );
                        break;
                    case 2:
                        segment.getAddress().clear();
                        assertEquals(
                            "Cleared transient address count",
                            0,
                            segment.getAddress().size()
                        );
                        persistenceManager.currentTransaction().commit();
                        assertEquals(
                            "Cleared committed address count",
                            0,
                            segment.getAddress().size()
                        );
                        break;
                    case 3:
                        EmailAddress transientAddress = emailAddressClass.createEmailAddress();
                        transientAddress.setAddress("john.player@games.net");
                        segment.addAddress(false, "0003", transientAddress);
                        assertEquals(
                            "Transient added address count",
                            3,
                            segment.getAddress().size()
                        );
                        transientAddress.refDelete(); // segment.getAddress().remove(transientAddress);
                        persistenceManager.currentTransaction().commit();
                        assertEquals(
                            "Commited address count",
                            2,
                            segment.getAddress().size()
                        );
                        break;
                    default:
                        fail("No more instructions");
                }
                assertTrue(
                    "Identity should be available outside the unit of work",
                    Arrays.equals(
                        refGetPath(segment).getSuffix(
                            refGetPath(segment).size() - 2
                        ),
                        new String[]{"segment",SEGMENT_NAME}
                    )
                );
            }

            try {
                persistenceManager.currentTransaction().begin();
                InternationalPostalAddress duplicateAddress = postalAddressClass.createInternationalPostalAddress();
                duplicateAddress.setCountry("Switzerland");
                duplicateAddress.setCity("Zurich");
                duplicateAddress.setHouseNumber("57");
                duplicateAddress.setPostalCode("8005");
                duplicateAddress.setStreet("Bahnhofstr.");
                duplicateAddress.setAddressLine(new String[]{"Familie", "Muster"});
                emailAddress.setAddress("hans.muster@app1.int");
                assertEquals(
                    "Transient E-Mail-Address should have changed",
                    "hans.muster@app1.int",
                    emailAddress.getAddress()
                );
                segment.addAddress(false,"0001", duplicateAddress);
                persistenceManager.currentTransaction().commit();
                fail("DUPLICATE expected");
            } catch (JmiServiceException exception) {
                assertTrue(
                    "Early duplicate recognition", 
                    persistenceManager.currentTransaction().isActive()
                );
                assertEquals(
                    "Duplicate exception expected",
                    BasicException.Code.DUPLICATE,
                    exception.getExceptionCode()
                );
                persistenceManager.currentTransaction().rollback();
            } catch (JDOException exception) {
                assertFalse(
                    "Late duplicate recognition", 
                    persistenceManager.currentTransaction().isActive()
                );
                BasicException exceptionStack = BasicException.toExceptionStack(exception);
                assertTrue(
                    "Unit of work failure",
                    exceptionStack.getExceptionCode() == BasicException.Code.ABORT ||
                    exceptionStack.getExceptionCode() == BasicException.Code.ROLLBACK
                );
            }
            assertEquals(
                "Persistent E-Mail-Address shouldn't have changed",
                "hans.muster@app1.ch",
                emailAddress.getAddress()
            );
            assertTrue(
                "Identity should be available after unit of work failure",
                emailAddress.getIdentity().endsWith("/segment/Standard/address/0002")
            );

            // invoke sendMessageTemplate (struct with object reference field)
            persistenceManager.currentTransaction().begin();
            MessageTemplate messageTemplate = messageTemplateClass.createMessageTemplate();
            messageTemplate.setText("hello world");
            segment.addMessageTemplate(
                false,
                "template0",
                messageTemplate
            );
            persistenceManager.currentTransaction().commit();
            persistenceManager.currentTransaction().begin();
            EmailAddressSendMessageTemplateResult sendResult = emailAddress.sendMessageTemplate(
                app1Package.createEmailAddressSendMessageTemplateParams(
                    messageTemplate,
                    0,
                    "hello world"
                )
            );
            assertNotNull("Send result", sendResult);
            persistenceManager.currentTransaction().commit();

            // create a person without qualifier
            Person person;


           {
                //
                // CR0003390 Code Accessor
                // 
                persistenceManager.currentTransaction().begin();
                person = segment.getPerson(false,"DOE");
                Runtime runtime = Runtime.getRuntime();
                segment.refAddToUnitOfWork();
                long initialMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
                for(
                    int i = 1, limit = 10000;
                    i < 1000;
                    i++
                ){
                    person = segment.getPerson("DOE");
                    long currentMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
                    long additionalMemoryUsage = currentMemoryUsage - initialMemoryUsage;
                    if(additionalMemoryUsage > limit) {
                        runtime.gc();
                        currentMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
                        additionalMemoryUsage = currentMemoryUsage - initialMemoryUsage;
                        assertFalse(
                           "Memory used up after " + i + " failed retrievals: " + additionalMemoryUsage,
                            additionalMemoryUsage > limit
                        );
                    }
                }
                persistenceManager.currentTransaction().commit();
            }

            try {
                //
                // CR0003686 
                //
                persistenceManager.currentTransaction().begin();
                person = personClass.createPerson();
                person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1963-01-01"));
                person.setLastName("Rossi");
                person.setSalutation("Signor");
                person.setSex((short)0);
                person.getGivenName().add("Alfonso");
                person.getAge(); // TODO Load required
                segment.addPerson(false, nextId(), person);
                persistenceManager.currentTransaction().commit();
                fail("'Signor' was expected not to be supported");
            } catch(JDOFatalDataStoreException exception) {
                //
                // Unsupported language prevents commit
                //
            }

            persistenceManager.currentTransaction().begin();
            person = personClass.createPerson();
            person.setForeignId("FX");
            person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1960-01-01"));
            person.setBirthdateAsDateTime(new Date());
            /* d = */ person.getBirthdateAsDateTime();
            person.setLastName("MusterX");
            person.setSalutation("Herr");
            person.setSex((short)0);
            person.getGivenName().add("Hans");
            person.getGivenName().add("Heiri");
            person.setGivenName("Hans", "Heiri");
            person.getAssignedAddress().addAll(Arrays.asList(postalAddress,emailAddress));
            segment.addPerson(false, nextId(), person);
            persistenceManager.currentTransaction().commit();

            assertEquals("person.refMofId() must be object path", 1, new Path(person.refMofId()).size() % 2);
            assertEquals("person's path must be object path", 1, refGetPath(person).size() % 2);
            assertEquals("person.refIdentity() must corrspond to its path", refGetPath(person).toXRI(), person.getIdentity());
            assertEquals("person.refMofId() must corrspond to its path", refGetPath(person).toXRI(), person.refMofId());

            assertEquals(
                "Initial postal code without country code",
                "8005",
                postalAddress.getPostalCode()
            );

            // Add country code to postal code
            persistenceManager.currentTransaction().begin();
            person.voidOp();
            persistenceManager.currentTransaction().commit();

//          try {
//          persistenceManager.currentTransaction().begin();
//          postalAddress.setPostalCode("D-S-8005");
//          persistenceManager.currentTransaction().commit();
//          fail("CONCURRENT_ACCESS_FAILURE expected");
//          } catch (JDOOptimisticVerificationException exception){
//          assertEquals(
//          "CONCURRENT_ACCESS_FAILURE expected",
//          BasicException.Code.CONCURRENT_ACCESS_FAILURE,
//          BasicException.toStackedException(exception).getCause(null).getExceptionCode()
//          );
//          }

            assertEquals(
                "voidOp should have updated the postal codes",
                "CH-8005",
                postalAddress.getPostalCode()
            );

            // get assigned addresses by index
            for(
                    int i = 0;
                    i < 2;
                    i++
            ) {
                // postal code not yet refreshed
                List<Address> addresses = person.getAssignedAddress();
                Address address = addresses.get(i);
                System.out.println("assigned address=" + refGetPath(address));
            }

            persistenceManager.currentTransaction().begin();
            PostalAddress additionalAddress = app1Package.getPostalAddress().createPostalAddress();
            additionalAddress.setCity("Zurich");
            additionalAddress.setHouseNumber("1");
            additionalAddress.setPostalCode("8050");
            additionalAddress.setStreet("Technoparkstrasse");
            // get assigned addresses by iterator
            System.out.println("adding three more addresses");
            segment.addAddress(false, "CR0002096", additionalAddress);
            person.getAssignedAddress().addAll(Arrays.asList(postalAddress,additionalAddress,emailAddress));
            persistenceManager.currentTransaction().commit();

            List<Address> assignedAddresses = person.getAssignedAddress();
            for(Address address : assignedAddresses) {
                // postal code refreshed
                if(refGetPath(address).equals(refGetPath(postalAddress))) {
                    if(address instanceof DelegatingRefObject_1_0) {
                        assertSame(
                            "created and retrieved object should be the same",
                            ((DelegatingRefObject_1_0)address).openmdxjdoGetDataObject(),
                            ((DelegatingRefObject_1_0)postalAddress).openmdxjdoGetDataObject()
                        );
                    } else {
                        assertSame(
                            "created and retrieved object should be the same",
                            ((RefObject_1_0)address).refDelegate(),
                            ((RefObject_1_0)postalAddress).refDelegate()
                        );
                    }
                }
                System.out.println("assigned address=" + refGetPath(address));
            }

            assertEquals("number of assigned addresses", 5, person.getAssignedAddress().size());

            // assignAddress by operation. This operation does not really
            // perform an assign. It is just there to see whether the operation
            // invocation works.
            persistenceManager.currentTransaction().begin();
            person.assignAddress(
                app1Package.createPersonAssignAddressParams(
                    Arrays.asList(
                        new Address[]{
                            postalAddress,
                            emailAddress
                        }
                    )
                )
            );
            persistenceManager.currentTransaction().commit();

            //
            // CR0002096
            // 
            persistenceManager.currentTransaction().begin();
            additionalAddress.refDelete();
            persistenceManager.currentTransaction().commit();
            persistenceManager.currentTransaction().begin();
            int j = 0;
            assignedAddresses = person.getAssignedAddress();
            for(
                    Iterator<Address> i = assignedAddresses.iterator();
                    i.hasNext();
                    j++
            ){
                // postal code refreshed
                Address address;
                try {
                    address = i.next();
                    if(address == null || refGetPath(address) == null) {
                        throw new InvalidObjectException(
                            address,
                            "Returning null was the former behaviour"
                        );
                    }
                    if(refGetPath(address).equals(refGetPath(postalAddress))) {
                        if(address instanceof DelegatingRefObject_1_0) {
                            assertSame(
                                "created and retrieved object should be the same",
                                ((DelegatingRefObject_1_0)address).openmdxjdoGetDataObject(),
                                ((DelegatingRefObject_1_0)postalAddress).openmdxjdoGetDataObject()
                            );
                        } else {
                            assertSame(
                                "created and retrieved object should be the same",
                                ((RefObject_1_0)address).refDelegate(),
                                ((RefObject_1_0)postalAddress).refDelegate()
                            );
                        }
                    }
                    System.out.println("Assigned address " + j + ": " + refGetPath(address));
                } catch (InvalidObjectException exception) {
                    i.remove();
                    System.out.println("Assigned address " + j + ": removed");
                }
            }
            persistenceManager.currentTransaction().commit();
            assertEquals("number of assigned addresses", 4, person.getAssignedAddress().size());

            //
            // CR0002987
            // 
            System.out.println("Explicit rollback test");
            persistenceManager.currentTransaction().begin();
            additionalAddress = app1Package.getPostalAddress().createPostalAddress();
            additionalAddress.setCity("Seldwyla");
            additionalAddress.setHouseNumber("0");
            additionalAddress.setPostalCode("0000");
            additionalAddress.setStreet("Kirchgasse");
            assertFalse("Additional address not yet persistent", JDOHelper.isPersistent(additionalAddress));
            assertFalse("Additional address not yet new", JDOHelper.isNew(additionalAddress));
            segment.addAddress(false,"9001", additionalAddress);
            assertTrue("Additional address now persistent", JDOHelper.isPersistent(additionalAddress));
            assertTrue("Additional address now new", JDOHelper.isNew(additionalAddress));
            persistenceManager.currentTransaction().rollback();
            assertFalse("Additional address no longer persistent", JDOHelper.isPersistent(additionalAddress));
            assertFalse("Additional address no longer new", JDOHelper.isNew(additionalAddress));
            try {
                System.out.println("Implicit rollback test");
                persistenceManager.currentTransaction().begin();
                additionalAddress = app1Package.getPostalAddress().createPostalAddress();
                additionalAddress.setCity("Seldwyla");
                additionalAddress.setHouseNumber("0");
                additionalAddress.setPostalCode("0000");
                additionalAddress.setStreet("Kirchgasse");
                assertFalse("Additional address not yet persistent", JDOHelper.isPersistent(additionalAddress));
                assertFalse("Additional address not yet new", JDOHelper.isNew(additionalAddress));
                segment.addAddress(false,"CR0002987", additionalAddress);
                assertTrue("Additional address now persistent", JDOHelper.isPersistent(additionalAddress));
                assertTrue("Additional address now new", JDOHelper.isNew(additionalAddress));
                NameFormat jmiNameFormat = app1Package.getNameFormat().createNameFormat();
                jmiNameFormat.setDescription("modified description");
                segment.addNameFormat(false,nextId(), jmiNameFormat);
                persistenceManager.currentTransaction().commit();
                fail("constraint isFrozen --> object can not be updated");
            } catch(JDOFatalDataStoreException e) {
                assertFalse("Additional address no longer new", JDOHelper.isNew(additionalAddress));
                assertFalse(
                    "Additional address no longer persistent", 
                    JDOHelper.isPersistent(additionalAddress)
                );
            }

            for(
                    int i = 0;
                    i < 2;
                    i++
            ){
                assertNull("No TRANSIENT person expected", segment.getPerson(false, "TRANSIENT"));
                if(i==0) persistenceManager.currentTransaction().begin();
            }
            // create and remove in same unit of work
            person = personClass.createPerson();
            person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1960-01-01"));
            person.setBirthdateAsDateTime(new Date());
            person.setForeignId("FX");
            person.setLastName("MusterX");
            person.setSalutation("Herr");
            person.setSex((short)0);
            person.getMemberOfGroup().addAll(
                Arrays.asList(new String[]{"group A", "group B"})
            );
            person.getGivenName().addAll(
                Arrays.asList(new String[]{"Hans", "Heiri"})
            );
            person.getAdditionalInfo().put(
                new Integer(0),
                "additional info 1"
            );
            person.getAdditionalInfo().put(
                new Integer(1),
                "additional info 2"
            );
            person.getAssignedAddress().addAll(
                Arrays.asList(new Address[]{postalAddress, emailAddress})
            );

            segment.addPerson(false,"TRANSIENT", person);
            segment.getPerson(false, "TRANSIENT").refDelete(); // get and remove it in same unit of work
            persistenceManager.currentTransaction().commit();

            // create some PersonGroups
            persistenceManager.currentTransaction().begin();
            PersonGroup g0 = personGroupClass.createPersonGroup();
            g0.setName("Group 0");
            segment.addPersonGroup(
                false,
                "g0",
                g0
            );
            PersonGroup g1 = personGroupClass.createPersonGroup();
            g1.setName("Group 1");
            segment.addPersonGroup(
                false,
                "g1",
                g1
            );
            PersonGroup g2 = personGroupClass.createPersonGroup();
            g2.setName("Group 2");
            segment.addPersonGroup(
                false,
                "g2",
                g2
            );
            persistenceManager.currentTransaction().commit();

            // create some Persons
            persistenceManager.currentTransaction().begin();
            for(
                    int i = 0;
                    i <= N_PERSONS;
                    i++
            ) {
                person = personClass.createPerson();
                person.setForeignId("F" + i);
                person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1960-01-01"));
                person.setBirthdateAsDateTime(new Date());
                person.setLastName("Muster" + i);
                person.setSalutation("Herr");
                person.setSex((short)0);
                person.getGivenName().add("Hans");
                person.getGivenName().add("Heiri");
                person.setGivenName(new String[]{"Hans", "Heiri"});
                person.getAssignedAddress().add(postalAddress);
                person.getPersonGroup().add(g0);
                person.getPersonGroup().add(g1);
                person.getPersonGroup().add(g2);
                if(i == N_PERSONS) {
//                  TODO or NOTTODO            
//                  try {
//                  segment.addForeignPerson("F" + N_PERSONS, person);
//                  }
//                  catch(JmiServiceException e) {
//                  System.out.println("exception=" + e);
//                  //assertEquals("excepted exception NOT_SUPPORTED", CommonExceptions.NOT_SUPPORTED, e.getExceptionCode());
//                  }
                }
                else {
                    segment.addPerson(false, "000" + i, person);
                }
            }
            persistenceManager.currentTransaction().commit();

            // get person on 'composite' association 'SegmentHasPerson'
            person = segment.getPerson(false,"0001");
            System.out.println("person.age=" + person.getAge());
            System.out.println("person givenName=" + person.getGivenName().get(0));
            System.out.println("person.identity=" + person.getIdentity());
            System.out.println("person.creationDateTime=" + person.getCreationDateTime());
            System.out.println("person.createdAt=" + person.getCreatedAt());

            // test unqualified feature retrieval
            assertTrue("postalAddress.address must be instance of String", emailAddress.refGetValue("address") instanceof String);
            assertTrue("segment.address must be instance of Container", segment.refGetValue("address") instanceof RefContainer);
            assertTrue("postalAddress.address must be instance of String", emailAddress.refGetValue("address") instanceof String);

            // test performance of native reading all attributes of person 1000 times
            startedAt = System.currentTimeMillis();
            for(
                    int i = 0;
                    i < 1000;
                    i++
            ) {
                person.refGetValue("lastName");
                person.refGetValue("foreignId");
                person.refGetValue("givenName");
                person.refGetValue("sex");
                person.refGetValue("salutation");
                person.refGetValue("birthdate");
                person.refGetValue("birthdateAsDateTime");
                person.refGetValue("additionalInfo");
                person.refGetValue("memberOfGroup");
                person.refGetValue("age");
                person.refGetValue("creationDateTime");
            }
            System.out.println("time for inspecting person 1000 times [native]=" + (System.currentTimeMillis() - startedAt));

            // test performance of accessor.jmi of reading all attributes of person 1000 times
            startedAt = System.currentTimeMillis();
            for(
                    int i = 0;
                    i < 1000;
                    i++
            ) {
                person.getLastName();
                person.getForeignId();
                person.getGivenName();
                person.getSex();
                person.getSalutation();
                person.getBirthdate();
                person.getBirthdateAsDateTime();
                person.getAdditionalInfo();
                person.getMemberOfGroup();
                person.getAge();
                person.getCreationDateTime();
            }
            System.out.println("time for inspecting person 1000 times [jmi]=" + (System.currentTimeMillis() - startedAt));
            //
            // test the performance of reflective JMI accesses
            //
            startedAt = System.currentTimeMillis();
            for(
                    int i = 0;
                    i < 1000;
                    i++
            ) {
                person.refGetValue("lastName");
                person.refGetValue("foreignId");
                person.refGetValue("givenName");
                person.refGetValue("sex");
                person.refGetValue("salutation");
                person.refGetValue("birthdate");
                person.refGetValue("birthdateAsDateTime");
                person.refGetValue("additionalInfo");
                person.refGetValue("memberOfGroup");
                person.refGetValue("age");
                person.refGetValue("creationDateTime");
            }
            System.out.println("time for inspecting person 1000 times [jmi delegation]=" + (System.currentTimeMillis() - startedAt));

            // test refMetaObject      
            ModelElement_1_0 personDef = ((RefMetaObject_1)person.refMetaObject()).getElementDef();
            /* ModelElement_1_0 salutationDef = */ model.getFeatureDef(
                personDef,
                "salutation",
                false
            );
            /* salutationDef = */ model.getFeatureDef(
                personDef,
                "blabla",
                false
            );
            Map<?,?> attributes = (Map<?,?>)personDef.objGetValue("attribute");
            /* salutationDef = (ModelElement_1_0) */ attributes.get("salutation");

            // get person on 'none', derived association 'SegmentReferencesForeignPerson'
            person = segment.getForeignPerson("F1");
            assertNotNull("Foreign Person", person);
            PersistenceManager m = JDOHelper.getPersistenceManager(person);
            assertSame(
                "Derived association marshalling", 
                persistenceManager, 
                m
            );

            System.out.println("person.age=" + person.getAge());
            System.out.println("person givenName=" + person.getGivenName().get(0));

            int people = segment.getForeignPerson().size();
            System.out.println("Number of people: " + people);
                
            // get persons with filter 1
            PersonQuery personQuery = app1Package.createPersonQuery();
            personQuery.lastName().like(
                "Muster1.*"
            );
            personQuery.birthdateAsDateTime().lessThanOrEqualTo(
                new Date()
            );
            personQuery.orderByCreatedAt().ascending();
            SegmentHasPerson.Person<Person> personCollection = segment.getPerson();
            for(
                    Iterator<Person> i = personCollection.iterator();
                    i.hasNext();
            ){
                Person p = i.next();
                m = JDOHelper.getPersistenceManager(p);
                assertSame(
                    "Query result marshalling", 
                    persistenceManager, 
                    m
                );
                break; // Test at least one persistence manager
            }

//          List<Person> personList = personCollection.getAll(personQuery);

            List<Person> personList = segment.getPerson(personQuery);
            boolean nobodyOutThere = personList.isEmpty();
            System.out.println("There are " + (nobodyOutThere ? "no" : "some") + " people");
            assertFalse("Anybody out there", nobodyOutThere);
            for(Person p : personList) {
                assertSame(
                    "Query result marshalling", 
                    persistenceManager, 
                    JDOHelper.getPersistenceManager(p)
                );
                SysLog.trace("person", p);
            }

            // get persons with SOUNDS like filter
            personQuery = app1Package.createPersonQuery();
            personQuery.lastName().like(
                StringTypePredicate.SOUNDS,
                "Maasteer"
            );

            people = segment.getPerson().size();
            // 1 added by XmlImporter, 1 added with addPerson(), N_PERSONS added by addPerson()

            SegmentHasPerson.Person<Person> allPeople = segment.getPerson();
            List<Person> maasteer = allPeople.getAll(personQuery);
            int numberOfPersons = maasteer.size();
            assertEquals(
                "number of persons found with SOUNDS_LIKE",
                TEST_PERSON_COUNT + 3,
                numberOfPersons
            );
            persistenceManager.currentTransaction().begin();
            maasteer.clear();
            assertEquals(
                "number of persons found with SOUNDS_LIKE",
                people - TEST_PERSON_COUNT - 3,
                segment.getPerson().size()
            );
            persistenceManager.currentTransaction().rollback();

            allPeople = segment.getPerson();
            maasteer = allPeople.getAll(personQuery);
            {
                assertTrue("People found with SOUNDS_LIKE: Second Last",maasteer.listIterator(TEST_PERSON_COUNT + 2).hasNext());
                assertFalse("People found with SOUNDS_LIKE: Second Last",maasteer.listIterator(TEST_PERSON_COUNT + 3).hasNext());
            }
            numberOfPersons = maasteer.size();
            assertEquals(
                "number of persons found with SOUNDS_LIKE",
                TEST_PERSON_COUNT + 3,
                numberOfPersons
            );
            persistenceManager.currentTransaction().begin();
            maasteer.clear();
            assertEquals(
                "number of persons found with SOUNDS_LIKE",
                people - TEST_PERSON_COUNT - 3,
                segment.getPerson().size()
            );
            persistenceManager.currentTransaction().rollback();
            
            // find persons with assigned address
            personQuery = app1Package.createPersonQuery();
            personQuery.thereExistsAssignedAddress().equalTo(postalAddress);
            personCollection = segment.getPerson();
            for(Person p : personCollection.getAll(personQuery)) {
                SysLog.trace("person", p);
            }

            // find persons with empty filter
            personQuery = app1Package.createPersonQuery();
            personCollection = segment.getPerson();
            for(Person p : personCollection.getAll(personQuery)) {
                SysLog.trace("person", (Person)p);
            }

            // 
            // Test CR0003454
            // 
            personQuery = app1Package.createPersonQuery();
            personQuery.foreignId().like("F.");
            personQuery.orderByForeignId().ascending();
            List<Person> cr0003454 = segment.getPerson(personQuery);
            ListIterator<Person> pi = cr0003454.listIterator();
            for(
                    int i = 0;
                    i < 6;
                    i++
            ) {
                assertEquals("ListIterator.nextIndex()", i, pi.nextIndex());
                Person pp = (Person) pi.next();
                System.out.println("person["+i+"] "+pp.getForeignId());
            }
            for(
                    int i = 5;
                    i >= 0;
                    i--
            ) {
                assertEquals("ListIterator.previousIndex()", i, pi.previousIndex());
                Person pp = (Person) pi.previous();
                assertEquals("Person["+i+"].foreignId", "F"+i, pp.getForeignId());
            }

            // modify given name
            persistenceManager.currentTransaction().begin();
            person.setGivenName(new String[]{"Heiri"});
            System.out.println("person modified givenName=" + person.getGivenName().get(0));
            persistenceManager.currentTransaction().commit();
            persistenceManager.refresh(person);
            assertEquals("giveName", "Heiri", person.getGivenName().get(0));

            persistenceManager.currentTransaction().begin();
            person.getGivenName().clear();
            persistenceManager.currentTransaction().commit();
            persistenceManager.refresh(person);
            assertEquals("size givenName", person.getGivenName().size(), 0);

            // person.formatAs
            persistenceManager.currentTransaction().begin(); // isQuery() is false      
            PersonFormatNameAsResult formattedName = person.formatNameAs(
                app1Package.createPersonFormatNameAsParams("Standard")
            );
            persistenceManager.currentTransaction().commit(); // result available after commit only               
            System.out.println("formatted name=" + formattedName.getFormattedName());
            System.out.println("formatted name as set=" + formattedName.getFormattedNameAsSet());
            System.out.println("formatted name as list=" + formattedName.getFormattedNameAsList());
            System.out.println("formatted name as sparsearray=" + formattedName.getFormattedNameAsSparseArray());

            // test optional argument
            persistenceManager.currentTransaction().begin(); // isQuery() is false               
            formattedName = person.formatNameAs(
                app1Package.createPersonFormatNameAsParams(
                    null // default value is Standard
                )
            );
            persistenceManager.currentTransaction().commit(); // result available after commit only               
            System.out.println("formatted name=" + formattedName.getFormattedName());
            // test exceptions
            try {
                person.formatNameAs(
                    app1Package.createPersonFormatNameAsParams(
                        "InvalidFormat"
                    )
                );
                fail("CanNotFormatNameException expected");
            } catch(CanNotFormatNameException e) {
                System.out.println("formatNameAs() raised exception as expected \n" + e.getMessage());
            }

            // test dateOp (date and dateTime in operation parameter)
            // Test for non-query operation with result 
            persistenceManager.currentTransaction().begin();
            Date dateTimeNow = new Date();
            PersonDateOpResult dateOpResult = person.dateOp(
                app1Package.createPersonDateOpParams(
                    Datatypes.create(
                        XMLGregorianCalendar.class, 
                        DateFormat.getInstance().format(dateTimeNow).substring(0, 8)
                    ),
                    dateTimeNow
                )
            );
            persistenceManager.currentTransaction().commit();
            System.out.println("dateOp.dateResult=" + dateOpResult.getDateResult());
            System.out.println("dateOp.dateTimeResult=" + dateOpResult.getDateTimeResult());

            // no more NOT_FOUND exceptions
            assertNull("Not existing person", segment.getPerson("alskdjflaksdjf"));

            // remove some persons

            System.out.println("removing person=" + segment.getPerson("0001").getLastName());
            System.out.println("removing person=" + segment.getPerson("00053").getLastName());
            System.out.println("removing person=" + segment.getPerson("00082").getLastName());

            int initialPersonCount = segment.getPerson().size();
            persistenceManager.currentTransaction().begin();
            segment.getPerson(false,"0001").refDelete();
            segment.getPerson(false,"00053").refDelete();
            segment.getPerson(false,"00082").refDelete();
            int finalPersonCount = segment.getPerson().size();
            assertEquals(
                "Transient person count",
                initialPersonCount - 3,
                finalPersonCount
            );
            persistenceManager.currentTransaction().rollback();

            finalPersonCount = segment.getPerson().size();
            assertEquals(
                "Rollback person count",
                initialPersonCount,
                finalPersonCount
            );
            persistenceManager.currentTransaction().begin();
            segment.getPerson(false,"0001").refDelete();
            segment.getPerson(false,"00053").refDelete();
            segment.getPerson(false,"00082").refDelete();
            finalPersonCount = segment.getPerson().size();
            assertEquals(
                "Transient person count",
                initialPersonCount - 3,
                finalPersonCount
            );
            persistenceManager.currentTransaction().commit();
            finalPersonCount = segment.getPerson().size();
            assertEquals(
                "Commit person count",
                initialPersonCount - 3,
                finalPersonCount
            );

            // ... and test whether they are removed
            assertNull("person 0001 not removed", segment.getPerson("0001"));

            // CR0003390 Code Accessor
            persistenceManager.currentTransaction().begin();
            person = segment.getPerson("DOE");
            assertNull("DOE does not exist", person);
            person = personClass.createPerson();
            segment.addPerson(false,"DOE", person);
            assertTrue("DOE is persistent-new", JDOHelper.isNew(person));
            assertFalse("DOE is persistent-new", JDOHelper.isDeleted(person));
            person.refDelete();
            person = segment.getPerson("DOE");
            assertTrue("DOE is persistent-new-deleted", JDOHelper.isNew(person));
            assertTrue("DOE is persistent-new-deleted", JDOHelper.isDeleted(person));
            persistenceManager.currentTransaction().rollback();

            // Add after failed get
            assertNull("person NO1 exists", segment.getPerson("NO1"));

            persistenceManager.currentTransaction().begin();
            person = personClass.createPerson();
            person.setForeignId("X1");
            person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1961-11-11"));
            person.setBirthdateAsDateTime(new Date());
            person.setLastName("Muster1");
            person.setSalutation("Herr");
            person.setSex((short)0);
            person.getGivenName().add("Hans");
            person.getGivenName().add("Heiri");
            person.setGivenName(new String[]{"Hans", "Heiri"});
            person.getAssignedAddress().add(postalAddress);
            segment.addPerson(false, "NO1", person);
            persistenceManager.currentTransaction().rollback();

            // ... and test whether they are removed
            assertNull("Person N01", segment.getPerson(false,"NO1"));
            segment.getPerson().remove(QualifierType.REASSIGNABLE,"NO1");
            
            // A non-existent person
            assertNull("Person N02", segment.getPerson(false,"NO2"));
            segment.getPerson().remove(QualifierType.REASSIGNABLE,"NO2");
            
            // Add after failed removal
            persistenceManager.currentTransaction().begin();
            person = personClass.createPerson();
            person.setForeignId("X2");
            person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1961-11-11"));
            person.setBirthdateAsDateTime(new Date());
            person.setLastName("Muster1");
            person.setSalutation("Herr");
            person.setSex((short)0);
            person.getGivenName().add("Hans");
            person.getGivenName().add("Heiri");
            person.setGivenName(new String[]{"Hans", "Heiri"});
            person.getAssignedAddress().add(postalAddress);
            segment.addPerson(false,"NO2", person);
            persistenceManager.currentTransaction().rollback();

            assertNull("person 00053 not removed", segment.getPerson("00053"));
            assertNull("person 00082 not removed", segment.getPerson("00082"));

            // postalAddress.formatAs
            AddressFormatAsResult formattedAddress = null;
            formattedAddress = postalAddress.formatAs(
                app1Package.createAddressFormatAsParams(
                    "Standard"
                )
            );
            System.out.println("formatted address=" + formattedAddress.getFormattedAddress());

            // emailAddress.formatAs
            formattedAddress = emailAddress.formatAs(
                app1Package.createAddressFormatAsParams(
                    "Standard"
                )
            );
            System.out.println("formatted address=" + formattedAddress.getFormattedAddress());

            // get addresses by iterator
            SegmentHasAddress.Address<Address> addresses = segment.getAddress();
            for(Address address : addresses) {
                System.out.println("address.id=" + address.getId());
                System.out.println("address=" + address);

                // invoke sendMessage on PostalAddress
                if(address instanceof PostalAddress) {
                    persistenceManager.currentTransaction().begin(); // isQuery() is false               
                    ((PostalAddress)address).sendMessage(
                        app1Package.createPostalAddressSendMessageParams(
                            new byte[]{'h', 'e', 'l', 'l', 'o'}
                        )
                    );
                    persistenceManager.currentTransaction().commit();
                }
                else if(address instanceof EmailAddress) {
                    persistenceManager.currentTransaction().begin(); // isQuery() is false               
                    ((EmailAddress)address).sendMessage(
                        app1Package.createEmailAddressSendMessageParams(
                            "hello"
                        )
                    );
                    persistenceManager.currentTransaction().commit();
                }
                else {
                    fail("address format " + address.getClass().getName() + " unknown");
                }
            }

            //
            // test cycles
            //
            persistenceManager.currentTransaction().begin();
            CycleMember1 member1 = cycleMember1Class.createCycleMember1();
            member1.setDescription("this is member1");
            CycleMember2 member2 = cycleMember2Class.createCycleMember2();
            member2.setDescription("this is member2");
            member1.setM2(member2);
            member2.setM1(member1);
            segment.addCycleMember1(false, new BigDecimal(1), member1);
            segment.addCycleMember2(false, "member2", member2);
            persistenceManager.currentTransaction().commit();

            // verify member1, member2
            member1 = segment.getCycleMember1(new BigDecimal(1));
            member2 = member1.getM2();
            System.out.println("member1" + member1);
            System.out.println("member2" + member2);

            {
                assertNotNull("We need a member value for the next test", member2);
                CycleMember1Query query = app1Package.createCycleMember1Query();
                query.thereExistsM2().equalTo(member2);
                query.m2().isNonNull();
                try {
                    query.thereExistsM2().equalTo(null);
                    fail("equalTo's argument must not be null");
                } catch (JmiServiceException exception) {
                    assertEquals("equalTo(null)", BasicException.Code.BAD_PARAMETER, exception.getExceptionCode());
                }
            }

            
            // test streams
            persistenceManager.currentTransaction().begin();

            final int contentLength = 1000;
            byte[] content = new byte[contentLength];
            for(
                    int i = 0;
                    i < contentLength;
                    i++
            ) {
                content[i] = (byte)((short)(i % 256));
            }

            Document document = documentClass.createDocument();

            document.setContent(
                org.w3c.cci2.BinaryLargeObjects.valueOf(content)
            );
            document.setDescription(
                "a random document"
            );
            document.setKeyword(
                new HashSet<String>(
                        Arrays.asList("random", "document", "junit")
                )
            );
            segment.addDocument(false, "myDoc", document);
            persistenceManager.currentTransaction().commit();

            // verify returned document
            document = segment.getDocument("myDoc");
            persistenceManager.refresh(document);
            System.out.println("document.description=" + document.getDescription());
            System.out.println("document.keyword=" + document.getKeyword());
            BinaryLargeObject contentLo = document.getContent();
            Long documentSize = contentLo.getLength();
            if(documentSize != null) {
                assertEquals("document size", contentLength, documentSize.longValue());
            }
            for(int r = 0; r < 2; r++) {
                //
                // test with input stream method
                //
                System.out.println("verifying content (with InputStream)");
                InputStream contentIs = contentLo.getContent();
                assertNotNull("A large object's stream", contentIs);
                for(
                        int i = 0;
                        i < contentLength;
                        i += 10
                ) {
                    assertEquals("content at position " + i, i % 256, contentIs.read());
                    contentIs.skip(9);
                }
                contentIs.close();
                System.out.println("OK");
            }
            for(int r = 0; r < 2; r++) {
                //
                // test with output stream
                //
                System.out.println("verifying content (with OutputStream)");
                ByteArrayOutputStream contentOs = new ByteArrayOutputStream();
                contentLo.getContent(contentOs, 0);
                contentOs.close();
                InputStream contentIs = new ByteArrayInputStream(contentOs.toByteArray());
                for(
                        int i = 0;
                        i < contentLength;
                        i += 10
                ) {
                    assertEquals("content at position " + i, i % 256, contentIs.read());
                    contentIs.skip(9);
                }
                contentIs.close();
                System.out.println("OK");
            }
            //
            // Modify content
            //
            persistenceManager.currentTransaction().begin();
            for(
                    int i = 0;
                    i < contentLength;
                    i++
            ) {
                content[i] = (byte)((short)(i % 137));
            }
            document.setContent(
                org.w3c.cci2.BinaryLargeObjects.valueOf(content)
            );
            persistenceManager.currentTransaction().commit();
            persistenceManager.refresh(document);
            contentLo = document.getContent();
            documentSize = contentLo.getLength();
            if(documentSize != null) {
                assertEquals("document size", contentLength, documentSize.longValue());
            }
            for(int r = 0; r < 2; r++) {
                //
                // test with input stream method
                //
                System.out.println("verifying content (with InputStream)");
                InputStream contentIs = contentLo.getContent();
    
                for(
                        int i = 0;
                        i < contentLength;
                        i += 10
                ) {
                    assertEquals("Run " + r + ": content at position " + i, i % 137, contentIs.read());
                    contentIs.skip(9);
                }
                contentIs.close();
                System.out.println("OK");
            }
            for(int r = 0; r < 2; r++) {
                //
                // test with output stream
                //
                System.out.println("verifying content (with OutputStream)");
                ByteArrayOutputStream contentOs = new ByteArrayOutputStream();
                contentLo.getContent(contentOs, 0);
                contentOs.close();
                InputStream contentIs = new ByteArrayInputStream(contentOs.toByteArray());
                for(
                        int i = 0;
                        i < contentLength;
                        i += 10
                ) {
                    assertEquals("content at position " + i, i % 137, contentIs.read());
                    contentIs.skip(9);
                }
                contentIs.close();
                System.out.println("OK");
            }
        }
        
        public void testInMemoryProvider() throws ServiceException {
            PersistenceManager persistenceManager = this.context.getPersistenceManager();
            PersonClass personClass = getPackage().getPerson();
            Segment segment = getSegment();
            PostalAddress postalAddress = (PostalAddress) segment.getAddress(false, "0001");
            PersonGroup g0 = segment.getPersonGroup(false, "g0");
            PersonGroup g1 = segment.getPersonGroup(false, "g1");
            PersonGroup g2 = segment.getPersonGroup(false, "g2");

            // Create persons
            for(
                    int i = 0;
                    i <= LARGE_N_PERSONS;
                    i++
            ) {
                if(i % 100 == 0) {
                    System.out.println(i + " persons created. Free memory " + Runtime.getRuntime().freeMemory());
                }
                persistenceManager.currentTransaction().begin();
                Person person = personClass.createPerson();
                person.setForeignId("F" + i);
                person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1960-01-01"));
                person.setBirthdateAsDateTime(new Date());
                person.setLastName("Muster" + i);
                person.setSalutation("Herr");
                person.setSex((short)0);
                person.getGivenName().add("Hans");
                person.getGivenName().add("Heiri");
                person.setGivenName(new String[]{"Hans", "Heiri"});
                person.getAssignedAddress().add(postalAddress);
                person.getPersonGroup().add(g0);
                person.getPersonGroup().add(g1);
                person.getPersonGroup().add(g2);
                segment.addPerson(false,"L000" + i, person);
                persistenceManager.currentTransaction().commit();
            }

            // Retrieve persons
            int ii = 0;
            int limit = 1000000;
            Runtime runtime = Runtime.getRuntime();
            long initialMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
            System.out.println("initial memory usage " + initialMemoryUsage);
            org.openmdx.test.app1.cci2.SegmentHasPerson.Person<Person> allPeople = segment.getPerson();
            for(Person pers : allPeople) {
                if(ii++ % 100 == 0) {
                    long currentMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
                    System.out.println(ii + " persons retrieved. Current memory usage " + currentMemoryUsage);
                    long additionalMemoryUsage = currentMemoryUsage - initialMemoryUsage;
                    if(additionalMemoryUsage > limit) {
                        runtime.gc();
                        currentMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
                        additionalMemoryUsage = currentMemoryUsage - initialMemoryUsage;
                        assertFalse(
                            "Memory used up after " + ii + " failed retrievals: " + additionalMemoryUsage,
                            additionalMemoryUsage > limit
                        );
                    }
                }
                limit += 3500;
                persistenceManager.refresh(pers);
            }
        }
        
        public void testSerialization() throws ServiceException, IOException, ClassNotFoundException {
            Segment segment = getSegment();
            {
                FileOutputStream ostream = new FileOutputStream(this.context.getScratchFile());
                ObjectOutputStream s = new ObjectOutputStream(ostream);
                s.writeObject(segment.getPerson());
                s.flush();
                ostream.close();
            }
            {
                FileInputStream istream = new FileInputStream(this.context.getScratchFile());
                ObjectInputStream p = new ObjectInputStream(istream);
                Collection<?> persons = (Collection<?>)p.readObject();
                int j = 0;
                for(Object q : persons) {
                    System.out.println("person[" + j + "]=" + q);
                }
                istream.close();
            }
        }
        
        public void removeSegment() throws ServiceException{
            Transaction transaction = this.context.getPersistenceManager().currentTransaction();
            transaction.begin();
            getSegment().refDelete();
            transaction.commit();
        }

        protected Segment getSegment(
        ) throws ServiceException{
            return (Segment) this.context.getPersistenceManager().getObjectById(
                Segment.class,
                App1Package.AUTHORITY_XRI + "/provider/" + PROVIDER_NAME + "/segment/" + SEGMENT_NAME
            );
        }
        
        protected App1Package getPackage() throws ServiceException{
            return (App1Package) (
                    (RefObject)this.context.getPersistenceManager().newInstance(Segment.class)
            ).refImmediatePackage();
        }

   }

    /**
     * Object id accessor
     * 
     * @param refObject
     * 
     * @return the object id as path
     */
    static Path refGetPath(
        RefObject refObject
    ){
        Object objectId = JDOHelper.getObjectId(refObject);
        return
        objectId instanceof Path ? (Path) objectId :
            objectId == null ? null :
                new Path(objectId.toString());
    }

    
    //------------------------------------------------------------------------
    // Class TestContext
    //------------------------------------------------------------------------    

    /**
     * Test Context
     */
    protected static class TestContext {

        /**
         * Constructor 
         *
         * @param name
         */
        public TestContext(
            String name
         ) {
            this.name = name;
        }
        
        /**
         * 
         */
        private final String name;
        
        /**
         * 
         */
        private File scratchFile;
        
        /**
         * 
         */
        private Dataprovider_1_1Connection dataproviderConnection;
        
        /**
         * 
         */
        private PersistenceManager persistenceManager;
        
        /**
         * Retrieve scratchFile.
         *
         * @return Returns the scratchFile.
         * @throws IOException 
         */
        protected File getScratchFile() throws IOException {
            if(this.scratchFile == null) {
                this.scratchFile = File.createTempFile(this.name, null);
            }
            System.out.println ("Scratch File = " + scratchFile);
            return this.scratchFile;
        }
        
        protected void close(){
            if(this.persistenceManager != null) {
                this.persistenceManager.close();
                this.persistenceManager = null;
            }
            if(this.dataproviderConnection != null) {
                this.dataproviderConnection.close();
                this.dataproviderConnection = null;
            }
        }
        
        public Dataprovider_1_1Connection getDataproviderConnection(
        ) throws ServiceException {
            if(this.dataproviderConnection == null) {
                this.dataproviderConnection = accessorFactory.createConnection();
            }
            return this.dataproviderConnection;
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.ManagerFactory_2_0#createManager()
         */
        public PersistenceManager getPersistenceManager(
        ) throws ServiceException {
            if(this.persistenceManager == null) try {
                this.persistenceManager = accessorFactory.getEntityManager(
                    Collections.singletonList(System.getProperty("user.name"))
                );
            } catch (ResourceException exception) {
                throw new ServiceException(exception);
            }
            return this.persistenceManager;
        }
        
    }
    
    
    static private int N_PERSONS = 100;
    static private int LARGE_N_PERSONS = 1000;
    static private int TEST_PERSON_COUNT = N_PERSONS - 1; // TODO one is missing for some reason 
    
    protected static final String PROVIDER_NAME = "JmiJdbc";

    protected static final String SEGMENT_NAME = "Standard";
    
    //---------------------------------------------------------------------------
    // Deployment Configuration    
    //---------------------------------------------------------------------------    
    
    private static final String[] CONNECTOR_URL = {
//      "file:../test-core/src/connector/openmdx-2/postgresql-7.rar"
//      "file:../test-core/src/connector/openmdx-2/oracle-10g.rar"
        "file:../test-core/src/connector/openmdx-2/sql-server-2005.rar"
    };
    private static final String[] APPLICATION_URL = {
        "file:../test-core/src/ear/test-app1.ear"
    };
    private static final String[] MODELS = {
        "org:openmdx:test:app1",
        "test:openmdx:state2"
    };
    private static final String GATEWAY_JNDI_NAME = 
        "test/openmdx/test/app1/Gateway";
    private static final String ENTITY_MANAGER_FACTORY_JNDI_NAME = 
        "test/openmdx/test/app1/EntityProviderFactory";
    protected static final Deployment_1 accessorFactory = new Deployment_1(
        "xri://ejb.openmdx.org*ENTERPRISE_APPLICATION_CONTAINER",
//      "xri://openejb.apache.org*ENTERPRISE_APPLICATION_CONTAINER",
        CONNECTOR_URL,
        APPLICATION_URL,
        false, // log deployment detail
        ENTITY_MANAGER_FACTORY_JNDI_NAME,
        GATEWAY_JNDI_NAME,
        MODELS
    );
    
}

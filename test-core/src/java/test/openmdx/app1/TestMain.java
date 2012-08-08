/*
 * ====================================================================
 * Name:        $Id: TestMain.java,v 1.102 2012/01/05 16:31:46 hburger Exp $
 * Description: Unit test for model app1
 * Revision:    $Revision: 1.102 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2012/01/05 16:31:46 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
package test.openmdx.app1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.jdo.Constants;
import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOOptimisticVerificationException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.InvalidCallException;
import javax.jmi.reflect.InvalidObjectException;
import javax.jmi.reflect.RefBaseObject;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.resource.cci.ConnectionFactory;
import javax.resource.spi.CommException;
import javax.servlet.ServletException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.application.rest.spi.EntityManagerProxyFactory_2;
import org.openmdx.application.rest.spi.InboundConnectionFactory_2;
import org.openmdx.application.xml.Exporter;
import org.openmdx.application.xml.Importer;
import org.openmdx.audit2.cci.AuditQueries;
import org.openmdx.audit2.jmi1.Audit2Package;
import org.openmdx.audit2.jmi1.Involvement;
import org.openmdx.audit2.jmi1.UnitOfWork;
import org.openmdx.audit2.spi.Configuration;
import org.openmdx.audit2.spi.InvolvementPersistence;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.accessor.jmi.spi.DelegatingRefObject_1_0;
import org.openmdx.base.accessor.jmi.spi.EntityManagerFactory_1;
import org.openmdx.base.accessor.jmi.spi.RefMetaObject_1;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.accessor.rest.spi.Synchronization_2_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.aop0.UpdateAvoidance;
import org.openmdx.base.cci2.ExtentCapable;
import org.openmdx.base.collection.Sets;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Modifiable;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.base.persistence.spi.Queries;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.rest.spi.ConnectionFactoryAdapter;
import org.openmdx.base.transaction.TransactionAttributeType;
import org.openmdx.generic1.cci2.PropertyQuery;
import org.openmdx.generic1.cci2.PropertySetHasProperties;
import org.openmdx.generic1.cci2.UriPropertyQuery;
import org.openmdx.generic1.jmi1.BooleanProperty;
import org.openmdx.generic1.jmi1.DecimalProperty;
import org.openmdx.generic1.jmi1.Generic1Package;
import org.openmdx.generic1.jmi1.IntegerProperty;
import org.openmdx.generic1.jmi1.Property;
import org.openmdx.generic1.jmi1.StringProperty;
import org.openmdx.generic1.jmi1.UriProperty;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactoryBuilder;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.naming.ComponentEnvironment;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.Container;
import org.w3c.cci2.SparseArray;
import org.w3c.cci2.StringTypePredicate;
import org.w3c.format.DateTimeFormat;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi.StateAccessor;
import org.w3c.spi2.Datatypes;

import test.openmdx.app1.aop2.NaturalPerson;
import test.openmdx.app1.cci2.AddressQuery;
import test.openmdx.app1.cci2.CycleMember1Query;
import test.openmdx.app1.cci2.InvoiceHasInvoicePosition;
import test.openmdx.app1.cci2.InvoicePositionQuery;
import test.openmdx.app1.cci2.InvoiceQuery;
import test.openmdx.app1.cci2.PersonQuery;
import test.openmdx.app1.cci2.ProductQuery;
import test.openmdx.app1.cci2.SegmentHasAddress;
import test.openmdx.app1.cci2.SegmentHasPerson;
import test.openmdx.app1.jmi1.Address;
import test.openmdx.app1.jmi1.AddressFormat;
import test.openmdx.app1.jmi1.AddressFormatAsParams;
import test.openmdx.app1.jmi1.AddressFormatAsResult;
import test.openmdx.app1.jmi1.App1Package;
import test.openmdx.app1.jmi1.CanNotFormatNameException;
import test.openmdx.app1.jmi1.CycleMember1;
import test.openmdx.app1.jmi1.CycleMember1Class;
import test.openmdx.app1.jmi1.CycleMember2;
import test.openmdx.app1.jmi1.CycleMember2Class;
import test.openmdx.app1.jmi1.Document;
import test.openmdx.app1.jmi1.DocumentClass;
import test.openmdx.app1.jmi1.EmailAddress;
import test.openmdx.app1.jmi1.EmailAddressClass;
import test.openmdx.app1.jmi1.EmailAddressSendMessageParams;
import test.openmdx.app1.jmi1.EmailAddressSendMessageTemplateParams;
import test.openmdx.app1.jmi1.EmailAddressSendMessageTemplateResult;
import test.openmdx.app1.jmi1.InternationalPostalAddress;
import test.openmdx.app1.jmi1.InternationalPostalAddressClass;
import test.openmdx.app1.jmi1.Invoice;
import test.openmdx.app1.jmi1.InvoiceClass;
import test.openmdx.app1.jmi1.InvoicePosition;
import test.openmdx.app1.jmi1.InvoicePositionClass;
import test.openmdx.app1.jmi1.MessageTemplate;
import test.openmdx.app1.jmi1.MessageTemplateClass;
import test.openmdx.app1.jmi1.NameFormat;
import test.openmdx.app1.jmi1.Person;
import test.openmdx.app1.jmi1.PersonAssignAddressParams;
import test.openmdx.app1.jmi1.PersonClass;
import test.openmdx.app1.jmi1.PersonDateOpParams;
import test.openmdx.app1.jmi1.PersonDateOpResult;
import test.openmdx.app1.jmi1.PersonFormatNameAsParams;
import test.openmdx.app1.jmi1.PersonFormatNameAsResult;
import test.openmdx.app1.jmi1.PersonGroup;
import test.openmdx.app1.jmi1.PersonGroupClass;
import test.openmdx.app1.jmi1.PostalAddress;
import test.openmdx.app1.jmi1.PostalAddressSendMessageParams;
import test.openmdx.app1.jmi1.Product;
import test.openmdx.application.rest.http.ServletPort;

/**
 * Test Main
 */
@RunWith(Suite.class)
@SuiteClasses(
    {
        TestMain.LocalConnectionTest.class,
        TestMain.ProxyConnectionTest.class,
        TestMain.OptimisticContainerManagedTransactionTest.class,
        TestMain.PessimisticContainerManagedTransactionTest.class
    }
)
public class TestMain {

    protected static final boolean DUMP = false;
    protected static final String AUDIT_PROVIDER_NAME = "Audit";
    protected static final String DATA_PROVIDER_NAME = "Data";
    protected static final String SEGMENT_NAME = "Standard";

    protected static final Path DATA_SEGMENT_ID = new Path(
        "xri://@openmdx*test.openmdx.app1/provider"
    ).getDescendant(
        DATA_PROVIDER_NAME,
        "segment",
        SEGMENT_NAME
    ).lock();
    
    @BeforeClass
    public static void initialize(
    ) throws NamingException{
        if(!NamingManager.hasInitialContextFactoryBuilder()) {
            NonManagedInitialContextFactoryBuilder.install(null);
        }
    }


    //------------------------------------------------------------------------
    // Class AbstractTest
    //------------------------------------------------------------------------

    /**
     * Abstract Test
     */
    public abstract static class AbstractTest {

        /**
         * Constructor 
         */
        protected AbstractTest(
        ){
            this.taskIdentifier = new Object(){

                /* (non-Javadoc)
                 * @see java.lang.Object#toString()
                 */
                @Override
                public String toString() {
                    return AbstractTest.this.taskId;
                }

            };
        }

        protected PersistenceManager entityManager;
        protected Authority authority;
        protected Provider provider;
        protected String taskId;
        private final Object taskIdentifier;
        private final Date start = new Date();
        protected PersistenceManagerFactory entityManagerFactory;

        @Before
        public  void setUp(){
            entityManagerFactory = newEntityManagerFactory();
            this.entityManager = entityManagerFactory.getPersistenceManager();
            this.authority = this.entityManager.getObjectById(
                Authority.class,
                App1Package.AUTHORITY_XRI
            );
            this.provider = authority.getProvider(
                false,
                DATA_PROVIDER_NAME
            );
            UserObjects.setTaskIdentifier(
                this.entityManager, 
                this.taskIdentifier
            );
        }

        @After
        public  void tearDown(){
            this.entityManager.close();
            this.entityManager = null;
        }
        
        protected PersistenceManagerFactory newEntityManagerFactory(){
            PersistenceManagerFactory entityManagerFactory = JDOHelper.getPersistenceManagerFactory(
                configuration(),
                "test-Main-EntityManagerFactory"
            );
            entityManagerFactory.getDataStoreCache().pinAll(true, AddressFormat.class);
            return entityManagerFactory;
        }
        
        /**
         * Provide the configuration to be amendet by subclasses
         * 
         * @return the configuration used for overriding
         */
        protected Map<String, Object> configuration(){
            return new HashMap<String, Object>();
        }

        protected void begin(){
            this.entityManager.currentTransaction().begin();
        }

        protected void commit(){
            this.entityManager.currentTransaction().commit();
        }

        protected void rollback(){
            this.entityManager.currentTransaction().rollback();
        }

        protected Date getStart(){
            return this.start;
        }
        
    }


    //------------------------------------------------------------------------
    // Class RepeatableTest
    //------------------------------------------------------------------------

    /**
     * Abstract Tests
     */
    protected abstract static class RepeatableTest extends AbstractTest {

        /**
         * Constructor 
         *
         */
        protected RepeatableTest() {
            this.structureCreation = structureCreationSeed;
            structureCreationSeed = next(structureCreationSeed);
        }

        /**
         * The initial value changes from instantiation to instantiation
         */
        static private StructureCreation structureCreationSeed = StructureCreation.values()[0]; 

        /**
         * The initial value changes from query to query
         */
        private StructureCreation structureCreation; 
        
        static private final boolean RID_AND_OID_ARE_SEPARATED = true;
        static private final int INSPECTION_COUNT = 250;
        static private final int MEMBER_COUNT = 9;
        static private final int N_PERSONS = 100;
        static private final int LARGE_N_PERSONS = 1000;
        static private final int TEST_PERSON_COUNT = N_PERSONS - 1; // TODO one is missing for some reason
        static private final int SIMILAR_NAME_COUNT = 3; 
        private final boolean VALIDATE_PERSISTENCE_MANAGER = !(this instanceof ProxyConnectionTest); // TODO include Proxy Connections
        
        protected long id;

        /**
         * Switch back and forth to test both variants
         * 
         * @return <code>true</code> if the package should be used to
         * acquire structures.
         */
        protected StructureCreation nextStructureCreation(){
            return this.structureCreation = next(this.structureCreation);
        }

        /**
         * Round robin
         * 
         * @param value the current structure creation kind
         * 
         * @return the next structure creation kind
         */
        private static StructureCreation next(StructureCreation value){
            return StructureCreation.values()[
                (value.ordinal() + 1) % StructureCreation.values().length                                   
            ];
        }
        
        protected String nextId(){
            return "ID" + this.id++;
        }

        @SuppressWarnings("unchecked")
		protected void testCR20019014(){
            PropertyQuery query = (PropertyQuery) entityManager.newQuery(
                Property.class
            );
            PersistenceHelper.setClasses(
                query,
                IntegerProperty.class, DecimalProperty.class
            );
        }
        
        protected void testCR20018726(
        ) throws ServiceException{
            try {
                super.taskId = "CR20018726";
                test.openmdx.app1.jmi1.Segment segment = (test.openmdx.app1.jmi1.Segment) provider.getSegment(SEGMENT_NAME);
                List<Address> addresses = segment.getAddress((AddressQuery)null);
                for(Address address : addresses) {
                    System.out.println("1st display of " + JDOHelper.getObjectId(address));
                }
                for(Address address : addresses) {
                    System.out.println("2nd display of " + JDOHelper.getObjectId(address));
                }
            } finally {
                super.taskId = null;
            }
        }

        protected void testCR20019917(){
            try {
                super.taskId = "CR20019917";
                Object userObject = new Object();
                this.entityManager.setUserObject(userObject);
                assertSame("initial UserObject", userObject, this.entityManager.getUserObject());
                this.entityManager.setUserObject(BigDecimal.ONE);
                assertSame("intermediate UserObject", BigDecimal.ONE, this.entityManager.getUserObject());
                this.entityManager.setUserObject(null);
                assertNull("final UserObject", this.entityManager.getUserObject());
            } finally {
                super.taskId = null;
            }
        }
        
        @SuppressWarnings("deprecation")
        protected void testCR20019462(
        ) throws ServiceException{
            try {
                super.taskId = "CR20019462";
                Invoice invoice = this.entityManager.newInstance(Invoice.class);
                InvoicePosition position = this.entityManager.newInstance(InvoicePosition.class);
                Container<InvoicePosition> positions = invoice.getInvoicePosition();
                assertTrue(positions.isEmpty());
                invoice.addInvoicePosition(position);
                assertEquals(1, positions.size());
                positions.remove(position);
                assertTrue(positions.isEmpty());
            } finally {
                super.taskId = null;
            }
        }

        protected void testCR20018800(
        ){
            try {
                super.taskId = "CR20018800";
                PersistenceManager original = this.entityManager;
                List<String> originalPrinicpals = UserObjects.getPrincipalChain(original); 
                PersistenceManager sibling = entityManagerFactory.getPersistenceManager();
                List<String> siblingPrinicpals = UserObjects.getPrincipalChain(sibling); 
                assertEquals("sibling", originalPrinicpals, siblingPrinicpals);
                PersistenceManagerFactory factory = original.getPersistenceManagerFactory();
                PersistenceManager clone = factory.getPersistenceManager();
                List<String> clonePrinicpals = UserObjects.getPrincipalChain(clone); 
                assertEquals("clone", originalPrinicpals, clonePrinicpals);
            } finally {
                super.taskId = null;
            }
        }

        protected void testPackageAcquisition(
        ){
            UnitOfWork instance = this.entityManager.newInstance(UnitOfWork.class);
            Audit2Package audit2Package = (Audit2Package)instance.refImmediatePackage();
            assertEquals("MOF ID", "org:openmdx:audit2:audit2", audit2Package.refMofId());
        }

        protected void resetAuditSegment(
        ){
            Authority authority = this.entityManager.getObjectById(
                Authority.class,
                Audit2Package.AUTHORITY_XRI
            );
            Provider provider = authority.getProvider(
                false,
                AUDIT_PROVIDER_NAME
            );
            org.openmdx.audit2.jmi1.Segment segment = (org.openmdx.audit2.jmi1.Segment) provider.getSegment(
                false, 
                TestMain.SEGMENT_NAME
            );
            if(segment != null) {
                this.begin();
                segment.refDelete();
                this.commit();
            }
            segment = this.entityManager.newInstance(org.openmdx.audit2.jmi1.Segment.class);
            this.begin();
            provider.addSegment(false, TestMain.SEGMENT_NAME, segment);
            this.commit();
        }

        protected void resetDataSegment(
        ){
            test.openmdx.app1.jmi1.Segment segment = (test.openmdx.app1.jmi1.Segment) provider.getSegment(
                false, 
                SEGMENT_NAME
            );
            if(segment != null) {
                this.begin();
                segment.refDelete();
                this.commit();
            }
            segment = this.entityManager.newInstance(test.openmdx.app1.jmi1.Segment.class);
            this.begin();
            provider.addSegment(false, SEGMENT_NAME, segment);
            this.commit();
        }

        @SuppressWarnings("deprecation")
        protected void testMain(
        ) throws ServiceException, CanNotFormatNameException, IOException, ParseException, ClassNotFoundException{
            this.id = 500000l;
            System.out.println("getting root package...");
            //          Authority app1 = (Authority) persistenceManager.getObjectById(new Path(App1Package.AUTHORITY_XRI));
            Authority app1 = super.authority;
            PersistenceManager persistenceManager = super.entityManager;
            RefPackage rootPkg = app1.refOutermostPackage();
            App1Package app1Package = (App1Package) app1.refOutermostPackage().refPackage(app1.refGetPath().getBase());

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
            Provider provider = app1.getProvider(false, DATA_PROVIDER_NAME);
            test.openmdx.app1.jmi1.Segment segment = (test.openmdx.app1.jmi1.Segment) provider.getSegment(SEGMENT_NAME);
            long startedAt = 0;

            //
            // CR20018821
            //
            try {
                super.taskId = "CR20018821";
                @SuppressWarnings("unused")
                RefContainer<?> segments = (RefContainer<?>) persistenceManager.getObjectById(
                    provider.refGetPath().getChild("segment")
                );
            } finally {
                super.taskId = null;
            }
            //
            // CR20018821
            //
            try {
                super.taskId = "CR20018821";
                this.begin();
                Importer.importObjects(
                    Importer.asTarget(persistenceManager),
                    Importer.asSource(
                        new URL("xri://+resource/test/openmdx/app1/data.xml")
                    )
                );
                this.commit();
            } finally {
                super.taskId = null;
            }

            //
            // CR20019971
            //
            try {
                super.taskId = "CR20019971";
            	InvoicePosition position = this.entityManager.newInstance(InvoicePosition.class);
            	assertNull("Not yet contained", position.getInvoice());                         
            	Invoice invoice = this.entityManager.newInstance(Invoice.class);
            	invoice.addInvoicePosition(position);
            	assertSame("Transient but contained", invoice, position.getInvoice());
            } finally {
            	super.taskId = null;
            }
            //
            // CR20019996
            //
            BooleanProperty booleanProperty;
            try {
                super.taskId = "CR20019996";
                booleanProperty = generic1Package.getBooleanProperty().createBooleanProperty();
                booleanProperty.refSetValue("org:openmdx:generic1:Property:description","A SparseArray Of Flags");
                booleanProperty.getBooleanValue().put(0, Boolean.TRUE);
            } finally {
                super.taskId = null;
            }
            //
            // Test Invoice
            //
            Invoice invoice;
            try {
                super.taskId = "CR0003551";
                InvoiceClass invoiceClass = app1Package.getInvoice();
                InvoicePositionClass invoicePositionClass = app1Package.getInvoicePosition();
                invoice = invoiceClass.createInvoice();        
                invoice.setDescription("this is an invoice for PG0");
                invoice.setProductGroupId("PG0");
                assertNull("CR0003551", JDOHelper.getObjectId(invoice));
                @SuppressWarnings("unchecked")
				RefContainer<Invoice> refInvoices = (RefContainer<Invoice>) segment.<Invoice>getInvoice();
                assertNull("Not yet contained", PersistenceHelper.getLastXRISegment(booleanProperty));
                invoice.addProperty(false, "flag", booleanProperty);
                assertFalse(JDOHelper.isPersistent(booleanProperty));
                assertEquals("Transient object has alread XRI qualifier", "flag", PersistenceHelper.getLastXRISegment(booleanProperty));
                this.begin();
                refInvoices.refAdd(RefContainer.REASSIGNABLE, nextId(), invoice);
                assertNotNull("CR0003551", JDOHelper.getObjectId(invoice));
                for(int i = 0; i < 10; i++) {
                    InvoicePosition invoicePosition = invoicePositionClass.createInvoicePosition();
                    invoicePosition.setDescription("this is an invoice position for P" + i);
                    invoicePosition.setProductId("P" + i);
                    assertNull("CR0003551", JDOHelper.getObjectId(invoicePosition));
                    invoice.addInvoicePosition(false, nextId(), invoicePosition);
                    assertNotNull("CR0003551", JDOHelper.getObjectId(invoicePosition));
                }
                this.commit();
                assertTrue(JDOHelper.isPersistent(booleanProperty));
                assertEquals("Persistent object has alread XRI qualifier", "flag", PersistenceHelper.getLastXRISegment(booleanProperty));
            } finally {
                super.taskId = null;
            }
            //
            // CR20019962
            //
            try {
                super.taskId = "CR20019962";
                invoice.getInvoicePosition((InvoicePositionQuery)null);
                try {
                    invoice.getInvoicePosition((String)null);
                    fail("null is not allowed as qualifier");
                } catch (JmiServiceException exception) {
                	assertEquals("Invalid Argument", BasicException.Code.DEFAULT_DOMAIN, exception.getExceptionDomain());
                	assertEquals("Invalid Argument", BasicException.Code.BAD_PARAMETER, exception.getExceptionCode());
                }            
                try {
                    invoice.getInvoicePosition(false,(String)null);
                    fail("null is not allowed as qualifier");
                } catch (JmiServiceException exception) {
                	assertEquals("Invalid Argument", BasicException.Code.DEFAULT_DOMAIN, exception.getExceptionDomain());
                	assertEquals("Invalid Argument", BasicException.Code.BAD_PARAMETER, exception.getExceptionCode());
                }            
            } finally {
                super.taskId = null;
            }            
            //
            // CR20019959
            //
            try {
                super.taskId = "CR20019959";
                {
                    
                    ProductQuery productQuery = (ProductQuery) entityManager.newQuery(Product.class);
                    productQuery.createdAt().lessThanOrEqualTo(new Date());
                    InvoicePositionQuery invoicePositionQuery = (InvoicePositionQuery) entityManager.newQuery(InvoicePosition.class);
                    invoicePositionQuery.product().elementOf(PersistenceHelper.asSubquery(productQuery));            
                    @SuppressWarnings("unused") // We must not execute this query as the product reference is derived!
					List<InvoicePosition> invoicePositions = invoice.getInvoicePosition(invoicePositionQuery);
                }
                {   
                    InvoicePositionQuery invoicePositionQuery = (InvoicePositionQuery) entityManager.newQuery(InvoicePosition.class);
                    invoicePositionQuery.productId().equalTo("P6");
                    InvoiceQuery invoiceQuery = (InvoiceQuery) entityManager.newQuery(Invoice.class);
                    invoiceQuery.thereExistsInvoicePosition().elementOf(PersistenceHelper.asSubquery(invoicePositionQuery));
                    List<Invoice> invoices = segment.getInvoice(invoiceQuery);
                    if (!RID_AND_OID_ARE_SEPARATED) {
                        assertFalse("There exist invoices including product P6", invoices.isEmpty());
                    }
                    
                }
            } finally {
                super.taskId = null;
            }
            //
            // CR0000823
            //
            try {
            	super.taskId = "CR0000823";
                Person person = persistenceManager.newInstance(Person.class);
                person.setGivenName("Klaus", "Maria");
                //
                // Before the unit of work's begin
                //
                assertEquals("Second part of first name", "Maria", person.getGivenName().get(1));
                assertNull("LastName", person.getLastName());
                if(persistenceManager.getPersistenceManagerFactory().getProperties().contains(Constants.OPTION_TRANSACTIONAL_TRANSIENT)) {
                    this.begin();
                    persistenceManager.makeTransactional(person);
                    person.setLastName("Brandauer");
                    //
                    // In the unit of work
                    //
                    assertEquals("Second part of first name", "Maria", person.getGivenName().get(1));
                    assertEquals("LastName", "Brandauer", person.getLastName());
                    this.rollback();
                    //
                    // After the unit of work's roll-back
                    //
                    assertEquals("Second part of first name", "Maria", person.getGivenName().get(1));
                    assertNull("LastName", person.getLastName());
                } else {
                    try {
                        this.begin();
                        persistenceManager.makeTransactional(person);
                        fail(Constants.OPTION_TRANSACTIONAL_TRANSIENT + " is not supported");
                    } catch (JDOUnsupportedOptionException expected) {
                        person.setLastName("Brandauer");
                        //
                        // In the unit of work
                        //
                        assertEquals("Second part of first name", "Maria", person.getGivenName().get(1));
                        assertEquals("LastName", "Brandauer", person.getLastName());
                        this.rollback();
                        //
                        // After the unit of work's roll-back
                        //
                        assertEquals("Second part of first name", "Maria", person.getGivenName().get(1));
                        assertEquals("LastName", "Brandauer", person.getLastName());
                    }
                }
            } finally {
            	super.taskId = null;
            }
            
            //
            // CR20019835
            //
            try {
                super.taskId = "CR20019835";
                this.begin();
                Invoice i4711 = this.entityManager.newInstance(Invoice.class);
                i4711.setDescription("K\u00F6lnische W\u00E4sser");
                i4711.setInternationalProductGroupIdentification("K-4711");
                segment.addInvoice("CR20019862", i4711);
                this.commit();
                fail("Missing mandatory feature ProductGroupId");
            } catch (JDOFatalDataStoreException exception) {
                assertEquals(
                    "Missing mandatory feature ProductGroupId", 
                    BasicException.Code.VALIDATION_FAILURE, 
                    Throwables.getCause(exception, null).getExceptionCode()
                );
            } finally {
                super.taskId = null;
            }
            //
            // CR20019862
            //
            try {
                super.taskId = "CR20019862";
                {
                    this.begin();
                    Invoice i4711 = this.entityManager.newInstance(Invoice.class);
                    i4711.setDescription("K\u00F6lnische W\u00E4sser");
                    i4711.setInternationalProductGroupIdentification("K-4711");
                    i4711.setProductGroupId("4711");
                    segment.addInvoice("CR20019862", i4711);
                    this.commit();
                }
                {
                    InvoiceQuery query = (InvoiceQuery) this.entityManager.newQuery(Invoice.class);
                    query.thereExistsInternationalProductGroupIdentification().like("K-.*");
                    List<Invoice> groups = segment.getInvoice(query);
                    assertEquals("K\u00F6lnisch Wasser", 1, groups.size());
                }
            } finally {
                super.taskId = null;
            }
            //
            // CR20019372
            //
            try {
                this.taskId = "CR20019372";
                this.begin();
                Invoice auditInvoice = this.entityManager.newInstance(Invoice.class);
                auditInvoice.setDescription("An invoice for audit tests");
                auditInvoice.setProductGroupId("PG0");
                StringProperty stringProperty = this.entityManager.newInstance(StringProperty.class);
                stringProperty.setDescription("Non-application id");
                stringProperty.getStringValue().put(1, "CR20019479");
                auditInvoice.getProperty().add(stringProperty);
                segment.addInvoice("CR20019372", auditInvoice);
                UriProperty uriProperty = this.entityManager.newInstance(UriProperty.class);
                uriProperty.setDescription("Non-application id");
                uriProperty.getUriValue().put(2, URI.create("xri://+ChangeRequest/20019533"));
                auditInvoice.getProperty().add(uriProperty);
                for(int i = 0; i < 5; i++){
                    InvoicePosition auditPosition = this.entityManager.newInstance(InvoicePosition.class);
                    auditPosition.setDescription("An invoice position for audit tests");
                    auditPosition.setProductId("P" + i);
                    auditInvoice.addInvoicePosition("IP" + i, auditPosition);
                }
                this.commit();
                this.begin();
                auditInvoice.setDescription("Invoice CR20019372");
                for(int i = 0; i < 5; i+=2){
                    InvoicePosition auditPosition = auditInvoice.getInvoicePosition("IP" + i);
                    auditPosition.setDescription("P" + i + ", an invoice position for audit tests");
                }
                this.commit();
            } finally {
                super.taskId = null;
            }
            Invoice propertyHolder = segment.getInvoice("CR20019372");
            Path propertyId = null;
            //
            // CR20019629
            //
            try {
                this.taskId = "CR20019629";
                Collection<Property> properties = propertyHolder.<Property>getProperty();
            	this.begin();
                assertEquals(2, properties.size());
                UriProperty uriProperty = this.entityManager.newInstance(UriProperty.class);
                uriProperty.getUriValue().put(0, URI.create("xri://+ChangeRequest/20019629"));
                propertyHolder.addProperty(this.taskId, uriProperty);
                assertEquals(3, properties.size());
                for(Property property : new ArrayList<Property>(properties)) {
                    if(property instanceof StringProperty) {
                        propertyId = property.refGetPath();
                    }
                    property.refDelete();
                }
                for(Property property : properties) {
                    fail("There should be no properties left: " + property);
                }
            } finally {
                this.rollback();
                super.taskId = null;
            }
            //
            // CR20019858
            //
            try {
                super.taskId = "CR20019858";
                final String propertyValue = "CR20019479";
                final Integer propertyIndex = Integer.valueOf(1);
                PropertySetHasProperties.Property<StringProperty> properties = propertyHolder.<StringProperty>getProperty();
                StringProperty property = properties.get(QualifierType.REASSIGNABLE, propertyId.getBase());
                assertEquals("Property value", propertyValue, property.getStringValue().get(propertyIndex));
                this.begin();
                property.getStringValue().put(propertyIndex, propertyValue);
                this.commit();
            } finally {
                super.taskId = null;
            }
            //
            // CR20019915
            //
            try {
                super.taskId = "CR20019915";
                PropertySetHasProperties.Property<Property> container = propertyHolder.<Property>getProperty();
                List<Property> list = propertyHolder.<Property>getProperty((PropertyQuery)null);
                this.begin();
                //
                // After begin
                //
                assertEquals("List size after begin", 2, list.size());
                assertEquals("Container size after begin", 2, container.size());
                //
                // Add new property
                //
                IntegerProperty newProperty = this.entityManager.newInstance(IntegerProperty.class);
                newProperty.getIntegerValue().put(0, Integer.valueOf(20019915));
                propertyHolder.addProperty(this.taskId, newProperty);
                //
                // After insert
                //
                System.out.println("CR20019915: List");
                assertEquals("List size after insert", 3, list.size());
                for(Property p : list) {
                    System.out.println(p.refClass().refMofId() + ": " + p.refMofId());
                }
                System.out.println("CR20019915: Container");
                assertEquals("Container size after insert", 3, container.size());
                for(Property p : container) {
                    System.out.println(p.refClass().refMofId() + ": " + p.refMofId());
                }
                //
                // Before roll back
                //
                assertEquals("List size before roll back", 3, list.size());
                assertEquals("Container size before roll back", 3, container.size());
                this.rollback();
            } finally {
                super.taskId = null;
            }
            
            // CR20019816
            //
            if(VALIDATE_PERSISTENCE_MANAGER) try {
                this.taskId = "CR20019816";
            	this.begin();
                PersistenceManager differentManager = newEntityManagerFactory().getPersistenceManager();
				UriProperty uriProperty = differentManager.newInstance(UriProperty.class);
                uriProperty.getUriValue().put(0, URI.create("xri://+ChangeRequest/20019816"));
                try {
	                propertyHolder.addProperty(this.taskId, uriProperty);
	                fail("Data Object Manager Mismatch");
                } catch (RuntimeException exception) {
                	assertEquals(
                		"Data Object Manager Mismatch", 
                		BasicException.Code.BAD_PARAMETER, 
                		BasicException.toExceptionStack(exception).getExceptionCode()
                	);
                }
            } finally {
                this.rollback();
                super.taskId = null;
            }                
            //
            // CR20019533
            //
            try {
                this.taskId = "CR20019533";
                UriPropertyQuery query = (UriPropertyQuery) this.entityManager.newQuery(UriProperty.class);
                query.thereExistsUriValue().equalTo(URI.create("xri://+ChangeRequest/20019533"));
                List<UriProperty> properties = propertyHolder.<UriProperty>getProperty(query);
                assertEquals(1, properties.size());
                assertFalse("Plug-in-provided id", properties.get(0).refGetPath().getLastComponent().isPlaceHolder());
            } finally {
                super.taskId = null;
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
                if(this instanceof LocalConnectionTest) {
                    assertTrue("Implementation detail", flag instanceof DelegatingRefObject_1_0);
                    DelegatingRefObject_1_0 entity = (DelegatingRefObject_1_0) flag;
                    Object dataObject = entity.openmdxjdoGetDataObject();
                    assertTrue(dataObject instanceof PersistenceCapable);
                    assertTrue(dataObject instanceof RefObject_1_0);
                    ObjectView_1_0 objectView = ((RefObject_1_0)dataObject).refDelegate();
                    assertNotNull(objectView);
                    assertNotSame("Made persistent", entity.openmdxjdoGetDelegate(), dataObject);
                }
                if(this instanceof ProxyConnectionTest) {
                    assertFalse("Implementation detail", flag instanceof DelegatingRefObject_1_0);
                }
            }
            {
                //
                // Read via extent
                //
                String xriPattern = segment.refGetPath().getDescendant("invoice",":*","invoicePosition","%").toXRI(); 
                InvoicePositionQuery invoicePositionQuery = (InvoicePositionQuery) PersistenceHelper.newQuery(
                    entityManager.getExtent(InvoicePosition.class),
                    xriPattern
                );
                List<InvoicePosition> invoicePositions = segment.getExtent(invoicePositionQuery);
                assertTrue("Invoice Positions: Second Last", invoicePositions.listIterator(14).hasNext());
                assertFalse("Invoice Positions: Last", invoicePositions.listIterator(15).hasNext());
                assertEquals("Invoice Positions: Size", 15,invoicePositions.size());
            }
            {
                //
                // Read via Query
                //
                Path xriPattern = segment.refGetPath().getDescendant("invoice",":*","invoicePosition","%");
                Query query = PersistenceHelper.newQuery(
                    entityManager.getExtent(InvoicePosition.class),
                    xriPattern
                );
                query.setCandidates(segment.getExtent());                
                @SuppressWarnings("unchecked")
				List<InvoicePosition> invoicePositions = (List<InvoicePosition>) query.execute();
                assertTrue("Invoice Positions: Second Last", invoicePositions.listIterator(14).hasNext());
                assertFalse("Invoice Positions: Last", invoicePositions.listIterator(15).hasNext());
                assertEquals("Invoice Positions: Size", 15,invoicePositions.size());
            }
            {
                InvoicePositionQuery invoicePositionQuery = app1Package.createInvoicePositionQuery();
                // get products without price. price is an expensive derived
                // atttribute. Therefore this iteration should be much faster
                // than the next one
                startedAt = System.currentTimeMillis();
                InvoiceHasInvoicePosition.InvoicePosition<InvoicePosition> allInvoicePositions = invoice.getInvoicePosition();
                Collection<InvoicePosition> someInvoicePositions = invoicePositionQuery == null ?
                    allInvoicePositions :
                        allInvoicePositions.getAll(invoicePositionQuery);
                for(InvoicePosition invoicePosition : someInvoicePositions) {
                    Product product = invoicePosition.getProduct();
                    product.getDescription();
                }
                System.out.println("time for retrieving 10 invoice positions (without price)=" + (System.currentTimeMillis() - startedAt));

                startedAt = System.currentTimeMillis();
                allInvoicePositions  = invoice.getInvoicePosition();
                for(InvoicePosition invoicePosition : allInvoicePositions){
                    Product product = invoicePosition.getProduct();
                    String description = product.getDescription();
                    if(description == null) {
                        description = product.refGetPath().getBase();
                    }
                    System.out.println("Product " + description + " costs " + product.getPrice());
                }
                System.out.println("time for retrieving 10 invoice positions (with price)=" + (System.currentTimeMillis() - startedAt));
            }
            try {
                super.taskId = "CR20019855";
                for(Invoice i : segment.<Invoice>getInvoice()) {
                    for(InvoicePosition p : i.<InvoicePosition>getInvoicePosition()) {
                        assertNotNull("The product id is mandatory", p.getProductId());
                    }
                }
            } finally {
                super.taskId = null;
            }
            //
            // CR20019668 
            // 
            try {
                this.taskId = "CR20019668";
                RefQuery_1_0 query = (RefQuery_1_0) Queries.prepareQuery(
                    persistenceManager.newQuery(Invoice.class), 
                    segment.getInvoice(), 
                    "forAllDescription().unlike(\"./.\");thereExistsInvoicePosition().productId().like(\"P.*\");forAllProperty().name().equalTo(\"FLAG\")"
                );
                List<Condition> conditions = query.refGetFilter().getCondition();
                assertEquals("Conditions", 4, conditions.size());
                assertEquals("Condition 0", SystemAttributes.OBJECT_INSTANCE_OF, conditions.get(0).getFeature());
                assertEquals("Condition 0", Quantifier.THERE_EXISTS, conditions.get(0).getQuantifier());
                assertEquals("Condition 0", ConditionType.IS_IN, conditions.get(0).getType());
                assertEquals("Condition 0", 1, conditions.get(0).getValue().length);
                assertEquals("Condition 0", "test:openmdx:app1:Invoice", conditions.get(0).getValue(0));
                assertEquals("Condition 1", "description", conditions.get(1).getFeature());
                assertEquals("Condition 1", Quantifier.FOR_ALL, conditions.get(1).getQuantifier());
                assertEquals("Condition 1", ConditionType.IS_UNLIKE, conditions.get(1).getType());
                assertEquals("Condition 1", 1, conditions.get(1).getValue().length);
                assertEquals("Condition 1", "./.", conditions.get(1).getValue(0));
                assertEquals("Condition 2", "invoicePosition", conditions.get(2).getFeature());
                assertEquals("Condition 2", Quantifier.THERE_EXISTS, conditions.get(2).getQuantifier());
                assertEquals("Condition 2", ConditionType.IS_IN, conditions.get(2).getType());
                assertEquals("Condition 2", 1, conditions.get(2).getValue().length);
                assertTrue("Condition 2", conditions.get(2).getValue(0) instanceof Filter);
                List<Condition> conditions2 = ((Filter)conditions.get(2).getValue(0)).getCondition();
                assertEquals("Conditions 2", 2, conditions2.size());
                assertEquals("Condition 2.0", SystemAttributes.OBJECT_INSTANCE_OF, conditions2.get(0).getFeature());
                assertEquals("Condition 2.0", Quantifier.THERE_EXISTS, conditions2.get(0).getQuantifier());
                assertEquals("Condition 2.0", ConditionType.IS_IN, conditions2.get(0).getType());
                assertEquals("Condition 2.0", 1, conditions2.get(0).getValue().length);
                assertEquals("Condition 2.0", "test:openmdx:app1:InvoicePosition", conditions2.get(0).getValue(0));
                assertEquals("Condition 2.1", "productId", conditions2.get(1).getFeature());
                assertEquals("Condition 2.1", Quantifier.THERE_EXISTS, conditions2.get(1).getQuantifier());
                assertEquals("Condition 2.1", ConditionType.IS_LIKE, conditions2.get(1).getType());
                assertEquals("Condition 2.1", 1, conditions2.get(1).getValue().length);
                assertEquals("Condition 2.1", "P.*", conditions2.get(1).getValue(0));
                assertEquals("Condition 3", "property", conditions.get(3).getFeature());
                assertEquals("Condition 3", Quantifier.FOR_ALL, conditions.get(3).getQuantifier());
                assertEquals("Condition 3", ConditionType.IS_IN, conditions.get(3).getType());
                assertEquals("Condition 3", 1, conditions.get(3).getValue().length);
                assertTrue("Condition 3", conditions.get(3).getValue(0) instanceof Filter);
                List<Condition> conditions3 = ((Filter)conditions.get(3).getValue(0)).getCondition();
                assertEquals("Conditions 3", 2, conditions3.size());
                assertEquals("Condition 3.0", SystemAttributes.OBJECT_INSTANCE_OF, conditions3.get(0).getFeature());
                assertEquals("Condition 3.0", Quantifier.THERE_EXISTS, conditions3.get(0).getQuantifier());
                assertEquals("Condition 3.0", ConditionType.IS_IN, conditions3.get(0).getType());
                assertEquals("Condition 3.0", 1, conditions3.get(0).getValue().length);
                assertEquals("Condition 3.0", "org:openmdx:generic1:Property", conditions3.get(0).getValue(0));
                assertEquals("Condition 3.1", "name", conditions3.get(1).getFeature());
                assertEquals("Condition 3.1", Quantifier.THERE_EXISTS, conditions3.get(1).getQuantifier());
                assertEquals("Condition 3.1", ConditionType.IS_IN, conditions3.get(1).getType());
                assertEquals("Condition 3.1", 1, conditions3.get(1).getValue().length);
                assertEquals("Condition 3.1", "FLAG", conditions3.get(1).getValue(0));
            } finally {
                super.taskId = null;
            }
            //
            // CR20019771: ModelAware filter 
            // 
            try {
                this.taskId = "CR20019771";
                //
                // Complex query.
                //
                if (!RID_AND_OID_ARE_SEPARATED) {
                    //
                    // Complex queries only work either with DbObjectWithIdAsKey...
                    //
                    InvoiceQuery invoiceQuery = (InvoiceQuery)persistenceManager.newQuery(Invoice.class);
                    invoiceQuery.thereExistsInvoicePosition().productId().like("P.*");
                    List<Invoice> matchingInvoices = segment.getInvoice(invoiceQuery);
                    assertEquals("Complex query", 2, matchingInvoices.size());
                } 
                try {
                    //
                    // ... or on transient objects
                    //
                    this.begin();
                    {
                        //
                        // Containment
                        //
                        final int positionCount = 10;
                        final int propertyCount = 4;
                        Invoice transientInvoice = persistenceManager.newInstance(Invoice.class);
                        for(int i = 0; i < positionCount; i++) {
                            InvoicePosition invoicePosition = persistenceManager.newInstance(InvoicePosition.class);
                            transientInvoice.addInvoicePosition("T" + i, invoicePosition);
                            for(int j = 0; j < propertyCount; j++) {
                                IntegerProperty positionProperty = persistenceManager.newInstance(IntegerProperty.class);
                                String prefix = 
                                    i % 2 == 0 && j % 2 == 0 ? "EVEN" : 
                                    i % 2 == 1 && j % 2 == 1 ? "ODD" :
                                    "MIXED";
                                positionProperty.getIntegerValue().put(j, 1000 * i + j);
                                positionProperty.setDescription(prefix + "[" + i + "," + j + "]");
                                invoicePosition.addProperty("P" + j, positionProperty);
                            }
                            assertEquals("Properties/Position", propertyCount, invoicePosition.getProperty().size());
                        }
                        assertEquals("Positions/Invoice", positionCount, transientInvoice.getInvoicePosition().size());
                        InvoicePositionQuery positionQuery = (InvoicePositionQuery) persistenceManager.newQuery(InvoicePosition.class);
                        positionQuery.thereExistsProperty().thereExistsDescription().like("ODD.*");
                        assertEquals("ODDs", positionCount / 2, transientInvoice.getInvoicePosition(positionQuery).size());
                    }
                    {
                        //
                        // References stored as attributes
                        //
                        final int personCount = 10;
                        final int groupCount = 4;
                        test.openmdx.app1.jmi1.Segment transientSegment = persistenceManager.newInstance(test.openmdx.app1.jmi1.Segment.class);
                        PersonGroup[] groups = new PersonGroup[groupCount];
                        for(int i = 0; i < groupCount; i++) {
                            groups[i] = persistenceManager.newInstance(PersonGroup.class);
                            String name = "G" + i;
                            groups[i].setName(name);
                            transientSegment.addPersonGroup(name, groups[i]);
                        }
                        assertEquals("Groups/Segment", groupCount, transientSegment.getPersonGroup().size());
                        for(int j = 0; j < personCount; j++) {
                            Person person = persistenceManager.newInstance(Person.class);
                            if(j > 0) {
                                PersonGroup group = groups[j % groupCount];
                                person.getPersonGroup().add(group);
                            }
                            transientSegment.addPerson("P" + j, person);
                        }
                        assertEquals("People/Segment", personCount, transientSegment.getPerson().size());
                        {
                            PersonQuery personQuery = (PersonQuery) persistenceManager.newQuery(Person.class);
                            personQuery.thereExistsPersonGroup().name().equalTo("G1");
                            assertEquals("Some G1", 3, transientSegment.getPerson(personQuery).size());
                        }
                        {
                            PersonQuery personQuery = (PersonQuery) persistenceManager.newQuery(Person.class);
                            personQuery.thereExistsPersonGroup().name().equalTo("G3");
                            assertEquals("Some G3", 2, transientSegment.getPerson(personQuery).size());
                        }
                        {
                            PersonQuery personQuery = (PersonQuery) persistenceManager.newQuery(Person.class);
                            personQuery.forAllPersonGroup().name().equalTo("G3");
                            assertEquals("All G3", 3, transientSegment.getPerson(personQuery).size());
                        }
                        {
                            PersonQuery personQuery = (PersonQuery) persistenceManager.newQuery(Person.class);
                            personQuery.forAllPersonGroup().name().like("G.*");
                            assertEquals("All Start With G", personCount, transientSegment.getPerson(personQuery).size());
                        }
                        {
                            PersonQuery personQuery = (PersonQuery) persistenceManager.newQuery(Person.class);
                            personQuery.thereExistsPersonGroup().name().like("G.*");
                            assertEquals("Some Start With G", personCount - 1, transientSegment.getPerson(personQuery).size());
                        }
                    }
                } finally {
                    // Person objects are still incomplete
                    this.rollback();
                }
            } finally {
                super.taskId = null;
            }

            /**
             * Test Address
             */
            // get AddressFormat
            try {
                super.taskId = "CR20019366";
                PersistenceManager segmentManager = JDOHelper.getPersistenceManager(segment).getPersistenceManagerFactory().getPersistenceManager();
                
                Collection<AddressFormat> addressFormats = ((test.openmdx.app1.jmi1.Segment)segmentManager.getObjectById(JDOHelper.getObjectId(segment))).getAddressFormat();
                for(AddressFormat addressFormat : addressFormats) {
                    PersistenceManager formatManager = JDOHelper.getPersistenceManager(addressFormat);
                    assertSame("cci2.getContainer()", segmentManager, formatManager);
                    System.out.println("addressFormat=" + addressFormat);
                }
            } finally {
                super.taskId = null;
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
                this.begin();
                nameFormat.refSetValue(
                    "description",
                    "modified description"
                );
                this.commit();
                fail("all attributes are non changeable --> object can not be updated");
            } catch(JDOFatalDataStoreException e) {
                System.out.println("all attributes are non changeable --> object can not be updated");
            }

            try {
                NameFormat nameFormat = (NameFormat) persistenceManager.newInstance(NameFormat.class);
                this.begin();
                nameFormat.setDescription(
                    "a description"
                );
                segment.getNameFormat().add(nameFormat);
                this.commit();
                fail("constraint isFrozen --> object can not be updated");
            } catch(JDOFatalDataStoreException e) {
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
                this.begin();
                postalAddress = postalAddressClass.createInternationalPostalAddress();
                postalAddress.setCountry("Switzerland");
                postalAddress.setCity("Zurich");
                postalAddress.setHouseNumber("57");
                postalAddress.setPostalCode("8005");
                postalAddress.setStreet("Bahnhofstr.");
                postalAddress.setAddressLine("Familie", "Muster");
                segment.addAddress(false, "0001", postalAddress);
                Object postalAddressId = JDOHelper.getObjectId(postalAddress);
                // create a EmailAddress
                emailAddress = emailAddressClass.createEmailAddress();
                emailAddress.setAddress("hans.muster@app1.ch");
                segment.addAddress(false, "0002", emailAddress);
                int addressCount = segment.getAddress().size(); 
                assertEquals("Transient added address count", 2, addressCount);
                switch(i){
                    case 0: {
                        this.rollback();
                        addressCount = segment.getAddress().size(); 
                        assertEquals("Rolled back address count", 0, addressCount);
                    } break;
                    case 1: {
                        this.commit();
                        assertEquals(
                            "Commited address count",
                            2,
                            segment.getAddress().size()
                        );
                        InternationalPostalAddress retrievedAddress = (InternationalPostalAddress) this.entityManager.getObjectById(postalAddressId);
                        InternationalPostalAddress clonedAddress = PersistenceHelper.clone(retrievedAddress);
                        assertFalse(JDOHelper.isPersistent(clonedAddress));
                        this.entityManager.retrieve(retrievedAddress);
                        this.begin();
                        segment.getAddress().clear();
                        addressCount = segment.getAddress().size(); 
                        assertEquals("Transient cleared address count", 0, addressCount);
                        this.commit();
                        addressCount = segment.getAddress().size(); 
                        assertEquals("Cleared persistent address count", 0, addressCount);
                    } break;
                    case 2: {
                        segment.getAddress().clear();
                        addressCount = segment.getAddress().size(); 
                        assertEquals("Cleared transient address count", 0, addressCount);
                        this.commit();
                        addressCount = segment.getAddress().size(); 
                        assertEquals("Cleared committed address count", 0, addressCount);
                        assertTrue(
                            "Cleared committed address count",
                            segment.getAddress().isEmpty()
                        );
                    } break;
                    case 3: {
                        EmailAddress transientAddress = emailAddressClass.createEmailAddress();
                        transientAddress.setAddress("john.player@games.net");
                        segment.addAddress(false, "0003", transientAddress);
                        addressCount = segment.getAddress().size(); 
                        assertEquals("Transient added address count", 3, addressCount);
                        transientAddress.refDelete(); // segment.getAddress().remove(transientAddress);
                        this.commit();
                        addressCount = segment.getAddress().size(); 
                        assertEquals("Commited address count", 2, addressCount);
                    } break;
                    default:
                        fail("No more instructions");
                }
            }
            Path segmentId = (Path) JDOHelper.getObjectId(segment);
            assertTrue(
                "Identity should be available outside the unit of work",
                Arrays.equals(
                    segmentId.getSuffix(
                        segmentId.size() - 2
                    ),
                    new String[]{"segment",SEGMENT_NAME}
                )
            );

            try {
                this.begin();
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
                this.commit();
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
                this.rollback();
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
            //
            // CR220019366 Missing implementation
            // 
            try {
                super.taskId = "CR220019366";
                Address original = segment.getAddress("0001");
                assertEquals("Address.id()", "0001", original.getId());
                PersistenceManager sibling = entityManagerFactory.getPersistenceManager();
                Path xri = new Path(original.refMofId()); 
                @SuppressWarnings("unchecked")
				RefContainer<Address> container = (RefContainer<Address>) sibling.getObjectById(xri.getParent());
                Address copy = container.refGet(RefContainer.REASSIGNABLE, xri.getBase());
                assertEquals("Address.id()", "0001", copy.getId());
                assertNotSame(original, copy);
                assertEquals(original, copy);
            } finally {
                super.taskId = null;
            }
            //
            // CR20018768 refresh
            // 
            try {
                super.taskId = "CR20018768";
                this.begin();
                for(int i = 3; i >= 0; i--) {
                    emailAddress.setAddress("jean.\u00e9echantillon");
                    assertTrue(JDOHelper.isDirty(emailAddress));
                    assertEquals(
                        "jean.\u00e9echantillon",
                        emailAddress.getAddress()
                    );
                    switch(i){
                        case 3: 
                            this.entityManager.refresh(emailAddress);
                            break;
                        case 2:
                            this.entityManager.refreshAll(segment.getAddress());
                            break;
                        case 1:
                            AddressQuery query = null;
                            this.entityManager.refreshAll(segment.getAddress(query));
                            break;
                        default:
                            this.entityManager.refreshAll();
                    }
                    assertTrue(!JDOHelper.isDirty(emailAddress));
                    assertEquals(
                        "Persistent E-Mail-Address be reset",
                        "hans.muster@app1.ch",
                        emailAddress.getAddress()
                    );
                }
                this.commit();
                for(int i = 3; i >= 0; i--) {
                    switch(i){
                        case 3: 
                            this.entityManager.evict(emailAddress);
                            break;
                        case 2:
                            this.entityManager.evictAll(segment.getAddress());
                            break;
                        case 1:
                            AddressQuery query = null;
                            this.entityManager.evictAll(segment.getAddress(query));
                            break;
                        default:
                            this.entityManager.evictAll();
                    }
                }
                for(int i = 3; i >= 0; i--) {
                    switch(i){
                        case 3: 
                            this.entityManager.retrieve(emailAddress);
                            break;
                        case 2:
                            this.entityManager.retrieveAll(segment.getAddress());
                            break;
                        case 1:
                            AddressQuery query = null;
                            this.entityManager.retrieveAll(segment.getAddress(query));
                            break;
                        default:
                            // there is no retrireveAll() operation
                    }
                }
            } finally {
                super.taskId = null;
            }
            // invoke sendMessageTemplate (struct with object reference field)
            this.begin();
            MessageTemplate messageTemplate = messageTemplateClass.createMessageTemplate();
            messageTemplate.setText("hello world");
            segment.addMessageTemplate(
                false,
                "template0",
                messageTemplate
            );
            this.commit();
            this.begin();
            EmailAddressSendMessageTemplateParams emailAddressSendMessageTemplateParams;
            switch(this.nextStructureCreation()){
                case BY_PACKAGE:
                    emailAddressSendMessageTemplateParams = app1Package.createEmailAddressSendMessageTemplateParams(
                        messageTemplate,
                        0,
                        "hello world"
                    ); 
                    break;
                case BY_MEMBER:
                    emailAddressSendMessageTemplateParams = Datatypes.create(
                        EmailAddressSendMessageTemplateParams.class,
                        Datatypes.member(EmailAddressSendMessageTemplateParams.Member.body, messageTemplate),
                        Datatypes.member(EmailAddressSendMessageTemplateParams.Member.priority, 0),
                        Datatypes.member(EmailAddressSendMessageTemplateParams.Member.subject, "hello world")
                    );
                    break;
                case BY_POSITION:
                    emailAddressSendMessageTemplateParams = Datatypes.create(
                        EmailAddressSendMessageTemplateParams.class,
                            messageTemplate,
                            0,
                            "hello world"
                        );
                    break;
                default: 
                    emailAddressSendMessageTemplateParams = null;
            }
            
            EmailAddressSendMessageTemplateResult sendResult = emailAddress.sendMessageTemplate(
                emailAddressSendMessageTemplateParams
            );
            assertNotNull("Send result", sendResult);
            this.commit();

            // create a person without qualifier
            Person person;

            //
            // CR20019366 Marshalling
            //
            try {
                super.taskId = "CR20019366";
                Collection<Person> people = segment.getPerson();
                Person aPerson = people.iterator().next();
                assertSame(
                    "segment.getPerson()",
                    JDOHelper.getPersistenceManager(segment),
                    JDOHelper.getPersistenceManager(aPerson)
                );
            } finally {
                super.taskId = null;
            }

            //
            // CR0003390 Code Accessor
            // 
            try {
                super.taskId = "CR0003390";
                this.begin();
                person = segment.getPerson(false,"DOE");
                Runtime runtime = Runtime.getRuntime();
                persistenceManager.makeTransactional(segment);
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
                this.commit();
            } finally {
                super.taskId = null;
            }

            //
            // CR0003686 
            //
            try {
                super.taskId = "CR0003686";
                this.begin();
                person = personClass.createPerson();
                assertEquals(
                    "Mix-in", 
                    this instanceof LocalConnectionTest, 
                    person instanceof NaturalPerson
                );
                person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1963-01-01"));
                person.setLastName("Rossi");
                person.setSalutation("Signor");
                person.getGivenName().add("Alfonso");
                int age = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR) - 1963;
                assertEquals("Age", age, person.getAge()); 
                person.setSex((short)0);
                segment.addPerson(false, nextId(), person);
                this.commit();
                fail("'Signor' was expected not to be supported");
            } catch(JDOFatalDataStoreException exception) {
                System.out.println("Unsupported language prevents commit");
            } finally {
                super.taskId = null;
            }

            try {
                super.taskId = "CR20019721";
                this.begin();
                person = personClass.createPerson();
                person.setForeignId("FX");
                XMLGregorianCalendar birthDate = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("1960-01-01");
                birthDate.setTimezone(-1);
                person.setBirthdate(birthDate);
                person.setBirthdateAsDateTime(Datatypes.create(Date.class, "19600101T120000.000Z"));
                assertEquals(
                	"Born at noon", 
                	"1960-01-01T12:00:00.000Z", 
                	DateTimeFormat.EXTENDED_UTC_FORMAT.format(person.getBirthdateAsDateTime())
                );
                person.setLastName("MusterX");
                person.setSalutation("Herr");
                person.setSex((short)0);
                person.setGivenName("Hans", "Heiri");
                SparseArray<String> additionalInfo = person.getAdditionalInfo();
                additionalInfo.put(0, "Null");
                additionalInfo.put(2, "Zwei");
                person.getAssignedAddress().addAll(Arrays.asList(postalAddress,emailAddress));
                if(this instanceof ProxyConnectionTest) {
                    try {
                        segment.addPerson(person);
                        fail("Birthdate with time zone is invalid: " + birthDate);
                    } catch (JmiServiceException expected) {
                        BasicException exceptionStack = BasicException.toExceptionStack(expected);
                        assertEquals("Unit of work has to be rolled back", BasicException.Code.TRANSFORMATION_FAILURE, exceptionStack.getExceptionCode());
                        assertEquals("Unit of work has to be rolled back", CommException.class.getName(), exceptionStack.getExceptionClass());
                        assertEquals("cause", BasicException.Code.BAD_PARAMETER, exceptionStack.getCause(null).getExceptionCode());
                        assertEquals("cause", IllegalArgumentException.class.getName(), exceptionStack.getCause(null).getExceptionClass());
                    } finally {
                        this.rollback();
                    }
                } else if(this instanceof OptimisticContainerManagedTransactionTest) {
                    try {
                        segment.addPerson(person);
                        this.commit();
                        fail("Birthdate with time zone is invalid: " + birthDate);
                    } catch (JDOFatalDataStoreException expected) {
                        BasicException exceptionStack = BasicException.toExceptionStack(expected);
                        assertEquals("Unit of work has to be rolled back", BasicException.Code.TRANSFORMATION_FAILURE, exceptionStack.getExceptionCode());
                        assertEquals("Initial Cause", BasicException.Code.GENERIC, exceptionStack.getCause(null).getExceptionCode());
                        assertTrue(
                        	"Initial Cause", 
                        	IllegalArgumentException.class.getName().equals(exceptionStack.getCause(null).getExceptionClass()) || // JRE 6
                        	NumberFormatException.class.getName().equals(exceptionStack.getCause(null).getExceptionClass()) // JRE 5.0
                        );
                    }
                } else if(this instanceof PessimisticContainerManagedTransactionTest) {
                    try {
                        segment.addPerson(person);
                        this.commit();
                        fail("Birthdate with time zone is invalid: " + birthDate);
                    } catch (JDOFatalDataStoreException expected) {
                        BasicException exceptionStack = BasicException.toExceptionStack(expected);
                        assertEquals("Unit of work should have been rolled back", BasicException.Code.TRANSFORMATION_FAILURE, exceptionStack.getExceptionCode());
                        assertEquals("Unit of work should have been rolled back", BasicException.Code.GENERIC, exceptionStack.getCause(null).getExceptionCode());
                        assertTrue(
                        	"Initial Cause", 
                        	IllegalArgumentException.class.getName().equals(exceptionStack.getCause(null).getExceptionClass()) || // JRE 6
                        	NumberFormatException.class.getName().equals(exceptionStack.getCause(null).getExceptionClass()) // JRE 5.0
                        );
                    }
                } else {
                    try {
                        segment.addPerson(person);
                        this.commit();
                        fail("Birthdate with time zone is invalid: " + birthDate);
                    } catch (JDOFatalDataStoreException expected) {
                        BasicException exceptionStack = BasicException.toExceptionStack(expected);
                        assertEquals("Unit of work should have been rolled back", BasicException.Code.ROLLBACK, exceptionStack.getExceptionCode());
                        assertEquals("Initial Cause", BasicException.Code.GENERIC, exceptionStack.getCause(null).getExceptionCode());
                        assertTrue(
                        	"Initial Cause", 
                        	IllegalArgumentException.class.getName().equals(exceptionStack.getCause(null).getExceptionClass()) || // JRE 6
                        	NumberFormatException.class.getName().equals(exceptionStack.getCause(null).getExceptionClass()) // JRE 5.0
                        );
                    }
                }
            } finally {
                super.taskId = null;
            }
            
            {
                this.begin();
                person = personClass.createPerson();
                person.setForeignId("FX");
                XMLGregorianCalendar birthDate = Datatypes.create(XMLGregorianCalendar.class, "1960-01-01");
                person.setBirthdate(birthDate);
                person.setBirthdateAsDateTime(Datatypes.create(Date.class, "19600101T120000.000Z"));
                assertEquals(
                	"Born at noon", 
                	"1960-01-01T12:00:00.000Z", 
                	DateTimeFormat.EXTENDED_UTC_FORMAT.format(person.getBirthdateAsDateTime())
                );
                person.setLastName("MusterX");
                person.setSalutation("Herr");
                person.setSex((short)0);
                person.getGivenName().add("Hans");
                person.getGivenName().add("Heiri");
                SparseArray<String> additionalInfo = person.getAdditionalInfo();
                additionalInfo.put(0, "Null");
                additionalInfo.put(2, "Zwei");
                person.getAssignedAddress().addAll(Arrays.asList(postalAddress,emailAddress));
                segment.addPerson(false, nextId(), person);
                this.commit();
            }
            
            Path personId = (Path) JDOHelper.getObjectId(person);
            if(!(this instanceof AbstractContainerManagedTransactionTest)){
                {
                    this.begin();
                    person.getAdditionalInfo().put(10, "Ten");
                }
                //
                // CR20019182 persistenceCapable.equals() 
                // 
                try {
                    super.taskId = "CR20019182";
                    PersistenceManager anotherPersistenceManager = super.entityManager.getPersistenceManagerFactory().getPersistenceManager();
                    Person anotherPerson = (Person) anotherPersistenceManager.getObjectById(personId);
                    assertNotSame("Same person in different persistence managers", person, anotherPerson);
                    assertEquals("Same person in different persistence managers", person, anotherPerson);
                    SparseArray<String> anotherInfo = anotherPerson.getAdditionalInfo();
                    assertEquals("CR20018969", 2, anotherInfo.size());
                    assertEquals("CR20018969.0", "Null", anotherInfo.get(0));
                    assertNull("CR20018969.1", anotherInfo.get(1));
                    assertEquals("CR20018969.2", "Zwei", anotherInfo.get(2));
                    assertNull("CR20018969.3", anotherInfo.get(3));
                    assertTrue("CR20018969.1_2", anotherInfo.subMap(1, 2).isEmpty());
                    SparseArray<String> tail = anotherInfo.tailMap(1);
                    assertEquals("CR20018969.1_", 1, tail.size());
                    SparseArray<String> head = anotherInfo.headMap(1);
                    assertEquals("CR20018969._1", 1, head.size());
                    assertEquals("CR20018969.0", "Null", head.get(0));
                    assertNull("CR20018969.0", tail.get(0));
                    assertNull("CR20018969.2", head.get(2));
                    assertEquals("CR20018969.2", "Zwei", tail.get(2));
                    anotherPersistenceManager.currentTransaction().begin();
                    for(Map.Entry<Integer,String> e : anotherInfo.entrySet()) {
                        e.setValue(e.getKey().toString());
                    }
                    anotherPersistenceManager.currentTransaction().commit();
                } finally {
                    super.taskId = null;
                }            
                try {
                  this.commit();
                  fail("CONCURRENT_ACCESS_FAILURE expected");
                } catch (JDOOptimisticVerificationException exception){
                    assertEquals(
                        "CONCURRENT_ACCESS_FAILURE expected",
                        BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                        BasicException.toExceptionStack(exception).getCause(null).getExceptionCode()
                    );
                }          
                {
                    PersistenceManager anotherPersistenceManager = super.entityManager.getPersistenceManagerFactory().getPersistenceManager();
                    Person anotherPerson = (Person) anotherPersistenceManager.getObjectById(personId);
                    SparseArray<String> anotherInfo = anotherPerson.getAdditionalInfo();
                    assertEquals("CR20018969", 2, anotherInfo.size());
                    assertEquals("CR20018969.0", "0", anotherInfo.get(0));
                    assertNull("CR20018969.1", anotherInfo.get(1));
                    assertEquals("CR20018969.2", "2", anotherInfo.get(2));
                    assertNull("CR20018969.3", anotherInfo.get(3));
                    assertTrue("CR20018969.1_2", anotherInfo.subMap(1, 2).isEmpty());
                    SparseArray<String> tail = anotherInfo.tailMap(1);
                    assertEquals("CR20018969.1_", 1, tail.size());
                    SparseArray<String> head = anotherInfo.headMap(1);
                    assertEquals("CR20018969._1", 1, head.size());
                    assertEquals("CR20018969.0", "0", head.get(0));
                    assertNull("CR20018969.0", tail.get(0));
                    assertNull("CR20018969.2", head.get(2));
                    assertEquals("CR20018969.2", "2", tail.get(2));
                }
            }
            Path pId = (Path) JDOHelper.getObjectId(person);
            assertEquals("person.refMofId() must be object path", 1, new Path(person.refMofId()).size() % 2);
            assertEquals("person's path must be object path", 1, pId.size() % 2);
            assertEquals("person.refIdentity() must corrspond to its path", pId.toXRI(), person.getIdentity());
            assertEquals("person.refMofId() must corrspond to its path", pId.toXRI(), person.refMofId());

            assertEquals(
                "Initial postal code without country code",
                "8005",
                postalAddress.getPostalCode()
            );

            
            // Add country code to postal code
            this.begin();
            person.voidOp();
            //
            // The postal address object itself is untouched 
            // but has been modified by an operation on the person object
            //
            persistenceManager.makeTransactional(postalAddress);
            this.commit();

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
                System.out.println("assigned address=" + JDOHelper.getObjectId(address));
            }

            PostalAddress additionalAddress;
            try {
                super.taskId = "CR0002096";
                this.begin();
                additionalAddress = app1Package.getPostalAddress().createPostalAddress();
                additionalAddress.setCity("Zurich");
                additionalAddress.setHouseNumber("1");
                additionalAddress.setPostalCode("8050");
                additionalAddress.setStreet("Technoparkstrasse");
                // get assigned addresses by iterator
                System.out.println("adding three more addresses");
                segment.addAddress(false, "CR0002096", additionalAddress);
                person.getAssignedAddress().addAll(Arrays.asList(postalAddress,additionalAddress,emailAddress));
                this.commit();
            } finally {
                super.taskId = null;
            }
            List<Address> assignedAddresses = person.getAssignedAddress();
            for(Address address : assignedAddresses) {
                // postal code refreshed
                if(JDOHelper.getObjectId(address).equals(JDOHelper.getObjectId(postalAddress))) {
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
                System.out.println("assigned address=" + JDOHelper.getObjectId(address));
            }
            assertEquals("number of assigned addresses", 5, person.getAssignedAddress().size());
    
            // assignAddress by operation. This operation does not really
            // perform an assign. It is just there to see whether the operation
            // invocation works.
            this.begin();
            PersonAssignAddressParams personAssignAddressParams;
            switch(this.nextStructureCreation()) {
                case BY_MEMBER:
                    personAssignAddressParams = Datatypes.create(
                        PersonAssignAddressParams.class,
                        Datatypes.member(
                            PersonAssignAddressParams.Member.address,
                            Arrays.asList(
                                new Address[]{
                                    postalAddress,
                                    emailAddress
                                }
                            )
                        )
                    );
                    break;
                case BY_PACKAGE:
                    personAssignAddressParams = app1Package.createPersonAssignAddressParams(
                        Arrays.asList(
                            new Address[]{
                                postalAddress,
                                emailAddress
                            }
                        )
                    );
                    break;
                case BY_POSITION:
                    personAssignAddressParams = Datatypes.create(
                        PersonAssignAddressParams.class,
                        Arrays.asList(
                            new Address[]{
                                postalAddress,
                                emailAddress
                            }
                        )
                    );
                    break;
                default:
                    personAssignAddressParams = null;
            }
            person.assignAddress(personAssignAddressParams);
            this.commit();

            //
            // CR20018578 State After Removal
            // 
            try {
                super.taskId = "CR20018578";
                this.begin();
                assertTrue("Persistent", JDOHelper.isPersistent(additionalAddress));
                assertFalse("Deleted", JDOHelper.isDeleted(additionalAddress));
                assertNotNull("Object Id", JDOHelper.getObjectId(additionalAddress));
                additionalAddress.refDelete();
                assertTrue("Persistent", JDOHelper.isPersistent(additionalAddress));
                assertTrue("Deleted", JDOHelper.isDeleted(additionalAddress));
                assertNotNull("Object Id", JDOHelper.getObjectId(additionalAddress));
                Address a = segment.getAddress("CR0002096");
                assertNotNull("Deleted additional address", a);
                assertSame("Deleted additional address", additionalAddress, a);
                this.commit();
                assertFalse("Persistent", JDOHelper.isPersistent(additionalAddress));
                assertNull("Object Id", JDOHelper.getObjectId(additionalAddress));
                assertNotNull("Transactional Object Id", JDOHelper.getTransactionalObjectId(additionalAddress));
                assertNull("Deleted additional address", segment.getAddress("CR0002096"));
            } finally {
                super.taskId = null;
            }
            
            //
            // CR0002096
            // 
            try {
                super.taskId = "CR0002096";
                this.begin();
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
                        assertNotNull("Returning null was the former behaviour", address);
                        assertNotNull("Returning null was the former behaviour", JDOHelper.getObjectId(address)); // was current object id
                        if(JDOHelper.getObjectId(address).equals(JDOHelper.getObjectId(postalAddress))) {
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
                        System.out.println("Assigned address " + j + ": " + JDOHelper.getObjectId(address));
                    } catch (InvalidObjectException exception) {
                        i.remove();
                        System.out.println("Assigned address " + j + ": removed");
                    }
                }
                this.commit();
                assertEquals("number of assigned addresses", 4, person.getAssignedAddress().size());
            } finally {
                super.taskId = null;
            }

            //
            // CR20018837
            //
            try {
                super.taskId = "CR20018837";
                System.out.println("Removal test");
                for(int i = 0; i < 8; i++) {
                    boolean persistentNew = i % 2 == 0;
                    String invoiceId = this.taskId + (char)('a' + i);
                    this.begin();
                    Invoice additionalInvoice = this.entityManager.newInstance(Invoice.class);
                    additionalInvoice.setProductGroupId("PG" + i);
                    additionalInvoice.setDescription("Invoice # " + invoiceId);
                    assertFalse("Step " + i + " Additional invoice not yet deleted", JDOHelper.isDeleted(additionalInvoice));
                    assertFalse("Step " + i + " Additional invoice not yet persistent", JDOHelper.isPersistent(additionalInvoice));
                    assertFalse("Step " + i + " Additional invoice not yet new", JDOHelper.isNew(additionalInvoice));
                    assertFalse("Step " + i + " Additional invoice not yet persistent", JDOHelper.isDirty(additionalInvoice));
                    InvoicePosition additionalPosition = this.entityManager.newInstance(InvoicePosition.class);
                    additionalPosition.setProductId("P" + i);
                    assertFalse("Step " + i + " Additional position not yet deleted", JDOHelper.isDeleted(additionalPosition));
                    assertFalse("Step " + i + " Additional position not yet persistent", JDOHelper.isPersistent(additionalPosition));
                    assertFalse("Step " + i + " Additional position not yet new", JDOHelper.isNew(additionalPosition));
                    assertFalse("Step " + i + " Additional position not yet persistent", JDOHelper.isDirty(additionalPosition));
                    segment.addInvoice(false,invoiceId , additionalInvoice);
                    assertSame("Step " + i + " Created invoice retrieval", additionalInvoice, segment.getInvoice(false, invoiceId));
                    assertFalse("Step " + i + " Additional invoice not yet deleted", JDOHelper.isDeleted(additionalInvoice));
                    assertTrue("Step " + i + " Additional invoice now persistent", JDOHelper.isPersistent(additionalInvoice));
                    assertTrue("Step " + i + " Additional invoice now new", JDOHelper.isNew(additionalInvoice));
                    assertTrue("Step " + i + " Additional invoice now persistent", JDOHelper.isDirty(additionalInvoice));
                    additionalInvoice.addInvoicePosition(false,Integer.toString(i) , additionalPosition);
                    assertSame("Step " + i + " Created position retrieval", additionalPosition, additionalInvoice.getInvoicePosition(false,Integer.toString(i)));
                    assertFalse("Step " + i + " Additional invoice not yet deleted", JDOHelper.isDeleted(additionalPosition));
                    assertTrue("Step " + i + " Additional invoice now persistent", JDOHelper.isPersistent(additionalPosition));
                    assertTrue("Step " + i + " Additional invoice now new", JDOHelper.isNew(additionalPosition));
                    assertTrue("Step " + i + " Additional invoice now persistent", JDOHelper.isDirty(additionalPosition));
                    Object positionId = JDOHelper.getObjectId(additionalPosition);
                    if(!persistentNew) {
                        this.commit();
                        this.begin();
                    }
                    switch(i/2) {
                        case 0:
                            additionalInvoice.refDelete();
                            break;
                        case 1:
                            segment.getInvoice().remove(QualifierType.REASSIGNABLE, invoiceId);
                            break;
                        case 2:
                            this.entityManager.deletePersistent(additionalInvoice);
                            break;
                        case 3:
                            segment.getInvoice().remove(additionalInvoice);
                            break;
                    }
                    assertSame("Step " + i + " Deleted invoice retrieval", additionalInvoice, segment.getInvoice(false, invoiceId));
                    assertTrue("Step " + i + " Additional invoice now deleted", JDOHelper.isDeleted(additionalInvoice));
                    assertTrue("Step " + i + " Additional invoice still persistent", JDOHelper.isPersistent(additionalInvoice));
                    assertEquals("Step " + i + " Additional invoice might be new new", i % 2 == 0, JDOHelper.isNew(additionalInvoice));
                    assertTrue("Step " + i + " Additional invoice now deleted", JDOHelper.isDirty(additionalInvoice));
                    assertSame("Step " + i + " Deleted position retrieval", additionalPosition, this.entityManager.getObjectById(positionId));
                    if(persistentNew) {
                        assertTrue("Step " + i + " Additional position now deleted", JDOHelper.isDeleted(additionalPosition));
                        assertTrue("Step " + i + " Additional position still persistent", JDOHelper.isPersistent(additionalPosition));
                        assertEquals("Step " + i + " Additional position might be new", i % 2 == 0, JDOHelper.isNew(additionalPosition));
                        assertTrue("Step " + i + " Additional position now deleted", JDOHelper.isDirty(additionalPosition));
                    }
                    this.commit();
                    assertNull("Step " + i + " Deleted invoice retrieval", segment.getInvoice(false, invoiceId));
                    assertFalse("Step " + i + " Additional invoice now transient", JDOHelper.isDeleted(additionalInvoice));
                    assertFalse("Step " + i + " Additional invoice now transient", JDOHelper.isPersistent(additionalInvoice));
                    assertFalse("Step " + i + " Additional invoice now transient", JDOHelper.isNew(additionalInvoice));
                    assertFalse("Step " + i + " Additional invoice now transient", JDOHelper.isDirty(additionalInvoice));
                    assertFalse("Step " + i + " Additional position now transient", JDOHelper.isDeleted(additionalPosition));
                    assertFalse("Step " + i + " Additional position now transient", JDOHelper.isPersistent(additionalPosition));
                    assertFalse("Step " + i + " Additional position now transient", JDOHelper.isNew(additionalPosition));
                    assertFalse("Step " + i + " Additional position now transient", JDOHelper.isDirty(additionalPosition));
                }
            } finally {
                super.taskId = null;
            }
            //
            // CR20018837
            //
            try {
                super.taskId = "CR20018837";
                System.out.println("Persistent-new-deleted test");
                this.begin();
                additionalAddress = app1Package.getPostalAddress().createPostalAddress();
                additionalAddress.setCity("Liestal");
                additionalAddress.setHouseNumber("2");
                additionalAddress.setPostalCode("2222");
                additionalAddress.setStreet("Nebenstrasse");
                assertFalse("Additional address not yet deleted", JDOHelper.isDeleted(additionalAddress));
                assertFalse("Additional address not yet persistent", JDOHelper.isPersistent(additionalAddress));
                assertFalse("Additional address not yet new", JDOHelper.isNew(additionalAddress));
                segment.addAddress(false,"9002", additionalAddress);
                assertFalse("Additional address not yet deleted", JDOHelper.isDeleted(additionalAddress));
                assertTrue("Additional address now persistent", JDOHelper.isPersistent(additionalAddress));
                assertTrue("Additional address now new", JDOHelper.isNew(additionalAddress));
                additionalAddress.refDelete();
                assertSame("Deleted address retrieval", additionalAddress, segment.getAddress(false, "9002"));
                assertTrue("Additional address now deleted", JDOHelper.isDeleted(additionalAddress));
                assertTrue("Additional address still persistent", JDOHelper.isPersistent(additionalAddress));
                assertTrue("Additional address still new", JDOHelper.isNew(additionalAddress));
                this.rollback();
                assertFalse("Additional address no longer deleted", JDOHelper.isDeleted(additionalAddress));
                assertFalse("Additional address no longer persistent", JDOHelper.isPersistent(additionalAddress));
                assertFalse("Additional address no longer new", JDOHelper.isNew(additionalAddress));
                this.begin();
                additionalAddress = app1Package.getPostalAddress().createPostalAddress();
                additionalAddress.setCity("Liestal");
                additionalAddress.setHouseNumber("3");
                additionalAddress.setPostalCode("2222");
                additionalAddress.setStreet("Nebenstrasse");
                assertFalse("Additional address not yet deleted", JDOHelper.isDeleted(additionalAddress));
                assertFalse("Additional address not yet persistent", JDOHelper.isPersistent(additionalAddress));
                assertFalse("Additional address not yet new", JDOHelper.isNew(additionalAddress));
                segment.addAddress(false,"9003", additionalAddress);
                assertFalse("Additional address not yet deleted", JDOHelper.isDeleted(additionalAddress));
                assertTrue("Additional address now persistent", JDOHelper.isPersistent(additionalAddress));
                assertTrue("Additional address now new", JDOHelper.isNew(additionalAddress));
                segment.getAddress().remove(QualifierType.REASSIGNABLE, "9003");
                assertSame("Deleted address retrieval", additionalAddress, segment.getAddress(false, "9003"));
                assertTrue("Additional address now deleted", JDOHelper.isDeleted(additionalAddress));
                assertTrue("Additional address still persistent", JDOHelper.isPersistent(additionalAddress));
                assertTrue("Additional address still new", JDOHelper.isNew(additionalAddress));
                this.commit();
                assertFalse("Additional address no longer deleted", JDOHelper.isDeleted(additionalAddress));
                assertFalse("Additional address no longer persistent", JDOHelper.isPersistent(additionalAddress));
                assertFalse("Additional address no longer new", JDOHelper.isNew(additionalAddress));
                this.begin();
                additionalAddress = app1Package.getPostalAddress().createPostalAddress();
                additionalAddress.setCity("Liestal");
                additionalAddress.setHouseNumber("4");
                additionalAddress.setPostalCode("2222");
                additionalAddress.setStreet("Nebenstrasse");
                assertFalse("Additional address not yet deleted", JDOHelper.isDeleted(additionalAddress));
                assertFalse("Additional address not yet persistent", JDOHelper.isPersistent(additionalAddress));
                assertFalse("Additional address not yet new", JDOHelper.isNew(additionalAddress));
                segment.addAddress(false,"9004", additionalAddress);
                assertFalse("Additional address not yet deleted", JDOHelper.isDeleted(additionalAddress));
                assertTrue("Additional address now persistent", JDOHelper.isPersistent(additionalAddress));
                assertTrue("Additional address now new", JDOHelper.isNew(additionalAddress));
                //            segment.getAddress().remove(additionalAddress);
                //            assertSame("Deleted address retrieval", additionalAddress, segment.getAddress(false, "9004"));
                //            assertTrue("Additional address now deleted", JDOHelper.isDeleted(additionalAddress));
                assertTrue("Additional address still persistent", JDOHelper.isPersistent(additionalAddress));
                assertTrue("Additional address still new", JDOHelper.isNew(additionalAddress));
                this.rollback();
                assertFalse("Additional address no longer deleted", JDOHelper.isDeleted(additionalAddress));
                assertFalse("Additional address no longer persistent", JDOHelper.isPersistent(additionalAddress));
                assertFalse("Additional address no longer new", JDOHelper.isNew(additionalAddress));
                //
                System.out.println("Transient container test");
                @SuppressWarnings("unused")
                test.openmdx.app1.jmi1.Segment transientSegment = app1Package.getSegment().createSegment();
                this.begin();
                //            additionalAddress = app1Package.getPostalAddress().createPostalAddress();
                //            additionalAddress.setCity("Frick");
                //            additionalAddress.setHouseNumber("3");
                //            additionalAddress.setPostalCode("1111");
                //            additionalAddress.setStreet("Hauptstrasse");
                //            assertFalse("Additional address not deleted", JDOHelper.isDeleted(additionalAddress));
                //            assertFalse("Additional address not persistent", JDOHelper.isPersistent(additionalAddress));
                //            assertFalse("Additional address not new", JDOHelper.isNew(additionalAddress));
                //            transientSegment.addAddress(false,"9003", additionalAddress);
                //            assertFalse("Additional address not deleted", JDOHelper.isDeleted(additionalAddress));
                //            assertFalse("Additional address not persistent", JDOHelper.isPersistent(additionalAddress));
                //            assertFalse("Additional address not new", JDOHelper.isNew(additionalAddress));
                //            assertSame("Transient address retrieval", additionalAddress, transientSegment.getAddress(false, "9003"));
                //            transientSegment.getAddress().remove(QualifierType.REASSIGNABLE, "9003");
                //            assertNull("Removed address retrieval", transientSegment.getAddress(false, "9003"));
                //            assertFalse("Additional address not deleted", JDOHelper.isDeleted(additionalAddress));
                //            assertFalse("Additional address not persistent", JDOHelper.isPersistent(additionalAddress));
                //            assertFalse("Additional address not new", JDOHelper.isNew(additionalAddress));
                this.rollback();
                this.begin();
                //            additionalAddress = app1Package.getPostalAddress().createPostalAddress();
                //            additionalAddress.setCity("Frick");
                //            additionalAddress.setHouseNumber("4");
                //            additionalAddress.setPostalCode("1111");
                //            additionalAddress.setStreet("Hauptstrasse");
                //            transientSegment.addAddress(false,"9004", additionalAddress);
                //            assertSame("Transient address retrieval", additionalAddress, transientSegment.getAddress(false, "9004"));
                //            assertTrue("Transient address removal", transientSegment.getAddress().remove(additionalAddress));
                //            assertNull("Removed address retrieval", transientSegment.getAddress(false, "9004"));
                this.rollback();
            } finally {
                super.taskId = null;
            }
            //
            // CR0002987
            // 
            try {
                super.taskId = "CR0002987";
                System.out.println("Explicit rollback test");
                this.begin();
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
                this.rollback();
                assertFalse("Additional address no longer persistent", JDOHelper.isPersistent(additionalAddress));
                assertFalse("Additional address no longer new", JDOHelper.isNew(additionalAddress));
                try {
                    System.out.println("Implicit rollback test");
                    this.begin();
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
                    this.commit();
                    fail("constraint isFrozen --> object can not be updated");
                } catch(JDOFatalDataStoreException e) {
                    assertFalse("Additional address no longer new", JDOHelper.isNew(additionalAddress));
                    assertFalse(
                        "Additional address no longer persistent", 
                        JDOHelper.isPersistent(additionalAddress)
                    );
                }
            } finally {
                super.taskId = null;
            }

            for(
                    int i = 0;
                    i < 2;
                    i++
            ){
                assertNull("No TRANSIENT person expected", segment.getPerson(false, "TRANSIENT"));
                if(i==0) this.begin();
            }
            // create and remove in same unit of work
            person = personClass.createPerson();
            person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1960-01-01"));
            person.setBirthdateAsDateTime(Datatypes.create(Date.class, "19600101T120000.000Z"));
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
            this.commit();

            PersonGroup g0;
            PersonGroup g1;
            PersonGroup g2;
            //
            // create some PersonGroups
            //
            try {
                super.taskId = "CR20019430";
                this.begin();
                g0 = personGroupClass.createPersonGroup();
                g0.setName("Group 0");
                segment.addPersonGroup(
                    false,
                    "g0",
                    g0
                );
                g1 = personGroupClass.createPersonGroup();
                segment.addPersonGroup(
                    false,
                    "g1",
                    g1
                );
                g2 = personGroupClass.createPersonGroup();
                g2.setName("Group 2");
                segment.addPersonGroup(
                    false,
                    "g2",
                    g2
                );
                this.entityManager.flush();
                g1.setName("Group 1");
                this.commit();
            } finally {
                super.taskId = null;
            }

            // create some Persons
            this.begin();
            for(
                    int i = 0;
                    i <= N_PERSONS;
                    i++
            ) {
                person = personClass.createPerson();
                person.setForeignId("F" + i);
                person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1960-01-01"));
                person.setBirthdateAsDateTime(Datatypes.create(Date.class, "19600101T120000.000Z"));
                person.setLastName("Muster" + i);
                person.setSalutation("Herr");
                person.setSex((short)0);
                person.setGivenName(new String[]{"Hans", "Heiri"});
                person.getAssignedAddress().add(postalAddress);
                person.getPersonGroup().add(g0);
                person.getPersonGroup().add(g1);
                person.getPersonGroup().add(g2);
                if(i < N_PERSONS) {
                    segment.addPerson(false, "000" + i, person);
                } else if (this instanceof LocalConnectionTest) try {
                    //
                    // CR20019192 UnsupportedOperationException in JMI collection delegate calls 
                    // 
                    super.taskId = "CRCR20019192";
                    segment.addForeignPerson("F" + N_PERSONS, person);
                    fail("This shared assoication is expected to be unmodifiable");
                } catch(InvalidCallException expected) {
                    // We expect to pass this exception handler
                } finally {
                    super.taskId = null;
                }
            }
            this.commit();
            
            // get person on 'composite' association 'SegmentHasPerson'
            person = segment.getPerson(false,"0001");
            System.out.println("person.age=" + person.getAge());
            System.out.println("person givenName=" + person.getGivenName().get(0));
            System.out.println("person.identity=" + person.getIdentity());
            System.out.println("person.creationDateTime=" + person.getCreationDateTime());
            System.out.println("person.createdAt=" + person.getCreatedAt());

            // test unqualified feature retrieval
            assertTrue("postalAddress.address must be instance of String", emailAddress.refGetValue("address") instanceof String);
            assertTrue("segment.address must be instance of Container", segment.refGetValue("address") instanceof RefContainer<?>);
            assertTrue("postalAddress.address must be instance of String", emailAddress.refGetValue("address") instanceof String);
            //
            // test performance of accessor.jmi of reading all non-derived attributes of person INSPECTION_COUNT times
            //
            for(int j = 0; j < 2; j++) {
                startedAt = System.currentTimeMillis();
                for(
                        int i = 0;
                        i < INSPECTION_COUNT;
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
    //                person.getAge();
    //                person.getCreationDateTime();
                }
            }
            {
                long elapsed = System.currentTimeMillis() - startedAt;
                System.out.println(
                    "time for inspecting person " + INSPECTION_COUNT + " times [JMI]=" + elapsed + " ms, i.e. " + 
                    BigDecimal.valueOf(elapsed).divide(BigDecimal.valueOf(INSPECTION_COUNT * MEMBER_COUNT), 3, BigDecimal.ROUND_HALF_UP) + " ms per feature"
                );
            }
            //
            // test the performance of reflective JMI accesses
            //
            startedAt = System.currentTimeMillis();
            for(
                    int i = 0;
                    i < INSPECTION_COUNT;
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
//                person.refGetValue("age");
//                person.refGetValue("creationDateTime");
            }
            {
                long elapsed = System.currentTimeMillis() - startedAt;
                System.out.println(
                    "time for inspecting person " + INSPECTION_COUNT + " times [refGetValue()]=" + elapsed + " ms, i.e. " + 
                    BigDecimal.valueOf(elapsed).divide(BigDecimal.valueOf(INSPECTION_COUNT * MEMBER_COUNT), 3, BigDecimal.ROUND_HALF_UP) + " ms per feature"
                );
            }
            if(this instanceof LocalConnectionTest) {
                DataObject_1_0 personData = ((RefObject_1_0) ((DelegatingRefObject_1_0) person).openmdxjdoGetDataObject()).refDelegate();
                startedAt = System.currentTimeMillis();
                for(
                        int i = 0;
                        i < INSPECTION_COUNT;
                        i++
                ) {
                    personData.objGetValue("lastName");
                    personData.objGetValue("foreignId");
                    personData.objGetValue("givenName");
                    personData.objGetValue("sex");
                    personData.objGetValue("salutation");
                    personData.objGetValue("birthdate");
                    personData.objGetValue("birthdateAsDateTime");
                    personData.objGetValue("additionalInfo");
                    personData.objGetValue("memberOfGroup");
//                    personData.objGetValue("age");
//                    personData.objGetValue("creationDateTime");
                }
                long elapsed = System.currentTimeMillis() - startedAt;
                System.out.println(
                    "time for inspecting person " + INSPECTION_COUNT + " times [objGetValue()]=" + elapsed + " ms, i.e. " + 
                    BigDecimal.valueOf(elapsed).divide(BigDecimal.valueOf(INSPECTION_COUNT * MEMBER_COUNT), 3, BigDecimal.ROUND_HALF_UP) + " ms per feature"
                );
            }
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

            {
                // get person on 'none', derived association 'SegmentReferencesForeignPerson'
                person = segment.getForeignPerson("F1");
                assertNotNull("Foreign Person", person);
                PersistenceManager m = JDOHelper.getPersistenceManager(person);
                assertSame(
                    "Derived association marshalling", 
                    persistenceManager, 
                    m
                );
            }
            {
                //
                // CR20018977 Dispatching to Association Impl 
                // 
                Path foreignId = segment.refGetPath().getDescendant("foreignPerson", "F1");
                Person personByForeignId = (Person) persistenceManager.getObjectById(foreignId);
                assertSame("CR20018977", personByForeignId, person);
            }

            System.out.println("person.age=" + person.getAge());
            System.out.println("person givenName=" + person.getGivenName().get(0));
            //
            // 20019656 isEmpty()'s iteration
            //
            SegmentHasPerson.Person<Person> allPeople = segment.getPerson();
            try {
                super.taskId = "CR20019656";
                int count = 0;
                People: for(@SuppressWarnings("unused") Person aPerson : allPeople) {
                    if(++count > 50) {
                        break People;
                    }
                }
                if(allPeople.isEmpty()) {
                    fail("There should be people");
                }
            } finally {
                super.taskId = null;
            }
            
            {
                int people = segment.getForeignPerson().size();
                System.out.println("Number of people: " + people);
            }
            // get persons with filter 1
            PersonQuery personQuery = app1Package.createPersonQuery();
            personQuery.lastName().like(
                "Muster1.*"
            );
            personQuery.birthdateAsDateTime().lessThanOrEqualTo(
                new Date()
            );
            personQuery.orderByCreatedAt().ascending();
            SegmentHasPerson.Person<Person> personCollection;
            List<Person> personList;
            try {
                super.taskId = "CR20019366";
                personCollection = segment.getPerson();
                Person[] personArray = new Person[personCollection.size()];
                personCollection.toArray(personArray);
                for(Person person20019366 : personArray) {
                    if(this instanceof ProxyConnectionTest) {
                        assertFalse("Person Proxy", person20019366 instanceof NaturalPerson);
                    } else {
                        assertTrue("Person", person20019366 instanceof NaturalPerson);
                        assertFalse("Person", ((NaturalPerson)person20019366).isRetired());
                    }
                }
                personList = segment.getPerson((PersonQuery)null);
                Iterator<Person> containerIterator = personCollection.iterator();
                Iterator<Person> listIterator = personList.iterator();
                Person containerElement = containerIterator.next();
                Person listElement = listIterator.next();
                if(this instanceof ProxyConnectionTest) {
                    assertFalse("listElement", listElement instanceof NaturalPerson);
                    assertFalse("containerElement", containerElement instanceof NaturalPerson);
                } else {
                    assertTrue("listElement", listElement instanceof NaturalPerson);
                    assertFalse("listElement", ((NaturalPerson)listElement).isRetired());
                    assertTrue("containerElement", containerElement instanceof NaturalPerson);
                    assertFalse("containerElement", ((NaturalPerson)containerElement).isRetired());
                }
                for(
                    Iterator<Person> i = personCollection.iterator();
                    i.hasNext();
                ){
                    Person p = i.next();
                    PersistenceManager m = JDOHelper.getPersistenceManager(p);
                    assertSame(
                        "Query result marshalling", 
                        persistenceManager, 
                        m
                    );
                    break; // Test at least one persistence manager
                }
            } finally {
                super.taskId = null;
            }

            // personList = personCollection.getAll(personQuery);
            personList = segment.getPerson(personQuery);
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
            {
                int people = allPeople.size();
                assertEquals(
                    "1 added by XmlImporter, 1 added with addPerson(), N_PERSONS added by addPerson()",
                    N_PERSONS + 2, 
                    people
                );
    
                List<Person> maasteer = allPeople.getAll(personQuery);
                int numberOfPersons = maasteer.size();
                assertEquals(
                    "number of persons found with SOUNDS_LIKE",
                    TEST_PERSON_COUNT + SIMILAR_NAME_COUNT,
                    numberOfPersons
                );
                this.begin();
                maasteer.clear();
                assertEquals(
                    "Container emptied",
                    0,
                    segment.getPerson().size()
                );
                this.rollback();
                allPeople = segment.getPerson();
                maasteer = allPeople.getAll(personQuery);
                {
                    assertTrue(
                        "People found with SOUNDS_LIKE: Second Last",
                        maasteer.listIterator(TEST_PERSON_COUNT + SIMILAR_NAME_COUNT - 1).hasNext()
                    );
                    assertFalse(
                        "People found with SOUNDS_LIKE: Last",
                        maasteer.listIterator(TEST_PERSON_COUNT + SIMILAR_NAME_COUNT).hasNext()
                    );
                }
                numberOfPersons = maasteer.size();
                assertEquals(
                    "number of persons found with SOUNDS_LIKE",
                    TEST_PERSON_COUNT + SIMILAR_NAME_COUNT,
                    numberOfPersons
                );
                this.begin();
                maasteer.clear();
                int remaining = segment.getPerson().size(); 
                assertEquals(
                    "container emptied",
                    0,
                    remaining
                );
                this.rollback();
            }
            //
            // CR20019185  Batching does not work for extent queries 
            //
            try {
                super.taskId = "CR20019185";
                //
                // Object Identity IS_LIKE Condition
                //
                personQuery = (PersonQuery) PersistenceHelper.newQuery(
                    persistenceManager.getExtent(Person.class),
                    segment.refMofId() + "/person/($..)"
                );
                //
                // String Feature IS_LIKE Condition
                //
                personQuery.lastName().like(
                    StringTypePredicate.SOUNDS,
                    "Maasteer"
                );
                //
                // Reference IS_LIKE Condition
                //
                personQuery.forAllAssignedAddress().elementOf(
                    PersistenceHelper.getCandidates(
                        persistenceManager.getExtent(Address.class),
                        segment.refMofId() + "/address/($..)"
                    )
                );
                List<Person> maasteer = segment.getExtent(personQuery);
                int numberOfPersons = 0;
                for(@SuppressWarnings("unused") Person p : maasteer) {
                    numberOfPersons++;
                }
                assertEquals(
                    "number of persons found with SOUNDS_LIKE",
                    TEST_PERSON_COUNT + SIMILAR_NAME_COUNT,
                    numberOfPersons
                );
                
            } finally {
                super.taskId = null;
            }
            
            //
            // find persons with assigned address
            //
            try {
                super.taskId = "CR20019482";
                personQuery = app1Package.createPersonQuery();
                personQuery.thereExistsAssignedAddress().equalTo(postalAddress);
                personCollection = segment.getPerson();
                FetchPlan fetchPlan = ((Query)personQuery).getFetchPlan(); 
                fetchPlan.setGroup(FetchPlan.ALL);
                fetchPlan.setFetchSize(47);
                for(Person p : personCollection.getAll(personQuery)) {
                    SysLog.trace("person", p);
                }
            } finally {
                super.taskId = null;
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
            try {
                super.taskId = "CR0003454";
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
            } finally {
                super.taskId = null;
            }
            //
            // modify given name
            //
            try {
                super.taskId = "CR20019430";
                this.begin();
                assertFalse("Pre-modify state", JDOHelper.isDirty(person));
                person.getGivenName().clear();
                assertTrue("Pre-flush state", JDOHelper.isDirty(person));
                assertTrue("pre-flush attribute retrieval", person.getGivenName().isEmpty());
                this.entityManager.flush();
                assertTrue("Post-flush state", JDOHelper.isDirty(person));
                assertTrue("Post-flush attribute retrieval", person.getGivenName().isEmpty());
                person.setGivenName("Heiri");
                person.setLastName("Imhof");
                person.setForeignId("HI");
                person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "20000401"));
                person.setBirthdateAsDateTime(Datatypes.create(Date.class, "20000401T120000.000Z"));
                person.setSalutation("Herr");
                this.commit();
                assertEquals("givenName", Collections.singletonList("Heiri"), person.getGivenName());
                assertFalse("Post-commit state", JDOHelper.isDirty(person));
            } finally {
                super.taskId = null;
            }
            //
            // keep given name
            //
            try {
                super.taskId = "CR20019472";
                this.begin();
                List<String> givenName = person.getGivenName();
                givenName.set(0, new String(givenName.get(0)));
                assertTrue("Phantom modification", JDOHelper.isDirty(person));
                this.commit();
            } finally {
                super.taskId = null;
            }
            //
            // Empty given name
            //
            if(!(this instanceof AbstractContainerManagedTransactionTest)) try {
                super.taskId = "CR20019967";
	            assertFalse("A givenName", person.getGivenName().isEmpty());
                PersistenceManager anotherManager = newEntityManagerFactory().getPersistenceManager();
	            try {
	                Person samePerson = anotherManager.getObjectById(Person.class, person.refMofId()); 
		            anotherManager.currentTransaction().begin();
		            samePerson.getGivenName().clear();
		            assertTrue("No givenName", samePerson.getGivenName().isEmpty());
		            anotherManager.currentTransaction().commit();
		            assertTrue("No givenName", samePerson.getGivenName().isEmpty());
		            anotherManager.refresh(samePerson);
		            assertTrue("No givenName", samePerson.getGivenName().isEmpty());
	            } finally {
	            	if(anotherManager.currentTransaction().isActive()) {
	            		anotherManager.currentTransaction().rollback();
	            	}
	            	anotherManager.close();
	            }
                anotherManager = newEntityManagerFactory().getPersistenceManager();
	            try {
	                Person samePerson = anotherManager.getObjectById(Person.class, person.refMofId()); 
		            assertTrue("No givenName", samePerson.getGivenName().isEmpty());
	            } finally {
	            	if(anotherManager.currentTransaction().isActive()) {
	            		anotherManager.currentTransaction().rollback();
	            	}
	            	anotherManager.close();
	            }
	            this.entityManager.refresh(person);
	            assertTrue("No givenName", person.getGivenName().isEmpty());
            } finally {
                super.taskId = null;
            }
            // person.formatAs
            this.begin(); // isQuery() is false      
            PersonFormatNameAsParams personFormatNameAsParams;
            switch(this.nextStructureCreation()) {
                case BY_MEMBER:
                    personFormatNameAsParams = Datatypes.create(
                        PersonFormatNameAsParams.class, 
                        Datatypes.member(PersonFormatNameAsParams.Member.type, "Standard")
                    );
                    break;
                case BY_PACKAGE:
                    personFormatNameAsParams = app1Package.createPersonFormatNameAsParams("Standard");
                    break;
                case BY_POSITION:
                    personFormatNameAsParams = Datatypes.create(
                        PersonFormatNameAsParams.class, 
                        "Standard"
                    );
                    break;
                default:
                    personFormatNameAsParams = null;
                
            }
            PersonFormatNameAsResult formattedName = person.formatNameAs(personFormatNameAsParams);
            this.commit(); // result available after commit only               
            System.out.println("formatted name=" + formattedName.getFormattedName());
            System.out.println("formatted name as set=" + formattedName.getFormattedNameAsSet());
            System.out.println("formatted name as list=" + formattedName.getFormattedNameAsList());
            System.out.println("formatted name as sparsearray=" + formattedName.getFormattedNameAsSparseArray());

            // test optional argument
            this.begin(); // isQuery() is false               
            switch(this.nextStructureCreation()) {
                case BY_MEMBER:
                    personFormatNameAsParams = Datatypes.create(
                        PersonFormatNameAsParams.class, 
                        Datatypes.member(PersonFormatNameAsParams.Member.type, null)
                    );
                    break;
                case BY_PACKAGE:
                    personFormatNameAsParams = app1Package.createPersonFormatNameAsParams(
                        null // default value is Standard
                    );
                    break;
                case BY_POSITION:
                    personFormatNameAsParams = Datatypes.create(
                        PersonFormatNameAsParams.class, 
                        (String)null
                    );
                    break;
                default:
                    personFormatNameAsParams = null;
            }
            formattedName = person.formatNameAs(personFormatNameAsParams);
            this.commit(); // result available after commit only               
            System.out.println("formatted name=" + formattedName.getFormattedName());
            try {
                switch(this.nextStructureCreation()) {
                    case BY_MEMBER:
                        personFormatNameAsParams = Datatypes.create(
                            PersonFormatNameAsParams.class, 
                            Datatypes.member(PersonFormatNameAsParams.Member.type, "InvalidFormat")
                        );
                        break;
                    case BY_PACKAGE:
                        personFormatNameAsParams = app1Package.createPersonFormatNameAsParams(
                            "InvalidFormat"
                        );
                        break;
                    case BY_POSITION:
                        personFormatNameAsParams = Datatypes.create(
                            PersonFormatNameAsParams.class, 
                            "InvalidFormat"
                        );
                        break;
                    default:
                        personFormatNameAsParams = null;
                }
                person.formatNameAs(
                    app1Package.createPersonFormatNameAsParams(
                        "InvalidFormat"
                    )
                );
                fail("CanNotFormatNameException expected");
            } catch(CanNotFormatNameException e) {
                e.toString();
                System.out.println("formatNameAs() raised exception as expected \n" + e.getMessage());
            }
            
            try {
                super.taskId = "CR20019666";
                throw new CanNotFormatNameException(
                    super.taskId
                );
            } catch(CanNotFormatNameException e) {
                e.toString();
                System.out.println("TestMain raised exception as expected \n" + e.getMessage());
            } finally {
                super.taskId = null;
            }
            
            // test dateOp (date and dateTime in operation parameter)
            // Test for non-query operation with result 
            this.begin();
            Date dateTimeNow = new Date();
            XMLGregorianCalendar dateIn = Datatypes.create(
                XMLGregorianCalendar.class, 
                DateTimeFormat.BASIC_UTC_FORMAT.format(dateTimeNow).substring(0, 8)
            );
            PersonDateOpParams personDateOpParams;
            switch(nextStructureCreation()) {
                case BY_MEMBER:
                    personDateOpParams = Datatypes.create(
                        PersonDateOpParams.class,
                        Datatypes.member(PersonDateOpParams.Member.dateIn, dateIn),
                        Datatypes.member(PersonDateOpParams.Member.dateTimeIn, dateTimeNow)
                    );
                    break;
                case BY_PACKAGE:
                    personDateOpParams = app1Package.createPersonDateOpParams(
                        dateIn,
                        dateTimeNow
                    );
                    break;
                case BY_POSITION:
                    personDateOpParams = Datatypes.create(
                        PersonDateOpParams.class,
                        dateIn,
                        dateTimeNow
                    );
                    break;
                default:
                    personDateOpParams = null;
            }
            PersonDateOpResult dateOpResult = person.dateOp(personDateOpParams);
            this.commit();
            System.out.println("dateOp.dateResult=" + dateOpResult.getDateResult());
            System.out.println("dateOp.dateTimeResult=" + dateOpResult.getDateTimeResult());

            // no more NOT_FOUND exceptions
            assertNull("Not existing person", segment.getPerson("alskdjflaksdjf"));

            // remove some persons

            System.out.println("removing person=" + segment.getPerson("0001").getLastName());
            System.out.println("removing person=" + segment.getPerson("00053").getLastName());
            System.out.println("removing person=" + segment.getPerson("00082").getLastName());

            int initialPersonCount = segment.getPerson().size();
            this.begin();
            segment.getPerson(false,"0001").refDelete();
            segment.getPerson(false,"00053").refDelete();
            segment.getPerson(false,"00082").refDelete();
            int finalPersonCount = segment.getPerson().size();
            assertEquals(
                "Transient person count",
                initialPersonCount - 3,
                finalPersonCount
            );
            this.rollback();

            finalPersonCount = segment.getPerson().size();
            assertEquals(
                "Rollback person count",
                initialPersonCount,
                finalPersonCount
            );
            this.begin();
            segment.getPerson(false,"0001").refDelete();
            segment.getPerson(false,"00053").refDelete();
            segment.getPerson(false,"00082").refDelete();
            finalPersonCount = segment.getPerson().size();
            assertEquals(
                "Transient person count",
                initialPersonCount - 3,
                finalPersonCount
            );
            this.commit();
            finalPersonCount = segment.getPerson().size();
            assertEquals(
                "Commit person count",
                initialPersonCount - 3,
                finalPersonCount
            );

            // ... and test whether they are removed
            assertNull("person 0001 not removed", segment.getPerson("0001"));

            //
            // CR0003390 Code Accessor
            //
            try {
                super.taskId = "CR0003390";
                this.begin();
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
                this.rollback();

                // Add after failed get
                assertNull("person NO1 exists", segment.getPerson("NO1"));

                this.begin();
                person = personClass.createPerson();
                person.setForeignId("X1");
                person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1961-11-11"));
                person.setBirthdateAsDateTime(Datatypes.create(Date.class, "19611111T120000.000Z"));
                person.setLastName("Muster1");
                person.setSalutation("Herr");
                person.setSex((short)0);
                person.getGivenName().add("Hans");
                person.getGivenName().add("Heiri");
                person.setGivenName(new String[]{"Hans", "Heiri"});
                person.getAssignedAddress().add(postalAddress);
                segment.addPerson(false, "NO1", person);
                this.rollback();

                // ... and test whether they are removed
                assertNull("Person N01", segment.getPerson(false,"NO1"));
                segment.getPerson().remove(QualifierType.REASSIGNABLE,"NO1");

                // A non-existent person
                assertNull("Person N02", segment.getPerson(false,"NO2"));
                segment.getPerson().remove(QualifierType.REASSIGNABLE,"NO2");

                // Add after failed removal
                this.begin();
                person = personClass.createPerson();
                person.setForeignId("X2");
                person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1961-11-11"));
                person.setBirthdateAsDateTime(Datatypes.create(Date.class, "19611111T120000.000Z"));
                person.setLastName("Muster1");
                person.setSalutation("Herr");
                person.setSex((short)0);
                person.getGivenName().add("Hans");
                person.getGivenName().add("Heiri");
                person.setGivenName(new String[]{"Hans", "Heiri"});
                person.getAssignedAddress().add(postalAddress);
                segment.addPerson(false,"NO2", person);
                this.rollback();

                assertNull("person 00053 not removed", segment.getPerson("00053"));
                assertNull("person 00082 not removed", segment.getPerson("00082"));

                // postalAddress.formatAs
                AddressFormatAsResult formattedAddress = null;
                AddressFormatAsParams addressFormatAsParams;
                switch(nextStructureCreation()) {
                    case BY_MEMBER:
                        addressFormatAsParams = Datatypes.create(
                            AddressFormatAsParams.class,
                            Datatypes.member(AddressFormatAsParams.Member.type, "Standard")
                        );
                        break;
                    case BY_PACKAGE:
                        addressFormatAsParams = app1Package.createAddressFormatAsParams(
                            "Standard"
                        );
                        break;
                    case BY_POSITION:
                        addressFormatAsParams = Datatypes.create(
                            AddressFormatAsParams.class,
                            "Standard"
                        );
                        break;
                    default:
                        addressFormatAsParams = null;
                }
                formattedAddress = postalAddress.formatAs(addressFormatAsParams);
                System.out.println("formatted address=" + formattedAddress.getFormattedAddress());

                // emailAddress.formatAs
                formattedAddress = emailAddress.formatAs(addressFormatAsParams);
                System.out.println("formatted address=" + formattedAddress.getFormattedAddress());

                // get addresses by iterator
                SegmentHasAddress.Address<Address> addresses = segment.getAddress();
                for(Address address : addresses) {
                    System.out.println("address.id=" + address.getId());
                    System.out.println("address=" + address);

                    // invoke sendMessage on PostalAddress
                    if(address instanceof PostalAddress) {
                        this.begin(); // isQuery() is false
                        byte[] document =  new byte[]{'h', 'e', 'l', 'l', 'o'};
                        PostalAddressSendMessageParams postalAddressSendMessageParams;
                        switch(nextStructureCreation()) {
                            case BY_MEMBER:
                                postalAddressSendMessageParams = Datatypes.create(
                                    PostalAddressSendMessageParams.class,
                                    Datatypes.member(PostalAddressSendMessageParams.Member.document, document)
                                );
                                break;
                            case BY_PACKAGE:
                                postalAddressSendMessageParams = app1Package.createPostalAddressSendMessageParams(document);
                                break;
                            case BY_POSITION:
                                postalAddressSendMessageParams = Datatypes.create(
                                    PostalAddressSendMessageParams.class,
                                    document
                                );
                                break;
                            default:
                                postalAddressSendMessageParams = null;
                        }
                        ((PostalAddress)address).sendMessage(postalAddressSendMessageParams);
                        this.commit();
                    }
                    else if(address instanceof EmailAddress) {
                        this.begin(); // isQuery() is false   
                        EmailAddressSendMessageParams emailAddressSendMessageParams;
                        switch(nextStructureCreation()) {
                            case BY_MEMBER:
                                emailAddressSendMessageParams = Datatypes.create(
                                    EmailAddressSendMessageParams.class,
                                    Datatypes.member(EmailAddressSendMessageParams.Member.text, "hello")
                                );
                                break;
                            case BY_PACKAGE:
                                emailAddressSendMessageParams = app1Package.createEmailAddressSendMessageParams(
                                    "hello"
                                );
                                break;
                            case BY_POSITION:
                                emailAddressSendMessageParams = Datatypes.create(
                                    EmailAddressSendMessageParams.class,
                                    "hello"
                                );
                                break;
                            default:
                                emailAddressSendMessageParams = null;
                        }
                        ((EmailAddress)address).sendMessage(emailAddressSendMessageParams);
                        this.commit();
                    }
                    else {
                        fail("address format " + address.getClass().getName() + " unknown");
                    }
                }
            } finally {
                super.taskId = null;
            }
            
            try {
                super.taskId = "CR20019669";
                SegmentHasAddress.Address<Address> addresses = segment.getAddress();
                assertTrue(
                    "Persistence Capable Container",
                    addresses instanceof PersistenceCapable
                );
                assertEquals(
                    "Container Id", 
                    ((Path)JDOHelper.getObjectId(segment)).getChild("address"), 
                    JDOHelper.getObjectId(addresses)
                );
                PersistenceManager manager = JDOHelper.getPersistenceManager(addresses);
                assertSame(
                    "Container Persistence Manager", 
                    JDOHelper.getPersistenceManager(segment), 
                    manager
                );
            } finally {
                super.taskId = null;
            }

            try {
                super.taskId = "CR20019669";
                SegmentHasAddress.Address<Address> addresses = segment.getAddress();
                PersistenceManager manager = JDOHelper.getPersistenceManager(addresses);
                Object addressId = JDOHelper.getObjectId(addresses);
                String xri = ((Path)addressId).toXRI();
                assertEquals(
                    "Validating a RefContainer's string representation",
                    SegmentHasAddress.Address.class.getName() + ": " + xri, 
                    addresses.toString()
                );
                Object transientAddressId = JDOHelper.getTransactionalObjectId(addresses);
                assertTrue("Transient container id", transientAddressId instanceof TransientContainerId);
                {
                    RefBaseObject container =  (RefBaseObject) manager.getObjectById(transientAddressId);
                    assertTrue("The container's JMI class", container instanceof SegmentHasAddress.Address<?>);
                    assertEquals(addresses, container);
                    assertSame("The container's manager", manager, JDOHelper.getPersistenceManager(container));
                    assertEquals("The container's id", xri, container.refMofId());
                }
                {
                    RefBaseObject container =  (RefBaseObject) manager.getObjectById(addressId);
                    assertTrue("The container's JMI class", container instanceof SegmentHasAddress.Address<?>);
                    assertEquals(addresses, container);
                    assertEquals("The container's id", xri, container.refMofId());
                    assertSame("The container's manager", manager, JDOHelper.getPersistenceManager(container));
                }
            } finally {
                super.taskId = null;
            }
            
            try {
                super.taskId = "CR20019719";
                personQuery = app1Package.createPersonQuery();
                personQuery.createdAt().equalTo(new Date());
                List<Person> people = segment.<Person>getPerson().getAll(personQuery);
                assertTrue(people.isEmpty());
            } finally {
                super.taskId = null;
            }
            
            {
                //
                // test cycles
                //
                this.begin();
                CycleMember1 member1 = cycleMember1Class.createCycleMember1();
                member1.setDescription("this is member1");
                CycleMember2 member2 = cycleMember2Class.createCycleMember2();
                member2.setDescription("this is member2");
                member1.setM2(member2);
                member2.setM1(member1);
                segment.addCycleMember1(false, new BigDecimal(1), member1);
                segment.addCycleMember2(false, "member2", member2);
                this.commit();
    
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
            }

            // test streams
            this.begin();

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
                BinaryLargeObjects.valueOf(content)
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
            this.commit();

            // verify returned document
            document = segment.getDocument("myDoc");
            System.out.println("document.description=" + document.getDescription());
            System.out.println("document.keyword=" + document.getKeyword());
            BinaryLargeObject contentLo = document.getContent();
            {
                assertNotNull("BLOB", contentLo);
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
            }
            {
                //
                // Modify content
                //
                this.begin();
                for(
                        int i = 0;
                        i < contentLength;
                        i++
                ) {
                    content[i] = (byte)((short)(i % 137));
                }
                document.setContent(
                    BinaryLargeObjects.valueOf(content)
                );
                this.commit();
            }
            {
                contentLo = document.getContent();
                assertNotNull("BLOB", contentLo);
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
            //
            // CR20018821
            //
            try {
                super.taskId = "CR20018821";
                this.begin();
                Importer.importObjects(
                    Importer.asTarget(persistenceManager),
                    Importer.asSource(
                        new URL("xri://+resource/test/openmdx/app1/data.xml")
                    )
                );
                if(this instanceof LocalConnectionTest) {
	                //
	                // CR20019858
	                //
	                UpdateAvoidance updateAvoidance = UserObjects.getPlugInObject(this.entityManager, UpdateAvoidance.class);
	                assertNotNull(UpdateAvoidance.class.getSimpleName(), updateAvoidance);
	                updateAvoidance.touchAllDirtyObjects(this.entityManager.currentTransaction());
                }
                this.commit();
                File file = File.createTempFile("data", ".zip");
                Exporter.export(
                    Exporter.asTarget(file, Exporter.MIME_TYPE_XML),
                    persistenceManager,
                    null,
                    segment.refGetPath()
                );
                System.out.println(segment.refGetPath().toXRI() + " exported to " + file);
                //
                // Validate date-time values
                //
                person = (Person) persistenceManager.getObjectById(personId);
                assertEquals(
                    "Birthdate as date/time", 
                    Datatypes.create(Date.class, "1960-01-01T12:00:00Z"), 
                    person.getBirthdateAsDateTime()
                );
            } finally {
                super.taskId = null;
            }
            
            //
            // CR20018889
            //
            try {
                super.taskId = "CR20018889";
                File file = File.createTempFile("CR20018889.", null);
                int cardinality;
                {
                    Collection<test.openmdx.app1.cci2.Person> jmiPeople = segment.getPerson();
                    Collection<test.openmdx.app1.cci2.Person> jpaPeople = persistenceManager.detachCopyAll(jmiPeople);
                    Collection<test.openmdx.app1.cci2.Address> jpaAddresses = new ArrayList<test.openmdx.app1.cci2.Address>();
                    cardinality = jpaPeople.size();
                    assertEquals("people", cardinality, jmiPeople.size());
                    assertTrue("Implementation detail", jpaPeople instanceof ArrayList<?>);
                    for(test.openmdx.app1.cci2.Person p : jpaPeople) {
                        assertFalse("deleted", JDOHelper.isDeleted(p));
                        assertTrue("detached", JDOHelper.isDetached(p));
                        assertFalse("dirty", JDOHelper.isDirty(p));
                        assertFalse("new", JDOHelper.isNew(p));
                        assertFalse("persistent", JDOHelper.isPersistent(p));
                        assertFalse("transactional", JDOHelper.isTransactional(p));
                        if("F10".equals(p.getForeignId())) {
                            test.openmdx.app1.jpa3.Person jpaPersion = (test.openmdx.app1.jpa3.Person) p;
                            test.openmdx.app1.jpa3.EmailAddress jpaAddress = new test.openmdx.app1.jpa3.EmailAddress();
                            StateAccessor.getInstance().setTransactionalObjectId(
                                jpaAddress, 
                                segment.refGetPath().getDescendant("address", "NoReply10")
                            );
                            String jpaAddress_Id = (String)JDOHelper.getObjectId(jpaAddress);
                            jpaAddress.setAddress("noreply@openmdx.org");
                            jpaAddresses.add(jpaAddress);
                            jpaPersion.getGivenName().set(1, "Heinrich");
                            jpaPersion.getAssignedAddress_Id().add(jpaAddress_Id);
                            assertFalse("deleted", JDOHelper.isDeleted(jpaPersion));
                            assertTrue("detached", JDOHelper.isDetached(jpaPersion));
                            assertTrue("dirty", JDOHelper.isDirty(jpaPersion));
                            assertFalse("new", JDOHelper.isNew(jpaPersion));
                            assertFalse("persistent", JDOHelper.isPersistent(jpaPersion));
                            assertFalse("transactional", JDOHelper.isTransactional(jpaPersion));
                        }
                    }
                    test.openmdx.app1.cci2.Document jpaDocument = persistenceManager.detachCopy(
                        segment.getDocument("myDoc")
                    );
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                    objectOutputStream.writeObject(jpaPeople);
                    objectOutputStream.writeObject(jpaAddresses);
                    objectOutputStream.writeObject(jpaDocument);
                    objectOutputStream.close();
                }
                {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                    @SuppressWarnings("unchecked")
					Collection<test.openmdx.app1.cci2.Person> jpaPeople = (Collection<test.openmdx.app1.cci2.Person>) objectInputStream.readObject();
                    @SuppressWarnings("unchecked")
					Collection<test.openmdx.app1.cci2.Address> jpaAddresses = (Collection<test.openmdx.app1.cci2.Address>) objectInputStream.readObject();
                    test.openmdx.app1.cci2.Document jpaDocument = (test.openmdx.app1.cci2.Document) objectInputStream.readObject();
                    assertEquals("people", cardinality, jpaPeople.size());
                    assertEquals("addresses", 1, jpaAddresses.size());
                    objectInputStream.close();
                    this.begin();
                    for(test.openmdx.app1.cci2.Address jpaAddress : jpaAddresses) {
                        assertFalse("deleted", JDOHelper.isDeleted(jpaAddress));
                        assertFalse("detached", JDOHelper.isDetached(jpaAddress));
                        assertFalse("dirty", JDOHelper.isDirty(jpaAddress));
                        assertFalse("new", JDOHelper.isNew(jpaAddress));
                        assertFalse("persistent", JDOHelper.isPersistent(jpaAddress));
                        assertFalse("transactional", JDOHelper.isTransactional(jpaAddress));
                        test.openmdx.app1.cci2.Address jmiAddress = persistenceManager.makePersistent(jpaAddress);
                        assertFalse("deleted", JDOHelper.isDeleted(jmiAddress));
                        assertFalse("detached", JDOHelper.isDetached(jmiAddress));
                        assertTrue("dirty", JDOHelper.isDirty(jmiAddress));
                        assertTrue("new", JDOHelper.isNew(jmiAddress));
                        assertTrue("persistent", JDOHelper.isPersistent(jmiAddress));
                        assertTrue("transactional", JDOHelper.isTransactional(jmiAddress));
                    }
                    for(test.openmdx.app1.cci2.Person jpaPerson : jpaPeople) {
                        boolean dirty = "F10".equals(jpaPerson.getForeignId()); 
                        assertFalse("deleted", JDOHelper.isDeleted(jpaPerson));
                        assertTrue("detached", JDOHelper.isDetached(jpaPerson));
                        assertEquals("dirty", dirty, JDOHelper.isDirty(jpaPerson));
                        assertFalse("new", JDOHelper.isNew(jpaPerson));
                        assertFalse("new", JDOHelper.isPersistent(jpaPerson));
                        assertFalse("transactional", JDOHelper.isTransactional(jpaPerson));
                        test.openmdx.app1.cci2.Person jmiPerson = persistenceManager.makePersistent(jpaPerson);
                        assertFalse("deleted", JDOHelper.isDeleted(jmiPerson));
                        assertFalse("detached", JDOHelper.isDetached(jmiPerson));
                        assertEquals("dirty", dirty, JDOHelper.isDirty(jmiPerson));
                        assertFalse("new", JDOHelper.isNew(jmiPerson));
                        assertTrue("persistent", JDOHelper.isPersistent(jmiPerson));
                        assertEquals("transactional", dirty, JDOHelper.isTransactional(jmiPerson));
                        if(dirty) assertEquals("Middle Name", "Heinrich", jmiPerson.getGivenName().get(1));
                    }
                    {
                        contentLo = jpaDocument.getContent();
                        assertNotNull("BLOB", contentLo);
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
                    this.commit();
                }
            } finally {
                super.taskId = null;
            }
            // 
            // Test CR20019184
            // 
            try {
                super.taskId = "CR20019184";
                this.begin();
                postalAddress = postalAddressClass.createInternationalPostalAddress();
                postalAddress.setCountry("Suisse");
                postalAddress.setCity("Gen\u00E8ve");
                postalAddress.setHouseNumber("10");
                postalAddress.setPostalCode("1201");
                postalAddress.setStreet("rue Voltaire");
                postalAddress.setAddressLine("Hotel", "Ibis Gen\u00E8ve Centre Gare");
                segment.addAddress(false, "lost+found", postalAddress);
                this.commit();
            } finally {
                super.taskId = null;
            }
            
        }

        /**
         * Test the audit entries
         * 
         * @param run the number of completed runs
         */
        protected void testAudit(
            int run
        ){
            Person person = (Person) super.entityManager.getObjectById(
                super.entityManager.newObjectIdInstance(
                    Person.class, 
                    DATA_SEGMENT_ID.getDescendant("person","ID500012")
                )
            );
            Invoice invoice = (Invoice) super.entityManager.getObjectById(
                super.entityManager.newObjectIdInstance(
                    Invoice.class,
                    DATA_SEGMENT_ID.getDescendant("invoice","CR20019372")
               )
            );
            //
            // Touch
            //
            super.taskId = "CR20018820";
            this.begin();
            person.setGivenName("Rainer Maria");
            this.commit();
            //
            // By Task Id
            //
            Collection<UnitOfWork> task = AuditQueries.getUnitOfWorkBelongingToTask(super.entityManager, "CR20018578"); 
            assertEquals("Size of task CR20018578", 1 * run, task.size());
            task = AuditQueries.getUnitOfWorkBelongingToTask(super.entityManager, "CR0002096"); 
            assertEquals("Size of task CR0002096", 2 * run, task.size());
            dumpTask("CR0002096", task);
            //
            // By Object
            //
            testAudit(person, run, false);
            testAudit(person, run, true);
            //
            // With Children
            //
            testAudit(invoice, run, false);
            testAudit(invoice, run, true);
        }

        private void testAudit(
            Invoice invoice,
            int run,
            boolean scoped
        ){
            int factor = scoped ? 1 : run;
            int create = SharedObjects.getPlugInObject(this.entityManager, Configuration.class).getPersistenceMode() == InvolvementPersistence.EMBEDDED ? 0 : 1;
            String scope = scoped ? " (run " + run + ")" : " (run 1.." + run + ")";
            Date from = scoped ? super.getStart() : null;
            Collection<UnitOfWork> task = AuditQueries.getUnitOfWorkInvolvingObject(from, null, invoice); 
            String id = "Involve InvoicePosition CR20019372.";            
            dumpTask(id + scope, task);
            assertEquals(id + scope, (1 * create + 1) * factor, task.size());
            id = "Involve InvoicePosition... CR20019372.";            
            Collection<ExtentCapable> tree = PersistenceHelper.getCandidates(
                this.entityManager.getExtent(ExtentCapable.class, true),
                invoice.refMofId() + "/($...)"
            );
            task = AuditQueries.getUnitOfWorkInvolvingObject(from, null, tree); 
            assertEquals(id + scope, (1 * create + 1) * factor, task.size());
            for(UnitOfWork unitOfWork : task) {
                for(Iterator<Involvement> i = unitOfWork.<Involvement>getInvolvement().iterator(); i.hasNext();) {
                    Involvement involvement = i.next();
                    Modifiable modifiable = involvement.getBeforeImage();
                    System.out.println(modifiable.refMofId());
                }
            }
        }
        
        private void testAudit(
            Person person,
            int run,
            boolean scoped
        ){
            int factor = scoped ? 1 : run;
            int create = SharedObjects.getPlugInObject(this.entityManager, Configuration.class).getPersistenceMode() == InvolvementPersistence.EMBEDDED ? 0 : 1;
            String scope = scoped ? " (run " + run + ")" : " (run 1.." + run + ")";
            Date from = scoped ? super.getStart() : null;
            Collection<UnitOfWork> task = AuditQueries.getUnitOfWorkInvolvingObject(from, null, person); 
            String id = "involve Person # ID500012";            
            dumpTask(id + scope, task);
            assertEquals(id + scope, (1 * create + 4) * factor, task.size());
            if(scoped || run == 1) {
                Modifiable lastImage = null;
                for(UnitOfWork unitOfWork : task){
                    Involvement involvement = unitOfWork.getInvolvement(
                        person.refGetPath().toString()
                    );
                    assertNotNull(id, involvement);
                    if(lastImage != null) {
                        assertSame(id, lastImage, involvement.getBeforeImage());
                    }
                    lastImage = involvement.getAfterImage();
                }
                assertNotNull(id, lastImage);
            }
            if(create == 0) {
                try {
                    task = AuditQueries.getUnitOfWorkCreatingObject(from, null, person); 
                    fail("Create not audited in EMBEDDED mode");
                } catch (UnsupportedOperationException exception) {
                    // Create not audited in audit1 mode
                }
            } else {
                id = "create Person # ID500012"; 
                task = AuditQueries.getUnitOfWorkCreatingObject(from, null, person); 
                dumpTask(id + scope, task);
                assertEquals(id + scope, (1 * create + 0) * factor, task.size());
            }
            if(run == 1) {
                id = "touch Person # ID500012"; 
                task = AuditQueries.getUnitOfWorkTouchingObject(from, null, null, person ); 
                dumpTask(id + scope, task);
                assertEquals(id + scope, (0 * create + 4) * factor, task.size());
                id = "touch specific attributes of Person # ID500012"; 
                task = AuditQueries.getUnitOfWorkTouchingObject(
                    from, 
                    null, 
                    Sets.asSet(
                        Arrays.asList(
                            "test:openmdx:app1:Person:assignedAddress",
                            "test:openmdx:app1:Person:additionalInfo"
                        )
                    ), 
                    person
                );
                dumpTask(id + scope, task);
                assertEquals(id + scope, (0 * create + 3) * factor, task.size());
            }
            if(scoped || run == 1) {
                id = "remove Person # ID500012";
                task = AuditQueries.getUnitOfWorkRemovingObject(from, null, person); 
                dumpTask(id + scope, task);
                assertEquals(id + scope, 0 * factor, task.size());
            }
            id = "all units of work"; 
            task = AuditQueries.getUnitOfWorkForTimeRange(super.entityManager, from, null);
            dumpTask(id + scope, task);
            assertEquals(id + scope, (16 * create + 17) * factor, task.size());
            id = "units of work involving people"; 
            task = AuditQueries.getUnitOfWorkInvolvingObject(
                from, 
                null, 
                PersistenceHelper.getCandidates(
                    super.entityManager.getExtent(Person.class),
                    DATA_SEGMENT_ID.getDescendant("person", "%")
                )
            );
            dumpTask(id + scope, task);
            assertEquals(id + scope, (3 * create + 8) * factor, task.size());
            id = "units of work involving deleted object";
            Path addressId = DATA_SEGMENT_ID.getDescendant("address","CR0002096"); 
            task = AuditQueries.getUnitOfWorkInvolvingObject(
                from, 
                null, 
                PersistenceHelper.getCandidates(
                    super.entityManager.getExtent(Person.class),
                    addressId.toXRI()
                )
            );
            dumpTask(id + scope, task);
            assertEquals(id + scope, (1 * create + 1) * factor, task.size());
            id = "units of work deleting object";
            task = AuditQueries.getUnitOfWorkRemovingObject(
                from, 
                null, 
                PersistenceHelper.getCandidates(
                    super.entityManager.getExtent(Person.class),
                    addressId.toXRI()
                )
            );
            dumpTask(id + scope, task);
            assertEquals(id + scope, (0 * create + 1) * factor, task.size());
            for(UnitOfWork unitOfWork : task) {
                for(Involvement involvement : unitOfWork.<Involvement>getInvolvement()) {
                    assertNull(id + scope, involvement.getAfterImage());
                }
            }
        }

        private void dumpTask(
            String title,
            Collection<UnitOfWork> task
        ){
            if(DUMP) {
                System.out.println(title);
                for(UnitOfWork unitOfWork : task){
                    System.out.println("\t" + unitOfWork.getUnitOfWorkId() + " (" + DateTimeFormat.EXTENDED_UTC_FORMAT.format(unitOfWork.getCreatedAt()) + ")");
                    System.out.println("\t\tinvolvement");
                    for(Involvement involvement : unitOfWork.<Involvement>getInvolvement()) {
                        System.out.println("\t\t\t" + getResourceIdentifier(involvement));
                        System.out.println(
                            "\t\t\t\t objectId = " + 
                            involvement.getObjectId()
                        );
                        System.out.println(
                            "\t\t\t\t object = " + 
                            getResourceIdentifier(involvement.getObject())
                        );
                        System.out.println(
                            "\t\t\t\t beforeImage = " + 
                            getResourceIdentifier(involvement.getBeforeImage())
                        );
                        try {
                            System.out.println(
                                "\t\t\t\t afterImage = " + 
                                getResourceIdentifier(involvement.getAfterImage())
                            );
                        } catch (Exception exception) {
                            System.out.println(
                                "\t\t\t\t afterImage is N/A: " +
                                exception.getMessage() 
                            );
                        }
                        try {
                            System.out.println(
                                "\t\t\t\t modifiedFeature = " + 
                                involvement.getModifiedFeature()
                            );
                        } catch (Exception exception) {
                            System.out.println(
                                "\t\t\t\t modifiedFeature is N/A: " +
                                exception.getMessage() 
                            );
                        }
                    }
                }
            }
        }
        
        protected static String getResourceIdentifier(
            Object pc
        ){
            return 
                pc == null ? null:
                JDOHelper.isPersistent(pc) ? ((Path)JDOHelper.getObjectId(pc)).toXRI() :
                JDOHelper.getTransactionalObjectId(pc).toString();
        }
        
        protected void testInMemoryProvider() throws ServiceException, ParseException {
            PersonClass personClass = getPackage().getPerson();
            test.openmdx.app1.jmi1.Segment segment = getSegment();
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
                this.begin();
                Person person = personClass.createPerson();
                person.setForeignId("F" + i);
                person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1960-01-01"));
                person.setBirthdateAsDateTime(Datatypes.create(Date.class, "19600101T120000.000Z"));
                person.setLastName("Muster" + i);
                person.setSalutation("Herr");
                person.setSex((short)0);
                person.setGivenName(new String[]{"Hans", "Heiri"});
                person.getAssignedAddress().add(postalAddress);
                person.getPersonGroup().add(g0);
                person.getPersonGroup().add(g1);
                person.getPersonGroup().add(g2);
                segment.addPerson(false,"L" + (1000000 + i), person);
                this.commit();
            }

            // Retrieve persons
            int ii = 0;
            int limit = 1000000;
            Runtime runtime = Runtime.getRuntime();
            long initialMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
            System.out.println("initial memory usage " + initialMemoryUsage);
            test.openmdx.app1.cci2.SegmentHasPerson.Person<Person> allPeople = segment.getPerson();
            for(@SuppressWarnings("unused") Person pers : allPeople) {
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
            }
        }

        protected test.openmdx.app1.jmi1.Segment getSegment(
        ) throws ServiceException{
            return (test.openmdx.app1.jmi1.Segment) super.entityManager.getObjectById(
                DATA_SEGMENT_ID
            );
        }

        protected App1Package getPackage() throws ServiceException{
            return (App1Package) (
                    (RefObject)super.entityManager.newInstance(test.openmdx.app1.jmi1.Segment.class)
            ).refImmediatePackage();
        }

        
        //--------------------------------------------------------------------
        // Enum StructureCreation
        //--------------------------------------------------------------------

        /**
         * Structure Creation Mode 
         */
        protected static enum StructureCreation {
            BY_PACKAGE, BY_POSITION, BY_MEMBER;
        }
        
    }

    /**
     * 1st Run
     */
    public static class LocalConnectionTest extends RepeatableTest {

        @Test
        public void run(
        ) throws Exception{
//          super.testInMemoryProvider();
            super.resetAuditSegment();
            super.resetDataSegment();
            super.testCR20019917();
            super.testCR20019462();
            super.testCR20018726();
            super.testCR20019014();
            super.testMain();
            super.testAudit(1);
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class AbstractContainerManagedTransactionTest
    //------------------------------------------------------------------------

    /**
     * 3rd Run
     */
    public static abstract class AbstractContainerManagedTransactionTest extends LocalConnectionTest {

        /**
         * Constructor 
         */
        protected AbstractContainerManagedTransactionTest() {
            super();
        }

        private UserTransaction userTransaction;
        
        /**
         * Defines whether the unit of work is optimistic or pessimistic
         * 
         * @return <code>true</code> if the unit of work is optimistic
         */
        protected abstract Boolean isOptimistic();
                
        /* (non-Javadoc)
         * @see test.openmdx.app1.TestMain.AbstractTest#configuration()
         */
        @Override
        protected Map<String, Object> configuration() {
            Map<String,Object> amendment = super.configuration();
            amendment.put(
                ConfigurableProperty.TransactionType.qualifiedName(), 
                Constants.JTA
            );
            amendment.put(
                ConfigurableProperty.ContainerManaged.qualifiedName(),
                Boolean.TRUE.toString()
            );
            amendment.put(
                ConfigurableProperty.Optimistic.qualifiedName(),
                this.isOptimistic().toString()
            );
            return amendment;
        }

        @Override
        public void run(
        ) throws Exception{
            this.userTransaction = ComponentEnvironment.lookup(
                UserTransaction.class
            );
            this.userTransaction.begin();
            super.resetAuditSegment();
            super.resetDataSegment();
            super.testCR20019462();
            super.testCR20018726();
            super.testCR20019014();
            super.testMain();
            this.userTransaction.rollback();
        }

        /* (non-Javadoc)
         * @see test.openmdx.app1.TestMain.AbstractTest#begin()
         */
        @Override
        protected void begin() {
            try {
                if(this.userTransaction.getStatus() == Status.STATUS_ACTIVE) {
                    this.userTransaction.rollback();
                }
                this.userTransaction.begin();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
            ((Synchronization_2_0)entityManager.currentTransaction()).afterBegin();
        }

        /* (non-Javadoc)
         * @see test.openmdx.app1.TestMain.AbstractTest#commit()
         */
        @Override
        protected void commit() {
            try {
                try {
                    ((Synchronization_2_0)entityManager.currentTransaction()).beforeCompletion();
                } catch (JDOException commitException) {
                    try {
                        this.userTransaction.rollback();
                    } catch (Exception rollbackException) {
                        throw new JDOFatalDataStoreException(
                            "Container managed rollback failed",
                            rollbackException
                        );
                    } finally {
                        ((Synchronization_2_0)entityManager.currentTransaction()).afterCompletion(Status.STATUS_ROLLEDBACK);
                    }
                    throw commitException;
                }
                try {
                    this.userTransaction.commit();
                } catch (Exception commitException) {
                    ((Synchronization_2_0)entityManager.currentTransaction()).afterCompletion(Status.STATUS_ROLLEDBACK);
                    throw new JDOFatalDataStoreException(
                        "Container managed commit failed",
                        commitException
                    );
                }
                ((Synchronization_2_0)entityManager.currentTransaction()).afterCompletion(Status.STATUS_COMMITTED);
            } finally {
                try {
                    this.userTransaction.begin();
                } catch (Exception exception) {
                    throw new RuntimeException(
                        "Could not start container managed read-only transaction",
                        exception
                    );
                }
            }
        }

        /* (non-Javadoc)
         * @see test.openmdx.app1.TestMain.AbstractTest#rollback()
         */
        @Override
        protected void rollback() {
            try {
                this.userTransaction.rollback();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            } finally {
                ((Synchronization_2_0)entityManager.currentTransaction()).afterCompletion(Status.STATUS_ROLLEDBACK);
            }
            try {
                this.userTransaction.begin();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }

    }

    
    //------------------------------------------------------------------------
    // Class OptimisticContainerManagedTransactionTest
    //------------------------------------------------------------------------

    /**
     * Test Optimistic Container-Managed Transactions
     */
    public static class OptimisticContainerManagedTransactionTest extends AbstractContainerManagedTransactionTest {

        /* (non-Javadoc)
         * @see test.openmdx.app1.TestMain.AbstractContainerManagedTransactionTest#isOptimistic()
         */
        @Override
        protected Boolean isOptimistic() {
            return Boolean.TRUE;
        }
        
        
    }
    

    //------------------------------------------------------------------------
    // Class PessimisticContainerManagedTransactionTest
    //------------------------------------------------------------------------

    /**
     * Test Optimistic Container-Managed Transactions
     */
    public static class PessimisticContainerManagedTransactionTest extends AbstractContainerManagedTransactionTest {

        /* (non-Javadoc)
         * @see test.openmdx.app1.TestMain.AbstractContainerManagedTransactionTest#isOptimistic()
         */
        @Override
        protected Boolean isOptimistic() {
            return Boolean.FALSE;
        }
        
        
    }
    

    //------------------------------------------------------------------------
    // Class ProxyConnectionTest
    //------------------------------------------------------------------------

    /**
     * 2nd Run
     */
    public static class ProxyConnectionTest extends RepeatableTest {

        /**
         * Tells whether a servlet connection shall be used
         * @return
         */
        protected boolean useServlet(){
            return true;
        }
        
        /* (non-Javadoc)
         * @see test.openmdx.app1.TestMain.AbstractTest#newEntityManagerFactory()
         */
        @Override
        protected PersistenceManagerFactory newEntityManagerFactory() {
            try {
                ConnectionFactory inboundConnectionFactory = useServlet() ?  new ConnectionFactoryAdapter(
                    new ServletPort(
                        Collections.singletonMap(
                            "entity-manager-factory-name",
                            "jdo:test-Main-EntityManagerFactory"
                        )
                    ),
                    true, // supportsLocalTransactionDemarcation
                    TransactionAttributeType.NEVER
                ) : InboundConnectionFactory_2.newInstance(
                    "jdo:test-Main-EntityManagerFactory"
                );
                Map<String,Object> dataManagerProxyConfiguration = new HashMap<String,Object>();
                dataManagerProxyConfiguration.put(
                    ConfigurableProperty.ConnectionFactory.qualifiedName(),
                    inboundConnectionFactory
                );
                dataManagerProxyConfiguration.put(
                    ConfigurableProperty.PersistenceManagerFactoryClass.qualifiedName(),
                    EntityManagerProxyFactory_2.class.getName()
                );    
                PersistenceManagerFactory outboundConnectionFactory = JDOHelper.getPersistenceManagerFactory(
                    dataManagerProxyConfiguration
                );
    
                Map<String,Object> entityManagerConfiguration = new HashMap<String,Object>();
                entityManagerConfiguration.put(
                    ConfigurableProperty.ConnectionFactory.qualifiedName(),
                    outboundConnectionFactory
                );
                entityManagerConfiguration.put(
                    ConfigurableProperty.PersistenceManagerFactoryClass.qualifiedName(),
                    EntityManagerFactory_1.class.getName()
                );    
                return JDOHelper.getPersistenceManagerFactory(
                    entityManagerConfiguration
                );
            } catch (ServletException exception) {
                throw new JDOFatalDataStoreException("Unable to provide proxy persistence manager factory", exception);
            }
        }

        @Test
        public void run(
        ) throws Exception{
            super.resetDataSegment();
            super.testPackageAcquisition();
            super.testCR20019462();
            super.testCR20018800();
            super.testMain();
// TODO     super.testAudit(2);
        }

    }

}

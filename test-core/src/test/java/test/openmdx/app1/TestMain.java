/*
 * ====================================================================
 * Project:     openMDX/Test Core, http://www.openmdx.org/
 * Description: Test Main 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
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
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jdo.Constants;
import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.JDOOptimisticVerificationException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.InvalidCallException;
import javax.jmi.reflect.InvalidObjectException;
import javax.jmi.reflect.RefBaseObject;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
#if JAVA_8
import javax.resource.spi.CommException;
import javax.servlet.ServletException;
import javax.transaction.UserTransaction;
#else
import jakarta.resource.spi.CommException;
import jakarta.servlet.ServletException;
import jakarta.transaction.UserTransaction;
#endif
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.application.rest.adapter.InboundConnectionFactory_2;
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
import org.openmdx.base.accessor.rest.DirtyObjects;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.aop0.UpdateAvoidance;
import org.openmdx.base.cci2.ExtentCapable;
import org.openmdx.base.collection.Sets;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Modifiable;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.jmi1.Segment;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.ClassicSegments;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.resource.cci.ConnectionFactory;
import org.openmdx.base.rest.cci.ConditionRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.base.rest.connector.EntityManagerProxyFactory_2;
import org.openmdx.base.rest.spi.RestConnectionFactory;
import org.openmdx.base.transaction.Status;
import org.openmdx.base.transaction.TransactionAttributeType;
import org.openmdx.generic1.cci2.PropertyQuery;
import org.openmdx.generic1.cci2.PropertySetHasProperties;
import org.openmdx.generic1.cci2.StringPropertyQuery;
import org.openmdx.generic1.cci2.UriPropertyQuery;
import org.openmdx.generic1.jmi1.BooleanProperty;
import org.openmdx.generic1.jmi1.DecimalProperty;
import org.openmdx.generic1.jmi1.Generic1Package;
import org.openmdx.generic1.jmi1.IntegerProperty;
import org.openmdx.generic1.jmi1.Property;
import org.openmdx.generic1.jmi1.StringProperty;
import org.openmdx.generic1.jmi1.UriProperty;
import org.openmdx.junit5.OpenmdxTestCoreStandardExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.kernel.loading.Factory;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.naming.ComponentEnvironment;
import org.openmdx.preferences2.jmi1.Entry;
import org.openmdx.preferences2.jmi1.Node;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.CharacterLargeObjects;
import org.w3c.cci2.Container;
import org.w3c.cci2.RegularExpressionFlag;
import org.w3c.cci2.SparseArray;
import org.w3c.cci2.StringTypePredicate;
import org.w3c.format.DateTimeFormat;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi.StateAccessor;
import org.w3c.spi2.Datatypes;

import test.openmdx.app1.aop2.NaturalPerson;
import test.openmdx.app1.aop2.PriceCalculator;
import test.openmdx.app1.aop2.PropagatedUserObject;
import test.openmdx.app1.cci2.AddressQuery;
import test.openmdx.app1.cci2.CycleMember1Query;
import test.openmdx.app1.cci2.EmailAddressQuery;
import test.openmdx.app1.cci2.GenericAddressQuery;
import test.openmdx.app1.cci2.InvoiceHasInvoicePosition;
import test.openmdx.app1.cci2.InvoicePositionQuery;
import test.openmdx.app1.cci2.InvoiceQuery;
import test.openmdx.app1.cci2.PersonQuery;
import test.openmdx.app1.cci2.PostalAddressQuery;
import test.openmdx.app1.cci2.ProductQuery;
import test.openmdx.app1.cci2.SegmentHasAddress;
import test.openmdx.app1.cci2.SegmentHasPerson;
import test.openmdx.app1.cci2.SegmentQuery;
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
import test.openmdx.app1.jmi1.GenericAddress;
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
import test.openmdx.app1.jmi1.TextDocument;
import test.openmdx.application.rest.http.ServletPort;
import test.openmdx.model1.jmi1.ClassContainingOperations;
import test.openmdx.model1.jmi1.Model1Package;

/**
 * Test Main
 */
public class TestMain {

	protected static final String ENTITY_MANAGER_FACTORY_NAME = "test-Main-EntityManagerFactory";
	protected static final String ENTITY_MANAGER_PROXY_FACTORY_NAME = "test-Main-EntityManagerProxyFactory";
	protected static final boolean DUMP = false;
	protected static final String AUDIT_PROVIDER_NAME = "Audit";
	protected static final String DATA_PROVIDER_NAME = "Data";
	protected static final String TRANSIENT_PROVIDER_NAME = "Transient";
	protected static final String STANDARD_SEGMENT_NAME = "Standard";

	private static final Path CONFIGURATION_SEGMENT = new Path(
			"xri://@openmdx*org:openmdx:preferences2/provider/(@openmdx!configuration)/segment/org.openmdx.jdo.DataManager");
	private static final Path CONFIGURATION_NODES = CONFIGURATION_SEGMENT.getDescendant("preferences",
			"test.openmdx.app1.Data.PlugIn", "node");

	private static final String STANDARD_FORMAT = "Standard";

	// ------------------------------------------------------------------------
	// Class AbstractTest
	// ------------------------------------------------------------------------

	/**
	 * Abstract Test
	 */
	@ExtendWith(OpenmdxTestCoreStandardExtension.class)
	public abstract static class AbstractTest {

		/**
		 * Constructor
		 */
		protected AbstractTest() {
			this.taskIdentifier = new Object() {

				/*
				 * (non-Javadoc)
				 * 
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

		protected String getProviderName() {
			return DATA_PROVIDER_NAME;
		}

		protected boolean isBackedUpByStandardDB() {
			return DATA_PROVIDER_NAME.equals(getProviderName());
		}

		protected final Path dataSegmentId = new Path("xri://@openmdx*test.openmdx.app1").getDescendant("provider",
				getProviderName(), "segment", STANDARD_SEGMENT_NAME);

		protected org.openmdx.base.persistence.spi.UnitOfWork currentUnitOfWork() {
			return (org.openmdx.base.persistence.spi.UnitOfWork) PersistenceHelper.currentUnitOfWork(entityManager);
		}

		protected abstract boolean isContainerManaged();

		@BeforeEach
		public void jdoSetUp() {
			entityManagerFactory = newEntityManagerFactory();
			if (!isContainerManaged()) {
				prolog();
			}
		}

		protected void prolog() {
			this.entityManager = entityManagerFactory.getPersistenceManager();
			this.authority = this.entityManager.getObjectById(Authority.class, App1Package.AUTHORITY_XRI);
			this.provider = authority.getProvider(false, getProviderName());
			UserObjects.setTaskIdentifier(this.entityManager, this.taskIdentifier);
		}

		@AfterEach
		public void tearDown() {
			if (this.entityManager != null) {
				try {
					this.entityManager.close();
				} catch (RuntimeException exception) {
					SysLog.warning("Tear down failure", exception);
				} finally {
					this.entityManager = null;
				}
			}
		}

		protected PersistenceManagerFactory newEntityManagerFactory() {
			PersistenceManagerFactory entityManagerFactory = ReducedJDOHelper
					.getPersistenceManagerFactory(configuration(), ENTITY_MANAGER_FACTORY_NAME);
			entityManagerFactory.getDataStoreCache().pinAll(true, AddressFormat.class);
			return entityManagerFactory;
		}

		/**
		 * Provide the configuration to be amendet by subclasses
		 * 
		 * @return the configuration used for overriding
		 */
		protected Map<String, Object> configuration() {
			return new HashMap<String, Object>();
		}

		protected void begin() {
			PersistenceHelper.currentUnitOfWork(this.entityManager).begin();
		}

		protected void commit() {
			PersistenceHelper.currentUnitOfWork(this.entityManager).commit();
		}

		protected void rollback() {
			PersistenceHelper.currentUnitOfWork(this.entityManager).rollback();
		}

		protected Date getStart() {
			return this.start;
		}

	}

	// ------------------------------------------------------------------------
	// Class RepeatableTest
	// ------------------------------------------------------------------------

	/**
	 * Abstract Tests
	 */
	public abstract static class AbstractRepeatableTest extends AbstractTest {

		/**
		 * Constructor
		 */
		protected AbstractRepeatableTest() {
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

		static private final int INSPECTION_COUNT = 250;
		static private final int MEMBER_COUNT = 9;
		static private final int N_PERSONS = 100;
		static private final int LARGE_N_PERSONS = 1000;
		static private final int TEST_PERSON_COUNT = N_PERSONS - 1; // TODO one is missing for some reason
		static private final int SIMILAR_NAME_COUNT = 3;
		private final boolean VALIDATE_PERSISTENCE_MANAGER = !(this instanceof ProxyConnectionTest); // TODO include
																										// Proxy
																										// Connections
		private final boolean REMOTE_EXCEPTIONS_ARE_GENERIC = true; // TODO CR20020019
		private final boolean PROXIED_EXTENT_IS_AMENDMENT_AWARE = false; // TODO CR20020326
		private final boolean INCOMPLETE_OBJECTS_CAN_BE_FLUSHED = false; // TODO CR20020411
		private final boolean PROXY_IS_DIRTY_COLLECTION_AWARE = false; // TODO

		protected long id;

		/**
		 * TODO
		 * 
		 * @return {@code true} if remote exception are mapped to GENERIC
		 */
		protected boolean causeIsGeneric() {
			return REMOTE_EXCEPTIONS_ARE_GENERIC && this instanceof RemoteConnectionTest;
		}

		protected boolean testConcurrentAccess() {
			return true;
		}

		/**
		 * Switch back and forth to test both variants
		 * 
		 * @return {@code true} if the package should be used to acquire
		 *         structures.
		 */
		protected StructureCreation nextStructureCreation() {
			return this.structureCreation = next(this.structureCreation);
		}

		/**
		 * Round robin
		 * 
		 * @param value the current structure creation kind
		 * 
		 * @return the next structure creation kind
		 */
		private static StructureCreation next(StructureCreation value) {
			return StructureCreation.values()[(value.ordinal() + 1) % StructureCreation.values().length];
		}

		protected String nextId() {
			return "ID" + this.id++;
		}

		protected void testCR20019014() {
			PropertyQuery query = (PropertyQuery) entityManager.newQuery(Property.class);
			PersistenceHelper.setClasses(query, IntegerProperty.class, DecimalProperty.class);
		}

		protected void testCR20018726() throws ServiceException {
			try {
				super.taskId = "CR20018726";
				test.openmdx.app1.jmi1.Segment segment = (test.openmdx.app1.jmi1.Segment) provider
						.getSegment(STANDARD_SEGMENT_NAME);
				List<Address> addresses = segment.getAddress((AddressQuery) null);
				for (Address address : addresses) {
					System.out.println("1st display of " + ReducedJDOHelper.getObjectId(address));
				}
				for (Address address : addresses) {
					System.out.println("2nd display of " + ReducedJDOHelper.getObjectId(address));
				}
			} finally {
				super.taskId = null;
			}
		}

		protected void testCR20019917() {
			try {
				super.taskId = "CR20019917";
				Object userObject = new Object();
				this.entityManager.setUserObject(userObject);
				Assertions.assertSame(userObject, this.entityManager.getUserObject(), "initial UserObject");
				this.entityManager.setUserObject(BigDecimal.ONE);
				Assertions.assertSame(BigDecimal.ONE, this.entityManager.getUserObject(), "intermediate UserObject");
				this.entityManager.setUserObject(null);
				Assertions.assertNull(this.entityManager.getUserObject(), "final UserObject");
			} finally {
				super.taskId = null;
			}
		}

		@SuppressWarnings("deprecation")
		protected void testCR20019462() throws ServiceException {
			try {
				super.taskId = "CR20019462";
				Invoice invoice = this.entityManager.newInstance(Invoice.class);
				InvoicePosition position = this.entityManager.newInstance(InvoicePosition.class);
				Container<InvoicePosition> positions = invoice.getInvoicePosition();
				Assertions.assertTrue(positions.isEmpty());
				invoice.addInvoicePosition(position);
				Assertions.assertEquals(1,  positions.size());
				positions.remove(position);
				Assertions.assertTrue(positions.isEmpty());
			} finally {
				super.taskId = null;
			}
		}

		protected void testCR20018800() {
			try {
				super.taskId = "CR20018800";
				PersistenceManager original = this.entityManager;
				List<String> originalPrinicpals = UserObjects.getPrincipalChain(original);
				PersistenceManager sibling = entityManagerFactory.getPersistenceManager();
				List<String> siblingPrinicpals = UserObjects.getPrincipalChain(sibling);
				Assertions.assertEquals(originalPrinicpals,  siblingPrinicpals, "sibling");
				PersistenceManagerFactory factory = original.getPersistenceManagerFactory();
				PersistenceManager clone = factory.getPersistenceManager();
				List<String> clonePrinicpals = UserObjects.getPrincipalChain(clone);
				Assertions.assertEquals(originalPrinicpals,  clonePrinicpals, "clone");
			} finally {
				super.taskId = null;
			}
		}

		protected void testCR20020032() throws ServiceException, IOException {
//            File target = File.createTempFile("orm", ".html");
//            ClassToTableMapping classToTableMapping = new ClassToTableMapping(ENTITY_MANAGER_FACTORY_NAME);
//            classToTableMapping.exportToHTML(target);
//            System.out.println("ORM exported to " + target);
		}

		protected void testPackageAcquisition() {
			UnitOfWork instance = this.entityManager.newInstance(UnitOfWork.class);
			Audit2Package audit2Package = (Audit2Package) instance.refImmediatePackage();
			Assertions.assertEquals("org:openmdx:audit2:audit2",  audit2Package.refMofId(), "MOF ID");
		}

		protected void resetAuditSegment() {
			Authority authority = this.entityManager.getObjectById(Authority.class, Audit2Package.AUTHORITY_XRI);
			Provider provider = authority.getProvider(false, AUDIT_PROVIDER_NAME);
			org.openmdx.audit2.jmi1.Segment segment = (org.openmdx.audit2.jmi1.Segment) provider.getSegment(false,
					STANDARD_SEGMENT_NAME);
			if (segment != null) {
				this.begin();
				segment.refDelete();
				this.commit();
			}
			segment = this.entityManager.newInstance(org.openmdx.audit2.jmi1.Segment.class);
			this.begin();
			provider.addSegment(false, STANDARD_SEGMENT_NAME, segment);
			this.commit();
		}

		protected void resetDataSegment() {
			test.openmdx.app1.jmi1.Segment segment = (test.openmdx.app1.jmi1.Segment) provider.getSegment(false,
					STANDARD_SEGMENT_NAME);
			if (segment != null) {
				this.begin();
				segment.refDelete();
				this.commit();
			}
			segment = this.entityManager.newInstance(test.openmdx.app1.jmi1.Segment.class);
			this.begin();
			try {
				super.taskId = "CR20020187";
				Path segmentId = provider.refGetPath().getDescendant("segment", STANDARD_SEGMENT_NAME);
				this.entityManager.getObjectById(segmentId);
				Assertions.fail("Segment has been deleted");
			} catch (JDOObjectNotFoundException expected) {
				// Segment has been deleted
			} finally {
				super.taskId = null;
			}
			provider.addSegment(false, STANDARD_SEGMENT_NAME, segment);
			this.commit();
		}

		class CountryChanger extends Thread {

			private CountryChanger(Path addressId, String countryName, long pause1, long pause2) {
				this.addressId = addressId;
				this.countryName = countryName;
				this.pause1 = pause1;
				this.pause2 = pause2;
			}

			private final Path addressId;
			private final String countryName;
			private Boolean committed;
			private final long pause1;
			private final long pause2;

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {
				pause(pause1);
				PersistenceManager persistenceManager = entityManager.getPersistenceManagerFactory()
						.getPersistenceManager();
				InternationalPostalAddress address = (InternationalPostalAddress) persistenceManager
						.getObjectById(addressId);
				persistenceManager.currentTransaction().begin();
				String formerCountryName = address.getCountry();
				address.setCountry(countryName);
				pause(pause2);
				try {
					persistenceManager.makeTransactional(address);
					persistenceManager.currentTransaction().commit();
					System.out.println(Thread.currentThread().getName() + " changes country from " + formerCountryName
							+ " to " + countryName);
					committed = Boolean.TRUE;
				} catch (JDOOptimisticVerificationException exception) {
					System.out.println(Thread.currentThread().getName() + " can't change country from "
							+ formerCountryName + " to " + countryName);
					committed = Boolean.FALSE;
				} finally {
					persistenceManager.close();
				}
			}

			private synchronized void pause(long pause) {
				if (pause > 0L)
					try {
						System.out.println("Waiting " + pause + " ms");
						wait(pause);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
			}

			/**
			 * @return the committed
			 */
			boolean isCommitted() {
				return committed.booleanValue();
			}

		}

		@SuppressWarnings("deprecation")
		protected void testMain() throws Exception {
			this.id = 500000l;
			System.out.println("getting root package...");
			Authority app1 = super.authority;
			RefPackage rootPkg = app1.refOutermostPackage();
			App1Package app1Package = (App1Package) app1.refOutermostPackage()
					.refPackage(app1.refGetPath().getLastSegment().toClassicRepresentation());

			Generic1Package generic1Package = (Generic1Package) rootPkg.refPackage("org:openmdx:generic1");
			Model_1_0 model = ((RefRootPackage_1) rootPkg).refModel();
			rootPkg.refPackage("test:openmdx:state2");
			PersonClass personClass = app1Package.getPerson();
			InternationalPostalAddressClass postalAddressClass = app1Package.getInternationalPostalAddress();
			EmailAddressClass emailAddressClass = app1Package.getEmailAddress();
			CycleMember1Class cycleMember1Class = app1Package.getCycleMember1();
			CycleMember2Class cycleMember2Class = app1Package.getCycleMember2();
			MessageTemplateClass messageTemplateClass = app1Package.getMessageTemplate();
			DocumentClass documentClass = app1Package.getDocument();

			//
			// CR20018821
			//
			try {
				super.taskId = "CR20018821";
				@SuppressWarnings("unused")
				RefContainer<?> segments = (RefContainer<?>) super.entityManager
						.getObjectById(provider.refGetPath().getChild("segment"));
			} finally {
				super.taskId = null;
			}

			// segment
			final Provider provider = app1.getProvider(false, getProviderName());
			final test.openmdx.app1.jmi1.Segment segment = (test.openmdx.app1.jmi1.Segment) provider
					.getSegment(STANDARD_SEGMENT_NAME);
			long startedAt = 0;

			//
			// CR20018821
			//
			try {
				super.taskId = "CR20018821";
				@SuppressWarnings("unused")
				RefContainer<?> segments = (RefContainer<?>) super.entityManager
						.getObjectById(provider.refGetPath().getChild("segment"));
			} finally {
				super.taskId = null;
			}
			//
			// CR20018821
			//
			try {
				super.taskId = "CR20018821";
				this.begin();
				Importer.importObjects(Importer.asTarget(super.entityManager),
						Importer.asSource(new URL(Resources.toResourceXRI("test/openmdx/app1/data.xml"))));
				this.commit();
			} finally {
				super.taskId = null;
			}
			//
			// CR20020355
			//
			try {
				super.taskId = "CR20020355";
				Object noPosition = null;
				InvoicePosition jmiPosition = this.entityManager.newInstance(InvoicePosition.class);
				test.openmdx.app1.jpa3.InvoicePosition jpaPosition = new test.openmdx.app1.jpa3.InvoicePosition();
				Assertions.assertNull(PersistenceHelper.getClassName(noPosition), "CR20020355");
				Assertions.assertEquals("test:openmdx:app1:InvoicePosition",  PersistenceHelper.getClassName(jmiPosition), "CR20020355");
				Assertions.assertEquals("test.openmdx.app1.jpa3.InvoicePosition",  PersistenceHelper.getClassName(jpaPosition), "CR20020355");
			} finally {
				super.taskId = null;
			}
			//
			// CR20059789
			//
			if (isBackedUpByStandardDB()) {
				try {
					super.taskId = "CR20059789";
					final PersistenceManager pm = JDOHelper.getPersistenceManager(segment);
					pm.currentTransaction().begin();
					Assertions.assertNotEquals("CR20059789", segment.getDescription());
					segment.setDescription("CR20059789");
					Assertions.assertEquals("CR20059789", segment.getDescription());
					final org.openmdx.base.persistence.spi.UnitOfWork uow = (org.openmdx.base.persistence.spi.UnitOfWork) PersistenceHelper
							.currentUnitOfWork(pm);
					uow.beforeCompletion();
					uow.clear();
					pm.evictAll();
					Assertions.assertEquals("CR20059789", segment.getDescription());
					pm.currentTransaction().rollback();
					Assertions.assertNotEquals("CR20059789", segment.getDescription());
				} finally {
					super.taskId = null;
				}
			}
			//
			// CR20019971
			//
			try {
				super.taskId = "CR20019971";
				InvoicePosition position = this.entityManager.newInstance(InvoicePosition.class);
				Assertions.assertNull(position.getInvoice(), "Not yet contained");
				Invoice invoice = this.entityManager.newInstance(Invoice.class);
				invoice.addInvoicePosition(position);
				Assertions.assertSame(invoice, position.getInvoice(), "Transient but contained");
			} finally {
				super.taskId = null;
			}
			//
			// CR10011870
			//
			try {
				super.taskId = "CR10011870";
				PriceCalculator inaccessable = (PriceCalculator) entityManager
						.getUserObject(PriceCalculator.class.getSimpleName());
				Assertions.assertNull(inaccessable, "Inaccessable user object");
				PropagatedUserObject propagated = (PropagatedUserObject) entityManager
						.getUserObject(PropagatedUserObject.class);
				if (this instanceof ProxyConnectionTest) {
					Assertions.assertNull(propagated, "Propagated user object");
				} else {
					Assertions.assertNotNull(propagated, "Propagated user object");
				}

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
				booleanProperty.refSetValue("org:openmdx:generic1:Property:description", "A SparseArray Of Flags");
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
				invoice.setPaymentPeriod(Datatypes.create(Duration.class, "P30D")); // CR20020068
				Assertions.assertNull(ReducedJDOHelper.getObjectId(invoice), "CR0003551");
				@SuppressWarnings("unchecked")
				RefContainer<Invoice> refInvoices = (RefContainer<Invoice>) segment.<Invoice>getInvoice();
				Assertions.assertNull(PersistenceHelper.getLastXRISegment(booleanProperty), "Not yet contained");
				invoice.addProperty(false, "flag", booleanProperty);
				Assertions.assertFalse(ReducedJDOHelper.isPersistent(booleanProperty));
				Assertions.assertEquals("flag",  PersistenceHelper.getLastXRISegment(booleanProperty), "Transient object has alread XRI qualifier");
				this.begin();
				refInvoices.refAdd(RefContainer.REASSIGNABLE, nextId(), invoice);
				Assertions.assertNotNull(ReducedJDOHelper.getObjectId(invoice), "CR0003551");
				for (int i = 0; i < 10; i++) {
					InvoicePosition invoicePosition = invoicePositionClass.createInvoicePosition();
					invoicePosition.setDescription("this is an invoice position for P" + i);
					invoicePosition.setProductId("P" + i);
					Assertions.assertNull(ReducedJDOHelper.getObjectId(invoicePosition), "CR0003551");
					invoice.addInvoicePosition(false, nextId(), invoicePosition);
					Assertions.assertNotNull(ReducedJDOHelper.getObjectId(invoicePosition), "CR0003551");
				}
				this.commit();
				Assertions.assertTrue(ReducedJDOHelper.isPersistent(booleanProperty));
				Assertions.assertEquals("flag",  PersistenceHelper.getLastXRISegment(booleanProperty), "Persistent object has alread XRI qualifier");
			} finally {
				super.taskId = null;
			}
			//
			// I111
			//
			try {
				super.taskId = "I111";
				PersistenceHelper.retrieveAllDescendants(invoice);
				Container<Property> properties = invoice.getProperty();
				Assertions.assertEquals(properties.size(), 1, "Invoice Properties");
				Property property = invoice.getProperty("flag");
				Assertions.assertEquals("A SparseArray Of Flags", property.getDescription());
				Container<InvoicePosition> positions = invoice.getInvoicePosition();
				Assertions.assertEquals(positions.size(), 10, "Invoice Positions");
			} finally {
				super.taskId = null;
			}
			//
			// CR20020449
			//
			try {
				this.taskId = "CR20020449";
				if (isBackedUpByStandardDB()) {
					InvoiceQuery invoiceQuery = (InvoiceQuery) entityManager.newQuery(Invoice.class);
					invoiceQuery.productGroupId().like(RegularExpressionFlag.CASE_INSENSITIVE.getFlag(), "Pg0");
					((Query) invoiceQuery).getFetchPlan().setFetchSize(FetchPlan.FETCH_SIZE_GREEDY);
					List<Invoice> invoices = segment.getInvoice(invoiceQuery);
					Assertions.assertEquals(1,  invoices.size());
				}
				{
					InvoiceQuery invoiceQuery = (InvoiceQuery) entityManager.newQuery(Invoice.class);
					invoiceQuery.thereExistsDescription().like(RegularExpressionFlag.ACCENT_INSENSITIVE.getFlag(),
							"this is \u00E4n invoice for PG0");
					try {
						final List<Invoice> invoices = segment.getInvoice(invoiceQuery);
						Assertions.assertEquals(1,  invoices.size());
					} catch (Exception acceptable) {
						Assertions.assertEquals( BasicException.Code.NOT_SUPPORTED,   new ServiceException(acceptable).log().getExceptionCode(), "The actual database has no configuration for the requested LIKE flavour");
					}
				}
			} finally {
				super.taskId = null;
			}
			//
			// CR20019962
			//
			try {
				super.taskId = "CR20019962";
				invoice.getInvoicePosition((InvoicePositionQuery) null);
				try {
					invoice.getInvoicePosition((String) null);
					Assertions.fail("null is not allowed as qualifier");
				} catch (JmiServiceException exception) {
					Assertions.assertEquals(BasicException.Code.DEFAULT_DOMAIN,  exception.getExceptionDomain(), "Invalid Argument");
					Assertions.assertEquals( BasicException.Code.BAD_PARAMETER,   exception.getExceptionCode(), "Invalid Argument");
				}
				try {
					invoice.getInvoicePosition(false, (String) null);
					Assertions.fail("null is not allowed as qualifier");
				} catch (JmiServiceException exception) {
					Assertions.assertEquals(BasicException.Code.DEFAULT_DOMAIN,  exception.getExceptionDomain(), "Invalid Argument");
					Assertions.assertEquals( BasicException.Code.BAD_PARAMETER,   exception.getExceptionCode(), "Invalid Argument");
				}
			} finally {
				super.taskId = null;
			}
			//
			// CR20019592
			//
			try {
				super.taskId = "CR20019592";
				ProductQuery productQuery = (ProductQuery) entityManager.newQuery(Product.class);
				productQuery.createdAt().lessThanOrEqualTo(null);
				Assertions.fail("Null values are not allowed in queries");
			} catch (JmiServiceException expected) {
				Assertions.assertEquals( BasicException.Code.BAD_PARAMETER,   expected.getExceptionCode(), "Null values are not allowed in queries");
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
					InvoicePositionQuery invoicePositionQuery = (InvoicePositionQuery) entityManager
							.newQuery(InvoicePosition.class);
					invoicePositionQuery.product().elementOf(PersistenceHelper.asSubquery(productQuery));
					invoice.getInvoicePosition(invoicePositionQuery); // We must not execute this query as the product
																		// reference is derived!
				}
				{
					InvoicePositionQuery invoicePositionQuery = (InvoicePositionQuery) entityManager
							.newQuery(InvoicePosition.class);
					invoicePositionQuery.productId().equalTo("P6");
					InvoiceQuery invoiceQuery = (InvoiceQuery) entityManager.newQuery(Invoice.class);
					invoiceQuery.thereExistsInvoicePosition()
							.elementOf(PersistenceHelper.asSubquery(invoicePositionQuery));
					List<Invoice> invoices = segment.getInvoice(invoiceQuery);
					Assertions.assertFalse(invoices.isEmpty(), "There exist invoices including product P6");
					for (Invoice anInvoice : invoices) {
						System.out.println(anInvoice);
						for (InvoicePosition anInvoicePosition : anInvoice.<InvoicePosition>getInvoicePosition()) {
							System.out.println(anInvoicePosition.getProductId());
						}
					}
				}
			} finally {
				super.taskId = null;
			}
			//
			// CR20022111
			//
			try {
				super.taskId = "CR20022111";
				{
					InvoiceQuery invoiceQuery = (InvoiceQuery) entityManager.newQuery(Invoice.class);
					invoiceQuery.thereExistsInvoicePosition().productId().equalTo("P6");
					List<Invoice> invoices = segment.getInvoice(invoiceQuery);
					Assertions.assertFalse(invoices.isEmpty(), "There exist invoices including product P6");
					for (Invoice anInvoice : invoices) {
						System.out.println(anInvoice);
						for (InvoicePosition anInvoicePosition : anInvoice.<InvoicePosition>getInvoicePosition()) {
							System.out.println(anInvoicePosition.getProductId());
						}
					}
				}
			} finally {
				super.taskId = null;
			}
			//
			// CR20041420
			//
			try {
				super.taskId = "CR20041420";
				if (isBackedUpByStandardDB()) {
					//
					// Read via extent
					//
					String xriPattern = segment.refGetPath().getDescendant("invoice", ":*", "invoicePosition", "%")
							.toXRI();
					InvoicePositionQuery invoicePositionQuery = (InvoicePositionQuery) PersistenceHelper
							.newQuery(entityManager.getExtent(InvoicePosition.class), xriPattern);
					invoicePositionQuery.productId().equalTo("P6");
					InvoiceQuery invoiceQuery = invoicePositionQuery.invoice();
					invoiceQuery.productGroupId().equalTo("PG0");
					List<InvoicePosition> invoicePositions = segment.getExtent(invoicePositionQuery);
					Assertions.assertFalse(invoicePositions.isEmpty(), "Invoice Positions");
				}
			} finally {
				super.taskId = null;
			}
			//
			// CR0000823
			//
			try {
				super.taskId = "CR0000823";
				Person person = super.entityManager.newInstance(Person.class);
				person.setGivenName("Klaus", "Maria");
				//
				// Before the unit of work's begin
				//
				Assertions.assertEquals("Maria",  person.getGivenName().get(1), "Second part of first name");
				Assertions.assertNull(person.getLastName(), "LastName");
				if (super.entityManager.getPersistenceManagerFactory().getProperties()
						.contains(Constants.OPTION_TRANSACTIONAL_TRANSIENT)) {
					this.begin();
					super.entityManager.makeTransactional(person);
					person.setLastName("Brandauer");
					//
					// In the unit of work
					//
					Assertions.assertEquals("Maria",  person.getGivenName().get(1), "Second part of first name");
					Assertions.assertEquals("Brandauer",  person.getLastName(), "LastName");
					this.rollback();
					//
					// After the unit of work's roll-back
					//
					Assertions.assertEquals("Maria",  person.getGivenName().get(1), "Second part of first name");
					Assertions.assertNull(person.getLastName(), "LastName");
				} else {
					try {
						this.begin();
						super.entityManager.makeTransactional(person);
						Assertions.fail(Constants.OPTION_TRANSACTIONAL_TRANSIENT + " is not supported");
					} catch (JDOUnsupportedOptionException expected) {
						person.setLastName("Brandauer");
						//
						// In the unit of work
						//
						Assertions.assertEquals("Maria",  person.getGivenName().get(1), "Second part of first name");
						Assertions.assertEquals("Brandauer",  person.getLastName(), "LastName");
						this.rollback();
						//
						// After the unit of work's roll-back
						//
						Assertions.assertEquals("Maria",  person.getGivenName().get(1), "Second part of first name");
						Assertions.assertEquals("Brandauer",  person.getLastName(), "LastName");
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
				Assertions.fail("Missing mandatory feature ProductGroupId");
			} catch (JDOFatalDataStoreException exception) {
				Assertions.assertEquals( (causeIsGeneric() ? BasicException.Code.GENERIC : BasicException.Code.VALIDATION_FAILURE),   Throwables.getCause(exception, null).getExceptionCode(), "Missing mandatory feature ProductGroupId");
			} finally {
				super.taskId = null;
			}
			//
			// CR20019862 & CR20020187
			//
			try {
				super.taskId = "CR20020187";
			} finally {
				super.taskId = null;
				this.begin();
				Invoice i4711 = this.entityManager.newInstance(Invoice.class);
				i4711.setDescription("K\u00F6lnische W\u00E4sser");
				i4711.setInternationalProductGroupIdentification("K-4711");
				i4711.setProductGroupId("4711");
				Assertions.assertNull(segment.getInvoice("CR20019862"), "Invoice not yet added");
				segment.addInvoice("CR20019862", i4711);
				this.commit();
			}
			try {
				super.taskId = "CR20019862";
				InvoiceQuery query = (InvoiceQuery) this.entityManager.newQuery(Invoice.class);
				query.thereExistsInternationalProductGroupIdentification().like("K-.*");
				List<Invoice> groups = segment.getInvoice(query);
				Assertions.assertEquals( 1,   groups.size(), "K\u00F6lnisch Wasser");
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
				for (int i = 0; i < 5; i++) {
					InvoicePosition auditPosition = this.entityManager.newInstance(InvoicePosition.class);
					auditPosition.setDescription("An invoice position for audit tests");
					auditPosition.setProductId("P" + i);
					auditInvoice.addInvoicePosition("IP" + i, auditPosition);
				}
				this.commit();
				this.begin();
				auditInvoice.setDescription("Invoice CR20019372");
				for (int i = 0; i < 5; i += 2) {
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
				Assertions.assertEquals(2,  properties.size());
				UriProperty uriProperty = this.entityManager.newInstance(UriProperty.class);
				uriProperty.getUriValue().put(0, URI.create("xri://+ChangeRequest/20019629"));
				propertyHolder.addProperty(this.taskId, uriProperty);
				Assertions.assertEquals(3,  properties.size());
				for (Property property : new ArrayList<Property>(properties)) {
					if (property instanceof StringProperty) {
						propertyId = property.refGetPath();
					}
					property.refDelete();
				}
				for (Property property : properties) {
					Assertions.fail("There should be no properties left: " + property);
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
				PropertySetHasProperties.Property<StringProperty> properties = propertyHolder
						.<StringProperty>getProperty();
				StringProperty property = properties.get(QualifierType.REASSIGNABLE,
						propertyId.getLastSegment().toClassicRepresentation());
				Assertions.assertEquals(propertyValue,  property.getStringValue().get(propertyIndex), "Property value");
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
				List<Property> list = propertyHolder.<Property>getProperty((PropertyQuery) null);
				this.begin();
				//
				// After begin
				//
				Assertions.assertEquals( 2,   list.size(), "List size after begin");
				Assertions.assertEquals( 2,   container.size(), "Container size after begin");
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
				Assertions.assertEquals( 3,   list.size(), "List size after insert");
				for (Property p : list) {
					System.out.println(p.refClass().refMofId() + ": " + p.refMofId());
				}
				System.out.println("CR20019915: Container");
				Assertions.assertEquals( 3,   container.size(), "Container size after insert");
				for (Property p : container) {
					System.out.println(p.refClass().refMofId() + ": " + p.refMofId());
				}
				//
				// Before roll back
				//
				Assertions.assertEquals( 3,   list.size(), "List size before roll back");
				Assertions.assertEquals( 3,   container.size(), "Container size before roll back");
				this.rollback();
			} finally {
				super.taskId = null;
			}

			// CR20019816
			//
			if (VALIDATE_PERSISTENCE_MANAGER)
				try {
					this.taskId = "CR20019816";
					this.begin();
					PersistenceManager differentManager = newEntityManagerFactory().getPersistenceManager();
					UriProperty uriProperty = differentManager.newInstance(UriProperty.class);
					uriProperty.getUriValue().put(0, URI.create("xri://+ChangeRequest/20019816"));
					try {
						propertyHolder.addProperty(this.taskId, uriProperty);
						Assertions.fail("Data Object Manager Mismatch");
					} catch (RuntimeException exception) {
						Assertions.assertEquals( BasicException.Code.BAD_PARAMETER,   BasicException.toExceptionStack(exception).getExceptionCode(), "Data Object Manager Mismatch");
					}
				} finally {
					this.rollback();
					super.taskId = null;
				}
			//
			// CR20019533
			//
			if (isBackedUpByStandardDB())
				try {
					this.taskId = "CR20019533";
					UriPropertyQuery query = (UriPropertyQuery) this.entityManager.newQuery(UriProperty.class);
					query.thereExistsUriValue().equalTo(URI.create("xri://+ChangeRequest/20019533"));
					List<UriProperty> properties = propertyHolder.<UriProperty>getProperty(query);
					Assertions.assertEquals(1,  properties.size());
					Assertions.assertFalse(ClassicSegments.isPlaceholder(properties.get(0).refGetPath().getLastSegment()), "Plug-in-provided id");
				} finally {
					super.taskId = null;
				}
			{
				System.out.println("Invoice instanceof " + Arrays.toString(invoice.getClass().getInterfaces()));
				Path flagId = ((Path) ReducedJDOHelper.getObjectId(invoice)).getDescendant("property", "flag");
				BooleanProperty flag = (BooleanProperty) super.entityManager.getObjectById(flagId);
				Assertions.assertNotNull(flag, "Flag");
				SparseArray<Boolean> flags = flag.getBooleanValue();
				Assertions.assertNotNull(flags, "flags");
				Assertions.assertEquals(Boolean.TRUE,  flags.get(0), "Flag[0]");
				PersistenceManager m = ReducedJDOHelper.getPersistenceManager(flag);
				Assertions.assertSame(super.entityManager, m, "Class with root parent");
				if (this instanceof AbstractLocalConnectionTest) {
					Assertions.assertTrue(flag instanceof DelegatingRefObject_1_0, "Implementation detail");
					DelegatingRefObject_1_0 entity = (DelegatingRefObject_1_0) flag;
					Object dataObject = entity.openmdxjdoGetDataObject();
					Assertions.assertTrue(dataObject instanceof PersistenceCapable);
					Assertions.assertTrue(dataObject instanceof RefObject_1_0);
					ObjectView_1_0 objectView = ((RefObject_1_0) dataObject).refDelegate();
					Assertions.assertNotNull(objectView);
					Assertions.assertNotSame(entity.openmdxjdoGetDelegate(), dataObject, "Made persistent");
				}
				if (this instanceof ProxyConnectionTest) {
					Assertions.assertFalse(flag instanceof DelegatingRefObject_1_0, "Implementation detail");
				}
			}
			if (isBackedUpByStandardDB()) {
				{
					//
					// Read via extent
					//
					String xriPattern = segment.refGetPath().getDescendant("invoice", ":*", "invoicePosition", "%")
							.toXRI();
					InvoicePositionQuery invoicePositionQuery = (InvoicePositionQuery) PersistenceHelper
							.newQuery(entityManager.getExtent(InvoicePosition.class), xriPattern);
					List<InvoicePosition> invoicePositions = segment.getExtent(invoicePositionQuery);
					Assertions.assertTrue(invoicePositions.listIterator(14).hasNext(), "Invoice Positions: Second Last");
					Assertions.assertFalse(invoicePositions.listIterator(15).hasNext(), "Invoice Positions: Last");
					Assertions.assertEquals( 15,   invoicePositions.size(), "Invoice Positions: Size");
				}
				{
					//
					// Read via Query
					//
					Path xriPattern = segment.refGetPath().getDescendant("invoice", ":*", "invoicePosition", "%");
					Query query = PersistenceHelper.newQuery(entityManager.getExtent(InvoicePosition.class),
							xriPattern);
					query.setCandidates(segment.getExtent());
					@SuppressWarnings("unchecked")
					List<InvoicePosition> invoicePositions = (List<InvoicePosition>) query.execute();
					Assertions.assertTrue(invoicePositions.listIterator(14).hasNext(), "Invoice Positions: Second Last");
					Assertions.assertFalse(invoicePositions.listIterator(15).hasNext(), "Invoice Positions: Last");
					Assertions.assertEquals( 15,   invoicePositions.size(), "Invoice Positions: Size");
				}
				{
					InvoicePositionQuery invoicePositionQuery = app1Package.createInvoicePositionQuery();
					// get products without price. price is an expensive derived
					// atttribute. Therefore this iteration should be much faster
					// than the next one
					startedAt = System.currentTimeMillis();
					InvoiceHasInvoicePosition.InvoicePosition<InvoicePosition> allInvoicePositions = invoice
							.getInvoicePosition();
					Collection<InvoicePosition> someInvoicePositions = invoicePositionQuery == null
							? allInvoicePositions
							: allInvoicePositions.getAll(invoicePositionQuery);
					for (InvoicePosition invoicePosition : someInvoicePositions) {
						Product product = invoicePosition.getProduct();
						product.getDescription();
					}
					System.out.println("time for retrieving 10 invoice positions (without price)="
							+ (System.currentTimeMillis() - startedAt));

					startedAt = System.currentTimeMillis();
					allInvoicePositions = invoice.getInvoicePosition();
					for (InvoicePosition invoicePosition : allInvoicePositions) {
						Product product = invoicePosition.getProduct();
						String description = product.getDescription();
						if (description == null) {
							description = product.refGetPath().getLastSegment().toClassicRepresentation();
						}
						System.out.println("Product " + description + " costs " + product.getPrice());
					}
					System.out.println("time for retrieving 10 invoice positions (with price)="
							+ (System.currentTimeMillis() - startedAt));
				}
				testDirtyExtent(segment, false);
				testDirtyExtent(segment, true);
			}
			try {
				super.taskId = "CR20020326";
				String productId = "P3";
				InvoicePositionQuery invoicePositionQuery = createInvoicePositionQuery(segment, productId);
				List<InvoicePosition> invoicePositions = segment.getExtent(invoicePositionQuery);
				validateInvoicePositions(invoicePositions, productId, 2);
				this.begin();
				InvoicePosition invoicePosition0 = invoicePositions.get(0);
				invoicePosition0.setProductId("p3");
				Assertions.assertEquals(Collections.singleton("productId"),  DirtyObjects.getModifiedFeatures(invoicePosition0), "CR10011367");
				validateInvoicePositions(invoicePositions, productId, 1);
				validateInvoicePositions(
						segment.<InvoicePosition>getExtent(createInvoicePositionQuery(segment, productId)), productId,
						1);
				this.rollback();
				validateInvoicePositions(invoicePositions, productId, 2);
				validateInvoicePositions(
						segment.<InvoicePosition>getExtent(createInvoicePositionQuery(segment, productId)), productId,
						2);
				if (PROXIED_EXTENT_IS_AMENDMENT_AWARE || !(this instanceof ProxyConnectionTest)) {
					this.begin();
					segment.<InvoicePosition>getExtent(createInvoicePositionQuery(segment, "P2")).get(0)
							.setProductId(productId);
					validateInvoicePositions(invoicePositions, productId, 3);
					validateInvoicePositions(
							segment.<InvoicePosition>getExtent(createInvoicePositionQuery(segment, productId)),
							productId, 3);
					this.rollback();
				}
				validateInvoicePositions(invoicePositions, productId, 2);
				validateInvoicePositions(
						segment.<InvoicePosition>getExtent(createInvoicePositionQuery(segment, productId)), productId,
						2);
			} finally {
				super.taskId = null;
			}
			try {
				super.taskId = "CR20019855";
				for (Invoice i : segment.<Invoice>getInvoice()) {
					for (InvoicePosition p : i.<InvoicePosition>getInvoicePosition()) {
						Assertions.assertNotNull(p.getProductId(), "The product id is mandatory");
					}
				}
			} finally {
				super.taskId = null;
			}
			//
			// CR20020022
			//
			try {
				this.taskId = "CR20020022";
				RefQuery_1_0 query = (RefQuery_1_0) super.entityManager.newQuery(CycleMember1.class);
				org.openmdx.base.persistence.spi.Queries.applyStatements(query,
						"thereExistsM2(){"
								+ "thereExistsDescription().elementOf(\"Cycle Member \\\"CR20020022\\\"\",\"n/a\");"
								+ "m1().isNull()" + "}");
				validateCycleQuery(query);
				this.taskId = "CR10010637";
				ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
				objectOutputStream.writeObject(query);
				objectOutputStream.flush();
				ByteArrayInputStream byteInputStream = new ByteArrayInputStream(byteOutputStream.toByteArray());
				ObjectInputStream objectInputStream = new ObjectInputStream(byteInputStream);
				RefQuery_1_0 deserializedQuery = (RefQuery_1_0) objectInputStream.readObject();
				validateCycleQuery(deserializedQuery);
				this.taskId = "CR?";
				RefQuery_1_0 clonedQuery = PersistenceHelper.clone(query);
				validateCycleQuery(clonedQuery);
			} finally {
				super.taskId = null;
			}
			//
			// CR20020024
			//
			try {
				this.taskId = "CR20020024";
				String cycle1ContainerId = segment.refMofId() + "/cycleMember1";
				String cycle2Id = segment.refMofId() + "/cycleMember2/CR20020022";
				RefQuery_1_0 query = (RefQuery_1_0) super.entityManager.newQuery(
						org.openmdx.base.persistence.cci.Queries.QUERY_LANGUAGE,
						new URI(cycle1ContainerId + '?' + "queryType="
								+ URLEncoder.encode("test:openmdx:app1:CycleMember1", "UTF-8") + '&' + "query="
								+ URLEncoder.encode("thereExistsM2().equalTo(\"" + cycle2Id + "\")", "UTF-8")) // a
																												// simple
																												// String
																												// would
																												// work
																												// as
																												// well...
				);
				List<ConditionRecord> conditions = query.refGetFilter().getCondition();
				Assertions.assertEquals( 2,   conditions.size(), "Conditions");
				Assertions.assertEquals(SystemAttributes.OBJECT_INSTANCE_OF,  conditions.get(0).getFeature(), "Condition 0");
				Assertions.assertEquals(Quantifier.THERE_EXISTS,  conditions.get(0).getQuantifier(), "Condition 0");
				Assertions.assertEquals(ConditionType.IS_IN,  conditions.get(0).getType(), "Condition 0");
				Assertions.assertEquals( 1,   conditions.get(0).getValue().length, "Condition 0");
				Assertions.assertEquals("test:openmdx:app1:CycleMember1",  conditions.get(0).getValue(0), "Condition 0");
				Assertions.assertEquals("m2",  conditions.get(1).getFeature(), "Condition 1");
				Assertions.assertEquals(Quantifier.THERE_EXISTS,  conditions.get(1).getQuantifier(), "Condition 1");
				Assertions.assertEquals(ConditionType.IS_IN,  conditions.get(1).getType(), "Condition 1");
				Assertions.assertEquals( 1,   conditions.get(1).getValue().length, "Condition 1");
				Assertions.assertEquals(new Path(cycle2Id),  conditions.get(1).getValue(0), "Condition 1");
			} finally {
				super.taskId = null;
			}
			//
			// CR20019668
			//
			try {
				this.taskId = "CR20019668";
				RefQuery_1_0 query = (RefQuery_1_0) super.entityManager.newQuery(Invoice.class);
				org.openmdx.base.persistence.spi.Queries.applyStatements(query,
						"forAllDescription().unlike(\"./.\");thereExistsInvoicePosition().productId().like(\"P.*\");forAllProperty().name().equalTo(\"FLAG\")");
				List<ConditionRecord> conditions = query.refGetFilter().getCondition();
				Assertions.assertEquals( 4,   conditions.size(), "Conditions");
				Assertions.assertEquals(SystemAttributes.OBJECT_INSTANCE_OF,  conditions.get(0).getFeature(), "Condition 0");
				Assertions.assertEquals(Quantifier.THERE_EXISTS,  conditions.get(0).getQuantifier(), "Condition 0");
				Assertions.assertEquals(ConditionType.IS_IN,  conditions.get(0).getType(), "Condition 0");
				Assertions.assertEquals( 1,   conditions.get(0).getValue().length, "Condition 0");
				Assertions.assertEquals("test:openmdx:app1:Invoice",  conditions.get(0).getValue(0), "Condition 0");
				Assertions.assertEquals("description",  conditions.get(1).getFeature(), "Condition 1");
				Assertions.assertEquals(Quantifier.FOR_ALL,  conditions.get(1).getQuantifier(), "Condition 1");
				Assertions.assertEquals(ConditionType.IS_UNLIKE,  conditions.get(1).getType(), "Condition 1");
				Assertions.assertEquals( 1,   conditions.get(1).getValue().length, "Condition 1");
				Assertions.assertEquals("./.",  conditions.get(1).getValue(0), "Condition 1");
				Assertions.assertEquals("invoicePosition",  conditions.get(2).getFeature(), "Condition 2");
				Assertions.assertEquals(Quantifier.THERE_EXISTS,  conditions.get(2).getQuantifier(), "Condition 2");
				Assertions.assertEquals(ConditionType.IS_IN,  conditions.get(2).getType(), "Condition 2");
				Assertions.assertEquals( 1,   conditions.get(2).getValue().length, "Condition 2");
				Assertions.assertTrue(conditions.get(2).getValue(0) instanceof QueryFilterRecord, "Condition 2");
				List<ConditionRecord> conditions2 = ((QueryFilterRecord) conditions.get(2).getValue(0)).getCondition();
				Assertions.assertEquals( 2,   conditions2.size(), "Conditions 2");
				Assertions.assertEquals(SystemAttributes.OBJECT_INSTANCE_OF,  conditions2.get(0).getFeature(), "Condition 2.0");
				Assertions.assertEquals(Quantifier.THERE_EXISTS,  conditions2.get(0).getQuantifier(), "Condition 2.0");
				Assertions.assertEquals(ConditionType.IS_IN,  conditions2.get(0).getType(), "Condition 2.0");
				Assertions.assertEquals( 1,   conditions2.get(0).getValue().length, "Condition 2.0");
				Assertions.assertEquals("test:openmdx:app1:InvoicePosition",  conditions2.get(0).getValue(0), "Condition 2.0");
				Assertions.assertEquals("productId",  conditions2.get(1).getFeature(), "Condition 2.1");
				Assertions.assertEquals(Quantifier.THERE_EXISTS,  conditions2.get(1).getQuantifier(), "Condition 2.1");
				Assertions.assertEquals(ConditionType.IS_LIKE,  conditions2.get(1).getType(), "Condition 2.1");
				Assertions.assertEquals( 1,   conditions2.get(1).getValue().length, "Condition 2.1");
				Assertions.assertEquals("P.*",  conditions2.get(1).getValue(0), "Condition 2.1");
				Assertions.assertEquals("property",  conditions.get(3).getFeature(), "Condition 3");
				Assertions.assertEquals(Quantifier.FOR_ALL,  conditions.get(3).getQuantifier(), "Condition 3");
				Assertions.assertEquals(ConditionType.IS_IN,  conditions.get(3).getType(), "Condition 3");
				Assertions.assertEquals( 1,   conditions.get(3).getValue().length, "Condition 3");
				Assertions.assertTrue(conditions.get(3).getValue(0) instanceof QueryFilterRecord, "Condition 3");
				List<ConditionRecord> conditions3 = ((QueryFilterRecord) conditions.get(3).getValue(0)).getCondition();
				Assertions.assertEquals( 2,   conditions3.size(), "Conditions 3");
				Assertions.assertEquals(SystemAttributes.OBJECT_INSTANCE_OF,  conditions3.get(0).getFeature(), "Condition 3.0");
				Assertions.assertEquals(Quantifier.THERE_EXISTS,  conditions3.get(0).getQuantifier(), "Condition 3.0");
				Assertions.assertEquals(ConditionType.IS_IN,  conditions3.get(0).getType(), "Condition 3.0");
				Assertions.assertEquals( 1,   conditions3.get(0).getValue().length, "Condition 3.0");
				Assertions.assertEquals("org:openmdx:generic1:Property",  conditions3.get(0).getValue(0), "Condition 3.0");
				Assertions.assertEquals("name",  conditions3.get(1).getFeature(), "Condition 3.1");
				Assertions.assertEquals(Quantifier.THERE_EXISTS,  conditions3.get(1).getQuantifier(), "Condition 3.1");
				Assertions.assertEquals(ConditionType.IS_IN,  conditions3.get(1).getType(), "Condition 3.1");
				Assertions.assertEquals( 1,   conditions3.get(1).getValue().length, "Condition 3.1");
				Assertions.assertEquals("FLAG",  conditions3.get(1).getValue(0), "Condition 3.1");
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
				{
					//
					// From Cache
					//
					InvoiceQuery invoiceQuery = (InvoiceQuery) super.entityManager.newQuery(Invoice.class);
					invoiceQuery.thereExistsInvoicePosition().productId().like("P.*");
					List<Invoice> matchingInvoices = segment.getInvoice(invoiceQuery);
					Assertions.assertEquals( 2,   matchingInvoices.size(), "Complex query");
				}
				{
					//
					// From Database
					//
					PersistenceManager anotherPersistenceManager = super.entityManager.getPersistenceManagerFactory()
							.getPersistenceManager();
					InvoiceQuery invoiceQuery = (InvoiceQuery) anotherPersistenceManager.newQuery(Invoice.class);
					invoiceQuery.thereExistsInvoicePosition().productId().like("P.*");
					test.openmdx.app1.jmi1.Segment sameSegment = anotherPersistenceManager
							.getObjectById(test.openmdx.app1.jmi1.Segment.class, segment.refMofId());
					List<Invoice> matchingInvoices = sameSegment.getInvoice(invoiceQuery);
					Assertions.assertEquals( 2,   matchingInvoices.size(), "Complex query");
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
						Invoice transientInvoice = super.entityManager.newInstance(Invoice.class);
						for (int i = 0; i < positionCount; i++) {
							InvoicePosition invoicePosition = super.entityManager.newInstance(InvoicePosition.class);
							transientInvoice.addInvoicePosition("T" + i, invoicePosition);
							for (int j = 0; j < propertyCount; j++) {
								IntegerProperty positionProperty = super.entityManager
										.newInstance(IntegerProperty.class);
								String prefix = isEven(i) && isEven(j) ? "EVEN"
										: isOdd(i) && isOdd(j) ? "ODD" : "MIXED";
								positionProperty.getIntegerValue().put(j, 1000 * i + j);
								positionProperty.setDescription(prefix + "[" + i + "," + j + "]");
								invoicePosition.addProperty("P" + j, positionProperty);
							}
							Assertions.assertEquals( propertyCount,   invoicePosition.getProperty().size(), "Properties/Position");
						}
						Assertions.assertEquals( positionCount,   transientInvoice.getInvoicePosition().size(), "Positions/Invoice");
						InvoicePositionQuery positionQuery = (InvoicePositionQuery) super.entityManager
								.newQuery(InvoicePosition.class);
						positionQuery.thereExistsProperty().thereExistsDescription().like("ODD.*");
						Assertions.assertEquals( (positionCount / 2),   transientInvoice.getInvoicePosition(positionQuery).size(), "ODDs");
					}
					{
						//
						// References stored as attributes
						//
						final int personCount = 10;
						final int groupCount = 4;
						test.openmdx.app1.jmi1.Segment transientSegment = super.entityManager
								.newInstance(test.openmdx.app1.jmi1.Segment.class);
						PersonGroup[] groups = new PersonGroup[groupCount];
						for (int i = 0; i < groupCount; i++) {
							groups[i] = super.entityManager.newInstance(PersonGroup.class);
							String name = "G" + i;
							groups[i].setName(name);
							transientSegment.addPersonGroup(name, groups[i]);
						}
						Assertions.assertEquals( groupCount,   transientSegment.getPersonGroup().size(), "Groups/Segment");
						for (int j = 0; j < personCount; j++) {
							Person person = super.entityManager.newInstance(Person.class);
							if (j > 0) {
								PersonGroup group = groups[j % groupCount];
								person.getPersonGroup().add(group);
							}
							transientSegment.addPerson("P" + j, person);
						}
						Assertions.assertEquals( personCount,   transientSegment.getPerson().size(), "People/Segment");
						{
							PersonQuery personQuery = (PersonQuery) super.entityManager.newQuery(Person.class);
							personQuery.thereExistsPersonGroup().name().equalTo("G1");
							Assertions.assertEquals( 3,   transientSegment.getPerson(personQuery).size(), "Some G1");
						}
						{
							PersonQuery personQuery = (PersonQuery) super.entityManager.newQuery(Person.class);
							personQuery.thereExistsPersonGroup().name().equalTo("G3");
							Assertions.assertEquals( 2,   transientSegment.getPerson(personQuery).size(), "Some G3");
						}
						{
							PersonQuery personQuery = (PersonQuery) super.entityManager.newQuery(Person.class);
							personQuery.forAllPersonGroup().name().equalTo("G3");
							Assertions.assertEquals( 3,   transientSegment.getPerson(personQuery).size(), "All G3");
						}
						{
							PersonQuery personQuery = (PersonQuery) super.entityManager.newQuery(Person.class);
							personQuery.forAllPersonGroup().name().like("G.*");
							Assertions.assertEquals( personCount,   transientSegment.getPerson(personQuery).size(), "All Start With G");
						}
						{
							PersonQuery personQuery = (PersonQuery) super.entityManager.newQuery(Person.class);
							personQuery.thereExistsPersonGroup().name().like("G.*");
							Assertions.assertEquals( (personCount - 1),   transientSegment.getPerson(personQuery).size(), "Some Start With G");
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
				PersistenceManager segmentManager = ReducedJDOHelper.getPersistenceManager(segment)
						.getPersistenceManagerFactory().getPersistenceManager();

				Collection<AddressFormat> addressFormats = ((test.openmdx.app1.jmi1.Segment) segmentManager
						.getObjectById(ReducedJDOHelper.getObjectId(segment))).getAddressFormat();
				for (AddressFormat addressFormat : addressFormats) {
					PersistenceManager formatManager = ReducedJDOHelper.getPersistenceManager(addressFormat);
					Assertions.assertSame(segmentManager, formatManager, "cci2.getContainer()");
					System.out.println("addressFormat=" + addressFormat);
				}
			} finally {
				super.taskId = null;
			}

			// get NameFormat
			Collection<NameFormat> nameFormats = segment.getNameFormat();
			for (NameFormat nameFormat : nameFormats) {
				System.out.println("nameFormat=" + nameFormat);
			}
			Assertions.assertNull(segment.getNameFormat(false, "unknown"), "Unknown name format");
			Assertions.assertNull(segment.getAddressFormat("unknown"), "Unknown address format");

			if (isBackedUpByStandardDB())
				try {
					super.taskId = "CR20020187";
					segment.getNameFormat("CR20020187");
					Assertions.fail("CR20020187");
				} catch (JmiServiceException exception) {
					boolean rolledBack = isTransactionRolledBack(exception);
					if (this instanceof AbstractContainerManagedTransactionTest) {
						Assertions.assertFalse(rolledBack, "We remain in container managed transaction");
					} else {
						org.openmdx.base.persistence.cci.UnitOfWork unitOfWork = PersistenceHelper
								.currentUnitOfWork(ReducedJDOHelper.getPersistenceManager(segment));
						Assertions.assertFalse(unitOfWork.isActive());
						Assertions.assertTrue(rolledBack, "RuntimeException lead to roll-back");
					}
				} finally {
					super.taskId = null;
				}
			// modify feature
			for (NameFormat nameFormat : nameFormats)
				try {
					this.begin();
					nameFormat.refSetValue("description", "modified description");
					this.commit();
					Assertions.fail("all attributes are non changeable --> object can not be updated");
				} catch (JDOFatalDataStoreException e) {
					System.out.println("all attributes are non changeable --> object can not be updated");
				}

			if (isBackedUpByStandardDB())
				try {
					NameFormat nameFormat = (NameFormat) super.entityManager.newInstance(NameFormat.class);
					this.begin();
					nameFormat.setDescription("a description");
					segment.getNameFormat().add(nameFormat);
					this.commit();
					Assertions.fail("constraint isFrozen --> object can not be updated");
				} catch (JDOFatalDataStoreException e) {
					System.out.println("constraint isFrozen --> object can not be updated");
				}

			InternationalPostalAddress postalAddress = null;
			EmailAddress emailAddress = null;
			Assertions.assertEquals( 0,   segment.getAddress().size(), "Initial address count");
			for (int i = 0; i < 4; i++) {
				System.out.println(new String[] { "Rollback address addition", "Clear persistent address collection",
						"Clear transient address collection", "Commit address addition" }[i]);
				this.begin();
				postalAddress = createPostalAddress(segment, "0001");
				Object postalAddressId = ReducedJDOHelper.getObjectId(postalAddress);
				// create a EmailAddress
				emailAddress = emailAddressClass.createEmailAddress();
				emailAddress.setAddress("hans.muster@app1.ch");
				segment.addAddress(false, "0002", emailAddress);
				int addressCount = segment.getAddress().size();
				Assertions.assertEquals( 2,   addressCount, "Transient added address count");
				switch (i) {
				case 0: {
					this.rollback();
					addressCount = segment.getAddress().size();
					Assertions.assertEquals( 0,   addressCount, "Rolled back address count");
				}
					break;
				case 1: {
					this.commit();
					Assertions.assertEquals( 2,   segment.getAddress().size(), "Commited address count");
					InternationalPostalAddress retrievedAddress = (InternationalPostalAddress) this.entityManager
							.getObjectById(postalAddressId);
					InternationalPostalAddress clonedAddress = PersistenceHelper.clone(retrievedAddress);
					Assertions.assertFalse(ReducedJDOHelper.isPersistent(clonedAddress));
					this.entityManager.retrieve(retrievedAddress);
					this.begin();
					segment.getAddress().clear();
					addressCount = segment.getAddress().size();
					Assertions.assertEquals( 0,   addressCount, "Transient cleared address count");
					this.commit();
					addressCount = segment.getAddress().size();
					Assertions.assertEquals( 0,   addressCount, "Cleared persistent address count");
				}
					break;
				case 2: {
					segment.getAddress().clear();
					addressCount = segment.getAddress().size();
					Assertions.assertEquals( 0,   addressCount, "Cleared transient address count");
					this.commit();
					addressCount = segment.getAddress().size();
					Assertions.assertEquals( 0,   addressCount, "Cleared committed address count");
					Assertions.assertTrue(segment.getAddress().isEmpty(), "Cleared committed address count");
				}
					break;
				case 3: {
					EmailAddress transientAddress = emailAddressClass.createEmailAddress();
					transientAddress.setAddress("john.player@games.net");
					segment.addAddress(false, "0003", transientAddress);
					addressCount = segment.getAddress().size();
					Assertions.assertEquals( 3,   addressCount, "Transient added address count");
					transientAddress.refDelete(); // segment.getAddress().remove(transientAddress);
					this.commit();
					addressCount = segment.getAddress().size();
					Assertions.assertEquals( 2,   addressCount, "Commited address count");
				}
					break;
				default:
					Assertions.fail("No more instructions");
				}
			}
			Path segmentId = (Path) ReducedJDOHelper.getObjectId(segment);
			Assertions.assertTrue(Arrays.equals(
			segmentId.getSuffix(segmentId.size() - 2), new String[] { "segment", STANDARD_SEGMENT_NAME }), "Identity should be available outside the unit of work");
			//
			// CR20020342
			//
			try {
				super.taskId = "CR20020342";
				{
					begin();
					PostalAddress address = app1Package.getPostalAddress().createPostalAddress();
					address.setCity("Zurich");
					address.setHouseNumber("57");
					address.setPostalCode("8005");
					address.setStreet("Bahnhofstr.");
					address.setAddressLine("Schweizer Bank", "Abeteilung Nummernkonti");
					segment.addAddress(false, "1001", address);
					commit();
				}
				if (isBackedUpByStandardDB()) {
					{
						PostalAddressQuery addressQuery = (PostalAddressQuery) PersistenceHelper.newQuery(
								this.entityManager.getExtent(PostalAddress.class),
								segment.refMofId() + "/address/($..)");
						addressQuery.thereExistsAddressLine().equalTo("Muster");
						List<PostalAddress> addressList = segment.getExtent(addressQuery);
						Assertions.assertFalse(addressList.isEmpty(), "Multivalue predicate on extent");
						for (PostalAddress address : addressList) {
							Assertions.assertTrue(address.getAddressLine().contains("Muster"), "Multivalue predicate");
						}
					}
					{
						PostalAddressQuery addressQuery = (PostalAddressQuery) PersistenceHelper.newQuery(
								this.entityManager.getExtent(PostalAddress.class),
								segment.refMofId() + "/address/($..)");
						addressQuery.forAllAddressLine().notEqualTo("Muster");
						List<PostalAddress> addressList = segment.getExtent(addressQuery);
						Assertions.assertFalse(addressList.isEmpty(), "Multivalue predicate on extent");
						for (PostalAddress address : addressList) {
							Assertions.assertFalse(address.getAddressLine().contains("Muster"), "Multivalue predicate");
						}
					}
				}
			} finally {
				super.taskId = null;
			}
			try {
				this.begin();
				InternationalPostalAddress duplicateAddress = postalAddressClass.createInternationalPostalAddress();
				duplicateAddress.setCountry("Switzerland");
				duplicateAddress.setCity("Zurich");
				duplicateAddress.setHouseNumber("57");
				duplicateAddress.setPostalCode("8005");
				duplicateAddress.setStreet("Bahnhofstr.");
				duplicateAddress.setAddressLine(new String[] { "Familie", "Muster" });
				emailAddress.setAddress("hans.muster@app1.int");
				Assertions.assertEquals("hans.muster@app1.int",  emailAddress.getAddress(), "Transient E-Mail-Address should have changed");
				segment.addAddress(false, "0001", duplicateAddress);
				this.commit();
				Assertions.fail("DUPLICATE expected");
			} catch (JmiServiceException exception) {
				Assertions.assertTrue(currentUnitOfWork().isActive(), "Early duplicate recognition");
				Assertions.assertEquals( BasicException.Code.DUPLICATE,   exception.getExceptionCode(), "Duplicate exception expected");
				this.rollback();
			} catch (JDOException exception) {
				Assertions.assertFalse(currentUnitOfWork().isActive(), "Late duplicate recognition");
				BasicException exceptionStack = BasicException.toExceptionStack(exception);
				Assertions.assertTrue(exceptionStack.getExceptionCode() == BasicException.Code.ABORT
				|| exceptionStack.getExceptionCode() == BasicException.Code.ROLLBACK, "Unit of work failure");
			}
			Assertions.assertEquals("hans.muster@app1.ch",  emailAddress.getAddress(), "Persistent E-Mail-Address shouldn't have changed");
			Assertions.assertTrue(emailAddress.getIdentity().endsWith("/segment/" + STANDARD_SEGMENT_NAME + "/address/0002"), "Identity should be available after unit of work failure");
			//
			// CR220019366 Missing implementation
			//
			try {
				super.taskId = "CR220019366";
				Address original = segment.getAddress("0001");
				Assertions.assertEquals("0001",  original.getId(), "Address.id()");
				PersistenceManager sibling = entityManagerFactory.getPersistenceManager();
				Path xri = new Path(original.refMofId());
				@SuppressWarnings("unchecked")
				RefContainer<Address> container = (RefContainer<Address>) sibling.getObjectById(xri.getParent());
				Address copy = container.refGet(RefContainer.REASSIGNABLE,
						xri.getLastSegment().toClassicRepresentation());
				Assertions.assertEquals("0001",  copy.getId(), "Address.id()");
				Assertions.assertNotSame(original, copy);
				Assertions.assertEquals(original, copy);
			} finally {
				super.taskId = null;
			}
			//
			// CR20018768 refresh
			//
			try {
				super.taskId = "CR20018768";
				this.begin();
				for (int i = 3; i >= 0; i--) {
					emailAddress.setAddress("jean.\u00e9echantillon");
					Assertions.assertTrue(ReducedJDOHelper.isDirty(emailAddress));
					Assertions.assertEquals("jean.\u00e9echantillon", emailAddress.getAddress());
					switch (i) {
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
					Assertions.assertTrue(!ReducedJDOHelper.isDirty(emailAddress));
					Assertions.assertEquals("hans.muster@app1.ch",  emailAddress.getAddress(), "Persistent E-Mail-Address be reset");
				}
				this.commit();
				for (int i = 3; i >= 0; i--) {
					switch (i) {
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
				for (int i = 3; i >= 0; i--) {
					switch (i) {
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
					try {
						super.taskId = "CR20020326";
						EmailAddressQuery addressQuery = (EmailAddressQuery) this.entityManager
								.newQuery(EmailAddress.class);
						addressQuery.address().like("hans.muster@.*");
						List<EmailAddress> selection = segment.<EmailAddress>getAddress(addressQuery);
						this.entityManager.evictAll(segment.<Address>getAddress());
						this.entityManager.retrieveAll(selection);
					} finally {
						super.taskId = null;
					}

				}
			} finally {
				super.taskId = null;
			}
			//
			// Refresh Selection
			//
			try {
				super.taskId = "CR20020326";
				EmailAddressQuery addressQuery = (EmailAddressQuery) this.entityManager.newQuery(EmailAddress.class);
				addressQuery.address().like("hans.muster@.*");
				List<EmailAddress> selection = segment.<EmailAddress>getAddress(addressQuery);
				this.entityManager.evictAll(segment.<Address>getAddress());
				this.entityManager.retrieveAll(selection);
			} finally {
				super.taskId = null;
			}
			//
			// Refresh Plain Extent
			//
			try {
				super.taskId = "CR20020326";
				AddressQuery extentQuery = (AddressQuery) PersistenceHelper.newQuery(
						this.entityManager.getExtent(Address.class),
						segment.refGetPath().getDescendant("address", ":*"));
				List<Address> selection = segment.<Address>getExtent(extentQuery);
				this.entityManager.retrieveAll(selection);
			} finally {
				super.taskId = null;
			}
			//
			// Refresh Extent
			//
			try {
				super.taskId = "CR20020326";
				EmailAddressQuery extentQuery = (EmailAddressQuery) PersistenceHelper.newQuery(
						this.entityManager.getExtent(EmailAddress.class),
						segment.refGetPath().getDescendant("address", ":*"));
				extentQuery.address().like("hans.muster@.*");
				List<EmailAddress> selection = segment.<EmailAddress>getExtent(extentQuery);
				this.entityManager.retrieveAll(selection);
			} finally {
				super.taskId = null;
			}
			// invoke sendMessageTemplate (struct with object reference field)
			this.begin();
			MessageTemplate messageTemplate = messageTemplateClass.createMessageTemplate();
			messageTemplate.setText("hello world");
			segment.addMessageTemplate(false, "template0", messageTemplate);
			this.commit();
			this.begin();
			EmailAddressSendMessageTemplateParams emailAddressSendMessageTemplateParams;
			switch (this.nextStructureCreation()) {
			case BY_PACKAGE:
				emailAddressSendMessageTemplateParams = app1Package
						.createEmailAddressSendMessageTemplateParams(messageTemplate, 0, "hello world");
				break;
			case BY_MEMBER:
				emailAddressSendMessageTemplateParams = Datatypes.create(EmailAddressSendMessageTemplateParams.class,
						Datatypes.member(EmailAddressSendMessageTemplateParams.Member.body, messageTemplate),
						Datatypes.member(EmailAddressSendMessageTemplateParams.Member.priority, 0),
						Datatypes.member(EmailAddressSendMessageTemplateParams.Member.subject, "hello world"));
				break;
			case BY_POSITION:
				emailAddressSendMessageTemplateParams = Datatypes.create(EmailAddressSendMessageTemplateParams.class,
						messageTemplate, 0, "hello world");
				break;
			default:
				emailAddressSendMessageTemplateParams = null;
			}
			//
			// CR20020011 PersistenceHelper.clone()
			//
			try {
				super.taskId = "CR20020011";
				MessageTemplate clone = PersistenceHelper.clone(messageTemplate);
				segment.addMessageTemplate(false, "CR20020011", clone);
			} finally {
				super.taskId = null;
			}
			EmailAddressSendMessageTemplateResult sendResult = emailAddress
					.sendMessageTemplate(emailAddressSendMessageTemplateParams);
			Assertions.assertNotNull(sendResult, "Send result");
			{
				PersistenceManager targetManager = ReducedJDOHelper.getPersistenceManager(emailAddress);
				Assertions.assertSame(this.entityManager, targetManager, "Operation Target Manager");
				targetManager.flush();
				MessageTemplate deliveredBody = sendResult.getDeliveredBody();
				Assertions.assertNotNull(messageTemplate, "messageTemplate");
				PersistenceManager resultManager = ReducedJDOHelper.getPersistenceManager(deliveredBody);
				Assertions.assertSame(targetManager, resultManager, "Operation Result Manager");
				Assertions.assertEquals("hello world",  deliveredBody.getText(), "Body");
			}
			this.commit();

			// create a person without qualifier
			Person person;

			//
			// CR20019366 Marshalling
			//
			if (isBackedUpByStandardDB())
				try {
					super.taskId = "CR20019366";
					Collection<Person> people = segment.getPerson();
					Person aPerson = people.iterator().next();
					Assertions.assertSame(ReducedJDOHelper.getPersistenceManager(segment), ReducedJDOHelper.getPersistenceManager(aPerson), "segment.getPerson()");
				} finally {
					super.taskId = null;
				}

			//
			// CR0003390 Code Accessor
			//
			try {
				super.taskId = "CR0003390";
				this.begin();
				person = segment.getPerson(false, "DOE");
				Runtime runtime = Runtime.getRuntime();
				super.entityManager.makeTransactional(segment);
				long initialMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
				for (int i = 1, limit = 10000; i < 1000; i++) {
					person = segment.getPerson("DOE");
					long currentMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
					long additionalMemoryUsage = currentMemoryUsage - initialMemoryUsage;
					if (additionalMemoryUsage > limit) {
						runtime.gc();
						currentMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
						additionalMemoryUsage = currentMemoryUsage - initialMemoryUsage;
						Assertions.assertFalse(additionalMemoryUsage > limit, "Memory used up after " + i + " failed retrievals: " + additionalMemoryUsage);
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
				Assertions.assertEquals( (this instanceof AbstractLocalConnectionTest),   (person instanceof NaturalPerson), "Mix-in");
				person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1963-01-01"));
				person.setLastName("Rossi");
				person.setSalutation("Signor");
				person.getGivenName().add("Alfonso");
				int age = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR) - 1963;
				Assertions.assertEquals(age, person.getAge(), "Age");
				person.setSex((short) 0);
				segment.addPerson(false, nextId(), person);
				this.commit();
				Assertions.fail("'Signor' was expected not to be supported");
			} catch (JDOFatalDataStoreException exception) {
				System.out.println("Unsupported language prevents commit");
			} finally {
				super.taskId = null;
			}

			try {
				super.taskId = "CR20019721";
				this.begin();
				person = personClass.createPerson();
				person.setForeignId("YF");
				XMLGregorianCalendar birthDate = DatatypeFactories.xmlDatatypeFactory()
						.newXMLGregorianCalendar("1960-01-01");
				birthDate.setTimezone(-1);
				person.setBirthdate(birthDate);
				person.setBirthdateAsDateTime(Datatypes.create(Date.class, "19600101T120000.000Z"));
				Assertions.assertEquals("1960-01-01T12:00:00.000Z",  DateTimeFormat.EXTENDED_UTC_FORMAT.format(person.getBirthdateAsDateTime()), "Born at noon");
				person.setLastName("MusterX");
				person.setSalutation("Herr");
				person.setSex((short) 0);
				person.setGivenName("Hans", "Heiri");
				SparseArray<String> additionalInfo = person.getAdditionalInfo();
				additionalInfo.put(0, "Null");
				additionalInfo.put(2, "Zwei");
				person.getAssignedAddress().addAll(Arrays.asList(postalAddress, emailAddress));
				if (this instanceof ProxyConnectionTest) {
					try {
						segment.addPerson(person);
						Assertions.fail("Birthdate with time zone is invalid: " + birthDate);
					} catch (JmiServiceException expected) {
						BasicException exceptionStack = BasicException.toExceptionStack(expected);
						if (this.causeIsGeneric()) {
							Assertions.assertEquals( BasicException.Code.GENERIC,   exceptionStack.getExceptionCode(), "Unit of work has to be rolled back");
						} else {
							Assertions.assertEquals( BasicException.Code.TRANSFORMATION_FAILURE,   exceptionStack.getExceptionCode(), "Unit of work has to be rolled back");
							Assertions.assertEquals(CommException.class.getName(),  exceptionStack.getExceptionClass(), "Unit of work has to be rolled back");
							if (BasicException.Code.GENERIC == exceptionStack.getCause(null).getExceptionCode()) {
								Assertions.assertEquals(org.xml.sax.SAXException.class.getName(),  exceptionStack.getCause(null).getExceptionClass(), "cause");
							} else {
								Assertions.assertEquals( BasicException.Code.BAD_PARAMETER,   exceptionStack.getCause(null).getExceptionCode(), "cause");
								Assertions.assertEquals(IllegalArgumentException.class.getName(),  exceptionStack.getCause(null).getExceptionClass(), "cause");
							}
						}
						System.out.println("ProxyConnectionTest: " + exceptionStack.getCause(null).getExceptionClass());
					} finally {
						this.rollback();
					}
				} else if (this instanceof OptimisticContainerManagedTransactionTest) {
					try {
						segment.addPerson(person);
						this.commit();
						Assertions.fail("Birthdate with time zone is invalid: " + birthDate);
					} catch (JDOFatalDataStoreException expected) {
						BasicException exceptionStack = BasicException.toExceptionStack(expected);
						Assertions.assertEquals( BasicException.Code.ROLLBACK,   exceptionStack.getExceptionCode(), "Unit of work has to be rolled back");
						Assertions.assertEquals( BasicException.Code.GENERIC,   exceptionStack.getCause(null).getExceptionCode(), "Initial Cause");
						Assertions.assertTrue(IllegalArgumentException.class.getName()
						.equals(exceptionStack.getCause(null).getExceptionClass()) || // JRE 6
						NumberFormatException.class.getName()
								.equals(exceptionStack.getCause(null).getExceptionClass()), "Initial Cause");
					}
				} else if (this instanceof PessimisticContainerManagedTransactionTest) {
					try {
						segment.addPerson(person);
						this.commit();
						Assertions.fail("Birthdate with time zone is invalid: " + birthDate);
					} catch (JDOFatalDataStoreException expected) {
						BasicException exceptionStack = BasicException.toExceptionStack(expected);
						Assertions.assertEquals( BasicException.Code.ROLLBACK,   exceptionStack.getExceptionCode(), "Unit of work should have been rolled back");
						Assertions.assertEquals( BasicException.Code.GENERIC,   exceptionStack.getCause(null).getExceptionCode(), "The initial case is a RuntimeException");
						Assertions.assertTrue(IllegalArgumentException.class.getName()
						.equals(exceptionStack.getCause(null).getExceptionClass()) || // JRE 6
						NumberFormatException.class.getName()
								.equals(exceptionStack.getCause(null).getExceptionClass()), "Initial Cause");
					}
				} else {
					if (isBackedUpByStandardDB()) {
						try {
							segment.addPerson(person);
							this.commit();
							Assertions.fail("Birthdate with time zone is invalid: " + birthDate);
						} catch (JDOFatalDataStoreException expected) {
							BasicException exceptionStack = BasicException.toExceptionStack(expected);
							Assertions.assertEquals( BasicException.Code.ROLLBACK,   exceptionStack.getExceptionCode(), "Unit of work should have been rolled back");
							Assertions.assertEquals( BasicException.Code.GENERIC,   exceptionStack.getCause(null).getExceptionCode(), "Initial Cause");
							Assertions.assertTrue(IllegalArgumentException.class.getName()
							.equals(exceptionStack.getCause(null).getExceptionClass()) || // JRE 6
							NumberFormatException.class.getName()
									.equals(exceptionStack.getCause(null).getExceptionClass()), "Initial Cause");
						}
					} else {
						this.rollback();
					}
				}
			} finally {
				super.taskId = null;
			}

			try {
				super.taskId = "CR20020187";
				this.begin();
				person = personClass.createPerson();
				person.setForeignId("YF");
				XMLGregorianCalendar birthDate = Datatypes.create(XMLGregorianCalendar.class, "1960-01-01");
				person.setBirthdate(birthDate);
				person.setBirthdateAsDateTime(Datatypes.create(Date.class, "19600101T120000.000Z"));
				Assertions.assertEquals("1960-01-01T12:00:00.000Z",  DateTimeFormat.EXTENDED_UTC_FORMAT.format(person.getBirthdateAsDateTime()), "Born at noon");
				person.setLastName("MusterX");
				person.setSalutation("Herr");
				person.setSex((short) 0);
				person.getGivenName().add("Hans");
				person.getGivenName().add("Heiri");
				SparseArray<String> additionalInfo = person.getAdditionalInfo();
				additionalInfo.put(0, "Null");
				additionalInfo.put(2, "Zwei");
				person.getAssignedAddress().addAll(Arrays.asList(postalAddress, emailAddress));
				Path personId = segment.refGetPath().getDescendant("person", nextId());
				try {
					ReducedJDOHelper.getPersistenceManager(segment).getObjectById(personId);
					Assertions.fail("Person does not yet exist");
				} catch (JDOObjectNotFoundException exception) {
					// Person does not yet exist
				}
				segment.addPerson(false, personId.getLastSegment().toClassicRepresentation(), person);
				this.commit();
			} finally {
				super.taskId = null;
			}
			//
			// CR20020554
			//
			try {
				super.taskId = "CR20020554";
				this.begin();
				GenericAddress address = entityManager.newInstance(GenericAddress.class);
				StringProperty telexNumber = entityManager.newInstance(StringProperty.class);
				telexNumber.setDescription("A Swiss telex number of maybe inapproriate length");
				address.addProperty("telex", telexNumber);
				segment.addAddress(false, "CR20020554", address);
				person.getAssignedAddress().add(address);
				this.commit();
				{
					StringPropertyQuery numberQuery = (StringPropertyQuery) entityManager
							.newQuery(org.openmdx.generic1.cci2.StringProperty.class);
					numberQuery.forAllStringValue().equalTo("45112200");
					numberQuery.thereExistsDescription().startsWith("A Swiss");
					Collection<StringPropertyQuery> selectedNumbers = PersistenceHelper.asSubquery(numberQuery);
					GenericAddressQuery addressQuery = (GenericAddressQuery) entityManager
							.newQuery(GenericAddress.class);
					addressQuery.thereExistsProperty().elementOf(selectedNumbers);
					List<Address> selectedAddresses = segment.getAddress(addressQuery);
					Assertions.assertEquals(1,  selectedAddresses.size());
				}
				this.begin();
				telexNumber.getStringValue().put(Integer.valueOf(0), "45112233");
				this.commit();
				{
					StringPropertyQuery numberQuery = (StringPropertyQuery) entityManager
							.newQuery(org.openmdx.generic1.cci2.StringProperty.class);
					numberQuery.forAllStringValue().equalTo("45112200");
					numberQuery.thereExistsDescription().startsWith("A Swiss");
					Collection<StringPropertyQuery> selectedNumbers = PersistenceHelper.asSubquery(numberQuery);
					GenericAddressQuery addressQuery = (GenericAddressQuery) entityManager
							.newQuery(GenericAddress.class);
					addressQuery.thereExistsProperty().elementOf(selectedNumbers);
					List<Address> selectedAddresses = segment.getAddress(addressQuery);
					Assertions.assertTrue(selectedAddresses.isEmpty());
				}
				if (isBackedUpByStandardDB()) {
					{
						StringPropertyQuery numberQuery = (StringPropertyQuery) entityManager
								.newQuery(org.openmdx.generic1.cci2.StringProperty.class);
						numberQuery.forAllStringValue().equalTo("45112233");
						numberQuery.thereExistsDescription().startsWith("A Swiss");
						Collection<StringPropertyQuery> selectedNumbers = PersistenceHelper.asSubquery(numberQuery);
						GenericAddressQuery addressQuery = (GenericAddressQuery) entityManager
								.newQuery(GenericAddress.class);
						addressQuery.thereExistsProperty().elementOf(selectedNumbers);
						List<Address> selectedAddresses = segment.getAddress(addressQuery);
						Assertions.assertEquals(1,  selectedAddresses.size());
					}
					{
						StringPropertyQuery numberQuery = (StringPropertyQuery) entityManager
								.newQuery(org.openmdx.generic1.cci2.StringProperty.class);
						numberQuery.thereExistsDescription().startsNotWith("A German");
						numberQuery.thereExistsStringValue().like("45.*");
						Collection<StringPropertyQuery> selectedNumbers = PersistenceHelper.asSubquery(numberQuery);
						GenericAddressQuery addressQuery = (GenericAddressQuery) entityManager
								.newQuery(GenericAddress.class);
						addressQuery.thereExistsProperty().elementOf(selectedNumbers);
						List<Address> selectedAddresses = segment.getAddress(addressQuery);
						Assertions.assertEquals(1,  selectedAddresses.size());
					}
				}
				{
					StringPropertyQuery numberQuery = (StringPropertyQuery) entityManager
							.newQuery(org.openmdx.generic1.cci2.StringProperty.class);
					numberQuery.thereExistsStringValue().elementOf("45112200");
					Collection<StringPropertyQuery> selectedNumbers = PersistenceHelper.asSubquery(numberQuery);
					GenericAddressQuery addressQuery = (GenericAddressQuery) entityManager
							.newQuery(GenericAddress.class);
					addressQuery.thereExistsProperty().elementOf(selectedNumbers);
					List<Address> selectedAddresses = segment.getAddress(addressQuery);
					Assertions.assertTrue(selectedAddresses.isEmpty());
				}
				if (isBackedUpByStandardDB()) {
					StringPropertyQuery numberQuery = (StringPropertyQuery) entityManager
							.newQuery(org.openmdx.generic1.cci2.StringProperty.class);
					numberQuery.thereExistsStringValue().elementOf("45112233", "45112234", "45112235");
					numberQuery.thereExistsDescription().startsWith("A Swiss");
					Collection<StringPropertyQuery> selectedNumbers = PersistenceHelper.asSubquery(numberQuery);
					GenericAddressQuery addressQuery = (GenericAddressQuery) entityManager
							.newQuery(GenericAddress.class);
					addressQuery.thereExistsProperty().elementOf(selectedNumbers);
					Collection<Address> selectedAddresses = segment.getAddress(addressQuery);
					Assertions.assertEquals(1,  selectedAddresses.size());
					SegmentQuery segmentQuery = (SegmentQuery) entityManager
							.newQuery(test.openmdx.app1.jmi1.Segment.class);
					selectedAddresses = PersistenceHelper.asSubquery(addressQuery);
					segmentQuery.thereExistsAddress().elementOf(selectedAddresses);
					List<Segment> selectedSegments = provider.getSegment(segmentQuery);
					Assertions.assertEquals(1,  selectedSegments.size());
				}
			} finally {
				super.taskId = null;
			}
			Path personId = (Path) ReducedJDOHelper.getObjectId(person);
			if (!(this instanceof AbstractContainerManagedTransactionTest)) {
				{
					this.begin();
					person.getAdditionalInfo().put(10, "Ten");
				}
				//
				// CR20019182 persistenceCapable.equals()
				//
				try {
					super.taskId = "CR20019182";
					PersistenceManager anotherPersistenceManager = super.entityManager.getPersistenceManagerFactory()
							.getPersistenceManager();
					Person anotherPerson = (Person) anotherPersistenceManager.getObjectById(personId);
					Assertions.assertNotSame(person, anotherPerson, "Same person in different persistence managers");
					Assertions.assertEquals(person,  anotherPerson, "Same person in different persistence managers");
					SparseArray<String> anotherInfo = anotherPerson.getAdditionalInfo();
					Assertions.assertEquals( 2,   anotherInfo.size(), "CR20018969");
					Assertions.assertEquals("Null",  anotherInfo.get(0), "CR20018969.0");
					Assertions.assertNull(anotherInfo.get(1), "CR20018969.1");
					Assertions.assertEquals("Zwei",  anotherInfo.get(2), "CR20018969.2");
					Assertions.assertNull(anotherInfo.get(3), "CR20018969.3");
					Assertions.assertTrue(anotherInfo.subMap(1, 2).isEmpty(), "CR20018969.1_2");
					SparseArray<String> tail = anotherInfo.tailMap(1);
					Assertions.assertEquals( 1,   tail.size(), "CR20018969.1_");
					SparseArray<String> head = anotherInfo.headMap(1);
					Assertions.assertEquals( 1,   head.size(), "CR20018969._1");
					Assertions.assertEquals("Null",  head.get(0), "CR20018969.0");
					Assertions.assertNull(tail.get(0), "CR20018969.0");
					Assertions.assertNull(head.get(2), "CR20018969.2");
					Assertions.assertEquals("Zwei",  tail.get(2), "CR20018969.2");
					PersistenceHelper.currentUnitOfWork(anotherPersistenceManager).begin();
					for (Map.Entry<Integer, String> e : anotherInfo.entrySet()) {
						e.setValue(e.getKey().toString());
					}
					PersistenceHelper.currentUnitOfWork(anotherPersistenceManager).commit();
				} finally {
					super.taskId = null;
				}
				if (isBackedUpByStandardDB()) {
					try {
						this.commit();
						Assertions.fail("CONCURRENT_ACCESS_FAILURE expected");
					} catch (JDOOptimisticVerificationException exception) {
						Throwable cause = exception.getCause();
						Assertions.assertTrue(cause instanceof JDOOptimisticVerificationException, "A nested exception per object");
						BasicException exceptionStack = BasicException.toExceptionStack(cause);
						Assertions.assertEquals( BasicException.Code.ROLLBACK,   exceptionStack.getExceptionCode(), "ROLLBACK expected");
						Assertions.assertEquals( BasicException.Code.CONCURRENT_ACCESS_FAILURE,   exceptionStack.getCause(null).getExceptionCode(), "CONCURRENT_ACCESS_FAILURE expected");
					}
				} else {
					this.rollback();
				}
				{
					PersistenceManager anotherPersistenceManager = super.entityManager.getPersistenceManagerFactory()
							.getPersistenceManager();
					Person anotherPerson = (Person) anotherPersistenceManager.getObjectById(personId);
					SparseArray<String> anotherInfo = anotherPerson.getAdditionalInfo();
					Assertions.assertEquals( 2,   anotherInfo.size(), "CR20018969");
					Assertions.assertEquals("0",  anotherInfo.get(0), "CR20018969.0");
					Assertions.assertNull(anotherInfo.get(1), "CR20018969.1");
					Assertions.assertEquals("2",  anotherInfo.get(2), "CR20018969.2");
					Assertions.assertNull(anotherInfo.get(3), "CR20018969.3");
					Assertions.assertTrue(anotherInfo.subMap(1, 2).isEmpty(), "CR20018969.1_2");
					SparseArray<String> tail = anotherInfo.tailMap(1);
					Assertions.assertEquals( 1,   tail.size(), "CR20018969.1_");
					SparseArray<String> head = anotherInfo.headMap(1);
					Assertions.assertEquals( 1,   head.size(), "CR20018969._1");
					Assertions.assertEquals("0",  head.get(0), "CR20018969.0");
					Assertions.assertNull(tail.get(0), "CR20018969.0");
					Assertions.assertNull(head.get(2), "CR20018969.2");
					Assertions.assertEquals("2",  tail.get(2), "CR20018969.2");
				}
			}
			Path pId = (Path) ReducedJDOHelper.getObjectId(person);
			Assertions.assertEquals( 1,   (new Path(person.refMofId()).size() % 2), "person.refMofId() must be object path");
			Assertions.assertEquals( 1,   (pId.size() % 2), "person's path must be object path");
			Assertions.assertEquals(pId.toXRI(),  person.getIdentity(), "person.refIdentity() must corrspond to its path");
			Assertions.assertEquals(pId.toXRI(),  person.refMofId(), "person.refMofId() must corrspond to its path");

			if (!(this instanceof AbstractContainerManagedTransactionTest))
				try {
					super.taskId = "CR20020005";
					PersistenceManagerFactory persistenceManagerFactory = super.entityManager
							.getPersistenceManagerFactory();
					PersistenceManager persistenceManager1 = persistenceManagerFactory.getPersistenceManager();
					Assertions.assertEquals(Constants.TX_REPEATABLE_READ,  persistenceManagerFactory.getTransactionIsolationLevel(), "Transaction Isolation Level");
					{
						final Date transactionTime1 = new Date(System.currentTimeMillis() - 20L);
						UserObjects.setTransactionTime(persistenceManager1, new Factory<Date>() {
							public Date instantiate() {
								return transactionTime1;
							}

							public Class<? extends Date> getInstanceClass() {
								return Date.class;
							}
						});
					}
					PersistenceManager persistenceManager2 = super.entityManager.getPersistenceManagerFactory()
							.getPersistenceManager();
					{
						final Date transactionTime2 = new Date(System.currentTimeMillis() - 10L);
						UserObjects.setTransactionTime(persistenceManager2, new Factory<Date>() {
							public Date instantiate() {
								return transactionTime2;
							}

							public Class<? extends Date> getInstanceClass() {
								return Date.class;
							}
						});
					}
					{
						PersistenceHelper.currentUnitOfWork(persistenceManager2).begin();
						Person person2 = (Person) persistenceManager2.getObjectById(personId);
						person2.getGivenName().add("Maria");
						PersistenceHelper.currentUnitOfWork(persistenceManager2).commit();
					}
					if (this instanceof AbstractLocalConnectionTest) {
						if (isBackedUpByStandardDB()) {
							try {
								PersistenceHelper.currentUnitOfWork(persistenceManager1).begin();
								Person person1 = (Person) persistenceManager1.getObjectById(personId);
								person1.getGivenName().add("Klaus");
								PersistenceHelper.currentUnitOfWork(persistenceManager1).commit();
								Assertions.fail("Write lock failure expected");
							} catch (JDOOptimisticVerificationException expected) {
								// Expected exception
							}
							try {
								PersistenceHelper.currentUnitOfWork(persistenceManager1).begin();
								Person person1 = (Person) persistenceManager1.getObjectById(personId);
								persistenceManager1.makeTransactional(person1);
								PersistenceHelper.currentUnitOfWork(persistenceManager1).commit();
								Assertions.fail("Read lock failure expected");
							} catch (JDOOptimisticVerificationException expected) {
								// Expected exception
							}
						}
						{
							PersistenceHelper.currentUnitOfWork(persistenceManager1).begin();
							Person person1 = (Person) persistenceManager1.getObjectById(personId);
							persistenceManager1.makeTransactional(person1);
							persistenceManager1.refresh(person1);
							PersistenceHelper.currentUnitOfWork(persistenceManager1).commit();
						}
						PersistenceManager persistenceManager3 = super.entityManager.getPersistenceManagerFactory()
								.getPersistenceManager();
						{
							PersistenceHelper.currentUnitOfWork(persistenceManager3).begin();
							Person person3 = (Person) persistenceManager3.getObjectById(personId);
							persistenceManager3.makeTransactional(person3);
							PersistenceHelper.currentUnitOfWork(persistenceManager3).commit();
						}
					}
				} finally {
					super.taskId = null;
				}

			Assertions.assertEquals("8005",  postalAddress.getPostalCode(), "Initial postal code without country code");

			// Add country code to postal code
			entityManager.refresh(person);
			this.begin();
			person.voidOp();
			//
			// The postal address object itself is untouched
			// but has been modified by an operation on the person object
			//
			super.entityManager.makeTransactional(postalAddress);
			this.commit();
			Assertions.assertEquals("CH-8005",  postalAddress.getPostalCode(), "voidOp should have updated the postal codes");

			// get assigned addresses by index
			for (int i = 0; i < 2; i++) {
				// postal code not yet refreshed
				List<Address> addresses = person.getAssignedAddress();
				Address address = addresses.get(i);
				System.out.println("assigned address=" + ReducedJDOHelper.getObjectId(address));
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
				person.getAssignedAddress().addAll(Arrays.asList(postalAddress, additionalAddress, emailAddress));
				this.commit();
			} finally {
				super.taskId = null;
			}
			//
			// CR20020252 Join by reference
			//
			if (isBackedUpByStandardDB())
				try {
					super.taskId = "CR20020252";
					PostalAddressQuery additionalAddressQuery = (PostalAddressQuery) super.entityManager
							.newQuery(PostalAddress.class);
					additionalAddressQuery.street().equalTo("Technoparkstrasse");
					Collection<PostalAddress> additionalAddresses = PersistenceHelper
							.<PostalAddress>asSubquery(additionalAddressQuery);
					PersonQuery personQuery = (PersonQuery) super.entityManager.newQuery(Person.class);
					personQuery.thereExistsAssignedAddress().elementOf(additionalAddresses);
					List<Person> people = segment.getPerson(personQuery);
					Assertions.assertEquals( 1,   people.size(), "Join by reference");
					Assertions.assertSame(person, people.iterator().next(), "Join by reference");
				} finally {
					super.taskId = null;
				}
			List<Address> assignedAddresses = person.getAssignedAddress();
			for (Address address : assignedAddresses) {
				// postal code refreshed
				if (ReducedJDOHelper.getObjectId(address).equals(ReducedJDOHelper.getObjectId(postalAddress))) {
					if (address instanceof DelegatingRefObject_1_0) {
						Assertions.assertSame(((DelegatingRefObject_1_0) address).openmdxjdoGetDataObject(), ((DelegatingRefObject_1_0) postalAddress).openmdxjdoGetDataObject(), "created and retrieved object should be the same");
					} else {
						Assertions.assertSame(((RefObject_1_0) address).refDelegate(), ((RefObject_1_0) postalAddress).refDelegate(), "created and retrieved object should be the same");
					}
				}
				System.out.println("assigned address=" + ReducedJDOHelper.getObjectId(address));
			}
			Assertions.assertEquals( 6,   person.getAssignedAddress().size(), "number of assigned addresses");

			// assignAddress by operation. This operation does not really
			// perform an assign. It is just there to see whether the operation
			// invocation works.
			this.begin();
			PersonAssignAddressParams personAssignAddressParams;
			switch (this.nextStructureCreation()) {
			case BY_MEMBER:
				personAssignAddressParams = Datatypes.create(PersonAssignAddressParams.class,
						Datatypes.member(PersonAssignAddressParams.Member.address,
								Arrays.asList(new Address[] { postalAddress, emailAddress })));
				break;
			case BY_PACKAGE:
				personAssignAddressParams = app1Package
						.createPersonAssignAddressParams(Arrays.asList(new Address[] { postalAddress, emailAddress }));
				break;
			case BY_POSITION:
				personAssignAddressParams = Datatypes.create(PersonAssignAddressParams.class,
						Arrays.asList(new Address[] { postalAddress, emailAddress }));
				break;
			default:
				personAssignAddressParams = null;
			}
			//
			// CR20020140 Non-query operation
			//
			try {
				super.taskId = "CR20020140";
				Assertions.assertFalse(ReducedJDOHelper.isDirty(person), "Person is clean before the address is assigned");
				person.assignAddress(personAssignAddressParams);
				Object oldVersion = person.getModifiedAt();
				Assertions.assertTrue(ReducedJDOHelper.isDirty(person), "Person is dirty after the address has been assigned");
				this.commit();
				Object newVersion = person.getModifiedAt();
				Assertions.assertFalse(ReducedJDOHelper.isDirty(person), "Person is clean after commit");
				Assertions.assertFalse(oldVersion.equals(newVersion), "Person has been touched");
			} finally {
				super.taskId = null;
			}
			//
			// CR20018578 State After Removal
			//
			try {
				super.taskId = "CR20018578";
				this.begin();
				Assertions.assertTrue(ReducedJDOHelper.isPersistent(additionalAddress), "Persistent");
				Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalAddress), "Deleted");
				Assertions.assertNotNull(ReducedJDOHelper.getObjectId(additionalAddress), "Object Id");
				additionalAddress.refDelete();
				Assertions.assertTrue(ReducedJDOHelper.isPersistent(additionalAddress), "Persistent");
				Assertions.assertTrue(ReducedJDOHelper.isDeleted(additionalAddress), "Deleted");
				Assertions.assertNotNull(ReducedJDOHelper.getObjectId(additionalAddress), "Object Id");
				Address a = segment.getAddress("CR0002096");
				Assertions.assertNotNull(a, "Deleted additional address");
				Assertions.assertSame(additionalAddress, a, "Deleted additional address");
				this.commit();
				Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalAddress), "Persistent");
				Assertions.assertNull(ReducedJDOHelper.getObjectId(additionalAddress), "Object Id");
				Assertions.assertNotNull(ReducedJDOHelper.getTransactionalObjectId(additionalAddress), "Transactional Object Id");
				Assertions.assertNull(segment.getAddress("CR0002096"), "Deleted additional address");
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
				for (Iterator<Address> i = assignedAddresses.iterator(); i.hasNext(); j++) {
					// postal code refreshed
					Address address;
					try {
						address = i.next();
						Assertions.assertNotNull(address, "Returning null was the former behaviour");
						Assertions.assertNotNull(ReducedJDOHelper.getObjectId(address), "Returning null was the former behaviour"); // was
																															// current
																															// object
																															// id
						if (ReducedJDOHelper.getObjectId(address).equals(ReducedJDOHelper.getObjectId(postalAddress))) {
							if (address instanceof DelegatingRefObject_1_0) {
								Assertions.assertSame(((DelegatingRefObject_1_0) address).openmdxjdoGetDataObject(), ((DelegatingRefObject_1_0) postalAddress).openmdxjdoGetDataObject(), "created and retrieved object should be the same");
							} else {
								Assertions.assertSame(((RefObject_1_0) address).refDelegate(), ((RefObject_1_0) postalAddress).refDelegate(), "created and retrieved object should be the same");
							}
						}
						System.out.println("Assigned address " + j + ": " + ReducedJDOHelper.getObjectId(address));
					} catch (InvalidObjectException exception) {
						i.remove();
						System.out.println("Assigned address " + j + ": removed");
					}
				}
				this.commit();
				Assertions.assertEquals( 5,   person.getAssignedAddress().size(), "number of assigned addresses");
			} finally {
				super.taskId = null;
			}

			//
			// CR20018837
			//
			try {
				super.taskId = "CR20018837";
				System.out.println("Removal test");
				for (int i = 0; i < 8; i++) {
					boolean persistentNew = isEven(i);
					String invoiceId = this.taskId + (char) ('a' + i);
					this.begin();
					Invoice additionalInvoice = this.entityManager.newInstance(Invoice.class);
					additionalInvoice.setProductGroupId("PG" + i);
					additionalInvoice.setDescription("Invoice # " + invoiceId);
					Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalInvoice), "Step " + i + " Additional invoice not yet deleted");
					Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalInvoice), "Step " + i + " Additional invoice not yet persistent");
					Assertions.assertFalse(ReducedJDOHelper.isNew(additionalInvoice), "Step " + i + " Additional invoice not yet new");
					Assertions.assertFalse(ReducedJDOHelper.isDirty(additionalInvoice), "Step " + i + " Additional invoice not yet persistent");
					InvoicePosition additionalPosition = this.entityManager.newInstance(InvoicePosition.class);
					additionalPosition.setProductId("P" + i);
					Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalPosition), "Step " + i + " Additional position not yet deleted");
					Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalPosition), "Step " + i + " Additional position not yet persistent");
					Assertions.assertFalse(ReducedJDOHelper.isNew(additionalPosition), "Step " + i + " Additional position not yet new");
					Assertions.assertFalse(ReducedJDOHelper.isDirty(additionalPosition), "Step " + i + " Additional position not yet persistent");
					segment.addInvoice(false, invoiceId, additionalInvoice);
					Assertions.assertSame(additionalInvoice, segment.getInvoice(false, invoiceId), "Step " + i + " Created invoice retrieval");
					Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalInvoice), "Step " + i + " Additional invoice not yet deleted");
					Assertions.assertTrue(ReducedJDOHelper.isPersistent(additionalInvoice), "Step " + i + " Additional invoice now persistent");
					Assertions.assertTrue(ReducedJDOHelper.isNew(additionalInvoice), "Step " + i + " Additional invoice now new");
					Assertions.assertTrue(ReducedJDOHelper.isDirty(additionalInvoice), "Step " + i + " Additional invoice now persistent");
					additionalInvoice.addInvoicePosition(false, Integer.toString(i), additionalPosition);
					Assertions.assertSame(additionalPosition, additionalInvoice.getInvoicePosition(false, Integer.toString(i)), "Step " + i + " Created position retrieval");
					Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalPosition), "Step " + i + " Additional invoice not yet deleted");
					Assertions.assertTrue(ReducedJDOHelper.isPersistent(additionalPosition), "Step " + i + " Additional invoice now persistent");
					Assertions.assertTrue(ReducedJDOHelper.isNew(additionalPosition), "Step " + i + " Additional invoice now new");
					Assertions.assertTrue(ReducedJDOHelper.isDirty(additionalPosition), "Step " + i + " Additional invoice now persistent");
					Object positionId = ReducedJDOHelper.getObjectId(additionalPosition);
					if (!persistentNew) {
						this.commit();
						String outerTask = super.taskId;
						//
						// CR20020252 Join By Containment
						//
						try {
							super.taskId = "CR20020252";
							InvoicePositionQuery invoicePositionQuery = (InvoicePositionQuery) super.entityManager
									.newQuery(InvoicePosition.class);
							invoicePositionQuery.productId().equalTo("P" + i);
							Collection<InvoicePosition> invoicePositions = PersistenceHelper
									.<InvoicePosition>asSubquery(invoicePositionQuery);
							InvoiceQuery invoiceQuery = (InvoiceQuery) super.entityManager.newQuery(Invoice.class);
							invoiceQuery.thereExistsInvoicePosition().elementOf(invoicePositions);
							List<Invoice> invoices = segment.getInvoice(invoiceQuery);
							boolean found = false;
							for (Invoice candidate : invoices) {
								found |= candidate == additionalInvoice;
							}
							Assertions.assertTrue(found, "Join by containment");
						} finally {
							super.taskId = outerTask;
						}
						//
						// CR20020252 Join By Parent
						//
						if (isBackedUpByStandardDB())
							try {
								super.taskId = "CR20020252";
								InvoiceQuery invoiceQuery = (InvoiceQuery) super.entityManager.newQuery(Invoice.class);
								invoiceQuery.productGroupId().equalTo("PG" + i);
								Collection<Invoice> invoices = PersistenceHelper.<Invoice>asSubquery(invoiceQuery);
								InvoicePositionQuery invoicePositionQuery = (InvoicePositionQuery) PersistenceHelper
										.newQuery(entityManager.getExtent(InvoicePosition.class), segment.refGetPath()
												.getDescendant("invoice", ":*", "invoicePosition", ":*"));
								invoicePositionQuery.invoice().elementOf(invoices);
								List<InvoicePosition> invoicePositions = segment.getExtent(invoicePositionQuery);
								boolean found = false;
								for (InvoicePosition candidate : invoicePositions) {
									found |= candidate == additionalPosition;
								}
								Assertions.assertTrue(found, "Join by parent");
							} finally {
								super.taskId = outerTask;
							}
						this.begin();
					}
					switch (i / 2) {
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
					Assertions.assertSame(additionalInvoice, segment.getInvoice(false, invoiceId), "Step " + i + " Deleted invoice retrieval");
					Assertions.assertTrue(ReducedJDOHelper.isDeleted(additionalInvoice), "Step " + i + " Additional invoice now deleted");
					Assertions.assertTrue(ReducedJDOHelper.isPersistent(additionalInvoice), "Step " + i + " Additional invoice still persistent");
					Assertions.assertEquals( isEven(i),   ReducedJDOHelper.isNew(additionalInvoice), "Step " + i + " Additional invoice might be new new");
					Assertions.assertTrue(ReducedJDOHelper.isDirty(additionalInvoice), "Step " + i + " Additional invoice now deleted");
					Assertions.assertSame(additionalPosition, this.entityManager.getObjectById(positionId), "Step " + i + " Deleted position retrieval");
					if (persistentNew) {
						Assertions.assertTrue(ReducedJDOHelper.isDeleted(additionalPosition), "Step " + i + " Additional position now deleted");
						Assertions.assertTrue(ReducedJDOHelper.isPersistent(additionalPosition), "Step " + i + " Additional position still persistent");
						Assertions.assertEquals( isEven(i),   ReducedJDOHelper.isNew(additionalPosition), "Step " + i + " Additional position might be new");
						Assertions.assertTrue(ReducedJDOHelper.isDirty(additionalPosition), "Step " + i + " Additional position now deleted");
					}
					this.commit();
					Assertions.assertNull(segment.getInvoice(false, invoiceId), "Step " + i + " Deleted invoice retrieval");
					Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalInvoice), "Step " + i + " Additional invoice now transient");
					Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalInvoice), "Step " + i + " Additional invoice now transient");
					Assertions.assertFalse(ReducedJDOHelper.isNew(additionalInvoice), "Step " + i + " Additional invoice now transient");
					Assertions.assertFalse(ReducedJDOHelper.isDirty(additionalInvoice), "Step " + i + " Additional invoice now transient");
					Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalPosition), "Step " + i + " Additional position now transient");
					Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalPosition), "Step " + i + " Additional position now transient");
					Assertions.assertFalse(ReducedJDOHelper.isNew(additionalPosition), "Step " + i + " Additional position now transient");
					Assertions.assertFalse(ReducedJDOHelper.isDirty(additionalPosition), "Step " + i + " Additional position now transient");
				}
			} finally {
				super.taskId = null;
			}
			//
			// CR20020557 Idem-potent Concurrent Access
			//
			if (testConcurrentAccess())
				try {
					super.taskId = "CR20020557";
					Path anAddressId;
					{
						this.begin();
						InternationalPostalAddress anAddress = entityManager
								.newInstance(InternationalPostalAddress.class);
						anAddress.setCountry("Deutschland");
						anAddress.setCity("Berlin");
						anAddress.setHouseNumber("21-24");
						anAddress.setPostalCode("10789");
						anAddress.setStreet("Tauentzienstrasse");
						anAddress.setAddressLine("KaDeWe Berlin", "Betriebsst\u00E4tte der Karstadt Premium GmbH");
						segment.addAddress(false, "CR20020557", anAddress);
						anAddressId = anAddress.refGetPath();
						this.commit();
					}
					for (long pause = 50L; pause < 1000L; pause += 50L) {
						CountryChanger thread1 = new CountryChanger(anAddressId, "Allemagne" + pause, -1, pause);
						CountryChanger thread2 = new CountryChanger(anAddressId, "Germany" + pause, pause, pause);
						thread1.start();
						thread2.start();
						thread1.join();
						thread2.join();
						Assertions.assertTrue(thread1.isCommitted() ^ thread2.isCommitted(), "With " + pause + " ms pause one thread must fail and one succeed");
					}
					for (long pause = 50L; pause < 1000L; pause += 50L) {
						CountryChanger thread1 = new CountryChanger(anAddressId, "Deutschland" + pause, -1, pause);
						CountryChanger thread2 = new CountryChanger(anAddressId, "Deutschland" + pause, pause, pause);
						thread1.start();
						thread2.start();
						thread1.join();
						thread2.join();
						Assertions.assertTrue(thread1.isCommitted() ^ thread2.isCommitted(), "Exactly one must succed: With " + pause + " ms pause did thread1 "
						+ (thread1.isCommitted() ? "commit" : "succeed") + " and thread2 did "
						+ (thread1.isCommitted() ? "commit" : "roll back"));
					}
				} finally {
					super.taskId = null;
				}
			//
			// CR20018837 Persistent-new-deleted
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
				Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalAddress), "Additional address not yet deleted");
				Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address not yet persistent");
				Assertions.assertFalse(ReducedJDOHelper.isNew(additionalAddress), "Additional address not yet new");
				segment.addAddress(false, "9002", additionalAddress);
				Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalAddress), "Additional address not yet deleted");
				Assertions.assertTrue(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address now persistent");
				Assertions.assertTrue(ReducedJDOHelper.isNew(additionalAddress), "Additional address now new");
				additionalAddress.refDelete();
				Assertions.assertSame(additionalAddress, segment.getAddress(false, "9002"), "Deleted address retrieval");
				Assertions.assertTrue(ReducedJDOHelper.isDeleted(additionalAddress), "Additional address now deleted");
				Assertions.assertTrue(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address still persistent");
				Assertions.assertTrue(ReducedJDOHelper.isNew(additionalAddress), "Additional address still new");
				this.rollback();
				Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalAddress), "Additional address no longer deleted");
				Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address no longer persistent");
				Assertions.assertFalse(ReducedJDOHelper.isNew(additionalAddress), "Additional address no longer new");
				this.begin();
				additionalAddress = app1Package.getPostalAddress().createPostalAddress();
				additionalAddress.setCity("Liestal");
				additionalAddress.setHouseNumber("3");
				additionalAddress.setPostalCode("2222");
				additionalAddress.setStreet("Nebenstrasse");
				Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalAddress), "Additional address not yet deleted");
				Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address not yet persistent");
				Assertions.assertFalse(ReducedJDOHelper.isNew(additionalAddress), "Additional address not yet new");
				segment.addAddress(false, "9003", additionalAddress);
				Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalAddress), "Additional address not yet deleted");
				Assertions.assertTrue(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address now persistent");
				Assertions.assertTrue(ReducedJDOHelper.isNew(additionalAddress), "Additional address now new");
				segment.getAddress().remove(QualifierType.REASSIGNABLE, "9003");
				Assertions.assertSame(additionalAddress, segment.getAddress(false, "9003"), "Deleted address retrieval");
				Assertions.assertTrue(ReducedJDOHelper.isDeleted(additionalAddress), "Additional address now deleted");
				Assertions.assertTrue(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address still persistent");
				Assertions.assertTrue(ReducedJDOHelper.isNew(additionalAddress), "Additional address still new");
				this.commit();
				Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalAddress), "Additional address no longer deleted");
				Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address no longer persistent");
				Assertions.assertFalse(ReducedJDOHelper.isNew(additionalAddress), "Additional address no longer new");
				this.begin();
				additionalAddress = app1Package.getPostalAddress().createPostalAddress();
				additionalAddress.setCity("Liestal");
				additionalAddress.setHouseNumber("4");
				additionalAddress.setPostalCode("2222");
				additionalAddress.setStreet("Nebenstrasse");
				Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalAddress), "Additional address not yet deleted");
				Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address not yet persistent");
				Assertions.assertFalse(ReducedJDOHelper.isNew(additionalAddress), "Additional address not yet new");
				segment.addAddress(false, "9004", additionalAddress);
				Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalAddress), "Additional address not yet deleted");
				Assertions.assertTrue(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address now persistent");
				Assertions.assertTrue(ReducedJDOHelper.isNew(additionalAddress), "Additional address now new");
				// segment.getAddress().remove(additionalAddress);
				// assertSame("Deleted address retrieval", additionalAddress,
				// segment.getAddress(false, "9004"));
				// assertTrue("Additional address now deleted",
				// ReducedJDOHelper.isDeleted(additionalAddress));
				Assertions.assertTrue(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address still persistent");
				Assertions.assertTrue(ReducedJDOHelper.isNew(additionalAddress), "Additional address still new");
				this.rollback();
				Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalAddress), "Additional address no longer deleted");
				Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address no longer persistent");
				Assertions.assertFalse(ReducedJDOHelper.isNew(additionalAddress), "Additional address no longer new");
				//
			} finally {
				super.taskId = null;
			}
			//
			// CR20020256
			//
			try {
				super.taskId = "CR20020256";
				System.out.println("Transient container test");
				test.openmdx.app1.jmi1.Segment transientSegment = app1Package.getSegment().createSegment();
				this.begin();
				additionalAddress = app1Package.getPostalAddress().createPostalAddress();
				additionalAddress.setCity("Frick");
				additionalAddress.setHouseNumber("3");
				additionalAddress.setPostalCode("1111");
				additionalAddress.setStreet("Hauptstrasse");
				Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalAddress), "Additional address not deleted");
				Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address not persistent");
				Assertions.assertFalse(ReducedJDOHelper.isNew(additionalAddress), "Additional address not new");
				transientSegment.addAddress(false, "9003", additionalAddress);
				Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalAddress), "Additional address not deleted");
				Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address not persistent");
				Assertions.assertFalse(ReducedJDOHelper.isNew(additionalAddress), "Additional address not new");
				Assertions.assertSame(additionalAddress, transientSegment.getAddress(false, "9003"), "Transient address retrieval");
				transientSegment.getAddress().remove(QualifierType.REASSIGNABLE, "9003");
				Assertions.assertNull(transientSegment.getAddress(false, "9003"), "Removed address retrieval");
				Assertions.assertFalse(ReducedJDOHelper.isDeleted(additionalAddress), "Additional address not deleted");
				Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address not persistent");
				Assertions.assertFalse(ReducedJDOHelper.isNew(additionalAddress), "Additional address not new");
				this.rollback();
				this.begin();
				additionalAddress = app1Package.getPostalAddress().createPostalAddress();
				additionalAddress.setCity("Frick");
				additionalAddress.setHouseNumber("4");
				additionalAddress.setPostalCode("1111");
				additionalAddress.setStreet("Hauptstrasse");
				transientSegment.addAddress(false, "9004", additionalAddress);
				Assertions.assertSame(additionalAddress, transientSegment.getAddress(false, "9004"), "Transient address retrieval");
				Assertions.assertTrue(transientSegment.getAddress().remove(additionalAddress), "Transient address removal");
				Assertions.assertNull(transientSegment.getAddress(false, "9004"), "Removed address retrieval");
				this.rollback();
			} finally {
				super.taskId = null;
			}
			//
			// CR0002987
			//
			AccessorToAnotherDatabase testForeign = new GenericTableAcessor();
			try {
				super.taskId = "CR0002987";
				System.out.println("Explicit rollback test");
				this.begin();
				additionalAddress = app1Package.getPostalAddress().createPostalAddress();
				additionalAddress.setCity("Seldwyla");
				additionalAddress.setHouseNumber("0");
				additionalAddress.setPostalCode("0000");
				additionalAddress.setStreet("Kirchgasse");
				Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address not yet persistent");
				Assertions.assertFalse(ReducedJDOHelper.isNew(additionalAddress), "Additional address not yet new");
				segment.addAddress(false, "9001", additionalAddress);
				Assertions.assertTrue(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address now persistent");
				Assertions.assertTrue(ReducedJDOHelper.isNew(additionalAddress), "Additional address now new");
				if (this instanceof PessimisticContainerManagedTransactionTest && testForeign.isEnabled()) {
					Assertions.assertEquals( 1,   testForeign.insert("2PC-Test", 1, "Implicitely rolled back"), "1 row inserted");
				}
				this.rollback();
				Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address no longer persistent");
				Assertions.assertFalse(ReducedJDOHelper.isNew(additionalAddress), "Additional address no longer new");
				if (isBackedUpByStandardDB())
					try {
						System.out.println("Implicit rollback test");
						this.begin();
						additionalAddress = app1Package.getPostalAddress().createPostalAddress();
						additionalAddress.setCity("Seldwyla");
						additionalAddress.setHouseNumber("0");
						additionalAddress.setPostalCode("0000");
						additionalAddress.setStreet("Kirchgasse");
						Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address not yet persistent");
						Assertions.assertFalse(ReducedJDOHelper.isNew(additionalAddress), "Additional address not yet new");
						segment.addAddress(false, "CR0002987", additionalAddress);
						Assertions.assertTrue(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address now persistent");
						Assertions.assertTrue(ReducedJDOHelper.isNew(additionalAddress), "Additional address now new");
						NameFormat jmiNameFormat = app1Package.getNameFormat().createNameFormat();
						jmiNameFormat.setDescription("modified description");
						segment.addNameFormat(false, nextId(), jmiNameFormat);
						if (this instanceof PessimisticContainerManagedTransactionTest && testForeign.isEnabled()) {
							Assertions.assertEquals( 1,   testForeign.insert("2PC-Test", 2, "Explicitely rolled back"), "1 row inserted");
						}
						this.commit();
						Assertions.fail("constraint isFrozen --> object can not be updated");
					} catch (JDOFatalDataStoreException e) {
						Assertions.assertFalse(ReducedJDOHelper.isNew(additionalAddress), "Additional address no longer new");
						Assertions.assertFalse(ReducedJDOHelper.isPersistent(additionalAddress), "Additional address no longer persistent");
					}
			} finally {
				super.taskId = null;
			}
			try {
				super.taskId = "CR20019671";
				if (this instanceof PessimisticContainerManagedTransactionTest && testForeign.isEnabled()) {
					this.begin();
					Assertions.assertEquals( 1,   testForeign.insert("2PC-Test", 0, "Committed"), "1 row inserted");
					this.commit();
					Assertions.assertEquals( 1,   testForeign.retrieve().size(), "1 of 3 committed");
				}
				Assertions.assertNull(segment.getAddress("9001"), "CR20019671 explicit rollback");
				Assertions.assertNull(segment.getAddress("CR0002987"), "CR20019671 implicit rollback");
			} finally {
				super.taskId = null;
			}
			for (int i = 0; i < 2; i++) {
				Assertions.assertNull(segment.getPerson(false, "TRANSIENT"), "No TRANSIENT person expected");
				if (i == 0)
					this.begin();
			}
			// create and remove in same unit of work
			person = personClass.createPerson();
			person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1960-01-01"));
			person.setBirthdateAsDateTime(Datatypes.create(Date.class, "19600101T120000.000Z"));
			person.setForeignId("YF");
			person.setLastName("MusterX");
			person.setSalutation("Herr");
			person.setSex((short) 0);
			person.getMemberOfGroup().addAll(Arrays.asList(new String[] { "group A", "group B" }));
			person.getGivenName().addAll(Arrays.asList(new String[] { "Hans", "Heiri" }));
			person.getAdditionalInfo().put(Integer.valueOf(0), "additional info 1");
			person.getAdditionalInfo().put(Integer.valueOf(1), "additional info 2");
			person.getAssignedAddress().addAll(Arrays.asList(new Address[] { postalAddress, emailAddress }));

			segment.addPerson(false, "TRANSIENT", person);
			segment.getPerson(false, "TRANSIENT").refDelete(); // get and remove it in same unit of work
			this.commit();

			createPersonGroups(segment, "g");

			// create some Persons
			{
				PersonGroup g0 = segment.getPersonGroup(false, "g0");
				PersonGroup g1 = segment.getPersonGroup(false, "g1");
				PersonGroup g2 = segment.getPersonGroup(false, "g2");
				this.begin();
				for (int i = 0; i <= N_PERSONS; i++) {
					person = personClass.createPerson();
					person.setForeignId("F" + i);
					person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1960-01-01"));
					person.setBirthdateAsDateTime(Datatypes.create(Date.class, "19600101T120000.000Z"));
					person.setLastName("Muster" + i);
					person.setSalutation("Herr");
					person.setSex((short) 0);
					person.setGivenName(new String[] { "Hans", "Heiri" });
					person.getAssignedAddress().add(postalAddress);
					person.getPersonGroup().add(g0);
					person.getPersonGroup().add(g1);
					person.getPersonGroup().add(g2);
					if (i < N_PERSONS) {
						segment.addPerson(false, "000" + i, person);
					} else if (this instanceof AbstractLocalConnectionTest)
						try {
							//
							// CR20019192 UnsupportedOperationException in JMI collection delegate calls
							//
							super.taskId = "CRCR20019192";
							segment.addForeignPerson("F" + N_PERSONS, person);
							Assertions.fail("This shared assoication is expected to be unmodifiable");
						} catch (InvalidCallException expected) {
							// We expect to pass this exception handler
						} finally {
							super.taskId = null;
						}
				}
				this.commit();
			}

			// get person on 'composite' association 'SegmentHasPerson'
			person = segment.getPerson(false, "0001");
			System.out.println("person.age=" + person.getAge());
			System.out.println("person givenName=" + person.getGivenName().get(0));
			System.out.println("person.identity=" + person.getIdentity());
			System.out.println("person.creationDateTime=" + person.getCreationDateTime());
			System.out.println("person.createdAt=" + person.getCreatedAt());

			// test unqualified feature retrieval
			Assertions.assertTrue(emailAddress.refGetValue("address") instanceof String, "postalAddress.address must be instance of String");
			Assertions.assertTrue(segment.refGetValue("address") instanceof RefContainer<?>, "segment.address must be instance of Container");
			Assertions.assertTrue(emailAddress.refGetValue("address") instanceof String, "postalAddress.address must be instance of String");
			//
			// test performance of accessor.jmi of reading all non-derived attributes of
			// person INSPECTION_COUNT times
			//
			for (int j = 0; j < 2; j++) {
				startedAt = System.currentTimeMillis();
				for (int i = 0; i < INSPECTION_COUNT; i++) {
					person.getLastName();
					person.getForeignId();
					person.getGivenName();
					person.getSex();
					person.getSalutation();
					person.getBirthdate();
					person.getBirthdateAsDateTime();
					person.getAdditionalInfo();
					person.getMemberOfGroup();
					// person.getAge();
					// person.getCreationDateTime();
				}
			}
			{
				long elapsed = System.currentTimeMillis() - startedAt;
				System.out.println(
						"time for inspecting person " + INSPECTION_COUNT + " times [JMI]=" + elapsed + " ms, i.e. "
								+ BigDecimal.valueOf(elapsed).divide(
										BigDecimal.valueOf(INSPECTION_COUNT * MEMBER_COUNT), 3,
										BigDecimal.ROUND_HALF_UP)
								+ " ms per feature");
			}
			//
			// test the performance of reflective JMI accesses
			//
			startedAt = System.currentTimeMillis();
			for (int i = 0; i < INSPECTION_COUNT; i++) {
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
				System.out.println("time for inspecting person " + INSPECTION_COUNT + " times [refGetValue()]="
						+ elapsed + " ms, i.e. "
						+ BigDecimal.valueOf(elapsed).divide(BigDecimal.valueOf(INSPECTION_COUNT * MEMBER_COUNT), 3,
								BigDecimal.ROUND_HALF_UP)
						+ " ms per feature");
			}
			if (this instanceof AbstractLocalConnectionTest) {
				DataObject_1_0 personData = ((RefObject_1_0) ((DelegatingRefObject_1_0) person)
						.openmdxjdoGetDataObject()).refDelegate();
				startedAt = System.currentTimeMillis();
				for (int i = 0; i < INSPECTION_COUNT; i++) {
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
				System.out.println("time for inspecting person " + INSPECTION_COUNT + " times [objGetValue()]="
						+ elapsed + " ms, i.e. "
						+ BigDecimal.valueOf(elapsed).divide(BigDecimal.valueOf(INSPECTION_COUNT * MEMBER_COUNT), 3,
								BigDecimal.ROUND_HALF_UP)
						+ " ms per feature");
			}
			// test refMetaObject
			ModelElement_1_0 personDef = ((RefMetaObject_1) person.refMetaObject()).getElementDef();
			/* ModelElement_1_0 salutationDef = */ model.getFeatureDef(personDef, "salutation", false);
			/* salutationDef = */ model.getFeatureDef(personDef, "blabla", false);
			Map<?, ?> attributes = (Map<?, ?>) personDef.objGetValue("attribute");
			/* salutationDef = (ModelElement_1_0) */ attributes.get("salutation");

			{
				// get person on 'none', derived association 'SegmentReferencesForeignPerson'
				person = segment.getForeignPerson("F1");
				Assertions.assertNotNull(person, "Foreign Person");
				PersistenceManager m = ReducedJDOHelper.getPersistenceManager(person);
				Assertions.assertSame(super.entityManager, m, "Derived association marshalling");
			}
			{
				//
				// CR20018977 Dispatching to Association Impl
				//
				Path foreignId = segment.refGetPath().getDescendant("foreignPerson", "F1");
				Person personByForeignId = (Person) super.entityManager.getObjectById(foreignId);
				Assertions.assertSame(personByForeignId, person, "CR20018977");
			}

			System.out.println("person.age=" + person.getAge());
			System.out.println("person givenName=" + person.getGivenName().get(0));
			//
			// 20019656 isEmpty()'s iteration
			//
			final SegmentHasPerson.Person<Person> allPeople = segment.getPerson();
			try {
				super.taskId = "CR20019656";
				int count = 0;
				People: for (@SuppressWarnings("unused")
				Person aPerson : allPeople) {
					if (++count > 50) {
						break People;
					}
				}
				if (allPeople.isEmpty()) {
					Assertions.fail("There should be people");
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
			personQuery.lastName().like("Muster1.*");
			personQuery.birthdateAsDateTime().lessThanOrEqualTo(new Date());
			personQuery.orderByCreatedAt().ascending();
			SegmentHasPerson.Person<Person> personCollection;
			List<Person> personList;
			try {
				super.taskId = "CR20019366 & CR20071972";
				personCollection = segment.getPerson();
				final Person[] personArray = new Person[personCollection.size()];
				personCollection.toArray(personArray);
				for (Person person20019366 : personArray) {
					validatePerson(person20019366);
				}
				final Stream<Person> personStream = personCollection.stream();
				final Collector<Person, ?, List<Person>> personCollector = Collectors.toList();
				personList = personStream.collect(personCollector);
				for (Person person20071972 : personList) {
					validatePerson(person20071972);
				}
				personList = segment.getPerson((PersonQuery) null);
				Iterator<Person> containerIterator = personCollection.iterator();
				Iterator<Person> listIterator = personList.iterator();
				Person containerElement = containerIterator.next();
				Person listElement = listIterator.next();
				if (this instanceof ProxyConnectionTest) {
					Assertions.assertFalse(listElement instanceof NaturalPerson, "listElement");
					Assertions.assertFalse(containerElement instanceof NaturalPerson, "containerElement");
				} else {
					Assertions.assertTrue(listElement instanceof NaturalPerson, "listElement");
					Assertions.assertFalse(((NaturalPerson) listElement).isRetired(), "listElement");
					Assertions.assertTrue(containerElement instanceof NaturalPerson, "containerElement");
					Assertions.assertFalse(((NaturalPerson) containerElement).isRetired(), "containerElement");
				}
				for (Iterator<Person> i = personCollection.iterator(); i.hasNext();) {
					Person p = i.next();
					PersistenceManager m = ReducedJDOHelper.getPersistenceManager(p);
					Assertions.assertSame(super.entityManager, m, "Query result marshalling");
					break; // Test at least one persistence manager
				}
			} finally {
				super.taskId = null;
			}

			// personList = personCollection.getAll(personQuery);
			personList = segment.getPerson(personQuery);
			boolean nobodyOutThere = personList.isEmpty();
			System.out.println("There are " + (nobodyOutThere ? "no" : "some") + " people");
			Assertions.assertFalse(nobodyOutThere, "Anybody out there");
			for (Person p : personList) {
				Assertions.assertSame(super.entityManager, ReducedJDOHelper.getPersistenceManager(p), "Query result marshalling");
				SysLog.trace("person", p);
			}

			try {
				super.taskId = "CR20020554";
				{
					//
					// From Cache
					//
					personQuery = (PersonQuery) this.entityManager.newQuery(Person.class);
					personQuery.thereExistsPersonGroup().name().equalTo("Group 0");
					List<Person> people = segment.getPerson(personQuery);
					Assertions.assertEquals( 100,   people.size(), "Cached complex query");
				}
				if (isBackedUpByStandardDB()) {
					//
					// From Database
					//
					PersistenceManager anotherPersistenceManager = super.entityManager.getPersistenceManagerFactory()
							.getPersistenceManager();
					personQuery = (PersonQuery) anotherPersistenceManager.newQuery(Person.class);
					personQuery.thereExistsPersonGroup().name().equalTo("Group 0");
					test.openmdx.app1.jmi1.Segment sameSegment = anotherPersistenceManager
							.getObjectById(test.openmdx.app1.jmi1.Segment.class, segment.refMofId());
					List<Person> people = sameSegment.getPerson(personQuery);
					Assertions.assertEquals( 100,   people.size(), "Standard complex query");
				}
			} finally {
				super.taskId = null;
			}

			// get persons with SOUNDS like filter
			personQuery = app1Package.createPersonQuery();
			personQuery.lastName().like(StringTypePredicate.SOUNDS, "Maasteer");
			if (isBackedUpByStandardDB()) {
				int people = allPeople.size();
				Assertions.assertEquals( (N_PERSONS + 2),   people, "1 added by XmlImporter, 1 added with addPerson(), N_PERSONS added by addPerson()");

				List<Person> maasteer = allPeople.getAll(personQuery);
				int numberOfPersons = maasteer.size();
				Assertions.assertEquals( (TEST_PERSON_COUNT + SIMILAR_NAME_COUNT),   numberOfPersons, "number of persons found with SOUNDS_LIKE");
				this.begin();
				maasteer.clear();
				Assertions.assertEquals( 0,   segment.getPerson().size(), "Container emptied");
				this.rollback();
				maasteer = allPeople.getAll(personQuery);
				{
					Assertions.assertTrue(maasteer.listIterator(TEST_PERSON_COUNT + SIMILAR_NAME_COUNT - 1).hasNext(), "People found with SOUNDS_LIKE: Second Last");
					Assertions.assertFalse(maasteer.listIterator(TEST_PERSON_COUNT + SIMILAR_NAME_COUNT).hasNext(), "People found with SOUNDS_LIKE: Last");
				}
				numberOfPersons = maasteer.size();
				Assertions.assertEquals( (TEST_PERSON_COUNT + SIMILAR_NAME_COUNT),   numberOfPersons, "number of persons found with SOUNDS_LIKE");
				Counter<Person> counter = new PackageValidator<Person>(segment.refOutermostPackage());
				allPeople.processAll(personQuery, counter);
				Assertions.assertEquals( (TEST_PERSON_COUNT + SIMILAR_NAME_COUNT),   counter.getCount(), "number of persons found with SOUNDS_LIKE");
				this.begin();
				maasteer.clear();
				int remaining = segment.getPerson().size();
				Assertions.assertEquals( 0,   remaining, "container emptied");
				if (PROXY_IS_DIRTY_COLLECTION_AWARE || !(this instanceof ProxyConnectionTest)) {
					counter = new PackageValidator<Person>(segment.refOutermostPackage());
					allPeople.processAll(personQuery, counter);
					Assertions.assertEquals( 0,   counter.getCount(), "container emptied");
				}
				this.rollback();
			}
			//
			// CR20019185 Batching does not work for extent queries
			//
			if (isBackedUpByStandardDB())
				try {
					super.taskId = "CR20019185";
					//
					// Object Identity IS_LIKE Condition
					//
					personQuery = (PersonQuery) PersistenceHelper.newQuery(super.entityManager.getExtent(Person.class),
							segment.refMofId() + "/person/($..)");
					//
					// String Feature IS_LIKE Condition
					//
					personQuery.lastName().like(StringTypePredicate.SOUNDS, "Maasteer");
					//
					// Reference IS_LIKE Condition
					//
					personQuery.forAllAssignedAddress().elementOf(PersistenceHelper.getCandidates(
							super.entityManager.getExtent(Address.class), segment.refMofId() + "/address/($..)"));
					List<Person> maasteer = segment.getExtent(personQuery);
					int numberOfPersons = 0;
					for (@SuppressWarnings("unused")
					Person p : maasteer) {
						numberOfPersons++;
					}
					Assertions.assertEquals( (TEST_PERSON_COUNT + SIMILAR_NAME_COUNT),   numberOfPersons, "number of persons found with SOUNDS_LIKE");

				} finally {
					super.taskId = null;
				}

			//
			// CR20019185 Batching does not work for extent queries
			//
			if (isBackedUpByStandardDB())
				try {
					super.taskId = "CR20019185";
					//
					// Object Identity IS_LIKE Condition
					//
					personQuery = (PersonQuery) PersistenceHelper.newQuery(super.entityManager.getExtent(Person.class),
							segment.refMofId() + "/person/($..)");
					personQuery.orderByLastName().ascending();
					((Query) personQuery).getFetchPlan().setFetchSize(1000);
					List<Person> people = segment.getExtent(personQuery);
					int numberOfPersons = people.size();
					Assertions.assertEquals( (TEST_PERSON_COUNT + SIMILAR_NAME_COUNT),   numberOfPersons, "number of persons found with SOUNDS_LIKE");

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
				FetchPlan fetchPlan = ((Query) personQuery).getFetchPlan();
				fetchPlan.setGroup(FetchPlan.ALL);
				fetchPlan.setFetchSize(47);
				for (Person p : personCollection.getAll(personQuery)) {
					SysLog.trace("person", p);
				}
			} finally {
				super.taskId = null;
			}

			// find persons with empty filter
			personQuery = app1Package.createPersonQuery();
			personCollection = segment.getPerson();
			for (Person p : personCollection.getAll(personQuery)) {
				SysLog.trace("person", (Person) p);
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
				for (int i = 0; i < 6; i++) {
					Assertions.assertEquals( i,   pi.nextIndex(), "ListIterator.nextIndex()");
					Person pp = (Person) pi.next();
					System.out.println("person[" + i + "] " + pp.getForeignId());
				}
				for (int i = 5; i >= 0; i--) {
					Assertions.assertEquals( i,   pi.previousIndex(), "ListIterator.previousIndex()");
					Person pp = (Person) pi.previous();
					Assertions.assertEquals("F" + i,  pp.getForeignId(), "Person[" + i + "].foreignId");
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
				Assertions.assertFalse(ReducedJDOHelper.isDirty(person), "Pre-modify state");
				person.getGivenName().clear();
				Assertions.assertTrue(ReducedJDOHelper.isDirty(person), "Pre-flush state");
				Assertions.assertTrue(person.getGivenName().isEmpty(), "pre-flush attribute retrieval");
				this.entityManager.flush();
				Assertions.assertTrue(ReducedJDOHelper.isDirty(person), "Post-flush state");
				Assertions.assertTrue(person.getGivenName().isEmpty(), "Post-flush attribute retrieval");
				person.setGivenName("Heiri");
				person.setLastName("Imhof");
				person.setForeignId("HI");
				person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "20000401"));
				person.setBirthdateAsDateTime(Datatypes.create(Date.class, "20000401T120000.000Z"));
				person.setSalutation("Herr");
				this.commit();
				Assertions.assertEquals(Collections.singletonList("Heiri"),  person.getGivenName(), "givenName");
				Assertions.assertFalse(ReducedJDOHelper.isDirty(person), "Post-commit state");
			} finally {
				super.taskId = null;
			}
			//
			// Avoid null values
			//
			try {
				super.taskId = "CR20019351";
				this.begin();
				List<String> givenName = person.getGivenName();
				givenName.set(0, null);
				Assertions.fail("Null values can neither be set nor be added to lists");
			} catch (JDOUserException expected) {
				Assertions.assertTrue(expected.getNestedExceptions()[0] instanceof NullPointerException);
			} finally {
				this.rollback();
				super.taskId = null;
			}
			//
			// keep given name
			//
//            try {
//                super.taskId = "CR20081408";
//                this.begin();
//                DirtyObjects.touch(person);
//                assertTrue("Touch", ReducedJDOHelper.isDirty(person));
//                this.commit();
//            } finally {
//                super.taskId = null;
//            }
			//
			// keep given name
			//
			try {
				super.taskId = "CR20019472";
				this.begin();
				List<String> givenName = person.getGivenName();
//                givenName.set(0, "CR20019472");
				givenName.set(0, new String(givenName.get(0)));
//                assertTrue("Phantom modification", ReducedJDOHelper.isDirty(person));
				this.commit();
			} finally {
				super.taskId = null;
			}
			//
			// Empty given name
			//
			if (!isContainerManaged())
				try {
					super.taskId = "CR20019967";
					Assertions.assertFalse(person.getGivenName().isEmpty(), "A givenName");
					PersistenceManager anotherManager = newEntityManagerFactory().getPersistenceManager();
					UserObjects.setBulkLoad(anotherManager, true);
					try {
						Person samePerson = anotherManager.getObjectById(Person.class, person.refMofId());
						PersistenceHelper.currentUnitOfWork(anotherManager).begin();
						samePerson.getGivenName().clear();
						Assertions.assertTrue(samePerson.getGivenName().isEmpty(), "No givenName");
						PersistenceHelper.currentUnitOfWork(anotherManager).commit();
						Assertions.assertTrue(samePerson.getGivenName().isEmpty(), "No givenName");
						anotherManager.refresh(samePerson);
						Assertions.assertTrue(samePerson.getGivenName().isEmpty(), "No givenName");
					} finally {
						if (PersistenceHelper.currentUnitOfWork(anotherManager).isActive()) {
							PersistenceHelper.currentUnitOfWork(anotherManager).rollback();
						}
						anotherManager.close();
					}
					anotherManager = newEntityManagerFactory().getPersistenceManager();
					try {
						Person samePerson = anotherManager.getObjectById(Person.class, person.refMofId());
						Assertions.assertTrue(samePerson.getGivenName().isEmpty(), "No givenName");
					} finally {
						if (PersistenceHelper.currentUnitOfWork(anotherManager).isActive()) {
							PersistenceHelper.currentUnitOfWork(anotherManager).rollback();
						}
						anotherManager.close();
					}
					this.entityManager.refresh(person);
					Assertions.assertTrue(person.getGivenName().isEmpty(), "No givenName");
				} finally {
					super.taskId = null;
				}
			// person.formatAs
			this.begin(); // isQuery() is false
			PersonFormatNameAsParams personFormatNameAsParams;
			switch (this.nextStructureCreation()) {
			case BY_MEMBER:
				personFormatNameAsParams = Datatypes.create(PersonFormatNameAsParams.class,
						Datatypes.member(PersonFormatNameAsParams.Member.type, STANDARD_FORMAT));
				break;
			case BY_PACKAGE:
				personFormatNameAsParams = app1Package.createPersonFormatNameAsParams(STANDARD_FORMAT);
				break;
			case BY_POSITION:
				personFormatNameAsParams = Datatypes.create(PersonFormatNameAsParams.class, STANDARD_FORMAT);
				break;
			default:
				personFormatNameAsParams = null;

			}
			PersonFormatNameAsResult formattedName = person.formatNameAs(personFormatNameAsParams);
			this.commit(); // result available after commit only
			final String formattedNameString = formattedName.getFormattedName();
			Assertions.assertNotNull(formattedNameString, "formattedNameString must not be null");
			Assertions.assertFalse("formattedNameString must not be empty".isEmpty());
			System.out.println("formatted name=" + formattedNameString);
			final Set<String> formattedNameSet = formattedName.getFormattedNameAsSet();
			Assertions.assertEquals(1,  formattedNameSet.size(), "formattedNameSet must have cardinality 1");
			System.out.println("formatted name as set=" + formattedNameSet);
			final List<String> formattedNameList = formattedName.getFormattedNameAsList();
			Assertions.assertEquals(1,  formattedNameList.size(), "formattedNameList must have cardinality 1");
			System.out.println("formatted name as list=" + formattedNameList);
			final SparseArray<String> formattedNameArray = formattedName.getFormattedNameAsSparseArray();
			Assertions.assertEquals(1,  formattedNameArray.size(), "formattedNameArray must have cardinality 1");
			System.out.println("formatted name as sparsearray=" + formattedNameArray);

			// test optional argument
			this.begin(); // isQuery() is false
			switch (this.nextStructureCreation()) {
			case BY_MEMBER:
				personFormatNameAsParams = Datatypes.create(PersonFormatNameAsParams.class,
						Datatypes.member(PersonFormatNameAsParams.Member.type, null));
				break;
			case BY_PACKAGE:
				personFormatNameAsParams = app1Package.createPersonFormatNameAsParams(null // default value is Standard
				);
				break;
			case BY_POSITION:
				personFormatNameAsParams = Datatypes.create(PersonFormatNameAsParams.class, (String) null);
				break;
			default:
				personFormatNameAsParams = null;
			}
			formattedName = person.formatNameAs(personFormatNameAsParams);
			this.commit(); // result available after commit only
			System.out.println("formatted name=" + formattedNameString);
			try {
				switch (this.nextStructureCreation()) {
				case BY_MEMBER:
					personFormatNameAsParams = Datatypes.create(PersonFormatNameAsParams.class,
							Datatypes.member(PersonFormatNameAsParams.Member.type, "InvalidFormat"));
					break;
				case BY_PACKAGE:
					personFormatNameAsParams = app1Package.createPersonFormatNameAsParams("InvalidFormat");
					break;
				case BY_POSITION:
					personFormatNameAsParams = Datatypes.create(PersonFormatNameAsParams.class, "InvalidFormat");
					break;
				default:
					personFormatNameAsParams = null;
				}
				person.formatNameAs(app1Package.createPersonFormatNameAsParams("InvalidFormat"));
				Assertions.fail("CanNotFormatNameException expected");
			} catch (CanNotFormatNameException e) {
				System.out.println("formatNameAs() raised exception as expected: " + e.getMessage());
			}

			try {
				super.taskId = "CR20019666";
				throw new CanNotFormatNameException(super.taskId);
			} catch (CanNotFormatNameException e) {
				System.out.println("TestMain raised exception as expected: " + e.getMessage());
			} finally {
				super.taskId = null;
			}

			// test dateOp (date and dateTime in operation parameter)
			// Test for non-query operation with result
			this.begin();
			Date dateTimeNow = new Date();
			XMLGregorianCalendar dateIn = Datatypes.create(XMLGregorianCalendar.class,
					DateTimeFormat.BASIC_UTC_FORMAT.format(dateTimeNow).substring(0, 8));
			PersonDateOpParams personDateOpParams;
			switch (nextStructureCreation()) {
			case BY_MEMBER:
				personDateOpParams = Datatypes.create(PersonDateOpParams.class,
						Datatypes.member(PersonDateOpParams.Member.dateIn, dateIn),
						Datatypes.member(PersonDateOpParams.Member.dateTimeIn, dateTimeNow));
				break;
			case BY_PACKAGE:
				personDateOpParams = app1Package.createPersonDateOpParams(dateIn, dateTimeNow);
				break;
			case BY_POSITION:
				personDateOpParams = Datatypes.create(PersonDateOpParams.class, dateIn, dateTimeNow);
				break;
			default:
				personDateOpParams = null;
			}
			PersonDateOpResult dateOpResult = person.dateOp(personDateOpParams);
			this.commit();
			System.out.println("dateOp.dateResult=" + dateOpResult.getDateResult());
			System.out.println("dateOp.dateTimeResult=" + dateOpResult.getDateTimeResult());

			// no more NOT_FOUND exceptions
			Assertions.assertNull(segment.getPerson("alskdjflaksdjf"), "Not existing person");

			// remove some persons

			System.out.println("removing person=" + segment.getPerson("0001").getLastName());
			System.out.println("removing person=" + segment.getPerson("00053").getLastName());
			System.out.println("removing person=" + segment.getPerson("00082").getLastName());

			int initialPersonCount = segment.getPerson().size();
			this.begin();
			segment.getPerson(false, "0001").refDelete();
			segment.getPerson(false, "00053").refDelete();
			segment.getPerson(false, "00082").refDelete();
			int finalPersonCount = segment.getPerson().size();
			Assertions.assertEquals( (initialPersonCount - 3),   finalPersonCount, "Transient person count");
			this.rollback();

			finalPersonCount = segment.getPerson().size();
			Assertions.assertEquals( initialPersonCount,   finalPersonCount, "Rollback person count");
			this.begin();
			segment.getPerson(false, "0001").refDelete();
			segment.getPerson(false, "00053").refDelete();
			segment.getPerson(false, "00082").refDelete();
			finalPersonCount = segment.getPerson().size();
			Assertions.assertEquals( (initialPersonCount - 3),   finalPersonCount, "Transient person count");
			this.commit();
			finalPersonCount = segment.getPerson().size();
			Assertions.assertEquals( (initialPersonCount - 3),   finalPersonCount, "Commit person count");

			// ... and test whether they are removed
			Assertions.assertNull(segment.getPerson("0001"), "person 0001 not removed");

			//
			// CR0003390 Code Accessor
			//
			try {
				super.taskId = "CR0003390";
				this.begin();
				person = segment.getPerson("DOE");
				Assertions.assertNull(person, "DOE does not exist");
				person = personClass.createPerson();
				segment.addPerson(false, "DOE", person);
				Assertions.assertTrue(ReducedJDOHelper.isNew(person), "DOE is persistent-new");
				Assertions.assertFalse(ReducedJDOHelper.isDeleted(person), "DOE is persistent-new");
				person.refDelete();
				person = segment.getPerson("DOE");
				Assertions.assertTrue(ReducedJDOHelper.isNew(person), "DOE is persistent-new-deleted");
				Assertions.assertTrue(ReducedJDOHelper.isDeleted(person), "DOE is persistent-new-deleted");
				this.rollback();

				// Add after failed get
				Assertions.assertNull(segment.getPerson("NO1"), "person NO1 exists");

				this.begin();
				person = personClass.createPerson();
				person.setForeignId("X1");
				person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1961-11-11"));
				person.setBirthdateAsDateTime(Datatypes.create(Date.class, "19611111T120000.000Z"));
				person.setLastName("Muster1");
				person.setSalutation("Herr");
				person.setSex((short) 0);
				person.getGivenName().add("Hans");
				person.getGivenName().add("Heiri");
				person.setGivenName(new String[] { "Hans", "Heiri" });
				person.getAssignedAddress().add(postalAddress);
				segment.addPerson(false, "NO1", person);
				this.rollback();

				// ... and test whether they are removed
				Assertions.assertNull(segment.getPerson(false, "NO1"), "Person N01");
				segment.getPerson().remove(QualifierType.REASSIGNABLE, "NO1");

				// A non-existent person
				Assertions.assertNull(segment.getPerson(false, "NO2"), "Person N02");
				segment.getPerson().remove(QualifierType.REASSIGNABLE, "NO2");

				// Add after failed removal
				this.begin();
				person = personClass.createPerson();
				person.setForeignId("X2");
				person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1961-11-11"));
				person.setBirthdateAsDateTime(Datatypes.create(Date.class, "19611111T120000.000Z"));
				person.setLastName("Muster1");
				person.setSalutation("Herr");
				person.setSex((short) 0);
				person.getGivenName().add("Hans");
				person.getGivenName().add("Heiri");
				person.setGivenName(new String[] { "Hans", "Heiri" });
				person.getAssignedAddress().add(postalAddress);
				segment.addPerson(false, "NO2", person);
				this.rollback();

				Assertions.assertNull(segment.getPerson("00053"), "person 00053 not removed");
				Assertions.assertNull(segment.getPerson("00082"), "person 00082 not removed");

				// postalAddress.formatAs
				AddressFormatAsResult formattedAddress = null;
				AddressFormatAsParams addressFormatAsParams;
				switch (nextStructureCreation()) {
				case BY_MEMBER:
					addressFormatAsParams = Datatypes.create(AddressFormatAsParams.class,
							Datatypes.member(AddressFormatAsParams.Member.type, STANDARD_FORMAT));
					break;
				case BY_PACKAGE:
					addressFormatAsParams = app1Package.createAddressFormatAsParams(STANDARD_FORMAT);
					break;
				case BY_POSITION:
					addressFormatAsParams = Datatypes.create(AddressFormatAsParams.class, STANDARD_FORMAT);
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
				if (isBackedUpByStandardDB())
					for (Address address : addresses) {
						System.out.println("address.id=" + address.getId());
						System.out.println("address=" + address);

						// invoke sendMessage on PostalAddress
						if (address instanceof PostalAddress) {
							this.begin(); // isQuery() is false
							byte[] document = new byte[] { 'h', 'e', 'l', 'l', 'o' };
							PostalAddressSendMessageParams postalAddressSendMessageParams;
							switch (nextStructureCreation()) {
							case BY_MEMBER:
								postalAddressSendMessageParams = Datatypes.create(PostalAddressSendMessageParams.class,
										Datatypes.member(PostalAddressSendMessageParams.Member.document, document));
								break;
							case BY_PACKAGE:
								postalAddressSendMessageParams = app1Package
										.createPostalAddressSendMessageParams(document);
								break;
							case BY_POSITION:
								postalAddressSendMessageParams = Datatypes.create(PostalAddressSendMessageParams.class,
										document);
								break;
							default:
								postalAddressSendMessageParams = null;
							}
							((PostalAddress) address).sendMessage(postalAddressSendMessageParams);
							this.commit();
						} else if (address instanceof EmailAddress) {
							this.begin(); // isQuery() is false
							EmailAddressSendMessageParams emailAddressSendMessageParams;
							switch (nextStructureCreation()) {
							case BY_MEMBER:
								emailAddressSendMessageParams = Datatypes.create(EmailAddressSendMessageParams.class,
										Datatypes.member(EmailAddressSendMessageParams.Member.text, "hello"));
								break;
							case BY_PACKAGE:
								emailAddressSendMessageParams = app1Package
										.createEmailAddressSendMessageParams("hello");
								break;
							case BY_POSITION:
								emailAddressSendMessageParams = Datatypes.create(EmailAddressSendMessageParams.class,
										"hello");
								break;
							default:
								emailAddressSendMessageParams = null;
							}
							((EmailAddress) address).sendMessage(emailAddressSendMessageParams);
							this.commit();
						} else if (address instanceof GenericAddress) {
							SysLog.detail("Generic addresses are not sent");
						} else {
							Assertions.fail("address format " + address.getClass().getName() + " unknown");
						}
					}
			} finally {
				super.taskId = null;
			}

			try {
				super.taskId = "CR20019669";
				SegmentHasAddress.Address<Address> addresses = segment.getAddress();
				Assertions.assertTrue(addresses instanceof PersistenceCapable, "Persistence Capable Container");
				Assertions.assertEquals(((Path) ReducedJDOHelper.getObjectId(segment)).getChild("address"),  ReducedJDOHelper.getObjectId(addresses), "Container Id");
				PersistenceManager manager = ReducedJDOHelper.getPersistenceManager(addresses);
				Assertions.assertSame(ReducedJDOHelper.getPersistenceManager(segment), manager, "Container Persistence Manager");
			} finally {
				super.taskId = null;
			}

			try {
				super.taskId = "CR20019669";
				SegmentHasAddress.Address<Address> addresses = segment.getAddress();
				PersistenceManager manager = ReducedJDOHelper.getPersistenceManager(addresses);
				Object addressId = ReducedJDOHelper.getObjectId(addresses);
				String xri = ((Path) addressId).toXRI();
				Assertions.assertEquals(SegmentHasAddress.Address.class.getName() + ": " + xri,  addresses.toString(), "Validating a RefContainer's string representation");
				Object transientAddressId = ReducedJDOHelper.getTransactionalObjectId(addresses);
				Assertions.assertTrue(transientAddressId instanceof TransientContainerId, "Transient container id");
				{
					RefBaseObject container = (RefBaseObject) manager.getObjectById(transientAddressId);
					Assertions.assertTrue(container instanceof SegmentHasAddress.Address<?>, "The container's JMI class");
					Assertions.assertEquals(addresses, container);
					Assertions.assertSame(manager, ReducedJDOHelper.getPersistenceManager(container), "The container's manager");
					Assertions.assertEquals(xri,  container.refMofId(), "The container's id");
				}
				{
					RefBaseObject container = (RefBaseObject) manager.getObjectById(addressId);
					Assertions.assertTrue(container instanceof SegmentHasAddress.Address<?>, "The container's JMI class");
					Assertions.assertEquals(addresses, container);
					Assertions.assertEquals(xri,  container.refMofId(), "The container's id");
					Assertions.assertSame(manager, ReducedJDOHelper.getPersistenceManager(container), "The container's manager");
				}
			} finally {
				super.taskId = null;
			}

			try {
				super.taskId = "CR20019719";
				personQuery = app1Package.createPersonQuery();
				personQuery.createdAt().equalTo(new Date());
				List<Person> people = segment.<Person>getPerson().getAll(personQuery);
				Assertions.assertTrue(people.isEmpty());
			} finally {
				super.taskId = null;
			}

			try {
				super.taskId = "CR10010692";
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
				segment.addCycleMember1(false, BigDecimal.valueOf(1), member1);
				segment.addCycleMember2(false, "member2", member2);
				this.commit();

				@SuppressWarnings("unused")
				test.openmdx.app1.cci2.CycleMember1 jpaMember = this.entityManager.detachCopy(member1);

				// verify member1, member2
				member1 = segment.getCycleMember1(BigDecimal.valueOf(1));
				member2 = member1.getM2();
				System.out.println("member1" + member1);
				System.out.println("member2" + member2);
				Assertions.assertNotNull(member2, "We need a member value for the next test");
				CycleMember1Query query = app1Package.createCycleMember1Query();
				query.thereExistsM2().equalTo(member2);
				query.m2().isNonNull();
				try {
					query.thereExistsM2().equalTo(null);
					Assertions.fail("equalTo's argument must not be null");
				} catch (JmiServiceException exception) {
					Assertions.assertEquals( BasicException.Code.BAD_PARAMETER,   exception.getExceptionCode(), "equalTo(null)");
				}
			} finally {
				super.taskId = null;
			}
			if (isBackedUpByStandardDB()) {
				//
				// Binary Large Object
				//
				byte[] binaryContent = new byte[1000];
				BinaryLargeObject binaryLargeObject;
				try {
					super.taskId = "CR20020206";
					this.begin();
					Document binaryDocument = documentClass.createDocument();
					binaryDocument.setKeyword(new HashSet<String>(Arrays.asList("random", "document", "junit")));
					binaryDocument.setDescription("an empty document");
					this.commit();
					this.begin();
					Assertions.assertNull(binaryDocument.getContent(), "Initilly no content");
					for (int i = 0; i < binaryContent.length; i++) {
						binaryContent[i] = (byte) ((short) (i % 256));
					}
					binaryDocument.setContent(BinaryLargeObjects.valueOf(binaryContent));
					binaryDocument.setDescription("a random document");
					segment.addDocument(false, "myDoc", binaryDocument);
					this.commit();
					binaryDocument = (Document) segment.getDocument("myDoc");
					System.out.println("document.description=" + binaryDocument.getDescription());
					System.out.println("document.keyword=" + binaryDocument.getKeyword());
					binaryLargeObject = binaryDocument.getContent();
					this.begin();
					Assertions.assertNotNull(binaryLargeObject, "BLOB");
					Long documentSize = binaryLargeObject.getLength();
					if (documentSize != null) {
						Assertions.assertEquals(binaryContent.length, documentSize.longValue(), "document size");
					}
					for (int r = 0; r < 2; r++) {
						//
						// test with input stream method
						//
						System.out.println("verifying content (with InputStream)");
						try (InputStream contentIs = binaryLargeObject.getContent()) {
							Assertions.assertNotNull(contentIs, "A large object's stream");
							for (int i = 0; i < binaryContent.length; i += 10) {
								Assertions.assertEquals((i % 256), contentIs.read(), "content at position " + i);
								contentIs.skip(9);
							}
						}
						System.out.println("OK");
					}
					for (int r = 0; r < 2; r++) {
						//
						// test with output stream
						//
						System.out.println("verifying content (with OutputStream)");
						final byte[] octets;
						try (ByteArrayOutputStream contentOs = new ByteArrayOutputStream()) {
							binaryLargeObject.getContent(contentOs, 0);
							octets = contentOs.toByteArray();
						}
						try (InputStream contentIs = new ByteArrayInputStream(octets)) {
							for (int i = 0; i < binaryContent.length; i += 10) {
								Assertions.assertEquals((i % 256), contentIs.read(), "content at position " + i);
								contentIs.skip(9);
							}
						}
						System.out.println("OK");
					}
					binaryDocument.setDescription("A test document");
					this.commit();
					PersistenceManager differentManager = newEntityManagerFactory().getPersistenceManager();
					Document theDocument = (Document) differentManager
							.getObjectById(ReducedJDOHelper.getObjectId(binaryDocument));
					Assertions.assertEquals("A test document",  theDocument.getDescription(), "Description changed after content access");
					//
					// Modify content
					//
					this.begin();
					for (int i = 0; i < binaryContent.length; i++) {
						binaryContent[i] = (byte) ((short) (i % 137));
					}
					binaryDocument.setContent(BinaryLargeObjects.valueOf(binaryContent));
					this.commit();
					binaryLargeObject = binaryDocument.getContent();
					Assertions.assertNotNull(binaryLargeObject, "BLOB");
					documentSize = binaryLargeObject.getLength();
					if (documentSize != null) {
						Assertions.assertEquals(binaryContent.length,  documentSize.longValue(), "document size");
					}
					for (int r = 0; r < 2; r++) {
						//
						// test with input stream method
						//
						System.out.println("verifying content (with InputStream)");
						try (InputStream contentIs = binaryLargeObject.getContent()) {
							for (int i = 0; i < binaryContent.length; i += 10) {
								Assertions.assertEquals((i % 137), contentIs.read(), "Run " + r + ": content at position " + i);
								contentIs.skip(9);
							}
						}
						System.out.println("OK");
					}
					for (int r = 0; r < 2; r++) {
						//
						// test with output stream
						//
						System.out.println("verifying content (with OutputStream)");
						final byte[] octets;
						try (ByteArrayOutputStream contentOs = new ByteArrayOutputStream()) {
							binaryLargeObject.getContent(contentOs, 0);
							octets = contentOs.toByteArray();
						}
						try (InputStream contentIs = new ByteArrayInputStream(octets)) {
							for (int i = 0; i < binaryContent.length; i += 10) {
								Assertions.assertEquals( (i % 137),   contentIs.read(), "content at position " + i);
								contentIs.skip(9);
							}
						}
						System.out.println("OK");
					}
				} finally {
					super.taskId = null;
				}
				//
				// Character Large Object
				//
				char[] characterContent = new char[5000]; // > 4000 to challenge ORACLE
				CharacterLargeObject characterLargeObject;
				try {
					super.taskId = "CR20020207";
					this.begin();
					TextDocument textDocument = this.entityManager.newInstance(TextDocument.class);
					textDocument.setDescription("an empty document");
					textDocument.setKeyword(new HashSet<String>(Arrays.asList("random text", "document", "junit")));
					segment.addDocument(false, "myText", textDocument);
					this.commit();
					this.begin();
					Assertions.assertNull(textDocument.getText(), "Initially no text");
					for (int i = 0; i < characterContent.length; i++) {
						characterContent[i] = Character.isLetterOrDigit(i) ? (char) i : '_';
					}
					textDocument.setText(CharacterLargeObjects.valueOf(characterContent));
					textDocument.setDescription("a random text document");
					this.commit();
					textDocument = (TextDocument) segment.getDocument("myText");
					System.out.println("document.description=" + textDocument.getDescription());
					System.out.println("document.keyword=" + textDocument.getKeyword());
					this.begin();
					characterLargeObject = textDocument.getText();
					Assertions.assertNotNull(characterLargeObject, "CLOB");
					Long documentSize = characterLargeObject.getLength();
					if (documentSize != null) {
						Assertions.assertEquals(characterContent.length, documentSize.longValue(), "document size");
					}
					for (int r = 0; r < 2; r++) {
						//
						// test with reader method
						//
						System.out.println("verifying content (with Reader)");
						try (Reader contentIs = characterLargeObject.getContent()) {
							Assertions.assertNotNull(contentIs, "A large object's stream");
							for (int i = 0; i < characterContent.length; i += 10) {
								Assertions.assertEquals((Character.isLetterOrDigit(i) ? (char) i : '_'), contentIs.read(), "content at position " + i);
								contentIs.skip(9);
							}
						}
						System.out.println("OK");
					}
					for (int r = 0; r < 2; r++) {
						//
						// test with writer
						//
						System.out.println("verifying content (with Writer)");
						final char[] octets;
						try (CharArrayWriter contentOs = new CharArrayWriter()) {
							characterLargeObject.getContent(contentOs, 0);
							octets = contentOs.toCharArray();
						}
						try (Reader contentIs = new CharArrayReader(octets)) {
							for (int i = 0; i < characterContent.length; i += 10) {
								Assertions.assertEquals((Character.isLetterOrDigit(i) ? (char) i : '_'), contentIs.read(), "content at position " + i);
								contentIs.skip(9);
							}
						}
						System.out.println("OK");
					}
					textDocument.setDescription("A test document");
					this.commit();
					PersistenceManager differentManager = newEntityManagerFactory().getPersistenceManager();
					TextDocument theDocument = (TextDocument) differentManager
							.getObjectById(ReducedJDOHelper.getObjectId(textDocument));
					Assertions.assertEquals("A test document",  theDocument.getDescription(), "Description changed after content access");
					//
					// Modify content
					//
					this.begin();
					for (int i = 0; i < characterContent.length; i++) {
						characterContent[i] = Character.isLetterOrDigit(2 * i) ? (char) (2 * i) : '_';
					}
					textDocument.setText(CharacterLargeObjects.valueOf(characterContent));
					this.commit();
					characterLargeObject = textDocument.getText();
					Assertions.assertNotNull(binaryLargeObject, "CLOB");
					documentSize = characterLargeObject.getLength();
					if (documentSize != null) {
						Assertions.assertEquals(characterContent.length, documentSize.longValue(), "document size");
					}
					for (int r = 0; r < 2; r++) {
						//
						// test with reader method
						//
						System.out.println("verifying content (with Reader)");
						try (Reader contentIs = characterLargeObject.getContent()) {
							for (int i = 0; i < characterContent.length; i += 10) {
								Assertions.assertEquals((Character.isLetterOrDigit(2 * i) ? (char) (2 * i) : '_'),  contentIs.read(), "Run " + r + ": content at position " + i);
								contentIs.skip(9);
							}
						}
						System.out.println("OK");
					}
					for (int r = 0; r < 2; r++) {
						//
						// test with Writer
						//
						System.out.println("verifying content (with Writer)");
						final char[] text;
						try (CharArrayWriter contentOs = new CharArrayWriter()) {
							characterLargeObject.getContent(contentOs, 0);
							text = contentOs.toCharArray();
						}
						try (Reader contentIs = new CharArrayReader(text)) {
							for (int i = 0; i < characterContent.length; i += 10) {
								Assertions.assertEquals((Character.isLetterOrDigit(2 * i) ? (char) (2 * i) : '_'), contentIs.read(), "content at position " + i);
								contentIs.skip(9);
							}
						}
						System.out.println("OK");
					}
				} finally {
					super.taskId = null;
				}
				//
				// CR20018821
				//
				try {
					super.taskId = "CR20018821";
					this.begin();
					Importer.importObjects(Importer.asTarget(super.entityManager),
							Importer.asSource(new URL(Resources.toResourceXRI("test/openmdx/app1/data.xml"))));
					if (this instanceof AbstractLocalConnectionTest) {
						//
						// CR20019858
						//
						UpdateAvoidance updateAvoidance = UserObjects.getPlugInObject(this.entityManager,
								UpdateAvoidance.class);
						Assertions.assertNotNull(updateAvoidance, UpdateAvoidance.class.getSimpleName());
						updateAvoidance.touchAllDirtyObjects(this.entityManager);
					}
					this.commit();
					{
						File file = File.createTempFile("data", ".zip");
						Exporter.export(Exporter.asTarget(file, Exporter.MIME_TYPE_XML), super.entityManager, null,
								segment.refGetPath());
						System.out.println(segment.refGetPath() + " exported to " + file);
					}
					//
					// Validate date-time values
					//
					person = (Person) super.entityManager.getObjectById(personId);
					Assertions.assertEquals(Datatypes.create(Date.class, "1960-01-01T12:00:00Z"),  person.getBirthdateAsDateTime(), "Birthdate as date/time");
				} finally {
					super.taskId = null;
				}
				//
				// CR10010692
				//
				try {
					super.taskId = "CR10010692";
					PostalAddressQuery query = (PostalAddressQuery) this.entityManager.newQuery(PostalAddress.class);
					@SuppressWarnings("unused")
					List<test.openmdx.app1.cci2.PostalAddress> addresses = segment.<test.openmdx.app1.cci2.PostalAddress>getAddress()
							.getAll(query);
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
						Collection<test.openmdx.app1.cci2.Person> jpaPeople = super.entityManager
								.detachCopyAll(jmiPeople);
						Collection<test.openmdx.app1.cci2.Address> jpaAddresses = new ArrayList<test.openmdx.app1.cci2.Address>();
						cardinality = jpaPeople.size();
						Assertions.assertEquals(cardinality, jmiPeople.size(), "people");
						Assertions.assertTrue(jpaPeople instanceof ArrayList<?>, "Implementation detail");
						for (test.openmdx.app1.cci2.Person p : jpaPeople) {
							Assertions.assertFalse(ReducedJDOHelper.isDeleted(p), "deleted");
							Assertions.assertTrue(ReducedJDOHelper.isDetached(p), "detached");
							Assertions.assertFalse(ReducedJDOHelper.isDirty(p), "dirty");
							Assertions.assertFalse(ReducedJDOHelper.isNew(p), "new");
							Assertions.assertFalse(ReducedJDOHelper.isPersistent(p), "persistent");
							Assertions.assertFalse(ReducedJDOHelper.isTransactional(p), "transactional");
							if ("F10".equals(p.getForeignId())) {
								test.openmdx.app1.jpa3.Person jpaPersion = (test.openmdx.app1.jpa3.Person) p;
								test.openmdx.app1.jpa3.EmailAddress jpaAddress = new test.openmdx.app1.jpa3.EmailAddress();
								StateAccessor.getInstance().setTransactionalObjectId(jpaAddress,
										segment.refGetPath().getDescendant("address", "NoReply10"));
								String jpaAddress_Id = (String) ReducedJDOHelper.getObjectId(jpaAddress);
								jpaAddress.setAddress("noreply@openmdx.org");
								jpaAddresses.add(jpaAddress);
								jpaPersion.getGivenName().set(1, "Heinrich");
								jpaPersion.getAssignedAddress_Id().add(jpaAddress_Id);
								Assertions.assertFalse(ReducedJDOHelper.isDeleted(jpaPersion), "deleted");
								Assertions.assertTrue(ReducedJDOHelper.isDetached(jpaPersion), "detached");
								Assertions.assertTrue(ReducedJDOHelper.isDirty(jpaPersion), "dirty");
								Assertions.assertFalse(ReducedJDOHelper.isNew(jpaPersion), "new");
								Assertions.assertFalse(ReducedJDOHelper.isPersistent(jpaPersion), "persistent");
								Assertions.assertFalse(ReducedJDOHelper.isTransactional(jpaPersion), "transactional");
							}
						}
						{
							test.openmdx.app1.cci2.Document jpaDocument = (test.openmdx.app1.cci2.Document) super.entityManager
									.detachCopy(segment.getDocument("myDoc"));
							try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
								ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
								objectOutputStream.writeObject(jpaPeople);
								objectOutputStream.writeObject(jpaAddresses);
								objectOutputStream.writeObject(jpaDocument);
							}
						}
					}
					{
						final Collection<test.openmdx.app1.cci2.Person> jpaPeople;
						final Collection<test.openmdx.app1.cci2.Address> jpaAddresses;
						final test.openmdx.app1.cci2.Document jpaDocument;
						try (FileInputStream fileInputStream = new FileInputStream(file);
								ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
							jpaPeople = (Collection<test.openmdx.app1.cci2.Person>) objectInputStream.readObject();
							jpaAddresses = (Collection<test.openmdx.app1.cci2.Address>) objectInputStream.readObject();
							jpaDocument = (test.openmdx.app1.cci2.Document) objectInputStream.readObject();
							Assertions.assertEquals(cardinality, jpaPeople.size(), "people");
							Assertions.assertEquals(1, jpaAddresses.size(), "addresses");
						}
						this.begin();
						for (test.openmdx.app1.cci2.Address jpaAddress : jpaAddresses) {
							Assertions.assertFalse(ReducedJDOHelper.isDeleted(jpaAddress), "deleted");
							Assertions.assertFalse(ReducedJDOHelper.isDetached(jpaAddress), "detached");
							Assertions.assertFalse(ReducedJDOHelper.isDirty(jpaAddress), "dirty");
							Assertions.assertFalse(ReducedJDOHelper.isNew(jpaAddress), "new");
							Assertions.assertFalse(ReducedJDOHelper.isPersistent(jpaAddress), "persistent");
							Assertions.assertFalse(ReducedJDOHelper.isTransactional(jpaAddress), "transactional");
							test.openmdx.app1.cci2.Address jmiAddress = super.entityManager.makePersistent(jpaAddress);
							Assertions.assertFalse(ReducedJDOHelper.isDeleted(jmiAddress), "deleted");
							Assertions.assertFalse(ReducedJDOHelper.isDetached(jmiAddress), "detached");
							Assertions.assertTrue(ReducedJDOHelper.isDirty(jmiAddress), "dirty");
							Assertions.assertTrue(ReducedJDOHelper.isNew(jmiAddress), "new");
							Assertions.assertTrue(ReducedJDOHelper.isPersistent(jmiAddress), "persistent");
							Assertions.assertTrue(ReducedJDOHelper.isTransactional(jmiAddress), "transactional");
						}
						for (test.openmdx.app1.cci2.Person jpaPerson : jpaPeople) {
							boolean dirty = "F10".equals(jpaPerson.getForeignId());
							Assertions.assertFalse(ReducedJDOHelper.isDeleted(jpaPerson), "deleted");
							Assertions.assertTrue(ReducedJDOHelper.isDetached(jpaPerson), "detached");
							Assertions.assertEquals(dirty, ReducedJDOHelper.isDirty(jpaPerson), "dirty");
							Assertions.assertFalse(ReducedJDOHelper.isNew(jpaPerson), "new");
							Assertions.assertFalse(ReducedJDOHelper.isPersistent(jpaPerson), "new");
							Assertions.assertFalse(ReducedJDOHelper.isTransactional(jpaPerson), "transactional");
							test.openmdx.app1.cci2.Person jmiPerson = super.entityManager.makePersistent(jpaPerson);
							Assertions.assertFalse(ReducedJDOHelper.isDeleted(jmiPerson), "deleted");
							Assertions.assertFalse(ReducedJDOHelper.isDetached(jmiPerson), "detached");
							Assertions.assertEquals(dirty, ReducedJDOHelper.isDirty(jmiPerson), "dirty");
							Assertions.assertFalse(ReducedJDOHelper.isNew(jmiPerson), "new");
							Assertions.assertTrue(ReducedJDOHelper.isPersistent(jmiPerson), "persistent");
							Assertions.assertEquals(dirty, ReducedJDOHelper.isTransactional(jmiPerson), "transactional");
							if (dirty)
								Assertions.assertEquals("Heinrich",  jmiPerson.getGivenName().get(1), "Middle Name");
						}
						{
							binaryLargeObject = jpaDocument.getContent();
							Assertions.assertNotNull(binaryLargeObject, "BLOB");
							Long documentSize = binaryLargeObject.getLength();
							if (documentSize != null) {
								Assertions.assertEquals(binaryContent.length, documentSize.longValue(), "document size");
							}
							for (int r = 0; r < 2; r++) {
								//
								// test with input stream method
								//
								System.out.println("verifying content (with InputStream)");
								try (InputStream contentIs = binaryLargeObject.getContent()) {
									for (int i = 0; i < binaryContent.length; i += 10) {
										Assertions.assertEquals((i % 137), contentIs.read(), "Run " + r + ": content at position " + i);
										contentIs.skip(9);
									}
								}
								System.out.println("OK");
							}
							for (int r = 0; r < 2; r++) {
								//
								// test with output stream
								//
								System.out.println("verifying content (with OutputStream)");
								final byte[] octets;
								try (ByteArrayOutputStream contentOs = new ByteArrayOutputStream()) {
									binaryLargeObject.getContent(contentOs, 0);
									octets = contentOs.toByteArray();
								}
								try (InputStream contentIs = new ByteArrayInputStream(octets)) {
									for (int i = 0; i < binaryContent.length; i += 10) {
										Assertions.assertEquals((i % 137), contentIs.read(), "content at position " + i);
										contentIs.skip(9);
									}
								}
								System.out.println("OK");
							}
						}
						this.commit();
					}
				} finally {
					super.taskId = null;
				}
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
			//
			// Test CR20051350
			//
//            try {
//                super.taskId = "CR20051350";
//                InternationalPostalAddress jpaAddress = this.entityManager.detachCopy(postalAddress);
//                assertEquals("Hotel", jpaAddress.getAddressLine().get(0));
//            } finally {
//                super.taskId = null;
//            }
		}

		/**
		 * @param person20019366
		 */
		private void validatePerson(Person person20019366) {
			Assertions.assertNotNull(person20019366, "Person");
			if (this instanceof ProxyConnectionTest) {
				Assertions.assertFalse(person20019366 instanceof NaturalPerson, "Mix-In interface should be absent");
			} else {
				Assertions.assertTrue(person20019366 instanceof NaturalPerson, "Mix-In interface should be present");
				Assertions.assertFalse(((NaturalPerson) person20019366).isRetired(), "Person");
			}
		}

		private InternationalPostalAddress createPostalAddress(test.openmdx.app1.jmi1.Segment segment, String id)
				throws ServiceException {
			InternationalPostalAddress postalAddress = getPackage().getInternationalPostalAddress()
					.createInternationalPostalAddress();
			postalAddress.setCountry("Switzerland");
			postalAddress.setCity("Zurich");
			postalAddress.setHouseNumber("57");
			postalAddress.setPostalCode("8005");
			postalAddress.setStreet("Bahnhofstr.");
			postalAddress.setAddressLine("Familie", "Muster");
			segment.addAddress(false, id, postalAddress);
			return postalAddress;
		}

		/**
		 * Create some PersonGroups
		 */
		protected PersonGroup[] createPersonGroups(test.openmdx.app1.jmi1.Segment segment, String prefix) {
			final PersonGroup[] result = new PersonGroup[3];
			try {
				super.taskId = "CR20019430";
				PersonGroupClass personGroupClass = getPackage().getPersonGroup();
				this.begin();
				result[0] = personGroupClass.createPersonGroup();
				result[0].setName("Group 0");
				segment.addPersonGroup(false, prefix + "0", result[0]);
				result[1] = personGroupClass.createPersonGroup();
				segment.addPersonGroup(false, prefix + "1", result[1]);
				result[2] = personGroupClass.createPersonGroup();
				result[2].setName("Group 2");
				segment.addPersonGroup(false, prefix + "2", result[2]);
				if (!INCOMPLETE_OBJECTS_CAN_BE_FLUSHED) {
					result[1].setName("");
				}
				this.entityManager.flush();
				result[1].setName("Group 1");
				this.commit();
			} catch (ServiceException e) {
				e.printStackTrace();
			} finally {
				super.taskId = null;
			}
			return result;
		}

		/**
		 * @param i the argument to be tested
		 * @return {@code true} if the argument is odd
		 */
		private boolean isOdd(int i) {
			return !isEven(i);
		}

		/**
		 * @param i the argument to be tested
		 * @return {@code true} if the argument ie even
		 */
		private boolean isEven(int i) {
			return i % 2 == 0;
		}

		/**
		 * CR20020326 Dirty Extents
		 * 
		 * @param segment
		 * @param flush   tells whether the result shall be flushed before the queries
		 */
		private void testDirtyExtent(test.openmdx.app1.jmi1.Segment segment, boolean flush) {
			System.out.println("Current TX is " + this.entityManager.currentTransaction().isActive());
			try {
				super.taskId = "CR20020326";
				String productId = "P3";
				InvoicePositionQuery invoicePositionQuery = createInvoicePositionQuery(segment, productId);
				List<InvoicePosition> invoicePositions = segment.getExtent(invoicePositionQuery);
				validateInvoicePositions(invoicePositions, productId, 2);
				this.begin();
				invoicePositions.get(0).setProductId("p3");
				validateInvoicePositions(invoicePositions, productId, 1);
				if (flush) {
					this.entityManager.flush();
				}
				validateInvoicePositions(
						segment.<InvoicePosition>getExtent(createInvoicePositionQuery(segment, productId)), productId,
						1);
				this.rollback();
				validateInvoicePositions(invoicePositions, productId, 2);
				validateInvoicePositions(
						segment.<InvoicePosition>getExtent(createInvoicePositionQuery(segment, productId)), productId,
						2);
				if (PROXIED_EXTENT_IS_AMENDMENT_AWARE || !(this instanceof ProxyConnectionTest)) {
					this.begin();
					segment.<InvoicePosition>getExtent(createInvoicePositionQuery(segment, "P2")).get(0)
							.setProductId(productId);
					validateInvoicePositions(invoicePositions, productId, 3);
					if (flush) {
						this.entityManager.flush();
					}
					validateInvoicePositions(
							segment.<InvoicePosition>getExtent(createInvoicePositionQuery(segment, productId)),
							productId, 3);
					this.rollback();
				}
				validateInvoicePositions(invoicePositions, productId, 2);
				validateInvoicePositions(
						segment.<InvoicePosition>getExtent(createInvoicePositionQuery(segment, productId)), productId,
						2);
			} finally {
				if (this.entityManager.currentTransaction().isActive()) {
					this.rollback();
				}
				super.taskId = null;
			}

		}

		/**
		 * @param invoicePositions
		 * @param productId
		 * @param expectedCount
		 */
		private void validateInvoicePositions(List<InvoicePosition> invoicePositions, String productId,
				int expectedCount) {
			int count = 0;
			for (InvoicePosition p : invoicePositions) {
				Assertions.assertEquals(productId,  p.getProductId(), "Remaining invoice positions");
				count++;
			}
			Assertions.assertEquals(expectedCount, count, "Invoice positions with product id " + productId);
			Assertions.assertEquals(expectedCount, invoicePositions.size(), "Invoice positions with product id " + productId);
		}

		/**
		 * @param segment
		 * @param productId
		 * @return
		 */
		private InvoicePositionQuery createInvoicePositionQuery(test.openmdx.app1.jmi1.Segment segment,
				String productId) {
			String xriPattern = segment.refGetPath().getDescendant("invoice", ":*", "invoicePosition", "%").toXRI();
			InvoicePositionQuery invoicePositionQuery = (InvoicePositionQuery) PersistenceHelper
					.newQuery(entityManager.getExtent(InvoicePosition.class), xriPattern);
			invoicePositionQuery.productId().equalTo(productId);
			return invoicePositionQuery;
		}

		/**
		 * Retrieve the Transient provider
		 *
		 * @return the Transient provider
		 */
		protected Provider getModelTestProvider() {
			Authority authority = entityManager.getObjectById(Authority.class, Model1Package.AUTHORITY_XRI);
			Provider provider = authority.getProvider(TestMain.STANDARD_SEGMENT_NAME);
			return provider;
		}

		/**
		 * getModelTestProvider Retrieve the Test segment
		 * 
		 * @return the Test segment
		 */
		protected test.openmdx.model1.jmi1.Segment getModelTestSegment() {
			Provider provider = getModelTestProvider();
			test.openmdx.model1.jmi1.Segment segment = (test.openmdx.model1.jmi1.Segment) provider.getSegment("Test");
			if (segment == null) {
				PersistenceManager persistenceManager = ReducedJDOHelper.getPersistenceManager(provider);
				segment = persistenceManager.newInstance(test.openmdx.model1.jmi1.Segment.class);
				provider.addSegment("Test", segment);
			}
			return segment;
		}

		/**
		 * Retrieve the Operations object
		 * 
		 * @return the Operations object
		 */
		protected ClassContainingOperations getModelTestOperations() {
			test.openmdx.model1.jmi1.Segment segment = getModelTestSegment();
			ClassContainingOperations operations = segment.getClassContainingOperations("Operations");
			if (operations == null) {
				PersistenceManager persistenceManager = ReducedJDOHelper.getPersistenceManager(segment);
				operations = persistenceManager.newInstance(ClassContainingOperations.class);
				segment.addClassContainingOperations("Operations", operations);
			}
			return operations;
		}

		/**
		 * @param exception
		 * @return
		 */
		private boolean isTransactionRolledBack(JmiServiceException exception) {
			boolean rolledBack = false;
			for (Throwable throwable = exception; throwable != null; throwable = throwable.getCause()) {
				if (throwable instanceof BasicException) {
					rolledBack |= ((BasicException) throwable).getExceptionCode() == BasicException.Code.ROLLBACK;
				} else if (throwable instanceof BasicException.Holder) {
					rolledBack |= ((BasicException.Holder) throwable)
							.getExceptionCode() == BasicException.Code.ROLLBACK;
				}
			}
			return rolledBack;
		}

		/**
		 * Validate the cycle query
		 * 
		 * @param query
		 */
		private void validateCycleQuery(RefQuery_1_0 query) {
			Assertions.assertTrue(query instanceof CycleMember1Query, "CycleMember1QueryCCI Query Interface");
			List<ConditionRecord> m1Conditions = query.refGetFilter().getCondition();
			Assertions.assertEquals(2,   m1Conditions.size(), "m1 Conditions");
			Assertions.assertEquals(SystemAttributes.OBJECT_INSTANCE_OF,  m1Conditions.get(0).getFeature(), "m1 Condition 0");
			Assertions.assertEquals(Quantifier.THERE_EXISTS,  m1Conditions.get(0).getQuantifier(), "m1 Condition 0");
			Assertions.assertEquals(ConditionType.IS_IN,  m1Conditions.get(0).getType(), "m1 Condition 0");
			Assertions.assertEquals( 1,   m1Conditions.get(0).getValue().length, "m1 Condition 0");
			Assertions.assertEquals("test:openmdx:app1:CycleMember1",  m1Conditions.get(0).getValue(0), "m1 Condition 0");
			Assertions.assertTrue(m1Conditions.get(1).getValue(0) instanceof QueryFilterRecord, "m1 Condition 1");
			List<ConditionRecord> m2Conditions = ((QueryFilterRecord) m1Conditions.get(1).getValue(0)).getCondition();
			Assertions.assertEquals( 3,   m2Conditions.size(), "m2 Conditions");
			Assertions.assertEquals(SystemAttributes.OBJECT_INSTANCE_OF,  m2Conditions.get(0).getFeature(), "m2 Condition 0");
			Assertions.assertEquals(Quantifier.THERE_EXISTS,  m2Conditions.get(0).getQuantifier(), "m2 Condition 0");
			Assertions.assertEquals(ConditionType.IS_IN,  m2Conditions.get(0).getType(), "m2 Condition 0");
			Assertions.assertEquals( 1,   m2Conditions.get(0).getValue().length, "m2 Condition 0");
			Assertions.assertEquals("test:openmdx:app1:CycleMember2",  m2Conditions.get(0).getValue(0), "m2 Condition 0");
			Assertions.assertEquals("description",  m2Conditions.get(1).getFeature(), "m2 Condition 1");
			Assertions.assertEquals(Quantifier.THERE_EXISTS,  m2Conditions.get(1).getQuantifier(), "m2 Condition 1");
			Assertions.assertEquals(ConditionType.IS_IN,  m2Conditions.get(1).getType(), "m2 Condition 1");
			Assertions.assertEquals( 2,   m2Conditions.get(1).getValue().length, "m2 Condition 1");
			Assertions.assertEquals("Cycle Member \"CR20020022\"",  m2Conditions.get(1).getValue(0), "m2 Condition 1");
			Assertions.assertEquals("n/a",  m2Conditions.get(1).getValue(1), "m2 Condition 1");
			Assertions.assertEquals("m1",  m2Conditions.get(2).getFeature(), "m2 Condition 2");
			Assertions.assertEquals(Quantifier.FOR_ALL,  m2Conditions.get(2).getQuantifier(), "m2 Condition 2");
			Assertions.assertEquals(ConditionType.IS_IN,  m2Conditions.get(2).getType(), "m2 Condition 2");
			Assertions.assertEquals( 0,   m2Conditions.get(2).getValue().length, "m2 Condition 2");
		}

		/**
		 * Test the audit entries
		 * 
		 * @param run the number of completed runs
		 */
		protected void testAudit(int run) {
			Person person = (Person) super.entityManager.getObjectById(super.entityManager
					.newObjectIdInstance(Person.class, dataSegmentId.getDescendant("person", "ID500012")));
			Invoice invoice = (Invoice) super.entityManager.getObjectById(super.entityManager
					.newObjectIdInstance(Invoice.class, dataSegmentId.getDescendant("invoice", "CR20019372")));
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
			Assertions.assertEquals( (1 * run),   task.size(), "Size of task CR20018578");
			task = AuditQueries.getUnitOfWorkBelongingToTask(super.entityManager, "CR0002096");
			Assertions.assertEquals( (2 * run),   task.size(), "Size of task CR0002096");
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

		private void testAudit(Invoice invoice, int run, boolean scoped) {
			int factor = scoped ? 1 : run;
			int create = SharedObjects.getPlugInObject(this.entityManager, Configuration.class)
					.getPersistenceMode() == InvolvementPersistence.EMBEDDED ? 0 : 1;
			String scope = scoped ? " (run " + run + ")" : " (run 1.." + run + ")";
			Date from = scoped ? super.getStart() : null;
			Collection<UnitOfWork> task = AuditQueries.getUnitOfWorkInvolvingObject(from, null, invoice);
			String id = "Involve InvoicePosition CR20019372.";
			dumpTask(id + scope, task);
			Assertions.assertEquals( ((1 * create + 1) * factor),   task.size(), id + scope);
			id = "Involve InvoicePosition... CR20019372.";
			Collection<ExtentCapable> tree = PersistenceHelper.getCandidates(
					this.entityManager.getExtent(ExtentCapable.class, true), invoice.refMofId() + "/($...)");
			task = AuditQueries.getUnitOfWorkInvolvingObject(from, null, tree);
			Assertions.assertEquals( ((1 * create + 1) * factor),   task.size(), id + scope);
			for (UnitOfWork unitOfWork : task) {
				for (Iterator<Involvement> i = unitOfWork.<Involvement>getInvolvement().iterator(); i.hasNext();) {
					Involvement involvement = i.next();
					Modifiable modifiable = involvement.getBeforeImage();
					System.out.println(modifiable.refMofId());
				}
			}
		}

		private void testAudit(Person person, int run, boolean scoped) {
			int factor = scoped ? 1 : run;
			int create = SharedObjects.getPlugInObject(this.entityManager, Configuration.class)
					.getPersistenceMode() == InvolvementPersistence.EMBEDDED ? 0 : 1;
			String scope = scoped ? " (run " + run + ")" : " (run 1.." + run + ")";
			Date from = scoped ? super.getStart() : null;
			Collection<UnitOfWork> task = AuditQueries.getUnitOfWorkInvolvingObject(from, null, person);
			String id = "involve Person # ID500012";
			dumpTask(id + scope, task);
			Assertions.assertEquals( ((1 * create + 8) * factor),   task.size(), id + scope);
			if (scoped || run == 1) {
				Modifiable lastImage = null;
				for (UnitOfWork unitOfWork : task) {
					Involvement involvement = unitOfWork.getInvolvement(person.refGetPath().toClassicRepresentation());
					Assertions.assertNotNull(involvement, id);
					if (lastImage != null) {
						Assertions.assertSame(lastImage, involvement.getBeforeImage(), id);
					}
					lastImage = involvement.getAfterImage();
				}
				Assertions.assertNotNull(lastImage, id);
			}
			if (create == 0) {
				try {
					task = AuditQueries.getUnitOfWorkCreatingObject(from, null, person);
					Assertions.fail("Create not audited in EMBEDDED mode");
				} catch (UnsupportedOperationException exception) {
					// Create not audited in audit1 mode
				}
			} else {
				id = "create Person # ID500012";
				task = AuditQueries.getUnitOfWorkCreatingObject(from, null, person);
				dumpTask(id + scope, task);
				Assertions.assertEquals( ((1 * create + 0) * factor),   task.size(), id + scope);
			}
			if (run == 1) {
				id = "touch Person # ID500012";
				task = AuditQueries.getUnitOfWorkTouchingObject(from, null, null, person);
				dumpTask(id + scope, task);
				Assertions.assertEquals( ((0 * create + 8) * factor),   task.size(), id + scope);
				id = "touch specific attributes of Person # ID500012";
				task = AuditQueries.getUnitOfWorkTouchingObject(from, null, Sets.asSet(Arrays
						.asList("test:openmdx:app1:Person:assignedAddress", "test:openmdx:app1:Person:additionalInfo")),
						person);
				dumpTask(id + scope, task);
				Assertions.assertEquals( ((0 * create + 4) * factor),   task.size(), id + scope);
			}
			if (scoped || run == 1) {
				id = "remove Person # ID500012";
				task = AuditQueries.getUnitOfWorkRemovingObject(from, null, person);
				dumpTask(id + scope, task);
				Assertions.assertEquals( (0 * factor),   task.size(), id + scope);
			}
			id = "all units of work";
			task = AuditQueries.getUnitOfWorkForTimeRange(super.entityManager, from, null);
			dumpTask(id + scope, task);
			Assertions.assertEquals( ((16 * create + 69) * factor),   task.size(), id + scope);
			id = "units of work involving people";
			task = AuditQueries.getUnitOfWorkInvolvingObject(from, null, PersistenceHelper.getCandidates(
					super.entityManager.getExtent(Person.class), dataSegmentId.getDescendant("person", "%")));
			dumpTask(id + scope, task);
			Assertions.assertEquals( ((3 * create + 14) * factor),   task.size(), id + scope);
			id = "units of work involving deleted object";
			Path addressId = dataSegmentId.getDescendant("address", "CR0002096");
			task = AuditQueries.getUnitOfWorkInvolvingObject(from, null,
					PersistenceHelper.getCandidates(super.entityManager.getExtent(Person.class), addressId.toXRI()));
			dumpTask(id + scope, task);
			Assertions.assertEquals( ((1 * create + 1) * factor),   task.size(), id + scope);
			id = "units of work deleting object";
			task = AuditQueries.getUnitOfWorkRemovingObject(from, null,
					PersistenceHelper.getCandidates(super.entityManager.getExtent(Person.class), addressId.toXRI()));
			dumpTask(id + scope, task);
			Assertions.assertEquals( ((0 * create + 1) * factor),   task.size(), id + scope);
			for (UnitOfWork unitOfWork : task) {
				for (Involvement involvement : unitOfWork.<Involvement>getInvolvement()) {
					Assertions.assertNull(involvement.getAfterImage(), id + scope);
				}
			}
		}

		private void dumpTask(String title, Collection<UnitOfWork> task) {
			if (DUMP) {
				System.out.println(title);
				for (UnitOfWork unitOfWork : task) {
					System.out.println("\t" + unitOfWork.getUnitOfWorkId() + " ("
							+ DateTimeFormat.EXTENDED_UTC_FORMAT.format(unitOfWork.getCreatedAt()) + ")");
					System.out.println("\t\tinvolvement");
					for (Involvement involvement : unitOfWork.<Involvement>getInvolvement()) {
						System.out.println("\t\t\t" + getResourceIdentifier(involvement));
						System.out.println("\t\t\t\t objectId = " + involvement.getObjectId());
						System.out.println("\t\t\t\t object = " + getResourceIdentifier(involvement.getObject()));
						System.out.println(
								"\t\t\t\t beforeImage = " + getResourceIdentifier(involvement.getBeforeImage()));
						try {
							System.out.println(
									"\t\t\t\t afterImage = " + getResourceIdentifier(involvement.getAfterImage()));
						} catch (Exception exception) {
							System.out.println("\t\t\t\t afterImage is N/A: " + exception.getMessage());
						}
						try {
							System.out.println("\t\t\t\t modifiedFeature = " + involvement.getModifiedFeature());
						} catch (Exception exception) {
							System.out.println("\t\t\t\t modifiedFeature is N/A: " + exception.getMessage());
						}
					}
				}
			}
		}

		protected static String getResourceIdentifier(Object pc) {
			return pc == null ? null : ReducedJDOHelper.getAnyObjectId(pc).toString();
		}

		protected void testTransientProviderPerformance() throws ServiceException, ParseException {
			PersonClass personClass = getPackage().getPerson();
			test.openmdx.app1.jmi1.Segment segment = getSegment();
			final PersonGroup[] g = createPersonGroups(segment, "100000");
			this.begin();
			PostalAddress postalAddress = createPostalAddress(segment, "1000001");
			this.commit();
			// Create persons
			for (int i = 0; i <= LARGE_N_PERSONS; i++) {
				if (i % 100 == 0) {
					System.out.println(i + " persons created. Free memory " + Runtime.getRuntime().freeMemory());
				} else {
					System.out.println("Person " + i);
				}
				this.begin();
				Person person = personClass.createPerson();
				person.setForeignId("F" + i);
				person.setBirthdate(Datatypes.create(XMLGregorianCalendar.class, "1960-01-01"));
				person.setBirthdateAsDateTime(Datatypes.create(Date.class, "19600101T120000.000Z"));
				person.setLastName("Muster" + i);
				person.setSalutation("Herr");
				person.setSex((short) 0);
				person.setGivenName(new String[] { "Hans", "Heiri" });
				person.getAssignedAddress().add(postalAddress);
				person.getPersonGroup().add(g[0]);
				person.getPersonGroup().add(g[1]);
				person.getPersonGroup().add(g[2]);
				segment.addPerson(false, "L" + (1000000 + i), person);
				this.commit();
			}

			// Retrieve persons
			int ii = 0;
			int limit = 1000000;
			Runtime runtime = Runtime.getRuntime();
			long initialMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
			System.out.println("initial memory usage " + initialMemoryUsage);
			test.openmdx.app1.cci2.SegmentHasPerson.Person<Person> allPeople = segment.getPerson();
			for (@SuppressWarnings("unused")
			Person pers : allPeople) {
				if (ii++ % 100 == 0) {
					long currentMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
					System.out.println(ii + " persons retrieved. Current memory usage " + currentMemoryUsage);
					long additionalMemoryUsage = currentMemoryUsage - initialMemoryUsage;
					if (additionalMemoryUsage > limit) {
						runtime.gc();
						currentMemoryUsage = runtime.totalMemory() - runtime.freeMemory();
						additionalMemoryUsage = currentMemoryUsage - initialMemoryUsage;
						Assertions.assertFalse(additionalMemoryUsage > limit, "Memory used up after " + ii + " failed retrievals: " + additionalMemoryUsage);
					}
				}
				limit += 3500;
			}
		}

		protected test.openmdx.app1.jmi1.Segment getSegment() throws ServiceException {
			return (test.openmdx.app1.jmi1.Segment) super.entityManager.getObjectById(dataSegmentId);
		}

		protected App1Package getPackage() throws ServiceException {
			return (App1Package) ((RefObject) super.entityManager.newInstance(test.openmdx.app1.jmi1.Segment.class))
					.refImmediatePackage();
		}

		// --------------------------------------------------------------------
		// Enum StructureCreation
		// --------------------------------------------------------------------

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
	protected abstract static class AbstractLocalConnectionTest extends AbstractRepeatableTest {

		protected void run() throws Exception {
			super.resetAuditSegment();
			super.resetDataSegment();
			super.testCR20019917();
			super.testCR20019462();
			super.testCR20018726();
			super.testCR20019014();
			super.testCR20020032();
			super.testMain();
			if(isBackedUpByStandardDB()) {
				super.testAudit(1);
			}
		}

	}

	public static class StandardProviderTest extends AbstractLocalConnectionTest {

		@Test
		public void run() throws Exception {
			super.run();
		}

		@Override
		protected final boolean isContainerManaged() {
			return false;
		}

	}

	public static class TransientProviderTest extends AbstractLocalConnectionTest {

		@Test
		public void run() throws Exception {
			super.run();
			super.testTransientProviderPerformance();
		}

		@Override
		protected final boolean isContainerManaged() {
			return false;
		}

		@Override
		protected String getProviderName() {
			return TRANSIENT_PROVIDER_NAME;
		}

		@Override
		protected boolean testConcurrentAccess() {
			return false;
		}

	}

	/**
	 * 1st Run
	 */
	public static class PreferencesTest extends AbstractRepeatableTest {

		@Test
		public void run() throws Exception {
			testCR20044760();
		}

		@Override
		protected final boolean isContainerManaged() {
			return false;
		}

		public void testCR20044760() {
			final Collection<Node> nodes = getNodes();
			Assertions.assertFalse(nodes.isEmpty(), "There are no nodes");
			for (Node node : nodes) {
				Collection<Entry> entries = node.<Entry>getEntry();
				for (Object entry : entries) {
					System.out.println(entry);
				}

			}
		}

		@SuppressWarnings("unchecked")
		private Collection<Node> getNodes() {
			return (Collection<Node>) entityManager.getObjectById(CONFIGURATION_NODES);
		}

	}

	// ------------------------------------------------------------------------
	// Class AbstractContainerManagedTransactionTest
	// ------------------------------------------------------------------------

	/**
	 * 3rd Run
	 */
	public static abstract class AbstractContainerManagedTransactionTest extends AbstractLocalConnectionTest {

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
		 * @return {@code true} if the unit of work is optimistic
		 */
		protected abstract Boolean isOptimistic();

		/*
		 * (non-Javadoc)
		 * 
		 * @see test.openmdx.app1.TestMain.AbstractTest#isContainerManaged()
		 */
		@Override
		protected final boolean isContainerManaged() {
			return true;
		}

		protected boolean testConcurrentAccess() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see test.openmdx.app1.TestMain.AbstractTest#configuration()
		 */
		@Override
		protected Map<String, Object> configuration() {
			Map<String, Object> amendment = super.configuration();
			amendment.put(ConfigurableProperty.TransactionType.qualifiedName(), Constants.JTA);
			amendment.put(ConfigurableProperty.ContainerManaged.qualifiedName(), Boolean.TRUE.toString());
			amendment.put(ConfigurableProperty.Optimistic.qualifiedName(), this.isOptimistic().toString());
			return amendment;
		}

		@Override
		public void run() throws Exception {
			this.userTransaction = ComponentEnvironment.lookup(UserTransaction.class);
			if (this.userTransaction.getStatus() == Status.STATUS_ACTIVE.ordinal()) {
				this.userTransaction.rollback();
			}
			this.userTransaction.begin();
			if (isContainerManaged()) {
				prolog();
			}
			super.resetAuditSegment();
			super.resetDataSegment();
			super.testCR20019462();
			super.testCR20018726();
			super.testCR20019014();
			super.testMain();
			this.userTransaction.rollback();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see test.openmdx.app1.TestMain.AbstractTest#begin()
		 */
		@Override
		protected void begin() {
			try {
				if (this.userTransaction.getStatus() == Status.STATUS_ACTIVE.ordinal()) {
					this.userTransaction.rollback();
				}
				this.userTransaction.begin();
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
			currentUnitOfWork().afterBegin();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see test.openmdx.app1.TestMain.AbstractTest#commit()
		 */
		@Override
		protected void commit() {
			try {
				try {
					currentUnitOfWork().beforeCompletion();
				} catch (JDOException commitException) {
					try {
						this.userTransaction.rollback();
					} catch (Exception rollbackException) {
						throw new JDOFatalDataStoreException("Container managed rollback failed", rollbackException);
					} finally {
						currentUnitOfWork().afterCompletion(Status.STATUS_ROLLEDBACK);
					}
					throw commitException;
				}
				try {
					this.userTransaction.commit();
				} catch (Exception commitException) {
					currentUnitOfWork().afterCompletion(Status.STATUS_ROLLEDBACK);
					throw new JDOFatalDataStoreException("Container managed commit failed", commitException);
				}
				currentUnitOfWork().afterCompletion(Status.STATUS_COMMITTED);
			} finally {
				try {
					this.userTransaction.begin();
				} catch (Exception exception) {
					throw new RuntimeException("Could not start container managed read-only transaction", exception);
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see test.openmdx.app1.TestMain.AbstractTest#rollback()
		 */
		@Override
		protected void rollback() {
			try {
				this.userTransaction.rollback();
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			} finally {
				currentUnitOfWork().afterCompletion(Status.STATUS_ROLLEDBACK);
			}
			try {
				this.userTransaction.begin();
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		}

	}

	// ------------------------------------------------------------------------
	// Class OptimisticContainerManagedTransactionTest
	// ------------------------------------------------------------------------

	/**
	 * Test Optimistic Container-Managed Transactions
	 */
	public static class OptimisticContainerManagedTransactionTest extends AbstractContainerManagedTransactionTest {

		/*
		 * (non-Javadoc)
		 * 
		 * @see test.openmdx.app1.TestMain.AbstractContainerManagedTransactionTest#
		 * isOptimistic()
		 */
		@Override
		protected Boolean isOptimistic() {
			return Boolean.TRUE;
		}

	}

	// ------------------------------------------------------------------------
	// Class PessimisticContainerManagedTracreatePostalAddressnsactionTest
	// ------------------------------------------------------------------------

	/**
	 * Test Optimistic Container-Managed Transactions
	 */
	public static class PessimisticContainerManagedTransactionTest extends AbstractContainerManagedTransactionTest {

		/*
		 * (non-Javadoc)
		 * 
		 * @see test.openmdx.app1.TestMain.AbstractContainerManagedTransactionTest#
		 * isOptimistic()
		 */
		@Override
		protected Boolean isOptimistic() {
			return Boolean.FALSE;
		}

	}

	// ------------------------------------------------------------------------
	// Class ProxyConnectionTest
	// ------------------------------------------------------------------------

	/**
	 * 2nd Run
	 */
	public static class ProxyConnectionTest extends AbstractRepeatableTest {

		/**
		 * Tells whether a servlet connection shall be used
		 * 
		 * @return
		 */
		protected boolean useServlet() {
			return true;
		}

		@Override
		protected final boolean isContainerManaged() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see test.openmdx.app1.TestMain.AbstractTest#newEntityManagerFactory()
		 */
		@Override
		protected PersistenceManagerFactory newEntityManagerFactory() {
			try {
				ConnectionFactory inboundConnectionFactory = useServlet()
						? new RestConnectionFactory(
								new ServletPort(Collections.singletonMap("entity-manager-factory-name",
										"jdo:test-Main-EntityManagerFactory")),
								true, // supportsLocalTransactionDemarcation
								TransactionAttributeType.NEVER)
						: InboundConnectionFactory_2.newInstance("jdo:test-Main-EntityManagerFactory");
				Map<String, Object> dataManagerProxyConfiguration = new HashMap<String, Object>();
				dataManagerProxyConfiguration.put(ConfigurableProperty.ConnectionFactory.qualifiedName(),
						inboundConnectionFactory);
				dataManagerProxyConfiguration.put(ConfigurableProperty.PersistenceManagerFactoryClass.qualifiedName(),
						EntityManagerProxyFactory_2.class.getName());
				PersistenceManagerFactory outboundConnectionFactory = ReducedJDOHelper
						.getPersistenceManagerFactory(dataManagerProxyConfiguration);

				Map<String, Object> entityManagerConfiguration = new HashMap<String, Object>();
				entityManagerConfiguration.put(ConfigurableProperty.ConnectionFactory.qualifiedName(),
						outboundConnectionFactory);
				entityManagerConfiguration.put(ConfigurableProperty.PersistenceManagerFactoryClass.qualifiedName(),
						EntityManagerFactory_1.class.getName());
				return ReducedJDOHelper.getPersistenceManagerFactory(entityManagerConfiguration);
			} catch (ServletException exception) {
				throw new JDOFatalDataStoreException("Unable to provide proxy persistence manager factory", exception);
			}
		}

		@Test
		public void run() throws Exception {
			super.resetDataSegment();
			super.testPackageAcquisition();
			super.testCR20019462();
			super.testCR20018800();
			super.testMain();
// TODO     super.testAudit(2);
		}

	}

	@Disabled("openMDX test server usually not running")
	public static class RemoteConnectionTest extends ProxyConnectionTest {

		/*
		 * (non-Javadoc)
		 * 
		 * @see test.openmdx.app1.TestMain.ProxyConnectionTest#newEntityManagerFactory()
		 */
		@Override
		protected PersistenceManagerFactory newEntityManagerFactory() {
			return ReducedJDOHelper.getPersistenceManagerFactory(configuration(), ENTITY_MANAGER_PROXY_FACTORY_NAME);
		}

	}

}

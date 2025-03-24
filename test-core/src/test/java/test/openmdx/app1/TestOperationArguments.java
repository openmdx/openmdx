/*
 * ====================================================================
 * Project:     openMDX/Test Core, http://www.openmdx.org/
 * Description: Test Operation Arguments 
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

import java.math.BigDecimal;
import java.util.Collections;

import javax.jdo.PersistenceManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openmdx.base.collection.Sets;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.cci.UnitOfWork;
import org.openmdx.junit5.JDOExtension;
import org.openmdx.junit5.OpenmdxTestCoreStandardExtension;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.w3c.spi2.Structures;

import test.openmdx.model1.jmi1.ClassContainingOperations;
import test.openmdx.model1.jmi1.ClassContainingOperationsTestComplexStruct0_1_0_1Params;
import test.openmdx.model1.jmi1.ClassContainingOperationsTestComplexStruct0_n_0_1Params;
import test.openmdx.model1.jmi1.ComplexStruct0_1;
import test.openmdx.model1.jmi1.ComplexStruct0_n;
import test.openmdx.model1.jmi1.Model1Package;
import test.openmdx.model1.jmi1.Segment;
import test.openmdx.model1.jmi1.SimpleStruct0_1;
import test.openmdx.model1.jmi1.SimpleStruct0_n;
import test.openmdx.model1.jmi1.SimpleStruct1_1;
import test.openmdx.model1.jmi1.SimpleStructList;
import test.openmdx.model1.jmi1.SimpleStructSet;
import test.openmdx.model1.jmi1.SimpleStructSparseArray;
import test.openmdx.model1.jmi1.TestComplexStruct0_1_0_1Result;
import test.openmdx.model1.jmi1.TestComplexStruct0_n_0_1Result;

/**
 * Test Operation Arguments
 */
@ExtendWith(OpenmdxTestCoreStandardExtension.class)
public class TestOperationArguments {

	@RegisterExtension
	static JDOExtension jdoExtension = JDOExtension.withEntityManagerFactoryName("test-Main-EntityManagerFactory");

    private static final String TRANSIENT_PROVIDER_NAME = "Transient";    
    
    /**
     * CR10011193
     */
    @Test
    public void invokeOperationWithNestedStructs(){
    	begin();
        ClassContainingOperations operations = getModelTestOperations();
        ClassContainingOperationsTestComplexStruct0_1_0_1Params in = Structures.create(
            ClassContainingOperationsTestComplexStruct0_1_0_1Params.class,
            Structures.create(
                ComplexStruct0_1.class,
                Structures.create(
                    SimpleStruct0_1.class, 
                    null, // binaryField
                    null, // booleanField
                    null, // dateTimeField
                    null, // decimalField
                    null, // durationField
                    null, // integerField
                    null, // longField
                    null, // shortField
                    "CR10011193" // stringField
                ),
                null, // simpleStruct0_nField
                null, // simpleStruct1_1Field
                null, // simpleStructListField
                null, // simpleStructSetField
                null // simpleStructSparseArrayField
            )
        );
        TestComplexStruct0_1_0_1Result out = operations.testComplexStruct0_1_0_1(in);
        this.commit();
        Assertions.assertNotNull(out, "CR10011193");
        Assertions.assertNotNull(out.getResult(), "CR10011193");
        Assertions.assertNotNull(out.getResult().getSimpleStruct0_1Field(), "CR10011193");
        Assertions.assertEquals(out.getResult().getSimpleStruct0_1Field().getStringField(), "CR10011193");
    }

    /**
     * CR10011473
     */
    @Test
    public void callOperationWithListOfStructs(){
        this.begin();
        ClassContainingOperations operations = getModelTestOperations();
        ClassContainingOperationsTestComplexStruct0_n_0_1Params in = Structures.create(
            ClassContainingOperationsTestComplexStruct0_n_0_1Params.class,
            Structures.create(
                ComplexStruct0_n.class,
                new SimpleStruct0_1[]{
                    Structures.create(
                        SimpleStruct0_1.class, 
                        null, // binaryField
                        null, // booleanField
                        null, // dateTimeField
                        null, // decimalField
                        null, // durationField
                        Integer.valueOf(0), // integerField
                        null, // longField
                        null, // shortField
                        "CR10011473" // stringField
                    ),
                    Structures.create(
                        SimpleStruct0_1.class, 
                        null, // binaryField
                        null, // booleanField
                        null, // dateTimeField
                        null, // decimalField
                        null, // durationField
                        Integer.valueOf(1), // integerField
                        null, // longField
                        null, // shortField
                        "CR10011473bis" // stringField
                    ),
                }, // SimpleStruct0_1Field
                new SimpleStruct0_n[]{}, // simpleStruct0_nField
                new SimpleStruct1_1[]{}, // simpleStruct1_1Field
                new SimpleStructList[]{}, // simpleStructListField
                new SimpleStructSet[]{
                    Structures.create(
                        SimpleStructSet.class,
                        new Boolean[]{}, // booleanField
                        new #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant#endif []{}, // dateTimeField
                        new BigDecimal[]{}, // decimalField
                        new #if CLASSIC_CHRONO_TYPES javax.xml.datatype.Duration #else java.time.Duration #endif []{}, // durationField
                        new Integer[]{
                           Integer.valueOf(0)
                        }, // integerField
                        new Long[]{}, // longField
                        new Short[]{}, // shortField
                        new String[]{
                            "CR10011473a",
                            "CR10011473b"
                        } // stringField
                    ),
                    Structures.create(
                        SimpleStructSet.class,
                        new Boolean[]{}, // booleanField
                        new #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant#endif []{}, // dateTimeField
                        new BigDecimal[]{}, // decimalField
                        new #if CLASSIC_CHRONO_TYPES javax.xml.datatype.Duration #else java.time.Duration #endif []{}, // durationField
                        new Integer[]{
                            Integer.valueOf(1)
                         }, // integerField
                         new Long[]{}, // longField
                         new Short[]{}, // shortField
                         new String[]{
                            "CR10011473bis"
                        } // stringField
                    )
                }, // simpleStructSetField
                new SimpleStructSparseArray[]{} // simpleStructSparseArrayField
            )
        );
        TestComplexStruct0_n_0_1Result out = operations.testComplexStruct0_n_0_1(in);
        this.commit();
        Assertions.assertNotNull(out, "CR10011473");
        Assertions.assertNotNull(out.getResult(), "CR10011473");
        Assertions.assertNotNull(out.getResult().getSimpleStruct0_1Field(), "CR10011473");
        Assertions.assertEquals(out.getResult().getSimpleStruct0_1Field().get(0).getStringField(), "CR10011473");
        Assertions.assertEquals(out.getResult().getSimpleStruct0_1Field().get(1).getStringField(), "CR10011473bis");
        Assertions.assertEquals(Integer.valueOf(0), out.getResult().getSimpleStruct0_1Field().get(0).getIntegerField());
        Assertions.assertEquals(Integer.valueOf(1), out.getResult().getSimpleStruct0_1Field().get(1).getIntegerField());
        Assertions.assertEquals(Sets.asSet("CR10011473a","CR10011473b"), out.getResult().getSimpleStructSetField().get(0).getStringField());
        Assertions.assertEquals(Collections.singleton(Integer.valueOf(1)), out.getResult().getSimpleStructSetField().get(1).getIntegerField());
        Assertions.assertTrue(out.getResult().getSimpleStructSetField().get(1).getShortField().isEmpty());
    }
    
	@Test
    public void callOperationWithSetArgument(
    ) throws Exception {
        this.begin();
        ClassContainingOperations operations = getModelTestOperations();
        ClassContainingOperationsTestComplexStruct0_n_0_1Params in = Structures.create(
            ClassContainingOperationsTestComplexStruct0_n_0_1Params.class,
            Structures.create(
                ComplexStruct0_n.class,
                new SimpleStruct0_1[]{
                    Structures.create(
                        SimpleStruct0_1.class, 
                        null, // binaryField
                        null, // booleanField
                        null, // dateTimeField
                        null, // decimalField
                        null, // durationField
                        Integer.valueOf(0), // integerField
                        null, // longField
                        null, // shortField
                        "CR10011473" // stringField
                    ),
                    Structures.create(
                        SimpleStruct0_1.class, 
                        null, // binaryField
                        null, // booleanField
                        null, // dateTimeField
                        null, // decimalField
                        null, // durationField
                        Integer.valueOf(1), // integerField
                        null, // longField
                        null, // shortField
                        "CR10011473bis" // stringField
                    ),
                }, // SimpleStruct0_1Field
                new SimpleStruct0_n[]{}, // simpleStruct0_nField
                new SimpleStruct1_1[]{}, // simpleStruct1_1Field
                new SimpleStructList[]{}, // simpleStructListField
                new SimpleStructSet[]{
                    Structures.create(
                        SimpleStructSet.class,
                        new Boolean[]{}, // booleanField
                        new #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant#endif []{}, // dateTimeField
                        new BigDecimal[]{}, // decimalField
                        new #if CLASSIC_CHRONO_TYPES javax.xml.datatype.Duration #else java.time.Duration #endif []{}, // durationField
                        new Integer[]{
                           Integer.valueOf(0)
                        }, // integerField
                        new Long[]{}, // longField
                        new Short[]{}, // shortField
                        new String[]{
                            "CR10011473a",
                            "CR10011473b"
                        } // stringField
                    ),
                    Structures.create(
                        SimpleStructSet.class,
                        new Boolean[]{}, // booleanField
                        new #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant#endif []{}, // dateTimeField
                        new BigDecimal[]{}, // decimalField
                        new #if CLASSIC_CHRONO_TYPES javax.xml.datatype.Duration #else java.time.Duration #endif []{}, // durationField
                        new Integer[]{
                            Integer.valueOf(1)
                         }, // integerField
                         new Long[]{}, // longField
                         new Short[]{}, // shortField
                         new String[]{
                            "CR10011473bis"
                        } // stringField
                    )
                }, // simpleStructSetField
                new SimpleStructSparseArray[]{} // simpleStructSparseArrayField
            )
        );
        TestComplexStruct0_n_0_1Result out = operations.testComplexStruct0_n_0_1(in);
        this.commit();
        Assertions.assertNotNull(out, "CR10011473");
        Assertions.assertNotNull(out.getResult(), "CR10011473");
        Assertions.assertNotNull(out.getResult().getSimpleStruct0_1Field(), "CR10011473");
        Assertions.assertEquals(out.getResult().getSimpleStruct0_1Field().get(0).getStringField(), "CR10011473");
        Assertions.assertEquals(out.getResult().getSimpleStruct0_1Field().get(1).getStringField(), "CR10011473bis");
        Assertions.assertEquals(Integer.valueOf(0), out.getResult().getSimpleStruct0_1Field().get(0).getIntegerField());
        Assertions.assertEquals(Integer.valueOf(1), out.getResult().getSimpleStruct0_1Field().get(1).getIntegerField());
        Assertions.assertEquals(Sets.asSet("CR10011473a","CR10011473b"), out.getResult().getSimpleStructSetField().get(0).getStringField());
        Assertions.assertEquals(                                     Collections.singleton(Integer.valueOf(1)), out.getResult().getSimpleStructSetField().get(1).getIntegerField());
        Assertions.assertTrue(out.getResult().getSimpleStructSetField().get(1).getShortField().isEmpty());
    }

    /**
     * Retrieve the Operations object
     * 
     * @return the Operations object
     */
    private ClassContainingOperations getModelTestOperations(
    ){
        Segment segment = getModelTestSegment();
        ClassContainingOperations operations = segment.getClassContainingOperations("Operations");
        if(operations == null) {
            PersistenceManager persistenceManager = ReducedJDOHelper.getPersistenceManager(segment);
            operations = persistenceManager.newInstance(ClassContainingOperations.class);
            segment.addClassContainingOperations("Operations", operations);
        }
        return operations;
    }
    
    /**
     * Retrieve the Transient provider
     *
     * @return the Transient provider
     */
    private Provider getModelTestProvider(
    ) {
        final Authority authority = getEntityManager().getObjectById(Authority.class, Model1Package.AUTHORITY_XRI);
		return authority.getProvider(TRANSIENT_PROVIDER_NAME);
    }
    
    /**
     * Retrieve the Test segment
     * 
     * @return the Test segment
     */
    private Segment getModelTestSegment(
    ){
        Provider provider = getModelTestProvider();
        Segment segment = (Segment) provider.getSegment("Test");
        if(segment == null) {
            PersistenceManager persistenceManager = ReducedJDOHelper.getPersistenceManager(provider);
            segment = persistenceManager.newInstance(Segment.class);
            provider.addSegment("Test", segment);
        }
        return segment;
    }

    private PersistenceManager getEntityManager(){
    	return jdoExtension.getEntityManager();
    }
    
    private void begin(){
        getUnitOfWork().begin();
    }

    private void commit(){
        getUnitOfWork().commit();
    }

    private UnitOfWork getUnitOfWork() {
    	return PersistenceHelper.currentUnitOfWork(getEntityManager());
    }
    
}

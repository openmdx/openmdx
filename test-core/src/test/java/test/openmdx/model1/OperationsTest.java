/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Callbacks Test
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

package test.openmdx.model1;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Collections;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.junit5.OpenmdxTestCoreStandardExtension;
import org.w3c.cci2.SortedMaps;
import org.w3c.spi2.Datatypes;
import org.w3c.time.SystemClock;
import test.openmdx.model1.jmi1.ClassContainingOperations;
import test.openmdx.model1.jmi1.ComplexStruct1_1;
import test.openmdx.model1.jmi1.Model1Package;
import test.openmdx.model1.jmi1.Segment;
import test.openmdx.model1.jmi1.TestComplexStruct1_1_1_1Result;

/**
 * Callbacks Test
 */
@ExtendWith(OpenmdxTestCoreStandardExtension.class)
class OperationsTest {

    protected static final String ENTITY_MANAGER_FACTORY_NAME = "test-Main-EntityManagerFactory";
    protected static PersistenceManagerFactory entityManagerFactory;
    protected PersistenceManager entityManager;

    @BeforeAll
    static void createPersistenceManagerFactory(
    ) {
        entityManagerFactory = JDOHelper.getPersistenceManagerFactory(
            ENTITY_MANAGER_FACTORY_NAME
        );
    }
    
    @BeforeEach
    void prolog(){
        this.entityManager = entityManagerFactory.getPersistenceManager();
        this.entityManager.currentTransaction().begin();
    }

    @AfterEach
    void epilog(){
        this.entityManager.currentTransaction().commit();
    }

    private ComplexStruct1_1 createComplexStruct1_1(){
        final Model1Package model1Package = getModel1Package();
        return model1Package.createComplexStruct1_1(
            model1Package.createSimpleStruct0_1(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            ),
            model1Package.createSimpleStruct0_n(
                new ByteArrayInputStream(new byte[0]),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
            ),
            model1Package.createSimpleStruct1_1(
                new byte[0],
                false,
                SystemClock.getInstance().now(),
                BigDecimal.ZERO,
                Datatypes.create(Datatypes.DURATION_CLASS, "PT0H"),
                0,
                0L,
                (short)0,
                ""
            ),
            model1Package.createSimpleStructList(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
            ),
            model1Package.createSimpleStructSet(
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet()
            ),
            model1Package.createSimpleStructSparseArray(
                SortedMaps.emptySparseArray(),
                SortedMaps.emptySparseArray(),
                SortedMaps.emptySparseArray(),
                SortedMaps.emptySparseArray(),
                SortedMaps.emptySparseArray(),
                SortedMaps.emptySparseArray(),
                SortedMaps.emptySparseArray(),
                SortedMaps.emptySparseArray()
            )
        );
    }

    @Test
    void testOperationResultEntityManager(){
        // Arrange
        ClassContainingOperations operations = entityManager.newInstance(ClassContainingOperations.class);
        getModel1Segment().addClassContainingOperations(operations);
        // Act
        ComplexStruct1_1 argument = createComplexStruct1_1();
        TestComplexStruct1_1_1_1Result result = operations.testComplexStruct1_1_1_1(
            #if CLASSIC_CHRONO_TYPES
            getModel1Package().createClassContainingOperationsTestComplexStruct1_1_1_1Params(
                argument
            )
            #else
            argument
            #endif
        );
        // Assert
        Assertions.assertEquals(argument, result.getResult());
    }

    private Model1Package getModel1Package(){
        return (Model1Package) getTransientProvider().refOutermostPackage().refPackage(
            "test:openmdx:model1"
        );
    }

    /**
     * Retrieve the Test segment
     *
     * @return the Test segment
     */
    private Segment getModel1Segment(
    ){
        final Provider provider = getTransientProvider();
        Segment segment = (Segment) provider.getSegment("Test");
        if(segment == null) {
            segment = entityManager.newInstance(Segment.class);
            provider.addSegment("Test", segment);
        }
        return segment;
    }

    /**
     * Retrieve the Transient provider
     *
     * @return the Transient provider
     */
    private Provider getTransientProvider(
    ) {
        final Authority authority = entityManager.getObjectById(Authority.class, Model1Package.AUTHORITY_XRI);
        return authority.getProvider("Transient");
    }

}

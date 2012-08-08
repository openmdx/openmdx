/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestStructures.java,v 1.2 2011/03/09 11:04:46 hburger Exp $
 * Description: TestStructures 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/03/09 11:04:46 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2011, OMEX AG, Switzerland
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

package test.w3c.spi2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.w3c.cci2.SparseArray;
import org.w3c.spi2.Structures;

/**
 * TestStructures
 *
 */
public class TestStructures {

    @Test
    public void testFormatBeans(){
        
        PersonFormatNameAsResultBean bean = new PersonFormatNameAsResultBean();
        bean.setFormattedName("Von und zu Gutenberg");
        bean.setFormattedNameAsList("Hans", "Georg", "Auf der Mauer");
        bean.setFormattedNameAsSet("Hans-Jakobli", "Babettli");
        bean.setFormattedNameAsSparseArray("von oben", null, "von unten");
        
        validate(Structures.fromJavaBean(null, test.openmdx.app1.cci2.PersonFormatNameAsResult.class, bean));
        validate(Structures.fromJavaBean(null, test.openmdx.app1.jmi1.PersonFormatNameAsResult.class, bean));
        
    }

    private void validate(
        test.openmdx.app1.cci2.PersonFormatNameAsResult structure
    ){
        assertEquals("Von und zu Gutenberg", structure.getFormattedName());
        {
            List<String> value = structure.getFormattedNameAsList();
            assertNotNull(value);
            assertEquals("List", 3, value.size());
            assertEquals("Hans", value.get(0));
            assertEquals("Georg", value.get(1));
            assertEquals("Auf der Mauer", value.get(2));
        }
        {
            Set<String> value = structure.getFormattedNameAsSet();
            assertNotNull(value);
            assertEquals("Set", 2, value.size());
            assertTrue(value.contains("Hans-Jakobli"));
            assertFalse(value.contains("Meier"));
            assertTrue(value.contains("Babettli"));
        }
        {
            SparseArray<String> value = structure.getFormattedNameAsSparseArray();
            assertNotNull(value);
            assertEquals("SparseArray", 2, value.size());
            assertEquals("von oben", value.get(0));
            assertNull(value.get(1));
            assertEquals("von unten", value.get(2));
        }
    }
    
    @Test
    public void testStructBeans(){
        
        SimpleStruct0_1Bean simple = new SimpleStruct0_1Bean();
        simple.setBooleanField(true);
        simple.setIntegerField(4711);
        
        ComplexStruct0_1Bean complex = new ComplexStruct0_1Bean();
        complex.setSimpleStruct0_1Field(simple);
                
        validate(Structures.fromJavaBean(null, test.openmdx.model1.cci2.ComplexStruct0_1.class, complex));
        validate(Structures.fromJavaBean(null, test.openmdx.model1.jmi1.ComplexStruct0_1.class, complex));

        ComplexStructListBean list = new ComplexStructListBean();
        list.setSimpleStruct0_1Field(simple);

        validate(Structures.fromJavaBean(null, test.openmdx.model1.cci2.ComplexStructList.class, list));
        validate(Structures.fromJavaBean(null, test.openmdx.model1.jmi1.ComplexStructList.class, list));
    }

    private void validate(
        test.openmdx.model1.cci2.ComplexStructList complex
    ){
        List<test.openmdx.model1.cci2.SimpleStruct0_1> list = complex.getSimpleStruct0_1Field();
        assertNotNull(list);
        assertEquals("Singleton List", 1, list.size());
        validate(list.get(0));    

        validate(Structures.toJavaBean(complex, new ComplexStructListBean()));
    }
    
    private void validate(
        ComplexStructListBean complex
    ){
        SimpleStruct0_1Bean[] list = complex.getSimpleStruct0_1Field();
        assertNotNull(list);
        assertEquals("Singleton List", 1, list.length);
        validate(list[0]);        
    }
    
    private void validate(
        test.openmdx.model1.cci2.ComplexStruct0_1 complex
    ){
        validate(complex.getSimpleStruct0_1Field());
        assertNull(complex.getSimpleStruct1_1Field());

        validate(Structures.toJavaBean(complex, new ComplexStruct0_1Bean()));
    }

    private void validate(
        ComplexStruct0_1Bean complex
    ){
        validate(complex.getSimpleStruct0_1Field());
        assertNull(complex.getSimpleStruct1_1Field());
    }
    
    private void validate(
        test.openmdx.model1.cci2.SimpleStruct0_1 simple
    ){
        assertNotNull(simple);
        assertTrue("Boolean", simple.isBooleanField());
        assertNull("String", simple.getStringField());
        assertEquals("Integer", Integer.valueOf(4711),simple.getIntegerField());
        
        validate(Structures.toJavaBean(simple, new SimpleStruct0_1Bean()));
    }

    private void validate(
        SimpleStruct0_1Bean simple
    ){
        assertNotNull(simple);
        assertTrue("Boolean", simple.isBooleanField());
        assertNull("String", simple.getStringField());
        assertEquals("Integer",4711,simple.getIntegerField());
    }
    
}

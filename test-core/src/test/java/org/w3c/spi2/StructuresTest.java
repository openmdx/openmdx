/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: TestStructures 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2011-2021, OMEX AG, Switzerland
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
package org.w3c.spi2;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.cci2.SparseArray;

/**
 * Structures Test
 */
public class StructuresTest {

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
        Assertions.assertEquals("Von und zu Gutenberg", structure.getFormattedName());
		// TODO Auto-generated method stub
        {
            List<String> value = structure.getFormattedNameAsList();
            Assertions.assertNotNull(value);
            Assertions.assertEquals(3, value.size(), "List");
            Assertions.assertEquals("Hans", value.get(0));
            Assertions.assertEquals("Georg", value.get(1));
            Assertions.assertEquals("Auf der Mauer", value.get(2));
        }
        {
            Set<String> value = structure.getFormattedNameAsSet();
            Assertions.assertNotNull(value);
            Assertions.assertEquals(2, value.size(), "Set");
            Assertions.assertTrue(value.contains("Hans-Jakobli"));
            Assertions.assertFalse(value.contains("Meier"));
            Assertions.assertTrue(value.contains("Babettli"));
        }
        {
            SparseArray<String> value = structure.getFormattedNameAsSparseArray();
            Assertions.assertNotNull(value);
            Assertions.assertEquals(2, value.size(), "SparseArray");
            Assertions.assertEquals("von oben", value.get(0));
            Assertions.assertNull(value.get(1));
            Assertions.assertEquals("von unten", value.get(2));
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
        Assertions.assertNotNull(list);
        Assertions.assertEquals(1, list.size(), "Singleton List");
        validate(list.get(0));    
        validate(Structures.toJavaBean(complex, new ComplexStructListBean()));
    }
    
    private void validate(
        ComplexStructListBean complex
    ){
        SimpleStruct0_1Bean[] list = complex.getSimpleStruct0_1Field();
        Assertions.assertNotNull(list);
        Assertions.assertEquals(1, list.length, "Singleton List");
        validate(list[0]);        
    }
    
    private void validate(
        test.openmdx.model1.cci2.ComplexStruct0_1 complex
    ){
        validate(complex.getSimpleStruct0_1Field());
        Assertions.assertNull(complex.getSimpleStruct1_1Field());
        validate(Structures.toJavaBean(complex, new ComplexStruct0_1Bean()));
    }

    private void validate(
        ComplexStruct0_1Bean complex
    ){
        validate(complex.getSimpleStruct0_1Field());
        Assertions.assertNull(complex.getSimpleStruct1_1Field());
    }
    
    private void validate(
        test.openmdx.model1.cci2.SimpleStruct0_1 simple
    ){
    	Assertions.assertNotNull(simple);
        Assertions.assertTrue(simple.isBooleanField(), "Boolean");
        Assertions.assertNull(simple.getStringField(), "String");
        Assertions.assertEquals(Integer.valueOf(4711), simple.getIntegerField(), "Integer");
        
        validate(Structures.toJavaBean(simple, new SimpleStruct0_1Bean()));
    }

    private void validate(
        SimpleStruct0_1Bean simple
    ){
    	Assertions.assertNotNull(simple);
        Assertions.assertTrue(simple.isBooleanField(), "Boolean");
        Assertions.assertNull(simple.getStringField(), "String");
        Assertions.assertEquals(4711, simple.getIntegerField(), "Integer");
    }
    
}

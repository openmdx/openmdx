/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: ComplexStructListBean 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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

/**
 * ComplexStructListBean
 *
 */
public class ComplexStructListBean
{
    private SimpleStruct0_1Bean[] simpleStruct0_1Field = new SimpleStruct0_1Bean[]{};
    private Object[] simpleStruct0_nField = new Object[]{};
    private Object simpleStruct1_1Field;
    private Object[] simpleStructListField = new Object[]{};
    private Object[] simpleStructSetField = new Object[]{};
    private Object[] simpleStructSparseArrayField = new Object[]{};
    public SimpleStruct0_1Bean[] getSimpleStruct0_1Field() {
        return this.simpleStruct0_1Field;
    }
    public SimpleStruct0_1Bean getSimpleStruct0_1Field(int index) {
        return this.simpleStruct0_1Field[index];
    }
    public void setSimpleStruct0_1Field(SimpleStruct0_1Bean... simpleStruct0_1Field) {
        this.simpleStruct0_1Field = simpleStruct0_1Field;
    }
    public void setSimpleStruct0_1Field(int index, SimpleStruct0_1Bean simpleStruct0_1Field) {
        this.simpleStruct0_1Field[index] = simpleStruct0_1Field;
    }
    public Object[] getSimpleStruct0_nField() {
        return this.simpleStruct0_nField;
    }
    public Object getSimpleStruct0_nField(int index) {
        return this.simpleStruct0_nField[index];
    }
    public void setSimpleStruct0_nField(Object... simpleStruct0NField) {
        this.simpleStruct0_nField = simpleStruct0NField;
    }
    public void setSimpleStruct0_nField(int index, Object simpleStruct0NField) {
        this.simpleStruct0_nField[index] = simpleStruct0NField;
    }
    public Object getSimpleStruct1_1Field() {
        return this.simpleStruct1_1Field;
    }
    public void setSimpleStruct1_1Field(Object simpleStruct1_1Field) {
        this.simpleStruct1_1Field = simpleStruct1_1Field;
    }
    public Object[] getSimpleStructListField() {
        return this.simpleStructListField;
    }
    public Object getSimpleStructListField(int index) {
        return this.simpleStructListField[index];
    }
    public void setSimpleStructListField(Object... simpleStructListField) {
        this.simpleStructListField = simpleStructListField;
    }
    public void setSimpleStructListField(int index,Object simpleStructListField) {
        this.simpleStructListField[index] = simpleStructListField;
    }
    public Object[] getSimpleStructSetField() {
        return this.simpleStructSetField;
    }
    public Object getSimpleStructSetField(int index) {
        return this.simpleStructSetField[index];
    }
    public void setSimpleStructSetField(Object... simpleStructSetField) {
        this.simpleStructSetField = simpleStructSetField;
    }
    public void setSimpleStructSetField(int index, Object simpleStructSetField) {
        this.simpleStructSetField[index] = simpleStructSetField;
    }
    public Object[] getSimpleStructSparseArrayField() {
        return this.simpleStructSparseArrayField;
    }
    public Object getSimpleStructSparseArrayField(int index) {
        return this.simpleStructSparseArrayField[index];
    }
    public void setSimpleStructSparseArrayField(
        Object... simpleStructSparseArrayField) {
        this.simpleStructSparseArrayField = simpleStructSparseArrayField;
    }
    public void setSimpleStructSparseArrayField(
        int index,
        Object simpleStructSparseArrayField) {
        this.simpleStructSparseArrayField[index] = simpleStructSparseArrayField;
    }
    
}

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ValueObject.java,v 1.2 2006/03/08 10:54:51 hburger Exp $
 * Description: ValueObject 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/03/08 10:54:51 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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

package org.openmdx.test.base.object.jre.since1_5;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * ValueObject
 */
public class ValueObject implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -138991290040416222L;

    transient Object id;
    public String identity;
    public Date field1;
    public Set field2;
    public Date field3;
    public Set field4;
    public int field5;
    public String field6;
    public String field7;
    
    public final Date getField1() {
    return this.field1;}
    
    public final Set getField2() {
    return this.field2;}
    
    public final Date getField3() {
    return this.field3;}
    
    public final Set getField4() {
    return this.field4;}
    
    public final int getField5() {
    return this.field5;}
    
    public final String getField6() {
    return this.field6;}
    
    public final String getField7() {
    return this.field7;}
    
    public final Object getId() {
    return this.id;}
    
    public final String getIdentity() {
    return this.identity;}
    
    public final void setField1(Date f1) {
    this.field1 = f1;}
    
    public final void setField2(Set f2) {
    this.field2 = f2;}
    
    public final void setField3(Date f3) {
    this.field3 = f3;}
    
    public final void setField4(Set f4) {
    this.field4 = f4;}
    
    public final void setField5(int f5) {
    this.field5 = f5;}
    
    public final void setField6(String f6) {
    this.field6 = f6;}
    
    public final void setField7(String f7) {
    this.field7 = f7;}
    
    public final void setIdentity(String identity) {
    this.identity = identity;}
        
}

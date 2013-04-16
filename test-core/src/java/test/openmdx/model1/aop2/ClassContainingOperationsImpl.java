/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: ClassContainingOperationsImpl 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
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
package test.openmdx.model1.aop2;

import org.openmdx.base.aop2.AbstractObject;
import org.w3c.spi2.Structures;

import test.openmdx.model1.jmi1.ComplexStruct0_1;
import test.openmdx.model1.jmi1.TestComplexStruct0_1_0_1Result;

/**
 * ClassContainingOperationsImpl
 */
public class ClassContainingOperationsImpl<S extends test.openmdx.model1.jmi1.ClassContainingOperations,N extends test.openmdx.model1.cci2.ClassContainingOperations> 
    extends AbstractObject<S,N,Void> 
{

    /**
     * Constructor 
     *
     * @param same
     * @param next
     */
    public ClassContainingOperationsImpl(
        S same,
        N next
    ) {
        super(same, next);
    }

    public test.openmdx.model1.jmi1.TestComplexStruct0_1_0_1Result testComplexStruct0_1_0_1(
        test.openmdx.model1.jmi1.ClassContainingOperationsTestComplexStruct0_1_0_1Params in
    ){
        ComplexStruct0_1 arg = in.getArg();
        return Structures.create(
            TestComplexStruct0_1_0_1Result.class,
            arg
        );
    }
    
}

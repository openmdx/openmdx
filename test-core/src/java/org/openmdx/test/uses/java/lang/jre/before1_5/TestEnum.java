/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestEnum.java,v 1.2 2006/01/06 17:44:07 hburger Exp $
 * Description: class TestPathTransformation
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/01/06 17:44:07 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.test.uses.java.lang.jre.before1_5;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.uses.java.lang.Enum;

public class TestEnum extends TestCase {

    /**
     * Constructs a test case with the given name.
     */
    public TestEnum(String name) {
        super(name);
    }
    
    /**
     * The batch TestRunner can be given a class to run directly.
     * To start the batch runner from your main you can write: 
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    
    /**
     * A test runner either expects a static method suite as the
     * entry point to get a test to run or it will extract the 
     * suite automatically. 
     */
    public static Test suite() {
        return new TestSuite(TestEnum.class);
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public void testBaseEnum(
    ) throws IOException, ClassNotFoundException{
        printEnum(BaseEnumeration.values());
        assertSame(
            BaseEnumeration.Z.toString(), 
            BaseEnumeration.Z,
            BaseEnumeration.valueOf(BaseEnumeration.Z.name())
        );
        ByteArrayOutputStream raw = new  ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(raw);
        out.writeObject(BaseEnumeration.Y);
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(raw.toByteArray()));
        assertSame(
            BaseEnumeration.Y.toString(), 
            BaseEnumeration.Y,
            in.readObject()
        );
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testStateEnum(
    ){
        printEnum(State.values());
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testPublicInnerEnum(
    ){
        printEnum(PublicInnerEnumeration.values());
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testProtectedInnerEnum(
    ){
        printEnum(ProtectedInnerEnumeration.values());
    }

    protected void printEnum(
        Enum[] list
    ){
        System.out.println(getName());
        for(
            int i = 0;
            i < list.length;
            i++
        ){
            Enum e = list[i];
            System.out.println(e.toString()+'@'+e.ordinal()+": "+e.getClass().getName()+'.'+e.name());
        }
        System.out.println();
    }

    /**
     * Protected Inner Class Works
     */
    static protected class ProtectedInnerEnumeration extends Enum {
    
        protected ProtectedInnerEnumeration(){}
        
        private static final long serialVersionUID = 3978706181705642293L;

        final public static ProtectedInnerEnumeration PASSIVE = new ProtectedInnerEnumeration();
        final public static ProtectedInnerEnumeration ACTIVE = new ProtectedInnerEnumeration();
        final public static ProtectedInnerEnumeration SUSPENDED = new ProtectedInnerEnumeration();
        
        public static ProtectedInnerEnumeration[] values(
        ){
            return (ProtectedInnerEnumeration[]) getEnumConstants(ProtectedInnerEnumeration.class);
        }
        public static ProtectedInnerEnumeration valueOf(
            String name
        ){
            return (ProtectedInnerEnumeration) valueOf(ProtectedInnerEnumeration.class, name);
        }

    }

    /**
     * Public Inner Class Works
     */
    static public class PublicInnerEnumeration extends Enum {

        protected PublicInnerEnumeration() {}
        
        private static final long serialVersionUID = 3258691000862979381L;

        public final static PublicInnerEnumeration X = new PublicInnerEnumeration();
        public final static PublicInnerEnumeration Y = new PublicInnerEnumeration();
        public final static PublicInnerEnumeration Z = new PublicInnerEnumeration();
        public final static PublicInnerEnumeration A = new PublicInnerEnumeration();

        public static PublicInnerEnumeration[] values(
        ){
            return (PublicInnerEnumeration[]) getEnumConstants(PublicInnerEnumeration.class);
        }
        public static PublicInnerEnumeration valueOf(
            String name
        ){
            return (PublicInnerEnumeration) valueOf(PublicInnerEnumeration.class, name);
        }

    }

}

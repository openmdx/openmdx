/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestTransactionId.java,v 1.2 2005/04/04 12:39:44 hburger Exp $
 * Description: TestTransactionId
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/04/04 12:39:44 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.test.kernel.application.container;

import java.io.UnsupportedEncodingException;

import javax.transaction.xa.Xid;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.application.container.transaction.TransactionIdFactory;
import org.openmdx.kernel.text.format.HexadecimalFormatter;

/**
 * TestTransactionId
 */
public class TestTransactionId extends TestCase {
    
    /**
     * Constructor
     */
    public TestTransactionId(
        String name
    ) {
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
      return new TestSuite(TestTransactionId.class);
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp(
    ) throws Exception {
        this.generator = new TransactionIdFactory();
    }

    protected void tearDown(
    ) throws Exception {
        this.generator = null;
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     * 
     * @throws ServiceException
     */
    public void testTransactionId(
    ) throws ServiceException, UnsupportedEncodingException {
        Xid transactionId = this.generator.createTransactionId();
        System.out.println("Transaction: " + transactionId);
        assertEquals(
            "TransactionId toString()",
            "Xid:" + new HexadecimalFormatter("LC".getBytes("ASCII"))+ '-' + new HexadecimalFormatter(transactionId.getGlobalTransactionId()),
            transactionId.toString()
        );
        Xid transactionBranchId = this.generator.createTransactionBranchId(transactionId, 0x1234);
        System.out.println("Branch:      " + transactionBranchId);
        assertEquals(
            "TransactionBranchId toString()",
            transactionId.toString() + "-00001234",
            transactionBranchId.toString()
        );
    }

    private TransactionIdFactory generator;
      
}

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestFilter.java,v 1.6 2006/08/11 09:25:23 hburger Exp $
 * Description: Test Filter
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/08/11 09:25:23 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.test.base.query;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.IsBetweenCondition;
import org.openmdx.base.query.IsGreaterCondition;
import org.openmdx.base.query.IsGreaterOrEqualCondition;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.IsLikeCondition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.query.SoundsLikeCondition;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.query.Quantors;

/**
 * Test Filter
 */
public class TestFilter
    extends TestCase
{

    /**
     * Constructs a test case with the given name.
     */
    public TestFilter(String name) {
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
        return new TestSuite(TestFilter.class);
    }

    public void testEncode() {
      try {
        String scratchFile = System.getProperty("java.io.tmpdir") + getClass().getName() + ".tmp";
        System.out.println("writing to file " + scratchFile);
        OutputStream os = new FileOutputStream(scratchFile);
        Filter filter = new Filter(
            new Condition[]{
                new IsBetweenCondition(
                    Quantors.FOR_ALL,
                    "fBetween",
                    true,
                    "Lower", "Upper"
                ),
                new IsBetweenCondition(
                    Quantors.FOR_ALL,
                    "fBetween",
                    false,
                    "Lower", "Upper"
                ),
                new IsGreaterCondition(
                    Quantors.FOR_ALL,
                    "fGreater",
                    true,
                    new Integer[]{new Integer(-1000), new Integer(1000)}
                ),
                new IsGreaterCondition(
                    Quantors.FOR_ALL,
                    "fGreater",
                    false,
                    new Integer[]{new Integer(-1000), new Integer(1000)}
                ),
                new IsGreaterOrEqualCondition(
                    Quantors.FOR_ALL,
                    "fGreaterOrEqual",
                    true,
                    new Double[]{new Double("-9999999999999.123"), new Double("99999999999.123")}
                ),
                new IsGreaterOrEqualCondition(
                    Quantors.FOR_ALL,
                    "fGreaterOrEqual",
                    false,
                    new Double[]{new Double("-9999999999999.123"), new Double("99999999999.123")}
                ),
                new IsInCondition(
                    Quantors.FOR_ALL,
                    "fIsIn",
                    true,
                    new Boolean[]{Boolean.FALSE, Boolean.TRUE}
                ),
                new IsInCondition(
                    Quantors.FOR_ALL,
                    "fIsIn",
                    false,
                    new Boolean[]{Boolean.FALSE, Boolean.TRUE}
                ),
                new IsLikeCondition(
                    Quantors.FOR_ALL,
                    "fIsLike",
                    true,
                    new String[]{"IsLikeCondition", "IsLikeCondition"}
                ),
                new IsLikeCondition(
                    Quantors.FOR_ALL,
                    "fIsLike",
                    false,
                    new String[]{"IsLikeCondition", "IsLikeCondition"}
                ),
                new SoundsLikeCondition(
                    Quantors.FOR_ALL,
                    "fSoundsLike",
                    true,
                    new String[]{"uri:@openmdx:a.b.c/:*/This/%", "uri:@openmdx:a.b.c/:*/That/%"}
                ),
                new SoundsLikeCondition(
                    Quantors.FOR_ALL,
                    "fSoundsLike",
                    false,
                    new String[]{"uri:@openmdx:a.b.c/:*/This/%", "uri:@openmdx:a.b.c/:*/That/%"}
                )
            },
            new OrderSpecifier[]{
                new OrderSpecifier("order_ASC", Directions.ASCENDING),
                new OrderSpecifier("order_DESC", Directions.DESCENDING)
            }
        );
        XMLEncoder encoder = new XMLEncoder(os);
        encoder.writeObject(filter);
        encoder.close();

        InputStream is = new FileInputStream(scratchFile);
        XMLDecoder decoder = new XMLDecoder(is);
        //... Filter filter2 = (Filter)
            decoder.readObject();
        decoder.close();

      }
      catch(FileNotFoundException e) {
        fail("can not open scratch file");
      }
    }

}

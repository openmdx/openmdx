/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestIteration.java,v 1.8 2009/02/04 11:06:38 hburger Exp $
 * Description: class TestIteration 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/04 11:06:38 $
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
package org.openmdx.test.compatibility.base.dataprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderLayers;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.Directions;
import org.openmdx.application.dataprovider.cci.RequestCollection;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

public class TestIteration extends TestCase {

    /**
     * Constructs a test case with the given name.
     */
    public TestIteration(String name) {
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
        return new TestSuite(TestIteration.class);
    }

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected List root;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected RequestCollection requests;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected Layer_1_0 layer;

    /**
     * Where the objects are stored
     */ 
    final static Path REFERENCE = new Path("xri:@openmdx:org.openmdx.test1/provider");

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp(
    ) throws Exception {
        this.root = new ArrayList();
        for(
            int index = 0;
            index < 10;
            index++
        ) this.root.add(
            new DataproviderObject(
                REFERENCE.getChild("p" + index)
            )
        );
        this.layer = new Layer(root);
        this.layer.activate(
            DataproviderLayers.PERSISTENCE,
            new Configuration(),
            null
        );
        this.requests = new RequestCollection(
            new ServiceHeader(),
            this.layer
        );
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void tearDown(
    ) throws Exception {
        this.layer.deactivate();
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testAtOnce(
    ) throws ServiceException {
        try {
            requests.clear(); // for the sake of security
            List reply = requests.addFindRequest(
                new Path(""),
                null
            );
            assertEquals("Compare the whole list", root, reply);
        } catch (ServiceException exception) {
            exception.log();
            throw exception;
        } catch (RuntimeServiceException exception) {
            AppLog.error(
        exception.getMessage(),
        exception.toString()
      );
            throw exception;
        }
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testIterating(
    ) throws ServiceException {
        try {
            requests.clear(); // for the sake of security
            List reply = requests.addFindRequest(
                new Path(""),
                null,
                AttributeSelectors.NO_ATTRIBUTES,
                0,
                2,
                Directions.ASCENDING
            );
            assertEquals("Compare the whole list", root, reply);
        } catch (ServiceException exception) {
            exception.log();
            throw exception;
        } catch (RuntimeServiceException exception) {
      AppLog.error(
        exception.getMessage(),
        exception.toString()
      );
            throw exception;
        }
    }


    //------------------------------------------------------------------------
    // Classes
    //------------------------------------------------------------------------
    
    class Layer 
        extends Layer_1
    {
        
        final List buffer;
        
        Layer(
            List objects
        ){
            this.buffer = objects;
        }   

        /**
         * Get the objects specified by the references and filter properties.
         *
         * @param       request
         *              the request
         *
         * @exception   ServiceException
         *              on failure
         */
        public DataproviderReply find(
            ServiceHeader header,
            DataproviderRequest request
        ) throws ServiceException {
            DataproviderReply reply;
            if(
                request.path().isEmpty() &&
                request.attributeFilter().length == 0
            ){
                List objects = new ArrayList();
                int count = request.size();
                boolean hasMore = false;
                if (
                    request.position() < this.buffer.size()
                ) switch(request.direction()){
                    case Directions.ASCENDING:
                        for(
                            ListIterator iterator = this.buffer.listIterator(
                                request.position()
                            );
                            (hasMore = iterator.hasNext()) && count > 0;
                            count--
                        ) objects.add(iterator.next());
                    break;
                    case Directions.DESCENDING:
                        for(
                            ListIterator iterator = this.buffer.listIterator(
                                request.position()+1
                            );
                            (hasMore = iterator.hasPrevious()) && count > 0;
                            count--
                        ) objects.add(0,iterator.previous());
                    break;
                }
                reply = new DataproviderReply(objects);
                reply.context(
                    DataproviderReplyContexts.HAS_MORE
                ).set(
                    0,
                    new Boolean(hasMore)
                );
                reply.context(
                    DataproviderReplyContexts.TOTAL
                ).set(
                    0,
                    new Integer(this.buffer.size())
                );
                reply.context(
                    DataproviderReplyContexts.ATTRIBUTE_SELECTOR
                ).set(
                  0,
                  new Short(request.attributeSelector())
                );
                reply.context(
                    DataproviderReplyContexts.ITERATOR
                ).set(
                  0,
                  new byte[]{}
                );
            } else {
                reply = new DataproviderReply();
                reply.context(DataproviderReplyContexts.HAS_MORE).set(0,Boolean.FALSE);
                reply.context(DataproviderReplyContexts.TOTAL).set(0,new Integer(0));
            }
            AppLog.info("request="+request,"reply="+reply);
            return reply;
        }   
    }
            
}

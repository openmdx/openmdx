/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestRoleApplication_1.java,v 1.8 2008/09/10 18:10:56 hburger Exp $
 * Description: User Profile Service
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 18:10:56 $
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
package org.openmdx.test.compatibility.base.dataprovider.layer.model;

import java.util.Arrays;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * @author anyff
 *
 * test application layer for roleObject
 */
public class TestRoleApplication_1 extends Layer_1 {

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#get(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest   request
    ) throws ServiceException {
        Path path = request.path();
        return path.isLike(STATE_PATTERN_PATTERN) ? newReply(
            path,
            path.get(0) + ":Pattern"
        ) : path.isLike(STATE_SEGMENT_PATTERN) || path.isLike(ROLE_SEGMENT_PATTERN) ? newReply(
            path,
            path.get(0) + ":Segment"
        ) : super.get(
            header, 
            request
        );
    }


    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#prolog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest[])
     */
    public void prolog(
        ServiceHeader header, 
        DataproviderRequest[] requests
    ) throws ServiceException {
        SysLog.detail("Entries into the CR20006725 segment should be rejected", Arrays.asList(requests));
        for(
                int i = 0;
                i < requests.length;
                i++
        ) {
            if(requests[i].path().isLike(REJECTED_PATTERN)) throw new ServiceException(
                "CR20006725",
                1,
                "This layer may be bypassed by lenient requests",
                new BasicException.Parameter("path", requests[i].path())
            );
        }
        SysLog.detail("None of the request is an entry of the CR20006725 segment", Arrays.asList(requests));
        super.prolog(header, requests);
    }

    protected DataproviderReply newReply(
        Path path,
        String objectClass
    ){
        DataproviderObject object = new DataproviderObject(path);
        object.values(
            SystemAttributes.OBJECT_CLASS
        ).add(
            objectClass
        );
        return new DataproviderReply(object);
    }


    /**
     * supply some derived Attributes for the classes knowing them.
     */
    public void epilog(
        ServiceHeader header,
        DataproviderRequest[] requests,
        DataproviderReply[] replies
    ) throws ServiceException {
        SysLog.trace("in epilog");
        if(requests.length != replies.length) {
            RuntimeServiceException assertionFailure = new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.ASSERTION_FAILURE,
                "The numbers of requests and replies do not match",
                null
            );
            SysLog.error(
                assertionFailure.getMessage(), 
                assertionFailure.getCause()
            );
            throw assertionFailure;
        }
        super.epilog(
            header,
            requests,
            replies
        );

        for (
                int i = 0;
                i < replies.length;
                i++
        ) {            
            DataproviderObject[] objects = replies[i].getObjects();
            DataproviderObject object = null;

            for (
                    int j = 0;
                    j < objects.length;
                    j++
            ) {            
                object = objects[j];

                SysLog.trace("epilog: object.class", object.getValues(SystemAttributes.OBJECT_CLASS));

                // don't bother if object_class can not be detected
                // the attribtue just stays empty.
                if (object.getValues(SystemAttributes.OBJECT_CLASS) != null 
                        && 
                        !object.getValues(SystemAttributes.OBJECT_CLASS).isEmpty()
                        && 
                        (object.getValues(SystemAttributes.OBJECT_CLASS).get(0).equals("org:openmdx:test:compatibility:role1:RoleNoRole")
                                || object.getValues(SystemAttributes.OBJECT_CLASS).get(0).equals("org:openmdx:test:compatibility:role1:RoleClassRoleB")
                                || object.getValues(SystemAttributes.OBJECT_CLASS).get(0).equals("org:openmdx:test:compatibility:role1:RoleClassD")
                                || object.getValues(SystemAttributes.OBJECT_CLASS).get(0).equals("org:openmdx:test:compatibility:role1:RoleClassDRoleA")
                                || object.getValues(SystemAttributes.OBJECT_CLASS).get(0).equals("org:openmdx:test:compatibility:state1:RoleClassRoleB")
                                || object.getValues(SystemAttributes.OBJECT_CLASS).get(0).equals("org:openmdx:test:compatibility:state1:RoleClassD")
                                || object.getValues(SystemAttributes.OBJECT_CLASS).get(0).equals("org:openmdx:test:compatibility:state1:RoleClassDRoleA")
                        )
                ) {
                    SysLog.trace("epilog: setting derived attribute \"application\"");
                    object.values("application").add("test");
                }
            }
        }
    }

    private final static Path STATE_PATTERN_PATTERN = new Path(
        new String[]{
            ":org:openmdx:test:compatibility:state1:*",
            "provider", ":*",
            "segment", ":*"            
        }
    );

    private static final Path STATE_SEGMENT_PATTERN = STATE_PATTERN_PATTERN.getDescendant(
        new String[]{
            ":*", ":*"
        }
    );

    private final static Path ROLE_SEGMENT_PATTERN = new Path(
        new String[]{
            "org:openmdx:test:compatibility:role1",
            "provider", ":*",
            "segment", ":*"            
        }
    );

    private final static Path REJECTED_PATTERN = new Path(
        "xri:@openmdx:org.openmdx.test.compatibility.state1.inclusive/provider/Jdbc/segment/Inclusive/inclusive/CR20006725/:*/:*"
    );

}

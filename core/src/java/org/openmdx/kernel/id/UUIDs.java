/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UUIDs.java,v 1.5 2008/01/08 16:16:32 hburger Exp $
 * Description: UUIDs
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/08 16:16:32 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
package org.openmdx.kernel.id;

import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.id.cci.UUIDBuilder;
import org.openmdx.kernel.id.cci.UUIDGenerator;
import org.openmdx.kernel.id.plugin.TimeBasedUUIDGeneratorUsingRandomBasedNode;


import java.util.UUID;




/**
 * UUIDs
 * <p>
 * This class can be configured with system properties, e.g.<ul>
 * <li>-Dorg.openmdx.uuid.generator=org.openmdx.kernel.id.plugin.TimeBasedUUIDGeneratorUsingRandomBasedNode (default)
 * <li>-Dorg.openmdx.uuid.generator=org.openmdx.kernel.id.plugin.RandomBasedUUIDGenerator
 * </ol>
 */
public class UUIDs {

    /**
     * Returns an URN representation of a UUID according to 
     * http://www.ietf.org/internet-drafts/draft-mealling-uuid-urn-03.txt.
     * <p>
     * The UUID "f81d4fae-7dec-11d0-a765-00a0c91e6bf6" for example is
     * represented as "urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6".
     * 
     * @param uuid to be represented as URN
     * 
     * @return an URN corresponding to the provided uuid
     */
    public static final String toURN(
         UUID uuid
    ){
         return "urn:uuid:" + uuid;
    }

    /**
     * Load the configured UUID provider
     * <p>
     * If multiple threads need to generate UUIDs simultaneously then
     * each should obtain its own <code>UUIDBuilder</code> instance.
     * 
     * @return a UUID provider instance
     */
    public static UUIDGenerator getGenerator(
    ){
        try {
            return (UUIDGenerator) UUIDs.uuidGenerator.newInstance();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Load the configured UUID provider
     * <p>
     * If multiple threads need to build UUIDs simultaneously then
     * each should obtain its own <code>UUIDBuilder</code> instance.
     * 
     * @return a UUID provider instance
     */
    public static UUIDBuilder getBuilder(
        String namespace
    ){
        return null; //...
    }

    /**
     * The NIL UUID as specified in 
     * http://www.ietf.org/internet-drafts/draft-mealling-uuid-urn-03.txt.
     */
    public final static UUID NIL = new UUID(0l, 0l);

    /**
     * The UUID generator class
     */
    private static Class uuidGenerator;

    /**
     * The UUID provider system property.
     */
    public static final String GENERATOR = "org.openmdx.uuid.generator";

    static {
        try {
            uuidGenerator = Classes.getApplicationClass(
                System.getProperty(
                    GENERATOR,
                    TimeBasedUUIDGeneratorUsingRandomBasedNode.class.getName()
                )
            );
        } catch (ClassNotFoundException e) {
            uuidGenerator = null;
            e.printStackTrace();
        }
    }

}

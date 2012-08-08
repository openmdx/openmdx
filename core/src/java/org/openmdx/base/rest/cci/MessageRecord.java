/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: MessageRecord.java,v 1.3 2010/03/23 19:03:08 hburger Exp $
 * Description: Message Record 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/23 19:03:08 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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

package org.openmdx.base.rest.cci;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.naming.Path;


/**
 * Message Record
 */
public interface MessageRecord extends MappedRecord {

    /**
     * An message record's name
     */
    String NAME = "org:openmdx:kernel:Message";
    
    /**
     * Retrieve the message's destination and id
     * 
     * @return the destination and id
     */
    Path getPath();
    
    /**
     * Set the message's destination and id
     * 
     * @param path the message's destination and id
     */
    void setPath(Path path);
    
    /**
     * Retrieve the message body
     * 
     * @return the message body
     */
    MappedRecord getBody();
    
    /**
     * Set the message body
     * 
     * @param body the message body, or <code>null</code> to assign 
     * an <code>org::openmdx::base::Void</code> instance
     */
    void setBody(MappedRecord body);

    /**
     * Retrieve the target
     * 
     * @return the arget
     */
    Path getTarget();
    
    /**
     * Retrieve the message id
     * 
     * @return the message id
     */
    String getMessageId();
    
    /**
     * Retrieve the message correlation id
     * 
     * @return the message correlation id
     */
    String getCorellationId();

    /**
     * Clone the record
     * 
     * @return a shallow clone
     */
    MessageRecord clone(
    ) throws CloneNotSupportedException;
    
}

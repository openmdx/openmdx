/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UnitOfWorkReply.java,v 1.6 2005/02/21 13:10:34 hburger Exp $
 * Description: Dataprovider Cursor
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/02/21 13:10:34 $
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
package org.openmdx.compatibility.base.dataprovider.cci;

import java.io.Serializable;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.text.format.IndentingFormatter;


public class UnitOfWorkReply 
    implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 3257569520427086136L;

    /**
     * Reply to successfull requests
     */
    public UnitOfWorkReply(
        DataproviderReply[] replies
    ){
        this.replies = replies;
        this.status = null;
    }

    /**
     * Reply to failed requests
     */
    public UnitOfWorkReply(
        ServiceException exception
    ){
        this.replies = null;
        this.status = exception;
    }


    //------------------------------------------------------------------------
    // Members
    //------------------------------------------------------------------------

    /**
     * Get the objects list
     *
     * @return      a list containing objects of class DataproviderObject;
     *              null in case of failure
     */
    public DataproviderReply[] getReplies(
    ){
        return this.replies;
    }

    /**
     * Get the status
     *
     * @return      the reason for the requests failure;
     *              null in case of success
     */
    public ServiceException getStatus(
    ){
        return this.status;
    }
    
    /**
     * Check whether the working unit failed
     *
     * @return  True if the working unit failed
     */
    public boolean failure(
    ){
        return this.status != null;
    }   

    /**
     * Check whether any of the working units failed
     *
     * @return  True if any of the working units failed
     */
    public static boolean failure(
        UnitOfWorkReply[] workingUnits
    ){
        for(
            int index = 0;
            index < workingUnits.length;
            index++
        ) if (workingUnits[index].failure()) return true;
        return false;
    }
                
                
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /**
     * Returns a string representation of the object. In general, the
     * toString method returns a string that "textually represents" this
     * object. The result should be a concise but informative representation
     * that is easy for a person to read. It is recommended that all
     * subclasses override this method.
     *
     * @return      a string representation of the object.
     */
    public String toString(
    ){
    	return getClass().getName() + ": " + (
    		failure() ?
	    	    getStatus().toString() :
	    	    IndentingFormatter.toString(getReplies())
	    );
    } 


    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    /**
     *
     */
    private final DataproviderReply[] replies;

    /**
     *
     */
    private final ServiceException status;

}


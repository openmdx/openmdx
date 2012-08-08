/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Involvement_1.java,v 1.7 2010/06/01 08:59:09 hburger Exp $
 * Description: Involvement 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/01 08:59:09 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.compatibility.audit1.aop1;

import java.util.Date;
import java.util.List;

import org.openmdx.audit2.spi.Qualifiers;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.IsGreaterCondition;
import org.openmdx.base.query.IsLikeCondition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.id.UUIDs;


/**
 * Involvement
 */
public class Involvement_1 extends org.openmdx.audit2.aop1.Involvement_1 {

    /**
     * Constructor 
     *
     * @param self
     * @param next
     */
    public Involvement_1(
        ObjectView_1_0 self, 
        Interceptor_1 next
    ) {
        super(self, next);
    }

    private static final Path NULL = new Path("");

    /**
     * 
     */
    private Path afterImagePath = Involvement_1.NULL;
    
    /**
     * Retrive the unit of work container
     * 
     * @return the unit of work container
     */
    protected Container_1_0 getUnitOfWorkContainer(
    ){
        return (Container_1_0) self.jdoGetPersistenceManager().getObjectById(
            self.jdoGetObjectId().getPrefix(6)
        );
    }
    
    /**
     * Retrieve the before image path
     * 
     * @return the before image path
     * 
     * @throws ServiceException  
     */
    protected Path getBeforeImagePath(
    ) throws ServiceException {
        return Qualifiers.getAudit1ImageId(
            this.getConfiguration(), 
            this.getObjectPath(), 
            this.getUnitOfWorkId()
        );
    }

    /**
     * Retrieve the before image
     * 
     * @return the before image
     * @throws ServiceException  
     */
    protected ObjectView_1_0 getBeforeImage(
    ) throws ServiceException {
        return (ObjectView_1_0) self.jdoGetPersistenceManager().getObjectById(
            this.getBeforeImagePath()
        );
    }

    /**
     * Retrieve the next unit of work
     * 
     * @return the next unit of work
     * 
     * @throws ServiceException
     */
    protected ObjectView_1_0 getNextUnitOfWork(
    ) throws ServiceException{
        Date createdAt = (Date) this.getUnitOfWork().objGetValue(SystemAttributes.CREATED_AT);
        List<DataObject_1_0> involving  = this.getUnitOfWorkContainer().subMap(
            new Filter(
                new IsLikeCondition(
                    Quantifier.THERE_EXISTS,
                    "involved",
                    true, // IS_LIKE,
                    Qualifiers.toAudit1InvolvedId(this.getObjectPath())
                ),
                new IsGreaterCondition(
                    Quantifier.THERE_EXISTS,
                    SystemAttributes.CREATED_AT,
                    true, // IS_GREATER,
                    createdAt
                )
            )
        ).values(
            new OrderSpecifier(
                SystemAttributes.CREATED_AT,
                SortOrder.ASCENDING
            )
        );
        return involving.isEmpty() ? null : (ObjectView_1_0)involving.get(0);
    }
    
    
    /**
     * Retrieve the after image path
     * 
     * @return the after image path
     */
    protected Path getAfterImagePath(
    ) throws ServiceException {
        if(this.afterImagePath == Involvement_1.NULL) {
            ObjectView_1_0 nextUnitOfWork = this.getNextUnitOfWork();
            if(nextUnitOfWork == null) {
                ObjectView_1_0 object = this.getObject();
                if(object == null) {
                    return this.afterImagePath = null;
                }
                Path candidate = Qualifiers.getAudit1ImageId(
                    this.getConfiguration(), 
                    this.getObjectPath(), 
                    UUIDConversion.toUID(UUIDs.newUUID())
                );
                return this.afterImagePath = ((DataObject_1)object.objGetDelegate()).getBeforeImage(candidate).jdoGetObjectId();
            } else {
                return this.afterImagePath = Qualifiers.getAudit1ImageId(
                    this.getConfiguration(), 
                    this.getObjectPath(), 
                    (String) nextUnitOfWork.objGetValue("unitOfWorkId")
                );
            }
        } else {
            return this.afterImagePath;
        }
    }
    
    /**
     * Retrieve the after image
     * 
     * @return the after image
     */
    protected ObjectView_1_0 getAfterImage(
    ) throws ServiceException {
        Path afterImagePath = this.getAfterImagePath();
        if(afterImagePath == null) {
            return null;
        } 
        ObjectView_1_0 afterImage = (ObjectView_1_0) self.jdoGetPersistenceManager().getObjectById(
            afterImagePath
        );
        Date transactionTime = (Date) this.getUnitOfWork().objGetValue(SystemAttributes.CREATED_AT);
        Date objectVersion = (Date) afterImage.objGetValue(SystemAttributes.MODIFIED_AT);
        boolean matching = transactionTime.equals(objectVersion);
        return matching ? afterImage : null;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.DelegatingObject_1#objGetValue(java.lang.String)
     */
    @Override
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        return
            "beforeImage".equals(feature) ? this.getBeforeImage() : 
            "afterImage".equals(feature) ? this.getAfterImage() :
            super.objGetValue(feature);
    }

    
}

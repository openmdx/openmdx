/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Qualifiers.java,v 1.6 2010/01/03 15:02:03 wfro Exp $
 * Description: Qualifiers 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/01/03 15:02:03 $
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
package org.openmdx.audit2.spi;

import java.util.Date;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.format.DateTimeFormat;

/**
 * Qualifiers
 */
public class Qualifiers {

    /**
     * Constructor 
     *
     */
    private Qualifiers(
    ) {
        // Avoid instantiation
    }

    /**
     * Leave out all delimiters
     */
    private static final DateTimeFormat VERSION_SUBSEGMENT = DateTimeFormat.getInstance("!yyyyMMddHHmmssSSS");

    /**
     * Build an org::openmdx::audit2 compliant qualifier
     * 
     * @param qualifier
     * @param version
     * 
     * @return an audit2 compliant qualifier
     */
    public static String toAudit2ImageQualifier(
        String qualifier,
        Date version
    ){
        return qualifier + (version == null ? "!%" : VERSION_SUBSEGMENT.format(version));
    }

    /**
     * Extract the base from an audit2 qualifier
     * 
     * @param segment
     * 
     * @return the base from an audit2 qualifier
     */
    public static String getAudit2ObjectQualifier(
        String segment
    ){
        return segment.substring(0, segment.lastIndexOf('!'));
    }
    
    /**
     * Build an org::openmdx::compatibility::audit1 compliant qualifier
     * 
     * @param qualifier
     * @param unitOfWorkId
     * 
     * @return an audit1 compliant qualifier
     */
    public static String toAudit1ImageQualifier(
        String qualifier,
        String unitOfWorkId
    ){
        return qualifier + ":uow:" + unitOfWorkId;
    }
    
    /**
     * Extract the base from an audit2 qualifier
     * 
     * @param qualifier
     * 
     * @return the base from an audit2 qualifier
     */
    public static String getAudit1ObjectQualifier(
        String qualifier
    ){
        return qualifier.substring(0, qualifier.lastIndexOf(":uow:"));
    }

    public static Path toAudit1InvolvedId(
        Path objectId
    ){
        return objectId.getDescendant("view:Audit:involved", ":*");
    }

    /**
     * Create a before or after image identifier
     * 
     * @param configuration
     * @param objectId
     * @param modifiedAt
     * @param unitIfWorkId
     * 
     * @return the object's before or after image id
     * @throws ServiceException
     */
    public static Path getAudit1ImageId(
        Configuration configuration, 
        Path objectId, 
        String unitOfWorkId
    ) throws ServiceException {
        if (unitOfWorkId == null) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "'unitOfWorkId' must not be null"
        );
        for (Map.Entry<Path, Path> entry : configuration.getMapping().entrySet()) {
            if (objectId.startsWith(entry.getKey())) {
                Path imageId = new Path(entry.getValue());
                int iLimit = objectId.size() - 1;
                for (
                    int i = entry.getKey().size(); 
                    i < iLimit; 
                    i++
                ) {
                    String segment = objectId.get(i);
                    imageId.add(segment);
                    if(segment.endsWith("%")) return imageId;
                }
                return imageId.add(toAudit1ImageQualifier(objectId.get(iLimit), unitOfWorkId));
            }
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE,
            "Data object pattern for the given object id missing",
            new BasicException.Parameter("objectId", objectId)
        );
    }

    /**
     * Create a before or after image identifier
     * 
     * @param configuration
     * @param objectId
     * @param modifiedAt
     * @param unitIfWorkId
     * 
     * @return the object's before or after image id
     * @throws ServiceException 
     * @throws ServiceException
     */
    public static Path getAudit2ImageId(
        Configuration configuration, 
        Path objectId, 
        Date modifiedAt
    ) throws ServiceException{
        for (Map.Entry<Path, Path> entry : configuration.getMapping().entrySet()) {
            if (objectId.startsWith(entry.getKey())) {
                Path imageId = new Path(entry.getValue());
                int iLimit = objectId.size() - 1;
                for (
                    int i = entry.getKey().size(); 
                    i < iLimit; 
                    i++
                ) {
                    String segment = objectId.get(i);
                    imageId.add(segment);
                    if(segment.endsWith("%")) return imageId;
                }
                String segment = objectId.get(iLimit);
                return imageId.add(
                    segment.endsWith("%") ? segment : toAudit2ImageQualifier(segment, modifiedAt)
                ); 
            }
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE,
            "Data object pattern for the given object id missing",
            new BasicException.Parameter("objectId", objectId)
        );
    }

    
    /**
     * Derive the object id from its before or after image id
     * 
     * @param configuration
     * @param imageId
     * 
     * @return the object id
     * 
     * @throws ServiceException
     */
    public static Path getObjectId(
        Configuration configuration, 
        Path imageId
    ) throws ServiceException{
        for (Map.Entry<Path, Path> entry : configuration.getMapping().entrySet()) {
            if (imageId.startsWith(entry.getValue())) {
                Path objectId = new Path(entry.getKey());
                int iLimit = imageId.size() - 1;
                for (
                    int i = entry.getKey().size(); 
                    i < iLimit; 
                    i++
                ) {
                    objectId.add(imageId.get(i));
                }
                objectId.add(
                    configuration.isAudit1Persistence() ? 
                        getAudit1ObjectQualifier(imageId.get(iLimit)) :
                        getAudit2ObjectQualifier(imageId.get(iLimit))
                );
                return objectId;
            }
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE,
            "Data object pattern for the given image id missing",
            new BasicException.Parameter("imageId", imageId)
        );
    }

}

/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Qualifiers 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.XRISegment;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.format.DateTimeFormat;
import org.w3c.spi2.Datatypes;

#if CLASSIC_CHRONO_TYPES import org.w3c.format.DateTimeFormat;#endif

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
     * Build an org::openmdx::audit2 compliant after image qualifier
     * 
     * @param qualifier
     * @param version
     * 
     * @return an audit2 compliant after image qualifier
     */
    public static String toAudit2AfterImageQualifier(
        String qualifier,
        #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif version
    ){
        return qualifier + '*' + (version == null ? "%" : DateTimeFormat.BASIC_UTC_FORMAT.format(version));
    }

    /**
     * Build an org::openmdx::audit2 compliant before image qualifier
     * 
     * @param qualifier
     * @param unitOfWorkId
     * 
     * @return an audit2 compliant before image qualifier
     */
    public static String toAudit2BeforeImageQualifier(
        String qualifier,
        String unitOfWorkId
    ){
        return qualifier + "!" + (unitOfWorkId == null ? "%" : unitOfWorkId);
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
        return segment.substring(
            0, 
            Math.max(segment.lastIndexOf('!'), segment.lastIndexOf('*'))
        );
    }
    
    /**
     * Create a before or after image identifier
     * 
     * @param configuration
     * @param objectId
     * @param unitOfWorkId
     * @return the object's before or after image id
     * 
     * @throws ServiceException
     */
    public static Path getAudit2BeforeImageId(
        Configuration configuration, 
        Path objectId, 
        String unitOfWorkId
    ) throws ServiceException{
        for (Map.Entry<Path, Path> entry : configuration.getMapping().entrySet()) {
            if (objectId.startsWith(entry.getKey())) {
            	List<String> imageId = new ArrayList<>();
            	for(XRISegment c : entry.getValue().getSegments()) {
            		imageId.add(c.toString());
            	}
                int iLimit = objectId.size() - 1;
                for (
                    int i = entry.getKey().size(); 
                    i < iLimit; 
                    i++
                ) {
                    String segment = objectId.getSegment(i).toClassicRepresentation();
                    imageId.add(segment);
                    if(segment.endsWith("%")) return new Path(imageId.toArray(new String[imageId.size()]));
                }
                String segment = objectId.getSegment(iLimit).toClassicRepresentation();
                imageId.add(
                    segment.endsWith("%") ? segment : toAudit2BeforeImageQualifier(segment, unitOfWorkId)
                ); 
                return new Path(imageId.toArray(new String[imageId.size()]));
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
     * @return the object's before or after image id
     * 
     * @throws ServiceException
     */
    public static Path getAudit2AfterImageId(
        Configuration configuration, 
        Path objectId, 
        #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif modifiedAt
    ) throws ServiceException{
        for (Map.Entry<Path, Path> entry : configuration.getMapping().entrySet()) {
            if (objectId.startsWith(entry.getKey())) {
                List<String> imageId = new ArrayList<String>(); 
                for(XRISegment c : entry.getValue().getSegments()){
                	imageId.add(c.toString());
                }
                int iLimit = objectId.size() - 1;
                for (
                    int i = entry.getKey().size(); 
                    i < iLimit; 
                    i++
                ) {
                    String segment = objectId.getSegment(i).toClassicRepresentation();
                    imageId.add(segment);
                    if(segment.endsWith("%")) return new Path(imageId.toArray(new String[imageId.size()]));
                }
                String segment = objectId.getSegment(iLimit).toClassicRepresentation();
                imageId.add(
                    segment.endsWith("%") ? segment : toAudit2AfterImageQualifier(segment, modifiedAt)
                );
                return new Path(imageId.toArray(new String[imageId.size()]));
            }
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE,
            "Data object pattern for the given object id missing",
            new BasicException.Parameter("objectId", objectId)
        );
    }
    
}

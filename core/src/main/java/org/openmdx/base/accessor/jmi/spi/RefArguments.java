/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: RefClass_1 class
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
package org.openmdx.base.accessor.jmi.spi;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefQualifier;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class RefArguments {

    final List<RefQualifier> qualifiers;
    final RefObject value;

    public RefArguments(List<RefQualifier> qualifiers, RefObject value) {
        this.qualifiers = qualifiers;
        this.value = value;
    }

    static RefArguments newInstance(
            Object[] source
    ) throws ServiceException {
        if(source == null) {
            return null;
        }
        else {
            final int length = source.length;
            int size = length;
            if(
                    (size == 1) &&
                            (source[0] instanceof RefObject_1_0)
            ){
                return new RefArguments(Collections.emptyList(), (RefObject) source[0]);
            }
            else if(
                    (size == 1) &&
                            (source[0] instanceof String || source[0] instanceof Number)
            ){
                return new RefArguments(Collections.singletonList(
                        new RefQualifier(QualifierType.REASSIGNABLE, source[0])
                ), null);
            }
            else if(
                    size == 2 &&
                            (source[0] instanceof String)
            ){
                return new RefArguments(Collections.singletonList(
                        new RefQualifier(QualifierType.REASSIGNABLE, source[0])
                ), (RefObject) source[1]);
            }
            else {
                List<RefQualifier> qualifiers = new ArrayList<>();
                int end = length % 2 == 0 ? length : length - 1;
                for(
                        int i = 0;
                        i < end;
                        i += 2
                ){
                    if (source[i] instanceof Boolean) {
                        qualifiers.add(
                                new RefQualifier((Boolean) source[i], validateSubSegment(source[i + 1]))
                        );
                    } else if (source[i] instanceof QualifierType) {
                        qualifiers.add(
                                new RefQualifier((QualifierType) source[i], validateSubSegment(source[i + 1]))
                        );
                    } else {
                         throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.NOT_IMPLEMENTED,
                                "TODO",
                                new BasicException.Parameter("source", source));
                    }
                }
                return new RefArguments(qualifiers, length % 2 == 0 ? null : (RefObject) source[length - 1]);
            }
        }
    }

    /**
     * Validate an XRI sub-segment
     *
     * @param subSegment
     *
     * @return the validated XRI sub-segment
     *
     * @exception JmiException BAD_PARAMETER if the sub-segment is {@code null}Y
     */
    private static Object validateSubSegment(
            Object subSegment
    ){
        if(subSegment == null) {
            throw new JmiServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "Null is an invalid value for an XRI sub-segment"
            );
        } else {
            return subSegment;
        }
    }

}


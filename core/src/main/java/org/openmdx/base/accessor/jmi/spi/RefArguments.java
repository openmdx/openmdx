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

    private RefArguments(List<RefQualifier> qualifiers, RefObject value) {
        this.qualifiers = qualifiers;
        this.value = value;
    }

    static RefArguments newInstance(
        Object[] source
    ) throws ServiceException {
        if(source == null || source.length == 0) {
            return null;
        } else {
            final int qualifierLimit;
            final RefObject value;
            if (source[source.length - 1] instanceof RefObject) {
                qualifierLimit = source.length - 1;
                value = (RefObject) source[qualifierLimit];
            } else {
                qualifierLimit = source.length;
                value = null;
            }
            switch (qualifierLimit) {
                case 0:
                    return new RefArguments(
                            Collections.emptyList(),
                            value
                    );
                case 1:
                    return new RefArguments(
                            Collections.singletonList(newRefQualifier(QualifierType.REASSIGNABLE, source[0])),
                            value
                    );
                default: {
                    if (qualifierLimit % 2 == 1) throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_IMPLEMENTED,
                            "Odd number of qualifier arguments",
                            new BasicException.Parameter("source", source)
                    );
                    final List<RefQualifier> qualifiers = new ArrayList<>();
                    for (int i = 0; i < qualifierLimit;) {
                        Object qualifierType = source[i++];
                        Object qualifierValue = source[i++];
                        if(qualifierType != null) {
                            qualifiers.add(newRefQualifier(qualifierType, qualifierValue));
                        }
                    }
                    return new RefArguments(qualifiers, value);
                }
            }
        }
    }

    private static RefQualifier newRefQualifier(
        Object qualifierType,
        Object qualifierValue
    ) throws ServiceException {
        if (qualifierType instanceof Boolean) {
            return new RefQualifier((Boolean) qualifierType, qualifierValue);
        } else if (qualifierType instanceof QualifierType) {
            return new RefQualifier((QualifierType) qualifierType, qualifierValue);
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The qualifier type must be specified as a Boolean or a QualifierType",
                new BasicException.Parameter("type", qualifierType),
                new BasicException.Parameter("Value", qualifierValue)
            );
        }
    }

}


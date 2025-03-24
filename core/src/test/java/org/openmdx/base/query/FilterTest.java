/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Filter Test
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.query;

import java.beans.XMLDecoder;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.spi.QueryExtension;
import org.openmdx.base.rest.cci.QueryExtensionRecord;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.log.SysLog;
#if CLASSIC_CHRONO_TYPES import org.w3c.spi.ImmutableDatatypeFactory;#endif
import org.w3c.spi2.Datatypes;
import org.w3c.time.SystemClock;

/**
 * Test Filter
 */
public class FilterTest {

    @Test
    public void testEncode() throws IOException, ServiceException {
        Filter filter = new Filter(
            Arrays.asList(
                new IsBetweenCondition(
                    Quantifier.FOR_ALL,
                    "fBetween",
                    true,
                    "Lower", "Upper"
                ),
                new IsBetweenCondition(
                    Quantifier.FOR_ALL,
                    "fOutside",
                    false,
                    "Lower", "Upper"
                ),
                new IsGreaterCondition(
                    Quantifier.FOR_ALL,
                    "fGreater",
                    true,
                    -1000
                ),
                new IsGreaterCondition(
                    Quantifier.FOR_ALL,
                    "fGreater",
                    false,
                    1000
                ),
                new IsGreaterOrEqualCondition(
                    Quantifier.FOR_ALL,
                    "fGreaterOrEqual",
                    true,
                    Double.parseDouble("-9999999999999.123")
                ),
                new IsGreaterOrEqualCondition(
                    Quantifier.FOR_ALL,
                    "fGreaterOrEqual",
                    false,
                    Double.parseDouble("99999999999.123")
                ),
                new IsInCondition(
                    Quantifier.FOR_ALL,
                    "fIsIn",
                    true,
                    Boolean.FALSE, 
                    Boolean.TRUE
                ),
                new IsInCondition(
                    Quantifier.FOR_ALL,
                    "fIsIn",
                    false,
                    Boolean.FALSE, 
                    Boolean.TRUE
                ),
                new IsLikeCondition(
                    Quantifier.FOR_ALL,
                    "fIsLike",
                    true,
                    "uri:@openmdx:a.b.c/:*/This/%", 
                    "uri:@openmdx:a.b.c/:*/That/%"
                ),
                new IsLikeCondition(
                    Quantifier.FOR_ALL,
                    "fIsUnlike",
                    false,
                    "uri:@openmdx:a.b.c/:*/This/%", 
                    "uri:@openmdx:a.b.c/:*/That/%"
                ),
                new SoundsLikeCondition(
                    Quantifier.FOR_ALL,
                    "fSoundsLike",
                    true,
                    "Mueller", 
                    "Maeder"
                ),
                new SoundsLikeCondition(
                    Quantifier.FOR_ALL,
                    "fSoundsUnlike",
                    false,
                    "Mueller", 
                    "Maeder"
                ),
                new AnyTypeCondition(
                    Quantifier.THERE_EXISTS,
                    "createdAt",
                    ConditionType.IS_NOT_IN
                )
            ),
            Arrays.asList(
                new OrderSpecifier("order_ASC", SortOrder.ASCENDING),
                new OrderSpecifier("order_DESC", SortOrder.DESCENDING)
            ),
            null // extension
        );
        QueryExtensionRecord extension = new QueryExtension();
        extension.setClause("SELECT something FROM somewhere WHERE b0 = ? AND i0 = ? AND i1 = ? AND i2 = ?");
        extension.setBooleanParam(new boolean[]{true});
        extension.setIntegerParam(new int[]{1,2,3});
        filter.getExtension().add(extension);
        //
        // CR20018833
        //
        try(
            XMLDecoder decoder = new XMLDecoder(
                new URL(null, Resources.toResourceXRI("org/openmdx/base/query/CR20018833.xml"), new org.openmdx.kernel.url.protocol.xri.Handler()).openStream()
            )
        ){
            Filter filter1 = (Filter) decoder.readObject();
            Assertions.assertEquals(filter, filter1, "Deserialization");
        }
        filter.getCondition().add(
            new IsInstanceOfCondition(
                true,
                "org:openmdx:base:ExtentCapable"
            )
        );
        filter.getCondition().add(
            new IsInstanceOfCondition(
                false,
                "org:openmdx:base:BasicObject"
            )
        );
        extension.setDateParam(
            Datatypes.create(
                Datatypes.DATE_CLASS,
                "2000-02-29"
            )
        );
        extension.setDateTimeParam(SystemClock.getInstance().now());
        extension.setDecimalParam(BigDecimal.ONE, BigDecimal.TEN);
        extension.setStringParam("String parameter 0", "String parameter 1");
        String xml = JavaBeans.toXML(filter);
        SysLog.info(xml);
        Object filter2 = JavaBeans.fromXML(xml);
        Assertions.assertEquals(filter, filter2, "Deserialization");
    }

}

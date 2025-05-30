/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Fallback Java Beans Transformer
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
package org.openmdx.base.beans;

import java.util.Optional;
import java.lang.reflect.Method;

import org.openmdx.base.exception.ExceptionListener;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.SysLog;

/**
 * Fallback Java Beans Decoder
 */
class FallbackBeanDecoder {

    private final Object xstream;
    private final Method xstreamFromXML;

    private static final FallbackBeanDecoder INSTANCE = new FallbackBeanDecoder();

    private FallbackBeanDecoder () {
        xstream = getXstream();
        xstreamFromXML = getFromXML(xstream);
        if(isAvailable()) {
            SysLog.info("XStream found. Using as fallback for XML decoding");
        }
    }

    static Optional<Object> decode(
        String encodedJavaBean
    ) {
        return INSTANCE.isAvailable() ?
            Optional.of(INSTANCE.fromXML(encodedJavaBean)) :
            Optional.empty();
    }

    private boolean isAvailable () {
        return xstreamFromXML != null;
    }

    private Object fromXML(
        CharSequence encodedJavaBean
    ) {
        try {
            return INSTANCE.xstreamFromXML.invoke(INSTANCE.xstream, encodedJavaBean);
        } catch(Exception e) {
            throw new RuntimeServiceException(e);
        }
    }

    private static Method getFromXML(final Object xstream) {
        if (xstream == null) {
            return null;
        }
        try {
            final Method fromXML = xstream.getClass().getMethod("fromXML", String.class);
            return fromXML;
        } catch (Exception e) {
            return null;
        }
    }

    private static Object getXstream() {
        try {
            return Classes.getApplicationClass("com.thoughtworks.xstream.XStream").newInstance();
        } catch (Exception e) {
            return null;
        }
    }

}

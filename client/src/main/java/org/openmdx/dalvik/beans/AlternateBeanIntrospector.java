/*
 * ====================================================================
 * Project:     openMDX/Dalvik, http://www.openmdx.org/
 * Description: Alternate Java Bean Introspector 
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
package org.openmdx.dalvik.beans;

import java.lang.reflect.Method;

import org.openmdx.dalvik.uses.java.beans.PropertyDescriptor;
import org.openmdx.kernel.loading.BeanIntrospector;

/**
 * Alternate Java Bean Introspector
 * 
 * @since openMDX 2.12
 */
public class AlternateBeanIntrospector implements BeanIntrospector {

    /* (non-Javadoc)
     * @see org.openmdx.base.text.conversion.spi.JavaBeanIntrospector#getModifier(java.lang.Class, java.lang.String)
     */
    @Override
    public Method getPropertyModifier(Class<?> javaBeanClass, String propertyName){
        Method setter;
        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(
                propertyName,
                javaBeanClass
            );
            setter = propertyDescriptor.getWriteMethod();
        } catch (Exception exception) {
            throw new RuntimeException(
                javaBeanClass.getName() + ": Unable to acquire the modifier for the property " + propertyName
            );
        }
        if(setter == null) {
            throw new RuntimeException(
                javaBeanClass.getName() + ": No modifier found for the property " + propertyName
            );
        }
        return setter;
    }

}

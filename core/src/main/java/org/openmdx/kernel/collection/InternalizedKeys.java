/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Internalized Keys 
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
package org.openmdx.kernel.collection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openmdx.kernel.exception.BasicException;

/**
 * Internalized Keys
 */
public class InternalizedKeys {

    /**
     * Constructor
     */
    public InternalizedKeys() {
        // Avoid instantiation
    }

    /**
     * Short Cache extension
     */
    private static final ConcurrentMap<Short, Short> shortCacheExtension = new ConcurrentHashMap<Short, Short>();
    private static final short JDK_SHORT_CACHE_LOWER_BOUND = -128;
    private static final short JDK_SHORT_CACHE_UPPER_BOUND = 127;

    /**
     * Integer Cache Extension
     */
    private static final ConcurrentMap<Integer, Integer> integerCacheExtension = new ConcurrentHashMap<Integer, Integer>();
    private static final int JDK_INTEGER_CACHE_LOWER_BOUND = -128;
    private static final int JDK_INTEGER_CACHE_UPPER_BOUND = 127;

    /**
     * Long Cache Extension
     */
    private static ConcurrentMap<Long, Long> longCache = new ConcurrentHashMap<Long, Long>();
    private static final long JDK_LONG_CACHE_LOWER_BOUND;
    private static final long JDK_LONG_CACHE_UPPER_BOUND;

    static {
        JDK_LONG_CACHE_LOWER_BOUND = isCached(-128L) ? -128L : Long.MAX_VALUE;
        JDK_LONG_CACHE_UPPER_BOUND = isCached(127L) ? 127L : Long.MIN_VALUE;
    }

    private static boolean isCached(long candidate) {
        return Long.valueOf(candidate) == Long.valueOf(candidate);
    }
    
    /**
     * Determines whether the given key is internalizable
     * 
     * @param key
     * 
     * @return <code>true</code> if the given key is internalizable
     */
    public static boolean isInternalizable(
        Object key
    ) {
        return key == null ||
            key instanceof String ||
            key instanceof Integer ||
            key instanceof Long ||
            key instanceof Short;
    }

    /**
     * Normalize the key to use an identity hash map.
     * <p>
     * This method supports
     * <li><code>String</code>s
     * <li><code>Integer</code>s in the range <code>-128</code> to <code>128</code>
     * </ul>
     * 
     * @param <T>
     * 
     * @param key
     *            the key to be normalized
     * 
     * @return the normalized key
     * 
     * @exception IllegalArgumentException
     *                unless the key is one of
     *                <ul>
     *                <li>a <code>java.lang.String</code> instance
     *                <li>a <code>java.lang.Instance</code> instance in the range
     *                <code>-128</code> to <code>127</code>
     *                </ul>
     * @exception NullPointerException
     *                if the key is <code>null</code>
     * @exception IllegalArgumentException
     *                if the key can't be internalized
     */
    @SuppressWarnings("unchecked")
    public static <T> T internalize(
        T key
    ) {
        if (key == null) {
            return null;
        } else if (key instanceof String) {
            return (T) ((String) key).intern();
        } else if (key instanceof Integer) {
            return (T) internalize((Integer) key);
        } else if (key instanceof Long) {
            return (T) internalize((Long) key);
        } else if (key instanceof Short) {
            return (T) internalize((Short) key);
        } else {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "Inappropriate key class",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("key", key),
                        new BasicException.Parameter("supported", String.class.getName(), Integer.class.getName()),
                        new BasicException.Parameter("actual", key.getClass().getName())
                    )
                )
            );
        }
    }

    /**
     * Internalize a Short
     * 
     * @param actual
     *            the actual value
     * 
     * @return an internalized Short
     */
    private static Short internalize(
        Short actual
    ) {
        final short value = actual.shortValue();
        return 
            value >= JDK_SHORT_CACHE_LOWER_BOUND && value <= JDK_SHORT_CACHE_UPPER_BOUND ? Short.valueOf(value) :
            shortCacheExtension.computeIfAbsent(actual, key -> key);
    }

    /**
     * Internalize an Integer
     * 
     * @param actual
     *            the actual value()
     * 
     * @return an internalized Integer
     */
    private static Integer internalize(
        Integer actual
    ) {
        final int value = actual.intValue();
        return 
            value >= JDK_INTEGER_CACHE_LOWER_BOUND && value <= JDK_INTEGER_CACHE_UPPER_BOUND ? Integer.valueOf(value) :
            integerCacheExtension.computeIfAbsent(actual, key -> key);
    }

    /**
     * Internalize a Long
     * 
     * @param actual
     *            the actual value
     * 
     * @return an internalized Long
     */
    private static Long internalize(
        Long actual
    ) {
        final long value = actual.longValue();
        return 
            value >= JDK_LONG_CACHE_LOWER_BOUND && value <= JDK_LONG_CACHE_UPPER_BOUND ? Long.valueOf(value) :
            longCache.computeIfAbsent(actual, key -> key);
    }

}

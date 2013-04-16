/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Resources 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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
package org.openmdx.kernel.loading;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

import org.openmdx.kernel.platform.Platform;

/**
 * Resources
 */
public class Resources {

    /**
     * Constructor 
     */
    private Resources() {
        // Avoid instantiation
    }

    /**
     * Retrieve the context class loader
     * 
     * @return the context class loader
     */
    private static final ClassLoader getClassLoader(
    ){
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Get a resource URL
     *
     * @param     name
     *            fully qualified name of the desired resource
     *
     * @return    a URL for reading the resource,
     *            or <code>null</code> if the resource could not be found or
     *            the caller doesn't have adequate privileges to get the
     *            resource.
     */
    public static URL getResource(
        String name
    ){
        return getClassLoader().getResource(name);
    }
    
    public static String toMetaInfPath(
        String entry
    ){
        return Platform.getProperty("meta-inf", "META-INF/") + entry;
    }
    
    public static String toMetaInfXRI(
        String entry
    ){
        return "xri://+resource/" + toMetaInfPath(entry); 
    }
        
    public static Iterable<URL> getMetaInfResources(
        String entry
    ) throws IOException {
        return getMetaInfResources(getClassLoader(), entry);
    }

    public static Iterable<URL> getMetaInfResources(
        ClassLoader classLoader,
        String entry
    ){
        return new AsIterable(classLoader, toMetaInfPath(entry));
    }

    /**
     * META-INF Resources
     */
    private static class AsIterable implements Iterable<URL> {

        /**
         * Constructor 
         *
         * @param classLoader
         * @param name
         * 
         * @throws IOException
         */
        AsIterable(
            ClassLoader classLoader,
            String name
        ){
            this.classLoader = classLoader;
            this.name = name; // toMetaInfPath(entry);            
        }

        final ClassLoader classLoader;
        final String name;
        
        /* (non-Javadoc)
         * @see java.lang.Iterable#iterator()
         */
        @Override
        public Iterator<URL> iterator() {
            try {
                return new AsIterator(classLoader.getResources(name));
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
        
    }

    /**
     * META-INF Resource Iterator
     */
    static class AsIterator implements Iterator<URL> {

         /**
          * Constructor 
          *
          * @param delegate
          */
        AsIterator(Enumeration<URL> delegate) {
            this.delegate = delegate;
        }

        /**
         * The Enumeration wrapped into an Iterator
         */
        private final Enumeration<URL> delegate;
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext(
        ){
            return this.delegate.hasMoreElements();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public URL next() {
            return this.delegate.nextElement();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }

}

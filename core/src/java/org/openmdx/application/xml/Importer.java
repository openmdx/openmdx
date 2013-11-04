/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: XML Importer
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.application.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jdo.PersistenceManager;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.dataprovider.cci.DataproviderRequestProcessor;
import org.openmdx.application.xml.jmi.BasicImportPlugIn;
import org.openmdx.application.xml.jmi.PersistenceManagerTarget;
import org.openmdx.application.xml.jmi.StateImportPlugIn;
import org.openmdx.application.xml.spi.DataproviderTarget;
import org.openmdx.application.xml.spi.ImportHelper;
import org.openmdx.application.xml.spi.ImportMode;
import org.openmdx.application.xml.spi.ImportTarget;
import org.openmdx.application.xml.spi.MapTarget;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.xml.AdaptiveInputStreamReader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

/**
 * XML Importer
 */
public class Importer {

    /**
     * Constructor 
     */
	protected Importer(
	) {		
	    // Avoid instantiation
	}
	
	private static final ImportHelper importHelper = new ImportHelper();
	
    /**
     * Import objects 
     * @param target the object sink
     * @param source the XML source
     * @param errorHandler <code>null</code> leads to standard error handling
     * 
     * @throws ServiceException  
     */
    public static void importObjects (
        ImportTarget target,
        Iterable<InputSource> source,
        ErrorHandler errorHandler
    ) throws ServiceException {
        importObjects(
            target, 
            source, 
            errorHandler,
            ImportMode.SET
        );
    }

    /**
     * Import objects 
     * 
     * @param target the object sink
     * @param source the XML source
     * 
     * @throws ServiceException  
     */
    public static void importObjects (
        ImportTarget target,
        Iterable<InputSource> source
    ) throws ServiceException {
        importObjects(
            target, 
            source, 
            null, // errorHandler
            ImportMode.SET
        );
    }

    /**
     * Import objects 
     * 
     * @param target the object sink
     * @param source the XML source
     * 
     * @throws ServiceException  
     */
    public static void importObjects (
        ImportTarget target,
        Iterable<InputSource> source,
        ErrorHandler errorHandler,
        ImportMode defaultImportMode
    ) throws ServiceException {
        importHelper.importObjects(
            target, 
            source, 
            null, // errorHandler
            defaultImportMode
        );
    }

    /**
     * Input source factory method
     * 
     * @param source the input source specification
     * 
     * @return the corresponding <code>InputSource Enumeration</code>
     */
    public static Iterable<InputSource> asSource(
        String source
    ) {
        return ImportHelper.asSource(
            new InputSource(source)
        );
    }

    /**
     * Input source factory method
     * 
     * @param source the input source specification
     * 
     * @return the corresponding <code>InputSource Enumeration</code>
     */
    public static Iterable<InputSource> asSource(
        URL source
    ) {
        return ImportHelper.asSource(
            new InputSource(source.toString())
        );
    }

    /**
     * Input source factory method
     * 
     * @param source the input source specification
     * 
     * @return the corresponding <code>InputSource Enumeration</code>
     */
    public static Iterable<InputSource> asSource(
        File source
    ) {
        return ImportHelper.asSource(
            new InputSource(source.getAbsolutePath())
        );
    }

    /**
     * Input source factory method
     * 
     * @param source the input source specification
     * 
     * @return the corresponding <code>InputSource Enumeration</code>
     */
    public static Iterable<InputSource> asSource(
        InputStream source
    ) {
        return ImportHelper.asSource(
            new InputSource(source)
        );
    }
    
    /**
     * Input source factory method
     * 
     * @param source the input archive specification
     * @param pattern the archive entry specification
     * 
     * @return the corresponding <code>InputSource Enumeration</code>
     */
    public static Iterable<InputSource> asSource(
        final URL source,
        final String pattern
    ) {
        return new Iterable<InputSource>(){

            /* (non-Javadoc)
             * @see java.lang.Iterable#iterator()
             */
            public Iterator<InputSource> iterator() {
                try {
                    return new ArchiveSource(
                        source.openStream(),
                        pattern
                    );
                } catch (IOException exception) {
                    throw new RuntimeServiceException(exception);
                }
            }
            
        };
    }

    /**
     * Input source factory method
     * 
     * @param source the input archive specification
     * @param pattern the archive entry specification
     * 
     * @return the corresponding <code>InputSource Enumeration</code>
     */
    public static Iterable<InputSource> asSource(
        final File source,
        final String pattern
    ) throws ServiceException {
        return new Iterable<InputSource>(){

            /* (non-Javadoc)
             * @see java.lang.Iterable#iterator()
             */
            public Iterator<InputSource> iterator() {
                try {
                    return new ArchiveSource(
                        new FileInputStream(source),
                        pattern
                    );
                } catch (FileNotFoundException exception) {
                    throw new RuntimeServiceException(exception);
                }
            }
            
        };
    }

    /**
     * Input source factory method
     * 
     * @param source the input archive specification
     * @param pattern the archive entry specification
     * 
     * @return the corresponding <code>InputSource Enumeration</code>
     */
    public static Iterable<InputSource> asSource(
        final InputStream source,
        final String pattern
    ) {
        return new Iterable<InputSource>(){

            /**
             * Access count
             */
            private int credit = 1;
            
            /* (non-Javadoc)
             * @see java.lang.Iterable#iterator()
             */
            public Iterator<InputSource> iterator(
            ) {
                if(this.credit-- <= 0) throw new IllegalStateException(
                    "A stream source is expcected to be accessed once only"
                );
                return new ArchiveSource(
                    source, 
                    pattern
                );
            }
            
        };
    }
    
    /**
     * <code>ImportTarget</code> factory method
     * 
     * @param target
     * 
     * @return the corresponding <code>ImportTarget</code>
     */
    public static ImportTarget asTarget(
        Map<Path,MappedRecord> target
    ){
        return new MapTarget(target);
    }

    /**
     * <code>ImportTarget</code> factory method
     * 
     * @param target
     * 
     * @return the corresponding <code>ImportTarget</code>
     */
    public static ImportTarget asTarget(
        DataproviderRequestProcessor target
    ){
        return new DataproviderTarget(target);
    }
    
    /**
     * <code>ImportTarget</code> factory method
     * 
     * @param target
     * 
     * @return the corresponding <code>ImportTarget</code>
     */
    public static ImportTarget asTarget(
        PersistenceManager target
    ){
        return new PersistenceManagerTarget(
            target,
            new StateImportPlugIn (
                new BasicImportPlugIn()
            )
        );
    }

    /**
     * Create a regular expression from the eclipse like patterns.
     * 
     * @param pattern an eclipse like pattern with "**" and "*" as wildcard sequences
     * 
     * @return the corresponding regular expression pattern
     */
    protected static Pattern newPattern(
        String pattern
    ){
        return pattern == null ? null : Pattern.compile(
            "^" + pattern.replaceAll("\\*\\*", ".\t").replaceAll("\\*", "[^/]*").replace('\t', '*') + "$"
        );
    }
    
    
    //------------------------------------------------------------------------
    // Class ArchiveSource
    //------------------------------------------------------------------------
    
    /**
     * Allows to iterate over the archive's entries
     */
    protected static class ArchiveSource implements Iterator<InputSource> {

        /**
         * Constructor 
         *
         * @param archive
         * @param pattern
         */
        protected ArchiveSource(
            InputStream archive,
            String pattern
        ) {
            this.archive = new ZipInputStream(archive);
            this.pattern = newPattern(pattern);
        }

        /**
         * 
         */
        private ZipInputStream archive;
        
        /**
         * 
         */
        private final Pattern pattern;
        
        /**
         * The next entry to be returned
         */
        private ZipEntry prefetched = null;
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            while(this.prefetched == null && this.archive != null) try {
                ZipEntry candidate = this.archive.getNextEntry();
                if(candidate == null) {
                    this.archive = null;
                } else if (this.pattern == null || this.pattern.matcher(candidate.getName()).matches()){
                    this.prefetched = candidate;
                }
            } catch (IOException exception) {
                this.archive = null;
                throw new RuntimeServiceException(exception);
            }
            return this.prefetched != null;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public InputSource next() {
            if(!hasNext()) {
                throw new NoSuchElementException("End of archive reached");
            }
            try {
                return new InputSource(
                    new AdaptiveInputStreamReader(
                        this.archive,
                        null, // encoding 
                        true, // byteOrderMarkAware 
                        true, // xmlDeclarationAware 
                        false // propagateClose
                    )
                );
            } catch (IOException exception) {
                throw new RuntimeServiceException(exception);
            } finally {
                this.prefetched = null;
            }
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove(
        ) {
            throw new UnsupportedOperationException();
        }
        
    }
        
}

/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: UML Documentation Mapper
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
package org.openmdx.application.mof.mapping.pimdoc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.zip.ZipOutputStream;

import org.openmdx.application.mof.mapping.spi.AbstractMapper_1;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.exception.BasicException;

/**
 * UML Model documentation in HTML format
 */
public class PIMDocMapper extends AbstractMapper_1 {

    /**
     * Public Constructor 
     * 
     * Uses default configuration values
     * 
     * @param markdown {@code true} if annotations use markdown
     * 
     * @throws ServiceException 
     */
    public PIMDocMapper(
    	boolean markdown
    ) throws ServiceException{
        this(markdown, new PIMDocConfiguration(null));   
    }

    /**
     * Public Constructor
     * 
     * @param markdown {@code true} if annotations use markdown
     * @param configurationURL the PIMDoc Configuration Properties URL
     * 
     * @throws ServiceException
     */
    public PIMDocMapper(boolean markdown, String configurationURL) throws ServiceException {
    	this(markdown, new PIMDocConfiguration(configurationURL));
	}

    /**
     * Internal Constructor 
     * 
     * @param markdown {@code true} if annotations use markdown
     * @param configuration the PIMDoc Configuration
     * 
     */
    private PIMDocMapper(
    	boolean markdown,
    	PIMDocConfiguration configuration
    ){
        super(markdown, PACKAGE_SUFFIX);    
        this.configuration = configuration;
    }
    
    private final PIMDocConfiguration configuration;

	/**
     * The UML documentation directories doen't need a package suffix
     */
    private static final String PACKAGE_SUFFIX = null;
    
    @Override
	public void externalize(
		final String qualifiedPackageName, 
		final Model_1_0 model,
		final ZipOutputStream zip
	) throws ServiceException {
        super.model = model;
        try (
    		PIMDocExternalizer externalizer = new PIMDocExternalizer(
    			new BiConsumer<String, ByteArrayOutputStream>() {
					
					@Override
					public void accept(String entryName, ByteArrayOutputStream content) {
						try {
							addToZip(zip, entryName, content);
						} catch (IOException ioException) {
							throw new RuntimeServiceException(
								ioException,
								BasicException.Code.DEFAULT_DOMAIN,
								BasicException.Code.TRANSFORMATION_FAILURE,
								"Unable to add content to the archive",
								new BasicException.Parameter("entry-name", entryName)
							);
						}
					}
					
				},
    			markdown, configuration
    		)
        ){
        	externalizer.accept(getMatchingPackages(qualifiedPackageName));
	    } catch(Exception ex) {
	        throw new ServiceException(ex).log();
	    }    
    }

}

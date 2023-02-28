/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: The PIM Documentation Mapper
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

import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.openmdx.application.mof.mapping.cci.Mapper_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.ZipSink;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.exception.BasicException;

/**
 * UML Model documentation in HTML format
 */
public class PIMDocMapper implements Mapper_1_0 {

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
     * @param configurationDirectory the PIMDoc Configuration Directory
     * 
     * @throws ServiceException
     */
    public PIMDocMapper(boolean markdown, String configurationDirectory) throws ServiceException {
    	this(markdown, new PIMDocConfiguration(configurationDirectory));
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
    	this.markdown = markdown;
        this.configuration = configuration;
    }
    
    private final boolean markdown;
    private final PIMDocConfiguration configuration;
    
    /**
     * We may support both representations as they are not propagated for further processing anyway.
     */
    private static final List<String> EXPORT_ALL = Arrays.asList("%", "%:%"); 

    @Override
	public void externalize(
		final String qualifiedPackageName, 
		final Model_1_0 model,
		final ZipOutputStream zip
	) throws ServiceException {
    	validateArguments(qualifiedPackageName);
    	final PIMDocExternalizer pimDocExternalizer = new PIMDocExternalizer(model, new ZipSink(zip), markdown, configuration);
    	pimDocExternalizer.externalize();
    }

	private void validateArguments(final String qualifiedPackageName) throws ServiceException {
		if(!EXPORT_ALL.contains(qualifiedPackageName)) {
    		throw new ServiceException(
    			BasicException.Code.DEFAULT_DOMAIN,
    			BasicException.Code.BAD_PARAMETER,
    			"The PIM Documentation Mapper requires the whole model to be taken into account, "
    			+ "provide a configuration with a 'table-of-content' property to customize the table of content",
    			new BasicException.Parameter("expected", EXPORT_ALL),
    			new BasicException.Parameter("actual", qualifiedPackageName),
    			new BasicException.Parameter("table-of-content", configuration.getTableOfContentEntries())
    		);
    	}
	}

}

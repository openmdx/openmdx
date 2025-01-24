/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: MapperFactory_1
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.application.mof.mapping.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openmdx.application.mof.externalizer.spi.AnnotationFlavour;
import org.openmdx.application.mof.externalizer.spi.ChronoFlavour;
import org.openmdx.application.mof.externalizer.spi.ExternalizationConfiguration;
import org.openmdx.application.mof.externalizer.spi.ExternalizationScope;
import org.openmdx.application.mof.externalizer.spi.JakartaFlavour;
import org.openmdx.application.mof.mapping.cci.Mapper_1_0;
import org.openmdx.application.mof.mapping.java.JavaExportFormat;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.loading.Classes;

/**
 * MapperFactory_1
 */
public class MapperFactory_1 implements Iterable<Mapper_1_0> {

	/**
	 * Constructor
	 * 
	 * @param extendedFormats flags and parameterized formats
	 */
	public MapperFactory_1(
		List<String> extendedFormats
	) {
		List<String> parameterizedFormats = new ArrayList<>(extendedFormats);
        this.configuration = new ExternalizationConfiguration(
			AnnotationFlavour.fromExtendedFormats(parameterizedFormats),
			JakartaFlavour.fromExtendedFormats(parameterizedFormats),
			ChronoFlavour.fromExtendedFormats(parameterizedFormats),
			ExternalizationScope.fromExtendedFormats(parameterizedFormats)
		);
		this.parameterizedFormats = Collections.unmodifiableList(parameterizedFormats);
	}

	/**
	 * Provides a set of configuration elements
	 */
	private final ExternalizationConfiguration configuration;

	/**
	 * The parameterized formats (without flags)
	 */
	private final List<String> parameterizedFormats;
	
    /**
     * Create a mapper instance
     * 
     * @param parametrizedFormat the format, either predefined or as class name
     * 
     * @return the mapper instance implied by {@code parametrizedFormat}
     */
    private Mapper_1_0 newMapper(
        String parametrizedFormat
    ) throws ServiceException {
    	final MappingFormatParser parser = new MappingFormatParser(parametrizedFormat);
		final String id = parser.getId();
		if(JavaExportFormat.supports(id)) {
			return JavaExportFormat.fromId(id).createMapper(configuration);
		} else if (ModelExportFormat.supports(id)) {
			return ModelExportFormat.fromId(id).createMapper(configuration, parser.getArguments());
		} else {
			return createMapperPlugIn(id, parser.getAmendedArguments(configuration));
		}
    }

	private Mapper_1_0 createMapperPlugIn(
		String qualifiedClassName,
		Object[] arguments
	) throws ServiceException {
		try {
			return Classes.newApplicationInstance(Mapper_1_0.class, qualifiedClassName, arguments);
		} catch (Exception exception) {
			throw new ServiceException(exception);
		}
	}

	@Override
	public Iterator<Mapper_1_0> iterator() {
		
		return new Iterator<Mapper_1_0>() {
			
			final Iterator<String> delegate = parameterizedFormats.iterator();

			@Override
			public boolean hasNext() {
				return delegate.hasNext();
			}

			@Override
			public Mapper_1_0 next() {
				try {
					return newMapper(delegate.next());
				} catch (ServiceException e) {
					throw new RuntimeServiceException(e);
				}
			}
			
		};
		
	}
    
}

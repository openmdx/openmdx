/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Package Mapper 
 * Owner: the original authors. 
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
package org.openmdx.application.mof.mapping.pimdoc.text;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.openmdx.application.mof.externalizer.spi.AnnotationFlavour;
import org.openmdx.application.mof.mapping.pimdoc.PIMDocConfiguration;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.spi.PIMDocFileType;

/**
 * Package Mapper 
 */
public class PackageMapper extends ElementMapper {

	/**
     * Constructor 
     */
    public PackageMapper(
    	Sink sink, 
        ModelElement_1_0 packageToBeExported,
        AnnotationFlavour annotationFlavour, 
        PIMDocConfiguration configuration
    ){
		super("Package", sink, packageToBeExported, annotationFlavour, configuration);
		this.classesMapper = new ClassesMapper(pw, this.element, annotationRenderer);
		this.dataTypesMapper = new DataTypesMapper(pw, this.element, annotationRenderer);
		this.albumMapper = new AlbumMapper(pw, this.element, annotationRenderer, getAlbum());
    }    

	private final CompartmentMapper classesMapper;
	private final CompartmentMapper dataTypesMapper;
	private final CompartmentMapper albumMapper;
    
	@Override
	protected void columnBody() {
		printLine("\t<div class=\"column-body\">");
		mapAnnotation("\t\t", element);
		classesMapper.compartment(true);
		dataTypesMapper.compartment(true);
		albumMapper.compartment(true);
		printLine("\t</div>");
	}
	
	private SortedMap<String,String> getAlbum()  {
		final URI directory = getPackage();
		final SortedMap<String,String> album = new TreeMap<>();
		for(Map.Entry<URI,String> e : sink.getTableOfContent().entrySet()) {
			final String p = directory.relativize(e.getKey()).getPath();
			if(PIMDocFileType.GRAPHVIZ_SOURCE.test(p) && p.indexOf('/') < 0) {
				album.put(p, e.getValue());
			}
		}
		return album;
	}

	private URI getPackage() {
		try {
			return new URI('/' + element.getModel().toJavaPackageName(element, null).replace('.', '/') + '/');
		} catch (URISyntaxException | ServiceException exception) {
			throw new RuntimeServiceException(exception);
		}
	}
	
}

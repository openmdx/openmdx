/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Class Mapper 
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
package org.openmdx.application.mof.mapping.pimdoc;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;

/**
 * Class Mapper 
 */
public class ClassMapper extends ElementMapper {

	/**
     * Constructor 
     */
    public ClassMapper(
        Sink sink, 
        ModelElement_1_0 classToBeExported,
        boolean markdown, 
        PIMDocConfiguration configuration
    ){
		super("Class", sink, classToBeExported, markdown, configuration);
		this.classPropertiesMapper = new ClassPropertiesMapper(pw, element, this::getHref);		
		this.classHierarchyMapper = new ClassHierarchyMapper(pw, element, this::getHref);		
		this.structuralFeaturesMapper = new StructuralFeaturesMapper(pw, element, this::getHref);		
		this.behaviouralFeatureyMapper = new BehaviouralFeaturesMapper(pw, element, this::getHref);		
    }    
    
    private final CompartmentMapper classPropertiesMapper;
    private final CompartmentMapper classHierarchyMapper;
    private final CompartmentMapper structuralFeaturesMapper;
    private final CompartmentMapper behaviouralFeatureyMapper;

	@Override
	protected void columnBody() {
		printLine("\t<div class=\"column-body\">");
		annotation(element);
		classPropertiesMapper.compartment(false);
		classHierarchyMapper.compartment(false);
		structuralFeaturesMapper.compartment(true);
		behaviouralFeatureyMapper.compartment(true);
		printLine("\t</div>");
	}
	
	@Override
	protected String getTitle() {
		return getTitlePrefix() + super.getTitle();
	}

	private String getTitlePrefix(){
		try {
			return element.isAbstract() ? "Abstract " : "";
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}
	
}

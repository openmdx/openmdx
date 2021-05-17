/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: JPA3 MetaDataMapper 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */

package org.openmdx.application.mof.mapping.java;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;

import org.openmdx.application.mof.mapping.cci.AttributeDef;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.cci.ReferenceDef;
import org.openmdx.application.mof.mapping.cci.StructuralFeatureDef;
import org.openmdx.application.mof.mapping.java.metadata.FieldMetaData;
import org.openmdx.application.mof.mapping.java.metadata.FieldPersistenceModifier;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.w3c.format.DateTimeFormat;

/**
 * openMDX Meta Data Mapper
 */
public class NativeMetaDataMapper extends AbstractMetaDataMapper {

	/**
	 * Constructor
	 */
	public NativeMetaDataMapper(
        ModelElement_1_0 classDef, 
        Writer writer,
		Model_1_0 model, 
        Format format, 
        String packageSuffix,
		String sliceClassName, 
        MetaData_1_0 metaData, 
        PrimitiveTypeMapper primitiveTypeMapper, 
        ObjectRepositoryMetadataPlugin plugin
    ) throws ServiceException {
        super(
            classDef,
            writer, 
            model,
            format, 
            packageSuffix, 
    		sliceClassName, 
            metaData, 
            primitiveTypeMapper, 
            plugin
        );
	}

	/**
	 * Return the persistence modifier if explicitly defined
	 * 
	 * @param featureDef
	 * 
	 * @return the persistence modifier if it is explicitly defined
	 * 
	 * @throws ServiceException
	 */
    protected Boolean isPersistent(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        FieldMetaData fieldMetaData = getFieldMetaData(featureDef.getQualifiedName());
        if(fieldMetaData != null) {
        	FieldPersistenceModifier persistenceModifier = fieldMetaData.getPersistenceModifier();
        	if(persistenceModifier != null){
        		return Boolean.valueOf(
        			persistenceModifier == FieldPersistenceModifier.PERSISTENT || persistenceModifier == FieldPersistenceModifier.VERSION
        		);
        	}
        }
        return null;
    }

	/* (non-Javadoc)
	 * @see org.openmdx.application.mof.mapping.java.AbstractMetaDataMapper#mapReference(org.openmdx.application.mof.mapping.cci.ReferenceDef)
	 */
	@Override
	public void mapReference(ReferenceDef featureDef) throws ServiceException {
		// never invoked
	}

	/* (non-Javadoc)
	 * @see org.openmdx.application.mof.mapping.java.AbstractMetaDataMapper#mapSize(org.openmdx.application.mof.mapping.cci.StructuralFeatureDef)
	 */
	@Override
	public void mapSize(
		StructuralFeatureDef featureDef
	) throws ServiceException {
		// never invoked
	}

	/* (non-Javadoc)
	 * @see org.openmdx.application.mof.mapping.java.AbstractMetaDataMapper#mapEmbedded(org.openmdx.application.mof.mapping.cci.StructuralFeatureDef, org.openmdx.application.mof.mapping.java.metadata.FieldMetaData)
	 */
	@Override
	public void mapEmbedded(
		StructuralFeatureDef featureDef,
		FieldMetaData fieldMetaData
	) throws ServiceException {
		// never invoked
	}

	/* (non-Javadoc)
	 * @see org.openmdx.application.mof.mapping.java.AbstractMetaDataMapper#mapAttribute(org.openmdx.application.mof.mapping.cci.AttributeDef)
	 */
	@Override
	public void mapAttribute(AttributeDef attributeDef) throws ServiceException {
		if(this.isDeclaringClass(attributeDef)) {
			Boolean persistent = isPersistent(attributeDef);
			if(persistent != null) {
				this.pw.print(attributeDef.getQualifiedName().replaceAll(":", "\\\\:"));
				this.pw.print(" = ");
				this.pw.println(persistent.booleanValue() ? "PERSISTENT" : "TRANSIENT");
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.openmdx.application.mof.mapping.java.AbstractMetaDataMapper#mapEnd(org.openmdx.application.mof.mapping.java.AbstractMetaDataMapper)
	 */
	@Override
	public void mapEnd(
		AbstractMetaDataMapper sliceClass
	) throws ServiceException {
		// never invoked
	}

	/* (non-Javadoc)
	 * @see org.openmdx.application.mof.mapping.java.AbstractMetaDataMapper#mapBegin(boolean)
	 */
	@Override
	public void mapBegin(boolean isSliceHolder) throws ServiceException {
		// never invoked
	}

	/* (non-Javadoc)
	 * @see org.openmdx.application.mof.mapping.java.AbstractMetaDataMapper#setProcess(boolean)
	 */
	@Override
	public void setProcess(boolean process) {
		// never invoked
	}

	/**
	 * Print the XML file header
	 */
	public static void fileHeader(
	    PrintWriter pw
	) {
		pw.println("# Name: $Id: NativeMetaDataMapper.java,v 1.3 2011/09/07 11:54:31 hburger Exp $");
		pw.println("# Generated by: openMDX Meta Data Mapper");
		pw.println("# Date: " + DateTimeFormat.EXTENDED_UTC_FORMAT.format(new Date()));
		pw.println("#");
		pw.println("# GENERATED - DO NOT CHANGE MANUALLY");
		pw.println("#");
	}
    
    /**
     * Print the XML file footer
     */
    public static void fileFooter(
        PrintWriter pw
    ) {
		pw.println("#");
		pw.println("# END OF GENERATED FILE");
    }
    
}

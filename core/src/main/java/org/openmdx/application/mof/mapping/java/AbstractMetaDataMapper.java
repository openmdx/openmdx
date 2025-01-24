/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract Meta Data Mapper 
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
package org.openmdx.application.mof.mapping.java;

import java.io.Writer;

import org.openmdx.application.mof.externalizer.spi.ExternalizationConfiguration;
import org.openmdx.application.mof.mapping.cci.AttributeDef;
import org.openmdx.application.mof.mapping.cci.ClassDef;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.cci.ReferenceDef;
import org.openmdx.application.mof.mapping.cci.StructuralFeatureDef;
import org.openmdx.application.mof.mapping.java.metadata.ClassMetaData;
import org.openmdx.application.mof.mapping.java.metadata.ClassPersistenceModifier;
import org.openmdx.application.mof.mapping.java.metadata.FieldMetaData;
import org.openmdx.application.mof.mapping.spi.MapperUtils;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

/**
 * Abstract Meta Data Mapper
 */
public abstract class AbstractMetaDataMapper extends AbstractClassMapper {

	protected AbstractMetaDataMapper(
        ModelElement_1_0 classDef,
        Writer writer,
        Model_1_0 model,
        ExternalizationConfiguration configuration,
        JavaExportFormat format,
        String sliceClassName,
        MetaData_1_0 metaData,
        PrimitiveTypeMapper primitiveTypeMapper,
        ObjectRepositoryMetadataPlugin plugin
    ) throws ServiceException {
        super(
            classDef,
            writer, 
            model,
            configuration,
            format,
            metaData,
            primitiveTypeMapper
        );
        this.writer = writer;
		this.sliceClassName = sliceClassName;
        this.plugin = plugin;
        this.packageName = this.getNamespace(
            MapperUtils.getNameComponents(
                MapperUtils.getPackageName(
                    this.classDef.getQualifiedName()
                )
            )
        );
	}

	protected final Writer writer;
	protected final String sliceClassName;
    protected final String packageName;
    protected final ObjectRepositoryMetadataPlugin plugin;
	
    /**
     * Test whether a given class is persistence aware
     *
     * @return {@code true} if the given class is persistence aware
     */
    protected boolean isPersistenceAware(
        ClassDef classDef
    ) {
        ClassMetaData classMetaData = (ClassMetaData) classDef.getClassMetaData();
        return 
            classMetaData != null && 
            classMetaData.getPersistenceModifier() == ClassPersistenceModifier.PERSISTENCE_AWARE;
    }

    /**
     * Tells whether the method is invoked in its own class specific meta data mapper
     *
     * @return {@code true} if the method is invoked in its own class specific meta data mapper
     */
    protected boolean isDeclaringClass(
        AttributeDef attributeDef
    ){
        return attributeDef.getQualifiedName().equals(
            classDef.getQualifiedName() + ":" + attributeDef.getName()
        );
    }
    
    public abstract void mapReference(
        ReferenceDef featureDef
    ) throws ServiceException;

	public abstract void mapSize(
	    StructuralFeatureDef featureDef
	) throws ServiceException;

    public abstract void mapEmbedded(
        StructuralFeatureDef featureDef,
        FieldMetaData fieldMetaData
    ) throws ServiceException;

    public abstract void mapAttribute(
        AttributeDef featureDef
    ) throws ServiceException;

	public abstract void mapEnd(
        AbstractMetaDataMapper sliceClass
    ) throws ServiceException;

	public abstract void mapBegin(
	    boolean isSliceHolder
	) throws ServiceException;

    public abstract void setProcess(
        boolean process
    );

}

/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Model Externalizer Implementation
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
package org.openmdx.application.mof.externalizer.spi;

import java.util.List;
#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.IndexedRecord;
import jakarta.resource.cci.MappedRecord;
#endif
import org.openmdx.application.mof.externalizer.cci.ModelExternalizer_1_0;
import org.openmdx.application.mof.externalizer.cci.ModelImporter_1_0;
import org.openmdx.application.mof.repository.accessor.ModelProvider_2;
import org.openmdx.base.dataprovider.cci.Channel;
import org.openmdx.base.dataprovider.cci.DataproviderRequestProcessor;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;

/**
 * Dataprovider 2 based model externalizer
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ModelExternalizer_1 implements ModelExternalizer_1_0 {

	/**
	 * Creates an in-process org::omg::model1 compliant dataprovider with
	 * in-memory persistence. With the method importModel() models can be
	 * imported to this dataprovider and with operations such as
	 * externalizeAsJar() operations can be performed on the populated
	 * dataprovider.
	 * 
	 * @param openmdxjdoDir
	 *            base URL for openmdxjdo files
	 * @param annotationFlavour
	 *            tells whetherß annotations use markdown
	 */
	public ModelExternalizer_1(
		String openmdxjdoDir, 
		AnnotationFlavour annotationFlavour,
		JakartaFlavour jakartaFlavour,
		ChronoFlavour chronoFlavour
	){
		try {
			this.dataprovider = ModelProvider_2.newModelExternalizationDataprovider(openmdxjdoDir);
			this.annotationFlavour = annotationFlavour;
			this.jakartaFlavour = jakartaFlavour;
			this.chronoFlavour = chronoFlavour;
		} catch (RuntimeException e) {
			throw Throwables.log(e);
		}
	}

	protected final Port<RestConnection> dataprovider;

    private static final String PROVIDER_NAME = "Mof";
    
    /**
     * Tells whether annotations use markdown or not
     */
    private final AnnotationFlavour annotationFlavour;

    /**
     * Tells which Jakarta flavour to use
     */
    private final JakartaFlavour jakartaFlavour;

    /**
     * Tells which JMI flavour to use
     */
    private final ChronoFlavour chronoFlavour;
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmdx.application.mof.externalizer.spi.ModelExternalizer#importModel
	 * (org.openmdx.application.mof.externalizer.cci.ModelImporter_1_0)
	 */
	@Override
	public void importModel(
		ModelImporter_1_0 modelImporter
	) throws ResourceException {
		SysLog.trace("starting import");
   	    modelImporter.process(newChannel());
		SysLog.trace("import finished");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmdx.application.mof.externalizer.spi.ModelExternalizer#
	 * externalizePackageAsJar(java.lang.String, java.util.List)
	 */
	@Override
	public byte[] externalizePackageAsJar(
		String modelName, 
		List formats
	) throws ResourceException {

		final Channel channnel = newChannel();

		SysLog.trace("> Externalize package " + modelName);

		final Path modelPackagePath = toModelId(modelName);

		SysLog.trace("> Externalize package");
		//
		// Call externalizePackage
		//
		MessageRecord request = Records.getRecordFactory().createMappedRecord(MessageRecord.class);
		request.setResourceIdentifier(modelPackagePath.getChild("externalizePackage"));
		MappedRecord params = Records.getRecordFactory().createMappedRecord("org:omg:model1:PackageExternalizeParams");
		request.setBody(params);
		IndexedRecord values = Records.getRecordFactory().createIndexedRecord(Multiplicity.LIST.code());
		params.put("format", values);
		annotationFlavour.applyExtendedFormat(values);
		jakartaFlavour.applyExtendedFormat(values);
		chronoFlavour.applyExtendedFormat(values);
		values.addAll(formats);
		MessageRecord result = channnel.addOperationRequest(request);
		return (byte[]) result.getBody().get("packageAsJar");
	}

	/**
	 * !!! segmentName is of the form x0:x1:x2:...
	 */
	private Path toModelId(String modelName) {
		return new Path(
			new String[] {
				"org:omg:model1",
				"provider",
				PROVIDER_NAME,
				"segment",
				modelName,
				"element",
				modelName + ":" + modelName.substring(modelName.lastIndexOf(':') + 1) 
			}
		);
	}

	private Channel newChannel() throws ResourceException {
		return new DataproviderRequestProcessor(null, this.dataprovider);
	}

}

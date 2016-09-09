/*
 * ====================================================================
 * Project:     openMDX/Test Core, http://www.openmdx.org/
 * Description: Test Model Repository 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2016, OMEX AG, Switzerland
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
package test.openmdx.app1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.naming.Context;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.openmdx.application.mof.repository.accessor.ModelBuilder_1;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Dumper;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.junit.rules.EntityManagerFactoryRule;
import org.openmdx.junit.rules.EntityManagerRule;
import org.openmdx.junit.rules.SystemPropertyRule;
import org.openmdx.kernel.log.SysLog;

/**
 * Test Model Repository
 */
public class TestModelRepository {
    
    @ClassRule
    public static final SystemPropertyRule systemPropertyRule = new SystemPropertyRule() //
    	.setProperty("java.protocol.handler.pkgs","org.openmdx.kernel.url.protocol") // 
    	.setProperty(Context.INITIAL_CONTEXT_FACTORY,"org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactory");
    
    @ClassRule
	public static EntityManagerFactoryRule entityManagerFactoryRule = new EntityManagerFactoryRule() //
		.setName("test-Main-EntityManagerFactory");

    @Rule
    public EntityManagerRule entityManagerRule = new EntityManagerRule(entityManagerFactoryRule);
    
    /**
     * CR20020284
     */
    @Test
    public void loadAndSaveModelAsWBXML() throws ServiceException, IOException{
        Model_1_0 model = Model_1Factory.getModel();
        Map<Path, ModelElement_1_0> content1 = getContent(model);
        ByteArrayOutputStream outputStream1 =  new ByteArrayOutputStream();
        Model_1Dumper.save(outputStream1, "application/vnd.openmdx-xmi.wbxml", model);
        model = loadForeignModel("xri://+zip*(xri://+resource/test/opencrx/openmdxmof.wbxml.zip)/openmdxmof.wbxml");
        model = loadForeignModel("xri://+resource/test/opencrx/openmdxmof.wbxml");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream1.toByteArray());
        long startLoading = System.currentTimeMillis();
        model = new ModelBuilder_1(false, inputStream).build();
        long restoreTime = System.currentTimeMillis() - startLoading;
        SysLog.performance("Restoring app1 model", restoreTime);
        Map<Path, ModelElement_1_0> content3 = getContent(model);
        ByteArrayOutputStream outputStream3 =  new ByteArrayOutputStream();
        Model_1Dumper.save(outputStream3, "application/vnd.openmdx-xmi.wbxml", model);
        assertRepositoryEquality(content1, content3);
    }
    
    private Map<Path,ModelElement_1_0> getContent(
        Model_1_0 model
    ) throws ServiceException{
        Map<Path,ModelElement_1_0> content = new TreeMap<Path, ModelElement_1_0>();
        for(ModelElement_1_0 e : model.getContent()) {
            content.put(e.jdoGetObjectId(), e);
        }
        return content;
    }
            
    /**
     * Compare two model contents for equality
     * 
     * @param r1Model element XRI
     * @param r2
     * @throws ServiceException 
     */
    @SuppressWarnings("unchecked")
    private void assertRepositoryEquality(
        Map<Path,ModelElement_1_0> r1,
        Map<Path,ModelElement_1_0> r2
    ) throws ServiceException{
        Set<Path> k1 = r1.keySet();
        Set<Path> k2 = r2.keySet();
        assertEquals("Model element XRIs", k1.size(), k2.size());
        assertEquals("Model element XRIs", k1, k2);
        for(Path k : k1){
            ModelElement_1_0 e1 = r1.get(k);
            ModelElement_1_0 e2 = r2.get(k);
            assertEquals("Model element class: " + k.toXRI(), e1.objGetClass(), e2.objGetClass());
            Set<String> f1 = new TreeSet<String>(e1.objDefaultFetchGroup());
            Set<String> f2 = new TreeSet<String>(e2.objDefaultFetchGroup());
            if(!e1.jdoGetObjectId().getLastSegment().toClassicRepresentation().contains("$UNNAMED$")){ 
                if(!"org.omg.model1.Class".equals(e1.objGetClass())){
                    f1.remove("allFeature");
                    f1.remove("allFeatureWithSubtype");
                    f1.remove(SystemAttributes.OBJECT_INSTANCE_OF);
                    f2.remove("allFeature");
                    f2.remove("allFeatureWithSubtype");
                    f2.remove(SystemAttributes.OBJECT_INSTANCE_OF);
                }
                f1.remove("dereferencedType");
                f1.remove("content");
                f1.remove("compositeReference");
                f1.remove(SystemAttributes.OBJECT_IDENTITY);
                f2.remove("dereferencedType");
                f2.remove("content");
                f2.remove("compositeReference");
                f2.remove(SystemAttributes.OBJECT_IDENTITY);
                assertEquals("Model type " + e1.objGetClass() +  ": " + k.toXRI(), f1, f2);
                for(String f : f1) {
                    Object v1 = ((Delegating_1_0<ObjectRecord>)e1).objGetDelegate().get(f);
                    Object v2 = ((Delegating_1_0<ObjectRecord>)e2).objGetDelegate().get(f);
                    String property = "Feature " + k.toXRI() + "#" + f;
                    if(v1 == null) {
                        assertNull(property, v2);
                    } else {
                        assertEquals(property, v1, v2);
                    }
                }
            }
        }
    }
            
    private void dumpModel(
        File destination,
        boolean binary
    ) throws FileNotFoundException, ServiceException{
        Model_1_0 model = Model_1Factory.getModel();
        if(binary) {
            final File file = new File(destination, "openmdxmof.wbxml");
            final FileOutputStream outputStream = new FileOutputStream(file);
            Model_1Dumper.save(outputStream, "application/vnd.openmdx-xmi.wbxml", model);
        } else {
            final File file = new File(destination, "openmdxmof.xml");
            final FileOutputStream outputStream =  new FileOutputStream(file);
            Model_1Dumper.save(outputStream, "text/xml", model);
        }
    }
    
    @Test
    public void dumpModel() throws FileNotFoundException, ServiceException{
        final File build = new File("build");
        final File platform = new File(build, "jre-" + System.getProperty("java.specification.version"));
        final File destination = new File(platform, "tmp");
        dumpModel(destination, false);
    }
        
    private Model_1_0 loadForeignModel(
        String uri
    ) throws ServiceException, IOException {
        URL url = new URL(uri);
        System.out.println(url);
        long startLoading = System.currentTimeMillis();
        Model_1_0 model = new ModelBuilder_1(false, url).build();
        long restoreTime = System.currentTimeMillis() - startLoading;
        SysLog.performance("Restoring openCRX model " + uri, restoreTime);
        return model;
    }
        
}

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openmdx.application.mof.repository.accessor.ModelBuilder_1;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Dumper;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.junit5.JDOExtension;
import org.openmdx.junit5.OpenmdxTestCoreStandardExtension;
import org.openmdx.kernel.log.SysLog;

/**
 * Test Model Repository
 */
@ExtendWith(OpenmdxTestCoreStandardExtension.class)
public class TestModelRepository {
    
	static JDOExtension jdoExtension = JDOExtension.withEntityManagerFactoryName("test-Main-EntityManagerFactory");

	/**
     * CR20020284
     */
    @Test
    public void loadAndSaveModelAsWBXML() throws ServiceException, IOException{
        Model_1_0 model = Model_1Factory.getModel();
        Map<Path, ModelElement_1_0> content1 = getContent(model);
        ByteArrayOutputStream outputStream1 =  new ByteArrayOutputStream();
        Model_1Dumper.save(outputStream1, "application/vnd.openmdx-xmi.wbxml", model);
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

    /**
     * CR20020284
     */
    @Test
    public void loadForeignModel() throws ServiceException, IOException{
        Model_1_0 model1 = loadForeignModel("xri://+zip*(xri://+resource/test/opencrx/openmdxmof.wbxml.zip)/openmdxmof.wbxml");
        Map<Path, ModelElement_1_0> content1 = getContent(model1);
        Model_1_0 model2 = loadForeignModel("xri://+resource/test/opencrx/openmdxmof.wbxml");
        Map<Path, ModelElement_1_0> content2 = getContent(model2);
        assertRepositoryEquality(content1, content2);
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
    private void assertRepositoryEquality(
        Map<Path,ModelElement_1_0> r1,
        Map<Path,ModelElement_1_0> r2
    ) throws ServiceException{
        Set<Path> k1 = r1.keySet();
        Set<Path> k2 = r2.keySet();
        Assertions.assertEquals(k1.size(), k2.size(), "Model element XRIs");
        Assertions.assertEquals(k1, k2, "Model element XRIs");
        for(Path k : k1){
            ModelElement_1_0 e1 = r1.get(k);
            ModelElement_1_0 e2 = r2.get(k);
            Assertions.assertEquals(e1.objGetClass(), e2.objGetClass(), "Model element class: " + k.toXRI());
            Set<String> f1 = new TreeSet<String>(e1.objDefaultFetchGroup());
            Set<String> f2 = new TreeSet<String>(e2.objDefaultFetchGroup());
            if(!e1.jdoGetObjectId().getLastSegment().toClassicRepresentation().contains("$UNNAMED$")){ 
                f1.remove(SystemAttributes.CREATED_BY);
                f1.remove(SystemAttributes.MODIFIED_BY);
                f2.remove(SystemAttributes.CREATED_BY);
                f2.remove(SystemAttributes.MODIFIED_BY);
                Assertions.assertEquals(f1, f2, "Model type " + e1.objGetClass() +  ": " + k.toXRI());
                for(String f : f1) {
                    Object v1 = e1.getDelegate().get(f);
                    Object v2 = e2.getDelegate().get(f);
                    String property = "Feature " + k.toXRI() + "#" + f;
                    if(v1 == null) {
                        Assertions.assertNull(v2, property);
                    } else {
                        Assertions.assertEquals(v1, v2, property);
                    }
                }
            }
        }
    }
            
    public static void main(String...strings) throws ServiceException, FileNotFoundException{
        Set<String> classes = new HashSet<String>();
        final Model_1_0 model = Model_1Factory.getModel();
//        for(ModelElement_1_0 element : model.getContent()) {
//            classes.add(element.objGetClass().substring(15));
//        }
        classes.add("Tag");
        final File dir = new File("build/src/java/org/openmdx/base/mof/repository/spi");
        dir.mkdirs();
        for(String name : classes) {
            final File file = new File(dir, name + "Record.java");
            final ModelElement_1_0 classDef = model.getElement("org:omg:model1:" + name);
            final Map<String, ModelElement_1_0> attributeDefs = model.getAttributeDefs(classDef, false, true);
            try(final PrintWriter writer = new PrintWriter(file)){
                writer.println("/*");
                writer.println(" * ====================================================================");
                writer.println(" * Project:     openMDX/Core, http://www.openmdx.org/");
                writer.println(" * Description: Query Record");
                writer.println(" * Description: org::omg::model1::" + name + " Record");
                writer.println(" */");
                writer.println("package org.openmdx.base.mof.repository.spi;");
                writer.println();
                writer.println("import org.openmdx.base.rest.spi.AbstractMappedRecord;");
                writer.println();
                writer.println("/**");
                writer.println(" * org::omg::model1::" + name + " Record");
                writer.println(" */");
                writer.println("public class " + name + "Record");
                writer.println("  extends AbstractMappedRecord<org.openmdx.base.mof.repository.cci." + name  + "Record.Member>");
                writer.println("  implements org.openmdx.base.mof.repository.cci." + name + "Record");
                writer.println("{");
                writer.println();
                writer.println("    /**");
                writer.println("     * Allows to share the member information among the instances");
                writer.println("     */");
                writer.println("    private static final Members<Member> MEMBERS = Members.newInstance(Member.class);");
                writer.println();
                writer.println();
                writer.println("    /**");
                writer.println("     * Implements <code>Serializable</code>");
                writer.println("     */");
                writer.println();
                writer.println("    @Override");
                writer.println("    public void makeImmutable() {");
                writer.println("        super.makeImmutable();");
                writer.println("    }");
                writer.println();
                writer.println("    @Override");
                writer.println("    public String getRecordName() {");
                writer.println("        return NAME;");
                writer.println("    }");
                writer.println();
                writer.println("    /**");
                writer.println("     * Retrieve a value by index");
                writer.println("     *");
                writer.println("     * @param index the index");
                writer.println("     * @return the value");
                writer.println("     */");
                writer.println("     @Override");
                writer.println("     protected Object get(");
                writer.println("         Member index");
                writer.println(      "){");
                writer.println("         switch(index) {");
                for(String attr : attributeDefs.keySet()) {
                    writer.println("             case " + attr + ": return null; // TODO");
                }
                writer.println("             default: return super.get(index);");
                writer.println(      "}");
                writer.println();
                writer.println("    /**");
                writer.println("     * Retrieve a value by index");
                writer.println("     * ");
                writer.println("     * @param index the index");
                writer.println("     * @param value the new value");
                writer.println("     * ");
                writer.println("     * @return the old value");
                writer.println("     */");
                writer.println("     @Override");
                writer.println("     protected void put(");
                writer.println("         Member index,");
                writer.println("         Object value");
                writer.println("     ){");
                writer.println("         switch(index) {");
                for(String attr : attributeDefs.keySet()) {
                    writer.println("             case " + attr + ":");
                    writer.println("                 break;");
                }
                writer.println("            default:");
                writer.println("                 super.put(index, value);");
                writer.println("         }");
                writer.println("     }");
                writer.println("         ");
                writer.println();
                writer.println("    @Override");
                writer.println("    protected Members<Member> members() {");
                writer.println("        return MEMBERS;");
                writer.println("    }");
                writer.println();
                writer.println("}");
            }
        }
    }
    
    private void dumpModel(
        File destination,
        boolean binary
    ) throws FileNotFoundException, ServiceException{
        final String mimeType = binary ? "application/vnd.openmdx-xmi.wbxml" : "text/xml";
        final File file = new File(destination, binary ? "openmdxmof.wbxml" : "openmdxmof.xml");
        final FileOutputStream outputStream = new FileOutputStream(file);
        Model_1Dumper.save(outputStream, mimeType, Model_1Factory.getModel());
    }
    
    @Test
    public void dumpModel() throws FileNotFoundException, ServiceException, InterruptedException{
        final File build = new File("build");
        final File temp = new File(build, "tmp");
        final File destination = new File(temp, "dumpModel");
        final boolean created = destination.mkdir();
        System.out.println("Directory " + destination.getAbsolutePath() + (created ? " created" : " did exist"));
        if(created) Thread.sleep(500);
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

    @Test
    public void determineFeatureType() throws ServiceException {
        //
        // Arrange
        //
        final Model_1_0 model = Model_1Factory.getModel();
        final String qualifiedFeatureName = "org:openmdx:base:Creatable:createdAt";
        //
        // Act
        //
        final ModelElement_1_0 attributeDef = model.getElement(qualifiedFeatureName);
        final String qualifiedTypeName = attributeDef.getType().getLastSegment().toString();
        //
        // Assert
        //
        Assertions.assertEquals("org:w3c:dateTime", qualifiedTypeName);
    }
}

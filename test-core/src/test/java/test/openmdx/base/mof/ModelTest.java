/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Model Test
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
package test.openmdx.base.mof;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;

/**
 * Model Test
 */
public class ModelTest {

    @Test
    public void testGetCompositeReference (
    ) throws ServiceException {
        Model_1_0 model1 = Model_1Factory.getModel();
        Model_1_0 model = model1;
        // org:omg:model1:Classifier
        ModelElement_1_0 compositeReferenceClassifier = model.getCompositeReference(model.getElement("org:omg:model1:Classifier"));
        Assertions.assertEquals("org:omg:model1:Segment:element",compositeReferenceClassifier.getQualifiedName(),"Composite reference of 'org:omg:model1:Classifier'");
        ModelElement_1_0 compositeReferenceClassifierExposedEnd = model.getElement(compositeReferenceClassifier.getExposedEnd());
        Assertions.assertEquals("org:omg:model1:SegmentContainsElement:segment",compositeReferenceClassifierExposedEnd.getQualifiedName(),"Exposed end of 'org:omg:model1:Segment:element'");
        Assertions.assertEquals("org:omg:model1:Segment",model.getElement(compositeReferenceClassifierExposedEnd.getType()).getQualifiedName(),"Referenced type of 'org:omg:model1:Segment:element'");
        // org:omg:model1:Segment
        ModelElement_1_0 compositeReferenceSegment = model.getCompositeReference(model.getElement("org:omg:model1:Segment"));
        Assertions.assertEquals("org:openmdx:base:Provider:segment",compositeReferenceSegment.getQualifiedName(),"Composite reference of 'org:omg:model1:Segment'");        
        ModelElement_1_0 compositeReferenceSegmentExposedEnd = model.getElement(compositeReferenceSegment.getExposedEnd());
        Assertions.assertEquals("org:openmdx:base:ProviderProvidesSegment:provider",compositeReferenceSegmentExposedEnd.getQualifiedName(),"Exposed end of 'org:openmdx:base:Provider:segment'");
        Assertions.assertEquals("org:openmdx:base:Provider",model.getElement(compositeReferenceSegmentExposedEnd.getType()).getQualifiedName(),"Referenced type of 'org:openmdx:base:Provider:segment'");
    }

}

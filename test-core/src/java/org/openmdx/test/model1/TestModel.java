package org.openmdx.test.model1;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_3;
import org.openmdx.base.mof.spi.Model_1Factory;

/**
 * TestModel
 *
 */
public class TestModel
    extends TestCase
{

    /**
     * Pass control to the non-graphical test runner
     */
    public static void main(
        String[] args
    ) {
        TestRunner.run(suite());
    }

    /**
     * 
     */
    public static Test suite(
    ){
        return new TestSuite(TestModel.class);
    }

    /**
     *
     */
    public void testGetCompositeReference (
    ) throws ServiceException {
        Model_1_3 model1 = Model_1Factory.getModel();
        Model_1_3 model = model1;
        // org:omg:model1:Classifier
        ModelElement_1_0 compositeReferenceClassifier = model.getCompositeReference(model.getElement("org:omg:model1:Classifier"));
        assertEquals(
            "Composite reference of 'org:omg:model1:Classifier'", 
            "org:omg:model1:Segment:element",
            compositeReferenceClassifier.objGetValue("qualifiedName")
        );
        ModelElement_1_0 compositeReferenceClassifierExposedEnd = model.getElement(compositeReferenceClassifier.objGetValue("exposedEnd"));
        assertEquals(
            "Exposed end of 'org:omg:model1:Segment:element'", 
            "org:omg:model1:SegmentContainsElement:segment",
            compositeReferenceClassifierExposedEnd.objGetValue("qualifiedName")
        );
        assertEquals(
            "Referenced type of 'org:omg:model1:Segment:element'",
            "org:omg:model1:Segment",
            model.getElement(compositeReferenceClassifierExposedEnd.objGetValue("type")).objGetValue("qualifiedName")
        );
        // org:omg:model1:Segment
        ModelElement_1_0 compositeReferenceSegment = model.getCompositeReference(model.getElement("org:omg:model1:Segment"));
        assertEquals(
            "Composite reference of 'org:omg:model1:Segment'", 
            "org:openmdx:base:Provider:segment",
            compositeReferenceSegment.objGetValue("qualifiedName")
        );        
        ModelElement_1_0 compositeReferenceSegmentExposedEnd = model.getElement(compositeReferenceSegment.objGetValue("exposedEnd"));
        assertEquals(
            "Exposed end of 'org:openmdx:base:Provider:segment'", 
            "org:openmdx:base:ProviderProvidesSegment:provider",
            compositeReferenceSegmentExposedEnd.objGetValue("qualifiedName")
        );
        assertEquals(
            "Referenced type of 'org:openmdx:base:Provider:segment'",
            "org:openmdx:base:Provider",
            model.getElement(compositeReferenceSegmentExposedEnd.objGetValue("type")).objGetValue("qualifiedName")
        );
    }

}

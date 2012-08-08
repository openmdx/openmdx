package org.openmdx.test.model1;


import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.accessor.basic.spi.Model_1;

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

    protected static Model_1_3 getModel(
    ) throws ServiceException {
        Model_1_3 model = new Model_1();
        model.addModels(
            Arrays.asList(
                new String[]{
                    "org:un",
                    "org:iso",
                    "org:w3c",
                    "org:oasis-open",
                    "org:openmdx:base",
                    "org:openmdx:compatibility:view1",
                    "org:omg:model1"
                }
            )
        );
        return model;
    }

    /**
     *
     */
    public void testGetCompositeReference (
    ) throws ServiceException {
        Model_1_3 model = getModel();
        // org:omg:model1:Classifier
        ModelElement_1_0 compositeReferenceClassifier = model.getCompositeReference(model.getElement("org:omg:model1:Classifier"));
        assertEquals(
            "Composite reference of 'org:omg:model1:Classifier'", 
            "org:omg:model1:Segment:element",
            compositeReferenceClassifier.values("qualifiedName").get(0)
        );
        ModelElement_1_0 compositeReferenceClassifierExposedEnd = model.getElement(compositeReferenceClassifier.getValues("exposedEnd").get(0));
        assertEquals(
            "Exposed end of 'org:omg:model1:Segment:element'", 
            "org:omg:model1:SegmentContainsElement:segment",
            compositeReferenceClassifierExposedEnd.values("qualifiedName").get(0)
        );
        assertEquals(
            "Referenced type of 'org:omg:model1:Segment:element'",
            "org:omg:model1:Segment",
            model.getElement(compositeReferenceClassifierExposedEnd.values("type").get(0)).values("qualifiedName").get(0)
        );
        // org:omg:model1:Segment
        ModelElement_1_0 compositeReferenceSegment = model.getCompositeReference(model.getElement("org:omg:model1:Segment"));
        assertEquals(
            "Composite reference of 'org:omg:model1:Segment'", 
            "org:openmdx:base:Provider:segment",
            compositeReferenceSegment.values("qualifiedName").get(0)
        );        
        ModelElement_1_0 compositeReferenceSegmentExposedEnd = model.getElement(compositeReferenceSegment.getValues("exposedEnd").get(0));
        assertEquals(
            "Exposed end of 'org:openmdx:base:Provider:segment'", 
            "org:openmdx:base:ProviderProvidesSegment:provider",
            compositeReferenceSegmentExposedEnd.values("qualifiedName").get(0)
        );
        assertEquals(
            "Referenced type of 'org:openmdx:base:Provider:segment'",
            "org:openmdx:base:Provider",
            model.getElement(compositeReferenceSegmentExposedEnd.values("type").get(0)).values("qualifiedName").get(0)
        );
    }

}

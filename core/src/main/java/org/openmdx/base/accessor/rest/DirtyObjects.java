/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Dirty Features 
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
package org.openmdx.base.accessor.rest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.jmi.reflect.RefObject;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.kernel.log.SysLog;


/**
 * Dirty Objects
 */
public class DirtyObjects {

    /**
     * Constructor 
     */
    private DirtyObjects() {
        // Avoid instantiation
    }

    /**
     * Retrieve the attribute definitions
     * 
     * @param dataObject
     * 
     * @return the attribute definitions
     * 
     * @throws ServiceException
     */
    private static Map<String,ModelElement_1_0> getAttributeDefs(
        DataObject_1 dataObject
    ) throws ServiceException{
        ModelElement_1_0 classifier = dataObject.getClassifier();
        return classifier.getModel().getAttributeDefs(
            classifier,
            false, // sub-types
            true // includeDerived
        );
    }
    
    /**
     * Test whether two objects are either both {@code null} or equal.
     * 
     * @param left
     * @param right
     * 
     * @return {@code true} if either both objects are {@code null} or equal.
     */
    private static boolean equal(
        Object left,
        Object right
    ){
        return left == right || (
            left != null && right != null && left.equals(right)
        );
    }
    
    /**
     * Tells whether the given feature has been modified
     * 
     * @param feature
     * 
     * @return {@code true} if the feature has been modified
     * 
     * @throws ServiceException
     */
    private static boolean isFeatureModified(
        ModelElement_1_0 feature,
        DataObject_1 afterImage,
        DataObject_1 beforeImage
    ) throws ServiceException {
        Multiplicity multiplicity = ModelHelper.getMultiplicity(feature);
        String featureName = feature.getName();
        switch(multiplicity) {
            case SINGLE_VALUE: case OPTIONAL: 
                return !equal(beforeImage.objGetValue(featureName), afterImage.objGetValue(featureName));
            case LIST: 
                return !equal(beforeImage.objGetList(featureName), afterImage.objGetList(featureName));
            case SET: 
                return !equal(beforeImage.objGetSet(featureName), afterImage.objGetSet(featureName));
            case SPARSEARRAY: 
                return !equal(beforeImage.objGetSparseArray(featureName), afterImage.objGetSparseArray(featureName));
            case STREAM:
                SysLog.log(
                    Level.FINER, 
                    "{0} features are not compared with their before image, " +
                    "the feature {1} in the object {2} as therefore treated as modified", 
                    multiplicity, 
                    featureName, 
                    afterImage.jdoIsPersistent() ? afterImage.jdoGetObjectId() : afterImage.jdoGetTransactionalObjectId()
                );
                return true;
                
            default:
                SysLog.log(
                    Level.WARNING, 
                    "Unsupported Multiplicity {0}, treat the feature {1} in the object {2} as modified", 
                    multiplicity, 
                    featureName, 
                    afterImage.jdoIsPersistent() ? afterImage.jdoGetObjectId() : afterImage.jdoGetTransactionalObjectId()
                );
                return true;
        }
    }
            
    /**
     * Determines which features have been modified
     * 
     * @return the names of the modified features
     * 
     * @throws ServiceException
     */
    public static Set<String> getModifiedFeatures(
        DataObject_1_0 dataObject
    ) throws ServiceException {
        DataObject_1 afterImage = (DataObject_1) dataObject;
        Set<String> dirtyFeatures = new HashSet<String>();
        DataObject_1 beforeImage = afterImage.getBeforeImage();
        Map<String,ModelElement_1_0> attributes = getAttributeDefs(afterImage);
        for(String dirtyFeature : afterImage.getState(false).dirtyFeatures(true)) {
            if(isFeatureModified(attributes.get(dirtyFeature), afterImage, beforeImage)) {
                dirtyFeatures.add(dirtyFeature);
            }
        }
        return dirtyFeatures;
    }
    
    /**
     * Determines which features have been modified
     * 
     * @return the names of the modified features
     * 
     * @throws NullPointerException if {@code refObject} is {@code null}
     * @throws ServiceException in case of failure
     */
    public static Set<String> getModifiedFeatures(
        RefObject refObject
    ) throws ServiceException {
        return getModifiedFeatures(DataObjects.getDataObject(refObject));
    }
    
    /** 
    * Touches a given JMI object explicitely
    * 
    * @param the object to be touched 
    * 
    * @throws NullPointerException if {@code refObject} is {@code null}
    * @throws ServiceException if touching fails
    */ 
    public static void touch( 
        RefObject refObject 
    ) throws ServiceException { 
        DataObjects.getDataObject(refObject).touch(); 
    } 
 
}

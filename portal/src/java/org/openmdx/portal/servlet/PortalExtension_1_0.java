/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: PortalExtension_1_0.java,v 1.19 2008/08/26 23:35:11 wfro Exp $
 * Description: Evaluator_1_0
 * Revision:    $Revision: 1.19 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/08/26 23:35:11 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.servlet;

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefStruct;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.portal.servlet.control.ControlFactory;
import org.openmdx.portal.servlet.control.GridControl;
import org.openmdx.portal.servlet.view.ObjectView;

public interface PortalExtension_1_0 {

    /**
     * Evaluates the title obj the object refObj.
     * 
     * @param refObj object to evaluate the title
     * @param locale locale index. Can be used to lookup code texts
     * @param localeAsString the locale name
     * @param application TODO
     * @return evaluated object title
     */
    public String getTitle(
        RefObject_1_0 refObj,
        short locale,
        String localeAsString,
        ApplicationContext application
    );

    /**
     * Determines whether the ui element is enabled in the context of view. The result
     * is typically used in the ui rendering process to determine whether an ui element should
     * be rendered as enabled or disabled.
     * 
     * @param elementName name of element to be tested (normally qualified ui element name)
     * @param refObj evaluate in the context of the object
     * @param context evaluate in the application context
     * @return true when the element is enabled
     */
    public boolean isEnabled(
        String elementName,
        RefObject_1_0 refObj,
        ApplicationContext context
    );
  
    /**
     * Return query filter clause which must be performed when a user issues a 
     * find on the object identity. 
     * 
     * @param qualifiedReferenceName qualified reference name
     * @return identity query. The query must have exactly one string parameter,
     *         e.g. (title LIKE ?s0).
     */
    public String getIdentityQueryFilterClause(        
        String qualifiedReferenceName
    );
    
    public int getGridPageSize(
        String referencedTypeName
    );
    
    /**
     * Returns true if the rows of the grid containing objects of type 
     * referencedTypeName are colour enabled.
     */
    public boolean hasGridColours(
        String referencedTypeName
    );

    /**
     * Returns true if the grid content should be loaded and showed the first time
     * the grid is displayed.  
     */
    public boolean showGridContentOnInit(
        GridControl gridControl,
        ApplicationContext application
    );

    /**
     * Get autocompleter for the specified object.
     * 
     * @return null if no autocompleter is available.
     */
    public Autocompleter_1_0 getAutocompleter(
        ApplicationContext application,
        RefObject_1_0 context,
        String qualifiedFeatureName
    );

    /**
     * Returns true if the specified type is not a valid lookup type. 
     * Autocompleter must exclude non lookup types in their lookup options.
     */
    public boolean isLookupType(
        ModelElement_1_0 classDef
    );
    
    /**
     * Returns a list of <code>FilterProperty</code>s which are added to the query
     * when processing a FIND_OBJECTS action. E.g. non-active objects should not
     * be returned by the find objects reply a filter of the form active=true can
     * be returned.
     */
    public List getFindObjectsBaseFilter(
        ApplicationContext application,
        RefObject_1_0 context,
        String qualifiedFeatureName
    );
    
    /**
     * Maps the form request to the specified object. The object must either
     * be instanceof RefObject_1_0 or Map.
     * 
     * @param fieldMap map containing the (modeled) field definitions of the target object.
     *        Entries are of the form (key=qualified feature name, value=feature instance of
     *        org.openmdx.portal.servlet.attribute.Attribute).
     *        
     * @param fieldMap Map containing the fields received by the form post request.
     *        Entries are of the form (key='feature[index][.false | .true]', value).
     */
    public void updateObject(
        Object target,
        Map parameterMap,
        Map fieldMap,
        ApplicationContext application,
        PersistenceManager pm
    );
    
    /**
     * Get object which allows to lookup objects of referenced type
     */
    public RefObject_1_0 getLookupObject(
        ModelElement_1_0 lookupType,
        RefObject_1_0 startFrom,
        ApplicationContext application,
        PersistenceManager pm
    ) throws ServiceException;

    /**
     * Get view which allows to lookup objects of referenced types
     */
    public ObjectView getLookupView(
        String id,
        ModelElement_1_0 lookupType,
        RefObject_1_0 startFrom,
        String filterValues,
        ControlFactory controlFactory,
        ApplicationContext application
    ) throws ServiceException;
    
    /**
     * Returns true if the qualifier for the specified inspector is user-defineable.
     * User-defineable qualifiers can be entered by the user when creating a new object.
     */
    public boolean hasUserDefineableQualifier(
        org.openmdx.ui1.jmi1.Inspector inspector,
        ApplicationContext application
    );
    
    /**
     * Renders the text of a text value field. E.g. an implementation can replace
     * text macros such as 'activity:#<activity number>' with a href of the corresponding
     * SELECT_OBJECT action.
     *  
     * @return preprocessed text
     */
    public void renderTextValue(
        HtmlPage p,
        String value
    ) throws ServiceException;
 
    /**
     * Get date style for given feature
     * @param qualifiedFeatureName null or qualified name of feature for which date style is returned. 
     */
    public int getDateStyle(
       String qualifiedFeatureName,
       ApplicationContext application
    );

    /**
     * Get time style for given feature 
     * @param qualifiedFeatureName null or qualified name of feature for which date style is returned.
     */
    public int getTimeStyle(
       String qualifiedFeatureName,
       ApplicationContext application
    );

    /**
     * Get data binding with given name 
     */
    public DataBinding_1_0 getDataBinding(
        String dataBindingName,
        ApplicationContext application
    );
    
    /**
     * Invoked after the execution of an operation. The method can handles operation 
     * results and must return the target object of the next view to be shown.
     * @param target operation was invoked on the target object
     * @param operationName invoked operation
     * @param params parameter of operation invocation
     * @param result operation result
     * @return object to be shown after operation invocation. null if user should not
     *         be directed to a new view.
     */
    public RefObject_1_0 handleOperationResult(
        RefObject_1_0 target,
        String operationName,
        RefStruct params,
        RefStruct result
    ) throws ServiceException;
    
}

//--- End of File -----------------------------------------------------------

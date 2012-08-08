/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: PortalExtension_1_0.java,v 1.42 2010/11/01 13:41:49 wfro Exp $
 * Description: Evaluator_1_0
 * Revision:    $Revision: 1.42 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/11/01 13:41:49 $
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

import javax.jmi.reflect.RefStruct;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.query.Condition;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.control.Control;
import org.openmdx.portal.servlet.control.GridControl;
import org.openmdx.portal.servlet.view.ObjectView;

public interface PortalExtension_1_0 {

    /**
     * Evaluates the title obj the object refObj.
     * 
     * @param refObj object to evaluate the title
     * @param locale locale index. Can be used to lookup code texts
     * @param localeAsString the locale name
     * @param asShortTitle true for object title in short form
     * @param application application context
     * @return evaluated object title
     */
    public String getTitle(
        RefObject_1_0 refObj,
        short locale,
        String localeAsString,
        boolean asShortTitle,
        ApplicationContext application
    );

    /**
     * Determines whether the ui element is enabled for the specified object. The result
     * is used in the rendering process to determine whether an ui element should
     * be rendered as enabled or disabled.
     * 
     * @param elementName name of element to be tested (normally qualified ui element name)
     * @param refObj enable / disable element when rendering this object
     * @param applicationContext application context
     * @return true when the element is enabled
     */
    public boolean isEnabled(
        String elementName,
        RefObject_1_0 refObj,
        ApplicationContext applicationContext
    );
  
    /**
     * Determines whether the control is enabled for the specified object. The result
     * is used in the rendering process to determine whether the control should rendered.
     * 
     * @param control to be tested
     * @param refObj enable / disable element when rendering this object
     * @param applicationContext application context
     * @return true when the element is enabled
     */
    public boolean isEnabled(
        Control control,
        RefObject_1_0 refObj,
        ApplicationContext applicationContext
    );
  
    /**
     * Determines whether the specified object is enabled. The object can be a root object,
     * UI segment, etc.
     * 
     * @param refObj enable / disable element when rendering this object
     * @param applicationContext application context
     * @return true when the element is enabled
     */
    public boolean isEnabled(
        RefObject_1_0 refObj,
        ApplicationContext applicationContext
    );
  
    /**
     * Return query filter clause which must be performed when a user issues a 
     * find on the object identity. 
     * 
     * @param field return the query for field
     * @param filterValue field is queried for this value.
     * @param queryFilterStringParamCount use this count for string parameters for query filters
     * @param application the application context
     *
     * @return a filter
     */
    public org.openmdx.base.query.Filter getQuery(        
    	org.openmdx.ui1.jmi1.ValuedField field,
        String filterValue,
        int queryFilterStringParamCount,
        ApplicationContext application
    );
    
    public int getGridPageSize(
        String referencedTypeName
    );
    
    /**
     * Returns colors which is used as foreground and background colors when an 
     * object is rendered in a grid.
     * @return foreground (at index 0) and background color (at index 1) or null. 
     */
    public String[] getGridRowColors(
        RefObject_1_0 obj
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
     * The default implementation shows the search form according to the
     * user settings. The default value is false if no user setting is found.
     */
    public boolean showSearchForm(
        GridControl gridControl,
        ApplicationContext app
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
    ) throws ServiceException;
    
    /**
     * Returns a list of <code>FilterProperty</code>s which are added to the query
     * when processing a FIND_OBJECTS action. E.g. non-active objects should not
     * be returned by the find objects reply a filter of the form active=true can
     * be returned.
     */
    public List<Condition> getFindObjectsBaseFilter(
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
        Map<String,Object[]> parameterMap,
        Map<String,Attribute> fieldMap,
        ApplicationContext application
    );
    
    /**
     * Get object which allows to lookup objects of referenced type
     */
    public RefObject_1_0 getLookupObject(
        ModelElement_1_0 lookupType,
        RefObject_1_0 startFrom,
        ApplicationContext application
    ) throws ServiceException;

    /**
     * Get view which allows to lookup objects of referenced types
     */
    public ObjectView getLookupView(
        String id,
        ModelElement_1_0 lookupType,
        RefObject_1_0 startFrom,
        String filterValues,
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
     * SELECT_OBJECT action. If isWiki is true then use wiki tags for rendering.
     *  
     * @return preprocessed text
     */
    public void renderTextValue(
        ViewPort p,
        String value,
        boolean asWiki
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
    public DataBinding getDataBinding(
        String dataBindingName
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

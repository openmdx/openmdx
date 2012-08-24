/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: Evaluator_1_0
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
import java.util.TimeZone;

import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefStruct;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.Condition;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.control.Control;
import org.openmdx.portal.servlet.control.GridControl;
import org.openmdx.portal.servlet.view.Grid;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.ui1.jmi1.ValuedField;

public interface PortalExtension_1_0 {

    /**
     * Evaluates the title obj the object refObj.
     * 
     * @param refObj object to evaluate the title
     * @param locale locale index. Can be used to lookup code texts
     * @param localeAsString the locale name
     * @param asShortTitle true for object title in short form
     * @param app application context
     * @return evaluated object title
     */
    String getTitle(
        RefObject_1_0 refObj,
        short locale,
        String localeAsString,
        boolean asShortTitle,
        ApplicationContext app
    );

    /**
     * Evaluates the title for a given action and locale.
     * 
     * @param obj title is evaluated for action for given object.
     * @param action action id
     * @param title default title for this action
     * @param app application context
     */
    String getTitle(
    	Object obj,
    	Action action,
    	String title,
    	ApplicationContext app
    );

    /**
     * Returns true if user has permission for specified elementName and actions
     * in the context of object.
     * 
     * @param elementName name of element to be tested (normally qualified ui element name)
     * @param object 
     * @param app application context
     * @param action permission is tested for actions
     * @return true if user has permission for all actions
     */
    boolean hasPermission(
        String elementName,
        RefObject_1_0 object,
        ApplicationContext app,
        String action
    );
  
    /**
     * Returns true if user has permission for specified control and actions
     * in the context of object.
     * 
     * @param control to be tested
     * @param object context
     * @param action permission is tested for action
     * @param app application context
     * @return returns true if user has permission for all actions
     */
    boolean hasPermission(
        Control control,
        RefObject_1_0 object,
        ApplicationContext app,
        String action
    );
  
    /**
     * Returns true if user has permission for specified actions
     * in the context of object.
     *  
     * @param object context
     * @param actions permission is tested for actions
     * @param app application context
     * @return true when the element is enabled
     */
    boolean hasPermission(
        RefObject_1_0 object,
        ApplicationContext app,
        String action        
    );
  
    /**
     * Return query filter clause which must be performed when a user issues a 
     * find on the object identity. 
     * 
     * @param field return the query for field
     * @param filterValue field is queried for this value.
     * @param queryFilterStringParamCount use this count for string parameters for query filters
     * @param app the application context
     *
     * @return a filter
     */
    org.openmdx.base.query.Filter getQuery(        
    	org.openmdx.ui1.jmi1.ValuedField field,
        String filterValue,
        int queryFilterStringParamCount,
        ApplicationContext app
    );
    
    int getGridPageSize(
        String referencedTypeName
    );
    
    /**
     * Returns colors which is used as foreground and background colors when an 
     * object is rendered in a grid.
     * @return foreground (at index 0) and background color (at index 1) or null. 
     */
    String[] getGridRowColors(
        RefObject_1_0 obj
    );

    /**
     * Returns true if the grid content should be loaded and showed the first time
     * the grid is displayed.  
     */
    boolean showGridContentOnInit(
        GridControl gridControl,
        ApplicationContext app
    );

    /**
     * The default implementation shows the search form according to the
     * user settings. The default value is false if no user setting is found.
     */
    boolean showSearchForm(
        GridControl gridControl,
        ApplicationContext app
    );
    
    /**
     * Get autocompleter for the specified object.
     * 
     * @return null if no autocompleter is available.
     */
    Autocompleter_1_0 getAutocompleter(
        ApplicationContext application,
        RefObject_1_0 context,
        String qualifiedFeatureName
    );

    /**
     * Returns true if the specified type is not a valid lookup type. 
     * Autocompleter must exclude non lookup types in their lookup options.
     */
    boolean isLookupType(
        ModelElement_1_0 classDef
    ) throws ServiceException;
    
    /**
     * Returns a list of <code>FilterProperty</code>s which are added to the query
     * when processing a FIND_OBJECTS action. E.g. non-active objects should not
     * be returned by the find objects reply a filter of the form active=true can
     * be returned.
     */
    List<Condition> getFindObjectsBaseFilter(
        ApplicationContext app,
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
    void updateObject(
        Object target,
        Map<String,String[]> parameterMap,
        Map<String,Attribute> fieldMap,
        ApplicationContext app
    );
    
    /**
     * Get object which allows to lookup objects of referenced type
     */
    RefObject_1_0 getLookupObject(
        ModelElement_1_0 lookupType,
        RefObject_1_0 startFrom,
        ApplicationContext app
    ) throws ServiceException;

    /**
     * Get view which allows to lookup objects of referenced types
     */
    ObjectView getLookupView(
        String id,
        ModelElement_1_0 lookupType,
        RefObject_1_0 startFrom,
        String filterValues,
        ApplicationContext app
    ) throws ServiceException;
    
    /**
     * Returns true if the qualifier for the specified inspector is user-defineable.
     * User-defineable qualifiers can be entered by the user when creating a new object.
     */
    boolean hasUserDefineableQualifier(
        org.openmdx.ui1.jmi1.Inspector inspector,
        ApplicationContext app
    );
    
    /**
     * Renders the text of a text value field. E.g. an implementation can replace
     * text macros such as 'activity:#<activity number>' with a href of the corresponding
     * SELECT_OBJECT action. If isWiki is true then use wiki tags for rendering.
     *  
     * @return preprocessed text
     */
    void renderTextValue(
        ViewPort p,
        String value,
        boolean asWiki
    ) throws ServiceException;

    /**
     * Get date style for given feature
     * @param qualifiedFeatureName null or qualified name of feature for which date style is returned. 
     */
    int getDateStyle(
       String qualifiedFeatureName,
       ApplicationContext app
    );

    /**
     * Get time style for given feature 
     * @param qualifiedFeatureName null or qualified name of feature for which date style is returned.
     */
    int getTimeStyle(
       String qualifiedFeatureName,
       ApplicationContext app
    );

    /**
     * Get time zone for given feature. For most date and dateTime features the time zone of the 
     * current context is returned, i.e. TimeZone.getTimeZone(app.getCurrentTimeZone()). However,
     * for fields such as birthday it makes sense to return UTC (the displayed date is then
     * not adjusted with the meaning relative to the place of birth).
     * @param qualifiedFeatureName
     * @param app
     * @return
     */
    TimeZone getTimeZone(
    	String qualifiedFeatureName,
    	ApplicationContext app
    );

    /**
     * Get data binding with given name 
     */
    DataBinding getDataBinding(
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
    RefObject_1_0 handleOperationResult(
        RefObject_1_0 target,
        String operationName,
        RefStruct params,
        RefStruct result
    ) throws ServiceException;
    
    /**
     * Return the new user role based on the current content and request object.
     * By default the current role is returned.
     */
    String getNewUserRole(
    	ApplicationContext app,
    	Path requestedObject
    );

    /**
     * Get actions for specified grid. The actions are added to the grid's action menu.
     * @return list of actions or null.
     */
    List<Action> getGridActions(
    	ObjectView view,
    	Grid grid
    ) throws ServiceException;

    /**
     * Get action-factory.
     * @return action factory.
     */
    ActionFactory_1_0 getActionFactory();

    /*
     * Get admin principal name for given realm.
     */
    String getAdminPrincipal(
        String realmName
    );

    /**
     * Get principal name for root.
     */
    public boolean isRootPrincipal(
        String principalName
    );
    
    /**
     * Get all roles of principal.
     * @param principalName principal name or principal chain {p0, ..., pn}
     */
    List<String> getUserRoles(
        Path realmIdentity,
        String principalName,
        PersistenceManager pm
    ) throws ServiceException;

    /**
     * Check whether principal is defined and valid for given realm.
     * @param principalName principal name or principal chain {p0, ..., pn}
     */
    boolean checkPrincipal(
        Path realmIdentity,
        String principalName,
        PersistenceManager pm
    ) throws ServiceException;
    
    /**
     * Set last login at for given principal.
     * @param principalName principal name or principal chain {p0, ..., pn}
     */
    void setLastLoginAt(
    	Path realmIdentity,
    	String segmentName,    	
    	String principalName,
    	PersistenceManager pm
    ) throws ServiceException;
 
    /**
     * Get autostart URL. This method is invoked immediately after creation of the session
     * and the application context. If not null, the user is forwarded to 
     * the specified URL.
     * 
     * @return autostart URL or null
     */
    String getAutostartUrl(
    	ApplicationContext app
    );

    /**
     * 
     */
    
    public interface ConditionParser {
    	
    	Condition parse(
    		String token
    	);
    	
    	int getOffset();
    	
    }
    
    /**
     * Return condition parser for specified field.
     * @param field
     * @param defaultCondition
     * @return
     */
    ConditionParser getConditionParser(
    	ValuedField field,
    	Condition defaultCondition
    );

    /**
     * Get custom-specific extension.
     * @return custom-specific extension.
     */
    Object getExtension(
    	String name
    );

}

//--- End of File -----------------------------------------------------------

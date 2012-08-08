/*
 * ====================================================================
 * Name:        $Id: AbstractState_1.java,v 1.29 2008/06/28 00:21:27 hburger Exp $
 * Description: State_1 plug-in
 * Revision:    $Revision: 1.29 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/28 00:21:27 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.layer.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequestContexts;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.layer.persistence.common.AbstractIterator;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.naming.PathComponent;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.AggregationKind;


/**
 * This class implements the state pattern.
 * <p>
 * For each spice object a root object and several state objects are produced
 * and maintained. The root object contains validFrom, validTo, createdAt, 
 * createdBy, modifiedAt, modifiedBy, object_class  for the entire spice object.
 * It also contains STATE_NUMBER, which indicates the current number of states.
 * At the moment it does not contain any further attributes.
 * <p>
 * A state object represents a state of a spice object which is valid in the 
 * time span validFrom, validTo. It contains all attributes the object has at 
 * that time. It also contains validFrom, validTo, createdAt, 
 * createdBy, modifiedAt, modifiedBy, object_class but the values are valid only
 * for the state (except object_class which may not change). Each state takes
 * for createdAt, createdBy, modifiedAt, modifiedBy the date it was created or 
 * modified (not the entire object!). 
 * <p> 
 * States and root objects are stored side by side, the root object taking the
 * original path. The states get added to the id of the root a growing
 * number inside colons (":"). Leading to a path like ".../objId:2:".
 * This number is the state number. 
 * The state number is maintained in the root object (STATE_NUMBER).
 * <p>
 * States get invalidated by updates. They are replaced by new states having
 * correct validFrom, validTo and other changed attribute values. The invalidated
 * state gets an attribute invalidatedAt set to the timepoint it was replaced. 
 * remove() invalidates all states of an object and sets the invalidatedAt
 * attribute of the root object. Valid states are the ones missing the attribute
 * invalidatedAt.
 * <p>
 * validFrom and validTo are forever if they are null. The validFrom date is 
 * including, the validTo is in respectively excluding depending on the abstract
 * subclass. To have a state a and b following each other immediately<ul>
 * <li>a.validTo == b.validFrom <i>(in case of StateWithHoles_1 and DateState_1)</i>
 * <li>a.validTo == b.valdiFrom + 1 day <i>(in case of DateStateExcludingEnd_1)</i>
 * </ul>
 * <p>
 * The header provides two settings, requestedAt and requestedFor. RequestedAt
 * defines the time one is looking at the data, requestedFor defines which 
 * state in this subset of states is desired.
 * <p>
 * For create, update or modify requests, requestedFor must be between 
 * validFrom, validTo of the request. A NOT_SUPPORTED exception is thrown
 * otherwise. If not set, it defaults to the validFrom date
 * of the request, even if it is null, which then delivers the very first state.
 * The requestedAt is not treated at all, because the object 
 * returned would be totally different from the requested changes, which could 
 * be a bit confusing.
 * <p>
 * For updates of role the requestedFor must be at a valid state, containing
 * the role to update.
 * <p>
 */
@SuppressWarnings("unchecked")
public abstract class AbstractState_1 
  extends Standard_1 
{

    protected AbstractState_1() {
        super();
    }
    
    protected static class OperationParameter {

        public static OperationParameter newInstance(
            String operationQualifier, 
            Pattern validForPattern
        ) throws ServiceException{
            return new OperationParameter(
                operationQualifier, 
                validForPattern
            );
        }        
           
        private OperationParameter(
            String operationQualifier, 
            Pattern validForPattern
        ) throws ServiceException {
            this.validForPattern = validForPattern;
            if (operationQualifier.indexOf(";") > 0) {
                _parameters.put(
                    "qualifier", 
                    operationQualifier.substring(0,operationQualifier.indexOf(";"))
                );
                _parameters.put(
                    "operationQualifier", 
                    operationQualifier
                );
            
                parse(operationQualifier);
            }
            else {
                _parameters.put("qualifier", operationQualifier);
            }
        }
        
        public static boolean hasStateParameter(String operationQualifier) {
             return hasParameter(operationQualifier, State_1_Attributes.OP_STATE);
        }
        
        public static boolean hasPeriodParameter(String operationQualifier) {
             return hasParameter(operationQualifier, State_1_Attributes.OP_VALID_FROM) ||
                hasParameter(operationQualifier, State_1_Attributes.OP_VALID_TO);
        }
        
        public boolean isStateOperation() {
            return hasStateParameter(getOperationQualifier());
        }
        
        public boolean isPeriodOperation() {
            return hasPeriodParameter(getOperationQualifier());
        }

        public String getQualifier() {
            return (String)_parameters.get("qualifier");
        }
        
        public String getOperationQualifier() {
            return (String)_parameters.get("operationQualifier");
        }
        
        public String getDate() {
            return (String)_parameters.get("stateAtDate");
        }
        
        public String getStateNumber() {
            return (String)_parameters.get("stateNumber");
        }
 
        public boolean isFirst() {
            return _parameters.containsKey(State_1_Attributes.OP_VAL_FIRST);
        }
        
        public boolean isLast() {
            return _parameters.containsKey(State_1_Attributes.OP_VAL_LAST);
        }
        public boolean isUndef() {
            return _parameters.containsKey(OP_STATE_UNDEF);
        }
        public String getValidFrom() {
            return (String)_parameters.get(State_1_Attributes.OP_VALID_FROM);
        }
        
        public String getValidTo() {
            return (String)_parameters.get(State_1_Attributes.OP_VALID_TO);
        }
        
        public boolean skipMissingStates() {
            return _parameters.containsKey(State_1_Attributes.OP_SKIP_MISSING_STATES);
        }
        
        /**
         * Returns a new path cleaned from all known operationParameters.
         * 
         * @return
         */
        static public Path removeAllOperationParameters(Path path) {
            Path reply = path.getPrefix(1);
            for (int i = 1; i < path.size(); i++) {
                String component = path.get(i);
                int idx = component.indexOf(';');
                int pos = 0;
                if (idx>=0) {
                    StringBuilder newComponent = new StringBuilder();
                    do {
                        int idx1 = -1;
                        String knownOp = null;
                        newComponent.append(component.substring(pos,idx));
                        for (Iterator k = _knownOperationParameters.iterator(); k.hasNext() && idx1!=(idx+1);) {
                            knownOp = (String) k.next();
                            idx1 = component.indexOf(knownOp,idx);
                        }
                        if (idx1!=(idx+1)) {
                        	// found a knownOp but further down the string or none at all
                            pos = idx + 1;
                            newComponent.append(';');
                        } else {
                        	// found a knownOp; this must be ignored
                            idx = component.indexOf(';',idx1 + knownOp.length());
                            pos = (idx>0) ? idx : component.length();
                        }
                        idx = component.indexOf(';',pos);
                    } while (idx>=0);
                    newComponent.append(component.substring(pos));
                    reply.add(newComponent.toString());
                } else {
                    reply.add(component);
                }
            }
            return reply;
        }
        
        
        /**
         * Builds a map of the parameters contained in the qualifier.
         * <p>
         * The map holds the parameters name and its value. For the parameters 
         * validFrom and validTo, the keyword <i>ever</i> is mapped to null. 
         * <p>
         * Invalid combinations of operation parameters lead to an exception.
         * 
         * @param qualifier
         * @param parameter
         * @return
         */
        private void parse(
            String operationQualifier
        ) throws ServiceException {
            
            StringTokenizer tok = new StringTokenizer(operationQualifier, ";=", true);
        
            String paramForAccess = null;
            String paramForUpdate = null;
        
            BasicException.Parameter error = null;
        
            String param = null;
            String token = null;
            String value = null;
            while (tok.hasMoreTokens() && error == null) {
                token = tok.nextToken();
                param = null;
                value = null;
            
                if (token.equals(";") && tok.hasMoreTokens()) {
                    param = token = tok.nextToken();
                
                    if (tok.hasMoreTokens()) {
                        token = tok.nextToken();
                    }
                
                    if (token.equals("=") && tok.hasMoreTokens()) {
                        value = token = tok.nextToken();
                    }
                
                    if (param != null ) {
                        // start of next parameter, treat old
    
                        if (param.equals(State_1_Attributes.OP_STATE)) {
                            paramForAccess = State_1_Attributes.OP_STATE;
                            // check value
                            if (value.equals(State_1_Attributes.OP_VAL_FIRST)) {
                                _parameters.put(State_1_Attributes.OP_VAL_FIRST, null);
                            }
                            else if (value.equals(State_1_Attributes.OP_VAL_LAST)) {
                                _parameters.put(State_1_Attributes.OP_VAL_LAST, null);
                            }
                            else if (validForPattern.matcher(value).matches()) {
                                _parameters.put("stateAtDate", value);
                            }
                            else if (NUMBER_PATTERN.matcher(value).matches())
                            {
                                _parameters.put("stateNumber", value);
                            }
                            else {
                                // illegal value
                                error = new BasicException.Parameter(State_1_Attributes.OP_STATE, value);
                            }
                        }
                        else if (param.equals(State_1_Attributes.OP_VALID_FROM)) {
                            paramForUpdate = State_1_Attributes.OP_VALID_FROM;
                            // check value
                            if (
                                !value.equals(State_1_Attributes.OP_VAL_EVER) && 
                                !validForPattern.matcher(value).matches()
                            ) {
                                error = new BasicException.Parameter(State_1_Attributes.OP_VALID_FROM, value);
                            }
                        }
                        else if (param.equals(State_1_Attributes.OP_VALID_TO)) {
                            paramForUpdate = State_1_Attributes.OP_VALID_TO;
                            // check value
                            if (
                                !value.equals(State_1_Attributes.OP_VAL_EVER) && 
                                !validForPattern.matcher(value).matches()
                            ) {
                                error = new BasicException.Parameter(State_1_Attributes.OP_VALID_TO, value);
                            }
                        }
                        else if (param.equals(State_1_Attributes.OP_SKIP_MISSING_STATES)) {
                            paramForUpdate = State_1_Attributes.OP_SKIP_MISSING_STATES;
                            // check value
                            if (value != null) {
                                error = new BasicException.Parameter(State_1_Attributes.OP_SKIP_MISSING_STATES, value);
                            }
                        }
                        // don't care for additional params; they are just not ours.
                        
                        if (value != null 
                            && value.equals(State_1_Attributes.OP_VAL_EVER)
                        ) {
                            value = null;
                        }
                        _parameters.put(param, value);
                        
                    }
                }
            }
            
            // paramForAccess is not defined, but paramForUpdate is set
            if (paramForAccess == null && paramForUpdate != null) {
                _parameters.put(OP_STATE_UNDEF, null);
            }
        
            if (error != null 
            ) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER, 
                    new BasicException.Parameter[]{
                        error,
                        new BasicException.Parameter(
                            "operation", operationQualifier
                        )
                    },
                    "Illegal operation parameter."
                );            
            }

        }
    
        /**
         * Check if the operation parameter is present.
         * 
         * @param qualifier
         * @param parameter
         * @return
         */
        private static boolean hasParameter(
            String operationQualifier,
            String parameter
        ) {
            return operationQualifier != null 
                && operationQualifier.indexOf(";" + parameter + "=") > 0;
        }
        
        String _qualifier = null;
        String _operationString = null;
        Map _parameters = new HashMap();
        private final Pattern validForPattern;
        
        static Set _knownOperationParameters = new HashSet();
        
        // static initialisation
        static {
            _knownOperationParameters.add(State_1_Attributes.OP_SKIP_MISSING_STATES);
            _knownOperationParameters.add(State_1_Attributes.OP_STATE + "=");
            _knownOperationParameters.add(State_1_Attributes.OP_VALID_FROM + "=");
            _knownOperationParameters.add(State_1_Attributes.OP_VALID_TO + "=");
        }
        
    }

    // --------------------------------------------------------------------------

    /**
     * helper class for building statefulReferencePaths
     */
    protected static class PathNType {

        public static PathNType newInstance(
            Path path, 
            ModelElement_1_0 type
        ){
            return new PathNType(
                path, 
                type,
                new HashSet()
            );
        }
        
        public static PathNType newInstance(
            Path path, 
            ModelElement_1_0 type,
            Set visitedElements
        ){
            return new PathNType(
                path, 
                type,
                visitedElements
            );
        }

        private PathNType(
        	Path path, 
        	ModelElement_1_0 type,
        	Set visitedElements
        ) {
            this.path = path;
            this.type = type;
            this.visitedElements = visitedElements;
        }
       
        public ModelElement_1_0 type() {
            return this.type;
        }
      
        public Path path() {
            return this.path;
        }
        
        public Set visitedElements() {
        	return Collections.unmodifiableSet(visitedElements);
        }
      
      	private Set visitedElements;
        private ModelElement_1_0 type;
        private Path path;
        
    }
    
    
    // --------------------------------------------------------------------------
    protected class UpdateSpec {
        
        protected UpdateSpec(
            ServiceHeader header,
            DataproviderRequest request, 
            Pattern validForPattern
        ) throws ServiceException {
            this(
                header, 
                request, 
                validForPattern, 
                request.path().getBase()
            );
        }

        protected UpdateSpec(
            ServiceHeader header,
            DataproviderRequest request, 
            Pattern validForPattern, 
            String base
        ) throws ServiceException {
            // only the operation parameters in the base of the path have any influence
            _operationParameter = OperationParameter.newInstance(
                base, 
                validForPattern
            );
            
            _objectPath = OperationParameter.removeAllOperationParameters(request.path()); //.getParent().getChild(_operationParameter.getQualifier());
            
            _at = getRequestedAt(header, request);
            if (request.object().getValues(SystemAttributes.MODIFIED_BY) != null &&
                !request.object().getValues(SystemAttributes.MODIFIED_BY).isEmpty()
            ) {
                _by = request.object().getValues(SystemAttributes.MODIFIED_BY);
            }
            else {
                _by = header.getPrincipalChain();
            }
            
            if (_operationParameter.isPeriodOperation()) {
                _isValidPeriodSet = true;
                _validFrom = _operationParameter.getValidFrom();
                _validTo = toExclusiveValidTo(_operationParameter.getValidTo());
            }
            else if (_operationParameter.isStateOperation()){
                // 
            }
            else {
                // no settings as operation parameters in the qualifier
                // try reading validFrom, validTo from attributes:
            
                if (request.object().getValues(validFromAttribute()) != null) {
                    _isValidPeriodSet = true;
                    if (!request.object().getValues(validFromAttribute()).isEmpty()) {
                        _validFrom = (String) 
                            request.object().values(validFromAttribute()).get(0);
                    }
                }
                
                SparseList validToAttribute = request.object().getValues(validToAttribute()); 
                if (validToAttribute != null) {
                    _isValidPeriodSet = true;
                    if (!validToAttribute.isEmpty()) {
                        _validTo = toExclusiveValidTo( 
                            (String) validToAttribute.get(0)
                        );
                    }
                }
            }
            
        }
        
        public String getModificationDate() {
            return _at;
        }
        
        public List getModificationPrincipal() {
            return _by;
        }
        
        
        /**
         * 
         * @return
         */
        public boolean skipMissingStates() {
            return _operationParameter.skipMissingStates();
        }

       
        public boolean isValidPeriodSet() {
            return _isValidPeriodSet;
        }
        
        public boolean isStateOperation() {
            return _operationParameter.isStateOperation();
        } 
        
        public boolean isPeriodOperation() {
            return _operationParameter.isPeriodOperation();
        }
        
        /**
         * @return
         */
        public String getValidFrom() {
            return _validFrom;
        }
        
        /**
         * @return
         */
        public String getValidTo() {
            return _validTo;
        }

        
        public OperationParameter getOperationParameter() {
            return _operationParameter;
        }
        
        /**
         * Path to object; without any operation parameters.
         * 
         * @return
         */
        public Path getObjectPath() {
            return _objectPath;
        }
        
        
        /**
         * In case of state operation request, validFrom, validTo is known only
         * after reading the state. It must be set before continuing.
         * 
         * @param validFrom
         * @param validTo
         */
        public void setValidPeriod(
            String validFrom, 
            String validTo
        ) throws ServiceException {
            if (_operationParameter.isStateOperation()) {
                _validFrom = validFrom;
                _validTo = validTo;
                _isValidPeriodSet = true;
            }
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter(
                            "qualifier", 
                            _operationParameter.getOperationQualifier())
                    },
                    "setting of valid period is only possible in case of state operation"
                );
            }
        }

        private boolean _isValidPeriodSet = false;
        
        private Path _objectPath;
        private String _validFrom;
        private String _validTo;
        private OperationParameter _operationParameter;
        private String _at;
        private List _by;
    }   

    protected boolean isShowDBEnabled(){
        return this.enableShowDB && SysLog.isTraceOn();
    }
    
    protected abstract Pattern validForPattern();
    
    protected abstract String stateTypeName();

    protected abstract String getRequestedFor(
        ServiceHeader header
    );

    protected abstract String getRequestedAt(
        ServiceHeader header
    );

    protected abstract String getRequestedAt(
        ServiceHeader header,
        DataproviderRequest request
    );
    
    protected abstract boolean excludingEnd();
    
    protected abstract String validFromAttribute();
    
    protected abstract String validToAttribute();
    
    protected abstract String toExclusiveValidTo(
        String validTo
    ) throws ServiceException;

    protected abstract String toModelledValidTo(
        String validTo
    ) throws ServiceException;
    
    // --------------------------------------------------------------------------
    protected String getRequestedFor(
        ServiceHeader header,
        DataproviderRequest request
    ){
        String requestedFor = header.getRequestedFor();
        if (requestedFor == null) {
            SparseList validFrom = request.object().getValues(validFromAttribute()); 
            if(validFrom != null) requestedFor = (String)validFrom.get(0);
        }
        return requestedFor;
    }

    // --------------------------------------------------------------------------
    /**
     * print a single object. May be extended to a more readable form in the
     * future.
     */
    private void showObject(DataproviderObject object) {
        if (object != null) {
            SysLog.trace("show object", object);
        }
    }
 
    // --------------------------------------------------------------------------
    /**
     * Show the content of the db. 
     * This provides some advantages over direct db access, as the objects
     * are shown in correct order and in a readable format.
     * 
     * @param header used for request
     * @param idPath path containing id
     */
    private void showDB(
        ServiceHeader header,
        DataproviderRequest request,
        Path idPath
    ) {
        
        try {          
        Path findPath = (Path)idPath.clone();
        String objectId = findPath.remove(idPath.size()-1);
        int p = objectId.indexOf(';');
        if (p > 0) {
            objectId = objectId.substring(0, p);
        }
        DataproviderRequest newRequest = 
            new DataproviderRequest(
                new DataproviderObject(findPath), 
                DataproviderOperations.ITERATION_START,
                new FilterProperty[] {
                    new FilterProperty( 
                        Quantors.THERE_EXISTS,
                        ID_ATTRIBUTE_NAME,
                        FilterOperators.IS_IN,
                        new Object[] {objectId}
                    )
                },
                0,
                Integer.MAX_VALUE, 
                Directions.ASCENDING,    // direction
                AttributeSelectors.ALL_ATTRIBUTES,
                null
            );
            
        newRequest.contexts().putAll(request.contexts());
        
        DataproviderReply reply = 
            super.find(
                header,
                newRequest
            );
        DataproviderObject[] objects = reply.getObjects();
        
        // first print all invalidated
        for (int i = 0; i < objects.length; i++) {
            if (objects[i].getValues(State_1_Attributes.INVALIDATED_AT) != null &&
                !objects[i].getValues(State_1_Attributes.INVALIDATED_AT).isEmpty()
            ) {
                showObject(objects[i]);
            }
        }
        
        ArrayList validObjects = new ArrayList();
        String candFrom = null;
        List actFromList = null;
        
        // now get the ones still valid
        for (int i = 0; i < objects.length; i++) {
            if (objects[i].getValues(State_1_Attributes.INVALIDATED_AT) == null ||
                objects[i].getValues(State_1_Attributes.INVALIDATED_AT).isEmpty()
            ) {
                if (objects[i].getValues(validFromAttribute()) == null ||
                    objects[i].getValues(validFromAttribute()).isEmpty()
                ) {
                    validObjects.add(0,objects[i]);
                }
                else {
                    actFromList = objects[i].getValues(validFromAttribute());
                    candFrom = (String) objects[i].getValues(validFromAttribute()).get(0);
                    // search for place to insert
                    boolean found = false;
                    int pos = -1;
                    
                    for (int j = 0; j < validObjects.size() && !found; j++) {
                        actFromList = ((DataproviderObject)validObjects.get(j)).getValues(validFromAttribute());
                        if (
                            actFromList != null 
                            &&
                            !actFromList.isEmpty()
                            &&
                            candFrom.compareTo((String) actFromList.get(0)) < 0
                        ) {
                             found = true;
                             pos = j;
                        }
                    }
                    if (pos == -1) {
                        validObjects.add(objects[i]); // add at end
                    }
                    else {
                        validObjects.add(pos, objects[i]);
                    }
                }
            }
        }
        
        // now print the objects:
        for (int j = 0; j < validObjects.size(); j++) {
            showObject((DataproviderObject)validObjects.get(j));
        }

        // now get the root:
        newRequest = 
            new DataproviderRequest(
                new DataproviderObject(idPath), 
                DataproviderOperations.OBJECT_RETRIEVAL,
                null,
                0,
                Integer.MAX_VALUE, 
                Directions.ASCENDING,    // direction
                AttributeSelectors.ALL_ATTRIBUTES,
                null
            );
        newRequest.contexts().putAll(request.contexts());
        

        reply = super.get(header, newRequest);
        
        showObject(reply.getObject());
        
        } 
        catch (Exception e) {
            // just to avoid exceptions here
        }
    }
        
        

    // --------------------------------------------------------------------------
    /**
     * Read the model and prepare a map of reference paths leading to objects
     * with state. 
     * 
     * @see Layer_1_0#deactivate()
     */
    @SuppressWarnings("deprecation")
    public void activate(
        short id, 
        Configuration configuration, 
        Layer_1_0 delegation
    ) throws Exception {       
        super.activate(id, configuration, delegation);
        
        List models = configuration.values(SharedConfigurationEntries.MODEL);
        if(models.size() > 0) {
          _model = (Model_1_0)models.get(0);
        }
        else {
          throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.INVALID_CONFIGURATION, 
            null,
            "A model must be configured with options 'modelPackage' and 'packageImpl'"
          );
        }
        
        if (configuration.containsEntry(LayerConfigurationEntries.ENABLE_HOLES_IN_OBJECT_VALIDITY)) {
            enableHolesInValidity = configuration.isOn(
                LayerConfigurationEntries.ENABLE_HOLES_IN_OBJECT_VALIDITY
            );
        }
        else {
            enableHolesInValidity = true;
        }
        SysLog.info(
            "configuration " + LayerConfigurationEntries.ENABLE_HOLES_IN_OBJECT_VALIDITY, 
            Boolean.valueOf(enableHolesInValidity)
        );

        this.enableDisjunctStateCreation = configuration.isOn(
            LayerConfigurationEntries.ENABLE_DISJUNCT_STATE_CREATION
        );
        SysLog.info(
            "configuration " + LayerConfigurationEntries.ENABLE_DISJUNCT_STATE_CREATION, 
            Boolean.valueOf(this.enableDisjunctStateCreation)
        );
        
        this.enableShowDB = configuration.isOn(
            LayerConfigurationEntries.ENABLE_SHOW_DB
        );
        SysLog.info(
            "configuration " + LayerConfigurationEntries.ENABLE_SHOW_DB, 
            Boolean.valueOf(this.enableShowDB)
        );
        
        String defaultState = configuration.getFirstValue(LayerConfigurationEntries.DEFAULT_STATE);
        if(!DEFAULT_STATE_VALUES.contains(defaultState)) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.INVALID_CONFIGURATION,
            new BasicException.Parameter[]{
                new BasicException.Parameter(
                    "value", 
                    defaultState
                ),
                new BasicException.Parameter(
                    "acceptable", 
                    DEFAULT_STATE_VALUES.subList(1, DEFAULT_STATE_VALUES.size() - 1)
                )
            },
            "Invalid " + LayerConfigurationEntries.DEFAULT_STATE + " configuration value"
        );
        this.defaultsToInitialState = LayerConfigurationEntries.INITIAL_STATE.equals(defaultState);
        SysLog.info(
            LayerConfigurationEntries.INITIAL_STATE,
            this.defaultsToInitialState ? LayerConfigurationEntries.INITIAL_STATE : LayerConfigurationEntries.CURRENT_STATE
        );
        
        List disableHistoryReferencePatterns = new ArrayList();
        for(
            Iterator i = configuration.values(LayerConfigurationEntries.DISABLE_HISTORY_REFERENCE_PATTERN).populationIterator();
            i.hasNext();
        ) disableHistoryReferencePatterns.add(new Path((String)i.next()));
        for(
            Iterator i = configuration.values(LayerConfigurationEntries.COMPATIBILITY_DISABLE_HISTORY_REFERENCE_PATTERN).populationIterator();
            i.hasNext();
        ) disableHistoryReferencePatterns.add(new Path((String)i.next()));
        this.disableHistoryReferencePatterns = Path.toPathArray(disableHistoryReferencePatterns);
        SysLog.info(
            LayerConfigurationEntries.DISABLE_HISTORY_REFERENCE_PATTERN,
            disableHistoryReferencePatterns
        );

        this.statefulReferencePaths = null;
        
    }

    /**
     * Doing lazy initialisation of the model.
     * 
     * This should help preventing reading instable model information.
     * 
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0#prolog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest[])
     */
    public void prolog(
        ServiceHeader header, 
        DataproviderRequest[] requests
    ) throws ServiceException {
        super.prolog(header, requests);
        
        if (this.statefulReferencePaths == null) {
            this.statefulReferencePaths = iterateModelForstatefulReferencePaths(
                null,
                _model.getDereferencedType(AUTHORITY_TYPE_NAME)
            );
        }        
    }
    
    
    /**
     * Test only! 
     * 
     * Test iteration of the model for stateful paths. 
     */
    public Map testModelIteration(Model_1_0 model)throws ServiceException{
    	_model = model;
    	Map result = iterateModelForstatefulReferencePaths(
                null,
                _model.getDereferencedType(AUTHORITY_TYPE_NAME)
            );

    	return result;
    }

    // --------------------------------------------------------------------------
    /**
     * @see Layer_1_0#deactivate()
     */
    public void deactivate() throws Exception {
        super.deactivate();
        this.statefulReferencePaths = null;
    }


    /** 
     * a < b --> -1
     * a > b --> 1
     * a == b --> 0
     * <p>
     * null is smallest value
     * 
     * @param a
     * @param b
     * @return
     */    
    private int compareAtStart(Object a, Object b) {
        return compareStringDate(a, b, true);
    }
    
    
    /** 
     * a < b --> -1
     * a > b --> 1
     * a == b --> 0
     * <p>
     * null is largest value
     * 
     * @param a
     * @param b
     * @return
     */    
    private int compareAtEnd(Object a, Object b) {
        return compareStringDate(a, b, false);
    }
    
    // --------------------------------------------------------------------------
    /**
     * compare two dates in their String representation.
     * 
     * nullSmall steers the descision if one of the values is null: if true, 
     * a null value is regarded as the smallest possible, if false, it's the
     * largest possible.
     * 
     * a < b --> -1
     * a > b --> 1
     * a == b --> 0
     * 
     */
    private int compareStringDate(
        Object _a,
        Object _b,
        boolean nullSmall
    )  {
        Object a = _a;
        Object b = _b;
        String aDate = null;
        String bDate = null;
        
        if (a != null && a instanceof SparseList) {
            SparseList aList = (SparseList) a;
            if (aList.isEmpty()) {
                // the same as it where null
                a = null;
            }
            else {
                aDate = (String) aList.get(0);
            }
        }
        if (b != null && b instanceof SparseList) {
            SparseList bList = (SparseList) b;
            if (bList.isEmpty()) {
                // the same as it where null
                b = null;
            }
            else {
                bDate = (String) bList.get(0);
            }
        }

        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            if (nullSmall) {
                return -1;
            }
            else {
                return 1;
            }
        }
        if (b == null) {
            if (nullSmall) {
                return 1;
            }
            else {
                return -1;
            }
        }

        if (a instanceof String) {
            aDate = (String) a;
        }

        if (b instanceof String) {
            bDate = (String) b;
        }
        
        return aDate.compareTo(bDate);
    }


    // --------------------------------------------------------------------------
    /** 
     * is the type supplied derived from the class typeName?
     */
    private boolean isDerived(
        ModelElement_1_0 type,
        String typeName
    ) {
        boolean isDerived = false; 
        for (Iterator i = type.values("allSupertype").iterator();
            i.hasNext() && !isDerived;
        ) {
            isDerived = 
                ((Path) i.next()).getBase().equals(typeName);
        }
        return isDerived;
    }

    // --------------------------------------------------------------------------
    /** 
     * Find all the classes which are reachable from the startClass or 
     * its subclasses through a reference. Add those classes to the 
     * reachableClasses collection, together with the path leading to the new 
     * class.
     * 
     * @param startPath  path up to the startClass 
     * @param startClass  class from which the references have to be followed
     * @param reachableClasses  list of all class and Path, new found ones
     *                           get added.
     */
    private void reachableClasses(
    	PathNType startPoint,
        ArrayList reachableClasses
    ) throws ServiceException  {
        Path instancePath                = null;
        Path subTypeModelPath            = null;
        ModelElement_1_0 subTypeObj      = null;
        ModelElement_1_0 contentObj      = null;
        Path refendPath                  = null;
        ModelElement_1_0 refend          = null;
        Path nextClassTypePath           = null;
        ModelElement_1_0 nextClassType   = null;
        String pathComponent             = null;
        
        ModelElement_1_0 type = startPoint.type();
        Path path = startPoint.path();
        
		// SysLog.trace("starting from path: " + path + " class: " + type.getValues("name"));
        
        // for each subtype of startClass (this contains also the startClass itself)
        for (Iterator subIter = type.values("allSubtype").iterator();
            subIter.hasNext();
        ) {
            // Path 
            instancePath = path == null ? null : new Path(path);
            // Path 
            subTypeModelPath = (Path) subIter.next();
            
            //DataproviderObject 
            subTypeObj = this._model.getDereferencedType(subTypeModelPath);

            // for each contained (attribute or reference)
            for (Iterator contentIter = subTypeObj.values("feature").iterator();
                contentIter.hasNext();
            ) {
                // DataproviderObject 
                contentObj = this._model.getElement(contentIter.next());
//                SysLog.trace("contentObj.identity: ", contentObj.values(SystemAttributes.OBJECT_IDENTITY));
//                SysLog.trace("contentObj.path: ", contentObj.path());
       
                // check if it is a reference
                // and make sure it is the first visited
                if (contentObj.values(SystemAttributes.OBJECT_CLASS).get(0).
                        equals("org:omg:model1:Reference") 
                    && !startPoint.visitedElements().contains(contentObj)
                ) {
                    // Path 
                    refendPath = (Path) contentObj.values("referencedEnd").get(0);
                    
                    // DataproviderObject 
                    refend = this._model.getElement(refendPath);
                    
                    // only interested in aggregated objects
                    if (AggregationKind.COMPOSITE.equals(
                            refend.values("aggregation").get(0)
                        )
                        ||
                        // need shared aggregations because they may occur in an
                        // access path and must be recognized as stated.
                        AggregationKind.SHARED.equals(
                            refend.values("aggregation").get(0)
                        )
                    ) {
                        nextClassTypePath = (Path) refend.values("type").get(0);
                        nextClassType = this._model.getDereferencedType(nextClassTypePath);
                        pathComponent = (String) refend.values("name").get(0);
                        
                        // exclude which have self reflective shared relations
                        // TODO: these classes must be removed from models because
                        //       they violate modeling constraints
                        //       For now they are left unchanged to prevent
                        //       a change in behavior.
                        Object nextClassName = nextClassType.getValues("qualifiedName").get(0);
                        if(
                          !(ROLE_TYPE_NAME.equals(nextClassName)) &&
                          !(STATE_TYPE_NAME.equals(nextClassName)) &&
						  !(VIEW_TYPE_NAME.equals(nextClassName))
                        ){         
							if (!startPoint.visitedElements().contains(nextClassType)) {
	                            if (path == null) {
	                                nextClassTypePath = new Path(pathComponent);
	                            }
	                            else {
	                                nextClassTypePath = new Path(instancePath);
	                                nextClassTypePath.add(pathComponent);
	                            }
	                            Set refsVisited = new HashSet(startPoint.visitedElements());
	                            refsVisited.add(contentObj);
	                            refsVisited.add(nextClassType);
	                            reachableClasses.add(PathNType.newInstance(nextClassTypePath, nextClassType, refsVisited));
	                        }  
                        }                  
                    }
                }
            }
        }           
    }

    
    // --------------------------------------------------------------------------
    /**
     * Prepare a map of reference paths leading to state enabled classes.
     * A path leading to an class which itself is not state enabled, but one of 
     * its subclasses is, gets added to the map.
     * 
     * @param path  starting path, may be null
     * @param start type object to start with
     * 
     */
    private HashMap iterateModelForstatefulReferencePaths(
        final Path path,
        final ModelElement_1_0 start
    ) throws ServiceException {
    	HashMap statePaths = new HashMap();
        ArrayList openClassesAndPaths = new ArrayList();
        HashSet visitedPaths = new HashSet();
        HashSet visitedClasses = new HashSet();
        PathNType startPoint = PathNType.newInstance(path, start);
        
        /* int modelSize = */ _model.getContent().size();
        
        openClassesAndPaths.add(startPoint);
        
        while (openClassesAndPaths.size() > 0) {
            startPoint = (PathNType) openClassesAndPaths.remove(0);
            if (startPoint.path() != null &&
                startPoint.path().size() > PATHLENGTH_THRESHOLD
            ){
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE, 
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("path", startPoint.path()),
                        new BasicException.Parameter("PATHLENGTH_THRESHOLD", PATHLENGTH_THRESHOLD)
                    },
                    "Encountered long path while traversing model for stateful paths. Most Probably the algorithm" +
                    " is looping. Please check your models for potential loops. " 
                );	            	
            }
            
            
            // can't put the two statements in a single or'ed one, because
            // the evaluation stops after the first true
            boolean pathNotVisited = visitedPaths.add(startPoint.path());
            boolean classNotVisited = visitedClasses.add(startPoint.type());
            if (pathNotVisited || classNotVisited) {
                if (isDerived(startPoint.type(), stateTypeName())) {
                    statePaths.put(startPoint.path(), startPoint.type());
                }
                reachableClasses(startPoint, openClassesAndPaths);
            }
        }
    	
        return statePaths;
     }
     
     // -------------------------------------------------------------------------
     /**
      * For the operations create, modify, replace, remove validState and 
      * historyState are not supported.
      */
     private void assertValidPathForChanges(
        Path path
     ) throws ServiceException {
        int size = path.size();
        if (path.get(size-2).equals(State_1_Attributes.REF_HISTORY) ||
            path.get(size-2).equals(State_1_Attributes.REF_VALID) ||
            path.get(size-2).equals(State_1_Attributes.REF_STATE)
        ) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                new BasicException.Parameter[]{
                    new BasicException.Parameter("path", path),
                    new BasicException.Parameter("component", State_1_Attributes.REF_HISTORY),
                    new BasicException.Parameter("component", State_1_Attributes.REF_VALID),
                    new BasicException.Parameter("component", State_1_Attributes.REF_STATE)
                },
                "No changes (create, modify, replace or remove) allowed with a path containing those components."
            );
        }
    }
    
    
    /**
     * Assert that the client is aware of states to change data in any way.
     *
     */
    private void assertStateAwareRequest(
        UpdateSpec spec,
        DataproviderObject requestObject
    ) throws ServiceException {
        if (!spec.isValidPeriodSet() 
            && !spec.isPeriodOperation()
            && !spec.isStateOperation()
        ) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED, 
                new BasicException.Parameter[]{
                    new BasicException.Parameter("path", requestObject.path()),
                },
                "Trying to change stated data without state information. (Must set object_validFrom/To or some OperationParameter)" 
            );            
        }          
    }
    
    
    /**
     * Assert that all the states are within the boundaries of the request. 
     * <p> 
     * if allowAdjacent is true, states which end when the request starts or 
     * states which start when the request ends are allowed.
     * <p>
     * This is quite an important invariant for most of the algorithms used,
     * thats why it can be checked here.
     *
     */
    private void assertStatesWithinRequest(
        List states,
        UpdateSpec spec,
        boolean allowAdjacent
    ) throws ServiceException {
        boolean inside = true;
        
        for (Iterator s = states.iterator(); s.hasNext();) {
            DataproviderObject state = (DataproviderObject) s.next();
            String stateFrom = readNullableStringValue(state, validFromAttribute());
            String stateTo = toExclusiveValidTo(readNullableStringValue(state, validToAttribute()));
            
            if (allowAdjacent &&
                (( 
                    stateTo != null &&   // for ever
                    compareAtStart(spec.getValidFrom(), stateTo) > 0
                ) 
                ||
                (
                    stateFrom != null &&  // since ever
                    compareAtEnd(spec.getValidTo(), stateFrom) < 0
                ))
            ) {
                inside = false;
            }
            else if (!allowAdjacent &&
                (( 
                    stateTo != null &&   // for ever
                    compareAtStart(spec.getValidFrom(), stateTo) > 0
                ) 
                ||
                (
                    stateFrom != null &&  // since ever
                    compareAtEnd(spec.getValidTo(), stateFrom) < 0
                ))
            ) {
                inside = false;
            }
            
            if (!inside) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("state", state.path()),
                        new BasicException.Parameter("request.validFrom", spec.getValidFrom()),
                        new BasicException.Parameter("request.validTo", spec.getValidTo()),
                        new BasicException.Parameter("state.validFrom", stateFrom),
                        new BasicException.Parameter("state.validTo", stateTo)
                    },
                    "state outside of requested valid period. This is an internal error." 
                );            
            }          
        }
    }
    
    
    // --------------------------------------------------------------------------
    /**
     * Read string value denoted from object and return null if it is not present
     * or set to null.
     * 
     * @return
     */
    private String readNullableStringValue(
        DataproviderObject object, 
        String attribute
    ) { 
        SparseList values = object.getValues(attribute);
        return values == null ? null : (String)values.get(0);
    }
    
 
        
    // --------------------------------------------------------------------------
    /**
     * Check that validFrom, validTo are are in sequence (validFrom <= validTo)
     * and that requestedFor is null or between validFrom, validTo.
     * Define the time used for all changes through the modified at of the 
     * object.
     * 
     * @param spec containing the time and the user.
     */
    private void assertValidDates(
        UpdateSpec spec
    ) throws ServiceException {
        // check validFrom <= validTo               
        if (spec.getValidFrom() != null && 
            spec.getValidTo() != null  && 
            spec.getValidFrom().compareTo(spec.getValidTo()) > 0
        ) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                new BasicException.Parameter[]{
                    new BasicException.Parameter("validFrom", spec.getValidFrom()),
                    new BasicException.Parameter("validTo", spec.getValidTo())
                },
                "validFrom after validTo."
            );                  
        }
    }
        

        
   
    // --------------------------------------------------------------------------
    /**
     * Get the class description for the object.
     * 
     * @param object   object to get class for
     * 
     * @return class description from model, may be null if object_class entry 
     *                in object is missing.
     * 
     * @throws ServiceException if class not present in model
     */
    private ModelElement_1_0 getObjectClass(
        DataproviderObject object
    ) throws ServiceException {
        ModelElement_1_0 objClass = null;
        if (object != null &&
            object.getValues(SystemAttributes.OBJECT_CLASS) != null
        ) {
            String objectClassName = 
                (String)object.getValues(SystemAttributes.OBJECT_CLASS).get(0);
            
            if (objectClassName != null) {
                // get the class from model
                objClass = this._model.getDereferencedType(
                    objectClassName
                );
            }
            
            if (objClass == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("object", object)
                    },
                    "class not found"
                );
            }   
        }
        return objClass;
    }
    
    // --------------------------------------------------------------------------    
    /**
     * Tells whether the find request is for states 
     * 
     * @param   path 
     * 
     * @return  <code>true</code> if the find request is for states
     */
    private boolean isStateRequest(
        DataproviderRequest request
    ){
        for(
            int i = 0, iLimit = request.attributeFilter().length;
            i < iLimit;
            i++
        ){
            FilterProperty filter = request.attributeFilter()[i];
            if (
                Quantors.THERE_EXISTS == filter.quantor() &&
                State_1_Attributes.STATED_OBJECT.equals(filter.name()) &&
                filter.operator() == (
                    filter.values().isEmpty() ? FilterOperators.IS_NOT_IN : FilterOperators.IS_IN  
                )
            ) {
                if(!filter.values().isEmpty()) request.addAttributeFilterProperty(
                    new FilterProperty(
                        filter.quantor(),
                        SystemAttributes.OBJECT_IDENTITY,
                        filter.operator(),
                        toXRIs(filter.values())
                    )
                );
                return true;
            }
        }
        return false;
    }

    static String[] toXRIs (
        List paths
    ){
        String[] xris = new String[paths.size()];
        for(
            int i = 0;
            i < xris.length;
            i++
        ){
            Object path = paths.get(i);
            xris[i] = (
                path instanceof Path ? (Path)path : new Path(path.toString())
            ).toXri();
        }
        return xris;
    }
    
    // --------------------------------------------------------------------------    
    /**
     * Tells whether history states are disabled for a given path 
     * 
     * @param   path 
     * 
     * @return  <code>true</code> if history states are disabled for the given 
     * object or reference path
     */
    private boolean isHistoryDisabled(
        Path path
    ){
        Path reference = path.size() % 2 == 0 ? path : path.getParent();
        for(
            int i = 0;
            i < this.disableHistoryReferencePatterns.length;
            i++
        ) if (
            reference.isLike(this.disableHistoryReferencePatterns[i])            
        ){
            SysLog.trace("History state disabled", path);
            return true;
        }
        if(!reference.isEmpty()) {
            String base = reference.getBase();
            if (
                State_1_Attributes.REF_HISTORY.equals(base) ||
                State_1_Attributes.REF_VALID.equals(base) ||
                State_1_Attributes.REF_STATE.equals(base)
            ) {
                reference = reference.getPrefix(reference.size() - 2);
                for(
                    int i = 0;
                    i < this.disableHistoryReferencePatterns.length;
                    i++
                ) if (
                    reference.isLike(this.disableHistoryReferencePatterns[i])            
                ){
                    SysLog.trace("History state disabled", path);
                    return true;
                }
            }
        }
        SysLog.trace("History state enabled", path);
        return false;
    }
    
    // --------------------------------------------------------------------------
    /** 
     * Is the object derived from State?
     * 
     * @param   object  object to find out if it is derived
     * 
     * @return  true if it is derived
     */
    private boolean isStateful(
        DataproviderObject object
    ) throws ServiceException {
        boolean isStateful = false; 
        ModelElement_1_0 objClass = getObjectClass(object);

        if (objClass != null) {
            for (
                Iterator i = objClass.values("allSupertype").iterator();
                i.hasNext() && !isStateful;
            ) {
                isStateful = ((Path)i.next()).getBase().equals(stateTypeName());
            }
        }
        
        // check for invalid attempts to have state enabled and not state
        // enabled classes at the same path:
        Path refPath = new Path(object.path());
        cutStateSpecifiersFromPath(refPath);
        refPath = getReferencePath(refPath);
        
        if(SysLog.isTraceOn()) SysLog.trace(
            "stateful detail. isStateful=" + isStateful + "; refPath=" + refPath, 
            this.statefulReferencePaths.keySet()
        );

        if(this.statefulReferencePaths.containsKey(refPath) != isStateful) {
            throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("object.path", object.path()),
                        new BasicException.Parameter("class derived from state", isStateful)
                    },
                    "Trying to mix state enabled and non enabled classes at the same path."
                );
            }   
             
        
        return isStateful; 
    }     
        
    // --------------------------------------------------------------------------
    private String cutStateSpecifiersFromPath(
        Path path
    ) {
        int pos = path.size()-1;
        
        if (
            path.get(pos).equals(State_1_Attributes.REF_HISTORY) ||
            path.get(pos).equals(State_1_Attributes.REF_VALID) ||
            path.get(pos).equals(State_1_Attributes.REF_STATE)
        ) {
            path.remove(pos);
            pos--;
        }
        else if (
            path.get(pos-1).equals(State_1_Attributes.REF_HISTORY) ||
            path.get(pos-1).equals(State_1_Attributes.REF_VALID) ||
            path.get(pos-1).equals(State_1_Attributes.REF_STATE)
        ) {
            path.remove(pos);
            path.remove(pos-1);
            pos = pos - 2;
        }
        
        return path.getBase();
    }
    
    
    // --------------------------------------------------------------------------
    /** 
     * It is a lifetime request if the request contains only validFrom, validTo
     * entries (plus all the technical ones).
     * 
     * @param object  the object to check
     */
    private boolean isLifetimeRequest(
        DataproviderObject object
    ) {
        for (
            Iterator i = object.attributeNames().iterator();
            i.hasNext(); 
        ){
            Object attributeName = i.next();
            if(
                !SystemAttributes.OBJECT_CLASS.equals(attributeName) &&
                !SystemAttributes.CREATED_AT.equals(attributeName) &&
                !SystemAttributes.CREATED_BY.equals(attributeName) &&
                !SystemAttributes.MODIFIED_AT.equals(attributeName) &&
                !SystemAttributes.MODIFIED_BY.equals(attributeName) &&
                !validFromAttribute().equals(attributeName) &&
                !validToAttribute().equals(attributeName)
            ) return false;
        }
        return true;       
    } 
    
    // --------------------------------------------------------------------------
    /** 
     * It is a request which contains its own search criteria for the history 
     * period.
     * Filters for attribtutes object_createdAt, object_updatedAt or 
     * object_invalidatedAt are history filters.  
     */
    private boolean hasHistoryFilter(
        DataproviderRequest request
    ) {
        boolean historyFilter = false;
        FilterProperty[] attrFilters = request.attributeFilter();
        for (int i = 0;
            i < attrFilters.length && historyFilter == false; 
            i++
        ) {                 
            if (attrFilters[i] != null && 
                attrFilters[i].name() != null
            ) {
                if (attrFilters[i].name().equals(SystemAttributes.CREATED_AT) ||
                    attrFilters[i].name().equals(SystemAttributes.MODIFIED_AT) ||
                    attrFilters[i].name().equals(State_1_Attributes.INVALIDATED_AT)
                ) {
                    historyFilter = true;
                }
            }
        }
        return historyFilter;
    }
       
    // --------------------------------------------------------------------------
    /** 
     * It is a request which contains its own search criteria for the validity 
     * period.
     * Filters for attributes object_validFrom or object_validTo are validity 
     * filters.
     */
    private boolean hasValidityFilter(
        DataproviderRequest request
    ) {
        boolean validityFilter = false;
        FilterProperty[] attrFilters = request.attributeFilter();
        for (int i = 0; 
            i < attrFilters.length && validityFilter == false; 
            i++
        ) {                 
            if (attrFilters[i] != null && 
                attrFilters[i].name() != null
            ) {
                if (attrFilters[i].name().equals(validFromAttribute()) ||
                    attrFilters[i].name().equals(validToAttribute()) 
                ) {
                    validityFilter = true;
                }
            }
        }
        return validityFilter;
    }
   
 
    // --------------------------------------------------------------------------
    /**
     * Create the state which is cut from a lifetime adjustment
     * 
     * @param atStart   if the start or the end of the object is cut
     * @param date      the date at which the cut must be executed
     * @param originalStates the objects to cut
     * 
     * @return may return null if the edges are already fitting
     * @throws ServiceException 
     */    
    private DataproviderObject createLifetimeEdgeState(
        boolean atStart, 
        String date, 
        DataproviderObject[] originalStates,
        UpdateSpec spec
    ) throws ServiceException  {
        DataproviderObject cutted = null; 
        // from the involved states get the one which is cut
        // by the new ValidFrom date
        for (
            int i = 0; 
            i < originalStates.length && cutted == null;
            i++
        ) {
            DataproviderObject state = originalStates[i];
            
            if (atStart) {
                int j = compareStringDate(
                    state.getValues(validToAttribute()),
                    date,
                    false
                );
                if ( this.excludingEnd() ? j > 0 : j >= 0 ) {
                    // the start of this state has to be reset.
                	cutted = createSplitState(
                        state, 
                        null, 
                        date, 
                        toExclusiveValidTo(readNullableStringValue(state, validToAttribute())), 
                        spec, 
                        false
                    );
                }
            }
            else {
                if (compareStringDate(
                        state.getValues(validFromAttribute()),
                        date,
                        false
                    ) < 0
                ) {
                    // the end of this state has to be reset. 
                	cutted = createSplitState(state, null, readNullableStringValue(state, validFromAttribute()), date, spec, false);
                }
            }                

        }
        return cutted;
    }
        
        
    // --------------------------------------------------------------------------
    /**
     * create the states needed at the object boundary to replace the 
     * existing ones and detect the states invalidated by the new boundary
     * 
     * @param header  original header
     * @param request original request for context
     * @param idPath  complete path to object
     * @param rootDate  valid date saved
     * @param requestDate  new boundary date
     * @param atStart   if the operation is at the start or the end
     * @param originalStates   list that gets filled with the states to be invalidated
     */
    private DataproviderObject createObjectBoundaryStates(
        ServiceHeader header, 
        DataproviderRequest request,
        Path idPath,
        SparseList rootDate,
        SparseList requestDate,
        boolean atStart,
        UpdateSpec spec,
        List originalStates  // out parameter
    ) throws ServiceException {
        DataproviderObject cut = null; 
        DataproviderObject[] originalStatesArray = null;
        String rootDateValue = rootDate == null ? null : (String) rootDate.get(0);
        String requestDateValue = requestDate == null ? null : (String) requestDate.get(0);
        if(!atStart){
            rootDateValue = toExclusiveValidTo(rootDateValue);
            requestDateValue = toExclusiveValidTo(requestDateValue);
        }
        // allow the requestData and rootDate to be equal. Because a value must 
        // be returned, just treat it as if it were inside the existing.
        if (
            (atStart && compareAtStart(requestDateValue, rootDateValue) > 0) 
            ||
            (!atStart && compareAtEnd(requestDateValue, rootDateValue) < 0) 
        ) {
            // it is a reduction of lifetime 
            String validFrom = null;
            String validTo = null;
            if (atStart) {
                validFrom = rootDateValue;
                validTo = requestDateValue;
            }
            else {
                validFrom = requestDateValue;   
                validTo = rootDateValue;
            }
            
            originalStatesArray = getInvolvedStates(
                header, 
                request,
                idPath,
                validFrom,
                validTo,
                OP_SET_LIFETIME, INCLUDE_STATE_AT_START, INCLUDE_STATE_AT_END
            );
            
            cut = createLifetimeEdgeState(
                atStart, 
                requestDateValue, 
                originalStatesArray, 
                spec
            );
        }
        else if (compareAtStart(requestDateValue, rootDateValue) == 0) {
            // nothing to do the new lifetime is the same as the old
        }
        else if (requestDateValue != null) {
            // an enlargement of lifetime is not allowed 
            // but if the value is null it is treated as "leave the end
            // as it is"
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                new BasicException.Parameter[]{
                    new BasicException.Parameter(
                        "object",
                        idPath
                    ),
                    new BasicException.Parameter(
                        "boundary",
                        atStart ? "start of object" : "end of object"
                    ),
                    new BasicException.Parameter(
                        "existing boundary",
                         rootDate
                    ),
                    new BasicException.Parameter(
                        "requested boundary",
                         requestDate
                    )
                },
                "Trying to increase the lifetime at boundary."
            );
        }
        
        if (originalStatesArray != null) {
            for (int i = 0; i < originalStatesArray.length; i++) {
                originalStates.add(originalStatesArray[i]);
            }
        }        
        return cut;
    }

    // --------------------------------------------------------------------------
    /**
     * Assert that the object has the Attributes required for the class. 
     * Also set object_createdAt and object_createdBy.
     *
     * @param   object  object to check
     * @param   pathId  id of the object as it is in the path
     */
    private void assertRequiredAttributes(
        DataproviderObject object,
        String pathId
    ) throws ServiceException {
        SysLog.trace("> assertRequiredAttributes " );

        object.values(SystemAttributes.CREATED_AT).set(
            0,
            object.values(SystemAttributes.MODIFIED_AT).get(0)
        );
        object.clearValues(SystemAttributes.CREATED_BY).addAll(
            object.values(SystemAttributes.MODIFIED_BY)
        );
        
        object.values(ID_ATTRIBUTE_NAME).set(
            0,
            pathId
        );

        ModelElement_1_0 objClass = getObjectClass(object);
        
        Map modelAttributes = (Map)objClass.values("attribute").get(0);

        for(
            Iterator i = modelAttributes.values().iterator(); 
            i.hasNext();
        ) {
            ModelElement_1_0 modelAttribute = (ModelElement_1_0)i.next();
            
            // derived attributes do not have to be part of the request!
            if (modelAttribute.getValues("isDerived") == null ||
                ! ((Boolean)modelAttribute.getValues("isDerived").get(0)).booleanValue() 
            ) {
            // multiplicity
            String attributeName = (String)modelAttribute.values("name").get(0);
            String multiplicity = (String)modelAttribute.values("multiplicity").get(0);

            int size = 0;
            if(object.getValues(attributeName) != null) {
                size = object.getValues(attributeName).size();
            }
            if((multiplicity.startsWith("1..") && (size < 1)) 
            ) { 
                ServiceException e = new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("path", object.path()),
                        new BasicException.Parameter("attribute", attributeName),
                    },
                    "missing required attribute."
                );              
                throw e;
            }
            }
        }
        SysLog.trace("< assertRequiredAttributes " );    
    }
        
    // --------------------------------------------------------------------------
    /** 
     * Set the identity.
     * 
     */
    protected void setIdentity(
        DataproviderRequest request, DataproviderObject replyObject
    ) throws ServiceException {
        super.setIdentity(request, replyObject);
        // in case object contains feature "identity" replace it
        if (replyObject.containsAttributeName(SystemAttributes.OBJECT_IDENTITY)) {
            
            PathComponent comp = new PathComponent(replyObject.path().getBase());
            
            // remove state endings <path>/<id>:<stateNr>:
            if (comp.size() > 2 && comp.isPrivate()) {
                /*
                path = replyObject.path().getParent().add(comp.getPrefix(comp.size()-2));
    
                replyObject.clearValues(SystemAttributes.OBJECT_IDENTITY).add(
                    path.toUri()
                );
                */
            }
            
            // remove all the OperationParameters for the identity
            Path idPath = new Path(replyObject.path());
            for (int i = 0; i < idPath.size(); i++) {
                String part = idPath.get(i);


                if (part.equals(State_1_Attributes.REF_HISTORY) 
                    || part.equals(State_1_Attributes.REF_STATE)
                    || part.equals(State_1_Attributes.REF_VALID) 
                ) {
                    idPath.remove(i);
                    if (i < idPath.size()) {
                        idPath.remove(i);
                    }
                }
                else {
                    int pos = part.indexOf(';');
                    if (pos > 0) {
                        part = part.substring(0, pos);
                        
                        idPath.remove(i);
                        idPath.add(i, part);
                    }
                }
                    
            }
            replyObject.clearValues(SystemAttributes.OBJECT_IDENTITY).add(
                idPath.toUri()
            );
            if(isStateRequest(request)) {
                replyObject.clearValues(State_1_Attributes.STATED_OBJECT).add(
                    idPath
                );
            }
        }

    }
    
    // --------------------------------------------------------------------------
    /**
     * Treat all objects returned; if it is a stateful object: remove attribute
     * ID_ATTRIBUTE_NAME and remove stateSeparatorPos from id.
     * 
     * The parameter isStateful works only if at a certain path there
     * are only stateful or non stateful object, but not mixed. This is 
     * used in find() requests, where we are depending on that information 
     * based on the path, because if the request specifies no_attributes
     * we can't find the class.
     * 
     * @param reply  reply to treat
     * @param isStateful  if the object is stateful
     */
    private DataproviderReply completeReply(
        DataproviderRequest request,
        DataproviderReply _reply,
        boolean isStateful,
        String idCompletion, 
        Path originalRequestPath,
        boolean originalPathWithQualifier
    ) throws ServiceException {
        SysLog.trace("> State_1.completeReply");
        DataproviderReply reply = _reply;
        // remove state attributes
        if (reply != null && reply.getObjects() != null) 
        {
            String base = null;
            for (int i = 0; i < reply.getObjects().length; i++) {
                DataproviderObject object = reply.getObjects()[i];
                
                if (isStateful) {
                    String qualifier = null;
                    
                    //
                    // remove unwanted attributes
                    //
                    object.attributeNames().remove(ID_ATTRIBUTE_NAME);

                                        
                    // 
                    // manage path
                    // 
                    
                    // remove state id from path
                    base = object.path().remove(object.path().size()-1);
                    
                    String stateNumber = null;
                    PathComponent comp = new PathComponent(base);
                    // id of object may use path components itself. Thus the 
                    // length of a path component is undefined.
                    
                    if (comp.size() > 2 && 
                        comp.isPrivate() 
                    ) {
                        stateNumber = comp.get(comp.size()-2);
                        //comp = comp.getPrefix(comp.size()-2);
                        qualifier = comp.getPrefix(comp.size()-2).toString();
                        //object.path().add(comp.getPrefix(comp.size()-2));
                    }
                    else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE, 
                            new BasicException.Parameter[]{
                                new BasicException.Parameter("path", object.path()),
                                new BasicException.Parameter("object id segment", base),
                                new BasicException.Parameter("object id sub-segments", comp.getSuffix(0))
                            },
                            "Encountered path of state without state number (should be <id:number:>)."
                        );      
                    }
                    
                    if (originalRequestPath != null) {
                        // if it is supplied just apply it
                        //if (originalPathWithQualifier) {
                            object.path().setTo(new Path(originalRequestPath));
                        //}
                        //else {
                        //    object.path().setTo(new Path(originalRequestPath));
                        //}
                    }
                    
                    // add idCompletion in case of several states of the same 
                    // object:
                    if (idCompletion != null) {
                        if (idCompletion.equals(OP_STATE_EXTENDED)) {
                            // create <id>;state=<stateNumber>
                            // String id = object.path().remove(object.path().size()-1);
                            object.path().add(
                                qualifier + OP_STATE_EXTENDED + stateNumber
                            );
                        }
                        else {
                            // create <id>/state/<stateNumber>
                            // object.path().add(idCompletion).add(stateNumber);
                            object.path().add(stateNumber);
                        }
                    }
                    else if (!originalPathWithQualifier){
                        object.path().add(qualifier);
                    }
                        
                    // else path is ok
                }
            }
        }
        reply = super.completeReply(
            request, 
            reply
        );
        
        //
        // set derived attributes 
        // (empty attribues are removed in super.completeReply())
        //
        
        if (isStateful && reply != null && reply.getObjects() != null) {
            for (int i = 0; i < reply.getObjects().length; i++) {
                DataproviderObject object = reply.getObjects()[i];

                // add empty attributes as null
                object.values(State_1_Attributes.INVALIDATED_AT);
                // supply some attributes still in model, but never used
                object.values(State_1_Attributes.UNDERLYING_STATE);
                object.values(State_1_Attributes.STATED_OBJECT);
                // add datastore context
                object.clearValues(
                    SystemAttributes.CONTEXT_PREFIX + State_1_Attributes.STATE_CONTEXT + ':' + SystemAttributes.OBJECT_CLASS
                ).add(
                    "org:openmdx:compatibility:state1:StateCapable"
                );
                object.clearValues(
                    SystemAttributes.CONTEXT_PREFIX + State_1_Attributes.STATE_CONTEXT + ':' + State_1_Attributes.KEEPING_INVALIDATED_STATES
                ).add(
                    Boolean.valueOf(!this.isHistoryDisabled(object.path()))
                );
            }
        }
        
        SysLog.trace("< completeReply", new Integer(reply.getObjects().length));
                    
        return reply;
    }
    
    // --------------------------------------------------------------------------
    /** 
     * Create the root object which contains for now the instanceNumber. 
     */
    private DataproviderObject createRoot(
        ServiceHeader header,
        DataproviderRequest request,
        DataproviderObject object,
        UpdateSpec spec
    ) throws ServiceException {
        
        DataproviderObject root = null;
        
        root = new DataproviderObject(spec.getObjectPath());
        
        root.clearValues(SystemAttributes.OBJECT_CLASS).addAll( 
            object.getValues(SystemAttributes.OBJECT_CLASS));
        root.clearValues(SystemAttributes.CREATED_AT).set(0, spec.getModificationDate());
        root.clearValues(SystemAttributes.CREATED_BY).addAll(spec.getModificationPrincipal());
        root.clearValues(SystemAttributes.MODIFIED_AT).set(0,spec.getModificationDate());
        root.clearValues(SystemAttributes.MODIFIED_BY).addAll(spec.getModificationPrincipal());
        if (spec.getValidFrom() != null) {
            root.clearValues(validFromAttribute()).set(0, spec.getValidFrom());
        }
            
        if (spec.getValidTo() != null) {
            root.clearValues(validToAttribute()).add(toModelledValidTo(spec.getValidTo()));
        }
        
        root.values(STATE_NUMBER).set(0, new Long(0));
        
        DataproviderRequest newRequest = 
            new DataproviderRequest(
                root, 
                DataproviderOperations.OBJECT_CREATION, 
                AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                null
            );

        newRequest.contexts().putAll(request.contexts());

        super.create(header, newRequest);
            
        // saved 0 need -1 because the following steps take this number + 1
        // for getting the number to use next.
        root.values(STATE_NUMBER).set(0, new Long(-1));
        
        return root;        
    }

    // --------------------------------------------------------------------------
    /** 
     * Create the states from the objects array with the instance number
     *  from root.
     */
    private ArrayList createStates(
        ServiceHeader header,
        DataproviderRequest request,
        List objects,
        DataproviderObject root
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("createStates");
        DataproviderObject state = null;
        DataproviderReply reply = null;
        ArrayList replies = new ArrayList();
        
        long stateNumber = 0;
        
        try {
            stateNumber = ((Number) root.values(STATE_NUMBER).get(0)).longValue();
        } catch (NullPointerException e) {
            SysLog.warning("Why are we here?", e);
        }
        String id = root.path().getBase();
        SysLog.trace("number of states to create", new Integer(objects.size()));
        for (Iterator i = objects.iterator();
            i.hasNext();
        ) {
            state = (DataproviderObject) i.next(); 
            
            if(SysLog.isTraceOn()) SysLog.trace(
                "creating state",
                "validFrom: " +  state.getValues(validFromAttribute()) + 
                " validTo: " + state.getValues(validToAttribute())
            );
            
            stateNumber++;
            
            // must change path of state
            state.path().remove(state.path().size()-1); 
            
            // add id for finding object
            state.values(ID_ATTRIBUTE_NAME).set(0,id);
            
            // add FIELD_DELIMINTER directly to avoid parsing for components in PathComponent
            state.path().add( 
                id + PathComponent.FIELD_DELIMITER + stateNumber + PathComponent.FIELD_DELIMITER);
            
            DataproviderRequest newRequest = 
                new DataproviderRequest(
                    state, 
                    DataproviderOperations.OBJECT_CREATION,
                    request.attributeFilter(),
                    request.position(),
                    request.size(),
                    request.direction(),
                    AttributeSelectors.ALL_ATTRIBUTES,
                    request.attributeSpecifier()
                );
            newRequest.contexts().putAll(request.contexts());

            StopWatch_1.instance().startTimer("createStates-DB");
            reply = super.create(header, newRequest);
            StopWatch_1.instance().stopTimer("createStates-DB");
                
            replies.add(reply);
        }
        
        root.values(STATE_NUMBER).set(0, new Long(stateNumber));
        
        StopWatch_1.instance().stopTimer("createStates");
        return replies;
    }
    
    // --------------------------------------------------------------------------
    /**
     * Direct access to an individual state. 
     * <p> 
     * There are two forms of calling this method
     * <ul>
     * <li> objectPath and number are null; in which case request.path() is 
     * expected to be of the form
     * <somePath>/[object Id]/[(state|historyState|validState)]/#[stateNumber] 
     * </li>
     * <li> objectPath and number are set: in which case request.path() does not 
     * matter at all.
     * </li>
     * </ul>
     * 
     * @param header   header, requestedAt, requestedFor settings are not used
     * @param request  original request
     * @param objectPath path leading to the object or null
     * @param stateNumber state number or null
     */
    private DataproviderReply getStateDirect(
        ServiceHeader header, 
        DataproviderRequest request,
        Path objectPath,
        String number
    ) throws ServiceException  {
        String stateNumber = null;
        
        Path requestPath = null;

        if (number == null) {
            // first correct path of object
            requestPath = new Path(request.path());
            stateNumber = requestPath.remove(requestPath.size()-1);
            requestPath.remove(requestPath.size()-1);
        }
        else {
            requestPath = new Path(objectPath);
            stateNumber = number;
        }
        
        String objectId = requestPath.remove(requestPath.size()-1);
        
        requestPath.add( 
            objectId + 
            PathComponent.FIELD_DELIMITER + 
            stateNumber + 
            PathComponent.FIELD_DELIMITER
        );        
        
        DataproviderRequest newRequest = 
            new DataproviderRequest(
                new DataproviderObject(requestPath), 
                DataproviderOperations.OBJECT_RETRIEVAL,
                new FilterProperty[] {}, // no FilterProperties
                0,                       // position
                Integer.MAX_VALUE,       // size
                Directions.ASCENDING,    // direction
                AttributeSelectors.ALL_ATTRIBUTES, 
                null                     // attributeSpecifier
            );
        newRequest.contexts().putAll(request.contexts());

        // add FIELD_DELIMINTER directly to avoid parsing for components in PathComponent

        return super.get(header, newRequest);
    }
    
    /*
     * Get the state which is denoted by the operation parameter state=.
     */
    private DataproviderReply getStateForStateOperationParameter(
        ServiceHeader header,
        DataproviderRequest request,
        UpdateSpec spec
    ) throws ServiceException {
        DataproviderReply reply = null;

        if (spec.getOperationParameter().getStateNumber() != null) {
            Path requestPath = new Path(spec.getObjectPath());
            reply = getStateDirect(header, request, requestPath, spec.getOperationParameter().getStateNumber());
        }
        else if (spec.getOperationParameter().isFirst() 
            || spec.getOperationParameter().isLast() 
            || spec.getOperationParameter().isUndef()
        ) {
            String objectId = spec.getOperationParameter().getQualifier();
            
            DataproviderReply currentStates = findStates(
                header, 
                request, 
                spec.getObjectPath().getParent(),
                objectId,
                null, // validFrom
                null, // validTo
                null, // only currently valid ones are searched 
                INCLUDE_STATE_AT_START,
                INCLUDE_STATE_AT_END,
                USE_REQUESTED_AT, false, null
            );
            
            TreeMap sorter = new TreeMap();
            for (int i = 0; i < currentStates.getObjects().length; i++) {
                String validFrom = 
                    readNullableStringValue(currentStates.getObjects()[i], validFromAttribute());
                sorter.put(
                    validFrom == null ? EARLIEST_DATE : validFrom,
                    currentStates.getObjects()[i]
                );
            }
            DataproviderObject firstLast = null;
            if ((spec.getOperationParameter().isFirst() 
                || spec.getOperationParameter().isUndef()) && sorter.size()>0
            ) {
                firstLast = (DataproviderObject)sorter.get(sorter.firstKey());              
            }
            else if (sorter.size()>0){
                firstLast = (DataproviderObject)sorter.get(sorter.lastKey());
            }
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("path", request.path()),
                        new BasicException.Parameter("operationQualifier", spec.getOperationParameter().getOperationQualifier())
                    },
                    "no valid state for state=first or state=last operation"
                );
            }
            reply = new DataproviderReply(firstLast);
            reply.contexts().putAll(currentStates.contexts());
        }
        else if (spec.getOperationParameter().getDate() != null) {
            String requestedFor = spec.getOperationParameter().getDate();
            String objectId = spec.getOperationParameter().getQualifier();
            
            reply = findStates(
                header, 
                request, 
                spec.getObjectPath().getParent(),
                objectId,
                requestedFor, 
                requestedFor,
                null, 
                EXCLUDE_STATE_AT_START,
                INCLUDE_STATE_AT_END,
                USE_REQUESTED_AT, false, null
            );
        }
        return reply;
    }

    // --------------------------------------------------------------------------
    /**
     * Finds all the states with the specified filter properties, in the 
     * range validFrom, validTo and with regard of requestedAt and requestedFor.
     * <p>
     * If the objectId is unknown all states of objects fullfilling the filter 
     * properties are returned, otherwise only the states of this special 
     * object.
     * <p>
     * validFrom, validTo take as default the actual date.
     * <p> 
     * The request is not used as is, just certain attributes of it. These
     * are: attributeFilters, position, size, direction, attributeSelector,
     * and attributeSpecifier. The path must be supplied separately, note that
     * it should not contain the objectId. This can be specified separately by 
     * the objectId parameter.
     * <p>
     * Sorting order can be defined via the specifiers. Specifiers are added at
     * the end of the specifiers, thus allowing client provided specifiers to
     * take precedence. 
     * 
     * @param header     header containing requestedAt, requestedFor
     * @param request    the request 
     * @param pathToId   path without the id
     * @param objectId   id of object if known
     * @param validFrom  validity range of the states searched for
     * @param validTo     
     * @param excludeObjectsUpToStart  if true leave out 
     *                                  states with validTo == requestedFor.
     * @param excludeObjectsFromEnd TODO
     * @param useRequestedAt wether or not to use requestedAt
     * @param useExtent distingushes between standard and extent search
     * @param specifiers     AttributeSpecifiers for sort order 
     */ 
    private DataproviderReply findStates(
        ServiceHeader header,
        DataproviderRequest request,
        Path   pathToId,
        String objectId,
        String validFrom,
        String validTo,
        String _requestedAt, 
        boolean excludeObjectsUpToStart,
        boolean excludeObjectsFromEnd,
        boolean useRequestedAt, 
        boolean useExtent, AttributeSpecifier[] specifiers
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("findStates");
        String requestedAt = _requestedAt;
        ArrayList newFilters = new ArrayList();
        //
        // need to distinguish between find operations, where the state
        // on the validTo is not wanted and other operations where
        // we need this state to append the next one.
        //
        Path typePath = pathToId;
        //
        // object.validFrom <= validTo
        //
        if (validTo != null) {
            newFilters.add(
                new FilterProperty(
                    Quantors.FOR_ALL,  // include empty validFrom (since ever)
                    validFromAttribute(),
                    excludeObjectsFromEnd ? FilterOperators.IS_LESS : FilterOperators.IS_LESS_OR_EQUAL,
                    new Object[] {validTo}
                )
            );
        }
        //
        // object.validTo >= validFrom
        //
        if (validFrom != null) {  
            newFilters.add(
                new FilterProperty(
                    Quantors.FOR_ALL, // includes empty validTo (for ever)
                    validToAttribute(),
                    excludeObjectsUpToStart ? FilterOperators.IS_GREATER : FilterOperators.IS_GREATER_OR_EQUAL,
                    new Object[] {toModelledValidTo(validFrom)} 
                )
            );
        }
        //           
        // add the existing filters:
        //
        boolean typed = false;
        boolean stateIdFilter = false;
        boolean invalidatedAtFilter = false;
        for (
            int i = 0, iLimit = request.attributeFilter().length; 
            i < iLimit;
            i++
        ) {
            FilterProperty filter = request.attributeFilter()[i];
            //
            // code like in AbstractDatabase
            //
            if(State_1_Attributes.STATED_OBJECT.equals(filter.name())) {
                // TODO ignore explicit
            } else if (SystemAttributes.OBJECT_IDENTITY.equals(filter.name())) {
                // must map identity filter to filter for object_stateId
                List stateIdFilterValues = new ArrayList();
                List identityFilterValues = new ArrayList();
                for(
                  int j = 0, jLimit = filter.values().size();
                  j < jLimit;
                  j++
                ) {
                    String filterString = (String)filter.getValue(j);
                    // remove possible wildcard (... like 'a%')
                    boolean wildcardSuffix = filterString.endsWith("%");
                    try {
                        // create path for correct treatment of wildcards etc
                        Path filterPath = new Path(
                            wildcardSuffix ? filterString.substring(0, filterString.length()-1) : filterString
                        );
                        if(useExtent) {
                            if(wildcardSuffix) throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.NOT_SUPPORTED,
                                new BasicException.Parameter[] {
                                    new BasicException.Parameter(SystemAttributes.OBJECT_IDENTITY, filterString),
                                },
                                "'%' wildcard not supported if 'extent' operation"
                            );
                            if(
                                filter.operator() == FilterOperators.IS_LIKE || 
                                filter.operator() == FilterOperators.IS_UNLIKE
                            ) {
                                if(filterPath.size() % 2 == 0) filterPath.add(":*");
                                identityFilterValues.add(filterPath.toXri());
                                String stateIdFilterValue = filterPath.getBase(); 
                                stateIdFilterValues.add(
                                    stateIdFilterValue.startsWith(":") && stateIdFilterValue.endsWith("*") ?
                                    stateIdFilterValue.substring(1, stateIdFilterValue.length() - 1) + '%' :
                                    stateIdFilterValue
                                );
                            } else {
                                identityFilterValues.add(filterPath.toXri());
                                stateIdFilterValues.add(filterPath.getBase());
                            }
                            typePath = filterPath;
                        } else if (
                            filterPath.startsWith(pathToId) &&
                            filterPath.size() == pathToId.size() + 1
                        ) { 
                            stateIdFilterValues.add(
                                filterPath.getBase() + (wildcardSuffix ? "%" : "")
                            );
                        } else {
                            // add a non matching filter value. Don't throw an
                            // exception to allow the request to try a match 
                            // with the remaining filter values. 
                            // For the case of a comparing filter (smaller, 
                            // greater, in between) add extrem value.
                            stateIdFilterValues.add(
                                filterPath.compareTo(pathToId) < 0 ? 
                                    IDENTITY_UNDERFLOW :
                                    IDENTITY_OVERFLOW
                            );
                        }
                    } 
                    catch (Exception exception) {
                        stateIdFilterValues.add(
                            filterString.compareTo(pathToId.toUri()) < 0 ? 
                                IDENTITY_UNDERFLOW :
                                IDENTITY_OVERFLOW
                        );
                    }
                }
                stateIdFilter = true;
                if(useExtent) {
                    newFilters.add(
                        new FilterProperty(
                            filter.quantor(),
                            SystemAttributes.OBJECT_IDENTITY,
                            filter.operator(),
                            identityFilterValues.toArray(new String[identityFilterValues.size()])
                        )
                    );
                    for(
                        Iterator j = stateIdFilterValues.iterator();
                        stateIdFilter && j.hasNext();
                    ) stateIdFilter = !"%".equals(j.next());
                    if(stateIdFilter) newFilters.add(
                        new FilterProperty(
                            Quantors.THERE_EXISTS,
                            ID_ATTRIBUTE_NAME,
                            filter.operator(),
                            stateIdFilterValues.toArray(new String[stateIdFilterValues.size()])
                        )
                    );
                } else {
                    newFilters.add(
                        new FilterProperty(
                            Quantors.THERE_EXISTS,
                            ID_ATTRIBUTE_NAME,
                            filter.operator(),
                            stateIdFilterValues.toArray(new String[stateIdFilterValues.size()])
                        )
                    );
                }
            } else {
                typed |=  
                    SystemAttributes.OBJECT_INSTANCE_OF.equals(filter.name()) ||
                    SystemAttributes.OBJECT_CLASS.equals(filter.name());
                newFilters.add(filter);
            }
        }        

        if (objectId != null) {
            //
            // if it is search with exact id (i.e. a get())
            //
            newFilters.add(
                new FilterProperty(
                    Quantors.THERE_EXISTS,
                    ID_ATTRIBUTE_NAME,
                    FilterOperators.IS_IN,
                    new Object[] {objectId}
                )
            );
        } else if(!stateIdFilter) {
            //
            // guarantee existence of ID_ATTRIBUTE_NAME to exclude root object.
            //
            newFilters.add(
                new FilterProperty(
                    Quantors.THERE_EXISTS,
                    ID_ATTRIBUTE_NAME,
                    FilterOperators.IS_NOT_IN,
                    new Object[] {}
                )
            );
        }  
        
        if (useRequestedAt && !isHistoryDisabled(typePath)) {
            if (requestedAt == null) { 
                // need only the valid filter (invalidatedAt must be null)
                if(!invalidatedAtFilter) newFilters.add(
                    new FilterProperty(
                        Quantors.FOR_ALL,    // include empty invalidated_at only
                        State_1_Attributes.INVALIDATED_AT,
                        FilterOperators.IS_IN,
                        new Object[] {}
                    )
                );
    
                requestedAt = getRequestedAt(header);
            } else {
                //
                // the ones which have only been invalidated after my request
                // or the ones which are still valid
                //
                newFilters.add(
                    new FilterProperty(
                        Quantors.FOR_ALL,      // include empty invalidated_at (still valid)
                        State_1_Attributes.INVALIDATED_AT,
                        FilterOperators.IS_GREATER,
                        new Object[] {requestedAt}
                    )
                );
            }
            //   
            // the ones which are existing at that time
            //
            newFilters.add(
                new FilterProperty(
                    Quantors.THERE_EXISTS,
                    SystemAttributes.MODIFIED_AT,  // OBJECT_CREATED_AT 
                    FilterOperators.IS_LESS_OR_EQUAL,
                    new Object[] {requestedAt}
                )
            );
        }
        
        AttributeSpecifier[] newSpecifiers = null;
        if (specifiers == null || specifiers.length == 0) {
            newSpecifiers = request.attributeSpecifier();
        }
        else {        
            AttributeSpecifier[] requestSpecifiers = request.attributeSpecifier();
            newSpecifiers = new AttributeSpecifier[
                requestSpecifiers.length + specifiers.length];
            int p = 0;
            for (int i = 0; i < requestSpecifiers.length; i++) {
                newSpecifiers[p++] = requestSpecifiers[i];
            }
                
            for (int i = 0; i < specifiers.length; i++) {
                newSpecifiers[p++] = specifiers[i];            
            }
        }        

         
        DataproviderRequest findRequest = new DataproviderRequest(
            new DataproviderObject(pathToId), 
            DataproviderOperations.ITERATION_START,
            (FilterProperty[])newFilters.toArray(
                new FilterProperty[newFilters.size()]
            ),
            request.position(),
            request.size(),
            request.direction(),
            request.attributeSelector(),
            newSpecifiers
            //(AttributeSpecifier[]) specifierMap.values().toArray(new AttributeSpecifier[0])
        );
        findRequest.contexts().putAll(
            request.contexts()
        );
        if (
            !typed &&
            useDatatypes()
         ) {
            String objectType = stateTypeName();
            if(objectId != null) try {                    
                DataproviderObject_1_0 core = getDelegation(
                ).get(
                    header,
                    new DataproviderRequest(
                        new DataproviderObject(pathToId.getChild(objectId)),
                        DataproviderOperations.OBJECT_RETRIEVAL,
                        AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES,
                        null
                    )
                ).getObject();
                objectType = (String)core.values(
                    SystemAttributes.OBJECT_CLASS
                ).get(
                    0
                );
            } catch (Exception excpetion) {
                // Fall back to STATE_TYPE_NAME
            }
            findRequest.context(
                DataproviderRequestContexts.OBJECT_TYPE
            ).set(
                0,
                objectType
            );
        }
        SysLog.trace("findStates-DB", findRequest);
        
        StopWatch_1.instance().startTimer("findStates-DB");
        DataproviderReply reply = super.find(header, findRequest);
        StopWatch_1.instance().stopTimer("findStates-DB");
        
        /**
         * Really it's ok as long as the states are passed on to the caller. 
         * But if there are that many states to involved in an update, this
         * would be a problem, which has to be treated elsewhere. 
         *
        if (((Boolean)reply.context(DataproviderReplyContexts.HAS_MORE).get(0)).booleanValue()) {
            // just to find the problem faster:
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_IMPLEMENTED,
                new BasicException.Parameter[] {
                    new BasicException.Parameter("find path", pathToId),
                    new BasicException.Parameter("results", reply.getObjects().length)
                },
                "Found to many states to treat correctly."
            );
        }
         */
        
        StopWatch_1.instance().stopTimer("findStates");
        return reply;
    }
        
    // --------------------------------------------------------------------------
    /**
     * Sorts the states in the reply ascending using the specified sortAttribute
     * which must be a date.
     * 
     * @param  reply the reply object of which the states have to be sorted
     * @param sortAttribute   the attribute to use for sorting, must be a date.
     */
    private void sortStates(
        DataproviderReply reply,
        String sortAttribute
    ) {
        
        DataproviderObject[] objects = reply.getObjects();
        DataproviderObject swap = null;
        
        // just a simple sort. 
        for (int i = 0; i < objects.length; i++) {
            int min = i;
            SparseList minDate = objects[min].getValues(sortAttribute);  // may be null
            int j; 
             
            // Find the smallest element in the unsorted list 
            for (j = i + 1; j < objects.length; j++) 
            { 
                // compare according to sortAttribute
                if (compareAtStart(
                        objects[j].getValues(sortAttribute), 
                        minDate
                    ) < 0
                 ) {
                    min = j;
                    minDate = objects[min].getValues(sortAttribute);
                 }
            } 
            
            // Swap the smallest unsorted element into the end of the sorted list. 
            swap = objects[min]; 
            objects[min] = objects[i]; 
            objects[i] = swap; 
        }
    }

    // --------------------------------------------------------------------------
    /** 
     * create a new state from the requested object and the loaded object in
     * the frame validFrom, validTo, either replacing or updating the existing
     *  values.
     * If the requested object is null, the loaded is just reduced to the new
     * validFrom, validTo dates.
     * @throws ServiceException 
     */
    private DataproviderObject createSplitState(
        DataproviderObject loaded,
        DataproviderObject requested,
        String validFrom, 
        String validTo,
        UpdateSpec spec, 
        boolean replace
    ) throws ServiceException {
        DataproviderObject object = new DataproviderObject(loaded);
        
        if (requested != null) {
            String attributeName = null;
                
            for (
                Iterator i = requested.attributeNames().iterator();
                i.hasNext();
            ) {
                attributeName = (String)i.next();
                                                        
                if (replace) {
                    object.clearValues(attributeName).
                        addAll(requested.getValues(attributeName));
                }
                else {
                    SparseList objectValues = object.values(attributeName);
                    SparseList requestedValues = requested.getValues(attributeName);
                    for (
                        ListIterator valueIter = requestedValues.populationIterator();
                        valueIter.hasNext();
                    ) {
                        objectValues.set(valueIter.nextIndex(), valueIter.next());
                    }
                }
            }
            // set the object_class separately: (invalidated with replace == false)
            object.clearValues(SystemAttributes.OBJECT_CLASS).
               addAll(requested.getValues(SystemAttributes.OBJECT_CLASS));
              
            object.values(SystemAttributes.CREATED_AT).set(0,spec.getModificationDate());
            object.clearValues(SystemAttributes.CREATED_BY).addAll(spec.getModificationPrincipal());
        }
        
        // now correct validFrom, validTo
        {
            SparseList target = object.clearValues(validFromAttribute());
            if (validFrom != null) target.add(validFrom);            
        }
        {
            SparseList target = object.clearValues(validToAttribute());
            if (validTo != null) target.add(toModelledValidTo(validTo));            
        }
        
        object.values(SystemAttributes.MODIFIED_AT).set(0,spec.getModificationDate());
        object.clearValues(SystemAttributes.MODIFIED_BY).addAll(spec.getModificationPrincipal());
        
        return object;
    }           

    // --------------------------------------------------------------------------
    /**
     * Create the needed states based on the object loaded from storage and
     * the object which holds the requested changes.
     * 
     * ArrayList may stay empty if the loaded and the requested states are 
     * just after each other. In this case the loaded state must remain valid
     * and may not be invalidated, except if the state has the same values as
     * the requested state; in this case the loaded must be invalidated and 
     * returned, to be melted later on.
     */
    private ArrayList updateState(
        DataproviderObject loaded,
        DataproviderObject requested,
        UpdateSpec spec,
        boolean replace
    ) throws ServiceException {
        ArrayList splitStates = new ArrayList();
                
        String loadedValidFrom = null;
        String loadedValidTo = null;
        String requestedValidFrom = null;
        String requestedValidTo = null;
        
        loadedValidFrom = 
            readNullableStringValue(loaded, validFromAttribute());
        if (loadedValidFrom == null) {
            loadedValidFrom = "0000";  // smallest value
        }
        
        loadedValidTo = toExclusiveValidTo(
            readNullableStringValue(loaded, validToAttribute())
        );
        if (loadedValidTo == null) {
            loadedValidTo = "9999";  // biggest value
        }        
        
        requestedValidFrom = 
            (spec.getValidFrom() == null ? "0000": spec.getValidFrom());

        requestedValidTo = 
            (spec.getValidTo() == null ? "9999" : spec.getValidTo());

        // we are only interested in the time span inside the loaded 
        // the other loaded ones will fill the rest of the time
        if (requestedValidFrom.compareTo(loadedValidTo) == 0 
            || loadedValidFrom.compareTo(requestedValidTo) == 0
        ) {
            // the loaded state is immediately before or after the 
            // requested update period. This is ok and does not require any 
            // splitting!
            
            // The loaded should be removed from the set of states to invalidate
            // (because it is still needed). This is achieved by returning an 
            // empty splitStates list.
        }        
        else if (requestedValidFrom.compareTo(loadedValidFrom) <= 0) {
            if (requestedValidTo.compareTo(loadedValidTo) >= 0) {
                // the loaded gets mixed entirely with the request
                splitStates.add(
                    createSplitState(
                        loaded, 
                        requested, 
                        loadedValidFrom.equals("0000") ? null : loadedValidFrom, 
                        loadedValidTo.equals("9999") ? null : loadedValidTo, 
                        spec,
                        replace
                    )
                );
            }
            else {
                // loaded gets merged up to requestedValidTo
                splitStates.add(
                    createSplitState(
                        loaded, 
                        requested, 
                        loadedValidFrom.equals("0000") ? null : loadedValidFrom, 
                        requestedValidTo.equals("9999") ? null : requestedValidTo, 
                        spec,
                        replace
                    )
                );
                if (!requestedValidTo.equals("9999")) {
                    splitStates.add(
                        createSplitState(
                            loaded,
                            null, 
                          requestedValidTo, 
                            loadedValidTo.equals("9999") ? null : loadedValidTo,
                          spec,
                          replace
                        )
                    );
                }
            }
        }
        else if (requestedValidFrom.compareTo(loadedValidTo) < 0) {
            if (requestedValidTo.compareTo(loadedValidTo) <= 0) {
                // requested is fully inside the loaded
                
                splitStates.add(
                    createSplitState(
                        loaded,
                        null,
                        loadedValidFrom.equals("0000") ? null : loadedValidFrom, 
                        requestedValidFrom.equals("0000") ? null : requestedValidFrom,
                        spec,
                        replace
                    )
                );
                
                splitStates.add(
                    createSplitState(
                        loaded, 
                        requested, 
                        requestedValidFrom.equals("0000") ? null : requestedValidFrom, 
                        requestedValidTo.equals("9999") ? null : requestedValidTo, 
                        spec,
                        replace
                    )
                );
                if (!requestedValidTo.equals("9999") 
                    && requestedValidTo.compareTo(loadedValidTo) < 0
                ) {
                    splitStates.add(
                        createSplitState(
                            loaded,
                            null,
                            requestedValidTo, 
                            loadedValidTo.equals("9999") ? null : loadedValidTo,
                            spec,
                            replace
                        )
                    );
                }
            }
            else {
                // requested starts inside loaded but ends outside
                if (!requestedValidFrom.equals("0000")) {
                    splitStates.add(
                        createSplitState(
                            loaded,
                            null,
                            loadedValidFrom.equals("0000") ? null : loadedValidFrom,
                            requestedValidFrom,
                            spec,
                            replace
                        )
                    );
                }
                splitStates.add(
                    createSplitState(
                        loaded, 
                        requested, 
                        requestedValidFrom.equals("0000") ? null : requestedValidFrom, 
                        loadedValidTo.equals("9999") ? null : loadedValidTo, 
                        spec,
                        replace
                    )
                );
            }
        }
        else {
            // error why was this state among the loaded ones,
            // this state is not within the time periode.
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                new BasicException.Parameter[]{
                    new BasicException.Parameter("request.validFrom", requestedValidFrom),
                    new BasicException.Parameter("request.validTo", requestedValidTo),
                    new BasicException.Parameter("loaded.validFrom", loadedValidFrom),
                    new BasicException.Parameter("loaded.validTo", loadedValidTo)
                },
                "Object loaded is outside request."
            );

        }
        
        return splitStates;
    }
    
    // --------------------------------------------------------------------------
    /**
     * compare if the two objects have the same values apart from valid_to
     * valid_from, created_at created_by, modified_at, modified_by and 
     * instance_of.
     * 
     * If instance_of is not present in one of the objects, an empty attribute 
     * is created.
     * 
     */    
    private boolean haveEqualValues(
        DataproviderObject a, 
        DataproviderObject b
    ) {
        boolean equal = true; 
        String attributeName = null;
        
        Set deltaAttributeNames = new HashSet(b.attributeNames());
                    
        for (Iterator i = a.attributeNames().iterator();
            i.hasNext() && equal;
        ) {
            attributeName = (String) i.next();
            
            deltaAttributeNames.remove(attributeName);
            
            if (!ignoreAttributeForStateCompare(attributeName)) equal = equal(
                a.getValues(attributeName),
                b.getValues(attributeName)
            );
        }
        
        // only ignoring attributes may survive
        for (Iterator i = deltaAttributeNames.iterator(); 
            i.hasNext() && equal; 
        ) {
            attributeName = (String) i.next();
            equal = 
                b.getValues(attributeName).size() == 0 ||
                ignoreAttributeForStateCompare(attributeName);
        }

        return equal;
    }
    
    // --------------------------------------------------------------------------
    /**
     * Compare two dataprovider attributes, treating <code>null</code> and empty 
     * lists the same way.
     * 
     * @param left the first argument
     * @param left the second argument
     * 
     * @return true if the two arguments are equal
     */
    private static boolean equal(
        SparseList left,
        SparseList right
    ){
        return left == null || left.isEmpty() ?
            right == null || right.isEmpty() :
            left.equals(right);
    }

    // --------------------------------------------------------------------------
    /**
     * Decide if the attribute has to be ignored for comparing two states of 
     * an object. 
     * 
     */
    private boolean ignoreAttributeForStateCompare(String attributeName) {
        return 
            attributeName.equals(validFromAttribute()) ||
            attributeName.equals(validToAttribute())   || 
            attributeName.equals(ID_ATTRIBUTE_NAME) ||
            attributeName.equals(SystemAttributes.CREATED_AT) ||
            attributeName.equals(SystemAttributes.CREATED_BY) ||
            attributeName.equals(SystemAttributes.MODIFIED_AT) ||
            attributeName.equals(SystemAttributes.MODIFIED_BY) ||
            attributeName.equals(SystemAttributes.OBJECT_INSTANCE_OF) ||
            attributeName.equals(SystemAttributes.OBJECT_IDENTITY) ||
            attributeName.startsWith(SystemAttributes.VIEW_PREFIX) || // support for view pattern. Those attributes are removed by persistence layer
            attributeName.startsWith(SystemAttributes.CONTEXT_PREFIX); // support for context pattern. Those attributes are removed by persistence layer
    }
    

    // --------------------------------------------------------------------------
    /**
     * Append the valid period of the extent to valid period of the base if the 
     * extent attaches without a gap in validity.
     * <p>
     * extendForward defines if the extension should be executed in the future 
     * or the past. 
     * 
     * @return true if the extent was appended
     * @throws ServiceException 
     */
    private boolean extendValidity(
        DataproviderObject base,
        DataproviderObject extent,
        boolean extendForward
    ) throws ServiceException {
        boolean extended = false;
        
        if (extendForward) {
            String baseValidTo = toExclusiveValidTo(readNullableStringValue(base, validToAttribute()));
            if (baseValidTo != null &&
                baseValidTo.equals(readNullableStringValue(extent, validFromAttribute())) &&
                haveEqualValues(base, extent)
            ) {
                extended = true;
                String nextValidTo = toExclusiveValidTo(
                    readNullableStringValue(extent, validToAttribute())
                );
                SparseList target = base.clearValues(validToAttribute());         
                if (nextValidTo != null) target.add(toModelledValidTo(nextValidTo));
            }
        }
        else {
            String baseValidFrom = readNullableStringValue(base, validFromAttribute());
            if (baseValidFrom != null &&
                baseValidFrom.equals(toExclusiveValidTo(readNullableStringValue(extent, validToAttribute()))) &&
                haveEqualValues(base, extent)
            ) {
                extended = true;
                String lastValidFrom = 
                    readNullableStringValue(extent, validFromAttribute());
                SparseList target = base.clearValues(validFromAttribute());
                if (lastValidFrom != null) target.add(lastValidFrom);
            }
        }
        
        return extended;
    }


        
    // --------------------------------------------------------------------------
    /**
     * Get the states in the range validFrom, validTo from storage.
     * 
     * @param header  original header
     * @param request for context
     * @param idPath  path containing id of object
     * @param validFrom  validFrom
     * @param validTo    validTo
     * @param excludeObjectsUpToStart TODO
     * @param excludeObjectsFromEnd TODO
     * @return ArrayList containing the states involved
     */
    private DataproviderObject[] getInvolvedStates(
        ServiceHeader header,
        DataproviderRequest request,
        Path idPath,
        String validFrom, 
        String validTo,
        int operation, 
        boolean excludeObjectsUpToStart, 
        boolean excludeObjectsFromEnd
    ) throws ServiceException {
        DataproviderReply reply;
        // get all states of the object within the [from, to] range of the object
        
        DataproviderRequest newRequest = 
            new DataproviderRequest(
                new DataproviderObject(idPath.getParent()),
                DataproviderOperations.ITERATION_START,
                new FilterProperty[] {}, // no FilterProperties
                0,                       // position
                Integer.MAX_VALUE, 
                Directions.ASCENDING,    // direction
                AttributeSelectors.ALL_ATTRIBUTES,
                null
            );
        newRequest.contexts().putAll(request.contexts());
        
        reply = findStates(
            header, 
            newRequest,
            idPath.getParent(),
            idPath.getBase(),
            validFrom,
            validTo, 
            null,                    // check this! it was header.requestedAt()
            excludeObjectsUpToStart,
            excludeObjectsFromEnd,        
            USE_REQUESTED_AT, // check this! for updates, was USE_REQ...
            false, null
        );
        
        // can not correctly handle that many states of a single object.
        if (((Boolean)reply.context(DataproviderReplyContexts.HAS_MORE).get(0)).booleanValue()) {
            // just to find the problem faster:
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_IMPLEMENTED,
                new BasicException.Parameter[] {
                    new BasicException.Parameter("find path", idPath.getParent()),
                    new BasicException.Parameter("results", reply.getObjects().length)
                },
                "Found to many states to treat correctly."
            );
        }

        
        if (!enableHolesInValidity &&
            (   (operation != OP_REMOVE && reply.getObjects().length == 0)
                ||
                (operation == OP_REMOVE && (validFrom != null || validTo != null))
            )
        ) {
            // For remove requests: setting validFrom, validTo in remove request 
            // is useless if one does not allow holes in validity
            // For update requests: trying to update to a time where no valid 
            // states exist leads to holes.
            
            // NOTE: this prevents holes only if there are none so far, it does 
            // not help if the data already contains holes.
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND, 
                new BasicException.Parameter[]{
                    new BasicException.Parameter(
                        "path", 
                        idPath
                    ),
                    new BasicException.Parameter(
                        "validFrom",
                        validFrom
                    ),
                    new BasicException.Parameter(
                        "validTo",
                        validTo
                    ),
                    new BasicException.Parameter(
                        "operation",
                        (operation == OP_REMOVE ? "remove" : "update")
                    ),
                    new BasicException.Parameter(
                        LayerConfigurationEntries.ENABLE_HOLES_IN_OBJECT_VALIDITY,
                        enableHolesInValidity
                    )
                },
                "Could not find underlying states for update, or trying to remove with validFrom, validTo set. "
                + "This is prohibited as holes are not supported by configuration."
            );
        }
        
        return reply.getObjects();
    }    

    // --------------------------------------------------------------------------
    /**
     * Update the originalStates received with the requested changes, 
     * splitting the states where required. If 
     * the existing states don't cover the whole periode of the request, 
     * additional states are generated at the beginning and the end of the 
     * period. 
     * <p>
     * Adjust the roots validTo and validFrom if the new values enlarge the
     * lifetime of the object.
     * <p>
     * borderingStates collects the states immediately at the start or the end
     * of the update period (if any). They may later be merged to the new 
     * states.
     * 
     * @param request original request
     * @param root    root of the object
     * @param originalStates
     * @param spec
     * @param replace  true if is a replace request 
     * @param borderingStates states at the border of the requests validity
     * 
     * @return  ArrayList containing all the generated new states.
     */
    private ArrayList prepareInvolvedStates(
        DataproviderRequest request, 
        DataproviderObject root,
        List originalStates,
        UpdateSpec spec,
        boolean replace,
        List borderingStates
    ) throws ServiceException {
        ArrayList modifyStates = new ArrayList();
        DataproviderObject objectState = null;
        
        TreeMap statePeriods = new TreeMap();
        
        for (Iterator i = originalStates.iterator(); i.hasNext();) {
            objectState = (DataproviderObject) i.next();
            
            String startDate = 
                readNullableStringValue(objectState, validFromAttribute());
            
            // use EARLIEST_DATE as indicator for smallest date
            statePeriods.put(
                startDate == null ? EARLIEST_DATE : startDate, 
                toExclusiveValidTo(readNullableStringValue(objectState, validToAttribute()))
            );

            ArrayList newStates = 
                updateState(objectState, request.object(), spec, replace);
                
            // no new states is only possible if the loaded state is following 
            // the update period immediately. 
            // Remove the loaded state from the originalStates to prevent its 
            // invalidation.
            if (newStates.size() == 0) {
                i.remove();
                borderingStates.add(objectState);
            }
            
            modifyStates.addAll(newStates);
        }
        
        assertStatesWithinRequest(originalStates, spec, false); 
        
        Map.Entry nextPeriod = null;
        Map.Entry period = null;
        // now check for holes and overlaping states
        for (Iterator p = statePeriods.entrySet().iterator(); p.hasNext(); ) {
            if (nextPeriod == null) {
                period = (Map.Entry) p.next();
            }
            else {
                period = nextPeriod;
            }
            
            String periodValidFrom = (String) period.getKey();
            String periodValidTo = (String) period.getValue();
            
            // check if the update is overlaping at the start
            if (period.getKey().equals(statePeriods.firstKey()) &&
                !periodValidFrom.equals(EARLIEST_DATE) &&
                compareAtStart(spec.getValidFrom(), periodValidFrom) < 0 &&
                !spec.skipMissingStates()
            ) {
                // need additional state for the periode from the start of
                // the request up to the start of the first state
                
                DataproviderObject early = 
                    createSplitState(request.object(), request.object(), spec.getValidFrom(), periodValidFrom, spec, replace);               
                assertRequiredAttributes(early, root.path().getBase());
                modifyStates.add(early);
            }
            
            // check if the update is overlaping at the end
            if (period.getKey().equals(statePeriods.lastKey()) &&
                periodValidTo != null &&
                compareAtEnd(spec.getValidTo(), periodValidTo) > 0 &&
                !spec.skipMissingStates()
            ) {
                // need additional state for the periode from the end of
                // the last state to the end of the request

                DataproviderObject late = 
                    createSplitState(request.object(), request.object(), periodValidTo, spec.getValidTo(), spec, replace);
                assertRequiredAttributes(late, root.path().getBase());
                modifyStates.add(late);
            }
            
            if (p.hasNext()) {
                // check if the existing states have a hole in validity
                nextPeriod = (Map.Entry) p.next();
                String nextPeriodValidFrom = (String) nextPeriod.getKey();
                String nextPeriodValidTo = (String) nextPeriod.getValue();
                
                // check sequence of loaded states
                if (periodValidTo == null ||
                    nextPeriodValidFrom == null ||
                    periodValidTo.compareTo(nextPeriodValidFrom) > 0 ||
                    (
                        // non consecutive states while holes are not allowed
                        periodValidTo.compareTo(nextPeriodValidFrom) < 0 &&
                        !enableHolesInValidity
                    )
                ) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE, 
                        new BasicException.Parameter[]{
                            new BasicException.Parameter(
                                "request.path", request.path()
                            ),
                            new BasicException.Parameter(
                                "period.validFrom", periodValidFrom
                            ),
                            new BasicException.Parameter(
                                "period.validTo", periodValidTo
                            ),
                            new BasicException.Parameter(
                                "nextPeriod.validFrom", nextPeriodValidFrom
                            ),
                            new BasicException.Parameter(
                                "nextPeriod.validTo", nextPeriodValidTo
                            ),
                            new BasicException.Parameter(
                                LayerConfigurationEntries.ENABLE_HOLES_IN_OBJECT_VALIDITY,
                                enableHolesInValidity
                            )
                        },
                        "Mixed up states; State has successor which starts before state ends "+
                        "or next state is not adjacent to previous, even though config demands it." 
                    );
                }
                else if (periodValidTo.compareTo(nextPeriodValidFrom) < 0) {
                    // non consecutive states
                    
                    if (!spec.skipMissingStates()) {
                        // need additional state for the periode from the end of
                        // the last state to the start of the next state

                        DataproviderObject filler = 
                            createSplitState(request.object(), request.object(), periodValidTo, nextPeriodValidFrom, spec, replace);
                        assertRequiredAttributes(filler, root.path().getBase());
                        modifyStates.add(filler);
                    }
                }
                // else periodValidTo.compareTo(nextPeriodValidFrom) == 0 --> they are equal
            }
               
        }
        
        if (originalStates.size() == 0) {
            // creating a new state where no states existed
            DataproviderObject filler = 
                createSplitState(request.object(), request.object(), spec.getValidFrom(), spec.getValidTo(), spec, replace);
            assertRequiredAttributes(filler, root.path().getBase());
            modifyStates.add(filler);
        }

        assertStatesWithinRequest(modifyStates, spec, true);
        
        return modifyStates;
    }
    
    // --------------------------------------------------------------------------
    /**
     * merges states which are following each other and have the same values.
     * <p>
     * Check if the borderingStates can be merged with the first or the last state 
     * of states. If it can be merged, it has to be added to the
     * originalStates list, so that it will be removed later on.
     * <p>
     * The states provided may have holes in their validity.
     * 
     * @param states the new states with the new values
     * @param originalStates  original states which will be deleted later on
     * @param borderingStates states at the border of the updates valid period
     */
    private ArrayList mergeConsecutiveStates(
        ArrayList states,
        List originalStates,
        List borderingStates
    ) throws ServiceException {
        TreeMap fromMap = new TreeMap();

        String stateValidFrom = null;   
        
        DataproviderObject state = null;
        ArrayList reducedStates = new ArrayList();
        
        // order states
        for (Iterator i = states.iterator(); i.hasNext(); ) {
            state = (DataproviderObject)i.next();
            
            stateValidFrom = readNullableStringValue(state, validFromAttribute());
            
            fromMap.put(
                stateValidFrom == null ? EARLIEST_DATE : stateValidFrom,
                state
            );
        }
        
        DataproviderObject nextState = null;       
        state = null;
        for (Iterator p = fromMap.values().iterator(); p.hasNext(); ) {
            if (state == null) {
                // first state
                state = (DataproviderObject) p.next();
                
                boolean extended = false;
                // try extending the bordering states at start. Don't know the
                // sequence of the states, just try both
                if (borderingStates.size() > 0) {
                    DataproviderObject bordering = (DataproviderObject) 
                        borderingStates.get(0);
                    if (extendValidity(state, bordering, false)) {
                        // make sure that the bordering state gets invalidated
                        originalStates.add(bordering);
                        extended = true;
                    }
                }
                
                if (borderingStates.size() == 2 && 
                    !extended                       // only one can match at the beginning
                ) {
                    DataproviderObject bordering = (DataproviderObject) 
                        borderingStates.get(1);
                    if (extendValidity(state, bordering, false)) {
                        // make sure that the bordering state gets invalidated
                        originalStates.add(bordering);
                    }
                }                    
            }
            
            if (p.hasNext()) {
                nextState = (DataproviderObject) p.next();
                
                if (extendValidity(state, nextState, true)) {
                    // nothing to do; nextState was merged and is no longer needed
                }
                else {
                    reducedStates.add(state);
                    state = nextState; // try again with that
                }
            }
        }
            
        // last state
        reducedStates.add(state);
        
        boolean extended = false;
        // try extending the bordering states at end. Don't know the
        // sequence of the states, just try both
        if (borderingStates.size() > 0) {
            DataproviderObject bordering = (DataproviderObject) 
                borderingStates.get(0);
            if (extendValidity(state, bordering, true)) {
                // make sure that the bordering state gets invalidated
                originalStates.add(bordering);
                extended = true;
            }
        }
        
        if (borderingStates.size() == 2 && 
            !extended                       // only one can match at the end
        ) {
            DataproviderObject bordering = (DataproviderObject) 
                borderingStates.get(1);
            if (extendValidity(state, bordering, true)) {
                // make sure that the bordering state gets invalidated
                originalStates.add(bordering);
            }
        }
        
        return reducedStates;
    }                 

    // --------------------------------------------------------------------------
    /** 
     * Update root with instance number, validFrom, validTo, modifiedAt, 
     * modifiedTo, invalidatedAt.
     * 
     * @param header used for update
     * @param request original request
     * @param root   root object to update
     * @param spec    user and time for updates
     * @param rootAction  one of RootUpdateAction  
     */
    private void updateRoot(
        ServiceHeader header,
        DataproviderRequest request,
        DataproviderObject root, 
        UpdateSpec spec,
        int rootAction
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("updateRoot");
        boolean removeRoot;

        // first set validTo, validFrom
        switch (rootAction) {
            case (OP_SET_LIFETIME): {            
                // lifetime was adjusted validTo, validFrom show new
                // lifetime of object if they are not null
                String date = spec.getValidFrom();
                if (date != null) {
                    root.clearValues(validFromAttribute()).add(date);
                }
                date = spec.getValidTo();
                if (date != null) {
                    root.clearValues(validToAttribute()).add(toModelledValidTo(date));
                }
                root.clearValues(State_1_Attributes.INVALIDATED_AT);
                removeRoot = false;
                break;
            }
            case (OP_UPDATE_NORMAL): {
                // normal update, validFrom, validTo show only validity of request.
                String requestDate = spec.getValidFrom(); 
                String rootDate = readNullableStringValue(root, validFromAttribute());
                    
                if (compareAtStart(requestDate, rootDate) < 0) {
                    SparseList target = root.clearValues(validFromAttribute()); 
                    if (requestDate != null) target.add(requestDate);
                }
                
                requestDate = spec.getValidTo();
                rootDate = readNullableStringValue(root, validToAttribute());
                if (compareAtEnd(requestDate, rootDate) > 0) {
                    SparseList target = root.clearValues(validToAttribute()); 
                    if (requestDate != null) target.add(toModelledValidTo(requestDate));
                }
                root.values(State_1_Attributes.INVALIDATED_AT).clear();
                removeRoot = false;
                break;
            }
            case (OP_REMOVE):{
                // leave validFrom, validTo, but set invalidatedAt
                root.clearValues(State_1_Attributes.INVALIDATED_AT).add(spec.getModificationDate());
                removeRoot = this.isHistoryDisabled(root.path());
                break;
            }
            default: throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                new BasicException.Parameter[]{
                    new BasicException.Parameter("operationCode", rootAction),
                },
                "Unknown operation code in updateRoot."
            );
        }
        
        // maintain consistent times
        root.clearValues(SystemAttributes.MODIFIED_AT).add(spec.getModificationDate());        
        root.clearValues(SystemAttributes.MODIFIED_BY).addAll(spec.getModificationPrincipal());
        
        if(SysLog.isTraceOn()) SysLog.trace(
            removeRoot ? "remove" : "update",
            "Root: " + root.path() + 
            " validFrom: " + root.getValues(validFromAttribute()) +
            " validTo: " + root.getValues(validToAttribute()) +
            " invalidated: " + root.getValues(State_1_Attributes.INVALIDATED_AT) 
        );
        DataproviderRequest newRequest = new DataproviderRequest(
            root, 
            removeRoot ? DataproviderOperations.OBJECT_REMOVAL : DataproviderOperations.OBJECT_REPLACEMENT, 
            AttributeSelectors.NO_ATTRIBUTES,
            null
        );
        
        newRequest.contexts().putAll(request.contexts());
            
        if(removeRoot) {
            super.remove(
                header,
                newRequest
            );
        } else {
            super.replace(
                header,
                newRequest
            );
        }
        StopWatch_1.instance().stopTimer("updateRoot");

    }  

    // --------------------------------------------------------------------------
    /**
     * Set the invalidatedAt date to now. Those states have all been replaced
     * by newer ones.
     * <p>
     * if the request is present, it's settings are used for the access.
     * 
     * @param header  
     * @param states to invalidate
     * @param spec    user and time for updates
     * @param request  request to use if present
     * @param useRequest  use request's specification or dummy
     */
    private ArrayList invalidateStates(
        ServiceHeader header, 
        List states,
        UpdateSpec spec,
        DataproviderRequest request,
        boolean useRequest
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("invalidateStates");
        ArrayList replies = new ArrayList();
        Set removed = new HashSet();      
        for (Iterator i = states.iterator(); i.hasNext(); ) {
            DataproviderObject state = (DataproviderObject)i.next();
            Path path = state.path();
            if(isHistoryDisabled(path)) {
                if(removed.add(path)) {
                    DataproviderObject remove = new DataproviderObject(path);
                    SysLog.trace("removing state", remove.path());                
                    StopWatch_1.instance().startTimer("removeStates-DB");
                    DataproviderRequest newRequest = useRequest ? new DataproviderRequest(
                        remove,
                        DataproviderOperations.OBJECT_REMOVAL, 
                        request.attributeSelector(),
                        request.attributeSpecifier()
                    ) : new DataproviderRequest(
                        remove,
                        DataproviderOperations.OBJECT_REMOVAL, 
                        AttributeSelectors.NO_ATTRIBUTES,
                        null
                    );
                    newRequest.contexts().putAll(request.contexts());                    
                    newRequest.context(
                        DataproviderRequestContexts.OBJECT_TYPE
                    ).set(
                        0,
                        state.values(SystemAttributes.OBJECT_CLASS).get(0)
                    );
                    replies.add(
                        super.remove(
                            header,
                            newRequest
                        )        
                    ); 
                    StopWatch_1.instance().stopTimer("removeStates-DB");
                }
            } else {
                DataproviderObject update = new DataproviderObject(path);
                update.values(State_1_Attributes.INVALIDATED_AT).set(
                    0,
                    spec.getModificationDate()
//                    request.object().getValues(SystemAttributes.MODIFIED_AT).get(0)
                );
                SysLog.trace("invalidating state", update.path());
                StopWatch_1.instance().startTimer("invalidateStates-DB");
                DataproviderRequest newRequest = useRequest ? new DataproviderRequest(
                    update,
                    DataproviderOperations.OBJECT_REPLACEMENT, 
                    request.attributeSelector(),
                    request.attributeSpecifier()
                ) : new DataproviderRequest(
                    update,
                    DataproviderOperations.OBJECT_REPLACEMENT, 
                    AttributeSelectors.NO_ATTRIBUTES,
                    null
                );
                newRequest.contexts().putAll(request.contexts());
                newRequest.context(
                    DataproviderRequestContexts.OBJECT_TYPE
                ).set(
                    0,
                    state.values(SystemAttributes.OBJECT_CLASS).get(0)
                );
                replies.add(
                    super.replace(
                        header,   
                        newRequest
                    )        
                );
                StopWatch_1.instance().stopTimer("invalidateStates-DB");
            }
        }
        StopWatch_1.instance().stopTimer("invalidateStates");

        return replies;
    }
        
    // --------------------------------------------------------------------------
    /** 
     * Get the root of the dataprovider object.
     */
    private DataproviderObject getRoot(
        ServiceHeader header,
        DataproviderRequest request,
        UpdateSpec spec
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("getRoot");

        DataproviderRequest newRequest = 
            new DataproviderRequest(
                new DataproviderObject(spec.getObjectPath().getParent()),
                DataproviderOperations.ITERATION_START, 
                AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                null
            );
        newRequest.addAttributeFilterProperty(
            new FilterProperty(
                Quantors.THERE_EXISTS,
                SystemAttributes.OBJECT_IDENTITY,
                FilterOperators.IS_IN,
                new String[] { spec.getObjectPath().toUri() } 
            )
        );
        newRequest.contexts().putAll(request.contexts());
        
        DataproviderReply reply = 
            super.find(
                header,
                newRequest
            );
            
        StopWatch_1.instance().stopTimer("getRoot");
        if (reply.getObjects().length == 0) {
            return null;
        }
        else {
            return reply.getObject(); 
        }       
        
        
        /*p find rather then get to avoid exception
        DataproviderRequest newRequest = 
            new DataproviderRequest(
                new DataproviderObject(spec.getObjectPath()),
                DataproviderOperations.OBJECT_RETRIEVAL, 
                AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                null
            );
        
        newRequest.contexts().putAll(request.contexts());
        
        DataproviderReply reply = 
            super.get(
                header,
                newRequest
            );
            
        return reply.getObject();
        */


    }  
              
    //---------------------------------------------------------------------------
    /**
     * same as getRoot(), but assure that the root has not been invalidated.
     */          
    private DataproviderObject getValidRoot(
        ServiceHeader header,
        DataproviderRequest request,
        UpdateSpec spec
    ) throws ServiceException {
        DataproviderObject root = getRoot(header, request, spec);
        
        if (root == null ||
            (
            root.getValues(State_1_Attributes.INVALIDATED_AT) != null &&
            !root.getValues(State_1_Attributes.INVALIDATED_AT).isEmpty()
            )
        ) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND, 
                new BasicException.Parameter[]{
                    new BasicException.Parameter(
                        "request.path", 
                        request.path()
                    ),
                    new BasicException.Parameter(
                        "object.invalidatedAt", 
                        (root == null ?
                            "non existing object" :
                            root.getValues(State_1_Attributes.INVALIDATED_AT).toString()
                        )
                    )
                },
                "Accessing invalidated or non existing object for changing its state." 
            );
        }
            
        return root;
    }
   
    //---------------------------------------------------------------------------
    /** 
     * Get the path consisting of the references in the path supplied and remove
     * role entries still contained in path. 
     * <p>
     * role components may occur anywhere in the path if a role is the container
     * for other classes.
     * 
     * @param path 
     * 
     * @return new path
     */ 
    private Path getReferencePath(Path path) {
        Path result = new Path(path);
        
        for (int i = 0; i < result.size(); i++) {
            result.remove(i);
            if (i < result.size() && result.get(i).equals(RoleAttributes.REF_ROLE)){
                result.remove(i);
                if (i < result.size()) {
                    result.remove(i);
                }
            }
        }
        
        return result;
    }

    
    // --------------------------------------------------------------------------
    /** 
     * Determine if the path specified leads to stateful classes.
     * If the last reference of the path is validState or historyState the 
     * decision is taken without these endings.
     * 
     */
    protected boolean leadsTostatefulClass(Path path) {
        Path refPath = getReferencePath(path);
        
        if (refPath.getBase().equals(State_1_Attributes.REF_VALID) ||
            refPath.getBase().equals(State_1_Attributes.REF_HISTORY) ||
            refPath.getBase().equals(State_1_Attributes.REF_STATE)
        ) {
            refPath.remove(refPath.size()-1);
        }
        return this.statefulReferencePaths.containsKey(refPath);        
    }
   
    // --------------------------------------------------------------------------
    /**
     * Common method for replace and modify. Behaviour can be adjusted by 
     * isReplace.
     */ 
    private DataproviderReply update(
        ServiceHeader header, 
        DataproviderRequest request,
        boolean isReplace
    ) throws ServiceException {   
        DataproviderReply reply = null;
        DataproviderObject root = null;
        List originalStates = new ArrayList();
        ArrayList modifyStates = null;
        ArrayList stateReplies = null;
        boolean isLifetimeAdjust = false; 
        
        assertValidPathForChanges(request.path());
        cutStateSpecifiersFromPath(request.path());
            
        UpdateSpec spec = new UpdateSpec(
            header, 
            request, 
            validForPattern()
        );
        
        // TODO rework path treatment
        request.path().setTo(
            OperationParameter.removeAllOperationParameters(request.path()));

        
        assertStateAwareRequest(spec, request.object());
        assertValidDates(spec);

        // get Root for instanceNumber 
        root = getValidRoot(header, request, spec);
        
        if (isLifetimeRequest(request.object())) {
            SysLog.trace("lifetime request");
            isLifetimeAdjust = true;
            modifyStates = new ArrayList();
            boolean needExistingReturnState = false;
            
            if (readNullableStringValue(root, validFromAttribute()) == null &&
                readNullableStringValue(root, validToAttribute()) == null
            ) {
                // both are null which means that no changes at the lifetime 
                // should occur
                needExistingReturnState = true;
            }
            else {
                DataproviderObject cutInValidFrom = null; 
                DataproviderObject cutInValidTo = null; 
                // first at the start
                cutInValidFrom = createObjectBoundaryStates( 
                  header,
                  request,
                  request.object().path(),
                  root.getValues(validFromAttribute()),
                  request.object().getValues(validFromAttribute()),
                  true,
                  spec,
                  originalStates
                );
              
                // then at the end
                cutInValidTo = createObjectBoundaryStates( 
                  header,
                  request,
                  request.object().path(),
                  root.getValues(validToAttribute()),
                  request.object().getValues(validToAttribute()),
                  false,
                  spec,
                  originalStates
                );
              
                // check if it was the same state
                if (cutInValidFrom != null && 
                  cutInValidTo != null && 
                  cutInValidFrom.path().equals(cutInValidTo.path())
                ) {
                  cutInValidFrom.values(validToAttribute()).set(0, 
                      cutInValidTo.values(validToAttribute()).get(0)
                  );
                  cutInValidTo = null;
                  modifyStates.add(cutInValidFrom);
                }
                else if (cutInValidFrom == null && cutInValidTo == null) {
                    // both are null: no cutting took place; the old and the new
                    // boundaries are the same. Still need an object to return:
                    needExistingReturnState = true;
                }
                else {
                    if (cutInValidFrom != null) {
                        modifyStates.add(cutInValidFrom);
                    }
                  
                    if (cutInValidTo != null) {
                        modifyStates.add(cutInValidTo);
                    }
  
                }
           }
            
           if (needExistingReturnState) {
              
                // both are null which means that no changes at the lifetime should occur
                // need an object to return      
                DataproviderObject[] originalStatesArray = getInvolvedStates(
                    header,
                    request,
                    request.path(),
                    (String)request.object().values(validFromAttribute()).get(0),
                    (String)request.object().values(validToAttribute()).get(0),
                    OP_SET_LIFETIME, 
                    INCLUDE_STATE_AT_START, 
                    INCLUDE_STATE_AT_END
                );
                stateReplies = new ArrayList();
                for (int i = 0; i < originalStatesArray.length; i++) {
                    stateReplies.add(new DataproviderReply(originalStatesArray[i]));
                }
            }            
        }
        else {
            // normal update
            
            if (spec.getOperationParameter().isStateOperation() && 
                !spec.getOperationParameter().isPeriodOperation()
            ) {
                // update of a single state only if validFrom= , validTo= are 
                // not set. Otherwise its a normal update, based on another state.
                
                // get the required state to find the update period
                DataproviderObject stateToUpdate = 
                    getStateForStateOperationParameter(header, request, spec).getObject();
                
                if(readNullableStringValue(stateToUpdate, State_1_Attributes.INVALIDATED_AT) != null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        new BasicException.Parameter[]{
                            new BasicException.Parameter("path", request.path()),
                            new BasicException.Parameter("state", stateToUpdate.path()),
                            new BasicException.Parameter("invalidatedAt", stateToUpdate.values(State_1_Attributes.INVALIDATED_AT).get(0))
                            
                        },
                        "Updates of states are only allowed for valid states."
                    );
                }
                
                spec.setValidPeriod(
                    readNullableStringValue(stateToUpdate, validFromAttribute()),
                    readNullableStringValue(stateToUpdate, validToAttribute())
                );
            }
       
            DataproviderObject[] originalStatesArray = null;
            originalStatesArray = getInvolvedStates(
                header,
                request,
                spec.getObjectPath(),
                spec.getValidFrom(), // validFrom,
                spec.getValidTo(),   // validTo,
                OP_UPDATE_NORMAL, INCLUDE_STATE_AT_START, INCLUDE_STATE_AT_END
            );
            
            for (int i = 0; i < originalStatesArray.length; i++) {
                originalStates.add(originalStatesArray[i]);
            }
            ArrayList borderingStates = new ArrayList();
            
            modifyStates = 
                prepareInvolvedStates(request, root, originalStates, spec, isReplace, borderingStates);
            modifyStates = 
                mergeConsecutiveStates(modifyStates, originalStates, borderingStates);

        }
        if (stateReplies == null) {
            stateReplies = createStates(header, request, modifyStates, root);              
            invalidateStates(
                header, 
                originalStates, 
                spec, 
                request,
                false
            );             
                                
            // write root
            updateRoot(header, request, root, spec, isLifetimeAdjust ? OP_SET_LIFETIME : OP_UPDATE_NORMAL);
        }
        reply = findReply(header, request, stateReplies, spec);    
        
        if (isShowDBEnabled()) {
            showDB(header, request, request.path());
        }
        
        return reply;
    }
   
    // --------------------------------------------------------------------------
    /** 
     * find the correct reply to return according to the requestedAt. requestedFor 
     * settings of the request. 
     * 
     * If the object is not contained in the set it has to loaded from storage 
     * (tbd)
     * 
     * @param header  header for access to DB
     * @param request the original request containing requestedAt, requestedFor
     * @param replies  replies that have been created in this request.
     * 
     * @return DataproviderObject for the reply 
     */
    private DataproviderReply findReply(
        ServiceHeader header,
        DataproviderRequest request, 
        List replies,
        UpdateSpec spec
    ) throws ServiceException {
        DataproviderReply theReply = null;
        if(request.operation() == DataproviderOperations.OBJECT_REMOVAL) {
            theReply = new DataproviderReply(
                new DataproviderObject(
                    replies.isEmpty() ? 
                        request.path() :
                        ((DataproviderReply)replies.get(0)).getObject().path()
                )
           );
        } else {
            if (spec.isStateOperation() || spec.isPeriodOperation()) {
                // TODO for now just select the reply object from the set of replies
                // But if the state= indicates an state outside of the set, this 
                // state should be returned. Problem here: I don't think that 
                // this outside state is wanted in the reply (It had to be gotten 
                // by a "get for update" to be able to update it.)
                if (replies.size() == 1) {
                    theReply = (DataproviderReply)replies.get(0);
                }
                else if (replies.size() > 1) {
                    if (spec.getOperationParameter().getStateNumber() != null
                    ) {
                        // take any of the results
                        theReply = (DataproviderReply)replies.get(0);
                    }
                    else if (spec.getOperationParameter().getDate() != null){
                        theReply = searchReplyAtDate(
                            replies, spec.getOperationParameter().getDate(), true);
                    }
                    else if (spec.getOperationParameter().isFirst()) {
                        theReply = searchReplyAtDate(
                            replies, EARLIEST_DATE, true);
                    }
                    else if (spec.getOperationParameter().isLast() ||
                        spec.getOperationParameter().isUndef()
                    ) {
                        theReply = searchReplyAtDate(
                            replies, LATEST_DATE, true);
                    }
                }
            } 
            else if (replies.size() > 0){
                // no operation, normal behavior
                String requestedFor = getRequestedFor(header, request);             
                // Date requstedAt must be checked tbd
                        
                theReply = searchReplyAtDate(replies, requestedFor, false);
                
                if (theReply == null) {
                    if (replies.size() > 0) {
                        // this is only true for the old behavior
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED, 
                            new BasicException.Parameter[]{
                                new BasicException.Parameter("object", request.path()),
                                new BasicException.Parameter("requestedFor", requestedFor),
                                new BasicException.Parameter("request.validFrom", request.object().values(validFromAttribute())),
                                new BasicException.Parameter("request.validTo", request.object().values(validToAttribute()))
                            },
                            "requestedFor must be inside the period of the update"
                        );
                    }
                }
            }
            
            if (theReply == null) { 
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    new BasicException.Parameter[]{
                        new BasicException.Parameter(
                            "request", request),
                        new BasicException.Parameter(
                            "OperationQualifier", spec.getOperationParameter().getOperationQualifier()),
                        new BasicException.Parameter(
                            "replies.size", replies.size())
                    },
                    "no reply to return found."
                );
            }
            
            // the objects may contain an invalidatedAt entry if they were reused
            // from the invalidateStates() as used in remove()
            theReply.getObject().attributeNames().remove(State_1_Attributes.INVALIDATED_AT);
            
        }
        return theReply;            
    }
    
    /**
     * Search  Reply which lies at the date specified. Date may also be 
     * EARLIEST_DATE, LATEST_DATE or a real date. If EARLIEST_DATE or LATEST_DATE
     * or searchNearest is set, the corresponding Reply is searched. If a real 
     * date is specified and searchNearest is false, the reply's object must be
     * at the date specified, otherwise null is returned.
     * date == null means that a state with validFrom == null must be returned,
     * this is slightly different from date == EARLIEST_DATE; because there, 
     * just the first of the present states is returned.
     * 
     * @param replies
     * @param theReply
     * @param date
     * @return
     * @throws ServiceException 
     */
    private DataproviderReply searchReplyAtDate(
        List replies,
        String date,
        boolean searchNearest
    ) throws ServiceException {
        DataproviderReply reply;
        DataproviderReply theReply = null;
        
        String earliestFrom = LATEST_DATE;
        String latestFrom = EARLIEST_DATE;
        DataproviderReply earliestReply = null;
        DataproviderReply latestReply = null;
        
        String objectValidFrom;
        String objectValidTo;
        for (Iterator i = replies.iterator();
            i.hasNext() && theReply == null;
        ) {
            reply = (DataproviderReply) i.next();
            
            objectValidFrom = (String) reply.getObject().
                values(validFromAttribute()).get(0);
            objectValidTo = toExclusiveValidTo(
                (String) reply.getObject().values(validToAttribute()).get(0)
            );
            
            if (
                (
                    objectValidFrom == null  // valid since ever
                    ||
                    date == null
                    ||
                    (
                        date != null &&
                        date.compareTo(objectValidFrom) >= 0 
                    )
                )
                &&
                (
                    objectValidTo == null   // valid for ever
                    ||
                    date == null    // every validTo is larger than that
                    ||
                    date.compareTo(objectValidTo) < 0 
                )
            ) {
                // found the one with matching date
                theReply = reply;
            }    
            else {
                objectValidFrom = (objectValidFrom == null ? EARLIEST_DATE : objectValidFrom);
                objectValidTo = (objectValidTo == null ? LATEST_DATE : objectValidTo);
                
                if (objectValidFrom.compareTo(earliestFrom) <= 0) {
                    earliestFrom = objectValidFrom;
                    earliestReply = reply;
                }
                else if (objectValidFrom.compareTo(latestFrom) >= 0) {
                    latestFrom = objectValidFrom;
                    latestReply = reply;
                }
            }
        } 
            
        if (theReply == null && date != null) {
            // if date == null and none was found, then there is no matching
            if (date.equals(EARLIEST_DATE) 
                || (searchNearest && date.compareTo(earliestFrom) <= 0)
            ) {
                theReply = earliestReply;
            }
            else if (date.equals(LATEST_DATE)
                || (searchNearest && date.compareTo(latestFrom) >= 0)
            ) {
                theReply = latestReply;
            }
            else if (searchNearest) {
                // must be a hole in the validity, just return the latest
                theReply = latestReply;
            }
            // else reply stays null
        }
                    
        return theReply;
    }
            
        
    
    
    /**
     * Take the decision to create a new object or to replace an existing 
     * object based on the existence of the object, regardless of the valid 
     * period of the object. 
     * <p>
     * This requires to set "INTERCEPTION:propagateSet" to true in the 
     * configuration of the provider. 
     * <p>
     * NOTE: if the set request is handled only in the model layer, other 
     * plugins (application plugins or other standard plugins) may be required 
     * to handle the set() operation too. This may be of concern to plugins
     * which take some action on create() or replace() requests.
     */
    public DataproviderReply set(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("State_1.set");
        DataproviderReply reply = null;
        DataproviderObject root = null;
        boolean doCreate = false;
        
        if (isStateful(request.object())) {
            try {
                UpdateSpec spec = new UpdateSpec(
                    header, 
                    request, 
                    validForPattern()
                );
                root = getRoot(header, request, spec);
            }
            catch (ServiceException se) {
                // ignore
            }
    
            if (root != null 
                && 
                (root.getValues(State_1_Attributes.INVALIDATED_AT) == null 
                 ||
                 root.getValues(State_1_Attributes.INVALIDATED_AT).isEmpty()
                ) 
            ) {
                // if the root is still valid, the object can be modified
                doCreate = false;
            }
            else {
                // root invalid or no root at all: object must be created
                doCreate = true;
            }
        }
        else {
            StopWatch_1.instance().startTimer("state1SetSearch");
            DataproviderRequest newRequest = 
                new DataproviderRequest(
                    new DataproviderObject(request.path().getParent()),
                    DataproviderOperations.ITERATION_START, 
                    AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES,
                    null
                );
            newRequest.addAttributeFilterProperty(
                new FilterProperty(
                    Quantors.THERE_EXISTS,
                    SystemAttributes.OBJECT_IDENTITY,
                    FilterOperators.IS_IN,
                    new String[] { request.path().toUri()}
                )
            );
            newRequest.contexts().putAll(request.contexts());
        
            DataproviderObject[] existingObjs =
                super.find(
                    header,
                    newRequest
                ).getObjects();
            
            if (existingObjs.length == 0) {
                doCreate = true;
            }
            else {
                doCreate = false;
                request.object().setDigest(existingObjs[0].getDigest());    
            }
            StopWatch_1.instance().stopTimer("state1SetSearch");
         
            /*p find rather then get to avoid exception
            try {
                DataproviderObject existing = get(
                    header,
                    new DataproviderRequest(
                        new DataproviderObject(request.path()),
                        DataproviderOperations.OBJECT_RETRIEVAL,
                        AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES,
                        null
                    )
                ).getObject();
                doCreate = false;
                request.object().setDigest(
                    existing.getDigest()
                );
            } 
            catch (ServiceException exception) {
                doCreate = true;
            }
            */
        }
        
        
        if (doCreate) {
            request.object().clearValues(
                SystemAttributes.CREATED_BY
            ).addAll(
                request.object().values(SystemAttributes.MODIFIED_BY)
            );
            request.object().clearValues(
                SystemAttributes.CREATED_AT
            ).addAll(
                request.object().values(SystemAttributes.MODIFIED_AT)
            );
            
            reply = this.create(header, request); 
        }
        else {
            reply = this.replace(header, request);
        }
        
        StopWatch_1.instance().stopTimer("State_1.set");

        return reply;
    }
   
    // --------------------------------------------------------------------------
    /**
     * Enable getting objects which are stated. Either objects or
     * single states may be returned.
     * <p>
     * A normal get returns the object in it's state valid at header.requesetedAt,
     * header.requestedFor.
     * <p>
     * A get access by the reference BasicObjectHasState takes the requestedAt and
     * requestedFor date as its qualifier (<at-date>:<for-date>).
     * <p>
     * a get access by the reference BasicObjectHasValidState takes the 
     * requestedAt date as its qualifier. The requestedFor date is defined by 
     * the header.requestedFor.
     * <p>
     * a get access by the reference BasicObjectHasHistoryState takes the 
     * requestedFor date as its qualifier. The requestedAt date is defined by 
     * the header.requestedAt.
     * <p>
     * Get operations by these references return a state of the object (Path:
     * ..<objectPath>/[state|validState|historyState]/<id>
     * <p>
     * A get access can also contain operation parameters. Operation Parameters 
     * are added to the object id, seperated by a ";". The following operation 
     * parameters are supported
     * <ul> 
     * <li> 
     * validFrom=<date>;validTo=<date>, needed for update request to specify the
     * valid period of the data. A "get for update" must first be executed, to 
     * establish the object on the client side.
     * </li>
     * <li>
     * state=(<date>|<stateNumber>|first|last), allowing direct access to the
     * specified state.
     * </li>
     * <ul>
     * Combination of the two kind of parameters are possible. If  "state=" is
     * left out when getting with "validFrom=", "validTo=", "state=" defaults to 
     * "first".
     */
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("State_1.get");
        SysLog.trace("> State_1.get", request.path());

        DataproviderReply reply = null;
        boolean isStateful = false; 
        Path originalRequestPath = null;
        
        if (leadsTostatefulClass(request.path())) {
            // there are state enabled classes
            
            originalRequestPath = new Path(request.path());
            
            isStateful = true;
            UpdateSpec spec = new UpdateSpec(
                header, 
                request, 
                validForPattern()
            );
            
            // TODO rework path treatment
            request.path().setTo(
                OperationParameter.removeAllOperationParameters(request.path())
            );
            String reference = request.path().get(request.path().size()-2); 
            String qualifier = request.path().getBase();
            if (State_1_Attributes.REF_HISTORY.equals(reference)) {
                if (DATE_TIME_PATTERN.matcher(qualifier).matches()) {
                    String requestedFor = getRequestedFor(header);       
                    String requestedAt = qualifier;
                    String objectId = cutStateSpecifiersFromPath(request.path());
                    reply = findStates(
                        header, 
                        request, 
                        request.path().getParent(),
                        objectId,
                        requestedFor, 
                        requestedFor,
                        requestedAt, 
                        EXCLUDE_STATE_AT_START,
                        INCLUDE_STATE_AT_END,       
                        USE_REQUESTED_AT,  // have both, requestedAt and requestedFor
                        false, null
                    );
                } else {
                    // direct access to a state through it's number
                    reply = getStateDirect(header, request, null, null);
                }
            } else if (State_1_Attributes.REF_VALID.equals(reference)) {                
                if (DATE_TIME_PATTERN.matcher(qualifier).matches()) {
                    String requestedFor = qualifier;
                    String objectId = cutStateSpecifiersFromPath(request.path());
                    reply = findStates(
                        header, 
                        request, 
                        request.path().getParent(),
                        objectId,
                        requestedFor, 
                        requestedFor,
                        header.getRequestedAt(), 
                        EXCLUDE_STATE_AT_START,
                        INCLUDE_STATE_AT_END,
                        USE_REQUESTED_AT, false, null
                    );
                } else {
                    // direct access to a state through it's number
                    reply = getStateDirect(header, request, null, null);
                }
            } else if (State_1_Attributes.REF_STATE.equals(reference)) {
                if (AT_FOR_PATTERN.matcher(qualifier).matches()) {
                    // state qualifier must be at:for
                    PathComponent pathComp = new PathComponent(qualifier);                    
                    String requestedAt = pathComp.get(0); 
                    String requestedFor = pathComp.get(1); 
                    String objectId = cutStateSpecifiersFromPath(request.path());
                    reply = findStates(
                        header, 
                        request, 
                        request.path().getParent(),
                        objectId,
                        requestedFor, 
                        requestedFor,
                        requestedAt, 
                        EXCLUDE_STATE_AT_START,
                        INCLUDE_STATE_AT_END,
                        USE_REQUESTED_AT, false, null
                    );
                } else {
                    // direct access to a state through it's number
                    reply = getStateDirect( header, request, null, null);
                }
            }
            else if (spec.getOperationParameter().isPeriodOperation()  // OperationParameter.hasStateParameter(request.path().getBase())
                || spec.getOperationParameter().isStateOperation()      // OperationParameter.hasPeriodParameter(request.path().getBase())
            ) {
                SysLog.trace("operation", request.path().getBase());
                // get the required state
                reply = getStateForStateOperationParameter(header, request, spec);
            } else if (
                this.defaultsToInitialState &&
                header.getRequestedFor() == null
            ) {
                String base = request.path().getBase() + (";" + State_1_Attributes.OP_STATE + "=0");
                SysLog.trace("initial state", base);
                reply = getStateForStateOperationParameter(
                    header, 
                    request, 
                    new UpdateSpec(
                        header,
                        request,
                        validForPattern(),
                        base
                    )
                );
            } else {
                String requestedFor = getRequestedFor(header); 
                String objectId = cutStateSpecifiersFromPath(request.path());
                
                reply = findStates(
                    header, 
                    request, 
                    request.path().getParent(),
                    objectId,
                    requestedFor, 
                    requestedFor,
                    header.getRequestedAt(), 
                    EXCLUDE_STATE_AT_START,
                    INCLUDE_STATE_AT_END,
                    USE_REQUESTED_AT, 
                    false
, null // attribute specifiers
                );
            }            
                            
            // there should be found exactly one
            if (reply.getObjects().length < 1) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND, 
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("request", request)
                    },
                    "Object not found. Perhaps no valid state at requested_at, requested_for."
                );
            }
            else if (reply.getObjects().length > 1) {
                // found more then one entry
                // error in code or DB
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("header.requestedAt", header.getRequestedAt()),
                        new BasicException.Parameter("header.requestedFor", header.getRequestedFor()),
                        new BasicException.Parameter("request", request),
                        new BasicException.Parameter("objects received", reply.getObjects())
                    },
                    "Request should lead to only one state."
                );
            } 
            
            if (isShowDBEnabled()) {
                showDB(header, request, request.path());
            }
        }
        else {
            reply = super.get(header, request);
        }
        
        StopWatch_1.instance().stopTimer("State_1.get");
        SysLog.trace("< State_1.get", request.path());

        return completeReply(
            request,
            reply, 
            isStateful, 
            null, 
            originalRequestPath, 
            true
        );
    }


    // --------------------------------------------------------------------------
    /**
     * Enable the search for objects which are stated. Either objects or
     * single states may be returned.
     * <p>
     * The time point, at which a find request is executed is by default defined
     * by header.requestedAt, header.requestedFor. If they are not set, both
     * take as default the current time point. This ensures that one gets the 
     * current states of the objects. 
     * <p>
     * If one or more of the time-attributes object_validFrom, object_validTo, 
     * object_invalidatedAt, object_createdAt or object_modifiedAt are used as
     * filters, no longer objects but states of objects are returned (Path: 
     * <objectPath>/state/<stateId>). There may be more than one state per 
     * object which matches the filters. 
     * <p>
     * If object_validFrom or object_validTo occur as filter, header.requestedAt
     * is used for setting the time point. If one of the other time-attributes 
     * is used, header.requestedFor is used as time point. If filter for both
     * kind of time-attributes are present, none of the header contexts is used.
     * If none of the time-attributes is used as filter, it's a normal search 
     * for objects, using the time point specified by header.requestedAt, 
     * header.requestedFor.
     * <p>
     * A stated object has the derived references BasicObjectHasState,
     * BasicObjectHasValidState and BasicObjectHasHistoryState. These are 
     * handled here. A find on BasicObjectHasState searches within all the 
     * states of an object. A find on BasicObjectHasValidState searches within
     * all states valid at header.requestedAt (object_modifiedAt, 
     * object_invalidatedAt). A find on 
     * BasicObjectHasHistoryState searches within all states valid at 
     * header.requestedFor (object_validFrom, object_validTo).
     * 
     */
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("State_1.find");
        SysLog.trace("> State_1.find", request.path());

        DataproviderReply reply = null;
        Path requestPath = request.path(); // for non stated iteration continuation
        boolean useExtent = requestPath.isLike(EXTENT_PATTERN);
        boolean isStateful = request.operation() == DataproviderOperations.ITERATION_START && leadsTostatefulClass(
            useExtent ? getIdentity(request) : requestPath
        );
        String idCompletion = null;
        Path originalRequestPath = null;

        if(isStateful) {
            //
            // there are state enabled classes
            //
            originalRequestPath = new Path(request.path());
            UpdateSpec spec = new UpdateSpec(
                header, 
                request, 
                validForPattern()
            );
            requestPath = spec.getObjectPath();

            if (requestPath.getBase().equals(State_1_Attributes.REF_HISTORY)) {
                // a find for different states of a single object
                // Request for states which have been valid at a certain 
                // time (state.validFrom, state.validTo)
                String requestedFor = getRequestedFor(header);                    
                String objectId = cutStateSpecifiersFromPath(requestPath);
                // change path to represent the path used for searching. 
                // like that the path will be the same in an iteration continuation
                // and Database must not complain.
                requestPath.remove(requestPath.size() -1 );
                reply = findStates(
                    header,
                    request, 
                    requestPath, // request.path().getParent();
                    objectId,
                    requestedFor,
                    requestedFor,
                    null,
                    EXCLUDE_STATE_AT_START,
                    INCLUDE_STATE_AT_END,
                    OMIT_REQUESTED_AT, 
                    useExtent, new AttributeSpecifier[] { 
                        new AttributeSpecifier(
                            SystemAttributes.MODIFIED_AT, 
                            0, 
                            Directions.ASCENDING
                        )
                    }
                );
                
                sortStates(reply, SystemAttributes.MODIFIED_AT);
                idCompletion = State_1_Attributes.REF_HISTORY;
                
            } else if (requestPath.getBase().equals(State_1_Attributes.REF_VALID)) {
                // a find for different states of a single object.
                // Request for entire object which has been valid at a certain
                // time. (state.invalidatedAt) 

                String objectId = cutStateSpecifiersFromPath(requestPath);
                requestPath.remove(requestPath.size() -1 );
                reply = findStates(
                    header,
                    request, 
                    requestPath,
                    objectId,
                    null,
                    null,
                    header.getRequestedAt(),
                    EXCLUDE_STATE_AT_START,
                    INCLUDE_STATE_AT_END,
                    USE_REQUESTED_AT, 
                    useExtent, new AttributeSpecifier[] { 
                        new AttributeSpecifier(
                            validFromAttribute(), 
                            0, 
                            Directions.ASCENDING
                        )
                    }                   
                );                
                sortStates(reply, validFromAttribute());
                idCompletion = State_1_Attributes.REF_VALID;
            } else if (requestPath.getBase().equals(State_1_Attributes.REF_STATE)) {
                // a find for different states of a single object
                // Request for all states. Can be further narrowed by
                // attributeFilters on modifiedAt, invalidatedAt, valid_from, valid_to
                
                String objectId = cutStateSpecifiersFromPath(requestPath);
                requestPath.remove(requestPath.size() -1 );
                
                reply = findStates(
                    header,
                    request, 
                    requestPath,
                    objectId,
                    null,
                    null,
                    null,
                    EXCLUDE_STATE_AT_START,
                    INCLUDE_STATE_AT_END,
                    OMIT_REQUESTED_AT, 
                    useExtent, new AttributeSpecifier[] { 
                        new AttributeSpecifier(
                            SystemAttributes.MODIFIED_AT, 
                            0, 
                            Directions.ASCENDING
                        ),
                        new AttributeSpecifier(

                            validFromAttribute(), 
                            0, 
                            Directions.ASCENDING
                        )
                    }                   
                );                
                sortStates(reply, validFromAttribute());
                idCompletion = State_1_Attributes.REF_STATE;
            } else if (this.isStateRequest(request)) {
                // a find for different states of a single object
                // Request for all states. Can be further narrowed by
                // attributeFilters on modifiedAt, invalidatedAt, valid_from, valid_to
                
                reply = findStates(
                    header,
                    request, 
                    requestPath,
                    requestPath.size() % 2 == 0 ? null : requestPath.getBase(),
                    null,
                    null,
                    null,
                    EXCLUDE_STATE_AT_START,
                    INCLUDE_STATE_AT_END,
                    OMIT_REQUESTED_AT, 
                    useExtent, new AttributeSpecifier[] { 
                        new AttributeSpecifier(
                            SystemAttributes.MODIFIED_AT, 
                            0, 
                            Directions.ASCENDING
                        ),
                        new AttributeSpecifier(

                            validFromAttribute(), 
                            0, 
                            Directions.ASCENDING
                        )
                    }                   
                );                
                sortStates(reply, validFromAttribute());
                idCompletion = OP_STATE_EXTENDED;
            } else {
                //
                // find for states of different objects:
                //
                
                boolean historyFilter  = hasHistoryFilter(request);
                boolean validityFilter = hasValidityFilter(request);
                String requestedFor = validityFilter ? null : getRequestedFor(header);
                reply = findStates(
                    header, 
                    request, 
                    requestPath,
                    null, // objectId
                    requestedFor, // validFrom
                    requestedFor, // validTo
                    header.getRequestedAt(),
                    EXCLUDE_STATE_AT_START,
                    INCLUDE_STATE_AT_END,
                    historyFilter ? OMIT_REQUESTED_AT : USE_REQUESTED_AT, 
                    useExtent, new AttributeSpecifier[] { 
                        new AttributeSpecifier(
                            ID_ATTRIBUTE_NAME, 
                            0, 
                            Directions.ASCENDING
                        ),
                        new AttributeSpecifier(
                            SystemAttributes.MODIFIED_AT, 
                            0, 
                            Directions.ASCENDING
                        ),
                        new AttributeSpecifier(

                            validFromAttribute(), 
                            0, 
                            Directions.ASCENDING
                        )
                    }
                );
                
                if (validityFilter || historyFilter) {
                    // anything, just make it differ from REF_STATE
                    idCompletion = OP_STATE_EXTENDED; 
                }
            }
            
        } else {
            if (request.operation() == DataproviderOperations.ITERATION_CONTINUATION) {
                originalRequestPath = new Path(request.path());
                
                State_1Iterator iterator = (State_1Iterator) AbstractIterator.deserialize(
                    (byte[])request.context(DataproviderReplyContexts.ITERATOR).get(0)
                );
                // replace my context by the old one
                request.context(DataproviderReplyContexts.ITERATOR)
                    .set(0, iterator.getIterator());
                isStateful = iterator.isStateful();
                idCompletion = iterator.getIdCompletion();
                requestPath = iterator.getInternalRequestPath();
                request.object().path().setTo(requestPath);
                
            }
            reply = super.find(header, request);
        }
        
        // add my context containing the old one
        reply.context(DataproviderReplyContexts.ITERATOR).set(0,
            AbstractIterator.serialize(
                new State_1Iterator(
                    isStateful, 
                    idCompletion,
                    requestPath,
                    (byte[])reply.context(DataproviderReplyContexts.ITERATOR).get(0)
                )
            )
        );
        
        DataproviderReply finalReply = completeReply(
            request,
            reply, 
            isStateful, 
            idCompletion, 
            useExtent ? null : originalRequestPath, 
            false
        );
            
        SysLog.trace("reply count", new Integer(finalReply.getObjects().length));
        StopWatch_1.instance().stopTimer("State_1.find");
        SysLog.trace("< State_1.find", request.path());
        return finalReply;
    }

    protected DataproviderRequest discardStateRequestIndicator(
        DataproviderRequest request
    ){
        int workAround = State_1_Attributes.indexOfStatedObject(request.attributeFilter()); 
        if( workAround < 0) {
            return request;
        } else {
            List attributeFilters = new ArrayList(
                Arrays.asList(request.attributeFilter())
            );
            attributeFilters.remove(workAround);
            return new DataproviderRequest(
                request.object(),
                request.operation(),
                (FilterProperty[]) attributeFilters.toArray(
                    new FilterProperty[attributeFilters.size()]
                ),
                request.position(),
                request.size(),
                request.direction(),
                AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                request.attributeSpecifier()
            );
        }
    }
    
    /**
     * Retrieves the extent request's identity filter
     * 
     * @param request
     * 
     * @returnthe extent request's identity filter
     * 
     * @throws ServiceException
     */
    private static final Path getIdentity(
        DataproviderRequest request
    ) throws ServiceException{
        FilterProperty[] filters = request.attributeFilter();
        for(
            int i = 0;
            i < filters.length;
            i++
        ) if(SystemAttributes.OBJECT_IDENTITY.equals(filters[i].name())) {
            List values = filters[i].values();
            if(values == null || values.isEmpty()) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("path", request.path())
                },
                "An extent request's identity filter is empty"
            ); 
            Object identity = values.get(0);
            try {
                return new Path((String)identity);
            } catch (Exception exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("path", request.path()),
                        new BasicException.Parameter(SystemAttributes.OBJECT_IDENTITY, identity)
                        
                    },
                    "An extent request's identity filter value can't be interpreted as path"
                );
            }
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            new BasicException.Parameter[]{
                new BasicException.Parameter("path", request.path())
                
            },
            "An extent request has no identity filter"
        );
    }
    
    // --------------------------------------------------------------------------
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("State_1.create");
        SysLog.trace("> State_1.create", request.path());
        
        DataproviderReply reply = null;
        boolean isStateful = isStateful(request.object());
        Path originalRequestPath = null;
        
        if (isStateful) {
            //
            // is it a stateful object
            //
            DataproviderObject root = null;
            ArrayList states = new ArrayList();
            ArrayList replies = null;
            boolean mustUpdateRoot = false; 
            originalRequestPath = new Path(request.path());
            
            assertValidPathForChanges(request.path());
            
            UpdateSpec spec = new UpdateSpec(
                header, 
                request, 
                validForPattern()
            );
            
            // allow creation for non stated clients!
            // assertStateAwareRequest(spec, request.object());
            
            assertValidDates(spec);
            try {
                // create root object containing instanceNumber 
                root = createRoot(header, request, request.object(), spec); // this also detects duplicates!
            }
            catch (ServiceException se) {
                if (se.getExceptionCode() == BasicException.Code.DUPLICATE) {
                    // assume it was created and removed before. Try getting 
                	// all the valid states. If there are none it can be recreated
                	
                	DataproviderObject[] existingStates = getInvolvedStates(
            	       header,
            	       request,
            	       spec.getObjectPath(),
            	       enableDisjunctStateCreation ? spec.getValidFrom() : null, 
        	           enableDisjunctStateCreation ? spec.getValidTo() : null,
            	       OP_CREATE, 
            	       enableDisjunctStateCreation ? EXCLUDE_STATE_AT_START : INCLUDE_STATE_AT_START, 
                       enableDisjunctStateCreation ? EXCLUDE_STATE_AT_END : INCLUDE_STATE_AT_END
                	);
                	
                	if (existingStates.length > 0) {
                	    throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.DUPLICATE, 
                            new BasicException.Parameter[]{
                                new BasicException.Parameter("request.path", request.path()),
                            },
                            "Trying to create object which exists and is still valid."
                        );
                	}
                	
                	root = getRoot(header, request, spec);
                	
                    mustUpdateRoot = true;
                }
                else {
                    // it's another exception, just rethrow
                    throw se;
                }
            }
            DataproviderObject firstState = new DataproviderObject(request.object());
            // create object with name id#instanceNumber
            
            // set the path to get rid of operationParameter if any
            firstState.path().setTo(spec.getObjectPath());
                
            // set validFrom/To regardless if it is set in the object or the 
            // request's operation parameters.
            firstState.clearValues(validFromAttribute()).add(spec.getValidFrom());
            firstState.clearValues(validToAttribute()).add(toModelledValidTo(spec.getValidTo()));
            
            states.add(firstState);
            replies = createStates(header, request, states, root);
            
            if (mustUpdateRoot) {
                updateRoot(header, request,  root, spec, OP_UPDATE_NORMAL);
            }
            // else 
                // can leave it out because there is just one state created,
                // which is taken into account in creation of root.
            
            //
            reply = findReply(header, request, replies, spec); 
            
            if (isShowDBEnabled()) {
                showDB(header, request, spec.getObjectPath());
            }
        }      
        else {
            reply = super.create(header,request);
        }
        
        StopWatch_1.instance().stopTimer("State_1.create");
        SysLog.trace("< State_1.create", request.path());
        return completeReply(
            request,
            reply, 
            isStateful, 
            null, 
            originalRequestPath, 
            true
        );
    }
    
    // --------------------------------------------------------------------------
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("State_1.modify");
        SysLog.trace("> State_1.modify", request.path());
        
        DataproviderReply reply = null;
        boolean isStateful = false;
        // no matter what, the reply path must always be the same as the request path
        Path originalRequestPath = new Path(request.path());
                
        // is it a stateful object
        if (isStateful(request.object())) {
            isStateful = true;
            
            reply = update(header, request, false);
        }      
        else {
            reply = super.modify(header,request);
        }
        StopWatch_1.instance().stopTimer("State_1.modify");
        SysLog.trace("< State_1.modify", request.path());
        return completeReply(
            request,
            reply, 
            isStateful, 
            null, 
            originalRequestPath, 
            true
        );
    }

    // --------------------------------------------------------------------------
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("State_1.replace");
        SysLog.trace("> State_1.replace", request.path());

        DataproviderReply reply = null;
        boolean isStateful = false;
        // no matter what, the reply path must always be the same as the request path
        Path originalRequestPath = new Path(request.path());

        if (isStateful(request.object())) {
            isStateful = true;
            
            reply = update(header, request, true);
        }      
        else {
            reply = super.replace(header,request);
        }
        StopWatch_1.instance().stopTimer("State_1.replace");
        SysLog.trace("< State_1.replace", request.path());
        
        return completeReply(
            request,
            reply, 
            isStateful, 
            null, 
            originalRequestPath, 
            true
        );
    }

    // --------------------------------------------------------------------------
    public DataproviderReply remove(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("State_1.remove");
        SysLog.trace("> State_1.remove", request.path());

        DataproviderReply reply = null;
        boolean isStateful = false;
        Path originalRequestPath = new Path(request.path());
        
        if (leadsTostatefulClass(request.path())) {
            // there may be stateful classes but also non stateful
            
            assertValidPathForChanges(request.path());
            
            UpdateSpec spec = new UpdateSpec(
                header, 
                request, 
                validForPattern()
            );
            
            // not possible with remove requests (they do not have any data)
            // assertStateAwareRequest(spec, request.object());
            assertValidDates(spec);

            DataproviderObject root = getValidRoot(header, request, spec);
            
            if (isStateful(root)) {
                isStateful = true;
                ArrayList invalidationReplies = null;
                ArrayList originalStates = new ArrayList();
                DataproviderObject[] originalStatesArray = null;
                // no matter what, the reply path must always be the same as the request path
                originalRequestPath = new Path(request.path());
                
                if (spec.isStateOperation() && !spec.isPeriodOperation()) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_IMPLEMENTED,
                        null,
                        "remove request for operation state= is not implemented, so far."
                    );
                }
                       
                originalStatesArray = 
                    getInvolvedStates(header, request, spec.getObjectPath(), spec.getValidFrom(), spec.getValidTo(), OP_REMOVE, INCLUDE_STATE_AT_START, INCLUDE_STATE_AT_END);

                for (int i = 0; i < originalStatesArray.length; i++) {
                    originalStates.add(originalStatesArray[i]);
                }
                
                if (spec.getValidFrom() == null &&
                    spec.getValidTo() == null
                ) {
                    // the whole object is removed
                    invalidationReplies = invalidateStates(
                        header, 
                        originalStates,
                        spec,
                        request,
                        true
                    );
                                    
                    // write root
                    updateRoot(header, request, root, spec, OP_REMOVE);
                    
                    reply = findReply(header, request, invalidationReplies, spec);  
                }
                else {
                    // only part of the object is removed
                    if (originalStatesArray.length == 0) {
                        // no valid states at the time to invalidate were found
                        
                        // TODO: What now?
                        // must execute a search to find a state to return?
                        // or throw an exception because there couldn't be found
                        // any involved States ?
                        
                        // I think it fits more to return any state
                        DataproviderObject[] states = 
                            getInvolvedStates(
                                header, 
                                request, 
                                spec.getObjectPath(), 
                                null, 
                                null, 
                                OP_REMOVE, INCLUDE_STATE_AT_START, INCLUDE_STATE_AT_END
                            );
                        
                        if (states.length == 0) {
                            // TODO how to handle this?
                            // deleting all valid states of an object with an 
                            // operation doesn't invalidate the object! 
                            // The object can't be recreated and can't be deleted!
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                new BasicException.Parameter[] {
                                    new BasicException.Parameter("object path", spec.getObjectPath()),
                                    new BasicException.Parameter("operationQualifier", spec.getOperationParameter().getOperationQualifier())
                                },
                                "no more valid states for object to return."
                            );
                        }
                        
                        // return any of the states found
                        reply = new DataproviderReply(states[0]);
   
                    }
                    else  {
                        // found existing states to invalidate, must create 
                        // new edges by cutting existing states
                        
                        List modifyStates = new ArrayList();
                        // note: need inverse setting for atStart as in 
                        // with lifetime setting
                        DataproviderObject edge = createLifetimeEdgeState(
                            false, spec.getValidFrom(), originalStatesArray, spec);
                        
                        if (edge != null) {
                            modifyStates.add(edge);
                        }
                        
                        edge = createLifetimeEdgeState(
                            true, spec.getValidTo(), originalStatesArray, spec);
                        
                        if (edge != null) {
                            modifyStates.add(edge);
                        }
                            
                        // now create the two new states:
                        createStates(header, request, modifyStates, root);
                        
                        invalidationReplies = invalidateStates(
                            header, 
                            originalStates,
                            spec,
                            request,
                            true
                        );
                                            
                        updateRoot(header, request, root, spec, OP_UPDATE_NORMAL);
                        
                        reply = findReply(header, request, invalidationReplies, spec); 
                        /*
                        // create empty reply object 
                        DataproviderObject replyObject = 
                            new DataproviderObject(
                                (DataproviderObject)modifyStates.get(0),
                                false
                            );
                        replyObject.values(SystemAttributes.OBJECT_CLASS).addAll(
                            ((DataproviderObject)modifyStates.get(0)).getValues(SystemAttributes.OBJECT_CLASS)
                        );
                        */
                    }
                }
                
                if (isShowDBEnabled()) {
                    showDB(header, request, request.path());
                }
            }
        }
        
        if (!isStateful) {
            reply = super.remove(header,request);
        }
        StopWatch_1.instance().stopTimer("State_1.remove");
        SysLog.trace("< State_1.remove", request.path());

        return completeReply(
            request,
            reply, 
            isStateful, 
            null, 
            originalRequestPath, 
            true
        );
    }

    // --------------------------------------------------------------------------
    // Variables
    // --------------------------------------------------------------------------
    private Model_1_0 _model = null;
    
    // reference paths leading to objects with state enabled
    private HashMap statefulReferencePaths = null;
    private Path[] disableHistoryReferencePatterns;
    
    /**
     * longest path which is allowed before the search for stated path exits.
     * Only reference elements (not identifiers) are counted.
     */
    private static final int PATHLENGTH_THRESHOLD = 10; 
    
    private static final int OP_SET_LIFETIME = 0;
    private static final int OP_UPDATE_NORMAL = 1;
    private static final int OP_REMOVE = 2;
    private static final int OP_CREATE = 3;
        
    // undef is set to the operation Parameter if no other setting is present
    // for state=
    private static final String OP_STATE_UNDEF = "undef";
    private static final String OP_STATE_EXTENDED = ";"+ State_1_Attributes.OP_STATE + "=";
        
    // for readability in calls to findStates()
    private static final boolean USE_REQUESTED_AT = true;
    private static final boolean OMIT_REQUESTED_AT = false;
    
    private static final boolean EXCLUDE_STATE_AT_START = true;
    private static final boolean INCLUDE_STATE_AT_START = false;

    private static final boolean EXCLUDE_STATE_AT_END = true;
    private static final boolean INCLUDE_STATE_AT_END = false;
    
    // need those in Standard
    protected static final String STATE_NUMBER = "object_stateNumber";
    protected static final String ID_ATTRIBUTE_NAME = "object_stateId";
    
    private static final String AUTHORITY_TYPE_NAME = "org:openmdx:base:Authority";
    protected static final String STATE_TYPE_NAME = "org:openmdx:compatibility:state1:State";
    private static final String ROLE_TYPE_NAME = "org:openmdx:compatibility:role1:Role";
    private static final String VIEW_TYPE_NAME = "org:openmdx:compatibility:view1:View";
    
    // earliest date can be used in some places for null start date
    private static final String EARLIEST_DATE = "0000";
    
    // latest date can be used in some places for null end date
    private static final String LATEST_DATE = "9999";
    
    // dummy string for identity search which shouldn't be found
    /**
     * Dummy strings for identity search, providing a minimal and a maximal 
     * value for comparing searches (lower, greater, in between).  
     * 
     * Works fine provided that no object id is less or greater than the values...
     */  
    private static final String IDENTITY_UNDERFLOW = "        ;underflow;State_1";
    private static final String IDENTITY_OVERFLOW = "~~~~~~~~;overflow;State_1";
    
    private boolean defaultsToInitialState = false;
    private boolean enableDisjunctStateCreation = false;
    private boolean enableHolesInValidity = true;
    private boolean enableShowDB = false;
    
    protected static final Path EXTENT_PATTERN = new Path(
        new String[]{":*","provider",":*","segment",":*","extent"}
    );

    /**
     * CR20006542 uses DATE_TIME_PATTERN instead of NUMBER_PATTERN for shared 
     * association access type discrimination.
     */
    protected static final Pattern DATE_TIME_PATTERN = Pattern.compile(
        "^[0-9]{8}+T[0-9]{6}\\.[0-9]{3}Z$"
    );

    /**
     * CR20006542 uses AT_FOR_PATTERN instead of NUMBER_PATTERN for shared 
     * association access type discrimination.
     */
    protected static final Pattern AT_FOR_PATTERN = Pattern.compile(
        "^[0-9]{8}+T[0-9]{6}\\.[0-9]{3}Z:[0-9]{8}+T[0-9]{6}\\.[0-9]{3}Z$"
    );
    
    protected static final DateFormat DATE_TIME_FORMAT = DateFormat.getInstance();

    /**
     * CR20006542 removes 7 digit limit on state numbers 
     */
    protected static final Pattern NUMBER_PATTERN = Pattern.compile("^[0-9]+$");

    private static final List DEFAULT_STATE_VALUES = Arrays.asList(
        new String[]{
            null,
            LayerConfigurationEntries.CURRENT_STATE,
            LayerConfigurationEntries.INITIAL_STATE
        }
    );
    
}

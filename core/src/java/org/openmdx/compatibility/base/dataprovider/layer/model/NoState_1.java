/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: NoState_1.java,v 1.11 2008/09/10 08:55:21 hburger Exp $
 * Description: NoState_1 
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:21 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;


/**
 * NoState_1
 */
@SuppressWarnings("unchecked")
public class NoState_1
extends Standard_1
{


    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.Standard_1#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception {
        super.activate(id, configuration, delegation);
        this.singleStateMode = configuration.isOn(LayerConfigurationEntries.SINGLE_STATE_MODE);
        this.implicitelyNull = this.singleStateMode ? 
            IMPLICITELY_NULL_IN_SINGLE_STATE_MODE :
                IMPLICITELY_NULL_IN_OTHER_MODE;    
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.Standard_1#prepareObject(org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject)
     */
    private void prepareObject(
        DataproviderRequest request, 
        DataproviderObject object, 
        Set instanceOf
    ) throws ServiceException {
        if(
                instanceOf != null &&
                instanceOf.contains("org:openmdx:compatibility:state1:BasicState")
        ) {
            assertFeatureIsNull(object, State_1_Attributes.INVALIDATED_AT, false);
            assertFeatureIsNull(object, State_1_Attributes.STATED_OBJECT, true);
            assertFeatureIsNull(object, State_1_Attributes.UNDERLYING_STATE, true);
            if(!this.singleStateMode) {
                if(instanceOf.contains("org:openmdx:compatibility:state1:DateState")){
                    assertFeatureIsNull(object, State_1_Attributes.STATE_VALID_FROM, false);
                    assertFeatureIsNull(object, State_1_Attributes.STATE_VALID_TO, false);
                } else if(
                        instanceOf.contains("org:openmdx:compatibility:state1:DateTimeState") ||
                        instanceOf.contains("org:openmdx:compatibility:state1:DateStateExcludingEnd") 
                ){
                    assertFeatureIsNull(object, State_1_Attributes.VALID_FROM, false);
                    assertFeatureIsNull(object, State_1_Attributes.VALID_TO, false);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.Standard_1#completeObject(org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest, org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject)
     */
    private void completeObject(
        DataproviderRequest request, 
        DataproviderObject object,
        Set instanceOf
    ) throws ServiceException {
        if(instanceOf != null) {
            if(instanceOf.contains("org:openmdx:compatibility:state1:BasicState")) {
                object.values(State_1_Attributes.INVALIDATED_AT);
                object.values(State_1_Attributes.STATED_OBJECT);
                object.values(State_1_Attributes.UNDERLYING_STATE);
                short operation = request.operation();
                if(
                        DataproviderOperations.ITERATION_START == operation ||
                        DataproviderOperations.ITERATION_CONTINUATION == operation
                ) {
                    Path path = object.path();
                    String[] components = path.getSuffix(0);
                    components[components.length - 1] += ';' + State_1_Attributes.OP_STATE + "=0";
                    path.setTo(new Path(components));
                    //          } else {
                    //              String qualifier = object.path().getBase();
                    //              if (AbstractState_1.OperationParameter.hasPeriodParameter(qualifier)) {
                    //                  String[] components = object.path().getSuffix(0);
                    //                  components[components.length - 1] += 
                    //                      ';' + State_1_Attributes.OP_VALID_FROM + '=' + State_1_Attributes.OP_VAL_EVER +
                    //                      ';' + State_1_Attributes.OP_VALID_TO + '=' + State_1_Attributes.OP_VAL_EVER;
                    //                  object.path().setTo(new Path(components));
                    //              }
                }
                if(!this.singleStateMode) {
                    if(instanceOf.contains("org:openmdx:compatibility:state1:DateState")){
                        object.values(State_1_Attributes.STATE_VALID_FROM);
                        object.values(State_1_Attributes.STATE_VALID_TO);
                    } else if(
                            instanceOf.contains("org:openmdx:compatibility:state1:DateTimeState") ||
                            instanceOf.contains("org:openmdx:compatibility:state1:DateStateExcludingEnd") 
                    ){
                        object.values(State_1_Attributes.VALID_FROM);
                        object.values(State_1_Attributes.VALID_TO);
                    }
                }
                object.clearValues(
                    SystemAttributes.CONTEXT_PREFIX + State_1_Attributes.STATE_CONTEXT + ':' + SystemAttributes.OBJECT_CLASS
                ).add(
                    "org:openmdx:compatibility:state1:StateIncapable"
                );
            }
            if(instanceOf.contains("org:openmdx:base:ExtentCapable")) {
                object.clearValues(SystemAttributes.OBJECT_IDENTITY).add(
                    AbstractState_1.OperationParameter.removeAllOperationParameters(object.path()).toXri()
                );
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.Standard_1#prepareRequest(org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    protected DataproviderRequest prepareRequest(
        DataproviderRequest request
    ) throws ServiceException {
        Path path = request.path();
        validateBaseQualifier(path);
        //
        // ReferenceFilter
        //
        for(
                int i = 0;
                i < path.size();
                i += 2
        ){
            String qualifier = path.get(i);
            if(
                    AbstractState_1.OperationParameter.hasStateParameter(qualifier) ||
                    AbstractState_1.OperationParameter.hasPeriodParameter(qualifier)
            ){
                DataproviderObject object = new DataproviderObject(
                    AbstractState_1.OperationParameter.removeAllOperationParameters(path)
                );
                object.addClones(request.object(), true);
                return super.prepareRequest(
                    new DataproviderRequest(
                        request, 
                        object,
                        request.operation(),
                        request.attributeFilter(),
                        request.position(),
                        request.size(),
                        request.direction(),
                        request.attributeSelector(),
                        request.attributeSpecifier()
                    )
                );
            }
        }
        //
        // Delegate
        //
        return super.prepareRequest(request);
    }

    /**
     * 
     * @param path
     * @throws ServiceException
     */
    private void validateBaseQualifier(
        Path path
    ) throws ServiceException {
        String base = path.getBase();
        if(path.size() % 2 == 0) {
            if(
                    State_1_Attributes.REF_HISTORY.equals(base) ||
                    State_1_Attributes.REF_STATE.equals(base) ||
                    State_1_Attributes.REF_VALID.equals(base)
            ) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "This provider treats stated object like non-stated ones. " +
                "That's why the given state reference is not supported.",
                new BasicException.Parameter(SystemAttributes.OBJECT_IDENTITY, path),
                new BasicException.Parameter("reference", base)
            );
        } else {
            if(base.indexOf(';') >= 0) {
                AbstractState_1.OperationParameter operationQualifier = AbstractState_1.OperationParameter.newInstance(
                    path.getBase(),
                    VALID_FOR_PATTERN
                );
                // 
                // State Number
                //
                if(operationQualifier.isStateOperation()) {
                    String stateNumber = operationQualifier.getStateNumber(); 
                    if(stateNumber != null && !"0".equals(stateNumber)) throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        "This provider treats stated object like non-stated ones. " +
                        "That's why the only valid state number is 0.",
                        new BasicException.Parameter(SystemAttributes.OBJECT_IDENTITY, path),
                        new BasicException.Parameter("stateNumber", stateNumber)
                    );
                }
                // 
                // Period
                //
                if(operationQualifier.isPeriodOperation()) {
                    assertValidityIsNotRestricted(path, State_1_Attributes.OP_VALID_FROM, operationQualifier.getValidFrom());
                    assertValidityIsNotRestricted(path, State_1_Attributes.OP_VALID_TO, operationQualifier.getValidTo());
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.Standard_1#completeObject(org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest, org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject)
     */
    protected void completeObject(
        DataproviderRequest request, 
        DataproviderObject object
    ) throws ServiceException {
        super.completeObject(request, object);
        completeObject(request, object, getInstanceOf(object));
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.Standard_1#create(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply create(
        ServiceHeader header, 
        DataproviderRequest request
    ) throws ServiceException {
        DataproviderObject object = request.object(); 
        prepareObject(request, object, getInstanceOf(object));
        return super.create(header, request);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.Standard_1#modify(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply modify(
        ServiceHeader header, 
        DataproviderRequest request
    ) throws ServiceException {
        DataproviderObject object = request.object(); 
        prepareObject(request, object, getInstanceOf(object));
        return super.modify(header, request);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.Standard_1#replace(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply replace(
        ServiceHeader header, 
        DataproviderRequest request
    ) throws ServiceException {
        DataproviderObject object = request.object(); 
        prepareObject(request, object, getInstanceOf(object));
        return super.replace(header, request);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.Standard_1#find(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply find(
        ServiceHeader header, 
        DataproviderRequest request
    ) throws ServiceException {
        List attributeFilter = new ArrayList(Arrays.asList(request.attributeFilter()));
        if(implicitelyFalse(attributeFilter)) {
            DataproviderReply reply = new DataproviderReply();
            reply.context(DataproviderReplyContexts.TOTAL).set(
                0,
                new Integer(0)
            );
            reply.context(DataproviderReplyContexts.HAS_MORE).set(
                0,
                Boolean.FALSE
            );
            return reply; 
        } else {
            int i = State_1_Attributes.indexOfStatedObject(request.attributeFilter());
            if(i >= 0) {
                FilterProperty filter = request.attributeFilter()[i];
                if(filter.values().isEmpty()) {
                    attributeFilter.remove(i);
                } else {
                    attributeFilter.set(
                        i,
                        new FilterProperty(
                            filter.quantor(),
                            SystemAttributes.OBJECT_IDENTITY,
                            filter.operator(),
                            (Object[])toXRIs(filter.values())
                        )
                    );
                }
            }
            return super.find(
                header, 
                new DataproviderRequest(
                    request, 
                    request.object(),
                    request.operation(),
                    (FilterProperty[])attributeFilter.toArray(
                        new FilterProperty[attributeFilter.size()]
                    ),
                    request.position(),
                    request.size(),
                    request.direction(),
                    request.attributeSelector(),
                    request.attributeSpecifier()
                )
            );
        }
    }

    /**
     * Assert that the given state feature is <code>null</code>.
     * 
     * @param object
     * @param feature
     * @param fixable 
     * 
     * @throws ServiceException
     */
    private static void assertFeatureIsNull(
        DataproviderObject object,
        String feature, 
        boolean fixable
    ) throws ServiceException {
        SparseList values = object.getValues(feature);
        if(values != null){
            if(!values.isEmpty() && !fixable) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "This provider treats stated object like non-stated ones. " +
                "That's why the mentionned feature should be null.",
                new BasicException.Parameter(SystemAttributes.OBJECT_IDENTITY, object.path()),
                new BasicException.Parameter(SystemAttributes.OBJECT_CLASS, object.getValues(SystemAttributes.OBJECT_CLASS)),
                new BasicException.Parameter("feature", feature),
                new BasicException.Parameter("values", values)
            );
            object.attributeNames().remove(feature);
        }
    }

    /**
     * Tells whether one of the predicates implicitely evaluates to <code>false</code>.
     * 
     * @param filter all predicates which evaluate implicitely to <code>true</code>
     * are removed from the filter.
     */
    private boolean implicitelyFalse(
        List attributeFilter
    ){
        for(
                Iterator i = attributeFilter.iterator();
                i.hasNext();
        ){
            FilterProperty predicate = (FilterProperty) i.next();
            if(this.implicitelyNull.contains(predicate.name())) {
                if(predicate.quantor() == Quantors.FOR_ALL) {
                    i.remove(); // implicitely true
                } else {
                    return true; // implicitely false
                }
            }
        }
        return false;
    }

    /**
     * Assert that a given operation parameter is <code>null</code>.
     * 
     * @param path
     * @param parameter
     * @param value
     * 
     * @throws ServiceException
     */
    private static void assertValidityIsNotRestricted(
        Path path,
        String parameter, 
        String value
    ) throws ServiceException {
        if(value != null && !State_1_Attributes.OP_VAL_EVER.equals(value)) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "This provider treats stated object like non-stated ones. " +
            "That's why the only allowed value for the given parameter is '" + State_1_Attributes.OP_VAL_EVER + "'.",
            new BasicException.Parameter(SystemAttributes.OBJECT_IDENTITY, path),
            new BasicException.Parameter("qualier", path.getBase()),
            new BasicException.Parameter("parameter", parameter),
            new BasicException.Parameter("value", value)
        );
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

    /**
     * Date or DateTime
     */
    protected static final Pattern VALID_FOR_PATTERN = Pattern.compile("^[0-9]{8}(T[0-9]{6}\\.[0-9]{3}Z)?$");

    /**
     * Tells whether the plug-in holds a single state per object instead of an object which is valid forever. 
     */
    private boolean singleStateMode;

    /**
     * These features are implicitely <code>null</code>
     */
    private Collection implicitelyNull;

    private static final List<String> IMPLICITELY_NULL_IN_SINGLE_STATE_MODE = Collections.singletonList(
        State_1_Attributes.INVALIDATED_AT
    );
    private static final List<String> IMPLICITELY_NULL_IN_OTHER_MODE = Collections.unmodifiableList(
        Arrays.asList(
            State_1_Attributes.INVALIDATED_AT,
            State_1_Attributes.STATE_VALID_FROM,
            State_1_Attributes.STATE_VALID_TO,
            State_1_Attributes.VALID_FROM,
            State_1_Attributes.VALID_TO
        )
    );

}

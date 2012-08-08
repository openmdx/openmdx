/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DependencyAwareExplorer_1.java,v 1.6 2007/11/21 17:59:55 hburger Exp $
 * Description: Dependency Aware Explorer Plug-In
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/11/21 17:59:55 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
package org.openmdx.compatibility.runtime1.layer.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.layer.model.State_1_Attributes;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * Dependency Aware Explorer Plug-In
 * <p>
 * The Dependency Aware Explorer is used by replacing 
 * org.openmdx.compatibility.runtime1.layer.application.Explorer_1 by
 * org.openmdx.compatibility.runtime1.layer.application.DependencyAwareExplorer_1 
 * in openMDX' gateway configuration.
 * <p>
 * It tries to resolve the following object request dependencies:<ul>
 * <li> Requests creating an object have to be executed before requests
 *      referring to it
 * <li> Requests removing an object have to be executed after requests
 *      referring to it
 * </ul>
 * It takes the following steps:<ol>
 * <li> Try to bundle all dataprovider requests belonging to the same exposed 
 *      path and to resolve the dependencies between these bundles and execute
 *      the bundles in the resulting order unless there are cycles.
 * <li> Try to resolve the dependencies between the individual dataprovider 
 *      request step 1 leads to cycles and execute the dataprovider requests
 *      in the resulting order unless there are cycles by bundling all 
 *      consecutive requests belonging to the same exposed path.
 * <li> Execute all dataprovider requests in the original order if step 2 leads
 *      to cycles by bundling all consecutive requests belonging to the same 
 *      exposed path. 
 * </ol>
 */
public class DependencyAwareExplorer_1
    extends Explorer_1
{

    /**
     * Requests are re-ordered only if necessary  
     */
    private boolean reluctantReordering;

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.runtime1.layer.application.AbstractExplorer_1#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception {
        super.activate(id, configuration, delegation);
        this.reluctantReordering = configuration.isOn(
            LayerConfigurationEntries.RELUCTANT_REORDERING
        );
    }

    /**
     * Process a single unit of work
     *
     * @param   header          the service header
     * @param   unitOfWork      a working unit
     *
     * @return  a collection of working unit replies
     */
    protected UnitOfWorkReply process(
        ServiceHeader header,
        UnitOfWorkRequest unitOfWork
    ) {
        DataproviderRequest[] requests = unitOfWork.getRequests();
        if (this.reluctantReordering && !reorderingRequired(requests, true)) {
            return super.process(header, unitOfWork);
        } else try {
            UnitOfWorkReply reply = new UnitOfWorkReply(
                new DataproviderReply[requests.length]
            );
            //
            // Handle empty requests
            //
            if (requests.length == 0) {
                return reply;
            }
            //
            // Get mappings
            //
            Map mappingDependencies = new TreeMap();
            Map mappingMembers = new HashMap();
            Integer[] mappings = new Integer[requests.length];
            for (
                int i = 0; 
                i < mappings.length; 
                i++
            ) {
                Integer mapping = getMapping(requests[i]);
                mappings[i] = mapping;
                Set mappingDependency = (Set) mappingDependencies.get(mapping);
                if (mappingDependency == null) {
                    mappingDependencies.put(
                        mapping,
                        mappingDependency = new HashSet()
                    );
                }
                List mappingMember = (List) mappingMembers.get(mapping);
                if (mappingMember == null) {
                    mappingMembers.put(
                        mapping,
                        mappingMember = new ArrayList()
                    );
                }
                mappingMember.add(new Integer(i));
            }
            //
            // Get dependencies
            //
            Set[] requestDependencies = new Set[mappings.length];
            Set[] lenientDependencies = new Set[mappings.length];
            for (
                int i = 0; 
                i < mappings.length; 
                i++
            ) {
                for (
                    int j = 0; 
                    j < mappings.length; 
                    j++
                ) {
                    if (i != j) {
                        if(dependsOn(requests[i], requests[j], true)) {
                            if (requestDependencies[i] == null) {
                                requestDependencies[i] = new HashSet();
                            }
                            requestDependencies[i].add(new Integer(j));
                            Set mappingDependency = (Set) mappingDependencies.get(mappings[i]);
                            if (mappingDependency == null) {
                                mappingDependencies.put(
                                    mappings[i],
                                    mappingDependency = new HashSet()
                                );
                            }
                            if (!mappings[i].equals(mappings[j])) {
                                mappingDependency.add(mappings[j]);
                            }
                        }
                        if(dependsOn(requests[i], requests[j], false)) {
                            if (lenientDependencies[i] == null) {
                                lenientDependencies[i] = new HashSet();
                            }
                            lenientDependencies[i].add(new Integer(j));
                        }
                    }
                }
            }
            //
            // Resolve dependencies 
            //
            List mappingOrder = resolve(mappingDependencies);
            if (mappingOrder == null) {
                //
                // Handle request dependencies
                //            
                List requestOrder = resolve(toMap(requestDependencies));
                if(requestOrder == null) {
                    requestOrder = resolve(toMap(lenientDependencies));
                    if (requestOrder == null) {
                        SysLog.warning(
                            "Circular dependency detected, keeping original request order",
                            unitOfWork
                       );
                        //
                        // I guess there are statedObject features leading to 
                        // circular references of objects not being instances
                        // of org::openmdx::compatibility::state1::BasicState.
                        //
                        requestOrder = new ArrayList();
                        for (
                            int i = 0; 
                            i < mappings.length; 
                            i++
                        ){
                            requestOrder.add(new Integer(i));
                        }
                    } else {
                        SysLog.warning(
                            "Circular dependency detected, ordering by states and children only",
                            unitOfWork
                       );
                    }
                }
                int currentPosition = 0;
                Integer currentMapping = mappings[currentPosition];
                for (
                    int nextPosition = 1; 
                    nextPosition < requests.length; 
                    nextPosition++
                ) {
                    Integer nextMapping = mappings[nextPosition];
                    if (!nextMapping.equals(currentMapping)) {
                        reply = process(
                            header,
                            unitOfWork,
                            reply,
                            toArray(
                                requestOrder.subList(currentPosition, nextPosition)
                            ),
                            currentMapping
                        );
                        if (reply.failure()) {
                            return reply;
                        }
                        currentMapping = nextMapping;
                        currentPosition = nextPosition;
                    }
                }
                return process(
                    header,
                    unitOfWork,
                    reply,
                    toArray(
                        requestOrder.subList(currentPosition, requestOrder.size())
                    ),
                    currentMapping
                );
            } else {
                for (
                    Iterator i = mappingOrder.iterator(); 
                    i.hasNext();
                ) {
                    Integer currentMapping = (Integer) i.next();
                    List requestSequence = (List) mappingMembers.get(currentMapping);
                    List requestOrder = resolve(
                        toMap(
                            requestDependencies,
                            requestSequence
                        )
                    );
                    if (requestOrder == null) {
                        requestOrder = resolve(
                            toMap(
                                lenientDependencies,
                                requestSequence
                            )
                        );
                        if(requestOrder == null) {
                            //
                            // I guess there are statedObject features leading to 
                            // circular references of objects not being instances
                            // of org::openmdx::compatibility::state1::BasicState.
                            //
                            requestOrder = requestSequence;
                        }
                    }
                    reply = process(
                        header,
                        unitOfWork,
                        reply,
                        toArray(requestOrder),
                        currentMapping
                    );
                    if (reply.failure()) {
                        return reply;
                    }
                }
                return reply;
            } 
        } catch (ServiceException exception) {
            return new UnitOfWorkReply(exception);
        }
    }

    /**
     * Test whether re-ordering is required
     * 
     * @param requests
     * @param referenceAware tells whether only the hierarchy is checked or 
     * whether references are checked as well
     * 
     * @return <code>true</code> if re-ordering is required
     */
    private boolean reorderingRequired(
        DataproviderRequest[] requests,
        boolean referenceAware) {
        for (int i = 0; i < requests.length; i++) {
            for (int j = i + 1; j < requests.length; j++) {
                if (dependsOn(requests[i], requests[j], referenceAware)) { 
                    return true; 
                }
            }
        }
        return false;
    }

    private final boolean isStateOf(
        DataproviderRequest stateCandidate,
        DataproviderRequest initialStateCandidate) {
        SparseList statedObject = stateCandidate.object().getValues(
            State_1_Attributes.STATED_OBJECT);
        return 
            statedObject != null && 
            initialStateCandidate.path().equals(statedObject.get(0));
    }

    private final boolean isChildOf(
        DataproviderRequest childCandidate,
        DataproviderRequest parentCandidate
    ) {
        return childCandidate.path().startsWith(parentCandidate.path());
    }

    /**
     * Tests whether the left request should be executed after the right one.
     * 
     * @param request
     * @param path
     * @param referenceAware tells whether only the hierarchy is checked or 
     * whether references are checked as well
     * 
     * @return <code>true</code> if the left request should be executed after the right one
     */
    private boolean dependsOn(
        DataproviderRequest left,
        DataproviderRequest right,
        boolean referenceAware
    ) {
        return (
            left.operation() == DataproviderOperations.OBJECT_CREATION && 
            right.operation() == DataproviderOperations.OBJECT_CREATION && (
                 isChildOf(left, right) || 
                 isStateOf(left, right)
            )
        ) || (
            left.operation() == DataproviderOperations.OBJECT_REMOVAL && 
            isChildOf(right, left)
        ) || (
            referenceAware && (
                (
                    left.operation() == DataproviderOperations.OBJECT_REMOVAL && 
                    isReferencedBy(left.path(), right)
                ) || (
                    right.operation() == DataproviderOperations.OBJECT_CREATION && 
                    isReferencedBy(right.path(), left)
                )
            )
        );
    }

    /**
     * Tests whether an object is referenced by a given request.
     * 
     * @param left
     * @param right
     * @return
     */
    private boolean isReferencedBy(Path left, DataproviderRequest right) {
        short operation = right.operation();
        return (operation == DataproviderOperations.OBJECT_CREATION
            || operation == DataproviderOperations.OBJECT_MODIFICATION
            || operation == DataproviderOperations.OBJECT_OPERATION
            || operation == DataproviderOperations.OBJECT_REPLACEMENT || operation == DataproviderOperations.OBJECT_SETTING)
            && isReferencedBy(left, right.object());
    }

    /**
     * Tests whether an object is referenced by a given dataprovider object.
     * 
     * @param left
     * @param right
     * @return
     */
    private boolean isReferencedBy(Path left, DataproviderObject right) {
        for (
            Iterator a = right.attributeNames().iterator(); 
            a.hasNext();
        ){
            for (
                Iterator v = right.getValues((String) a.next()).populationIterator(); 
                v.hasNext();
            ){
                if (left.equals(v.next())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * COnverts an Integer List to an int[]
     * 
     * @param source
     * @return
     */
    private int[] toArray(List source) {
        int[] target = new int[source.size()];
        for (int i = 0; i < target.length; i++) {
            target[i] = ((Integer) source.get(i)).intValue();
        }
        return target;
    }

    /**
     * Lazy conversion of array to map.
     * 
     * @param source an array of sets
     * @return the map with all null entries replaced by empty sets.
     */
    private Map toMap(Set[] source) {
        Map target = new TreeMap();
        for (
            int i = 0; 
            i < source.length; 
            i++
        ) {
            target.put(
                new Integer(i),
                source[i] == null ? Collections.EMPTY_SET : source[i]
            );
        }
        return target;
    }

    /**
     * Lazy conversion of array to map.
     * 
     * @param source an array of sets
     * @return the map with all null entries replaced by empty sets.
     */
    private Map toMap(Set[] source, List selection) {
        Map target = new TreeMap();
        for (
            Iterator i = selection.iterator(); 
            i.hasNext();
        ) {
            Integer key = (Integer) i.next();
            Set value = source[key.intValue()];
            target.put(
                key, 
                value == null ? Collections.EMPTY_SET : value
            );
        }
        return target;
    }

    /**
     * Resolve a dependency list
     * 
     * @param dependency map
     * 
     * @return the order in which the chunks should be processed;
     * or null of dependency resolution failed
     */
    private List resolve(Map dependency) {
        List order = new ArrayList();
        Set pending = new HashSet();
        for (
            Iterator i = dependency.keySet().iterator(); 
            i.hasNext();
        ) {
            if (!resolve(order, i.next(), dependency, pending)) { 
                return null; 
            }
        }
        return order;
    }

    /**
     * Resolve the dependencies
     * 
     * @param order
     * @param key
     * @param dependency
     * 
     * @return true if the dependencies could be resolved
     */
    private boolean resolve(
        List order, 
        Object key, 
        Map dependency, 
        Set pending
    ) {
        Collection dependents = (Collection) dependency.get(key);
        if (dependents == null || order.contains(key)) {
            return true;
        } else if (pending.contains(key)) {
            return false;
        } else {
            pending.add(key);
            try {
                for (
                    Iterator i = dependents.iterator(); 
                    i.hasNext();
                ) {
                    if (!resolve(order, i.next(), dependency, pending)) {
                        return false;
                    }
                }
            } catch (RuntimeException exception) {
                SysLog.error(
                    "Return false due to unexpected dependency resolution failure",
                    new RuntimeServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        new BasicException.Parameter[] {
                            new BasicException.Parameter("order", order),
                            new BasicException.Parameter("key", key),
                            new BasicException.Parameter("dependency", dependency),
                            new BasicException.Parameter("pending", pending),
                            new BasicException.Parameter("dependents", dependents)
                        },
                        "Unexpected Dependency Resolution Failure"
                     )
                );
                return false;
            }
            order.add(key);
            pending.remove(key);
            return true;
        }
    }

}

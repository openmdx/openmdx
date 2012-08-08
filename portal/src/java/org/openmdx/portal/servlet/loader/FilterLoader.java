/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: FilterLoader.java,v 1.15 2008/04/04 17:01:12 hburger Exp $
 * Description: TextsLoader class
 * Revision:    $Revision: 1.15 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/04 17:01:12 $
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
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet.loader;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.portal.servlet.Filter;
import org.openmdx.portal.servlet.Filters;
import org.openmdx.portal.servlet.RoleMapper_1_0;
import org.openmdx.portal.servlet.UiContext;
import org.openmdx.portal.servlet.WebKeys;

public class FilterLoader
    extends Loader {

    //-------------------------------------------------------------------------
    public FilterLoader(
        ServletContext context,
        RoleMapper_1_0 roleMapper,
        Model_1_3 model
    ) {
        super(
            context,
            roleMapper
        );
        this.model = model;
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void createDefaultFilters(
        String qualifiedReferenceName,
        ModelElement_1_0 referencedType,
        Map filterMap,
        UiContext uiContext,
        Model_1_0 model
    ) throws ServiceException {
        Filters filters = (Filters)filterMap.get(
            qualifiedReferenceName
        );
        if(filters == null) {
            filterMap.put(
                qualifiedReferenceName,
                filters = new Filters(
                    new String[]{qualifiedReferenceName},
                    new Filter[]{}
                )
            );
        }

        // ALL filter: selects all objects
        filters.addFilter(
            new Filter(
                "All",
                null,
                FILTER_GROUP_NAME_SYSTEM,
                WebKeys.ICON_FILTER_ALL,
                null,
                new Condition[]{},
                new OrderSpecifier[]{},
                new Object[]{this.getClass().getName()}
            )
        );
        // DEFAULT filter: Is typically overriden by user settings
        filters.addFilter(
            new Filter(
                Filters.DEFAULT_FILTER_NAME,
                null,
                FILTER_GROUP_NAME_SYSTEM,
                WebKeys.ICON_FILTER_DEFAULT,
                null,
                new Condition[]{},
                new OrderSpecifier[]{},
                new Object[]{this.getClass().getName()}
            )
        );

        // Create a filter for each referenced type (only if there is more than one referenced type)
        Map assertableInspectors = uiContext.getAssertableInspectors();
        if(referencedType.values("allSubtype").size() > 1) {
            for(Iterator k = referencedType.values("allSubtype").iterator(); k.hasNext(); ) {
                ModelElement_1_0 subtype = model.getElement(k.next());
                if(!((Boolean)subtype.values("isAbstract").get(0)).booleanValue()) {
                    String inspectorQualifiedName = (String)subtype.values("qualifiedName").get(0);
                    org.openmdx.ui1.jmi1.AssertableInspector inspector =
                        (org.openmdx.ui1.jmi1.AssertableInspector)assertableInspectors.get(inspectorQualifiedName);
                    if(inspector == null) {
                        AppLog.warning("No inspector found for", inspectorQualifiedName);
                        System.out.println("WARNING: no inspector found for " + inspectorQualifiedName);
                    } else {
                        if(inspector.getIconKey() == null) {
                            AppLog.warning("No icon key found for inspector", inspectorQualifiedName);
                            System.out.println("WARNING: no icon key found for inspector " + inspectorQualifiedName);
                        }
                        String iconKey = (inspector == null) || (inspector.getIconKey() == null)
                        ? WebKeys.ICON_DEFAULT
                            : inspector.getIconKey().substring(inspector.getIconKey().lastIndexOf(":") + 1) + ".gif";
                        Integer[] order = null;
                        if(inspector.getOrder() != null) {
                            order = (Integer[])inspector.getOrder().toArray(new Integer[inspector.getOrder().size()]);
                        }
                        if(
                            (inspector != null) &&
                            inspector.isFilterable() &&
                            !inspector.getLabel().isEmpty()
                        ) {
                            filters.addFilter(
                                new Filter(
                                    (String)inspector.getLabel().get(0),
                                    (String[])inspector.getLabel().toArray(new String[inspector.getLabel().size()]),
                                    FILTER_GROUP_NAME_CLASSES,
                                    iconKey,
                                    order,
                                    new Condition[]{
                                        new IsInCondition(
                                            Quantors.THERE_EXISTS,
                                            SystemAttributes.OBJECT_CLASS,
                                            true,
                                            new Object[]{subtype.values("qualifiedName").get(0)}
                                        )
                                    },
                                    new OrderSpecifier[]{},
                                    new Object[]{this.getClass().getName()}                                                        
                                )
                            );
                        }
                    }
                }
            }
        }        
    }
    
    //-------------------------------------------------------------------------
    /**
     * Loads filter configuration. Assumes that ui config is loaded.
     */
    @SuppressWarnings("unchecked")
    synchronized public boolean loadFilters(
        UiContext uiContext,
        Map target
    ) throws ServiceException {

        boolean resetSession = false;
        long crc = this.getCRCForResourcePath(
            this.context,
            "/WEB-INF/config/filters/"
        );
        if(crc != filterCRC) {

            // load filters from config
            target.clear();
            Set reourcePaths = this.context.getResourcePaths("/WEB-INF/config/filters/");
            if(reourcePaths != null) {
                System.out.println("Loading /WEB-INF/config/filters/");
                Set filterResources = new TreeSet(reourcePaths);
                for(
                        Iterator i = filterResources.iterator(); 
                        i.hasNext(); 
                ) {
                    try {
                        String path = (String)i.next();
                        if(!path.endsWith("/")) {
                            InputStream is = this.context.getResourceAsStream(path);
                            try {
                                XMLDecoder decoder = new XMLDecoder(
                                    is,
                                    null,
                                    new ExceptionListener() {
                                        public void exceptionThrown(Exception e) {
                                            if(
                                                !(e instanceof ClassNotFoundException) || 
                                                !"org.openmdx.uses.java.beans.XMLDecoder".equals(e.getMessage())
                                            ) {
                                                throw new RuntimeException(e);
                                            }
                                        }                                    
                                    }                
                                );
                                Filters f = (Filters)decoder.readObject();
                                // merge loaded filter with existing
                                for(int j = 0; j < f.getForReference().length; j++) {
                                    if(target.get(f.getForReference()[j]) == null) {
                                        target.put(
                                            f.getForReference()[j],
                                            new Filters()
                                        );
                                    }
                                    Filters existing = (Filters)target.get(f.getForReference()[j]);
                                    for(int k = 0; k < f.getFilter().length; k++) {
                                        existing.addFilter(
                                            f.getFilter(k)
                                        );
                                    }
                                }
                                is.close();
                            }
                            catch(Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                    catch(Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }

            // Create standard filters for all referenced types
            for(
                Iterator i = this.model.getContent().iterator(); 
                i.hasNext(); 
            ) {
                ModelElement_1_0 element = (ModelElement_1_0)i.next();
                if(this.model.isClassType(element)) {
                    // Default filters for all modeled features
                    for(
                        Iterator j = ((Map)element.values("allFeature").get(0)).values().iterator(); 
                        j.hasNext(); 
                    ) {
                        ModelElement_1_0 feature = (ModelElement_1_0)j.next();
                        if(this.model.isReferenceType(feature)) {                            
                            String qualifiedReferenceName = element.values("qualifiedName").get(0) + ":" + feature.values("name").get(0);
                            ModelElement_1_0 referencedType = this.model.getElement(feature.values("type").get(0));                            
                            this.createDefaultFilters(
                                qualifiedReferenceName, 
                                referencedType, 
                                target, 
                                uiContext, 
                                this.model
                            );
                        }                        
                    }
                    // Default filters for all customized features
                    for(
                        Iterator j = uiContext.getUiSegment().getFeatureDefinition().iterator();
                        j.hasNext();
                    ) {
                        org.openmdx.ui1.jmi1.FeatureDefinition featureDefinition = (org.openmdx.ui1.jmi1.FeatureDefinition)j.next();
                        if(featureDefinition instanceof org.openmdx.ui1.jmi1.StructuralFeatureDefinition) {
                            org.openmdx.ui1.jmi1.StructuralFeatureDefinition structuralFeature = (org.openmdx.ui1.jmi1.StructuralFeatureDefinition)featureDefinition;
                            String qualifiedReferenceName = structuralFeature.refGetPath().getBase();
                            ModelElement_1_0 container = this.model.getElement(qualifiedReferenceName.substring(0, qualifiedReferenceName.lastIndexOf(":")));
                            if(
                                (structuralFeature.isReference() != null) && 
                                structuralFeature.isReference().booleanValue() &&
                                this.model.isSubtypeOf(element, container)
                            ) {
                                ModelElement_1_0 referencedType = this.model.getElement(structuralFeature.getType());                            
                                this.createDefaultFilters(
                                    qualifiedReferenceName, 
                                    referencedType, 
                                    target, 
                                    uiContext, 
                                    this.model
                                );                                
                            }
                        }
                    }
                }
            }
            this.filterCRC = crc;
            resetSession = true;
            System.out.println("Loaded filters #" + target.keySet().size());
        }
        return resetSession;
    }

    //-------------------------------------------------------------------------
    private final static String FILTER_GROUP_NAME_SYSTEM = "0";
    private final static String FILTER_GROUP_NAME_CLASSES = "1";

    private long filterCRC = -1L;
    private final Model_1_3 model;

}

//--- End of File -----------------------------------------------------------

/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: TextsLoader class
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2013, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.loader;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.IsInstanceOfCondition;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Filter;
import org.openmdx.portal.servlet.Filters;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.UiContext;
import org.openmdx.portal.servlet.WebKeys;
import org.w3c.cci2.BinaryLargeObjects;

/**
 * FilterLoader
 *
 */
public class FilterLoader
    extends Loader {

    /**
     * Constructor.
     *
     * @param context
     * @param portalExtension
     * @param model
     */
    public FilterLoader(
        ServletContext context,
        PortalExtension_1_0 portalExtension,
        Model_1_0 model
    ) {
        super(
            context,
            portalExtension
        );
        this.model = model;
    }

    /**
     * Create default filters for given reference.
     * 
     * @param qualifiedReferenceName
     * @param referencedType
     * @param filterMap
     * @param uiContext
     * @param model
     * @throws ServiceException
     */
    private void createDefaultFilters(
        String qualifiedReferenceName,
        ModelElement_1_0 referencedType,
        Map<String,Filters> filterMap,
        UiContext uiContext,
        Model_1_0 model
    ) throws ServiceException {
    	String consolePrefix = new Date() + "  ";
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
                null, // orders
                null, // conditions
                null, // orderSpecifiers
                null, // extensions
                this.getClass().getName()
            )
        );
        // DEFAULT filter: Is typically overriden by user settings
        filters.addFilter(
            new Filter(
                Filters.DEFAULT_FILTER_NAME,
                null,
                FILTER_GROUP_NAME_SYSTEM,
                WebKeys.ICON_FILTER_DEFAULT,
                null, // orders
                null, // conditions
                null, // orderSpecifiers
                null, // extensions
                this.getClass().getName()
            )
        );
        // Create a filter for each referenced type (only if there is more than one referenced type)
        if(!"org:openmdx:base:ExtentCapable".equals(referencedType.objGetValue("qualifiedName"))) {
	        Map assertableInspectors = uiContext.getAssertableInspectors(UiContext.MAIN_PERSPECTIVE);
	        if(referencedType.objGetList("allSubtype").size() > 1) {
	            for(Iterator k = referencedType.objGetList("allSubtype").iterator(); k.hasNext(); ) {
	                ModelElement_1_0 subtype = model.getElement(k.next());
	                if(!((Boolean)subtype.objGetValue("isAbstract")).booleanValue()) {
	                    String inspectorQualifiedName = (String)subtype.objGetValue("qualifiedName");
	                    org.openmdx.ui1.jmi1.AssertableInspector inspector =
	                        (org.openmdx.ui1.jmi1.AssertableInspector)assertableInspectors.get(inspectorQualifiedName);
	                    if(inspector == null) {
	                    	SysLog.warning("No inspector found for", inspectorQualifiedName);
	                        System.out.println(consolePrefix + "WARNING: no inspector found for " + inspectorQualifiedName);
	                    } else {
	                    	String iconKey = inspector.getIconKey();
	                        iconKey = iconKey == null 
	                        	? WebKeys.ICON_DEFAULT 
	                        	: iconKey.substring(iconKey.lastIndexOf(":") + 1) + ".gif";
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
	                                    Collections.singletonList(
	                                        (Condition)new IsInstanceOfCondition(
	                                        	false, // SystemAttributes.OBJECT_CLASS
	                                            (String)subtype.objGetValue("qualifiedName")
	                                        )
	                                    ),
	                                    null, // order
	                                    null, // extension
	                                    this.getClass().getName()                                                        
	                                )
	                            );
	                        }
	                    }
	                }
	            }
	        }
        }
    }

    /**
     * Loads filter configuration. Assumes that ui config is loaded.
     * 
     * @param uiContext
     * @param filterStore
     * @return
     * @throws ServiceException
     */
    synchronized public boolean loadFilters(
        UiContext uiContext,
        Map<String,Filters> filterStore
    ) throws ServiceException {
    	String messagePrefix = new Date() + "  ";
        boolean resetSession = false;
        long crc = this.getCRCForResourcePath(
            this.context,
            "/WEB-INF/config/filters/"
        );
        if(crc != filterCRC) {
            // load filters from config
            filterStore.clear();
            Set<String> reourcePaths = this.context.getResourcePaths("/WEB-INF/config/filters/");
            if(reourcePaths != null) {
            	System.out.println(messagePrefix + "Loading /WEB-INF/config/filters/");
                SysLog.info("Loading /WEB-INF/config/filters/");
                Set<String> filterResources = new TreeSet<String>(reourcePaths);
                for(String path: filterResources) { 
                    try {
                        if(!path.endsWith("/")) {
                            try {
                            	ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                BinaryLargeObjects.streamCopy(
                                	this.context.getResourceAsStream(path), 
                                	0L, 
                                	bos
                                );
                                bos.close();
                                Filters f = (Filters)JavaBeans.fromXML(
                                	new String(bos.toByteArray(), "UTF-8")
                                );
                                // merge loaded filter with existing
                                for(int j = 0; j < f.getForReference().length; j++) {
                                    if(filterStore.get(f.getForReference()[j]) == null) {
                                        filterStore.put(
                                            f.getForReference()[j],
                                            new Filters()
                                        );
                                    }
                                    Filters existing = (Filters)filterStore.get(f.getForReference()[j]);
                                    for(int k = 0; k < f.getFilter().length; k++) {
                                        existing.addFilter(
                                            f.getFilter(k)
                                        );
                                    }
                                }
                            } catch(Exception e) {
                            	new ServiceException(e).log();
                                System.out.println(messagePrefix + "STATUS: " + e.getMessage() + " (for more info see log)");
                            }
                        }
                    } catch(Exception e) {
                    	new ServiceException(e).log();
                        System.out.println(messagePrefix + "STATUS: " + e.getMessage() + " (for more info see log)");
                    }
                }
            }
            // Create standard filters for all referenced types
            for(ModelElement_1_0 element: this.model.getContent()) {
                if(element.isClassType()) {
                    // Default filters for all modeled features
                	@SuppressWarnings("unchecked")
                    Collection<ModelElement_1_0> features = element.objGetMap("allFeature").values();
                    for(ModelElement_1_0 feature: features) {
                        if(feature.isReferenceType() && !this.model.referenceIsStoredAsAttribute(feature)) {                            
                            String qualifiedReferenceName = element.objGetValue("qualifiedName") + ":" + feature.objGetValue("name");
                            ModelElement_1_0 referencedType = this.model.getDereferencedType(feature.objGetValue("type"));                            
                            this.createDefaultFilters(
                                qualifiedReferenceName, 
                                referencedType, 
                                filterStore, 
                                uiContext, 
                                this.model
                            );
                        }                        
                    }
                    // Default filters for all customized features
                    for(org.openmdx.ui1.jmi1.FeatureDefinition featureDefinition: uiContext.getUiSegment(UiContext.MAIN_PERSPECTIVE).<org.openmdx.ui1.jmi1.FeatureDefinition>getFeatureDefinition()) {
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
                                    element.objGetValue("qualifiedName") + ":" + qualifiedReferenceName.substring(qualifiedReferenceName.lastIndexOf(":") + 1), 
                                    referencedType, 
                                    filterStore, 
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
            System.out.println(messagePrefix + "Done (" + filterStore.keySet().size() + " filters)");
            SysLog.info("Done", filterStore.keySet().size());
        }
        return resetSession;
    }

    //-------------------------------------------------------------------------
    private final static String FILTER_GROUP_NAME_SYSTEM = "0";
    private final static String FILTER_GROUP_NAME_CLASSES = "1";

    private long filterCRC = -1L;
    private final Model_1_0 model;

}

//--- End of File -----------------------------------------------------------

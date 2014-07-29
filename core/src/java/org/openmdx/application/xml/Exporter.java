/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: XML Exporter
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2014, OMEX AG, Switzerland
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
package org.openmdx.application.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.zip.ZipOutputStream;

import javax.jdo.PersistenceManager;
import javax.jmi.model.AggregationKind;
import javax.jmi.model.AggregationKindEnum;
import javax.jmi.reflect.RefObject;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.application.xml.spi.ExportFilter;
import org.openmdx.application.xml.spi.ExportTarget;
import org.openmdx.application.xml.spi.XMLTarget;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.state2.cci2.DateStateQuery;
import org.openmdx.state2.jmi1.DateState;
import org.openmdx.state2.jmi1.StateCapable;
import org.w3c.cci2.SparseArray;

/**
 * Exporter
 */
public class Exporter {

    /**
     * Constructor 
     *
     * @param target
     * @param source
     */
	public Exporter(
	    ExportTarget target,
	    ExportSource source
	) {
	    this.target = target;
	    this.source = source;
	    this.model = Model_1Factory.getModel();
	}

	/**
	 * 
	 */
	private final ExportTarget target;
	
	/**
	 * 
	 */
	private final ExportSource source;
	
	/**
	 * 
	 */
    private final Model_1_0 model;
	
    /**
     * Export as XML data
     */
    public static final String MIME_TYPE_XML = "text/xml";  
    
    /**
     * Export as multi-document archive
     */
    public static final String MIME_TYPE_ZIP = "application/zip";
    
    /**
     * A reference filter including compositions only
     */
    public static ExportFilter CompositionFilter = new ExportFilter(){

        /**
         * Accept shared and composite aggregations
         */
        public boolean include(
            AggregationKind aggregationKind,
            String qualifiedName,
            int distance
        ) {
            return  aggregationKind == AggregationKindEnum.COMPOSITE;
        }

        /**
         * Accept shared and composite aggregations
         */
        public boolean exclude(
            Path objectId
        ) {
        	if(objectId == null) {
        		return false;
        	} else {
        		int s = objectId.size();
        		return s % 2 == 0 && s > 2 && objectId.getComponent(s - 2).isPrivate();
        	}
        }
        
    };

    /**
     * These attributes shall not be exported
     */
    final static Collection<String> EXCLUDED_ATTRIBUTES = Collections.singleton(
        SystemAttributes.OBJECT_INSTANCE_OF
    );

    /**
     * Export the given aggregating reference
     * 
     * @param referenceId
     * 
     * @throws ServiceException 
     */
    private void exportAggregation(
        Path referenceId
    ) throws ServiceException{
        String simpleName = referenceId.getLastSegment().toClassicRepresentation();
        Iterator<Map.Entry<Path, RefObject>> objects = this.source.children(referenceId);
        boolean empty = !objects.hasNext();
        this.target.startReference(simpleName, empty);
        if(!empty) {
            while(objects.hasNext()) {
                Map.Entry<Path, RefObject> e = objects.next();
                exportObject(e.getKey(), e.getValue());
            }
        }
        this.target.endReference(simpleName, empty);
    }

    /**
     * Provide the write operation argument
     * 
     * @param object either a <code>refObject</code> or a primitive type
     */
    private static Object toValue(
        Object object
    ){
       return object instanceof RefObject ? ReducedJDOHelper.getObjectId(object) : object; 
    }
    
    /**
     * Export an attribute with its values
     * 
     * @param object
     * @param attributeName
     * @param attributeDef
     * 
     * @throws ServiceException
     */
    private void exportAttribute(
        RefObject object,
        String attributeName,
        ModelElement_1_0 attributeDef
    ) throws ServiceException{
        String qualifiedName = (String) attributeDef.getQualifiedName();
        ModelElement_1_0 attributeType = this.model.getDereferencedType(attributeDef.getType());
        String typeName = (String) attributeType.getQualifiedName();
        Object attributeValue = this.model.isReferenceType(attributeDef) ? 
           PersistenceHelper.getFeatureReplacingObjectById(object, attributeName) : 
           object.refGetValue(attributeName);
        Multiplicity multiplicity = ModelHelper.getMultiplicity(attributeDef);
        boolean empty =  
            attributeValue instanceof Map<?,?> ? ((Map<?,?>)attributeValue).isEmpty() :
            attributeValue instanceof Collection<?> ? ((Collection<?>)attributeValue).isEmpty() :
            attributeValue == null;
        this.target.startAttribute(
            qualifiedName, 
            typeName, 
            multiplicity, 
            attributeValue, 
            empty
        );    
        if(!empty) {
        	switch(multiplicity) {
	        	case SPARSEARRAY: {
	                if(attributeValue instanceof SortedMap<?,?>) {
	                    for(Map.Entry<?, ?> e : ((SortedMap<?,?>)attributeValue).entrySet()) {
	                        this.target.write(
	                            typeName, 
	                            multiplicity, 
	                            ((Integer)e.getKey()).intValue(), 
	                            toValue(e.getValue())
	                        );
	                    }
	                } else {
	                    for(
	                        ListIterator<?> i = ((SparseArray<?>)attributeValue).populationIterator();
	                        i.hasNext();
	                    ){
	                        this.target.write(
	                            typeName, 
	                            multiplicity, 
	                            i.nextIndex(), 
	                            toValue(i.next())
	                        );
	                    }
	                }
	            } break;
	        	case SET: case LIST: {
	                int position = 0;
	                for(
	                    Iterator<?> i = ((Collection<?>)attributeValue).iterator();
	                    i.hasNext();
	                    position++
	                ){
	                    this.target.write(
	                        typeName, 
	                        multiplicity, 
	                        position, 
	                        toValue(i.next())
	                    );
	                }
	            } break;
	            default: {
	                this.target.write(
                        typeName, 
                        multiplicity, 
                        0, 
                        toValue(attributeValue)
                    );
	            	
	            }
        	}
        }
        this.target.endAttribute(
            qualifiedName, 
            typeName, 
            multiplicity, 
            attributeValue, 
            empty
        );
    }
    
    /**
     * Export an object's attributes
     * 
     * @param object
     * 
     * @throws ServiceException 
     */
    private void exportAttributes(
        RefObject object
    ) throws ServiceException{
        if(object == null) {
            this.target.startAttributes(true);
            this.target.endAttributes(true);
        } else {
            ModelElement_1_0 objectClass = model.getElement(
                object.refClass().refMofId()
            );
            Map<String, ModelElement_1_0> modelAttributes = this.model.getAttributeDefs(
                objectClass, 
                false, // includeSubtypes
                true // includeDerived
            );
            this.target.startAttributes(modelAttributes.isEmpty());
            for(Map.Entry<String, ModelElement_1_0> attribute : modelAttributes.entrySet()) {
                String attributeName = attribute.getKey();
                if(!EXCLUDED_ATTRIBUTES.contains(attributeName)) {
                    exportAttribute(
                        object,
                        attributeName,
                        attribute.getValue()
                    );
                }
            }
            this.target.endAttributes(modelAttributes.isEmpty());
        }
    }
    
    /**
     * Export the aggregated objects
     * 
     * @param objectId
     * 
     * @throws ServiceException
     */
    private void exportChildren(
        Path objectId
    ) throws ServiceException{
        Iterator<Map.Entry<Path, RefObject>> references = this.source.children(objectId);
        boolean empty = !references.hasNext();
        this.target.startChildren(empty);
        if(!empty) {
            while(references.hasNext()){
                exportAggregation(references.next().getKey());
            }
        }
        this.target.endChildren(empty);
    }
    
    /**
     * Export the given object
     * 
     * @param objectId
     * @param object
     * 
     * @throws ServiceException 
     */
    private void exportObject(
        Path objectId,
        RefObject object
    ) throws ServiceException{
        boolean noOperation = object == null;
        final RefObject refObject;
        if(noOperation) {
            refObject = this.source.getObjectbyId(objectId);
        } else {
            refObject = object;
        }
        this.target.startObject(refObject, noOperation);
        exportAttributes(noOperation ? null : refObject);
        exportChildren(objectId);
        this.target.endObject(refObject);
    }
    
    /**
     * Export authorities
     * 
     * @throws ServiceException
     */
    public void export(
    ) throws ServiceException{
        Iterator<Map.Entry<Path,RefObject>> authorities = this.source.children(new Path("")); 
        boolean empty = !authorities.hasNext();
        this.target.exportProlog(empty);
        if(!empty) {
            while(authorities.hasNext()) {
                Map.Entry<Path,RefObject> authorityEntry = authorities.next();
                Path authorityId = authorityEntry.getKey();
                String qualifiedName = authorityId.get(0);
                this.target.startAuthority(qualifiedName);
                exportObject(authorityId, authorityEntry.getValue());
                this.target.endAuthority(qualifiedName);
            }
        }
        this.target.exportEpilog(empty);
    }

    /**
     * Create an <code>Exporter</code> and export the given objects
     * 
     * @param target
     * @param persistenceManager
     * @param exportFilter defaults to the <code>AggregationOnlyFilter</code> in case of <code>null</code>
     * @param objectIds the starting points
     * 
     * @throws ServiceException
     */
    public static void export(
        ExportTarget target,
        PersistenceManager persistenceManager,
        ExportFilter exportFilter,
        Path... objectIds
    ) throws ServiceException {
        //
        // 1st pass
        //
        Closure closure = new Closure(
            exportFilter == null ? CompositionFilter : exportFilter
        );
        closure.populate(persistenceManager, objectIds);
        //
        // 2nd pass
        //
        Exporter exporter = new Exporter(
            target,
            closure.getSource()
        );
        exporter.export();
    }

    /**
     * Support <code>RefObject</code>/<code>String</code> specification
     * 
     * @param startFrom
     * @param rawFilter
     * @param itemMimeType
     * 
     * @throws ServiceException
     */
    public static Object[] exportIntoToByteArray(
        RefObject startFrom, 
        String rawFilter,
        String itemMimeType
    ) throws ServiceException {
        List<Path> startingPoints = new ArrayList<Path>();
        startingPoints.add((Path) ReducedJDOHelper.getObjectId(startFrom));
        //
        // Starting identities are separated from the export filter by '$'
        //
        String filter = rawFilter;
        int delimiterPosition = filter == null ? -1 : filter.indexOf('$');
        if(delimiterPosition > 0) {
            StringTokenizer tokenizer = new StringTokenizer(
                filter.substring(0, delimiterPosition), 
                "\t\n ;,", 
                false
            );
            while (tokenizer.hasMoreTokens()) {
                startingPoints.add(new Path(tokenizer.nextToken()));
            }
            filter = filter.substring(delimiterPosition + 1);
        }
        //
        // Options are separated by '!'
        //
        delimiterPosition = filter == null ? -1 : filter.indexOf('!');
        if(delimiterPosition > 0) {
            SysLog.info("Options ignored", filter.substring(delimiterPosition + 1));
            filter = filter.substring(0, delimiterPosition);
        }
        //
        // Prepare target according to MIME type
        //
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ExportTarget target = asTarget(byteStream, itemMimeType);
        //
        // Export objects
        //
        export(
            target,
            ReducedJDOHelper.getPersistenceManager(startFrom),
            newFilter(filter),
            startingPoints.toArray(new Path[startingPoints.size()])
        );
        return MIME_TYPE_XML.equals(itemMimeType) ? new Object[]{
            "Export.zip",
            MIME_TYPE_ZIP,
            byteStream.toByteArray()
        } : new Object[]{
            "Export.bin",
            itemMimeType,
            byteStream.toByteArray()
        };
    }
    
    /**
     * Parse filter string
     * 
     * @param filter the stringified filter value
     * 
     * @return an <code>ExportFilter</code>
     */
    public static ExportFilter newFilter(
        String filter
    ){
        List<String> referenceFilter = new ArrayList<String>();
        StringTokenizer referenceFilterTokenizer = new StringTokenizer(
            filter, 
            "\t\n ;,", 
            false
        );
        while (referenceFilterTokenizer.hasMoreTokens()) {
            String referenceName = referenceFilterTokenizer.nextToken();
            if(!referenceName.endsWith("]")) {
                referenceName += "[1]"; // by default maxLevel is 1
            }
            referenceFilter.add(referenceName);
        }
        return null;
    }
        
    /**
     * Create a MIME-TYPE specific <code>ExportTarget</code>
     * 
     * @param target the final destination
     * @param mimeType the document's MIME type
     * 
     * @return the corresponding <code>ExportTarget</code>
     * 
     * @throws ServiceException
     */
    public static ExportTarget asTarget(
        OutputStream target,
        String mimeType
    ) throws ServiceException {
        if(MIME_TYPE_XML.equals(mimeType)) {
            return new XMLTarget(new ZipOutputStream(target));
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.NOT_SUPPORTED, 
                "Unsupported mime type", 
                new BasicException.Parameter("actual", mimeType),
                new BasicException.Parameter("supported", MIME_TYPE_XML)
            );
        }
    }

    /**
     * Create a MIME-TYPE specific <code>ExportTarget</code>
     * 
     * @param target the final destination
     * @param mimeType the document's MIME type
     * 
     * @return the corresponding <code>ExportTarget</code>
     * 
     * @throws ServiceException
     */
    public static ExportTarget asTarget(
        File target,
        String mimeType
    ) throws ServiceException {
        try {
            return asTarget(
                new FileOutputStream(target),
                mimeType
            );
        } catch (FileNotFoundException exception) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.CREATION_FAILURE, 
                "Unable to create output file", 
                new BasicException.Parameter("target", target.toString()),
                new BasicException.Parameter("mime-type", mimeType)
            );
        }
    }
    
    
    //------------------------------------------------------------------------
	// Class ExportSource
    //------------------------------------------------------------------------
	
	/**
	 * Allows to iterate level by level
	 * <p>
	 * The entry's value is<ul>
	 * <li>always <code>null</code> in case of references
	 * <li><code>null</code> if not the object itself has to be exported but 
	 * but some or all of its aggregate children
	 * </ul>
	 */
	public interface ExportSource {
	    
	    /**
	     * This method may be called if no-operation requests require the objects as well
	     * 
	     * @param objectId
	     * 
	     * @return te requested object
	     */
	    RefObject getObjectbyId(
	        Path objectId
	    );
	    
	    /**
	     * Represents the children of the given resource
	     * 
	     * @param resourceId
	     * 
	     * @return an <code>Iterator</code> for<ul>
	     * <li>either references of an object
	     * <li>or objects of a reference
	     * </ul>
	     */
	    Iterator<Map.Entry<Path,RefObject>> children(
	        Path resourceId
	     );
	    
	}
	
	
    //------------------------------------------------------------------------
    // Class ExportByLevel
    //------------------------------------------------------------------------
    
	/**
	 * Provide level specific iterators
	 */
	static class LevelIterators implements ExportSource {

	    /**
         * Constructor 
         *
         * @param delegate
         */
        public LevelIterators(
            Iterator<Entry<Path, RefObject>> delegate
        ) {
            this.delegate = delegate;
        }

        /**
	     * 
	     */
	    private final Iterator<Map.Entry<Path,RefObject>> delegate;

	    /**
	     * 
	     */
	    private Map.Entry<Path,RefObject> pending = null;

	    /**
	     * Retrieve the next pending object
	     * 
	     * @return the next pending object, or <code>null</code> at the end.
	     */
	    protected Map.Entry<Path,RefObject> getPending(
	    ){
	        if(this.pending == null && this.delegate.hasNext()) {
	            this.pending = this.delegate.next();
	        }
	        return this.pending;
	    }

	    /**
	     * 
	     */
	    protected void removePending(){
	        this.pending = null;
	    }
	    
        /**
         * Represents the children of the given resource
         * 
         * @param resourceId
         * 
         * @return a collection representing<ul>
         * <li>either references of an object
         * <li>or objects of a reference
         * </ul>
         */
	    public Iterator<Map.Entry<Path,RefObject>> children(
	        final Path resorceId
	    ){
            return new Iterator<Map.Entry<Path, RefObject>>(){

                /**
                 * 
                 */
                Map.Entry<Path,RefObject> prefetched;
                
                public boolean hasNext() {
                    if(prefetched == null) {
                        Map.Entry<Path,RefObject> pending = getPending();
                        if(pending != null){
                            final Path pendingId = pending.getKey();
                            if(pendingId.startsWith(resorceId)) {
                                if(pendingId.size() == resorceId.size() + 1) {
                                    this.prefetched = pending;
                                    removePending();
                                } else {
                                    this.prefetched = new Map.Entry<Path, RefObject>() {

                                        public Path getKey() {
                                            return pendingId.getPrefix(resorceId.size() + 1);
                                        }

                                        public RefObject getValue() {
                                            return null;
                                        }

                                        public RefObject setValue(
                                            RefObject value
                                        ) {
                                            throw new UnsupportedOperationException();
                                        }
                                        
                                    };
                                }
                            }
                        }
                    }
                    return this.prefetched != null;
                }

                public Map.Entry<Path, RefObject> next() {
                    if(!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    Map.Entry<Path, RefObject> prefetched = this.prefetched;
                    this.prefetched = null;
                    return prefetched;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
                
            };
	    }

        /* (non-Javadoc)
         * @see org.openmdx.application.xml.Exporter.ExportSource#getObjectbyId(org.openmdx.base.naming.Path)
         */
        public RefObject getObjectbyId(
            Path objectId
        ) {
            return (RefObject) ReducedJDOHelper.getPersistenceManager(
                getPending().getValue()
            ).getObjectById(
                objectId)
            ;
        }
	    
	}

	
	//------------------------------------------------------------------------
	// Class Closure
	//------------------------------------------------------------------------
	
	/**
	 * Collect all objects to be exported
	 */
    public static class Closure {
        
        /**
         * Constructor 
         *
         * @param exportFilter the export filter must not be <code>null</code>
         */
        public Closure(
            ExportFilter exportFilter
        ) {
            this.exportFilter = exportFilter;
        }

        /**
         * The reference filter
         */
        private final ExportFilter exportFilter;
        
        /**
         * The model accessor
         */
        private final Model_1_0 model = Model_1Factory.getModel();
        
        /**
         * Collect all objects to be exported
         */
        private final SortedMap<Path,RefObject> population = new TreeMap<Path,RefObject>();

        /**
         * Probe non-aggregate reference
         * 
         * @param refId
         * 
         * @return <code>true</code> if pending modifications have been added
         */
        private boolean probe(
            Path refId
        ) throws ServiceException {
            boolean pending = !population.containsKey(refId) && !this.exportFilter.exclude(refId);
            if(pending){
                population.put(refId, null);
            }
            return pending;
        }

        /**
         * Probe aggregate reference
         * 
         * @param refObject
         * @param distance
         * 
         * @return <code>true</code> if the tree has been modified
         * @throws ServiceException  
         */
        private void probe(
            RefObject refObject,
            int distance
        ) throws ServiceException {
            Path refId = (Path) ReducedJDOHelper.getObjectId(refObject);
            if(population.containsKey(refId) ? population.get(refId) == null : !this.exportFilter.exclude(refId)) {
                visit(refId, refObject, distance);
            }
        }
        
        /**
         * Find closure
         * 
         * @param refId
         * @param current
         * @param distance
         * 
         * @return <code>true</code> if there are pending visits
         * 
         * @throws ServiceException
         */
        @SuppressWarnings({
            "unchecked", "cast"
        })
        private boolean visit(
            Path refId,
            RefObject current,
            int distance
        ) throws ServiceException {
            population.put(refId, current);
            boolean pending = false;
            String objectType = current.refClass().refMofId();
            Map<String, ModelElement_1_0> references = (Map<String, ModelElement_1_0>) model.getElement(objectType).objGetMap("reference");
            for (ModelElement_1_0 featureDef : references.values()) {
                ModelElement_1_0 referencedEnd = model.getElement(featureDef.getReferencedEnd());
                AggregationKind aggregationKind = AggregationKindEnum.forName((String) referencedEnd.getAggregation());
                String referenceName = (String) featureDef.getName();
                String qualifiedReferenceName = (String) featureDef.getQualifiedName();
                if(AggregationKindEnum.NONE == aggregationKind) {
                    if(this.exportFilter.include(aggregationKind, qualifiedReferenceName, distance)) {
                        Object value = PersistenceHelper.getFeatureReplacingObjectById(current, referenceName);
                        if(value instanceof Map<?,?>) {
                            Map<?,Path> values = (Map<?, Path>) value;
                            for(Path referenced : values.values()) {
                                pending |= probe(referenced);
                            }
                        } else if (value instanceof Collection<?>) {
                            Collection<Path> values = (Collection<Path>) value;
                            for(Path referenced : values) {
                                pending |= probe(referenced);
                            }
                        } else {
                            Path referenced = (Path) value;
                            pending |= probe(referenced);
                        }
                    }
                } else {
                    if (
                    	this.exportFilter.include(aggregationKind, qualifiedReferenceName, distance) &&
                    	!this.exportFilter.exclude(refId.getChild(referenceName))
                    ) {
                        RefContainer<?> container = (RefContainer<?>) current.refGetValue(referenceName);
                        boolean stated = false;
                        for(RefObject referenced : container) {
                            probe(referenced, distance);
                            if(!stated) {
                            	stated = (referenced instanceof StateCapable) && !(referenced instanceof DateState);
                            }
                        }
                        if(stated) {
                        	DateStateQuery query = (DateStateQuery) ReducedJDOHelper.getPersistenceManager(current).newQuery(DateState.class);
                            for(RefObject referenced : container.getAll(query)) {
                                probe(referenced, distance);
                            }
                        }
                    }
                }
            } 
            return pending;
        }

        /**
         * Determine the closure
         * 
         * @param persistenceManager
         * @param writer
         * @param objectIds
         * 
         * @throws ServiceException
         */
        void populate(
            PersistenceManager persistenceManager,
            Path... objectIds
        ) throws ServiceException {
            //
            // 1st pass
            //
            SortedMap<Path,RefObject> tree = new TreeMap<Path,RefObject>();
            List<Path> pendingIds = new ArrayList<Path>(Arrays.asList(objectIds));
            for(
                int distance = 0;
                !pendingIds.isEmpty();
                distance++
            ){
                boolean pending = false;
                for(Path objectId : pendingIds) {
                    pending |= visit(
                        objectId,
                        (RefObject) persistenceManager.getObjectById(objectId),
                        distance
                    );
                }
                pendingIds.clear();
                if(pending) {
                    for(Map.Entry<Path, RefObject> e : tree.entrySet()) {
                        if(e.getValue() == null) {
                            pendingIds.add(e.getKey());
                        }
                    }
                }
            }
        }
        
        /**
         * Provide the export source
         * 
         * @return the export source
         */
        ExportSource getSource(
        ){
            return new LevelIterators(this.population.entrySet().iterator());
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class ReferenceFilter
    //------------------------------------------------------------------------
    
    /**
     * Filter by reference name and distance
     */
    public static class ReferenceFilter implements ExportFilter {
        
        /**
         * Constructor 
         *
         * @param referenceFilter
         */
        public ReferenceFilter(
            String[] referenceFilter
        ) {
            this.referenceFilter = referenceFilter;
        }

        /**
         * The reference filter array
         */
        private final String[] referenceFilter;

        /**
         * The reference filters' distance suffix array
         */
        private static final String[] DISTANCE = {
            "[1]",
            "[2]",
            "[3]",
            "[4]"
        };
        
        /* (non-Javadoc)
         * @see org.openmdx.application.xml.spi.ExportFilter#exclude(org.openmdx.base.naming.Path)
         */
        public boolean exclude(Path objectId) {
            return objectId.size() <= 5; // Reject authorities, providers and segments
        }

        /**
         * Extract the simple name from a qualified name
         * 
         * @param qualifiedName
         * 
         * @return the corresponding simple name
         */
        private static String getSimpleName(String qualifiedName) {
            return qualifiedName.substring(qualifiedName.lastIndexOf(':') + 1);
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.application.xml.spi.ExportFilter#include(javax.jmi.model.AggregationKind, java.lang.String, int)
         */
        public boolean include(
            AggregationKind aggregationKind,
            String qualifiedName,
            int distance
        ) {
            if(distance >= DISTANCE.length) {
                return false;
            } else if (referenceFilter == null || aggregationKind == AggregationKindEnum.NONE) {
                return true;
            } else {
                String simpleName = getSimpleName(qualifiedName);
                int qualifiedNameAndDistanceLength = qualifiedName.length() + 3;
                int simpleNameAndDistanceLength = simpleName.length() + 3;
                for (
                    int i = distance; 
                    i < DISTANCE.length; 
                    i++
                ) {
                    for(String referenceFilter : this.referenceFilter) {
                        int filterLength = referenceFilter.length();
                        if(
                            (
                                filterLength == qualifiedNameAndDistanceLength && 
                                referenceFilter.startsWith(qualifiedName) && 
                                referenceFilter.endsWith(DISTANCE[i])
                            ) || (
                                filterLength == simpleNameAndDistanceLength && 
                                referenceFilter.startsWith(simpleName) && 
                                referenceFilter.endsWith(DISTANCE[i])
                            )
                        ){
                            return true;
                        }
                    }
                }
                return false;
            }
        }

    }

}

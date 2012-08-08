/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Audit_1.java,v 1.42 2009/06/01 16:28:27 wfro Exp $
 * Description: accessor.Audit_1 plugin
 * Revision:    $Revision: 1.42 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/01 16:28:27 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 */
package org.openmdx.audit1.accessor.layer.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderOperations;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.DataproviderRequestContexts;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.RequestCollection;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.layer.persistence.jdbc.Database_1;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.base.query.AttributeSpecifier;
import org.openmdx.base.query.Directions;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
import org.openmdx.base.text.format.DatatypeFormat;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * Plugin implementing the org:openmdx:compatibility:audit1 features. The plugin does the following:
 * <ul>
 *   <li>intercepts replace() and modify() requests. These requests are delegated
 *       1:1 to 'object store' plugin. The returned modified object - also called
 *       object after image - is then propagated as create-request to the configured
 *       audit provider.</li>
 *   <li>implements the derived features of audit1 for get() and find()
 *       operations. This requires the access to both, the 'object store' plugin 
 *       and the audit provider.</li>
 *   <li>The first configured provider resource is used as audit provider.</li>
 *   <li>The current implementation requires Database_1 to be the 'object store'
 *       plugin (the plugin extends Database_1). This is a restriction implied
 *       by openMDX 1 and not by the Audit_1 plugin itself.</li> 
 * </ul> 
 */
@SuppressWarnings("unchecked")
public class Audit_1
extends Database_1 {

    //------------------------------------------------------------------------
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws ServiceException {

        super.activate(
            id, 
            configuration, 
            delegation
        );

        // get audit provider
        List dataproviders = configuration.values(
            LayerConfigurationEntries.DATAPROVIDER
        );
        if(dataproviders.size() < 2) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "two audit providers must be configured: dataprovider:0 audit objects; dataprovider:1 units of work. They must be configured with configuration option  'dataprovider'"
            );
        }
        this.auditProviderBeforeImage = (Dataprovider_1_0)dataproviders.get(0);
        this.auditProviderUnitOfWork = (Dataprovider_1_0)dataproviders.get(1);

        // get audit mapping
        this.auditMapping = new HashMap();
        List exposedPaths = configuration.values(
            SharedConfigurationEntries.EXPOSED_PATH
        );
        List auditPaths = configuration.values(
            LayerConfigurationEntries.AUDIT_MAPPING
        );
        if(exposedPaths.size() != auditPaths.size()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "for each exposed path a corresponding audit path must be configured using the option 'auditMapping'",
                new BasicException.Parameter("exposed paths", exposedPaths),
                new BasicException.Parameter("audit paths", auditPaths)
            );
        }
        for(
                ListIterator i = exposedPaths.listIterator();
                i.hasNext();
        ) {
            int index = i.nextIndex();
            this.auditMapping.put(
                i.next(),
                new Path((String)auditPaths.get(index))
            );
        }
        SysLog.detail("configured audit mapping", this.auditMapping);
        // Switches
        this.datatypeFormat = configuration.isOn(
            SharedConfigurationEntries.XML_DATATYPES
        ) ? DatatypeFormat.newInstance(true) : null;
        this.returnAllInvolved = configuration.isOn(
            LayerConfigurationEntries.RETURN_ALL_INVOLVED
        );

    }

    //------------------------------------------------------------------------
    // Audit_1
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    private boolean areEqual(
        Object e1,
        Object e2
    ) {
        return
        (((e1 == null) || (e2 == null)) && (e1 == e2)) ||
        e1.equals(e2);        
    }

    //------------------------------------------------------------------------
    /**
     * Return the set of attributes which's values changed in o2 relative to o1.
     */
    private Set getChangedAttributes(
        MappedRecord o1,
        MappedRecord o2
    ) throws ServiceException {
        if(o2 == null) {
            return new HashSet();
        }        
        ObjectHolder_2Facade o1Facade;
        ObjectHolder_2Facade o2Facade;
        try {
            o1Facade = ObjectHolder_2Facade.newInstance(o1);
            o2Facade = ObjectHolder_2Facade.newInstance(o2);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        // Touch all o1 attributes in o2
        for(
            Iterator i = o1Facade.getValue().keySet().iterator();
            i.hasNext();
        ) {
            o2Facade.attributeValues((String)i.next());
        }        
        // Diff
        Set changedAttributes = new HashSet();
        for(
            Iterator i = o2Facade.getValue().keySet().iterator();
            i.hasNext();
        ) {
            String attributeName = (String)i.next();
            SparseList v1 = o1Facade.attributeValues(attributeName);
            SparseList v2 = o2Facade.attributeValues(attributeName);
            boolean isEqual =
                (v1.firstIndex() == v2.firstIndex()) &&
                (v1.lastIndex() == v2.lastIndex()) &&
                !SystemAttributes.OBJECT_INSTANCE_OF.equals(attributeName) &&
                !SystemAttributes.OBJECT_IDENTITY.equals(attributeName);
            if(isEqual) {
                // Compare the elements of the two lists
                for(int j = v1.firstIndex(); j < v1.lastIndex(); j++) {
                    Object e1 = v1.get(j);
                    Object e2 = v1.get(j);
                    // Convert String to XMLGregorianCalendar if either operand is an XMLGregorianCalendar
                    if(this.useDatatypes()) {                  
                        if((e1 instanceof XMLGregorianCalendar) && (e2 instanceof String)) {
                            e2 = this.datatypeFormat.marshal(e2);
                        }
                        if((e2 instanceof XMLGregorianCalendar) && (e1 instanceof String)) {
                            e1 = this.datatypeFormat.marshal(e1);
                        }
                    }
                    if(!areEqual(e1, e2)) {
                        isEqual = false;
                        break;
                    }
                }
            }          
            if(isEqual) {
                changedAttributes.add(attributeName);                                
            }
        }
        return changedAttributes;
    }

    //------------------------------------------------------------------------
    /**
     * Maps a given objectPath to the corresponding audit path.
     * <p>
     * If auditAuthority == true the authority of the returned path is set
     * to 'org:openmdx:compatibility:audit1', otherwise it is set to 
     * the authority configured with 'auditMapping'. 
     */
    private Path mapAuditPath(
        Path objectPath,
        boolean auditAuthority
    ) throws ServiceException {
        for(
            Iterator i = this.auditMapping.entrySet().iterator();
            i.hasNext();
        ) {
            Entry entry = (Entry)i.next();
            if(objectPath.startsWith((Path)entry.getKey())) {
                Path mappedAuditPath = new Path(((Path)entry.getValue()));
                Path auditPath = new Path("");
                for(int j = 0; j < objectPath.size(); j++) {
                    // replace starting path components with configured audit path
                    if(auditPath.size() < mappedAuditPath.size()) {
                        if(auditAuthority && (j == 0)) {
                            auditPath.add("org:openmdx:compatibility:audit1");
                        }
                        else {
                            auditPath.add(mappedAuditPath.get(j));
                        }
                    }
                    // do not modify remaining path components
                    else {
                        auditPath.add(objectPath.get(j));
                    }
                }
                return auditPath;
            }
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE,
            "no audit provider found for path",
            new BasicException.Parameter("path", objectPath)
        );
    }

    //------------------------------------------------------------------------
    /**
     * convert object to audit object (rewrite path according to mapping)
     */
    private MappedRecord objectToBeforeImage(
        MappedRecord object,
        String unitOfWorkId
    ) throws ServiceException {
        Path mappedPath = this.mapAuditPath(
            ObjectHolder_2Facade.getPath(object),
            false
        );
        String lastComponent = mappedPath.getBase() + ":uow:" + unitOfWorkId;
        Path beforeImagePath = mappedPath.getParent().getChild(lastComponent);
        MappedRecord beforeImage;
        try {
            beforeImage = ObjectHolder_2Facade.cloneObject(object);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        try {
            ObjectHolder_2Facade.newInstance(beforeImage).setPath(beforeImagePath);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        if(this.useDatatypes()) this.convertXMLDatatypeValues(beforeImage);
        return beforeImage;
    }

    //------------------------------------------------------------------------
    private void addAsNamespace(
        MappedRecord target,
        String namespaceId,
        MappedRecord source
    ) throws ServiceException {
        ObjectHolder_2Facade targetFacade = null;
        ObjectHolder_2Facade sourceFacade = null;
        try {
            targetFacade = ObjectHolder_2Facade.newInstance(target);
            sourceFacade = ObjectHolder_2Facade.newInstance(source);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        if(source != null) {
            for(
                Iterator j = targetFacade.getValue().keySet().iterator();
                j.hasNext();
            ) {
                String attributeName = (String)j.next();
                if(SystemAttributes.OBJECT_CLASS.equals(attributeName)) {
                    targetFacade.getValue().setRecordName(
                        sourceFacade.getValue().getRecordName()
                    );
                }
                else {
                    targetFacade.attributeValues(namespaceId + ":" + attributeName).addAll(
                        sourceFacade.attributeValues(attributeName)
                    );
                }
            }
        }
    }

    //------------------------------------------------------------------------
    /**
     * Convert a list of beforeImages to a list of Involved.
     */
    private List beforeImagesToInvolved(
        ServiceHeader header,
        Path auditablePath,
        List<MappedRecord> beforeImages
    ) throws ServiceException {
        // get current auditable. The current is required as last/current 'BeforeImage'
        MappedRecord current = null;
        try {
            try {
                current = super.get(
                    header,
                    new DataproviderRequest(
                        ObjectHolder_2Facade.newInstance(auditablePath).getDelegate(),
                        DataproviderOperations.OBJECT_RETRIEVAL,
                        AttributeSelectors.ALL_ATTRIBUTES,
                        null
                    )
                ).getObject();
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
        }
        catch(ServiceException e) {
            // in case the object was removed the 'AfterImage' of the last
            // involved is missing
            if(e.getExceptionCode() != BasicException.Code.NOT_FOUND) {
                throw e;
            }
        }
        // iterate the list to guarantee that all objects are present
        // and list size is known. This is required in case of 
        // batchSize of audit provider < number of involved objects and
        // TOTAL is unknown
        int ii = 0;
        for(
            Iterator i = beforeImages.iterator(); 
            i.hasNext(); 
            ii++
        ) {
            i.next();
        }

        // The involved objects are the difference objects of the beforeImages
        List<MappedRecord> involvedObjects = new ArrayList<MappedRecord>();
        List<MappedRecord> objects = new ArrayList<MappedRecord>(beforeImages);
        objects.add(current);
        for(
            int i = 1; 
            i < objects.size(); 
            i++
        ) {
            MappedRecord beforeImage = objects.get(i-1);
            ObjectHolder_2Facade beforeImageFacade;
            try {
                beforeImageFacade = ObjectHolder_2Facade.newInstance(beforeImage);
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
            beforeImageFacade.clearAttributeValues(SystemAttributes.OBJECT_IDENTITY).add(auditablePath.toURI());
            MappedRecord afterImage = objects.get(i);
            if(afterImage != null) {
                try {
                    ObjectHolder_2Facade.newInstance(afterImage).clearAttributeValues(SystemAttributes.OBJECT_IDENTITY).add(
                        auditablePath.toURI()
                    );
                } 
                catch (ResourceException e) {
                    throw new ServiceException(e);
                }
            }
            // path of involved = <auditablePath>/involved/<unitOfWorkId>
            PathComponent base = new PathComponent(beforeImageFacade.getPath().getBase());
            String unitOfWorkId = base.getLastField();
            ObjectHolder_2Facade involvedFacade;
            try {
                involvedFacade = ObjectHolder_2Facade.newInstance(
                    auditablePath.getDescendant(
                        "view:Audit:involved", unitOfWorkId
                    ),
                    "org:openmdx:compatibility:audit1:Involved"
                );
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
            // unitOfWorkId
            involvedFacade.attributeValues("unitOfWorkId").add(
                unitOfWorkId
            );
            // taskId not supported currently
            involvedFacade.attributeValues("taskId").add(
                "N/A"
            );
            // modifiedFeature
            involvedFacade.attributeValues("modifiedFeature").addAll(
                this.getChangedAttributes(beforeImage, afterImage)
            );

            this.addAsNamespace(
                involvedFacade.getDelegate(),
                "view:BeforeImage",
                beforeImage
            );
            this.addAsNamespace(
                involvedFacade.getDelegate(),
                "view:AfterImage",
                afterImage
            );

            involvedObjects.add(
                involvedFacade.getDelegate()
            );
        }
        SysLog.trace("involved objects", involvedObjects);
        return involvedObjects;
    }

    //------------------------------------------------------------------------
    private List getBeforeImages(
        ServiceHeader header,
        Path involvedRefeference
    ) throws ServiceException {
        // path of before images without :uow suffix
        Path beforeImageCorePath = this.mapAuditPath(
            involvedRefeference, 
            false
        ).getParent();
        RequestCollection channel = new RequestCollection(
            header,
            this.auditProviderBeforeImage
        );
        List beforeImages = channel.addFindRequest(
            beforeImageCorePath.getParent(),
            new FilterProperty[]{
                new FilterProperty(
                    Quantors.THERE_EXISTS,
                    SystemAttributes.OBJECT_IDENTITY, 
                    FilterOperators.IS_LIKE,
                    beforeImageCorePath.toURI() + ":uow:%"
                )
            },
            AttributeSelectors.ALL_ATTRIBUTES,
            new AttributeSpecifier[]{
                new AttributeSpecifier(
                    SystemAttributes.CREATED_AT,
                    0,
                    Directions.ASCENDING
                )
            },
            0,
            Integer.MAX_VALUE,
            Directions.ASCENDING
        );
        SysLog.trace("before images", beforeImages);
        return beforeImages;
    }

    //------------------------------------------------------------------------
    private MappedRecord getUnitOfWork(
        RequestCollection unitOfWorkChannel,
        Path unitOfWorkPath
    ) throws ServiceException {
        // get/create UnitOfWork
        MappedRecord unitOfWork = null;
        try {
            // try to get unit of work object
            unitOfWork = unitOfWorkChannel.addGetRequest(
                unitOfWorkPath,
                AttributeSelectors.ALL_ATTRIBUTES,
                null
            );
        }
        catch(ServiceException e) {
            if(e.getExceptionCode() != BasicException.Code.NOT_FOUND) {
                throw e; 
            }
            // create if it does not exist
            try {
                unitOfWork = ObjectHolder_2Facade.newInstance(
                    unitOfWorkPath,
                    "org:openmdx:compatibility:audit1:UnitOfWork"
                ).getDelegate();
            } 
            catch (ResourceException e0) {
                throw new ServiceException(e0);
            }
            unitOfWorkChannel.addCreateRequest(
                unitOfWork
            );
        }
        return unitOfWork;
    }

    //------------------------------------------------------------------------
    private boolean isInvolvedRequest(
        Path path
    ) {
        String referenceName = path.size() % 2 == 0
        ? path.get(path.size()-1)
            : path.get(path.size()-2);
        return "view:Audit:involved".equals(referenceName);
    }

    //------------------------------------------------------------------------
    private void storeBeforeImage(
        MappedRecord _beforeImageAsObject,
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        // get objectToBeReplaced with an explicit get if not defined
        MappedRecord beforeImageAsObject = _beforeImageAsObject == null ? 
            super.get(
                header,
                new DataproviderRequest(
                    request.object(),
                    request.operation(),
                    AttributeSelectors.ALL_ATTRIBUTES,
                    request.attributeSpecifier()
                )
            ).getObject() : 
                _beforeImageAsObject;
        MappedRecord beforeImage = this.objectToBeforeImage(
            beforeImageAsObject,
            this.unitOfWorkId
        );
        // lazy-init beforeImageChannel and unitsOfWork
        if(this.beforeImageChannel == null) {
            SysLog.trace("starting new batch");
            // create channel on-demand (will be reset in epilog.
            // Channel is used to store before images. A batching 
            // channel minimizes th roundtrips to the audit provider. 
            this.beforeImageChannel = new RequestCollection(
                header,
                this.auditProviderBeforeImage
            );
            this.beforeImageChannel.beginBatch();

            // pre-allocate units of work objects. They are modified during
            // create, modify, remove, replace and set operations and stored
            // stored in epilog(). This minimizes replace requests units of
            // work objects.
            this.unitsOfWork = new HashMap();
        }

        // lazy-creation of unit of work object
        Path mappedPath = this.mapAuditPath(
            request.path(),
            true
        );    
        Path unitOfWorkPath = mappedPath.getPrefix(5).getDescendant(
            "unitOfWork", this.unitOfWorkId
        );
        if(this.unitsOfWork.get(unitOfWorkPath) == null) {
            try {
                this.unitsOfWork.put(
                    unitOfWorkPath,
                    ObjectHolder_2Facade.newInstance(
                        unitOfWorkPath
                    ).getDelegate()
                );
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
            SysLog.trace("unit of work added", unitOfWorkPath);
        }

        // create before image
        this.beforeImageChannel.addCreateRequest(
            beforeImage
        );

        // update unit of work (will be stored in epilog)    
        MappedRecord unitOfWork = this.unitsOfWork.get(
            unitOfWorkPath
        );
        if(unitOfWork == null) {
            SysLog.trace("unitOfWork is null for path", unitOfWorkPath);
        }
        ObjectHolder_2Facade unitOfWorkFacade;
        try {
            unitOfWorkFacade = ObjectHolder_2Facade.newInstance(unitOfWork);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        Set involvedObjects = new HashSet(unitOfWorkFacade.attributeValues("involved"));
        involvedObjects.add(
            request.path().getDescendant(
                "view:Audit:involved", this.unitOfWorkId
            )
        );
        unitOfWorkFacade.clearAttributeValues("involved").addAll(
            involvedObjects
        );
    }

    //------------------------------------------------------------------------
    private void completeObject(
        MappedRecord object
    ) throws ServiceException {
        ObjectHolder_2Facade facade;
        try {
            facade = ObjectHolder_2Facade.newInstance(object);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        // all objects managed by audit1 plugin provide the namespace 'Audit'
        facade.clearAttributeValues("view:Audit:" + SystemAttributes.OBJECT_CLASS).add(
            "org:openmdx:compatibility:audit1:Auditable"
        );
    }

    //------------------------------------------------------------------------
    private DataproviderReply completeReply(
        DataproviderReply reply
    ) throws ServiceException {
        for(int i = 0; i < reply.getObjects().length; i++) {
            this.completeObject(
                reply.getObjects()[i]
            );
        }
        return reply;
    }

    //------------------------------------------------------------------------
    // Layer_1_0
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    public void prolog(
        ServiceHeader header,
        DataproviderRequest[] requests
    ) throws ServiceException {
        SysLog.trace("entering prolog", new Integer(requests.length));
        super.prolog(
            header,
            requests
        );
        String unitOfWorkId = requests.length == 0 ? 
            null : 
            (String)requests[0].context(DataproviderRequestContexts.UNIT_OF_WORK_ID).get(0);

        SysLog.trace("unit of work (request)", unitOfWorkId);
        SysLog.trace("unit of work (current)", this.unitOfWorkId);

        // guarantee that this.unitOfWorkId has always a valid value
        this.unitOfWorkId = unitOfWorkId != null ? 
            unitOfWorkId : 
            this.unitOfWorkId != null ? 
                this.unitOfWorkId :
                super.uidAsString();    
    }

    //------------------------------------------------------------------------
    public void epilog(
        ServiceHeader header,
        DataproviderRequest[] requests,
        DataproviderReply[] replies
    ) throws ServiceException {
        super.epilog(
            header,
            requests,
            replies
        );
        // update units of work and replace
        if(
            (this.unitsOfWork != null) &&
            (this.beforeImageChannel != null)
        ) {
            RequestCollection unitOfWorkChannel = new RequestCollection(
                header,
                this.auditProviderUnitOfWork
            );
            for(
                Iterator i = this.unitsOfWork.keySet().iterator();
                i.hasNext();
            ) {
                Path unitOfWorkPath = (Path)i.next();
                MappedRecord sourceUnitOfWork = this.unitsOfWork.get(unitOfWorkPath);
                ObjectHolder_2Facade sourceUnitOfWorkFacade;
                try {
                    sourceUnitOfWorkFacade = ObjectHolder_2Facade.newInstance(sourceUnitOfWork);
                } 
                catch (ResourceException e) {
                    throw new ServiceException(e);
                }
                if(
                    (sourceUnitOfWorkFacade.getAttributeValues("involved") != null) && 
                    !sourceUnitOfWorkFacade.attributeValues("involved").isEmpty()
                ) {
                    MappedRecord unitOfWorkToBeReplaced = this.getUnitOfWork(
                        unitOfWorkChannel,
                        unitOfWorkPath
                    );
                    ObjectHolder_2Facade.getValue(unitOfWorkToBeReplaced).putAll(
                        ObjectHolder_2Facade.getValue(this.unitsOfWork.get(unitOfWorkPath))
                    );
                    unitOfWorkChannel.addReplaceRequest(
                        unitOfWorkToBeReplaced
                    );
                }
            }
        }

        // reset
        if(this.unitsOfWork != null) {
            this.unitsOfWork.clear();
        }
        this.unitOfWorkId = null;

        // store before images and reset
        if(this.beforeImageChannel != null) {
            SysLog.trace("storing before images");
            this.beforeImageChannel.endBatch();
        }
        this.beforeImageChannel = null;
    }

    //------------------------------------------------------------------------
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {

        // get involved objects
        if(this.isInvolvedRequest(request.path())) {
            List involvedObjects = this.beforeImagesToInvolved(
                header,
                request.path().getPrefix(request.path().size()-2),
                this.getBeforeImages(
                    header,
                    request.path().getParent()
                )
            );
            int pos = -1;
            MappedRecord involved = null;
            int ii = 0;
            for(                
                Iterator<MappedRecord> i = involvedObjects.iterator();
                i.hasNext();
                ii++
            ) {
                involved = i.next();
                if(ObjectHolder_2Facade.getPath(involved).equals(request.path())) {
                    pos = ii;
                    break;
                }
            }
            if(pos >= 0) {
                if(this.returnAllInvolved) {
                    // Put requested involved at index 0 and
                    // return list of all involved objects. Returning
                    // all involved objects may reduce request roundtrips.
                    involvedObjects.remove(pos);
                    involvedObjects.add(0, involved);
                    return this.completeReply(
                        new DataproviderReply(
                            involvedObjects
                        )
                    );                    
                }
                else {
                    return this.completeReply(
                        new DataproviderReply(
                            involved
                        )
                    );
                }
            }
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "requested Involved not found",
                    new BasicException.Parameter("path", request.path())
                );
            }
        }
        else {
            return this.completeReply(
                super.get(
                    header,
                    request
                )
            );
        }
    }

    //------------------------------------------------------------------------
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {

        // get involved objects
        if(this.isInvolvedRequest(request.path())) {
            List involvedObjects = this.getBeforeImages(
                header,
                request.path()
            );      
            // map paths to .../view/Audit/involved/<unit of work id>
            DataproviderReply reply = new DataproviderReply(
                this.beforeImagesToInvolved(
                    header,
                    request.path().getPrefix(request.path().size()-1),
                    involvedObjects
                )
            );
            reply.context(DataproviderReplyContexts.HAS_MORE).set(0, Boolean.FALSE);
            reply.context(DataproviderReplyContexts.TOTAL).set(0,new Integer(involvedObjects.size()));
            return this.completeReply(
                reply
            );
        }
        else {
            return this.completeReply(
                super.find(
                    header,
                    request
                )
            );
        }
    }

    //------------------------------------------------------------------------
    private MappedRecord getBeforeImageView(
        MappedRecord object
    ) throws ServiceException {
        try {
            ObjectHolder_2Facade facade = ObjectHolder_2Facade.newInstance(object);
            ObjectHolder_2Facade beforeImageFacade = ObjectHolder_2Facade.newInstance(
                ObjectHolder_2Facade.getPath(object)            
            );
            for(
                Iterator i = facade.getValue().keySet().iterator();
                i.hasNext();
            ) {
                String attributeName = (String)i.next();
                if(attributeName.startsWith("view:BeforeImage:")) {
                    beforeImageFacade.attributeValues(
                        attributeName.substring("view:BeforeImage:".length())
                    ).addAll(
                        facade.attributeValues(attributeName)
                    );
                }
            }
            return beforeImageFacade.getDelegate();
        }
        catch(ResourceException e) {
            throw new ServiceException(e);
        }
    }

    //------------------------------------------------------------------------
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        DataproviderReply reply = super.replace(
            header,
            request
        );
        this.storeBeforeImage(
            this.getBeforeImageView(reply.getObject()),
            header,
            request
        );
        return reply;
    }

    //------------------------------------------------------------------------
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.storeBeforeImage(
            null, // datastore does not return a view:BeforeImage in case of modify
            header,
            request
        );
        return this.completeReply(
            super.modify(
                header,
                request
            )
        );
    }

    //------------------------------------------------------------------------
    public DataproviderReply remove(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.storeBeforeImage(
            null, // datastore does not return a view:BeforeImage in case of remove
            header,
            request
        );
        return this.completeReply(
            super.remove(
                header,
                request
            )
        );
    }

    //------------------------------------------------------------------------
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return this.completeReply(
            super.create(
                header,
                request
            )
        );
    }

    //------------------------------------------------------------------------

    /**
     * Tells whether XML datatype formatting is required
     * @return
     */
    protected boolean useDatatypes(){
        return this.datatypeFormat != null;
    }

    // --------------------------------------------------------------------------

    /**
     * Converting<ul>
     * <li><code>javax.xml.XMLGregorianCalendar</code>
     * <li><code>javax.xml.Duration</code>
     * </ul>values to <code>String</code> values.
     * 
     * @param object the <code>DataproviderObject</code> to be converted 
     * 
     * @throws ServiceException 
     */
    private void convertXMLDatatypeValues(
        MappedRecord object
    ) throws ServiceException{
        ObjectHolder_2Facade facade;
        try {
            facade = ObjectHolder_2Facade.newInstance(object);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        for(
            Iterator i = facade.getValue().keySet().iterator();
            i.hasNext();
        ) {
            for (
                ListIterator j = facade.attributeValues((String) i.next()).populationIterator();
                j.hasNext();
            ) {
                Object value = j.next();
                if(
                    value instanceof Duration || 
                    value instanceof XMLGregorianCalendar
                ) {
                    j.set(this.datatypeFormat.unmarshal(value));
                } 
                else {
                    break;
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------

    // map containing for each exposed path a corresponding audit path
    private Map auditMapping = null;
    private Dataprovider_1_0 auditProviderBeforeImage = null;
    private Dataprovider_1_0 auditProviderUnitOfWork = null;
    private String unitOfWorkId = null; // initialized by prolog
    private boolean returnAllInvolved = false;

    // for each request.path() the corresponding unit of work object
    // from audit provider. The units of work are retrieved/created
    // in prolog(), then modified in replace, create, modify requests
    // and in epilog() stored to audit provider. 
    private Map<Path,MappedRecord> unitsOfWork = null;

    // channel to audit provider used to store before images
    // Created during prolog() and committed in epilog()
    private RequestCollection beforeImageChannel = null;

    /**
     * Not <code>null</code> if <code>String</code> values for<ol>
     * <li><code>org::w3c::date</code>
     * <li><code>org::w3c::dateTime</code>
     * <li><code>org::w3c::duration</code>
     * </ol>should be converted to their corresponding XML datatypes<ol>
     * <li><code>javax.xml.XMLGregorianCalendar</code>
     * <li><code>javax.xml.XMLGregorianCalendar</code>
     * <li><code>javax.xml.Duration</code>
     * </ol>and vice versa.
     * 
     * @see LayerConfigurationEntries#XML_DATATYPES
     */
    private DatatypeFormat datatypeFormat;

}

//--- End of File -----------------------------------------------------------

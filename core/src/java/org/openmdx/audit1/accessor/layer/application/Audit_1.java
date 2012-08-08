/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Audit_1.java,v 1.32 2008/09/10 08:55:22 hburger Exp $
 * Description: accessor.Audit_1 plugin
 * Revision:    $Revision: 1.32 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:22 $
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

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DatatypeFormat;
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
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc.Database_1Jdbc2;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.naming.PathComponent;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
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
extends Database_1Jdbc2 {

    //------------------------------------------------------------------------
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception, ServiceException {

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
        DataproviderObject_1_0 o1,
        DataproviderObject_1_0 o2
    ) throws ServiceException {
        if(o2 == null) {
            return new HashSet();
        }        
        // Touch all o1 attributes in o2
        for(
                Iterator i = o1.attributeNames().iterator();
                i.hasNext();
        ) {
            o2.values((String)i.next());
        }        
        // Diff
        Set changedAttributes = new HashSet();
        for(
                Iterator i = o2.attributeNames().iterator();
                i.hasNext();
        ) {
            String attributeName = (String)i.next();
            SparseList v1 = o1.values(attributeName);
            SparseList v2 = o2.values(attributeName);
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
    private DataproviderObject objectToBeforeImage(
        DataproviderObject_1_0 object,
        String unitOfWorkId
    ) throws ServiceException {
        Path mappedPath = this.mapAuditPath(
            object.path(),
            false
        );
        String lastComponent = mappedPath.getBase() + ":uow:" + unitOfWorkId;
        Path beforeImagePath = mappedPath.getParent().getChild(lastComponent);
        DataproviderObject beforeImage = new DataproviderObject(object);
        beforeImage.path().setTo(beforeImagePath);
        if(useDatatypes()) this.convertXMLDatatypeValues(beforeImage);
        return beforeImage;
    }

    //------------------------------------------------------------------------
    private void addAsNamespace(
        DataproviderObject target,
        String namespaceId,
        DataproviderObject source
    ) {
        if(source != null) {
            for(
                    Iterator j = source.attributeNames().iterator();
                    j.hasNext();
            ) {
                String attributeName = (String)j.next();
                target.values(namespaceId + ":" + attributeName).addAll(
                    SystemAttributes.OBJECT_CLASS.equals(attributeName) ? 
                        source.values(attributeName).subList(0, 1) :
                            source.values(attributeName)
                );
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
        List beforeImages
    ) throws ServiceException {

        // get current auditable. The current is required as last/current 'BeforeImage'
        DataproviderObject current = null;
        try {
            current = super.get(
                header,
                new DataproviderRequest(
                    new DataproviderObject(auditablePath),
                    DataproviderOperations.OBJECT_RETRIEVAL,
                    AttributeSelectors.ALL_ATTRIBUTES,
                    null
                )
            ).getObject();
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
        List involvedObjects = new ArrayList();
        List objects = new ArrayList(beforeImages);
        objects.add(current);
        for(
                int i = 1; 
                i < objects.size(); 
                i++
        ) {
            DataproviderObject beforeImage = (DataproviderObject)objects.get(i-1);
            beforeImage.clearValues(SystemAttributes.OBJECT_IDENTITY).add(auditablePath.toUri());
            DataproviderObject afterImage = (DataproviderObject)objects.get(i);
            if(afterImage != null) {
                afterImage.clearValues(SystemAttributes.OBJECT_IDENTITY).add(auditablePath.toUri());
            }

            // path of involved = <auditablePath>/involved/<unitOfWorkId>
            PathComponent base = new PathComponent(beforeImage.path().getBase());
            String unitOfWorkId = base.getLastField();
            DataproviderObject involved = new DataproviderObject(
                auditablePath.getDescendant(
                    "view:Audit:involved", unitOfWorkId
                )
            );
            involved.values(SystemAttributes.OBJECT_CLASS).add(
                "org:openmdx:compatibility:audit1:Involved"
            );

            // unitOfWorkId
            involved.values("unitOfWorkId").add(
                unitOfWorkId
            );

            // taskId not supported currently
            involved.values("taskId").add(
                "N/A"
            );

            // modifiedFeature
            involved.values("modifiedFeature").addAll(
                this.getChangedAttributes(beforeImage, afterImage)
            );

            this.addAsNamespace(
                involved,
                "view:BeforeImage",
                beforeImage
            );
            this.addAsNamespace(
                involved,
                "view:AfterImage",
                afterImage
            );

            involvedObjects.add(involved);
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
                    beforeImageCorePath.toUri() + ":uow:%"
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
    private DataproviderObject getUnitOfWork(
        RequestCollection unitOfWorkChannel,
        Path unitOfWorkPath
    ) throws ServiceException {
        // get/create UnitOfWork
        DataproviderObject unitOfWork = null;
        try {
            // try to get unit of work object
            unitOfWork = new DataproviderObject(
                unitOfWorkChannel.addGetRequest(
                    unitOfWorkPath,
                    AttributeSelectors.ALL_ATTRIBUTES,
                    null
                )
            );
        }
        catch(ServiceException e) {
            if(e.getExceptionCode() != BasicException.Code.NOT_FOUND) {
                throw e; 
            }
            // create if it does not exist
            unitOfWork = new DataproviderObject(
                unitOfWorkPath
            );
            unitOfWork.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:compatibility:audit1:UnitOfWork");
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
        DataproviderObject _beforeImageAsObject,
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        // get objectToBeReplaced with an explicit get if not defined
        DataproviderObject beforeImageAsObject = _beforeImageAsObject == null ? 
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

            DataproviderObject beforeImage = this.objectToBeforeImage(
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
                this.unitsOfWork.put(
                    unitOfWorkPath,
                    new DataproviderObject(
                        unitOfWorkPath
                    )
                );
                SysLog.trace("unit of work added", unitOfWorkPath);
            }

            // create before image
            this.beforeImageChannel.addCreateRequest(
                beforeImage
            );

            // update unit of work (will be stored in epilog)    
            DataproviderObject unitOfWork = (DataproviderObject)this.unitsOfWork.get(
                unitOfWorkPath
            );
            if(unitOfWork == null) {
                SysLog.trace("unitOfWork is null for path", unitOfWorkPath);
            }
            Set involvedObjects = new HashSet(unitOfWork.values("involved"));
            involvedObjects.add(
                request.path().getDescendant(
                    "view:Audit:involved", this.unitOfWorkId
                )
            );
            unitOfWork.clearValues("involved").addAll(
                involvedObjects
            );
    }

    //------------------------------------------------------------------------
    private void completeObject(
        DataproviderObject object
    ) {
        // all objects managed by audit1 plugin provide the namespace 'Audit'
        object.clearValues("view:Audit:" + SystemAttributes.OBJECT_CLASS).add(
            "org:openmdx:compatibility:audit1:Auditable"
        );
    }

    //------------------------------------------------------------------------
    private DataproviderReply completeReply(
        DataproviderReply reply
    ) {
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
        String unitOfWorkId = requests.length == 0 
        ? null 
            : (String)requests[0].context(DataproviderRequestContexts.UNIT_OF_WORK_ID).get(0);

        SysLog.trace("unit of work (request)", unitOfWorkId);
        SysLog.trace("unit of work (current)", this.unitOfWorkId);

        // guarantee that this.unitOfWorkId has always a valid value
        this.unitOfWorkId = unitOfWorkId != null
        ? unitOfWorkId
            : this.unitOfWorkId != null
            ? this.unitOfWorkId
                : super.uidAsString();    
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
                DataproviderObject sourceUnitOfWork = (DataproviderObject)this.unitsOfWork.get(unitOfWorkPath);
                if(
                        (sourceUnitOfWork.getValues("involved") != null) && 
                        (sourceUnitOfWork.values("involved").size() > 0)
                ) {
                    DataproviderObject unitOfWorkToBeReplaced = this.getUnitOfWork(
                        unitOfWorkChannel,
                        unitOfWorkPath
                    );
                    unitOfWorkToBeReplaced.addClones(
                        (DataproviderObject_1_0)this.unitsOfWork.get(unitOfWorkPath),
                        true
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
            DataproviderObject involved = null;
            int ii = 0;
            for(                
                    Iterator i = involvedObjects.iterator();
                    i.hasNext();
                    ii++
            ) {
                involved = (DataproviderObject)i.next();
                if(involved.path().equals(request.path())) {
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
    private DataproviderObject getBeforeImageView(
        DataproviderObject object
    ) throws ServiceException {
        DataproviderObject beforeImage = new DataproviderObject(
            object.path()
        );
        for(
                Iterator i = object.attributeNames().iterator();
                i.hasNext();
        ) {
            String attributeName = (String)i.next();
            if(attributeName.startsWith("view:BeforeImage:")) {
                beforeImage.values(
                    attributeName.substring("view:BeforeImage:".length())
                ).addAll(
                    object.values(attributeName)
                );
            }
        }
        return beforeImage;
    }

    //------------------------------------------------------------------------
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest	request
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
        DataproviderRequest	request
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
        DataproviderObject_1_0 object
    ) throws ServiceException{
        for(
                Iterator i = object.attributeNames().iterator();
                i.hasNext();
        ) {
            for (
                    ListIterator j = object.values((String) i.next()).populationIterator();
                    j.hasNext();
            ) {
                Object value = j.next();
                if(
                        value instanceof Duration || 
                        value instanceof XMLGregorianCalendar
                ) {
                    j.set(this.datatypeFormat.unmarshal(value));
                } else {
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
    private Map unitsOfWork = null;

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

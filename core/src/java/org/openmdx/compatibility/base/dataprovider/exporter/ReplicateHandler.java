/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ReplicateHandler.java,v 1.20 2008/09/10 08:55:29 hburger Exp $
 * Description: handler for replicating a target provider to a source
 * Revision:    $Revision: 1.20 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:29 $
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
package org.openmdx.compatibility.base.dataprovider.exporter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.layer.model.RoleAttributes;
import org.openmdx.compatibility.base.dataprovider.layer.model.State_1_Attributes;
import org.openmdx.compatibility.base.dataprovider.layer.model.StopWatch_1;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.BasicException.Parameter;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;


/**
 * @author anyff
 */
@SuppressWarnings("unchecked")
public class ReplicateHandler 
implements TraversalHandler, ErrorHandler {

    /**
     * 
     */
    public ReplicateHandler() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructor for a ReplicateHandler.
     * <p>
     * The pathMap contains the mapping of source paths to target paths. In most
     * of the cases its enough to provide the starting section of the two paths.
     * In case of a partial replication the pathMap does not provide any
     * information about which parts are beeing replicated and which are left
     * out.
     *
     * @param header     header to use for requests to provider
     * @param provider   provider which gets replicated
     * @param model      model of the data
     * @param pathMap    mapping between the source and target provider
     */
    public ReplicateHandler(
        ServiceHeader header,
        Dataprovider_1_0 provider,
        Model_1_0 model,
        Map pathMap
    ) {
        this.model = model;
        this.pathMap = pathMap;

        this.writeRequestDispenser = new RequestDispenser(header, provider);
        this.readRequestDispenser = new RequestDispenser(header, provider);
    }


    /**
     * Start of a reference.
     * <p>
     * A new reference may only start after all the states of the object have
     * been received.
     *
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#startReference(java.lang.String)
     */
    public boolean startReference(
        String reference
    ) throws ServiceException {

        treatCollectedStates();

        return true;
    }

    /**
     * End of a reference.
     *
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#endReference(java.lang.String)
     */
    public void endReference(
        String reference
    ) throws ServiceException {
        // nothing to do
    }

    /**
     * Start of an object.
     *
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#startObject(java.lang.String, java.lang.String, java.lang.String, short)
     */
    public boolean startObject(
        Path parentPath,
        String qualifiedName,
        String qualifierName,
        String id,
        short operation
    ) throws ServiceException {
        if (this.lastOperation != OPERATION_UNSET) {
            throw new ServiceException(BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "Operation setting was not consumed as expected. Possible violation of protocol.",
                new Parameter[] {
                new Parameter("qualifiedName", qualifiedName),
                new Parameter("qualifierName", qualifierName),
                new Parameter("id", id),
                new Parameter("operation", operation),
                new Parameter("last operation", this.lastOperation)
            }
            );
        }
        this.lastOperation = operation;

        if (this.objectStates != null) {
            if (!this.objectStatesQualifiedName.equals(qualifiedName) 
                    ||
                    !this.objectStatesId.equals(id)
            ) {
                treatCollectedStates();
            }
            // else still same object, different state of it
        }

        // the states have been treated, must restart
        if (this.objectStates == null) {
            this.objectStates = new ArrayList();
            this.objectStatesQualifiedName = qualifiedName;
            this.objectStatesId = id;
        }

        return true;
    }

    /**
     * End of an object.
     *
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#endObject(java.lang.String)
     */
    public void endObject(String qualifiedName) throws ServiceException {
        if (qualifiedName.equals(_startingStatedQualifier)) {
            this._startingStatedQualifier = null;
        }
    }

    public Map getAttributeTags(
        DataproviderObject_1_0 object
    ) throws ServiceException {
        return null;
    }

    /**
     * Object with all features.
     *
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#featureComplete(org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0)
     */
    public boolean featureComplete(
        Path parentPath,
        DataproviderObject_1_0 object
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("featureComplete");

        SysLog.detail("featureComplete, op: " + this.lastOperation , object);

        // consume operation
        if (this.lastOperation == OPERATION_UNSET) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "Operation was not renewed; possible violation of protocol.",
                new Parameter[] {
                    new Parameter("last operation", this.lastOperation)
                }
            );
        }
        short operation = this.lastOperation;
        this.lastOperation = OPERATION_UNSET;

        if (object.path().toUri().indexOf(this.objectStatesId) <0) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "Object is not the one currently treated.",
                new Parameter[] {
                    new Parameter("current object id", this.objectStatesId),
                    new Parameter("object.path", object.path())
                }
            );

        }

        if (operation == TraversalHandler.SET_OP 
                && object.getValues(SystemAttributes.OBJECT_CLASS) != null
                && !object.getValues(SystemAttributes.OBJECT_CLASS).equals("org:openmdx:base:Authority")
        ) {

            DataproviderObject syncObject = mapRequestObject(object);

            // just collect that state, regardless if is is stated or roled or not at all
            // but leave out empty states, which belong to the currently treated
            // object
            if (object.attributeNames().size() > 2 &&
                    object.path().toUri().indexOf(this.objectStatesId) >= 0
            ) {
                this.objectStates.add(syncObject);
            }
            // else it's just the last, empty featureComplete, where the 
            // refrences will be contained

        }

        StopWatch_1.instance().stopTimer("featureComplete");

        return true;
    }

    /**
     * Remove references which exist in the target provider but not in the
     * source provider.
     *
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#contentComplete(org.openmdx.compatibility.base.naming.Path, java.lang.String, java.util.List)
     */
    public void contentComplete(
        Path objectPath,
        String objectClassName,
        List references
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("contentComplete");

        DataproviderObject_1_0 objectClass =
            this.model.getDereferencedType(objectClassName);

        for (Iterator refIter = objectClass.getValues("feature").iterator();
        refIter.hasNext();
        ) {
            String featureName = ((Path) refIter.next()).getBase();

            DataproviderObject_1_0 elementType = this.model.getElement(featureName);

            // reference
            if (isCompositeReference(elementType)) {
                Path childReferenceSource =
                    objectPath.getChild((String) elementType.values("name").get(0));

                // check if the path is contained in references
                if (!references.contains(childReferenceSource)) {
                    // get the objects at this path in target and delete them
                    List objects =
                        getExistingObjects(
                            mapRequestPath(childReferenceSource, LOCAL_PATH),
                            leadsToStatedClass(childReferenceSource)
                        );

                    // objectMap contains one entry per object
                    Map objectMap = new HashMap();

                    for (Iterator objIter = objects.iterator();
                    objIter.hasNext();
                    ) {
                        DataproviderObject_1_0 object =
                            (DataproviderObject_1_0) objIter.next();
                        objectMap.put(
                            createCorePath(object.path()),
                            object
                        );
                    }

                    for (Iterator del = objectMap.values().iterator();
                    del.hasNext();
                    ) {
                        DataproviderObject_1_0 targetObject =
                            (DataproviderObject_1_0) del.next();

                        deleteExistingObject(targetObject, null);
                    }
                }
            }
        }
        StopWatch_1.instance().stopTimer("contentComplete");
    }

    /**
     * Remove objects which are not needed at this reference.
     *
     * @param reference   the reference to complete in source format
     * @param sourceIds   the ids of the objects in source format
     *
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#referenceComplete(org.openmdx.compatibility.base.naming.Path, java.util.Collection)
     */
    public void referenceComplete(
        Path reference,
        Collection sourceIds
    ) throws ServiceException {
        // objectStates is always in use.
        StopWatch_1.instance().startTimer("referenceComplete");

        treatCollectedStates();

        if (reference.getBase().equals(RoleAttributes.REF_ROLE)) {
            removeSuperfluousRoles(reference, sourceIds);
        }
        else {
            removeSuperfluousObjects(reference, sourceIds);
        }        
        StopWatch_1.instance().stopTimer("referenceComplete");
    }

    /**
     * Delivering of startPaths allows to check for existence of the objects
     * in the target provider. An exception is thrown if the parent objects
     * do not exist.
     *
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#startTraversal(java.util.List)
     */
    public void startTraversal(List startPaths) throws ServiceException {
        this.writeRequestDispenser.startTraversal();

        this.deletedObjects = new HashSet();
    }

    /**
     * End traversal.
     *
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#endTraversal()
     */
    public void endTraversal() throws ServiceException {
        this.writeRequestDispenser.endTraversal();
    }

    /**
     * ReplicateHandler's transaction support is only accepted for write
     * transactions. With spice2 all calls within a transactional unit are
     * only performed on endUnitOfWork(), which does not allow intermediate
     * reads.
     *
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#setTransactionBehavior(short)
     */
    public void setTransactionBehavior(short transactionBehavior) {
        this.writeRequestDispenser.setTransactionBehavior(transactionBehavior);
    }

    /**
     * Return the currently active transaction behavior.
     *
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#getTransactionBehavior()
     */
    public short getTransactionBehavior() {
        return this.writeRequestDispenser.getTransactionBehavior();
    }

    // -- ErrorHandler ----------------------------------------------------------
    public void fatalError(
        ServiceException fatalError
    ) throws ServiceException {
        // rollback !!
    }

    /**
     * Can not handle error and continue; throw exception which should result 
     * in a fatalError.
     */
    public void error(
        ServiceException error
    ) throws ServiceException {
        SysLog.error("Can not handle error", error);
        throw new ServiceException(error);
    }


    /**
     * report the warning and continue.
     */
    public void warning(
        ServiceException warning
    ) throws ServiceException {
        SysLog.warning("received warning", warning);
    }


    /** 
     * Treat the states of the object which have been collected. 
     * This inlcudes treatement also of non stated objects.
     * 
     *
     */
    protected void treatCollectedStates(
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("treatCollectedStates");

        if (this.objectStates != null) {

            if (this.objectStates.size() > 0) {
                // System.out.println("#### Treat states of " + this._objectStatesId);

                boolean isToplevelObject = false;

                DataproviderObject object = (DataproviderObject)
                this.objectStates.iterator().next();

                // every object with one or more states is a top level object,
                // if there is no top level object starting with the same path
                if (isToplevelObject = isToplevelObjectPath(object.path())) {
                    ensureExistenceOfParents(object);  
                }
                this.writeRequestDispenser.startObject(isToplevelObject);

                if (this.model.isSubtypeOf(object, "org:openmdx:compatibility:state1:BasicState")) {
                    applyStates(objectStates, new HashMap() /* _rolesPerState*/ );
                }
                else {
                    setNonStatedObject(object);
                }

                this.writeRequestDispenser.endObject(isToplevelObject);
            }

            this.objectStatesId = null;
            this.objectStates = null;
            this.objectStatesQualifiedName = null;
        }
        StopWatch_1.instance().stopTimer("treatCollectedStates"); //(this._objectStates == null ? 0 : this._objectStates.size()));

    }


    /**
     * Find out if the elementType is a reference and the objects referenced
     * are contained.
     *
     * @param referenceType  type of the reference
     * @return boolean
     */
    protected boolean isCompositeReference(DataproviderObject_1_0 elementType)
    throws ServiceException {
        boolean isComposite = false;

        if (elementType.getValues(SystemAttributes.OBJECT_CLASS).get(0).equals("org:omg:model1:Reference")
        ) {
            String associationEnd =
                ((Path) elementType.getValues("referencedEnd").get(0)).getBase();

            DataproviderObject_1_0 associationEndType =
                this.model.getElement(associationEnd);

            isComposite =
                associationEndType.getValues("aggregation").get(0).equals("composite");
        }

        return isComposite;
    }


    /** 
     * Returns true if the path has already been identified as leading to a 
     * top level object or there is no top level object for the path supplied.
     * <p>
     * If the segment itself is defined as toplevel object, all the objects within
     * the segment are regarded as toplevel objects, too. If another path leading
     * to a single object is contained, that object is considered beeing a top
     * level object, not its children.
     * <p>
     * Note: the collection of top level paths is built up of the paths 
     * specified as parameters to this method. The top level paths are not 
     * previously set.
     * <p>
     * The paths specified should be mapped to represent a path in the target 
     * provider.
     * 
     * @param path  path of an object in target provider
     * @return true if it is a top level path, false otherwise
     */
    protected boolean isToplevelObjectPath(
        Path path
    ) {
        boolean isToplevel = false;

        int size = path.size();

        if (size == (SEGMENT_PATH_LENGTH)
                || size == (SEGMENT_PATH_LENGTH + 2)
        ) {
            isToplevel = true;
        } else {
            isToplevel = true;
            for (Iterator i = this.mappedStartPaths.iterator();
            i.hasNext() && isToplevel;
            ) {
                Path startPath = (Path) i.next();

                if (path.startsWith(startPath)) {
                    isToplevel = false;
                }
            }

            // don't add the ones beeing found based on the length,
            // to keep the collection small
            if (isToplevel) {
                this.mappedStartPaths.add(path);
            }
        }

        return isToplevel;

    }


    /**
     * Decide if the path is within a segment.
     *
     * @param path
     * @return boolean
     */
    protected boolean isInternalPath(
        Path path
    ) {
        return path.size() > SEGMENT_PATH_LENGTH;
    }

    protected boolean isRolePath(
        Path path
    ) {
        return 
        path.get(path.size()-2).equals(RoleAttributes.REF_ROLE) 
        || path.get(path.size()-1).equals(RoleAttributes.REF_ROLE);
    }


    /**
     * Ensure that the objects parent exist and are valid.
     * <p>
     * For stated objects, a time within validFrom, validTo is checked, not the
     * entire period.
     *
     * @param mappedObject
     */
    protected void ensureExistenceOfParents(
        DataproviderObject_1_0 mappedObject
    ) throws ServiceException {
        Path parentPath = toStatelessPath(new Path(mappedObject.path())).getParent().getParent();

        if (parentPath.size() >= 7) {
            try {
                getExistingState(
                    parentPath,
                    null,
                    mappedObject.getValues(State_1_Attributes.VALID_FROM),
                    mappedObject.getValues(State_1_Attributes.VALID_TO)
                );
            } catch (ServiceException se) {
                // only in case of noTransaction or OBJECT_TRANSACTION the data
                // has already been written 
                if ((this.writeRequestDispenser.getTransactionBehavior() == NO_TRANSACTION 
                        ||
                        this.writeRequestDispenser.getTransactionBehavior() == OBJECT_TRANSACTION
                ) 
                &&
                se.getExceptionStack().getExceptionCode() == BasicException.Code.NOT_FOUND
                ) {
                    throw new ServiceException(
                        se,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "No parent for object.", 
                        new Parameter[] {
                            new Parameter("object path", mappedObject.path()),
                            new Parameter("parent path", parentPath)
                        }
                    );
                } 
                else {
                    throw se;
                }
            }
        }
    }

    protected void setNonStatedObject(
        DataproviderObject_1_0 object
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("setNonStatedObject");
        DataproviderObject reqObj = new DataproviderObject(object);

        RequestCollection requests =
            this.writeRequestDispenser.getRequestCollection();

        SysLog.trace(
            "create non stated: ",
            reqObj.path()
        );

        requests.addSetRequest(reqObj);
        StopWatch_1.instance().stopTimer("setNonStatedObject");
    }

    protected void setObjectStates(List states) throws ServiceException {
        int count = 0;

        for (Iterator s = states.iterator(); s.hasNext();) {
            DataproviderObject reqObj = (DataproviderObject)s.next();
            SysLog.trace("create stated: " + reqObj.path() + " validFrom: "
                + reqObj.getValues(State_1_Attributes.VALID_FROM)
                + " validTo: " + reqObj.getValues(State_1_Attributes.VALID_TO)
            );
        }

//      List lastValidTo = new ArrayList();
//      lastValidTo.add(new String("initialDummy")); 
        for (Iterator s = states.iterator(); s.hasNext();) {
            DataproviderObject reqObj =
                new DataproviderObject((DataproviderObject_1_0) s.next());

            toStatelessPath(reqObj.path());
//          Path createPath = createCorePath(reqObj.path());

//          reqObj.path().setTo(createPath);

            RequestCollection requests =
                this.writeRequestDispenser.getRequestCollection();

            // new RequestCollection(_header, _provider);
            SysLog.trace("create stated: " + reqObj.path() + " validFrom: "
                + reqObj.getValues(State_1_Attributes.VALID_FROM)
                + " validTo: " + reqObj.getValues(State_1_Attributes.VALID_TO)
            );

//          if (lastValidTo.equals(reqObj.getValues(State_1_Attributes.VALID_FROM))) {
//          requests.addReplaceRequest(reqObj);
//          }
//          else {
//          requests.addCreateRequest(reqObj);
//          }
//          lastValidTo = reqObj.getValues(State_1_Attributes.VALID_TO);

            if (count++ == 0 || isRolePath(reqObj.path())) {
                requests.addCreateRequest(reqObj);
            } else {
                requests.addReplaceRequest(reqObj);
            }
        }
    }

    /**
     * replace a single state.
     * May only be used if the state already exists.
     *
     * @param newState
     */
    protected void replaceObjectState(DataproviderObject_1_0 newState)
    throws ServiceException {
        DataproviderObject reqObj =
            new DataproviderObject(newState);

        toStatelessPath(reqObj.path());

        RequestCollection requests =
            this.writeRequestDispenser.getRequestCollection();

        SysLog.trace("replace object state: " + reqObj.path() + " validFrom: "
            + reqObj.getValues(State_1_Attributes.VALID_FROM) + " validTo: "
            + reqObj.getValues(State_1_Attributes.VALID_TO)
        );

        requests.addReplaceRequest(reqObj);
    }

    /**
     * Create a role.
     *
     * @param role
     * @throws ServiceException
     */
    private void createRoleOfState(DataproviderObject_1_0 role)
    throws ServiceException {
        String roleName = findRole(role, null);

        DataproviderObject reqObj = new DataproviderObject(role);

        reqObj.path().setTo(
            createCorePath(reqObj.path()).add("role").add(roleName)
        );

        RequestCollection requests =
            this.writeRequestDispenser.getRequestCollection();

        SysLog.trace("create stated role: " + reqObj.path() + " validFrom: "
            + reqObj.getValues(State_1_Attributes.VALID_FROM) + " validTo: "
            + reqObj.getValues(State_1_Attributes.VALID_TO)
        );

        requests.addCreateRequest(reqObj);
    }

    /**
     * Update a role.
     *
     * @param role
     * @throws ServiceException
     */
    private void updateRoleOfState(DataproviderObject_1_0 role)
    throws ServiceException {
        String roleName = findRole(role, null);

        DataproviderObject reqObj = new DataproviderObject(role);

        reqObj.path().setTo(
            createCorePath(reqObj.path()).add("role").add(roleName)
        );

        RequestCollection requests =
            this.writeRequestDispenser.getRequestCollection();

        SysLog.trace("update stated role: " + reqObj.path() + " validFrom: "
            + reqObj.getValues(State_1_Attributes.VALID_FROM) + " validTo: "
            + reqObj.getValues(State_1_Attributes.VALID_TO)
        );

        requests.addReplaceRequest(reqObj);
    }

    /**
     * create a new Path without state or role attachments.
     * <p>
     *
     * @param extendedPath path to remove state or role attachements from
     * @return Path        new path
     */
    protected Path createCorePath(Path extendedPath) {
        Path path = new Path(extendedPath);
        int size = path.size();

        while (size > 0) {
            if ("role".equals(path.get(size - 1))
                    || "state".equals(path.get(size - 1))
            ) {
                path.remove(--size);
            } else if ("role".equals(path.get(size - 2))
                    || "state".equals(path.get(size - 2))
            ) {
                path.remove(--size);
                path.remove(--size);
            } else {
                // to end loop
                size = 0;
            }
        }

        return path;
    }

    /**
     * remove state references from the path supplied. 
     *
     * @param path   path to remove state attachements from
     * @return Path  same path as supplied, but without state attachments
     */
    protected Path toStatelessPath(Path path) {
        int size = path.size();

        while (size > 0) {
            if (State_1_Attributes.REF_VALID.equals(path.get(size - 1))
                    || State_1_Attributes.REF_STATE.equals(path.get(size - 1))
            ) {
                path.remove(--size);
            } 
            else if (State_1_Attributes.REF_VALID.equals(path.get(size - 2))
                    || State_1_Attributes.REF_STATE.equals(path.get(size - 2))
            ) {
                path.remove(--size);
                path.remove(--size);
            } else {
                // to end loop
                size = 0;
            }
        }

        return path;
    }

    /**
     * Determine if the objects at a certain reference are stated.
     *
     * @param path   path ending in reference
     */
    protected boolean leadsToStatedClass(
        Path path
    ) throws ServiceException {
        boolean leadsToStated = false;

        // 
        // Sequence of types in the array:
        //     lastReferencedType, 
        //     exposedType, 
        //     referencedType
        // 
        DataproviderObject_1_0[] types = this.model.getTypes(path);

        DataproviderObject_1_0 referencedType = types[2];

        for (Iterator i = referencedType.values("allSupertype").iterator();
        i.hasNext();
        ) {
            if ("org:openmdx:compatibility:state1:State".equals(((Path) i.next()).getBase())) {
                leadsToStated = true;
            }
        }

        return leadsToStated;
    }

    /**
     * Find the role of the object or the path.
     * <p>
     * The objects object_inRole is searched first, then the path of the object.
     * The path specified is only searched if object is null.
     * <p>
     * returns null if no role was found.
     *
     * @param object    object to search role from
     * @param path      alternative path to determine the role
     * @return String   name of the role
     */
    private String findRole(
        DataproviderObject_1_0 object,
        Path path
    ) {
        String roleName = null;

        if ((object != null)
                && (object.getValues(RoleAttributes.IN_ROLE) != null)
                && (object.getValues(RoleAttributes.IN_ROLE).size() > 0)
        ) {
            roleName = (String) object.getValues(RoleAttributes.IN_ROLE).get(0);

            return roleName;
        } else {
            Path search = null;

            if (object != null) {
                search = object.path();
            } else if (path != null) {
                search = path;
            } else {
                return null; // no path at all
            }

            // try searching the path
            for (int i = search.size() - 1; i >= 0; i--) {
                if ("role".equals(search.get(i))) {
                    if (search.size() > (i + 1)) {
                        roleName = search.get(i + 1);

                        return roleName;
                    } else {
                        // no role specified avoid searching rest of path
                        return null;
                    }
                }
            }
        }

        return roleName; // attention: more returns in method
    }

    /**
     * remove objects which are only present in the target provider.
     *
     * @param sourceReference  reference to remove objects from
     * @param sourcePaths      object ids received from the source
     */
    protected void removeSuperfluousObjects(
        Path sourceReference,
        Collection sourcePaths
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("removeSuperfluousObjects");

        ArrayList mappedSourcePaths = new ArrayList();
        boolean leadsToStatedClass = leadsToStatedClass(sourceReference);

        Map targetIdMap = new HashMap();

        List objects =
            getExistingObjects(
                mapRequestPath(sourceReference, LOCAL_PATH),
                leadsToStatedClass
            );

        for (Iterator iter = objects.iterator(); iter.hasNext();) {
            DataproviderObject_1_0 targetObj =
                (DataproviderObject_1_0) iter.next();

            // received states instead of objects, convert to objects and at the 
            // same time remove double entries
            if (leadsToStatedClass) {
                // any of the states will be ok
                targetIdMap.put(
                    //createCorePath(targetObj.path()),
                    toStatelessPath(new Path(targetObj.path())),
                    targetObj
                );

            } else {
                targetIdMap.put(
                    targetObj.path(),
                    targetObj
                );
            }
        }

        SysLog.trace("targetObjects: " + targetIdMap.keySet());
        SysLog.trace("sourcePaths:   " + sourcePaths);

        // convert sourcePaths to mapped target paths:
        if (sourcePaths != null) {
            for (Iterator convert = sourcePaths.iterator(); convert.hasNext();) {
                mappedSourcePaths.add(
                    mapRequestPath((Path) convert.next(), LOCAL_PATH)
                );
            }
        }

        for (Iterator tIter = targetIdMap.entrySet().iterator();
        tIter.hasNext();
        ) {
            Map.Entry entry = (Map.Entry) tIter.next();
            Path tPath = (Path) entry.getKey();

            if (!mappedSourcePaths.contains(tPath)) {
                deleteExistingObject(
                    (DataproviderObject_1_0) entry.getValue(),
                    null
                );
            }

            mappedSourcePaths.remove(tPath);
        }

        // all objects should be existing in the target by now 
        // only in case of noTransaction the data has already been written 
        if (this.writeRequestDispenser.getTransactionBehavior() == NO_TRANSACTION
                && mappedSourcePaths.size() > 0
        ) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Missing objects in target provider.", new Parameter[] {
                    new Parameter("missingObjects", mappedSourcePaths)
                }
            );
        }

        StopWatch_1.instance().stopTimer("removeSuperfluousObjects");

    }

    /**
     * remove the roles from the target provider which do not exist in the
     * source provider.
     * 
     * @param roleObject    object read from the target provider
     * @param objectIds     paths of the roles of this object
     */
    protected void removeSuperfluousRoles(
        Path roleReference,
        Collection objectIds
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("removeSuperfluousRoles");

        Path objectReference = createCorePath(roleReference);

        boolean leadsToStatedClass = leadsToStatedClass(objectReference.getParent());

        List objects = null; 
        if (leadsToStatedClass) {
            Path validPath = objectReference.getChild("validState");

            // validState will deliver all valid states, no use of a state filter
            objects =
                getExistingObjects(validPath, OMIT_STATE_FILTER);
        }
        else {
            try {
                // get the object
                DataproviderObject_1_0 roleObject =
                    getExistingObject(
                        mapRequestPath(
                            objectReference,
                            LOCAL_PATH
                        )
                    );

                objects = new ArrayList();
                objects.add(roleObject);
            } // TODO: is this the correct solution? or would a find for objId/role deliver better results?
            catch (ServiceException se) {
                if (se.getExceptionStack().getExceptionCode() == BasicException.Code.NOT_FOUND
                ) {
                    objects = new ArrayList();
                }
                else {
                    throw se;
                }

            }
        }

        Set requiredRoles = new HashSet();
        for (Iterator roleIter = objectIds.iterator(); roleIter.hasNext();) {
            requiredRoles.add(((Path) roleIter.next()).getBase());
        }

        // search for the roles existing in any one state
        Map existingRoles = new HashMap(); // collect a state for each role
        for (Iterator o = objects.iterator(); o.hasNext();) {
            DataproviderObject_1_0 roleObject = (DataproviderObject_1_0) o.next();
            if (roleObject.getValues(RoleAttributes.HAS_ROLE) != null) {
                for (Iterator r = roleObject.getValues(RoleAttributes.HAS_ROLE).iterator();
                r.hasNext();
                ) {
                    String role = (String)r.next();
                    existingRoles.put(role, roleObject);
                }
            }
        }

        // check for missing role
        // only in case of noTransaction the data has already been written 
        if (this.writeRequestDispenser.getTransactionBehavior() == NO_TRANSACTION
                && !existingRoles.keySet().containsAll(requiredRoles)
        ){
            // Error: all roles should be existing in the target by now 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Missing roles in target provider.", 
                new Parameter[] {
                    new Parameter("roleReference", roleReference),
                    new Parameter("existingRoles", existingRoles),
                    new Parameter("requiredRoles", requiredRoles)
                }
            );
        }

        // now remove the superfluous roles
        if (!requiredRoles.containsAll(existingRoles.keySet())) {
            existingRoles.keySet().removeAll(requiredRoles);

            for (Iterator r = existingRoles.entrySet().iterator(); 
            r.hasNext();
            ) {
                Map.Entry entry = (Map.Entry)r.next();

                deleteExistingObject(
                    (DataproviderObject_1_0)entry.getValue(), 
                    (String)entry.getKey()
                );                
            }
        }

        StopWatch_1.instance().stopTimer("removeSuperfluousRoles");
    }


    /**
     * Map the object received to a request object.
     * <p>
     * The object received can be a RequestedObject and thus must be changed
     * into a DataproviderObject. At the same time, the path of the object and
     * all contained paths are changed according to the pathMap.
     *
     * @param object
     * @return DataproviderObject  object prepared for storing
     */
    protected DataproviderObject mapRequestObject(
        DataproviderObject_1_0 object
    ) throws ServiceException {
        DataproviderObject result = new DataproviderObject(object);
        Path mapPath = null;

        mapPath = mapRequestPath(object.path(), LOCAL_PATH);

        result.path().setTo(mapPath);

        // change paths contained:
        for (Iterator a = result.attributeNames().iterator(); a.hasNext();) {
            String attribute = (String) a.next();

            for (int i = 0; i < result.values(attribute).size(); i++) {
                Object value = result.values(attribute).get(i);

                if (value instanceof Path) {
                    Path pathValue = (Path) value;
                    Path newPath = mapRequestPath(pathValue, REMOTE_PATH);

                    if (newPath != null) {
                        result.values(attribute).set(i, newPath);

                        checkExistenceOfReferencedObject(newPath, object);
                    }

                    // else stay with original path.
                    // no checking for existence of referenced object. 
                }
            }
        }

        return result;
    }


    /**
     * Map the path received to a request path. If pathIsLocal is set, the path
     * is expected to be local. This means that it must be mapped to another
     * path contained in the _pathMap. An exception is thrown if no matching
     * path can be found. If pathIsLocal is false and no matching path can be
     * found null is returned.
     *
     * @param receivedPath  path to map
     * @param pathIsLocal   if it is local, it must be mapped
     *
     * @return Path  mapped path or null
     * @throws ServiceException
     */
    protected Path mapRequestPath(
        Path receivedPath,
        boolean pathIsLocal
    ) throws ServiceException {
        Path newPath = null;

        // search for the mapping path:
        for (Iterator iter = this.pathMap.keySet().iterator();
        iter.hasNext() && (newPath == null);
        ) {
            Path mappingPath = (Path) iter.next();

            if (receivedPath.startsWith(mappingPath)) {
                newPath = new Path((Path) this.pathMap.get(mappingPath));

                newPath.addAll(receivedPath.getSuffix(mappingPath.size()));
            }
        }

        if ((newPath == null) && pathIsLocal) {
            // must find a mapping path
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "no mapping path for object path.",
                new Parameter[] {
                    new Parameter("original path", receivedPath),
                    new Parameter("path map", this.pathMap)
                }
            );
        }

        return newPath;
    }

    /**
     * Write a warning to the log if the object referenced by path is outside
     * of the replicated set and it does not exist.
     * <p>
     * If the object is within the replicated set, it may be delivered later
     * so that it is useless to check for it.
     * <p>
     * If the reference leads to a stated object, the object must be valid at
     * the time of the replication or within the valid period of the
     * referencing object.
     *
     *
     * @param mappedPath   reference to check
     * @param object       object holding the reference
     */
    protected void checkExistenceOfReferencedObject(
        Path mappedPath,
        DataproviderObject_1_0 object
    ) throws ServiceException {
        boolean insideSyncSet = false;

        for (Iterator sp = this.mappedStartPaths.iterator();
        sp.hasNext() && !insideSyncSet;
        ) {
            Path startPath = (Path) sp.next();

            if (mappedPath.startsWith(startPath)) {
                insideSyncSet = true;
            }
        }

        if (!insideSyncSet) {
            try {
                if (leadsToStatedClass(mappedPath)) {
                    getExistingState(
                        createCorePath(mappedPath),
                        findRole(null, mappedPath),
                        object.getValues(State_1_Attributes.VALID_FROM),
                        object.getValues(State_1_Attributes.VALID_TO)
                    );
                } else {
                    // not interested in result, just make sure that it exists
                    getExistingObject(mappedPath);
                }
            } catch (ServiceException se) {
                if (se.getExceptionStack().getExceptionCode() == BasicException.Code.NOT_FOUND
                ) {
                    SysLog.warning("Replication inserts dangling reference "
                        + mappedPath + " with object ", object
                    );
                }
            }
        }
    }

    /**
     * Delete the object or the role if one is specified.
     *
     * @param object the object to delete
     * @param role   role to delete (if null, the object is deleted)
     */
    protected void deleteExistingObject(
        DataproviderObject_1_0 object,
        String role
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("deleteExistingObject");
        // SysLog.trace("removing object: " + object.path() + " role: " + role);

        // can not delete RoleTypes
        if (!this.model.isSubtypeOf(
            object.getValues("object_class").get(0),
            "org:openmdx:compatibility:role1:RoleType"
        )
        ) {
            // TODO ev. cast object for the right isSubtypeOf Method...
            //            ServiceHeader header = _header;

            /* no longer needed, if requestedFor is null, the first state of the object is
             * returned
             */

            //            if (this._model.isInstanceof(object, "ch:omex:generic:State")) {
            //                // must set a valid date to determine the state which should 
            //                // be returned by the delete operation.
            //                String date = dateWithin(
            //                    object.getValues(State_1.VALID_FROM),
            //                    object.getValues(State_1.VALID_TO)
            //                );
            //                // requestedFor must be within validFrom, validTo
            //                header =
            //                    new ServiceHeader(
            //                        (String[]) _header.getPrincipalChain().toArray(
            //                            new String[0]),
            //                        _header.getSessionId(),
            //                        false,
            //                        _header.getQualityOfService(),
            //                        null,
            //                        date);
            //            }
            RequestCollection requests =
                this.writeRequestDispenser.getRequestCollection();

            // only delete the object if it has not already been deleted.
            // Most important in a long transaction and for deletion of roles.
            if (this.deletedObjects.add(createCorePath(object.path()))) {
                if (role == null) {
                    requests.addRemoveRequest(toStatelessPath(new Path(object.path())));
                }
                else {
                    requests.addRemoveRequest(
                        object.path().getChild(RoleAttributes.REF_ROLE).add(role)
                    );
                }
            }
        }
        StopWatch_1.instance().stopTimer("deleteExistingObject");
    }

    /**
     * Get the object at that path. Path is expected to lead to an object.
     *
     * @param startPath
     * @return List
     */
    protected DataproviderObject_1_0 getExistingObject(
        Path startPath
    ) throws ServiceException {
        DataproviderObject_1_0 object = null;

        RequestCollection requests =
            this.readRequestDispenser.getRequestCollection();

        // new RequestCollection(this._header, this._provider);
        object = requests.addGetRequest(startPath);

        // not found exception is an error. The object should already exist.
        return object;
    }


    /**
     * Get existing state within the timespan [validFrom, validTo).
     * <p>
     * if the timespan includes the current time, the state at the current time
     * is searched for. If role is specified, the state is search in that role.
     *
     * @param corePath    path without role or state elements
     * @param role        if set, the role state is searched
     * @param validFrom   start of the valid period of the state searched for
     * @param validTo     end of the valid period of the state searched for
     * @return DataproviderObject_1_0  state found
     */
    protected DataproviderObject_1_0 getExistingState(
        Path corePath,
        String role,
        SparseList validFrom,
        SparseList validTo
    ) throws ServiceException {
        DataproviderObject_1_0 result = null;

        // NOTE: find would be possible now with identity filter!
        // must do a get to find the state of exactly that objects role.
        // Find would return the states matching the criteria of all objects.

        RequestCollection requests = 
            this.readRequestDispenser.getTimedRequestCollection(
                (String) (validFrom == null ? 
                    null : 
                        (validFrom.size() == 0 ? 
                            null : 
                                validFrom.get(0)
                        )
                ),
                (String) (validTo == null ? 
                    null : 
                        (validTo.size() == 0 ? 
                            null : 
                                validTo.get(0)
                        )
                )
            );


        if (role != null) {
            result = requests.addGetRequest(corePath.add("role").add(role));
        } else {
            result = requests.addGetRequest(corePath);
        }

        return result;
    }



    /**
     * Get the objects at that path. Path is excpeted to end in a reference.
     * <p>
     * Adding the state filter allows searching for objects which have no
     * currently active state and thus would not be found by a normal search.
     * Of course, this is only possible with stated objects.
     *
     *
     * @param startPath
     * @param addStateFilter  if the filter for states shuold be added.
     *
     * @return List
     */
    protected List getExistingObjects(
        Path startPath,
        boolean addStateFilter
    ) throws ServiceException {
        StopWatch_1.instance().startTimer("getExistingObjects");
        List objects = null;
        RequestCollection requests =
            this.readRequestDispenser.getRequestCollection();

        // new RequestCollection(this._header, this._provider);
        try {
            // get valid object states
            // doing a find on the objects would not help, this returns just
            // the ones valid at requestedAt, requestedFor
            FilterProperty[] filters = null;

            if (addStateFilter) {
                filters = new FilterProperty[2];

                // this is just a dummy request to have a filter containing 
                // an object_validTo request, otherwise the operation will only be 
                // executed on the current date (header.requestedFor, 
                // header.requestedAt)
                FilterProperty dummyFilter =
                    new FilterProperty(Quantors.FOR_ALL,
                        State_1_Attributes.VALID_TO, FilterOperators.IS_NOT_IN
                    );

                // and of course we are only interested in valid states
                FilterProperty validFilter =
                    new FilterProperty(Quantors.FOR_ALL,
                        State_1_Attributes.INVALIDATED_AT,
                        FilterOperators.IS_IN
                    );

                filters[0] = dummyFilter;
                filters[1] = validFilter;
            }

            objects =
                requests.addFindRequest(
                    startPath, 
                    filters, //new FilterProperty[] { dummyFilter, validFilter },
                    AttributeSelectors.ALL_ATTRIBUTES, 
                    0, 
                    Integer.MAX_VALUE,
                    Directions.ASCENDING
                );
        }
        // don't care about NOT_FOUND exceptions
        catch (ServiceException e) {
            if (e.getExceptionStack().getExceptionCode() != BasicException.Code.NOT_FOUND
            ) {
                throw e;
            } else {
                objects = new ArrayList();
            }
        }
        StopWatch_1.instance().stopTimer("getExistingObjects");
        return objects;
    }

    /**
     * Apply the sourceStates to the target provider.
     * <p>
     * The paths of the sourceStates must already have been mapped.
     * <p>
     * If the states of the existing target object do not match the states of
     * the source object, the target object is deleted entirely and replaced by
     * the new source object. This can be done because the contained object are
     * delivered from source.
     *
     * @param sourceStates    states of one object from the source provider.
     * @param rolesPerState   map containing the roles for each state.
     * @throws ServiceException
     */
    protected void applyStates(
        List sourceStates,
        Map rolesPerState
    ) throws ServiceException {
        if (sourceStates.size() > 0) {
            // first get existing states
            Path searchPath =
                new Path(((DataproviderObject) sourceStates.get(0)).path());

            // must extract id part of searchPath (without roles and states)
            toStatelessPath(searchPath);
            searchPath.add("validState");

            // validState will deliver all valid states, no use of a state filter
            List targetStates =
                getExistingObjects(searchPath, OMIT_STATE_FILTER);

            // compare the states
            // Maintain the states only if the target states have the same 
            // intervals as the source states. Otherwise delete the object and
            // create all states new. If they have the same intervals update 
            // the content.  
            boolean equalIntervals = true;

            // The same with roles: if the intervals of the roles have changed,
            // the entire role has to be deleted and recreated later on. Of 
            // course if the states are deleted, the roles must be recreated 
            // anyway. 
            // rolesToDelete collects all the roles which have differing 
            // intervals or are not present at all in source. It must collect 
            // one target state which has that role, to be able to delete it 
            // later on.
            Map rolesToDelete = new HashMap();

            for (Iterator t = targetStates.iterator(), s =
                sourceStates.iterator();
            t.hasNext() && s.hasNext() && equalIntervals;
            ) {
                DataproviderObject_1_0 target =
                    (DataproviderObject_1_0) t.next();
                DataproviderObject_1_0 source =
                    (DataproviderObject_1_0) s.next();

                equalIntervals =
                    (((target.getValues(State_1_Attributes.VALID_FROM) == null)
                            && (source.getValues(State_1_Attributes.VALID_FROM) == null))
                            || ((target.getValues(State_1_Attributes.VALID_FROM) != null)
                                    && (source.getValues(State_1_Attributes.VALID_FROM) != null)
                                    && target.getValues(State_1_Attributes.VALID_FROM).equals(
                                        source.getValues(State_1_Attributes.VALID_FROM)
                                    )))
                                    && (((target.getValues(State_1_Attributes.VALID_TO) == null)
                                            && (source.getValues(State_1_Attributes.VALID_TO) == null))
                                            || ((target.getValues(State_1_Attributes.VALID_TO) != null)
                                                    && (source.getValues(State_1_Attributes.VALID_TO) != null)
                                                    && target.getValues(State_1_Attributes.VALID_TO).equals(
                                                        source.getValues(State_1_Attributes.VALID_TO)
                                                    )));

                // check the roles:
                if (equalIntervals
                        && (target.getValues(RoleAttributes.HAS_ROLE) != null)
                ) {
                    // System.out.println("@@@@ checking equal role intercvals for " + target.path());
                    /*
                    for (Iterator r =
                            target.getValues(RoleAttributes.HAS_ROLE).iterator();
                        r.hasNext();
                    ) {
                        String targetRoleName = (String) r.next();
                        List sourceRoles =
                            (List) rolesPerState.get(
                                target.getValues(State_1_Attributes.VALID_FROM)
                                      .get(0)
                            );
                        boolean commonRole = false;

                        for (Iterator sr = sourceRoles.iterator();
                            sr.hasNext() && !commonRole
                            && !rolesToDelete.keySet().contains(targetRoleName);
                        ) {
                            if (targetRoleName.equals(
                                    findRole((DataproviderObject) sr.next(),
                                        null
                                    )
                                )
                            ) {
                                commonRole = true;
                            }
                        }

                        if (!commonRole) {
                            rolesToDelete.put(targetRoleName, target);
                        }
                    }
                     */
                }
            }

            if (!equalIntervals) {
                // delete object entirely and recreate it with new states
                // cut the state component from path:

                DataproviderObject deleter =
                    new DataproviderObject((DataproviderObject_1_0) targetStates
                        .get(0)
                    );

                toStatelessPath(deleter.path());

                deleteExistingObject(deleter, null);

                setObjectStates(sourceStates);

                rolesToDelete.clear();
            } else if (targetStates.size() == 0) {
                equalIntervals = false;

                setObjectStates(sourceStates);
                rolesToDelete.clear();
            } else {
                // check for differing states to update:
                for (Iterator t = targetStates.iterator(), 
                        s = sourceStates.iterator(); 
                t.hasNext() && s.hasNext();
                ) {
                    DataproviderObject_1_0 target =
                        (DataproviderObject_1_0) t.next();
                    DataproviderObject_1_0 source =
                        (DataproviderObject_1_0) s.next();

                    boolean equal =
                        target.attributeNames().size() == 
                            source.attributeNames().size();

                    for (Iterator a = source.attributeNames().iterator();
                    a.hasNext() && equal;
                    ) {
                        String attribute = (String) a.next();

                        if (!attribute.equals(
                            SystemAttributes.CREATED_AT
                        )
                        && !attribute.equals(
                            SystemAttributes.CREATED_BY
                        )
                        && !attribute.equals(
                            SystemAttributes.MODIFIED_AT
                        )
                        && !attribute.equals(
                            SystemAttributes.MODIFIED_BY
                        )
                        && !attribute.equals(RoleAttributes.HAS_ROLE)
                        && !attribute.equals(RoleAttributes.IN_ROLE)
                        ) {
                            equal =
                                (source.getValues(attribute) != null)
                                && target.getValues(attribute).equals(
                                    source.getValues(attribute)
                                );
                        }
                    }

                    if (!equal) {
                        replaceObjectState(source);
                    }
                }

                // core states are updated.
            }

            // Check for the roles:
            // first delete all the roles in roleToDelete:
            // after that no more roles must be deleted from the states. Still
            // roles must be created or upated.
            for (Iterator delIter = rolesToDelete.entrySet().iterator();
            delIter.hasNext();
            ) {
                Map.Entry entry = (Map.Entry) delIter.next();

                deleteExistingObject((DataproviderObject_1_0) entry.getValue(),
                    (String) entry.getKey()
                );
            }

            // compare the roles for the states. Only in case of equalIntervals
            // the roles have been left untouched and require further treatement.
            if (equalIntervals) {
                // targetStates are still valid, except for the roles deleted.
                // The remaining roles 
                for (Iterator t = targetStates.iterator(); t.hasNext();) {
                    DataproviderObject_1_0 target =
                        (DataproviderObject_1_0) t.next();

                    String validFrom =
                        (target.getValues(State_1_Attributes.VALID_FROM) == null)
                        ? null
                            : (String) target.getValues(State_1_Attributes.VALID_FROM)
                            .get(0);

                    List stateRoles = (List) rolesPerState.get(validFrom);

                    if (stateRoles != null) {
                        // the rest of roles must be compared, updated or created:
                        updateRolesOfState(stateRoles, target, rolesToDelete);
                    }
                }
            }
            // else the states have been newly created. 
            // all roles must be set
            else {
                for (Iterator s = sourceStates.iterator(); s.hasNext();) {
                    DataproviderObject_1_0 state =
                        (DataproviderObject_1_0) s.next();

                    String validFrom =
                        (state.getValues(State_1_Attributes.VALID_FROM) == null)
                        ? null
                            : (String) state.getValues(State_1_Attributes.VALID_FROM)
                            .get(0);

                    List stateRoles = (List) rolesPerState.get(validFrom);

                    if (stateRoles != null) {
                        for (Iterator stateRolesIterator =
                            stateRoles.iterator();
                        stateRolesIterator.hasNext();
                        ) {
                            createRoleOfState((DataproviderObject_1_0) stateRolesIterator
                                .next()
                            );
                        }
                    }
                }
            }
        }
    }

    /**
     * Update the roles of the state to the roles in sourceRoles.
     * <p>
     * The roles existing in target are updated according to the new ones, but
     * only if they differ. Roles contained in rolesDeleted have been deleted
     * and must be recreated in the target provider.
     *
     * @param sourceRoles   The roles as they exist in source
     * @param state        the state from target, without roles
     * @param rolesDeleted the roles which have been deleted in the target
     */
    protected void updateRolesOfState(
        List sourceRoles,
        DataproviderObject_1_0 state,
        Map rolesDeleted
    ) throws ServiceException {
        for (Iterator sourceRolesIter = sourceRoles.iterator();
        sourceRolesIter.hasNext();
        ) {
            DataproviderObject_1_0 sourceRole =
                (DataproviderObject_1_0) sourceRolesIter.next();

            String roleName = findRole(sourceRole, null);

            if (rolesDeleted.keySet().contains(roleName)) {
                createRoleOfState(sourceRole);
            } else {
                // get the existing target role
                DataproviderObject_1_0 targetRole =
                    getExistingState(
                        createCorePath(state.path()),
                        roleName,
                        state.getValues(State_1_Attributes.VALID_FROM),
                        state.getValues(State_1_Attributes.VALID_TO)
                    );

                boolean updateRequired = false;

                for (Iterator attrs = targetRole.attributeNames().iterator();
                attrs.hasNext();
                ) {
                    String attribute = (String) attrs.next();

                    if (!state.attributeNames().contains(attribute)) {
                        // attribute of core, not interested in
                        if (sourceRole.containsAttributeName(attribute)) {
                            if (!updateRequired // no need to check for equal otherwise

                                    && !sourceRole.getValues(attribute).equals(
                                        targetRole.getValues(attribute)
                                    )
                            ) {
                                updateRequired = true;
                                SysLog.trace("updateRequired, differing: "
                                    + attribute + " target: " + targetRole
                                    + " source: " + sourceRole + " state: "
                                    + state
                                );
                            }
                        } else {
                            // must delete existing attribute:
                            sourceRole.values(attribute);
                            updateRequired = true;
                            SysLog.trace("updateRequired, deleting: "
                                + attribute + " target: " + targetRole
                                + " source: " + sourceRole + " state: " + state
                            );
                        }
                    }
                }

                // if sourceRole has more attributes than targetRole
                if (!targetRole.attributeNames().containsAll(
                    sourceRole.attributeNames()
                )
                ) {
                    updateRequired = true;
                    SysLog.trace("updateRequired, missing attributes: "
                        + " target: " + targetRole + " source: " + sourceRole
                        + " state: " + state
                    );
                }

                if (updateRequired) {
                    updateRoleOfState(sourceRole);
                }
            }
        }
    }

    /**
     * Dispense the RequestCollections for accessing the dataprovider according
     * to the current transaction behavior.
     * <p>
     *
     * The ReplicateHandler must feed the callbacks startObject(), endObject(),
     * startTopLevelObject(), endTopLevelObject(). The RequestDispenser needs
     * this information to be able to follow the currrent state of work.
     * <p>
     * A toplevel object is an object specified in the startPath list.
     *
     */
    static protected class RequestDispenser {
        /** the currently active transaction behavior */
        private short _transactionBehavior;

        /** the provider */
        private Dataprovider_1_0 _provider = null;

        /** the header to use */
        private ServiceHeader _header = null;

        /** the request collection for longer units of work */
        private RequestCollection _request = null;

        /**
         * Prepare the RequestDispenser.
         *
         * @param provider  the dataprovider to work with
         */
        public RequestDispenser(
            ServiceHeader header,
            Dataprovider_1_0 provider
        ) {
            _transactionBehavior = NO_TRANSACTION; // default
            _provider = provider;
            _header = header;

            _request = new RequestCollection(header, provider);
        }

        /**
         * Set the TransactionBehavior.
         * <p>
         * If the transaction behavior is set during a running transaction, it
         * the new behavior only gets assigned after the current transaction has
         * been ended. Note: changing the transaction behavior when the
         * current behavior is ONE_TRANSACTION is useless.
         *
         * @param transactionBehavior
         */
        public void setTransactionBehavior(
            short transactionBehavior
        ) {
            _transactionBehavior = transactionBehavior;
            if (_transactionBehavior == NO_TRANSACTION) {
                // always needed.
                _request = new RequestCollection(_header, _provider); 
            }
            else {
                _request = null;  // set request to null to detect missing transaction
            }
        }

        /**
         * Get the currently active transaction behavior.
         * <p>
         * If a new behavior has already been set, this can not be detected.
         *
         * @return short
         */
        public short getTransactionBehavior() {
            return _transactionBehavior;
        }

        /**
         * Get a RequestCollection. 
         * <p>
         * This is the central part of the RequestDispenser as the 
         * RequestCollection's transaction is started (must be!) and will be 
         * commited according to the setting of transaction behavior.
         *
         */
        public RequestCollection getRequestCollection(
        ) throws ServiceException {
            if (_request == null) {
                throw new ServiceException (
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    "No transaction open for RequestCollection"
                );
            }

            return _request;
        }

        /**
         * Get a RequestCollection which has requestedAt date set which is 
         * between validFrom and validTo. 
         * <p>
         * This request collection is intended for reading access, thus it is 
         * not related to any transaction treatment.
         * 
         * @param validFrom
         * @param validTo
         */
        public RequestCollection getTimedRequestCollection(
            String validFrom,
            String validTo
        ) throws ServiceException {
            if (_transactionBehavior != NO_TRANSACTION) {
                throw new ServiceException (
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    "Timed collections require a transaction behavior of noTransaction",
                    new Parameter [] {
                        new Parameter("transaction behavior", _transactionBehavior),
                        new Parameter("noTransaction would be", NO_TRANSACTION)
                    }
                );
            }

            String searchDate = dateWithin(validFrom, validTo);

            ServiceHeader header = 
                new ServiceHeader(_header.getPrincipalChain().toArray(
                    new String[0]
                ),
                _header.getCorrelationId(),
                false,
                _header.getQualityOfService(),
                null,
                searchDate
                );

            return new RequestCollection(header, _provider);
        }      

        /**
         * Returns true if currently a transaction is open.
         */
        public boolean hasOpenTransaction() {
            return _request != null;
        }


        /**
         * Start the transaction if transactional behaviour is set accordingly.
         */
        public void startTraversal() throws ServiceException {
            if (_transactionBehavior == ONE_TRANSACTION) {
                startTransaction();
            }
        }

        /**
         * End the transaction if transactional behaviour is set accordingly.
         */
        public void endTraversal() throws ServiceException {
            if (_transactionBehavior == ONE_TRANSACTION) {
                commitTransaction();
            }
        }

        /**
         * Rollback the current transaction. 
         */
        public void abandonTraversal() throws ServiceException {
            rollbackTransaction();
        }

        /**
         * An object starts, based on the transaction setting a new transaction 
         * is started.
         */
        public void startObject(
            boolean isToplevelObject
        ) throws ServiceException {
            if (isToplevelObject &&
                    _transactionBehavior == TOP_LEVEL_TRANSACTION
            ) {
                startTransaction();
            }
            else if (
                    _transactionBehavior == OBJECT_TRANSACTION
            ) {
                startTransaction();
            }
        }

        /**
         * An object has been treated, based in the transaction setting a new 
         * transaction is ended.
         */
        public void endObject(
            boolean isToplevelObject
        ) throws ServiceException {
            if (isToplevelObject &&
                    _transactionBehavior == TOP_LEVEL_TRANSACTION
            ) {
                commitTransaction();
            }
            else if (
                    _transactionBehavior == OBJECT_TRANSACTION
            ) {
                commitTransaction();
            }
        }

        private void startTransaction() throws ServiceException {
            SysLog.trace("startTransaction");

            if (_request != null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    "Open transaction when starting new one."
                );                
            }

            _request.beginUnitOfWork(false); // TODO must be configurable
        }

        private void commitTransaction() throws ServiceException {
            SysLog.trace("commitTransaction");

            if (_request == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    "No open transaction to commit."
                );                
            }            
            _request.endUnitOfWork();
        }

        /**
         * Note: no real transaction behavior without appserver
         * @throws ServiceException
         */
        private void rollbackTransaction() throws ServiceException {
            SysLog.trace("rollbackTransaction");

            if (_request != null) {
                _request.clear();
                _request = null;
            }
        }

        /**
         * Find a date between validFrom and validTo.
         * <p>
         * If the current time is
         * between validFrom, validTo, the current time is returned.
         *
         * @param validFrom null or the start date
         * @param validTo   null or the end date
         * @return String
         * @throws ServiceException
         */
        private String dateWithin(
            String validFrom,
            String validTo
        ) throws ServiceException {
            String searchDate = null;
            String now = DateFormat.getInstance().format(new Date());

            if (((validFrom == null)
                    || (validFrom.compareTo(now) < 0))
                    && ((validTo == null)
                            || (validTo.compareTo(now) > 0))
            ) {
                // current date is valid
                searchDate = now;
            } else {
                // must search on
                // find a date to set as requestedFor
                if (validFrom != null) {
                    searchDate = validFrom;
                } else if (validTo != null) {
                    try {
                        // set a date before validTo; because validFrom is null, 
                        // any date before validTo would do
                        Date validToDate =
                            DateFormat.getInstance().parse(validTo);
                        long validToTime = validToDate.getTime();
                        searchDate =
                            DateFormat.getInstance().format(
                                new Date(validToTime - 1)
                            );
                    } catch (ParseException e) {
                        throw new ServiceException(BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            "validTo is not a date.",
                            new Parameter[] { new Parameter("validTo", validTo) }
                        );
                    }
                }
            }

            return searchDate;
        }
    }

    private static int SEGMENT_PATH_LENGTH = 5;

    /** the datamodel used for the replication data */
    private Model_1_0 model = null;

    /** the start paths in their mapped form */
    private List mappedStartPaths = new ArrayList();

    /** map source paths to target paths */
    private Map pathMap = null;

    /** RequestDispenser for maintaining the transaction behavior. */
    private RequestDispenser writeRequestDispenser;
    private RequestDispenser readRequestDispenser;

    /**
     * Collect the states of the current object in this List. Requires that all
     * states of an object are consecutive, within a single state reference.
     * <p>
     * Within a state reference only states and roles of the current object can
     * occur. No further references leading to other objects are possible.
     * Therefore collecting states in a list is not critical regarding memory.
     */
    private List objectStates = null;

    /** 
     * qualifier of the object to which the states contained in _objectStates 
     * belong.
     */
    private String objectStatesQualifiedName = null;
    private String objectStatesId = null;

    /** Collect the roles within a state */
    //private List _stateRoles = null;

    /** preserve the roles per state. The states validFrom serves as key. */
    //private HashMap _rolesPerState = null;

    /** statedPath is the first path in a hierarchie containing a stated object.*/
    private String _startingStatedQualifier = null;
    private static boolean LOCAL_PATH = true;
    private static boolean REMOTE_PATH = false;
    private static boolean OMIT_STATE_FILTER = false;

    /** 
     *  Value to indicate that the last operation was consumed (helps keeping
     *  track of treating the protocol correctly)
     */
    private static short OPERATION_UNSET = -1; 
    // make sure not to use one of the operations defined in TraversalHandler

    /** the last operation received by startObject */
    private short lastOperation = OPERATION_UNSET;

    /**
     * Collect the objects deleted to avoid multiple deletes and to avoid 
     * deletes for roles on an object which has already been deleted.
     */
    private Set deletedObjects; 

}

/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: RefContainer_1 class
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.base.accessor.jmi.spi;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.oasisopen.jmi1.RefQualifier;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.collection.MarshallingConsumer;
import org.openmdx.base.collection.MarshallingSequentialList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSpliterator;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.PersistenceCapableCollection;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Filter;
import org.openmdx.base.rest.cci.FeatureOrderRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.AnyTypePredicate;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * RefContainer_1
 */
public class RefContainer_1
    extends AbstractCollection<RefObject_1_0>
    implements Serializable, RefContainer<RefObject_1_0>, PersistenceCapableCollection
{

    /**
     * Constructor
     *
     * @param marshaller  the marshaller to be applied to the elements, filter and order
     *           objects.
     * @param container  The delegate contains unmarshalled elements
     */
    public RefContainer_1(
        Marshaller marshaller,
        Container_1_0 container
    ) {
        this.marshaller = marshaller;
        this.container = container;
    }

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = 1211709415207799992L;

    private Marshaller marshaller;

    private Container_1_0 container;

    public Container_1_0 refDelegate(
    ) {
        return this.container;
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refImmediatePackage()
     */
    @Override
    public RefPackage refImmediatePackage() {
        throw new UnsupportedOperationException(
            "refImmediatePackage() is not supported, while refOutermostPackage() is"
        );
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refMetaObject()
     */
    @Override
    public RefObject refMetaObject(
    ) {
        throw new UnsupportedOperationException("Operation not supported by RefContainer_1");
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refMofId()
     */
    @Override
    public String refMofId(
    ) {
        Object containerId = this.jdoGetObjectId();
        return containerId instanceof Path ? ((Path)containerId).toXRI() : null;
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refVerifyConstraints(boolean)
     */
    @Override
    public Collection<?> refVerifyConstraints(
        boolean deepVerify
    ) {
        throw new UnsupportedOperationException("Operation not supported by RefContainer_1");
    }

    @Override
    public RefPackage_1_0 refOutermostPackage(
    ) {
        return (RefPackage_1_0) this.marshaller;
    }

    private void add(
        String qualifier,
        RefObject_1_0 value
    ) {
        try {
            ObjectView_1_0 objectView = (ObjectView_1_0) this.marshaller.unmarshal(value);
            objectView.objMove(
                this.container,
                qualifier
            );
        } catch(ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#iterator()
     */
    @Override
    public Iterator<RefObject_1_0> iterator() {
        return new MarshallingSet<RefObject_1_0>(
                this.marshaller,
                this.container.values()
        ).iterator();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        return this.container.size();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#clear()
     */
    @Override
    public void clear() {
        this.container.clear();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return this.container.isEmpty();
    }

    /**
     * Retrieve a member
     *
     * @param filter
     *
     * @return the requested member
     */
    private RefObject_1_0 get(String filter) {
        try {
            DataObject_1_0 viewObject = this.container.get(filter);
            return viewObject == null || viewObject.objDoesNotExist() ? null : (RefObject_1_0) this.marshaller.marshal(
            	 viewObject
            );
        } catch (ServiceException exception){
            if(exception.getExceptionCode() == BasicException.Code.NOT_FOUND) {
                return null;
            } else {
                throw new JmiServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.TRANSFORMATION_FAILURE,
                    "Marshal failure"
                );
            }
        }
    }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    @Override
    public boolean add(RefObject_1_0 o) {
        this.add(null, o);
        return true;
    }


    //------------------------------------------------------------------------
    // Implements Container
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
     */
    @Override
    public PersistenceManager openmdxjdoGetDataObjectManager() {
        return this.container.openmdxjdoGetDataObjectManager();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoEvict()
     */
    @Override
    public void openmdxjdoEvict(
        boolean allMembers, boolean allSubSets
    ) {
        this.container.openmdxjdoEvict(allMembers, allSubSets);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoRefreshAll()
     */
    @Override
    public void openmdxjdoRefresh() {
        this.container.openmdxjdoRefresh();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoRetrieve(javax.jdo.FetchPlan)
     */
    @Override
    public void openmdxjdoRetrieve(FetchPlan fetchPlan) {
        this.container.openmdxjdoRetrieve(fetchPlan);
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.Container#getAll(org.w3c.cci2.AnyTypePredicate)
     */
    @Override
    public List<RefObject_1_0> getAll(AnyTypePredicate predicate) {
        return this.refGetAll(predicate);
    }

    /* (non-Javadoc)
     * @see Container#processAll(AnyTypePredicate, Consumer)
     */
    @Override
    public void processAll(
        AnyTypePredicate predicate,
        Consumer<RefObject_1_0> action
    ) {
        final QueryComponents queryComponents = toQueryComponents(predicate);
        queryComponents.getSource().processAll(
            queryComponents.getFetchPlan(),
            queryComponents.getOrder(),
            toConsumer(action)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.Container#removeAll(org.w3c.cci2.AnyTypePredicate)
     */
    @Override
    public void removeAll(AnyTypePredicate predicate) {
        this.refRemoveAll(predicate);
    }


    /* (non-Javadoc)
     * @see java.util.Collection#spliterator()
     */
    @Override
    public Spliterator<RefObject_1_0> spliterator() {
        return new MarshallingSpliterator<>(
            RefObject_1_0.class,
            this.container.values().spliterator(),
            this.marshaller
        );
    }


    //------------------------------------------------------------------------
    // Implements Iterable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Iterable#forEach(java.util.function.Consumer)
     */
    @Override
    public void forEach(Consumer<? super RefObject_1_0> action) {
        this.container.values().forEach(toConsumer(action));
    }

    private MarshallingConsumer<DataObject_1_0, RefObject_1_0> toConsumer(Consumer<? super RefObject_1_0> consumer) {
        return new MarshallingConsumer<>(RefObject_1_0.class, consumer, this.marshaller);
    }


    //------------------------------------------------------------------------
    // Implements Collection
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.util.Collection#removeIf(java.util.function.Predicate)
     */
    @Override
    public boolean removeIf(Predicate<? super RefObject_1_0> filter) {
        boolean changed = false;
        for(Iterator<RefObject_1_0> i = iterator(); i.hasNext(); ) {
            if(filter.test(i.next())) {
                i.remove();
                changed = true;
            }
        }
        return changed;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#parallelStream()
     */
    @Override
    public Stream<RefObject_1_0> parallelStream() {
        return stream(); // No parallel support yet
    }


    //------------------------------------------------------------------------
    // Implements PersistenceCapable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoIsPersistent()
     */
    @Override
    public boolean jdoIsPersistent() {
        return this.container.jdoIsPersistent();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Container#openmdxjdoGetContainerId()
     */
    @Override
    public Object jdoGetObjectId() {
        return this.container.jdoGetObjectId();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Container#openmdxjdoGetTransientContainerId()
     */
    @Override
    public Object jdoGetTransactionalObjectId() {
        return this.container.jdoGetTransactionalObjectId();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
     */
    @Override
    public PersistenceManager jdoGetPersistenceManager(){
        return this.refOutermostPackage().refPersistenceManager();
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceStateManager(javax.jdo.spi.StateManager)
     */
    @Override
    public void jdoReplaceStateManager(
        StateManager sm
    ) throws SecurityException {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideField(int)
     */
    @Override
    public void jdoProvideField(int fieldNumber) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideFields(int[])
     */
    @Override
    public void jdoProvideFields(int[] fieldNumbers) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceField(int)
     */
    @Override
    public void jdoReplaceField(int fieldNumber) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFields(int[])
     */
    @Override
    public void jdoReplaceFields(int[] fieldNumbers) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFlags()
     */
    @Override
    public void jdoReplaceFlags() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyFields(java.lang.Object, int[])
     */
    @Override
    public void jdoCopyFields(Object other, int[] fieldNumbers) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoMakeDirty(java.lang.String)
     */
    @Override
    public void jdoMakeDirty(String fieldName) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
     */
    @Override
    public Object jdoGetVersion() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDirty()
     */
    @Override
    public boolean jdoIsDirty() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsTransactional()
     */
    @Override
    public boolean jdoIsTransactional() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsNew()
     */
    @Override
    public boolean jdoIsNew() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDeleted()
     */
    @Override
    public boolean jdoIsDeleted() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDetached()
     */
    @Override
    public boolean jdoIsDetached() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager)
     */
    @Override
    public PersistenceCapable jdoNewInstance(StateManager sm) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager, java.lang.Object)
     */
    @Override
    public PersistenceCapable jdoNewInstance(StateManager sm, Object oid) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance()
     */
    @Override
    public Object jdoNewObjectIdInstance() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance(java.lang.Object)
     */
    @Override
    public Object jdoNewObjectIdInstance(Object o) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(java.lang.Object)
     */
    @Override
    public void jdoCopyKeyFieldsToObjectId(Object oid) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldSupplier, java.lang.Object)
     */
    @Override
    public void jdoCopyKeyFieldsToObjectId(ObjectIdFieldSupplier fm, Object oid) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsFromObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldConsumer, java.lang.Object)
     */
    @Override
    public void jdoCopyKeyFieldsFromObjectId(
        ObjectIdFieldConsumer fm,
        Object oid
    ) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }


    //------------------------------------------------------------------------
    // Implements RefContainer
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refAdd(org.oasisopen.cci2.QualifierType, java.lang.Object, RefObject_1_0)
     */
    @Override
    public void refAdd(QualifierType qualifierType, Object value, RefObject_1_0 refObject) {
        this.refAdd(
                Collections.singletonList(
                        new RefQualifier(qualifierType, value)
                ),
                refObject
        );
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refAdd(java.util.List<RefQualifier>, org.openmdx.base.accessor.jmi.cci.RefObject_1_0)
     */
    @Override
    public void refAdd(List<RefQualifier> qualifierList, RefObject_1_0 refObject) {
        final RefQualifier qualifier = qualifierList.get(0);
        // TODO: KJDD temp
        final Object[] arguments = {qualifier.qualifierType, qualifier.qualifierValue};
        int objectIndex = arguments.length - 1;
        add(
                RefContainer_1.toQualifier(objectIndex, arguments),
                (RefObject_1_0) arguments[objectIndex]
        );
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refGet(org.oasisopen.cci2.QualifierType, java.lang.Object)
     */
    @Override
    public RefObject_1_0 refGet(QualifierType qualifierType, Object value) {
        return this.refGet(
                Collections.singletonList(
                        new RefQualifier(qualifierType, value)
                )
        );
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refGetAll(java.util.List<RefQualifier>)
     */
    @Override
    public RefObject_1_0 refGet(List<RefQualifier> qualifierList) {
        final RefQualifier qualifier = qualifierList.get(0);
        return get(
                RefContainer_1.toQualifier(
                        2,
                        new Object[] {
                                qualifier.qualifierType,
                                qualifier.qualifierValue
                        }
                )
        );
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refGetAll(java.lang.Object)
     */
    @Override
    public List<RefObject_1_0> refGetAll(Object rawQuery) {
        final QueryComponents queryComponents = toQueryComponents(rawQuery);
        return new MarshallingSequentialList<>(
            this.marshaller,
            queryComponents.getSource().values(queryComponents.getFetchPlan(), queryComponents.getOrder())
        );
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refRemove(org.oasisopen.cci2.QualifierType, java.lang.Object)
     */
    @Override
    public void refRemove(QualifierType qualifierType, Object value) {
        this.refRemove(Collections.singletonList(new RefQualifier(qualifierType, value)));
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refGetAll(java.util.List<RefQualifier>)
     */
    @Override
    public void refRemove(List<RefQualifier> qualifierList) {
//        RefObject_1_0 object = this.get(RefContainer_1.toQualifier(arguments.length, arguments));
        final RefQualifier qualifier = qualifierList.get(0);
        RefObject_1_0 object = this.get(RefContainer_1.toQualifier(
                2,
                new Object[] {
                        qualifier.qualifierType,
                        qualifier.qualifierValue
                }
            )
        );
        if(object != null) {
            if(JDOHelper.isPersistent(object)) {
                object.refDelete();
            } else {
                this.remove(object);
            }
        }


    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refRemoveAll(java.lang.Object)
     */
    @Override
    public long refRemoveAll(Object query) {
        long removed = 0;
        for(RefObject_1_0 refObject : this.refGetAll(query)) {
            refObject.refDelete();
            removed++;
        }
        return removed;
    }

    /**
     * Create a qualifier from its sub-segment specification array
     *
     * @param size
     * @param arguments
     *
     * @return the corresponding qualifier
     */
    static String toQualifier(
        int size,
        Object[] arguments
    ){
        switch(size) {
            case 0:
                return null;
            case 1:
                return String.valueOf(arguments[0]);
            default: {
                if((size & 1) == 1) throw new IllegalArgumentException(
                    "The ref-method was invoked with an odd number of arguments greater than one: " + arguments.length
                );
                StringBuilder qualifier = new StringBuilder(
                    arguments[0] == PERSISTENT ? "!" : ""
                ).append(
                    arguments[1] == null ?
                        org.openmdx.base.naming.TransactionalSegment.getClassicRepresentationOfNewInstance() :
                        String.valueOf(arguments[1])
                );
                for(
                    int i = 2;
                    i < size;
                    i++
                ){
                    qualifier.append(
                        arguments[i] == PERSISTENT ? '!' : '*'
                    ).append(
                        arguments[++i]
                    );
                }
                return qualifier.toString();
            }
        }
    }

    private QueryComponents toQueryComponents (
        Object rawQuery
    ){
        return new QueryComponents(this.container, unwrapQuery(rawQuery));
    }

    private static Object unwrapQuery(Object rawQuery) {
        if(rawQuery instanceof Object[]) {
            Object[] args = (Object[]) rawQuery;
            if(args.length == 1 && args[0] instanceof RefQuery_1_0) {
                return args[0];
            }
        }
        return rawQuery;
    }

    static class QueryComponents {

        private final Container_1_0 source;
        private final FeatureOrderRecord[] order;
        private final FetchPlan fetchPlan;

        QueryComponents(
            Container_1_0 container,
            Object query
        ) {
            if(query instanceof Condition[]) {
                source = container.subMap(
                    new Filter(
                        Arrays.asList((Condition[])query),
                        null,
                        null
                    )
                );
                fetchPlan = null;
                order = null;
            } else if(query instanceof FeatureOrderRecord[]) {
                source = container;
                fetchPlan = null;
                order = (FeatureOrderRecord[]) query;
            } else if(query instanceof QueryFilterRecord) {
                QueryFilterRecord filter = (QueryFilterRecord)query;
                List<FeatureOrderRecord> orderSpecifier = filter.getOrderSpecifier();
                source = container.subMap(filter);
                fetchPlan = null;
                order = orderSpecifier.toArray(new FeatureOrderRecord[orderSpecifier.size()]);
            } else if(query instanceof RefQuery_1_0) {
                RefQuery_1_0 refQuery = (RefQuery_1_0)query;
                QueryFilterRecord filter = refQuery.refGetFilter();
                List<FeatureOrderRecord> orderSpecifier = filter.getOrderSpecifier();
                source = container.subMap(filter);
                order = orderSpecifier.toArray(new FeatureOrderRecord[orderSpecifier.size()]);
                fetchPlan = refQuery.getFetchPlan();
            } else {
                source = container;
                order = null;
                fetchPlan = null;
            }
        }


        /**
         * Retrieve source.
         *
         * @return Returns the source.
         */
        Container_1_0 getSource() {
            return this.source;
        }


        /**
         * Retrieve order.
         *
         * @return Returns the order.
         */
        FeatureOrderRecord[] getOrder() {
            return this.order;
        }


        /**
         * Retrieve fetchPlan.
         *
         * @return Returns the fetchPlan.
         */
        FetchPlan getFetchPlan() {
            return this.fetchPlan;
        }

    }

}

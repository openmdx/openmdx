/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: In-Memory Layer Pug-In
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
package org.openmdx.base.dataprovider.layer.persistence.none;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.rest.spi.ObjectFilter;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.XRISegment;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ConditionRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * An in-memory data store
 * 
 * Required configuration entries
 * <ul>
 * <li>namespaceId
 * </ul>
 * <p>
 * A single namespace must not be used by different threads concurrently.
 */
public class InMemory_2 implements Port<RestConnection> {

    /**
     * @return the namespaceId
     */
    public String getNamespaceId() {
        return this.namespaceId;
    }

    /**
     * Sets the namspace id and provides the namespace local data
     * 
     * @param namespaceId
     *            the namespaceId to set
     */
    public void setNamespaceId(
        String namespaceId
    ) {
        this.namespaceId = namespaceId;
        this.data = getData(namespaceId);
    }

    private String namespaceId;

    /**
     * To identify extent queries
     */
    private static final Path EXTENT_PATTERN = new Path("xri://@openmdx*($.)/provider/($..)/segment/($..)/extent");

    /**
     * For internal requests
     */
    private final InteractionSpecs INTERNAL = InteractionSpecs.getRestInteractionSpecs(false);

    /**
     * Namespace local data
     */
    protected SortedMap<Path, SortedMap<XRISegment, ObjectRecord>> data;

    /**
     * ClassLoader local storage
     */
    private static final SortedMap<String, SortedMap<Path, SortedMap<XRISegment, ObjectRecord>>> storage = new TreeMap<String, SortedMap<Path, SortedMap<XRISegment, ObjectRecord>>>();

    /**
     * Defines the large objects' buffer size
     */
    protected static final int CHUNK_SIZE = 10000;

    /**
     * Drop the given namespace
     * 
     * @param namespaceId
     */
    protected static void dropNamespace(
        String namespaceId
    ) {
        storage.remove(namespaceId);
    }

    /**
     * @deprecated shall not be used anymore
     */
    @Deprecated
    public void setDatasourceName(
        String datasourceName
    ) {
        SysLog.warning(
            "The 'datasourceName' configuration is no longer supported and will be removed soon", 
            getClass().getName()
        );
    }

    /**
     * @return {@code null}
     * 
     * @deprecated shall not be used anymore
     */
    @Deprecated
    public String getDatasourceName() {
        SysLog.warning(
            "The 'datasourceName' configuration is no longer supported and will be removed soon", 
            getClass().getName()
        );
        return null;
    }

    /**
     * Retrieve the namespace local data
     * 
     * @param namespaceId
     * 
     * @return the namespace local data
     */
    private static synchronized SortedMap<Path, SortedMap<XRISegment, ObjectRecord>> getData(
        String namespaceId
    ) {
        SortedMap<Path, SortedMap<XRISegment, ObjectRecord>> data = storage.get(namespaceId);
        if (data == null) {
            SysLog.detail("Create namespace", namespaceId);
            storage.put(
                namespaceId,
                data = new TreeMap<Path, SortedMap<XRISegment, ObjectRecord>>());
        } else {
            SysLog.detail("Attach namespace", namespaceId);
        }
        return data;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.application.dataprovider.spi.OperationAwareLayer_1#
     * getInteraction(javax.resource.cci.Connection)
     */
    @Override
    public Interaction getInteraction(
        RestConnection connection
    )
        throws ResourceException {
        return new RestInteraction(connection);
    }

    /**
     * Large object holders
     */
    static interface Lob extends Serializable {

        /**
         * Retrieve the large object's value
         * 
         * @return
         */
        Object getValue();

    }

    /**
     * This class represents a binary large object
     */
    static final class Blob implements Lob {

        /**
         * 
         */
        private static final long serialVersionUID = 3763099665276547641L;

        /**
         * @serial
         */
        private byte[] value;

        /**
         * Constructor
         * 
         * @param source
         * @throws IOException
         */
        Blob(
            InputStream source
        )
            throws IOException {
            ByteArrayOutputStream target = new ByteArrayOutputStream(source.available());
            byte[] buffer = new byte[CHUNK_SIZE];
            for (int i = source.read(buffer); i > 0; i = source.read(buffer))
                target.write(buffer, 0, i);
            this.value = target.toByteArray();
        }

        /**
         * Retrieve the BLOB's content
         * 
         * @return
         */
        public Object getValue() {
            return new ByteArrayInputStream(this.value);
        }

    }

    /**
     * This class represents a character large object
     */
    static final class Clob implements Lob {

        /**
         * 
         */
        private static final long serialVersionUID = 3688510986948457785L;
        /**
         * @serial
         */
        private char[] value;

        /**
         * Constructor
         * 
         * @param source
         * @throws IOException
         */
        Clob(
            Reader source
        )
            throws IOException {
            CharArrayWriter target = new CharArrayWriter();
            char[] buffer = new char[CHUNK_SIZE];
            for (int i = source.read(buffer); i > 0; i = source.read(buffer))
                target.write(buffer, 0, i);
            this.value = target.toCharArray();
        }

        /**
         * Retrieve the BLOB's content
         * 
         * @return
         */
        public Object getValue() {
            return new CharArrayReader(this.value);
        }

    }

    //------------------------------------------------------------------------
    // Class RestInteraction
    //------------------------------------------------------------------------

    class RestInteraction extends AbstractRestInteraction {

        protected RestInteraction(
            RestConnection connection
        ) {
            super(connection);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.openmdx.base.rest.spi.AbstractRestInteraction#put(org.openmdx.
         * base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.cci.ObjectRecord,
         * javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean update(
            RestInteractionSpec ispec,
            ObjectRecord input,
            ResultRecord output
        )
            throws ResourceException {
            final Path xri = input.getResourceIdentifier();
            final ObjectRecord beforeImage;
            try {
                beforeImage = getMandatoryObject(xri);
            } catch (ResourceException exception) {
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        "Unable to update the object with the given resource identifier: "
                            + "The object could not be found in the name space",
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_FOUND,
                            new BasicException.Parameter("resourceIdentifier", xri),
                            new BasicException.Parameter("functionName", ispec.getFunctionName()),
                            new BasicException.Parameter("interactionVerb", ispec.getInteractionVerbName()))));
            }
            if (!input.getRecordName().equals(beforeImage.getRecordName())) {
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        "It is not allowed to change the object class",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter("resourceIdentifier", xri),
                            new BasicException.Parameter("functionName", ispec.getFunctionName()),
                            new BasicException.Parameter("interactionVerb", ispec.getInteractionVerbName()),
                            new BasicException.Parameter("oldClass", beforeImage.getRecordName()),
                            new BasicException.Parameter("newClass", input.getRecordName()))));
            }
            final ObjectRecord afterImage = beforeImage.clone();
            mergeObjectValues(input, afterImage);
            final SortedMap<XRISegment, ObjectRecord> container = getMandatoryContainer(xri.getParent());
            final ObjectRecord replaced = container.put(xri.getLastSegment(), afterImage);
            try {
                validateBeforeImage(input, replaced);
            } catch (ResourceException exception) {
                container.put(xri.getLastSegment(), replaced);
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        "Unable to update the object with the given resource identifier: "
                            + "The object has been modifed since it was read",
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                            new BasicException.Parameter("resourceIdentifier", xri),
                            new BasicException.Parameter("functionName", ispec.getFunctionName()),
                            new BasicException.Parameter("interactionVerb", ispec.getInteractionVerbName()))));
            }
            return true;
        }

        @SuppressWarnings("unchecked")
        private void mergeObjectValues(
            ObjectRecord input,
            ObjectRecord afterImage
        )
            throws ResourceException {
            final MappedRecord value;
            try {
                value = (MappedRecord) afterImage.getValue().clone();
            } catch (CloneNotSupportedException exception) {
                throw ResourceExceptions.toResourceException(exception);
            }
            value.putAll(input.getValue());
            afterImage.setValue(value);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.openmdx.base.rest.spi.AbstractRestInteraction#find(org.openmdx.
         * base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.cci.QueryRecord,
         * org.openmdx.base.rest.cci.ResultRecord)
         */
        @SuppressWarnings({
            "unchecked", "rawtypes"
        })
        @Override
        protected boolean find(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output
        )
            throws ResourceException {
            // TODO position, size
            final Path xri = input.getResourceIdentifier();
            final boolean extentQuery = xri.isLike(EXTENT_PATTERN);
            final QueryFilterRecord queryFilter = input.getQueryFilter();
            final ObjectRecordFilter objectFilter = queryFilter == null || queryFilter.getCondition().isEmpty() ? null
                : new ObjectRecordFilter(queryFilter, extentQuery);
            final ObjectRecordComparator comparator = queryFilter == null ? null : ObjectRecordComparator.getInstance(
                queryFilter.getOrderSpecifier());
            List<ObjectRecord> target = comparator == null ? output : new ArrayList();
            if (extentQuery) {
                findInExtent(target, objectFilter);
            } else {
                findInContainer(target, xri, objectFilter);
            }
            if (comparator != null) {
                Collections.sort(target, comparator);
                output.addAll(target);
            }
            output.setHasMore(false);
            return true;
        }

        private void findInContainer(
            List<ObjectRecord> target,
            final Path xri,
            final ObjectRecordFilter objectFilter
        ) {
            final Path containerFilter;
            final XRISegment segmentFilter;
            if (xri.isObjectPath()) {
                containerFilter = xri.getParent();
                segmentFilter = xri.getLastSegment();
            } else {
                containerFilter = xri;
                segmentFilter = null;
            }
            if (containerFilter.isPattern()) {
                for (Map.Entry<Path, SortedMap<XRISegment, ObjectRecord>> c : data.entrySet()) {
                    if (c.getKey().isLike(containerFilter)) {
                        findInContainer(target, c.getValue(), segmentFilter, objectFilter);
                    }
                }
            } else {
                findInContainer(target, getOptionalContainer(containerFilter), segmentFilter, objectFilter);
            }
        }

        private void findInExtent(
            List<ObjectRecord> target,
            final ObjectRecordFilter objectFilter
        ) {
            final Path identityPattern = objectFilter.getIdentityPattern();
            for (SortedMap<XRISegment, ObjectRecord> c : data.values()) {
                for (ObjectRecord o : c.values()) {
                    if (o.getResourceIdentifier().isLike(identityPattern) &&
                        objectFilter.accept(o)) {
                        target.add(o);
                    }
                }
            }
        }

        private void findInContainer(
            List<ObjectRecord> output,
            SortedMap<XRISegment, ObjectRecord> container,
            XRISegment segmentFilter,
            ObjectRecordFilter objectFilter
        ) {
            if (container != null) {
                for (Map.Entry<XRISegment, ObjectRecord> o : container.entrySet()) {
                    if (segmentFilter == null || segmentFilter.matches(o.getKey())) {
                        final ObjectRecord candidate = o.getValue();
                        if (objectFilter == null || objectFilter.accept(candidate)) {
                            output.add(candidate);
                        }
                    }
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.openmdx.base.rest.spi.AbstractRestInteraction#get(org.openmdx.
         * base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.cci.QueryRecord,
         * org.openmdx.base.rest.cci.ResultRecord)
         */
        @SuppressWarnings("unchecked")
        @Override
        protected boolean get(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output
        )
            throws ResourceException {
            final Path xri = input.getResourceIdentifier();
            final ObjectRecord object;
            if (xri.size() <= 3) {
                object = getMandatoryObject(xri);
            } else {
                final SortedMap<XRISegment, ObjectRecord> container = getOptionalContainer(xri.getParent());
                if (container == null) {
                    return false;
                }
                object = container.get(xri.getLastSegment());
                if (object == null) {
                    return false;
                }
            }
            output.add(object);
            output.setHasMore(false);
            output.setTotal(1L);
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.openmdx.base.rest.spi.AbstractRestInteraction#delete(org.openmdx.
         * base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.cci.QueryRecord)
         */
        @Override
        protected boolean delete(
            RestInteractionSpec ispec,
            QueryRecord input
        )
            throws ResourceException {
            final ResultRecord objects = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
            find(INTERNAL.GET, input, objects);
            boolean success = false;
            for (Object object : objects) {
                success |= delete(INTERNAL.DELETE, (ObjectRecord) object);
            }
            return success;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.openmdx.base.rest.spi.AbstractRestInteraction#verify(org.openmdx.
         * base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.cci.ObjectRecord)
         */
        @Override
        protected boolean verify(
            RestInteractionSpec ispec,
            ObjectRecord input
        )
            throws ResourceException {
            final Path xri = input.getResourceIdentifier();
            getMandatoryObject(xri);
            validateBeforeImage(input, getMandatoryObject(xri));
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.openmdx.base.rest.spi.AbstractRestInteraction#delete(org.openmdx.
         * base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.cci.ObjectRecord)
         */
        @Override
        protected boolean delete(
            RestInteractionSpec ispec,
            ObjectRecord input
        )
            throws ResourceException {
            final Path xri = input.getResourceIdentifier();
            final SortedMap<XRISegment, ObjectRecord> container = getOptionalContainer(xri.getParent());
            if (container == null) {
                return false;
            }
            final ObjectRecord beforeImage = container.remove(xri.getLastSegment());
            // Remove composites
            {
                for (Iterator<Path> i = InMemory_2.this.data.keySet().iterator(); i.hasNext();) {
                    Path containerIdentity = i.next();
                    if (containerIdentity.startsWith(xri)) {
                        i.remove();
                    }
                }
            }
            if (beforeImage == null) {
                return false;
            }
            try {
                validateBeforeImage(input, beforeImage);
            } catch (ResourceException exception) {
                container.put(xri.getLastSegment(), beforeImage);
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        "Unable to delete the object with the given resource identifier: "
                            + "The object has been modifed since it was read",
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                            new BasicException.Parameter("resourceIdentifier", xri),
                            new BasicException.Parameter("functionName", ispec.getFunctionName()),
                            new BasicException.Parameter("interactionVerb", ispec.getInteractionVerbName()))));
            }
            return true;
        }

        private void validateBeforeImage(
            ObjectRecord input,
            ObjectRecord beforeImage
        )
            throws ResourceException {
            final byte[] beforeImageVersion = beforeImage.getVersion();
            if (beforeImageVersion != null) {
                final byte[] expectedVersion = input.getVersion();
                if (!Arrays.equals(expectedVersion, beforeImageVersion)) {
                    throw ResourceExceptions.initHolder(
                        new ResourceException(
                            "Validation failed",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                                new BasicException.Parameter("resourceIdentifier", input.getResourceIdentifier()),
                                new BasicException.Parameter("expectedVersion", expectedVersion),
                                new BasicException.Parameter("beforeImageVersion", beforeImageVersion))));
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.openmdx.base.rest.spi.AbstractRestInteraction#create(org.openmdx.
         * base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.cci.ObjectRecord,
         * org.openmdx.base.rest.cci.ResultRecord)
         */
        @Override
        protected boolean create(
            RestInteractionSpec ispec,
            ObjectRecord input,
            ResultRecord output
        )
            throws ResourceException {
            final Path xri = input.getResourceIdentifier();
            final SortedMap<XRISegment, ObjectRecord> container;
            try {
                container = getMandatoryContainer(xri.getParent());
            } catch (ResourceException exception) {
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        "Unable to create the object with the given resource identifier: "
                            + "The parent object is missing in the namespace",
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_FOUND,
                            new BasicException.Parameter("resourceIdentifier", xri),
                            new BasicException.Parameter("functionName", ispec.getFunctionName()),
                            new BasicException.Parameter("interactionVerb", ispec.getInteractionVerbName()))));
            }
            final ObjectRecord inTheWay = container.get(xri.getLastSegment());
            if (inTheWay != null) {
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        "Unable to create the object with the given id: "
                            + "There is already an object with this resource identifier in the namespace",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.DUPLICATE,
                            new BasicException.Parameter("resourceIdentifier", xri),
                            new BasicException.Parameter("functionName", ispec.getFunctionName()),
                            new BasicException.Parameter("interactionVerb", ispec.getInteractionVerbName()))));
            }
            final ObjectRecord afterImage = input.clone();
            container.put(xri.getLastSegment(), afterImage);
            return true;
        }

        private SortedMap<XRISegment, ObjectRecord> getOptionalContainer(
            Path xri
        ) {
            return data.get(xri);
        }

        private SortedMap<XRISegment, ObjectRecord> getMandatoryContainer(
            Path xri
        )
            throws ResourceException {
            SortedMap<XRISegment, ObjectRecord> container = getOptionalContainer(xri);
            if (container == null && xri.size() > 1) {
                try {
                    getMandatoryObject(xri.getParent());
                } catch (ResourceException exception) {
                    throw ResourceExceptions.initHolder(
                        new ResourceException(
                            "Unable to retrieve the container with the given resource identifier",
                            BasicException.newEmbeddedExceptionStack(
                                exception,
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.NOT_FOUND,
                                new BasicException.Parameter("resourceIdentifier", xri))));
                }
                data.put(
                    xri,
                    container = new TreeMap<XRISegment, ObjectRecord>());
            }
            return container;
        }

        private ObjectRecord getOptionalObject(
            Path xri
        ) {
            final SortedMap<XRISegment, ObjectRecord> container = getOptionalContainer(xri.getParent());
            return container == null ? null : container.get(xri.getLastSegment());
        }

        private ObjectRecord getMandatoryObject(
            Path xri
        )
            throws ResourceException {
            ObjectRecord object = getOptionalObject(xri);
            if (object == null) {
                switch (xri.size()) {
                    case 1:
                        object = newObject(xri, "org:openmdx:base:Authority");
                        break;
                    case 3:
                        object = newObject(xri, "org:openmdx:base:Provider");
                        break;
                    default:
                        throw ResourceExceptions.initHolder(
                            new ResourceException(
                                "Unable to retrieve the object with the given resource identifier",
                                BasicException.newEmbeddedExceptionStack(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.NOT_FOUND,
                                    new BasicException.Parameter("resourceIdentifier", xri))));
                }
            }
            return object;
        }

        private ObjectRecord newObject(
            Path xri,
            String type
        )
            throws ResourceException {
            final ObjectRecord object = newObject(xri);
            final MappedRecord value = Records.getRecordFactory().createMappedRecord(
                type);
            object.setValue(value);
            return object;
        }

    }

    //------------------------------------------------------------------------
    // Class ObjectRecordFilter
    //------------------------------------------------------------------------

    class ObjectRecordFilter extends ObjectFilter {

        /**
         * Constructor
         */
        private ObjectRecordFilter(
            QueryFilterRecord filter,
            boolean extentQuery
        ) {
            super(null, filter, extentQuery);
        }

        /**
         * Implements {@code Serializable}
         */
        private static final long serialVersionUID = -3151451305490323997L;

        @Override
        protected ObjectRecordFilter newFilter(
            QueryFilterRecord delegate
        ) {
            return new ObjectRecordFilter(delegate, extentQuery);
        }

        @Override
        protected ModelElement_1_0 getClassifier(
            Object object
        ) {
            try {
                return Model_1Factory.getModel().getElement(((ObjectRecord) object).getValue().getRecordName());
            } catch (ServiceException e) {
                throw new RuntimeServiceException(e); // TODO
            }
        }

        private Map<XRISegment, ObjectRecord> getContainerAsEmptyMapWhenMissing(
            Path xri
        ) {
            final SortedMap<XRISegment, ObjectRecord> container = data.get(xri);
            return container == null ? Collections.emptyMap() : container;
        }
        
        @Override
        protected boolean isEmpty(
            Object object,
            String featureName,
            QueryFilterRecord filter
        )
            throws ServiceException {
            final Path xri = ((ObjectRecord) object).getResourceIdentifier().getChild(featureName);
            final ObjectRecordFilter objectFilter = new ObjectRecordFilter(filter, false);
            for (ObjectRecord candidate : getContainerAsEmptyMapWhenMissing(xri).values()) {
                if (objectFilter.accept(candidate)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected Iterator<?> getValuesIterator(
            Object candidate,
            ConditionRecord condition
        ) {
            if (candidate instanceof ObjectRecord) {
                final ObjectRecord objectRecord = (ObjectRecord) candidate;
                return getValuesIterator(objectRecord, condition.getFeature());
            }
            if (candidate instanceof Path) {
                final Path xri = (Path) candidate;
                final ObjectRecord objectRecord = getContainerAsEmptyMapWhenMissing(xri.getParent()).get(xri.getLastSegment());
                if (objectRecord == null) {
                    throw new RuntimeServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "Unresolvable resource identifier",
                        new BasicException.Parameter(BasicException.Parameter.XRI, xri));
                }
                return getValuesIterator(objectRecord, condition.getFeature());
            }
            throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Unexpected value holder",
                new BasicException.Parameter("acceptable", ObjectRecord.class.getName(), Path.class.getName()),
                new BasicException.Parameter("actual", candidate == null ? null : candidate.getClass().getName()));
        }

        private Iterator<?> getValuesIterator(
            ObjectRecord candidate,
            String feature
        ) {
            if (SystemAttributes.OBJECT_CLASS.equals(feature)) {
                return Collections.singleton(candidate.getValue().getRecordName()).iterator();
            } else
                try {
                    if (SystemAttributes.OBJECT_INSTANCE_OF.equals(feature)) {
                        return newInstanceOfIterator(getClassifier(candidate));
                    } else if (SystemAttributes.CORE.equals(feature) && isCoreInstance(getClassifier(candidate))) {
                        return Collections.emptySet().iterator();
                    } else {
                        final Object values = candidate.getValue().get(feature);
                        return 
                            values == null ? Collections.emptySet().iterator() :
                            values instanceof IndexedRecord ? ((IndexedRecord) values).iterator() :
                            values instanceof MappedRecord ? ((MappedRecord) values).values().iterator() :
                            Collections.singleton(values).iterator();
                    }
                } catch (ServiceException e) {
                    throw new RuntimeServiceException(
                        e,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "Unable to retrieve the given feature's values",
                        new BasicException.Parameter(BasicException.Parameter.XRI, candidate.getResourceIdentifier()),
                        new BasicException.Parameter("feature", feature));
                }
        }

    }

}

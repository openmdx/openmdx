/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Query Record 
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
package org.openmdx.base.rest.spi;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Set;

#if JAVA_8
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Record;
#else
import jakarta.resource.cci.IndexedRecord;
import jakarta.resource.cci.Record;
#endif

import org.openmdx.base.collection.Sets;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.xml.sax.SAXException;

/**
 * Query Record
 */
public class QueryRecord 
    extends AbstractMappedRecord<org.openmdx.base.rest.cci.QueryRecord.Member>
    implements org.openmdx.base.rest.cci.QueryRecord, Externalizable 
{

    /**
     * Constructor 
     */
	public QueryRecord() {
        super();
    }

    /**
     * Constructor for clones 
     *
     * @param that the object to be cloned
     */
    protected QueryRecord(
    	QueryRecord that
    ){
        super(that);    }
    
    /**
     * The filter to be applied to the query
     */
    private org.openmdx.base.rest.cci.QueryFilterRecord queryFilter;
    
    /**
     * The names of the explicitely requested features
     */
    private IndexedRecord featureName;
    
    /**
     * The name of the fetch group to be used
     */
    private String fetchGroupName;
    
    /**
     * The number of objects to be skipped
     */
    private Long position;
    
    /**
     * Tells whether refresh is required before answering the query
     */
    private boolean refresh;
    
    /**
     * The resource identifier of the objects to be retrieved
     */
    private Path resourceIdentifier;

    /**
     * The openMDX Query Language statements to be applied to the query
     */
    private String query;
    
    /**
     * The type of the objects to be retrieved
     */
    private String queryType;
    
    /**
     * The number of objects to be retrieved
     */
    private Long size;
    
    /**
     * Allows to share the member information among the instances
     */
    private static final Members<Member> MEMBERS = Members.newInstance(Member.class);

    /**
     * The eagerly acquired REST formatter instance
     */
    protected static final RestFormatter restFormatter = RestFormatters.getFormatter();
    
    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = -2709873653223744925L;

    /* (non-Javadoc)
	 * @see org.openmdx.base.rest.spi.AbstractMappedRecord#makeImmutable()
	 */
	@Override
	public void makeImmutable() {
		super.makeImmutable();
		freeze(this.queryFilter);
		freeze(this.featureName);
	}

	/**
     * Retrieve resourceIdentifier.
     *
     * @return Returns the resourceIdentifier.
     */
    @Override
    public Path getResourceIdentifier() {
        return this.resourceIdentifier;
    }
    
    /**
     * Set resourceIdentifier.
     * 
     * @param resourceIdentifier The resourceIdentifier to set.
     */
    @Override
    public void setResourceIdentifier(Path path) {
        this.resourceIdentifier = path;
    }
    
    /**
     * Retrieve position.
     *
     * @return Returns the position.
     */
    @Override
    public Long getPosition() {
        return this.position;
    }
    
    /**
     * Set position.
     * 
     * @param position The position to set.
     */
    @Override
    public void setPosition(Long position) {
        this.position = position;
    }
    
    /**
     * Retrieve query.
     *
     * @return Returns the query.
     */
    @Override
    public org.openmdx.base.rest.cci.QueryFilterRecord getQueryFilter() {
        return this.queryFilter;
    }
    
    /**
     * Set the query filter.
     * 
     * @param queryFilter The query filter to be set.
     */
    @Override
    public void setQueryFilter(org.openmdx.base.rest.cci.QueryFilterRecord queryFilter) {
        this.queryFilter = queryFilter;
    }
    
    /**
     * Retrieve queryType.
     *
     * @return Returns the queryType.
     */
    @Override
    public String getQueryType() {
        return this.queryType;
    }

    /**
     * Set queryType.
     * 
     * @param queryType The queryType to set.
     */
    @Override
    public void setQueryType(String queryType) {
        this.queryType = queryType; 
    }
    
    /**
     * Retrieve size.
     *
     * @return Returns the size.
     */
    @Override
    public Long getSize() {
        return this.size;
    }
    
    /**
     * Set size.
     * 
     * @param size The size to set.
     */
    @Override
    public void setSize(Long size) {
        this.size = size;
    }

    private IndexedRecord jcaFeatureName(){
        if(this.featureName == null) {
            this.featureName = newSet();
        }
        return this.featureName;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.QueryRecord#getFeatureName()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getFeatureName() {
        return Sets.asSet(jcaFeatureName());
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.QueryRecord#setFeatureName(java.util.Set)
     */
    @Override
    public void setFeatureName(Set<String> featureName) {
        replaceValues(jcaFeatureName(), featureName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.QueryRecord#getFetchGroupName()
     */
    @Override
    public String getFetchGroupName() {
        return this.fetchGroupName;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.QueryRecord#setFetchGroupName(java.lang.String)
     */
    @Override
    public void setFetchGroupName(String fetchGroupName) {
        this.fetchGroupName = fetchGroupName;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.QueryRecord#setRefresh(boolean)
     */
    @Override
    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.QueryRecord#isRefresh()
     */
    @Override
    public boolean isRefresh() {
        return this.refresh;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.QueryRecord#setQuery(java.lang.String)
     */
    @Override
    public void setQuery(String query) {
        this.query = query;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.QueryRecord#getQuery()
     */
    @Override
    public String getQuery() {
        return this.query;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.AbstractRecord#clone()
     */
    @Override
    public QueryRecord clone(
    ){
        return new QueryRecord(this);
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordName()
     */
    @Override
    public String getRecordName() {
        return NAME;
    }

    /**
     * Retrieve a value by index
     * 
     * @param index the index
     * @return the value
     */
    @Override
    protected Object get(
        Member index
    ){
        switch(index) {
            case featureName: return jcaFeatureName();
            case fetchGroupName: return getFetchGroupName();
            case position: return getPosition();
            case query: return getQuery();
            case queryFilter: return getQueryFilter();
            case queryType: return getQueryType();
            case refresh: return Boolean.valueOf(isRefresh());
            case resourceIdentifier: return toString(getResourceIdentifier());
            case size: return getSize();
            default: return super.get(index);
        }
    }

    /**
     * Set a value by index 
     * 
     * @param index the index
     * @param value the new value
     * 
     * @return the old value
     */
    @Override
    protected void put(
        Member index,
        Object value
    ){
        switch(index) {
            case featureName: 
                replaceValues(jcaFeatureName(), (Collection<?>)value);
                break;
            case fetchGroupName:
                setFetchGroupName((String) value);
                break;
            case position:
                setPosition(toLong(value));
                break;
            case query:
                setQuery((String) value);
                break;
            case queryFilter:
                setQueryFilter((QueryFilterRecord) value);
                break;
            case queryType:
                setQueryType((String) value);
                break;
            case refresh:
                setRefresh(toBoolean(value));
                break;
            case resourceIdentifier:
                setResourceIdentifier(toPath(value));
                break;
            case size:    
                setSize(toLong(value));
                break;
            default: 
            	super.put(index, value);
        }
    }

    /**
     * Tells whether the candidate is a query record
     * 
     * @param record the record to be inspected
     * 
     * @return {@code true} if the record's name equals to {@code org:openmdx:kernel:Query}.
     */
    public static boolean isCompatible(Record record) {
        return NAME.equals(record.getRecordName());
    }
    
	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.spi.AbstractMappedRecord#members()
	 */
	@Override
	protected Members<Member> members() {
		return MEMBERS;
	}

	
    //--------------------------------------------------------------------------
    // Implements Externalizable
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    @Override
    public void readExternal(
        ObjectInput in
    ) throws IOException, ClassNotFoundException {
        try {
            RestParser.parseResponse(
                this, 
                RestParser.asSource(in)
            );
        } catch (SAXException exception) {
            throw Throwables.initCause(
                new InvalidObjectException(exception.getMessage()),
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                "Unable to read externalized QueryRecord"
            );
        }
    }

    /* (non-Javadoc)
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    @Override
    public void writeExternal(
        ObjectOutput out
    ) throws IOException {
        try (Target target = restFormatter.asTarget(out)){
            restFormatter.format(target, this);
        } catch (Exception exception) {
            throw Throwables.initCause(
                new NotSerializableException(exception.getMessage()),
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                "Unable to externalize QueryRecord"
            );
        }
    }

}

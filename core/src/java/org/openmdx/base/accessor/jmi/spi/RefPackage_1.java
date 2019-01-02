/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: RefPackage_1 class
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.accessor.jmi.spi;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOFatalUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jmi.reflect.RefAssociation;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefEnum;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.jmi.reflect.RefStruct;
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.w3c.cci2.SparseArray;

//---------------------------------------------------------------------------
/**
 * Implementation of RefPackage 1.x. 
 * <p>
 * This implementation supports lightweight serialization. It contains only
 * members to the immediate and outermost package. Other members are static.
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class RefPackage_1 implements Jmi1Package_1_0, Serializable {

    /**
     * Constructor 
     *
     * @param qualifiedPackageName
     * @param outermostPackage
     * @param immediatePackage
     */
    public RefPackage_1(
        String qualifiedPackageName,
        RefPackage outermostPackage,
        RefPackage immediatePackage
    ) {
        this.outermostPackage = (RefRootPackage_1)outermostPackage;
        this.immediatePackage = immediatePackage;
        this.qualifiedPackageName = qualifiedPackageName;
    }

    /**
     * Checks whether the persistence manager is already closed
     * 
     * @return <code>true</code> if the persistence manager is already closed
     */
    protected boolean isClosed(){
    	return ((RefPackage_1_0)this.outermostPackage).refPersistenceManager().isClosed();
    }
    
    /**
     * Asserts that the associated persistence manager is open
     * 
     * @exception JDOFatalUserException
     */
    protected void assertOpen(
    ){
    	if(isClosed()) {
			throw BasicException.initHolder(
		        new JDOFatalUserException(
		            "The persistence manager is closed",
		            BasicException.newEmbeddedExceptionStack(
		                BasicException.Code.DEFAULT_DOMAIN,
		                BasicException.Code.ILLEGAL_STATE
		            )
		        )
		    );
		}
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_0#refDelegate()
     */
    @Override
    public PersistenceManager refDelegate() {
        return this.outermostPackage.refDelegate();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_0#getRefPackage(javax.resource.cci.InteractionSpec)
     */
    @Override
    public RefPackage_1_0 refPackage(InteractionSpec viewContext) {
        return this.outermostPackage.refPackage(viewContext);
    }


    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_4#refLegacyDelegate()
     */
    @Override
    public boolean isTerminal() {
        return this.outermostPackage.isTerminal();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_3#refPersistenceManagerFactory()
     */
    @Override
    public PersistenceManagerFactory refPersistenceManagerFactory() {
        return this.outermostPackage.refPersistenceManagerFactory();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_2#refContainer(org.openmdx.compatibility.base.naming.Path)
     */
    @Override
    public <C extends RefContainer<?>> C refContainer(
        Path resourceIdentifier,
        Class<C> containerClass
    ){
        return this.outermostPackage.refContainer(resourceIdentifier,containerClass);
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_2#refViewContext()
     */
    @Override
    public InteractionSpec refInteractionSpec() {
        return this.outermostPackage.refInteractionSpec();
    }

    /**
     * Retrieves the JDO Persistence Manager delegating to this package.
     * 
     * @return the JDO Persistence Manager delegating to this package.
     */
    @Override
    public PersistenceManager_1_0 refPersistenceManager(
    ) {
        return this.outermostPackage.refPersistenceManager();
    }

    
    /* (non-Javadoc)
	 * @see org.openmdx.base.accessor.jmi.spi.Jmi1Package_1_0#unmarshalUnchecked(java.lang.Object)
	 */
    @Override
	public Object unmarshalUnchecked(Object source) {
        return this.outermostPackage.unmarshalUnchecked(source);
	}

	//-------------------------------------------------------------------------
    @Override
    public final Model_1_0 refModel(
    ) {
        return Model_1Factory.getModel();
    }

    //-------------------------------------------------------------------------
    @Override
    public RefObject refObject(
        Path objectId
    ) {
        return this.outermostPackage.refObject(objectId);
    }

    //-------------------------------------------------------------------------
    public RefStruct refCreateStruct(
        String structName,
        List arg
    ) {
        try {
            IndexedRecord record = Records.getRecordFactory().createIndexedRecord(structName);
            if(arg != null) {
                for(Object value : arg){
                    record.add(toStructValue(value));
                }
            }
            return refMapping().newStruct(
                refOutermostPackage(),
                record
            );
        } catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        } catch (ResourceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------
    private static Object toStructValue(
        Object source
    ) throws ResourceException{
        if(source instanceof List<?>) {
            IndexedRecord target = Records.getRecordFactory().createIndexedRecord(Multiplicity.LIST.code());
            for(Object value : (List<?>)source){
                target.add(toStructValue(value));
            }
            return target;
        } else if (source instanceof Set<?>) {
            IndexedRecord target = Records.getRecordFactory().createIndexedRecord(Multiplicity.SET.code());
            for(Object value : (Set<?>)source){
                target.add(toStructValue(value));
            }
            return target;
        } else if (source instanceof SparseArray<?>) {
            MappedRecord target = Records.getRecordFactory().createMappedRecord(Multiplicity.SPARSEARRAY.code());
            for(Object e : ((SparseArray<?>)source).entrySet()) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) e;
                target.put(
                    entry.getKey(),
                    toStructValue(entry.getValue())
                );
            }
            return target;            
        } else {
        	return ReducedJDOHelper.replaceObjectById(source);
        }
    }
        
    /**
     * Create a structure proxy based on the record name
     * 
     * @param record
     * 
     * @return the structure proxy based on the record name
     */
    @Override
    public RefStruct refCreateStruct(
        Record record
    ) {
        try {
            return this.refMapping().newStruct(
                this.refOutermostPackage(),
                record
            );
        } catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------
    @Override
    public RefQuery_1_0 refCreateQuery(
        String type,
        boolean subclasses, 
        QueryFilterRecord filter
    ) throws ServiceException {
        String qualifiedClassName = type.endsWith("Query") ? type.substring(0, type.length() - "Query".length()) : type;
        Mapping_1_0 mapping = this.refMapping();
        return mapping.getClassMapping(
            qualifiedClassName
        ).newQuery(
            new Jmi1QueryInvocationHandler(
                new RefQuery_1(
                    filter,
                    mapping,
                    qualifiedClassName, 
                    subclasses
                )
            )
        ); 
    }

    //-------------------------------------------------------------------------
    @Override
    public RefObject refMetaObject(
    ) {
        try {
            return new RefMetaObject_1(
                this.refModel().getElement(
                    this.refMofId()
                )
            );
        } catch(ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    @Override
    public RefPackage refPackage(
        RefObject nestedPackage
    ) {
        return this.outermostPackage.refPackage(nestedPackage);
    }

    //-------------------------------------------------------------------------
    @Override
    public RefPackage refPackage(
        String nestedPackageName
    ) {
        return this.outermostPackage.refPackage(nestedPackageName);
    }

    //-------------------------------------------------------------------------
    @Override
    public Collection<?> refAllPackages(
    ) {
        return this.outermostPackage.refAllPackages();
    }

    //-------------------------------------------------------------------------
    @Override
    public RefClass refClass(
        RefObject type
    ) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    @Override
    public Jmi1Class_1_0 refClass(
        String qualifiedClassName
    ) {
        return this.refClass(
            qualifiedClassName,
            this
        );
    }

    //-------------------------------------------------------------------------
    /**
     * @param qualifedClassName qualified name of the returned class.
     * @param immediatePackage passed as package when constructing the class, i.e.
     *        refImmediatePackage() of the returned class is equal to immediatePackage.
     */
    Jmi1Class_1_0 refClass(
        String qualifiedClassName,
        Jmi1Package_1_0 immediatePackage
    ) {
        try {
            return this.refMapping().getClassMapping(qualifiedClassName).newClass(immediatePackage);
        } catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------
    @Override
    public Collection<RefClass> refAllClasses(
    ) {
        throw new UnsupportedOperationException("refAllClasses not supported");
    }

    //-------------------------------------------------------------------------
    /**
     * This implementation does not support association classes.
     */
    @Override
    public RefAssociation refAssociation(
        RefObject association
    ) {
        throw new UnsupportedOperationException("associations not supported");
    }

    //-------------------------------------------------------------------------
    /**
     * This implementation does not support association classes.
     */
    @Override
    public RefAssociation refAssociation(
        String associationName
    ) {
        throw new UnsupportedOperationException("assocations not supported");
    }

    //-------------------------------------------------------------------------
    /**
     * This implementation does not support association classes.
     */
    @Override
    public Collection<?> refAllAssociations(
    ) {
        return this.outermostPackage.refAllAssociations();
    }

    //-------------------------------------------------------------------------
    public RefStruct refCreateStruct(
        RefObject structType,
        List args
    ) {
        try {
            if(
                structType instanceof ModelElement_1_0 &&
                this.refModel().isStructureType(structType)
            ) {
                return this.refCreateStruct(
                    ((ModelElement_1_0)structType).getQualifiedName(),
                    args
                );
            }
            else {
                throw new ServiceException (
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "unsupported structure type. Must be [StructureType]",
                    new BasicException.Parameter("structure type", structType)
                );
            }
        } catch(ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * This implementation does not supporte enums.
     */
    @Override
    public RefEnum refGetEnum(
        RefObject enumType,
        String literalName
    ) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    //-------------------------------------------------------------------------
    /**
     * This implementation does not supporte enums.
     */
    @Override
    public RefEnum refGetEnum(
        String enumName,
        String literalName
    ) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    //-------------------------------------------------------------------------
    @Override
    public void refDelete(
    ) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    // RefBaseObject
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    @Override
    public RefPackage refImmediatePackage(
    ) {
        return this.immediatePackage;
    }

    //-------------------------------------------------------------------------
    @Override
    public RefRootPackage_1 refOutermostPackage(
    ) {
        return this.outermostPackage;
    }

    //-------------------------------------------------------------------------
    @Override
    public Collection<?> refVerifyConstraints(
        boolean deepVerify
    ) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.Jmi1Package_1_0#refImplementationMapper()
     */
    @Override
    public Mapping_1_0 refMapping() {
        return this.outermostPackage.refMapping();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        return 
            that instanceof RefPackage_1 &&
            this.refMofId().equals(((RefPackage_1)that).refMofId());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.refMofId().hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RefPackage " + this.refMofId();
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refMofId()
     */
    @Override
    public String refMofId() {
        return this.qualifiedPackageName;
    }

    
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 350730437983426852L;

    /**
     * These members are serialized when serializing a package. However, this
     * is cheap because 'immediatePackage' only contains the members 
     * 'immediatePackage', 'outmostPackage', 'implementationUri' and
     * the 'outmostPackage' the 'accessor'.
     */
    private final RefPackage immediatePackage;
    private final RefRootPackage_1 outermostPackage;
    private final String qualifiedPackageName;
    
}

//--- End of File -----------------------------------------------------------

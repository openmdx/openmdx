package org.openmdx.application.rest.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.UnavailableException;
import javax.rmi.PortableRemoteObject;

import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.ConnectionAdapter;
import org.openmdx.base.rest.spi.RestConnection;
import org.openmdx.kernel.Version;
import org.openmdx.kernel.exception.BasicException;

/**
 * Connection Factory Adapter
 */
public class Connection_2Factory implements ConnectionFactory {

    /**
     * Constructor 
     *
     * @param physicalConnection
     */
    private Connection_2Factory(
        RestConnection shareableConnection
    ){
       this.shareableConnection = shareableConnection;
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -1797567109005822231L;

    /**
     * The underlying physical connection
     */
    final RestConnection shareableConnection;
    
    /**
     * The resource adapter's metadata
     */
    private final ResourceAdapterMetaData metaData = new ResourceAdapterMetaData(){

        public String getAdapterName() {
            return "openMDX/REST outbound";
        }

        public String getAdapterShortDescription() {
            return "openMDX/2 Outbound REST Resource Adapter";
        }
        public String getAdapterVendorName() {
            return "OMEX AG";
        }

        public String getAdapterVersion() {
            return Version.getSpecificationVersion();
        }

        public String[] getInteractionSpecsSupported() {
            return new String[]{RestInteractionSpec.class.getName()};
        }

        /**
         * Retrieve the JCA specification version
         * 
         * @return the JCA specification version
         */
        public String getSpecVersion() {
            return "1.5.";
        }

        public boolean supportsExecuteWithInputAndOutputRecord() {
            return shareableConnection instanceof LocalAdapter;
        }

        public boolean supportsExecuteWithInputRecordOnly() {
            return true;
        }

        public boolean supportsLocalTransactionDemarcation() {
            return false;
        }
        
    };

    /**
     * Create a delegating connection factory
     *  
     * @param delegate
     * 
     * @return the connection factory
     * 
     * @throws ResourceException  
     */
    public static ConnectionFactory newInstance(
        Object delegate
    ) throws ResourceException {
        return new Connection_2Factory(
            delegate instanceof RestConnection ? (RestConnection)delegate :
            delegate instanceof Connection_2LocalHome ? LocalAdapter.newInstance(delegate) : 
            RemoteAdapter.newInstance(delegate)
        );
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getConnection()
     */
    public Connection getConnection(
    ) throws ResourceException {
        return ConnectionAdapter.newInstance(this.shareableConnection);
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getConnection(javax.resource.cci.ConnectionSpec)
     */
    public Connection getConnection(
        ConnectionSpec properties
    ) throws ResourceException {
        return ConnectionAdapter.newInstance(this.shareableConnection); // the properties are ignored at the moment
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getMetaData()
     */
    public ResourceAdapterMetaData getMetaData(
    ) throws ResourceException {
        return this.metaData;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getRecordFactory()
     */
    public RecordFactory getRecordFactory(
    ) throws ResourceException {
        return Records.getRecordFactory();
    }

    /* (non-Javadoc)
     * @see javax.resource.Referenceable#setReference(javax.naming.Reference)
     */
    public void setReference(Reference reference) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.naming.Referenceable#getReference()
     */
    public Reference getReference(
    ) throws NamingException {
        throw new UnsupportedOperationException();
    }
    

    
    //------------------------------------------------------------------------
    // Class RemoteAdapter
    //------------------------------------------------------------------------
    
    /**
     * Dataprovider 2 Remote Adapter 
     */
    static class RemoteAdapter implements RestConnection {

        /**
         * Constructor 
         *
         * @param dataprovider
         * @throws RemoteException 
         */
        private RemoteAdapter(
            Connection_2_0Remote dataprovider
        ) throws RemoteException {
            this.delegate = dataprovider;
            this.handle = dataprovider.getHandle();
        }

        /**
         * The EJB handle
         */
        private Handle handle;
        
        /**
         * the EJB based dataprovider instance
         */
        private transient Connection_2_0Remote delegate;
        
        /**
         * Dataprovider 2 connection factory
         * 
         * @param home the dataprovider's local home 
         * 
         * @return a new dataprovider connection
         * 
         * @throws ResourceException
         */
        static RestConnection newInstance(
            Object home
        ) throws ResourceException {
            try {
                Connection_2Home ejbHome = (Connection_2Home) PortableRemoteObject.narrow(
                    home,
                    Connection_2Home.class
                );
                return new RemoteAdapter(
                    ejbHome.create()
                );
            } catch (Exception exception) {
                throw ResourceExceptions.initHolder(
                    new UnavailableException(
                        "EJB creation failure",
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ACTIVATION_FAILURE
                        )
                    )
                );
            }
        }

        
        //------------------------------------------------------------------------
        // Implements Dataprovider_2_0
        //------------------------------------------------------------------------
        
        /**
         * Validate the connection's state
         * 
         * @return the delegate
         * 
         * @throws ResourceException
         */
        protected Connection_2_0Remote getDelegate(
        ) throws ResourceException {
            if(this.delegate == null) {
                if(this.handle == null) throw ResourceExceptions.initHolder(
                    new javax.resource.spi.IllegalStateException(
                        "The connection is closed", 
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ILLEGAL_STATE
                        )
                    )
                );
                try {
                    this.delegate = (Connection_2_0Remote) this.handle.getEJBObject();
                } catch (RemoteException exception) {
                    new UnavailableException(
                        "The EJB can not be re-acquired", 
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.COMMUNICATION_FAILURE
                        )
                    );
                }
            }
            return this.delegate;
        }
            
        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.transport.cci.Dataprovider_2_0#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
         */
        public Record execute(
            InteractionSpec ispec, 
            Record input
        ) throws ResourceException {
            try {
                return getDelegate().execute(ispec, input);
            } catch (RemoteException exception) {
                throw ResourceExceptions.initHolder(
                    new EISSystemException(
                        "Remote EJB Execution Failure", 
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.RestConnection#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
         */
        public boolean execute(
            InteractionSpec ispec,
            Record input,
            Record output
        ) throws ResourceException {
            throw ResourceExceptions.initHolder(
                new NotSupportedException(
                    "The execute method with input and output record is supported for local connections only",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED
                    )
               )
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.cci.Dataprovider_2_0#getMetaData()
         */
        public ConnectionMetaData getMetaData(
        ) throws ResourceException {
            try {
                return getDelegate().getMetaData();
            } catch (RemoteException exception) {
                throw ResourceExceptions.initHolder(
                    new EISSystemException(
                        "EJB Meta Data Acquistion Failure", 
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
        }

    }

    
    //------------------------------------------------------------------------
    // Class LocalAdapter
    //------------------------------------------------------------------------
    
    /**
     * Connection 2 Local Adapter 
     */
    static class LocalAdapter implements RestConnection {

        /**
         * Constructor 
         *
         * @param delegate
         */
        private LocalAdapter(
            Connection_2_0Local delegate
        ) {
            this.delegate = delegate;
        }

        /**
         * The delegate
         */
        private Connection_2_0Local delegate;

        /**
         * Connection 2 Factory
         * 
         * @param localHome the dataprovider's local home 
         * 
         * @return a new dataprovider connection
         * 
         * @throws ResourceException
         */
        static RestConnection newInstance(
            Object localHome
        ) throws ResourceException {
            try {
                Connection_2LocalHome ejbHome = (Connection_2LocalHome)localHome;
                return new LocalAdapter(
                    ejbHome.create()
                );
            } catch (Exception exception) {
                throw ResourceExceptions.initHolder(
                    new UnavailableException(
                        "EJB creation failure",
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ACTIVATION_FAILURE
                        )
                    )
                );
            }
        }
        
        
        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.transport.cci.Dataprovider_2_0#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
         */
        public Record execute(
            InteractionSpec ispec, 
            Record input
        ) throws ResourceException {
            try {
                return this.delegate.execute(ispec, input);
            } catch (EJBException exception) {
                throw ResourceExceptions.initHolder(
                    new EISSystemException(
                        "Local EJB Execution Failure", 
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.RestConnection#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
         */
        public boolean execute(
            InteractionSpec ispec,
            Record input,
            Record output
        ) throws ResourceException {
            try {
                return this.delegate.execute(ispec, input, output);
            } catch (EJBException exception) {
                throw ResourceExceptions.initHolder(
                    new EISSystemException(
                        "Local EJB Execution Failure", 
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.cci.Dataprovider_2_0#getMetaData()
         */
        public ConnectionMetaData getMetaData(
        ) throws ResourceException {
            try {
                return this.delegate.getMetaData();
            } catch (EJBException exception) {
                throw ResourceExceptions.initHolder(
                    new EISSystemException(
                        "EJB Meta Data Acquistion Failure", 
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
        }

    }

}
package org.openmdx.base.rest.spi;

import javax.resource.cci.ResourceAdapterMetaData;

import org.openmdx.base.Version;
import org.openmdx.base.resource.spi.RestInteractionSpec;

public class RestResourceAdapterMetaData implements ResourceAdapterMetaData {

	/**
	 * Constructor 
	 * 
	 * @param supportsLocalTransactionDemarcation tells whether
	 * local transaction demarcation is supported
	 */
	public RestResourceAdapterMetaData(
		boolean supportsLocalTransactionDemarcation
	) {
		this.supportsLocalTransactionDemarcation = supportsLocalTransactionDemarcation;
	}

	private final boolean supportsLocalTransactionDemarcation;
	/** 
     * Gets a tool displayable name of the resource adapter.
     *
     *  @return   String representing the name of the resource adapter
     */
    public String getAdapterName() {
        return "openMDX/REST";
    }

    /** 
     * Gets a tool displayable short desription of the resource
     * adapter.
     *
     * @return   String describing the resource adapter
     */
    public String getAdapterShortDescription() {
        return "openMDX/2 Plug-In Wrapper";
    }

    /** Gets the name of the vendor that has provided the resource 
     *  adapter.
     *
     *  @return   String representing name of the vendor that has 
     *            provided the resource adapter
     */
    public String getAdapterVendorName() {
        return "openMDX";
    }

    /** 
     * Gets the version of the resource adapter.
     *
     * @return   String representing version of the resource adapter
     */
    public String getAdapterVersion() {
        return Version.getSpecificationVersion();
    }

    /** 
     * Returns an array of fully-qualified names of InteractionSpec
     * types supported by the CCI implementation for this resource
     * adapter. Note that the fully-qualified class name is for 
     * the implementation class of an InteractionSpec. This method 
     * may be used by tools vendor to find information on the 
     * supported InteractionSpec types. The method should return 
     * an array of length 0 if the CCI implementation does not 
     * define specific InteractionSpec types.
     *
     * @return   Array of fully-qualified class names of
     *           InteractionSpec classes supported by this
     *           resource adapter's CCI implementation
     * @see      javax.resource.cci.InteractionSpec
     */
    public String[] getInteractionSpecsSupported() {
        return new String[]{RestInteractionSpec.class.getName()};
    }

    /** 
     * Returns a string representation of the version of the 
     * connector architecture specification that is supported by
     * the resource adapter.
     *
     * @return   String representing the supported version of 
     *           the connector architecture
     */
    public String getSpecVersion() {
        return "1.5.";
    }

    /** 
     * Returns true if the implementation class for the Interaction 
     * interface implements public boolean execute(InteractionSpec 
     * ispec, Record input, Record output) method; otherwise the 
     * method returns false.
     *
     * @return   boolean depending on method support
     * @see      javax.resource.cci.Interaction
     */
    public boolean supportsExecuteWithInputAndOutputRecord() {
        return true;
    }

    /** 
     * Returns true if the implementation class for the Interaction
     * interface implements public Record execute(InteractionSpec
     * ispec, Record input) method; otherwise the method returns 
     * false.
     *
     * @return   boolean depending on method support
     * @see      javax.resource.cci.Interaction
     */
    public boolean supportsExecuteWithInputRecordOnly() {
        return true;
    }

    /** 
     * Returns true if the resource adapter implements the LocalTransaction
     * interface and supports local transaction demarcation on the 
     * underlying EIS instance through the LocalTransaction interface.
     *
     * @return  true if resource adapter supports resource manager
     *          local transaction demarcation through LocalTransaction
     *          interface; false otherwise
     * @see     javax.resource.cci.LocalTransaction
     */
    public boolean supportsLocalTransactionDemarcation() {
        return supportsLocalTransactionDemarcation;
    }

}

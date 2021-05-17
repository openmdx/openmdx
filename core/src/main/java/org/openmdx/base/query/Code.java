package org.openmdx.base.query;

public interface Code {

    /**
     * Retrieve the code's external value
     * 
     * @return the external code value
     */
    short code();

    /**
     * Retrieve the code's UNICODE symbol
     * 
     * @return the code's UNICODE symbol
     */
    char symbol();
	
}

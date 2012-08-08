package org.openmdx.base.accessor.spi;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openmdx.base.accessor.cci.Structure_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * ListStructure_1
 */
public class ListStructure_1 implements Serializable, Structure_1_0 {

    /**
     * 
     */
    private static final long serialVersionUID = 3762537819129853747L;

    /**
     * Constructor
     *
     * @param structureClass
     *        The model class of the structure to be created
     * @param fieldNames
     *        the structure's field names
     * @param values
     *        the structure's values
     *
     * @exception ServiceException BAD_PARAMETER
     *            if (fieldNames.size() != values.size())
     * @exception NullPointerException
     *            if either of the arguments is null
     */
    public ListStructure_1(
        String structureType,
        List<String> fieldNames,
        List<?> values
    ){
        this.structureType = structureType;
        this.fieldNames = Collections.unmodifiableList(fieldNames);
        this.values = values;
    }

    /**
     * Constructor
     *
     * @param structureClass
     *        The model class of the structure to be created
     * @param fieldNames
     *        the structure's field names
     * @param values
     *        the structure's values
     *
     * @exception ServiceException BAD_PARAMETER
     *            if (fieldNames.length != values.length)
     * @exception NullPointerException
     *            if either of the arguments is null
     */
    public ListStructure_1(
        String structureClass,
        String[] fieldNames,
        Object[] values
    ) throws ServiceException {
        this(
            structureClass,
            Arrays.asList(fieldNames),
            Arrays.asList(values)
        );
        if(fieldNames.length != values.length) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.BAD_PARAMETER,
            "The lengths of fieldNames and values do not match",
            new BasicException.Parameter("fieldNames", fieldNames.length),
            new BasicException.Parameter("values", values.length)
        );  
    }

    //--------------------------------------------------------------------------
    // Implements Structure_1_0
    //--------------------------------------------------------------------------

    /**
     * Returns the object's model class.
     *
     * @return    the object's model class
     */
    public String objGetType(
    ){
        return this.structureType;
    }

    /**
     * Return the field names in this structure.
     *
     * @return  the (String) field names contained in this structure
     */
    public List<String> objFieldNames(
    ){
        return this.fieldNames;
    }

    /**
     * Get a field.
     *
     * @param       field
     *              the fields's name
     *
     * @return      the fields value which may be null.
     *
     * @exception   ServiceException BAD_PARAMETER
     *              if the structure has no such field
     */
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        int index = this.fieldNames.indexOf(feature);
        if(index == -1) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.BAD_PARAMETER,
            "This structure has no such field",
            new BasicException.Parameter("feature", feature)
        );
        return this.values.get(index);
    }

    //--------------------------------------------------------------------------
    // Object
    //--------------------------------------------------------------------------
    public String toString(
    ) {
        return
        "[structureType=" + this.structureType + ", " +
        "content=" + values.toString() + "]";
    }

    //--------------------------------------------------------------------------
    // Instance Members
    //--------------------------------------------------------------------------

    /**
     * The structure's model class
     */
    private final String structureType;

    /**
     * The structure's values.
     */
    protected final List<?> values;

    /**
     * The structure's field names.
     */  
    private final List<String> fieldNames;

}
/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ConfigurationEntryDescription.java,v 1.3 2004/04/02 16:59:00 wfro Exp $
 * Description: Configuration entry meta data
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:00 $
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
package org.openmdx.base.application.configuration;


import java.util.HashSet;
import java.util.Set;


/**
 * This class holds the meta data for a configuration entry managed by
 * implementations of the class. Entry descriptors may describe an entry as
 * either optional or mandatory. If an entry is described as optional and it
 * does not exits in a configuration a default entry will be used that is
 * constructed from the entry descriptors default values. If an entry is
 * described as mandatory the entry must exist in a configuration.
 *
 * @see <a href="AbstractConfiguration">AbstractConfiguration</a>
 */
public final class ConfigurationEntryDescription
{

    /**
     * Creates a new configuration entry description for a <b>mandatory</b>
     * single-valued entry. minOccurance and maxOccurance of the entry value are
     * set to 1.
     *
     * @param name The name of the configuration entry. Must not be null or empty.
     * @param annotation An optional annotation
     * @param type The type of the value.
     *
     * @throws IllegalArgumentException if any restrictions of these parameters
     * are violated.
     */
    public ConfigurationEntryDescription(
            String   name,
            String   annotation,
            Class    type)
        throws IllegalArgumentException
    {
        this(
            name,
            annotation,
            type,
            null,
            null,
            1,
            1);
    }


    /**
     * Creates a new configuration entry description for a single-valued entry.
     * minOccurance and maxOccurance of the entry value are set to 1. Dependent
     * of the default value the entry can be mandatory or optional.
     *
     * @param name The name of the configuration entry. Must not be null or empty.
     * @param annotation An optional annotation
     * @param type The type of the values.
     * @param defaultValue A default value. If the default value is null the
     * entry is mandatory else it is optional. A default value must be of the
     * above specified type.
     * @param valueSet An optional array of possible values. The elements must
     * be of the above specified type
     *
     * @throws IllegalArgumentException if any restrictions of these parameters
     * are violated.
     */
    public ConfigurationEntryDescription(
            String   name,
            String   annotation,
            Class    type,
            Object   defaultValue,
            Object[] valueSet)
        throws IllegalArgumentException
    {
        this(
            name,
            annotation,
            type,
            (defaultValue == null) ? null : new Object[]{defaultValue},
            valueSet,
            1,
            1);
    }


    /**
     * Creates a new configuration entry description for a
     * <b>mandatory</b> single-valued or multi-valued entry
     *
     * @param name The name of the configuration entry. Must not be null or
     * empty.
     * @param annotation An optional annotation
     * @param type The type of the values. If multi-valued all values must be of
     * this type
     * @param minOccurance The minimal occurance of the cardinality of values.
     * Allowed range [0,1...N]. The value 0 means that no minimal occurance is
     * required.
     * @param maxOccurance  The maximal occurance of the cardinality of values.
     * Allowed range [0,1...N]. The value 0 means that no maximal occurance
     * is required.
     * @throws IllegalArgumentException if any restrictions of these parameters
     * are violated.
     */
    public ConfigurationEntryDescription(
            String   name,
            String   annotation,
            Class    type,
            int      minOccurance,
            int      maxOccurance)
        throws IllegalArgumentException
    {
        this(
            name,
            annotation,
            type,
            null,
            null,
            minOccurance,
            maxOccurance);
    }


    /**
     * Creates a new configuration entry description for a single-valued or
     * multi-valued entry
     *
     * @param name The name of the configuration entry. Must not be null or
     * empty.
     * @param annotation An optional annotation
     * @param type The type of the values. If multi-valued all values must be of
     * this type
     * @param defaultValues An optional array of default values. The elements
     * must be of the above specified type
     * @param valueSet An optional array of possible values. The elements must
     * be of the above specified type
     * @param minOccurance The minimal occurance of the cardinality of values.
     * Allowed range [0,1...N]. The value 0 means that no minimal occurance is
     * required.
     * @param maxOccurance  The maximal occurance of the cardinality of values.
     * Allowed range [0,1...N]. The value 0 means that no maximal occurance
     * is required.
     * @throws IllegalArgumentException if any restrictions of these parameters
     * are violated.
     */
    public ConfigurationEntryDescription(
            String   name,
            String   annotation,
            Class    type,
            Object[] defaultValues,
            Object[] valueSet,
            int      minOccurance,
            int      maxOccurance)
        throws IllegalArgumentException
    {
        this.name          = name;
        this.annotation    = annotation;
        this.type          = type;
        this.defaultValues = defaultValues;
        this.valueSet      = new HashSet();
        this.minOccurance  = (minOccurance < 0) ? 0 : minOccurance;
        this.maxOccurance  = (maxOccurance < 0) ? 0 : maxOccurance;


        if ((this.name == null) || (this.name.length() == 0)) {
            throw new IllegalArgumentException(
                        "Bad ConfigurationEntryDescription element."
                        + "The name must not be null or empty");
        }

        if (this.type == null) {
            throw new IllegalArgumentException(
                        "Bad ConfigurationEntryDescription element '"+name+"'."
                        + "The type must not be null");
        }

        if (valueSet != null) {
            for(int ii=0; ii<valueSet.length; ii++) {
                Object value = valueSet[ii];

                if (value == null) {
                    throw new IllegalArgumentException(
                                "Bad ConfigurationEntryDescription element."
                                + "The valueSet element " + ii + "must not be null");
                }

                if (!type.isInstance(value)) {
                    throw new IllegalArgumentException(
                                "Bad ConfigurationEntryDescription element."
                                + "The valueSet element " + ii
                                + " type (" + value.getClass().getName()
                                + ") is not an instance of the specified"
                                + "type " + type);
                }

                this.valueSet.add(valueSet[ii]);
            }
        }


        if (defaultValues != null) {
            for(int ii=0; ii<defaultValues.length; ii++) {
                Object value = defaultValues[ii];

                if (value == null) {
                    throw new IllegalArgumentException(
                                "Bad ConfigurationEntryDescription element '"+name+"'."
                                + "The the default value element " + ii
                                + " must not be null");
                }

                if (!type.isInstance(value)) {
                    throw new IllegalArgumentException(
                                "Bad ConfigurationEntryDescription element '"+name+"'."
                                + "The type of the default value element " + ii
                                + " (" + value.getClass().getName()
                                +") is not an instance of the specified"
                                + " type " + type);
                }

                if (!this.valueSet.isEmpty()) {
                    if (!this.valueSet.contains(value)) {
                        throw new IllegalArgumentException(
                                    "Bad ConfigurationEntryDescription element '"+name+"'."
                                    + "The the default value element " + ii
                                    + " (" +value+ ") is not one of the"
                                    + " possible values.");
                    }
                }
            }

            if ((minOccurance > 0) && (defaultValues.length < minOccurance)) {
                throw new IllegalArgumentException(
                            "Bad ConfigurationEntryDescription element '"+name+"'."
                            + "The number of default values ("
                            + defaultValues.length + ")"
                            + " must not be lower than the  specified"
                            + " minOccurance (" + minOccurance + ")");
            }

            if ((maxOccurance > 0) && (defaultValues.length > maxOccurance)) {
                throw new IllegalArgumentException(
                            "Bad ConfigurationEntryDescription element '"+name+"'."
                            + "The number of default values ("
                            + defaultValues.length + ")"
                           + "must not be greater than the specified"
                            + " maxOccurance (" + maxOccurance + ")");
            }
        }
     }


    /**
     * Returns the name of the configuration entry
     *
     * @return The configuration entry's name
     */
    public String getName() { return this.name; }

    /**
     * Returns the annotation of the configuration entry
     *
     * @return The configuration entry's annotation
     */
    public String getAnnotation() { return this.annotation; }

    /**
     * Returns the type of the values of the configuration entry
     *
     * @return The configuration entry's type
     */
    public Class getType() { return this.type; }

    /**
     * Checks if the configuration entry is mandatory
     *
     * @return boolean
     */
    public boolean isMandatory() { return (defaultValues == null); }

    /**
     * Returns the default value of the configuration entry
     *
     * @return The configuration entry's default values
     */
    public Object[] getDefaultValues() { return this.defaultValues; }

    /**
     * Returns the value set of the configuration entry
     *
     * @return The configuration entry's value set
     */
    public Set getValueSet() { return this.valueSet; }

    /**
     * Returns the minimal occurance of the cardinality of values.
     * The value -1 means that no minimal occurance is required.
     *
     * @return The configuration entry's value min occurance
     */
    public int getMinOccurance() { return this.minOccurance; }

    /**
     * Returns the maximal occurance of the cardinality of values.
     * The value -1 means that no maximal occurance is required.
     *
     * @return The configuration entry's value max occurance
     */
    public int getMaxOccurance() { return this.maxOccurance; }


    /**
     * Returns a string representation of the cardinality.
     *
     * Returned format:
     * ">=X", "<=X", "X", "X..Y", "any"
     *
     * @return A string
     */
    public String getStringifiedCardinality()
    {
        if ((minOccurance == 0) && (maxOccurance == 0)) {
            return "any";
        }

        if (minOccurance == maxOccurance) {
            return String.valueOf(minOccurance);
        }

        if ((minOccurance != 0) && (maxOccurance != 0)) {
            return String.valueOf(minOccurance) + ".." + maxOccurance;
        }

        if (minOccurance == 0) {
            return "<=" + maxOccurance;
        }else{
            return ">=" + minOccurance;
        }
    }



    /** The configuration entry name */
    private final String name;

    /** The annotation */
    private final String annotation;

    /** The type of the value(s) */
    private final Class  type;

    /** The default values 0,1..N */
    private final Object[] defaultValues;

    /** The value set */
    private final HashSet  valueSet;

    /** Specifies the minimal occurance of the cardinality of values [0...N]*/
    private final int minOccurance;

    /** Specifies the maximal occurance of the cardinality of values [0...N]*/
    private final int maxOccurance;
}

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractConfiguration.java,v 1.12 2008/03/21 18:28:05 hburger Exp $
 * Description: AbstractConfiguration
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:28:05 $
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


import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.exception.InvalidCardinalityException;
import org.openmdx.compatibility.base.collection.SparseArray;
import org.openmdx.compatibility.base.collection.TreeSparseArray;
import org.openmdx.kernel.application.configuration.Configuration;
import org.openmdx.kernel.application.configuration.Report;
import org.openmdx.kernel.environment.cci.VersionNumber;

/**
 * A configuration holds multiple configuration entries, is versioned and has
 * a name.
 * A configuration entry is a name-value pair with with an additional
 * annotation field. The value field is a list of simple objects. The
 * annotation field holds additional describing information on a configuration
 * entry.
 *
 * This class is not threadsafe. Thread safety must be guaranteed by the
 * callers.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractConfiguration
    implements Configuration
{
    /**
     * Creates a new configuration object.
     *
     * @param name            The name of this configuration
     * @param majorVersion    The major version of the configuration [0,1,2,..]
     * @param minorVersion    The minor version of the configuration [0,1,2,..]
     */
    public AbstractConfiguration(
        String  name,
        int     majorVersion,
        int     minorVersion)
    {
        this.name = name;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;

        init();
    }


    /**
     * Creates a new configuration object based on version 0.0
     *
     * @param name    The name of this configuration
     */
    public AbstractConfiguration(String  name)
    {
        this(name, 0, 0);
    }


    /**
     * Returns the configuration name.
     *
     * @return A configuration name string
     */
    final public String getName()
    {
        return this.name;
    }


    /**
     * Returns the configuration's major version.
     *
     * @return A configuration major version
     */
    final public int getMajorVersion()
    {
        return this.majorVersion;
    }


    /**
     * Returns the configuration's minor version.
     *
     * @return A configuration minor version
     */
    final public int getMinorVersion()
    {
        return this.minorVersion;
    }

    /**
     * Sets a new version.
     *
     * The version information may be changed if the validation process
     * upgrades the configuration to a new version.
     *
     * @param majorVersion    The major version of the configuration [0,1,2,..]
     * @param minorVersion    The minor version of the configuration [0,1,2,..]
     */
    final public void setVersion(
        int     majorVersion,
        int     minorVersion)
    {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }


    /**
     * Apply the defaults from the configuration entry descriptions.
     *
     * Each entry from the configuration entry descriptions (name and
     * defaultValues) is added to this configuration if the entry does not
     * yet exist. Defaults are not applied to entries that exists but do not
     * have any values.
     *
     * <p>
     * Note: Applying defaults is not undoable.
     *
     */
    public void applyDefaults()
    {
        if (this.entryDescrMap == null) return;

        String   name;
        Object[] values;
        ConfigurationEntryDescription   entry;

        Iterator  iter = this.entryDescrMap.keySet().iterator();
        while(iter.hasNext()) {
            entry = (ConfigurationEntryDescription)this.entryDescrMap.get(iter.next());

            name   = entry.getName();
            values = entry.getDefaultValues();
            if (!entryExists(name) && (values != null)) {
                setValues(name, values);
            }
        }
    }


    /**
     * Adds a new value at a specified position to a multi-valued configuration
     * entry. Creates the entry if it does not exist. The specified
     * multi-valued value is added to the entry or replaced if it already exists.
     * If only one value is added to an entry at position 0, the entry
     * is single-valued.
     *
     * @param name        A string specifying the name of the configuration
     *                    entry
     * @param value       An object representing the value of the configuration
     *                    entry. May be null.
     * @param index       The index for value the multi-valued entry. Must not
     *                    be negative. Use 0 for a single-valued entry.
     * @throws IndexOutOfBoundsException if the specified value index is
     *         negative.
     */
    public void addValue(
            String   name,
            Object   value,
            int      index)
        throws IndexOutOfBoundsException
    {
        if ((name == null) || (name.length() == 0)) return;  // silently ignore

        if (index < 0) {
            throw new IndexOutOfBoundsException(
                    "A configuration entry value index must "
                    + " not be negative. Entry name: '" + name + "'");
        }else{
            SparseArray  sa = (SparseArray)this.configValues.get(name);
            if (sa == null) {
                sa = new TreeSparseArray();
                this.configValues.put(name, sa);
            }
            sa.set(index, value);
        }
    }


    /**
     * Add all configuration entries from a configuration.
     *
     * Add the entries to this configuration without affecting the configuration
     * version and the configuration entry descriptions.
     *
     * @param configuration  The configuration to be added
     * @param preserve       Preserve existing entries from beeing overwritten
     */
    public void addAll(
                AbstractConfiguration  configuration,
                boolean               preserve)
    {
        String    name;
        Iterator  iter = configuration.getEntryNames().iterator();
        while(iter.hasNext()) {
            name = (String)iter.next();
            if (!preserve || (preserve && !entryExists(name))) {
                List values = configuration.getValues(name);
                if (values != null) {
                    setValues(name, values.toArray());
                }
            }
        }
    }


    /**
     * Sets a new single-valued configuration entry or multi-valued value in a
     * configuration entry.
     *
     * If the name ends with an array specifier (e.g. "x.y.z[n]") the referenced
     * configuration entry will be considered as multi-valued and the value is
     * stored at the specified position. If the entry does not exits it is
     * created. The specified multi-valued value is added or replaced if it
     * already exists.
     *
     * If the name does not end with an array specifier (e.g. "x.y.z[n]") the
     * referenced configuration entry will be considered as single-valued.
     *
     * @param name        A string specifying the name of the configuration
     *                    entry
     * @param value       An object representing the value of the configuration
     *                    entry. May be null.
     * @throws IndexOutOfBoundsException if the specified array index is
     *         negative.
     * @throws IllegalArgumentException if the specified array index suffix
     *         has an invalid format.
     */
    public void setValue(
            String   name,
            Object   value)
        throws IndexOutOfBoundsException,
                IllegalArgumentException
    {
        if (!isValidName(name)) return;  // silently ignore

        int idx = getArrayIndex(name);

        if (idx == -1) {
            addValue(stripArrayIndex(name), value, 0);
        }else{
            addValue(stripArrayIndex(name), value, idx);
        }
    }


    /**
     * Adds a new multi-valued configuration entry.
     *
     *
     * @param name A string specifying the name of the configuration entry.
     * Must not have an have an array specifier
     * @param values An array with 0, 1 or more value objects. All objects
     * must be of identical type. The type must one of the supported value
     * types. May be a null if there are no values at all.
     *
     * @throws IllegalArgumentException if the specified configuration entry
     * name contains an array specifier.
     */
    public void setValues(
            String   name,
            Object[] values)
        throws IllegalArgumentException
    {
        if (!isValidName(name)) return;  // silently ignore

        if (values == null) {
            this.configValues.put(name, new TreeSparseArray());
        }else{
            this.configValues.put(name, new TreeSparseArray(Arrays.asList(values)));
        }
    }


    /**
     * Returns the values of a configuration entry. Works for single-valued and
     * and multi-valued entries.
     *
     * @param name  A string specifying the name of the configuration entry
     *
     * @return  A configuration entry value. Returns null if the entry does not
     * exist.
     */
    public List getValues(String  name)
    {
        SparseArray  sa = getRawEntry(name);

        return (sa == null) ? null: sa.asList();
    }


    /**
     * Returns the value of a configuration entry. The entry must be
     * single-valued.
     *
     * @param name  A string specifying the name of the configuration entry
     *
     * @return  A configuration entry value. Maybe a null object if no value
     * is found.
     *
     * @throws InvalidCardinalityException if the referenced configuration is
     * multi-valued.
     */
    public Object getValue(String  name)
        throws InvalidCardinalityException
    {
        SparseArray  sa = getRawEntry(name);
        if (sa == null) return null;   // entry does not exist
        if (sa.isEmpty()) return null; // no values at all


        if ((sa.start() != 0) || (sa.end() != 1)) {
            throw new InvalidCardinalityException(
                "The referenced configuration entry '" + name + "'"
                + " from the configuration '" + this.name + "' is not"
                + " single-valued");
        }

        return sa.get(0);
    }


    /**
     * Returns true if the entry exists and has at least one value.
     *
     * @return  true if the entry exists
     */
    public boolean entryExists(String name)
    {
        return (getRawEntry(name) != null);
    }


    /**
     * Returns the configuration entry names of this configuration
     *
     * @return  Returns a <code>Set</code> the configuration entry names of
     *          this configuration
     */
    public Set getEntryNames()
    {
        return this.configValues.keySet();
    }


    /**
     * Remove a configuration entry.
     *
     * @param name  A string specifying the name of the configuration entry
     */
    public void removeEntry(String  name)
    {
        if (!isValidName(name)) return;

        this.configValues.remove(name);
    }


    /**
     * Removes all configuration entries.
     */
    public void removeAllEntries()
    {
        init();
    }


    /**
     * Returns the entry descriptions as an unmodifiable <code>Map</code> of
     * <code>ConfigurationEntryDescription</code> objects. The entry
     * description name builds the map's key. Returns always a Map (that may
     * have no entries).
     *
     * @return Map
     */
    public Map getEntryDescriptions()
    {
        return Collections.unmodifiableMap(this.entryDescrMap);
    }


    /**
     * Validates the configuration.
     *
     * A concrete configuration validates the configuration entries. Validation
     * goes beyond simple verification:
     * <ul>
     * <li>Detects missing entries and adds its defaults
     * <li>Rejects unsupported entries
     * <li>Checks the version and possibly upgrades the configuration
     *     to the most current version
     * </ul>
     * To accomplish this the validator needs more information than is required
     * for just a simple verification.
     *
     * @return  A validation report
     */
    public abstract Report validate();


    /**
     * Verifies the configuration. All configuration entries are verified with
     * the configuration entry descriptions. A verification requires that the
     * configuration entry descriptions have been set.
     *
     * <p> The verification process verifies that:
     * <ul>
     * <li>each entry has an entry description
     * <li>multi-valued entries do not have value holes
     * <li>the value cardinality is valid
     * <li>the value types are valid
     * <li>each entry from entry description list is present
     * </ul>
     *
     * @return  A validation report
     */
    final public Report verify()
    {
        Report report;


        // create new report
        report = new Report(
        	this.name, 
			new VersionNumber(
			    new int[]{this.majorVersion, this.minorVersion}
			), 
			null
	    );

        // meta data available?
        if (this.entryDescrMap == null) {
            report.addError("No entry descriptions available");
            return report;
        }

        // check each configuration entry
        Iterator  iter = getEntryNames().iterator();
        while(iter.hasNext()) {
            String  entryName = (String)iter.next();

            // find the configuration entry description
            ConfigurationEntryDescription  descr;
            descr = (ConfigurationEntryDescription)this.entryDescrMap.get(entryName);
            if (descr == null) {
                report.addError(
                            "No entry description available for entry '"
                            + entryName + "'");
                continue;  // continue with next entry
            }

            // get the values (we'll always have a SparseArray)
            SparseArray sa = (SparseArray)this.configValues.get(entryName);
            if (sa == null) continue;

            // check each value
            Iterator it = sa.asList().iterator();
            int slot = 0;
            int cardinality = 0;
            while(it.hasNext()) {
                slot++;
                Object obj = it.next();

                if (obj == null) {
                    report.addError(
                                "Entry '" +  entryName + "'"
                                + " has a value hole at slot " + (slot-1));
                    continue;   // continue with next value
                }

                cardinality++;

                // check object type
                if (!descr.getType().isInstance(obj)) {
                    report.addError(
                                "Entry '" +  entryName + "'"
                                + " has an invalid value type '"
                                + obj.getClass() + "' at slot " + (slot-1));
                    continue;
                }

                // check valueSet
                Set valueSet = descr.getValueSet();
                if (!valueSet.isEmpty()) {
                    if (!valueSet.contains(obj)) {
                        report.addError(
                                    "Entry '" +  entryName + "'"
                                    + " has an invalid value '"
                                    + obj + "' at slot " + (slot-1)
                                    + " Valid values are: " + valueSet);
                        continue;
                    }
                }
            }

            // check cardinality
            if (!isCardinalityValid(cardinality, descr)) {
                report.addError(
                            "Entry '" +  entryName + "' has an invalid"
                            + " cardinality of " + cardinality + ". Required is"
                            + " " + descr.getStringifiedCardinality());
            }
        }

        // check if each all entries are available
        iter = entryDescrMap.values().iterator();
        while(iter.hasNext()) {
            ConfigurationEntryDescription  descr;

            descr = (ConfigurationEntryDescription)iter.next();
            if (!entryExists(descr.getName())) {
                if (descr.getDefaultValues() == null) {
                    // mandatory
                    report.addError(
                                "Entry '" +  descr.getName() + "' does"
                                + " not exist. This entry is mandatory.");
                }else{
                    // optional
                    report.addError(
                                "Entry '" +  descr.getName() + "' does"
                                + " not exist. This entry is optional."
                                + " Probably the defaults weren't applied.");
                }
            }
        }

        return report;
    }


    /**
     * Returns a multiline, formatted string representation of the
     * configuration.
     *
     * <p>Produces a multiline string following the sample:
     * <pre>
     *       CfgName: Test                  <-- config name
     *       Version: 1.0                   <-- version (major,minor)
     *       port=9100                      <-- single-valued
     *       filter[0]=Filter01             <-- multi-valued (3 values)
     *       filter[1]=Filter02                   :
     *       filter[2]=Filter03                   :
     * </pre>
     *
     * @return A formatted string
     */
    public String toString()
    {
        final String  EOL = "\n";
        StringBuilder buf = new StringBuilder(
        ).append(
            "CfgName: "
        ).append(
            this.name
        ).append(
            EOL
        ).append(
            "Version: "
        ).append(
            this.majorVersion
        ).append(
            "."
        ).append(
            this.minorVersion
        );

        Iterator iter = getEntryNames().iterator();
        while(iter.hasNext()) {
            buf.append(EOL);
            String name = (String)iter.next();

            SparseArray sa = (SparseArray)this.configValues.get(name);
            if ((sa == null) || sa.isEmpty()) {
                buf.append(
                    name
                ).append(
                    "="
                );
                continue;
            }

            if (sa.size() == 1) {
                buf.append(
                    name
                ).append(
                    "="
                ).append(
                    sa.get(0)
                );
            }else{
                for(int ii=0; ii<sa.size(); ii++) {
                    buf.append(
                        name
                    ).append(
                        "["
                    ).append(
                        ii
                    ).append(
                        "]="
                    ).append(
                        sa.get(ii)
                    );
                    if (ii<(sa.size()-1)) buf.append(EOL);
                }
            }
        }
        return buf.toString();
    }


    /**
     * Adds configuration entry descriptions to the existing entry descriptions
     *
     * @param entryDescriptions The entry descriptions
     * @throws IllegalArgumentException if one of the specified entry
     * descriptions already exists.
     */
    protected void addEntryDescription(
            ConfigurationEntryDescription[] entryDescriptions)
        throws IllegalArgumentException
    {
        for(int ii=0; ii<entryDescriptions.length; ii++) {
            String  name = entryDescriptions[ii].getName();
            if (this.entryDescrMap.get(name) != null) {
                throw new IllegalArgumentException(
                    "Configuration entries cannot overridden. The " +
                    "configuration entry '" + name + "' " +
                    "already exists in the configuration '" + this.name + "'");
            }else{
                this.entryDescrMap.put(name, entryDescriptions[ii]);
            }
        }
    }


    /**
     * Returns the values of a configuration entry.
     *
     * @param name  A string specifying the name of the configuration entry
     *
     * @return  A configuration entry value. Returns null if the entry does not
     *           exist or if the entry has no values.
     */
    private SparseArray getRawEntry(String  name)
    {
        if (!isValidName(name)) return null;

        SparseArray  sa = (SparseArray)this.configValues.get(name);
        if (sa == null) return null;
        if (sa.size() == 0) return null;

        return sa;
    }


    /**
     * Retrieves a trailing array index contained from configuration entry name
     *
     * @param name  A string specifying the name of the configuration entry
     *
     * @return   The index or -1 if no index has been supplied
     *
     * @throws IndexOutOfBoundsException if the specified array index is
     *         negative.
     * @throws IllegalArgumentException if the specified array index suffix
     *         has an invalid format.
     */
    private int getArrayIndex(String  name)
        throws IndexOutOfBoundsException,
                IllegalArgumentException
    {
        int posS   = name.lastIndexOf("[");
        int posE   = name.lastIndexOf("]");
        int length = name.length();


        if ((posS == -1) && (posE == -1)) {
            return -1;  // no array index specifier
        }

        // must end with ']'
        if (posE != (length-1)) {
            return -1;
        }

        // must have a '['
        if (posS == -1) {
            throw new IllegalArgumentException(
                    "Illegal configuration entry name array index specifier. " +
                    "Entry name: '" + name + "'");
        }


        String sIdx = name.substring(posS+1, length-1);
        int    idx;
        try {
            idx = Integer.parseInt(sIdx);
        }catch(NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Illegal configuration entry name array index specifier. " +
                    "Entry name: '" + name + "'");
        }

        if (idx < 0) {
            throw new IndexOutOfBoundsException(
                    "A configuration entry name array index specifier must " +
                    "not be negative. Entry name: '" + name + "'");
        }

        return idx;
    }


    /**
     * Strips a trailing array index from the configuration entry name
     *
     * @param name  A string specifying the name of the configuration entry
     *
     * @return   The index or -1 if no index has been supplied
     */
    private String stripArrayIndex(String  name)
    {
        if (name.endsWith("]")) {
            int pos = name.lastIndexOf("[");

            return (pos == -1) ? name : name.substring(0, pos);
        }else{
            return name;
        }
    }


    /**
     * Checks if a configuration entry name is valid
     *
     * @param name  A string specifying the name of the configuration entry
     *
     * @return   true if valid
     */
    private boolean isValidName(String  name)
    {
        return ((name != null) && (name.length() > 0));
    }


    /**
     * Initializes a configuration. Does not change the configuration name
     * and the version.
     */
    private void init()
    {
        this.configValues = new HashMap();
    }


    /**
     * Checks the cardinality
     *
     * @param cardinality  A cardinalityof a configuration entry
     * @param descr        The meta data of the configuration entry
     * @return true if the cardinality is ok
     */
    private boolean isCardinalityValid(
        int cardinality, ConfigurationEntryDescription  descr)
    {
        if (descr.getMinOccurance() != 0) {
            if (cardinality < descr.getMinOccurance()) return false;
        }

        if (descr.getMaxOccurance() != 0) {
            if (cardinality > descr.getMaxOccurance()) return false;
        }

        return true;
    }




    /** The configuration entry meta data */
    private HashMap entryDescrMap = new HashMap();

    /** The config entry data -> TODO put in one */
    private HashMap   configValues;

    /** The configuration name */
    private String name;

    /** The configuration version */
    private int majorVersion;
    private int minorVersion;

}

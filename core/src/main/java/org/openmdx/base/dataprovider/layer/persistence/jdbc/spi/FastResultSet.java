/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: FastResultSet
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2019, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.dataprovider.layer.persistence.jdbc.spi;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmdx.base.dataprovider.layer.persistence.jdbc.oracle.Datums;


/**
 * FastResultSet
 *
 * Optimizes the access to a ResultSet and guarantees that a column is read
 * at most once. This is required because for certain jdbc drivers (e.g. 
 * JdbcOdbc driver in .NET) a) rs.getObject() is very slow and b) may be
 * called at most once.
 */
public class FastResultSet {

    /**
     * Constructor 
     *
     * @param database
     * @param rs
     * @throws SQLException
     */
    public FastResultSet(
        Database_1_0 database,
        ResultSet rs
    ) throws SQLException {
        this.database = database;
        this.rs = rs;
        this.columnNames = FastResultSet.getColumnNames(
            rs.getMetaData()
        );
    }

    /**
     * Constructor 
     *
     * @param database
     * @param rs
     * @param columnNames
     * @throws SQLException
     */
    public FastResultSet(
    	Database_1_0 database,
        ResultSet rs,
        List<String> columnNames
    ) throws SQLException {
        this.database = database;
        this.rs = rs;
        this.columnNames = columnNames;
    }

    /**
     * Prepare column names from meta data.
     * 
     * @param rsmd
     * @return
     * @throws SQLException
     */
    static public List<String> getColumnNames(
        ResultSetMetaData rsmd
    ) throws SQLException {
        List<String> columnNames = new ArrayList<String>(rsmd.getColumnCount());
        for(
            int columnIndex = 1, columnCount = rsmd.getColumnCount(); 
            columnIndex <= columnCount; 
            columnIndex++
        ) {
            columnNames.add(
                rsmd.getColumnName(columnIndex).toLowerCase()
            );
        }
        return columnNames;
    }

    /**
     * Reads specified column from result set. Guarantees that columns 
     * OBJECT_OID, OBJECT_RID and OBJECT_IDX are read at most once otherwise
     * error 'ResultSet can not re-read row data for column' is thrown by
     * certain JDBC drivers.
     * 
     * @param columnName
     * @return
     * @throws SQLException
     */
    public Object getObject(
        String columnName
    ) throws SQLException {
        int index = this.columnNames.indexOf(columnName.toLowerCase());
        if(index < 0) {
            throw new SQLException("unkown column name " + columnName);
        }
        return this.getObject(index);
    }
  
    /**
     * Get column value at given index.
     * 
     * @param index the first column has the index 0.
     * @return
     * @throws SQLException
     */
    public Object getObject(
        int index
    ) throws SQLException {
        if(this.columnValues.isEmpty()) {
            for(int i = 0; i < this.columnNames.size(); i++) {
                this.columnValues.add(
                    toJdbcObject(
                        this.rs.getObject(i + 1)
                    )
                );
            }
        }
        Object value = this.columnValues.get(index);
        if(value instanceof String) {
            return this.database.internalizeStringValue(
                this.getColumnNames().get(index),
                (String)value
            );
        } else {
            return value;
        }
    }

    /**
     * Reset column values.
     * 
     * @throws SQLException
     */
    private void reset(
    ) throws SQLException {
        this.columnValues.clear();
    }

    /**
     * Skip to next row.
     * 
     * @return
     * @throws SQLException
     */
    public boolean next(
    ) throws SQLException {
        boolean hasMore = this.rs.next();
        this.reset();
        return hasMore;
    }

    public boolean isAbsolutePositioningEnabled(
    ) throws SQLException{
    	return this.rs.getType() != ResultSet.TYPE_FORWARD_ONLY;
    }
    
    /**
     * Absolute positioning.
     * 
     * @param position
     * @return
     * @throws SQLException
     */
    public boolean absolute(
        int position
    ) throws SQLException {
        boolean hasMore = this.rs.absolute(position);
        this.reset();
        return hasMore;
    }

    /**
     * Get column names.
     * 
     * @return
     */
    public List<String> getColumnNames(
    ) {
        return this.columnNames;
    }

    /**
     * Convert to jdbc object.
     * 
     * @param nativeObject
     * @return
     * @throws SQLException
     */
    protected Object toJdbcObject(
        Object nativeObject
    ) throws SQLException{
        return Datums.isDatum(nativeObject) ? Datums.toJdbcObject(nativeObject) : nativeObject;
    }

    //-----------------------------------------------------------------------
    // Variables
    //-----------------------------------------------------------------------
    private final ResultSet rs;
    private final Database_1_0 database;
    private final List<Object> columnValues = new ArrayList<Object>();
    private List<String> columnNames;

}

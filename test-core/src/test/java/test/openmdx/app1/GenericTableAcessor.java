/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Generic Table Accessor 
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
package test.openmdx.app1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.openmdx.kernel.collection.TreeSparseArray;
import org.w3c.cci2.SparseArray;
import org.w3c.format.DateTimeFormat;

/**
 * Generic Table Accessor
 */
class GenericTableAcessor implements AccessorToAnotherDatabase {

    /**
     * Constructor 
     */
    GenericTableAcessor(
    ){
        enabled = System.getProperty("org.openmdx.comp.env.jdbc.DataSource2") != null;
        oid = DateTimeFormat.BASIC_UTC_FORMAT.format(#if CLASSIC_CHRONO_TYPES new java.util.Date() #else java.time.Instant.now()#endif));
    }
    
    private final boolean enabled;
    private final static String rid = "CR20019671";
    private final String oid;
    
    private DataSource dataSource;
    private final static String INSERT_STATEMENT = "INSERT INTO VTR_GENERIC (" +
    		"OBJECT_RID, OBJECT_OID, OBJECT_IDX, OBJECT_NAME, OBJECT_VAL_STRING" +
    ") VALUES (?,?,?,?,?)";

    private final static String QUERY_STATEMENT = "SELECT OBJECT_IDX, OBJECT_NAME, OBJECT_VAL_STRING FROM VTR_GENERIC " +
            "WHERE OBJECT_RID = ? AND OBJECT_OID = ?";
    
    public boolean isEnabled(){
        return this.enabled;
    }
    
    Connection getConnection(
    ) throws SQLException{
        if(dataSource == null) try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/DataSource2");
        } catch (NamingException exception) {
            throw (SQLException)new SQLException("DataSource lookup failure").initCause(exception);
        }
        return dataSource.getConnection();
    }
    
    /* (non-Javadoc)
     * @see test.openmdx.app1.AccessorToAnotherDatabase#insert(java.lang.String, int, java.lang.String)
     */
    public int insert(
        String name,
        int idx,
        String value
    ) throws SQLException{
        if(isEnabled()) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement(INSERT_STATEMENT);
                statement.setString(1, rid);
                statement.setString(2, oid);
                statement.setInt(3, idx);
                statement.setString(4, name);
                statement.setString(5, value);
                return statement.executeUpdate();
            }
        } else {
            return 0;
        }
    }
    
    /* (non-Javadoc)
     * @see test.openmdx.app1.AccessorToAnotherDatabase#retrieve()
     */
    public SparseArray<String> retrieve(
    ) throws SQLException {
        final SparseArray<String> reply = new TreeSparseArray<String>();
        if(isEnabled()) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement(QUERY_STATEMENT);
                statement.setString(1, rid);
                statement.setString(2, oid);
                ResultSet resultSet = statement.executeQuery();
                while(resultSet.next()) {
                    reply.put(
                        resultSet.getInt("OBJECT_IDX"),
                        resultSet.getString("OBJECT_VAL_STRING")
                    );
                }
            }
        }
        return reply;
    }

}



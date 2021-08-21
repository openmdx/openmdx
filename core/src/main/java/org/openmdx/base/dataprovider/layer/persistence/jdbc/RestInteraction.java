/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Rest Interaction 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2017, OMEX AG, Switzerland
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc;

import static org.openmdx.base.accessor.cci.SystemAttributes.OBJECT_INSTANCE_OF;
import static org.openmdx.base.naming.SpecialResourceIdentifiers.EXTENT_REFERENCES;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.jdo.FetchPlan;
import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.AttributeSpecifier;
import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.rest.spi.LockAssertions;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.Database_2.QueryContext;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.DbObject;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.DbObjectConfiguration;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_2_0;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Target;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ConsumerRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.w3c.format.DateTimeFormat;

/**
 * Rest Interaction
 */
@SuppressWarnings("resource")
public class RestInteraction extends AbstractRestInteraction {

    public RestInteraction(
        Database_2 database,
        RestConnection connection
    )
        throws ResourceException {
        super(
            connection,
            database.newDelegateInteraction(connection));
        this.database = database;
    }

    private final Database_2_0 database;

    private Connection getConnection(
        RestInteractionSpec ispec,
        QueryRecord request
    )
        throws ServiceException {
        try {
            return database.getConnection(ispec, request);
        } catch (SQLException exception) {
            throw new ServiceException(exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#get(org
     * .openmdx.base.resource.spi.RestInteractionSpec,
     * org.openmdx.base.rest.spi.Query_2Facade,
     * javax.resource.cci.IndexedRecord)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean get(
        RestInteractionSpec ispec,
        QueryRecord request,
        ResultRecord result
    ) throws ResourceException {
        final long startTime = System.nanoTime();
        SysLog.detail("> get", request);
        try (Connection conn = getConnection(ispec, request)) {
            SingletonTarget target = new SingletonTarget(database);
            final short attributeSelector = AttributeSelectors.getAttributeSelector(request);
            // Attribute specifiers are ignored except in case of attributeSelector==SPECIFIED_AND_SYSTEM_ATTRIBUTES.
            // Save the possibly expensive call to attributeSpecifierAsMap
            final Map<String, AttributeSpecifier> attributeSpecifier = 
                attributeSelector == AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES
                || attributeSelector == AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES ?
                AttributeSpecifier.getAttributeSpecifierAsMap(request.getQueryFilter()) :
                Collections.<String, AttributeSpecifier>emptyMap();
            database.get(
                conn,
                ispec,
                request.getResourceIdentifier(),
                attributeSelector,
                attributeSpecifier,
                false, // objectClassAsAttribute
                target,
                false // isPreferringNotFoundException
            );
            if(target.isSaturated()) {
                result.add(target.getSingleton());
            }
            return target.isSaturated();
        } catch (Exception exception) {
            throw ResourceExceptions.toResourceException(exception);
        } finally {
            SysLog.detail("< get", System.nanoTime() - startTime + " ns");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.base.rest.spi.AbstractRestInteraction#consume(org.openmdx.
     * base.resource.spi.RestInteractionSpec,
     * org.openmdx.base.rest.cci.QueryRecord,
     * org.openmdx.base.rest.cci.ConsumerRecord)
     */
    @Override
    public boolean consume(
        RestInteractionSpec ispec,
        QueryRecord request,
        ConsumerRecord result
    ) throws ResourceException {
        final long startTime = System.nanoTime();
        SysLog.detail("> consume", request);
        final Target target = new ConsumerTarget(
            database,
            result
        );
        final boolean reply = DatabasePreferences.isConfigurationRequest(request.getResourceIdentifier()) ? 
            retrieveDatabaseConfiguration(request, target) : 
            retrieveDatabaseContent(ispec, request, target);
        SysLog.detail("< consume", System.nanoTime() - startTime + " ns");
        return reply;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#find(
     * org.openmdx.base.resource.spi.RestInteractionSpec,
     * org.openmdx.base.rest.spi.Query_2Facade,
     * javax.resource.cci.IndexedRecord)
     */
    @Override
    public boolean find(
        RestInteractionSpec ispec,
        QueryRecord request,
        ResultRecord result
    ) throws ResourceException {
        final long startTime = System.nanoTime();
        SysLog.detail("> find", request);
        final Target target = new ResultTarget(
            database,
            request,
            result
        );
        final boolean reply = DatabasePreferences.isConfigurationRequest(request.getResourceIdentifier()) ? 
            retrieveDatabaseConfiguration(request, target) : 
            retrieveDatabaseContent(ispec, request, target);
        SysLog.detail("< find", System.nanoTime() - startTime + " ns");
        return reply;
    }

    @SuppressWarnings("unchecked")
    private boolean retrieveDatabaseContent(
        RestInteractionSpec ispec,
        QueryRecord request,
        Target target
    ) throws ResourceException {
        final Model_1_0 model = Model_1Factory.getModel();
        String currentStatement = null;
        List<Object> currentStatementParameters = null;
        String statement = null;
        List<Object> statementParameters = null;
        boolean countResultSet = false;
        try (Connection conn = database.getConnection(ispec, request)) {
            final short attributeSelector = AttributeSelectors.getAttributeSelector(request);
            final List<FilterProperty> attributeFilter = FilterProperty.getFilterProperties(request.getQueryFilter());
            final List<AttributeSpecifier> attributeSpecifiers = AttributeSpecifier.getAttributeSpecifiers(request
                .getQueryFilter());
            final Map<String, AttributeSpecifier> attributeSpecifiersAsMap = AttributeSpecifier.getAttributeSpecifierAsMap(
                request.getQueryFilter());
            final DbObject dbObject;
            /**
             * prepare SELECT statement
             */
            SysLog.trace("ITERATION_START");
            try {
                dbObject = database.getDbObject(
                    conn,
                    request.getResourceIdentifier(),
                    attributeFilter,
                    true);
            } catch (ServiceException exception) {
                if (exception.getCause().getExceptionCode() != BasicException.Code.NOT_FOUND) {
                    throw exception;
                }
                SysLog.info(
                    "Could not create dbObject",
                    new IndentingFormatter(
                        ArraysExtension.asMap(
                            new String[] {
                                "reason",
                                "request path",
                                "filter"
                            },
                            new Object[] {
                                exception.getMessage(),
                                request.getResourceIdentifier(),
                                attributeFilter
                            })));
                exception.log();
                target.close(0L);
                SysLog.detail("< find");
                return true;
            }
            // Collect query extension
            Map<String, QueryExtension> queryExtensions = new HashMap<String, QueryExtension>();
            for (FilterProperty p : attributeFilter) {
                if (p.name().startsWith(SystemAttributes.CONTEXT_PREFIX) &&
                    p.name().endsWith(SystemAttributes.OBJECT_CLASS) &&
                    Database_1_Attributes.QUERY_EXTENSION_CLASS.equals(p.getValue(0))) {
                    String id = p.name().substring(0, p.name().indexOf(SystemAttributes.OBJECT_CLASS));
                    queryExtensions.put(
                        id,
                        new QueryExtension(id));
                }
            }
            // Get attribute and query filter. The query filter is passed as
            // FilterProperty with context prefix QUERY_FILTER_CONTEXT
            List<FilterProperty> filterProperties = new ArrayList<FilterProperty>();
            String columnSelector = Database_2.DEFAULT_COLUMN_SELECTOR;
            boolean stated = false;
            for (FilterProperty p : attributeFilter) {
                // Test for query extension
                QueryExtension queryExtension = null;
                for (String id : queryExtensions.keySet()) {
                    if (p.name().startsWith(id)) {
                        queryExtension = queryExtensions.get(id);
                        break;
                    }
                }
                // The filter property 'identity' requires special handling. It
                // is mapped to the filter property 'object_oid operator values'
                // This mapping is not required in case of an extent search because
                // dbObject is already correctly prepared
                if (SystemAttributes.OBJECT_IDENTITY.equals(p.name())) {
                    if (!request.getResourceIdentifier().isLike(EXTENT_REFERENCES)) {
                        filterProperties.add(
                            dbObject.mapToIdentityFilterProperty(p));
                    }
                } else if (queryExtension != null) {
                    // Query extension clause
                    if (p.name().endsWith(Database_1_Attributes.QUERY_EXTENSION_CLAUSE)) {
                        String clause = (String) p.getValue(0);
                        countResultSet |= clause.indexOf(Database_1_Attributes.HINT_COUNT) >= 0;
                        {
                            // !COLUMNS
                            int start = clause.indexOf(Database_1_Attributes.HINT_COLUMN_SELECTOR);
                            if (start >= 0) {
                                int end = clause.indexOf("*/", start);
                                columnSelector = clause.substring(
                                    start + Database_1_Attributes.HINT_COLUMN_SELECTOR.length(),
                                    end);
                                clause = clause.substring(0, start) +
                                    clause.substring(end + 2);
                            }
                        }
                        // !ORDER BY
                        {
                            int start = clause.indexOf(Database_1_Attributes.HINT_ORDER_BY);
                            if (start >= 0) {
                                int end = clause.indexOf("*/", start);
                                String orderByClause = clause.substring(
                                    start + Database_1_Attributes.HINT_ORDER_BY.length(),
                                    end);
                                StringTokenizer tokenizer = new StringTokenizer(orderByClause, ",", false);
                                while (tokenizer.hasMoreTokens()) {
                                    String orderByAttribute = tokenizer.nextToken().trim();
                                    SortOrder sortOrder = SortOrder.UNSORTED;
                                    if (orderByAttribute.endsWith("ASC") || orderByAttribute.endsWith("asc")) {
                                        sortOrder = SortOrder.ASCENDING;
                                        orderByAttribute = orderByAttribute.substring(0, orderByAttribute.length() - 3)
                                            .trim();
                                    } else if (orderByAttribute.endsWith("DESC") || orderByAttribute.endsWith("desc")) {
                                        sortOrder = SortOrder.DESCENDING;
                                        orderByAttribute = orderByAttribute.substring(0, orderByAttribute.length() - 4)
                                            .trim();
                                    }
                                    attributeSpecifiers.add(
                                        new AttributeSpecifier(
                                            orderByAttribute,
                                            sortOrder.code()));
                                }
                                clause = clause.substring(0, start) +
                                    clause.substring(end + 2);
                            }
                        }
                        queryExtension.setClause(clause);
                    } else {
                        // Query extension parameters
                        String paramName = p.name().substring(queryExtension.getId().length());
                        queryExtension.putParams(
                            paramName,
                            Arrays.asList(p.getValues()));
                    }
                } else if (OBJECT_INSTANCE_OF.equals(p.name())) {
                    if ((p.operator() == ConditionType.IS_IN.code()) &&
                        (p.quantor() == Quantifier.THERE_EXISTS.code())) {
                        FilterProperty mappedFilterProperty = database.mapInstanceOfFilterProperty(
                            request,
                            (Collection<String>) ((Collection<?>) Arrays.asList(p.getValues())));
                        if (mappedFilterProperty != null) {
                            for (Object superClass : mappedFilterProperty.values()) {
                                if (stated)
                                    break;
                                stated = BasicStates.isStated(superClass);
                            }
                            filterProperties.add(mappedFilterProperty);
                        }
                    } else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE,
                            "Property " + OBJECT_INSTANCE_OF + " only accepts condition " + ConditionType.IS_IN
                                + " and quantor " + Quantifier.THERE_EXISTS,
                            new BasicException.Parameter("ispec", ispec),
                            new BasicException.Parameter("input", request));
                    }
                } else {
                    // Attribute
                    filterProperties.add(p);
                }
            }
            // Mixins
            Set<String> mixins = new HashSet<String>();
            // ORDER BY attributes
            for (AttributeSpecifier attributeSpecifier : attributeSpecifiers) {
                // Add to orderBy set unless the order is UNSORTED or is an expression
                if (attributeSpecifier.order() != SortOrder.UNSORTED.code() &&
                    !attributeSpecifier.name().startsWith("(")) {
                    String attributeName = attributeSpecifier.name();
                    mixins.add(attributeName);
                }
            }
            // Prepare filter properties stored in primary dbObject
            ModelElement_1_0 referencedTypeDef = model.getTypes(dbObject.getReference())[2];
            List<FilterProperty> primaryFilterProperties = database.getPrimaryFilterProperties(
                referencedTypeDef,
                filterProperties);
            // Add primary filter properties to mixins
            List<ModelElement_1_0> primaryFilterPropertyDefs = database.getFilterPropertyDefs(
                referencedTypeDef,
                primaryFilterProperties);
            for (int i = 0; i < primaryFilterProperties.size(); i++) {
                FilterProperty filterProperty = primaryFilterProperties.get(i);
                ModelElement_1_0 filterPropertyDef = primaryFilterPropertyDefs.get(i);
                if ((filterPropertyDef == null) ||
                    !filterPropertyDef.isReferenceType() ||
                    model.referenceIsStoredAsAttribute(filterPropertyDef)) {
                    mixins.add(filterProperty.name());
                }
            }
            // View returning primary attributes. Allows sorting and
            // filtering with single-valued filter properties
            String dbObjectHint = null;
            for (QueryExtension queryExtension : queryExtensions.values()) {
                String clause = queryExtension.getClause();
                int posDbObjectHint = clause.indexOf(Database_1_Attributes.HINT_DBOBJECT);
                if (posDbObjectHint >= 0) {
                    dbObjectHint = clause.substring(
                        posDbObjectHint + Database_1_Attributes.HINT_DBOBJECT.length(),
                        clause.indexOf("*/", posDbObjectHint));
                    break;
                }
            }
            String view1WithMixinAttributes = database.getView(
                conn,
                dbObject,
                dbObjectHint,
                Database_2.VIEW_MODE_ADD_MIXIN_COLUMNS_TO_PRIMARY,
                columnSelector,
                mixins);
            // View returning multi-valued columns which allows filtering
            // of multi-valued filter properties
            String view2ForQuery = database.getView(
                conn,
                dbObject,
                dbObjectHint,
                Database_2.VIEW_MODE_SECONDARY_COLUMNS,
                columnSelector,
                null);
            List<String> includingClauses = new ArrayList<String>();
            List<List<Object>> includingClausesValues = new ArrayList<List<Object>>();
            List<String> exludingClauses = new ArrayList<String>();
            List<List<Object>> excludingClausesValues = new ArrayList<List<Object>>();
            String joinColumn = "v." + 
                (dbObject.getConfiguration().getDbObjectsForQueryJoinColumn() == null
                    ? dbObject.getObjectIdColumn().get(0)
                    : dbObject.getConfiguration().getDbObjectsForQueryJoinColumn().replace("${v2}.", "vv.").replace("${v}.", "v.")
                );
            ModelElement_1_0 referencedType = model.getTypes(dbObject.getReference())[2];
            // fetchSize
            final int fetchSize = target.getFetchSize();
            final int maxRows = fetchSize == FetchPlan.FETCH_SIZE_GREEDY || fetchSize == Integer.MAX_VALUE
                ? 0
                : Math.max(0, target.getStartPosition() + fetchSize + 1);
            // context is populated by custom-specific sub-classes of the database plug-in
            // QueryContext defines pre-defined context properties
            Map<String,Object> context = new HashMap<String,Object>();
            context.put(QueryContext.MAX_ROWS.name(), maxRows);
            this.database.filterToSqlClauses(
                conn,
                dbObject,
                stated,
                "v",
                view1WithMixinAttributes,
                view2ForQuery,
                Database_2_0.JoinType.SPECIFIED_COLUMN_WITH_OBJECT_ID,
                joinColumn,
                false, // stickyViewAlias
                referencedType,
                filterProperties,
                primaryFilterProperties,
                includingClauses,
                includingClausesValues,
                exludingClauses,
                excludingClausesValues,
                context
            );
            /**
             * get all slices of objects which match the reference and attribute
             * filter
             */
            statement = "";
            statementParameters = new ArrayList<Object>();
            statement += view1WithMixinAttributes.startsWith("SELECT") ? view1WithMixinAttributes + " AND " + dbObject
                .getReferenceClause()
                : "SELECT " + dbObject.getHint() + " " + columnSelector + " FROM " + view1WithMixinAttributes
                    + " v WHERE " + dbObject.getReferenceClause();
            statementParameters.addAll(
                dbObject.getReferenceValues());
            // Add clause if object id is a pattern
            if (dbObject.getObjectIdClause().indexOf("LIKE") >= 0) {
                statement += " AND " + dbObject.getObjectIdClause();
                statementParameters.addAll(
                    dbObject.getObjectIdValues());
            }
            // Query extensions
            for (QueryExtension queryExtension : queryExtensions.values()) {
                String clause = queryExtension.getClause();
                int pos = 0;
                while ((pos < clause.length()) &&
                    ((pos = clause.indexOf("?", pos)) >= 0)) {
                    int placeHolderEndPos = pos + 2;
                    while (placeHolderEndPos < clause.length() && Character.isDigit(clause.charAt(placeHolderEndPos))) {
                        placeHolderEndPos++;
                    }
                    int index = Integer.parseInt(clause.substring(pos + 2, placeHolderEndPos));
                    if (clause.startsWith("?s", pos)) {
                        statementParameters.add(
                            database.getSqlWildcards().fromJDO(((String) queryExtension.getParams("stringParam").get(
                                index))));
                    } else if (clause.startsWith("?i", pos)) {
                        statementParameters.add(
                            queryExtension.getParams("integerParam").get(index));
                    } else if (clause.startsWith("?n", pos)) {
                        statementParameters.add(
                            queryExtension.getParams("decimalParam").get(index));
                    } else if (clause.startsWith("?b", pos)) {
                        statementParameters.add(
                            queryExtension.getParams("booleanParam").get(index));
                    } else if (clause.startsWith("?d", pos)) {
                        // TODO CR20019719 verify whether replacement is done
                        statementParameters.add(
                            queryExtension.getParams("dateParam").get(index));
                    } else if (clause.startsWith("?t", pos)) {
                        statementParameters.add(
                            queryExtension.getParams("dateTimeParam").get(index));
                    }
                    pos++;
                }
                statement += " AND (";
                statement += clause.replaceAll("(\\?[sinbdt]\\d+)", "?");
                statement += ")";
                String databaseProductName = conn.getMetaData().getDatabaseProductName();
                if (databaseProductName.startsWith("DB2")) {
                    statement = statement.replace("LIKE UPPER(?)", "LIKE ?");
                }
            }
            // Positive attribute filter
            for (int i = 0; i < includingClauses.size(); i++) {
                String filterClause = includingClauses.get(i);
                if (filterClause.length() > 0) {
                    statement += " AND ";
                    statement += filterClause;
                    statementParameters.addAll(
                        includingClausesValues.get(i));
                }
            }
            // Negative attribute filter
            ExcludingClauses: for (String exludingClause : exludingClauses) {
                if (exludingClause.length() > 0) {
                    for (int i = 0; i < exludingClauses.size(); i++) {
                        String filterClause = exludingClauses.get(i);
                        if (filterClause.length() > 0) {
                            statement += " AND ";
                            statement += filterClause;
                            statementParameters.addAll(
                                excludingClausesValues.get(i));
                        }
                    }
                    break ExcludingClauses;
                }
            }
            // ORDER BY
            boolean hasOrderBy = false;
            for (AttributeSpecifier specifier : attributeSpecifiers) {
                // only add to ORDER set if specified order
                if (specifier.order() != SortOrder.UNSORTED.code()) {
                    if (!hasOrderBy) {
                        statement += " ORDER BY";
                    }
                    boolean viewIsIndexed = dbObject.getIndexColumn() != null;
                    statement += hasOrderBy ? ", " : " ";
                    if (specifier.name().startsWith("(")) {
                        statement += specifier.name() +
                            (specifier.order() == SortOrder.DESCENDING.code()
                                ? " DESC" + (database.isOrderNullsAsEmpty() ? " NULLS LAST" : "")
                                : " ASC" + (database.isOrderNullsAsEmpty() ? " NULLS FIRST" : ""));
                    } else {
                        // order on mixin view (vm.) in case of indexed slices, otherwise on primary view (v.)
                        statement += (viewIsIndexed ? "vm." : "v.") +
                            database.getColumnName(conn, specifier.name(), 0, false, true, false) +
                            (specifier.order() == SortOrder.DESCENDING.code()
                                ? " DESC" + (database.isOrderNullsAsEmpty() ? " NULLS LAST" : "")
                                : " ASC" + (database.isOrderNullsAsEmpty() ? " NULLS FIRST" : ""));
                    }
                    hasOrderBy = true;
                }
            }
            switch (database.getOrderAmendment(conn, dbObject)) {
                case BY_OBJECT_ID:
                    // ORDER BY object identity is required. Otherwise, iteration
                    // may not be deterministic. 
                    if (!hasOrderBy)
                        statement += " ORDER BY";
                    // rid
                    for (String referenceColumn : dbObject.getReferenceColumn()) {
                        statement += hasOrderBy ? ", " : " ";
                        statement += "v." + referenceColumn;
                        hasOrderBy = true;
                    }
                    // oid
                    for (Object objectIdColumn : dbObject.getObjectIdColumn()) {
                        statement += hasOrderBy ? ", " : " ";
                        statement += "v." + objectIdColumn;
                        hasOrderBy = true;
                    }
                    // idx
                    if (dbObject.getIndexColumn() != null) {
                        statement += hasOrderBy ? ", " : " ";
                        statement += "v." + dbObject.getIndexColumn();
                        hasOrderBy = true;
                    }
                    break;
                case INTRINSIC:
                    // The database's intrinsic ordering is (relatively) stable
                    // (compared to the probability of concurrent modifications)
                    break;
            }
            final boolean hasMore;
            // INJECT_STATEMENT
            String statement2 = statement;
            if(context.containsKey(QueryContext.INJECT_STATEMENT.name())) {
            	statement2 = context.get(QueryContext.INJECT_STATEMENT.name()) + statement2;
            }
            // INJECT_STATEMENT_PARAMS
            List<Object> statementParameters2 = new ArrayList<Object>(statementParameters);
            if(context.containsKey(QueryContext.INJECT_STATEMENT_PARAMS.name())) {
            	statementParameters2.addAll(0, (List<?>)context.get(QueryContext.INJECT_STATEMENT_PARAMS.name()));
            }
            try (
            	// Prepare and ...
                final PreparedStatement ps = database.prepareStatement(
                    conn,
                    currentStatement = statement2
                )
            ) {
                try {
                    // ... fill in statement parameters ...
                    currentStatementParameters = statementParameters2;
                    for(int i = 0, iLimit = currentStatementParameters.size(); i < iLimit; i++) {
                        database.setPreparedStatementValue(
                            conn,
                            ps,
                            i + 1,
                            currentStatementParameters.get(i)
                        );
                    }
                } catch (ServiceException exception) {
                    throw new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.GENERIC,
                        "Can't propagate the parameters to the prepared statement",
                        new BasicException.Parameter("statement", currentStatement));
                }
                // ... and finally execute
                try (
                    ResultSet rs = database.executeQuery(
                        ps,
                        currentStatement,
                        currentStatementParameters,
                        maxRows
                    )
                ) {
                    // get selected objects
                    hasMore = database.getObjects(
                        conn,
                        dbObject,
                        rs,
                        new ArrayList<ObjectRecord>(database.getObjectBatchSize()),
                        attributeSelector,
                        attributeSpecifiersAsMap,
                        false, // objectClassAsAttribute
                        target.getStartPosition(),
                        target.getObjectBatchSize(),
                        null,
                        target
                    );
                    SysLog.log(
                        Level.FINE, "Sys|*** hasMore={0}|objects.size()={1}", 
                        Boolean.valueOf(hasMore), 
                        Integer.valueOf(target.count())
                    );
                }
            }
            // Calculate context.TOTAL only when iterating
            if(request.getResourceIdentifier().isContainerPath()) {
                if (!hasMore) {
                    target.close(hasMore);
                } else if (countResultSet && (dbObject.getIndexColumn() == null)) {
                    // Issue a SELECT COUNT(*) if the result set is not indexed and counting is requested
                    String countStatement = statement;
                    if(countStatement.startsWith("SELECT")) {
                        countStatement = "SELECT COUNT(*) " + countStatement.substring(countStatement.indexOf("FROM"));
                        if(countStatement.indexOf("ORDER BY") > 0) {
                            countStatement = countStatement.substring(0, countStatement.indexOf("ORDER BY"));
                        }
                        // INJECT_STATEMENT
                        String countStatement2 = countStatement;
                        if(context.containsKey(QueryContext.INJECT_STATEMENT.name())) {
                        	countStatement2 = context.get(QueryContext.INJECT_STATEMENT.name()) + countStatement2;
                        }
                        // INJECT_STATEMENT_PARAMS
                        List<Object> countStatementParameters2 = new ArrayList<Object>(statementParameters);
                        if(context.containsKey(QueryContext.INJECT_STATEMENT_PARAMS.name())) {
                        	countStatementParameters2.addAll(0, (List<?>)context.get(QueryContext.INJECT_STATEMENT_PARAMS.name()));
                        }
                        try (
                            PreparedStatement ps = database.prepareStatement(
                                conn,
                                currentStatement = countStatement2
                            )
                        ) {
                            currentStatementParameters = countStatementParameters2;
                            for(int i = 0, iLimit = currentStatementParameters.size(); i < iLimit; i++) {
                                database.setPreparedStatementValue(
                                    conn,
                                    ps,
                                    i + 1,
                                    currentStatementParameters.get(i));
                            }
                            try (
                                ResultSet rs = database.executeQuery(
                                    ps,
                                    currentStatement,
                                    currentStatementParameters,
                                    0 // no limit for maxRows
                                )
                            ) {
                                if(rs.next()) {
                                    target.close(rs.getInt(1));
                                } else {
                                    target.close(hasMore);
                                }
                            }
                        }
                    } else {
                        target.close(hasMore);
                    }
                } else {
                    target.close(hasMore);
                }
            } else {
                target.close(hasMore);
            }
        } catch (SQLException exception) {
            throw ResourceExceptions.toResourceException(
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE,
                    "Error when executing SQL statement",
                    new BasicException.Parameter(BasicException.Parameter.XRI, request.getResourceIdentifier()),
                    new BasicException.Parameter("statement", currentStatement),
                    new BasicException.Parameter("parameters", currentStatementParameters),
                    new BasicException.Parameter("sqlErrorCode", exception.getErrorCode()),
                    new BasicException.Parameter("sqlState", exception.getSQLState()))
                );
        } catch (Exception exception) {
            throw ResourceExceptions.toResourceException(exception);
        }
        return true;
    }

    private boolean retrieveDatabaseConfiguration(
        final QueryRecord request,
        Target target
    )
        throws ResourceException {
        try {
            final DatabaseConfiguration dbConfiguration = database.getDatabaseConfiguration();
            DatabasePreferences.retrieveDatabaseConfiguration(
                request,
                target,
                dbConfiguration.getDbObjectConfigurations(),
                dbConfiguration.getFromToColumnNameMapping(),
                database.getMacroConfiguration().getStringMacros());
        } catch (ServiceException exception) {
            throw ResourceExceptions.toResourceException(exception);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#create(
     * org.openmdx.base.resource.spi.RestInteractionSpec,
     * org.openmdx.base.rest.spi.Object_2Facade,
     * javax.resource.cci.IndexedRecord)
     */
    @Override
    public boolean create(
        RestInteractionSpec ispec,
        ObjectRecord request,
        ResultRecord reply
    ) throws ResourceException {
        SysLog.detail("> create", request);
        try (Connection conn = database.getConnection(ispec, request)){
            database.create(
                conn,
                ispec,
                request,
                reply
            );
        } catch (Exception exception) {
            throw ResourceExceptions.toResourceException(exception);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#delete(
     * org.openmdx.base.resource.spi.RestInteractionSpec,
     * org.openmdx.base.rest.spi.Object_2Facade,
     * javax.resource.cci.IndexedRecord)
     */
    @Override
    public boolean delete(
        RestInteractionSpec ispec,
        ObjectRecord request
    ) throws ResourceException {
        final long startTime = System.nanoTime();
        SysLog.detail("> remove", request);
        String currentStatement = null;
        List<Object> statementParameters = null;
        try (Connection conn = database.getConnection(ispec, request)) {
            final Path accessPath = request.getResourceIdentifier();
            // Does object exist?// Avoid completion
            final SingletonTarget target = new SingletonTarget(null); // Avoid completion
            database.get(
                conn,
                ispec,
                accessPath,
                AttributeSelectors.NO_ATTRIBUTES,
                Collections.<String, AttributeSpecifier>emptyMap(),
                false, // objectClassAsAttribute
                target,
                true // throwNotFoundException
            );
            // Remove object ...
            database.createDbObject(
                conn,
                accessPath,
                true
            ).remove();
            // ... and its composites
            Map<Path, DbObjectConfiguration> processedDbObjectConfigurations = new HashMap<Path, DbObjectConfiguration>();
            for(DbObjectConfiguration dbObjectConfiguration : database.getDatabaseConfiguration().getDbObjectConfigurations()) {
                if(
                    (dbObjectConfiguration.getType().size() > accessPath.size()) &&
                    accessPath.isLike(dbObjectConfiguration.getType().getPrefix(accessPath.size())) &&
                    !processedDbObjectConfigurations.containsKey(dbObjectConfiguration.getType()) &&
                    !this.database.getDatabaseConfiguration().getDbObjectConfigurations(accessPath.getDescendant(dbObjectConfiguration.getType().getSuffix(accessPath.size()))).isEmpty()
                ) {
                    boolean processed = false;
                    // Check whether dbObjectConfiguration is already processed
                    for(Iterator<DbObjectConfiguration> j = processedDbObjectConfigurations.values().iterator(); j.hasNext();) {
                        DbObjectConfiguration processedDbObjectConfiguration = j.next();
                        // dbObject is processed if type if 
                        // <ul>
                        //   <li>db object is composite to processed db object
                        //   <li>dbObjectForUpdate1 are equal
                        // </ul>
                        boolean dbObjectForUpdate1Matches =
                            (dbObjectConfiguration.getDbObjectForUpdate1() == null) ||
                            (processedDbObjectConfiguration.getDbObjectForUpdate1() == null)
                                ? dbObjectConfiguration.getDbObjectForUpdate1() == processedDbObjectConfiguration.getDbObjectForUpdate1()
                                : dbObjectConfiguration.getDbObjectForUpdate1().equals(processedDbObjectConfiguration.getDbObjectForUpdate1());
                        if(
                            dbObjectForUpdate1Matches &&
                            (dbObjectConfiguration.getType().size() > processedDbObjectConfiguration.getType().size()) &&
                            dbObjectConfiguration.getType().getPrefix(processedDbObjectConfiguration.getType().size()).isLike(processedDbObjectConfiguration.getType())
                        ) {
                            processed = true;
                            break;
                        }
                    }
                    // Remove if not processed
                    if (!processed) {
                        database.createDbObject(
                            conn,
                            dbObjectConfiguration,
                            request.getResourceIdentifier(),
                            true
                        ).remove();
                        processedDbObjectConfigurations.put(
                            dbObjectConfiguration.getType(),
                            dbObjectConfiguration
                        );
                    }
                }
            }
        } catch (SQLException exception) {
            throw ResourceExceptions.toResourceException(
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE,
                    "Error when executing SQL statement",
                    new BasicException.Parameter(BasicException.Parameter.XRI, request.getResourceIdentifier()),
                    new BasicException.Parameter("errorCode", exception.getErrorCode()),
                    new BasicException.Parameter("statement", currentStatement),
                    new BasicException.Parameter("parameters", statementParameters),
                    new BasicException.Parameter("sqlErrorCode", exception.getErrorCode()),
                    new BasicException.Parameter("sqlState", exception.getSQLState())));
        } catch (ServiceException exception) {
            throw ResourceExceptions.toResourceException(exception);
        } finally {
            SysLog.detail("< remove", System.nanoTime() - startTime + " ns");
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#put(org
     * .openmdx.base.resource.spi.RestInteractionSpec,
     * org.openmdx.base.rest.spi.Object_2Facade,
     * javax.resource.cci.IndexedRecord)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean update(
        RestInteractionSpec ispec,
        ObjectRecord object,
        ResultRecord ignored
    ) throws ResourceException {
        final long startTime = System.nanoTime();
        SysLog.detail("> replace", object);
        String currentStatement = null;
        List<Object> objectIdValues = null;
        try (Connection conn = database.getConnection(ispec, object)){
            DbObject dbObject = database.createDbObject(
                conn,
                object.getResourceIdentifier(),
                true);
            MappedRecord newValue = object.getValue();
            Object writeLock = object.getVersion();
            Object readLock = object.getLock();
            // Get current object with ALL_ATTRIBUTES. objectClassAsAttribute=true
            // asserts that empty rows (all columns with null values) are not truncated
            SingletonTarget oldObject = new SingletonTarget(database); 
            database.get(
                conn,
                ispec,
                object.getResourceIdentifier(),
                AttributeSelectors.ALL_ATTRIBUTES,
                Collections.<String, AttributeSpecifier>emptyMap(),
                true, // objectClassAsAttribute
                oldObject,
                true // throwNotFoundException
            );
            ObjectRecord obj = oldObject.getSingleton();
            MappedRecord oldValue = obj.getValue();
            if (!newValue.isEmpty()) {
                ObjectRecord[] oldSlices = dbObject.sliceAndNormalizeObject(obj, false);
                // Replace attribute values
                oldValue.putAll(newValue);
                database.removeAttributes(
                    obj,
                    true, // removePrivate
                    true, // removeNonPersistent
                    true // removeSize
                );
                ObjectRecord[] newSlices = dbObject.sliceAndNormalizeObject(obj, true);
                // Replace existing slices
                for (int i = 0; i < java.lang.Math.min(oldSlices.length, newSlices.length); i++) {
                    if (!newSlices[i].equals(oldSlices[i])) {
                        if (i == 0) {
                            dbObject.replaceObjectSlice(
                                i,
                                newSlices[i],
                                oldSlices[i],
                                database.toWriteLock(writeLock),
                                database.toReadLock(readLock));
                        } else {
                            dbObject.replaceObjectSlice(
                                i,
                                newSlices[i],
                                oldSlices[i],
                                null,
                                null);
                        }
                    }
                }
                // Remove extra old slices
                if (oldSlices.length > newSlices.length) {
                    final boolean isIndexed = dbObject.getConfiguration().getDbObjectForUpdate2() != null || dbObject.getIndexColumn() != null;
                    try (
                        PreparedStatement ps = dbObject.getConfiguration().getDbObjectForUpdate2() != null ? database.prepareStatement(
                            conn,
                            currentStatement = "DELETE FROM " + dbObject.getConfiguration().getDbObjectForUpdate2() +
                                " WHERE " +
                                database.removeViewPrefix(
                                    dbObject.getReferenceClause() +
                                        " AND " + dbObject.getObjectIdClause() +
                                        " AND (" + database.getObjectIdxColumnName() + " >= ?)")
                                
                       ) : database.prepareStatement(
                            conn,
                            currentStatement = "DELETE FROM " + dbObject.getConfiguration().getDbObjectForUpdate1() +
                                " WHERE " +
                                database.removeViewPrefix(
                                    dbObject.getReferenceClause() +
                                        " AND " + dbObject.getObjectIdClause() +
                                        (isIndexed ? " AND (" + dbObject.getIndexColumn() + " >= ?)" : ""))
                      )
                    ){
                        int pos = 1;
                        List<Object> referenceValues = dbObject.getReferenceValues();
                        for (Object referenceValue : referenceValues) {
                            database.setPreparedStatementValue(
                                conn,
                                ps,
                                pos++,
                                referenceValue);
                        }
                        objectIdValues = dbObject.getObjectIdValues();
                        for (Object objectIdValue : objectIdValues) {
                            database.setPreparedStatementValue(
                                conn,
                                ps,
                                pos++,
                                objectIdValue);
                        }
                        if (isIndexed) {
                            ps.setInt(pos++, newSlices.length);
                        }
                        database.executeUpdate(ps, currentStatement, objectIdValues);
                    }
                }
                // Create extra new slices
                if (newSlices.length > oldSlices.length) {
                    String objectClass = Object_2Facade.getObjectClass(object);
                    for (int i = oldSlices.length; i < newSlices.length; i++) {
                        dbObject.createObjectSlice(
                            i,
                            objectClass,
                            newSlices[i]);
                    }
                }
            } else {
                if (writeLock instanceof byte[]) {
                    Object version = obj.getVersion();
                    if (Arrays.equals((byte[]) writeLock, (byte[]) version)) {
                        database.removePrivateAttributes(obj);
                    } else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                            "The object has been modified since it has been read",
                            new BasicException.Parameter(BasicException.Parameter.XRI, object.getResourceIdentifier()),
                            new BasicException.Parameter("expected", writeLock),
                            new BasicException.Parameter("actual", version));
                    }
                } else if (writeLock != null) {
                    SysLog.warning("Optimistic write lock expects a byte[] version", writeLock.getClass().getName());
                }
                if (LockAssertions.isReadLockAssertion(readLock)) {
                    java.util.Date transactionTime = LockAssertions.getTransactionTime(readLock);
                    java.util.Date modifiedAt = (java.util.Date) obj.getValue().get(SystemAttributes.MODIFIED_AT);
                    if (modifiedAt == null) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                            "The object's modification time can't be determined",
                            new BasicException.Parameter(BasicException.Parameter.XRI, object.getResourceIdentifier()),
                            new BasicException.Parameter("expected", readLock),
                            new BasicException.Parameter("actual"));
                    } else if (transactionTime.before(modifiedAt)) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                            "The object has been modified since the unit of work has started",
                            new BasicException.Parameter(BasicException.Parameter.XRI, object.getResourceIdentifier()),
                            new BasicException.Parameter("expected", readLock),
                            new BasicException.Parameter(
                                "actual", SystemAttributes.MODIFIED_AT + '=' + DateTimeFormat.EXTENDED_UTC_FORMAT
                                    .format(modifiedAt)));
                    }
                } else if (readLock != null) {
                    SysLog.warning("Optimistic read lock expects a modifiedAt<=transactionTime assertion", readLock);
                }
            }
            return true;
        } catch (SQLException exception) {
            throw ResourceExceptions.toResourceException(
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE,
                    "Error when executing SQL statement",
                    new BasicException.Parameter(BasicException.Parameter.XRI, object.getResourceIdentifier()),
                    new BasicException.Parameter("statement", currentStatement),
                    new BasicException.Parameter("parameters", objectIdValues),
                    new BasicException.Parameter("sqlErrorCode", exception.getErrorCode()),
                    new BasicException.Parameter("sqlState", exception.getSQLState())));
        } catch (Exception exception) {
            throw ResourceExceptions.toResourceException(exception);
        } finally {
            SysLog.detail("< replace", System.nanoTime() - startTime + " ns");
        }
    }

}
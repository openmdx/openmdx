package org.openmdx.base.dataprovider.layer.persistence.jdbc.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.openmdx.application.dataprovider.spi.EmbeddedFlags;
import org.openmdx.application.dataprovider.spi.EmbeddedFlags.FlagsAndValue;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.query.spi.AccentInsensitivePattern;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.RegularExpressionFlag;

public enum LikeFlavour {

	LOWER_SQL {

		@Override
		protected void apply(StringBuilder clause, Collection<? super String> values, String columnName, String rawValue) {
			clause.append("LOWER(").append(columnName).append(") LIKE LOWER(?)");
			values.add(rawValue);
		}

	}, UPPER_SQL {

		@Override
		protected void apply(StringBuilder clause, Collection<? super String> values, String columnName, String rawValue) {
			clause.append("UPPER(").append(columnName).append(") LIKE UPPER(?)");
			values.add(rawValue);
		}

	}, LOWER_JAVA {
		
		@Override
		protected void apply(StringBuilder clause, Collection<? super String> values, String columnName, String rawValue) {
			clause.append("LOWER(").append(columnName).append(") LIKE ?");
			values.add(rawValue.toLowerCase());
		}

	}, UPPER_JAVA {

		@Override
		protected void apply(StringBuilder clause, Collection<? super String> values, String columnName, String rawValue) {
			clause.append("UPPER(").append(columnName).append(") LIKE ?");
			values.add(rawValue.toUpperCase());
		}

	}, BINARY_AI {

		@Override
		protected void apply(StringBuilder clause, Collection<? super String> values, String columnName, String rawValue) {
			clause.append("utl_raw.cast_to_varchar2(nlssort(").append(columnName).append(",'nls_sort=binary_ai')) LIKE ?");
			values.add(AccentInsensitivePattern.fold(rawValue.endsWith("%") ? rawValue : (rawValue + '_')));
		}
		
	}, REGEXP_LIKE {

		@Override
		protected void apply(StringBuilder clause, Collection<? super String> values, String columnName, String rawValue) {
			final FlagsAndValue flagsAndValue = EmbeddedFlags.getInstance().parse(rawValue);
			values.add(flagsAndValue.getValue());
			final String matchParam = RegularExpressionFlag.toMatchParameter(flagsAndValue.getFlagSet());
			if(matchParam.isEmpty()) {
				clause.append("REGEXP_LIKE(").append(columnName).append(",?)");
			} else {
				clause.append("REGEXP_LIKE(").append(columnName).append(",?,?)");
				values.add(matchParam);
			}
		}
		
	}, NOT_SUPPORTED {

		@Override
		protected void apply(
		    StringBuilder clause, 
		    Collection<? super String> values, 
		    String columnName, 
		    String rawValue
		) throws ServiceException {
			throw new ServiceException(
			    BasicException.Code.DEFAULT_DOMAIN,
			    BasicException.Code.NOT_SUPPORTED,
                "The actual database has no configuration for the requested LIKE flavour"
			);
		}
		
	};
	
	protected abstract void apply(
	    StringBuilder clause, 
	    Collection<? super String> values, 
	    String columnName, 
	    String rawValue
	) throws ServiceException;
	
	private static final Pattern OR = Pattern.compile("\\s*\\|\\s*");

	public static void applyAll(
	    List<LikeFlavour> likeFlavours, 
	    StringBuilder clause, 
	    Collection<? super String> values, 
	    String columnName, 
	    String value
	) throws ServiceException {
		String delimiter = "(";
		for(LikeFlavour likeFlavour : likeFlavours) {
			clause.append(delimiter);
			likeFlavour.apply(clause, values, columnName, value);
			delimiter = " OR ";
		}
		clause.append(")");
	}

	public static List<LikeFlavour> parse(String enumeration) {
		final String[] sourceEnumeration = OR.split(enumeration.trim());
		if(sourceEnumeration.length == 0) {
			throw new IllegalArgumentException("Inavlid CaseInsensitivity enumeration: '" + enumeration + "'");
		} else {
			List<LikeFlavour> likeFlavours = new ArrayList<LikeFlavour>(sourceEnumeration.length);
			for(String name : sourceEnumeration) {
				likeFlavours.add(LikeFlavour.valueOf(name));
			}
			return likeFlavours;
		}
	}
	
}

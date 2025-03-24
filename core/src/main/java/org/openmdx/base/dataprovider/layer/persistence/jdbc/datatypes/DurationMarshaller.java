/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: DurationMarshaller 
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc.datatypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConstants;
import #if CLASSIC_CHRONO_TYPES javax.xml.datatype #else java.time #endif.Duration;

import org.openmdx.base.dataprovider.layer.persistence.jdbc.LayerConfigurationEntries;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.postgresql.PGIntervalMarshaller;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
#if CLASSIC_CHRONO_TYPES import org.w3c.spi.ImmutableDatatypeFactory;#endif
import org.w3c.spi2.Datatypes;

/**
 * DurationMarshaller
 */
public class DurationMarshaller {

	/**
	 * Constructor
	 *
	 * @param durationType
	 * @throws ServiceException
	 */
	private DurationMarshaller(DurationType durationType) throws ServiceException {
		this.durationType = durationType;
	}

	/**
	 * Factory
	 * 
	 * @param durationType the duration type
	 * 
	 * @return an new {@code DurationMarshaller} instance
	 * @throws ServiceException
	 */
	public static DurationMarshaller newInstance(String durationType) throws ServiceException {
		return new DurationMarshaller(DurationType.toDurationType(durationType));
	}

	private final DurationType durationType;
	private static final int PRECISION = 3; // milliseconds
	private static final BigDecimal DAY_TIME_ZERO = BigDecimal.valueOf(0, PRECISION);
	private static final BigInteger YEAR_MONTHS_ZERO = BigInteger.ZERO;
	private static final BigInteger MONTHS_PER_YEAR = BigInteger.valueOf(12);
	private static final BigInteger HOURS_PER_DAY = BigInteger.valueOf(24);
	private static final BigInteger MINUTES_PER_HOUR = BigInteger.valueOf(60);
	private static final BigDecimal SECONDS_PER_MINUTE = BigDecimal.valueOf(60);

	private static final Pattern YEAR_TO_MONTH = Pattern.compile("^(-?)([0-9]+)-([0-9]+)$");
	private static final Pattern DAY_TO_SECOND = Pattern
			.compile("^(-?)([0-9]+) ([0-9]+)(?::([0-9]+)(?::([0-9]+(?:\\.[0-9]+))))?$");
	private static final PGIntervalMarshaller PG_INTERVAL_MARSHALLER = new PGIntervalMarshaller();

	@SuppressWarnings("unchecked")
	private <T extends Number> T getValue(Duration duration, DatatypeConstants.Field field) {
		Number value = duration.getField(field);
		if (value == null) {
			value = field == DatatypeConstants.SECONDS ? DAY_TIME_ZERO : YEAR_MONTHS_ZERO;
		}
		return (T) value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmdx.compatibility.base.marshalling.Marshaller#marshal(java.lang.
	 * Object)
	 */
	public Object marshal(Object source, String databaseProductName) throws ServiceException {
		if (Datatypes.DURATION_CLASS.isInstance(source)) {
			Duration duration = (Duration) source;
			ValueType valueType = ValueType.of(duration);
			if (valueType == null)
				return null;
			final int signum = duration.getSign();
			switch (durationType) {
				case INTERVAL: {
					if(PGIntervalMarshaller.isApplicableForDatabaseProduct(databaseProductName)) {
						switch (valueType) {
							case YEAR_MONTH: {
								BigInteger years = getValue(duration, DatatypeConstants.YEARS);
								BigInteger months = getValue(duration, DatatypeConstants.MONTHS);
								return PG_INTERVAL_MARSHALLER.marshal(signum, years.intValue(), months.intValue(), 0, 0, 0, 0);
							}
							case DAY_TIME: {
								BigInteger days = getValue(duration, DatatypeConstants.DAYS);
								BigInteger hours = getValue(duration, DatatypeConstants.HOURS);
								BigInteger minutes = getValue(duration, DatatypeConstants.MINUTES);
								BigDecimal seconds = getValue(duration, DatatypeConstants.SECONDS);
								return PG_INTERVAL_MARSHALLER.marshal(
									signum, 
									0, 
									0, 
									days.intValue(), 
									hours.intValue(), 
									minutes.intValue(), 
									seconds.doubleValue()
								);
							}
							case YEAR_MONTH_DAY_TIME:
								BigInteger years = getValue(duration, DatatypeConstants.YEARS);
								BigInteger months = getValue(duration, DatatypeConstants.MONTHS);
								BigInteger days = getValue(duration, DatatypeConstants.DAYS);
								BigInteger hours = getValue(duration, DatatypeConstants.HOURS);
								BigInteger minutes = getValue(duration, DatatypeConstants.MINUTES);
								BigDecimal seconds = getValue(duration, DatatypeConstants.SECONDS);
								return PG_INTERVAL_MARSHALLER.marshal(
									signum, 
									years.intValue(), 
									months.intValue(), 
									days.intValue(), 
									hours.intValue(), 
									minutes.intValue(), 
									seconds.doubleValue()
								);
							default:
								return null;
						}
					} else {
						final StringBuilder target = new StringBuilder(signum < 0 ? "-" : "");
						switch (valueType) {
							case YEAR_MONTH: {
								BigInteger years = getValue(duration, DatatypeConstants.YEARS);
								BigInteger months = getValue(duration, DatatypeConstants.MONTHS);
								return target.append(years).append("-").append(months).toString();
							}
							case DAY_TIME: {
								BigInteger days = getValue(duration, DatatypeConstants.DAYS);
								BigInteger hours = getValue(duration, DatatypeConstants.HOURS);
								BigInteger minutes = getValue(duration, DatatypeConstants.MINUTES);
								Number seconds = getValue(duration, DatatypeConstants.SECONDS);
								return target.append(days).append(" ").append(hours).append(":").append(minutes).append(":")
										.append(seconds).toString();
							}
							case YEAR_MONTH_DAY_TIME:
								throw new ServiceException(BasicException.Code.DEFAULT_DOMAIN,
										BasicException.Code.TRANSFORMATION_FAILURE,
										"An INTERVAL duration must be either a year-month or a day-time duration",
										new BasicException.Parameter("duration", duration));
							default:
								return null;
						}
					}
			}
			case NUMERIC:
				switch (valueType) {
				case YEAR_MONTH: {
					BigInteger years = getValue(duration, DatatypeConstants.YEARS);
					BigInteger months = getValue(duration, DatatypeConstants.MONTHS);
					BigInteger value = months.add(years.multiply(MONTHS_PER_YEAR));
					return signum < 0 ? value.negate() : value;
				}
				case DAY_TIME:
					BigInteger days = getValue(duration, DatatypeConstants.DAYS);
					BigInteger hours = getValue(duration, DatatypeConstants.HOURS);
					BigInteger minutes = getValue(duration, DatatypeConstants.MINUTES);
					BigDecimal seconds = getValue(duration, DatatypeConstants.SECONDS);
					BigDecimal value = seconds.add(new BigDecimal(
							minutes.add(hours.add(days.multiply(HOURS_PER_DAY)).multiply(MINUTES_PER_HOUR)))
									.multiply(SECONDS_PER_MINUTE));
					return signum < 0 ? value.negate() : value;
				case YEAR_MONTH_DAY_TIME:
					throw new ServiceException(BasicException.Code.DEFAULT_DOMAIN,
							BasicException.Code.TRANSFORMATION_FAILURE,
							"A NUMERIC duration must be either a year-month or a day-time duration",
							new BasicException.Parameter("duration", duration));
				default:
					return null;
				}
			case CHARACTER: {
				boolean normalized = true;
				BigInteger years = getValue(duration, DatatypeConstants.YEARS);
				BigInteger months = getValue(duration, DatatypeConstants.MONTHS);
				BigInteger days = getValue(duration, DatatypeConstants.DAYS);
				BigInteger hours = getValue(duration, DatatypeConstants.HOURS);
				BigInteger minutes = getValue(duration, DatatypeConstants.MINUTES);
				BigDecimal seconds = getValue(duration, DatatypeConstants.SECONDS);
				if (seconds.compareTo(SECONDS_PER_MINUTE) > 0) {
					normalized = false;
					BigDecimal[] values = seconds.divideAndRemainder(SECONDS_PER_MINUTE);
					seconds = values[1];
					minutes = minutes.add(values[0].toBigInteger());
				}
				if (minutes.compareTo(MINUTES_PER_HOUR) > 0) {
					normalized = false;
					BigInteger[] values = minutes.divideAndRemainder(MINUTES_PER_HOUR);
					minutes = values[1];
					hours = hours.add(values[0]);
				}
				if (hours.compareTo(HOURS_PER_DAY) > 0) {
					normalized = false;
					BigInteger[] values = hours.divideAndRemainder(HOURS_PER_DAY);
					hours = values[1];
					days = days.add(values[0]);
				}
				if (months.compareTo(MONTHS_PER_YEAR) > 0) {
					normalized = false;
					BigInteger[] values = months.divideAndRemainder(MONTHS_PER_YEAR);
					months = values[1];
					years = years.add(values[0]);
				}
				if (normalized) {
					return duration.toString();
				} else {
					StringBuilder target = new StringBuilder(signum < 0 ? "-" : "");
					target.append("P");
					boolean empty = true;
					if (years.signum() > 0) {
						empty = false;
						target.append(years).append("Y");
					}
					if (months.signum() > 0) {
						empty = false;
						target.append(months).append("M");
					}
					if (days.signum() > 0) {
						empty = false;
						target.append(days).append("D");
					}
					if (empty || hours.signum() > 0 || minutes.signum() > 0 || seconds.signum() > 0) {
						target.append("T");
						if (hours.signum() > 0) {
							empty = false;
							target.append(hours).append("H");
						}
						if (minutes.signum() > 0) {
							empty = false;
							target.append(minutes).append("M");
						}
						if (empty || seconds.signum() > 0) {
							target.append(seconds).append("S");
						}
					}
					return target.toString();
				}
			}
			default:
				return null;
			}
		} else {
			return source;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmdx.compatibility.base.marshalling.Marshaller#unmarshal(java.lang.
	 * Object)
	 */
	public Object unmarshal(Object source) throws ServiceException {
		if (source == null || Datatypes.DURATION_CLASS.isInstance(source)) {
			return source;
		} else {
			switch (durationType) {
			case CHARACTER:
				return Datatypes.create(Datatypes.DURATION_CLASS, source.toString());
			case INTERVAL: {
				if(PGIntervalMarshaller.isApplicableForDataType(source)) {
					return PG_INTERVAL_MARSHALLER.unmarshal(source);
				} else {
					String value = source.toString();
					Matcher matcher;
					if ((matcher = DAY_TO_SECOND.matcher(value)).matches()) {
						StringBuilder duration = new StringBuilder().append(matcher.group(1)).append('P')
								.append(matcher.group(2)).append("DT").append(matcher.group(3)).append("H");
						String minutes = matcher.group(4);
						if (minutes != null)
							duration.append(minutes).append("M");
						String seconds = matcher.group(5);
						if (seconds != null) {
							int dp = seconds.indexOf('.');
							if (dp < 0) {
								dp = seconds.indexOf(',');
							}
							int precision;
							if (dp > 0) {
								precision = seconds.length() - dp - 1;
								if (precision > PRECISION) {
									seconds = seconds.substring(0, dp + PRECISION + 1);
								}
								duration.append(seconds);
							} else {
								precision = 0;
								duration.append(seconds).append(".");
							}
							while (precision++ < PRECISION) {
								duration.append("0");
							}
							duration.append("S");
						}
						return Datatypes.create(Datatypes.DURATION_CLASS, duration.toString());
					} else if ((matcher = YEAR_TO_MONTH.matcher(value)).matches()) {
						StringBuilder duration = new StringBuilder().append(matcher.group(1)).append('P')
								.append(matcher.group(2)).append("Y").append(matcher.group(3)).append("M");
						return Datatypes.create(Datatypes.DURATION_CLASS, duration.toString());
					} else
						throw new ServiceException(BasicException.Code.DEFAULT_DOMAIN,
								BasicException.Code.TRANSFORMATION_FAILURE,
								getClass().getName() + " expects at least two fields (years and months or days and hours)",
								new BasicException.Parameter("value", value));
				}
			}
			case NUMERIC: {
				if (source instanceof Number) {
					Number value = (Number) source;
					return source instanceof BigDecimal && ((BigDecimal) source).scale() > 0
							? toDuration("T", value, "S")
							: toDuration("", value, "M");
				} else
					throw new ServiceException(BasicException.Code.DEFAULT_DOMAIN,
							BasicException.Code.TRANSFORMATION_FAILURE,
							getClass().getName() + " expects durationType "
									+ LayerConfigurationEntries.DURATION_TYPE_NUMERIC + " values being instances of "
									+ Number.class.getName(),
							new BasicException.Parameter("class", source.getClass().getName()),
							new BasicException.Parameter("value", source));
			}
			default:
				return source;
			}
		}
	}

	private Object toDuration(String prefix, Number infix, String suffix) {
		String value = infix.toString();
		return Datatypes.create(Datatypes.DURATION_CLASS, value.charAt(0) == '-' ? ("-P" + prefix + value.substring(1) + suffix)
				: ("P" + prefix + value + suffix));
	}

	// ------------------------------------------------------------------------
	// Enum DurationType
	// ------------------------------------------------------------------------

	/**
	 * The type used to store {@code org::w3c::duration} values
	 */
	static enum DurationType {

		/**
		 * {@code INTERVAL} <i>(domain defined by the database field definition)
		 */
		INTERVAL,

		/**
		 * <li>{@code CHARACTER} <i>(default)</i>
		 */
		CHARACTER,

		/**
		 * {@code NUMERIC} <i>(domain <b>either</b> year-month <b>or</b> date-time
		 * intervals!)</i>
		 */
		NUMERIC;

		static DurationType toDurationType(String durationType) throws ServiceException {
			try {
				return DurationType.valueOf(durationType);
			} catch (RuntimeException exception) {
				throw new ServiceException(exception, BasicException.Code.DEFAULT_DOMAIN,
						BasicException.Code.INVALID_CONFIGURATION, "Unsupported duration type",
						new BasicException.Parameter("supported", (Object[]) DurationType.values()),
						new BasicException.Parameter("requested", durationType));
			}
		}

	}

	/**
	 * The value type defines which fields are set
	 */
    enum ValueType {
		YEAR_MONTH, YEAR_MONTH_DAY_TIME, DAY_TIME;

	static ValueType of(Duration duration) {
		#if CLASSIC_CHRONO_TYPES
			boolean yearMonth = duration.isSet(DatatypeConstants.YEARS) || duration.isSet(DatatypeConstants.MONTHS);
			boolean dayTime = duration.isSet(DatatypeConstants.DAYS) || duration.isSet(DatatypeConstants.HOURS)
				|| duration.isSet(DatatypeConstants.MINUTES) || duration.isSet(DatatypeConstants.SECONDS);
			return yearMonth ? (dayTime ? YEAR_MONTH_DAY_TIME : YEAR_MONTH) : (dayTime ? DAY_TIME : null);
		#else
			long seconds = duration.getSeconds();
			int nanos = duration.getNano();
			boolean dayTime = seconds != 0 || nanos != 0;

			return dayTime ? ValueType.DAY_TIME : null;
		#endif
	}

	}

}

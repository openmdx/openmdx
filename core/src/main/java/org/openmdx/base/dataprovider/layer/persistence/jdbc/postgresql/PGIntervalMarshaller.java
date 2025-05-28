/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: PostgreSQL Interval Marshaller
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc.postgresql;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Period;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import #if CLASSIC_CHRONO_TYPES javax.xml.datatype #else java.time#endif.Duration;

import org.openmdx.base.Version;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;

/**
 * PostgreSQL Interval Marshaller
 */
public class PGIntervalMarshaller {

	private static final String PG_INTERVAL_CLASS = "org.postgresql.util.PGInterval";
	private static final Pattern PG_INTERVAL_PATTERN = Pattern.compile(
			"^(-?[0-9]+) years (-?[0-9]+) mons (-?[0-9]+) days (-?[0-9]+) hours (-?[0-9]+) mins (-?[0-9]+(?:\\.[0-9]+)) secs$");
	private static final BigInteger MONTHS_PER_YEAR = BigInteger.valueOf(12);
	private static final BigInteger HOURS_PER_DAY = BigInteger.valueOf(24);
	private static final BigInteger MINUTES_PER_HOUR = BigInteger.valueOf(60);
	private static final BigDecimal SECONDS_PER_MINUTE = BigDecimal.valueOf(60);

	public static boolean isApplicableForDatabaseProduct(String databaseProductName) {
		return "PostgreSQL".equals(databaseProductName);
	}

	public static boolean isApplicableForDataType(Object interval) {
		if(interval == null) {
			return false;
		}
		return PG_INTERVAL_CLASS.equals(interval.getClass().getName());
	}

	public Object marshal(
		int signum,
		int years,
		int months,
		int days,
		int hours,
		int minutes,
		double seconds
	) throws ServiceException {
		try {
			return signum < 0 ? Classes.newApplicationInstance(
				Object.class,
				PG_INTERVAL_CLASS,
				-years,
				-months,
				-days,
				-hours,
				-minutes,
				-seconds
			) : Classes.newApplicationInstance(
				Object.class,
				PG_INTERVAL_CLASS,
				years,
				months,
				days,
				hours,
				minutes,
				seconds
			);
		} catch (ClassNotFoundException | IllegalArgumentException | InstantiationException | IllegalAccessException
				| InvocationTargetException exception) {
			throw new ServiceException(
				exception,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.TRANSFORMATION_FAILURE,
				"Unable to convert a duration into a PGInterval",
				new BasicException.Parameter("signum", signum),
				new BasicException.Parameter("years", years),
				new BasicException.Parameter("months", months),
				new BasicException.Parameter("days", days),
				new BasicException.Parameter("hours", hours),
				new BasicException.Parameter("minutes", minutes),
				new BasicException.Parameter("seconds", seconds)
			);
		}
	}

	public Object unmarshal(Object interval) throws ServiceException {
		final Matcher matcher = PG_INTERVAL_PATTERN.matcher(interval.toString());
		if(matcher.matches()) {
			BigInteger years = new BigInteger(matcher.group(1));
			BigInteger months = new BigInteger(matcher.group(2));
			BigInteger days = new BigInteger(matcher.group(3));
			BigInteger hours = new BigInteger(matcher.group(4));
			BigInteger minutes = new BigInteger(matcher.group(5));
			BigDecimal seconds = new BigDecimal(matcher.group(6));
			boolean negative = years.signum() < 0 || months.signum() < 0 || days.signum() < 0 || hours.signum() < 0 || minutes.signum() < 0 || seconds.signum() < 0;
			if(negative) {
				boolean positive = years.signum() > 0 || months.signum() > 0 || days.signum() > 0 || hours.signum() > 0 || minutes.signum() > 0 || seconds.signum() > 0;
				if(positive) {
					throw new ServiceException(
						BasicException.Code.DEFAULT_DOMAIN,
						BasicException.Code.TRANSFORMATION_FAILURE,
						"Unable to convert the PGInterval: Duration does not allow mixing of positive and negative fields",
						new BasicException.Parameter("flavour", Version.getFlavourVersion()),
						new BasicException.Parameter("interval", interval)
					);
				} else {
					years = years.negate();
					months = months.negate();
					days = days.negate();
					hours = hours.negate();
					minutes = minutes.negate();
					seconds = seconds.negate();
				}
			}
			if (seconds.compareTo(SECONDS_PER_MINUTE) > 0) {
				BigDecimal[] values = seconds.divideAndRemainder(SECONDS_PER_MINUTE);
				seconds = values[1];
				minutes = minutes.add(values[0].toBigInteger());
			}
			if (minutes.compareTo(MINUTES_PER_HOUR) > 0) {
				BigInteger[] values = minutes.divideAndRemainder(MINUTES_PER_HOUR);
				minutes = values[1];
				hours = hours.add(values[0]);
			}
			if (hours.compareTo(HOURS_PER_DAY) > 0) {
				BigInteger[] values = hours.divideAndRemainder(HOURS_PER_DAY);
				hours = values[1];
				days = days.add(values[0]);
			}
			if (months.compareTo(MONTHS_PER_YEAR) > 0) {
				BigInteger[] values = months.divideAndRemainder(MONTHS_PER_YEAR);
				months = values[1];
				years = years.add(values[0]);
			}

			#if CLASSIC_CHRONO_TYPES
			if (years.signum() == 0 && months.signum() == 0) {
				years = null;
				months = null;
			} else if (days.signum() == 0 && hours.signum() == 0 && minutes.signum() == 0 && seconds.signum() == 0){
				days = null;
				hours = null;
				minutes = null;
				seconds = null;
			}
			return org.w3c.spi.DatatypeFactories.xmlDatatypeFactory().newDuration(
				!negative,
				years,
				months,
				days,
				hours,
				minutes,
				seconds
			);
			#else
			if (years.signum() == 0 && months.signum() == 0) {
				// Handle seconds properly by separating whole seconds and nanoseconds
				final long wholeSeconds = seconds.longValue();
				final int nanos = seconds.subtract(new BigDecimal(wholeSeconds))
					.movePointRight(9)
					.intValue();
				final Duration duration = Duration
						.ofDays(days.longValue())
						.plusHours(hours.longValue())
						.plusMinutes(minutes.longValue())
						.plusSeconds(wholeSeconds)
						.plusNanos(nanos);
				return negative ? duration.negated() : duration;
            } else if (hours.signum() == 0 && minutes.signum() == 0 && seconds.signum() == 0) {
				return Period.of(
						years.intValue(),
						months.intValue(),
						days.intValue()
				);
			} else {
				throw new ServiceException(
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.TRANSFORMATION_FAILURE,
					"Unable to convert the PGInterval: years/monnths and time values are mutually exclusive",
					new BasicException.Parameter("flavour", Version.getFlavourVersion()),
					new BasicException.Parameter("interval", interval)
				);
			}
			#endif
		} else {
			throw new ServiceException(
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.TRANSFORMATION_FAILURE,
				"Unable to parse the PGInterval",
				new BasicException.Parameter("flavour", Version.getFlavourVersion()),
				new BasicException.Parameter("interval", interval),
				new BasicException.Parameter("expected", PG_INTERVAL_PATTERN)
			);
		}
	}

}

/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Read Locks
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
package org.openmdx.base.accessor.rest.spi;

import java.text.ParseException;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.format.DateTimeFormat;
import org.w3c.spi2.Datatypes;
#if CLASSIC_CHRONO_TYPES import org.w3c.format.DateTimeFormat;#endif

/**
 * Lock Assertions
 */
public class LockAssertions {
	
	/**
	 * Constructor
	 */
	private LockAssertions(
	){
		// Avoid instantiation
	}

	/**
	 * The transaction time is appended to the read lock prefix giving
	 * the read lock assertion.
	 */
	private static final String READ_LOCK_PREFIX = SystemAttributes.MODIFIED_AT + "<=";

	/**
	 * Tells whether the lock assertion is a read lock assertion
	 * 
	 * @param lockAssertion
	 * 
	 * @return {@code true} if the lock assertion is a read lock assertion
	 */
	public static boolean isReadLockAssertion(
		Object lockAssertion
	){
		return lockAssertion instanceof String && ((String)lockAssertion).startsWith(READ_LOCK_PREFIX);
	}

	/**
	 * Create a new read lock assertion
	 * 
	 * @param transactionTime
	 * 
	 * @return the read lock assertion for the given transaction time
	 */
	public static Object newReadLockAssertion(
		#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif transactionTime
	){
		return READ_LOCK_PREFIX + DateTimeFormat.EXTENDED_UTC_FORMAT.format(transactionTime);
	}
	
	/**
	 * Extract the transaction time from a read lock assertion
	 * 
	 * @param readLockAssertion
	 * 
	 * @return the transaction time
	 * 
	 * @throws ServiceException
	 */
	public static #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif getTransactionTime(
		Object readLockAssertion
	) throws ServiceException {
		try {
			return DateTimeFormat.EXTENDED_UTC_FORMAT.parse(
					((String)readLockAssertion).substring(READ_LOCK_PREFIX.length())
			);
		} catch (ParseException exception) {
			throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.TRANSFORMATION_FAILURE,
					"Unable to extract the transaction time from the readLockAssertion"
			);
		}
	}
	
}

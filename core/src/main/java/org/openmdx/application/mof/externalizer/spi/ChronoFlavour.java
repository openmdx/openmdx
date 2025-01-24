/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: JMI Flavour
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
package org.openmdx.application.mof.externalizer.spi;

import java.util.Collection;

import org.openmdx.application.mof.mapping.cci.ExtendedFormatOptions;

public enum ChronoFlavour {
	
	CLASSIC {
		@Override
		public void applyExtendedFormat(Collection<String> extendedFormats) {
			extendedFormats.add(EXTENDED_FORMAT);
		}
	},
	CONTEMPORARY {
		@Override
		public void applyExtendedFormat(Collection<String> extendedFormats) {
			// default behaviour
			
		}
	};


	public static final ChronoFlavour DEFAULT = CLASSIC;
	private static final String EXTENDED_FORMAT = ExtendedFormatOptions.CLASSIC_CHRONO_TYPES;

	public abstract void applyExtendedFormat(Collection<String> extendedFormats);
	
	public boolean isClassic() {
		return this == CLASSIC;
	}
	
	public static ChronoFlavour fromFlavourVersion(String flavourVersion) {
		return "2".equals(flavourVersion) || "4".equals(flavourVersion) ? CLASSIC : CONTEMPORARY;
	}	

	public static ChronoFlavour fromExtendedFormats(Collection<String> extendedFormats) {
		return extendedFormats.remove(EXTENDED_FORMAT) ? CLASSIC : CONTEMPORARY;
	}
	
}

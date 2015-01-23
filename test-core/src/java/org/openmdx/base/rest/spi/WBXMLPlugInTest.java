/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: WBXML Plug-In Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014, OMEX AG, Switzerland
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
package org.openmdx.base.rest.spi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;

import org.junit.Test;
import org.openmdx.base.wbxml.CodeToken;

/**
 * WBXML Plug-In Test
 */
public class WBXMLPlugInTest {

	@Test
	public void whenPage0IsLoadedThenThereAreNoCollisions(){
		// Arrange
		final boolean lenient = false;
		// Act
		WBXMLPlugIn.preparePage0(lenient);
		// Assert that no exception did happen		
	}
	
	@Test
	public void whenRecordIsKnownThenTagsAreOnCodePage0(){
		// Arrange
		final WBXMLPlugIn testee = new WBXMLPlugIn();
		Collection<String> exceptions = Arrays.asList("clause", "condition", "dateParam", "extension", "fetchGroupName", "integerParam", "lock", "type");
		Set<String> missing = new TreeSet<String>();
		// Act
		getCodeForStuctureMembers(missing, testee, org.openmdx.base.rest.cci.ConditionRecord.Member.class);
		getCodeForStuctureMembers(missing, testee, org.openmdx.base.rest.cci.MessageRecord.Member.class);
		getCodeForStuctureMembers(missing, testee, org.openmdx.base.rest.cci.FeatureOrderRecord.Member.class);
		getCodeForStuctureMembers(missing, testee, org.openmdx.base.rest.cci.ObjectRecord.Member.class);
		getCodeForStuctureMembers(missing, testee, org.openmdx.base.rest.cci.QueryFilterRecord.Member.class);
		getCodeForStuctureMembers(missing, testee, org.openmdx.base.rest.cci.QueryExtensionRecord.Member.class);
		getCodeForStuctureMembers(missing, testee, org.openmdx.base.rest.cci.QueryRecord.Member.class);
		missing.removeAll(exceptions);
		// Assert
		Assert.assertEquals("Missing on page 0", Collections.emptySet(), missing);
	}
	
	private <E extends Enum<E>> void getCodeForStuctureMembers(
		Set<String> missing,
		WBXMLPlugIn testee, 
		Class<E> memberClass
	){
		for(E member : EnumSet.allOf(memberClass)) {
			final CodeToken tagToken = testee.getTagToken(null, member.name());
			final int codePage = tagToken.getPage();
			if(codePage != 0) {
				System.out.println("Tag " + member.name() + " lands on code page " + codePage);
				missing.add(member.name());
			}
		}
	}
	
}

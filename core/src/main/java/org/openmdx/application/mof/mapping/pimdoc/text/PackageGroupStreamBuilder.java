/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Package Group Stream Builder
 * Owner: the original authors. 
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.application.mof.mapping.pimdoc.text;

import java.net.URI;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.openmdx.application.mof.mapping.pimdoc.spi.PackageGroupComparator;

/**
 * Package Group Stream Builder
 */
class PackageGroupStreamBuilder {

	PackageGroupStreamBuilder(Stream<String> packageGroups) {
		packageGroups.forEach(this::addTableOfContentEntry);
	}

	private static final Comparator<String> COMPARATOR = new PackageGroupComparator();
	final SortedMap<String,PackageGroup> packageClusters = new TreeMap<String,PackageGroup>(COMPARATOR);
	
	private void addTableOfContentEntry(String qualifiedName) {
		packageClusters.computeIfAbsent(qualifiedName, PackageGroup::new);
	}

	PackageGroupStreamBuilder withClassesAndDataTypes(Stream<String> qualifiedNames) {
		qualifiedNames.forEach(this::withClassOrDataType);
		return this;
	}

	private void withClassOrDataType(String qualifiedName) {
		packageClusters.values().forEach(i -> i.offerClassOrDataType(qualifiedName));
	}

	PackageGroupStreamBuilder withDiagrams(Stream<Map.Entry<URI, String>> diagramEntries) {
		diagramEntries.forEach(this::withDiagram);
		return this;
	}
	
	private void withDiagram(Map.Entry<URI, String> diagramEntry) {
		packageClusters.values().forEach(i -> i.offerDiagram(diagramEntry));
	}
	
	Stream<PackageGroup> build() {
		return packageClusters.values().stream().filter(PackageGroup::hasContent);
	}
	
}
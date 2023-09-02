/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Package Group  
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
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.openmdx.application.mof.mapping.pimdoc.spi.PackageGroupComparator;
import org.openmdx.application.mof.repository.accessor.NamespaceLocation;

class PackageGroup {
	
	PackageGroup(final String packageClusterKey) {
		this.packageClusterKey = packageClusterKey;
		this.location = getLocation(packageClusterKey);
	}

	private static final Comparator<Map.Entry<String, String>> COMPARATOR = new FirstByValueThenByKeyComparator();
	private final String packageClusterKey;
	private final String location;
	private final Map<String, String> classesAndDataTypes = new HashMap<>(); 
	private final Map<String, String> diagrams = new HashMap<>();

	void offerClassOrDataType(String qualifiedName) {
		if(acceptClassOrDataType(qualifiedName)){
			final String simpleName = qualifiedName.substring(qualifiedName.lastIndexOf(':')+1);
			classesAndDataTypes.put(qualifiedName, simpleName);
		}
	}

	private boolean acceptClassOrDataType(String qualifiedName) {
		return PackageGroupComparator.matches(qualifiedName, packageClusterKey);
	}
	
	void offerDiagram(Map.Entry<URI, String> diagramEntry) {
		final String uri = diagramEntry.getKey().getPath().substring(1);
		if(acceptDiagram(uri)) {
			diagrams.put(uri, diagramEntry.getValue());
		}
	}
	
	private boolean acceptDiagram(String uri) {
		if(PackageGroupComparator.isCatchAllPattern(packageClusterKey)) {
			return true;
		} else if (PackageGroupComparator.isWildcardPattern(packageClusterKey)) {
			return uri.startsWith(location);
		} else {
			return uri.startsWith(location) && uri.substring(location.length()).indexOf('/') < 0;
		}
	}

	boolean hasContent() {
		return hasClassesOrDataTypes() || hasDiagrams();
	}
	
	boolean hasClassesOrDataTypes() {
		return !classesAndDataTypes.isEmpty();
	}

	boolean hasDiagrams() {
		return !diagrams.isEmpty();
	}
	
	String getPackageClusterKey() {
		return packageClusterKey;
	}

	void forEachClassOrDataType(BiConsumer<String, String> sink) {
		process(classesAndDataTypes, sink);
	}

	void forEachDiagram(BiConsumer<String, String> sink) {
		process(diagrams, sink);
	}
	
	private void process(Map<String, String> source, BiConsumer<String, String> sink) {
		source
			.entrySet()
			.stream()
			.sorted(COMPARATOR)
			.forEach(e -> sink.accept(e.getKey(), e.getValue()));
	}

	private static String getLocation(String qualifiedName) {
		final int end = qualifiedName.lastIndexOf(':');
		return NamespaceLocation.getLocation(end < 0 ? qualifiedName : qualifiedName.substring(0, end));
	}
	
}
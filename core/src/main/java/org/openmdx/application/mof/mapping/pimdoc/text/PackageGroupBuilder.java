/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Package Group Builder 
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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.openmdx.application.mof.mapping.pimdoc.spi.PackagePatternComparator;
import org.openmdx.application.mof.mapping.pimdoc.spi.SimpleNameComparator;

/**
 * Package Group Builder
 */
class PackageGroupBuilder extends TreeMap<String,SortedSet<String>> {

	public PackageGroupBuilder() {
		super(COMPARATOR);
	}

	private static final Comparator<String> COMPARATOR = new PackagePatternComparator();
	
	private static final long serialVersionUID = -4489160358886710466L;
	private final Comparator<String> simpleNameComparator = new SimpleNameComparator();
    final SortedMap<String, String> catchAllPkgDiagramIndexData = new TreeMap<>();
    final SortedMap<String, SortedMap<String, String>> wildcardPkgDiagramIndexData = new TreeMap<>();
    final SortedMap<String, SortedMap<String, String>> standardPkgDiagramIndexData = new TreeMap<>();

	
	public void addKey(String qualifiedName) {
		this.computeIfAbsent(qualifiedName, key -> new TreeSet<String>(simpleNameComparator));
	}

	public void addElement(String qualifiedName) {
		for(Map.Entry<String,SortedSet<String>> e : entrySet()) {
			if(isPartOfPackageGroup(e.getKey(), qualifiedName)) {
				e.getValue().add(qualifiedName);
			}
		}
	}
	
	boolean isPartOfPackageGroup(String packagePattern, String qualifiedName) {
		if(PackagePatternComparator.isWildcardPattern(packagePattern)) {
			return PackagePatternComparator.isCatchAllPattern(packagePattern) ||
				qualifiedName.startsWith(PackagePatternComparator.removeWildcard(packagePattern) + ':');
		} else {
			return getPackageId(packagePattern).equals(getPackageId(qualifiedName));
		}
	}

	private String getPackageId(String qualifiedName) {
		final int end = qualifiedName.lastIndexOf(':');
		return qualifiedName.substring(0, end);
	}
	
	public void normalize() {
		for(Iterator<Map.Entry<String,SortedSet<String>>> i = entrySet().iterator(); i.hasNext(); ) {
			final Map.Entry<String, SortedSet<String>> e = i.next();
			if(e.getValue().isEmpty()) {
				final String packagePattern = e.getKey();
				if(PackagePatternComparator.isCatchAllPattern(packagePattern)) {
					if(catchAllPkgDiagramIndexData.isEmpty()) i.remove();
				} else if (PackagePatternComparator.isWildcardPattern(packagePattern)) {
					if(wildcardPkgDiagramIndexData.getOrDefault(packagePattern, Collections.emptySortedMap()).isEmpty()) i.remove();
				} else {
					if(standardPkgDiagramIndexData.getOrDefault(packagePattern, Collections.emptySortedMap()).isEmpty()) i.remove();
				}
			}
		}
	}
	
}
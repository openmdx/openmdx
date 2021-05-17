/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: SharedAssociations 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2018, OMEX AG, Switzerland
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
package org.openmdx.application.mof.repository.accessor;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Predicate;

import org.openmdx.base.naming.Path;

/**
 * Shared Associations
 */
public class SharedAssociations {

    /**
     * Constructor
     *
     * @param sharedAssociationPredicate
     *            determine whether an association is shared provided the parent's association isn't.
     */
    public SharedAssociations(Predicate<Path> sharedAssociationPredicate) {
        this.sharedAssociationPredicate = sharedAssociationPredicate;
        this.sharedAssociations = new ConcurrentSkipListMap<>();
        this.sharedAssociations.put(SEGMENT_REFERENCE_PATTERN, Boolean.FALSE);
    }

    private final Predicate<Path> sharedAssociationPredicate;

    private final ConcurrentSkipListMap<Path,Boolean> sharedAssociations;

    private static final Path ANY_AUTHORITY_PATTERN = new Path("xri://@openmdx*($..)");
    private static final Path SEGMENT_REFERENCE_PATTERN = ANY_AUTHORITY_PATTERN.getDescendant("provider","($..)","segment");
    
    public boolean containsSharedAssociation(final Path xri ) {
        final int size = xri.size();
        return size > 1 && referenceContainsSharedAssociation(
            toPattern(size % 2 == 0 ? xri : xri.getParent())
        );
    }

    private boolean referenceContainsSharedAssociation(final Path pattern) {
        if(pattern.isEmpty()) {
            return false;
        } else {
            Boolean value = this.sharedAssociations.get(pattern);
            if(value == null) {
                if(referenceContainsSharedAssociation(getGrandparent(pattern))) {
                    value = Boolean.TRUE;
                } else {
                    value = Boolean.valueOf(this.sharedAssociationPredicate.test(pattern));
                }
                this.sharedAssociations.put(normalizePattern(pattern), value);
            }
            return value.booleanValue();
        }
    }

    private Path toPattern(final Path reference) {
        final int size = reference.size();
        return size == 4 && reference.isLike(SEGMENT_REFERENCE_PATTERN) ? SEGMENT_REFERENCE_PATTERN : (
            size == 2 ? ANY_AUTHORITY_PATTERN : toPattern(getGrandparent(reference)).getChild("($..)")
        ).getChild(
            reference.getLastSegment()
        );
    }
    
    private Path normalizePattern(
        Path pattern
    ) {
        final Path grandparent = getGrandparent(pattern);
        final Path floorKey = this.sharedAssociations.floorKey(grandparent);
        return floorKey != null && floorKey != grandparent && floorKey.equals(grandparent) ? replaceGrandparent(floorKey, pattern) : pattern;
    }

    /**
     * {@code protected} visibility for unit tests
     */
    protected Path replaceGrandparent(
        final Path normalizedGrandparent,
        Path pattern
    ) {
        // assert pattern.startsWith(normalizedGrandparent) && pattern.size() == normalizedGrandparent.size() + 2;
        return normalizedGrandparent.getChild("($..)").getChild(pattern.getLastSegment());
    }
    
    /**
     * Retrieve the grandparent
     * 
     * @param reference a reference
     * @return the grandparent, another reference
     */
    private static Path getGrandparent(Path reference) {
        return reference.getParent().getParent();
    }
    
}

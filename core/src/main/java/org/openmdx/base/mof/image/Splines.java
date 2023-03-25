/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Graphviz Splines
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
package org.openmdx.base.mof.image;

/**
 * Graphviz Splines
 */
public enum Splines {

	/**
	 * No edges are drawn at all.
	 */
	NONE,
	
	/**
	 * Edges are drawn as line segments.
	 */
	LINE,
	
	/**
	 * Edges should be drawn as polylines.
	 */
	POLYLINE,
	
	/**
	 *  Edges should be drawn as curved arcs
	 */
	CURVED,
	
	/**
	 * Edges should be routed as polylines of axis-aligned segments. Currently, the routing does not handle ports or, in dot, edge labels.
	 */
	ORTHO,
	
	/**
	 * Edges are drawn as splines routed around nodes.
	 */
	SPLINE;
	
	public static Splines fromAttribute(String value) {
		if(value == null) {
			return SPLINE; // The default value for the dot engine
		}
		if("".equals(value) || "none".equalsIgnoreCase(value)) {
			return NONE;
		}
		if("line".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
			return LINE;
		}
		if("polyline".equalsIgnoreCase(value) ) {
			return POLYLINE;
		}
		if("curved".equalsIgnoreCase(value) ) {
			return CURVED;
		}
		if("ortho".equalsIgnoreCase(value) ) {
			return ORTHO;
		}
		if("spline".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
			return LINE;
		}
		throw new IllegalArgumentException("Usupported Splines: " + value);
	}
	
}

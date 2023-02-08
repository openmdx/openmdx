/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Operations Mapper
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
package org.openmdx.application.mof.mapping.pimdoc;

import java.io.PrintWriter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;

/**
 * Behavioural Features Mapper
 */
class DeclaredOperationsMapper extends CompartmentMapper {
	
	DeclaredOperationsMapper(
		PrintWriter pw, 
		ModelElement_1_0 element, 
		Function<String, String> annotationRenderer
	){
		super(
			"declared-operations", "Declared Operations", "Operation ", //
			ModelElement_1_0::isOperationType, //
			true, pw, element, annotationRenderer, //
			"Name", "Abstract<br/>Kind", "Derived<br/>Type", "Query<br>&nbsp;"
		);
	}

	@Override
	protected void compartmentContent() {
		streamSortedElements().forEach(this::mapOperation);
	}

	private void mapOperation(ModelElement_1_0 current) {
		printLine("\t\t\t<table>");
		currentHead(current);
		printLine("\t\t\t\t<tbody class=\"uml-table-body\">");
		mapAnnotation(current);
  		mapNameAndFlags(current);
  		streamInParameters(current).forEach(this::mapInParameter);
  		streamResultParameters(current).forEach(this::mapResultParameter);
  		streamExceptions(current).forEach(this::mapException);
		printLine("\t\t\t\t</tbody>");
		printLine("\t\t\t</table>");
	}

	private void mapException(ModelElement_1_0 current) {
		printLine("\t\t\t\t\t<tr>");
  		mapName("throws ", current, "");
		printLine("\t\t\t\t\t\t<td>Exception</td>");
  		mapType(getType(current));
		printLine("\t\t\t\t\t</tr>");
	}
	
	private void mapInParameter(ModelElement_1_0 current) {
		printLine("\t\t\t\t\t<tr>");
  		mapName("in ", current, "");
		printLine("\t\t\t\t\t\t<td>Parameter</td>");
  		mapType(getType(current));
		printLine("\t\t\t\t\t</tr>");
	}

	private void mapResultParameter(ModelElement_1_0 current) {
		printLine("\t\t\t\t\t<tr>");
  		mapName("returns ", current, "");
		printLine("\t\t\t\t\t\t<td>return Value</td>");
  		mapType(getType(current));
		printLine("\t\t\t\t\t</tr>");
	}
	
	private void mapNameAndFlags(ModelElement_1_0 current) {
		printLine("\t\t\t\t\t<tr>");
  		mapName("", current, "()");
		mapAbstract(current);
		mapDerived(current);
		mapQuery(current);
		printLine("\t\t\t\t\t</tr>");
	}

	private void mapName(String namePrefix, ModelElement_1_0 current, String nameSuffix) {
		printLine(
			"\t\t\t\t\t\t<td>",
			current.getName(),
			nameSuffix,
			"</td>"
		);
	}

	private void mapType(ModelElement_1_0 type) {
		printLine("\t\t\t\t\t\t<td colspan=\"2\">");
		mapLink("\t\t\t\t\t\t\t", type);
		printLine("\t\t\t\t\t\t</td>");
	}
	
	private void mapAbstract(ModelElement_1_0 current) {
		mapBallotBox("\t\t\t\t\t\t", isAbstract(current), null);
	}

	private void mapDerived(ModelElement_1_0 current) {
		mapBallotBox("\t\t\t\t\t\t", isDerived(current), null);
	}

	private void mapQuery(ModelElement_1_0 current) {
		mapBallotBox("\t\t\t\t\t\t", isQuery(current), null);
	}
	
	private boolean isDerived(ModelElement_1_0 current){
		try {
			return Boolean.TRUE.equals(current.isDerived());
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}

	private boolean isQuery(ModelElement_1_0 current){
		return Boolean.TRUE.equals(current.objGetValue("isQuery"));
	}

	private boolean isAbstract(ModelElement_1_0 current) {
		try {
			return Boolean.TRUE.equals(current.isAbstract());
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}

	private Stream<ModelElement_1_0> streamReferences(ModelElement_1_0 current, String kind){
		final List<Object> list = current.objGetList(kind);
		return list == null ? Stream.empty() : list.stream().map(this::getElement);
	}

	private Stream<ModelElement_1_0> streamExceptions(ModelElement_1_0 current){
		return streamReferences(current, "exception");
	}
	
	private Stream<ModelElement_1_0> streamInParameters(ModelElement_1_0 current){
		return streamReferences(current, "content").filter(this::isInParameter);
	}
	
	private Stream<ModelElement_1_0> streamResultParameters(ModelElement_1_0 current){
		return streamReferences(current, "content").filter(this::isResultParameter);
	}
	
	private boolean isInParameter(ModelElement_1_0 param) {
        return org.omg.mof.cci.DirectionKind.IN_DIR.equals(param.objGetValue("direction"));
	}

	private boolean isResultParameter(ModelElement_1_0 param) {
        return "result".equals(param.getName());
	}
	
}
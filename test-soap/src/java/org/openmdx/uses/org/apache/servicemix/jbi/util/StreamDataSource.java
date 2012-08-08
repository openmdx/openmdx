/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmdx.uses.org.apache.servicemix.jbi.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * Stream DataSource for Mail and message attachments .
 * @author <a href="mailto:gnodet@logicblaze.com"> Guillaume Nodet</a>
 * @since 3.0
 */
public class StreamDataSource implements DataSource {

	private InputStream in;
	private String contentType;
	private String name;
	
	public StreamDataSource(InputStream in) {
		this(in, null, null);
	}

	public StreamDataSource(InputStream in, String contentType) {
		this(in, contentType, null);
	}

	public StreamDataSource(InputStream in, String contentType, String name) {
		this.in = in;
		this.contentType = contentType;
		this.name = name;
	}

	public InputStream getInputStream() throws IOException {
		if (in == null) throw new IOException("no data");
		return in;
	}

	public OutputStream getOutputStream() throws IOException {
    	throw new IOException("getOutputStream() not supported");
	}

	public String getContentType() {
		return contentType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}

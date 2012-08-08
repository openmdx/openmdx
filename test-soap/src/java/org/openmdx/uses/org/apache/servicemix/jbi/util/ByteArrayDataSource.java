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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.activation.DataSource;

/**
 * Byte array DataSource for Mail and message attachments 
 * @author George Gastaldi
 * @since 3.0
 */
public class ByteArrayDataSource implements DataSource, Serializable {

    private static final long serialVersionUID = 1L;
    
    private byte[] data;
    private String type;
    private String name = "unused";
    
    public ByteArrayDataSource(byte[] data, String type) {
        this.data = data;
        this.type = type;
    }

    public InputStream getInputStream() throws IOException {
		if (data == null) throw new IOException("no data");
		return new ByteArrayInputStream(data);
    }

    public OutputStream getOutputStream() throws IOException {
    	throw new IOException("getOutputStream() not supported");
    }

    public String getContentType() {
        return type;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
    	this.name = name;
    }
}
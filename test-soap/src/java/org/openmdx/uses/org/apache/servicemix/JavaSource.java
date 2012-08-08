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
package org.openmdx.uses.org.apache.servicemix;

import javax.xml.transform.Source;

/**
 * Represents an API to a JAXP {@link Source} which is capable of holding on to a POJO
 *
 * @version $Revision: 1.1 $
 */
public interface JavaSource extends Source {

    /**
     * Returns the POJO equivalent of the Source
     */
    public Object getObject();

    /**
     * Sets the POJO equivalent of the Source
     */
    public void setObject(Object object);
}

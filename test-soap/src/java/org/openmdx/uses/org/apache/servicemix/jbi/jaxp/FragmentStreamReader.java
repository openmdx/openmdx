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
package org.openmdx.uses.org.apache.servicemix.jbi.jaxp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

public class FragmentStreamReader extends StreamReaderDelegate implements XMLStreamReader {

	private static final int STATE_START_DOC = 0;
	private static final int STATE_FIRST_ELEM = 1;
	private static final int STATE_FIRST_RUN = 2;
	private static final int STATE_RUN = 3;
	private static final int STATE_END_DOC = 4;
	
	private int depth;
	private int state = STATE_START_DOC;
	private int event = START_DOCUMENT;
	private List rootPrefixes;
	
	public FragmentStreamReader(XMLStreamReader parent) {
		super(parent);
		rootPrefixes = new ArrayList();
		NamespaceContext ctx = getParent().getNamespaceContext();
		if (ctx instanceof ExtendedNamespaceContext) {
			Iterator it = ((ExtendedNamespaceContext) ctx).getPrefixes();
			while (it.hasNext()) {
				String prefix = (String) it.next();
				rootPrefixes.add(prefix);
			}
		}
	}
	
	public int getEventType() {
		return event;
	}

	public int next() throws XMLStreamException {
		switch (state) {
		case STATE_START_DOC:
			state = STATE_FIRST_ELEM;
			event = START_DOCUMENT;
			break;
		case STATE_FIRST_ELEM:
			state = STATE_FIRST_RUN;
			depth++;
			event = START_ELEMENT;
			break;
		case STATE_FIRST_RUN:
			state = STATE_RUN;
			// do not break
		case STATE_RUN:
			event = getParent().next();
			if (event == START_ELEMENT) {
				depth++;
			} else if (event == END_ELEMENT) {
				depth--;
				if (depth == 0) {
					state = STATE_END_DOC;
				}
			}
			break;
		case STATE_END_DOC:
			event = END_DOCUMENT;
			break;
		default:
			throw new IllegalStateException();
		}
		return event;
	}

    public int nextTag() throws XMLStreamException {
        int eventType = next();
        while((eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) // skip whitespace
            || (eventType == XMLStreamConstants.CDATA && isWhiteSpace()) // skip whitespace
            || eventType == XMLStreamConstants.SPACE
            || eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
            || eventType == XMLStreamConstants.COMMENT) {
            eventType = next();
         }
         if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
             throw new XMLStreamException("expected start or end tag", getLocation());
         }
         return eventType;    
    }
    
    public int getNamespaceCount() {
    	if (state == STATE_FIRST_RUN) {
    		return rootPrefixes.size();
    	} else {
    		return getParent().getNamespaceCount();
    	}
    }
    
    public String getNamespacePrefix(int i) {
    	if (state == STATE_FIRST_RUN) {
	    	return (String) rootPrefixes.get(i);
    	} else {
    		return getParent().getNamespacePrefix(i);
    	}
    }
    
    public String getNamespaceURI(int i) {
    	if (state == STATE_FIRST_RUN) {
	    	return getParent().getNamespaceContext().getNamespaceURI((String) rootPrefixes.get(i));
    	} else {
    		return getParent().getNamespaceURI(i);
    	}
    }
    
    public String getNamespaceURI(String prefix) {
    	if (state == STATE_FIRST_RUN) {
	    	return getParent().getNamespaceContext().getNamespaceURI(prefix);
    	} else {
    		return getParent().getNamespaceURI(prefix);
    	}
    }
    
}

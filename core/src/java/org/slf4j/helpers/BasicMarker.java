/* 
 * Copyright (c) 2004-2007 QOS.ch
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * ______________________________________________________________________
 *
 * Copyright (c) 2007-2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * JAVA 5 support added
 */
package org.slf4j.helpers;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.slf4j.Marker;

/**
 * An almost trivial implementation of the {@link Marker} interface.
 * 
 * <p>
 * <code>BasicMarker</code> lets users specify marker information. However, it
 * does not offer any useful operations on that information.
 * 
 * <p>
 * Simple logging systems which ignore marker data, just return instances of
 * this class in order to conform to the SLF4J API.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Joern Huxhorn
 * ______________________________________________________________________
 *
 * Copyright (c) 2007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * JAVA 5 support added
 */
public class BasicMarker implements Marker {

  private static final long serialVersionUID = 1803952589649545191L;

  final String name;
  List<Marker> children;
  final static private List<Marker> NO_CHILDREN = Collections.emptyList();

  BasicMarker(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public synchronized void add(Marker child) {
    if (child == null) {
      throw new NullPointerException(
          "Null children cannot be added to a Marker.");
    }
    if (children == null) {
      children = new Vector<Marker>();
    }

    // no point in adding the child multiple times
    if (children.contains(child)) {
      return;
    } else {
      // add the child
      children.add(child);
    }

  }

  public synchronized boolean hasChildren() {
    return ((children != null) && (children.size() > 0));
  }

  public synchronized Iterator<Marker> iterator() {
      return (
         children == null ? NO_CHILDREN : children
      ).iterator();
  }

  public synchronized boolean remove(Marker markerToRemove) {
    if (children == null) {
      return false;
    }

    int size = children.size();
    for (int i = 0; i < size; i++) {
      Marker m = children.get(i);
      if (m == markerToRemove) {
        children.remove(i);
        return true;
      }
    }
    // could not find markerToRemove
    return false;
  }

  public boolean contains(Marker other) {
    if (other == null) {
      throw new IllegalArgumentException("Other cannot be null");
    }

    if (this == other) {
      return true;
    }

    if (hasChildren()) {
      for (int i = 0; i < children.size(); i++) {
        Marker child = children.get(i);
        if (child.contains(other)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * This method is mainly used with Expression Evaluators.
   */
  public boolean contains(String name) {
    if (name == null) {
      throw new IllegalArgumentException("Other cannot be null");
    }

    if (this.name.equals(name)) {
      return true;
    }

    if (hasChildren()) {
      for (int i = 0; i < children.size(); i++) {
        Marker child = children.get(i);
        if (child.contains(name)) {
          return true;
        }
      }
    }
    return false;
  }

  private static String OPEN = "[ ";
  private static String CLOSE = " ]";
  private static String SEP = ", ";

  public String toString() {

    if (!this.hasChildren()) {
      return this.getName();
    }

    Iterator<Marker> it = this.iterator();
    Marker child;
    StringBuffer sb = new StringBuffer(this.getName());
    sb.append(' ').append(OPEN);
    while (it.hasNext()) {
      child = it.next();
      sb.append(child.getName());
      if (it.hasNext()) {
        sb.append(SEP);
      }
    }
    sb.append(CLOSE);

    return sb.toString();
  }
}

/*
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openmdx.dalvik.uses.java.beans;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Vector;

/**
 * The {@code XMLEncoder} class is a complementary alternative to
 * the {@code ObjectOutputStream} and can used to generate
 * a textual representation of a <em>JavaBean</em> in the same
 * way that the {@code ObjectOutputStream} can
 * be used to create binary representation of {@code Serializable}
 * objects. For example, the following fragment can be used to create
 * a textual representation the supplied <em>JavaBean</em>
 * and all its properties:
 * <pre>
 *       XMLEncoder e = new XMLEncoder(
 *                          new BufferedOutputStream(
 *                              new FileOutputStream("Test.xml")));
 *       e.writeObject(new JButton("Hello, world"));
 *       e.close();
 * </pre>
 * Despite the similarity of their APIs, the {@code XMLEncoder}
 * class is exclusively designed for the purpose of archiving graphs
 * of <em>JavaBean</em>s as textual representations of their public
 * properties. Like Java source files, documents written this way
 * have a natural immunity to changes in the implementations of the classes
 * involved. The {@code ObjectOutputStream} continues to be recommended
 * for interprocess communication and general purpose serialization.
 * <p>
 * The {@code XMLEncoder} class provides a default denotation for
 * <em>JavaBean</em>s in which they are represented as XML documents
 * complying with version 1.0 of the XML specification and the
 * UTF-8 character encoding of the Unicode/ISO 10646 character set.
 * The XML documents produced by the {@code XMLEncoder} class are:
 * <ul>
 * <li>
 * <em>Portable and version resilient</em>: they have no dependencies
 * on the private implementation of any class and so, like Java source
 * files, they may be exchanged between environments which may have
 * different versions of some of the classes and between VMs from
 * different vendors.
 * <li>
 * <em>Structurally compact</em>: The {@code XMLEncoder} class
 * uses a <em>redundancy elimination</em> algorithm internally so that the
 * default values of a Bean's properties are not written to the stream.
 * <li>
 * <em>Fault tolerant</em>: Non-structural errors in the file,
 * caused either by damage to the file or by API changes
 * made to classes in an archive remain localized
 * so that a reader can report the error and continue to load the parts
 * of the document which were not affected by the error.
 * </ul>
 * <p>
 * The XML syntax uses the following conventions:
 * <ul>
 * <li>
 * Each element represents a method call.
 * <li>
 * The "object" tag denotes an <em>expression</em> whose value is
 * to be used as the argument to the enclosing element.
 * <li>
 * The "void" tag denotes a <em>statement</em> which will
 * be executed, but whose result will not be used as an
 * argument to the enclosing method.
 * <li>
 * Elements which contain elements use those elements as arguments,
 * unless they have the tag: "void".
 * <li>
 * The name of the method is denoted by the "method" attribute.
 * <li>
 * XML's standard "id" and "idref" attributes are used to make
 * references to previous expressions - so as to deal with
 * circularities in the object graph.
 * <li>
 * The "class" attribute is used to specify the target of a static
 * method or constructor explicitly; its value being the fully
 * qualified name of the class.
 * <li>
 * Elements with the "void" tag are executed using
 * the outer context as the target if no target is defined
 * by a "class" attribute.
 * <li>
 * Java's String class is treated specially and is
 * written &lt;string&gt;Hello, world&lt;/string&gt; where
 * the characters of the string are converted to bytes
 * using the UTF-8 character encoding.
 * </ul>
 * <p>
 * Although all object graphs may be written using just these three
 * tags, the following definitions are included so that common
 * data structures can be expressed more concisely:
 * <p>
 * <ul>
 * <li>
 * The default method name is "new".
 * <li>
 * A reference to a java class is written in the form
 *  &lt;class&gt;javax.swing.JButton&lt;/class&gt;.
 * <li>
 * Instances of the wrapper classes for Java's primitive types are written
 * using the name of the primitive type as the tag. For example, an
 * instance of the {@code Integer} class could be written:
 * &lt;int&gt;123&lt;/int&gt;. Note that the {@code XMLEncoder} class
 * uses Java's reflection package in which the conversion between
 * Java's primitive types and their associated "wrapper classes"
 * is handled internally. The API for the {@code XMLEncoder} class
 * itself deals only with {@code Object}s.
 * <li>
 * In an element representing a nullary method whose name
 * starts with "get", the "method" attribute is replaced
 * with a "property" attribute whose value is given by removing
 * the "get" prefix and decapitalizing the result.
 * <li>
 * In an element representing a monadic method whose name
 * starts with "set", the "method" attribute is replaced
 * with a "property" attribute whose value is given by removing
 * the "set" prefix and decapitalizing the result.
 * <li>
 * In an element representing a method named "get" taking one
 * integer argument, the "method" attribute is replaced
 * with an "index" attribute whose value the value of the
 * first argument.
 * <li>
 * In an element representing a method named "set" taking two arguments,
 * the first of which is an integer, the "method" attribute is replaced
 * with an "index" attribute whose value the value of the
 * first argument.
 * <li>
 * A reference to an array is written using the "array"
 * tag. The "class" and "length" attributes specify the
 * sub-type of the array and its length respectively.
 * </ul>
 *
 *<p>
 * For more information you might also want to check out
 * <a
 href="http://java.sun.com/products/jfc/tsc/articles/persistence4">Using XMLEncoder</a>,
 * an article in <em>The Swing Connection.</em>
 * @see XMLDecoder
 * @see java.io.ObjectOutputStream
 * 
 * <p>
 * openMDX/Dalvik Notice (January 2013):<br>
 * THIS CODE HAS BEEN MODIFIED AND ITS NAMESPACE HAS BEEN PREFIXED WITH
 * {@code org.openmdx.dalvik.uses.}

 * </p>
 * @since openMDX 2.12
 * @author openMDX Team
 *
 * @author Philip Milne
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class XMLEncoder extends Encoder implements AutoCloseable {

    private static String encoding = "UTF-8";

    private OutputStream out;
    private Object owner;
    private int indentation = 0;
    private boolean internal = false;
    private Map valueToExpression;
    private Map targetToStatementList;
    private boolean preambleWritten = false;
    private NameGenerator nameGenerator;

    private class ValueData {
        public int refs = 0;
        public boolean marked = false; // Marked -> refs > 0 unless ref was a target.
        public String name = null;
        public Expression exp = null;
    }

    /**
     * Creates a new output stream for sending <em>JavaBeans</em>
     * to the stream {@code out} using an XML encoding.
     *
     * @param out The stream to which the XML representation of
     * the objects will be sent.
     *
     * @see XMLDecoder#XMLDecoder(InputStream)
     */
    public XMLEncoder(OutputStream out) {
        this.out = out;
        valueToExpression = new IdentityHashMap();
        targetToStatementList = new IdentityHashMap();
        nameGenerator = new NameGenerator();
    }

    /**
     * Sets the owner of this encoder to {@code owner}.
     *
     * @param owner The owner of this encoder.
     *
     * @see #getOwner
     */
    public void setOwner(Object owner) {
        this.owner = owner;
        writeExpression(new Expression(this, "getOwner", new Object[0]));
    }

    /**
     * Gets the owner of this encoder.
     *
     * @return The owner of this encoder.
     *
     * @see #setOwner
     */
    public Object getOwner() {
        return owner;
    }

    /**
     * Write an XML representation of the specified object to the output.
     *
     * @param o The object to be written to the stream.
     *
     * @see XMLDecoder#readObject
     */
    public void writeObject(Object o) {
        if (internal) {
            super.writeObject(o);
        }
        else {
            writeStatement(new Statement(this, "writeObject", new Object[]{o}));
        }
    }

    private Vector statementList(Object target) {
        Vector list = (Vector)targetToStatementList.get(target);
        if (list != null) {
            return list;
        }
        list = new Vector();
        targetToStatementList.put(target, list);
        return list;
    }


    private void mark(Object o, boolean isArgument) {
        if (o == null || o == this) {
            return;
        }
        ValueData d = getValueData(o);
        Expression exp = d.exp;
        // Do not mark liternal strings. Other strings, which might,
        // for example, come from resource bundles should still be marked.
        if (o.getClass() == String.class && exp == null) {
            return;
        }

        // Bump the reference counts of all arguments
        if (isArgument) {
            d.refs++;
        }
        if (d.marked) {
            return;
        }
        d.marked = true;
        Object target = exp.getTarget();
        if (!(target instanceof Class)) {
            statementList(target).add(exp);
            // Pending: Why does the reference count need to
            // be incremented here?
            d.refs++;
        }
        mark(exp);
    }

    private void mark(Statement stm) {
        Object[] args = stm.getArguments();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            mark(arg, true);
        }
        mark(stm.getTarget(), false);
    }


    /**
     * Records the Statement so that the Encoder will
     * produce the actual output when the stream is flushed.
     * <P>
     * This method should only be invoked within the context
     * of initializing a persistence delegate.
     *
     * @param oldStm The statement that will be written
     *               to the stream.
     * @see org.openmdx.dalvik.uses.java.beans.PersistenceDelegate#initialize
     */
    public void writeStatement(Statement oldStm) {
        // System.out.println("XMLEncoder::writeStatement: " + oldStm);
        boolean internal = this.internal;
        this.internal = true;
        try {
            super.writeStatement(oldStm);
            /*
               Note we must do the mark first as we may
               require the results of previous values in
               this context for this statement.
               Test case is:
                   os.setOwner(this);
                   os.writeObject(this);
            */
            mark(oldStm);
            statementList(oldStm.getTarget()).add(oldStm);
        }
        catch (Exception e) {
            getExceptionListener().exceptionThrown(new Exception("XMLEncoder: discarding statement " + oldStm, e));
        }
        this.internal = internal;
    }


    /**
     * Records the Expression so that the Encoder will
     * produce the actual output when the stream is flushed.
     * <P>
     * This method should only be invoked within the context of
     * initializing a persistence delegate or setting up an encoder to
     * read from a resource bundle.
     * <P>
     * For more information about using resource bundles with the
     * XMLEncoder, see
     * http://java.sun.com/products/jfc/tsc/articles/persistence4/#i18n
     *
     * @param oldExp The expression that will be written
     *               to the stream.
     * @see org.openmdx.dalvik.uses.java.beans.PersistenceDelegate#initialize
     */
    public void writeExpression(Expression oldExp) {
        boolean internal = this.internal;
        this.internal = true;
        Object oldValue = getValue(oldExp);
        if (get(oldValue) == null || (oldValue instanceof String && !internal)) {
            getValueData(oldValue).exp = oldExp;
            super.writeExpression(oldExp);
        }
        this.internal = internal;
    }

    /**
     * This method writes out the preamble associated with the
     * XML encoding if it has not been written already and
     * then writes out all of the values that been
     * written to the stream since the last time {@code flush}
     * was called. After flushing, all internal references to the
     * values that were written to this stream are cleared.
     */
    public void flush() {
        if (!preambleWritten) { // Don't do this in constructor - it throws ... pending.
            writeln("<?xml version=" + quote("1.0") +
                        " encoding=" + quote(encoding) + "?>");
            writeln("<java version=" + quote(System.getProperty("java.version")) +
                           " class=" + quote("java.beans.XMLDecoder") + ">");
            preambleWritten = true;
        }
        indentation++;
        Vector roots = statementList(this);
        for(int i = 0; i < roots.size(); i++) {
            Statement s = (Statement)roots.get(i);
            if ("writeObject".equals(s.getMethodName())) {
                outputValue(s.getArguments()[0], this, true);
            }
            else {
                outputStatement(s, this, false);
            }
        }
        indentation--;

        try {
            out.flush();
        }
        catch (IOException e) {
            getExceptionListener().exceptionThrown(e);
        }
        clear();
    }

    void clear() {
        super.clear();
        nameGenerator.clear();
        valueToExpression.clear();
        targetToStatementList.clear();
    }


    /**
     * This method calls {@code flush}, writes the closing
     * postamble and then closes the output stream associated
     * with this stream.
     */
    @Override
    public void close() {
        flush();
        writeln("</java>");
        try {
            out.close();
        }
        catch (IOException e) {
            getExceptionListener().exceptionThrown(e);
        }
    }

    private String quote(String s) {
        return "\"" + s + "\"";
    }

    private ValueData getValueData(Object o) {
        ValueData d = (ValueData)valueToExpression.get(o);
        if (d == null) {
            d = new ValueData();
            valueToExpression.put(o, d);
        }
        return d;
    }

    /**
     * Returns {@code true} if the argument,
     * a Unicode code point, is valid in XML documents.
     * Unicode characters fit into the low sixteen bits of a Unicode code point,
     * and pairs of Unicode <em>surrogate characters</em> can be combined
     * to encode Unicode code point in documents containing only Unicode.
     * (The {@code char} datatype in the Java Programming Language
     * represents Unicode characters, including unpaired surrogates.)
     * <par>
     * [2] Char ::= #x0009 | #x000A | #x000D
     *            | [#x0020-#xD7FF]
     *            | [#xE000-#xFFFD]
     *            | [#x10000-#x10ffff]
     * </par>
     *
     * @param code  the 32-bit Unicode code point being tested
     * @return  {@code true} if the Unicode code point is valid,
     *          {@code false} otherwise
     */
    private static boolean isValidCharCode(int code) {
        return (0x0020 <= code && code <= 0xD7FF)
            || (0x000A == code)
            || (0x0009 == code)
            || (0x000D == code)
            || (0xE000 <= code && code <= 0xFFFD)
            || (0x10000 <= code && code <= 0x10ffff);
    }

    private void writeln(String exp) {
        try {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < indentation; i++) {
                sb.append(' ');
            }
            sb.append(exp);
            sb.append('\n');
            this.out.write(sb.toString().getBytes(encoding));
        }
        catch (IOException e) {
            getExceptionListener().exceptionThrown(e);
        }
    }

    private void outputValue(Object value, Object outer, boolean isArgument) {
        if (value == null) {
            writeln("<null/>");
            return;
        }

        if (value instanceof Class) {
            writeln("<class>" + ((Class)value).getName() + "</class>");
            return;
        }

        ValueData d = getValueData(value);
        if (d.exp != null) {
            Object target = d.exp.getTarget();
            String methodName = d.exp.getMethodName();

            if (target == null || methodName == null) {
                throw new NullPointerException((target == null ? "target" :
                                                "methodName") + " should not be null");
            }

            if (target instanceof Field && methodName.equals("get")) {
                Field f = (Field)target;
                writeln("<object class=" + quote(f.getDeclaringClass().getName()) +
                        " field=" + quote(f.getName()) + "/>");
                return;
            }

            Class primitiveType = ReflectionUtils.primitiveTypeFor(value.getClass());
            if (primitiveType != null && target == value.getClass() &&
                methodName.equals("new")) {
                String primitiveTypeName = primitiveType.getName();
                // Make sure that character types are quoted correctly.
                if (primitiveType == Character.TYPE) {
                    char code = ((Character) value).charValue();
                    if (!isValidCharCode(code)) {
                        writeln(createString(code));
                        return;
                    }
                    value = quoteCharCode(code);
                    if (value == null) {
                        value = Character.valueOf(code);
                    }
                }
                writeln("<" + primitiveTypeName + ">" + value + "</" +
                        primitiveTypeName + ">");
                return;
            }

        } else if (value instanceof String) {
            writeln(createString((String) value));
            return;
        }

        if (d.name != null) {
            writeln("<object idref=" + quote(d.name) + "/>");
            return;
        }

        outputStatement(d.exp, outer, isArgument);
    }

    private static String quoteCharCode(int code) {
        switch(code) {
          case '&':  return "&amp;";
          case '<':  return "&lt;";
          case '>':  return "&gt;";
          case '"':  return "&quot;";
          case '\'': return "&apos;";
          case '\r': return "&#13;";
          default:   return null;
        }
    }

    private static String createString(int code) {
        return "<char code=\"#" + Integer.toString(code, 16) + "\"/>";
    }

    private String createString(String string) {
        CharsetEncoder encoder = Charset.forName(encoding).newEncoder();

        StringBuilder sb = new StringBuilder();
        sb.append("<string>");
        int index = 0;
        while (index < string.length()) {
            int point = string.codePointAt(index);
            int count = Character.charCount(point);

            if (isValidCharCode(point) && encoder.canEncode(string.substring(index, index + count))) {
                String value = quoteCharCode(point);
                if (value != null) {
                    sb.append(value);
                } else {
                    sb.appendCodePoint(point);
                }
                index += count;
            } else {
                sb.append(createString(string.charAt(index)));
                index++;
            }
/*
            String value = isValidCharCode(point) && encoder.canEncode(string.substring(index, index + count))
                    ? quoteCharCode(point)
                    : createString(point);

            if (value != null) {
                sb.append(value);
            } else {
                sb.appendCodePoint(point);
            }
            index += count;
*/
        }
        sb.append("</string>");
        return sb.toString();
    }

    private void outputStatement(Statement exp, Object outer, boolean isArgument) {
        Object target = exp.getTarget();
        String methodName = exp.getMethodName();

        if (target == null || methodName == null) {
            throw new NullPointerException((target == null ? "target" :
                                            "methodName") + " should not be null");
        }

        Object[] args = exp.getArguments();
        boolean expression = exp.getClass() == Expression.class;
        Object value = (expression) ? getValue((Expression)exp) : null;

        String tag = (expression && isArgument) ? "object" : "void";
        String attributes = "";
        ValueData d = getValueData(value);
        if (expression) {
            if (d.refs > 1) {
                String instanceName = nameGenerator.instanceName(value);
                d.name = instanceName;
                attributes = attributes + " id=" + quote(instanceName);
            }
        }

        // Special cases for targets.
        if (target == outer) {
        }
        else if (target == Array.class && methodName.equals("newInstance")) {
            tag = "array";
            attributes = attributes + " class=" + quote(((Class)args[0]).getName());
            attributes = attributes + " length=" + quote(args[1].toString());
            args = new Object[]{};
        }
        else if (target.getClass() == Class.class) {
            attributes = attributes + " class=" + quote(((Class)target).getName());
        }
        else {
            d.refs = 2;
            getValueData(target).refs++;
            outputValue(target, outer, false);
            if (isArgument) {
                outputValue(value, outer, false);
            }
            return;
        }


        // Special cases for methods.
        if ((!expression && methodName.equals("set") && args.length == 2 &&
             args[0] instanceof Integer) ||
             (expression && methodName.equals("get") && args.length == 1 &&
              args[0] instanceof Integer)) {
            attributes = attributes + " index=" + quote(args[0].toString());
            args = (args.length == 1) ? new Object[]{} : new Object[]{args[1]};
        }
        else if ((!expression && methodName.startsWith("set") && args.length == 1) ||
                 (expression && methodName.startsWith("get") && args.length == 0)) {
            attributes = attributes + " property=" +
                quote(Introspector.decapitalize(methodName.substring(3)));
        }
        else if (!methodName.equals("new") && !methodName.equals("newInstance")) {
            attributes = attributes + " method=" + quote(methodName);
        }

        Vector statements = statementList(value);
        // Use XML's short form when there is no body.
        if (args.length == 0 && statements.size() == 0) {
            writeln("<" + tag + attributes + "/>");
            return;
        }

        writeln("<" + tag + attributes + ">");
        indentation++;

        for(int i = 0; i < args.length; i++) {
            outputValue(args[i], null, true);
        }

        for(int i = 0; i < statements.size(); i++) {
            Statement s = (Statement)statements.get(i);
            outputStatement(s, value, false);
        }

        indentation--;
        writeln("</" + tag + ">");
    }
}

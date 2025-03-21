/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Performance Test
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
 * distribution.
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
package test.openmdx.base.resource;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.IndexedRecord;
import jakarta.resource.cci.MappedRecord;
#endif
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.io.UTF8Writer;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.RestFormatters;
import org.openmdx.base.rest.spi.RestParser;
import org.openmdx.base.rest.spi.RestSource;
import org.openmdx.base.rest.stream.RestTarget;
import org.openmdx.base.rest.stream.StandardRestFormatter;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.state2.cci.DateStateViews;
import org.xml.sax.InputSource;

public class PerformanceTest {

	static IndexedRecord testData;
	private static final int SIZE = 100;
	private static final int LOOP = 100;
	protected static final Path BASE = new Path("xri://@openmdx*test.app1/segment/JUnit");
	protected static final String URI = "xri://+test/REST";
	protected static final boolean WBXML = false;

	/**
	 * The eagerly acquired REST formatter instance
	 */
	protected static final StandardRestFormatter restFormatter = RestFormatters.getFormatter();

	private final SerializationTest[] tests = { new SerializationTest("Java (Externalizable)") {

		private BinarySink sink = new BinarySink();
		private boolean prolog = true;

		/*
		 * (non-Javadoc)
		 * 
		 * @see test.openmdx.base.resource.TestPerformance.SerializationTest#reset()
		 */
		@Override
		protected void reset() throws Exception {
			super.reset();
			this.prolog = false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * test.openmdx.base.resource.TestPerformance.SerializationTest#deserialize()
		 */
		@Override
		public void deserialize() throws Exception {
			ObjectInputStream source = new ObjectInputStream(sink.asSource());
			source.readObject();
			if (this.prolog) {
				Assertions.assertEquals("openMDX", (String) source.readObject());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see test.openmdx.base.resource.TestPerformance.SerializationTest#serialize()
		 */
		@Override
		public int serialize(IndexedRecord data) throws Exception {
			this.sink.reset();
			ObjectOutputStream target = new ObjectOutputStream(sink);
			target.writeObject(data);
			if (this.prolog) {
				target.writeObject("openMDX");
			}
			target.flush();
			return this.sink.size();
		}

	}, new SerializationTest("XML (UTF-16)") {

		private UTF16Sink sink = new UTF16Sink();

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * test.openmdx.base.resource.TestPerformance.SerializationTest#deserialize()
		 */
		@Override
		public void deserialize() throws Exception {
			RestSource source = sink.asSource();
			IndexedRecord resultSet = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
			RestParser.parseResponse(resultSet, source);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see test.openmdx.base.resource.TestPerformance.SerializationTest#serialize()
		 */
		@Override
		public int serialize(IndexedRecord resultSet) throws Exception {
			this.sink.reset();
			restFormatter.format(sink, BASE, resultSet);
			return sink.size();
		}

	}, new SerializationTest("XML (UTF-8)") {

		private UTF8Sink sink = new UTF8Sink();

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * test.openmdx.base.resource.TestPerformance.SerializationTest#deserialize()
		 */
		@Override
		public void deserialize() throws Exception {
			RestSource source = sink.asSource();
			IndexedRecord resultSet = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
			RestParser.parseResponse(resultSet, source);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see test.openmdx.base.resource.TestPerformance.SerializationTest#serialize()
		 */
		@Override
		public int serialize(IndexedRecord resultSet) throws Exception {
			this.sink.reset();
			restFormatter.format(sink, BASE, resultSet);
			return sink.size();
		}

	}, new SerializationTest("WBXML (UTF-8)") {

		private WBXMLSink sink = new WBXMLSink();

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * test.openmdx.base.resource.TestPerformance.SerializationTest#deserialize()
		 */
		@Override
		public void deserialize() throws Exception {
			RestSource source = this.sink.asSource();
			IndexedRecord resultSet = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
			RestParser.parseResponse(resultSet, source);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see test.openmdx.base.resource.TestPerformance.SerializationTest#serialize()
		 */
		@Override
		public int serialize(IndexedRecord resultSet) throws Exception {
			this.sink.reset();
			restFormatter.format(sink, BASE, resultSet);
			return this.sink.size();
		}

	} };

	@Test
	public void testSerialization() throws Exception {
		for (SerializationTest test : tests) {
			test.run(testData);
		}
		for (SerializationTest test : tests) {
			test.reset();
		}
		for (int i = 0; i < LOOP; i++) {
			for (SerializationTest test : tests) {
				test.run(testData);
			}
		}
		for (SerializationTest test : tests) {
			test.epilog();
		}
	}

	@BeforeAll
	@SuppressWarnings("unchecked")
	static public void setUp() throws ResourceException {
		XMLGregorianCalendar today = DateStateViews.today();
		testData = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
		for (int i = 0; i < SIZE; i++) {
			if (i < SIZE / 5) {
				//
				// 20% Mixed Data
				//
				Path xri = BASE.getDescendant("nonStated", String.valueOf(i));
				Object_2Facade object = Object_2Facade.newInstance(xri, "test:openmdx:datatypes1:Data");
				MappedRecord entry = object.getValue();
				entry.put("value1", Boolean.valueOf(i % 2 == 1)); // odd
				entry.put("value2", i);
				IndexedRecord value3 = Records.getRecordFactory().createIndexedRecord("list");
				value3.add(i * 1000);
				value3.add(1 * 1000000);
				entry.put("value3", value3);
				long value4 = System.currentTimeMillis();
				entry.put("value4", value4);
				BigDecimal value5 = BigDecimal.valueOf(i, -9);
				entry.put("value5", value5);
				UUID value6 = UUIDs.newUUID();
				entry.put("value6", value6.toString());
				entry.put("value7", #if CLASSIC_CHRONO_TYPES new java.util.Date #else java.time.Instant.ofEpochMilli#endif(value4));
				entry.put("value8", today);
				entry.put("value9", UUIDConversion.toURI(value6));
				BigInteger value10 = BigInteger.valueOf(1000000 * i);
				entry.put("value10", value10.toByteArray());
				testData.add(object.getDelegate());
			} else {
				//
				// 80% Character Data
				//
				Path xri = BASE.getDescendant("address", String.valueOf(i));
				Object_2Facade object = Object_2Facade.newInstance(xri, "test:openmdx:app1:PostalAddress");
				MappedRecord entry = object.getValue();
				entry.put("identity", xri.toXRI());
				IndexedRecord by = Records.getRecordFactory().createIndexedRecord("list");
				by.add("ownerPrincipal");
				by.add("group1Principal");
				by.add("group2Principal");
				by.add("group3Principal");
				#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif at = SystemClock.getInstance().now();
				entry.put(SystemAttributes.CREATED_AT, at);
				entry.put(SystemAttributes.CREATED_BY, by);
				entry.put(SystemAttributes.MODIFIED_AT, at);
				entry.put(SystemAttributes.MODIFIED_BY, by);
				entry.put("id", "address # " + i);
				IndexedRecord line = Records.getRecordFactory().createIndexedRecord("list");
				line.add("Address " + i + " line 1");
				line.add("Address " + i + " line 2");
				line.add("Address " + i + " line 3");
				entry.put("addressLine", line);
				entry.put("city", "Zurich");
				entry.put("houseNumber", "4711");
				entry.put("street", "Main street");
				entry.put("postalCode", "CH-8000");
				testData.add(object.getDelegate());
			}
		}
	}

	static abstract class SerializationTest {

		SerializationTest(String name) {
			this.name = name;
		}

		private final String name;

		private long elapsedTimeForSerialization;
		private long elapsedTimeForDeserialization;
		private long byteCount;

		protected void reset() throws Exception {
			this.elapsedTimeForSerialization = 0l;
			this.elapsedTimeForDeserialization = 0l;
			this.byteCount = 0l;
		}

		protected abstract int serialize(IndexedRecord resultSet) throws Exception;

		protected abstract void deserialize() throws Exception;

		protected void run(IndexedRecord data) throws Exception {
			long serializationStart = System.nanoTime();
			this.byteCount += serialize(data);
			this.elapsedTimeForSerialization += System.nanoTime() - serializationStart;
			long deserializationStart = System.nanoTime();
			deserialize();
			this.elapsedTimeForDeserialization += System.nanoTime() - deserializationStart;
		}

		protected void epilog() {
			System.out.println(name + " processing a result set with " + SIZE + " objects");
			System.out.println("\tserialization: " + (elapsedTimeForSerialization / 1000 / LOOP) + " \u00b5s");
			System.out.println("\tdeserialization: " + (elapsedTimeForDeserialization / 1000 / LOOP) + " \u00b5s");
			System.out.println("\ttotal: "
					+ ((elapsedTimeForDeserialization + elapsedTimeForDeserialization) / 1000 / LOOP) + " \u00b5s");
			System.out.println("\tdata: " + (byteCount / LOOP) + " bytes");
		}

	}

	static class WBXMLSink extends RestTarget {

		/**
		 * Constructor
		 */
		protected WBXMLSink() {
			super(URI);
		}

		private static final String MIME_TYPE = "application/vnd.openmdx.wbxml";

		private final BinarySink sink = new BinarySink();

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.openmdx.application.rest.http.RestFormat.Target#newWriter()
		 */
		@Override
		protected XMLStreamWriter newWriter() throws XMLStreamException {
			try {
				return restFormatter.getOutputFactory(MIME_TYPE).createXMLStreamWriter(sink);
			} catch (BasicException exception) {
				throw new XMLStreamException(exception);
			}
		}

		@Override
		public void reset() {
			super.reset();
			this.sink.reset();
		}

		RestSource asSource() {
			return new RestSource(getBase(), new InputSource(sink.asSource()), MIME_TYPE, null);
		}

		int size() {
			return sink.size();
		}

	}

	static class UTF16Sink extends RestTarget {

		/**
		 * Constructor
		 */
		protected UTF16Sink() {
			super(URI);
		}

		private final StringWriter sink = new StringWriter();

		private static final String MIME_TYPE = "text/xml";

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.openmdx.application.rest.http.RestFormat.Target#newWriter()
		 */
		@Override
		protected XMLStreamWriter newWriter() throws XMLStreamException {
			try {
				return restFormatter.getOutputFactory(MIME_TYPE).createXMLStreamWriter(sink);
			} catch (BasicException exception) {
				throw new XMLStreamException(exception);
			}
		}

		@Override
		public void reset() {
			super.reset();
			this.sink.getBuffer().setLength(0);
		}

		RestSource asSource() {
			return new RestSource(getBase(), new InputSource(new StringReader(sink.getBuffer().toString())), MIME_TYPE,
					null);
		}

		int size() {
			return 2 * this.sink.getBuffer().length();
		}

	}

	static class UTF8Sink extends RestTarget {

		/**
		 * Constructor
		 */
		protected UTF8Sink() {
			super(URI);
		}

		private static final String MIME_TYPE = "application/xml";

		private final BinarySink sink = new BinarySink();

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.openmdx.application.rest.http.RestFormat.Target#newWriter()
		 */
		@Override
		protected XMLStreamWriter newWriter() throws XMLStreamException {
			try {
				return restFormatter.getOutputFactory(MIME_TYPE).createXMLStreamWriter(new UTF8Writer(sink));
			} catch (BasicException exception) {
				throw new XMLStreamException(exception);
			}
		}

		@Override
		public void reset() {
			super.reset();
			this.sink.reset();
		}

		RestSource asSource() {
			InputSource source = new InputSource(this.sink.asSource());
			source.setEncoding("UTF-8");
			return new RestSource(getBase(), source, MIME_TYPE, null);
		}

		int size() {
			return this.sink.size();
		}

	}

	/**
	 * Binary Buffer
	 */
	static class BinarySink extends OutputStream {

		/**
		 * The buffer where data is stored.
		 */
		protected byte buf[];

		/**
		 * The number of valid bytes in the buffer.
		 */
		protected int count;

		/**
		 * Creates a new byte array output stream. The buffer capacity is initially 32
		 * bytes, though its size increases if necessary.
		 */
		public BinarySink() {
			this(32);
		}

		/**
		 * Creates a new byte array output stream, with a buffer capacity of the
		 * specified size, in bytes.
		 *
		 * @param size the initial size.
		 * @exception IllegalArgumentException if size is negative.
		 */
		public BinarySink(int size) {
			buf = new byte[size];
		}

		/**
		 * Writes the specified byte to this byte array output stream.
		 *
		 * @param b the byte to be written.
		 */
		@Override
		public void write(int b) {
			int newcount = count + 1;
			if (newcount > buf.length) {
				buf = ArraysExtension.copyOf(buf, Math.max(buf.length << 1, newcount));
			}
			buf[count] = (byte) b;
			count = newcount;
		}

		/**
		 * Writes {@code len} bytes from the specified byte array starting at offset
		 * {@code off} to this byte array output stream.
		 *
		 * @param b   the data.
		 * @param off the start offset in the data.
		 * @param len the number of bytes to write.
		 */
		@Override
		public void write(byte b[], int off, int len) {
			if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return;
			}
			int newcount = count + len;
			if (newcount > buf.length) {
				buf = ArraysExtension.copyOf(buf, Math.max(buf.length << 1, newcount));
			}
			System.arraycopy(b, off, buf, count, len);
			count = newcount;
		}

		public void reset() {
			count = 0;
		}

		InputStream asSource() {
			return new BinarySource(this.buf, 0, this.count);
		}

		int size() {
			return count;
		}
	}

	static class BinarySource extends InputStream {

		protected byte buf[];

		protected int pos;

		protected int mark = 0;

		protected int count;

		public BinarySource(byte buf[]) {
			this.buf = buf;
			this.pos = 0;
			this.count = buf.length;
		}

		public BinarySource(byte buf[], int offset, int length) {
			this.buf = buf;
			this.pos = offset;
			this.count = Math.min(offset + length, buf.length);
			this.mark = offset;
		}

		@Override
		public int read() {
			return (pos < count) ? (buf[pos++] & 0xff) : -1;
		}

		@Override
		public int read(byte b[], int off, int len) {
			if (b == null) {
				throw new NullPointerException();
			} else if (off < 0 || len < 0 || len > b.length - off) {
				throw new IndexOutOfBoundsException();
			}
			if (pos >= count) {
				return -1;
			}
			if (pos + len > count) {
				len = count - pos;
			}
			if (len <= 0) {
				return 0;
			}
			System.arraycopy(buf, pos, b, off, len);
			pos += len;
			return len;
		}

		@Override
		public long skip(long n) {
			if (pos + n > count) {
				n = count - pos;
			}
			if (n < 0) {
				return 0;
			}
			pos += n;
			return n;
		}

		@Override
		public int available() {
			return count - pos;
		}

		@Override
		public boolean markSupported() {
			return true;
		}

		@Override
		public void mark(int readAheadLimit) {
			mark = pos;
		}

		@Override
		public void reset() {
			pos = mark;
		}

	}

}

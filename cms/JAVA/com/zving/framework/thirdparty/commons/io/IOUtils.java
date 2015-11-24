/*
 * Copyright 2001-2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zving.framework.thirdparty.commons.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * General IO stream manipulation utilities.
 * <p>
 * This class provides static utility methods for input/output operations.
 * <ul>
 * <li>closeQuietly - these methods close a stream ignoring nulls and exceptions
 * <li>toXxx/read - these methods read data from a stream
 * <li>write - these methods write data to a stream
 * <li>copy - these methods copy all the data from one stream to another
 * <li>contentEquals - these methods compare the content of two streams
 * </ul>
 * <p>
 * The byte-to-char methods and char-to-byte methods involve a conversion step. Two methods are provided in each case, one that uses the
 * platform default encoding and the other which allows you to specify an encoding. You are encouraged to always specify an encoding because
 * relying on the platform default can lead to unexpected results, for example when moving from development to production.
 * <p>
 * All the methods in this class that read a stream are buffered internally. This means that there is no cause to use a
 * <code>BufferedInputStream</code> or <code>BufferedReader</code>. The default buffer size of 4K has been shown to be efficient in tests.
 * <p>
 * Wherever possible, the methods in this class do <em>not</em> flush or close the stream. This is to avoid making non-portable assumptions
 * about the streams' origin and further use. Thus the caller is still responsible for closing streams after use.
 * <p>
 * Origin of code: Excalibur.
 * 
 * @author Peter Donald
 * @author Jeff Turner
 * @author Matthew Hawthorne
 * @author Stephen Colebourne
 * @author Gareth Davis
 * @author Ian Springer
 * @author Niall Pemberton
 * @author Sandy McArthur
 * @version $Id: IOUtils.java 385117 2006-03-11 18:07:02Z scolebourne $
 */
public class IOUtils {
	// NOTE: This class is focussed on InputStream, OutputStream, Reader and
	// Writer. Each method should take at least one of these as a parameter,
	// or return one of them.

	/**
	 * The system line separator string.
	 */
	static {
		// avoid security issues
		StringWriter buf = new StringWriter(4);
		PrintWriter out = new PrintWriter(buf);
		out.println();
	}

	/**
	 * The default buffer size to use.
	 */
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	/**
	 * Instances should NOT be constructed in standard programming.
	 */
	public IOUtils() {
		super();
	}

	/**
	 * Get the contents of a <code>String</code> as a <code>byte[]</code> using the default character encoding of the platform.
	 * <p>
	 * This is the same as {@link String#getBytes()}.
	 * 
	 * @param input the <code>String</code> to convert
	 * @return the requested byte array
	 * @throws NullPointerException if the input is null
	 * @throws IOException if an I/O error occurs (never occurs)
	 * @deprecated Use {@link String#getBytes()}
	 */
	public static byte[] toByteArray(String input) throws IOException {
		return input.getBytes();
	}

	/**
	 * Get the contents of a <code>byte[]</code> as a String
	 * using the default character encoding of the platform.
	 * 
	 * @param input the byte array to read from
	 * @return the requested String
	 * @throws NullPointerException if the input is null
	 * @throws IOException if an I/O error occurs (never occurs)
	 * @deprecated Use {@link String#String(byte[])}
	 */
	public static String toString(byte[] input) throws IOException {
		return new String(input);
	}

	/**
	 * Get the contents of a <code>byte[]</code> as a String
	 * using the specified character encoding.
	 * <p>
	 * Character encoding names can be found at <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * 
	 * @param input the byte array to read from
	 * @param encoding the encoding to use, null means platform default
	 * @return the requested String
	 * @throws NullPointerException if the input is null
	 * @throws IOException if an I/O error occurs (never occurs)
	 * @deprecated Use {@link String#String(byte[],String)}
	 */
	public static String toString(byte[] input, String encoding) throws IOException {
		if (encoding == null) {
			return new String(input);
		} else {
			return new String(input, encoding);
		}
	}

	// copy from InputStream
	// -----------------------------------------------------------------------
	/**
	 * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
	 * 
	 * @param input the <code>InputStream</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException if an I/O error occurs
	 * @since Commons IO 1.1
	 */
	public static int copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

}

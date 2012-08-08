/*
 * Copyright 2005 OpenXRI Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openxri;


/*
 ********************************************************************************
 * Class: Parsable
 ********************************************************************************
 */ /**
 * This class provides a base class for all classes that are parsed according
 * to the XRI Syntax definition.
 *
 * @author =chetan
 */
@SuppressWarnings("unchecked")
public abstract class Parsable implements Comparable
{
	String msValue = null;
	boolean mbParsed = false;
	boolean mbParseResult = false;

	/*
	 ****************************************************************************
	 * Constructor()
	 ****************************************************************************
	 */ /**
	 * Protected Constructor used by package only
	 */
	Parsable()
	{
		setValue(null);

	} // Constructor()

	/*
	 ****************************************************************************
	 * Constructor()
	 ****************************************************************************
	 */ /**
	 * Constructs Parsable object from a String
	 */
	Parsable(String sValue)
	{
		setValue(sValue);

	} // Constructor()

	/*
	 ****************************************************************************
	 * setValue()
	 ****************************************************************************
	 */ /**
	 *
	 */
	private void setValue(String sValue)
	{
		msValue = sValue;

	} // setValue()

	/*
	 ****************************************************************************
	 * toString()
	 ****************************************************************************
	 */ /**
	 * Outputs the object according to the XRI Syntax defined for this object
	 */
	public String toString()
	{
		return msValue;

	} // toString()

	/*
	 ****************************************************************************
	 * parse()
	 ****************************************************************************
	 */ /**
	 * Parses the set value
	 *
	 * @throws XRIParseException
	 *             Thrown if entire value could not be parsed into the
	 *             object
	 */
	void parse()
	{
		String sValue = msValue;

		// only do work if the value isn't already parsed
		if (!mbParsed)
		{
			ParseStream oStream = new ParseStream(msValue);

			if (scan(oStream))
			{
				// Did we consume the entire string?
				mbParseResult = oStream.getData().length() == 0;
			}

			// Set to true even if we fail, no need to fail over and over again.
			mbParsed = true;
		}

		// throw an exception if things failed
		if (!mbParseResult)
		{
			throw new XRIParseException(
					"Not a valid " + this.getClass().getName() +
					" class: \"" + sValue + "\"");
		}

	} // parse()

	/*
	 ****************************************************************************
	 * scan()
	 ****************************************************************************
	 */ /**
	 * Scans the stream for parts that can be parsed into the object
	 *
	 * @param oParseStream The input stream to read from
	 * @return boolean Returns true if all or part of the stream could be
	 *         parsed into the object
	 */
	boolean scan(ParseStream oParseStream)
	{
		if (oParseStream == null)
		{
			return false;
		}

		ParseStream oStream = oParseStream.begin();

		if (doScan(oStream))
		{
			setParsedValue(oParseStream.getConsumed(oStream));
			oParseStream.end(oStream);
			return true;
		}

		return false;

	} // scan()

	/*
	 ****************************************************************************
	 * doScan()
	 ****************************************************************************
	 */ /**
	 * Scans the stream for parts that can be parsed into the object
	 * @param oParseStream The input stream to read from
	 * @return boolean Returns true if all or part of the stream could be
	 *         parsed into the object
	 */
	abstract boolean doScan(ParseStream oParseStream);

	/*
	 ****************************************************************************
	 * setParsedValue()
	 ****************************************************************************
	 */ /**
	 * Sets the parsed value for the object
	 * @param sValue The value to set the object to
	 */
	void setParsedValue(String sValue)
	{
		if (sValue != null)
		{
			msValue = sValue;
		}
		else
		{
			msValue = "";
		}

		mbParsed = true;
		mbParseResult = true;

	} // setParsedValue()

	public boolean equals(Object object) {

		if (object == null || ! (object instanceof Parsable)) return(false);

		Parsable other = (Parsable) object;

		if (this.msValue == null && other.msValue != null) return(false);
		if (this.msValue != null && other.msValue == null) return(false);
		if (this.msValue != null && other.msValue != null && ! this.msValue.equals(other.msValue)) return(false);

		return(true);
	}

	public int hashCode() {

		return(this.msValue == null ? 0 : this.msValue.hashCode());
	}

	public int compareTo(Object object) {

		Parsable other = (Parsable) object;

		if (object == null || this.msValue == null || other.msValue == null) throw new NullPointerException();

		return(this.msValue.compareTo(other.msValue));
	}
} // Class: Parsable

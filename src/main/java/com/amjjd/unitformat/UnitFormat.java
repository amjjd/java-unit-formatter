/*
 * UnitFormat.java - A unit-prefix selecting number formatter
 * 
 * Copyright 2008-2012 Andrew Duffy
 * http://github.com/amjjd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.amjjd.unitformat;

import java.math.RoundingMode;
import java.text.FieldPosition;
import java.text.ChoiceFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Formats a number by choosing an appropriate in units like <code>MB</code> or
 * <code>GiB</code>.
 * 
 * Caveats:
 * <ul>
 * <li>Formatting of the number of units is delegated to a {@link NumberFormat}
 * which may use parentheses to format a negative number; there is no way to get
 * the unit inside the parentheses in this case.</li>
 * <li>{@Link FieldPosition} is not supported.</li>
 * <li>{@link #setParseIntegerOnly} is unlikely to work as expected.</li>
 * </ul>
 */
public class UnitFormat extends NumberFormat
{
	private static final long serialVersionUID = 1L;

	/** Used to format the number of units. */
	private NumberFormat numberFormat;

	/** The base unit's symbol, i.e. <code>B</code>. */
	private String symbol;

	/** The number of times a prefix is larger than the next smallest prefix. */
	private double interval = 1000.0;

	/**
	 * The largest number that will be output before the next largest prefix is
	 * used.
	 */
	private double nextPrefixAt = 750.0;

	/**
	 * The {@link MessageFormat} string used to format a number; parameter
	 * <code>0</code> is the number of units, <code>1</code> the prefix and
	 * <code>2</code> the symbol.
	 */
	private String format = "{0} {1}{2}";

	/** The multiple prefixes in increasing order of magnitude. */
	private String[] multiples = SIMULTIPLES;

	/** The subdivision prefixes in decreasing order of magnitude. */
	private String[] subdivisions = SISUBDIVISIONS;

	/** The SI multiples at intervals of 1000. */
	private static final String[] SIMULTIPLES = {"k", "M", "G", "T", "P", "E", "Z", "Y"};

	/** The SI subdivisions at intervals of 1000. */
	private static final String[] SISUBDIVISIONS = {"m", "µ", "n", "p", "f", "a", "z", "y"};

	/** The IEC binary multiples at intervals of 1024. */
	private static final String[] IECMULTIPLES = {"Ki", "Mi", "Gi", "Ti", "Pi", "Ei", "Zi", "Yi"};

	/** The deprecated binary multiples at intervals of 1024. */
	private static final String[] CONFUSINGMULTIPLES = {"K", "M", "G", "T", "P", "E", "Z", "Y"};

	/**
	 * Creates a unit format with the given number formatter and symbol.
	 * 
	 * @param numberFormat The number formatter
	 * @param symbol The base unit's symbol
	 */
	private UnitFormat(NumberFormat numberFormat, String symbol)
	{
		this.numberFormat = numberFormat;
		this.symbol = symbol;
	}

	/**
	 * Gets a unit formatter that uses the default locale's number formatter and
	 * the given symbol.
	 * 
	 * @param symbol The symbol
	 * @return The formatter
	 */
	public static UnitFormat getSIInstance(String symbol)
	{
		return getSIInstance(Locale.getDefault(), symbol);
	}

	/**
	 * Gets a unit formatter that uses the given locale's number formatter and
	 * the given symbol.
	 * 
	 * @param locale The locale
	 * @param symbol The symbol
	 * @return The formatter
	 */
	public static UnitFormat getSIInstance(Locale locale, String symbol)
	{
		return new UnitFormat(NumberFormat.getInstance(locale), symbol).init();
	}

	/**
	 * Gets a unit formatter that uses the default locale's number formatter and
	 * bytes symbol, IEC (<code>Ki</code>, <code>Mi</code>, etc) prefixes at
	 * intervals of 1024 and a next unit threshold of 768.
	 * 
	 * @return The formatter
	 */
	public static UnitFormat getBytesInstance()
	{
		return getBytesInstance(Locale.getDefault());
	}

	/**
	 * Gets a unit formatter that uses the given locale's number formatter and
	 * bytes symbol, IEC (<code>Ki</code>, <code>Mi</code>, etc) prefixes at
	 * intervals of 1024 and a next unit threshold of 768. The minimum number of
	 * integer digits is 1, the minimum number of fraction digits is 0 and the
	 * maximum number of fraction digits is 1 in the returned formatter.
	 * 
	 * @param locale The locale
	 * @return The formatter
	 */
	public static UnitFormat getBytesInstance(Locale locale)
	{
		UnitFormat format = getSIBytesInstance(locale);
		format.interval = 1024.0;
		format.nextPrefixAt = 768.0;
		format.multiples = IECMULTIPLES;
		return format.init();
	}

	/**
	 * Gets a unit formatter that uses the default locale's number formatter and
	 * bytes symbol, SI prefixes at intervals of 1000 and a next unit threshold
	 * of 750. The minimum number of integer digits is 1, the minimum number of
	 * fraction digits is 0 and the maximum number of fraction digits is 1 in
	 * the returned formatter.
	 * 
	 * @return The formatter
	 */
	public static UnitFormat getSIBytesInstance()
	{
		return getSIBytesInstance(Locale.getDefault());
	}

	/**
	 * Gets a unit formatter that uses the given locale's number formatter and
	 * bytes symbol, SI prefixes at intervals of 1000 a next unit threshold of
	 * 750. The minimum number of integer digits is 1, the minimum number of
	 * fraction digits is 0 and the maximum number of fraction digits is 1 in
	 * the returned formatter.
	 * 
	 * @param locale The locale
	 * @return The formatter
	 */
	public static UnitFormat getSIBytesInstance(Locale locale)
	{
		UnitFormat format = new UnitFormat(NumberFormat.getInstance(locale), ResourceBundle.getBundle("com.amjjd.unitformat.units", locale).getString("B"));

		format.setMinimumIntegerDigits(1);
		format.setMaximumIntegerDigits(Integer.MAX_VALUE);
		format.setMinimumFractionDigits(0);
		format.setMaximumFractionDigits(1);

		return format.init();
	}

	/**
	 * Gets a unit formatter that uses the default locale's number formatter and
	 * bytes symbol, modified SI prefixes (<code>K</code>, <code>M</code>, etc.)
	 * at intervals of 1024 and a next unit threshold of 750. The minimum number
	 * of integer digits is 1, the minimum number of fraction digits is 0 and
	 * the maximum number of fraction digits is 1 in the returned formatter. Use
	 * of this format is not recommended due to the potential for confusion.
	 * 
	 * @return The formatter
	 */
	public static UnitFormat getConfusingBytesInstance()
	{
		return getConfusingBytesInstance(Locale.getDefault());
	}
	
	/**
	 * Gets a unit formatter that uses the given locale's number formatter and
	 * bytes symbol, modified SI prefixes (<code>K</code>, <code>M</code>, etc.)
	 * at intervals of 1024 and a next unit threshold of 750. The minimum number
	 * of integer digits is 1, the minimum number of fraction digits is 0 and
	 * the maximum number of fraction digits is 1 in the returned formatter. Use
	 * of this format is not recommended due to the potential for confusion.
	 * 
	 * @param locale The locale
	 * @return The formatter
	 */
	public static UnitFormat getConfusingBytesInstance(Locale locale)
	{
		UnitFormat format = getBytesInstance(locale);
		format.multiples = CONFUSINGMULTIPLES;
		return format.init();
	}
	
	@Override
	public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos)
	{
		String prefix = "";

		double signum = Math.signum(number);
		number *= signum;

		if(number > nextPrefixAt)
		{
			for(String p : multiples)
			{
				if(number <= nextPrefixAt)
					break;

				prefix = p;
				number /= interval;
			}
		}
		else if(number != 0.0)
		{
			double next = nextPrefixAt / interval;

			if(number <= next)
			{
				for(String p : subdivisions)
				{
					if(number > next)
						break;

					prefix = p;
					number *= interval;
				}
			}
		}

		return new MessageFormat(format).format(new Object[]{numberFormat.format(number * signum), prefix, symbol}, toAppendTo, null);
	}

	@Override
	public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos)
	{
		return format((double)number, toAppendTo, pos);
	}

	@Override
	public Number parse(String source, ParsePosition parsePosition)
	{
		int startIndex = parsePosition.getIndex(), endIndex = -1;
		double value = Double.NaN, scale = 1.0;

		// try with no prefix
		Object[] number = noPrefixParser.parse(source, parsePosition);
		if(parsePosition.getErrorIndex() < 0 && parsePosition.getIndex() != startIndex)
		{
			value = ((Number)number[0]).doubleValue();
			endIndex = parsePosition.getIndex();
		}

		// try with a prefix regardless of success above, in case it's longer
		parsePosition.setErrorIndex(-1);
		parsePosition.setIndex(startIndex);
		Object[] numberAndPrefix = parser.parse(source, parsePosition);
		if(parsePosition.getErrorIndex() < 0 && parsePosition.getIndex() != startIndex && parsePosition.getIndex() > endIndex)
		{
			value = ((Number)numberAndPrefix[0]).doubleValue();
			scale = ((Number)numberAndPrefix[1]).doubleValue();
		}
		else if(endIndex < 0)
		{
			return null;
		}
		else
		{
			parsePosition.setErrorIndex(-1);
			parsePosition.setIndex(endIndex);
		}

		double scaled = value * scale;
		long l = (long)scaled;
		if((double)l == scaled)
			return l;
		return scaled;

	}

	private MessageFormat noPrefixParser;
	private MessageFormat parser;

	private UnitFormat init() {
		// Neither MessageFormat nor ChoiceFormat likes parsing an empty string,
		// so we need to handle an empty prefix specially.
		String parseFormat = new MessageFormat(format).format(new Object[]{"{0,number}", "", symbol.length() > 0 ? ("'" + symbol + "'") : ""}).trim();
		noPrefixParser = new MessageFormat(parseFormat);
		noPrefixParser.setFormatByArgumentIndex(0, numberFormat);

		parseFormat = new MessageFormat(format).format(new Object[]{"{0,number}", "{1}", symbol.length() > 0 ? ("'" + symbol + "'") : ""}).trim();
		parser = new MessageFormat(parseFormat);
		parser.setFormatByArgumentIndex(0, numberFormat);

		double[] limits = new double[subdivisions.length + multiples.length];
		String[] prefixes = new String[limits.length];
		double limit = 1.0;
		for(int i=0; i<subdivisions.length; i++)
		{
			limit /= interval;
			limits[subdivisions.length - i - 1] = limit;
			prefixes[subdivisions.length - i - 1] = subdivisions[i];
		}
		limit = 1.0;
		for(int i=0; i<multiples.length; i++)
		{
			limit *= interval;
			limits[subdivisions.length + i] = limit;
			prefixes[subdivisions.length + i] = multiples[i];
		}
		parser.setFormatByArgumentIndex(1, new ChoiceFormat(limits, prefixes));

		return this;
	}

	/**
	 * Gets the base unit's symbol.
	 * 
	 * @return The symbol
	 */
	public String getSymbol()
	{
		return symbol;
	}

	/**
	 * Sets the base unit's symbol.
	 * 
	 * @param symbol The symbol
	 */
	public void setSymbol(String symbol)
	{
		this.symbol = symbol;
		init();
	}

	/**
	 * Gets the number of times a unit is larger than the next smallest unit.
	 * The default value is 1000.
	 * 
	 * @return The interval.
	 */
	public double getInterval()
	{
		return interval;
	}

	/**
	 * Sets the number of times a unit is larger than the next smallest unit.
	 * The default value is 1000.
	 * 
	 * @param interval The interval
	 */
	public void setInterval(double interval)
	{
		this.interval = interval;
		init();
	}

	/**
	 * Gets the largest number that will be output before the next largest
	 * prefix is used. The default value is 750.
	 * 
	 * @return The next unit threshold
	 */
	public double getNextUnitAt()
	{
		return nextPrefixAt;
	}

	/**
	 * Sets the largest number that will be output before the next largest
	 * prefix is used. The default value is 750.
	 * 
	 * @param nextUnitAt The next unit threshold
	 */
	public void setNextUnitAt(double nextUnitAt)
	{
		this.nextPrefixAt = nextUnitAt;
	}

	/**
	 * Gets the {@link MessageFormat} string used to format a number. The
	 * default pattern is <code>"{0} {1}{2}"</code> which puts a single space
	 * between the number and unit.
	 * 
	 * @return The format string; parameter <code>0</code> is the number of
	 *         units, <code>1</code> the prefix and <code>2</code> the symbol.
	 */
	public String getFormat()
	{
		return format;
	}

	/**
	 * Sets the {@link MessageFormat} string used to format a number. The
	 * default pattern is <code>"{0} {1}{2}"</code> which puts a single space
	 * between the number and unit.
	 * 
	 * @param format The format string; parameter <code>0</code> is the number
	 *            of units, <code>1</code> the prefix and <code>2</code> the
	 *            symbol.
	 */
	public void setFormat(String format)
	{
		this.format = format;
		init();
	}

	/**
	 * Gets the multiple prefixes. SI prefixes are used by default.
	 * 
	 * @return The multiple units in increasing order of magnitude, starting at
	 *         one interval
	 */
	public String[] getMultiples()
	{
		return multiples.clone();
	}

	/**
	 * Sets the multiple units. SI prefixes are used by default.
	 * 
	 * @param multiples The multiple units in increasing order of magnitude,
	 *            starting at one interval
	 */
	public void setMultiples(String[] multiples)
	{
		this.multiples = multiples.clone();
		init();
	}

	/**
	 * Gets the subdivision units. SI prefixes are used by default.
	 * 
	 * @return The subdivision units in decreasing order of magnitude, starting
	 *         at one interval
	 */
	public String[] getSubdivisions()
	{
		return subdivisions.clone();
	}

	/**
	 * Sets the subdivision units. SI prefixes are used by default.
	 * 
	 * @param subdivisions The subdivision units in decreasing order of
	 *            magnitude, starting at one interval
	 */
	public void setSubdivisions(String[] subdivisions)
	{
		this.subdivisions = subdivisions.clone();
		init();
	}

	@Override
	public int getMinimumIntegerDigits()
	{
		return numberFormat.getMinimumIntegerDigits();
	}

	@Override
	public void setMinimumIntegerDigits(int newValue)
	{
		numberFormat.setMinimumIntegerDigits(newValue);
	}

	@Override
	public int getMaximumIntegerDigits()
	{
		return numberFormat.getMaximumIntegerDigits();
	}

	@Override
	public void setMaximumIntegerDigits(int newValue)
	{
		numberFormat.setMaximumIntegerDigits(newValue);
	}

	@Override
	public int getMinimumFractionDigits()
	{
		return numberFormat.getMinimumFractionDigits();
	}

	@Override
	public void setMinimumFractionDigits(int newValue)
	{
		numberFormat.setMinimumFractionDigits(newValue);
	}

	@Override
	public int getMaximumFractionDigits()
	{
		return numberFormat.getMaximumFractionDigits();
	}

	@Override
	public void setMaximumFractionDigits(int newValue)
	{
		numberFormat.setMaximumFractionDigits(newValue);
	}

	@Override
	public boolean isGroupingUsed()
	{
		return numberFormat.isGroupingUsed();
	}

	@Override
	public void setGroupingUsed(boolean newValue)
	{
		numberFormat.setGroupingUsed(newValue);
	}

	@Override
	public RoundingMode getRoundingMode()
	{
		return numberFormat.getRoundingMode();
	}

	@Override
	public void setRoundingMode(RoundingMode roundingMode)
	{
		numberFormat.setRoundingMode(roundingMode);
	}

	@Override
	public boolean isParseIntegerOnly()
	{
		return numberFormat.isParseIntegerOnly();
	}

	@Override
	public void setParseIntegerOnly(boolean newValue)
	{
		numberFormat.setParseIntegerOnly(newValue);
	}
}

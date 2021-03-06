/*
 * UnitFormatTest.java - JUnit tests for UnitFormat.java
 * 
 * Copyright 2008 Andrew Duffy.
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

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Locale;

import org.junit.Test;

public class UnitFormatTest
{
	@Test
	public void testBytesInstanceFormatting()
	{
		UnitFormat format = UnitFormat.getBytesInstance(Locale.ENGLISH);
		assertEquals(1, format.getMinimumIntegerDigits());
		assertEquals(0, format.getMinimumFractionDigits());
		assertEquals(1, format.getMaximumFractionDigits());
				
		assertEquals("0 B", format.format(0L));
		
		assertEquals("25.5 KiB", format.format(25L * 1024L + 512L));
		assertEquals("100 MiB", format.format(100L * 1024L * 1024L));
		assertEquals("1 KiB", format.format(1024L));
		
		assertEquals("768 B", format.format(768L));
		assertEquals("0.8 KiB", format.format(769L));
		assertEquals("0.8 MiB", format.format(815L * 1024L));
		
		// note the inaccuracy of such a large double
		assertEquals("8,271.8 YiB", format.format(10000.0E24));
		
		format.setNextUnitAt(1536.0);
		assertEquals("768 B", format.format(768L));
		assertEquals("1,024 B", format.format(1024L));
		assertEquals("1,536 B", format.format(1536L));
		assertEquals("1.5 KiB", format.format(1537L));
		
		format = UnitFormat.getBytesInstance(Locale.FRENCH);
		assertEquals("25,5 Kio", format.format(25L * 1024L + 512L));
		assertEquals("1 Kio", format.format(1024L));
		
		format.setNextUnitAt(1536.0);
		assertEquals("1\u00A0024 o", format.format(1024L));
	}
	
	@Test
	public void testBytesInstanceParsing() throws ParseException
	{
		UnitFormat format = UnitFormat.getBytesInstance(Locale.ENGLISH);

		assertEquals(0L, format.parse("0 B"));

		assertEquals(25L * 1024L + 512L, format.parse("25.5 KiB"));
		assertEquals(100L * 1024L * 1024L, format.parse("100 MiB"));
		assertEquals(1024L, format.parse("1 KiB"));

		assertEquals(768L, format.parse("768 B"));
		assertEquals(1024.0 * 0.8, format.parse("0.8 KiB"));
		assertEquals(1024.0 * 1024.0 * 0.8, format.parse("0.8 MiB"));

		// note the inaccuracy of such a large double
		assertEquals(1.2089258196146292E27, format.parse("1,000 YiB"));

		format = UnitFormat.getBytesInstance(Locale.FRENCH);
		assertEquals(25L * 1024L + 512L, format.parse("25,5 Kio"));
		assertEquals(1024L, format.parse("1 Kio"));
	}

	@Test
	public void testSIInstanceFormatting()
	{
		UnitFormat format = UnitFormat.getSIInstance(Locale.ENGLISH, "m");
		format.setMinimumIntegerDigits(1);
		format.setMinimumFractionDigits(0);
		format.setMaximumFractionDigits(3);
		
		assertEquals("0 m", format.format(0L));
		assertEquals("25.5 km", format.format(25500L));
		
		assertEquals("750 m", format.format(750L));
		assertEquals("0.751 km", format.format(751L));
		
		assertEquals("100 mm", format.format(0.1));
		assertEquals("1 mm", format.format(0.001));
		assertEquals("100 µm", format.format(0.0001));
		assertEquals("1 µm", format.format(0.000001));
		
		assertEquals("749 mm", format.format(0.749));
		assertEquals("750 mm", format.format(0.75));
		assertEquals("0.751 m", format.format(0.751));
	}

	@Test
	public void testSIInstanceParsing() throws ParseException
	{
		UnitFormat format = UnitFormat.getSIInstance(Locale.ENGLISH, "m");

		assertEquals(0L, format.parse("0 m"));
		assertEquals(25500L, format.parse("25.5 km"));

		assertEquals(750L, format.parse("750 m"));
		assertEquals(751L, format.parse("0.751 km"));

		assertEquals(0.1, format.parse("100 mm"));
		assertEquals(0.001, format.parse("1 mm"));
		// note the inaccuracy
		assertEquals(9.999999999999999E-5, format.parse("100 µm"));
		assertEquals(0.000001, format.parse("1 µm"));

		assertEquals(0.749, format.parse("749 mm"));
		assertEquals(0.75, format.parse("750 mm"));
		assertEquals(0.751, format.parse("0.751 m"));
	}
}

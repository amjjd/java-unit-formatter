java-unit-formatter
===================

A `NumberFormat` that selects an appropriate scale and SI unit when formatting.

* Supports both [SI prefixes][1] and [binary ones][2].
* Automatically handles the French use of "o" as a symbol for byte.

[1]: http://en.wikipedia.org/wiki/International_System_of_Units
[2]: http://en.wikipedia.org/wiki/Binary_prefix

Sample code
-----------

    UnitFormat format = UnitFormat.getBytesInstance(Locale.ENGLISH);

    assertEquals("25.5 KiB", format.format(25L * 1024L + 512L));
    assertEquals("100 MiB", format.format(100L * 1024L * 1024L));
    assertEquals("1 KiB", format.format(1024L));

    assertEquals("768 B", format.format(768L));
    assertEquals("0.8 KiB", format.format(769L));
    assertEquals("0.8 MiB", format.format(815L * 1024L));

    assertEquals(0L, format.parse("0 B"));

    assertEquals(25L * 1024L + 512L, format.parse("25.5 KiB"));
    assertEquals(100L * 1024L * 1024L, format.parse("100 MiB"));
    assertEquals(1024L, format.parse("1 KiB"));

    assertEquals(768L, format.parse("768 B"));
    assertEquals(1024.0 * 0.8, format.parse("0.8 KiB"));
    assertEquals(1024.0 * 1024.0 * 0.8, format.parse("0.8 MiB"));

    format.setNextUnitAt(1536.0);
    assertEquals("768 B", format.format(768L));
    assertEquals("1,024 B", format.format(1024L));
    assertEquals("1,536 B", format.format(1536L));
    assertEquals("1.5 KiB", format.format(1537L));

    format = UnitFormat.getBytesInstance(Locale.FRENCH);
    assertEquals("25,5 Kio", format.format(25L * 1024L + 512L));
    assertEquals("1 Kio", format.format(1024L));

    assertEquals(25L * 1024L + 512L, format.parse("25,5 Kio"));
    assertEquals(1024L, format.parse("1 Kio"));

    format = UnitFormat.getSIInstance(Locale.ENGLISH, "m");
    format.setMinimumIntegerDigits(1);
    format.setMinimumFractionDigits(0);
    format.setMaximumFractionDigits(3);

    assertEquals("750 m", format.format(750L));
    assertEquals("0.751 km", format.format(751L));

    assertEquals("100 mm", format.format(0.1));
    assertEquals("1 mm", format.format(0.001));
    assertEquals("100 µm", format.format(0.0001));
    assertEquals("1 µm", format.format(0.000001));

Caveats
-------

* Formatting of the number of units is delegated to a `NumberFormat` which may
  use parentheses to format a negative number; there is no way to get the unit
  inside the parentheses in this case.
* `FieldPosition` is not supported.
* `setParseIntegerOnly` is unlikely to work as expected.
  
Maven
-----

Add a `repository` to your `pom.xml`:

    <repositories>
      ...
      <repository>
        <id>amjjd-snapshots</id>
        <url>https://github.com/amjjd/amjjd-mvn-repo/raw/master/releases/</url>
      </repository>
      ...
    </repositories>

... and a `dependency`:

    <dependencies>
      ...
      <dependency>
        <groupId>com.amjjd</groupId>
        <artifactId>java-unit-formatter</artifactId>
        <version>0.2</version>
        <scope>compile</scope>
      </dependency>
      ...
    </dependencies>

License
-------

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)


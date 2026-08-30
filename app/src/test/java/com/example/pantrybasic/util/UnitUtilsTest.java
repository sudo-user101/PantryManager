package com.example.pantrybasic.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link UnitUtils}'s name normalization and metric mass/volume conversion.
 * Run on the JVM with {@code ./gradlew test} - no emulator needed.
 */
public class UnitUtilsTest {

    private static final double DELTA = 0.0001;

    @Test
    public void normalizeName_lowercasesAndTrims() {
        assertEquals("tomato", UnitUtils.normalizeName("  Tomato  "));
    }

    @Test
    public void normalizeName_singularizesPlurals() {
        assertEquals("tomato", UnitUtils.normalizeName("Tomatoes"));
        assertEquals("onion", UnitUtils.normalizeName("onions"));
        assertEquals("egg", UnitUtils.normalizeName("eggs"));
        assertEquals("berry", UnitUtils.normalizeName("berries"));
    }

    @Test
    public void normalizeName_onlySingularizesFinalWordOfMultiWordNames() {
        assertEquals("chicken breast", UnitUtils.normalizeName("chicken breasts"));
    }

    @Test
    public void normalizeUnit_mapsSynonymsToCanonicalForm() {
        assertEquals("g", UnitUtils.normalizeUnit("grams"));
        assertEquals("kg", UnitUtils.normalizeUnit("Kilograms"));
        assertEquals("pcs", UnitUtils.normalizeUnit("pieces"));
    }

    @Test
    public void sameFamily_massUnitsAreInterchangeable() {
        assertTrue(UnitUtils.sameFamily("g", "kg"));
    }

    @Test
    public void sameFamily_volumeUnitsAreInterchangeable() {
        assertTrue(UnitUtils.sameFamily("ml", "l"));
    }

    @Test
    public void sameFamily_massAndVolumeAreNotInterchangeable() {
        assertFalse("g and ml must not be considered the same family", UnitUtils.sameFamily("g", "ml"));
    }

    @Test
    public void convert_kilogramsToGrams() {
        assertEquals(1000.0, UnitUtils.convert(1, "kg", "g"), DELTA);
    }

    @Test
    public void convert_litersToMilliliters() {
        assertEquals(1000.0, UnitUtils.convert(1, "l", "ml"), DELTA);
    }

    @Test
    public void convert_unknownFamily_returnsQuantityUnchanged() {
        assertEquals(500, UnitUtils.convert(500, "g", "ml"), DELTA);
    }

    @Test
    public void convert_countBasedUnit_returnsQuantityUnchanged() {
        assertEquals(2, UnitUtils.convert(2, "pcs", "pcs"), DELTA);
    }
}

package com.binzeefox.foxframe.tools.dev;

import static org.junit.Assert.*;

public class TextToolsTest {

    @org.junit.Test
    public void isObb() {
        assertFalse(TextTools.isObb(0));
        assertFalse(TextTools.isObb(2));
        assertFalse(TextTools.isObb(4));
        assertFalse(TextTools.isObb(6));
        assertFalse(TextTools.isObb("122212938849122"));

        assertTrue(TextTools.isObb(1));
        assertTrue(TextTools.isObb(3));
        assertTrue(TextTools.isObb(5));
        assertTrue(TextTools.isObb(7));
        assertTrue(TextTools.isObb("122212938849121"));
    }



    @org.junit.Test
    public void isInteger() {
    }

    @org.junit.Test
    public void isDouble() {
    }

    @org.junit.Test
    public void isChinese() {
    }

    @org.junit.Test
    public void hasChinese() {
    }

    @org.junit.Test
    public void idCard() {
    }
}
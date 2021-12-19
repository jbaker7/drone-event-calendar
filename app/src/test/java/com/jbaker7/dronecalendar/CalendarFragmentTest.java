package com.jbaker7.dronecalendar;

import org.junit.Test;

import static org.junit.Assert.*;

public class CalendarFragmentTest {
    @Test
    public void getTimeString() {
        String input = "2019-03-09 14:30:00";
        String output;
        String expected = "2:30 PM";

        CalendarFragment testCalendar = new CalendarFragment();
        output = testCalendar.getTimeString(input);
        assertEquals(expected,output);
    }

}
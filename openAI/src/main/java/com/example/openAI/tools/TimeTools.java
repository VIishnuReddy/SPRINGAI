package com.example.openAI.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TimeTools {

    @Tool(description = """
        Get the current local date, time, and day of week
        for a requested location.
        Accept a valid IANA time-zone ID, such as
        Asia/Kolkata, America/New_York, Europe/London,
        Asia/Tokyo, or Australia/Sydney.
        Use this tool whenever the user asks for the current
        time or date in a location.
        """)
    public String getCurrentTime(String timeZone) {

        try {
            ZoneId zoneId = ZoneId.of(timeZone);

            return ZonedDateTime.now(zoneId)
                    .format(DateTimeFormatter.ofPattern(
                            "EEEE, dd MMMM yyyy, hh:mm:ss a z"
                    ));

        } catch (Exception e) {
            return "Invalid time-zone ID: " + timeZone
                    + ". Please provide a valid IANA time-zone ID.";
        }
    }
}

package com.ultraflynn.sygrl.industry;

import java.time.LocalDateTime;

public class Blueprint {
    private final String name;
    private final LocalDateTime jobEndDate;

    public Blueprint(String name, LocalDateTime jobEndDate) {
        this.name = name;
        this.jobEndDate = jobEndDate;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getJobEndDate() {
        return jobEndDate;
    }
}

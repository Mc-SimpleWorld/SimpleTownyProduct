package org.nott.time;

import org.nott.exception.ConfigWrongException;

public enum TimePeriod {

    // 1S = 20 ticks
    MIN(60 * 20L),
    HOUR(60 * 60 * 20L),
    DAY(60 * 60 * 24 * 20L),
    WEEK(60 * 60 * 24 * 7 * 20L),
    ;

    private Long tickTime;


    TimePeriod(Long tickTime) {
        this.tickTime = tickTime;

    }

    public Long getTickTime() {
        return tickTime;
    }

    public static Long fromStringGetVal(String period) throws ConfigWrongException {
        period = period.toLowerCase().trim();
        boolean flag = false;
        for (TimePeriod value : TimePeriod.values()) {
            if(period.contains(value.name().toLowerCase())){
                flag = true;
            }
        }
        if(!flag){
            throw new ConfigWrongException("Time period %s is wrong.".formatted(period));
        }
        for (TimePeriod timePeriod : TimePeriod.values()) {
            if(period.contains(timePeriod.name().toLowerCase())){
                String val = period.replaceAll(timePeriod.name().toLowerCase(), "");
                if(val.isBlank() || val.isEmpty()){
                    throw new ConfigWrongException("Time period value is empty.");
                }
                try {
                    Long value = Long.parseLong(val);
                    return timePeriod.getTickTime() * value;
                } catch (NumberFormatException e) {
                    throw new ConfigWrongException("Time period value is not a number.");
                }

            }
        }
        return 0L;
    }
}

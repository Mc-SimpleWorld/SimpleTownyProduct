package org.nott.model.data;

import lombok.Data;

import java.sql.Timestamp;

/**
 * @author Nott
 * @date 2025-3-6
 */
@Data
public class StealActivitiesData {

    private String activityId;

    private String thiefUuid;

    private String thiefName;

    private String targetTownUuid;

    private String targetPlotUuid;

    private Timestamp startTime;

    private Timestamp endTime;
}

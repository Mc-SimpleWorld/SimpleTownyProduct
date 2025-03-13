package org.nott.model.activity;

import lombok.Data;

/**
 * @author Nott
 * @date 2025-3-13
 */
@Data
public class GroupWar {

    private boolean enable;

    private String warStartTime;

    private String warEndTime;

    private String warDuration;

    private Integer warSessionPerTown;

    private Integer killPoints;

    private Integer capturePoints;

    private String captureTime;

    private Integer captureRange;

    private String recruitDuration;

    private Integer minAttackers;

    private Double maxAttackersMultiplier;

    private Double minDefendersMultiplier;

    private Double cost;
}

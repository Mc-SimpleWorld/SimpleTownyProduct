package org.nott.model.activity;

import lombok.Data;

/**
 * @author Nott
 * @date 2025-3-13
 */
@Data
public class SneakAttack {

    private boolean enable;

    private String sneakStartTime;

    private String sneakEndTime;

    private float baseSuccessRate;

    private RateFactors successRateFactors;

    private Double cost;

    private String coolDown;

    private Double failCost;
}

@Data
class RateFactors{

    private float attackerPerPlayer;

    private float defenderPerPlayer;

    private float maxSuccessRate;

    private float minSuccessRate;

}

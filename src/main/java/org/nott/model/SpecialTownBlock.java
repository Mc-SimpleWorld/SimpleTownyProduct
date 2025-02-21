package org.nott.model;

import lombok.Data;

import java.util.List;

/**
 * @author Nott
 * @date 2025-2-21
 */
@Data
public class SpecialTownBlock {

    private String name;

    private Integer limitPerTown;

    private String mapKey;

    private Integer sameNationMax;

    private boolean neutrality;

    private Double basePrice;

    private Double baseSellPrice;

    private Integer baseGainNumber;

    private Double townLevelExponent;

    private List<String> repelBlockType;

    private boolean tradeAble;

    private String gainCoolDown;

    private String tradeCoolDown;

    private List<String> gainCommand;

}

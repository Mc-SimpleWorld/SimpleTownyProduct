package org.nott.model.data;

import lombok.Data;

/**
 * @author Nott
 * @date 2025-3-5
 */
@Data
public class LostResourceData {

    private String blockUuid;

    private Long lostAmount;

    private Integer lostRate;

    private Integer lostTimes;
}

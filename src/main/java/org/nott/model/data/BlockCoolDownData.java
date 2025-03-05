package org.nott.model.data;

import lombok.Data;

/**
 * @author Nott
 * @date 2025-3-5
 */
@Data
public class BlockCoolDownData {

    private String blockUuid;

    private String gainPlayerName;

    private String gainPlayerUid;

    private String gainTime;

    private Long gainCount;

    private Long cool;

}

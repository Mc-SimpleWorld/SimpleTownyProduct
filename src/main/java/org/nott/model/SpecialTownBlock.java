package org.nott.model;

import lombok.Data;

import java.util.List;

/**
 * @author Nott
 * @date 2025-2-21
 */
@Data
public class SpecialTownBlock {

    private List<PrivateTownBlock> privates;

    private List<PublicTownBlock> publics;

    public boolean existBlock(String name) {
        return privates.stream().anyMatch(block -> block.getName().equals(name)) || publics.stream().anyMatch(block -> block.getName().equals(name));
    }

}

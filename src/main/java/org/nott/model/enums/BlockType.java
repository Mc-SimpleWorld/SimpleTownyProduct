package org.nott.model.enums;

public enum BlockType {

    PUBLIC(),
    PRIVATE();

    public static boolean hasVal(String arg){
        BlockType[] values = BlockType.values();
        for (BlockType value : values) {
            if(arg.equalsIgnoreCase(value.name())){
                return true;
            }
        }
        return false;
    }
}

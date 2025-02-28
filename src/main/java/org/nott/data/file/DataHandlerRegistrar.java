package org.nott.data.file;

import lombok.Data;
import org.nott.SimpleTownyProduct;

/**
 * @author Nott
 * @date 2025-2-26
 */

public class DataHandlerRegistrar {

    private DataHandler<?, ?> handler;

    public DataHandlerRegistrar register(DataHandler<?, ?> handler) {
        this.handler = handler;
        return this;
    }


    public static DataHandlerRegistrar Builder(){
        return new DataHandlerRegistrar();
    }

    public void end(){
        this.handler.saveOnShutDown();
    }

    public DataHandlerRegistrar build() {
        if (handler == null) {
            throw new IllegalStateException("DataHandler is not registered.");
        }
        // Perform any additional initialization or validation here
        // 初始化数据
        this.handler.runOnStart();
        SimpleTownyProduct.logger.info("DataHandler has been successfully registered and initialized.");
        new Thread(() -> this.handler.runOnBackground());
        return this;
    }

}

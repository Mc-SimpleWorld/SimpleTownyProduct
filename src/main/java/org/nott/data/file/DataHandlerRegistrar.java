package org.nott.data.file;

import lombok.Data;
import org.nott.SimpleTownyProduct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Nott
 * @date 2025-2-26
 */

public class DataHandlerRegistrar {

    private final List<DataHandler<?, ?>> handlers = new ArrayList<>();

    public DataHandlerRegistrar register(DataHandler<?, ?>... handlers) {
        this.handlers.addAll(Arrays.asList(handlers));
        return this;
    }


    public static DataHandlerRegistrar Builder(){
        return new DataHandlerRegistrar();
    }

    public void end(){
        this.handlers.forEach(DataHandler::saveOnShutDown);
    }

    public DataHandlerRegistrar build() {
        for (DataHandler<?, ?> handler : this.handlers) {
            if (handler == null) {
                throw new IllegalStateException("DataHandler is not registered.");
            }
            // Perform any additional initialization or validation here
            // 初始化数据
            handler.runOnStart();
            SimpleTownyProduct.logger.info("DataHandler has been successfully registered and initialized.");
            new Thread(handler::runOnBackground).start();
        }
        return this;
    }

}

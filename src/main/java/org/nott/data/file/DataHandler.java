package org.nott.data.file;

public interface DataHandler<Data, File> {

    Data read();

    void write(Data d);

    void backUp(Data d);

    void runOnBackground();
    
    void saveOnShutDown();

    void runOnStart();
}

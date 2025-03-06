package org.nott.data.file;

public interface DataHandler<Data, File> {

    Data read(File file);

    void write(Data d, File file);

    void runOnBackground();
    
    void saveOnShutDown();

    void runOnStart();
}

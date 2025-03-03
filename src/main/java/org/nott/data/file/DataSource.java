package org.nott.data.file;

public interface DataSource<Data> {

    Data getDataInMemory();

    void putDataToMemory();

}

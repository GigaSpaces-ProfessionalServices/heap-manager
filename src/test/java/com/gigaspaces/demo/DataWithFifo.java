package com.gigaspaces.demo;

import com.gigaspaces.annotation.pojo.FifoSupport;
import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

@SpaceClass(fifoSupport = FifoSupport.OPERATION)
public class DataWithFifo {
    private String id;
    private String payload;

    @Override
    public String toString() {
        return "DataWithFifo [id=" + id + ", payload="+ payload + "]";
    }

    @SpaceId(autoGenerate = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPayload() {
        return payload;
    }

    public DataWithFifo setPayload(String payload) {
        this.payload = payload;
        return this;
    }
}

package com.gigaspaces.common;


import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceSequenceNumber;

@SpaceClass
public class Data {

    private String id;

    private Long sequenceNumber;

    private String data;

    private Boolean processed;


    /**
     * Constructs a new Data object.
     */
    public Data() {

    }

    public Data(String id, Long sequenceNumber, String data, Boolean processed) {
        this.id = id;
        this.sequenceNumber = sequenceNumber;
        this.data = data;
        this.processed = processed;
    }

    @SpaceId(autoGenerate = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @SpaceSequenceNumber
    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }
}

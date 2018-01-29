package com.gigaspaces.demo;

import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceIndex;
import com.gigaspaces.annotation.pojo.SpaceSequenceNumber;
import com.gigaspaces.metadata.index.SpaceIndexType;

public class DataWithSequence {
    private String id;
    private String payload;
    private Long seqId;

    @Override
    public String toString() {
        return "DataWithSequence [id=" + id + ", seqId=" + seqId + ", payload="+ payload + "]";
    }

    @SpaceId(autoGenerate = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @SpaceSequenceNumber
    @SpaceIndex(type = SpaceIndexType.EXTENDED)
    public Long getSeqId() {
        return seqId;
    }

    public void setSeqId(Long seqId) {
        this.seqId = seqId;
    }

    public String getPayload() {
        return payload;
    }

    public DataWithSequence setPayload(String payload) {
        this.payload = payload;
        return this;
    }
}

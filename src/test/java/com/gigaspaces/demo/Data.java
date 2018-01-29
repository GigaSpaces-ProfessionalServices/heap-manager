package com.gigaspaces.demo;

import com.gigaspaces.annotation.pojo.SpaceId;

public class Data {
    private String id;
    private String payload;

    @Override
    public String toString() {
        return "Data [id=" + id + ", payload="+ payload + "]";
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

    public Data setPayload(String payload) {
        this.payload = payload;
        return this;
    }
}

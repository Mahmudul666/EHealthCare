package com.example.ehealthcare.models;

public class ModelBmi {
    String id,bResult,bTime;

    public ModelBmi() {
    }

    public ModelBmi(String id, String bResult, String bTime) {
        this.id = id;
        this.bResult = bResult;
        this.bTime = bTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getbResult() {
        return bResult;
    }

    public void setbResult(String bResult) {
        this.bResult = bResult;
    }

    public String getbTime() {
        return bTime;
    }

    public void setbTime(String bTime) {
        this.bTime = bTime;
    }
}

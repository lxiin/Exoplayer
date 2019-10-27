package com.example.googleexoplayer;

import com.example.googleexoplayer.util.PlayerUtil;

public class SeekInfo {
    private final static SeekInfo instance = new SeekInfo();

    public static SeekInfo obtain() {
        return instance;
    }

    private SeekInfo() {
    }

    public long seekingPosition;
    public long duration;

    @Override
    public String toString() {

        return "SeekInfo{" +
                "seekingPosition=" + PlayerUtil.ms2HMS(seekingPosition) +
                ", duration=" + PlayerUtil.ms2HMS(duration) +
                '}';
    }

}

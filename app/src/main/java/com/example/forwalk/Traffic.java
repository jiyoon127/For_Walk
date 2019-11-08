package com.example.forwalk;

public class Traffic {
    long light;
    String loc;
    public Traffic(){
        this.light = -1;
        this.loc = "null";
    }

    public Traffic(long light, String loc){
        this.light = light;
        this.loc = loc;
    }

    public double get_lat(){
        double d_lat = Double.parseDouble(this.loc.substring(0,loc.indexOf(",")-1));
        return Math.floor(d_lat*1000000)/1000000.0;
    }

    public double get_lng(){
        double d_lng = Double.parseDouble(this.loc.substring(loc.indexOf(",")+1));
        return Math.floor(d_lng*1000000)/1000000.0;
    }

    public long get_light(){
        return this.light;
    }
}

package com.example.healthcare;

public enum colorRange{
    RED("red"),AMBER("amber"),GREEN("green");
    private final String color;
    colorRange(final String color){
        this.color = color;
    }
    @Override
    public String toString(){return this.color;}
}

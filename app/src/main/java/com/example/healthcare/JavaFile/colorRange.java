package com.example.healthcare.JavaFile;

public enum colorRange{
  RED("red",3),AMBER("amber",2),GREEN("green",1),NO_COLOR("grey",0);
  private final String color;
  private final int level;
  colorRange(final String color, final int level){
    this.color = color;
    this.level = level;
  }
  @Override
  public String toString(){return this.color;}
  public int getLevel(){return this.level;}
}
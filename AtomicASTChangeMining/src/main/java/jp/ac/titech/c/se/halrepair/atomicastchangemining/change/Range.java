package jp.ac.titech.c.se.halrepair.atomicastchangemining.change;

import java.io.Serializable;

public class Range implements Serializable {
    private static final long serialVersionUID = 1L;
    private int begin;
    private int end;

    public Range(int begin, int end){
        this.begin = begin;
        this.end = end;
    }

    public int getBegin(){
        return this.begin;
    }

    public int getEnd(){
        return this.end;
    }

    public String toString(){
        return "(" + begin+ ", "+end+")";
    }
}

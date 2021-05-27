package com.migrator.core.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PKRange {
    private Long min;
    private Long max;

    public Long getMin(){
        return min == null ? 0: min;
    }

    public Long getMax(){
        //return  100000l;
        return max == null ? 0: max;
    }
}

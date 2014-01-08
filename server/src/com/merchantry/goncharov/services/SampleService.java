package com.merchantry.goncharov.services;

import net.jcip.annotations.Immutable;

import java.util.Date;

@Immutable
@SuppressWarnings("ALL")
public class SampleService {
    public String echoString(String s) {
        return s;
    }

    public Integer calculateSum(Integer[] values) {
        Integer sum = 0;
        for (Integer value : values) {
            sum += value;
        }
        return sum;
    }

    public Object[] databaseQueryMock(String sql, Long timeout) throws InterruptedException {
        Thread.sleep(timeout);
        return new Object[] {
                new Object[] {1, "Ann"},
                new Object[] {2, "John"}
        };
    }

    public Date getCurrentDate() {
       return new Date();
    }
}

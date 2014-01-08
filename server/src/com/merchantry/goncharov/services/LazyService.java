package com.merchantry.goncharov.services;

@SuppressWarnings("ALL")
public class LazyService {
    public void doNothing(String whatever) {}

    public Object sleep(Long milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
        return null;
    }

    public Integer calculateSum(Integer[] values) {
        throw new UnsupportedOperationException("I don't want.");
    }

    public Unserializable tellMeTheTruth() {
        return new Unserializable();
    }

    private class Unserializable {
        private int value;
    }
}

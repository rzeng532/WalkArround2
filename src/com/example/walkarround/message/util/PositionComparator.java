package com.example.walkarround.message.util;

import java.util.Comparator;


public class PositionComparator implements Comparator<Integer> {
    @Override
    public int compare(Integer lhs, Integer rhs) {
        return lhs.compareTo(rhs);
    }
}

package com.neonpuck;

public enum ArenaType {
    CLASSIC, BUMPS, WARP;

    public static ArenaType next(ArenaType a){
        int i = (a.ordinal()+1) % values().length;
        return values()[i];
    }
}

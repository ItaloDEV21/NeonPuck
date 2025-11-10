package com.neonpuck;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayDeque;

public class Puck {
    public float x,y,vx,vy,r;
    private final ArrayDeque<float[]> trail = new ArrayDeque<>();
    private static final int TRAIL_MAX = 14;

    public Puck(float x,float y,float r){ this.x=x; this.y=y; this.r=r; }

    public void pushTrail(){
        trail.addFirst(new float[]{x,y});
        while(trail.size()>TRAIL_MAX) trail.removeLast();
    }
    public void clearTrail(){ trail.clear(); }

    public void draw(ShapeRenderer sr){
        int i=0;
        for(float[] p: trail){
            float t = 1f - (i++/(float)TRAIL_MAX);
            sr.setColor(139/255f,233/255f,253/255f, t*0.6f);
            float tr = r * (0.6f + 0.4f*t);
            sr.circle(p[0], p[1], tr);
        }
        sr.setColor(139/255f,233/255f,253/255f, 1f);
        sr.circle(x,y,r);
    }
}

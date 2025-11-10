package com.neonpuck;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Paddle {
    public float x,y,vx,vy,r;
    public boolean turbo=false, slow=false;
    public float baseR;

    public Paddle(float x,float y,float r){
        this.x=x; this.y=y; this.r=r; this.baseR=r;
    }

    public void draw(ShapeRenderer sr, Color c){
        sr.setColor(c);
        sr.circle(x,y,r);
    }

    public void tick(float dt){
        if(Math.abs(r - baseR) > 0.2f) r += (baseR - r) * 0.08f;
        else r = baseR;
    }
}

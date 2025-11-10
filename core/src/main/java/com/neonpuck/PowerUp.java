package com.neonpuck;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class PowerUp {
    public enum Type { TURBO, SLOW, ENLARGE, SHRINK }
    public final Type type;
    public float x,y,r;
    public long born;

    public PowerUp(Type t, float x, float y, float r){
        this.type=t; this.x=x; this.y=y; this.r=r; this.born= Time.ms();
    }

    public void draw(ShapeRenderer sr){
        sr.setColor(255/255f,121/255f,198/255f,1f);
        switch (type){
            case TURBO -> bolt(sr, x,y,r);
            case SLOW -> snow(sr, x,y,r);
            case ENLARGE -> plus(sr, x,y,r);
            case SHRINK -> minus(sr, x,y,r);
        }
    }

    private void bolt(ShapeRenderer sr, float x,float y,float r){
        sr.rectLine(x-r*0.2f,y-r*0.9f, x+r*0.1f,y-r*0.1f, 3);
        sr.rectLine(x-r*0.2f,y-r*0.1f, x+r*0.2f,y+r*0.9f, 3);
    }
    private void snow(ShapeRenderer sr, float x,float y,float r){
        for(int i=0;i<6;i++){
            double a = i*Math.PI/3;
            float xx = (float)(x + Math.cos(a)*r);
            float yy = (float)(y + Math.sin(a)*r);
            sr.rectLine(x,y,xx,yy, 3);
        }
    }
    private void plus(ShapeRenderer sr, float x,float y,float r){
        sr.rect(x-r*0.2f,y-r, r*0.4f, r*2);
        sr.rect(x-r,y-r*0.2f, r*2, r*0.4f);
    }
    private void minus(ShapeRenderer sr, float x,float y,float r){
        sr.rect(x-r,y-r*0.2f, r*2, r*0.4f);
    }
}

package com.example.safesteps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class PatternLockView extends View {
    public interface Listener { void onPatternChanged(String pattern); }
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Integer> selected = new ArrayList<>();
    private float downX, downY;
    private Listener listener;

    public PatternLockView(Context c) { super(c); setFocusable(true); }
    public void setListener(Listener l) { listener = l; }
    public void clear() { selected.clear(); invalidate(); if(listener != null) listener.onPatternChanged(""); }
    public String getPattern() { StringBuilder s=new StringBuilder(); for(int n:selected)s.append(n); return s.toString(); }

    protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w=getWidth(), h=getHeight(), size=Math.min(w,h), step=size/4f, ox=w/2f-size/2f, oy=h/2f-size/2f;
        paint.setStrokeCap(Paint.Cap.ROUND); paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(10); paint.setColor(Color.argb(90,255,255,255));
        if(selected.size()>1){ Path p=new Path(); for(int i=0;i<selected.size();i++){int n=selected.get(i);float x=ox+(n%3+1)*step,y=oy+(n/3+1)*step;if(i==0)p.moveTo(x,y);else p.lineTo(x,y);} c.drawPath(p,paint); }
        for(int n=0;n<9;n++){float x=ox+(n%3+1)*step,y=oy+(n/3+1)*step; paint.setStyle(Paint.Style.FILL); paint.setColor(selected.contains(n)?Color.rgb(35,210,190):Color.WHITE); c.drawCircle(x,y,22,paint); paint.setColor(Color.rgb(12,73,105)); c.drawCircle(x,y,9,paint);}
    }
    public boolean onTouchEvent(MotionEvent e){
        float w=getWidth(),h=getHeight(),size=Math.min(w,h),step=size/4f,ox=w/2f-size/2f,oy=h/2f-size/2f;
        if(e.getAction()==MotionEvent.ACTION_DOWN){clear();downX=e.getX();downY=e.getY(); addNear(e.getX(),e.getY(),ox,oy,step);return true;}
        if(e.getAction()==MotionEvent.ACTION_MOVE){addNear(e.getX(),e.getY(),ox,oy,step);return true;}
        if(e.getAction()==MotionEvent.ACTION_UP){if(listener!=null)listener.onPatternChanged(getPattern());return true;} return true;
    }
    private void addNear(float x,float y,float ox,float oy,float step){for(int n=0;n<9;n++){float cx=ox+(n%3+1)*step,cy=oy+(n/3+1)*step;if(Math.hypot(x-cx,y-cy)<step*.45 && !selected.contains(n)){selected.add(n);invalidate();}}}
}

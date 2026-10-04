package com.example.safesteps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PatternLockView extends View {

    private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<Integer> selectedPoints = new ArrayList<>();

    private float[] centerX = new float[9];
    private float[] centerY = new float[9];

    private float radius;
    private float currentX;
    private float currentY;
    private boolean drawing;

    public PatternLockView(Context context) {
        super(context);
        init();
    }

    public PatternLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PatternLockView(
            Context context,
            AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.WHITE);

        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(Color.rgb(215, 224, 235));

        selectedPaint.setStyle(Paint.Style.FILL);
        selectedPaint.setColor(Color.rgb(40, 125, 225));

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(10f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setColor(Color.rgb(40, 125, 225));

        setFocusable(true);
        setClickable(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        float size = Math.min(width, height);

        float left = (width - size) / 2f;
        float top = (height - size) / 2f;

        float cell = size / 3f;

        radius = cell * 0.16f;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;

                centerX[index] =
                        left + cell * col + cell / 2f;

                centerY[index] =
                        top + cell * row + cell / 2f;
            }
        }

        if (selectedPoints.size() > 1) {
            Path path = new Path();

            int first = selectedPoints.get(0);

            path.moveTo(
                    centerX[first],
                    centerY[first]
            );

            for (int i = 1; i < selectedPoints.size(); i++) {
                int point = selectedPoints.get(i);

                path.lineTo(
                        centerX[point],
                        centerY[point]
                );
            }

            if (drawing) {
                path.lineTo(currentX, currentY);
            }

            canvas.drawPath(path, linePaint);
        } else if (drawing && selectedPoints.size() == 1) {
            int first = selectedPoints.get(0);

            canvas.drawLine(
                    centerX[first],
                    centerY[first],
                    currentX,
                    currentY,
                    linePaint
            );
        }

        for (int i = 0; i < 9; i++) {
            if (selectedPoints.contains(i)) {
                canvas.drawCircle(
                        centerX[i],
                        centerY[i],
                        radius * 1.35f,
                        selectedPaint
                );

                circlePaint.setColor(Color.WHITE);

                canvas.drawCircle(
                        centerX[i],
                        centerY[i],
                        radius * 0.42f,
                        circlePaint
                );

                circlePaint.setColor(
                        Color.rgb(215, 224, 235)
                );
            } else {
                canvas.drawCircle(
                        centerX[i],
                        centerY[i],
                        radius,
                        circlePaint
                );
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                clearPattern();

                drawing = true;

                currentX = event.getX();
                currentY = event.getY();

                addPointAt(
                        event.getX(),
                        event.getY()
                );

                invalidate();

                return true;

            case MotionEvent.ACTION_MOVE:
                if (!drawing) {
                    return true;
                }

                currentX = event.getX();
                currentY = event.getY();

                addPointAt(
                        event.getX(),
                        event.getY()
                );

                invalidate();

                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (drawing) {
                    currentX = event.getX();
                    currentY = event.getY();

                    addPointAt(
                            event.getX(),
                            event.getY()
                    );

                    drawing = false;
                    invalidate();
                }

                return true;
        }

        return true;
    }

    private void addPointAt(float x, float y) {

        int point = findPoint(x, y);

        if (point < 0) {
            return;
        }

        if (!selectedPoints.contains(point)) {
            selectedPoints.add(point);
        }
    }

    private int findPoint(float x, float y) {

        for (int i = 0; i < 9; i++) {

            float dx = x - centerX[i];
            float dy = y - centerY[i];

            float distance =
                    (float) Math.sqrt(
                            dx * dx + dy * dy
                    );

            if (distance <= radius * 2.4f) {
                return i;
            }
        }

        return -1;
    }

    public String getPattern() {

        if (selectedPoints.isEmpty()) {
            return "";
        }

        StringBuilder result =
                new StringBuilder();

        for (Integer point : selectedPoints) {
            result.append(point + 1);
        }

        return result.toString();
    }

    public void clearPattern() {

        selectedPoints.clear();

        drawing = false;

        currentX = 0;
        currentY = 0;

        invalidate();
    }
}

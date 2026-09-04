package com.smartfamily.studentplanner.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CustomLoaderView extends View {

    private Paint paint;
    private int numLines = 16; // Number of lines
    private int color = 0xFFFFFFFF; // White color
    private int frame = 0; // Animation frame

    public CustomLoaderView(Context context) {
        super(context);
        init();
    }

    public CustomLoaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(8);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        startAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 5; // Invisible circle size

        for (int i = 0; i < numLines; i++) {
            float angle = (float) (i * (360.0 / numLines));
            float startX = width / 2f + (float) Math.cos(Math.toRadians(angle)) * (radius - 10);
            float startY = height / 2f + (float) Math.sin(Math.toRadians(angle)) * (radius - 10);
            float endX = width / 2f + (float) Math.cos(Math.toRadians(angle)) * (radius + 45); // Длина x2
            float endY = height / 2f + (float) Math.sin(Math.toRadians(angle)) * (radius + 45); // Длина x2

            // Brightness change effect
            int alpha = (int) (255 * (0.5 + 0.5 * Math.sin(Math.toRadians((i * (360.0 / numLines)) - frame))));
            paint.setAlpha(alpha);
            canvas.drawLine(startX, startY, endX, endY, paint);
        }
    }

    private void startAnimation() {
        new Thread(() -> {
            while (true) {
                frame += 10; // brightness change speed
                if (frame >= 360) frame = 0;
                postInvalidate();
                try {
                    Thread.sleep(25); // Animation speed
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

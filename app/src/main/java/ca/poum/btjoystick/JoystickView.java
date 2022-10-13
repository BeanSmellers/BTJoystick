package ca.poum.btjoystick;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {

    public interface OnMoveListener {
        /**
         * @param angle Movement angle (0 to 360)
         * @param power Movement power (0 to 1)
         */
        void onMove(int angle, float power);
    }

    private Paint joystickPaint, backgroundPaint;
    private float touchX, touchY;
    private boolean centerJoystick = true;
    private int layoutW, layoutH;
    private float joyR, bgR;
    float centerX, centerY;
    private OnMoveListener callback = null;

    public JoystickView(Context context) {
        super(context);
        init(null, 0);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.JoystickView, defStyle, 0);

        joystickPaint = new Paint();
        joystickPaint.setColor(Color.GREEN);
        joystickPaint.setAntiAlias(true);
        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(Color.GRAY);

        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background
        canvas.drawCircle(centerX, centerY, bgR, backgroundPaint);

        // Draw joystick
        if (centerJoystick) {
            canvas.drawCircle(centerX, centerY, joyR, joystickPaint);
        } else {
            canvas.drawCircle(touchX, touchY, joyR, joystickPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
//        Log.d("BTJ", "Canvas dim = (" + getWidth() + ", " + getHeight() + ")");

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // Bound move to circle
                float dx = e.getX() - centerX;
                float dy = e.getY() - centerY;
                double r = Math.sqrt(dx*dx + dy*dy);
                double rMax = bgR - joyR/4;
                if (r > rMax) {
                    double ratio = rMax/r;
                    dx *= ratio;
                    dy *= ratio;
                }

                // Calculate angle & power, send to callback listener
                if (callback != null) {
                    r = Math.sqrt(dx*dx + dy*dy);  // Distance to center
                    float power = (float) (r / rMax);
                    int angle = (int) Math.round(Math.toDegrees(Math.atan2(dy, dx)));
                    // Adjust angle to protractor notation
                    if (dy < 0) {
                        angle *= -1;
                    } else {
                        angle = 360 - angle;
                    }

                    callback.onMove(angle, power);
                }

                touchX = centerX + dx;
                touchY = centerY + dy;
                centerJoystick = false;
                this.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (callback != null) {
                    callback.onMove(0, 0);
                }

                centerJoystick = true;
                this.invalidate();
        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // Init touchX/Y to center
        layoutW = getWidth();
        layoutH = getHeight();

        // Set radii
        joyR = layoutW / 6f;
        bgR = layoutW / 2.5f;

        // Set center
        centerX = layoutW / 2f;
        centerY = layoutH / 2f;

    }

    public void setOnMoveListener(OnMoveListener listener) {
        callback = listener;
    }
}
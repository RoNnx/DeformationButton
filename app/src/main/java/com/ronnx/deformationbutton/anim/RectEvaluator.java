package com.ronnx.deformationbutton.anim;

import android.animation.TypeEvaluator;

public class RectEvaluator implements TypeEvaluator {

    /**
     * 复写evaluate（）
     * 在evaluate（）里写入对象动画过渡的逻辑
     */
    @Override
    public Object evaluate(float fraction, Object startValue, Object endValue) {
        Rect startRect = (Rect) startValue;
        Rect endRect = (Rect) endValue;

        float w = startRect.getWidth() + fraction * (endRect.getWidth() - startRect.getWidth());
        float r = startRect.getRadius() + fraction * (endRect.getRadius() - startRect.getRadius());
        float a = startRect.getAlpha() + fraction * (endRect.getAlpha() - startRect.getAlpha());

        return new Rect(r, w, a);
    }

}

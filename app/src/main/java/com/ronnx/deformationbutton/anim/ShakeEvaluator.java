package com.ronnx.deformationbutton.anim;

import android.animation.TypeEvaluator;

public class ShakeEvaluator implements TypeEvaluator {

    /**
     * 复写evaluate（）
     * 在evaluate（）里写入对象动画过渡的逻辑
     */
    @Override
    public Object evaluate(float fraction, Object startValue, Object endValue) {
        Shake startShake = (Shake) startValue;
        Shake endShake = (Shake) endValue;

        float x = startShake.getTranslateX() + fraction * (endShake.getTranslateX() - startShake.getTranslateX());
        float y = startShake.getTranslateY() + fraction * (endShake.getTranslateY() - startShake.getTranslateY());

        return new Shake(x, y);
    }

}

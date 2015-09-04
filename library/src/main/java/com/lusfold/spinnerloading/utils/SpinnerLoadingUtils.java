package com.lusfold.spinnerloading.utils;

/**
 * @author <a href="http://www.lusfold.com" target="_blank">Lusfold</a>
 * @date 9/4/15.
 */
public class SpinnerLoadingUtils {
    public static  float[] getVector(float radians, float length) {
        float x = (float) (Math.cos(radians) * length);
        float y = (float) (Math.sin(radians) * length);
        return new float[]{
                x, y
        };
    }

    public static float getDistance(float[] b1, float[] b2) {
        float x = b1[0] - b2[0];
        float y = b1[1] - b2[1];
        float d = x * x + y * y;
        return (float) Math.sqrt(d);
    }


    public static float getLength(float[] b) {
        return (float) Math.sqrt(b[0] * b[0] + b[1] * b[1]);
    }
}

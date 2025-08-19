package com.example.watermarkcamera.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.view.WindowManager;

public class DimensionUtil {

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static float px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int getOrientation(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getRealSize(point);
        if (point.x > point.y) {
            return Configuration.ORIENTATION_LANDSCAPE;
        } else {
            return Configuration.ORIENTATION_PORTRAIT;
        }
    }

    public static boolean isPortraitScreen(Context context) {
        return DimensionUtil.getOrientation(context) == Configuration.ORIENTATION_PORTRAIT;
    }

    public static boolean isLandAndFullScreen(Context context) {
        return DimensionUtil.getOrientation(context) == Configuration.ORIENTATION_LANDSCAPE;
    }
}

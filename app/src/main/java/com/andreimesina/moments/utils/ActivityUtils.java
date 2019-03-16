package com.andreimesina.moments.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.IOException;
import java.util.Map;

public class ActivityUtils {

    public static void goToActivity(Context activitySource, Class activityTarget) {
        Intent intent = new Intent(activitySource, activityTarget);
        ActivityCompat.startActivity(activitySource, intent, Bundle.EMPTY);
    }

    public static void goToActivity(Context activitySource, Class activityTarget,
                                    Map<Parcelable, String> extras) {
        Intent intent = new Intent(activitySource, activityTarget);
        for(Parcelable extra : extras.keySet()) {
            intent.putExtra(extras.get(extra), extra);
        }
        ActivityCompat.startActivity(activitySource, intent, Bundle.EMPTY);
    }

    public static void clearFocus(Activity activity) {
        View view = activity.getCurrentFocus();

        if(view != null) {
            view.clearFocus();
        }
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();

        if(view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    public static void fixImageExifOrientation(String pathOld, String pathNew) throws IOException {
        ExifInterface exif = new ExifInterface(pathOld);
        ExifInterface exif2 = new ExifInterface(pathNew);

        exif2.setAttribute(ExifInterface.TAG_ORIENTATION, exif.getAttribute(ExifInterface.TAG_ORIENTATION));
        exif2.saveAttributes();
    }

}

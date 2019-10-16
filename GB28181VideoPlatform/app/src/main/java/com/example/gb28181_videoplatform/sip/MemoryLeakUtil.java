package com.example.gb28181_videoplatform.sip;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Field;

public class MemoryLeakUtil {
    public static void fixInputMethodMemoryLeak(Context context) {
        if (context == null)
            return;
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null)
            return;

        String[] viewArr = new String[]{"mCurRootView", "mServedView", "mNextServedView"};
        Field field;
        Object fieldObj;
        for (String view : viewArr) {
            try {
                field = inputMethodManager.getClass().getDeclaredField(view);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                fieldObj = field.get(inputMethodManager);
                if (fieldObj != null && fieldObj instanceof View) {
                    View fieldView = (View) fieldObj;
                    if (fieldView.getContext() == context) {
                        //注意需要判断View关联的Context是不是当前Activity，否则有可能造成正常的输入框输入失效
                        field.set(inputMethodManager, null);
                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

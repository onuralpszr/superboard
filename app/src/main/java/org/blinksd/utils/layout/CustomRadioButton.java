package org.blinksd.utils.layout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import java.lang.reflect.Field;

public class CustomRadioButton extends RadioButton {

    public CustomRadioButton(Context c) {
        super(c);
        int i = DensityUtils.dpInt(8);
        setPadding(i, 0, i, 0);
        if (Build.VERSION.SDK_INT < 21) {
            Drawable drw = getButtonDrawable();
            if (drw != null) {
                drw.setColorFilter(0xFFDEDEDE, PorterDuff.Mode.SRC_IN);
            }

            if (Build.VERSION.SDK_INT < 16) {
                setPadding(i + drw.getIntrinsicWidth(), 0, i, 0);
            }
        } else {
            setPadding(i, 0, i, 0);
            int color = 0xFFDEDEDE;
            if (Build.VERSION.SDK_INT >= 31) {
                color = getResources().getColor(android.R.color.system_accent1_200, c.getTheme());
            }
            setButtonTintList(ColorStateList.valueOf(color));
            setButtonTintMode(PorterDuff.Mode.SRC_IN);

            setBackground(null);
        }

        //setRadioButton();
    }

    public Drawable getButtonDrawable() {
        if (Build.VERSION.SDK_INT >= 23) {
            return super.getButtonDrawable();
        }

        try {
            Field field = CompoundButton.class.getDeclaredField("mButtonDrawable");
            field.setAccessible(true);
            return (Drawable) field.get(this);
        } catch (Throwable t) {
            return null;
        }
    }
}
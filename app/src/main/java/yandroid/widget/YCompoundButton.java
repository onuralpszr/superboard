/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package yandroid.widget;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.AbsSavedState;
import android.view.Gravity;
import android.view.SoundEffectConstants;
import android.view.ViewDebug;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.Checkable;

import yandroid.util.Styleable;

/**
 * <p>
 * A button with two states, checked and unchecked. When the button is pressed
 * or clicked, the state changes automatically.
 * </p>
 *
 * <p><strong>XML attributes</strong></p>
 * <p>
 * </p>
 */

@SuppressWarnings("unused")
public abstract class YCompoundButton extends Button implements Checkable {
    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };
    private boolean mChecked;
    private int mButtonResource;
    private boolean mBroadcasting;
    private Drawable mButtonDrawable;
    private ColorStateList mButtonTintList = null;
    private PorterDuff.Mode mButtonTintMode = null;
    private boolean mHasButtonTint = false;
    private boolean mHasButtonTintMode = false;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private OnCheckedChangeListener mOnCheckedChangeWidgetListener;

    public YCompoundButton(Context context) {
        this(context, null);
    }

    public YCompoundButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YCompoundButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public YCompoundButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, Styleable.getStyleable("CompoundButton"), defStyleAttr, defStyleRes);

        final Drawable d = a.getDrawable(Styleable.getKey("CompoundButton_button"));
        if (d != null) {
            setButtonDrawable(d);
        }

        if (SDK_INT >= LOLLIPOP) {
            if (a.hasValue(Styleable.getKey("CompoundButton_buttonTintMode"))) {
                mButtonTintMode = parseTintMode(a.getInt(
                        Styleable.getKey("CompoundButton_buttonTintMode"), -1), mButtonTintMode);
                mHasButtonTintMode = true;
            }

            if (a.hasValue(Styleable.getKey("CompoundButton_buttonTint"))) {
                mButtonTintList = a.getColorStateList(Styleable.getKey("CompoundButton_buttonTint"));
                mHasButtonTint = true;
            }
        }

        final boolean checked = a.getBoolean(
                Styleable.getKey("CompoundButton_checked"), false);
        setChecked(checked);

        a.recycle();

        applyButtonTint();
    }

    @TargetApi(11)
    private PorterDuff.Mode parseTintMode(int value, PorterDuff.Mode defaultMode) {
        switch (value) {
            case 3:
                return PorterDuff.Mode.SRC_OVER;
            case 5:
                return PorterDuff.Mode.SRC_IN;
            case 9:
                return PorterDuff.Mode.SRC_ATOP;
            case 14:
                return PorterDuff.Mode.MULTIPLY;
            case 15:
                return PorterDuff.Mode.SCREEN;
            case 16:
                return PorterDuff.Mode.ADD;
            default:
                return defaultMode;
        }
    }

    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public boolean performClick() {
        toggle();

        final boolean handled = super.performClick();
        if (!handled) {
            // View only makes a sound effect if the onClickListener was
            // called, so we'll need to make one here instead.
            playSoundEffect(SoundEffectConstants.CLICK);
        }

        return handled;
    }

    @ViewDebug.ExportedProperty
    public boolean isChecked() {
        return mChecked;
    }

    /**
     * <p>Changes the checked state of this button.</p>
     *
     * @param checked true to check the button, false to uncheck it
     */
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
			/*
            notifyViewAccessibilityStateChangedIfNeeded(
                    AccessibilityEvent.CONTENT_CHANGE_TYPE_UNDEFINED);
			*/
            // Avoid infinite recursions if setChecked() is called from a listener
            if (mBroadcasting) {
                return;
            }

            mBroadcasting = true;
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(this, mChecked);
            }
            if (mOnCheckedChangeWidgetListener != null) {
                mOnCheckedChangeWidgetListener.onCheckedChanged(this, mChecked);
            }

            mBroadcasting = false;
        }
    }

    /**
     * Register a callback to be invoked when the checked state of this button
     * changes.
     *
     * @param listener the callback to call on checked state change
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    /**
     * Register a callback to be invoked when the checked state of this button
     * changes. This callback is used for internal purpose only.
     *
     * @param listener the callback to call on checked state change
     */
    void setOnCheckedChangeWidgetListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeWidgetListener = listener;
    }

    /**
     * Set the button graphic to a given Drawable, identified by its resource
     * id.
     *
     * @param resid the resource id of the drawable to use as the button
     *              graphic
     */
    public void setButtonDrawable(int resid) {
        if (resid != 0 && resid == mButtonResource) {
            return;
        }

        mButtonResource = resid;

        Drawable d = null;
        if (mButtonResource != 0) {
            d = getContext().getResources().getDrawable(mButtonResource);
        }
        setButtonDrawable(d);
    }

    /**
     * Set the button graphic to a given Drawable
     *
     * @param d The Drawable to use as the button graphic
     */
    public void setButtonDrawable(Drawable d) {
        if (mButtonDrawable != d) {
            if (mButtonDrawable != null) {
                mButtonDrawable.setCallback(null);
                unscheduleDrawable(mButtonDrawable);
            }

            mButtonDrawable = d;

            if (d != null) {
                d.setCallback(this);
                if (SDK_INT >= M) {
                    d.setLayoutDirection(getLayoutDirection());
                }
                if (d.isStateful()) {
                    d.setState(getDrawableState());
                }
                d.setVisible(getVisibility() == VISIBLE, false);
                setMinHeight(d.getIntrinsicHeight());
                applyButtonTint();
            }
        }
    }

    /**
     * @return the tint applied to the button drawable
     * @attr ref android.R.styleable#CompoundButton_buttonTint
     * @see #setButtonTintList(ColorStateList)
     */
    public ColorStateList getButtonTintList() {
        return mButtonTintList;
    }

    /**
     * Applies a tint to the button drawable. Does not modify the current tint
     * mode, which is {@link PorterDuff.Mode#SRC_IN} by default.
     * <p>
     * Subsequent calls to {@link #setButtonDrawable(Drawable)} will
     * automatically mutate the drawable and apply the specified tint and tint
     * mode using
     * {@link Drawable#setTintList(ColorStateList)}.
     *
     * @param tint the tint to apply, may be {@code null} to clear tint
     * @attr ref android.R.styleable#CompoundButton_buttonTint
     * @see #setButtonTintList(ColorStateList)
     * @see Drawable#setTintList(ColorStateList)
     */
    public void setButtonTintList(ColorStateList tint) {
        mButtonTintList = tint;
        mHasButtonTint = true;

        applyButtonTint();
    }

    /**
     * @return the blending mode used to apply the tint to the button drawable
     * @attr ref android.R.styleable#CompoundButton_buttonTintMode
     * @see #setButtonTintMode(PorterDuff.Mode)
     */
    public PorterDuff.Mode getButtonTintMode() {
        return mButtonTintMode;
    }

    /**
     * Specifies the blending mode used to apply the tint specified by
     * {@link #setButtonTintList(ColorStateList)}} to the button drawable. The
     * default mode is {@link PorterDuff.Mode#SRC_IN}.
     *
     * @param tintMode the blending mode used to apply the tint, may be
     *                 {@code null} to clear tint
     * @attr ref android.R.styleable#CompoundButton_buttonTintMode
     * @see #getButtonTintMode()
     * @see Drawable#setTintMode(PorterDuff.Mode)
     */
    public void setButtonTintMode(PorterDuff.Mode tintMode) {
        mButtonTintMode = tintMode;
        mHasButtonTintMode = true;

        applyButtonTint();
    }

    private void applyButtonTint() {
        if (SDK_INT < LOLLIPOP) {
            return;
        }

        if (mButtonDrawable != null && (mHasButtonTint || mHasButtonTintMode)) {
            mButtonDrawable = mButtonDrawable.mutate();

            if (mHasButtonTint) {
                mButtonDrawable.setTintList(mButtonTintList);
            }

            if (mHasButtonTintMode) {
                mButtonDrawable.setTintMode(mButtonTintMode);
            }

            // The drawable (or one of its children) may not have been
            // stateful before applying the tint, so let's try again.
            if (mButtonDrawable.isStateful()) {
                mButtonDrawable.setState(getDrawableState());
            }
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(YCompoundButton.class.getName());
        event.setChecked(mChecked);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if (SDK_INT >= ICE_CREAM_SANDWICH) {
            info.setClassName(YCompoundButton.class.getName());
            info.setCheckable(true);
            info.setChecked(mChecked);
        }
    }

    @Override
    public int getCompoundPaddingLeft() {
        int padding = super.getCompoundPaddingLeft();
        if (!isLayoutRtl()) {
            final Drawable buttonDrawable = mButtonDrawable;
            if (buttonDrawable != null) {
                padding += buttonDrawable.getIntrinsicWidth();
            }
        }
        return padding;
    }

    public boolean isLayoutRtl() {
        return SDK_INT >= JELLY_BEAN_MR1 &&
                getResources().getConfiguration().getLayoutDirection() == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL;
    }

    @Override
    public int getCompoundPaddingRight() {
        int padding = super.getCompoundPaddingRight();
        if (isLayoutRtl()) {
            final Drawable buttonDrawable = mButtonDrawable;
            if (buttonDrawable != null) {
                padding += buttonDrawable.getIntrinsicWidth();
            }
        }
        return padding;
    }

    public int getHorizontalOffsetForDrawables() {
        final Drawable buttonDrawable = mButtonDrawable;
        return (buttonDrawable != null) ? buttonDrawable.getIntrinsicWidth() : 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final Drawable buttonDrawable = mButtonDrawable;
        if (buttonDrawable != null) {
            final int verticalGravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;
            final int drawableHeight = buttonDrawable.getIntrinsicHeight();
            final int drawableWidth = buttonDrawable.getIntrinsicWidth();

            final int top;
            switch (verticalGravity) {
                case Gravity.BOTTOM:
                    top = getHeight() - drawableHeight;
                    break;
                case Gravity.CENTER_VERTICAL:
                    top = (getHeight() - drawableHeight) / 2;
                    break;
                default:
                    top = 0;
            }
            final int bottom = top + drawableHeight;
            final int left = isLayoutRtl() ? getWidth() - drawableWidth : 0;
            final int right = isLayoutRtl() ? getWidth() : drawableWidth;

            buttonDrawable.setBounds(left, top, right, bottom);

            final Drawable background = getBackground();
            if (background != null) {
                if (SDK_INT >= LOLLIPOP) {
                    background.setHotspotBounds(left, top, right, bottom);
                } else {
                    background.setBounds(left, top, right, bottom);
                }
            }
        }

        super.onDraw(canvas);

        if (buttonDrawable != null) {
            final int scrollX = getScrollX();
            final int scrollY = getScrollY();
            if (scrollX == 0 && scrollY == 0) {
                buttonDrawable.draw(canvas);
            } else {
                canvas.translate(scrollX, scrollY);
                buttonDrawable.draw(canvas);
                canvas.translate(-scrollX, -scrollY);
            }
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        if (mButtonDrawable != null) {
            int[] myDrawableState = getDrawableState();

            // Set the state of the Drawable
            mButtonDrawable.setState(myDrawableState);

            invalidate();
        }
    }

    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);

        if (mButtonDrawable != null && SDK_INT >= LOLLIPOP) {
            mButtonDrawable.setHotspot(x, y);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == mButtonDrawable;
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mButtonDrawable != null && SDK_INT >= HONEYCOMB) {
            mButtonDrawable.jumpToCurrentState();
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.checked = isChecked();
        return ss;
    }

    /**
     * Fix an Android bug by check class of Parcelable object
     * <p>
     * BUG: java.lang.ClassCastException: android.view.AbsSavedState$1
     * cannot be cast to android.widget.CompoundButton$SavedState
     *
     * @author frknkrc44
     */
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof AbsSavedState) {
            AbsSavedState ss = (AbsSavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            if (ss instanceof SavedState) {
                SavedState st = (SavedState) ss;
                setChecked(st.checked);
            }
            requestLayout();
            return;
        }
        super.onRestoreInstanceState(state);
        requestLayout();
    }

    /**
     * Interface definition for a callback to be invoked when the checked state
     * of a compound button changed.
     */
    public static interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param buttonView The compound button view whose state has changed.
         * @param isChecked  The new checked state of buttonView.
         */
        void onCheckedChanged(YCompoundButton buttonView, boolean isChecked);
    }

    public static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean checked;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            checked = (Boolean) in.readValue(getClass().getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(checked);
        }

        @Override
        public String toString() {
            return "CompoundButton.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " checked=" + checked + "}";
        }
    }
}

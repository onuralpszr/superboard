/*
 * Copyright (C) 2006 The Android Open Source Project
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

/**
 * This is custom Insets class for Switch and CompoundButton
 */

package yandroid.graphics;

import android.graphics.Rect;

import java.lang.reflect.Field;

/**
 * An Insets instance holds four integer offsets which describe changes to the four
 * edges of a Rectangle. By convention, positive values move edges towards the
 * centre of the rectangle.
 * <p>
 * Insets are immutable so may be treated as values.
 */

@SuppressWarnings("unused")
public class Insets {

    public static final Insets NONE = new Insets(0, 0, 0, 0);

    public int left;
    public int top;
    public int right;
    public int bottom;

    /**
     * Adapt system's Insets object to custom one
     *
     * @author frknkrc44
     */
    public Insets(Object insObj) {
        try {
            if (!insObj.getClass().getName().equals("android.graphics.Insets")) {
                throw new RuntimeException("Invalid class name: " + insObj.getClass().getName());
            }
            String[] fields = {"left", "top", "right", "bottom"};
            for (String field : fields) {
                Field f = insObj.getClass().getDeclaredField(field);
                f.setAccessible(true);
                switch (field) {
                    case "left":
                        left = (int) f.get(insObj);
                        break;
                    case "top":
                        top = (int) f.get(insObj);
                        break;
                    case "right":
                        right = (int) f.get(insObj);
                        break;
                    case "bottom":
                        bottom = (int) f.get(insObj);
                        break;
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public Insets(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    /**
     * Rect support for Insets class
     *
     * @author frknkrc44
     */
    public Insets(Rect rect) {
        left = rect.left;
        top = rect.top;
        right = rect.right;
        bottom = rect.bottom;
    }

    // Factory methods

    /**
     * Return an Insets instance with the appropriate values.
     *
     * @param left   the left inset
     * @param top    the top inset
     * @param right  the right inset
     * @param bottom the bottom inset
     * @return Insets instance with the appropriate values
     */
    public static Insets of(int left, int top, int right, int bottom) {
        if (left == 0 && top == 0 && right == 0 && bottom == 0) {
            return NONE;
        }
        return new Insets(left, top, right, bottom);
    }

    /**
     * Return an Insets instance with the appropriate values.
     *
     * @param r the rectangle from which to take the values
     * @return an Insets instance with the appropriate values
     */
    public static Insets of(Rect r) {
        return (r == null) ? NONE : of(r.left, r.top, r.right, r.bottom);
    }

    /**
     * Two Insets instances are equal iff they belong to the same class and their fields are
     * pairwise equal.
     *
     * @param o the object to compare this instance with.
     * @return true iff this object is equal {@code o}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Insets insets = (Insets) o;

        if (bottom != insets.bottom) return false;
        if (left != insets.left) return false;
        if (right != insets.right) return false;
        return top == insets.top;
    }

    @Override
    public int hashCode() {
        int result = left;
        result = 31 * result + top;
        result = 31 * result + right;
        result = 31 * result + bottom;
        return result;
    }

    @Override
    public String toString() {
        return "Insets{" +
                "left=" + left +
                ", top=" + top +
                ", right=" + right +
                ", bottom=" + bottom +
                '}';
    }
}

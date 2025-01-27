package yandroid.graphics;
/**
 * A little hack to get required hidden
 * methods for Switch and CompoundButton
 *
 * @author frknkrc44
 */

import android.annotation.SuppressLint;
import android.content.res.Resources;

import java.lang.reflect.Method;

public class ResourcesUtils {

    /**
     * Access to hidden getCompatibilityInfo method of Resources
     *
     * @author frknkrc44
     */
    @SuppressLint("DiscouragedPrivateApi")
    public static Object getCompatibilityInfo(Resources res) {
        try {
            Method m = Resources.class.getDeclaredMethod("getCompatibilityInfo");
            m.setAccessible(true);
            return m.invoke(res);
        } catch (Throwable ignored) {
        }
        return null;
    }

}

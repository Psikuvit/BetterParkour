package psikuvit.parkour.Utils;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import org.bukkit.Bukkit;

public class Reflection
{
    public static Class<?> getNMSClass(final String str) {
        return getClass("net.minecraft.server." + getVersion() + "." + str);
    }
    
    public static Class<?> getCraftClass(final String str) {
        return getClass("org.bukkit.craftbukkit." + getVersion() + "." + str);
    }
    
    public static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }
    
    public static Class<?> getClass(final String className) {
        try {
            return Class.forName(className);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static void setValue(final Object obj, final String name, final Object value) {
        try {
            final Field declaredField = obj.getClass().getDeclaredField(name);
            declaredField.setAccessible(true);
            declaredField.set(obj, value);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static Object getValue(final Object obj, final String name) {
        try {
            final Field declaredField = obj.getClass().getDeclaredField(name);
            if (!declaredField.isAccessible()) {
                declaredField.setAccessible(true);
            }
            return declaredField.get(obj);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static Method getMethod(final Object o, final String name, final Class<?>... parameterTypes) {
        try {
            final Method method = o.getClass().getMethod(name, parameterTypes);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static Field getField(final Field field) {
        field.setAccessible(true);
        return field;
    }
}

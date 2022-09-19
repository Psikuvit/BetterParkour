package psikuvit.parkour.TitleAPI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import psikuvit.parkour.Utils.Reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

public class Title
{
    private static Class<?> titlePacket;
    private static Class<?> enumTitleAction;
    private static Class<?> chatSerializer;
    private static Class<?> craftPlayer;
    private static Class<?> packet;
    private static Method getHandle;
    private static boolean invoked;
    private String title;
    private String subTitle;
    private int fadeIn;
    private int stay;
    private int fadeOut;
    private static int versionId;
    
    public Title() {
        if (!Title.invoked) {
            try {
                Title.packet = Reflection.getNMSClass("Packet");
                Title.craftPlayer = Reflection.getCraftClass("entity.CraftPlayer");
                Title.getHandle = Title.craftPlayer.getDeclaredMethod("getHandle");
                Title.titlePacket = Reflection.getNMSClass("PacketPlayOutTitle");
                Title.enumTitleAction = Reflection.getNMSClass("PacketPlayOutTitle$EnumTitleAction");
                Title.chatSerializer = Reflection.getNMSClass("IChatBaseComponent$ChatSerializer");
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            Title.invoked = true;
        }
    }
    
    public void setTitle(final String title) {
        this.title = title;
    }
    
    public Title setSubTitle(final String subTitle) {
        this.subTitle = subTitle;
        return this;
    }
    
    public void setFadeIn(final int fadeIn) {
        this.fadeIn = fadeIn;
    }
    
    public void setStay(final int stay) {
        this.stay = stay;
    }
    
    public void setFadeOut(final int fadeOut) {
        this.fadeOut = fadeOut;
    }

    public void sendTitle(final Collection<? extends Player> collection) {
        try {
            for (final Player obj : collection) {
                final Object instance = Title.titlePacket.newInstance();
                final Field declaredField = instance.getClass().getDeclaredField("a");
                declaredField.setAccessible(true);
                declaredField.set(instance, Reflection.getField(Title.enumTitleAction.getDeclaredField("TITLE")).get(null));
                final Field declaredField2 = instance.getClass().getDeclaredField("b");
                declaredField2.setAccessible(true);
                if (Title.versionId >= 12) {
                    declaredField2.set(instance, Title.chatSerializer.getMethod("a", String.class).invoke(null, "{\"text\": \"" + this.title + '\"' + "}"));
                }
                else {
                    declaredField2.set(instance, Title.chatSerializer.getMethod("a", String.class).invoke(null, "{'text': '" + this.title + "'}"));
                }
                final Object value = Reflection.getValue(Title.getHandle.invoke(obj), "playerConnection");
                Reflection.getMethod(value, "sendPacket", Title.packet).invoke(value, instance);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public Title sendSubTitle(final Collection<? extends Player> collection) {
        try {
            for (final Player obj : collection) {
                final Object instance = Title.titlePacket.newInstance();
                final Field declaredField = instance.getClass().getDeclaredField("a");
                declaredField.setAccessible(true);
                declaredField.set(instance, Reflection.getField(Title.enumTitleAction.getDeclaredField("SUBTITLE")).get(null));
                final Field declaredField2 = instance.getClass().getDeclaredField("b");
                declaredField2.setAccessible(true);
                if (Title.versionId >= 12) {
                    declaredField2.set(instance, Title.chatSerializer.getMethod("a", String.class).invoke(null, "{\"text\": \"" + this.subTitle + '\"' + "}"));
                }
                else {
                    declaredField2.set(instance, Title.chatSerializer.getMethod("a", String.class).invoke(null, "{'text': '" + this.subTitle + "'}"));
                }
                final Object value = Reflection.getValue(Title.getHandle.invoke(obj), "playerConnection");
                Reflection.getMethod(value, "sendPacket", Title.packet).invoke(value, instance);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return this;
    }
    
    public void sendTimes(final Collection<? extends Player> collection) {
        try {
            for (final Player obj : collection) {
                final Object instance = Title.titlePacket.newInstance();
                final Field declaredField = instance.getClass().getDeclaredField("a");
                declaredField.setAccessible(true);
                declaredField.set(instance, Reflection.getField(Title.enumTitleAction.getDeclaredField("TIMES")).get(null));
                final Field declaredField2 = instance.getClass().getDeclaredField("c");
                declaredField2.setAccessible(true);
                declaredField2.set(instance, this.fadeIn);
                final Field declaredField3 = instance.getClass().getDeclaredField("d");
                declaredField3.setAccessible(true);
                declaredField3.set(instance, this.stay);
                final Field declaredField4 = instance.getClass().getDeclaredField("e");
                declaredField4.setAccessible(true);
                declaredField4.set(instance, this.fadeOut);
                final Object value = Reflection.getValue(Title.getHandle.invoke(obj), "playerConnection");
                Reflection.getMethod(value, "sendPacket", Title.packet).invoke(value, instance);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    static {
        Title.versionId = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].replace(".", "#").split("#")[1]);
    }
}

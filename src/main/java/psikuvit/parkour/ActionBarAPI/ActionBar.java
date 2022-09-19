package psikuvit.parkour.ActionBarAPI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import psikuvit.parkour.Utils.Reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ActionBar
{
    private static Class<?> craftPlayer;
    private static Class<?> chatPacket;
    private static Class<?> chatComponentText;
    private static Class<?> chatBaseComponent;
    private static Class<?> chatMessageType;
    private static Class<?> packet;
    private static Method getHandle;
    private static Constructor<?> constructChatPacket;
    private static Constructor<?> constructChatText;
    private static boolean invoked;
    private static int versionId;
    private String message;

    public ActionBar() {
        if (!ActionBar.invoked) {
            try {
                ActionBar.craftPlayer = Reflection.getCraftClass("entity.CraftPlayer");
                ActionBar.chatPacket = Reflection.getNMSClass("PacketPlayOutChat");
                ActionBar.packet = Reflection.getNMSClass("Packet");
                ActionBar.chatBaseComponent = Reflection.getNMSClass("IChatBaseComponent");
                ActionBar.chatComponentText = Reflection.getNMSClass("ChatComponentText");
                if (ActionBar.versionId >= 16) {
                    ActionBar.chatMessageType = Reflection.getNMSClass("ChatMessageType");
                    ActionBar.constructChatPacket = ActionBar.chatPacket.getConstructor();
                }
                else if (ActionBar.versionId >= 12) {
                    ActionBar.chatMessageType = Reflection.getNMSClass("ChatMessageType");
                    ActionBar.constructChatPacket = ActionBar.chatPacket.getConstructor(ActionBar.chatBaseComponent);
                }
                else {
                    ActionBar.constructChatPacket = ActionBar.chatPacket.getConstructor(ActionBar.chatBaseComponent, Byte.TYPE);
                }
                ActionBar.constructChatText = ActionBar.chatComponentText.getConstructor(String.class);
                ActionBar.getHandle = ActionBar.craftPlayer.getDeclaredMethod("getHandle");
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            ActionBar.invoked = true;
        }
    }

    public void setMessage(final Object obj) {
        this.message = String.valueOf(obj);
    }

    public void send(final Player... array) {
        try {
            for (final Player obj : array) {
                final Object instance = ActionBar.constructChatText.newInstance(this.message);
                Object o;
                if (ActionBar.versionId >= 16) {
                    o = ActionBar.constructChatPacket.newInstance();
                    final Field declaredField = o.getClass().getDeclaredField("a");
                    declaredField.setAccessible(true);
                    declaredField.set(o, instance);
                    final Field declaredField2 = o.getClass().getDeclaredField("b");
                    declaredField2.setAccessible(true);
                    declaredField2.set(o, Reflection.getField(ActionBar.chatMessageType.getDeclaredField("GAME_INFO")).get(null));
                    final Field declaredField3 = o.getClass().getDeclaredField("c");
                    declaredField3.setAccessible(true);
                    declaredField3.set(o, obj.getUniqueId());
                }
                else if (ActionBar.versionId >= 12) {
                    o = ActionBar.constructChatPacket.newInstance(instance);
                    final Field declaredField4 = o.getClass().getDeclaredField("b");
                    declaredField4.setAccessible(true);
                    declaredField4.set(o, Reflection.getField(ActionBar.chatMessageType.getDeclaredField("GAME_INFO")).get(null));
                }
                else {
                    o = ActionBar.constructChatPacket.newInstance(instance, (byte) 2);
                }
                final Object value = Reflection.getValue(ActionBar.getHandle.invoke(obj), "playerConnection");
                Reflection.getMethod(value, "sendPacket", ActionBar.packet).invoke(value, o);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static {
        ActionBar.versionId = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].replace(".", "#").split("#")[1]);
    }
}

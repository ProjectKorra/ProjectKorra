package com.projectkorra.projectkorra.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.util.ReflectionHandler.PackageType;

public class ActionBar {

	private static boolean initialised = false;
	private static Constructor<?> chatSer;
	private static Constructor<?> packetChat;
	public static Method getHandle;
	private static Field playerConnection;
	private static Method sendPacket;
	private static int version;

	static {
		try {
			version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
			chatSer = ReflectionHandler.getConstructor(PackageType.MINECRAFT_SERVER.getClass("ChatComponentText"), String.class);
			if (version >= 12) {
				packetChat = PackageType.MINECRAFT_SERVER.getClass("PacketPlayOutChat").getConstructor(PackageType.MINECRAFT_SERVER.getClass("IChatBaseComponent"), PackageType.MINECRAFT_SERVER.getClass("ChatMessageType"));
			} else {
				packetChat = PackageType.MINECRAFT_SERVER.getClass("PacketPlayOutChat").getConstructor(PackageType.MINECRAFT_SERVER.getClass("IChatBaseComponent"), byte.class);
			}
			getHandle = ReflectionHandler.getMethod("CraftPlayer", PackageType.CRAFTBUKKIT_ENTITY, "getHandle");
			playerConnection = ReflectionHandler.getField("EntityPlayer", PackageType.MINECRAFT_SERVER, false, "playerConnection");
			sendPacket = ReflectionHandler.getMethod(playerConnection.getType(), "sendPacket", PackageType.MINECRAFT_SERVER.getClass("Packet"));
			initialised = true;
		}
		catch (ReflectiveOperationException e) {
			initialised = false;
		}
	}

	public static boolean isInitialised() {
		return initialised;
	}

	public static boolean sendActionBar(String message, Player... player) {
		if (!initialised) {
			return false;
		}
		try {
			Object o = chatSer.newInstance(message);
			Object packet;
			if (version >= 12) {
				packet = packetChat.newInstance(o, PackageType.MINECRAFT_SERVER.getClass("ChatMessageType").getEnumConstants()[2]);
			} else {
				packet = packetChat.newInstance(o, (byte)2);
			}
			sendTo(packet, player);
		}
		catch (ReflectiveOperationException e) {
			e.printStackTrace();
			initialised = false;
		}
		return initialised;
	}

	private static void sendTo(Object packet, Player... player) throws ReflectiveOperationException {
		for (Player p : player) {
			Object entityplayer = getHandle.invoke(p);
			Object PlayerConnection = playerConnection.get(entityplayer);
			sendPacket.invoke(PlayerConnection, packet);
		}
	}
}

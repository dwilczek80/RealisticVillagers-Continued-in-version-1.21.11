package me.matsubara.realisticvillagers.util;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

/**
 * Bukkit API that was renamed or reshaped between the oldest and newest versions this plugin
 * runs on.
 * <p>
 * Core is compiled against one API (1.21.8) but runs from 1.19.4 up, and a direct reference to
 * a name that doesn't exist on the running server doesn't fail at startup — the JVM resolves it
 * the first time that line runs, as a {@code NoSuchFieldError}/{@code NoSuchMethodError}, which
 * for a villager's AI tick means the villager is discarded. Everything here looks the thing up
 * by its stable name instead, so a missing one is a value the caller can handle.
 */
public final class BukkitCompat {

    /** Max health: {@code MAX_HEALTH} on newer APIs, {@code GENERIC_MAX_HEALTH} before. */
    public static final @Nullable Attribute MAX_HEALTH = attribute("MAX_HEALTH", "GENERIC_MAX_HEALTH");

    private static final @Nullable Method LEGACY_SPAWN;
    private static final @Nullable Class<?> LEGACY_CONSUMER;

    static {
        Method legacySpawn = null;
        Class<?> legacyConsumer = null;
        try {
            // The overload taking java.util.function.Consumer is newer than the one taking Bukkit's own.
            World.class.getMethod("spawn", Location.class, Class.class, Consumer.class);
        } catch (NoSuchMethodException newerMissing) {
            try {
                legacyConsumer = Class.forName("org.bukkit.util.Consumer");
                legacySpawn = World.class.getMethod("spawn", Location.class, Class.class, legacyConsumer);
            } catch (ReflectiveOperationException ignored) {
                // Neither exists; spawn() below reports it when something actually needs it.
            }
        }
        LEGACY_SPAWN = legacySpawn;
        LEGACY_CONSUMER = legacyConsumer;
    }

    private BukkitCompat() {
    }

    /** The enchantment with this vanilla key, or {@code null} on a version that doesn't have it (yet). */
    @SuppressWarnings("deprecation")
    public static @Nullable Enchantment enchantment(@NotNull String key) {
        return Enchantment.getByKey(NamespacedKey.minecraft(key));
    }

    /** Level of {@code enchantment} on {@code item}, 0 if the server has no such enchantment. */
    public static int level(org.bukkit.inventory.@NotNull ItemStack item, @Nullable Enchantment enchantment) {
        return enchantment == null ? 0 : item.getEnchantmentLevel(enchantment);
    }

    /** The first of these constants {@link Attribute} has, since the names changed in 1.21.2. */
    public static @Nullable Attribute attribute(@NotNull String... names) {
        for (String name : names) {
            try {
                Field field = Attribute.class.getField(name);
                if (field.get(null) instanceof Attribute attribute) return attribute;
            } catch (ReflectiveOperationException ignored) {
                // Try the next name.
            }
        }
        return null;
    }

    /**
     * {@link World#spawn(Location, Class, Consumer)}, which only takes a
     * {@code java.util.function.Consumer} on newer versions; older ones want Bukkit's own type.
     * <p>
     * {@code before} runs before the entity is added to the world either way — that's the whole
     * point of the consumer overload (settings applied after the add would flash for a tick,
     * and visibility set that late has already been sent to everyone).
     */
    public static <T extends Entity> @NotNull T spawn(
            @NotNull World world,
            @NotNull Location at,
            @NotNull Class<T> type,
            @NotNull Consumer<T> before) {
        if (LEGACY_SPAWN == null || LEGACY_CONSUMER == null) {
            return world.spawn(at, type, before);
        }

        Object adapter = Proxy.newProxyInstance(
                LEGACY_CONSUMER.getClassLoader(),
                new Class<?>[]{LEGACY_CONSUMER},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == args[0];
                            default -> "BukkitCompat consumer";
                        };
                    }
                    if (method.getName().equals("accept")) {
                        before.accept(type.cast(args[0]));
                    }
                    return null;
                });

        try {
            return type.cast(LEGACY_SPAWN.invoke(world, at, type, adapter));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Couldn't spawn " + type.getSimpleName() + " on this server version", exception);
        }
    }
}

package me.matsubara.realisticvillagers.nms.v26_1;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * The handful of CraftBukkit/NMS calls whose shape differs between Spigot and Paper (and its
 * forks, Purpur included) on 26.3.
 * <p>
 * This module compiles against Spigot, but most servers run Paper, and since 26.3 Paper's
 * CraftBukkit has drifted: {@code CraftItemStack#asCraftMirror} became {@code asBukkitMirror},
 * {@code CraftEventFactory#callEntityPickupItemEvent} takes a {@code LivingEntity}, and
 * {@code BlocksAttacks#disable} takes the attacker as an extra argument. The JVM links a call by
 * its exact descriptor, so a direct call compiled against the other family doesn't fail to
 * compile — it fails with {@code NoSuchMethodError} the first time a villager's AI reaches it,
 * which looks like the plugin crashing mid-game rather than at startup.
 * <p>
 * Everything is therefore resolved once, by name, when this class loads, trying both shapes.
 * {@link #verify()} forces that at startup so an unresolvable build is reported there.
 */
public final class CraftCompat {

    private static final MethodHandle MIRROR = resolveMirror();
    private static final MethodHandle PICKUP = resolvePickup();

    private static final MethodHandle DISABLE;
    private static final boolean DISABLE_TAKES_ATTACKER;

    static {
        MethodHandle disable;
        boolean takesAttacker;
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            disable = lookup.findVirtual(BlocksAttacks.class, "disable", MethodType.methodType(
                    void.class, ServerLevel.class, LivingEntity.class, float.class, ItemStack.class, LivingEntity.class));
            takesAttacker = true;
        } catch (ReflectiveOperationException paper) {
            try {
                disable = lookup.findVirtual(BlocksAttacks.class, "disable", MethodType.methodType(
                        void.class, ServerLevel.class, LivingEntity.class, float.class, ItemStack.class));
                takesAttacker = false;
            } catch (ReflectiveOperationException spigot) {
                throw new IllegalStateException("BlocksAttacks#disable has neither known signature on this server build", spigot);
            }
        }
        DISABLE = disable;
        DISABLE_TAKES_ATTACKER = takesAttacker;
    }

    private CraftCompat() {
    }

    /** Touching this class resolves every method above, so a server this can't run on says so at startup. */
    public static void verify() {
    }

    /**
     * A Bukkit view over {@code stack} that writes through to it — Spigot's
     * {@code asCraftMirror}, Paper's {@code asBukkitMirror}.
     */
    public static org.bukkit.inventory.@NotNull ItemStack mirror(@NotNull ItemStack stack) {
        try {
            return (org.bukkit.inventory.ItemStack) MIRROR.invokeExact(stack);
        } catch (Throwable throwable) {
            throw rethrow(throwable);
        }
    }

    public static @NotNull EntityPickupItemEvent callPickupEvent(
            @NotNull LivingEntity picker,
            @NotNull ItemEntity item,
            int remaining,
            boolean cancelled) {
        try {
            return (EntityPickupItemEvent) PICKUP.invoke(picker, item, remaining, cancelled);
        } catch (Throwable throwable) {
            throw rethrow(throwable);
        }
    }

    /** Puts {@code shield} on cooldown for {@code seconds}, crediting {@code attacker} where the server asks who did it. */
    public static void disableBlocking(
            @NotNull BlocksAttacks attacks,
            @NotNull ServerLevel level,
            @NotNull LivingEntity defender,
            float seconds,
            @NotNull ItemStack shield,
            @NotNull LivingEntity attacker) {
        try {
            if (DISABLE_TAKES_ATTACKER) {
                DISABLE.invoke(attacks, level, defender, seconds, shield, attacker);
            } else {
                DISABLE.invoke(attacks, level, defender, seconds, shield);
            }
        } catch (Throwable throwable) {
            throw rethrow(throwable);
        }
    }

    private static MethodHandle resolveMirror() {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType wanted = MethodType.methodType(org.bukkit.inventory.ItemStack.class, ItemStack.class);

        // Newest name first; each can return either the Bukkit type or CraftItemStack itself.
        for (String name : new String[]{"asBukkitMirror", "asCraftMirror"}) {
            for (Class<?> returned : new Class<?>[]{org.bukkit.inventory.ItemStack.class, CraftItemStack.class}) {
                try {
                    return lookup.findStatic(CraftItemStack.class, name, MethodType.methodType(returned, ItemStack.class))
                            .asType(wanted);
                } catch (ReflectiveOperationException ignored) {
                    // Try the next shape.
                }
            }
        }
        throw new IllegalStateException("CraftItemStack has neither asBukkitMirror nor asCraftMirror on this server build");
    }

    private static MethodHandle resolvePickup() {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // Paper takes the picking-up LivingEntity, Spigot the more general Entity.
        for (Class<?> picker : new Class<?>[]{LivingEntity.class, Entity.class}) {
            try {
                return lookup.findStatic(CraftEventFactory.class, "callEntityPickupItemEvent", MethodType.methodType(
                        EntityPickupItemEvent.class, picker, ItemEntity.class, int.class, boolean.class));
            } catch (ReflectiveOperationException ignored) {
                // Try the next shape.
            }
        }
        throw new IllegalStateException("CraftEventFactory#callEntityPickupItemEvent has no known signature on this server build");
    }

    private static RuntimeException rethrow(Throwable throwable) {
        if (throwable instanceof RuntimeException runtime) return runtime;
        if (throwable instanceof Error error) throw error;
        return new IllegalStateException(throwable);
    }
}

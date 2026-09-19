package me.matsubara.realisticvillagers.compatibility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.listeners.MerchantListener;
import me.matsubara.realisticvillagers.RealisticVillagers;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hands a villager's trading over to ValhallaMMO's premium merchant system, for servers running
 * it alongside this plugin.
 * <p>
 * Valhalla exposes no "open your interface for this villager" call. Its own trading lives entirely
 * inside its {@code PlayerInteractEntityEvent} listener — it decides whether the villager is one of
 * its custom merchants, builds the recipe list, and opens a vanilla-shaped {@code MerchantInventory}
 * that its own {@code InventoryOpenEvent}/{@code InventoryClickEvent} hooks then take over. So
 * rather than reimplement any of that, this class hands Valhalla a synthetic copy of the very
 * event it already knows how to handle — the same approach already used for VillagerTradeLimiter
 * in {@link VTLCompatibility}, and for the same reason: it is the only way to get another plugin's
 * closed logic to run correctly without guessing at what it does internally.
 * <p>
 * There is no public getter for the listener instance the way VTL offers one, so it is found the
 * way any plugin's registered listener can be found without one: among the handlers Bukkit already
 * has registered for the event.
 * <p>
 * The window a call here opens is expected to have already been marked with
 * {@link #expect(UUID, RealisticVillagers)} by the caller <i>before</i> this runs — see that
 * method's note on why marking lives there rather than in here.
 */
public class ValhallaCompatibility implements Compatibility {

    /**
     * Players a deliberate call into trading — this class's, or the plugin's own vanilla fallback
     * for when this class declined — is about to open a merchant window for.
     * <p>
     * Exists because of one thing found reading Valhalla's own bytecode: its interact handler is
     * registered at {@code EventPriority.HIGHEST} and never checks {@code event.isCancelled()} —
     * only this plugin's own priority and {@code ignoreCancelled} setting decides whether its menu
     * opens, not Valhalla's. So a raw right-click on a villager Valhalla already considers one of
     * its own custom merchants can open Valhalla's trading window on its own, a tick before this
     * plugin's menu would have appeared, with nothing here having asked for it. A guard elsewhere
     * (see {@code VillagerListeners#onValhallaMerchantOpen}) closes any merchant window that opens
     * for a managed villager without this having said it was expected.
     * <p>
     * That guard cannot tell "Valhalla opened this uninvited" apart from "the plugin's own vanilla
     * trading opened this, exactly as intended" by looking at the window alone — both are the same
     * kind of window, a plain {@code MerchantInventory}. So this set covers both outcomes of a
     * deliberate trade attempt, not only the one that goes through Valhalla, which is also why the
     * marking happens once, in the caller that knows about both branches, rather than in here.
     */
    private static final Set<UUID> EXPECTING = ConcurrentHashMap.newKeySet();

    /**
     * How long a player is allowed to stay "expecting" before the safety sweep drops them, in
     * ticks.
     * <p>
     * Generous rather than tight. {@code CustomMerchantManager.getMerchantData} can genuinely go
     * to disk for a villager whose data was not already cached, and the window that opens once it
     * answers is still one this plugin asked for. A hole held open a quarter of a second longer
     * than it had to be is nothing; a trade window closed out from under a player because the
     * sweep fired a moment too early is a real complaint.
     */
    private static final long EXPECTING_TIMEOUT_TICKS = 5L;

    /**
     * Marks a player as about to have a deliberately-opened trade window, before the attempt that
     * opens it runs.
     * <p>
     * Swept up a few ticks later regardless of what happens, so a click that opened nothing at all
     * — Valhalla declining, or the vanilla fallback finding an empty trade list and shaking its
     * head instead of opening anything — never leaves the player marked, which would otherwise let
     * some later, unrelated Valhalla open for them through unchallenged.
     */
    public static void expect(@NotNull UUID player, @NotNull RealisticVillagers plugin) {
        EXPECTING.add(player);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> EXPECTING.remove(player), EXPECTING_TIMEOUT_TICKS);
    }

    /**
     * Whether an unclaimed {@code MerchantInventory} open for this player should be let through.
     * <p>
     * One-shot: answering the question consumes it, so a second, unrelated open for the same
     * player right after — Valhalla reacting to some other click — is judged on its own, not
     * waved through on the coat-tails of the first.
     */
    public static boolean isExpecting(@NotNull UUID player) {
        return EXPECTING.remove(player);
    }

    @Override
    public boolean shouldTrack(Villager villager) {
        // Valhalla changes what trading looks like, not whether this plugin should be managing
        // the villager at all — her skin, family, schedule and everything else carry on exactly
        // as they would without it installed.
        return true;
    }

    /**
     * Opens Valhalla's own trading interface for this villager, converting her into one of its
     * custom merchants first if she is not one yet.
     * <p>
     * Callers are expected to have already marked the player with {@link #expect}; this method
     * only decides whether Valhalla has anything to offer and, if so, asks it to open — it does
     * not touch the expecting set itself, since the caller also owns the vanilla fallback this
     * declining leads to, and that fallback's own window needs the same marking.
     *
     * @return {@code false} when Valhalla has nothing to offer here — its trading system is
     * switched off server-wide, or nobody has configured a merchant type for this villager's
     * profession — in which case the caller falls back to ordinary trading exactly as if Valhalla
     * were not installed at all.
     */
    public boolean openTrade(
            @NotNull RealisticVillagers plugin,
            @NotNull Plugin valhalla,
            @NotNull Player player,
            @NotNull AbstractVillager villager) {

        if (!ValhallaMMO.isTradingSystemEnabled()) return false;

        if (!CustomMerchantManager.isCustomMerchant(villager)) {
            // Picks a merchant type from whatever is configured for her profession. Returns null,
            // cleanly, when there is nothing configured for it — never throws — so this is a
            // proper "no" rather than a fault to guard against.
            MerchantData created = CustomMerchantManager.convertToRandomMerchant(villager, player);
            if (created == null) return false;
        }

        MerchantListener listener = liveListener(valhalla);
        if (listener == null) return false;

        // The same click Valhalla would have seen had this plugin never intercepted the real one
        // — right hand, nothing more. Everything past this point — happiness, reputation, the
        // discount on the price, the recipes themselves — is Valhalla's own logic, running exactly
        // as it does for anyone who right-clicks one of its merchants directly.
        listener.onVillagerInteract(new PlayerInteractEntityEvent(player, villager, EquipmentSlot.HAND));
        return true;
    }

    /**
     * The listener Valhalla itself registered for this event, found the way any plugin's own
     * listener can be found when it offers no getter for it: among the ones Bukkit already has on
     * file for the event, rather than one built fresh here. A fresh instance would read the same
     * configuration back out — its fields are nothing but config values — but the one Bukkit is
     * already holding is the one actually wired into everything else Valhalla does, and there is
     * no reason to take even that small a risk when the real one is sitting right there.
     */
    private @Nullable MerchantListener liveListener(@NotNull Plugin valhalla) {
        for (RegisteredListener registered : PlayerInteractEntityEvent.getHandlerList().getRegisteredListeners()) {
            if (registered.getPlugin().equals(valhalla) && registered.getListener() instanceof MerchantListener listener) {
                return listener;
            }
        }
        return null;
    }
}

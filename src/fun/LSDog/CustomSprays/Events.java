package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.Data.DataManager;
import fun.LSDog.CustomSprays.Data.DataMySQL;
import fun.LSDog.CustomSprays.manager.SpraysManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 实现双击F喷漆
 */
public class Events implements Listener {

    private static final Map<UUID, Long> timeMap = new HashMap<>();

    private static final int CD = 350;
    // double click in 350 ms

    @EventHandler (priority = EventPriority.MONITOR)
    public void onToggleF(PlayerSwapHandItemsEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(CustomSprays.instant, () -> {
            Player player = e.getPlayer();
            UUID uuid = player.getUniqueId();
            Long t = timeMap.get(uuid);
            if ( t==null || System.currentTimeMillis() > t) {
                timeMap.put(uuid, System.currentTimeMillis() + CD);
            } else {
                timeMap.remove(uuid);
                if (!player.isSneaking()) { // 小喷漆
                    Bukkit.getScheduler().runTaskAsynchronously(CustomSprays.instant, () -> CustomSprays.spray(player, false));
                } else { // 大喷漆
                    Bukkit.getScheduler().runTaskAsynchronously(CustomSprays.instant, () -> CustomSprays.spray(player, true));
                }
            }
        });
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        // 初始化账户
        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomSprays.instant, () -> {
            if (e.getPlayer().isOnline() && DataManager.data instanceof DataMySQL) {
                DataMySQL.addAccountIfNotExist(e.getPlayer());
            }
        }, 10L);
        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomSprays.instant, () -> SpraysManager.playerSprayMap.forEach((uuid, sprays) -> sprays.forEach(spray -> {
            try {
                spray.spawn(Collections.singletonList(e.getPlayer()), false);
            } catch (ReflectiveOperationException exception) {
                exception.printStackTrace();
            }
        })), 20L);
        if (CustomSprays.latestVersion != null && e.getPlayer().isOp()) {
            e.getPlayer().sendMessage(CustomSprays.prefix + " §6§l嘿, 管理! CustomSprays 有个更新~~ §7-> §b§l" + CustomSprays.latestVersion);
            e.getPlayer().sendMessage(CustomSprays.prefix + " §6§lHey, OP! CustomSprays has an update~~ §7-> §b§l" + CustomSprays.latestVersion);
            //e.getPlayer().sendRawMessage("[{\"text\":\"*CustomSprays*\",\"color\":\"dark_blue\",\"bold\":true,\"italic\":true,\"underlined\":true,\"strikethrough\":false,\"obfuscated\":false,\"insertion\":\"https://gitee.com/pixelmc/CustomSprays/releases\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://gitee.com/pixelmc/CustomSprays/releases\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"*click*\"}}]");
            // Use less.
        }
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getAction().name().contains("RIGHT") && e.hasItem() && e.getMaterial().name().equalsIgnoreCase(CustomSprays.instant.getConfig().getString("spray_item"))) {
            CustomSprays.spray(e.getPlayer(), e.getPlayer().isSneaking());
        }
    }

}

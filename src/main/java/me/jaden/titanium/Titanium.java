package me.jaden.titanium;

import co.aikar.commands.PaperCommandManager;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import lombok.Setter;
import me.jaden.titanium.check.CheckManager;
import me.jaden.titanium.command.TitaniumCommand;
import me.jaden.titanium.data.DataManager;
import me.jaden.titanium.settings.TitaniumConfig;
import me.jaden.titanium.util.Ticker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
@Setter
public final class Titanium extends JavaPlugin {
    @Getter
    private static Titanium plugin;

    private final LegacyComponentSerializer componentSerializer = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.AMPERSAND_CHAR)
            .hexCharacter(LegacyComponentSerializer.HEX_CHAR).build();

    private TitaniumConfig titaniumConfig;

    private Ticker ticker;
    private BukkitRunnable task;

    private boolean done = false;
    private boolean allPluginsLoaded = true;

    private DataManager dataManager;
    private CheckManager checkManager;
    private PaperCommandManager commandManager;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().checkForUpdates(false).bStats(true);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        plugin = this;

        this.titaniumConfig = new TitaniumConfig(this);

        this.ticker = new Ticker();

        this.dataManager = new DataManager();
        this.checkManager = new CheckManager();

        this.commandManager = new PaperCommandManager(this);
        this.commandManager.registerCommand(new TitaniumCommand());

        checkServerStatus();

        //bStats
        new Metrics(this, 15258);

        PacketEvents.getAPI().init();
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        this.ticker.getTask().cancel();
    }

    private void checkServerStatus() {

        if (task != null) {
            return;
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {

                if (done) {
                    task.cancel();
                    return;
                }

                for (Plugin plugins : getServer().getPluginManager().getPlugins()) {
                    if (!plugins.isEnabled()) {
                        allPluginsLoaded = false;
                        break;
                    }
                }

                if (allPluginsLoaded) {
                    done = true;
                    task.cancel();
                }
            }
        };

        task.runTaskTimer(this, 20L, 40L);
    }
}

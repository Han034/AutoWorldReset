package com.forcestudio.autoWorldReset;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WorldResetManager {

    private final AutoWorldReset plugin;
    private FileConfiguration dataConfig;
    private File dataFile;

    // Aktif görevleri ve BossBar'ları takip etmek için
    private final Map<String, BossBar> activeBossBars = new HashMap<>();
    private final Map<String, BukkitTask> activeTasks = new HashMap<>();

    // Üzerinde doğulması güvenli olmayan zemin blokları
    private static final EnumSet<Material> UNSAFE_GROUND_MATERIALS = EnumSet.of(
            Material.LAVA, Material.WATER, Material.CACTUS, Material.MAGMA_BLOCK, Material.FIRE
    );

    public WorldResetManager(AutoWorldReset plugin) {
        this.plugin = plugin;
        setupDataFile();
    }

    //================================================================================
    // VERİ YÖNETİMİ
    //================================================================================

    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("data.yml dosyası oluşturulamadı!");
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("data.yml dosyasına kaydedilemedi!");
            e.printStackTrace();
        }
    }

    private long getLastResetTime(String worldName) {
        return dataConfig.getLong("last-reset." + worldName, 0L);
    }

    private void setLastResetTime(String worldName) {
        dataConfig.set("last-reset." + worldName, System.currentTimeMillis());
        saveData();
    }

    //================================================================================
    // OTOMATİK SIFIRLAMA KONTROLÜ
    //================================================================================

    public void checkAndResetWorlds() {
        ConfigurationSection worldsSection = plugin.getConfig().getConfigurationSection("worlds-to-reset");
        if (worldsSection == null) return;

        for (String worldName : worldsSection.getKeys(false)) {
            long intervalHours = worldsSection.getLong(worldName + ".reset-interval-hours");
            if (intervalHours <= 0) continue;

            long intervalMillis = TimeUnit.HOURS.toMillis(intervalHours);
            long lastReset = getLastResetTime(worldName);

            // Eğer hiç sıfırlanmadıysa (yeni eklendiyse), ilk sıfırlamayı hemen yap.
            if (lastReset == 0) {
                plugin.getLogger().info(worldName + " dünyası ilk defa sıfırlanacak. İşlem başlatılıyor...");
                resetWorld(worldName);
                continue;
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastReset > intervalMillis) {
                plugin.getLogger().info(worldName + " dünyasının sıfırlanma zamanı geldi. İşlem başlatılıyor...");
                resetWorld(worldName);
            }
        }
    }

    //================================================================================
    // ANA SIFIRLAMA MANTIĞI VE GERİ SAYIM
    //================================================================================

    public void resetWorld(String worldName) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (activeTasks.containsKey(worldName)) {
                plugin.getLogger().warning(worldName + " için zaten bir sıfırlama işlemi devam ediyor.");
                return;
            }

            World oldWorld = Bukkit.getWorld(worldName);
            if (oldWorld == null) {
                plugin.getLogger().warning(worldName + " adında bir dünya bulunamadı veya zaten yüklü değil!");
                return;
            }

            Bukkit.broadcastMessage(getMessage("reset-starting").replace("{world}", worldName));
            int countdownSeconds = plugin.getConfig().getInt("bossbar-countdown-seconds", 60);

            BossBar bossBar = Bukkit.createBossBar("Başlatılıyor...", BarColor.RED, BarStyle.SOLID);
            bossBar.setVisible(true);
            activeBossBars.put(worldName, bossBar);

            for (Player player : oldWorld.getPlayers()) {
                bossBar.addPlayer(player);
            }

            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
                private int remaining = countdownSeconds;

                @Override
                public void run() {
                    if (remaining <= 0) {
                        performActualReset(worldName);
                        // Bu görev artık işini bitirdi, kendisini listeden ve Bukkit'ten siler.
                        cleanupReset(worldName);
                        return;
                    }

                    String title = ChatColor.RED + "" + ChatColor.BOLD + worldName + " dünyası " + remaining + " saniye içinde sıfırlanacak!";
                    bossBar.setTitle(title);
                    bossBar.setProgress((double) remaining / countdownSeconds);
                    remaining--;
                }
            }, 0L, 20L);

            activeTasks.put(worldName, task);
        });
    }

    private void performActualReset(String worldName) {
        World oldWorld = Bukkit.getWorld(worldName);
        if (oldWorld == null) {
            cleanupReset(worldName);
            return;
        }

        Bukkit.broadcastMessage(getMessage("reset-imminent").replace("{world}", worldName));
        String fallbackWorldName = plugin.getConfig().getString("fallback-world", "world");
        World fallbackWorld = Bukkit.getWorld(fallbackWorldName);
        if (fallbackWorld == null) fallbackWorld = Bukkit.getWorlds().get(0);

        for (Player player : oldWorld.getPlayers()) {
            player.teleport(fallbackWorld.getSpawnLocation());
        }

        long seed = oldWorld.getSeed();
        World.Environment environment = oldWorld.getEnvironment();
        File worldFolder = oldWorld.getWorldFolder();

        if (!Bukkit.unloadWorld(oldWorld, false)) {
            plugin.getLogger().severe(worldName + " dünyası unload edilemedi! Sıfırlama iptal edildi.");
            cleanupReset(worldName);
            return;
        }

        deleteAndRecreateWorld(worldName, worldFolder, seed, environment);
    }

    private void deleteAndRecreateWorld(String worldName, File worldFolder, long seed, World.Environment environment) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                deleteFolder(worldFolder.toPath());
                plugin.getLogger().info(worldName + " dünya klasörü başarıyla silindi.");

                Bukkit.getScheduler().runTask(plugin, () -> {
                    World newWorld = new WorldCreator(worldName).environment(environment).createWorld();
                    if (newWorld == null) {
                        plugin.getLogger().severe(worldName + " dünyası oluşturulamadı!");
                        cleanupReset(worldName);
                        return;
                    }

                    Location safeSpawn = findSafeSpawnLocation(newWorld);
                    if (safeSpawn == null) {
                        String emergencyPath = "worlds-to-reset." + worldName + ".emergency-spawn";
                        if (plugin.getConfig().isConfigurationSection(emergencyPath)) {
                            safeSpawn = Location.deserialize(plugin.getConfig().getConfigurationSection(emergencyPath).getValues(true));
                            safeSpawn.setWorld(newWorld);
                            plugin.getLogger().info(worldName + " için config'de belirtilen acil durum noktası kullanılıyor.");
                        } else {
                            plugin.getLogger().warning(worldName + " için güvenli bir yer bulunamadı ve acil durum noktası belirtilmemiş. Güvenli platform inşa ediliyor...");
                            safeSpawn = buildEmergencyPlatform(newWorld);
                        }
                    } else {
                        plugin.getLogger().info(worldName + " için doğal bir güvenli doğma noktası bulundu.");
                    }

                    newWorld.setSpawnLocation(safeSpawn);
                    plugin.getLogger().info(worldName + " için yeni doğma noktası ayarlandı: " + safeSpawn.toVector());

                    setLastResetTime(worldName);
                    Bukkit.broadcastMessage(getMessage("reset-success").replace("{world}", worldName));
                    // Buradaki cleanup artık countdown task'i tarafından yapıldığı için kaldırıldı.
                });
            } catch (IOException e) {
                plugin.getLogger().severe(worldName + " dünya klasörü silinirken bir hata oluştu!");
                e.printStackTrace();
                cleanupReset(worldName);
            }
        });
    }

    //================================================================================
    // GÜVENLİ NOKTA BULMA MANTIĞI
    //================================================================================

    @Nullable
    private Location findSafeSpawnLocation(World world) {
        Location spawn = world.getSpawnLocation();
        int startX = spawn.getBlockX();
        int startZ = spawn.getBlockZ();

        if (isLocationSafe(world.getHighestBlockAt(startX, startZ).getLocation())) {
            return world.getHighestBlockAt(startX, startZ).getLocation().add(0.5, 1.0, 0.5);
        }

        int searchRadius = 200;
        for (int radius = 1; radius < searchRadius; radius++) {
            for (int i = -radius; i <= radius; i += radius * 2) { // Sadece köşeleri ve kenarları tara
                for (int j = -radius; j <= radius; j += radius * 2) {
                    Location potentialLocation = world.getHighestBlockAt(startX + i, startZ + j).getLocation();
                    if (isLocationSafe(potentialLocation)) {
                        return potentialLocation.add(0.5, 1.0, 0.5);
                    }
                }
            }
        }
        return null;
    }

    private Location buildEmergencyPlatform(World world) {
        Location spawn = world.getSpawnLocation();
        int platformY = 64;
        Location platformCenter = new Location(world, spawn.getBlockX() + 0.5, platformY + 1.0, spawn.getBlockZ() + 0.5);

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                world.getBlockAt(spawn.getBlockX() + x, platformY, spawn.getBlockZ() + z).setType(Material.GRASS_BLOCK);
                world.getBlockAt(spawn.getBlockX() + x, platformY + 1, spawn.getBlockZ() + z).setType(Material.AIR);
                world.getBlockAt(spawn.getBlockX() + x, platformY + 2, spawn.getBlockZ() + z).setType(Material.AIR);
            }
        }
        world.getBlockAt(spawn.getBlockX(), platformY + 1, spawn.getBlockZ()).setType(Material.TORCH);
        return platformCenter;
    }

    private boolean isLocationSafe(Location location) {
        Block ground = location.getBlock();
        Block feet = ground.getRelative(0, 1, 0);
        Block head = ground.getRelative(0, 2, 0);

        if (UNSAFE_GROUND_MATERIALS.contains(ground.getType())) return false;
        if (!feet.isPassable() || !head.isPassable()) return false;
        if (!ground.getType().isSolid()) return false;
        return true;
    }

    //================================================================================
    // YARDIMCI METOTLAR
    //================================================================================

    private void deleteFolder(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    private void cleanupReset(String worldName) {
        if (activeTasks.containsKey(worldName)) {
            if (!activeTasks.get(worldName).isCancelled()) {
                activeTasks.get(worldName).cancel();
            }
            activeTasks.remove(worldName);
        }
        if (activeBossBars.containsKey(worldName)) {
            activeBossBars.get(worldName).removeAll();
            activeBossBars.remove(worldName);
        }
    }

    public BossBar getActiveBossBar(String worldName) {
        return activeBossBars.get(worldName);
    }

    public void cleanupAllBossBars() {
        activeBossBars.values().forEach(BossBar::removeAll);
        activeBossBars.clear();
        activeTasks.values().forEach(BukkitTask::cancel);
        activeTasks.clear();
    }

    private String getMessage(String path) {
        String message = plugin.getConfig().getString("messages." + path, "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void sendWorldInfo(CommandSender sender, String worldName) {
        if (!plugin.getConfig().isConfigurationSection("worlds-to-reset." + worldName)) {
            sender.sendMessage(ChatColor.RED + worldName + " dünyası sıfırlanmak için ayarlanmamış.");
            return;
        }

        long intervalHours = plugin.getConfig().getLong("worlds-to-reset." + worldName + ".reset-interval-hours");
        long intervalMillis = TimeUnit.HOURS.toMillis(intervalHours);
        long lastReset = getLastResetTime(worldName);
        long nextReset = lastReset + intervalMillis;
        long remaining = nextReset - System.currentTimeMillis();

        sender.sendMessage(ChatColor.GOLD + "--- " + worldName + " Sıfırlama Bilgisi ---");
        sender.sendMessage(ChatColor.YELLOW + "Sıfırlama Aralığı: " + ChatColor.WHITE + intervalHours + " saat");

        if (lastReset == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Son Sıfırlanma: " + ChatColor.WHITE + "Daha önce sıfırlanmadı (ilk sıfırlama bekleniyor).");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Son Sıfırlanma: " + ChatColor.WHITE + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(lastReset)));
            sender.sendMessage(ChatColor.YELLOW + "Tahmini Sonraki Sıfırlanma: " + ChatColor.WHITE + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(nextReset)));
            if (remaining > 0) {
                sender.sendMessage(ChatColor.YELLOW + "Kalan Süre: " + ChatColor.WHITE + String.format("%d gün, %d saat, %d dakika",
                        TimeUnit.MILLISECONDS.toDays(remaining),
                        TimeUnit.MILLISECONDS.toHours(remaining) % 24,
                        TimeUnit.MILLISECONDS.toMinutes(remaining) % 60
                ));
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Kalan Süre: " + ChatColor.RED + "Sıfırlama zamanı geçti, bir sonraki kontrolde sıfırlanacak.");
            }
        }
    }
    public String getRemainingTimeFormatted(String worldName) {
        if (!plugin.getConfig().isConfigurationSection("worlds-to-reset." + worldName)) return "Ayarlı Değil";
        long intervalMillis = TimeUnit.HOURS.toMillis(plugin.getConfig().getLong("worlds-to-reset." + worldName + ".reset-interval-hours"));
        long lastReset = getLastResetTime(worldName);
        if(lastReset == 0) return "İlk sıfırlama bekleniyor";
        long remaining = (lastReset + intervalMillis) - System.currentTimeMillis();
        if (remaining <= 0) return "Şimdi sıfırlanabilir";

        return String.format("%d gün, %d sa, %d dk",
                TimeUnit.MILLISECONDS.toDays(remaining),
                TimeUnit.MILLISECONDS.toHours(remaining) % 24,
                TimeUnit.MILLISECONDS.toMinutes(remaining) % 60);
    }

    public String getLastResetFormatted(String worldName) {
        long lastReset = getLastResetTime(worldName);
        if (lastReset == 0) return "Hiçbir zaman";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(lastReset));
    }

    public String getNextResetFormatted(String worldName) {
        if (!plugin.getConfig().isConfigurationSection("worlds-to-reset." + worldName)) return "Ayarlı Değil";
        long intervalMillis = TimeUnit.HOURS.toMillis(plugin.getConfig().getLong("worlds-to-reset." + worldName + ".reset-interval-hours"));
        long lastReset = getLastResetTime(worldName);
        if (lastReset == 0) return "Yakında";
        long nextReset = lastReset + intervalMillis;
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(nextReset));
    }
}
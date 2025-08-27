package com.forcestudio.autoWorldReset;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * AutoWorldReset eklentisinin ana sınıfı.
 * Bu sınıf, eklenti başladığında ve kapandığında gerekli tüm işlemleri yönetir.
 * Yöneticileri (Manager), Komutları (Command) ve Olay Dinleyicilerini (Listener) başlatır.
 */
public class AutoWorldReset extends JavaPlugin {

    // Diğer sınıfların erişebilmesi için yönetici sınıflarını burada tutuyoruz.
    private WorldResetManager worldResetManager;
    private MenuManager menuManager;

    @Override
    public void onEnable() {
        // --- 1. YAPILANDIRMA DOSYASINI KUR ---
        // Eğer plugins/AutoWorldReset/config.yml yoksa, kaynaklardaki varsayılanı kopyalar.
        saveDefaultConfig();

        // --- 2. YÖNETİCİ SINIFLARINI BAŞLAT ---
        // Tüm dünya sıfırlama mantığını yönetecek olan sınıf.
        this.worldResetManager = new WorldResetManager(this);
        // Tüm GUI (menü) işlemlerini yönetecek olan sınıf.
        this.menuManager = new MenuManager(worldResetManager, this); // 'this' eklediğinizden emin olun.

        // --- 3. KOMUTLARI KAYDET ---
        // Eklentimizin komutlarını ilgili yönetici sınıflarına bağlıyoruz.
        getCommand("worldreset").setExecutor(new ResetCommand(this, worldResetManager));
        getCommand("awradmin").setExecutor(new AdminMenuCommand(menuManager));

        // --- 4. OLAY DİNLEYİCİLERİNİ (LISTENER) KAYDET ---
        // Oyuncuların dünya değiştirme olaylarını dinleyerek BossBar'ı doğru yönetmek için.
        getServer().getPluginManager().registerEvents(new WorldChangeListener(this, worldResetManager), this); // WorldChangeListener'a plugin referansını da veriyoruz.
        // Kendi kodladığımız menüdeki tıklama olaylarını dinlemek için.
        getServer().getPluginManager().registerEvents(new MenuClickListener(menuManager, worldResetManager, this), this);
        getServer().getPluginManager().registerEvents(new DamageListener(worldResetManager), this); // YENİ: Hasar dinleyicisini kaydet

        // --- 5. OTOMATİK SIFIRLAMA ZAMANLAYICISINI BAŞLAT ---
        startResetCheckScheduler();

        // Her şeyin yolunda gittiğini konsola bildir.
        getLogger().info("AutoWorldReset eklentisi (Bağımsız Menü) başarıyla aktif edildi!");
    }

    @Override
    public void onDisable() {
        // Eklenti kapanırken sunucuda çöp bırakmamak için temizlik yapıyoruz.

        // Aktif olan tüm BossBar'ları kaldır.
        if (worldResetManager != null) {
            worldResetManager.cleanupAllBossBars();
        }

        // Bu eklentiye ait tüm zamanlanmış görevleri iptal et. Bu, hataları ve hafıza sızıntılarını önler.
        Bukkit.getScheduler().cancelTasks(this);

        getLogger().info("AutoWorldReset eklentisi devre dışı bırakıldı.");
    }

    /**
     * Dünyaların sıfırlanma zamanını periyodik olarak kontrol eden ana zamanlayıcıyı başlatır.
     */
    private void startResetCheckScheduler() {
        // Bu görev, sunucuyu yormamak için ana thread dışında (asenkron) çalışır.
        // Her 60 saniyede bir (1200 tick) çalışarak dünyaları kontrol eder.
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            // Asıl kontrol işlemini WorldResetManager sınıfı yapar.
            worldResetManager.checkAndResetWorlds();
        }, 0L, 1200L); // 20 tick * 60 saniye = 1200 tick
    }
}
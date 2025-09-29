# AutoWorldReset

<div align="center">
    <img src="https://img.shields.io/badge/Minecraft-1.21-green.svg" alt="Minecraft Version">
    <img src="https://img.shields.io/badge/Java-21-orange.svg" alt="Java Version">
    <img src="https://img.shields.io/badge/Paper-1.21--R0.1--SNAPSHOT-blue.svg" alt="Paper Version">
    <img src="https://img.shields.io/badge/Version-1.0--SNAPSHOT-red.svg" alt="Plugin Version">
    <img src="https://img.shields.io/badge/License-MIT-lightgrey.svg" alt="License">
</div>

## 📖 Proje Hakkında

**AutoWorldReset**, Minecraft sunucuları için geliştirilmiş güçlü bir dünya sıfırlama eklentisidir. Bu eklenti sayesinde belirli dünyaları otomatik olarak programlanmış zaman aralıklarında sıfırlayabilir, oyuncularınıza sürekli taze ve temiz oyun deneyimi sunabilirsiniz.

### 🎯 Temel Özellikler

- ⏰ **Otomatik Dünya Sıfırlama**: Belirlediğiniz zaman aralıklarında dünyaları otomatik olarak sıfırlar
- 🎮 **Grafik Yönetim Paneli**: Oyun içi GUI menü ile kolay yönetim
- 📢 **Önceden Duyuru Sistemi**: Sıfırlamadan önce oyuncuları uyarır (10dk, 5dk, 1dk, 30s, 10s)
- 🛡️ **Spawn Koruma**: Yeni sıfırlanan dünyalarda oyuncular için hasar koruması
- 📊 **BossBar Geri Sayım**: Görsel geri sayım ile oyuncuları bilgilendirir
- 🏠 **Güvenli Teleportasyon**: Oyuncuları güvenli lokasyonlara ışınlar
- 🔧 **PlaceholderAPI Desteği**: Diğer eklentilerle entegrasyon
- 🌍 **Çoklu Dünya Desteği**: Birden fazla dünyayı aynı anda yönetir

## 🚀 Kurulum

### Gereksinimler

- **Minecraft Sunucu**: Paper 1.21 veya üzeri
- **Java**: 21 veya üzeri
- **Bağımlılıklar**: PlaceholderAPI (Opsiyonel)

### Adımlar

1. En son sürümü [Releases](https://github.com/Han034/AutoWorldReset/releases) sayfasından indirin
2. `AutoWorldReset.jar` dosyasını sunucunuzun `plugins` klasörüne kopyalayın
3. Sunucuyu yeniden başlatın
4. `plugins/AutoWorldReset/config.yml` dosyasını ihtiyaçlarınıza göre düzenleyin
5. `/worldreset reload` komutu ile yapılandırmayı yeniden yükleyin

## ⚙️ Yapılandırma

### config.yml Örneği

```yaml
# Sıfırlanacak dünyaların listesi
worlds-to-reset:
  kaynak_dunyasi:
    reset-interval-hours: 24
    emergency-spawn:
      x: 150
      y: 75
      z: -200

# Sıfırlama öncesi duyurular
announcements:
  - '10m'
  - '5m'
  - '1m'
  - '30s'
  - '10s'

# Ana lobi dünyası
fallback-world: 'world'

# BossBar geri sayım süresi
bossbar-countdown-seconds: 60

# Spawn koruma süresi (saniye)
spawn-protection-seconds: 180

# Mesajlar
messages:
  reset-starting: "&e&l[UYARI] &6{world} &edünyası için sıfırlama işlemi başlatılıyor!"
  reset-imminent: "&c&l[DİKKAT] &6{world} &cdünyası şimdi sıfırlanıyor! Lobiye gönderiliyorsunuz..."
  reset-success: "&a&l[BİLGİ] &6{world} &adünyası başarıyla sıfırlandı ve erişime açıldı!"
```

### Ayar Parametreleri

| Parametre | Açıklama | Varsayılan |
|-----------|----------|------------|
| `reset-interval-hours` | Sıfırlama aralığı (saat) | 24 |
| `emergency-spawn` | Acil durum spawn koordinatları | - |
| `fallback-world` | Ana lobi dünyası | world |
| `bossbar-countdown-seconds` | BossBar geri sayım süresi | 60 |
| `spawn-protection-seconds` | Spawn koruma süresi | 180 |

## 🎮 Komutlar

### Ana Komutlar

| Komut | Açıklama | İzin |
|-------|----------|------|
| `/worldreset reload` | Yapılandırmayı yeniden yükler | `worldresetter.admin` |
| `/worldreset reset <dünya>` | Belirtilen dünyayı hemen sıfırlar | `worldresetter.admin` |
| `/worldreset info <dünya>` | Dünya hakkında bilgi gösterir | `worldresetter.admin` |
| `/awradmin` | Yönetici panelini açar | `worldresetter.admin` |

### Komut Örnekleri

```bash
# Yapılandırmayı yeniden yükle
/worldreset reload

# Kaynak dünyasını hemen sıfırla
/worldreset reset kaynak_dunyasi

# Dünya bilgilerini görüntüle
/worldreset info kaynak_dunyasi

# Yönetici panelini aç
/awradmin
```

## 🔒 İzinler

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `worldresetter.admin` | Tüm komutlara ve yönetici paneline erişim | op |

## 🖼️ Ekran Görüntüleri

### Yönetici Paneli
- Grafik arayüz ile kolay dünya yönetimi
- Her dünya için detaylı bilgiler
- Tek tıkla sıfırlama özelliği

### BossBar Geri Sayım
- Görsel geri sayım göstergesi
- Oyuncular için net bilgilendirme
- Otomatik güncellenen süre gösterimi

## 🛠️ Geliştirme

### Proje Yapısı

```
src/
├── main/
│   ├── java/com/forcestudio/autoWorldReset/
│   │   ├── AutoWorldReset.java          # Ana eklenti sınıfı
│   │   ├── WorldResetManager.java       # Dünya sıfırlama yöneticisi
│   │   ├── MenuManager.java             # GUI menü yöneticisi
│   │   ├── AdminMenuCommand.java        # Admin menü komutu
│   │   ├── ResetCommand.java            # Sıfırlama komutu
│   │   ├── WorldChangeListener.java     # Dünya değişikliği dinleyicisi
│   │   ├── MenuClickListener.java       # Menü tıklama dinleyicisi
│   │   ├── DamageListener.java          # Hasar dinleyicisi
│   │   └── AutoWorldResetExpansion.java # PlaceholderAPI genişletmesi
│   └── resources/
│       ├── plugin.yml                   # Eklenti bilgileri
│       └── config.yml                   # Varsayılan yapılandırma
```

### Derleme

```bash
# Maven ile derleme
mvn clean package

# Shade plugin ile fat JAR oluşturma
mvn clean compile package
```

### Bağımlılıklar

- **Paper API**: 1.21-R0.1-SNAPSHOT
- **PlaceholderAPI**: 2.11.6 (Opsiyonel)

## 📊 PlaceholderAPI Desteği

Eklenti, PlaceholderAPI ile entegrasyon sağlar:

```
%autoworldreset_next_reset_<dünya>%    # Sonraki sıfırlama zamanı
%autoworldreset_last_reset_<dünya>%    # Son sıfırlama zamanı
%autoworldreset_time_remaining_<dünya>% # Kalan süre
```

## 🤝 Katkıda Bulunma

1. Projeyi fork edin
2. Feature branch oluşturun (`git checkout -b feature/yeni-ozellik`)
3. Değişikliklerinizi commit edin (`git commit -am 'Yeni özellik eklendi'`)
4. Branch'inizi push edin (`git push origin feature/yeni-ozellik`)
5. Pull Request oluşturun

### Geliştirme Kuralları

- Java 21 coding standards'larına uyun
- Tüm public metodlar için Javadoc yazın
- Unit testler ekleyin
- Türkçe yorum satırları kullanın

## 🐛 Sorun Bildirimi

Hata bulduğunuzda veya yeni özellik önerisi yapmak istediğinizde [Issues](https://github.com/Han034/AutoWorldReset/issues) sayfasını kullanın.

### Hata Raporu Şablonu

```
**Hata Açıklaması**
Kısa ve net hata açıklaması

**Nasıl Tekrarlanır**
1. Adım 1
2. Adım 2
3. Hata oluşur

**Beklenen Davranış**
Ne olması gerektiği

**Ekran Görüntüleri**
Varsa ekran görüntüleri

**Ortam**
- Minecraft Sürümü:
- Paper Sürümü:
- Plugin Sürümü:
```

## 📝 Değişiklik Günlüğü

### v1.0-SNAPSHOT
- ✨ İlk sürüm yayınlandı
- ⚡ Otomatik dünya sıfırlama özelliği
- 🎮 Grafik yönetici paneli
- 📢 Duyuru sistemi
- 🛡️ Spawn koruma sistemi
- 📊 BossBar geri sayım
- 🔧 PlaceholderAPI desteği

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakın.

## 👨‍💻 Geliştirici

**ForceStudio** - [GitHub](https://github.com/Han034)

---

<div align="center">
    <p>⭐ Bu projeyi beğendiyseniz yıldız vermeyi unutmayın!</p>
    <p>💬 Sorularınız için Issue açabilir veya Discord'dan ulaşabilirsiniz.</p>
</div>
# Kafetani — Android (Native)

Aplikasi Android native (Kotlin + Jetpack Compose) untuk Kafetani, dibuat dari nol
dan terhubung ke backend Laravel yang sama (lewat REST API baru di `routes/api.php`).

Cakupan fitur — **lengkap**, setara versi web:

- **Pelanggan**: daftar/login, lihat menu kafe (dengan filter kategori), lihat
  marketplace hasil tani + profil petani, detail produk, keranjang belanja,
  checkout, riwayat & detail pesanan, profil + logout.
- **Admin**: dashboard statistik, CRUD produk (dengan upload gambar), CRUD
  petani (dengan upload foto), kelola status pesanan.
- **Kasir**: POS sederhana — pilih menu, atur keranjang, catat pesanan
  dine-in/pickup.

## ⚠️ Batasan penting yang perlu kamu tahu

Kode ini ditulis **tanpa bisa di-compile/dijalankan langsung oleh saya** — sandbox
yang saya pakai tidak punya akses ke Android SDK / Google Maven (dibatasi
jaringan). Saya sudah:
- Menulis semua kode mengikuti best-practice Kotlin/Compose yang sudah lama stabil,
- Cross-check manual setiap pemanggilan fungsi antar file (nama & parameter
  cocok satu sama lain),
- Melakukan pengecekan sintaks otomatis (kurung kurawal seimbang, tidak ada
  karakter aneh/typo import).

Tapi karena belum pernah benar-benar di-build, **kemungkinan ada 1-2 typo kecil
yang baru ketahuan saat kamu buka di Android Studio** (biasanya cuma error
"unresolved reference" yang gampang diperbaiki). Kalau nemu error saat build,
paste pesan errornya ke saya — akan langsung saya perbaiki.

## Cara membuka & menjalankan

1. **Install Android Studio** (versi terbaru, sudah termasuk Android SDK & Gradle).
2. Buka folder `KafetaniAndroid/` ini lewat `File > Open` di Android Studio.
3. Tunggu proses **Gradle Sync** selesai (Android Studio otomatis download
   Gradle wrapper + semua dependency dari Google Maven/Maven Central — proses
   ini butuh koneksi internet normal, beda dengan sandbox saya tadi).
4. Jalankan backend Laravel di komputer yang sama (lihat bagian bawah).
5. Klik **Run ▶** — pilih emulator Android atau HP fisik yang tersambung USB.

## Menyambungkan ke backend Laravel

Aplikasi ini butuh backend Laravel (`kafetani-main`) yang sudah dilengkapi
endpoint API baru (lihat `routes/api.php` di project Laravel-nya). Jalankan dulu:

```bash
cd kafetani-main
composer install
php artisan migrate --seed
php artisan serve --host=0.0.0.0 --port=8000
```

`--host=0.0.0.0` penting supaya server bisa diakses dari emulator/HP, bukan cuma dari
laptop itu sendiri.

URL backend diatur di `app/build.gradle.kts` lewat `BASE_URL`:

| Skenario testing                              | Nilai `BASE_URL`                     |
|------------------------------------------------|---------------------------------------|
| Emulator Android Studio (default, sudah diset) | `http://10.0.2.2:8000/`               |
| HP Android fisik (satu Wi-Fi dgn laptop)        | `http://<IP-LOKAL-LAPTOP>:8000/`      |
| Nanti kalau backend sudah online                | `https://domainkamu.com/`             |

Cara cek IP lokal laptop: `ipconfig` (Windows, cari "IPv4 Address") atau
`ifconfig`/`ip addr` (Mac/Linux, cari yang di jaringan Wi-Fi kamu).

Setelah ganti `BASE_URL`, klik **Sync Now** lagi di Android Studio.

## Akun untuk login pertama kali

Pakai akun hasil `php artisan migrate --seed` dari project Laravel-nya (lihat
README Laravel untuk daftar akun admin/kasir default), atau daftar akun baru
langsung dari layar Register di app (otomatis jadi role "user"/pelanggan).

## Struktur project

```
app/src/main/java/com/kafetani/app/
├── data/               # Model, Retrofit API, Repository, penyimpanan token
│   ├── model/          # Data class (Product, Order, User, dll — sesuai JSON API)
│   ├── network/        # ApiService (Retrofit), auth interceptor, error handling
│   └── repository/     # 1 repository per area: Auth, Catalog, Order, Admin, Kasir
└── ui/
    ├── auth/           # Login, Register
    ├── customer/       # Menu, Marketplace, Cart, Riwayat Pesanan, Profil
    ├── admin/          # Dashboard, CRUD Produk, CRUD Petani, Kelola Pesanan
    ├── kasir/          # Layar POS
    ├── navigation/      # Semua route (Screen.kt) + NavHost
    ├── theme/          # Warna & tipografi (tema kopi + hijau tani)
    └── components/     # Komponen UI yang dipakai berulang
```

Arsitektur: MVVM sederhana — tiap layar punya ViewModel (StateFlow), akses data
lewat Repository, DI manual lewat `AppContainer` (tanpa Hilt, biar gampang dibaca).

## Mengganti nama paket / ikon aplikasi

- Nama paket default: `com.kafetani.app` (di `app/build.gradle.kts` bagian
  `namespace` & `applicationId`, dan folder `java/com/kafetani/app`). Kalau mau
  ganti, gunakan fitur refactor Android Studio (klik kanan folder package →
  Refactor → Rename), jangan diganti manual satu-satu.
- Ikon aplikasi saat ini dibuat sederhana dari vector (cangkir kopi coklat di
  atas warna espresso). Untuk ikon yang lebih rapi, klik kanan `res` → New →
  Image Asset di Android Studio.

## Submission Note: Story App Akhir - Dicoding

Letakkan pada local.properties:
```agsl
MAPS_API_KEY=AIzaSyA2PsNYG5Mjrut5OBK5Rx5E_6IYx3KjDLs
```
Fitur yang diimplementasikan untuk memenuhi kriteria Bintang 5:

1.  Paging 3 dengan RemoteMediator dan Room:
    - Menggunakan `StoryRemoteMediator` sebagai offline caching layer.
    - Mendukung scroll daftar cerita secara efisien dengan loading otomatis data berikutnya.
    - Sinkronisasi data antara server dan database lokal (Room).

2.  Google Maps Integration:
    - `MapsActivity` menampilkan sebaran lokasi cerita menggunakan marker.
    - Implementasi `LatLngBounds.Builder` untuk memastikan seluruh marker terlihat dalam layar (fit bounds).
    - Menerapkan Custom Map Style JSON (`R.raw.map_style`) untuk tampilan peta yang lebih profesional.

3.  Stack Widget:
    - Menyediakan Home Screen Widget tipe **Stack Widget** yang menampilkan daftar foto cerita terbaru secara interaktif.
    - Sinkronisasi data widget menggunakan `RemoteViewsService`.

4.  Geo-Location pada Add Story:
    - Fitur opsional "Sertakan Lokasi" pada `AddStoryActivity`.
    - Menggunakan `FusedLocationProviderClient` untuk akurasi koordinat (lat/lon) saat mengunggah cerita.

5.  Modern UI/UX:
    - **Edge-to-Edge Support**: Tampilan aplikasi mengisi seluruh layar hingga ke bawah system bars.
    - **Animations**:
        - Property Animation (Fade & Slide) pada halaman Login dan Register.
        - Shared Element Transition pada foto cerita dari list ke detail.
    - **UI States**: ProgressBar saat memuat data, penanganan error body parsing (HTTP status != 200), dan Empty State UI.
    - **Pull-to-Refresh**: Menggunakan `SwipeRefreshLayout` pada daftar cerita.

6.  Arsitektur & Clean Code:
    - MVVM (Model-View-ViewModel) + Repository Pattern.
    - Dependency Injection manual melalui class `Injection`.
    - Custom View untuk validasi input email dan password secara real-time.
    - Zero dead code, unused imports, dan mengikuti standar indentasi Android.

7.  Testing & Stability:
    - **Unit Testing**: `MainViewModelTest` menguji Paging 3 dengan data nyata (`PagingData`) dan mock repository.
    - **UI Testing**: `AuthenticationTest` menggunakan Espresso dan `IdlingResource` untuk menguji alur Login-Logout secara otomatis.
    - **Build Compatibility**: Konfigurasi `net.bytebuddy.experimental` untuk Mockito pada Java environment terbaru (25).

8.  Localization:
    - Mendukung penuh Bahasa Indonesia (`values-id`) dan Bahasa Inggris (`values`).

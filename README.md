# YummyAnime Aniyomi Extension

Russian anime extension for Aniyomi/Anikku (Tachiyomi fork) providing content from YummyAnime (site.yummyani.me).

## Features

- ✅ Browse popular anime
- ✅ Browse latest updates
- ✅ Search anime with filters
- ✅ View anime details
- ✅ Play episodes with multiple video players
- ✅ Support for Kodik, Sibnet, Alloha, and YouTube players
- ✅ Quality selection preferences
- ✅ Player preferences
- ✅ Russian language support

## Build Instructions

### Prerequisites

- Java 17 or higher
- Android SDK (API 34)
- Gradle 8.9

### Building from Monorepo

The preferred way to build is within the yuzono/anime-extensions monorepo:

```bash
cd yuzono/anime-extensions
./gradlew :src:ru:yummyanime:assembleRelease
```

Output: `src/ru/yummyanime/build/outputs/apk/release/aniyomi-ru.yummyanime-v14.1.apk`

### Standalone Build (for development)

For standalone development, the code structure is ready but requires the Tachiyomi/Aniyomi core library.

## Installation

### In Aniyomi/Anikku

1. Download the APK from releases
2. Open Aniyomi/Anikku
3. Go to Settings > Extensions
4. Click "Install from storage"
5. Select the APK

### Via Repository

Add this repository URL in Aniyomi/Anikku:

```
https://raw.githubusercontent.com/DirtyZXR/yummyanime-extensions-repo/main/repo/index.min.json
```

## Configuration

### Preferences

The extension supports the following preferences:

- **Preferred Quality**: Choose video quality (Any, 1080p, 720p, 480p, 360p)
- **Default Player**: Select preferred video player (Any, Kodik, Sibnet, Alloha, YouTube)

### Filters

- **Sort**: By popularity, rating, date, name, or views
- **Genre**: Action, Adventure, Comedy, Drama, Fantasy, Romance, Horror, Seinen, Shounen, Shoujo
- **Status**: All, Ongoing, Completed, Anons
- **Year**: Custom year filter

## Supported Video Players

- **Kodik**: High-quality video streaming
- **Sibnet**: Reliable Russian streaming
- **Alloha**: Multi-quality support
- **YouTube**: Direct YouTube integration
- **Direct**: Fallback for other formats

## API Information

- **Site**: https://site.yummyani.me
- **API Base**: https://site.yummyani.me/api/v1
- **Language**: Russian

## Project Structure

```
src/main/eu/kanade/tachiyomi/animeextension/ru/yummyanime/
├── YummyAnime.kt           # Main extension class
├── YummyAnimeDto.kt        # Data transfer objects
└── YummyAnimeFilters.kt    # Filter definitions
```

## Contributing

### Adding to Official Repository

See [OFFICIAL_INTEGRATION.md](docs/OFFICIAL_INTEGRATION.md) for detailed instructions on integrating this extension into the official yuzono/anime-extensions repository.

### Development

1. Follow Tachiyomi/Aniyomi extension development guidelines
2. Use `AnimeHttpSource` instead of `ParsedAnimeHttpSource`
3. Ensure all video URLs are absolute
4. Use UNIX Epoch time for episode timestamps
5. Return episodes in descending order

## Version Information

- **Extension Version**: 1
- **Library Version**: 13
- **Full Version**: 14.1
- **Package**: eu.kanade.tachiyomi.extension.ru.yummyanime

## Troubleshooting

### Extension Not Working

1. Check Aniyomi/Anikku version compatibility
2. Verify network connectivity
3. Try installing from repository instead of APK
4. Check if YummyAnime site is accessible

### Video Playback Issues

1. Try different video players in preferences
2. Check if video player service is blocking requests
3. Test with different quality settings
4. Verify source site is not down

### Build Issues

1. Ensure Java 17 is installed
2. Check Android SDK path
3. Verify Gradle version
4. Try building within monorepo structure

## License

This extension follows the GNU General Public License v3.0, same as the yuzono/anime-extensions project.

## Credits

- **Extension Author**: Denis Zelinsky (@DirtyZXR)
- **Content Source**: YummyAnime (site.yummyani.me)
- **Base Framework**: Tachiyomi/Aniyomi

## Links

- **Aniyomi**: https://github.com/jmir1/aniyomi
- **Anikku**: https://github.com/yuzono/anikku
- **Official Extensions**: https://github.com/yuzono/anime-extensions
- **YummyAnime**: https://site.yummyani.me

## Disclaimer

This extension is for educational purposes only. Please support the original content creators by using official streaming services when available.

# YummyAnime Extension - Official Integration Guide

## Overview

This guide explains how to integrate the YummyAnime extension into the official yuzono/anime-extensions repository.

## Current Status

- ✅ Code structure follows official guidelines
- ✅ Separate DTO files (YummyAnimeDto.kt)
- ✅ Separate filters file (YummyAnimeFilters.kt)
- ✅ Clean main source file (YummyAnime.kt)
- ⚠️ Standalone build requires Tachiyomi core library
- ✅ Ready for integration into yuzono/anime-extensions monorepo

## Integration Steps

### 1. Directory Structure

Create the following directory structure in `yuzono/anime-extensions/`:

```
src/ru/yummyanime/
├── build.gradle
├── src/
│   └── eu/
│       └── kanade/
│           └── tachiyomi/
│               └── animeextension/
│                   └── ru/
│                       └── yummyanime/
│                           ├── YummyAnime.kt
│                           ├── YummyAnimeDto.kt
│                           └── YummyAnimeFilters.kt
└── res/
    ├── mipmap-mdpi/
    │   └── ic_launcher.png
    ├── mipmap-hdpi/
    │   └── ic_launcher.png
    ├── mipmap-xhdpi/
    │   └── ic_launcher.png
    ├── mipmap-xxhdpi/
    │   └── ic_launcher.png
    └── mipmap-xxxhdpi/
        └── ic_launcher.png
```

### 2. Create build.gradle

Create `src/ru/yummyanime/build.gradle`:

```gradle
ext {
    extName = 'YummyAnime'
    extClass = '.YummyAnime'
    extVersionCode = 1
}

apply from: "$rootDir/common.gradle"
```

### 3. Prepare Source Files

Copy the following files from this project to `src/ru/yummyanime/src/eu/kanade/tachiyomi/animeextension/ru/yummyanime/`:

- `YummyAnime.kt` - Main extension class
- `YummyAnimeDto.kt` - Data transfer objects
- `YummyAnimeFilters.kt` - Filter definitions

**Note:** The source files are already structured correctly for the monorepo. No changes needed.

### 4. Icon Resources

The extension requires square icons with rounded corners in multiple densities:
- mipmap-mdpi: 48x48 px
- mipmap-hdpi: 72x72 px
- mipmap-xhdpi: 96x96 px
- mipmap-xxhdpi: 144x144 px
- mipmap-xxxhdpi: 192x192 px

**Important:** Remove any `web_hi_res_512.png` files from generated icons.

### 5. Update Main Repository Files

#### a. Add to `settings.gradle.kts`

Add the extension to the settings:

```kotlin
include(":src:ru:yummyanime")
```

#### b. Test Build

Build the extension from the monorepo root:

```bash
cd yuzono/anime-extensions
./gradlew :src:ru:yummyanime:assembleRelease
```

The output APK should be at:
`src/ru/yummyanime/build/outputs/apk/release/aniyomi-ru.yummyanime-v14.1.apk`

### 6. Verify Functionality

Test the extension in Aniyomi/Anikku:

1. Install the APK
2. Add the extension
3. Test browsing popular anime
4. Test browsing latest updates
5. Test search functionality
6. Test filters
7. Test viewing anime details
8. Test playing episodes with different players

## Code Guidelines Followed

### ✅ Correct Base Class

The extension uses `AnimeHttpSource` instead of the deprecated `ParsedAnimeHttpSource`.

### ✅ Proper Package Structure

Package: `eu.kanade.tachiyomi.animeextension.ru.yummyanime`

### ✅ Required Fields

All required SAnime fields are populated:
- `url` - Anime URL path
- `title` - Anime title
- `thumbnail_url` - Cover image URL

### ✅ Episode Timestamps

`SEpisode.date_upload` uses UNIX Epoch time in milliseconds.

### ✅ Absolute Video URLs

All video URLs are converted to absolute URLs using `toAbsoluteUrl()`.

### ✅ Episode List Order

Episodes are returned in descending order (newest first) using `.reversed()`.

### ✅ Version Convention

Version follows the convention: `14 + extVersionCode` (e.g., v14.1 when extVersionCode=1)

### ✅ Filter Types

Uses proper filter types:
- `AnimeFilter.Header` for informational text
- `AnimeFilter.Separator` for visual separation
- `AnimeFilter.Sort` for sorting options
- `AnimeFilter.Select` for dropdown selections
- `AnimeFilter.Text` for text input

### ✅ Dependencies

Uses only dependencies available in the monorepo:
- `kotlinx-serialization-json` for JSON parsing
- OkHttp for HTTP requests
- Jsoup for HTML parsing (if needed)

## API Integration

### Base URL
- Main: `https://site.yummyani.me`
- API: `https://site.yummyani.me/api/v1`

### Endpoints Used

- `/api/v1/anime/popular` - Popular anime list
- `/api/v1/anime/updates` - Latest updates
- `/api/v1/anime/search` - Search with filters
- `/api/v1/anime/{slug}` - Anime details
- `/api/v1/anime/{slug}/episodes` - Episode list
- `/api/v1/anime/{slug}/episodes/{id}/players` - Video players/links

### Video Players Supported

- **Kodik** - Extracts URL from JSON pattern
- **Sibnet** - Extracts from JavaScript
- **Alloha** - Extracts quality-specific URLs
- **YouTube** - Parses video ID
- **Direct** - Falls back to generic pattern matching

## Troubleshooting

### Build Errors

**Error:** "Unresolved reference 'animesource'"
**Solution:** Ensure you're building within the yuzono/anime-extensions monorepo, not standalone

**Error:** "common.gradle not found"
**Solution:** Run build from the monorepo root directory

### Runtime Errors

**Error:** Extension not visible in Aniyomi
**Solution:**
1. Check extension version code is unique
2. Verify APK contains all required metadata
3. Test with Aniyomi debug version for better error messages

**Error:** Video playback fails
**Solution:**
1. Check network connectivity
2. Verify video URLs are accessible
3. Test with different video players
4. Check if source site is blocking requests

## Testing Checklist

- [ ] Popular anime loads correctly
- [ ] Latest updates load correctly
- [ ] Search works with text query
- [ ] Search works with filters
- [ ] Anime details display correctly
- [ ] Episodes list loads in correct order
- [ ] Video playback works with default player
- [ ] Video playback works with different players
- [ ] Video quality sorting works correctly
- [ ] Preferences save and load correctly
- [ ] Extension shows in Aniyomi extension list
- [ ] Extension updates correctly

## Pull Request Process

1. Fork the yuzono/anime-extensions repository
2. Create a new branch: `git checkout -b add-yummyanime`
3. Copy the files as described above
4. Commit with descriptive message
5. Push to your fork
6. Create a pull request

### PR Description Template

```
## Summary
- Add YummyAnime extension for Russian anime content
- Supports multiple video players (Kodik, Sibnet, Alloha, YouTube)
- Implements search with filters (genre, status, year, sort)

## Test plan
- [ ] Browsed popular anime
- [ ] Searched for anime
- [ ] Viewed anime details
- [ ] Played episodes
- [ ] Tested different video players
```

## Contact

For questions or issues:
- GitHub Issues: https://github.com/yuzono/anime-extensions/issues
- Extension author: Denis Zelinsky (@DirtyZXR)

## License

This extension follows the same license as the yuzono/anime-extensions project (GNU General Public License v3.0).

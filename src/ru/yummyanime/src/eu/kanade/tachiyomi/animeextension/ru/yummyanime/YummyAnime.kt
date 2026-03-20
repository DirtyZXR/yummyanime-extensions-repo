package eu.kanade.tachiyomi.animeextension.ru.yummyanime

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.ListPreference
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.animesource.ConfigurableAnimeSource
import eu.kanade.tachiyomi.animesource.model.AnimeFilter
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.animesource.online.ParsedAnimeHttpSource
import eu.kanade.tachiyomi.network.GET
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import kotlinx.serialization.json.Json

class YummyAnime :
    ParsedAnimeHttpSource(),
    ConfigurableAnimeSource {

    override val name = "YummyAnime"

    override val baseUrl = "https://site.yummyani.me"

    override val lang = "ru"

    override val supportsLatest = true

    private val apiUrl = "$baseUrl/api/v1"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Content-Type", "application/json")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("Referer", baseUrl)
                    .header("Origin", baseUrl)
                    .method(original.method, original.body)
                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    companion object {
        private const val PREF_QUALITY_KEY = "preferred_quality"
        private const val PREF_PLAYER_KEY = "preferred_player"
    }

    // =============================== Preferences ===============================

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        ListPreference(screen.context).apply {
            key = PREF_QUALITY_KEY
            title = "Качество видео"
            entries = arrayOf("Любое", "1080p", "720p", "480p", "360p")
            entryValues = arrayOf("any", "1080", "720", "480", "360")
            setDefaultValue("any")
            summary = "%s"

            setOnPreferenceChangeListener { _, newValue ->
                preferences.edit().putString(key, newValue as String).commit()
            }
        }.also(screen::addPreference)

        ListPreference(screen.context).apply {
            key = PREF_PLAYER_KEY
            title = "Плеер по умолчанию"
            entries = arrayOf("Любой", "Kodik", "Sibnet", "Alloha", "YouTube")
            entryValues = arrayOf("any", "kodik", "sibnet", "alloha", "youtube")
            setDefaultValue("any")
            summary = "%s"

            setOnPreferenceChangeListener { _, newValue ->
                preferences.edit().putString(key, newValue as String).commit()
            }
        }.also(screen::addPreference)
    }

    override val preferences: SharedPreferences by lazy {
        Injekt.get<Application>().getSharedPreferences("source_$id", 0x0000)
    }

    // =============================== Popular ===============================

    override fun popularAnimeParse(response: Response): AnimesPage {
        val jsonResponse = response.body?.string() ?: return AnimesPage(emptyList(), false)
        val apiResponse = json.decodeFromString<AnimeListResponse>(jsonResponse)

        val hasNext = apiResponse.pagination?.hasNext ?: false
        val animes = apiResponse.data?.map { it.toSAnime() } ?: emptyList()

        return AnimesPage(animes, hasNext)
    }

    override fun popularAnimeRequest(page: Int): Request {
        val url = apiUrl.toHttpUrl().newBuilder()
            .addPathSegment("anime")
            .addPathSegment("popular")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", "20")
            .build()

        return GET(url)
    }

    override fun popularAnimeSelector(): String = throw UnsupportedOperationException()
    override fun popularAnimeFromElement(element: Element): SAnime = throw UnsupportedOperationException()
    override fun popularAnimeNextPageSelector(): String = throw UnsupportedOperationException()

    // =============================== Latest ===============================

    override fun latestUpdatesParse(response: Response): AnimesPage {
        val jsonResponse = response.body?.string() ?: return AnimesPage(emptyList(), false)
        val apiResponse = json.decodeFromString<AnimeListResponse>(jsonResponse)

        val hasNext = apiResponse.pagination?.hasNext ?: false
        val animes = apiResponse.data?.map { it.toSAnime() } ?: emptyList()

        return AnimesPage(animes, hasNext)
    }

    override fun latestUpdatesRequest(page: Int): Request {
        val url = apiUrl.toHttpUrl().newBuilder()
            .addPathSegment("anime")
            .addPathSegment("updates")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", "20")
            .build()

        return GET(url)
    }

    override fun latestUpdatesSelector(): String = throw UnsupportedOperationException()
    override fun latestUpdatesFromElement(element: Element): SAnime = throw UnsupportedOperationException()
    override fun latestUpdatesNextPageSelector(): String = throw UnsupportedOperationException()

    // =============================== Search ===============================

    override fun searchAnimeParse(response: Response): AnimesPage {
        val jsonResponse = response.body?.string() ?: return AnimesPage(emptyList(), false)
        val apiResponse = json.decodeFromString<AnimeListResponse>(jsonResponse)

        val hasNext = apiResponse.pagination?.hasNext ?: false
        val animes = apiResponse.data?.map { it.toSAnime() } ?: emptyList()

        return AnimesPage(animes, hasNext)
    }

    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request {
        val url = apiUrl.toHttpUrl().newBuilder()
            .addPathSegment("anime")
            .addPathSegment("search")
            .addQueryParameter("q", query)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", "20")

        filters.forEach { filter ->
            when (filter) {
                is YummyAnimeFilters.GenreFilter -> {
                    if (filter.state > 0) {
                        url.addQueryParameter("genre", filter.state.toString())
                    }
                }
                is YummyAnimeFilters.YearFilter -> {
                    if (filter.state.isNotEmpty()) {
                        url.addQueryParameter("year", filter.state)
                    }
                }
                is YummyAnimeFilters.StatusFilter -> {
                    if (filter.state > 0) {
                        val statusMap = mapOf(
                            1 to "ongoing",
                            2 to "completed",
                            3 to "anons",
                        )
                        url.addQueryParameter("status", statusMap[filter.state] ?: "")
                    }
                }
                is YummyAnimeFilters.SortFilter -> {
                    val sortMap = mapOf(
                        0 to "popular",
                        1 to "rating",
                        2 to "date",
                        3 to "title",
                        4 to "views",
                    )
                    url.addQueryParameter("sort", sortMap[filter.state?.index ?: 0] ?: "popular")
                }
                else -> {}
            }
        }

        return GET(url.build())
    }

    override fun searchAnimeSelector(): String = throw UnsupportedOperationException()
    override fun searchAnimeFromElement(element: Element): SAnime = throw UnsupportedOperationException()
    override fun searchAnimeNextPageSelector(): String = throw UnsupportedOperationException()

    override fun getFilterList() = YummyAnimeFilters.FILTER_LIST

    // =============================== Details ===============================

    override fun animeDetailsParse(document: Document): SAnime {
        throw UnsupportedOperationException("Use API-based details parsing")
    }

    override suspend fun getAnimeDetails(anime: SAnime): SAnime {
        val slug = anime.url.removePrefix("/anime/")
        val request = GET("$apiUrl/anime/$slug")
        val response = client.newCall(request).execute()
        val jsonResponse = response.body?.string() ?: return anime
        val apiResponse = json.decodeFromString<AnimeDetailsResponse>(jsonResponse)

        return anime.apply {
            title = apiResponse.data?.title ?: apiResponse.data?.titleOriginal ?: title
            thumbnail_url = apiResponse.data?.poster?.medium ?: apiResponse.data?.poster?.original ?: thumbnail_url
            description = apiResponse.data?.description ?: description
            genre = apiResponse.data?.genres?.joinToString(", ") { it.name ?: "" } ?: genre
            author = apiResponse.data?.studios?.joinToString(", ") { it.name ?: "" }
            artist = apiResponse.data?.studios?.joinToString(", ") { it.name ?: "" }
            status = convertStatus(apiResponse.data?.status)
            if (apiResponse.data?.year != null) {
                description = description?.let { "$it\nГод: ${apiResponse.data.year}" } ?: "Год: ${apiResponse.data.year}"
            }
        }
    }

    // =============================== Episodes ===============================

    override fun episodeListParse(response: Response): List<SEpisode> {
        val jsonResponse = response.body?.string() ?: return emptyList()
        val apiResponse = json.decodeFromString<EpisodesResponse>(jsonResponse)

        return apiResponse.data?.map { it.toSEpisode() }?.reversed() ?: emptyList()
    }

    override fun episodeListRequest(anime: SAnime): Request {
        val slug = anime.url.removePrefix("/anime/")
        val url = apiUrl.toHttpUrl().newBuilder()
            .addPathSegment("anime")
            .addPathSegment(slug)
            .addPathSegment("episodes")
            .build()

        return GET(url)
    }

    override fun episodeListSelector(): String = throw UnsupportedOperationException()
    override fun episodeFromElement(element: Element): SEpisode = throw UnsupportedOperationException()

    // =============================== Video List ===============================

    override fun videoListParse(response: Response): List<Video> {
        val jsonResponse = response.body?.string() ?: return emptyList()
        val playersResponse = json.decodeFromString<PlayersResponse>(jsonResponse)
        val videos = mutableListOf<Video>()

        val preferredPlayer = preferences.getString(PREF_PLAYER_KEY, "any")

        playersResponse.data?.forEach { player ->
            player.embeds?.forEach { embed ->
                val shouldInclude = when {
                    preferredPlayer == "any" -> true
                    embed.player?.name?.lowercase()?.contains(preferredPlayer ?: "") == true -> true
                    embed.url?.lowercase()?.contains(preferredPlayer ?: "") == true -> true
                    else -> false
                }

                if (shouldInclude) {
                    videos.addAll(extractVideosFromEmbed(embed))
                }
            }
        }

        return videos.sortedWith(
            compareByDescending<Video> { it.quality.startsWith("1080", true) }
                .thenByDescending { it.quality.startsWith("720", true) }
                .thenByDescending { it.quality.startsWith("480", true) }
                .thenByDescending { it.quality.startsWith("360", true) }
        )
    }

    override fun videoListRequest(episode: SEpisode): Request {
        val urlParts = episode.url.split("/")
        if (urlParts.size < 4) {
            return GET(baseUrl)
        }

        val animeSlug = urlParts[0]
        val episodeId = urlParts[3]

        val url = apiUrl.toHttpUrl().newBuilder()
            .addPathSegment("anime")
            .addPathSegment(animeSlug)
            .addPathSegment("episodes")
            .addPathSegment(episodeId)
            .addPathSegment("players")
            .build()

        return GET(url)
    }

    override fun videoListSelector(): String = throw UnsupportedOperationException()
    override fun videoFromElement(element: Element): Video = throw UnsupportedOperationException()
    override fun videoUrlParse(document: Document): String = throw UnsupportedOperationException()

    // =============================== Video Extraction ===============================

    private fun extractVideosFromEmbed(embed: Embed): List<Video> {
        val videos = mutableListOf<Video>()
        val embedUrl = embed.url ?: return videos

        when {
            embedUrl.contains("kodik", true) || embedUrl.contains("kodik.info", true) -> {
                videos.addAll(extractKodik(embedUrl))
            }
            embedUrl.contains("sibnet", true) || embedUrl.contains("sibnet.ru", true) -> {
                videos.addAll(extractSibnet(embedUrl))
            }
            embedUrl.contains("alloha", true) || embedUrl.contains("alloha.tv", true) -> {
                videos.addAll(extractAlloha(embedUrl))
            }
            embedUrl.contains("youtube", true) || embedUrl.contains("youtu.be", true) -> {
                videos.addAll(extractYoutube(embedUrl))
            }
            else -> {
                videos.addAll(extractDirectVideo(embedUrl, embed.player?.name ?: "Unknown"))
            }
        }

        return videos
    }

    private fun extractKodik(embedUrl: String): List<Video> {
        val videos = mutableListOf<Video>()
        try {
            val headers = Headers.Builder()
                .add("Referer", baseUrl)
                .build()

            val response = client.newCall(GET(embedUrl, headers)).execute()
            val html = response.body?.string() ?: return videos

            val kodikMatch = Regex("\"url\":\"([^\"]+)\"").find(html)
            kodikMatch?.groupValues?.getOrNull(1)?.let { url ->
                val decodedUrl = url.replace("\\u0026", "&")
                val absoluteUrl = toAbsoluteUrl(decodedUrl)
                videos.add(
                    Video(
                        url = absoluteUrl,
                        quality = "Kodik - HD",
                        videoUrl = absoluteUrl,
                    ),
                )
            }
        } catch (e: Exception) {
            // Ignore errors
        }
        return videos
    }

    private fun extractSibnet(embedUrl: String): List<Video> {
        val videos = mutableListOf<Video>()
        try {
            val headers = Headers.Builder()
                .add("Referer", baseUrl)
                .build()

            val response = client.newCall(GET(embedUrl, headers)).execute()
            val html = response.body?.string() ?: return videos

            val sibnetMatch = Regex("player\\.src\\s*=\\s*\"([^\"]+)\"").find(html)
            sibnetMatch?.groupValues?.getOrNull(1)?.let { url ->
                val absoluteUrl = toAbsoluteUrl(url)
                videos.add(
                    Video(
                        url = absoluteUrl,
                        quality = "Sibnet - 480p",
                        videoUrl = absoluteUrl,
                    ),
                )
            }
        } catch (e: Exception) {
            // Ignore errors
        }
        return videos
    }

    private fun extractAlloha(embedUrl: String): List<Video> {
        val videos = mutableListOf<Video>()
        try {
            val headers = Headers.Builder()
                .add("Referer", baseUrl)
                .build()

            val response = client.newCall(GET(embedUrl, headers)).execute()
            val html = response.body?.string() ?: return videos

            val qualityMatches = Regex("\"?url\"?:\\s*\"([^\"]+)\"[^}]*\"?quality\"?:\\s*\"?(\\d+)\"?").findAll(html)
            qualityMatches.forEach { match ->
                val url = match.groupValues[1]
                val quality = match.groupValues[2]
                val absoluteUrl = toAbsoluteUrl(url)
                videos.add(
                    Video(
                        url = absoluteUrl,
                        quality = "Alloha - ${quality}p",
                        videoUrl = absoluteUrl,
                    ),
                )
            }
        } catch (e: Exception) {
            // Ignore errors
        }
        return videos
    }

    private fun extractYoutube(embedUrl: String): List<Video> {
        val videos = mutableListOf<Video>()
        try {
            val videoId = when {
                embedUrl.contains("youtu.be/", true) -> {
                    embedUrl.substringAfter("youtu.be/").substringBefore("?")
                }
                embedUrl.contains("youtube.com/watch?v=", true) -> {
                    embedUrl.substringAfter("watch?v=").substringBefore("&")
                }
                embedUrl.contains("embed/", true) -> {
                    embedUrl.substringAfter("embed/").substringBefore("?")
                }
                else -> return videos
            }

            val youtubeUrl = "https://www.youtube.com/watch?v=$videoId"
            videos.add(
                Video(
                    url = youtubeUrl,
                    quality = "YouTube",
                    videoUrl = youtubeUrl,
                ),
            )
        } catch (e: Exception) {
            // Ignore errors
        }
        return videos
    }

    private fun extractDirectVideo(embedUrl: String, playerName: String): List<Video> {
        val videos = mutableListOf<Video>()
        try {
            val headers = Headers.Builder()
                .add("Referer", baseUrl)
                .build()

            val response = client.newCall(GET(embedUrl, headers)).execute()
            val html = response.body?.string() ?: return videos

            val videoMatches = Regex("\"'?(https?://[^\"']+\\.(?:mp4|webm|m3u8)[^\"']*)\"'?").findAll(html)
            videoMatches.forEach { match ->
                val url = match.groupValues[1]
                if (url.isNotEmpty() && !url.contains("javascript", true)) {
                    val absoluteUrl = toAbsoluteUrl(url)
                    videos.add(
                        Video(
                            url = absoluteUrl,
                            quality = "$playerName - Default",
                            videoUrl = absoluteUrl,
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }
        return videos
    }

    private fun toAbsoluteUrl(url: String): String {
        return if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "$baseUrl$url"
        }
    }

    // =============================== Converters ===============================

    private fun AnimeDto.toSAnime() = SAnime.create().apply {
        url = "/anime/$slug"
        title = title ?: titleOriginal ?: "Unknown"
        thumbnail_url = poster?.medium ?: poster?.original
        description = description
        genre = genres?.joinToString(", ") { it.name ?: "" }
        status = convertStatus(status)
        author = studios?.joinToString(", ") { it.name ?: "" }
        artist = studios?.joinToString(", ") { it.name ?: "" }
    }

    private fun EpisodeDto.toSEpisode() = SEpisode.create().apply {
        url = "${animeSlug ?: ""}/episodes/${id ?: ""}"
        name = title ?: "Серия ${number ?: "?"}"
        episode_number = number?.toFloat() ?: 0f
        date_upload = parseDateUpload(createdAt)
    }

    private fun convertStatus(status: String?): Int = when (status?.lowercase()) {
        "ongoing" -> SAnime.ONGOING
        "completed" -> SAnime.COMPLETED
        "anons" -> SAnime.LICENSED
        else -> SAnime.UNKNOWN
    }

    private fun parseDateUpload(dateStr: String?): Long {
        if (dateStr == null) return 0L
        return try {
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                val year = parts[0].toIntOrNull() ?: return 0L
                val month = parts[1].toIntOrNull() ?: return 0L
                val day = parts[2].toIntOrNull() ?: return 0L
                val calendar = java.util.Calendar.getInstance()
                calendar.set(year, month - 1, day, 0, 0, 0)
                calendar.timeInMillis
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
}

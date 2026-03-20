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
import eu.kanade.tachiyomi.animesource.online.HttpSource
import eu.kanade.tachiyomi.network.GET
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Locale

class YummyAnime :
    HttpSource(),
    ConfigurableAnimeSource {

    override val name = "YummyAnime (Тестовая)"

    override val baseUrl = "https://site.yummyani.me"

    override val lang = "ru"

    override val supportsLatest = true

    private val apiUrl = "$baseUrl/api/v1"

    override val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
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

    override fun fetchPopularAnime(page: Int): AnimesPage {
        val request = GET("$apiUrl/anime/popular?page=$page&limit=20")
        val response = client.newCall(request).execute()
        return parseAnimePageFromResponse(response)
    }

    // =============================== Latest ===============================

    override fun fetchLatestAnime(page: Int): AnimesPage {
        val request = GET("$apiUrl/anime/updates?page=$page&limit=20")
        val response = client.newCall(request).execute()
        return parseAnimePageFromResponse(response)
    }

    // =============================== Search ===============================

    override fun fetchSearchAnime(page: Int, query: String, filters: AnimeFilterList): AnimesPage {
        val urlBuilder = StringBuilder("$apiUrl/anime/search?q=$query&page=$page&limit=20")

        filters.forEach { filter ->
            when (filter) {
                is YummyAnimeFilters.GenreFilter -> {
                    if (filter.state > 0) {
                        urlBuilder.append("&genre=${filter.state}")
                    }
                }
                is YummyAnimeFilters.YearFilter -> {
                    if (filter.state.isNotEmpty()) {
                        urlBuilder.append("&year=${filter.state}")
                    }
                }
                is YummyAnimeFilters.StatusFilter -> {
                    if (filter.state > 0) {
                        val statusMap = mapOf(
                            1 to "ongoing",
                            2 to "completed",
                            3 to "anons",
                        )
                        urlBuilder.append("&status=${statusMap[filter.state] ?: ""}")
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
                    urlBuilder.append("&sort=${sortMap[filter.state?.index ?: 0] ?: "popular"}")
                }
                else -> {}
            }
        }

        val request = GET(urlBuilder.toString())
        val response = client.newCall(request).execute()
        return parseAnimePageFromResponse(response)
    }

    override fun getFilterList() = YummyAnimeFilters.FILTER_LIST

    // =============================== Details ===============================

    override fun getAnimeDetails(anime: SAnime): SAnime {
        val slug = anime.url.removePrefix("/anime/")
        val request = GET("$apiUrl/anime/$slug")
        val response = client.newCall(request).execute()
        return parseAnimeDetailsFromResponse(response)
    }

    // =============================== Episodes ===============================

    override fun getEpisodeList(anime: SAnime): List<SEpisode> {
        val slug = anime.url.removePrefix("/anime/")
        val request = GET("$apiUrl/anime/$slug/episodes")
        val response = client.newCall(request).execute()
        return parseEpisodesFromResponse(response)
    }

    // =============================== Video List ===============================

    override fun getVideoList(episode: SEpisode): List<Video> {
        val urlParts = episode.url.split("/")
        if (urlParts.size < 4) {
            return emptyList()
        }

        val animeSlug = urlParts[0]
        val episodeId = urlParts[3]

        val request = GET("$apiUrl/anime/$animeSlug/episodes/$episodeId/players")
        val response = client.newCall(request).execute()
        return parseVideosFromResponse(response)
    }

    // =============================== Parsing Methods ===============================

    private fun parseAnimePageFromResponse(response: Response): AnimesPage {
        val html = response.body?.string() ?: return AnimesPage(emptyList(), false)
        val document = Jsoup.parse(html)
        val animeElements = document.select("div.shortstoryContent")
        val animeList = mutableListOf<SAnime>()

        for (element in animeElements) {
            val linkElement = element.selectFirst("table div > a")
            val imgElement = element.selectFirst("table div > a img")
            val titleElement = element.selectFirst("table div > a img")

            if (linkElement != null && imgElement != null) {
                animeList.add(SAnime.create().apply {
                    url = linkElement.attr("abs:href").removePrefix(baseUrl)
                    title = titleElement.attr("alt")
                    thumbnail_url = imgElement.attr("abs:src")
                    status = SAnime.UNKNOWN
                })
            }
        }

        return AnimesPage(animeList, animeElements.size > 0)
    }

    private fun parseAnimeDetailsFromResponse(response: Response): SAnime {
        val html = response.body?.string() ?: return SAnime.create()
        val document = Jsoup.parse(html)
        val contentElement = document.selectFirst(".shortstory > .shortstoryContent td:first-of-type")

        return SAnime.create().apply {
            title = contentElement?.select(".shortstoryHead h1")?.text() ?: "Unknown"
            thumbnail_url = contentElement?.select("img:first-of-type")?.attr("abs:src") ?: ""
            description = contentElement?.select("p:nth-of-type(6) > span")?.text() ?: ""
            status = SAnime.UNKNOWN
            genre = ""
            author = ""
            artist = ""
        }
    }

    private fun parseEpisodesFromResponse(response: Response): List<SEpisode> {
        val html = response.body?.string() ?: return emptyList()
        val document = Jsoup.parse(html)

        val episodes = mutableListOf<SEpisode>()
        val episodeElements = document.select("div.episode-item")

        for ((index, element) in episodeElements.withIndex()) {
            val title = element.select("a")?.text() ?: "Серия ${index + 1}"
            val id = element.attr("data-id")

            episodes.add(SEpisode.create().apply {
                url = "test/$id"
                name = title
                episode_number = (index + 1).toFloat()
                date_upload = System.currentTimeMillis()
            })
        }

        return episodes.reversed()
    }

    private fun parseVideosFromResponse(response: Response): List<Video> {
        val html = response.body?.string() ?: return emptyList()
        val videos = mutableListOf<Video>()

        val playerElements = html.split("\"player_id\":").filter { it.contains("\"name\":") }
        for (player in playerElements) {
            val nameMatch = Regex("\"name\":\"([^\"]+)\"").find(player)
            val urlMatch = Regex("\"url\":\"([^\"]+)\"").find(player)

            if (nameMatch != null && urlMatch != null) {
                val url = urlMatch.groupValues[1].replace("\\\"", "\"")
                val playerName = nameMatch.groupValues[1]

                videos.add(Video(
                    url = url,
                    quality = "$playerName - Тест",
                    videoUrl = url
                ))
            }
        }

        return videos
    }
}

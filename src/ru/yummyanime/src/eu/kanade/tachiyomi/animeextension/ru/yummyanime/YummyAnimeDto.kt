package eu.kanade.tachiyomi.animeextension.ru.yummyanime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnimeListResponse(
    val data: List<AnimeDto>? = null,
    val pagination: PaginationDto? = null,
)

@Serializable
data class AnimeDetailsResponse(
    val data: AnimeDetailsDto? = null,
)

@Serializable
data class EpisodesResponse(
    val data: List<EpisodeDto>? = null,
)

@Serializable
data class PlayersResponse(
    val data: List<PlayerDto>? = null,
)

@Serializable
data class AnimeDto(
    val id: Int? = null,
    val slug: String? = null,
    val title: String? = null,
    @SerialName("title_original") val titleOriginal: String? = null,
    val description: String? = null,
    val status: String? = null,
    val year: Int? = null,
    val poster: PosterDto? = null,
    val genres: List<GenreDto>? = null,
    val studios: List<StudioDto>? = null,
    val rating: Float? = null,
    val views: Int? = null,
)

@Serializable
data class AnimeDetailsDto(
    val id: Int? = null,
    val slug: String? = null,
    val title: String? = null,
    @SerialName("title_original") val titleOriginal: String? = null,
    val description: String? = null,
    val status: String? = null,
    val year: Int? = null,
    val poster: PosterDto? = null,
    val genres: List<GenreDto>? = null,
    val studios: List<StudioDto>? = null,
    val rating: Float? = null,
    val views: Int? = null,
    val episodes: List<EpisodeDto>? = null,
)

@Serializable
data class PosterDto(
    val original: String? = null,
    val medium: String? = null,
    val small: String? = null,
)

@Serializable
data class GenreDto(
    val id: Int? = null,
    val name: String? = null,
    val slug: String? = null,
)

@Serializable
data class StudioDto(
    val id: Int? = null,
    val name: String? = null,
    val slug: String? = null,
)

@Serializable
data class PaginationDto(
    @SerialName("current_page") val currentPage: Int? = null,
    @SerialName("total_pages") val totalPages: Int? = null,
    @SerialName("has_next") val hasNext: Boolean = false,
    @SerialName("has_prev") val hasPrev: Boolean = false,
)

@Serializable
data class EpisodeDto(
    val id: Int? = null,
    @SerialName("anime_id") val animeId: Int? = null,
    @SerialName("anime_slug") val animeSlug: String? = null,
    val number: Int? = null,
    val title: String? = null,
    val description: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class PlayerDto(
    val id: Int? = null,
    val name: String? = null,
    val embeds: List<Embed>? = null,
)

@Serializable
data class Embed(
    val id: Int? = null,
    val player: PlayerInfo? = null,
    val url: String? = null,
    val quality: String? = null,
    val language: String? = null,
)

@Serializable
data class PlayerInfo(
    val id: Int? = null,
    val name: String? = null,
    val slug: String? = null,
)

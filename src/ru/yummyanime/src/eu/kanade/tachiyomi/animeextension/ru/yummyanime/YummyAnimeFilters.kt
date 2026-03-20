package eu.kanade.tachiyomi.animeextension.ru.yummyanime

import eu.kanade.tachiyomi.animesource.model.AnimeFilter
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList

class YummyAnimeFilters {

    class SortFilter : AnimeFilter.Sort(
        "Сортировка",
        arrayOf(
            "По популярности",
            "По рейтингу",
            "По дате",
            "По названию",
            "По просмотрам",
        ),
        Selection(0, false),
    )

    class GenreFilter : AnimeFilter.Select<String>(
        "Жанр",
        arrayOf(
            "Все",
            "Экшен",
            "Приключения",
            "Комедия",
            "Драма",
            "Фэнтези",
            "Романтика",
            "Хоррор",
            "Сэйнен",
            "Сёнэн",
            "Сёдзё",
        ),
    )

    class StatusFilter : AnimeFilter.Select<String>(
        "Статус",
        arrayOf(
            "Все",
            "Онгоинг",
            "Завершен",
            "Анонс",
        ),
    )

    class YearFilter : AnimeFilter.Text("Год")

    companion object {
        val FILTER_LIST: AnimeFilterList
            get() = AnimeFilterList(
                AnimeFilter.Header("Поиск по фильтрам"),
                AnimeFilter.Separator(),
                SortFilter(),
                GenreFilter(),
                StatusFilter(),
                YearFilter(),
            )
    }
}

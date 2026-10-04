package com.melvin.predictor.model

import com.google.gson.annotations.SerializedName

data class Sport(
    @SerializedName("key")           val key: String,
    @SerializedName("group")         val group: String,
    @SerializedName("title")         val title: String,
    @SerializedName("description")   val description: String,
    @SerializedName("active")        val active: Boolean,
    @SerializedName("has_outrights") val hasOutrights: Boolean
)

object FootballLeagues {

    val ALL_LEAGUES = mapOf(
        // 🏴󠁧󠁢󠁥󠁮󠁧󠁿 ENGLAND
        "soccer_epl"                           to "🏴 Premier League",
        "soccer_england_championship"          to "🏴 Championship",
        "soccer_fa_cup"                        to "🏴 FA Cup",
        "soccer_england_efl_cup"               to "🏴 EFL Cup",

        // 🇪🇸 SPAIN
        "soccer_spain_la_liga"                 to "🇪🇸 La Liga",
        "soccer_spain_segunda_division"        to "🇪🇸 Segunda Division",

        // 🇩🇪 GERMANY
        "soccer_germany_bundesliga"            to "🇩🇪 Bundesliga",
        "soccer_germany_bundesliga2"           to "🇩🇪 Bundesliga 2",

        // 🇮🇹 ITALY
        "soccer_italy_serie_a"                 to "🇮🇹 Serie A",
        "soccer_italy_serie_b"                 to "🇮🇹 Serie B",

        // 🇫🇷 FRANCE
        "soccer_france_ligue_one"              to "🇫🇷 Ligue 1",
        "soccer_france_ligue_two"              to "🇫🇷 Ligue 2",

        // 🇳🇱 NETHERLANDS
        "soccer_netherlands_eredivisie"        to "🇳🇱 Eredivisie",

        // 🇵🇹 PORTUGAL
        "soccer_portugal_primeira_liga"        to "🇵🇹 Primeira Liga",

        // 🇹🇷 TURKEY
        "soccer_turkey_super_league"           to "🇹🇷 Super Lig",

        // 🇷🇺 RUSSIA
        "soccer_russia_premier_league"         to "🇷🇺 Russian Premier League",

        // 🇧🇪 BELGIUM
        "soccer_belgium_first_div"             to "🇧🇪 Belgian Pro League",

        // 🇸🇦 SAUDI ARABIA
        "soccer_saudi_arabia_league"           to "🇸🇦 Saudi Pro League",

        // 🏆 UEFA
        "soccer_uefa_champs_league"            to "🏆 Champions League",
        "soccer_uefa_europa_league"            to "🥈 Europa League",
        "soccer_uefa_europa_conference_league" to "🥉 Conference League",
        "soccer_uefa_nations_league"           to "🌍 Nations League",

        // 🌎 WORLD
        "soccer_fifa_world_cup"                to "🌎 FIFA World Cup",
        "soccer_conmebol_copa_libertadores"    to "🌎 Copa Libertadores",
        "soccer_conmebol_copa_america"         to "🌎 Copa America",
        "soccer_africa_cup_of_nations"         to "🌍 AFCON",

        // 🇺🇸 USA
        "soccer_usa_mls"                       to "🇺🇸 MLS",

        // 🇧🇷 BRAZIL
        "soccer_brazil_campeonato"             to "🇧🇷 Brasileirao",

        // 🇦🇷 ARGENTINA
        "soccer_argentina_primera_division"    to "🇦🇷 Argentina Liga",

        // 🇯🇵 JAPAN
        "soccer_japan_j_league"               to "🇯🇵 J1 League",

        // 🇦🇺 AUSTRALIA
        "soccer_australia_aleague"             to "🇦🇺 A-League",

        // 🇰🇷 SOUTH KOREA
        "soccer_korea_kleague1"               to "🇰🇷 K League 1",

        // 🇿🇦 SOUTH AFRICA
        "soccer_south_africa_psl"             to "🇿🇦 South Africa PSL"
    )
}

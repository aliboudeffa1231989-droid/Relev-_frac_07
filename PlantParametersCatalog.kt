package com.example.data

enum class SubSection(val label: String, val shortCode: String) {
    ALL("Toutes sous-sections", "TOUS"),
    DEMETHANISEUR("Déméthaniseur", "C1 • DEMETH"),
    DEETHANISEUR("Dééthaniseur", "C2 • DEETH"),
    DEPROPANISEUR("Dépropaniseur", "C3 • DEPROP"),
    DEBUTANISEUR("Débutaniseur", "C4 • DEBUTA"),
    DEISOPENTANISEUR("Déisopentaniseur", "iC5 • DEISO"),
    REINJECTION_LPG("Réinjection LPG/Gasoline", "REINJ / GAS")
}

enum class ParameterMeasureType(val label: String, val unit: String) {
    LEVEL("Niveau", "%"),
    PRESSURE("Pression", "bar"),
    TEMPERATURE("Température", "°C")
}

data class PlantParameter(
    val id: Int,
    val number: Int,
    val name: String,
    val tag: String,
    val subSection: SubSection,
    val measureType: ParameterMeasureType,
    val partNumber: Int // 1 or 2
)

object PlantParametersCatalog {

    val DEFAULT_SLOTS = listOf(
        1 to "08H00",
        2 to "14H00",
        3 to "20H00",
        4 to "02H00"
    )

    val parameters: List<PlantParameter> = listOf(
        // Partie 1
        PlantParameter(1, 1, "Niveau déméthaniseur", "07-LG1017", SubSection.DEMETHANISEUR, ParameterMeasureType.LEVEL, 1),
        PlantParameter(2, 2, "Pression tête déméthaniseur", "07-PG1273", SubSection.DEMETHANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(3, 3, "Pression HHP propane vers 20-MD04", "07-PG1492", SubSection.DEMETHANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(4, 4, "Press tête 07-MD01 vers 16-MJ06-A/B/C", "07-PG1457", SubSection.DEMETHANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(5, 5, "Press tête 07MD01", "07-PG1338", SubSection.DEMETHANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(6, 6, "Press LP propane sortie 07-MC16", "07-PG1339", SubSection.DEMETHANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(7, 7, "Pression GN sortie 07-MC16", "07-PG1343", SubSection.DEMETHANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(8, 8, "Press sortie 07-MD10 vers torche froide", "07-PG1218", SubSection.DEMETHANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(9, 9, "Press ref Ppe de reflux demeth 07-MJ06", "07-PG1219", SubSection.DEMETHANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(10, 10, "Press ref Ppe de reflux demeth 07-MJ06-A", "07-PG1220", SubSection.DEMETHANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(11, 11, "Press tête Dé-éthaniseur", "07-PG1265", SubSection.DEETHANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(12, 12, "Temp Sortie Dééthaniseur 07-MC14", "07-TG1317", SubSection.DEETHANISEUR, ParameterMeasureType.TEMPERATURE, 1),
        PlantParameter(13, 13, "Press Retour huile chaude Sortie 07-MC14", "07-PG1231", SubSection.DEETHANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(14, 14, "Press éthane sortie condenseur 07-MC03", "07-PG1043", SubSection.DEETHANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(15, 15, "Press ref pompe de reflux deeth 07-MJ01", "07-PG1032", SubSection.DEETHANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(16, 16, "Press ref Ppe de reflux deeth 07-MJ01-A", "07-PG1062", SubSection.DEETHANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(17, 17, "Press sortie condenseur réinjection", "07-PG1336", SubSection.DEETHANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(18, 18, "Press tête Dépropaniseur", "07-PG1320", SubSection.DEPROPANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(19, 19, "Press ref pompe de reflux Dépropa 07-MJ02", "07-PG1091", SubSection.DEPROPANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(20, 20, "Press ref pompe de reflux Déprop 07-MJ02-A", "07-PG1093", SubSection.DEPROPANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(21, 21, "Press sortie 07-MC15 (Refroid Propane)", "07-PG1531", SubSection.DEPROPANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(22, 22, "Press tête Débutaniseur", "07-PG1277", SubSection.DEBUTANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(23, 23, "Temp Retour huile rebouilleur débutaniseur", "07-PG1577", SubSection.DEBUTANISEUR, ParameterMeasureType.TEMPERATURE, 1),
        PlantParameter(24, 24, "Température Sortie Débutaniseur", "07-TG1275", SubSection.DEBUTANISEUR, ParameterMeasureType.TEMPERATURE, 1),
        PlantParameter(25, 25, "Press de Sortie ballon de reflux Débuta", "07-PG1116", SubSection.DEBUTANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(26, 26, "Press ref pompe de reflux Débuta 07-MJ03", "07-PG1123", SubSection.DEBUTANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(27, 27, "Press ref pompe de reflux Débuta 07-MJ03-A", "07-PG1128", SubSection.DEBUTANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(28, 28, "Press vapeur MP C3 sortie 07-MC12", "07-PG1354", SubSection.DEBUTANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(29, 29, "Press Gasoline sortie 07-MC13", "07-PG1361", SubSection.DEBUTANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(30, 30, "Press LP C3 sortie 07-MC09", "07-PG1131", SubSection.DEBUTANISEUR, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(31, 31, "Press tête de ballon de reinject 07-MD08", "07-PG1365", SubSection.REINJECTION_LPG, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(32, 32, "Press LPG reinjection vers 15-MC05", "07-PG1268", SubSection.REINJECTION_LPG, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(33, 33, "Press LPG reinjection vers tête 15-MD01", "07-PG1266", SubSection.REINJECTION_LPG, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(34, 34, "Press LPG reinjection vers tête 15-MD01 (Ligne B)", "07-PG1267", SubSection.REINJECTION_LPG, ParameterMeasureType.PRESSURE, 1),
        PlantParameter(35, 35, "Press ref pompe reinjection 07-MJ04", "07-PG1142", SubSection.REINJECTION_LPG, ParameterMeasureType.PRESSURE, 1),

        // Partie 2 (Suite)
        PlantParameter(36, 36, "Press ref pompe reinjection 07-MJ04-A", "07-PG1148", SubSection.REINJECTION_LPG, ParameterMeasureType.PRESSURE, 2),
        PlantParameter(37, 37, "Press tête Déisopentaniseur", "07-PG1168", SubSection.DEISOPENTANISEUR, ParameterMeasureType.PRESSURE, 2),
        PlantParameter(38, 38, "Température Sortie tête", "07-TG1229", SubSection.DEISOPENTANISEUR, ParameterMeasureType.TEMPERATURE, 2),
        PlantParameter(39, 39, "Press sortie ballon de reflux vers Torche", "07-PG1188", SubSection.DEISOPENTANISEUR, ParameterMeasureType.PRESSURE, 2),
        PlantParameter(40, 40, "Press ref Ppe de reflux Déisopent 07-MJ08", "07-PG1183", SubSection.DEISOPENTANISEUR, ParameterMeasureType.PRESSURE, 2),
        PlantParameter(41, 41, "Press ref Ppe de reflux Déisopent 07-MJ08A", "07-PG1184", SubSection.DEISOPENTANISEUR, ParameterMeasureType.PRESSURE, 2),
        PlantParameter(42, 42, "Press ref Ppe de reinj Déisopent 07-MJ11", "07-PG1187", SubSection.DEISOPENTANISEUR, ParameterMeasureType.PRESSURE, 2),
        PlantParameter(43, 43, "Press ref Ppe de reinj Déisopent 07-MJ11-A", "07-PG1155", SubSection.DEISOPENTANISEUR, ParameterMeasureType.PRESSURE, 2),
        PlantParameter(44, 44, "Press vapeur prop sortie 07-MC25", "07-PG1557", SubSection.DEISOPENTANISEUR, ParameterMeasureType.PRESSURE, 2),
        PlantParameter(45, 45, "Press LP prop sortie 07-MC26", "07-PG1567", SubSection.DEISOPENTANISEUR, ParameterMeasureType.PRESSURE, 2),
        PlantParameter(46, 46, "Press ref Ppe product Gasoline 07-MJ12", "07-PG1572", SubSection.REINJECTION_LPG, ParameterMeasureType.PRESSURE, 2),
        PlantParameter(47, 47, "Press ref Ppe product Gasoline 07-MJ12-A", "07-PG1574", SubSection.REINJECTION_LPG, ParameterMeasureType.PRESSURE, 2)
    )
}

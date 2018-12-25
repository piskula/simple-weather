package sk.momosi.simplewidget.entity

data class ResponseDto (
    val weather: Array<WeatherDto>,
    val main: MainDto,
    val dt: Long,
    val name: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResponseDto

        if (dt != other.dt) return false

        return true
    }

    override fun hashCode(): Int {
        return dt.hashCode()
    }

}

package src.utils

import java.text.SimpleDateFormat
import java.util.*

class CommonUtils {
    fun CurrentDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z")
        val date = Date(System.currentTimeMillis())
        return formatter.format(date)
    }
}
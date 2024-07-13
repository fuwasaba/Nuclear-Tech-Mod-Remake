package at.martinthedragon.nucleartech.logging

import org.slf4j.Logger

fun Logger.debugY(text: String, cat: String = "general") {
    val txt = "[$cat] $text"
    this.debug(txt)
    ClientLog.logs.add(text)
}

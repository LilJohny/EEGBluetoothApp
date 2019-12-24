package com.example.bluetoothapp.presenters

internal enum class Type{
    READ, WRITE, NOTIFY, INDICATE
}



internal sealed class PresenterEvent

internal data class InfoEvent(val infoText: String): PresenterEvent()

internal data class ResultEvent(val result: List<Byte>, val type: Type) : PresenterEvent()

internal data class ErrorEvent(val error: Throwable, val type: Type) : PresenterEvent()

internal data class CompatibilityModeEvent(val isCompatibility: Boolean) : PresenterEvent()


package com.duetify.app.privacy

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** Runtime counter of requests Shields has blocked this session, surfaced in Settings. */
@Singleton
class ShieldsStats @Inject constructor() {
    private val _blocked = MutableStateFlow(0L)
    val blocked: StateFlow<Long> = _blocked.asStateFlow()

    fun record() {
        _blocked.update { it + 1 }
    }
}

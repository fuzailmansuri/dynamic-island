/*
 * Copyright (C) 2025-2026 YAAP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.axdynamicbar.data.source

import android.app.PendingIntent
import com.android.systemui.axdynamicbar.model.IslandEvent
import com.android.systemui.dagger.SysUISingleton
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@SysUISingleton
class SmartspaceIslandManager
@Inject
constructor() {
    private val _sportsEvents = MutableStateFlow<List<IslandEvent.Sports>>(emptyList())
    val sportsEvents: StateFlow<List<IslandEvent.Sports>> = _sportsEvents.asStateFlow()

    private val _nowPlayingEvent = MutableStateFlow<IslandEvent.NowPlaying?>(null)
    val nowPlayingEvent: StateFlow<IslandEvent.NowPlaying?> = _nowPlayingEvent.asStateFlow()

    private var listening = false

    fun startListening() {
        if (listening) return
        listening = true
    }

    fun stopListening() {
        if (!listening) return
        listening = false
        _sportsEvents.value = emptyList()
        _nowPlayingEvent.value = null
    }

    fun updateNowPlaying(songTitle: String, artist: String) {
        if (songTitle.isBlank()) {
            _nowPlayingEvent.value = null
            return
        }
        _nowPlayingEvent.value = IslandEvent.NowPlaying(
            songTitle = songTitle,
            artist = artist,
            key = "smartspace_now_playing",
        )
    }

    fun clearSportsEvent(key: String) {
        _sportsEvents.value = _sportsEvents.value.filter { it.key != key }
    }

    fun updateSports(events: List<IslandEvent.Sports>) {
        _sportsEvents.value = events
    }
}

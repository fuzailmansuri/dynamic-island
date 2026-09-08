/*
 * Copyright (C) 2025 The AxionAOSP Project
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
package com.android.systemui.util

import android.os.Handler
import android.os.Looper
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue

class WeakListenerManager<T>(
    private val backgroundHandler: Handler = Handler(Looper.getMainLooper()),
    private val mainHandler: Handler = Handler(Looper.getMainLooper()),
) {

    private val listeners = ConcurrentLinkedQueue<WeakReference<T>>()
    private var onActive: (() -> Unit)? = null
    private var onInactive: (() -> Unit)? = null
    private var isActive = false

    fun addListener(listener: T) {
        if (listeners.any { it.get() === listener }) return
        listeners.add(WeakReference(listener))
        cleanup()
        if (!isActive && listeners.isNotEmpty()) {
            isActive = true
            onActive?.invoke()
        }
    }

    fun removeListener(listener: T) {
        listeners.removeIf { it.get() == null || it.get() === listener }
        cleanup()
        if (isActive && listeners.isEmpty()) {
            isActive = false
            onInactive?.invoke()
        }
    }

    private fun isStillRegistered(listener: T): Boolean = listeners.any { it.get() === listener }

    fun notify(action: (T) -> Unit) {
        if (listeners.isEmpty()) return
        backgroundHandler.post {
            val snapshot = listeners
                .mapNotNull { it.get() }
                .toList()
            cleanup()
            if (snapshot.isNotEmpty()) {
                mainHandler.post {
                    for (listener in snapshot) {
                        if (isStillRegistered(listener)) action(listener)
                    }
                }
            }
        }
    }

    fun notifyOnMain(action: (T) -> Unit) {
        mainHandler.post {
            val snapshot = listeners.mapNotNull { it.get() }.toList()
            cleanup()
            snapshot.forEach { listener ->
                if (isStillRegistered(listener)) action(listener)
            }
        }
    }

    fun notifyOnBackground(action: (T) -> Unit) {
        backgroundHandler.post {
            val snapshot = listeners.mapNotNull { it.get() }.toList()
            cleanup()
            snapshot.forEach { listener ->
                if (isStillRegistered(listener)) action(listener)
            }
        }
    }

    fun setLifecycleCallbacks(onActive: (() -> Unit)?, onInactive: (() -> Unit)?) {
        this.onActive = onActive
        this.onInactive = onInactive
    }

    fun size(): Int = listeners.count { it.get() != null }
    fun isEmpty(): Boolean = size() == 0

    private fun cleanup() {
        listeners.removeIf { it.get() == null }
    }
}

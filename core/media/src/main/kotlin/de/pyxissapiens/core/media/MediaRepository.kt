package de.pyxissapiens.core.media

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Media storage for georeferenced photos/videos/sketches.
 * Scaffold: TODO implement scoped, optionally encrypted storage under
 * files/projects/{projectId}/media (docs/03 §5.3).
 */
@Singleton
class MediaRepository @Inject constructor(
    @Suppress("unused") @ApplicationContext private val context: Context,
)

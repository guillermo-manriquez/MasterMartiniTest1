package com.example.mastermartini.util.camera

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Where profile photos live: app-private files shared with the camera app through a FileProvider. */
interface ProfilePhotoStore {
    /** A new, empty destination for the camera app to write into. */
    fun newPhotoUri(userId: Long): Uri

    /** Removes a photo created by [newPhotoUri] (replaced, or the capture was cancelled). */
    suspend fun delete(photoUri: String)
}

class FileProviderPhotoStore(private val context: Context) : ProfilePhotoStore {

    private val directory = File(context.filesDir, DIRECTORY)

    override fun newPhotoUri(userId: Long): Uri {
        directory.mkdirs()
        // A new name per photo, so Coil never shows a cached copy of the previous one
        val file = File(directory, "user_${userId}_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    override suspend fun delete(photoUri: String) {
        val name = Uri.parse(photoUri).lastPathSegment ?: return
        withContext(Dispatchers.IO) { File(directory, name).delete() }
    }

    private companion object {
        const val DIRECTORY = "profile" // must match res/xml/file_paths.xml
    }
}

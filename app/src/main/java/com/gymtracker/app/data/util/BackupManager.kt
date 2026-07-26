package com.gymtracker.app.data.util

import android.content.Context
import android.net.Uri
import com.gymtracker.app.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Exports/imports the raw Room SQLite database file to/from a user-chosen location on
 * device storage via the Storage Access Framework. No cloud services are involved —
 * this is a fully local, offline backup mechanism.
 */
object BackupManager {

    suspend fun exportBackup(context: Context, destination: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            AppDatabase.closeInstance()
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            context.contentResolver.openOutputStream(destination)?.use { out ->
                dbFile.inputStream().use { input -> input.copyTo(out) }
            } ?: return@withContext Result.failure(IllegalStateException("Could not open destination for writing"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackup(context: Context, source: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            AppDatabase.closeInstance()
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            // Also clear the -wal/-shm side files so we don't reopen a stale journal.
            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()

            context.contentResolver.openInputStream(source)?.use { input ->
                dbFile.outputStream().use { out -> input.copyTo(out) }
            } ?: return@withContext Result.failure(IllegalStateException("Could not open backup file for reading"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

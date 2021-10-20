package com.ncsu.weed3d.storage

import android.content.Context
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.ncsu.weed3d.HomeActivity
import com.ncsu.weed3d.R
import com.ncsu.weed3d.data.DatabaseInstance
import com.ncsu.weed3d.data.VideoData
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

object GoogleDrive {

    private lateinit var googleDriveService: Drive
    var folderId: String = ""

    fun setupDrive(context: Context, account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account
        googleDriveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance(),
            credential)
            .setApplicationName(context.getString(R.string.app_name))
            .build()
        doAsync {
            try {
                val folders = googleDriveService.files().list().apply {
                    q = "name='Weed3D' and mimeType='application/vnd.google-apps.folder'"
                    spaces = "drive"
                }.execute()
                if (folders.files.size == 0) {
                    val fileMetadata = File()
                    fileMetadata.name = "Weed3D"
                    fileMetadata.mimeType = "application/vnd.google-apps.folder"

                    val folder = googleDriveService.files().create(fileMetadata).apply {
                        fields = "id"
                    }.execute()
                    folderId = folder.id

                } else {
                    folderId = folders.files[0].id
                }
            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: "", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun uploadImagesByDrive(data: VideoData, context: Context, callbackSuccess: () -> Unit) {
        doAsync {
            try {
                val name = data.goProUri.split("/").last()
                val fileMetadata =
                    File()
                fileMetadata.name = "${name}.jpg"
                fileMetadata.parents = Collections.singletonList(folderId)

                val fileTextMetadata = File()
                fileTextMetadata.name = "${name}.info"
                fileTextMetadata.parents = Collections.singletonList(folderId)

                val file = java.io.File(context.getExternalFilesDir("").toString() + data.deviceUri)
                val mediaContent = FileContent("image/jpeg", file)

                val db = DatabaseInstance.getInstance(context)
                val sensorsData = db.databaseDao().getSensorDataByVideoId(data.videoId)
                var sensorInfo = "Sensor Name; Time; Value\n"
                for(sensorData in sensorsData) {
                    sensorInfo += sensorData.sensorName.plus(";").plus(sensorData.time).plus(";").plus(sensorData.value).plus(";\n")
                }

                val fileSensors = java.io.File(context.cacheDir, "${name}.info")
                fileSensors.writeText(sensorInfo)
                val mediaContentText = FileContent("text/plain", fileSensors)

                data.remoteUrl = googleDriveService.files().create(fileMetadata, mediaContent).apply {
                    fields = "id"
                }.execute().id
                googleDriveService.files().create(fileTextMetadata, mediaContentText).apply {
                    fields = "id"
                }.execute()
                db.databaseDao().updateVideo(data)
                uiThread {
                    (context as? HomeActivity)?.toggleLoader(false, "")
                    callbackSuccess()
                }

            } catch (e: Exception) {
                uiThread {
                    (context as? HomeActivity)?.toggleLoader(false, "")
                    (context as? HomeActivity)?.showSuccessMessage(e.message.toString(), false)
                }
            }

        }
    }
}
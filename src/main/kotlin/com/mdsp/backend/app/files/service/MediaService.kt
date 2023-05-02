package com.mdsp.backend.app.files.service

import org.springframework.stereotype.Service
import uk.co.caprica.vlcjinfo.MediaInfo
import uk.co.caprica.vlcjinfo.MediaInfoFile


@Service
class MediaService {

    companion object {
        fun getVideoDuration(filename: String): Long {
            return try {
                val media: MediaInfo = MediaInfo.mediaInfo(filename)
                media.sections("Video").first().duration("Duration").asMilliSeconds()
            } catch (e: Exception) {
                0
            }
        }
    }
}

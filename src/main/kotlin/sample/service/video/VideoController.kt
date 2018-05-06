package sample.service.video

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.UrlResource
import org.springframework.core.io.support.ResourceRegion
import org.springframework.http.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.lang.Long.min

@RestController
class VideoController(@Value("\${video.location}") val videoLocation: String) {

    @GetMapping("/videos/{name}/full")
    fun getFullVideo(@PathVariable name: String, @RequestHeader headers: HttpHeaders): ResponseEntity<UrlResource> {
        val video = UrlResource("file:$videoLocation/$name")
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(video)
    }

    @GetMapping("/videos/{name}")
    fun getVideo(@PathVariable name: String, @RequestHeader headers: HttpHeaders): ResponseEntity<ResourceRegion> {
		val video = UrlResource("file:$videoLocation/$name")
        val region = resourceRegion(video, headers)
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(region)
    }

    private fun resourceRegion(video: UrlResource, headers: HttpHeaders): ResourceRegion {
        val contentLength = video.contentLength()
        val range = headers.range.firstOrNull()
        return if (range != null) {
            val start = range.getRangeStart(contentLength)
            val end = range.getRangeEnd(contentLength)
            val rangeLength = min(ChunkSize, end - start + 1)
            ResourceRegion(video, start, rangeLength)
        } else {
            val rangeLength = min(ChunkSize, contentLength)
            ResourceRegion(video, 0, rangeLength)
        }
    }

    companion object {
        const val ChunkSize = 1000000L
    }

}

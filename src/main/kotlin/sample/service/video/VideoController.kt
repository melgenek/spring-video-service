package sample.service.video

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.UrlResource
import org.springframework.core.io.support.ResourceRegion
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.lang.Long.min

@RestController
class VideoController(@Value("\${video.location}") val videoLocation: String) {

	@GetMapping("/videos/{name}")
    fun getFullVideo(@PathVariable name: String, @RequestHeader headers: HttpHeaders): ResourceRegion {
		val video = UrlResource("file:$videoLocation/$name")
        val contentLength = video.contentLength()
        val range = headers.range.first()
        val start = range.getRangeStart(contentLength)
        val end = range.getRangeEnd(contentLength)
        val rangeLength = min(1000000L, end - start + 1)
        return ResourceRegion(video, start, rangeLength)
	}

}

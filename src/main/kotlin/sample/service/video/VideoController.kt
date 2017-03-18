package sample.service.video

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import sample.service.video.Util.VIDEO_MP4
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption.*

@RestController
class VideoController(@Value("\${video.location}") val videoLocation: String) {

	@GetMapping("/videos/{name}")
	fun getFullVideo(@PathVariable name: String): ResponseEntity<Resource> {
		val video = UrlResource("file:$videoLocation/$name")
		return ResponseEntity.ok()
				.contentType(VIDEO_MP4)
				.body(video)
	}

	@PostMapping("/upload/{fileName}")
	fun uploadFile(@PathVariable fileName: String, request: ServerHttpRequest): Mono<Void> {
		val filePath = Paths.get(videoLocation, fileName)
		// file channel that creates a file or clears the existing one
		val fileChannel = FileChannel.open(filePath, TRUNCATE_EXISTING, CREATE, WRITE)
		return request.body
				.doOnNext { fileChannel.write(it.asByteBuffer()) } // write to file each time server gets data
				.doOnComplete { fileChannel.close() }
				.doOnError {
					fileChannel.close()
					Files.delete(filePath) // delete file created by opened channel
				}
				.then()
	}

}

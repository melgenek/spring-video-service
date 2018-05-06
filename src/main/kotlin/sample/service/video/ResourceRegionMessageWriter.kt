package sample.service.video

import org.reactivestreams.Publisher
import org.springframework.core.ResolvableType
import org.springframework.core.codec.ResourceRegionEncoder
import org.springframework.core.io.Resource
import org.springframework.core.io.support.ResourceRegion
import org.springframework.http.*
import org.springframework.http.codec.HttpMessageWriter
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import reactor.core.publisher.Mono
import java.io.IOException
import java.util.*

class ResourceRegionMessageWriter : HttpMessageWriter<ResourceRegion> {

    private val regionEncoder = ResourceRegionEncoder()

    private val mediaTypes: List<MediaType> = MediaType.asMediaTypes(regionEncoder.encodableMimeTypes)

    override fun canWrite(elementType: ResolvableType, mediaType: MediaType?): Boolean = regionEncoder.canEncode(elementType, mediaType)

    override fun getWritableMediaTypes(): List<MediaType> = mediaTypes

    override fun write(inputStream: Publisher<out ResourceRegion>,
                       elementType: ResolvableType,
                       mediaType: MediaType?,
                       message: ReactiveHttpOutputMessage,
                       hints: Map<String, Any>): Mono<Void> = TODO("use server implementation below")


    override fun write(inputStream: Publisher<out ResourceRegion>,
                       actualType: ResolvableType,
                       elementType: ResolvableType,
                       mediaType: MediaType?, request: ServerHttpRequest,
                       response: ServerHttpResponse, hints: Map<String, Any>): Mono<Void> {

        val headers = response.headers
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes")

        return Mono.from(inputStream).flatMap { resourceRegion ->
            response.statusCode = HttpStatus.PARTIAL_CONTENT
            val resourceMediaType = getResourceMediaType(mediaType, resourceRegion.resource)
            headers.contentType = resourceMediaType

            val contentLength = resourceRegion.resource.contentLength()
            val start = resourceRegion.position
            val end = Math.min(start + resourceRegion.count - 1, contentLength - 1)
            headers.add("Content-Range", "bytes " + start + '-'.toString() + end + '/'.toString() + contentLength)
            headers.contentLength = end - start + 1

            zeroCopy(resourceRegion.resource, resourceRegion, response)
                    .orElseGet({
                        val input = Mono.just(resourceRegion)
                        val body = this.regionEncoder.encode(input, response.bufferFactory(), REGION_TYPE, resourceMediaType, emptyMap<String, Any>())
                        response.writeWith(body)
                    })
        }
    }


    private fun writeSingleRegion(region: ResourceRegion, message: ReactiveHttpOutputMessage): Mono<Void> {
        return zeroCopy(region.resource, region, message)
                .orElseGet({
                    val input = Mono.just(region)
                    val mediaType = message.headers.contentType
                    val body = this.regionEncoder.encode(input, message.bufferFactory(), REGION_TYPE, mediaType, emptyMap<String, Any>())
                    message.writeWith(body)
                })
    }

    private companion object {
        fun getResourceMediaType(mediaType: MediaType?, resource: Resource): MediaType {
            return if (mediaType !== null && mediaType.isConcrete && mediaType !== MediaType.APPLICATION_OCTET_STREAM) mediaType
            else MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM)
        }

        fun zeroCopy(resource: Resource, region: ResourceRegion,
                     message: ReactiveHttpOutputMessage): Optional<Mono<Void>> {
            if (message is ZeroCopyHttpOutputMessage && resource.isFile) {
                try {
                    val file = resource.file
                    val pos = region.position
                    val count = region.count
                    return Optional.of(message.writeWith(file, pos, count))
                } catch (ex: IOException) {
                    // should not happen
                }
            }
            return Optional.empty()
        }


        val REGION_TYPE = ResolvableType.forClass(ResourceRegion::class.java)

    }


}
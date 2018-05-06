package sample.service.video

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.reactive.config.WebFluxConfigurer


@SpringBootApplication
class Application : WebFluxConfigurer {

	override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
		configurer.customCodecs().writer(ResourceRegionMessageWriter())
	}

}

fun main(args: Array<String>) {
	SpringApplication.run(Application::class.java, *args)
}

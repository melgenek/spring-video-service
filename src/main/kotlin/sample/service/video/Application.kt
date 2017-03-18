package sample.service.video

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.config.ViewResolverRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.view.freemarker.FreeMarkerConfigurer


@SpringBootApplication
class Application : WebFluxConfigurer {

	@Bean
	fun freeMarkerConfigurer(applicationContext: ReactiveWebApplicationContext): FreeMarkerConfigurer {
		val configurer = FreeMarkerConfigurer()
		configurer.setTemplateLoaderPath("classpath:/templates/")
		configurer.setResourceLoader(applicationContext)
		return configurer
	}

	override fun configureViewResolvers(registry: ViewResolverRegistry) {
		registry.freeMarker()
	}

}

fun main(args: Array<String>) {
	SpringApplication.run(Application::class.java, *args)
}

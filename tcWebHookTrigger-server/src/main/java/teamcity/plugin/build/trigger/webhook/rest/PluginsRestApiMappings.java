package teamcity.plugin.build.trigger.webhook.rest;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import teamcity.plugin.rest.core.RestApiMappings;

@Configuration
public class PluginsRestApiMappings {

	@Bean
	RestApiMappings restApiMappings() {
		return () -> Arrays.asList("/webhook-trigger/*");
	}

}

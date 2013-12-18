package com.jschneider.springmvc.test.harness;

import java.util.List;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.jschneider.springmvc.RequestBodyPartMethodArgumentResolver;

@Configuration
@ComponentScan(basePackages = "com.jschneider.springmvc.test.harness")
public class WebConfig extends WebMvcConfigurationSupport {
	RequestBodyPartMethodArgumentResolver requestBodyPartMethodArgumentResolver = new RequestBodyPartMethodArgumentResolver();
	
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		addDefaultHttpMessageConverters(converters);
		
		requestBodyPartMethodArgumentResolver.setMessageConverters(converters);
		for(HttpMessageConverter<?> converter : converters) {
			if(converter instanceof MappingJackson2HttpMessageConverter) {
				requestBodyPartMethodArgumentResolver.setObjectMapper(((MappingJackson2HttpMessageConverter) converter).getObjectMapper());
			}
		}
	}
	
	@Override
	protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(requestBodyPartMethodArgumentResolver);
	}
}

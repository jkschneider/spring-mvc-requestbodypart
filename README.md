Spring MVC @RequestBodyPart
==========================

The purpose of this extension is to provide a way to split a post body across multiple method parameters in a Spring MVC controller.

The trivial case where we want to unmarshal the entire post body into a single method argument is covered by Spring's built-in @RequestBody annotation

```java
@Controller
@RequestMapping(value = "/pets", method = RequestMethod.POST)
public void addPet(@RequestBody Pet pet) {
	// implementation omitted
}
```

Suppose we have the following post body (in JSON):
	
```javascript
{ pet: { name: 'fluffy', type: 'dog' }, owner: { name: 'jon' } }
```

In this case, we are trying to pass a pet owner along with the pet in the post body like so:
	
```java
@Controller
@RequestMapping(value = "/pets", method = RequestMethod.POST)
public void addPet(@RequestBody Pet pet, @RequestBody Owner owner) {
	// THIS DOES NOT WORK
}
```
	
This extension adds a `@RequestBodyPart` annotation to Spring's existing suite of tools.

```java
@Controller
@RequestMapping(value = "/pets", method = RequestMethod.POST)
public void addPet(@RequestBodyPart Pet pet, @RequestBodyPart Owner owner) {
	// THIS WORKS!
}
```
	
Configuration
==========================

Configuration isn't super clean right now due to some limitations in Spring.  You need to have at least the following in your configuration:

```java
@Configuration
@ComponentScan(basePackages = "...")
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
```

It is necessary to extend `WebMvcConfigurationSupport` in order to configure message converters and add custom argument resolvers.

The argument resolver requires a Jackson object mapper, but unfortunately it is not possible to inject this mapper into the argument resolver with typical dependency injection strategies, thus the manual configuration.

Building
==========================
From the repository root, run `mvn package`

Testing
==========================
Start `TomcatServer` and navigate to http://localhost:8080

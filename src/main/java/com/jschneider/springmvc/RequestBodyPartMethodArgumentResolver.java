package com.jschneider.springmvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestBodyPartMethodArgumentResolver implements HandlerMethodArgumentResolver {
	private RequestResponseBodyMethodProcessor requestResponseBodyMethodProcessor;
	private ObjectMapper objectMapper;
	
	WeakHashMap<NativeWebRequest, Map<String, Object>> deserializedBody = new WeakHashMap<>();
	
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(RequestBodyPart.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object resolveArgument(MethodParameter parameter,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) throws Exception {
		
		Map<String, Object> map = deserializedBody.get(webRequest);
		if(map == null) {
			MethodParameter parameterModified = new MapGeneratingMethodParameter(parameter);
			map = (Map<String, Object>) requestResponseBodyMethodProcessor.resolveArgument(parameterModified, mavContainer, webRequest, binderFactory);
			deserializedBody.put(webRequest, map);
		}

		RequestBodyPart annotation = parameter.getParameterAnnotation(RequestBodyPart.class);
		Object deserialized = map.get(annotation.value().isEmpty() ? parameter.getParameterName() : annotation.value());
 		return objectMapper.convertValue(deserialized, parameter.getParameterType());
	}
	
	private class MapGeneratingMethodParameter extends MethodParameter {
		public MapGeneratingMethodParameter(MethodParameter original) {
			super(original);
		}
		
		@Override
		public Type getGenericParameterType() {
			return Map.class;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Annotation> T getParameterAnnotation(Class<T> clazz) {
			if(RequestBody.class.equals(clazz)) {
				try {
					return (T) getClass().getDeclaredMethod("requestBodyShim", String.class).getParameterAnnotations()[0][0];
				} catch (NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
			}
			return super.getParameterAnnotation(clazz);
		}
		
		@SuppressWarnings("unused")
		protected void requestBodyShim(@RequestBody String shim) {}
	}

	public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
		requestResponseBodyMethodProcessor = new RequestResponseBodyMethodProcessor(messageConverters);
	}
	
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
}

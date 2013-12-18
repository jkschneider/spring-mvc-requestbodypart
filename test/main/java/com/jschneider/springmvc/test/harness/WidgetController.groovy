package com.jschneider.springmvc.test.harness

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

import com.jschneider.springmvc.RequestBodyPart

@Controller
public class WidgetController {
	@RequestMapping(method=RequestMethod.POST, value = "/test1")
	@ResponseBody
	public String test1(@RequestBodyPart Person person, @RequestBodyPart Widget widget) {
		assert person?.name == "jon schneider"
		assert person?.age == 30
		return "success"
	}
	
	@RequestMapping(method=RequestMethod.POST, value = "/test2")
	@ResponseBody
	public String test2(@RequestBodyPart("person") Person person1, @RequestBodyPart Widget widget) {
		assert person1?.name == "jon schneider"
		assert person1?.age == 30
		return "success"
	}
	
	@RequestMapping(method=RequestMethod.POST, value = "/test3")
	@ResponseBody
	public String test3(@RequestBodyPart String name, @RequestBodyPart Double amount) {
		assert name == "jon schneider"
		assert amount == 1.25
		return "success"
	}
}

package org.chronotics.talaria.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControllerBasic {
	
	@RequestMapping(
    		value = "/apiexam1", 
    		method = RequestMethod.GET)
    @ResponseBody
    public String daoExample(
    		@RequestParam("para1") String _para1,
    		@RequestParam("para2") String _para2) {
		return null;
    }
	
	@RequestMapping(
    		value = "/apiexam2", 
    		method = RequestMethod.POST, 
    		consumes = {"application/json"})
    @ResponseBody
    public String insertRecord(
    		@RequestBody String _json) {
    	return null;
    }
}

package com.example.inventory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.attoparser.config.ParseConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Benjamin
 *
 */
@RestController
public class InventoryController {

	@Autowired
	InventoryManagementApplication inventoryManagement;


	/*login method is set up to take a json 
	 * payload and return a string as a response. 
	 * This is just for testing. Eventually it will return
	 * a json object containing all of the data needed to 
	 * build the users dashboard if they successfully authenticate in.
	 * 
	 * test with: curl -H "Content-Type: application/json" --data '{"username":"xyz","password":"123"}' @body.json http://localhost:8080/login
	 */
	@RequestMapping("/login")
	@ResponseBody
	public Boolean login(@RequestBody String payload) throws SQLException, ClassNotFoundException {

		try {
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String username = jsonNode.get("username").asText();
			String password = jsonNode.get("password").asText();
			
			Boolean authenticated = inventoryManagement.authenticateIntoApplication(username, password); 
			return authenticated;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	//test with: curl -H "Content-Type: application/json" --data '{"bucketName":"Unit1","partNumbersAllowed":"123789-121", "department":"testDept", "unitOfMeasurement":"pounds", "maxMeasurement":"300", "location":"testLocation"}' @body.json http://localhost:8080/createDigitalStorageItem
	@RequestMapping(value = "/createDigitalStorageItem")
	@ResponseBody
	public Boolean createDigitalStorageItem(@RequestBody String payload) throws SQLException {
		
		try {
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String bucketName = jsonNode.get("bucketName").asText();
			String partNumbersAllowed = jsonNode.get("partNumbersAllowed").asText();
			String department = jsonNode.get("department").asText();
			String unitOfMeasurement = jsonNode.get("unitOfMeasurement").asText();
			int maxMeasurement = jsonNode.get("maxMeasurement").asInt();
			String location = jsonNode.get("location").asText();
			
			boolean response = inventoryManagement.createDigitalStorageItem(bucketName, partNumbersAllowed, department, unitOfMeasurement, maxMeasurement, location);
			return response;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	
	@RequestMapping(value = "/removePartsToStorage")
	@ResponseBody 
	public Boolean removePartsToStorage(@RequestBody String payload) throws SQLException {

		try {
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			int bucketID = jsonNode.get("bucketID").asInt();
			String partNumber = jsonNode.get("partNumber").asText();
			String serialNumber = jsonNode.get("serialNumber").asText();
			
			boolean response = inventoryManagement.removePartsToStorage(bucketID, partNumber, serialNumber);
			return response;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@RequestMapping(method=RequestMethod.POST, value = "/partSetUp")
	@ResponseBody 
	public Boolean setUpPartNumber(@RequestBody String payload) throws SQLException {
		
		try {
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String partNumber = jsonNode.get("partNumber").asText();
			int trackByWeight = jsonNode.get("trackByWeight").asInt();
			int weight = jsonNode.get("weight").asInt();
			
			boolean response = inventoryManagement.setUpPartNumber(partNumber, trackByWeight, weight);
			return response;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@RequestMapping(value = "/addPartsToStorage")
	@ResponseBody
	public Boolean addPartsToStorage(@RequestBody String payload) throws SQLException{

		try {
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String username = jsonNode.get("username").asText();
			String csrf = jsonNode.get("csrf").asText();
			String department = jsonNode.get("department").asText();
			int unit = jsonNode.get("unit").asInt();
			String type = jsonNode.get("type").asText();
			int hasWeight = jsonNode.get("hasWeight").asInt();
			int serialNo = jsonNode.get("serialNo").asInt();
			int partNo = jsonNode.get("partNo").asInt();
			int weight = jsonNode.get("weight").asInt();
			
			boolean response = inventoryManagement.addPartsToStorage(username, csrf, department, unit, type, hasWeight, serialNo, partNo, weight);
			return response;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	@RequestMapping(value = "/unit")
	@ResponseBody
	public Unit unitData(@RequestBody String payload) throws SQLException {
		
		Unit unitcall = null;
		try {
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			int bucketID = jsonNode.get("BucketId").asInt();
			//System.out.println(bucketID);
			unitcall = inventoryManagement.unitData(bucketID);
			return unitcall;
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return unitcall;
	}
	
	
	//test with: curl -H "Content-Type: application/json" --data '{"UserName":"bchoi","Password":"yo", "Admin":"test"}' @body.json http://localhost:8080/addUser
	@RequestMapping(value = "/addUser")
	@ResponseBody
	public Boolean addUser(@RequestBody String payload) throws SQLException {
		
		try {
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String UserName = jsonNode.get("UserName").asText();
			String Password = jsonNode.get("Password").asText();
			String Admin = jsonNode.get("Admin").asText();
			
			boolean response = inventoryManagement.addUser(UserName, Password, Admin);
			return response;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	//test with: curl -H "Content-Type: application/json" --data '{"UserName":"bchoi","Password":"yo", "Admin":"test"}' @body.json http://localhost:8080/removeUser
	@RequestMapping(value = "/removeUser")
	@ResponseBody
	public Boolean removeUser(@RequestBody String payload) throws SQLException {
		
		try {
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String UserName = jsonNode.get("UserName").asText();
			String Password = jsonNode.get("Password").asText();
			String Admin = jsonNode.get("Admin").asText();
			
			boolean response = inventoryManagement.removeUser(UserName, Password, Admin);
			return response;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
}

package com.example.inventory;

import java.sql.SQLException;
import java.util.Map;

import org.attoparser.config.ParseConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
	 * test with: curl -H "Content-Type: application/json" --data '{"username":"Dan","password":"test"}' @body.json http://localhost:8080/login
	 */
	@RequestMapping("/login")
	@ResponseBody
	public Boolean login(@RequestBody Map<String, String> payload) throws SQLException, ClassNotFoundException {
		//The controller receives the request from the front end and then sends it
		//to inventoryManagement to perform processing. 
		//DashboardData dashData = new DashboardData();
		boolean authenticated = false;
		String username = payload.get("username");
		String password = payload.get("password");
		authenticated = inventoryManagement.authenticateIntoApplication(username, password); 

		return authenticated;
	}

    //test with: curl -H "Content-Type: application/json" --data '{"bucketName":"Unit1","partNumbersAllowed":"123789-121", "department":"testDept", "unitOfMeasurement":"pounds", "maxMeasurement":"300", "location":"testLocation"}' @body.json http://localhost:8080/createDigitalStorageItem
	@RequestMapping(value = "/createDigitalStorageItem")
	@ResponseBody
	public Boolean createDigitalStorageItem(@RequestBody Map<String, String> payload) throws SQLException {
		String bucketName = payload.get("bucketName");
		String partNumbersAllowed = payload.get("partNumbersAllowed");
		String department = payload.get("department");
		String unitOfMeasurement = payload.get("unitOfMeasurement");
		String maxMeasurement = payload.get("maxMeasurement");
		String location = payload.get("location");
		int maxMeasConverted = Integer.parseInt(maxMeasurement);
		boolean response = inventoryManagement.createDigitalStorageItem(bucketName, partNumbersAllowed, department, unitOfMeasurement, maxMeasConverted, location);
		
		return response;
	}

	@RequestMapping(value = "/addPartsToStorage")
	@ResponseBody 
	public Boolean addPartsToStorage(@RequestBody Map<String, String> payload) throws SQLException {
		String bucketID = payload.get("bucketID");
		int bucketIDconverted = Integer.parseInt(bucketID);
		String partNumber = payload.get("partNumber");
		String serialNumber = payload.get("serialNumber");
		
		boolean response = inventoryManagement.addPartsToStorage(bucketIDconverted, partNumber, serialNumber);
		return response;
	}

}

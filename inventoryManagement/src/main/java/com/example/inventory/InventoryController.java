package com.example.inventory;

import com.example.inventory.datamodels.Unit;
import com.example.inventory.datamodels.Items;
import com.example.inventory.datamodels.DashboardData;
import com.example.inventory.datamodels.User;
import com.example.inventory.datamodels.Department;



import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	public Map<String, String> login(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload) throws SQLException, ClassNotFoundException {
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
		response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		RequestContextHolder.currentRequestAttributes().getSessionId();
		HashMap<String, String> loginResp = new HashMap<String, String>();
		try {
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String username = jsonNode.get("username").asText();
			String password = jsonNode.get("password").asText();
			
			Boolean authenticated = inventoryManagement.authenticateIntoApplication(username, password);

			// >>>>> CSRF CODE HERE
			if (authenticated){
				HttpSession session = request.getSession();
				String token = UUID.randomUUID().toString();

				session.setAttribute("csrf", token);
				session.setAttribute("username", username);
				System.out.println(session.getAttribute("csrf"));
				loginResp.put("csrf", token);
			}
			// <<<<< CSRF CODE ENDS HERE
			
			loginResp.put("success", authenticated.toString());

			return loginResp;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	@RequestMapping("/logout")
	@ResponseBody
	public Map<String, String> logout(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload) throws SQLException, ClassNotFoundException {
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
		response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		RequestContextHolder.currentRequestAttributes().getSessionId();
		HashMap<String, String> map = new HashMap<>();
		try{
			request.getSession().invalidate();
			map.put("success", "true");
		}catch(Exception e){
			map.put("success", "false");
		}

		return map;
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

	
	@RequestMapping(value = "/removePartsFromStorage")
	@ResponseBody 
	public Boolean removePartsToStorage(HttpServletRequest request, HttpServletResponse resp, @RequestBody String payload) throws SQLException {

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
	public Map<String, String> addPartsToStorage(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload) throws SQLException{
		
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
		response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
		response.setHeader("Access-Control-Allow-Credentials", "true");

		try {
			HashMap<String, String> addItemResp = new HashMap<String, String>();
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String username = jsonNode.get("username").asText();
			String csrf = jsonNode.get("csrf").asText();
			String department = jsonNode.get("department").asText();
			int unit = jsonNode.get("unit").asInt();
			String type = jsonNode.get("type").asText();
			int hasWeight = jsonNode.get("hasWeight").asInt(); // CHANGED TO BOOLEAN FROM INT -- SIAM
			int serialNo = jsonNode.get("serialNo").asInt();
			int partNo = jsonNode.get("partNo").asInt();
			
			// >>>>>> BEFORE
			// int weight = jsonNode.get("weight").asInt();
			// ======
			int weight = 1;
			if (hasWeight != 0) weight = jsonNode.get("weight").asInt();
			// <<<<<<

			HttpSession session = request.getSession();
			if (session.getAttribute("username") != username){
				addItemResp.put("success", "false");
			}else if (session.getAttribute("csrf") != csrf){
				addItemResp.put("success", "false");
			}else{
				Boolean responseAdd = inventoryManagement.addPartsToStorage(username, csrf, department, unit, type, hasWeight, serialNo, partNo, weight);
				addItemResp.put("success", responseAdd.toString());
			}
			
			return addItemResp;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@RequestMapping(value = "/unit")
	@ResponseBody
	public Map<String, String> unitData(HttpServletResponse response, @RequestBody String payload) throws SQLException, IOException {
		System.out.println("fml");
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
		response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		HashMap<String, String> unitResp = new HashMap<String, String>();
		JsonNode jsonNode = new ObjectMapper().readTree(payload);
		System.out.println(jsonNode);
		int bucketID = jsonNode.get("unitID").asInt();
		//int bucketID = 1;
		System.out.println("Got herer");
		Unit unitcall = inventoryManagement.unitData(bucketID);
		System.out.println("Got herer");
		System.out.println(unitcall.getItems());
		System.out.println("got here");
		unitResp.put("success", "true");
		unitResp.put("items", unitcall.getItems().toString());
		System.out.println("got here");
		return unitResp;

	}
	

	
	@RequestMapping(value = "/dashboard")
	@ResponseBody
	public List<Department> returnDashboard(HttpServletResponse response) throws SQLException {
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
		response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		List<DashboardData> dashboard = new ArrayList<DashboardData>();
		
		dashboard = inventoryManagement.gatherDashboardData();
		
		HashMap<String, ArrayList<DashboardData>> map = new HashMap<>();
		for (DashboardData bucket: dashboard) {
			map.compute(bucket.getDepartmentId(), (key,  value)->{
				if (value == null) {
					value = new ArrayList<DashboardData>();
				} 
				value.add(bucket);
				return value;
				
			});
				
		}
		ArrayList<Department> ret_list = new ArrayList<Department>();
		for (Map.Entry<String, ArrayList<DashboardData>> entry: map.entrySet()) {
			ret_list.add(new Department(entry.getKey(), entry.getValue()));
		}
		return ret_list;
	}


	private void setHeaders(HttpServletResponse response){
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
		response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
		response.setHeader("Access-Control-Allow-Credentials", "true");
	}
}

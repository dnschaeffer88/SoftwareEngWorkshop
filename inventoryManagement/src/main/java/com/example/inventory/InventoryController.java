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
import com.google.gson.Gson;

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
		setHeaders(response);
		System.out.println("Call to /login");
		
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

				session.setMaxInactiveInterval(60 * 30);
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
		setHeaders(response);
		System.out.println("Call to /logout");
		
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
	public Map<String, String> createDigitalStorageItem(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload) throws SQLException {
		setHeaders(response);
		System.out.println("Call to /createDigitalStorageItem");

		HashMap<String, String> map = new HashMap<>();
		try {
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String bucketName = jsonNode.get("bucketName").asText();
			String partNumbersAllowed = jsonNode.get("partNumbersAllowed").asText();
			String department = jsonNode.get("department").asText();
			String unitOfMeasurement = jsonNode.get("unitOfMeasurement").asText();
			int maxMeasurement = jsonNode.get("maxMeasurement").asInt();
			String location = jsonNode.get("location").asText();

			String username = checkAuthorizedAccess(request, jsonNode);


			String resp = inventoryManagement.createDigitalStorageItem(username, "", bucketName, partNumbersAllowed, department, unitOfMeasurement, maxMeasurement, location);
			if (resp.equals("success")) {
				map.put("success", "true");
			} else {
				map.put("success", "false");
				map.put("error_message", resp);
			}
			return map;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		map.put("success", "false");
		map.put("error_message", "IOException encountered. Please try again.");
		return map;
	}

	// 'bucketId', 'user', 'csrf'
	@RequestMapping(value = "/removeDigitalStorageItem")
	@ResponseBody
	public Map<String, String> removeDigitalStorageItem(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload) throws SQLException {
		setHeaders(response);
		System.out.println("Call to /removeDigitalStorageItem");

		HashMap<String, String> map = new HashMap<>();
		try {
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String bucketId = jsonNode.get("bucketId").asText();
			String username = jsonNode.get("username").asText();
			String csrf = jsonNode.get("csrf").asText();
			
			String resp = inventoryManagement.removeDigitalStorageItem(username, csrf, bucketId);
			if (resp.equals("success")) {
				map.put("success", "true");
			} else {
				map.put("success", "false");
				map.put("error_message", resp);
			}
			return map;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		map.put("success", "false");
		map.put("error_message", "IOException encountered. Please try again.");
		return map;
	}

	
	@RequestMapping(value = "/removePartsFromStorage")
	@ResponseBody 
	public Boolean removePartsToStorage(HttpServletRequest request, HttpServletResponse resp, @RequestBody String payload) throws SQLException {

		setHeaders(resp);
		System.out.println("Call to /removePartsFromStorage");

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
	public Map<String, String> setUpPartNumber(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload){
		setHeaders(response);
		System.out.println("Call to /partSetUp");
		Map<String, String> map = new HashMap<>();

		try {
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String partNumber = jsonNode.get("partNumber").asText();
			int trackByWeight = jsonNode.get("trackByWeight").asInt();
			int weight = jsonNode.get("weight").asInt();
			System.out.println(weight);

			boolean hasWeight = trackByWeight == 1 ? true : false;

			String email = checkAuthorizedAccess(request, jsonNode);
			if (email == null){
				// TODO 
			}

			if (inventoryManagement.userIsAdmin(email)){
				Boolean resp = inventoryManagement.setUpPartNumber(partNumber, hasWeight, weight);
				if (resp){
					map.put("success", "true");
				}
				else{
					map.put("success", "false");
					map.put("errorMessage",/*TODO*/ "TODO");
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			map.put("success", "false");
		}
		return map;
	}
	
	@RequestMapping(value = "/addPartsToStorage")
	@ResponseBody
	public Map<String, String> addPartsToStorage(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload) throws SQLException{
		
		setHeaders(response);

		System.out.println("Call to /addPartsToStorage");

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
	public Map<String, String> unitData(HttpServletResponse response, HttpServletRequest request, @RequestBody String payload) throws SQLException{
		setHeaders(response);
		System.out.println("Call to /unit");
		HashMap<String, String> unitResp = new HashMap<String, String>();
		try{
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			System.out.println(jsonNode);

			String email = checkAuthorizedAccess(request, jsonNode);
			if (email == null) {
				unitResp.put("success", "false");
				unitResp.put("errorMessage", "Unauthorized Access");
				return unitResp;
			}

			String unitID = jsonNode.get("unitID").asText();
			String departmentName = jsonNode.get("departmentName").asText();

			System.out.println("Got herer");
			Unit unitcall = inventoryManagement.unitData(unitID, departmentName, email);
			System.out.println("Got herer");
			System.out.println(unitcall.items);
			System.out.println("got here");
			unitResp.put("success", "true");
			unitResp.put("items", unitcall.items.toString());
			System.out.println("got here");

			return unitResp;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		

	}
	

	
	@RequestMapping(value = "/dashboard")
	@ResponseBody
	public Map<String, String> returnDashboard(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload) throws SQLException {
		setHeaders(response);
		System.out.println("Call to /dashboard");
		HashMap<String, String> map = new HashMap<>();
		try{
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String csrf = jsonNode.get("csrf").asText();
			HttpSession session = request.getSession();
			String existingCsrf = session.getAttribute("csrf").toString();

			System.out.println(existingCsrf);
			System.out.println("<<<>>>>");
			System.out.println(csrf);
			String user = jsonNode.get("username").asText();
			System.out.println(user);
			if (!csrf.equals(existingCsrf)){ throw new IllegalAccessException();}
			String username = session.getAttribute("username").toString();
			
			
			return inventoryManagement.gatherDashboardData(username);
		}catch(IOException e){
			map.put("success", "false");
			map.put("errorMessage", "Failed to extract payload");
			return map;
		}catch(IllegalAccessException e){
			map.put("success", "false");
			map.put("errorMessage", "Unauthorized Access");
			return map;
		}
		
	}


	private void setHeaders(HttpServletResponse response){
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
		response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
		response.setHeader("Access-Control-Allow-Credentials", "true");
	}

	private String checkAuthorizedAccess(HttpServletRequest request, JsonNode jsonNode){

		HttpSession session = request.getSession();
		String email = session.getAttribute("username").toString();
		String csrf = session.getAttribute("csrf").toString();
		String currToken = jsonNode.get("csrf").asText();

		if (csrf == currToken) return email;

		return null;
	}

	@RequestMapping("/dummyUser")
	@ResponseBody
	public Map<String, String> dummyUser(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload) throws SQLException, ClassNotFoundException, IOException {
		System.out.println("Receieved Request on dummyUser to create user");
		JsonNode jsonNode = new ObjectMapper().readTree(payload);
		String username = jsonNode.get("username").asText();
		String password = jsonNode.get("password").asText();
		System.out.println(username);
		System.out.println(password);

		
		inventoryManagement.createQuickUser(username, password);
		return new HashMap<>();
	}
}

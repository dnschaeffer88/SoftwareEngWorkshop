package com.example.inventory;

import com.example.inventory.datamodels.Unit;
import com.example.inventory.datamodels.Item;
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


	@RequestMapping(value = "/checkLogin")
	@ResponseBody
	public Map<String, String> checkLogin(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload){

		setHeaders(response);
		Map<String, String> map = new HashMap<>();
		HttpSession session = request.getSession(false);
		System.out.println(session);
		if (session == null){
			map.put("success", "false");
			return map;
		}else{
			String token = UUID.randomUUID().toString();
			session.setAttribute("csrf", token);
			// session.setMaxInactiveInterval(60 * 30);
			map.put("success", "true");
			map.put("csrf", token);
			return map;
		}
	}

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
	public Map<String, String> login(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload){
		setHeaders(response);
		System.out.println("Call to /login");
		
		RequestContextHolder.currentRequestAttributes().getSessionId();
		HashMap<String, String> loginResp = new HashMap<String, String>();
		try {
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String username = jsonNode.get("username").asText();
			String password = jsonNode.get("password").asText();
			
			Boolean authenticated = inventoryManagement.authenticateIntoApplication(username, password);

			if (authenticated){
				HttpSession session = request.getSession();
				String token = UUID.randomUUID().toString();

				session.setMaxInactiveInterval(60 * 30);
				session.setAttribute("csrf", token);
				session.setAttribute("username", username);
				System.out.println(session.getAttribute("csrf"));
				loginResp.put("csrf", token);
				loginResp.put("success", "true");
			}else{
				loginResp.put("success", "false");
				loginResp.put("errorMessage", "Authentication Failed");
			}
			

		} catch (IOException e) {
			e.printStackTrace();
			loginResp.put("success", "false");
			loginResp.put("errorMessage", "Invalid Request");
		} catch (NullPointerException e){
			loginResp.put("success", "false");
			loginResp.put("errorMessage", "Invalid Request");
		}
		return loginResp;
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
			if (username == null){
				map.put("success", "false");
				map.put("errorMessage", "Unauthorized");
				return map;
			}


			String resp = inventoryManagement.createDigitalStorageItem(username, bucketName, partNumbersAllowed, department, unitOfMeasurement, maxMeasurement, location);
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
			String unitID = jsonNode.get("unitID").asText();
			String departmentName = jsonNode.get("departmentName").asText();

			String username = checkAuthorizedAccess(request, jsonNode);
			if (username == null){
				map.put("success", "false");
				map.put("errorMessage", "Unauthorized");
				return map;
			}

			
			String resp = inventoryManagement.removeDigitalStorageItem(username, departmentName, unitID);
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
	public Map<String, String> removePartsToStorage(HttpServletRequest request, HttpServletResponse resp, @RequestBody String payload) throws SQLException {

		setHeaders(resp);
		System.out.println("Call to /removePartsFromStorage");
		Map<String, String> map = new HashMap<>();

		try {
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String unitID = jsonNode.get("unitID").asText();
			String serialNo = jsonNode.get("serialNo").asText();
			String departmentName = jsonNode.get("departmentName").asText();

			String email = checkAuthorizedAccess(request, jsonNode);
			if (email == null){
				map.put("success", "false");
				map.put("errorMessage", "Unauthorized");
				return map;
			}
			
			String result = inventoryManagement.removePartsToStorage(email, departmentName, unitID, serialNo);
			if (result.equals("success")){
				map.put("success", "true");
				return map;
			}
			map.put("errorMessage", result);
			
		} catch (IOException e) {
			e.printStackTrace();
			map.put("errorMessage", "Incorrect request");
		}

		map.put("success", "false");
		return map;
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
				map.put("success", "false");
				map.put("errorMessage", "Unauthorized");
				return map;
			}

			if (inventoryManagement.userIsAdmin(email)){
				String resp = inventoryManagement.setUpPartNumber(partNumber, hasWeight, weight);
				if (resp.equals("success")){
					map.put("success", "true");
				}
				else{
					map.put("success", "false");
					map.put("errorMessage", resp);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			map.put("success", "false");
			map.put("errorMessage", "Invalid Request");
		}
		return map;
	}
	
	@RequestMapping(value = "/addPartsToStorage")
	@ResponseBody
	public Map<String, String> addPartsToStorage(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload) throws SQLException{
		
		setHeaders(response);
		System.out.println("Call to /addPartsToStorage");

		HashMap<String, String> addItemResp = new HashMap<String, String>();

		try {
			JsonNode jsonNode = new ObjectMapper().readTree(payload);

			String departmentName = jsonNode.get("departmentName").asText(); // was department
			String unitID = jsonNode.get("unitID").asText();
			String serialNo = jsonNode.get("serialNo").asText();
			String partNumber = jsonNode.get("partNo").asText();

			String email = checkAuthorizedAccess(request, jsonNode);
			if (email == null) {
				addItemResp.put("success", "false");
				addItemResp.put("errorMessage", "Unauthorized");
				return addItemResp;
			}

			String result = inventoryManagement.addPartsToStorage(email, departmentName, unitID, serialNo, partNumber);

			if (result.equals("success")){
				addItemResp.put("success", "true");
				return addItemResp;
			}else{
				addItemResp.put("errorMessage", result);
			}
			
		} catch (IOException e) {
			addItemResp.put("errorMessage", "Incorrect request");
			e.printStackTrace();
		}
		
		addItemResp.put("success", "false");
		return addItemResp;
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

			Unit unitcall = inventoryManagement.unitData(unitID, departmentName, email);
			unitResp.put("success", "true");
			Gson gson = new Gson();

			unitResp.put("allowedParts", gson.toJson(unitcall.partNumbersAllowed));
			unitResp.put("items", gson.toJson(unitcall.items));

			return unitResp;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		

	}
	

	
	@RequestMapping(value = "/dashboard")
	@ResponseBody
	public Map<String, String> returnDashboard(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload){
		setHeaders(response);
		System.out.println("Call to /dashboard");
		HashMap<String, String> map = new HashMap<>();
		try{
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String csrf = jsonNode.get("csrf").asText();
			HttpSession session = request.getSession();
			String existingCsrf = session.getAttribute("csrf").toString();

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

	@RequestMapping(value = "/manageUsers")
	@ResponseBody
	public Map<String, String> manageUsers(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload){
		setHeaders(response);
		System.out.println("Call to /manageUsers");
		Map<String, String> map = new HashMap<>();

		try{
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String email = checkAuthorizedAccess(request, jsonNode);
			if (email == null) {
				map.put("success", "false");
				map.put("errorMessage", "Unauthorized Access");
				return map;
			}

			String resp = inventoryManagement.getUsers(email);
			if (resp.startsWith("Failed")){
				map.put("success", "false");
				map.put("errorMessage", resp);
				return map;
			}

			map.put("success", "true");
			map.put("department", resp);
			return map;
		}catch(IOException e){
			e.printStackTrace();
			map.put("errorMessage", "Invalid Request");
		}catch(Exception e){
			e.printStackTrace();
			map.put("errorMessage", "Unknown Error");
		}
		
		map.put("success", "false");
		return map;
	}

	@RequestMapping("/addUserAsRegular")
	@ResponseBody
	public Map<String, String> addUserAsRegular(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload) {
		setHeaders(response);
		System.out.println("Call to /addUserAsRegular");
		Map<String, String> map = new HashMap<>();
		
		try{
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String departmentName = jsonNode.get("departmentName").asText();
			String addingEmail = jsonNode.get("newUserEmail").asText();
			
			String email = checkAuthorizedAccess(request, jsonNode);
			if (email == null){
				map.put("success", "false");
				map.put("errorMessage", "Unauthorized Access");
				return map;
			}

			String resp = inventoryManagement.addRegularUserToDepartment(email, departmentName, addingEmail);

			if (resp.startsWith("SUCCESS")){
				map.put("success", "true");
				map.put("userCreated", "true");
				map.put("password", resp.substring("SUCCESS".length()));
			} else if (resp.equals("Added Existing User")) {
				map.put("success", "true");
				map.put("userCreated", "false");
			} else { 
				map.put("success", "false");
				map.put("errorMessage", resp);
			}

			return map;
		}catch(IOException e){
			map.put("errorMessage", "Invalid Request");
		}catch(Exception e){
			e.printStackTrace();
			map.put("errorMessage", "Unknown Error");
		}
		
		map.put("success", "false");
		return map;
	}

	@RequestMapping("/addUserAsAdmin")
	@ResponseBody
	public Map<String, String> addUserAsAdmin(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload) {
		setHeaders(response);
		System.out.println("Call to /addUserAsAdmin");
		Map<String, String> map = new HashMap<>();
		
		try{
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String departmentName = jsonNode.get("departmentName").asText();
			String addingEmail = jsonNode.get("newUserEmail").asText();
			
			String email = checkAuthorizedAccess(request, jsonNode);
			if (email == null){
				map.put("success", "false");
				map.put("errorMessage", "Unauthorized Access");
				return map;
			}

			String resp = inventoryManagement.addAdminUserToDepartment(email, departmentName, addingEmail);

			if (resp.startsWith("SUCCESS")){
				map.put("success", "true");
				map.put("userCreated", "true");
				map.put("password", resp.substring("SUCCESS".length()));
			} else if (resp.equals("Added Existing User")) {
				map.put("success", "true");
				map.put("userCreated", "false");
			} else { 
				map.put("success", "false");
				map.put("errorMessage", resp);
			}

			return map;
		}catch(IOException e){
			map.put("errorMessage", "Invalid Request");
		}catch(Exception e){
			e.printStackTrace();
			map.put("errorMessage", "Unknown Error");
		}
		
		map.put("success", "false");
		return map;
	}

	@RequestMapping("/addDepartment")
	@ResponseBody
	public Map<String, String> addDepartment(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload) {
		setHeaders(response);
		System.out.println("Call to /addDepartment");
		Map<String, String> map = new HashMap<>();
		
		try{
			JsonNode jsonNode = new ObjectMapper().readTree(payload);
			String departmentName = jsonNode.get("departmentName").asText();
			
			String email = checkAuthorizedAccess(request, jsonNode);
			if (email == null){
				map.put("success", "false");
				map.put("errorMessage", "Unauthorized Access");
				return map;
			}

			String resp = inventoryManagement.addDepartment(email, departmentName);

			if (resp.equals("success")){
				map.put("success", "true");
			} else {
				map.put("success", "false");
				map.put("errorMessage", resp);
			}

			return map;
		}catch(IOException e){
			map.put("errorMessage", "Invalid Request");
		}catch(Exception e){
			e.printStackTrace();
			map.put("errorMessage", "Unknown Error");
		}
		
		map.put("success", "false");
		return map;
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
		System.out.println(csrf);
		System.out.println(currToken);

		if (csrf.equals(currToken)) return email;

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

package com.example.inventory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.inventory.datamodels.DashboardData;
import com.example.inventory.datamodels.Department;
import com.example.inventory.datamodels.Item;
import com.example.inventory.datamodels.PartNumber;
import com.example.inventory.datamodels.Unit;
import com.example.inventory.datamodels.User;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.FirebaseOptions.Builder;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class InventoryManagementApplication {

	private static Firestore db;
	private static BCryptPasswordEncoder bpe = new BCryptPasswordEncoder();
	private static HashMap<String, PartNumber> allParts = new HashMap<>();
	private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private static Pattern pattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*"
		+ "@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");


	public static void main(String[] args) {
		SpringApplication.run(InventoryManagementApplication.class, args);
		openConnection();
		// Gmail gmail = new Gma
	}

	private static void openConnection(){
		try {
			InputStream serviceAccount = (new InventoryManagementApplication()).getClass().getClassLoader().getResourceAsStream("pyrotask-bff53-firebase-adminsdk-4ipf2-7026069435.json");//new FileInputStream(
					//"com/example/inventory/pyrotask-bff53-firebase-adminsdk-4ipf2-7026069435.json");
					
			GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
			FirebaseOptions options = new Builder()
				.setCredentials(credentials)
				.setDatabaseUrl("https://pyrotask-bff53.firebaseio.com")
				.build();
			FirebaseApp.initializeApp(options);
			db = FirestoreClient.getFirestore();
			System.out.println("Connected to Firebase. Ready to handle requests");
			getAllParts();

		}catch(FileNotFoundException e){
			e.printStackTrace();
			System.out.println("FileException");
			
		}catch(IOException e){
			e.printStackTrace();
			System.out.println("IOExeption");
		}
	}

	public User grabUser(String email){
		try{
			User user = db.collection("users").document(email).get().get().toObject(User.class);
			return user;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}	
	}
	public boolean userIsAdmin(String email){
		User user = grabUser(email);
		if (user.admin.size() > 0) return true;
		return false;
	}

	private static void getAllParts(){
		HashMap<String, PartNumber> hs = new HashMap<>();
		try{
			db.collection("parts").get().get().forEach(part -> {
				PartNumber pn = part.toObject(PartNumber.class);
				hs.put(pn.name, pn);
			});
		}catch(Exception e){
			e.printStackTrace();
		}
		lock.writeLock().lock();
		try{
			allParts = hs;
			System.out.println("Parts information received and parsed");
		}finally{
			lock.writeLock().unlock();
		}
		System.out.println("Done with parsing parts phase");
		// getAllPartsSnapshot();
	}

	// private static void getAllPartsSnapshot(){
	// 	af = db.collection("parts").get();
	// 	db.collection("parts").addSnapshotListener(new EventListener<QuerySnapshot>(){
		
	// 		@Override
	// 		public void onEvent(QuerySnapshot value, FirestoreException error) {
	// 			if (error == null) return;

	// 			HashMap<String, PartNumber> hs = new HashMap<>();

	// 			value.forEach(snap -> {
	// 				PartNumber pn = snap.toObject(PartNumber.class);
	// 				hs.put(pn.name, pn);
	// 			});

	// 			lock.writeLock().lock();
	// 			try{
	// 				allParts = hs;
	// 				System.out.println("Parts information updated");
	// 			}finally{
	// 				lock.writeLock().unlock();
	// 			}
	// 			System.out.println("Done with parsing updated parts");
	// 		}
	// 	});
		
	// }
	
	public Boolean authenticateIntoApplication(String username, String password){
		try{
			System.out.println(username);
			User user = db.collection("users").document(username).get().get().toObject(User.class);
			
			if (bpe.matches(password, user.passwordHashed)){
				return true;
			}
			return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public String createDigitalStorageItem(String username,String bucketName, String partNumbersAllowed, String department,
			String unitOfMeasurement, int maxMeasConverted, String location) throws SQLException {
		System.out.println("UnitOfMeasurement:["+unitOfMeasurement+"]");

		User user = grabUser(username);
		if (!user.admin.contains(department)) return "You do not have the privilege for this action";

		if (bucketName.equals("") || partNumbersAllowed.equals("") ||
			department.equals("") || unitOfMeasurement.equals("") ||
			bucketName.equals("null") || partNumbersAllowed.equals("null") ||
			department.equals("null") || unitOfMeasurement.equals("null") ||
			maxMeasConverted == 0 || location.equals("") || location.equals("null")) {
				return "Empty fields encountered. All fields must be filled.";
		}
		String[] parts = partNumbersAllowed.replaceAll("[ ]+", "").split(",");
		//Set<String> acceptedParts = parseAllParts();

		// Need to check if the unit of measurement.
		for (String part: parts){
			if (!allParts.containsKey(part)){
				return "Part Number " + part + " doesn't match";
			}
			if (allParts.get(part).hasWeight != (unitOfMeasurement.equals("weight"))){
				return "Part Number " + part + " doesn't match unit's measurement";
			}
		}

		List<String> l = Arrays.asList(parts);

		DashboardData dd = new DashboardData(department, bucketName, 
			location, unitOfMeasurement, maxMeasConverted, (double)maxMeasConverted, l);

		try{
			db.collection("departments").document(department).collection("units").add(dd).get();
		}catch(Exception e){
			e.printStackTrace();
			return "Failed to add to the database.";
		}
		
		return "success";
	}

 	public String removeDigitalStorageItem(String email, String departmentName, String unitID){
		User user = grabUser(email);
		if (!user.admin.contains(departmentName)){
			return "Unauthorized";
		}
		try{
			db.collection("departments").document(departmentName).collection("units").document(unitID).delete().get();
		}catch(Exception e){
			e.printStackTrace();
			return "Error communicating with the database";
		}
		
		return "success";
	}

	public String setUpPartNumber(String partNumber, boolean trackByWeightConverted, double weightConverted){
		try{
			PartNumber pm = new PartNumber(partNumber, trackByWeightConverted, weightConverted);
			if (allParts.containsKey(partNumber)) return "Part Number already exists";

			db.collection("parts").document(partNumber).set(pm).get();
			getAllParts();
			
		}catch(Exception e){
			e.printStackTrace();
			return "Error communicating with database";
		}
		return "success";
	}

	//curl -H "Content-Type: application/json" --data '{"BucketId":"testDept"}' http://localhost:8080/unit
	public Unit unitData(String unitID, String departmentName, String email){
		try{
			DocumentReference ref = db.collection("departments").document(departmentName);
			
			Department d = ref.get().get().toObject(Department.class);
			if (!d.regulars.contains(email) && !d.admins.contains(email) && email != "admin") return null;
	
			Unit unit = ref.collection("units").document(unitID).get().get().toObject(Unit.class);
			List<Item> items = new ArrayList<>();
			ref.collection("units").document(unitID).collection("items").get().get().forEach(itemSnap -> {
				Item item = itemSnap.toObject(Item.class);
				item.setWeight(allParts.get(item.getPartNo()).weight);
				items.add(item);
			});

			unit.items = items;

			return unit;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
}


	public Map<String, String> gatherDashboardData(String email) {
		User user = grabUser(email);
		System.out.println(user.admin);

		List<Department> admin = new ArrayList<>();
		List<Department> regular = new ArrayList<>();
		Map<String, String> map = new HashMap<>();
		Gson gson = new Gson();


		try{
			user.admin.forEach((dept) -> {
				try{
					Department d = db.collection("departments").document(dept).get().get().toObject(Department.class);
					ArrayList<DashboardData> data = new ArrayList<>();
					db.collection("departments").document(dept).collection("units").get().get().getDocuments().forEach(unit -> {
						DashboardData dd = unit.toObject(DashboardData.class);
						dd.setBucketId(unit.getId());
						data.add(dd);
					});
	
					d.units = data;
					admin.add(d);
				}catch(Exception e){
					e.printStackTrace();
					map.put("success", "false");
					map.put("errorMessage", "Failed to parse Regular depts from database");
					throw new NullPointerException();
				}
				
			});
	
			user.regular.forEach((dept) -> {
				try{
					Department d = db.collection("departments").document(dept).get().get().toObject(Department.class);
					ArrayList<DashboardData> data = new ArrayList<>();
					db.collection("departments").document(dept).collection("units").get().get().getDocuments().forEach(unit -> {
						DashboardData dd = unit.toObject(DashboardData.class);
						dd.setBucketId(unit.getId());
						data.add(dd);
					});
	
					d.units = data;
					regular.add(d);
				}catch(Exception e){
					e.printStackTrace();
					map.put("success", "false");
					map.put("errorMessage", "Failed to parse Regular depts from database");
					throw new NullPointerException();
				}
				
			});
		}catch(NullPointerException e){
			return map;
		}

		map.put("success", "true");
		map.put("adminDepartments", gson.toJson(admin));
		map.put("regularDepartments", gson.toJson(regular));

		return map;
	}

	public String getUsers(String email){
		User user = grabUser(email);
		if (user == null) return "Failed: Failed to get user";

		List<Department> depts = new ArrayList<>();

		try{
			for (String name: user.admin){
				Department department = db.collection("departments").document(name).get().get().toObject(Department.class);
				depts.add(department);
			}
		}catch(Exception e){
			return "Failed: Error communicating with database";
		}

		Gson gson = new Gson();
		return gson.toJson(depts);
	}

	public void createQuickUser(String email, String pass){
		User user = new User(email, bpe.encode(pass), "");
		db.collection("users").document(email).set(user);
	}

	public String removePartsToStorage(String email, String departmentName, String unitID, String serialNo) {
		User user = grabUser(email);
		if (!user.admin.contains(departmentName) && !user.regular.contains(departmentName)){
			return "Unauthorized";
		}

		Unit unit = unitData(unitID, departmentName, email);
		if (unit == null) return "Implementation Error";
		DocumentReference unitRef = db.collection("departments").document(departmentName).collection("units").document(unitID);

		if (unit.hasWeight){
			Item item = null;
			try{
				item = unitRef.collection("items").document("serialNo").get().get().toObject(Item.class);
			}catch(Exception e){
				e.printStackTrace();
				return "Error communicating with database";
			}

			unit.capacity -= item.getWeight();
			try{
				unitRef.set(unit);
			}catch(Exception e){
				e.printStackTrace();
				return "Error communicating with database";
			}
		}else{
			unit.capacity -= 1;
			try{
				unitRef.set(unit);
			}catch(Exception e){
				e.printStackTrace();
				return "Error communicating with database";
			}
		}
		
		try{
			unitRef.collection("items").document(serialNo).delete().get();
		}catch(Exception e){
			return "FATAL: Capacity updated but item not removed";
		}
		return "success";
	}

	public String addPartsToStorage(String email, String departmentName, String unitID, String serialNo, String partNumber) {
		User user = grabUser(email);
		if (!user.admin.contains(departmentName) && !user.regular.contains(departmentName)){
			return "Unauthorized";
		}

		Unit unit = unitData(unitID, departmentName, email);
		if (unit == null) return "Implementation Error";

		if (!unit.partNumbersAllowed.contains(partNumber)){
			return "Part Number not allowed";
		}

		for (Item item : unit.items){
			if (item.getSerialNo().equals(serialNo)){
				return "Item with same serial number already exists";
			}
		}

		if (!allParts.containsKey(partNumber)) return "Implementation Error";

		PartNumber pn = allParts.get(partNumber);

		Item item = new Item(partNumber, serialNo, unit.hasWeight? pn.weight: 0, departmentName, "whatever", unitID);
		DocumentReference ref = db.collection("departments").document(departmentName).collection("units").document(unitID);

		if(pn.hasWeight){
			if (unit.capacity + pn.weight > unit.maxMeasurement) return "Not enough space";
			try{
				ref.collection("items").document(serialNo).set(item).get();
			}catch(Exception e){
				e.printStackTrace();
				return "Error communicating with database";
			}
			unit.capacity += pn.weight;
			try{
				ref.set(unit); // THIS MIGHT NOT WORK
			}catch(Exception e){
				return "FATAL: Was unable to update occupied space in the database";
			}
			
		}else{
			if (unit.capacity == unit.maxMeasurement) return "Not enough space";
			try{
				ref.collection("items").document(serialNo).set(item).get();
			}catch(Exception e){
				e.printStackTrace();
				return "Error communicating with database";
			}
			unit.capacity += 1;
			try{
				ref.set(unit); // THIS MIGHT NOT WORK
			}catch(Exception e){
				return "FATAL: Was unable to update occupied space in the database";
			}
		}
		return "success";
	}


	public String addRegularUserToDepartment(String email, String departmentName, String addingEmail) {
		if (notEmail(addingEmail)) return "Invalid address";


		User user = grabUser(email);
		if (user == null) return "Implementation Error";
		if (!user.admin.contains(departmentName)) return "Unauthorized Action";
		try{
			Department department = db.collection("departments").document(departmentName).get().get().toObject(Department.class);
			if (department.admins.contains(addingEmail) || department.regulars.contains(addingEmail)) return "User already in department";

			// String password = createUser(addingEmail);
			// if (password == null) throw new Exception();
			User newUser = grabUser(addingEmail);
			String password = null;
			if (newUser == null){
				password = createUser(addingEmail);
				if (password == null) throw new Exception();
				newUser = grabUser(addingEmail);
			}

			newUser.regular.add(departmentName);
			department.regulars.add(addingEmail);

			db.collection("departments").document(departmentName).set(department);
			db.collection("users").document(addingEmail).set(newUser);
			

			if (password == null) return "Added Existing User";
			return "SUCCESS" + password;

		}catch(Exception e){
			return "Error communicating with database";
		}
	}

	public String removeRegularUser(String email, String departmentName, String removal){
		User user = grabUser(email);
		if (user == null) return "Implementation Error";
		if (!user.admin.contains(departmentName)) return "Unauthorized Action";

		try{
			Department department = db.collection("departments").document(departmentName).get().get().toObject(Department.class);
			if (!department.regulars.contains(removal)) return "Invalid request";
			User remUser = grabUser(removal);
			remUser.regular.remove(departmentName);
			department.regulars.remove(removal);
			db.collection("departments").document(departmentName).set(department).get();
			db.collection("users").document(removal).set(remUser).get();
			return "success";
		}catch(Exception e){
			e.printStackTrace();
			return "Error communicating with database";
		}
	}

	public String addAdminUserToDepartment(String email, String departmentName, String addingEmail) {
		User user = grabUser(email);
		if (user == null) return "Implementation Error";
		if (!user.admin.contains(departmentName)) return "Unauthorized Action";
		// if (!isSuperUser(email)) return "Unauthorized Action"

		try{
			Department department = db.collection("departments").document(departmentName).get().get().toObject(Department.class);
			if (department.admins.contains(addingEmail)) return "Already an admin";

			// String password = createUser(addingEmail);
			// if (password == null) throw new Exception();
			User newUser = grabUser(addingEmail);
			String password = null;
			if (newUser == null){
				password = createUser(addingEmail);
				if (password == null) throw new Exception();
				newUser = grabUser(addingEmail);
			}

			newUser.admin.add(departmentName);
			newUser.regular.remove(departmentName);
			department.admins.add(addingEmail);
			department.regulars.remove(addingEmail);

			db.collection("departments").document(departmentName).set(department);
			db.collection("users").document(addingEmail).set(newUser);
			

			if (password == null) return "Added Existing User";
			return "SUCCESS" + password;

		}catch(Exception e){
			return "Error communicating with database";
		}
	}

	public String addDepartment(String email, String departmentName){
		if(!isSuperUser(email)) return "Unauthorized Action";
		try{
			if (db.collection("departments").document(departmentName).get().get().exists()){
				return "Department with this name already exists";
			}

			Department department = new Department(departmentName, new ArrayList<>());
			db.collection("departments").document(departmentName).set(department).get();
			User user = db.collection("users").document(email).get().get().toObject(User.class);
			user.admin.add(departmentName);
			db.collection("users").document(email).set(user);
			return "success";
		}catch(Exception e){
			e.printStackTrace();
			return "Error Communicating with database";
		}
	}

	public String changePass(String email, String oldPass, String newPass){
		User user = grabUser(email);
		if (user == null) return "Failed";
		if (!bpe.matches(oldPass, user.passwordHashed)) return "Failed";

		user.passwordHashed = bpe.encode(newPass);
		try{
			db.collection("users").document(email).set(user).get();
		}catch(Exception e){
			e.printStackTrace();
			return "Error communicating with database";
		}
		return "success";
	}

	public String changeName(String email, String name){
		User user = grabUser(email);
		if (user == null) return "Failed";
		user.name = name;
		try{
			db.collection("users").document(email).set(user).get();
		}catch(Exception e){
			e.printStackTrace();
			return "Error communicating with database";
		}
		return "success";
	}


	private String createUser(String email){
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuilder pass = new StringBuilder();
		for (int i = 0; i < 8; i++){
			pass.append(alphabet.charAt(random.nextInt(alphabet.length())));
		}

		String password = pass.toString();
		User user = new User(email, bpe.encode(password), "");
		try{
			db.collection("users").document(email).set(user).get();
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
		return password;
	}

	private boolean isSuperUser(String email){
		if (email.equals("admin")) return true;
		return false;
	}

	private boolean notEmail(String email){
		Matcher matcher = pattern.matcher(email);

		return !matcher.matches();
	}
}






package com.example.inventory;

import com.example.inventory.datamodels.Unit;
import com.example.inventory.datamodels.User;
import com.example.inventory.datamodels.Item;
import com.example.inventory.datamodels.DashboardData;
import com.example.inventory.datamodels.Department;
import com.example.inventory.datamodels.PartNumber;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.FirebaseOptions.Builder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class InventoryManagementApplication {
	private static String connectionUrl = "jdbc:sqlserver://pyro-db.cc5cts2xsvng.us-east-2.rds.amazonaws.com:1433;databaseName=FuzzyDB;user=Fuzzies;password=abcdefg1234567";

	private static Connection con = null;
	private static Firestore db;// = FirestoreClient.getFirestore();
	private static ApiFuture<QuerySnapshot> af;
	private static BCryptPasswordEncoder bpe = new BCryptPasswordEncoder();
	private static HashMap<String, PartNumber> allParts = new HashMap<>();
	private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();


	public static void main(String[] args) {
		SpringApplication.run(InventoryManagementApplication.class, args);
		openConnection();
	}

	private static void openConnection(){
		try {
			InputStream serviceAccount = new FileInputStream(
					"./src/main/java/com/example/inventory/pyrotask-bff53-firebase-adminsdk-4ipf2-7026069435.json");
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

	private static void getAllPartsSnapshot(){
		af = db.collection("parts").get();
		db.collection("parts").addSnapshotListener(new EventListener<QuerySnapshot>(){
		
			@Override
			public void onEvent(QuerySnapshot value, FirestoreException error) {
				if (error == null) return;

				HashMap<String, PartNumber> hs = new HashMap<>();

				value.forEach(snap -> {
					PartNumber pn = snap.toObject(PartNumber.class);
					hs.put(pn.name, pn);
				});

				lock.writeLock().lock();
				try{
					allParts = hs;
					System.out.println("Parts information updated");
				}finally{
					lock.writeLock().unlock();
				}
				System.out.println("Done with parsing updated parts");
			}
		});
		
	}
	
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
			if (allParts.get(part).hasWeight != (unitOfMeasurement == "weight")){
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

	//TODO
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

	public boolean setUpPartNumber(String partNumber, boolean trackByWeightConverted, double weightConverted){
		try{
			PartNumber pm = new PartNumber(partNumber, trackByWeightConverted, weightConverted);
			if (allParts.containsKey(partNumber)) return false;

			db.collection("parts").document(partNumber).set(pm).get();
			getAllParts();
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	//curl -H "Content-Type: application/json" --data '{"BucketId":"testDept"}' http://localhost:8080/unit
	public Unit unitData(String unitID, String departmentName, String email){
		try{
			DocumentReference ref = db.collection("departments").document(departmentName);
			
			Department d = ref.get().get().toObject(Department.class);
			if (!d.regulars.contains(email) && !d.admins.contains(email)) return null;
	
			Unit unit = ref.collection("units").document(unitID).get().get().toObject(Unit.class);
			List<Item> items = new ArrayList<>();
			ref.collection("units").document(unitID).collection("items").get().get().forEach(itemSnap -> {
				items.add(itemSnap.toObject(Item.class));
			});

			unit.items = items;

			return unit;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
}


	public Map<String, String> gatherDashboardData(String email) throws SQLException {
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

	public void createQuickUser(String email, String pass){
		User user = new User(email, bpe.encode(pass), "");
		db.collection("users").document(email).set(user);
	}

	public boolean removePartsToStorage(int bucketIDconverted, String partNumber, String serialNumber) throws SQLException {
		// TODO Auto-generated method stub
		PreparedStatement ps = null;
		ResultSet rs = null;
		//Boolean partLoaded = false;
		PreparedStatement ps2 = null;

		try {
			if (con.isClosed()) openConnection();

			String sql = "SELECT * FROM dbo.Items where BucketID = ? and SerialNumber = ? and PartNumber = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, bucketIDconverted);
			ps.setString(2, serialNumber);
			ps.setString(3,  partNumber);


			rs = ps.executeQuery();
			if(rs.isBeforeFirst()) {
				String sqlDelete = "DELETE FROM dbo.Items where BucketID = ? and SerialNumber = ? and PartNumber = ?";

				ps2 = con.prepareStatement(sqlDelete);
				ps2.setInt(1, bucketIDconverted);
				ps2.setString(2,  serialNumber);
				ps2.setString(3,  partNumber);
				ps2.executeUpdate();
			}

		} catch (SQLException e) {

			e.printStackTrace();
			//partLoaded = false;
			return false;
		} finally {
		

			if(!rs.isClosed()) {
				rs.close();
			}

			if(!ps.isClosed()) {
				ps.close();
			}
			/*
			if(!ps2.isClosed()) {
				ps2.close();
			}
			 */
		}
		return true;
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
			if (item.getSerialNo() == serialNo){
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
				ref.collection("items").add(item).get().get();
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
				ref.collection("items").add(item).get().get();
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
}


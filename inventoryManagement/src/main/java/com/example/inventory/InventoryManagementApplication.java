package com.example.inventory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.example.inventory.datamodels.DashboardData;
import com.example.inventory.datamodels.Department;
import com.example.inventory.datamodels.Item;
import com.example.inventory.datamodels.PartNumber;
import com.example.inventory.datamodels.Unit;
import com.example.inventory.datamodels.User;
import com.google.api.core.ApiFuture;
import com.google.api.services.gmail.Gmail;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QuerySnapshot;
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


	public static void main(String[] args) {
		SpringApplication.run(InventoryManagementApplication.class, args);
		openConnection();
		// Gmail gmail = new Gma
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

<<<<<<< HEAD
		User user = grabUser(username);
		if (!user.admin.contains(department)) return "You do not have the privilege for this action";
=======
	public boolean addPartsToStorage(String username, String csrf, String departmentId, String bucketName, int bucketId, String type, int hasWeight, int serialNo, int partNo, int weight) throws SQLException {
		// TODO Auto-generated method stub
		Connection con = null;
		PreparedStatement ps = null;
		PreparedStatement psInsert = null;
		ResultSet rs = null;
		ResultSet rs2 = null;

		String connectionUrl = "jdbc:sqlserver://pyro-db.cc5cts2xsvng.us-east-2.rds.amazonaws.com:1433;databaseName=FuzzyDB;user=Fuzzies;password=abcdefg1234567";

		try {
			con = DriverManager.getConnection(connectionUrl);

			String sql = "SELECT * FROM dbo.Items where Username = ? and CSRF = ? and DepartmentID = ? and BucketName = ? and BucketID = ? and Type = ? and HasWeight = ? and SerialNo = ? and PartNo = ? and Weight = ?";
			ps = con.prepareStatement(sql);
			//ps.setString(1, itemId);
			ps.setString(1, username);
			ps.setString(2, csrf);
			ps.setString(3, departmentId);
			ps.setString(4, bucketName);
			ps.setInt(5, bucketId);
			ps.setString(6, type);
			ps.setInt(7, hasWeight);
			ps.setInt(8, serialNo);
			ps.setInt(9, partNo);
			ps.setInt(10, weight);

			rs = ps.executeQuery();
			if(rs.isBeforeFirst()) {
				//while(rs.next()) {
					return false;
					//	System.out.println("UserName: " + rs.getString("UserName") + " Password: " + rs.getString("Password") + " Admin: " + rs.getString("Admin"));
				//}

			} else {
				int itemID = 0;
				
				String sqlMaxID = "SELECT max(ItemID) as itemId FROM dbo.Items";
				Statement state = con.createStatement();
				rs2 = state.executeQuery(sqlMaxID);
				if(rs2.isBeforeFirst()) {
					while(rs2.next()) {
						itemID = rs2.getInt("itemId");
					}
				}
				itemID++;
				String sqlInsert = "INSERT INTO dbo.Items(ItemID, Username, CSRF, DepartmentID, BucketName, BucketID, Type, HasWeight, SerialNo, PartNo, Weight) " + 
						"values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				psInsert = con.prepareStatement(sqlInsert);
				psInsert.setInt(1, itemID);
				psInsert.setString(2, username);
				psInsert.setString(3, csrf);
				psInsert.setString(4, departmentId);
				psInsert.setString(5, bucketName);
				psInsert.setInt(6, bucketId);
				psInsert.setString(7, type);
				//psInsert.setBoolean(7, hasWeight);
				if (hasWeight != 0)
					psInsert.setInt(8, 1);
				else
					psInsert.setInt(8, 0);
				psInsert.setInt(9, serialNo);
				psInsert.setInt(10, partNo);
				psInsert.setInt(11, weight);

				psInsert.executeUpdate();
			}
		} catch (SQLException e) {

			e.printStackTrace();
			//partLoaded = false;
			return false;
		} finally {
			if(!con.isClosed() && con != null) {
				con.close();
			}
>>>>>>> 7d6f150e1256b06b05161d3fac390d6ce69f31be

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
<<<<<<< HEAD
		}

		List<String> l = Arrays.asList(parts);

		DashboardData dd = new DashboardData(department, bucketName, 
			location, unitOfMeasurement, maxMeasConverted, (double)maxMeasConverted, l);
=======
		*/
		}

		return true;
	}
	
	public boolean removePartsToStorage(int bucketIDconverted, String partNumber, String serialNumber) throws SQLException {
		// TODO Auto-generated method stub
		// Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		//Boolean partLoaded = false;
		PreparedStatement ps2 = null;
		// String connectionUrl = "jdbc:sqlserver://pyro-db.cc5cts2xsvng.us-east-2.rds.amazonaws.com:1433;databaseName=FuzzyDB;user=Fuzzies;password=abcdefg1234567";

		try {
			// >>>
			// con = DriverManager.getConnection(connectionUrl);
			// ===
			if (con.isClosed()) openConnection();
			// <<<

			String sql = "SELECT * FROM dbo.Items where BucketID = ? and SerialNo = ? and PartNo = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, bucketIDconverted);
			ps.setString(2, serialNumber);
			ps.setString(3,  partNumber);


			rs = ps.executeQuery();
			if(rs.isBeforeFirst()) {
				String sqlDelete = "DELETE FROM dbo.Items where BucketID = ? and SerialNo = ? and PartNo = ?";

				ps2 = con.prepareStatement(sqlDelete);
				ps2.setInt(1, bucketIDconverted);
				ps2.setString(2,  serialNumber);
				ps2.setString(3,  partNumber);
				ps2.executeUpdate();
			}
>>>>>>> 7d6f150e1256b06b05161d3fac390d6ce69f31be

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

<<<<<<< HEAD
		Unit unit = unitData(unitID, departmentName, email);
		if (unit == null) return "Implementation Error";

		if (!unit.partNumbersAllowed.contains(partNumber)){
			return "Part Number not allowed";
		}
=======
	public List<String> addUser(String Email, String FirstName, String LastName, String Password, String Role, String Department) throws SQLException {
		
		List<String> depts = new ArrayList<String>();
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		ResultSet rsQuery = null;
		String sqlInsert = null;
		PreparedStatement psInsert = null;
		int recExists = 0;
		String sql = null;
		String userDept = null;
		int deptExists = 0;
		String connectionUrl = "jdbc:sqlserver://pyro-db.cc5cts2xsvng.us-east-2.rds.amazonaws.com:1433;databaseName=FuzzyDB;user=Fuzzies;password=abcdefg1234567";
		
		try {
			con = DriverManager.getConnection(connectionUrl);
			stmt = con.createStatement();
			String selectSql = "SELECT Email, FirstName, LastName, Password, Role FROM dbo.Login";
			rsQuery = stmt.executeQuery(selectSql);
			if (rsQuery != null) {
				while(rsQuery.next()) {
					if (rsQuery.getString("Email").contentEquals(Email))
						recExists = 1;			
				}
			}else {
				sqlInsert = "INSERT INTO dbo.Login(Email, FirstName, LastName, Password, Role) " + 
						"values(?, ?, ?, ?, ?)";
				psInsert = con.prepareStatement(sqlInsert);
				psInsert.setString(1, Email);
				psInsert.setString(2, FirstName);
				psInsert.setString(3, LastName);
				psInsert.setString(4, Password);
				psInsert.setString(5, Role);
				psInsert.executeUpdate();
				depts.add(Department);
				return depts;
			}
			
			if (recExists == 0) {
				sqlInsert = "INSERT INTO dbo.Login(Email, FirstName, LastName, Password, Role) " + 
						"values(?, ?, ?, ?, ?)";
				psInsert = con.prepareStatement(sqlInsert);
				psInsert.setString(1, Email);
				psInsert.setString(2, FirstName);
				psInsert.setString(3, LastName);
				psInsert.setString(4, Password);
				psInsert.setString(5, Role);
				psInsert.executeUpdate();
				
				sqlInsert = "INSERT INTO dbo.UserDeptJunctionTable(UserEmail, UserDepartment) " + "values(?, ?)";
				psInsert = con.prepareStatement(sqlInsert);
				psInsert.setString(1, Email);
				psInsert.setString(2, Department);
				psInsert.executeUpdate();
				depts.add(Department);
				return depts;
			}else{
				sql = "select UserEmail, UserDepartment from dbo.UserDeptJunctionTable";
				rs = stmt.executeQuery(sql);
				if(rs != null) {
					while(rs.next()) {
						if (rs.getString("UserEmail").contentEquals(Email)) {
							userDept = rs.getString("UserDepartment");
							depts.add(userDept);
							if (rs.getString("UserDepartment").contentEquals(Department)) {
								deptExists = 1;
							}
						}
						//System.out.println(rs.getString("UserDepartment"));
					}
					if (deptExists == 0) {
						sql = "INSERT INTO dbo.UserDeptJunctionTable(UserEmail, UserDepartment) " + "values(?, ?)";
						psInsert = con.prepareStatement(sql);
						psInsert.setString(1, Email);
						psInsert.setString(2, Department);
						psInsert.executeUpdate();
						depts.add(Department);
					}
					//System.out.println(depts);
					return depts;
				}
			}
		}catch (SQLException e) {
		}finally {
			if(!con.isClosed()) {
				con.close();
			}
		}
		return depts;		
	}
	
	public List<DashboardData> gatherDashboardData() throws SQLException {
		// Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		// String connectionUrl = "jdbc:sqlserver://pyro-db.cc5cts2xsvng.us-east-2.rds.amazonaws.com:1433;databaseName=FuzzyDB;user=Fuzzies;password=abcdefg1234567";
>>>>>>> 7d6f150e1256b06b05161d3fac390d6ce69f31be

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
}


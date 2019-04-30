package com.example.inventory;

import com.example.inventory.datamodels.Unit;
import com.example.inventory.datamodels.User;
import com.example.inventory.datamodels.Items;
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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class InventoryManagementApplication {
	private static String connectionUrl = "jdbc:sqlserver://pyro-db.cc5cts2xsvng.us-east-2.rds.amazonaws.com:1433;databaseName=FuzzyDB;user=Fuzzies;password=abcdefg1234567";

	private static Connection con = null;
	private static Firestore db;// = FirestoreClient.getFirestore();
	private static ApiFuture<QuerySnapshot> af;
	private static BCryptPasswordEncoder bpe = new BCryptPasswordEncoder();
	private static HashSet<PartNumber> allParts = new HashSet<>();
	private static HashSet<String> allPartNames = new HashSet<>();
	private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public static void main(String[] args) {
		SpringApplication.run(InventoryManagementApplication.class, args);
		openConnection();
	}

	private static void openConnection(){
		try {
			InputStream serviceAccount = new FileInputStream(
					"/Users/siamabdal-ilah/repos/backend_pyro/inventoryManagement/src/main/java/com/example/inventory/pyrotask-bff53-firebase-adminsdk-4ipf2-7026069435.json");
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

	private static void getAllParts(){
		HashSet<PartNumber> hs = new HashSet<>();
		HashSet<String> names = new HashSet<>();
		try{
			db.collection("parts").get().get().forEach(part -> {
				PartNumber pn = part.toObject(PartNumber.class);
				hs.add(pn);
				names.add(pn.name);
			});
		}catch(Exception e){
			e.printStackTrace();
		}
		lock.writeLock().lock();
		try{
			allParts = hs;
			allPartNames = names;
			System.out.println("Parts information received and parsed");
		}finally{
			lock.writeLock().unlock();
		}
		System.out.println("Done with parsing parts phase");
		System.out.println(allPartNames);
		getAllPartsSnapshot();
	}

	private static void getAllPartsSnapshot(){
		af = db.collection("parts").get();
		db.collection("parts").addSnapshotListener(new EventListener<QuerySnapshot>(){
		
			@Override
			public void onEvent(QuerySnapshot value, FirestoreException error) {
				if (error == null) return;

				HashSet<PartNumber> hs = new HashSet<>();
				HashSet<String> names = new HashSet<>();

				value.forEach(snap -> {
					PartNumber pn = snap.toObject(PartNumber.class);
					hs.add(pn);
					names.add(pn.name);
				});

				lock.writeLock().lock();
				try{
					allParts = hs;
					allPartNames = names;
					System.out.println("Parts information updated");
				}finally{
					lock.writeLock().unlock();
				}
				System.out.println("Done with parsing updated parts");
			}
		});
		
	}
	
	public Boolean authenticateIntoApplication(String username, String password) throws SQLException  {
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

	public Boolean createDigitalStorageItem(String bucketName, String partNumbersAllowed, String department,
			String unitOfMeasurement, int maxMeasConverted, String location) throws SQLException {
		String[] parts = partNumbersAllowed.replaceAll(" ", "").split(",");
		//Set<String> acceptedParts = parseAllParts();

		for (String part: parts){
			if (!allPartNames.contains(part)){
				System.out.println("Part Number doesn't match");
				return false;
			}
		}

		List<String> l = Arrays.asList(parts);
		System.out.println(l);

		DashboardData dd = new DashboardData(department, bucketName, 
			location, unitOfMeasurement, maxMeasConverted, (double)maxMeasConverted, l);
		try{
			System.out.println(dd.partNumbersAllowed);
			//db.collection("departments").document(department).collection("units").add(dd).get();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	public boolean setUpPartNumber(String partNumber, boolean trackByWeightConverted, double weightConverted){
		try{
			PartNumber pm = new PartNumber(partNumber, trackByWeightConverted, weightConverted);
			if (allPartNames.contains(partNumber)) return false;

			db.collection("parts").document(partNumber).set(pm).get();
			// getAllParts();
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	//curl -H "Content-Type: application/json" --data '{"BucketId":"testDept"}' http://localhost:8080/unit
	public Unit unitData(String unitID, String departmentName, String email) throws SQLException{
		try{
			DocumentReference ref = db.collection("departments").document(departmentName);

			Department d = ref.get().get().toObject(Department.class);
			if (!d.regulars.contains(email) && !d.admins.contains(email)) return null;
	
			Unit unit = ref.collection("units").document(unitID).get().get().toObject(Unit.class);
			List<Items> items = new ArrayList<>();
			ref.collection("units").document(unitID).collection("items").get().get().forEach(itemSnap -> {
				items.add(itemSnap.toObject(Items.class));
			});

			unit.items = items;

			return unit;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
}


	public List<Department> gatherDashboardData(String email) throws SQLException {
		User user = grabUser(email);
		System.out.println(user.admin);

		List<Department> dataList = new ArrayList<Department>();

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
				dataList.add(d);
			}catch(Exception e){
				e.printStackTrace();
			}
			
		});

		return dataList;
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

	public boolean addPartsToStorage(String username, String csrf, String department, int unit, String type, int hasWeight, int serialNo, int partNo, int weight) 
	throws SQLException {
		PreparedStatement ps = null;
		PreparedStatement psInsert = null;
		ResultSet rs = null;
		ResultSet rs2 = null;

		try {
			if (con.isClosed()) openConnection();

			String sql = "SELECT * FROM dbo.Items where Username = ? and CSRF = ? and Department = ? and Unit = ? and Type = ? and HasWeight = ? and SerialNo = ? and PartNo = ? and Weight = ?";
			ps = con.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, csrf);
			ps.setString(3, department);
			ps.setInt(4, unit);
			ps.setString(5, type);
			ps.setInt(6, hasWeight);
			ps.setInt(7, serialNo);
			ps.setInt(8, partNo);
			ps.setInt(9, weight);

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
				String sqlInsert = "INSERT INTO dbo.Items(ItemID, Username, CSRF, Department, Unit, Type, HasWeight, SerialNo, PartNo, Weight) " + 
						"values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				psInsert = con.prepareStatement(sqlInsert);
				psInsert.setInt(1, itemID);
				psInsert.setString(2, username);
				psInsert.setString(3, csrf);
				psInsert.setString(4, department);
				psInsert.setInt(5, unit);
				psInsert.setString(6, type);
				//psInsert.setBoolean(7, hasWeight);
				if (hasWeight != 0)
					psInsert.setInt(7, 1);
				else
					psInsert.setInt(7, 0);
				psInsert.setInt(8, serialNo);
				psInsert.setInt(9, partNo);
				psInsert.setInt(10, weight);

				psInsert.executeUpdate();
			}
		} catch (SQLException e) {

			e.printStackTrace();
			//partLoaded = false;
			return false;
		} finally {
			

			if(!rs.isClosed() && rs != null) {
				rs.close();
			}

			if(!ps.isClosed() && ps != null) {
				ps.close();
			}
			/*
			if(!psInsert.isClosed() && psInsert != null) {
				psInsert.close();
			}
			if(!rs2.isClosed() && rs2 != null) {
				rs2.close();
			}
			 */
		}

		return true;
	}

}


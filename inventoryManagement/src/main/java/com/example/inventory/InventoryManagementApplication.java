package com.example.inventory;

import com.example.inventory.datamodels.Unit;
import com.example.inventory.datamodels.User;
import com.example.inventory.datamodels.Items;
import com.example.inventory.datamodels.DashboardData;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.FirebaseOptions.Builder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class InventoryManagementApplication {
	private static String connectionUrl = "jdbc:sqlserver://pyro-db.cc5cts2xsvng.us-east-2.rds.amazonaws.com:1433;databaseName=FuzzyDB;user=Fuzzies;password=abcdefg1234567";

	private static Connection con = null;
	private static Firestore db;
	private static ApiFuture<DocumentSnapshot> af;
	private static BCryptPasswordEncoder bpe;

	public static void main(String[] args) {
		SpringApplication.run(InventoryManagementApplication.class, args);
		openConnection();

		// Use a service account
		// try {
		// 	InputStream serviceAccount = new FileInputStream(
		// 			"/Users/siamabdal-ilah/repos/backend_pyro/inventoryManagement/src/main/java/com/example/inventory/pyrotask-bff53-firebase-adminsdk-4ipf2-7026069435.json");
		// 	GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
		// 	FirebaseOptions options = new Builder()
		// 		.setCredentials(credentials)
		// 		.setDatabaseUrl("https://pyrotask-bff53.firebaseio.com")
		// 		.build();
		// 	FirebaseApp.initializeApp(options);

		// 	// FirestoreOptions options = FirebaseOptions.newBuilder()
		// 	// 	.setCredentials(credentials)
		// 	// 	.setTimestampsInSnapshotsEnabled(true).build();

		// 	// db = options.getService();

		// 	db = FirestoreClient.getFirestore();
		// 	getAllParts();
		// 	bpe = new BCryptPasswordEncoder();
		// }catch(FileNotFoundException e){
		// 	e.printStackTrace();
		// 	System.out.println("FileException");
			
		// }catch(IOException e){
		// 	e.printStackTrace();
		// 	System.out.println("IOExeption");
		// }
	}

	private static void getAllParts(){
		af = db.collection("parts").document("parts").get();
	}

	private static Set<String> parseAllParts(){
		try{
			DocumentSnapshot ds = af.get();
			String[] res = ds.toObject(String[].class);
			HashSet<String> hs = new HashSet<>();
			for (String r: res){
				hs.add(r);
			}
			return hs;
		} catch(Exception e){
			return null;
		}
		
	}

	private static void openConnection() /*throws SQLException*/{
		try{
			con = DriverManager.getConnection(connectionUrl);
		}catch(Exception e){
			e.printStackTrace();;
		}
	}

	public Boolean authenticateIntoApplication(String username, String password) throws SQLException  {
		boolean authenticated = false;
		try{
			User user = db.collection("users").document(username).get().get().toObject(User.class);
			if (bpe.matches(password, user.passwordHashed)){
				return true;
			}
			return false;
		}catch(Exception e){
			return false;
		}
	}

	public Boolean createDigitalStorageItem(String bucketName, String partNumbersAllowed, String department,
			String unitOfMeasurement, int maxMeasConverted, String location) throws SQLException {
		//Get next primary key to use for ID.

		String[] parts = partNumbersAllowed.replaceAll(" ", "").split(",");
		Set<String> acceptedParts = parseAllParts();

		for (String part: parts){
			if (!acceptedParts.contains(part)){
				return false;
			}
		}

		DashboardData dd = new DashboardData(department, bucketName, 
			location, unitOfMeasurement, maxMeasConverted, 0.0, null);
		
		ApiFuture<DocumentReference> ap = db.collection("departments").document(department).collection("units").add(dd);
		try{
			ap.get();
			return true;
		}catch(Exception e){
			return false;
		}
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

	public boolean setUpPartNumber(String partNumber, int trackByWeightConverted, double weightConverted) throws SQLException {
		PreparedStatement ps = null;
		PreparedStatement psInsert = null;
		ResultSet rs = null;

		try {	
			if (con.isClosed()) openConnection();

			String sql = "SELECT * FROM dbo.PartNumbers where PartNumber = ? and TrackByWeight = ? and Weight = ?";
			ps = con.prepareStatement(sql);
			ps.setString(1, partNumber);
			ps.setInt(2, trackByWeightConverted);
			ps.setDouble(3,  weightConverted);

			rs = ps.executeQuery();
			if(rs.isBeforeFirst()) {

				return false;

			} else {

				String sqlInsert = "INSERT INTO dbo.PartNumbers(PartNumber, TrackByWeight, Weight) " + 
						"values(?, ?, ?)";
				psInsert = con.prepareStatement(sqlInsert);
				psInsert.setString(1, partNumber);
				if (trackByWeightConverted != 0)
					psInsert.setInt(2, 1);
				else
					psInsert.setInt(2, 0);
				psInsert.setDouble(3, weightConverted);

				psInsert.executeUpdate();

			}
		} catch (SQLException e) {

			e.printStackTrace();
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
			 */
		}
		return true;
	}

	//curl -H "Content-Type: application/json" --data '{"BucketId":"testDept"}' http://localhost:8080/unit
	public Unit unitData(Integer bucketID) throws SQLException{
		
		Unit unitObject = new Unit(false, null);
		ResultSet rs = null;
		ResultSet rsConfirm = null;
		
		try {
			if (con.isClosed()) openConnection();

			String sqlConfirm = "select * from dbo.Buckets where UnitOfMeasurement = 'pounds' AND BucketID = ?"; //" UnitOfMeasurement = 'pounds' AND BucketID = '$[bucketID]'";
			PreparedStatement ps = null;
			ps = con.prepareStatement(sqlConfirm);
			ps.setString(1, bucketID.toString());

			rsConfirm = ps.executeQuery(); // CHANGED some parts 
			
			if (rsConfirm != null) {
				unitObject.setHasWeight(true);
			}else {
				unitObject.setHasWeight(false);
				//return unitObject;
			}
			
			Statement stmt = con.createStatement();
			List<Items> itemRecords = new ArrayList<Items>();
			String sql = "select BucketID, SerialNo, PartNo, Weight from dbo.Items";
			System.out.println(bucketID);
			System.out.println(unitObject);
			rs = stmt.executeQuery(sql);
			System.out.println(rs.getFetchSize());
			
			if(rs != null) {
				while(rs.next()) {
					//if (rs.getString("BucketID").contentEquals(bucketID)) {
					System.out.println(1);
					if (rs.getInt("BucketID") == bucketID) {
						Items aRecord = new Items();	
						
						String partNo = rs.getString("PartNo");
						String serialNo = rs.getString("SerialNo");
						int weight = rs.getInt("Weight");
						String department = rs.getString("DepartmentID");
						int hasWeight = rs.getInt("HasWeight");
						int unitId = rs.getInt("BucketID");
						
						aRecord.setPartNo(partNo);
						aRecord.setSerialNo(serialNo);
						aRecord.setWeight(Integer.toString(weight));
						aRecord.setDepartment(department);
						aRecord.setHasWeight(Integer.toString(hasWeight));
						aRecord.setUnit(Integer.toString(unitId));
						
						

						itemRecords.add(aRecord);
					}
					//System.out.println("PartNo: " + rs.getString("PartNo") + " SerialNo: " + rs.getString("SerialNo") + " Weight: " + rs.getInt("Weight"));
				}
				unitObject.setItems(itemRecords);
				System.out.println(unitObject);
			}
			return unitObject;
			
		} catch (SQLException e) {
		}

		return unitObject;
}


	public List<DashboardData> gatherDashboardData() throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		PreparedStatement ps = null;


		List<DashboardData> dataList = new ArrayList<DashboardData>();

		try {

			String sql = "select BucketID, DepartmentID, BucketName, Location, UnitOfMeasurement, MaxMeasurement from dbo.buckets";
			stmt = con.createStatement();

			rs = stmt.executeQuery(sql);
			if(rs != null) {
				while(rs.next()) {
					DashboardData dashboard = new DashboardData();	

					int bucketId = rs.getInt("BucketID");
					String departmentId = rs.getString("DepartmentID");
					String bucketName = rs.getString("BucketName");
					String location = rs.getString("Location");
					String unitOfMeas = rs.getString("UnitOfMeasurement");
					int maxMeasurement = rs.getInt("MaxMeasurement");

					dashboard.setBucketId(bucketId);
					dashboard.setDepartmentId(departmentId);
					dashboard.setUnitName(bucketName);
					dashboard.setLocation(location);
					dashboard.setUnitOfMeasurement(unitOfMeas);
					dashboard.setMaxMeasurement(maxMeasurement);

					dataList.add(dashboard);

					//	System.out.println("UserName: " + rs.getString("UserName") + " Password: " + rs.getString("Password") + " Admin: " + rs.getString("Admin"));
				}
				if(!dataList.isEmpty()) {
					for(DashboardData dashboardItem : dataList) {
						String sqlCapacity = "";
						if(("pounds").equals(dashboardItem.getUnitOfMeasurement())) {	
							sqlCapacity = "select sum(weight) as total from dbo.items where BucketID = ?";
						} else {
							sqlCapacity = "select count(*) as total from dbo.items where BucketID = ?";
						}
						ps = con.prepareStatement(sqlCapacity);
						ps.setInt(1, dashboardItem.getUnitID());
						rs = ps.executeQuery();
						if(rs.isBeforeFirst()) {
							while(rs.next()) {
								int total = rs.getInt("total");
								Double convTotal = Double.valueOf(total);
								Double capacity = (convTotal/dashboardItem.getMaxMeasurement()) * 100.0;
								dashboardItem.setCapacity(capacity);
							}
						}
					}
				}
			} 
		} catch (SQLException e) {


		}
		finally {
			// if(!con.isClosed()) {
			// 	con.close();
			// }
		}
		return dataList;
	}

	public void createQuickUser(String email, String pass){
		User user = new User(email, bpe.encode(pass), "");
		System.out.println(user.toString());
		db.collection("users").add(user).addListener(new Runnable(){
		
			@Override
			public void run() {
				System.out.println("Adding completed");
			}
		}, new ForkJoinPool());
		System.out.println("Adding user to Firebase");

	}
}


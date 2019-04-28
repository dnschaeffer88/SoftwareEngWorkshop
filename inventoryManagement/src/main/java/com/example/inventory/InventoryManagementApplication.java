package com.example.inventory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InventoryManagementApplication {
	private static String connectionUrl = "jdbc:sqlserver://pyro-db.cc5cts2xsvng.us-east-2.rds.amazonaws.com:1433;databaseName=FuzzyDB;user=Fuzzies;password=abcdefg1234567";

	private static Connection con = null;

	public static void main(String[] args) {
		SpringApplication.run(InventoryManagementApplication.class, args);
		openConnection();
	}

	private static void openConnection() /*throws SQLException*/{
		try{
			con = DriverManager.getConnection(connectionUrl);
		}catch(Exception e){
			e.printStackTrace();;
		}/*finally{
			if (!con.isClosed()){
				con.close();
			}
		}*/
	}

	public Boolean authenticateIntoApplication(String username, String password) throws SQLException  {
		boolean authenticated = false;
		//Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		//String connectionUrl = "jdbc:sqlserver://pyro-db.cc5cts2xsvng.us-east-2.rds.amazonaws.com:1433;databaseName=?;user=?;password=?";
		//String connectionUrl = "jdbc:sqlserver://pyro-db.cc5cts2xsvng.us-east-2.rds.amazonaws.com:1433;databaseName=FuzzyDB;user=Fuzzies;password=abcdefg1234567";

		try {
			// >>>
			// con = DriverManager.getConnection(connectionUrl);
			// ===
				if (con.isClosed()) openConnection();
			// <<<

			String sql = "SELECT * FROM dbo.Login where UserName = ? and password = ?";
			ps = con.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, password);

			rs = ps.executeQuery();
			if(rs != null) {
				while(rs.next()) {
					//Test
					authenticated = true;
					//	System.out.println("UserName: " + rs.getString("UserName") + " Password: " + rs.getString("Password") + " Admin: " + rs.getString("Admin"));
				}

			} 
		} catch (SQLException e) {

			e.printStackTrace();
			authenticated = false;
		} finally {
			// if(!con.isClosed()) {
			// 	con.close();
			// }

			if(!rs.isClosed()) {
				rs.close();
			}

			if(!ps.isClosed()) {
				ps.close();
			}

		}



		return authenticated;
	}

	public Boolean createDigitalStorageItem(String bucketName, String partNumbersAllowed, String department,
			String unitOfMeasurement, int maxMeasConverted, String location) throws SQLException {

		int bucketKey = 0;
		// Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		//String connectionUrl = "jdbc:sqlserver://pyro-db.cc5cts2xsvng.us-east-2.rds.amazonaws.com:1433;databaseName=?;user=?;password=?";
		// String connectionUrl = "jdbc:sqlserver://pyro-db.cc5cts2xsvng.us-east-2.rds.amazonaws.com:1433;databaseName=FuzzyDB;user=Fuzzies;password=abcdefg1234567";
		try {
			// >>>
			// con = DriverManager.getConnection(connectionUrl);
			// ===
			if (con.isClosed()) openConnection();
			// <<<
			
			String sql = "SELECT max(BucketID) as bucketId FROM dbo.Buckets";
			Statement state = con.createStatement();
			rs = state.executeQuery(sql);
			while(rs.next()) {
				bucketKey = rs.getInt("BucketId");
			}
			bucketKey++;

			//insert new Storage item into the buckets table.
			String insertSql = "insert into dbo.Buckets(BucketID, BucketName, PartNumbersAllowed, DepartmentID, UnitOfMeasurement, MaxMeasurement, Location) " +
					"VALUES(?, ?, ?, ?, ?, ?, ?)";
			ps = con.prepareStatement(insertSql);
			ps.setInt(1, bucketKey);
			ps.setString(2, bucketName);
			ps.setString(3, partNumbersAllowed);
			ps.setString(4, department);
			ps.setString(5, unitOfMeasurement);
			ps.setInt(6, maxMeasConverted);
			ps.setString(7, location);

			ps.executeUpdate();


		} 
		catch (SQLException e) {

			e.printStackTrace();
			return false;
		} 
		finally {
			
			if(!rs.isClosed()) {
				rs.close();
			}

			if(!ps.isClosed()) {
				ps.close();
			}
		}

		return true;
	}


	public boolean addPartsToStorage(String username, String csrf, String department, int unit, String type, int hasWeight, int serialNo, int partNo, int weight) throws SQLException {
		// TODO Auto-generated method stub
		// Connection con = null;
		PreparedStatement ps = null;
		PreparedStatement psInsert = null;
		ResultSet rs = null;
		ResultSet rs2 = null;

		// String connectionUrl = "jdbc:sqlserver://pyro-db.cc5cts2xsvng.us-east-2.rds.amazonaws.com:1433;databaseName=FuzzyDB;user=Fuzzies;password=abcdefg1234567";

		try {
			// >>>
			// con = DriverManager.getConnection(connectionUrl);
			// ===
			if (con.isClosed()) openConnection();
			// <<<

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
		// TODO Auto-generated method stub
		// Connection con = null;
		PreparedStatement ps = null;
		PreparedStatement psInsert = null;
		ResultSet rs = null;

		// String connectionUrl = "jdbc:sqlserver://pyro-db.cc5cts2xsvng.us-east-2.rds.amazonaws.com:1433;databaseName=FuzzyDB;user=Fuzzies;password=abcdefg1234567";

		try {
			// >>>
			// con = DriverManager.getConnection(connectionUrl);
			// ===
			if (con.isClosed()) openConnection();
			// <<<

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
	public Unit unitData(int bucketID) throws SQLException{
		
		Unit unitObject = new Unit(false, null);
		// Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		ResultSet rsConfirm = null;
		//PreparedStatement ps = null;
		// String connectionUrl = "jdbc:sqlserver://pyro-db.cc5cts2xsvng.us-east-2.rds.amazonaws.com:1433;databaseName=FuzzyDB;user=Fuzzies;password=abcdefg1234567";
		
		try {
			// >>>
			// con = DriverManager.getConnection(connectionUrl);
			// ===
			if (con.isClosed()) openConnection();
			// <<<

			stmt = con.createStatement();
			//System.out.println(bucketID);
			String sqlConfirm = "select * from dbo.Buckets where UnitOfMeasurement = 'pounds' AND BucketID = '$[bucketID]'";
			rsConfirm = stmt.executeQuery(sqlConfirm);
			
			if (rsConfirm != null) {
				unitObject.setHasWeight(true);
			}else {
				unitObject.setHasWeight(false);
				//return unitObject;
			}
			
			List<Items> itemRecords = new ArrayList<Items>();
			String sql = "select BucketID, SerialNo, PartNo, Weight from dbo.Items";
			rs = stmt.executeQuery(sql);
			if(rs != null) {
				while(rs.next()) {
					//if (rs.getString("BucketID").contentEquals(bucketID)) {
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
			}
			return unitObject;
			
		} catch (SQLException e) {
		}finally {
			// if(!con.isClosed()) {
			// 	con.close();
			// }
		}

		return unitObject;
}


	public List<DashboardData> gatherDashboardData() throws SQLException {
		// Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		// String connectionUrl = "jdbc:sqlserver://pyro-db.cc5cts2xsvng.us-east-2.rds.amazonaws.com:1433;databaseName=FuzzyDB;user=Fuzzies;password=abcdefg1234567";


		List<DashboardData> dataList = new ArrayList<DashboardData>();

		try {
			// >>>
			// con = DriverManager.getConnection(connectionUrl);
			// ===
			if (con.isClosed()) openConnection();
			// <<<

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


}


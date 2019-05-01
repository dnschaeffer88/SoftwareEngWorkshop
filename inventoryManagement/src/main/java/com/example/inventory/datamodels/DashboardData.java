package com.example.inventory.datamodels;

import java.util.List;

public class DashboardData {

	public String unitID;
	public String departmentName;
	public String unitName;
	public String location;
	public String unitOfMeasurement;
	public int maxMeasurement;
	public Double capacity;
	public List<String> partNumbersAllowed;
	
	
	
	public DashboardData(String unitID, String departmentName, String unitName, String location,
			String unitOfMeasurement, int maxMeasurement, Double capacity, List<String> partNumbersAllowed) {
		super();
		this.unitID = unitID;
		this.departmentName = departmentName;
		this.unitName = unitName;
		this.location = location;
		this.unitOfMeasurement = unitOfMeasurement;
		this.maxMeasurement = maxMeasurement;
		this.capacity = capacity;
		this.partNumbersAllowed = partNumbersAllowed;

	}
	
	public DashboardData(String departmentId, String unitName, String location,
	String unitOfMeasurement, int maxMeasurement, Double capacity, List<String> partNumbersAllowed){
		super();
		this.unitID = "";
		this.departmentName = departmentId;
		this.unitName = unitName;
		this.location = location;
		this.unitOfMeasurement = unitOfMeasurement;
		this.maxMeasurement = maxMeasurement;
		this.capacity = 0.0;
		this.partNumbersAllowed = partNumbersAllowed;
	}

	// @Override
	// public String toString() {
		
	// }

	public DashboardData() {}

	public String getUnitID() {
		return unitID;
	}

	public void setBucketId(String unitID) {
		this.unitID = unitID;
	}
}
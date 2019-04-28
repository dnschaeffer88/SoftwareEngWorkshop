package com.example.inventory.datamodels;

import java.util.List;

public class DashboardData {

	private int unitID;
	private String departmentId;
	private String unitName;
	private String location;
	private String unitOfMeasurement;
	private int maxMeasurement;
	private Double capacity;
	public List<String> partNumbersAllowed;
	
	
	
	public DashboardData(int unitID, String departmentId, String unitName, String location,
			String unitOfMeasurement, int maxMeasurement, Double capacity, List<String> partNumbersAllowed) {
		super();
		this.unitID = unitID;
		this.departmentId = departmentId;
		this.unitName = unitName;
		this.location = location;
		this.unitOfMeasurement = unitOfMeasurement;
		this.maxMeasurement = maxMeasurement;
		this.capacity = capacity;

	}
	
	public DashboardData(String departmentId, String unitName, String location,
	String unitOfMeasurement, int maxMeasurement, Double capacity, List<String> partNumbersAllowed){
		super();
		this.unitID = 0;
		this.departmentId = departmentId;
		this.unitName = unitName;
		this.location = location;
		this.unitOfMeasurement = unitOfMeasurement;
		this.maxMeasurement = maxMeasurement;
		this.capacity = capacity;
	}

	// @Override
	// public String toString() {
		
	// }

	public DashboardData() {}

	public int getUnitID() {
		return unitID;
	}

	public void setBucketId(int unitID) {
		this.unitID = unitID;
	}

	public String getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(String departmentId) {
		this.departmentId = departmentId;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getUnitOfMeasurement() {
		return unitOfMeasurement;
	}

	public void setUnitOfMeasurement(String unitOfMeasurement) {
		this.unitOfMeasurement = unitOfMeasurement;
	}

	public int getMaxMeasurement() {
		return maxMeasurement;
	}

	public void setMaxMeasurement(int maxMeasurement) {
		this.maxMeasurement = maxMeasurement;
	}

	public Double getCapacity() {
		return capacity;
	}

	public void setCapacity(Double capacity) {
		this.capacity = capacity;
	}
	
	
	
	
}

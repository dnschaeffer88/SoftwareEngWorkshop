package com.example.inventory;

public class Items {
	private String partNo;
	private String serialNo;
	private String weight;
	private String department;
	private String hasWeight;
	private String unit;
	
	public Items(String partNo, String serialNo, String weight, String department, String hasWeight, String unit) {
		super();
		this.partNo = partNo;
		this.serialNo = serialNo;
		this.weight = weight;
		this.department = department;
		this.hasWeight = hasWeight;
		this.unit = unit;
	}
	
	public Items() {
		
	}
	
	public String getPartNo() {
		return partNo;
	}

	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getHasWeight() {
		return hasWeight;
	}

	public void setHasWeight(String hasWeight) {
		this.hasWeight = hasWeight;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	
}

    
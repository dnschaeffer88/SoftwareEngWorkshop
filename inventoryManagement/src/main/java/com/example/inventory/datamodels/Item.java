package com.example.inventory.datamodels;
import java.lang.reflect.*;
import java.util.*;

public class Item {
	private String partNo;
	private String serialNo;
	private double weight;
	private String department;
	private String hasWeight;
	private String unit;
	
	public Item(String partNo, String serialNo, double weight, String department, String hasWeight, String unit) {
		super();
		this.partNo = partNo;
		this.serialNo = serialNo;
		this.weight = weight;
		this.department = department;
		this.hasWeight = hasWeight;
		this.unit = unit;
	}



	@Override
	public String toString(){
		String res = "{";

		for (Field f: this.getClass().getDeclaredFields()){
			res += f.getName() + ":";
			try{
				res += f.get(this).toString();
			}catch(IllegalAccessException e){
				res += "";
			}
			res += ",";
		}
		res += "}";

		return res;

		// return new JsonObject(this.toMap());
	}

	
	// public Map<String, String> toMap() {
	// 	Map<String, String> map = new HashMap<>();
	// 	for (Field f: this.getClass().getDeclaredFields()){
	// 		try{
	// 			map.put(f.getName(), f.get(this).toString());
	// 		}catch(IllegalAccessException e){
	// 			map.put(f.getName(), "");
	// 		}
	// 	}

	// 	return map;
	// }
	
	public Item() {
		
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

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
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

    
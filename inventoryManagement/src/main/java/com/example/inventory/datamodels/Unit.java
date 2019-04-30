package com.example.inventory.datamodels;

import java.util.List;
import java.lang.reflect.*;

public class Unit {
	public String unitID;
	public String departmentName;
	public String unitName;
	public String location;
	public String unitOfMeasurement;
	public int maxMeasurement;
	public Double capacity;
	public List<String> partNumbersAllowed;

	public boolean hasWeight;
	public List<Items> items;

	public Unit(String unitID, String departmentName, String unitName, String location,
			String unitOfMeasurement, int maxMeasurement, Double capacity, List<String> partNumbersAllowed) {
		super();
		this.unitID = unitID;
		this.departmentName = departmentName;
		this.unitName = unitName;
		this.location = location;
		this.unitOfMeasurement = unitOfMeasurement;
		this.maxMeasurement = maxMeasurement;
		this.capacity = 0.0;

		hasWeight = unitOfMeasurement == "weight" ? true : false;

	}
	
	// public Unit(boolean hasWeight, List<Items> items) {
	// 	super();
	// 	this.hasWeight = hasWeight;
	// 	this.items = items;
	// 	this.unitID = "";
	// }

	// public Unit(boolean hasWeight, List<Items> items, String ID){
	// 	super();
	// 	this.hasWeight = hasWeight;
	// 	this.items = items;
	// 	this.unitID = ID;
	// }

	public Unit(){}

	@Override
	public String toString() {
		String res = "{";
		for (Field f: this.getClass().getDeclaredFields()){
			if (f.getName() == "items"){
				res += "items: [";
				for (Items item: items){
					res += item.toString() + ",";
				}
				res += " ], ";
			}else if (f.getName() == "partNumbersAllowed"){
				res += "partNumbersAllowed: [";
				for (String number: partNumbersAllowed){
					res += number.toString() + ",";
				}
				res += " ], ";
			}
			
			else{
				res += f.getName() + ":";
				try{
					res += f.get(this).toString();
				}catch(IllegalAccessException e){
					res += "";
				}
				res += ",";
			}
		}

		res += "}";

		return res;
	}	
}
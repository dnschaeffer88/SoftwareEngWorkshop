package com.example.inventory.datamodels;

// THIS CLASS IS NOT USED ANYWHERE

public class StorageUnit {

	private String name;
	private String unitOfMeassure;
	private double maxWeight;
	private int maxNumOfItems;
	private String location;
	
	public StorageUnit(String name, String unitOfMeassure, double maxWeight, int maxNumOfItems, String location) {
		super();
		this.name = name;
		this.unitOfMeassure = unitOfMeassure;
		this.maxWeight = maxWeight;
		this.maxNumOfItems = maxNumOfItems;
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnitOfMeassure() {
		return unitOfMeassure;
	}

	public void setUnitOfMeassure(String unitOfMeassure) {
		this.unitOfMeassure = unitOfMeassure;
	}

	public double getMaxWeight() {
		return maxWeight;
	}

	public void setMaxWeight(double maxWeight) {
		this.maxWeight = maxWeight;
	}

	public int getMaxNumOfItems() {
		return maxNumOfItems;
	}

	public void setMaxNumOfItems(int maxNumOfItems) {
		this.maxNumOfItems = maxNumOfItems;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	
}

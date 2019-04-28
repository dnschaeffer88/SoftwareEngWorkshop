package com.example.inventory.datamodels;

import java.util.List;
import java.lang.reflect.*;

public class Unit {
	private boolean hasWeight;
	private List<Items> items;
	private String unitID;
	
	public Unit(boolean hasWeight, List<Items> items) {
		super();
		this.hasWeight = hasWeight;
		this.items = items;
		this.unitID = "";
	}

	public Unit(boolean hasWeight, List<Items> items, String ID){
		super();
		this.hasWeight = hasWeight;
		this.items = items;
		this.unitID = ID;
	}

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
			}else{
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

	public boolean isHasWeight() {
		return hasWeight;
	}

	public void setHasWeight(boolean hasWeight) {
		this.hasWeight = hasWeight;
	}

	public List<Items> getItems() {
		return items;
	}

	public void setItems(List<Items> items) {
		this.items = items;
	}
	
	
}
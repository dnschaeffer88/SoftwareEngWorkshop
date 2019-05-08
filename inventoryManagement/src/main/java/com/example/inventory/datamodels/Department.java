package com.example.inventory.datamodels;

import java.util.*;

public class Department{
  public String departmentName;
  public ArrayList<String> admins;
  public ArrayList<String> regulars;
  public ArrayList<DashboardData> units;

  public Department(String name, ArrayList<DashboardData> units){
    this.departmentName = name;
    this.units = units;
  }
  
  public Department(String name, ArrayList<DashboardData> units, ArrayList<String> admins, ArrayList<String> regulars){
    this.departmentName = name;
    this.units = units;
    this.admins = admins;
    this.regulars = regulars;
  }

  public Department(){
    this.admins = new ArrayList<>();
    this.regulars = new ArrayList<>();
  }

  /**
   * @return the name
   */
  public String getName() {
    return departmentName;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.departmentName = name;
  }

  /**
   * @return the admins
   */
  public ArrayList<String> getAdmins() {
    return admins;
  }

  /**
   * @param admins the admins to set
   */
  public void setAdmins(ArrayList<String> admins) {
    this.admins = admins;
  }

  /**
   * @return the regulars
   */
  public ArrayList<String> getRegulars() {
    return regulars;
  }

  /**
   * @param regulars the regulars to set
   */
  public void setRegulars(ArrayList<String> regulars) {
    this.regulars = regulars;
  }

  /**
   * @return the units
   */
  public ArrayList<DashboardData> getUnits() {
    return units;
  }

  /**
   * @param units the units to set
   */
  public void setUnits(ArrayList<DashboardData> units) {
    this.units = units;
  }

}
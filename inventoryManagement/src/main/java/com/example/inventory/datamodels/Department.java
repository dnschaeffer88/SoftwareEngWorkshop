package com.example.inventory.datamodels;

import java.util.*;

public class Department{
  public String name;
  public ArrayList<User> admin;
  public ArrayList<User> regular;
  public ArrayList<DashboardData> units;

  public Department(String name, ArrayList<DashboardData> units){
    this.name = name;
    this.units = units;
  }

}
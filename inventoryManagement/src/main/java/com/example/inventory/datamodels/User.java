package com.example.inventory.datamodels;
import java.util.*;


public class User{
  public String email;
  public String passwordHashed;
  public String name;
  public ArrayList<String> admin;
  public ArrayList<String> regular;

  public User(String email, String passwordHashed, String name, ArrayList<String> admin, ArrayList<String> regular){
    this.email = email;
    this.passwordHashed = passwordHashed;
    this.name = name;
    this.admin = admin;
    this.regular = regular;
  }

  public User(String email, String passwordHashed, String name){
    this.email = email;
    this.passwordHashed = passwordHashed;
    this.name = name;
    this.admin = new ArrayList<>();
    this.regular = new ArrayList<>();
  }
}
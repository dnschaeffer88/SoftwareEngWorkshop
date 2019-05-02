package com.example.inventory.datamodels;


public class PartNumber{
  public String name;
  public boolean hasWeight;
  public double weight;

  public PartNumber(String name, boolean hasWeight, double weight){
    this.name = name;
    this.hasWeight = hasWeight;
    this.weight = weight;
  }

  public PartNumber(){}

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (hasWeight ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    long temp;
    temp = Double.doubleToLongBits(weight);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PartNumber other = (PartNumber) obj;
    if (hasWeight != other.hasWeight)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (Double.doubleToLongBits(weight) != Double.doubleToLongBits(other.weight))
      return false;
    return true;
  }
}
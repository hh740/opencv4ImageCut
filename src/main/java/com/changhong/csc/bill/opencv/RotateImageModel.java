package com.changhong.csc.bill.opencv;

public class RotateImageModel {
	private String name = null;
	private double rotation = 0d;
	
	public RotateImageModel() {}
	
	public RotateImageModel(String name, double rotation) {
		this.name = name;
		this.rotation = rotation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getRotation() {
		return rotation;
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
	}
	
	@Override
    public String toString() {
        return new StringBuilder().
                append("{").
                append("name:").append(this.name).append(",").
                append("rotation:").append(this.rotation).
                append("}").toString();
    }
	
}

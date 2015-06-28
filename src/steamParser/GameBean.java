package steamParser;

import java.io.Serializable;

public class GameBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int appid;
	private String name = null;
	private int positive;
	private int negative;
	
	public int getAppid() {
		return appid;
	}
	public void setAppid(int appid) {
		this.appid = appid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPositive() {
		return positive;
	}
	public void setPositive(int positive) {
		this.positive = positive;
	}
	public int getNegative() {
		return negative;
	}
	public void setNegative(int negative) {
		this.negative = negative;
	}
	public double getRating() {
		return rating;
	}
	public void setRating(double rating) {
		this.rating = rating;
	}
	private double rating;
	
	
	
}

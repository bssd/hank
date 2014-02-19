package uk.co.bssd.hank.datetime;

import java.util.concurrent.TimeUnit;

/**
 * Represents a measure of time, i.e. a quantity and a unit, e.g. 10 seconds.
 * 
 * @author andystewart
 */
public class Time {

	public static Time seconds(int seconds) {
		return new Time(seconds, TimeUnit.SECONDS);
	}
	
	private int quantity;
	private TimeUnit unit;
	
	public Time(int quantity, TimeUnit unit) {
		this.quantity = quantity;
		this.unit = unit;
	}
	
	public int quantity() {
		return this.quantity;
	}
	
	public TimeUnit unit() {
		return this.unit;
	}
}
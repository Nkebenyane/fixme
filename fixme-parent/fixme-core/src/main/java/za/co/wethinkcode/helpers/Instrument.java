package za.co.wethinkcode.helpers;

public class Instrument {
	public int id;
	public String instrument;
	public int reserveQty;
	
	public Instrument(int id, String instrument, int reserveQty) {
		this.id = id;
		this.instrument = instrument;
		this.reserveQty = reserveQty;
	}
	
	public Instrument() {
		
	}
	
	@Override
	public String toString() {
		return instrument;
	}
}

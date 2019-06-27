package ie.tcd.scss.learning;

public class Action {
	
	private String name;

	protected final int index;
	
	
	public Action(int index) {
		this.index=index;
		this.name="Action " + this.index;
	}
	
	public Action(String name, int index) {
		this(index);
		this.name=name;
	}

	public String getName() {
		return name;
	}
	
	public int getIndex() {
		return this.index;
	}
	
}

package fr.pilato.elasticsearch.river.dropbox.connector;

public class PostOption {
	private String option;
	private String value;
	
	public PostOption() {
	}
	
	public PostOption(String option, String value) {
		super();
		this.option = option;
		this.value = value;
	}

	/**
	 * @return the option
	 */
	public String getOption() {
		return option;
	}
	
	/**
	 * @param option the option to set
	 */
	public void setOption(String option) {
		this.option = option;
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	
	
}

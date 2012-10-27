package com.gardner.drawitlearnit;

public class Option implements Comparable<Option> {
	private String name;
	private boolean isFile = false;
	private String data;
	private String path;

	public boolean isFile() {
		return isFile;
	}

	public void setFile(boolean isFile) {
		this.isFile = isFile;
	}

	public Option(String n, String d, String p, Boolean f) {
		name = n;
		data = d;
		path = p;
		isFile = f;
	}

	public String getName() {
		return name;
	}

	public String getData() {
		return data;
	}

	public String getPath() {
		return path;
	}

	@Override
	public int compareTo(Option o) {
		if (this.name != null)
			return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
		else
			throw new IllegalArgumentException();
	}
}

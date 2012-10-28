package com.gardner.services;

/**
 * Simple java bean/DTO representing a Dictionary Entry.
 * 
 * @author Peter Pascale
 */
public class DictionaryEntry {

	private final String word;
	private final String definition;
	private final String soundPath;
	private final String imagePath;
	private final String apiKey = "?apikey=45e3973e9ddc6545af00461e5b744ef6";
	private final String apiPath = "https://api.pearson.com/longman/dictionary";
	
	public DictionaryEntry(String word, String definition, String soundPath, String imagePath) {
		this.word = word;
		this.definition = definition;
		this.soundPath = soundPath;
		this.imagePath = imagePath;
	}
	
	public String getWord() {
		return word;
	}
	
	public String getDefinition() {
		return definition;
	}
	public String getSoundPath() {
		return apiPath + soundPath + apiKey;
	}
	public String getImagePath() {
		return apiPath + imagePath + apiKey;
	}
	public String toString() {
		return word + ": " + definition + ": " + imagePath;
	}
}


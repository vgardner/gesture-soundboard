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
	
	public DictionaryEntry(String word, String definition, String soundPath) {
		this.word = word;
		this.definition = definition;
		this.soundPath = soundPath;
	}
	
	public String getWord() {
		return word;
	}
	
	public String getDefinition() {
		return definition;
	}
	public String getSoundPath() {
		return "https://api.pearson.com/longman/dictionary" + soundPath + "?apikey=45e3973e9ddc6545af00461e5b744ef6";
	}
	public String toString() {
		return word + ": " + definition + ": " + soundPath;
	}
}


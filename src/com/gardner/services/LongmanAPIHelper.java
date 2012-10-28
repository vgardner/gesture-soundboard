package com.gardner.services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.gardner.services.DictionaryEntry;
import com.gardner.services.Settings;

/**
 * Wrapper around the Longman Dictionary API on Plug and Play.
 * 
 * @author Peter Pascale
 */
public class LongmanAPIHelper {

	private static final String URL_PREFIX = 
		"https://api.pearson.com/longman/dictionary/entry.json?apikey=";

	private static final String KEY_ENTRY = "Entry";
	private static final String KEY_HEAD = "Head"; 
	private static final String KEY_WORD = "HWD";
	private static final String KEY_TEXT = "#text";
	private static final String KEY_SENSE = "Sense";
	
	private static final String KEY_DEFINITION = "DEF";
	
	public String getRandomJSON() throws Exception {
		
		HTTPSCall call = new HTTPSCall(URL_PREFIX + Settings.API_KEY);
		String s = call.doRemoteCall();
		return s;
	}
		
	public DictionaryEntry getRandomDictionaryEntry(String query) throws Exception {
		
		String url = URL_PREFIX + Settings.API_KEY + "&q=" + query;
		HTTPSCall call = new HTTPSCall(url);
		Log.i(Settings.LOG_TAG, url);
		return deserializeDictionaryEntry(call.doRemoteCall());
	}
	
	
	private DictionaryEntry deserializeDictionaryEntry(String entryJSONAsString) throws Exception {
		Log.i(Settings.LOG_TAG, entryJSONAsString);
		DictionaryEntry entry;
		try {
			JSONObject entryJSON = new JSONObject(entryJSONAsString);
			entry = deserializeEntry(entryJSON);
		} catch (JSONException e) {
			Log.i(Settings.LOG_TAG, "Couldn't parse it.");
			throw new RuntimeException(e);
		}
		return entry;
	}
	
	private DictionaryEntry deserializeEntry(JSONObject entryJSON) throws Exception {
		
		String description;
		
		JSONObject objectJson = entryJSON.getJSONObject("Entries").getJSONObject(KEY_ENTRY);
		JSONArray groupArrayJson = objectJson.getJSONArray(KEY_SENSE);
		JSONObject objectJson2 = groupArrayJson.getJSONObject(0);
		
		return new DictionaryEntry(entryJSON.getJSONObject("Entries").getJSONObject(KEY_ENTRY).
				getJSONObject(KEY_HEAD).getJSONObject(KEY_WORD).getString(KEY_TEXT), 
				
				objectJson2.getJSONObject(KEY_DEFINITION).getString(KEY_TEXT));
	}

}

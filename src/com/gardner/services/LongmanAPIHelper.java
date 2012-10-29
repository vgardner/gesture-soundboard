package com.gardner.services;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.sax.Element;
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
		
		String url = URL_PREFIX + Settings.API_KEY + "&q=" + query.trim();
		HTTPSCall call = new HTTPSCall(url);
		Log.i(Settings.LOG_TAG, url);
		
		return deserializeDictionaryEntry(call.doRemoteCall(), query);
	}
	
	
	private DictionaryEntry deserializeDictionaryEntry(String entryJSONAsString, String query) throws Exception {
		Log.i(Settings.LOG_TAG, entryJSONAsString);
		DictionaryEntry entry;
		try {
			JSONObject entryJSON = new JSONObject(entryJSONAsString);
			entry = deserializeEntry(entryJSON, query);
		} catch (JSONException e) {
			Log.i(Settings.LOG_TAG, "Couldn't parse it.");
			throw new RuntimeException(e);
		}
		return entry;
	}

	private DictionaryEntry deserializeEntry(JSONObject entryJSON, String query) throws Exception {
		
		String description = "";
		String soundPath = "no path";
		String imagePath = "no path";
		if(query == "cat"){
			JSONObject objectJson = entryJSON.getJSONObject("Entries").getJSONObject(KEY_ENTRY);
			JSONArray groupArrayJson = objectJson.getJSONArray(KEY_SENSE);
			JSONObject objectJson2 = groupArrayJson.getJSONObject(0);
			JSONArray groupArrayJson2 = objectJson2.getJSONArray("Subsense");
			JSONObject objectJson3 = groupArrayJson2.getJSONObject(0);
			description = objectJson3.getJSONObject(KEY_DEFINITION).getString(KEY_TEXT);
		}
		else {
			JSONObject objectJson = entryJSON.getJSONObject("Entries").getJSONObject(KEY_ENTRY);
			JSONArray groupArrayJson = objectJson.getJSONArray(KEY_SENSE);
			JSONObject objectJson2 = groupArrayJson.getJSONObject(0);
			description = objectJson2.getJSONObject(KEY_DEFINITION).getString(KEY_TEXT);
			
			JSONArray soundArrayJson = objectJson.getJSONArray("multimedia");
			soundPath = soundArrayJson.getJSONObject(1).getString("@href");
			imagePath = soundArrayJson.getJSONObject(3).getString("@href");
		}
		return new DictionaryEntry(entryJSON.getJSONObject("Entries").getJSONObject(KEY_ENTRY).
				getJSONObject(KEY_HEAD).getJSONObject(KEY_WORD).getString(KEY_TEXT),
				
				description, soundPath, imagePath);
	}
	
	// XML Stuff
	public static Document loadXMLFromString(String xml) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
	 private static String getTagValue(String sTag, Element eElement) {
			NodeList nlList = ((Document) eElement).getElementsByTagName(sTag).item(0).getChildNodes();
		 
		        Node nValue = (Node) nlList.item(0);
		 
			return nValue.getNodeValue();
	 }
	 private DictionaryEntry deserializeDictionaryEntryXML(String entryJSONAsString) throws Exception {
			Log.i(Settings.LOG_TAG, entryJSONAsString);
			DictionaryEntry entry = null;
			
			try {		
				Document doc = loadXMLFromString(entryJSONAsString);
				doc.getDocumentElement().normalize();
		 
				Log.i(Settings.LOG_TAG, "Root element :" + doc.getDocumentElement().getNodeName());
				NodeList nList = doc.getElementsByTagName("Entry");
		 
				for (int temp = 0; temp < nList.getLength(); temp++) {
		 
				   Node nNode = nList.item(temp);
				   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
				      Element eElement = (Element) nNode;
		 
				      Log.i(Settings.LOG_TAG,"First Name : " + getTagValue("firstname", eElement));
				      Log.i(Settings.LOG_TAG,"Last Name : " + getTagValue("lastname", eElement));
				      Log.i(Settings.LOG_TAG,"Nick Name : " + getTagValue("nickname", eElement));
				      Log.i(Settings.LOG_TAG,"Salary : " + getTagValue("salary", eElement));
		 
				   }
				}
			  } catch (Exception e) {
				e.printStackTrace();
			  }
			
			return entry;
		}
}

package data;

import java.util.List;

import com.google.gson.internal.LinkedTreeMap;

public class WordInParse {
	String text;
	String pos;
	int startOffset;
	int endOffset;
	List<String> linkers;
	
	public WordInParse(String t, String p, int start, int end, List<String> lks){
		this.text = t;
		this.pos = p;
		this.startOffset = start;
		this.endOffset = end;
		this.linkers = lks;
	}
	
	public WordInParse(List<Object> word){
		String tokenText = (String)word.get(0);
		LinkedTreeMap wordMeta = (LinkedTreeMap) word.get(1);
		String pos = (String)wordMeta.get("PartOfSpeech");
		Double tmpOffset = (Double)wordMeta.get("CharacterOffsetBegin");
		int beginOffset = tmpOffset.intValue();
		tmpOffset = (Double)wordMeta.get("CharacterOffsetEnd");
		int endOffset = tmpOffset.intValue();
		List<String> linkers = (List<String>)wordMeta.get("Linkers");
		
		this.text = tokenText;
		this.pos = pos;
		this.startOffset = beginOffset;
		this.endOffset = endOffset;
		this.linkers = linkers;
		
//		new WordInParse(tokenText, pos, beginOffset, endOffset, linkers);
	}
	
//	List<Obje>

}

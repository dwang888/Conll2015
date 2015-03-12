package data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class SentParse {

	List<List<String>> dependencies;//element: depType, depHead, depDependent
	String parsetree;
	public List<Object> words;//["important", {"CharacterOffsetBegin": 4641, "CharacterOffsetEnd": 4650, "Linkers": ["arg2_14904", "arg2_14905"], "PartOfSpeech": "JJ"}]
	public List<WordInParse> wordsInParse;
	/**
	 * get token list
	 * */
	public List<String> getTokensList(){
		List<String> tokens = new ArrayList<String>();
		for(int i = 0; i < this.words.size(); i++){
			List<Object> wObj = (List<Object>) this.words.get(i);
			WordInParse w = new WordInParse(wObj);
			tokens.add(w.text);
		}
		return tokens;
	}
	
	public String getTokensListText(){
		List<String> tokens = this.getTokensList();
		return StringUtils.join(tokens, " ");
	}

}

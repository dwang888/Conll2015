package features;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;

import data.DocParse;
import data.PDTB;
import data.SentParse;
import data.WordInParse;

public class LinearBasicExtractor {

	public List<PDTB> loadPDTB(String path, String pathRaw){
		String rawText = readTxt(pathRaw);
		Gson gson = new Gson();
		List<PDTB> pdtbObjs = new ArrayList<PDTB>();
		System.out.println("Reading positive data point from a JSON file:\t" + path);
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line;
			while((line = br.readLine()) != null){
				PDTB pdtbObj = (PDTB) gson.fromJson(line, PDTB.class);
				
//				System.out.println("info from PDTB object:\t" + pdtbObj.DocID);
//				System.out.println("info from PDTB object:\t" + pdtbObj.ID);
//				System.out.println("info from PDTB object:\t" + pdtbObj.Type);
//				System.out.println("info from PDTB object:\t" + pdtbObj.Sense);
//				System.out.println("info from PDTB object:\t" + Arrays.toString(pdtbObj.Connective.CharacterSpanList.toArray()));
//				if(pdtbObj.Arg2.CharacterSpanList.size() > 1){
//					System.out.println(pdtbObj.Arg2.RawText);
//					String rawTextArg2 = pdtbObj.Arg2.RawText;
//					int startOffset = pdtbObj.Arg2.CharacterSpanList.get(1)[0];
//					int endOffset = pdtbObj.Arg2.CharacterSpanList.get(1)[1];
//					System.out.println(startOffset + "||" + endOffset);
//					System.out.println(rawText.substring(startOffset, endOffset));
//					System.out.println(pdtbObj.Arg2.RawText);					
//				}
				pdtbObjs.add(pdtbObj);
			}			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  

		return pdtbObjs;
	}
	
	public List<SentParse> loadParse(String path){
		Gson gson = new Gson();
		JsonParser jsonParser = new JsonParser();
		System.out.println("Reading parses from a JSON file:\t" + path);
		BufferedReader br;
		List<SentParse> sentParses = new ArrayList<SentParse>();
		
		try {
			br = new BufferedReader(new FileReader(path));
			String line;
			String docID = "wsj_1000";
			while((line = br.readLine()) != null){
//				PDTB docParseObj = (PDTB) gson.fromJson(line, PDTB.class);
				JsonObject jobj= jsonParser.parse(line).getAsJsonObject();
				jobj.getAsJsonObject(docID).getAsJsonArray("sentences");
				DocParse docPparseObj = (DocParse) gson.fromJson(jobj.get(docID), DocParse.class);
//				SentParse parseObj = (SentParse) gson.fromJson(jobj.get(docID), SentParse.class);
//				System.out.println(docPparseObj.sentences);
				for(SentParse parse : docPparseObj.sentences){
//					System.out.println(parse.parsetree);
//					System.out.println(parse.dependencies);
//					System.out.println(parse.getTokensListText());
					List<WordInParse> wordsInParse = new ArrayList<WordInParse>();
					for(int i = 0; i < parse.words.size(); i++){
						List<Object> word = (List<Object>) parse.words.get(i);
						String tokenText = (String)word.get(0);
						LinkedTreeMap wordMeta = (LinkedTreeMap) word.get(1);
						String pos = (String)wordMeta.get("PartOfSpeech");
						Double tmpOffset = (Double)wordMeta.get("CharacterOffsetBegin");
						int beginOffset = tmpOffset.intValue();
						tmpOffset = (Double)wordMeta.get("CharacterOffsetEnd");
						int endOffset = tmpOffset.intValue();
						List<String> linkers = (List<String>)wordMeta.get("Linkers");
						WordInParse wordInParse = new WordInParse(tokenText, pos, beginOffset, endOffset, linkers);
//						WordInParse wordInParse = new WordInParse(word);
//						System.out.println(beginOffset);
//						System.out.println(wordInParse.linkers);
						
//						WordInParse wordObj = (WordInParse) gson.fromJson(parse.words.get(i), WordInParse.class);
						wordsInParse.add(wordInParse);
					}
					parse.wordsInParse = wordsInParse;
					sentParses.add(parse);
//					for(WordInParse (WordInParse)word : parse.words){
//						System.out.println(word);
//					}
				}
			}
		} catch (JsonSyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sentParses;
	}
	
	public String readTxt(String path){	    
		String content = null;
	    try {
			content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return content;
	}
	
	public void extract(String pathRaw, String pathParse, String pathPDTB){
		List<PDTB> pdtbObjs = loadPDTB(pathPDTB, pathRaw);
		List<SentParse> sentParses = loadParse(pathParse);
		
		for(int i = 0; i < sentParses.size(); i++){
			SentParse sentParse = sentParses.get(i);
			for(int j = 0; j < sentParse.words.size(); j++){
				List<Object> word = (List<Object>) sentParse.words.get(i);
			}
		}
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String pdtbPath = "D:\\projects\\conll2015\\data\\pdtb_trial_data.json";
		String parsePath = "D:\\projects\\conll2015\\data\\pdtb_trial_parses.json";
		String rawPath = "D:\\projects\\conll2015\\data\\raw_train\\wsj_1000";
		LinearBasicExtractor tester = new LinearBasicExtractor();
		List<PDTB> pdtbObjs = tester.loadPDTB(pdtbPath, rawPath);
		List<SentParse> sentParses = tester.loadParse(parsePath);
	}

}

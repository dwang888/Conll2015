package features;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;

import data.DocParse;
import data.PDTB;
import data.SentParse;
import data.WordInParse;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;

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
				
//				System.out.println("DocID from PDTB object:\t" + pdtbObj.DocID);
//				System.out.println("ID from PDTB object:\t" + pdtbObj.ID);
//				System.out.println("type from PDTB object:\t" + pdtbObj.Type);
//				System.out.println("sense from PDTB object:\t" + pdtbObj.Sense);
//				System.out.println("info from PDTB object:\t" + Arrays.toString(pdtbObj.Connective.CharacterSpanList.toArray()));
//				if(pdtbObj.Arg2.CharacterSpanList.size() > 1){
//				if((pdtbObj.Arg2.CharacterSpanList.size() > 1 || pdtbObj.Arg1.CharacterSpanList.size() > 1)){
				if(!pdtbObj.Type.equalsIgnoreCase("Explicit")){
					System.out.println(pdtbObj.Type);
					System.out.println(pdtbObj.Sense);
					System.out.println("Arg1:\t" + pdtbObj.Arg1.RawText);					
					System.out.println("Arg2:\t" + pdtbObj.Arg2.RawText);
//					String rawTextArg2 = pdtbObj.Arg2.RawText;
					int startOffset = pdtbObj.Arg2.CharacterSpanList.get(0)[0];
					int endOffset = pdtbObj.Arg2.CharacterSpanList.get(0)[1];
					System.out.println(startOffset + "||" + endOffset);
					System.out.println(rawText.substring(startOffset, endOffset));
					System.out.println("-----------------");					
				}
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
//					System.out.println(parse);					
//					System.out.println(parse.parsetree.trim());
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
//						System.out.println(wordInParse.);
						
//						WordInParse wordObj = (WordInParse) gson.fromJson(parse.words.get(i), WordInParse.class);
						wordsInParse.add(wordInParse);
					}
					parse.wordsInParse = wordsInParse;
					sentParses.add(parse);
					System.out.println(parse.getTokensList());
					System.out.println(parse.getTokensList().size());
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
	
	public List<Tree> loadTrees(String path){
		List<Tree> trees = new ArrayList<Tree>();
		try {
			
			Reader reader = new FileReader(path);
			System.out.println(reader);
			
			PennTreeReader treeReader = new PennTreeReader(reader);
			Tree tree;
			while((tree = treeReader.readTree()) != null){
				trees.add(tree);
//				System.out.println("-----------");
//				System.out.println(tree.getLeaves());
//				System.out.println(StringUtils.join(tree.getLeaves(), " "));
				
				List<Tree> subtrees = tree.subTreeList();
				List<Tree> nodes = tree.pathNodeToNode(subtrees.get(2), subtrees.get(8));
//				for(Tree node : nodes){
//					System.out.println(node.label().toString());
//				}
				
//				tree = treeReader.readTree();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return trees;
	}
	
	/**
	 * whether a contain b or vice versa
	 * */
	public boolean ifContain(Tree a, Tree b){
		if(a.subTrees().contains(b) || b.subTrees().contains(a)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * extract S tree pairs from current sentence
	 * */
	public List<Tree[]> extractPairsInSentence(Tree tree){
		List<Tree[]> pairs = new ArrayList<Tree[]>();
		List<Tree> subtrees = tree.subTreeList();
		List<Tree> subSents = new ArrayList<Tree>();
		
		System.out.println(StringUtils.join(tree.getLeaves(), " "));
		for(int j = 0; j < subtrees.size(); j++){
			Tree subtree = subtrees.get(j);
			//exclude whole tree itself
			if(subtree.getLeaves().size() == tree.getLeaves().size()){
//				System.out.println(curTree + "----" + subtree);
				continue;
			}
			
			if(subtree.nodeString().equalsIgnoreCase("S")){
				subSents.add(subtree);
//				System.out.println(StringUtils.join(subtree.getLeaves(), " "));
			}			
			
		}
		
		//no or just 1 sub sentence
		if(subSents.size() <= 1){
			return pairs;
		}
		
		for(int i = 0; i < subSents.size()-1; i++){
			for(int j = i+1; j < subSents.size(); j++){
				if(!ifContain(subSents.get(i),subSents.get(j))){
					Tree[] pair = new Tree[]{subSents.get(i), subSents.get(j)};
					pairs.add(pair);
				}
			}
		}
		
		return pairs;		
	}
	
	public List<List<String>> capturePairs(List<Tree> trees){
		List<List<String>> fvs = new ArrayList<List<String>>();
		
		Tree curTree;
		Tree prevTree;
		Tree nextTree;
		
		for(int i = 0; i < trees.size(); i++){
			curTree = trees.get(i);
			prevTree = i>=1 ? trees.get(i-1) : null;
			nextTree = i<trees.size()-1 ? trees.get(i+1) : null;
			
			//extract pairs in current tree;
			List<Tree[]> pairsInCurrentSent = extractPairsInSentence(curTree);
			
			
			
			
			System.out.println(pairsInCurrentSent.size() + "------------------");
		}
		
		return fvs;
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
//		List<PDTB> pdtbObjs = tester.loadPDTB(pdtbPath, rawPath);
//		List<SentParse> sentParses = tester.loadParse(parsePath);
		List<Tree> trees = tester.loadTrees(parsePath);
		tester.capturePairs(trees);
	}

}

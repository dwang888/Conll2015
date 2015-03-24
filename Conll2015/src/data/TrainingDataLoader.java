package data;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
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

public class TrainingDataLoader {

	public List<PDTB> loadPDTB(String path, String pathRaw){
//		String rawText = readTxt(pathRaw);
		Map<String, String> rawTextHash = loadRawTextFromFolders(pathRaw);
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
//					System.out.println(pdtbObj.Type);
//					System.out.println(pdtbObj.Sense);
//					System.out.println("Arg1:\t" + pdtbObj.Arg1.RawText);					
//					System.out.println("Arg2:\t" + pdtbObj.Arg2.RawText);
//					String rawTextArg2 = pdtbObj.Arg2.RawText;
					int startOffset = pdtbObj.Arg2.CharacterSpanList.get(0)[0];
					int endOffset = pdtbObj.Arg2.CharacterSpanList.get(0)[1];
//					System.out.println(startOffset + "||" + endOffset);
//					System.out.println(rawText.substring(startOffset, endOffset));
//					System.out.println("-----------------");					
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
		
		System.out.format("%d PDTB objects loaded", pdtbObjs.size());

		return pdtbObjs;
	}
	
	public Map<String, String> loadRawTextFromFolders(String path){
		Map<String, String> rawTextHash = new HashMap<String, String>();
		File folder = new File(path);
		File[] files = folder.listFiles();
		
		for(int i = 0; i < files.length; i++){
			File file = files[i];
			if(file.isFile()){
				String content;
				try {
					content = FileUtils.readFileToString(file);
					rawTextHash.put(file.getName().split(".txt")[0], content);
//					System.out.println(file.getName());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		return rawTextHash;
	}
	
	public List<SentParse> loadParse(String path){
		Gson gson = new Gson();
		JsonParser jsonParser = new JsonParser();
		System.out.println("Reading parses from a JSON file:\t" + path);
		BufferedReader br;
		List<SentParse> sentParses = new ArrayList<SentParse>();
		
		File file = new File(path);
		String content;
		try {
			content = FileUtils.readFileToString(file);
			JsonObject jobj= jsonParser.parse(content).getAsJsonObject();
//			System.out.println(jobj.getAsJsonArray().size());
			System.out.println(content.split("\\r").length);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
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
				System.out.println(line);
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
//					System.out.println(parse.getTokensList());
//					System.out.println(parse.getTokensList().size());
//					for(WordInParse (WordInParse)word : parse.words){
//						System.out.println(word);
//					}
				}
			}
		} catch (JsonSyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.format("\n\n%d PDTB parse loaded \n\n", sentParses.size());
		
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
//			System.out.println(reader);
			
			PennTreeReader treeReader = new PennTreeReader(reader);
			Tree tree;
			while((tree = treeReader.readTree()) != null){
				trees.add(tree);
//				System.out.println("-----------");
//				System.out.println(tree.getLeaves());
//				System.out.println(StringUtils.join(tree.getLeaves(), " "));
				
				List<Tree> subtrees = tree.subTreeList();
//				List<Tree> nodes = tree.pathNodeToNode(subtrees.get(2), subtrees.get(8));
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
		
		System.out.format("\n\n%d parsing tree loaded \n\n", trees.size());
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
		
//		System.out.println(StringUtils.join(tree.getLeaves(), " "));
		for(int j = 0; j < subtrees.size(); j++){
			Tree subtree = subtrees.get(j);
			//exclude whole tree itself
			if(subtree.getLeaves().size() == tree.getLeaves().size()){
//				System.out.println(curTree + "----" + subtree);
				continue;
			}
			
			if(subtree.nodeString().startsWith("S")){
				
				subSents.add(subtree);
//				System.out.println(subtree.nodeString());
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
	
	/**
	 * extract S tree pairs between sentence
	 * */
	public List<Tree[]> extractPairsInterSentence(Tree tree1, Tree tree2){
		
		List<Tree[]> pairs = new ArrayList<Tree[]>();
		if(tree1 == null ||  tree2 == null){
			return pairs;
		}
		
		List<Tree> subtrees1 = tree1.subTreeList();
		List<Tree> subtrees2 = tree2.subTreeList();

		List<Tree> subSents1 = new ArrayList<Tree>();
		List<Tree> subSents2 = new ArrayList<Tree>();
		
		//detect S trees only 
		for(int j = 0; j < subtrees1.size(); j++){
			Tree subtree = subtrees1.get(j);
			if(subtree.nodeString().startsWith("S")){
				subSents1.add(subtree);
//				System.out.println(StringUtils.join(subtree.getLeaves(), " "));
			}			
		}
		for(int j = 0; j < subtrees2.size(); j++){
			Tree subtree = subtrees2.get(j);
			if(subtree.nodeString().startsWith("S")){
				subSents2.add(subtree);
			}			
		}
		
		//pair them
		for(int i = 0; i < subSents1.size(); i++){
			for(int j = 0; j < subSents2.size(); j++){
				Tree[] pair = new Tree[]{subSents1.get(i), subSents2.get(j)};
				pairs.add(pair);
			}
		}
		
		return pairs;		
	}
	
	public List<Tree[]> capturePairs(List<Tree> trees){
		List<List<String>> fvs = new ArrayList<List<String>>();
		List<Tree[]> pairs = new ArrayList<Tree[]>();
		
		Tree curTree;
//		Tree prevTree;
		Tree nextTree;
		
		for(int i = 0; i < trees.size(); i++){
			curTree = trees.get(i);
//			prevTree = i>=1 ? trees.get(i-1) : null;
			nextTree = i<trees.size()-1 ? trees.get(i+1) : null;
			
			//extract pairs in current tree;
			List<Tree[]> pairsInCurrentSent = extractPairsInSentence(curTree);			
//			List<Tree[]> pairsInterPrevCurSent = extractPairsInterSentence(prevTree, curTree);
			List<Tree[]> pairsInterCurNextSent = extractPairsInterSentence(curTree, nextTree);			
			
			pairs.addAll(pairsInCurrentSent);
//			pairs.addAll(pairsInterPrevCurSent);
			pairs.addAll(pairsInterCurNextSent);
			
//			System.out.println(pairsInterCurNextSent.size() + "------------------");
		}
		
//		System.out.println(pairs.size() + " sentences pairs extracted");
//		Set<Integer> checksums = new HashSet<Integer>();
//		List<Tree[]> newPairs = new ArrayList<Tree[]>();
//		for(Tree[] pair : pairs){
//			
////			System.out.println(pair[0].getLeaves());
////			System.out.println(pair[1].getLeaves());
//			if(checksums.contains(pair.hashCode())){
//				System.out.println(pair[1].getLeaves());
//				continue;
//			}
//			else{
//				checksums.add(pair.hashCode());
//				newPairs.add(pair);
//			}
//		}
		
		return pairs;
	}
	
	/**
	 * add word offset info to tree.
	 * */
	public List<Tree> mergeWordInfo2Tree(List<Tree> trees, List<SentParse> sentParses){
//		List<Tree> newTrees = List<Tree> SerializationUtils.clone(trees);
//		tree.getChildrenAsList().get(1).setFromString(labelStr);
		
		for(int i = 0; i < trees.size(); i++){
			Tree tree = trees.get(i);
			SentParse sentParse = sentParses.get(i);
			List<WordInParse> wordsInParse = sentParse.wordsInParse;
			List<Tree> leaves = tree.getLeaves(new ArrayList<Tree>());
//			tree.getChildrenAsList().get(1).setFromString(labelStr);
			for(int j = 0; j < leaves.size(); j++){
				WordInParse word = wordsInParse.get(j);
				leaves.get(j).setValue(word.text + "_" + word.startOffset + "_" + word.endOffset);
			}
			
		}
		
		for(Tree tree : trees){
//			System.out.println(tree.getLeaves());
		}
		
		return trees;
	}
	
	public void assignGoldLabels(List<Tree[]> pairs, List<PDTB> pdtbObjs){
		List<String> labels = new ArrayList<String>();
		
		for(int i = 0; i < pairs.size(); i++){
			Tree[] pair = pairs.get(i);
			Tree tree1 = pair[0];
			Tree tree2 = pair[1];
			int startTree1 = Integer.parseInt(tree1.getLeaves().get(0).toString().split("_")[1]);
			int endTree1 = Integer.parseInt(tree1.getLeaves().get(tree1.getLeaves().size()-1).toString().split("_")[1]);
			int startTree2 = Integer.parseInt(tree2.getLeaves().get(0).toString().split("_")[1]);
			int endTree2 = Integer.parseInt(tree2.getLeaves().get(tree2.getLeaves().size()-1).toString().split("_")[1]);
			for(int j = 0; j < pdtbObjs.size(); j++){
				PDTB pdtbObj = pdtbObjs.get(j);
				int startArg1 = pdtbObj.Arg1.CharacterSpanList.get(0)[0];
				int endArg1 = pdtbObj.Arg1.CharacterSpanList.get(0)[1];
				int startArg2 = pdtbObj.Arg2.CharacterSpanList.get(0)[0];
				int endArg2 = pdtbObj.Arg2.CharacterSpanList.get(0)[1];
//				System.out.format("%s %s %s %s %s %s %s %s ", startTree1, endTree2, startTree2, endTree2, startArg1, endArg1, startArg2, endArg2);
				if(startTree1==startArg1 && endTree1==endArg1 && startTree2==startArg2 && endTree2==endArg2){
					System.out.println(pdtbObj.Arg1.RawText);
					System.out.println(pdtbObj.Arg2.RawText);
					System.out.println(pdtbObj.Type + "\t" + pdtbObj.Connective + "\t" + pdtbObj.Sense);
					System.out.println("--------------- ");
				}
			}
			
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String pdtbPath = "D:\\projects\\conll2015\\data\\conll15st-train-dev\\conll15st_data\\conll15-st-03-04-15-train\\pdtb-data.json";
		String parsePath = "D:\\projects\\conll2015\\data\\conll15st-train-dev\\conll15st_data\\conll15-st-03-04-15-train\\pdtb-parses.json";
		String rawPath = "D:\\projects\\conll2015\\data\\conll15st-train-dev\\conll15st_data\\conll15-st-03-04-15-train\\raw\\";
		TrainingDataLoader loader = new TrainingDataLoader();
//		List<PDTB> pdtbObjs = loader.loadPDTB(pdtbPath, rawPath);
		List<SentParse> sentParses = loader.loadParse(parsePath);
//		List<Tree> trees = loader.loadTrees(parsePath);
//		trees = loader.mergeWordInfo2Tree(trees, sentParses);
//		List<Tree[]> pairs = loader.capturePairs(trees);
//		loader.assignGoldLabels(pairs, pdtbObjs);
	}

}

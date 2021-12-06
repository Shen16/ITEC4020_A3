package com.howtodoinjava.demo.lucene.file;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

//new imports
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.sun.net.httpserver.HttpServer;

 
public class LuceneReadIndexFromFileExample 
{
    //directory contains the lucene indexes
    private static final String INDEX_DIR = "indexedFiles";
 // Path used to read in compressed files
	final static String COMPRESSED_DATA_PATH = "//data//";
	
	// Path used to output decompressed files
	final static String DECOMPRESS_PATH = "//extracted//";
    

 
    public static void main(String[] args) throws Exception 
    {
    	
    	
    	String currentdir = System.getProperty("user.dir");
    	String path= currentdir+"/topics.txt";
    	
    	queryTopics(path);
    	
    	//System.out.println(currentdir);
//    	try {	
//    			String myList ="";
//    			String docid = "";
//    	      File myObj = new File(currentdir+"/topics.txt");
//    	      Scanner myReader = new Scanner(myObj);
//    	      //String actual = Files.readString(myObj);
//    	      while (myReader.hasNextLine()) {
//    	        String data = myReader.nextLine();
//    	        if (data.startsWith("<num>")) {
//    	        	docid = data.replaceAll("[^0-9]", ""); //numbers between 0 and 9 will be replaced with empty space
//    	        }
//    	        myList = myList+data;
//    	        
//    	        if (myList.contains("</top>")) {
//    	        	
//    	        	search(myList.replaceAll("[^a-z0-9]", " "),docid); //what is this line doing?  search (blank, "number: ")??
//    	        	myList="";
//    	        }
//    	        
//    	      }
//    	      myReader.close();
//    	      
//
//    	      
//    	    } catch (FileNotFoundException e) {
//    	      System.out.println("An error occurred.");
//    	      e.printStackTrace();
//    	    }
    	
    	
    	
    	
    	

        
    }
    
    
//    private static void search(String text, String docid) throws Exception { //String docid not used
//    	//Create lucene searcher. It search over a single IndexReader.
//        IndexSearcher searcher = createSearcher();
//         
//        //Search indexed contents using search term
//        TopDocs foundDocs = searchInContent(text, searcher);
//        
//        
//        
//        //Total found documents
//        System.out.println("Total Results :: " + foundDocs.totalHits);
//        System.out.println("Text: "+ text);
//         
//        //Let's print out the path of files which have searched term
//        for (ScoreDoc sd : foundDocs.scoreDocs) 
//        {
//            Document d = searcher.doc(sd.doc);
//            System.out.println(docid + " Q0 " + "Path : "+ d.get("path") + ", Score : " + sd.score + " Group7"+ d.get("contents"));
//        }
//    }
    
    private static List<ArrayList<Object>> search2(String title, String desc, String narr, String topicNum) throws Exception { //String docid not used
    	List<ArrayList<Object>> listOLists = new ArrayList<ArrayList<Object>>();
    	
//    	ArrayList<ArrayList<Ojects>> listOLists = new ArrayList<ArrayList<String>>();
    	
    	//Create lucene searcher. It search over a single IndexReader.
        IndexSearcher searcher = createSearcher();
         
        //Search indexed contents using search term
        TopDocs foundDocs = searchInContent(title, desc, narr, searcher);
        
        
        
        //Total found documents
        System.out.println("Total Results :" + foundDocs.totalHits);
//        System.out.println("Text: "+ text);
         
        //Let's print out the path of files which have searched term
        int i=0;
        for (ScoreDoc sd : foundDocs.scoreDocs) 
        {
        	i=i+1;
            Document d = searcher.doc(sd.doc);
            System.out.println(topicNum+ " Q0 " + "Path : "+ d.get("docno") + " "+ i + " , Score : " + sd.score + " Group7");
            
            ArrayList<Object> singleList = new ArrayList<Object>();
            singleList.add(d.get("docno"));
            singleList.add(d.get("url"));
            singleList.add(sd.score);
            listOLists.add(singleList);
        }
        return listOLists;
    }
     
    private static TopDocs searchInContent(String titl, String description, String narration, IndexSearcher searcher) throws Exception
    {
    	System.out.println("Title:" + titl);
    	System.out.println("Description:" + description);
    	System.out.println("Narration:" + narration);
    	
    	
    	
        //Create search query
        QueryParser qp = new QueryParser("content", new StandardAnalyzer()); //fields specified when building index
        String textToFind = "title:" + titl + " AND content:" + description; //seraches with keywords of title and body content
//        String textToFind = "content:" + description;
        Query query = qp.parse(textToFind);
         
        //search the index
        TopDocs hits = searcher.search(query, 5);
//        Explanation explain= searcher.explain(query, 0);
//        System.out.println("Explanation:" + explain);
        return hits;
    }
 
    private static IndexSearcher createSearcher() throws IOException 
    {
        Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
         
        //It is an interface for accessing a point-in-time view of a lucene index
        IndexReader reader = DirectoryReader.open(dir);
         
        //Index searcher
        IndexSearcher searcher = new IndexSearcher(reader);
        return searcher;
    }
    
    public static void queryTopics(String path) throws Exception {
		// The Map to be passed back once complete
//		Map<String, String> jsonMap = new HashMap<String, String>();
		
		System.out.println("Querying topics from: " + path);
		
		try {
			// The entire document that is currently being parsed.
			org.jsoup.nodes.Document entireDoc = Jsoup.parse(new File(path), null);
			
			for(int i = 0; i < entireDoc.select("top").size(); i++) {
//			for(int i = 0; i < 1; i++) {
				// Pull out each <top> element to be parsed
				Element topic = entireDoc.select("top").get(i);
				
				// Form the queries from the Element
//				Object[] results = searchFromTopic(topic);
				searchFromTopic(topic);
				
				// Store results to be returned and written to the output file
//				jsonMap.put(results[0], results[1]);
			}
			
//			System.out.println("Total topics found: " + entireDoc.select("top").size());
		} catch (IOException errIO) {
			errIO.printStackTrace();
		}

	}
    
    public static void searchFromTopic(Element e) throws Exception {
		String topicNum = "", title = "", desc = "", narr = "";
		Element ele;
		
		// Only update the Strings if the elements exist
		if ((ele = e.select("narr").first()) != null) {
			narr = ele.text().substring(ele.text().indexOf(":") + 2);
			ele.remove();
		}

		if ((ele = e.select("desc").first()) != null) {
			desc = ele.text().substring(ele.text().indexOf(":") + 2);
			ele.remove();
		}

		
		if ((ele = e.select("title").first()) != null) {
			title = ele.text();
			ele.remove();
		}
		
		if ((ele = e.select("num").first()) != null) {
			topicNum = ele.text().substring(ele.text().indexOf(":") + 2);
		}
		
//		Object[] results = new String[2];
//		results[0] = topicNum;
		// Perform the search with the data provided
//		results[1] = search2(title, desc);
		search2(title, desc, narr, topicNum);
		
		
//		return results;
	}
    
    
    
    
    
}
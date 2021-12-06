package com.howtodoinjava.demo.lucene.document;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;


import org.apache.lucene.search.IndexSearcher; //is used to search lucene documents from indexes. It takes one argument Directory, which points to index folder.
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs; //returns top list of documents for a given query
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class LuceneReadIndexExample 
{
	private static final String INDEX_DIR = "c:/temp/lucene6index";

	public static void main(String[] args) throws Exception 
	{
		IndexSearcher searcher = createSearcher(); //an index searcher or index directory: INDEX_DIR
		
		//Search by ID
		TopDocs foundDocs = searchById(1, searcher);
		
		System.out.println("Total Results :: " + foundDocs.totalHits);//TopDocs totalHits: The total number of hits for the query.
		
		for (ScoreDoc sd : foundDocs.scoreDocs)  //TopDocs scoreDocs: The top hits for the query. //for every document in the top doc list do the following
		{
			Document d = searcher.doc(sd.doc); //sd.doc is: int docID of each TopDocs retrived
			System.out.println(String.format(d.get("firstName"))); //prints Lokesh //gets firstname field from the doc
		}
		
		//Search by firstName
		TopDocs foundDocs2 = searchByFirstName("Brian", searcher);
		
		System.out.println("Toral Results :: " + foundDocs2.totalHits);
		
		for (ScoreDoc sd : foundDocs2.scoreDocs) 
		{
			Document d = searcher.doc(sd.doc);
			System.out.println(String.format(d.get("id"))); //prints 2 //gets id field from the doc
		}
	}
	
	private static TopDocs searchByFirstName(String firstName, IndexSearcher searcher) throws Exception
	{
		QueryParser qp = new QueryParser("firstName", new StandardAnalyzer());
		Query firstNameQuery = qp.parse(firstName); //firstname entered in searchByFirstName already is a string
		TopDocs hits = searcher.search(firstNameQuery, 10); //Finds the top 10 hits for query.
		return hits;
	}

	private static TopDocs searchById(Integer id, IndexSearcher searcher) throws Exception
	{
		QueryParser qp = new QueryParser("id", new StandardAnalyzer()); //takes in user's id input and a Standard Analyzer to create a Query parser
		Query idQuery = qp.parse(id.toString()); //parses the input is from the parser to get the query id //id enetered in searchById is int so need to convert to string to parse
		TopDocs hits = searcher.search(idQuery, 10); //Finds the top 10 hits (top 10 ranked docs) for query.
		return hits;
	}

	private static IndexSearcher createSearcher() throws IOException {
		Directory dir = FSDirectory.open(Paths.get(INDEX_DIR)); //first gets the directory where the index documents are stored
		IndexReader reader = DirectoryReader.open(dir); //opens the index directory and instantiates a reader
		IndexSearcher searcher = new IndexSearcher(reader); //creates an IndexSearcher and passes the reader is input
		return searcher;
	}
}

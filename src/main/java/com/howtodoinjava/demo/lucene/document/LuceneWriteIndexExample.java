package com.howtodoinjava.demo.lucene.document;

import java.io.IOException;
import java.nio.file.Paths; //to get path of index directory (where the indexed documents will be stored)
import java.util.ArrayList;
import java.util.List;

//required for index writer config
import org.apache.lucene.analysis.standard.StandardAnalyzer;

//To create a document with fields
import org.apache.lucene.document.Document; //class that represents Lucene indexed document.
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

//provides functionality to create and manage index. It’s constructor takes two arguments: FSDirectory and IndexWriterConfig.
import org.apache.lucene.index.IndexWriter;  
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

public class LuceneWriteIndexExample 
{
	private static final String INDEX_DIR = "c:/temp/lucene6index";

	public static void main(String[] args) throws Exception 
	{
		IndexWriter writer = createWriter();
		List<Document> documents = new ArrayList<>();
		
		Document document1 = createDocument(1, "Lokesh", "Gupta", "howtodoinjava.com");
		documents.add(document1);
		
		Document document2 = createDocument(2, "Brian", "Schultz", "example.com");
		documents.add(document2);
		
		//Let's clean everything first
		writer.deleteAll();
		
		writer.addDocuments(documents);
		writer.commit();
	    writer.close();
	}

	private static Document createDocument(Integer id, String firstName, String lastName, String website) 
	{
    	Document document = new Document();
    	document.add(new StringField("id", id.toString() , Field.Store.YES)); //id needs to be converted to string
    	document.add(new TextField("firstName", firstName , Field.Store.YES));
    	document.add(new TextField("lastName", lastName , Field.Store.YES));
    	document.add(new TextField("website", website , Field.Store.YES));
    	return document;
    }

	private static IndexWriter createWriter() throws IOException 
	{
		FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		IndexWriter writer = new IndexWriter(dir, config);
		return writer;
	}
}

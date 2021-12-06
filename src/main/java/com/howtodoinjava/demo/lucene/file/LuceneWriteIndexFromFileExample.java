package com.howtodoinjava.demo.lucene.file;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



 
public class LuceneWriteIndexFromFileExample 
{
//	final static String HTML_PATH_INTERNAL = "//public//pages//";
//	final static String HTML_PATH_PUBLIC = "/pages/";
	static int numOfDocuments = 0;
	
	 // Path used to read in compressed files
	final static String COMPRESSED_DATA_PATH = "//data//";
		
	// Path used to output decompressed files
	final static String DECOMPRESS_PATH = "//extracted//";
	
    public static void main(String[] args) throws ParseException
    {
    	decompress();
    	
    	String currentdir = System.getProperty("user.dir");
        //Input folder
        String docsPath = currentdir+ "/extracted";
         
        //Output folder
        String indexPath = currentdir+ "/indexedFiles";
        
       
 
        //Input Path Variable
        final Path docDir = Paths.get(docsPath); //creates a Path object storing directory path of input files
 
        try
        {
            //org.apache.lucene.store.Directory instance
            Directory dir = FSDirectory.open( Paths.get(indexPath) ); //path where indexed files will be written
             
            //analyzer with the default stop words
            Analyzer analyzer = new StandardAnalyzer();
             
            //IndexWriter Configuration
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND); //CREATE_OR_APPEND: Creates a new index if one does not exist, otherwise it opens the index and documents will be appended.
             
            //IndexWriter writes new index files to the directory
            IndexWriter writer = new IndexWriter(dir, iwc); //dir specifies where to write index files
             
            //Its recursive method to iterate all files and directories
            indexDocs(writer, docDir); //docDir = Paths.get(docsPath) //path for input files
            
            System.out.print("Done writing");
 
            writer.close();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
     
    static void indexDocs(final IndexWriter writer, Path path) throws IOException, ParseException 
    {
        //Directory?
        if (Files.isDirectory(path)) 
        {
            //Iterate directory
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() 
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException 
                {
                    try
                    {
                        //Index this file
                        indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
                    } 
                    catch (IOException | ParseException ioe) 
                    {
                        ioe.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } 
        else
        {
            //Index this file
            indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }
 
    static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException, ParseException 
    {
        try (InputStream stream = Files.newInputStream(file)) 
        {
        	// Gather all relevant JSONs for this file's documents
        	ArrayList<String> jsons = getJSONsFromPath(file.toString());
        	int size = jsons.size();
        	
        	for (int j = 0; j < size; j++) {
				// Increment out documents indexed stat
				numOfDocuments++;
				// Assign each page an index assuming i are files, and j are documents
				int documentIndex = numOfDocuments;
				
				//Get json fields
				JSONObject object = (JSONObject) new JSONParser().parse(jsons.get(j));
				
				String docno = object.get("docno").toString();
				String url = object.get("url").toString();
				String title = object.get("title").toString();
				String content = object.get("content").toString();
				
				
				//Create lucene Document
	            Document doc = new Document();
	             
	            doc.add(new StringField("path", file.toString(), Field.Store.YES));
	            doc.add(new LongPoint("modified", lastModified));
//	            doc.add(new TextField("contents", jsons.get(j), Store.YES));
	            
	            //new fields
	            doc.add(new StringField("url", url.toString(), Field.Store.YES)); //indexed but no tokenized
	            doc.add(new StringField("docno", docno.toString(), Field.Store.YES)); //indexed but no tokenized
	            doc.add(new TextField("title", title.toString(), Store.YES)); //indexed and tokenized (broken into keywords)
	            doc.add(new TextField("content", content.toString(), Store.YES)); //indexed and tokenized (broken into keywords)
	            
	            
	            
	            //Updates a document by first deleting the document(s) 
	            //containing <code>term</code> and then adding the new
	            //document.  The delete and then add are atomic as seen
	            //by a reader on the same index
	            
//	            writer.updateDocument(new Term("path", file.toString()), doc);
	            
	            writer.updateDocument(new Term("path", file.toString()), doc);
	            
//	            writer.updateDocument(new Term("docno", docno.toString()), doc);
//	            writer.addDocument(doc);
				
				

			}
        	
        }
    }
    
    public static ArrayList<String> getJSONsFromPath(String path) {
		// The String array to be passed back once complete
		ArrayList<String> jsons = new ArrayList<String>();

		try {
			// The entire document that is currently being parsed.
			org.jsoup.nodes.Document entireDoc = Jsoup.parse(new File(path), null); //specify it's jsoup document

			// Break each Document (<DOC>) down into it's own index
			// Pull the specified DOC element by index to be processed
			Elements docElements = entireDoc.select("doc");
			
			for(Element doc : docElements) {
				jsons.add(getJSONFromData(doc));
			}
		} catch (NullPointerException errNull) {
			errNull.printStackTrace();
		} catch (IndexOutOfBoundsException errIndex) {
			// This occurs when there are no more DOC tags to select, in which case return
			// the JSONs
			return jsons;
		} catch (IOException errIO) {
			errIO.printStackTrace();
		}

		return jsons;
	}
    
    public static String getJSONFromData(Element e) {
		try {
			String title = "", docno = "", olddocno = "", keywords = "", content = "";
			Element ele;
			
			// This can occur if some elements within a document aren't properly closed
			// If so, remove all elements after the first
			if(e.select("doc").size() > 1) {
				int i = 0;
				for(Element clean : e.select("doc")) {
					if (i != 0) { 
						clean.remove();
					}
					i++;
				}
			}

			// Only update the Strings if the elements exist
			if ((ele = e.selectFirst("title")) != null) {
				title = ele.text();
			}

			if ((ele = e.selectFirst("docno")) != null) {
				docno = ele.text();
			}

			if ((ele = e.selectFirst("docoldno")) != null) {
				olddocno = ele.text();
			}

			if ((ele = e.selectFirst("meta")) != null) {
				if (ele.hasAttr("name") && ele.attr("name") == "keywords") {
					if (ele.hasAttr("content")) {
						keywords = ele.attr("content");
						System.out.println("FOUND KEYWORDS: " + keywords);
					}
				}
			}

			// Gather only the body of the document as text content
			if ((ele = e.selectFirst("html")) != null) {
				content = ele.text();
			} else if ((ele = e.selectFirst("body")) != null) {
				content = ele.text();
			} else if ((ele = e.selectFirst("DOCHDR")) != null) {
				// If the document is malformed and is missing opening html and body tags
				// then instead strip out everything above and including the <DOCHDR>.
				// Then set the content as all remaining text for this <DOC>.
				int i = ele.elementSiblingIndex();
				
				// Recursively remove all siblings above the DOCHDR
				removePreviousSiblings(ele, i);
				content = e.text();
			} else {
				// In the unlikely scenario that the document is malformed and doesn't have an
				// html, body, or DOCHDR tag, then instead set the content as the entire <DOC>
				content = e.text();
			}
			
			// Store the document to be served in searches
//			storeDocument(docno, e.html()); //takes too much space

			// Build the json using the extracted data
			XContentBuilder builder = jsonBuilder().startObject().field("title", title).field("docno", docno)
					.field("olddocno", olddocno).field("keywords", keywords).field("content", content)
					.field("url", docno + ".html").endObject();
			
			return Strings.toString(builder);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return null;
	}
    
    
    public static void removePreviousSiblings(Element e, int jumpsLeft) {
		if (jumpsLeft > 0) {
			removePreviousSiblings(e.previousElementSibling(), jumpsLeft - 1);
		}

		e.remove();
	}
    
//    public static void storeDocument(String docno, String data) {
//		// Establish the results output path
//		String outputPath = System.getProperty("user.dir") + HTML_PATH_INTERNAL;
//		
//		String fileName = docno + ".html";
//				
//		// Create the output directories if they don't exist
//		new File(outputPath).mkdirs();
//		
//		try {
//			// Setup the writer to output to the file
//			BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath + fileName));
//			
//			// Write the data to the file
//			writer.write(data);
//			
//			// Close the writer when done
//			writer.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
    
    public static void decompress() {
		decompressDirectory("WT01");
		decompressDirectory("WT02");
		decompressDirectory("WT03");
	}
    public static void decompressDirectory(String folder) {
		// Establish paths to the given folders
		String filePath = System.getProperty("user.dir") + COMPRESSED_DATA_PATH + folder;
		String outputPath = System.getProperty("user.dir") + DECOMPRESS_PATH + folder;

		// Create the output directories if they don't exist
		new File(outputPath).mkdirs();

		// Gather all .GZ files in the directory
		File[] files = getFilesByExt(filePath, ".GZ");

		// Buffer used to read in data from file
		byte[] buffer = new byte[1024];

		try {
			if(files != null) {
				for (int i = 0; i < files.length; i++) {
					// Setup input streams to extract data from the file
					FileInputStream fis = new FileInputStream(files[i]);
					GZIPInputStream gis = new GZIPInputStream(fis);
	
					// The file name for the current File
					String fileName = files[i].getName();
					String outputName = fileName.substring(0, fileName.indexOf(".GZ")) + ".txt";
	
					// Setup output stream to push data into
					FileOutputStream fos = new FileOutputStream(outputPath + "\\" + outputName);
	
					// Stores data to be written
					int bytes_read;
	
					// Write data until the EOF has been reached
					while ((bytes_read = gis.read(buffer)) > 0) {
						fos.write(buffer, 0, bytes_read);
					}
	
					// Close streams when finished
					gis.close();
					fos.close();
				}
	
				System.out.println("Decompression complete for path:  " + filePath);
			} else {
				System.out.println("Error!  No .GZ files found in directory: " + filePath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    public static File[] getFilesByExt(String directory, String extension) {
		// Make the search case insensitive
		final String ext = extension.toUpperCase();
		File fileDir = new File(directory);

		// Returns a list of all files that match the extension
		return fileDir.listFiles(new FilenameFilter() {
			// Specify the acceptance filter
			public boolean accept(File fileDir, String fileName) {
				return fileName.toUpperCase().endsWith(ext);
			}
		});
	}
    
    
    
}

/**
 * Assignment 6
 * @author Azulia Yang
 * Collaborator: Victor Barbaros
 */

package searchengine;

import java.util.*;
import java.io.*;

// This class implements a google-like search engine
public class searchEngine {

    public HashMap<String, LinkedList<String> > wordIndex;   // this will contain a set of pairs (String, LinkedList of Strings)	
    public directedGraph internet;             // this is our internet graph
    
       
    // Constructor initializes everything to empty data structures
    // It also sets the location of the internet files
    searchEngine() {
	// Below is the directory that contains all the internet files
	htmlParsing.internetFilesLocation = "internetFiles";
	wordIndex = new HashMap<String, LinkedList<String> > ();		
	internet = new directedGraph();				
    } // end of constructor2015
    
    
    // Returns a String description of a searchEngine
    public String toString () {
	return "wordIndex:\n" + wordIndex + "\ninternet:\n" + internet;
    }
    
    
    // This does a graph traversal of the internet, starting at the given url.
    // For each new vertex seen, it updates the wordIndex, the internet graph,
    // and the set of visited vertices.
    
    void traverseInternet(String url) throws Exception {
               
        //building the graph via BFS
        //visiting the link's neighbours first(the websites it links to), and then its neighbours' neighbours and so on until all websites are visited        
       
        Queue<String> queue = new LinkedList<>();
        List<String> list = new ArrayList<String>();
        
        queue.add(url);
        
        while(!queue.isEmpty()){
            
            String temp = queue.remove();
            list.add(temp); // this list keep track of parsed links
           
            if(!(internet.getVertices().contains(temp))){
                internet.addVertex(temp); // adds current link as a vertex 
            }
            
            LinkedList<String> links = htmlParsing.getLinks(temp); // contains hyperlinks going out of given url        
            Iterator<String> linkIterator = links.iterator(); //to iterate through links             
            
            while(linkIterator.hasNext()){ //visits all the websites that the current webpage directs to
                
                String link = linkIterator.next();
                internet.addEdge(temp, link); // if the link is already a vertex, simply ass an edge from temp to link

                if(!queue.contains(link) && !list.contains(link)){ //to prevent from going back to the same website again
                    queue.add(link);
                }               
            }
        }       
            
         //this is to itearate through the the content of the given url to store all the words found on the page, again, using BFS
        //Using the graph that was previously built
            
        Queue<String> linkQueue = new LinkedList<>();
            
            linkQueue.add(url);

            while(!linkQueue.isEmpty()){
                String currentLink = linkQueue.remove();
                internet.setVisited(currentLink, true);
                
                LinkedList<String> neighbors = internet.getNeighbors(currentLink); //contains all neighbors of the currentLink we're interested in
                Iterator<String> neighborIterator = neighbors.iterator();
                
                LinkedList<String> content = htmlParsing.getContent(currentLink); //contains all content of the currentLink we're interested in
                Iterator<String> contentIterator = content.iterator();
                
                while(contentIterator.hasNext()){ //goes through all links on the current webpage 
                    String currentContent = contentIterator.next();
                    
                   if(!wordIndex.containsKey(currentContent)){ //if wordIndex does not contain the key, create a new LinkedList with the current website as content
                       LinkedList<String> newLink = new LinkedList<>();
                       newLink.add(currentLink);
                       wordIndex.put(currentContent, newLink); //put both the newly created LinkedList and the key word into the wordIndex
                   }
                   
                   else if(wordIndex.containsKey(currentContent)){ //if key is already in wordIndex, update the LinkedList associated to it
                       LinkedList<String> newValue = wordIndex.get(currentContent);
                       newValue.add(currentLink);
                       wordIndex.put(currentContent,newValue);
                       //updates the wordIndex at the specified key which already exists
                   }                                     
                 }
                
                while(neighborIterator.hasNext()){ 
                    String s  = neighborIterator.next();
                    if(!internet.getVisited(s) && !linkQueue.contains(s)){
                        linkQueue.add(s); //adds unvisited neighbors that are not alraedy in linkQueue
                    }
                }               
            }	
    } // end of traverseInternet
    
    
    /* This computes the pageRanks for every vertex in the internet graph.
       It will only be called after the internet graph has been constructed using 
       traverseInternet.
       Use the iterative procedure described in the text of the assignment to
       compute the pageRanks for every vertices in the graph. 
       
       This method will probably fit in about 30 lines.
    */
    void computePageRanks() {
        
        LinkedList<String> vertices = internet.getVertices(); //gets all websites(vertices)
        Iterator<String> verticesIterator = vertices.iterator();
        
        while(verticesIterator.hasNext()){
            String s = verticesIterator.next();           
            internet.setPageRank(s, 1); // sets all page rank of all websites to 1 by default
        }
               
        for (int i = 0; i<=100; i++){ //does about 100 iterations to calculate the page rank for each website on the graph 
         
        Iterator<String> vertices2 = vertices.iterator();
            while(vertices2.hasNext()){
                String temp = vertices2.next();
                double pageRank = 0.5; //default value of pageRank
                
                LinkedList<String> edgesInto = internet.getEdgesInto(temp); //get all websites linking into temp
                Iterator<String> edgeIterator = edgesInto.iterator();   
                
                while(edgeIterator.hasNext()){ //iterates through all the websites linking into temp
                    String edge = edgeIterator.next();
                    pageRank = pageRank + 0.5*(internet.getPageRank(edge)/internet.getOutDegree(edge)); //calculates pageRank iteratively
                }  
                internet.setPageRank(temp, pageRank); // updates the pageRank of temp
            }
        }
    } // end of computePageRanks
    
	
    /* Returns the URL of the page with the high page-rank containing the query word
       Returns the String "" if no web site contains the query.
       This method can only be called after the computePageRanks method has been executed.
       Start by obtaining the list of URLs containing the query word. Then return the URL 
       with the highest pageRank.
       This method should take about 25 lines of code.
    */
    String getBestURL(String query) {
        
        String best = ""; //to be the best url
        double ranking = 0;
	if(wordIndex.containsKey(query.toLowerCase())){ //this is a non-case sensitive search, so simply converts the query to lower case to make things easier
            LinkedList<String> websites = wordIndex.get(query); //creates LinkedList with the necessary websites to compare
            Iterator<String> webIterator = websites.iterator();
            
            best = webIterator.next(); //sets best as the first website in the iterator
            
            while(webIterator.hasNext()){
                 String web = webIterator.next();
                 double temp = internet.getPageRank(web); //stores the current page rank in a temporary value
                 if (temp> ranking){ //compares the temporary value with the value stored in ranking
                     ranking = temp; //updates the ranking(if applies)
                     best = web; //updates the best website(if applies)
                 }
            } 
        }
        return best;
    } // end of getBestURL
    
    
	
    public static void main(String args[]) throws Exception{		
	searchEngine mySearchEngine = new searchEngine();
	// to debug your program, start with.
//		mySearchEngine.traverseInternet("http://www.cs.mcgill.ca/~blanchem/250/a.html");
	
	// When your program is working on the small example, move on to
	mySearchEngine.traverseInternet("http://www.cs.mcgill.ca");
	
	mySearchEngine.computePageRanks();
	
	BufferedReader stndin = new BufferedReader(new InputStreamReader(System.in));
	String query;
	do {
	    System.out.print("Enter query: ");
	    query = stndin.readLine();
	    if ( query != null && query.length() > 0 ) {
		System.out.println("Best site = " + mySearchEngine.getBestURL(query));
	    }
	} while (query!=null && query.length()>0);				
    } // end of main
}

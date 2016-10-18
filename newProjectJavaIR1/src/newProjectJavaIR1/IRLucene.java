package newProjectJavaIR1;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.document.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import java.util.Map.Entry;

public class IRLucene {
	static IRLucene temp = new IRLucene();
	public static HashMap<String, LinkedList<Link>> index1 = new HashMap<>();

	public static void main(String[] args) throws IOException {

		//Generate Inverted Index
		String indexLoc= args[0];//replace absolute path with indexLoc
		File f = new File(args[0]); // current directory-C:/Users/Raghav Nayak/Desktop/index
																	
		Directory directory = FSDirectory.open(f.toPath());
		IndexReader indexReader = DirectoryReader.open(directory);

		String[] langs = { "text_es", "text_fr", "text_it", "text_ru", "text_sv", "text_pt", "text_nl", "text_de",
				"text_no", "text_da", "text_ja" };

		Fields fields = MultiFields.getFields(indexReader);
		for (String field : fields) {
			for (String lang : langs) {
				if (field.startsWith(lang)) {
					Terms terms = fields.terms(field);
					TermsEnum termsEnum = terms.iterator();

					BytesRef byteRef = null;
					while ((byteRef = termsEnum.next()) != null) {
						String term1 = byteRef.utf8ToString();
						PostingsEnum postingsEnum = MultiFields.getTermDocsEnum(indexReader, lang, byteRef,
								PostingsEnum.FREQS);
						int i;
						LinkedList<Link> tempDocList = new LinkedList<>();

						// For every doc which contains the current term in the
						// "Text" field:
						while ((i = postingsEnum.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
							Document doc = indexReader.document(i); // The
																	// document
							int docId = postingsEnum.docID();

							Link list = temp.new Link();
							list.docNo = docId;

							tempDocList.add(list);
						}

						index1.put(term1, tempDocList);
					}
				}
			}
		}

		// Program starts here...
		
		 ArrayList<String> inputString = new ArrayList<>();
		 
		 //Read input.txt to get the terms 
		 File inputFile = new File(args[2]);
		 BufferedReader reader=null; 
		 try{ 
			 reader = new BufferedReader(new FileReader(inputFile)); 
			 String text = null; 
			 while ((text =reader.readLine()) != null) { 
				 inputString.add(text); 
			 	} 
		 	}catch (FileNotFoundException e) { 
				 e.printStackTrace(); 
			}catch (IOException e) { 
				e.printStackTrace(); 
			} 
		 finally { 
			 try { 
				 if (reader != null) {
						reader.close(); 
						} 
				 } catch (IOException e) { } 
			 }
		 for(int x=0;x<inputString.size();x++){ 
		 getPostings(inputString.get(x)); 
		 taatOR(inputString.get(x));
		 taatAND(inputString.get(x)); 
		 daatOR(inputString.get(x)); 
		 }
		 
		 PrintStream out = new PrintStream(new FileOutputStream(args[1]));
		 System.setOut(out);

		//Testing...
		/*getPostings("afskrivning amtssparekas");
		taatAND("afskrivning amtssparekas");
		taatOR("afskrivning amtssparekas");
		daatAND("afskrivning amtssparekas");
		daatOR("afskrivning amtssparekas");*/
	}

	/*-----------------------------------------------------------------------------------------------------------*/
	// Get Postings List
	public static void getPostings(String input) {
		String[] queries = input.split(" ");
		for (String query : queries) {
			LinkedList<Link> termlist = index1.get(query);

			String pList = "";
			for (Iterator iterator = termlist.iterator(); iterator.hasNext();) {
				Link post = (Link) iterator.next();
				pList = pList + post.docNo + " ";
			}
			pList = pList.substring(0, pList.lastIndexOf(' '));
			System.out.println("GetPostings");
			System.out.println(query);
			System.out.println(pList);
		}
	}

	/*----------------------------------------------------------------------------------------------------------*/
	// Term-At-A-Time OR
	public static void taatOR(String query) {
		String[] inputs = query.split(" ");

		ArrayList<Integer> temp = new ArrayList<>();
		ArrayList<Integer> Final = new ArrayList<>(); 

		int comparisions = 0;

		for (String input : inputs) {
			LinkedList<Link> termlist = index1.get(input);
			for (Iterator iterator = termlist.iterator(); iterator.hasNext();) {
				Link post = (Link) iterator.next();
				temp.add(post.docNo);
			}
			if (Final.size() == 0) {
				Final.addAll(temp);
				temp.clear();
			} else if (Final.size() != 0) {
				for (int m = 0; m < temp.size(); m++) {
					comparisions++;
					if (!temp.get(m).equals(Final.contains(temp.get(m)))) {
						if(!Final.contains(temp.get(m))){
						Final.add(temp.get(m));}
					} else if (temp.get(m).equals(Final.contains(temp.get(m)))) {
						break;
					}
				}
			}
			temp.clear();
		}
		System.out.println("TaatOr");
		for (String z : inputs) {
			System.out.print(z + " ");
		}
		System.out.println(" ");
		Collections.sort(Final);
		if(Final.size()==0){System.out.println("Results: Empty");}
		else if(Final.size()>0){
		System.out.print("Results: ");
		Iterator<Integer> itr = Final.iterator();
		while (itr.hasNext()) {
			System.out.print(itr.next().toString() + " ");
		}
		}
		System.out.println(" ");
		System.out.println("Number of documents in result: " + Final.size());
		System.out.println("Number of comparisions: " + comparisions);
	}

	/*----------------------------------------------------------------------------------------------------------*/
	// Term-At-A-Time AND
	public static void taatAND(String query) {
		String[] inputs = query.split(" ");

		ArrayList<Integer> temp = new ArrayList<>();
		ArrayList<Integer> Final = new ArrayList<>();
		ArrayList<Integer> FinalAND = new ArrayList<>(); 
		HashMap<String, ArrayList<String>> storeLists = new HashMap<>();

		int comparisions = 0;

		for (String input : inputs) {
			LinkedList<Link> termlist = index1.get(input);
			for (Iterator iterator = termlist.iterator(); iterator.hasNext();) {
				Link post = (Link) iterator.next();
				temp.add(post.docNo);
			}
			if (inputs.length == 1) {
				FinalAND.addAll(temp);
			} else if (inputs.length >= 2) {
				if (Final.size() == 0) {
					Final.addAll(temp);
				} else if (Final.size() >= 1) {
					for (int m = 0; m < Final.size(); m++) {
						comparisions++;
						for (int n = 0; n < temp.size(); n++) {
							if (temp.get(n).equals(Final.get(m))) {
								if(!FinalAND.contains(temp.get(n))){
								FinalAND.add(temp.get(n));}
							}
						}
					}
				}
			}

			temp.clear();
		}
		System.out.println("TaatAnd");
		for (String z : inputs) {
			System.out.print(z + " ");
		}
		System.out.println(" ");
		Collections.sort(FinalAND);
		if(FinalAND.size()==0){System.out.println("Results: Empty");}
		else if(FinalAND.size()>0){
		System.out.print("Results: ");
		Iterator<Integer> itr = FinalAND.iterator();
		while (itr.hasNext()) {
			System.out.print(itr.next().toString() + " ");
		}
		}
		System.out.println(" ");
		System.out.println("Number of documents in results: " + FinalAND.size());
		System.out.println("Number of comparisions: " + comparisions);
	}

	/*-------------------------------------------------------------------------------------------------------------*/
	// Document-At-A-Time AND
	public static void daatAND(String query) {

		String[] inputs = query.split(" ");

		ArrayList<LinkedList<Link>> arrayOfPostingsList = new ArrayList<>();
		ArrayList<Integer> FinalAND = new ArrayList<>(); 
		int comparisions = 0;

		// If only one term given in argument..
		if (inputs.length == 1) {
			LinkedList<Link> termlist = index1.get(query);
			for (Iterator iterator = termlist.iterator(); iterator.hasNext();) {
				Link post = (Link) iterator.next();
				FinalAND.add(post.docNo);
			}
			return;
		}

		// If many terms give in argument..
		for (String input : inputs) {
			LinkedList<Link> termlist1 = index1.get(input);
			// cloning
			arrayOfPostingsList.add(new LinkedList(termlist1));
		}

		// Sort the ArrayList as per LinkedList size - Ascending Order
		Collections.sort(arrayOfPostingsList, new Comparator<LinkedList<Link>>() {
			public int compare(LinkedList<Link> o1, LinkedList<Link> o2) {
				// TODO Auto-generated method stub
				if (o1.size() < o2.size()) {
					return -1;
				} else if (o1.size() > o2.size()) {
					return 1;
				} else {
					return 0;
				}
			}
		});

		boolean done = false;
		ArrayList<Integer> collector = new ArrayList<>();

		while (!done) {

			LinkedList<Link> pList = arrayOfPostingsList.get(0);
			if (pList.isEmpty()) {
				break;
			}

			int prev = pList.getFirst().docNo;
			int matches = 1;

			int max = prev;
			int maxIndex = 0;

			for (int i = 1; i < arrayOfPostingsList.size(); i++) {
				LinkedList<Link> linkedList = arrayOfPostingsList.get(i);
				if (linkedList.isEmpty()) {
					done = true;
					matches = -1;
					break;
				}
				int curr = linkedList.getFirst().docNo;

				if (curr == prev) {
					matches++;
					comparisions++;
					prev = curr;
				}

				if (curr > max) {
					max = curr;
					comparisions++;
					maxIndex = i;
				}
			}

			if (matches == arrayOfPostingsList.size()) {
				// add the max element and any head to the result because they
				// match remove the head of the linked list

				collector.add(max);

				for (int i = 0; i < arrayOfPostingsList.size(); i++) {

					LinkedList<Link> l = arrayOfPostingsList.get(i);
					if (!l.isEmpty()) {
						l.removeFirst();
					}
					if (l.isEmpty()) {
						// break;
						// done here
						done = true;
					}
				}
			} else {
				// move the head to point to a location which matches max or the
				// element just greater than max or till the end of the list.

				for (int i = 0; i < arrayOfPostingsList.size(); i++) {

					if (i == maxIndex) {
						continue;
					}

					LinkedList<Link> l = arrayOfPostingsList.get(i);
					while (!l.isEmpty() && l.getFirst().docNo < max) {
						l.removeFirst();
					}
					if (l.isEmpty()) {
						// break;
						// done here
						done = true;
					}
				}
			}
		}
		System.out.println("DaatAnd");
		for (String z : inputs) {
			System.out.print(z + " ");
		}
		System.out.println(" ");
		
		//StringBuilder sb = new StringBuilder();
		for (Integer i : collector) {
			//sb.append(i + " ");
			FinalAND.add(i);
		}
		Collections.sort(FinalAND);
		if(FinalAND.size()==0){System.out.println("Results: Empty");}
		else if(FinalAND.size()>0){
			System.out.print("Results: ");
			Iterator<Integer> itr = FinalAND.iterator();
			while (itr.hasNext()) {
				System.out.print(itr.next().toString() + " ");
			}
		}
		System.out.println("");
		System.out.println("Number of documents in results: " + FinalAND.size());
		System.out.println("Number of comparisions: " + comparisions);

	}
	
	/*--------------------------------------------------------------------------------------------------------------------*/
	// Document-At-A-Time OR
		public static void daatOR(String query) {

			String[] inputs = query.split(" ");

			ArrayList<LinkedList<Link>> arrayOfPostingsList = new ArrayList<>();
			ArrayList<Integer> FinalOR = new ArrayList<>();
			int comparisions = 0;

			// If only one term given in argument..
			if (inputs.length == 1) {
				LinkedList<Link> termlist = index1.get(query);
				for (Iterator iterator = termlist.iterator(); iterator.hasNext();) {
					Link post = (Link) iterator.next();
					FinalOR.add(post.docNo);
				}
				return;
			}

			// If many terms give in argument..
			for (String input : inputs) {
				LinkedList<Link> termlist1 = index1.get(input);
				// cloning
				arrayOfPostingsList.add(new LinkedList(termlist1));
			}

			// Sort the ArrayList as per LinkedList size- Descending Order
			Collections.sort(arrayOfPostingsList, new Comparator<LinkedList<Link>>() {
				public int compare(LinkedList<Link> o1, LinkedList<Link> o2) {
					// TODO Auto-generated method stub
					if (o1.size() < o2.size()) {
						return 1;
					} else if (o1.size() > o2.size()) {
						return -1;
					} else {
						return 0;
					}
				}
			});
			
			
			boolean done=false;
			boolean repeat=false;
			while(!done){
				LinkedList<Link> pList = arrayOfPostingsList.get(0);
				if (pList.isEmpty()) {
					break;
				}
				int prev = pList.getFirst().docNo;
				FinalOR.add(prev);
				
				for(int i=1;i<arrayOfPostingsList.size();i++){
					LinkedList<Link> linkedList = arrayOfPostingsList.get(i);
					if (linkedList.isEmpty()) {
						done = true;
						break;
					}
					int curr = linkedList.getFirst().docNo;

					if (curr != prev) {
						FinalOR.add(curr);
						comparisions++;
					}
					if(curr == prev){
						if(!FinalOR.contains(curr)){
							FinalOR.add(curr);
						}
					}
				}
					for(int i=0; i<arrayOfPostingsList.size();i++){
						LinkedList<Link> l = arrayOfPostingsList.get(i);
						if(!l.isEmpty()){
							l.removeFirst();
						}
						else if(l.isEmpty()){
							arrayOfPostingsList.remove(i);
							done=true;
						}
					}
				
				
			}
			System.out.println("DaatOr");
			for (String z : inputs) {
				System.out.print(z + " ");
			}
			System.out.println(" ");
			Collections.sort(FinalOR);
			if(FinalOR.size()==0){System.out.println("Results: Empty");}
			else if(FinalOR.size()>0){
				System.out.print("Results: ");
				Iterator<Integer> itr = FinalOR.iterator();
				while (itr.hasNext()) {
					System.out.print(itr.next().toString() + " ");
				}
				System.out.println(" ");
			}
			System.out.println("Number of documents in result: " + FinalOR.size());
			System.out.println("Number of comparisions: " + comparisions);
					
		}

	/*-------------------------------------------------------------------------------------------------------*/
	// Print HashMaps for testing purposes..
	public static void printHashMaps() {
		Set<Map.Entry<String, LinkedList<Link>>> entrySet1 = index1.entrySet();
		Iterator<Entry<String, LinkedList<Link>>> entrySetIterator = entrySet1.iterator();
		while (entrySetIterator.hasNext()) {
			// System.out.println("------------------------------------------------");
			Entry<String, LinkedList<Link>> entry = entrySetIterator.next();
			LinkedList<Link> postingsList = entry.getValue();
			System.out.print("key: " + entry.getKey() + " value: ");
			for (Link posting : postingsList) {
				System.out.print(posting.docNo + ", ");
			}
			System.out.println("");
		}
	}

	public class Link {
		public int docNo;
		public int termF;
	}
}

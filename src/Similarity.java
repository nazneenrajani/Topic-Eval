import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import Jama.Matrix;


public class Similarity {
	static final int topics = 299;
	static int NOW=4016 ;
	static Map<String,Integer> words = new HashMap<String,Integer>();
	static double[][] LDAbow = new double[topics][NOW];
	static double[][] ATbow = new double[topics][NOW];
	static double[][] result = new double[topics][topics];
	static double[][] dotresult = new double[topics][topics];
	static Set<String> temp = new HashSet<String>();

	public static void main(String[] args){
		diagnose();
		test();
		topics();
		//dotproduct();
	}

	private static void dotproduct() {
		for(int j = 0; j <topics;j++)
			for(int i = 0; i < topics; i ++)
				dotresult[j][i] = 0.0;
		/*AT is down, rowwise and LDA is across columnwise*/
		for(int k = 0;k < topics; k++){
			double[] B = ATbow[k];
			double[] P = B;
			Arrays.sort(P);
			//			for(int i = 0;i<P.length; i++)
			//				System.out.println(P[i]);
			double[] Pdash = new double[NOW];
			for(int j = 0;j <P.length; j ++){
				Pdash[j] = P[NOW-j-1];
				//System.out.println(Pdash[j]);
			}
			for(int i = 0; i<topics ; i++){
				double[] A = LDAbow[i];
				double[] Qreverse = A;
				Arrays.sort(Qreverse);
				double[] Qdash = new double[NOW];
				for(int j = 0;j <Qreverse.length; j ++)
					Qdash[j] = Qreverse[NOW-j-1];
				dotresult[k][i] = rescaledDotProduct(new Matrix(A,NOW),new Matrix(B,NOW), new Matrix(Pdash,NOW), new Matrix(Qdash, NOW), new Matrix(Qreverse,NOW));
			}
		}
				try {
					File file = new File("rescaled_results");
					if (!file.exists()) {
						file.createNewFile();
					}
		
					FileWriter fw = new FileWriter(file.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					for(int k = 0;k < topics; k++){
						for(int i = 0; i<topics ; i++){
							bw.write(String.format("%.7f",dotresult[k][i]));
							bw.write("\t");
						}
						bw.write("\n");
					}
					bw.close();
		
				} catch (IOException e) {
					e.printStackTrace();
				}

	}

	private static void topics() {
		for(int j = 0; j <topics;j++)
			for(int i = 0; i < topics; i ++)
				result[j][i] = 0.0;
		/*AT is down, rowwise and LDA is across columnwise*/
		for(int k = 0;k < topics; k++){
			double[] B = ATbow[k];
			for(int i = 0; i<topics ; i++){
				double[] A = LDAbow[i];		
				result[k][i] = computeSimilarity(new Matrix(A,NOW),new Matrix(B,NOW));
				if(result[k][i]>0.3){
					System.out.print(i+" ");
					//System.out.print(" ");
				}
			}
			System.out.println();
		}
		
//		for(int i =0; i<result[0].length;i++)
//			System.out.println(result[0][i]);
//		try {
//			File file = new File("cosine_results");
//			if (!file.exists()) {
//				file.createNewFile();
//			}
//
//			FileWriter fw = new FileWriter(file.getAbsoluteFile());
//			BufferedWriter bw = new BufferedWriter(fw);
//			for(int k = 0;k < topics; k++){
//				for(int i = 0; i<topics ; i++){
//					bw.write(String.format("%.4f",result[k][i]));
//					bw.write("\t");
//				}
//				bw.write("\n");
//			}
//			bw.close();
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

	}

	private static void test() {
		int count = 0;
		for(String w: temp){
			words.put(w,count);
			count++;
		}
		System.out.println(NOW);
		BufferedReader nbr,br;	
		for(int i =0; i< topics;i++){
			for(int j = 0;j<NOW;j++){
				LDAbow[i][j]=0.0;
			}
		}
		for(int i =0; i< topics;i++){
			for(int j = 0;j<NOW;j++){
				ATbow[i][j]=0.0;
			}
		}
		try {
			br= new BufferedReader(new FileReader("1_ART_300"));
			String number,tmp;
			String[] line;
			int row = 0;
			while((number=br.readLine())!=null){
				//Map<String,Double> topicWords = new HashMap<String,Double>();
				if(number.equals(""))
					continue;
				if(number.replaceAll(" ", "").startsWith("TOPIC")){
					number=br.readLine();
					for(int k = 0; k<4193 ; k++){
						number = br.readLine();
						line =number.replaceAll(" ","").split("\t");
						tmp = line[0].toLowerCase();
						//topicWords.put(tmp,Double.parseDouble(line[1]));
						if(!words.containsKey(tmp))
							continue;
						LDAbow[row][words.get(tmp)] = Double.parseDouble(line[1]);
					}
					row++;
				}
			}
			nbr= new BufferedReader(new FileReader("1_AT_300"));
			row=0;
			while((number=nbr.readLine())!=null){
				Map<String,Double> topicWords = new HashMap<String,Double>();
				if(number.equals(""))
					continue;
				if(number.replaceAll(" ", "").startsWith("TOPIC")){
					number=nbr.readLine();
					for(int k = 0; k<4193 ; k++){
						number = nbr.readLine();
						line =number.replaceAll(" ","").split("\t");
						tmp = line[0].toLowerCase();
						//topicWords.put(tmp,Double.parseDouble(line[1]));
						if(!words.containsKey(tmp))
							continue;
						ATbow[row][words.get(tmp)] = Double.parseDouble(line[1]);
					}
					row++;
				}
			}
			br.close();
			nbr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void diagnose() {
		BufferedReader nbr,br;
		try {
			br= new BufferedReader(new FileReader("1_ART_300"));
			String number,tmp;
			String[] line;
			int count = 0;
			boolean flag = true;
			while((number=br.readLine())!=null){
				Set<String> t = new HashSet<String>();
				if(number.equals(""))
					continue;
				if(number.replaceAll(" ", "").startsWith("TOPIC")){
					number=br.readLine();
					for(int k = 0; k<4193 ; k++){
						number = br.readLine();
						line =number.replaceAll(" ","").split("\t");
						tmp = line[0].toLowerCase();
						t.add(tmp);
					}
					if(flag){
						temp = t;
						System.out.println("temp: "+temp.size());
						flag = false;
					}
					else
						temp.retainAll(t);
					//System.out.println("temp: "+temp.size());
				}
			}
			nbr= new BufferedReader(new FileReader("1_AT_300"));
			while((number=nbr.readLine())!=null){
				Set<String> t = new HashSet<String>();
				if(number.equals(""))
					continue;
				if(number.replaceAll(" ", "").startsWith("TOPIC")){
					number=nbr.readLine();
					for(int k = 0; k<4193 ; k++){
						number = nbr.readLine();
						line =number.replaceAll(" ","").split("\t");
						tmp = line[0].toLowerCase();
						t.add(tmp);
					}
					temp.retainAll(t);
				}
			}
			br.close();
			nbr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public static boolean isInteger(String s) {
		try { 
			Long.parseLong(s); 
		} catch(NumberFormatException e) { 
			return false; 
		}
		return true;
	}
	public static double computeSimilarity(Matrix sourceDoc, Matrix targetDoc) {
		double dotProduct = sourceDoc.arrayTimes(targetDoc).norm1();
		double eucledianDist = sourceDoc.normF() * targetDoc.normF();
		return dotProduct / eucledianDist;
	}

	public static double rescaledDotProduct(Matrix sourceDoc, Matrix targetDoc, Matrix Pdash, Matrix Qdash, Matrix Qreverse) {
		double dotProduct = sourceDoc.arrayTimes(targetDoc).norm1();
		double eucledianDist = sourceDoc.normF() * targetDoc.normF();
		double dot =  dotProduct / eucledianDist;
		double dmin = Pdash.arrayTimes(Qreverse).norm1();
		double dminDist = Pdash.normF() * Qreverse.normF();
		double dmax = Pdash.arrayTimes(Qdash).norm1();
		double dmaxDist = Pdash.normF() * Qdash.normF();
		double dmi = dmin/dminDist;
		double dma = dmax/dmaxDist;
		//System.out.println(dot);
		double num = dot-dmi;
		double den = dma-dmi;
		double result = num/den;
		//System.out.println(result);
		return result;
	}

}

package com.selenium.webscrape;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

public class mergeData {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FileWriter fileWriter = null;
		BufferedReader fileReader = null;
		String line = "";
		try {
			//Consolidate all stats into single file
			fileWriter = new FileWriter(System.getProperty("user.home") + "/NBA_stats.csv");
			//Loop through all files containing each letter
			for(int i = 97; i < 123; i++) {
				if(i == 120) {
					continue;
				}
				fileReader = new BufferedReader(new FileReader(System.getProperty("user.home")
						+ "/" + String.valueOf((char) i) + "_NBA_stats.csv"));
				//Add header if this is the first file
				if(i == 97) {
					fileWriter.append(fileReader.readLine() + "\n");
				} else {
					//Skip header if not first file
					fileReader.readLine();
				}
				while((line = fileReader.readLine()) != null) {
						fileWriter.append(line + "\n");
				}
				try {
					fileReader.close();
				} catch(Exception e) {
					System.out.println("Error closing fileReader");
					e.printStackTrace();
				}
			}
		} catch(Exception e) {
			System.out.println("Error in CSVMerge");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.close();
			} catch (Exception e) {
				System.out.println("Error closing fileReader");
				e.printStackTrace();
			}
		}
	}

}

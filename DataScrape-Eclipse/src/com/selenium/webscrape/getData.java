package com.selenium.webscrape;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.selenium.webscrape.playerDB;

public class getData {

	public static void main(String[] args) {		
		//Setup webscraper
		System.setProperty("webdriver.chrome.driver", "C:\\Selenium\\chromedriver.exe");
		
		//Setup URL strings
		String baseAddress = "https://www.basketball-reference.com/players/";
		String currentLetter = "a";
		StringBuilder address = new StringBuilder(baseAddress + currentLetter);
		
		//Database of player names and stats stored by year
		ArrayList<playerDB> database = new ArrayList<playerDB>();
		
		//Iterate through URLs of desired years and add to Hashmap of stats
		for(int i = 121; i < 123; i++) {//i = 97; i < 123; i++) {
			address.replace(baseAddress.length(), baseAddress.length() + 1, 
					String.valueOf((char) i));
			for(playerDB player : parsePlayers(address.toString())) {
				database.add(player);
			}
			//Write current letter to file
			writeCSV((char) i, database);
			database.clear();
		}
	}
	
	public static ArrayList<playerDB> parsePlayers(String address) {
		//Initialize current players, driver, and table
		String currPlayer;
		ChromeOptions options = new ChromeOptions();
		options.addExtensions(new File("C:\\Selenium\\abp.crx"));
		WebDriver driver = new ChromeDriver(options);
		driver.get(address);
		WebElement playerTable;
		List<WebElement> playerLinks;
		ArrayList<ArrayList<Double>> totalStats = new ArrayList<ArrayList<Double>>();
		ArrayList<playerDB> database = new ArrayList<playerDB>();
		
		//Navigate to table and start iterating through
		try {
			driver.findElement(By.id("players_control")).click();
		} catch(Exception e) {}
		playerTable = driver.findElement(By.xpath("//table[@id='players']/tbody"));
		playerLinks = playerTable.findElements(By.xpath(".//tr/th/strong/a"));
		
		//Parse the html table and add data to our table 
		for(WebElement we : playerLinks) {
			try {
				currPlayer = we.getText();
				playerDB player = new playerDB(currPlayer);
				totalStats = parseTable(we.getAttribute("href"));
				for(ArrayList<Double> item : totalStats) {
					player.addStats(item);
				}
				//Add current players stats to list
				database.add(player);
			} catch (Exception e) {
				continue;
			}
		}
		driver.quit();
		return database;
	}
	
	public static ArrayList<ArrayList<Double>> parseTable(String address) {		
		//Total stats: Year, Exp, Age, Tm, Pos, G, Gs, Mp, Fg, FgA, Fg%, 3P, 3PA, 3P%,
		//2P, 2PA, 2P%, eFG%, FT, FTA, FT%, ORB, DRB, TRB, Ast, Stl, Blk, Tov, PF, Pts
		ArrayList<ArrayList<Double>> totalStats = new ArrayList<ArrayList<Double>>();
		
		//Initialize data from each year
		ArrayList<Double> currYear = new ArrayList<Double>();
		WebElement statsTable;
		List<WebElement> statsYear, statsCurrYear;
		String currAttr, currStat;
		double exp = 0;
		
		//Add stats year by year to the database for that player
		ChromeOptions options = new ChromeOptions();
		options.addExtensions(new File("C:\\Selenium\\abp.crx"));
		WebDriver driver = new ChromeDriver(options);
		driver.get(address);
		statsTable = driver.findElement(By.xpath("//table[@id='per_game']/tbody"));
		statsYear = statsTable.findElements(By.xpath(".//tr[@class='full_table']"));
		
		//Iterate through full years (ignore incomplete years)
		for(WebElement we : statsYear) {
			currYear.clear();
			currYear.add(Double.parseDouble(we.getAttribute("id").substring(9)));
			currYear.add(exp);
			exp += 1;
			statsCurrYear = we.findElements(By.xpath(".//td"));
			
			//Iterate through stats for each year
			for(WebElement we2 : statsCurrYear) {
				currAttr = we2.getAttribute("data-stat");
				currStat = we2.getText();
				if(currStat.equals("")) {
					currYear.add(0.0);
				}
				
				//Check if we have team/league/position stat
				else if(currAttr.equals("team_id")) {
					
					//Use teams record that year as stat
					try {
						WebElement team = we2.findElement(By.xpath(".//a"));
						WebDriver newDriver = new ChromeDriver(options);
						newDriver.get(team.getAttribute("href"));
						List<WebElement> wl = newDriver.findElements(
								By.xpath("//table[@id='team_misc']/tbody/tr/td"));
						for(WebElement we3 : wl) {
							int wins = 0; int total = 0;
							if(we3.getAttribute("data-stat").equals("wins_pyth")) {
								wins = Integer.parseInt(we3.getText());
								total = Integer.parseInt(we3.getText());
							}
							if(we3.getAttribute("data-stat").equals("losses_pyth")) {
								total += Integer.parseInt(we3.getText());
								currYear.add((double) wins/total);
								break;
							}
						}
						newDriver.quit();
					} catch(Exception e) {
						
						//Special case where we can't find team record
						currYear.add(0.5000);
					}
				}
				else if(currAttr.equals("lg_id")) {
					
					//Ignore
					continue;
				}
				else if(currAttr.equals("pos")) {
					
					//Record position
					List<String> positions = new ArrayList<>
					(Arrays.asList("PG", "SG", "SF", "PF", "C"));
					try {
						currYear.add((double) 1 + positions.indexOf(currStat.substring(0, 2)));
					} catch(Exception e) {
						currYear.add((double) 1 + positions.indexOf(currStat.substring(0, 1)));
					}
				}
				else {
					
					//If not, add to stats for this year
					currYear.add(Double.parseDouble(currStat));
				}
			}
			totalStats.add((ArrayList<Double>) currYear.clone());
		}
		driver.quit();
		return totalStats;
	}
	
	public static void writeCSV(char letter, ArrayList<playerDB> database) {
		//Setup CSV file
		final String COMMA = ",";
		final String NEW_LINE = "\n";
		final String FILE_HEADER = 
			"Name,Year,Exp,Age,Tm,Pos,G,Gs,Mp,Fg,FgA,Fg%,3P,3PA,3P%,2P,2PA,2P%,eFG%,FT,FTA,FT%,ORB,DRB,TRB,Ast,Stl,Blk,Tov,PF,Pts";
		FileWriter fileWriter = null;
		
		//Write the database to a CSV file
		try {
			fileWriter = new FileWriter(System.getProperty("user.home") + "/"
					+ String.valueOf(letter) + "_NBA_stats.csv");
			
			fileWriter.append(FILE_HEADER); fileWriter.append(NEW_LINE);
			for(playerDB player : database) {
				ArrayList<ArrayList<Double>> totStats = player.getStats();
				for(ArrayList<Double> year : totStats) {
					fileWriter.append(player.getName());
					fileWriter.append(COMMA);
					for(double stat : year) {
						fileWriter.append(Double.toString(stat));
						fileWriter.append(COMMA);
					}
					fileWriter.append(NEW_LINE);
				}	
			}
		} catch(Exception e) {
			System.out.println("Error writing CSV");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing CSV");
				e.printStackTrace();
			}
		}
	}
}

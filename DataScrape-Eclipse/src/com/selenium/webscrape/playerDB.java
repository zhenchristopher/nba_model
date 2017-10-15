package com.selenium.webscrape;

import java.util.ArrayList;

public class playerDB {
	private ArrayList<ArrayList<Double>> totStats = new ArrayList<ArrayList<Double>>();
	private String name;
	
	public playerDB(String player) {
		name = player;
	}
	
	public void addStats(ArrayList<Double> stats) {
		//Standard stats: Age, Tm, Pos, G, Gs, Mp, Fg, FgA, Fg%, 3P, 3PA, 3P%, 2P
		//2PA, 2P%, eFG%, FT, FTA, FT%, ORB, DRB, TRB, Ast, Stl, Blk, Tov, PF, Pts
		
		totStats.add(stats);
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<ArrayList<Double>> getStats() {
		return totStats;
	}
	
	public void clear() {
		for(ArrayList<Double> sublist : totStats) {
			sublist.clear();
		}
		totStats.clear();
		name = null;
	}
	
}

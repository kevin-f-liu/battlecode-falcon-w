import java.util.ArrayList;
import java.util.Arrays;

import bc.Planet;

public class Tester {
	
	public static void foo(Object c) {
		System.out.println(c);
	}
	
	public static void main(String[] args) {
		boolean[][] m = 
			{
				{true,true,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true},
				{true,true,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true},
				{true,true,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true},
				{true,true,false,false,true,false,true,true,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true},
				{true,true,true,false,false,false,true,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true},
				{true,true,true,false,true,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true},
				{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true},
				{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true},
				{true,true,true,true,true,true,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true},
				{true,true,true,true,true,true,true,false,true,true,true,true,true,true,true,true,true,true,true,true,true,false,true,true,true,true,true,true,true,true},
				{true,true,true,false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true},
				{true,false,false,false,true,false,false,true,true,true,true,true,true,true,true,true,false,true,false,true,true,true,true,true,true,true,true,true,true,true},
				{true,false,true,true,true,false,false,true,true,true,true,true,true,true,false,false,false,true,false,false,true,true,true,true,true,true,true,true,true,true},
				{true,false,true,true,true,false,false,false,false,true,false,false,true,true,true,false,false,true,false,false,true,true,true,true,true,true,true,true,true,true},
				{true,false,false,true,true,true,true,false,false,false,false,false,true,false,false,false,false,true,false,false,false,false,false,false,true,true,true,true,true,true},
				{true,true,true,true,true,true,false,false,false,false,false,false,true,false,false,false,false,true,false,false,false,false,false,true,true,true,true,false,false,true},
				{true,true,true,true,true,true,true,true,true,true,false,false,true,false,false,true,true,true,false,false,true,false,false,false,false,true,true,true,false,true},
				{true,true,true,true,true,true,true,true,true,true,false,false,true,false,false,false,true,true,true,true,true,true,true,false,false,true,true,true,false,true},
				{true,true,true,true,true,true,true,true,true,true,true,false,true,false,true,true,true,true,true,true,true,true,true,false,false,true,false,false,false,true},
				{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,true,true,true},
				{true,true,true,true,true,true,true,true,false,true,true,true,true,true,true,true,true,true,true,true,true,true,false,true,true,true,true,true,true,true},
				{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,true,true,true,true,true,true},
				{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true},
				{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true},
				{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,true,false,true,true,true},
				{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,true,false,false,false,true,true,true},
				{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,true,true,false,true,false,false,true,true},
				{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,true,true},
				{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,true,true},
				{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,true,true}
		};
		int height = m.length;
		int width = m[0].length;
		FalconMap map = new FalconMap();
		map.height = height;
		map.width = width;
		map.map = new MapNode[height][width];
		map.planet = Planet.Earth;
		
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				char tag = '0';
				MapNode n = new MapNode(j, i, 0, tag, m[i][j]);
				map.map[i][j] = n;
				map.updateNodeTag(j, i, tag);
			}
		}
		
		map.updateNodeTag(10, 10, 'X');
		map.printMap();
		System.out.println(map.nodeContentMap);
//		int[] ans = map.ringSearch(8, 20, '1');
//		System.out.println(ans[0] + ", " + ans[1]);
		
//		PathFinder pf = new PathFinder(map);
//		int startx = 9;
//		int starty = 7;
//		int endx = 8;
//		int endy = 8;
//		pf.target(startx, starty, endx, endy);
//		ArrayList<int[]> path = pf.getPath();
//		for (int[] node : path) {
//			map.get(node[0], node[1]).setTag('x'); 
//		}
//		map.get(startx, starty).setTag('s');
//		
//		map.printMap();
//		
//		boolean running = true;
//		while (running) {
//			try {
//				System.out.println(pf.current);
//				System.out.println(pf.nextStep());
//				pf.advanceStep();
//			} catch (RuntimeException e) {
//				System.out.println(e.getMessage());
//				running  = false;
//			}
//			
//		}
		
		
	}
}
import java.util.ArrayList;
import java.util.Arrays;

public class Tester {
	
	public static void printArray(char[][] arr) {
		for (int i = 0; i < arr.length; i++) {
			System.out.println(arr[i]);
		}
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
		char[][] map = new char[height][width];
		
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				map[i][j] = m[i][j] ? '0' : '1';
			}
		}
		
		printArray(map);
		
		PathFinder pf = new PathFinder(map);
		pf.calculatePath(29, 29, 0, 0);
		ArrayList<int[]> path = pf.getPath();
		for (int[] node : path) {
			map[node[1]][node[0]] = 'x';
		}
		
		printArray(map);
		
		boolean running = true;
		while (running) {
			try {
				System.out.println(pf.advanceStep());
			} catch (RuntimeException e) {
				System.out.println(e.getMessage());
				running  = false;
			}
			
		}
	}
}
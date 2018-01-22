import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import bc.*;

public class Tester2 {
	public static void main(String[] arg) {
		ArrayList<Integer> a = new ArrayList<Integer>();
		a.add(6);
		a.add(5);
		a.add(3);
		a.add(3);
		a.add(2);
		a.add(1);
		
		int num = 4;
		for (int i = 0; i < a.size(); i++) {
			if (a.get(i) < num) {
				a.add(i,  num);
				break;
			}
		}
		
		System.out.println(a);
		
	}
}

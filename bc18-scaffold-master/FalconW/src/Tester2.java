import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import bc.*;

public class Tester2 {
	public static void main(String[] arg) {
		Set<Integer> a = new HashSet<Integer>();
		Set<Integer> b = new HashSet<Integer>();
		a.add(1);
		a.add(2);
		b.add(2);
		b.add(3);
		System.out.println(a);
		System.out.println(b);
		a.removeAll(b);
		System.out.println(a);
	}
}

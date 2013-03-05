package com.bizosys.hsearch.byteutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestFerrari;

import com.oneline.ferrari.TestAll;

public class SortedBytesStringTest extends TestCase {

	public static String[] modes = new String[] { "all", "random", "method" };
	public static String mode = modes[2];

	public static void main(String[] args) throws Exception {
		SortedBytesStringTest t = new SortedBytesStringTest();

		if (modes[0].equals(mode)) {
			TestAll.run(new TestCase[] { t });
		} else if (modes[1].equals(mode)) {
			TestFerrari.testRandom(t);

		} else if (modes[2].equals(mode)) {
			t.setUp();
			t.testLargeSet();
			t.tearDown();
		}

	}

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
	}

	public void testEqual() throws Exception {
		List<String> sortedList = new ArrayList<String>();
		sortedList.add(new String("first"));
		sortedList.add(new String("test"));

		Collections.sort(sortedList);
		System.out.println(sortedList);
		byte[] bytes = SortedBytesString.getInstance().toBytes(sortedList);
		List<Integer> positions = new ArrayList<Integer>();
		SortedBytesString.getInstance().parse(bytes).getEqualToIndexes("test", positions);
		System.out.println(positions.toString());
		assertNotNull(positions);
		for (int pos : positions) {
			assertEquals(SortedBytesString.getInstance().parse(bytes).getValueAt(pos), "test");
		}
		assertEquals(1, positions.size());
	}
	
	public void testSingleEqual()throws Exception{
		List<String> sortedList = new ArrayList<String>();
		sortedList.add(new String("first"));
		sortedList.add(new String("test"));
		sortedList.add(new String("test"));
		sortedList.add(new String("cos"));
		sortedList.add(new String("cos"));

		Collections.sort(sortedList);
		System.out.println(sortedList);
		byte[] bytes = SortedBytesString.getInstance().toBytes(sortedList);

		int pos = SortedBytesString.getInstance().parse(bytes).getEqualToIndex("cos");
		System.out.println(pos);
		assertEquals(SortedBytesString.getInstance().parse(bytes).getValueAt(pos), "cos");
	}
	
	public void testAddAll()throws Exception{
		SortedBytesString sbs = (SortedBytesString) SortedBytesString.getInstance();

		List<String> sortedList = new ArrayList<String>();
		sortedList.add(new String("first"));
		sortedList.add(new String("test"));
		sortedList.add(new String());
		sortedList.add(new String("test"));
		sortedList.add(new String("cos"));
		sortedList.add(new String("cos"));
		
		Collections.sort(sortedList);
		System.out.println(sortedList);
		byte[] bytes = sbs.toBytes(sortedList);
		sbs.parse(bytes);
		List<String> returnVals = new ArrayList<String>();
		sbs.addAll(returnVals);
		System.out.println("returned vals: "+returnVals.toString()+" and size is:"+returnVals.size());
		
		assertEquals(sbs.getSize(), 6);
	}
	
	public void testLargeSet() throws Exception {
		System.out.println("mem size"+Runtime.getRuntime().maxMemory()/(1024 * 1024));
		List<String> sortedList = new ArrayList<String>();
		
		String[] strArr = new String[]{"shubhendu","abinash","sunil","ravi","pramod","udbhav","dhuli","zombie","shubh","shubhend","shu","socks"};
		int count = 0;
		for(int i = 0; i < 4000000; i++){
			int index = (int)(Math.random()*12);
			sortedList.add(strArr[index]);
			if(strArr[index].equals("shubhendu"))
				count++;
		}
		
		System.out.println("No of shubhendu is :"+count);
		
		Collections.sort(sortedList);

		byte[] bytes = SortedBytesString.getInstance().toBytes(sortedList);
		
		System.out.println("byte length is :"+bytes.length);
		
		List<Integer> positions = new ArrayList<Integer>();
		
		long start = System.currentTimeMillis();
		
		SortedBytesString.getInstance().parse(bytes).getEqualToIndexes("shubhendu", positions);

		assertNotNull(positions);
		for (int pos : positions) {
			assertEquals(SortedBytesString.getInstance().parse(bytes).getValueAt(pos), "shubhendu");
		}
		
		long end = System.currentTimeMillis();
		assertEquals(count, positions.size());
		
		System.out.println("Returned shubhendu is "+positions.size() +" in "+(end - start) +" mill secs");
	}

	
}

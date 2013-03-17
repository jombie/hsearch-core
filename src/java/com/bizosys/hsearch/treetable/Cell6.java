package com.bizosys.hsearch.treetable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.bizosys.hsearch.byteutils.ISortedByte;
import com.bizosys.hsearch.byteutils.SortedBytesArray;
import com.bizosys.hsearch.byteutils.SortedBytesBase.Reference;
import com.bizosys.hsearch.hbase.ObjectFactory;

public class Cell6<K1, K2, K3, K4, K5, V> extends CellBase<K1> {

	public ISortedByte<K2> k2Sorter = null;
	public ISortedByte<K3> k3Sorter = null;
	public ISortedByte<K4> k4Sorter = null;
	public ISortedByte<K5> k5Sorter = null;
	public ISortedByte<V> vSorter = null;
	
	public Map<K1, Cell5<K2,K3,K4, K5, V>> sortedList;

	public Cell6 (ISortedByte<K1> k1Sorter,ISortedByte<K2> k2Sorter, ISortedByte<K3> k3Sorter, 
			ISortedByte<K4> k4Sorter, ISortedByte<K5> k5Sorter, ISortedByte<V> vSorter) {
		this.k1Sorter = k1Sorter;
		this.k2Sorter = k2Sorter;
		this.k3Sorter = k3Sorter;
		this.k4Sorter = k4Sorter;
		this.k5Sorter = k5Sorter;
		this.vSorter = vSorter;
	}
	
	public Cell6 (ISortedByte<K1> k1Sorter,ISortedByte<K2> k2Sorter, ISortedByte<K3> k3Sorter,
			ISortedByte<K4> k4Sorter, ISortedByte<K5> k5Sorter,
			ISortedByte<V> vSorter, Map<K1, Cell5<K2,K3,K4, K5, V>> sortedList ) {
		this(k1Sorter, k2Sorter, k3Sorter, k4Sorter, k5Sorter, vSorter);
		this.sortedList = sortedList;
	}

	public Cell6 (ISortedByte<K1> k1Sorter,ISortedByte<K2> k2Sorter, ISortedByte<K3> k3Sorter,
			ISortedByte<K4> k4Sorter, ISortedByte<K5> k5Sorter, 
			ISortedByte<V> vSorter, byte[] data ) {
		this(k1Sorter, k2Sorter, k3Sorter, k4Sorter, k5Sorter, vSorter);
		this.data = data;
	}
	
	
	//Builder
	
	public void put(K1 k1, K2 k2, K3 k3, K4 k4, K5 k5, V v) {
		if ( null == this.sortedList) this.sortedList = new TreeMap<K1, Cell5<K2,K3,K4, K5, V>>();
		
		Cell5<K2, K3, K4, K5, V> val = null;
		if ( this.sortedList.containsKey(k1)) val = sortedList.get(k1);
		else {
			val = new Cell5<K2, K3, K4, K5, V>(k2Sorter, k3Sorter, k4Sorter, k5Sorter, vSorter);
			sortedList.put(k1, val);
		}
		
		this.sortedList.put(k1, val);
		val.put(k2, k3, k4, k5, v);
	}
	
	public void sort(Comparator<CellKeyValue<K5, V>> comp) {
		if ( null == sortedList) return;
		for (Cell5<K2,K3,K4,K5,V> entry : sortedList.values()) {
			entry.sort(comp);
		}
	}
	
	public byte[] toBytes(Comparator<CellKeyValue<K5, V>> comp) throws IOException {
		this.sort(comp);
		return toBytes();
	}
	
	public byte[] toBytes(V minValue, V maximumValue, boolean leftInclusize, boolean rightInclusize, Comparator<V> vComp) throws IOException {
		
		List<K1> keysL = new ArrayList<K1>(1);
		List<byte[]> valuesL = new ArrayList<byte[]>(1);

		for (K1 k : this.getMap().keySet()) {
			byte[] valueB = this.getMap().get(k).toBytes(minValue, maximumValue, leftInclusize, rightInclusize, vComp);
			if ( null == valueB) continue;
			keysL.add(k);
			valuesL.add(valueB);
		}
		
		if (keysL.size() == 0 ) return null;
		byte[] cellB = serializeKV( k1Sorter.toBytes(keysL) , SortedBytesArray.getInstance().toBytes(valuesL));
		keysL.clear(); valuesL.clear();
		return cellB;
	}	

	/**
	 * Initialize with data.. 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public Map<K1, Cell5<K2,K3,K4, K5, V>> getMap(byte[] data) throws IOException {
		this.data = data;
		parseElements();
		return sortedList;
	}
	
	public Map<K1, Cell5<K2,K3,K4, K5, V>> getMap() throws IOException {
		if ( null != sortedList) return sortedList;
		if ( null != this.data) {
			parseElements();
			return sortedList;
		}
		throw new IOException("Cell is not initialized");
	}		
	
	public void getMap(K1 exactValue, K1 minimumValue, K1 maximumValue, Map<K1, Cell5<K2,K3,K4, K5, V>> rows) throws IOException 
	{

		if ( null == data) {
			System.err.println("Null Data - It should be an warning");
			return;
		}
		
		ISortedByte<byte[]> kvbytes =  SortedBytesArray.getInstance().parse(data);
		SortedBytesArray kvbytesA = (SortedBytesArray)kvbytes;

		Reference keyRef = kvbytesA.getValueAtReference(0);
		Reference valRef = kvbytesA.getValueAtReference(1);
		ISortedByte<byte[]> valSorter = SortedBytesArray.getInstance();
		if ( null != keyRef && null != valRef ) {
			valSorter.parse(data, valRef.offset, valRef.length);
		}
		
		Callback callback = new Callback(rows, valSorter);
		findMatchingPositions(exactValue, minimumValue, maximumValue, callback);
	}
	
	
	/**
	 * Find matching exact value
	 * @param exactValue
	 * @return
	 * @throws IOException
	 */
	public Collection<Cell5<K2, K3, K4, K5, V>> values(K1 exactValue) throws IOException {
		Collection<Cell5<K2, K3, K4, K5, V>> values = new ArrayList<Cell5<K2,K3,K4,K5,V>>();
		values(exactValue, null, null, values);
		return values;
	}
	
	/**
	 * Find Ranges
	 * @param minimumValue
	 * @param maximumValue
	 * @return
	 * @throws IOException
	 */
	public Collection<Cell5<K2, K3, K4, K5, V>> values(K1 minimumValue, K1 maximumValue) throws IOException {
		Collection<Cell5<K2, K3, K4, K5, V>> values = new ArrayList<Cell5<K2,K3,K4,K5,V>>();
		values(null, minimumValue, maximumValue, values);
		return values;
	}

	public void values(K1 exactValue, Collection<Cell5<K2, K3, K4, K5, V>> foundValues) throws IOException {
		values(exactValue, null, null, foundValues);
	}
	
	public void values(K1 minimumValue, K1 maximumValue, Collection<Cell5<K2, K3, K4, K5, V>> foundValues) throws IOException {
		values(null, minimumValue, maximumValue, foundValues);
	}
	
	private void values(K1 exactValue, K1 minimumValue, K1 maximumValue, 
			Collection<Cell5<K2, K3, K4, K5, V>> foundValues) throws IOException {


		if ( null == data) {
			System.err.println("Null Data - It should be an warning");
			return;
		}
		
		List<Integer> foundPositions = ObjectFactory.getInstance().getIntegerList();
		findMatchingPositions(exactValue, minimumValue, maximumValue, foundPositions);

		ISortedByte<byte[]> kvbytes =  SortedBytesArray.getInstance().parse(data);
		SortedBytesArray kvbytesA = (SortedBytesArray)kvbytes;

		Reference valRef = kvbytesA.getValueAtReference(1);
		if ( null != valRef )  {
			ISortedByte<byte[]> valSorter = SortedBytesArray.getInstance();
			valSorter.parse(data, valRef.offset, valRef.length);

			for (int position : foundPositions) {
			foundValues.add( new Cell5<K2, K3, K4, K5, V>(
				k2Sorter, k3Sorter, k4Sorter, k5Sorter, vSorter, valSorter.getValueAt(position)));
			}
		}
		ObjectFactory.getInstance().putIntegerList(foundPositions);
	}

	public Collection<Cell5<K2, K3, K4, K5, V>> values() throws IOException {
		Collection<Cell5<K2, K3, K4, K5, V>> values = new ArrayList<Cell5<K2,K3,K4,K5,V>>();
		values(values);
		return values;
	}
	
	public void values(Collection<Cell5<K2, K3, K4, K5, V>> values) throws IOException {
		if ( null == data) {
			System.err.println("Null Data - It should be an warning");
			return;
		}

		ISortedByte<byte[]> kvbytes =  SortedBytesArray.getInstance().parse(data);
		SortedBytesArray kvbytesA = (SortedBytesArray)kvbytes;

		Reference valRef = kvbytesA.getValueAtReference(1);
		if ( null == valRef ) return;
		ISortedByte<byte[]> valSorter = SortedBytesArray.getInstance();
		valSorter.parse(data, valRef.offset, valRef.length);

		int size = valSorter.getSize();
		for ( int i=0; i<size; i++) {
			values.add( 
				new Cell5<K2, K3, K4, K5, V>( k2Sorter, k3Sorter, k4Sorter, k5Sorter,  vSorter, valSorter.getValueAt(i)) );
		}
	}
	
	
	@Override
	public void parseElements() throws IOException {
		if ( null == this.sortedList) this.sortedList = new TreeMap<K1, Cell5<K2,K3,K4,K5,V>>();
		else this.sortedList.clear();
		
		List<K1> allKeys = new ArrayList<K1>();
		List<Cell5<K2,K3,K4,K5,V>> allValues = new ArrayList<Cell5<K2,K3,K4,K5,V>>();
		
		keySet(allKeys);
		values(allValues);
		int allKeysT = allKeys.size();
		if ( allKeysT != allValues.size() ) throw new IOException( 
			"Keys and Values tally mismatched : keys(" + allKeysT + ") , values(" + allValues.size() + ")");
		
		for ( int i=0; i<allKeysT; i++) {
			Cell5<K2, K3, K4, K5, V> aVal = allValues.get(i);
			sortedList.put(allKeys.get(i), aVal);
		}
	}

	@Override
	protected List<byte[]> getEmbeddedCellBytes() throws IOException {
		List<byte[]> values = new ArrayList<byte[]>();
		for (Cell5<K2,K3, K4, K5, V> cell5 : sortedList.values()) {
			values.add(cell5.toBytes());
		}
		return values;
	}
	
	@Override
	protected byte[] getKeyBytes() throws IOException {
		return k1Sorter.toBytes(this.sortedList.keySet());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void valuesUnchecked(K1 exactValue, K1 minimumValue, K1 maximumValue, Collection foundValues) throws IOException {
		this.values(exactValue, minimumValue, maximumValue, foundValues );
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void valuesUnchecked(Collection foundValues) throws IOException {
		this.values(foundValues );
	}
	
	public final class Callback extends EmptyList {
		
		public ISortedByte<byte[]> valSorter;
		Map<K1, Cell5<K2, K3, K4, K5, V>> rows;
		
		public Callback(Map<K1, Cell5<K2, K3, K4, K5, V>> rows, ISortedByte<byte[]> valSorter ) {
			this.rows = rows;
			this.valSorter = valSorter;
		}
		
		@Override
		public final boolean add(Integer position) {
			Cell5<K2, K3, K4, K5, V> cell5 = new Cell5<K2, K3, K4, K5, V>(
					k2Sorter, k3Sorter, k4Sorter, k5Sorter, vSorter, valSorter.getValueAt(position) );
				rows.put( k1Sorter.getValueAt(position), cell5);
			return true;
		}
	};
	
	
}

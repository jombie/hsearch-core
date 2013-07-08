package com.bizosys.hsearch.treetable.example.impl.donotmodify;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import com.bizosys.hsearch.byteutils.Storable;
import com.bizosys.hsearch.functions.HSearchReducer;
import com.bizosys.hsearch.functions.StatementWithOutput;

public class CountReducer implements HSearchReducer {

    @Override
    public final void appendCols(final StatementWithOutput[] queryOutput, final Collection<byte[]> mergedQueryOutput) throws IOException {
    	int maxColumnCount = 0;
    	for (StatementWithOutput col : queryOutput) {
    		if ( null == col) continue;
    		if ( null == col.cells) continue;
    		Iterator<byte[]> itr = col.cells.iterator();
    		if ( itr.hasNext() ) {
    			int val = Storable.getInt(0, itr.next() ) ; 
    			if  ( val >  maxColumnCount) val = maxColumnCount;
    		}
		}
    	mergedQueryOutput.add(Storable.putInt(maxColumnCount));
    }

    @Override
    public final void appendRows(final Collection<byte[]> mergedB, final Collection<byte[]> appendB) {
    	mergedB.addAll(appendB);
    }

    @Override
    public final void appendRows(final Collection<byte[]> mergedRows, final byte[] appendRowId, final Collection<byte[]> appendRows) {
        appendRows(mergedRows, appendRows);

    }
}
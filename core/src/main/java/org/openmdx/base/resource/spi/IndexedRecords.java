package org.openmdx.base.resource.spi;

import #if JAVA_8 javax.resource.cci.IndexedRecord #else jakarta.resource.cci.IndexedRecord #endif;

public class IndexedRecords {

	private IndexedRecords() {
		// Avoid instantiation
	}

	static int getHashCode(IndexedRecord record){
		final String recordName = record.getRecordName();
        if(isASet(record)) {
        	int hashCode = recordName == null ? 0 : recordName.hashCode();
	        for(Object member : record){
	            if(member != null) hashCode += member.hashCode();
	        }
	        return hashCode;
        } else {
        	int hashCode = recordName == null ? 1 : recordName.hashCode();
	        for(Object member : record){
	            hashCode *= 31;
	            if(member != null) hashCode += member.hashCode();
	        }
	        return hashCode;
        }
		
	}
	
    static boolean areEqual(IndexedRecord left, Object candidate) {
        if (left == candidate) return true;
        if(!(candidate instanceof IndexedRecord)) return false;
        final IndexedRecord right = (IndexedRecord) candidate;
        final int leftSize = left.size();
		if(
        	leftSize != right.size() ||
        	!areEqual(left.getRecordName(), right.getRecordName())
        ) return false;
        if(isASet(left)){
	        for(int i = 0; i < leftSize; i++) {
	        	if(!right.contains(left.get(i))) return false;
	        }
        } else {
	        for(int i = 0; i < leftSize; i++) {
	        	if(!areEqual(left.get(i), right.get(i))) return false;
	        }
        }
        return true;
    }
    
    private static boolean areEqual(Object left, Object right){
    	return (
    		left == right
    	) || (
    		left != null && right != null && left.equals(right)
    	);
    }
    
    private static boolean isASet(IndexedRecord left){
    	return SetRecord.NAME.equalsIgnoreCase(left.getRecordName());
    }

}

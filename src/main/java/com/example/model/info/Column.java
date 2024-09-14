package com.example.model.info;

/**
 * METADATA로부터 구한 컬럼 정보
 * PostgreSQL의 컬럼명은 소문자가 기본으로, 오라클에선 대문자로 사용되어 PostgreSQL의 경우엔 대문자로 치환필요
 */
public class Column {
	public String columnName; // columns.getString("COLUMN_NAME");
    public String dataType  ; // columns.getString("TYPE_NAME"); // LOB
    public int    columnSize; // columns.getInt("COLUMN_SIZE");
    public int    nullable  ; // columns.getInt("NULLABLE");

    public int    no;
    public String value = null;
    public Column(int no, String columnName, String dataType, int columnSize, int nullable) {
		super();
		this.no         = no;
		this.columnName = columnName;
		this.dataType = dataType;
		this.columnSize = columnSize;
		this.nullable = nullable;
	}

    public void setValue(String value) {
    	this.value = value;
    }

    public String getValue() {
    	return this.value;
    }

    public boolean isLob() {
    	if (  "LOB".indexOf(columnName) >= 0)
    		return true;
    	return false;
    }
}

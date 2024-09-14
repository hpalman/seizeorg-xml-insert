package com.example.app;

import org.slf4j.LoggerFactory;

import com.example.app.config.Config;
import com.example.model.info.Column;
import com.example.model.record.COLUMN;
import com.example.model.record.RESULTS;
import com.example.model.record.ROW;

import org.slf4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Hello world!
 *
 */
public class App
{
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	/**
	 * 테이블의 메타데이터 구함
	 * @param conn
	 * @param tableNamePattern
	 * @return 메타데이터가 들어간 테이블컬럼 정보
	 * @throws SQLException
	 */
	public static Map<String, Column> getColumns(Connection conn, String tableNamePattern) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        Map<String, Column> map = new LinkedHashMap<String, Column>();
        //return map;

        // 테이블 컬럼 정보 가져오기
        ResultSet columns = metaData.getColumns(null, null, tableNamePattern.toLowerCase() /* oracle toUpperCase()*/, null);

        int no = 0;
        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME").toUpperCase(); // PostgreSQL의 컬럼은 소문자
            String dataType   = columns.getString("TYPE_NAME"); // LOB
            int columnSize    = columns.getInt("COLUMN_SIZE");
            int nullable      = columns.getInt("NULLABLE");

            map.put(columnName, new Column(no++, columnName, dataType, columnSize, nullable) );
        }
        columns.close();
        return map;
	}

	/**
	 * PreparedStatement로 실행할 INSERT 구문 만들기
	 * @param tableName
	 * @param columns
	 * @return
	 */
	public static String getInsertStatement(String tableName, Map<String,Column> columns) {
        StringBuilder sbArg = new StringBuilder();
        StringBuilder sbKey = new StringBuilder();
        for (Map.Entry<String, Column> entry : columns.entrySet()) {
            String k = entry.getKey();
            // Column c = entry.getValue();
            if ( sbKey.length() > 0) {
            	sbKey.append(",");
            	sbArg.append(",");
            }
            sbKey.append(k);
            sbArg.append("?");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(tableName).append("(").append(sbKey).append(") VALUES(").append(sbArg).append(")");
        return sb.toString();
	}

	/**
	 * 테이블에 XML로부터 읽은 데이터를 적재
	 * @param conn
	 * @param tableName
	 * @return 추가된 행 갯수
	 * @throws SQLException
	 * @throws JAXBException
	 */
	private static int insert(Connection conn, String tableName) throws SQLException, JAXBException {
		PreparedStatement pstmt;
		Map<String, Column> columns;

		columns = getColumns(conn, tableName); // PostgreSQL : 소문자, Oracle: 대문자
        String sql = getInsertStatement(tableName, columns);

        // JAXB로 XML 데이터 READ
        File file = new File(Config.dataDir + tableName + ".xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(RESULTS.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        RESULTS results = (RESULTS) jaxbUnmarshaller.unmarshal(file);
        if ( results == null || null == results.getRows()) {
        	logger.info("{} no data", tableName);
        	return 0; //
        }
    	// DELETE
        logger.info(">> DELETE FROM {}", tableName);
        pstmt = conn.prepareStatement("DELETE FROM " + tableName);
        pstmt.executeUpdate();
        pstmt.close();

        int rowCount = 0;
        for (ROW row : results.getRows()) {
        	logger.info("rowCount:{}", rowCount+1);
        	// clear
            for (Map.Entry<String, Column> entry : columns.entrySet()) {
                // String k = entry.getKey();
                Column c = entry.getValue();
                c.setValue(null);
            }

        	for ( COLUMN col : row.getColumns() ) {
        		String name = col.getName();
        		if ( !columns.containsKey(name) ) {
        			logger.error("{} not found.",name);
        		}
        		// logger.info(col.getName());
        		Column c = columns.get(col.getName());
        		c.setValue(col.getValue());
        	}

        	// INSERT
            pstmt = conn.prepareStatement(sql);

            for (Map.Entry<String, Column> entry : columns.entrySet()) {
                // String k = entry.getKey();
                Column c = entry.getValue();

                String value = c.getValue();
                if ( value == null || "".equals(value) ) {
                    switch ( c.dataType ) {
                    	case "numeric":
                    		value = "0";
                    		break;
                    	default:
                    		// logger.error(c.columnName);
                    		break;
                    }
                }
                if ( c.isLob() ) {
                    pstmt.setString(c.no + 1, c.getValue());
                } else {
                    pstmt.setString(c.no + 1, value); // c.getValue());
                }
            }
            pstmt.executeUpdate();
            pstmt.close();
            rowCount++;
        }
        // logger.info("TABLE:{} insert:{} rows",  tableName, rowCount);
        return rowCount;
	}


	public static void main( String[] args )
	{
		logger.info("{} {} connecting... ", Config.jdbcUrl, Config.username);
		/**
		 * 1. 테이블명의
		 * 2. XML 데이터 읽기
		 * 3. INSERT LOB 처리
		 * @param args
		 */
	    try (Connection conn = DriverManager.getConnection(Config.jdbcUrl, Config.username, Config.password)) {

			for (String tableName : Config.tableNames ) {
				logger.info("{} insert started.", tableName);
				int row = insert(conn, tableName);
				logger.info("{} insert finished. data {}개 inserted", tableName, row);
			}
	    } catch (Exception e) {
	        logger.error("{}", e.getMessage(), e);
		} finally {
			logger.info("finished.");
		}
	}
}

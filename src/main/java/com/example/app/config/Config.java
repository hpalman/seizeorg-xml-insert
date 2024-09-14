package com.example.app.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 데이터베이스 연결 정보 등 환경 설정
 */
public class Config {
	private static final Logger logger = LoggerFactory.getLogger(Config.class);
	private static void readProperties() {
		Properties properties = new Properties();

		//try (InputStream input = new FileInputStream("config.properties")) {
		try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            // Load the properties file
            properties.load(input);

            // Access the property values
            jdbcUrl = properties.getProperty("app.jdbcUrl");
            username = properties.getProperty("app.username");
            password = properties.getProperty("app.password");
            dataDir  = properties.getProperty("app.dataDir");

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
	}
	public static ArrayList<String> tableNames = new ArrayList<>();
	private static void readTableNames() {
		String fileName = "tablenames.txt";
		// Load the file from the classpath
        try (InputStream inputStream = Config.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                System.out.println("File not found in classpath");
                return;
            }

            // Use Scanner to read the file line-by-line
            try (Scanner scanner = new Scanner(inputStream)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    // 빈행 무시
                    if ( line.length() == 0 )
                    	continue;
                    // # 또는 // 시작시 주석문
                    // 문자열 뒤에 # 또는 // 이 있으면 거기서부턴 주석 인식
                    if ( line.startsWith("//") )
                    	continue;
                    if ( line.startsWith("#") )
                    	continue;

                	int x = line.indexOf("#");
                	if ( x >= 0 ) {
                		line = line.substring(0, x);
                		line = line.trim();
                	}
                	else {
                		x = line.indexOf("//");
                		if ( x >= 0) {
                			line = line.substring(0, x);
                    		line = line.trim();
                		}
                	}

                    tableNames.add(line);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
	}

/*
    // 오라클용
    public static String jdbcUrl = "jdbc:oracle:thin:@192.168.8.71:1521:orcldb"; // Update with your DB details
    public static String username = "SIZOWN";
    public static String password = "seize!2020";

	public static String topDir = "E:/project/hklife/backup/hk/DDL_TABLE/table/lob/DATA_TABLE/lob.DATA_TABLE/";
*/
	// EDB용
    public static String jdbcUrl = "jdbc:edb://172.20.30.202:5444/pportaldb";
    public static String username = "pportalown";
    public static String password = "1234";

	public static String dataDir   = "C:/app/eXERD-v3.3.43/workspace/SEIZE_BASE/ERD-PostgreSQL/DATA_TABLE/";

	static {
		readProperties();
		readTableNames();
	}
	static {
        logger.debug("jdbcUrl:{}", jdbcUrl);
        logger.debug("username:{}", username);
        logger.debug("password:{}", password);
        logger.debug("dataDir:{}", dataDir);
	}
	/*
	 * 2024.09.14 허팔만
	 * ERD와 실 DB의 컬럼이 다른 것이 아주 일부 있으므로, 그런것들은 수작업으로 진행함
	 *
	 */
}

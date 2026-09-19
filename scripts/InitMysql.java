import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * MySQL 一次性初始化：建库（utf8mb4）-> 执行 schema.sql -> data.sql。
 * 用 JDK 源文件模式运行，无需 mysql 客户端：
 *   java --class-path mysql-connector-j.jar scripts/InitMysql.java <host:port> <user> <pwd> <db> <sql目录>
 * 库中已存在表时拒绝执行，除非设置环境变量 INIT_FORCE=1（会 DROP 全部表重来）。
 */
public class InitMysql {

    private static final String PARAMS =
            "useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
                    + "&useSSL=false&allowPublicKeyRetrieval=true";

    public static void main(String[] args) throws Exception {
        String host = args[0], user = args[1], password = args[2], db = args[3], sqlDir = args[4];

        try (Connection c = DriverManager.getConnection("jdbc:mysql://" + host + "/?" + PARAMS, user, password);
             Statement s = c.createStatement()) {
            s.execute("CREATE DATABASE IF NOT EXISTS `" + db
                    + "` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("[1/3] 数据库 `" + db + "` 已就绪（utf8mb4）");
        }

        try (Connection c = DriverManager.getConnection("jdbc:mysql://" + host + "/" + db + "?" + PARAMS, user, password);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SHOW TABLES")) {
            if (rs.next() && !"1".equals(System.getenv("INIT_FORCE"))) {
                System.err.println("数据库 `" + db + "` 中已存在表；重跑会 DROP 全部表并丢失数据，"
                        + "确认无误请设置 INIT_FORCE=1 后再执行。");
                System.exit(2);
            }

            System.out.println("[2/3] 执行 schema.sql 建表 ...");
            runSqlFile(c, sqlDir + "/schema.sql");
            System.out.println("[3/3] 执行 data.sql 灌种子数据 ...");
            runSqlFile(c, sqlDir + "/data.sql");

            for (String t : new String[]{"user", "blogger", "category", "video", "article", "favorite", "history"}) {
                try (ResultSet cr = s.executeQuery("SELECT COUNT(*) FROM `" + t + "`")) {
                    cr.next();
                    System.out.println("  " + t + ": " + cr.getInt(1) + " 行");
                }
            }
            System.out.println("初始化完成。");
        }
    }

    /** 去掉 -- 行注释后按分号切分执行（脚本中无存储过程/触发器，简单切分即可）。 */
    private static void runSqlFile(Connection c, String path) throws Exception {
        StringBuilder sql = new StringBuilder();
        for (String line : new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8).split("\n", -1)) {
            if (line.trim().startsWith("--")) continue;
            sql.append(line).append('\n');
        }
        for (String stmt : sql.toString().split(";")) {
            String q = stmt.trim();
            if (!q.isEmpty()) {
                try (Statement s = c.createStatement()) {
                    s.execute(q);
                }
            }
        }
    }
}

import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.stream.Stream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class CreateDB
{
  public String url;
  public CreateDB(String filename)
  {
    url = "jdbc:sqlite:" + filename;
    try (Connection conn = DriverManager.getConnection(url))
    {
      if (conn != null)
      {
        Statement statement = conn.createStatement();
        statement.setQueryTimeout(32);

        statement.execute("DROP TABLE IF EXISTS words");
        statement.execute("CREATE TABLE words (_id INTEGER PRIMARY KEY, word TEXT UNIQUE NOT NULL, length INTEGER NOT NULL)");
      }
    }
    catch (SQLException e) { System.err.println(e.getMessage()); }
  }

  public static void makeBatch(PreparedStatement ps, String word)
  {
    try
    {
      ps.setString(1, word);
      ps.setInt(2, word.length());
      ps.addBatch();
    }
    catch (SQLException e) { e.printStackTrace(); }
  }

  public static void main(String[] args)
  {
    CreateDB db = new CreateDB("words.db");
    String sql = "INSERT INTO words (word, length) values (?, ?)";

    try (Stream<String> stream = Files.lines(Paths.get(args[0]));
    // try (BufferedReader br = new BufferedReader(new FileReader(args[0]));
         Connection conn = DriverManager.getConnection(db.url);
         PreparedStatement ps = conn.prepareStatement(sql))
    {
      stream.forEach(x -> makeBatch(ps, x));
      // for (String word; (word = br.readLine()) != null;)
      // {
      //   // makeBatch(ps, word);
      //   // PreparedStatement ps = conn.prepareStatement(sql);
      //   ps.setString(1, word);
      //   ps.setInt(2, word.length());
      //   ps.addBatch();
      //   // ps.executeUpdate();
      // }
      ps.executeBatch();
    }
    catch (SQLException e) { System.err.println(e.getMessage()); }
    catch (IOException e) { System.err.println(e.getMessage()); }
  }
}

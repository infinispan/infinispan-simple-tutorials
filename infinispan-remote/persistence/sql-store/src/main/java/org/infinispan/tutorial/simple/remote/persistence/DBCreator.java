package org.infinispan.tutorial.simple.remote.persistence;

import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBCreator {

   static final String JDBC_DRIVER = "org.h2.Driver";
   static final String USER = "infinispan";
   static final String PASS = "secret";
   private String DB_URL;
   private org.h2.tools.Server server;

   public void startDBServer() {
      startDBServer("9123");
   }

   public void startDBServer(String port) {
      // Windows users may need to change the URL
      DB_URL = String.format("jdbc:h2:tcp://localhost:%s/~/example;", port);
      try {
         server = org.h2.tools.Server.createTcpServer("-tcpPort", port, "-tcpAllowOthers", "-ifNotExists").start();
      } catch (SQLException throwable) {
         throwable.printStackTrace();
      }
   }

   public void stopDBServer() {
      if (server != null) {
         server.stop();
      }
   }


   public void createAndPopulate() {
      Connection conn = null;
      Statement stmt = null;
      try {
         Class.forName(JDBC_DRIVER);
         System.out.println("Connecting to database...");
         conn = DriverManager.getConnection(DB_URL,USER,PASS);
         System.out.println("Creating table in given database...");
         stmt = conn.createStatement();
         String sqlCreate = IOUtils.toString(DBCreator.class.getClassLoader().getResource("initDB.sql"), StandardCharsets.UTF_8);
         String sqlInsert = IOUtils.toString(DBCreator.class.getClassLoader().getResource("populateDB.sql"), StandardCharsets.UTF_8);
         System.out.println("Creating table in given database...");
         stmt.executeUpdate(sqlCreate);
         System.out.println("Populating database...");
         stmt.executeUpdate(sqlInsert);
      } catch(Exception e) {
         e.printStackTrace();
      } finally {
         try{
            if(stmt!=null) stmt.close();
         } catch(SQLException se2) {
            se2.printStackTrace();
         }
         try {
            if(conn!=null) conn.close();
         } catch(SQLException se){
            se.printStackTrace();
         }
      }
   }

   public List<Author> readAuthors() {
      List<Author> authors = new ArrayList<>();
      Connection conn = null;
      Statement stmt = null;
      try {
         Class.forName(JDBC_DRIVER);
         System.out.println("Connecting to database...");
         conn = DriverManager.getConnection(DB_URL,USER,PASS);
         System.out.println("Connected database successfully...");
         stmt = conn.createStatement();
         String sql = "SELECT * FROM authors";
         ResultSet rs = stmt.executeQuery(sql);

         while(rs.next()) {
            int id  = rs.getInt("id");
            String isbn = rs.getString("isbn");
            String name = rs.getString("name");
            String country = rs.getString("country");

            authors.add(new Author(id, isbn, name, country));
         }
         rs.close();
      } catch(Exception e) {
         e.printStackTrace();
      } finally {
         try {
            if(stmt!=null) stmt.close();
         } catch(SQLException se2) {
         }
         try {
            if(conn!=null) conn.close();
         } catch(SQLException se) {
            se.printStackTrace();
         }
      }
      return authors;
   }

   public List<String> loadBooksId() {
      List<String> booksIds = new ArrayList<>();
      Connection conn = null;
      Statement stmt = null;
      try {
         Class.forName(JDBC_DRIVER);
         System.out.println("Connecting to database...");
         conn = DriverManager.getConnection(DB_URL,USER,PASS);
         System.out.println("Connected database successfully...");
         stmt = conn.createStatement();
         String sql = "SELECT * FROM books";
         ResultSet rs = stmt.executeQuery(sql);

         while(rs.next()) {
            String isbn = rs.getString("isbn");
            booksIds.add(isbn);
         }
         rs.close();
      } catch(Exception e) {
         e.printStackTrace();
      } finally {
         try {
            if(stmt!=null) stmt.close();
         } catch(SQLException se2) {
            se2.printStackTrace();
         }
         try {
            if(conn!=null) conn.close();
         } catch(SQLException se) {
            se.printStackTrace();
         }
      }
      return booksIds;
   }
}

package com.salquestfl.mailapageaday;

import java.util.*;

import java.io.File;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.util.*;

import java.sql.*;

/**
 * Scan the directory for .pdf files and convert them into .txt files
 *
 */
public class App 
{
    public static void main(String[] args)
    {
        if (args.length == 1) {
            String dirName = args[0];
            File dir = new File(dirName);
            if (dir.isDirectory()) {
                try {
                    processDocDir(dir);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                printUsage();
            }
        } else {
            printUsage();
        }
    }

    private static void convertPdfToText(File input, File output) throws IOException {
        PDDocument pd = PDDocument.load(input);
        PDFTextStripper stripper = new PDFTextStripper();
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
        stripper.writeText(pd, wr);
        if (pd != null) {
            pd.close();
        }
        wr.close();
    }


    private static void printUsage() {
        System.out.println("USAGE: mailapageaday <directory-name>");
    }

    private static void processDocDir(File dir) throws Exception {
        List<String> existingFiles = new ArrayList<String>();
        for(String fileName : dir.list()) {
            if (fileName.endsWith(".pdf")) {
                existingFiles.add(fileName);
            }
        }
        //Map<String, Integer> bookmarks = getBookmarks(conn);
        List<String> newFiles = new ArrayList<String>();
        String dirPath = dir.getPath();
        for(String fileName : existingFiles) {
            try {
                File output = new File(dirPath + "/" + fileName + ".txt");
                if (!output.exists()) {
                    newFiles.add(fileName);
                    System.out.println("Converting " + fileName);
                    File input = new File(dirPath + "/" + fileName);
                    convertPdfToText(input, output);
                }
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:mailapage.db");
        createBookmarksTable(conn);
        deleteOutdatedBookmarks(conn, existingFiles);
        addNewBookmarks(conn, newFiles);
        Map<String, Integer> bookmarks = getBookmarks(conn);
        for(Map.Entry<String, Integer> e : bookmarks.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
        conn.close();
    }

    private static Map<String, Integer> getBookmarks(Connection conn) throws Exception {
        String statement = "select `filename`, `page` from `bookmarks`";
        PreparedStatement dbStatement = conn.prepareStatement(statement);
        Map<String, Integer> bookmarks = new HashMap<String, Integer>();
        ResultSet rs = dbStatement.executeQuery();
        while (rs.next()) {
            String fileName = rs.getString("filename");
            int page = rs.getInt("page");
            bookmarks.put(fileName, page);
        }
        return bookmarks;
    }

    private static void deleteOutdatedBookmarks(Connection conn, List<String> existingFiles) throws SQLException {
        if (!existingFiles.isEmpty()) {
            StringBuilder statement = new StringBuilder();
            statement.append("delete from `bookmarks` where `filename` not in ( select `filename` from `bookmarks` `c` where \n");
            for (int index = 0; index < existingFiles.size(); index++) {
                if (index > 0) {
                    statement.append(" OR \n");
                }
                statement.append("`c`.`filename` = ?");
            }
            statement.append(" )");
            System.out.println("Delete Query: " + statement.toString());
            PreparedStatement dbStatement = conn.prepareStatement(statement.toString());
            int index = 1;
            for (String fileName : existingFiles) {
                dbStatement.setString(index, fileName);
                index++;
            }
            dbStatement.executeUpdate();
        }
    }

    private static void createBookmarksTable(Connection conn) throws SQLException {
        String statement = "create table if not exists `bookmarks` ( `id` INTEGER primary key autoincrement NOT NULL, `filename` TEXT NOT NULL, `page` INTEGER NOT NULL )";
        PreparedStatement dbStatement = conn.prepareStatement(statement);
        dbStatement.executeUpdate();
    }

    private static void addNewBookmarks(Connection conn, List<String> newFiles) throws SQLException {
        if (!newFiles.isEmpty()) {
            StringBuilder statement = new StringBuilder();
            statement.append("insert into `bookmarks` (`filename`, `page`) values \n");
            for (int index = 0; index < newFiles.size(); index++) {
                if (index > 0) {
                    statement.append(", \n");
                }
                statement.append("(?, 1)");
            }
            System.out.println("Insert Query: " + statement.toString());
            PreparedStatement dbStatement = conn.prepareStatement(statement.toString());
            int index = 1;
            for (String fileName : newFiles) {
                dbStatement.setString(index, fileName);
                index++;
            }
            dbStatement.executeUpdate();
        }
    }
}

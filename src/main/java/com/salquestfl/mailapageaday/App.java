package com.salquestfl.mailapageaday;

import java.io.*;
import java.util.*;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.util.*;

import java.sql.*;

/**
 * Scan the directory for .pdf files and convert them into .txt files
 *
 */
public class App extends TimerTask
{
    private String dirPath;

    public App(String dirPath) {
        this.dirPath = dirPath;
    }

    @Override
    public void run() {
        File dir = new File(dirPath);
        try {
            Map<String, Integer> bookmarks = processDocDir(dir);
            for (Map.Entry<String, Integer> bookmark : bookmarks.entrySet()) {
                String filename = bookmark.getKey();
                int page = bookmark.getValue();
                String filePath = dirPath + "/" + filename;
                String body = getPdfPage(filePath, page);
                String subject = filename + " : page " + page;
                Mailer.sendMail(subject, body);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        if (args.length >= 1) {
            String dirPath = args[0];
            File dir = new File(dirPath);
            if (dir.isDirectory()) {
                try {
                    Timer t = new Timer("mailapageaday timer task");
                    TimerTask task = new App(dirPath);
                    int hours = args.length >= 2 ? Integer.parseInt(args[1]) : 24;
                    long period = 60*60*1000*hours;
                    t.scheduleAtFixedRate(task, 0, period);
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

    private static String getPdfPage(String filePath, int page) throws IOException {
        PDDocument pd = PDDocument.load(filePath);
        StringWriter strWr = new StringWriter();
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(page);
        stripper.setEndPage(page+1);
        BufferedWriter wr = new BufferedWriter(strWr);
        stripper.writeText(pd, wr);
        if (pd != null) {
            pd.close();
        }
        wr.close();
        return strWr.toString();
    }


    private static void printUsage() {
        System.out.println("USAGE: mailapageaday <directory-name>");
    }

    private static Map<String, Integer> processDocDir(File dir) throws Exception {
        Set<String> existingFiles = getExistingFiles(dir);
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:mailapage.db");
        createBookmarksTable(conn);
        Map<String, Integer> existingBookmarks = getBookmarks(conn);
        Map<String, Integer> currentBookmarks = getCurrentBookmarks(existingFiles, existingBookmarks);
        deleteOutdatedBookmarks(conn, existingFiles);
        updateBookmarks(conn, currentBookmarks);
        conn.close();
        return currentBookmarks;
    }

    private static Map<String, Integer> getCurrentBookmarks(Set<String> existingFiles, Map<String, Integer> existingBookmarks) {
        Map<String, Integer> bookmarks = new HashMap<String, Integer>();
        for (String file: existingFiles) {
            Integer page = existingBookmarks.get(file);
            if (page != null) {
                bookmarks.put(file, page);
            } else {
                bookmarks.put(file, 1);
            }
        }
        return bookmarks;
    }

    private static Set<String> getExistingFiles(File dir) {
        Set<String> existingFiles = new HashSet<String>();
        for(String fileName : dir.list()) {
            if (fileName.endsWith(".pdf")) {
                existingFiles.add(fileName);
            }
        }
        return existingFiles;
    }

    private static Map<String, Integer> getBookmarks(Connection conn) throws Exception {
        String statement = "SELECT filename, page FROM bookmarks";
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


    private static void createBookmarksTable(Connection conn) throws SQLException {
        String statement = "CREATE TABLE IF NOT EXISTS bookmarks ( id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, filename TEXT NOT NULL, page INTEGER NOT NULL )";
        PreparedStatement dbStatement = conn.prepareStatement(statement);
        dbStatement.executeUpdate();
        String indexStatement = "CREATE UNIQUE INDEX IF NOT EXISTS filename_idx ON bookmarks(filename)";
        PreparedStatement dbIndexStatement = conn.prepareStatement(indexStatement);
        dbIndexStatement.executeUpdate();
    }

    private static void deleteOutdatedBookmarks(Connection conn, Collection<String> existingFiles) throws SQLException {
        if (!existingFiles.isEmpty()) {
            StringBuilder statement = new StringBuilder();
            statement.append("DELETE FROM bookmarks WHERE filename NOT IN (SELECT filename FROM bookmarks b WHERE ");
            for (int index = 0; index < existingFiles.size(); index++) {
                if (index > 0) {
                    statement.append(" OR ");
                }
                statement.append("filename = ?");
            }
            statement.append(")");
            System.out.println("Delete Query: " + statement.toString());
            PreparedStatement dbStatement = conn.prepareStatement(statement.toString());
            int index = 1;
            for (String fileName : existingFiles) {
                System.out.println(fileName);
                dbStatement.setString(index, fileName);
                index++;
            }
            dbStatement.executeUpdate();
        }
    }

    private static void updateBookmarks(Connection conn, Map<String, Integer> bookmarks) throws SQLException {
        if (!bookmarks.isEmpty()) {
            StringBuilder statement = new StringBuilder();
            statement.append("INSERT OR REPLACE INTO bookmarks (filename, page) VALUES ");
            for (int index = 0; index < bookmarks.size(); index++) {
                if (index > 0) {
                    statement.append(", ");
                }
                statement.append("(?, ?)");
            }
            System.out.println("Insert Query: " + statement.toString());
            PreparedStatement dbStatement = conn.prepareStatement(statement.toString());
            int index = 1;
            for (Map.Entry<String, Integer> entry : bookmarks.entrySet()) {
                System.out.println(entry.getKey());
                dbStatement.setString(index, entry.getKey());
                dbStatement.setInt(index + 1, entry.getValue() + 1); // increment page number
                index += 2;
            }
            dbStatement.executeUpdate();
        }
    }
}

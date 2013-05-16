package com.salquestfl.mailapageaday;

import java.io.File;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.util.*;

/**
 * Scan the directory for .pdf files and convert them into .txt files
 *
 */
public class App 
{
    static void convertPdfToText(String dirName, String pdfFileName) throws IOException {
      File input = new File(dirName + "/" + pdfFileName);
      File output = new File(dirName + "/" + pdfFileName + ".txt");
      PDDocument pd = PDDocument.load(input);
      PDFTextStripper stripper = new PDFTextStripper();
      BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
      stripper.writeText(pd, wr);
      if (pd != null) {
         pd.close();
      }
      wr.close();
    }

    public static void main(String[] args)
    {
        if (args.length == 1) {
            String dirName = args[0];
            File dir = new File(dirName);
            if (dir.isDirectory()) {
                for(String fileName : dir.list()) {
                    if (fileName.endsWith(".pdf")) {
                        System.out.println("Converting " + fileName);
                        try {
                            convertPdfToText(dirName, fileName);
                        } catch (IOException e) {
                            System.out.println(e.toString());
                        }
                    }
                }
            }
        } else {
            System.out.println("USAGE: mailapageaday <directory-name>");
        }
    }
}

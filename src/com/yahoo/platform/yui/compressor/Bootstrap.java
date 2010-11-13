package com.yahoo.platform.yui.compressor;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.apache.commons.io.FilenameUtils;

public class Bootstrap {
	private static String backupSuffix ;
	private static void compressCss(String origFile, String compressedFile){
		Reader in = null;
        Writer out = null;
        try {
            String charset = "UTF-8";
            String inputFilename = origFile;

            in = new InputStreamReader(new FileInputStream(inputFilename), charset);
            String outputFilename = compressedFile;
            
            CssCompressor compressor = new CssCompressor(in);

            // Close the input stream first, and then open the output stream,
            // in case the output file should override the input file.
            in.close(); in = null;

            if (outputFilename == null) {
                out = new OutputStreamWriter(System.out, charset);
            } else {
                out = new OutputStreamWriter(new FileOutputStream(outputFilename), charset);
            }

            int  linebreakpos = -1;

            compressor.compress(out, linebreakpos);

        }
		catch (EvaluatorException e) {
			System.out.println("Error compressing " + origFile);
	        e.printStackTrace();
	        // Return a special error code used specifically by the web front-end.
	        //System.exit(2);

		}catch (IOException e) {
			System.out.println("Error compressing " + origFile);
            e.printStackTrace();
            //System.exit(1);

        }finally {

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                	System.out.println("Error compressing " + origFile);
                    e.printStackTrace();
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                	System.out.println("Error compressing " + origFile);
                    e.printStackTrace();
                }
            }
        }
	}
	
	private static void compressJs(final String origFile, final String compressedFile){
		Reader in = null;
        Writer out = null;
        try {

            String charset = "UTF-8";
            String inputFilename = origFile;

            in = new InputStreamReader(new FileInputStream(inputFilename), charset);
            String outputFilename = compressedFile;
            JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {
                public void warning(String message, String sourceName,
                        int line, String lineSource, int lineOffset) {
                	if (line < 0) {
                        System.err.println("\n[WARNING] " + message);
                    } else {
                        System.err.println("\n[WARNING] " + line + ':' + lineOffset + ':' + message);
                    }
                }

                public void error(String message, String sourceName,
                        int line, String lineSource, int lineOffset) {
                	System.out.println("Error compressing " + origFile);
                	if (line < 0) {
                        System.err.println("\n[ERROR] " + message);
                    } else {
                        System.err.println("\n[ERROR] " + line + ':' + lineOffset + ':' + message);
                    }
                    
                }

                public EvaluatorException runtimeError(String message, String sourceName,
                        int line, String lineSource, int lineOffset) {
                    error(message, sourceName, line, lineSource, lineOffset);
                    return new EvaluatorException(message);
                }
            });
            // Close the input stream first, and then open the output stream,
            // in case the output file should override the input file.
            in.close(); in = null;

            if (outputFilename == null) {
                out = new OutputStreamWriter(System.out, charset);
            } else {
                out = new OutputStreamWriter(new FileOutputStream(outputFilename), charset);
            }

            boolean munge = true;
            boolean preserveAllSemiColons = false; 
            boolean disableOptimizations = false;
            boolean verbose = false;
            int  linebreakpos = -1;

            compressor.compress(out, linebreakpos, munge, verbose,
                    preserveAllSemiColons, disableOptimizations);

        }
		catch (EvaluatorException e) {
			System.out.println("Error compressing " + origFile);
	        e.printStackTrace();
	        // Return a special error code used specifically by the web front-end.
	        //System.exit(2);

		}catch (IOException e) {
			System.out.println("Error compressing " + origFile);
            e.printStackTrace();
            ///System.exit(1);

        }finally {

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                	System.out.println("Error compressing " + origFile);
                    e.printStackTrace();
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                	System.out.println("Error compressing " + origFile);
                    e.printStackTrace();
                }
            }
        }
    }
	
	private static String getDirectory(String[] args){
		String dir= ".";
		try{
			dir = args[0];
		}
		catch (Exception e) {
			dir = System.getProperty("user.dir");
		}
		return dir;
	}
	
	private static void _compressFile(String origFilePath, String compressedFilePath){
		String extension = FilenameUtils.getExtension(origFilePath);
		if(extension.equals("js")){
			compressJs(origFilePath, compressedFilePath);
		}
		else if(extension.equals("css")){
			compressCss(origFilePath, compressedFilePath);
		}
		else if(extension.equals("jsp")){
			String fileName = (new File(origFilePath)).getName();
			if(fileName.endsWith("_static_js_orig.jsp")){
				compressJs(origFilePath, compressedFilePath);
			}
			else if(fileName.endsWith("_css_orig.jsp")){
				compressCss(origFilePath, compressedFilePath);
			}
		}
	}
	
	private static void compressFile(File file){
		String origFilePath = file.getAbsolutePath();
		String extension = FilenameUtils.getExtension(origFilePath);
		String pathWithoutExtension = FilenameUtils.removeExtension(origFilePath);
		String backupFilePath = pathWithoutExtension + backupSuffix + "." + extension;
		
		(new File(origFilePath)).renameTo(new File(backupFilePath));
		_compressFile(backupFilePath, origFilePath);
	}
	
	private static long compressDir(File dir, boolean recurse){
		File[] subdirs = dir.listFiles(new FileFilter(){
			public boolean accept(File file){
				return file.isDirectory();
			}
		});
		
		long numFilesCompressed = 0;
		
		File[] compressables = dir.listFiles(new FileFilter(){
			public boolean accept(File file){
				String fileName = file.getName();
				String extension = FilenameUtils.getExtension(fileName);
				
				boolean selected = file.isFile() && extension.equals("js") || extension.equals("css");
				selected = selected || fileName.endsWith("_static_js.jsp") || fileName.endsWith("_css.jsp");
				
				return selected;
			}
		});
		
		for(File file: compressables){
			try{
				compressFile(file);
				numFilesCompressed += 1;
			}
			catch(Exception e){
				System.out.println("Error compressing " + file.getAbsolutePath());
				e.printStackTrace();
			}
			System.out.print(".");
			System.out.flush();
		}
		
		if(!recurse){
			return numFilesCompressed;
		}
		
		for(File subdir: subdirs){
			numFilesCompressed += compressDir(subdir, recurse);
		}
		
		return numFilesCompressed;
	}
	
	private static String getBackupSuffix(String[] args){
		String backupSuffix = "";
		try{
			backupSuffix = args[1];
		}
		catch(Exception e){
			backupSuffix = "_orig";
		}
		return backupSuffix;
	}
	
	private static boolean isRecursive(String[] args){
		boolean recurse;
		try{
			String str = args[2];
			recurse = !(str.equalsIgnoreCase("n") || str.equalsIgnoreCase("no") || str.equalsIgnoreCase("false"));
		}
		catch(Exception e){
			recurse = true;
		}
		return recurse;
	}
	
	public static void main(String[] args){
		String directory = getDirectory(args);
		backupSuffix = getBackupSuffix(args);
		boolean recurse = isRecursive(args);
		
		System.out.print("Compressing files under " + directory + " ");
		
		long startTime = System.currentTimeMillis();
		long numFilesCompressed = compressDir(new File(directory), recurse);
		long endTime = System.currentTimeMillis();
		
		System.out.println(" Done");
		System.out.flush();
		
		if(numFilesCompressed > 0) {
			System.out.println(numFilesCompressed + " files compressed in " + ((endTime-startTime) / 1000.0)+ " secs");
		}
	}
}
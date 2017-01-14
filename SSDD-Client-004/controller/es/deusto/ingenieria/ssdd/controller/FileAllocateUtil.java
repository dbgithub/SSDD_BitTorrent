package es.deusto.ingenieria.ssdd.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;

import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;

public class FileAllocateUtil {
	
	private MetainfoHandlerSingleFile single;
	private BitSet donwloadedChunks;
	private int donwloadedBytes = 0;
	
	public FileAllocateUtil(MetainfoHandlerSingleFile single){
		this.single= single;
		this.donwloadedChunks = new BitSet();
	}
	public RandomAccessFile getFileAllocated() throws FileNotFoundException, IOException{
		File file = new File("downloads/"+single.getMetainfo().getInfo().getName());
		//Initialize bitset with the number of bytes
		int bytes = single.getMetainfo().getInfo().getLength();
		donwloadedChunks = new BitSet(bytes);
		if(!(file.exists())){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			
			//File exists, so there is some data about that file. Check its integrity
			int pieceLengthBytes = single.getMetainfo().getInfo().getPieceLength();
			
	        byte[] buffer = new byte[pieceLengthBytes];
			try (BufferedInputStream bis = new BufferedInputStream(
	                new FileInputStream(file))) {
	            int tmp = 0;
	            int contador = 0;
	            while ((tmp = bis.read(buffer)) > 0) {
	                donwloadedBytes = donwloadedBytes + tmp;
	                donwloadedChunks.set(contador);
	                contador++;
	            }
	        }
		}
		System.out.println("Cardinality " + donwloadedChunks.cardinality());
		RandomAccessFile rf = null;
		try{
		    rf = new RandomAccessFile(file, "rw"); 
		    rf.setLength(bytes);
		    
		}catch(IOException ex){
		    ex.printStackTrace();
		}
		return rf;
	}
	public static void main(String[]args){
		
	}

}

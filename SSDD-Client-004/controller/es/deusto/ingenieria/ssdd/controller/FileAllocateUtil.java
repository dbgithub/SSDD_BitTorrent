package es.deusto.ingenieria.ssdd.controller;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;

public class FileAllocateUtil {
	
	private MetainfoHandlerSingleFile single;
	
	public FileAllocateUtil(MetainfoHandlerSingleFile single){
		this.single= single;
	}
	public RandomAccessFile getFileAllocated(){
		File file = new File("downloads/"+single.getMetainfo().getInfo().getName());
		if(!(file.exists())){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		long bytes = single.getMetainfo().getInfo().getLength();
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

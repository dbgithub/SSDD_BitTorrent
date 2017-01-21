package es.deusto.ingenieria.ssdd.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;

import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;

public class FileAllocateUtil {
	
	private MetainfoHandlerSingleFile single;
	public BitSet donwloadedChunks;
	public ArrayList<BitSet> myBlockInfoByPiece;
	public int numberOfPiecesDownloaded = 0;
	
	public FileAllocateUtil(MetainfoHandlerSingleFile single){
		this.single= single;
		this.donwloadedChunks = new BitSet();
	}
	public RandomAccessFile getFileAllocated() throws FileNotFoundException, IOException{
		File file = new File("downloads/"+single.getMetainfo().getInfo().getName());
		//Initialize bitset with the number of bytes
		int bytes = single.getMetainfo().getInfo().getLength();
		int numberOfPieces = single.getMetainfo().getInfo().getByteSHA1().size();
		int piecelength = single.getMetainfo().getInfo().getPieceLength();
		donwloadedChunks = new BitSet(numberOfPieces);
		myBlockInfoByPiece = new ArrayList<>();
		if(!(file.exists())){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//But all the bitset to false, representing each block of each piece
			
			System.out.println("Block size: "+ piecelength/16384);
			for(int i = 0; i< numberOfPieces; i++){
				BitSet temp = new BitSet(piecelength/16384);
				myBlockInfoByPiece.add(temp);
			}
			
			File outFile = new File("downloads/"+single.getMetainfo().getInfo().getName()+".state");
		    FileOutputStream fos = new FileOutputStream(outFile);
		    ObjectOutputStream oos = new ObjectOutputStream(fos);
		    oos.writeObject(donwloadedChunks);
		    oos.close();
		}
		else{
			ObjectInputStream ois =
	                 new ObjectInputStream(new FileInputStream("downloads/"+single.getMetainfo().getInfo().getName()+".state"));
			BitSet read = null;
			try {
				read = (BitSet) ois.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			donwloadedChunks = read;
			for(int x = 0; x < donwloadedChunks.size(); x++){
				BitSet temp = new BitSet(piecelength/16384);
				if(donwloadedChunks.get(x)){
					for(int y =0; y < temp.size(); y++){
						temp.set(y);
					}
				}
				myBlockInfoByPiece.add(temp);
			}
			
		}
		
		System.out.println("Downloaded now: "+ donwloadedChunks.cardinality());
		numberOfPiecesDownloaded = donwloadedChunks.cardinality();
		RandomAccessFile rf = null;
		try{
		    rf = new RandomAccessFile(file, "rw"); 
		    rf.setLength(bytes);
		    
		}catch(IOException ex){
		    ex.printStackTrace();
		}
		return rf;
	}
	public MetainfoHandlerSingleFile getSingle() {
		return single;
	}
	public void setSingle(MetainfoHandlerSingleFile single) {
		this.single = single;
	}
	public BitSet getDonwloadedChunks() {
		return donwloadedChunks;
	}
	public void setDonwloadedChunks(BitSet donwloadedChunks) {
		this.donwloadedChunks = donwloadedChunks;
	}
	public ArrayList<BitSet> getMyBlockInfoByPiece() {
		return myBlockInfoByPiece;
	}
	public void setMyBlockInfoByPiece(ArrayList<BitSet> myBlockInfoByPiece) {
		this.myBlockInfoByPiece = myBlockInfoByPiece;
	}
	public int getNumberOfPiecesDownloaded() {
		return numberOfPiecesDownloaded;
	}
	public void setNumberOfPiecesDownloaded(int numberOfPiecesDownloaded) {
		this.numberOfPiecesDownloaded = numberOfPiecesDownloaded;
	}
	
	

}

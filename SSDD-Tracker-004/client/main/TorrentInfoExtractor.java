package main;

import java.io.File;

import bitTorrent.metainfo.InfoDictionary;
import bitTorrent.metainfo.MetainfoFile;
import bitTorrent.metainfo.handler.MetainfoHandler;
import bitTorrent.metainfo.handler.MetainfoHandlerMultipleFile;
import bitTorrent.metainfo.handler.MetainfoHandlerSingleFile;

public class TorrentInfoExtractor {
	
	
	public MetainfoHandler extractInformationFromDirectory()
	{
		try {			
			File folder = new File("torrent");
			MetainfoHandler<?> handler = null;
			
			if (folder.isDirectory()) {
				for (File torrent : folder.listFiles()) {
					try {
						if (torrent.getPath().contains(".torrent")) {			
							handler = new MetainfoHandlerSingleFile();
							handler.parseTorrenFile(torrent.getPath());
							return handler;
						}
					} catch (Exception ex) {
						if (torrent.getPath().contains(".torrent")) {			
							handler = new MetainfoHandlerMultipleFile();
							handler.parseTorrenFile(torrent.getPath());
							return handler;
						}
					}
					
					if (handler != null) {
						System.out.println("#######################################\n" + torrent.getPath());
						System.out.println(handler.getMetainfo());
					}
				}
			}
		} catch (Exception ex) {
			System.err.println("# TorrentInfoExtractor: " + ex.getMessage());
		}
		return null;
	}
	
	public MetainfoHandler extractInformationFromFile(File torrent)
	{
		try {
			MetainfoHandler<?> handler = null;
			try {
				if (torrent.getPath().contains(".torrent")) {			
					handler = new MetainfoHandlerSingleFile();
					handler.parseTorrenFile(torrent.getPath());
					return handler;
				}
			} catch (Exception ex) {
				if (torrent.getPath().contains(".torrent")) {			
					handler = new MetainfoHandlerMultipleFile();
					handler.parseTorrenFile(torrent.getPath());
					return handler;
				}
			}
			
			if (handler != null) {
				System.out.println("#######################################\n" + torrent.getPath());
				System.out.println(handler.getMetainfo());
			}
		} catch (Exception ex) {
			System.err.println("# TorrentInfoExtractor: " + ex.getMessage());
		}
		return null;
	}
	
	public static void main(String[]args){
		TorrentInfoExtractor t = new TorrentInfoExtractor();
		MetainfoHandler prueba = t.extractInformationFromDirectory();
		if(prueba instanceof MetainfoHandlerMultipleFile){
			MetainfoHandlerMultipleFile multiple = (MetainfoHandlerMultipleFile) prueba;
			System.out.println("Obtenido \n"+multiple.getMetainfo());
		}
		else if (prueba instanceof MetainfoHandlerSingleFile){
			MetainfoHandlerSingleFile multiple = (MetainfoHandlerSingleFile) prueba;
			System.out.println("Obtenido \n"+multiple.getMetainfo());
		}
	}

}

package net.mdrjr.usbtool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileOps {

	
	public List<String> getFolderContets(String folder) {
		List<String> contents = new ArrayList<String>();
		contents.add(0, "[D] ..");
		File[] files = new File(folder).listFiles();
		
		String s;
		
		for (File file : files) {
			
			if(file.isDirectory()) {
				s = "[D]";
			} else {
				s = "[*]";
			}
			
			s = s + " " + file.getName();
			
			contents.add(s);				
		}
		
		return contents;
	}
	
	public boolean isISO(String file) {
		boolean ret = false;
		
		String ext = "";
		
		int i = file.lastIndexOf(".");
		int p = Math.max(file.lastIndexOf("/"), file.lastIndexOf("\\"));
		if(i > p)
			ext = file.substring(i+1);
		
		if(ext.equals("ISO") || ext.equals("iso"))
			ret = true;
		
		return ret;
	}
	
}

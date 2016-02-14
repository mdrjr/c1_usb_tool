package net.mdrjr.usbtool;

import java.io.InputStream;
import java.util.Scanner;

public class USBMagicTools {

	public String execCmd(String cmd) {
		String ret = "";
		try {
			Process proc = Runtime.getRuntime().exec(cmd);
		
			InputStream is = proc.getInputStream();
			Scanner scanner = new Scanner(is);
			Scanner s = scanner.useDelimiter("\\A");
			String val = "";
			if (s.hasNext()) {
			val = s.next();
			} else {
				val = "";
			}
			s.close();
			scanner.close();
			ret = val;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	} 
	
	
	public void unloadModule() {
			execCmd("rmmod g_mass_storage");
	}

	public void loadModule(String file) {
		unloadModule();
		FileOps fops = new FileOps();
		boolean isIso = fops.isISO(file);
		
		String cmdMountLine = "modprobe g_mass_storage file=" + file;
		
		if(isIso)
			cmdMountLine += " cdrom=1";
		
		System.out.println(cmdMountLine);
		
		execCmd(cmdMountLine);
	}
}

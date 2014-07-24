package us.shandian.vpn.util;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.StringBuilder;

public class RunCommand {
	static {
		System.loadLibrary("system");
	}

	public static String IPTABLES = "iptables";
	public static String IP = "ip";

	public static Process run(String command) throws IOException {
		ProcessBuilder builder = new ProcessBuilder("su");
		Process p = builder.start();
		DataOutputStream dos = new DataOutputStream(p.getOutputStream());

		dos.writeBytes(command + "\n");
		dos.flush();
		dos.writeBytes("exit\n");
		dos.flush();
		return p;
	}
	
	public static String readInput(Process proc) throws IOException {
		DataInputStream dis = new DataInputStream(proc.getInputStream());
		
		StringBuilder s = new StringBuilder();
		String str;
		while ((str = dis.readLine()) != null) {
			s.append(str).append("\n");
		}
		
		return s.toString();
	}
	
	public static String readError(Process proc) throws IOException {
		DataInputStream dis = new DataInputStream(proc.getErrorStream());
		
		StringBuilder s = new StringBuilder();
		String str;
		while ((str = dis.readLine()) != null) {
			s.append(str).append("\n");
		}
		
		return s.toString();
	}

	public static void exportBinaries(Context c) {
		IPTABLES = exportBinary(c, "iptables");
		IP = exportBinary(c, "ip");
	}

	// This method exports the correct binary for the device
	private static String exportBinary(Context c, String name) {
		File f = new File(c.getFilesDir().getPath() + "/" + name);
		if (!f.exists()) {
			try {
				AssetManager am = c.getAssets();
				DataInputStream in = new DataInputStream(am.open(getABI() + "/" + name));
				f.createNewFile();
				FileOutputStream o = new FileOutputStream(f);
				byte[] b = new byte[in.available()];
				in.readFully(b);
				o.write(b);
				in.close();
				o.close();
			} catch (Exception e) {
				f.delete();
				throw new RuntimeException(e);
			}
		}
		
		// Ensure permission
		try {
			run("chmod 0777 " + f.getPath()).waitFor();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return f.getPath();
	}

	private static native String getABI();
}

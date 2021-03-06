package us.shandian.vpn.manager;

import android.content.Context;
import android.text.TextUtils;

import java.lang.StringBuilder;

import us.shandian.vpn.R;
import us.shandian.vpn.util.RunCommand;
import static us.shandian.vpn.util.RunCommand.IP;
import static us.shandian.vpn.util.RunCommand.IPTABLES;
import static us.shandian.vpn.util.RunCommand.PGREP;
import static us.shandian.vpn.util.RunCommand.PKILL;

public class VpnManager
{
	private static final String PPP_UNIT = "100";
	private static final String PPP_INTERFACE = "ppp" + PPP_UNIT;
	private static final int MAX_WAIT_TIME = 15; // seconds
	private static String IFACE = "eth0";
	private static String GATEWAY = "0.0.0.0";
	
	// Start connection to a PPTP server
	public static boolean startVpn(Context context, VpnProfile p) {
		// Check
		if (TextUtils.isEmpty(p.server) || TextUtils.isEmpty(p.username) ||
			TextUtils.isEmpty(p.password)) {
			
			return false;
		}
		
		// Iface
		getDefaultIface();
		
		// Arguments to mtpd
		String[] args = new String[]{IFACE, "pptp", p.server, "1723", "name", p.username,
					"password", p.password, "linkname", "vpn", "refuse-eap", "nodefaultroute",
					"idle", "1800", "mtu", "1400", "mru", "1400", (p.mppe ? "+mppe" : "nomppe"),
					"unit", PPP_UNIT};
		
		// Start
		startMtpd(args);
		
		// Wait for mtpd
		if (!blockUntilStarted()) {
			return false;
		}
		
		if (!p.gfwlist) {
			// Set up ip route
			setupRoute();
		} else {
			// Set up gfwlist
			setupGfwList(context);
		}
		
		// Set up dns
		setupDns(p);
		
		return true;
	}
	
	public static void stopVpn() {
		// Kill all vpn stuff
		StringBuilder s = new StringBuilder();
		s.append(PKILL).append(" mtpd\n")
		 .append(PKILL).append(" pppd\n")
		 .append(IP).append(" ro flush table 200\n")
		 .append(IP).append(" ro flush dev ").append(PPP_INTERFACE).append("\n")
		 .append(IPTABLES).append(" -t nat -F\n")
		 .append(IPTABLES).append(" -t nat -X\n")
		 .append(IPTABLES).append(" -t nat -Z");
		
		try {
			RunCommand.run(s.toString()).waitFor();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean isVpnRunning() {
		try {
			Process p = RunCommand.run(PGREP + " mtpd");
			p.waitFor();
			if (!TextUtils.isEmpty(RunCommand.readInput(p).replace("\n", "").trim())) {
				return true;
			}
		} catch (Exception e) {
			
		}
		
		return false;
	}
	
	private static void getDefaultIface() {
		String routes;
		
		try {
			Process p = RunCommand.run(IP + " ro");
			p.waitFor();
			routes = RunCommand.readInput(p);
		} catch (Exception e) {
			routes = null;
		}
		
		if (routes != null) {
			for (String route : routes.split("\n")) {
				if (route.startsWith("default")) {
					boolean lastIsDev = false, lastIsVia = false;
					for (String ele : route.split(" ")) {
						if (lastIsDev) {
							IFACE = ele;
							lastIsDev = false;
						} else if (lastIsVia) {
							GATEWAY = ele;
							lastIsVia = false;
						} else if (ele.equals("dev")) {
							lastIsDev = true;
						} else if (ele.equals("via")) {
							lastIsVia = true;
						}
					}
					
					break;
				}
			}
		}
	}
	
	private static void startMtpd(String[] args) {
		StringBuilder s = new StringBuilder();
		s.append("mtpd");
		
		// Add args
		for (String arg : args) {
			s.append(" ").append(arg);
		}
		
		// Run
		try {
			RunCommand.run(s.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static boolean blockUntilStarted() {
		int n = MAX_WAIT_TIME * 2;
		
		for (int i = 0; i < n; i++) {
			try {
				Process p = RunCommand.run(IP + " ro");
				p.waitFor();
				String out = RunCommand.readInput(p);
				
				if (out.contains(PPP_INTERFACE)) {
					return true;
				} else {
					Thread.sleep(500);
				}
			} catch (Exception e) {
				break;
			}
		}
		
		return false;
	}
	
	private static void setupRoute() {
		StringBuilder s = new StringBuilder();
		s.append(IP).append(" ro add 0.0.0.0/1 dev ").append(PPP_INTERFACE).append("\n")
		 .append(IP).append(" ro add 128.0.0.0/1 dev ").append(PPP_INTERFACE).append("\n")
		 .append(IP).append(" ru add from all table 200 \n")
		 .append(IP).append(" ro add default dev ").append(PPP_INTERFACE).append(" table 200");
		
		// Run
		try {
			RunCommand.run(s.toString()).waitFor();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void setupGfwList(Context context) {
		// Bypass the Chinese Websites
		String[] gfwlist = context.getResources().getStringArray(R.array.gfwlist);

		StringBuilder s = new StringBuilder();

		for (String l : gfwlist) {
			s.append(IP).append(" ro add ").append(l)
						.append(" dev ").append(PPP_INTERFACE).append(" table 200\n");
		}

		// Run
		try {
			RunCommand.run(s.toString()).waitFor();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void setupDns(VpnProfile profile) {
		// For now, I haven't got any idea of how to get the DNS returned by pppd
		// So we just use 8.8.8.8 and 8.8.4.4
		
		String dns1 = null, dns2 = null;
		
		try {
			Process p = RunCommand.run("getprop net.dns1");
			p.waitFor();
			dns1 = RunCommand.readInput(p).replace("\n", "").trim();
			p = RunCommand.run("getprop net.dns2");
			p.waitFor();
			dns2 = RunCommand.readInput(p).replace("\n", "").trim();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		if (TextUtils.isEmpty(dns1) || TextUtils.isEmpty(dns2)) {
			return;
		}
		
		StringBuilder s = new StringBuilder();
		s.append(IPTABLES).append(" -t nat -A OUTPUT -d ").append(dns1).append("/32 -o ")
			.append(PPP_INTERFACE).append(" -p udp -m udp --dport 53 -j DNAT --to-destination ").append(profile.dns1).append(":53\n")
		 .append(IPTABLES).append(" -t nat -A OUTPUT -d ").append(dns2).append("/32 -o ")
			.append(PPP_INTERFACE).append(" -p udp -m udp --dport 53 -j DNAT --to-destination ").append(profile.dns2).append(":53");
		
		try {
			RunCommand.run(s.toString()).waitFor();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
}

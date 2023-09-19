package adadsmonitor;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Monitor implements ActionListener {

	ADADSLink link;
	Timer timer;
	TimerTask task;

	SystemTray tray;
	TrayIcon trayIcon;

	ArrayList<String> hcMemory;
	ArrayList<String> adhocMemory;
	Boolean hardcopy;
	
	public static void main(String[] args) {

		Boolean headless = true;
		long delay = 5 * 1000;

		Monitor mon = new Monitor();;
		try {
			mon.init(headless);

			mon.start(mon, delay);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	Monitor()  {
		hcMemory = new ArrayList<String>();
		adhocMemory = new ArrayList<String>();
		hardcopy = false;
		
		tray = SystemTray.getSystemTray();

		Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));

		PopupMenu popup = new PopupMenu();
		MenuItem close = new MenuItem("Close");
		close.addActionListener(this);
		popup.add(close);

		trayIcon = new TrayIcon(image, "ADADS Monitor", popup);
		trayIcon.setImageAutoSize(true);
	}

	void init(Boolean headless) throws AWTException {
		link = new ADADSLink(headless);
		link.open();

		timer = new Timer();
		tray.add(trayIcon);
	}

	void start(Monitor parent, long delay) {
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				ArrayList<String> queried = link.switchAndQuery();				
				if (hardcopy) {
					compare(parent, hardcopy, queried, hcMemory);
					hcMemory = queried;
				} else {
					compare(parent, hardcopy, queried, adhocMemory);
					adhocMemory = queried;
				}
				
				hardcopy = !hardcopy;
			}
		}, 0, delay);
	}

	void notify(String title, String message, MessageType type) {
		trayIcon.displayMessage(title, message, type);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		timer.cancel();
		link.close();
		tray.remove(trayIcon);
	}

	static void compare(Monitor mon, Boolean hardcopy, ArrayList<String> queried, ArrayList<String> memory) {
		ArrayList<String> comp = new ArrayList<String>(queried);
		comp.removeAll(memory);
		if (comp.size() > 0) {
			mon.notify("You've got mail!", String.format("%d new %s order(s).", comp.size(), hardcopy ? "Hardcopy" : "Ad Hoc"), MessageType.INFO);
		}
	}
}

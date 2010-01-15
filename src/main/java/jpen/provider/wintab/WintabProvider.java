/* [{
Copyright 2007, 2008 Nicolas Carranza <nicarran at gmail.com>

This file is part of jpen.

jpen is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License,
or (at your option) any later version.

jpen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with jpen.  If not, see <http://www.gnu.org/licenses/>.
}] */
package jpen.provider.wintab;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.PLevel;
import jpen.provider.AbstractPenProvider;
import jpen.provider.NativeLibraryLoader;
import jpen.provider.VirtualScreenBounds;
import jpen.internal.BuildInfo;
import jpen.internal.ObjectUtils;
import jpen.internal.Range;

public class WintabProvider
	extends AbstractPenProvider {
	private static final Logger L=Logger.getLogger(WintabProvider.class.getName());
	public static final int PERIOD=10;
	private static final NativeLibraryLoader LIB_LOADER=new NativeLibraryLoader(new String[]{""},
			new String[]{"64"},
			Integer.valueOf(BuildInfo.getProperties().getString("jpen.provider.wintab.nativeVersion")));
	//static{L.setLevel(Level.ALL);}

	static void loadLibrary(){
		LIB_LOADER.load();
	}

	final WintabAccess wintabAccess;
	private final Map<Integer, WintabDevice> cursorToDevice=new HashMap<Integer, WintabDevice>();
	private final Range[] levelRanges=new Range[PLevel.Type.VALUES.size()];
	final VirtualScreenBounds screenBounds=VirtualScreenBounds.getInstance();
	private final Thread thread;
	private boolean paused=true;

	public static class Constructor
		extends AbstractPenProvider.AbstractConstructor{
		//@Override
		public String getName() {
			return "Wintab";
		}
		//@Override
		public boolean constructable(PenManager penManager) {
			return System.getProperty("os.name").toLowerCase().contains("windows");
		}

		//@Override
		public PenProvider constructProvider() throws Throwable {
			loadLibrary();
			WintabAccess wintabAccess=new WintabAccess();
			return new WintabProvider(this, wintabAccess);
		}
		@Override
		public int getNativeVersion(){
			return LIB_LOADER.nativeVersion;
		}
		@Override
		public int getNativeBuild(){
			loadLibrary();
			return WintabAccess.getNativeBuild();
		}
		@Override
		public int getExpectedNativeBuild(){
			return Integer.valueOf(BuildInfo.getProperties().getString("jpen.provider.wintab.nativeBuild"));
		}
	}



	private WintabProvider(Constructor constructor, WintabAccess wintabAccess) {
		super(constructor);
		L.fine("start");
		this.wintabAccess=wintabAccess;

		for(int i=PLevel.Type.VALUES.size(); --i>=0;){
			PLevel.Type levelType=PLevel.Type.VALUES.get(i);
			levelRanges[levelType.ordinal()]=wintabAccess.getLevelRange(levelType);
		}

		thread=new Thread("jpen-WintabProvider") {
						 private long getPeriod(){
							 return PERIOD;
							 //return Math.max(10,
							 //						 getConstructor().getPenManager().pen.getPeriodMillis()/2);
						 }
						 public void run() {
							 while(true) {
								 processQuedEvents();
								 ObjectUtils.synchronizedWait(this, getPeriod());
								 while(getPaused()){
									 L.fine("going to wait...");
									 ObjectUtils.synchronizedWait(this, 0);
									 L.fine("notified");
								 }
							 }
						 }
					 }
					 ;
		thread.setDaemon(true);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();

		L.fine("end");
	}

	Range getLevelRange(PLevel.Type type) {
		return levelRanges[type.ordinal()];
	}

	private void processQuedEvents() {
		L.finer("start");
		while(wintabAccess.nextPacket()) {
			WintabDevice device=getDevice(wintabAccess.getCursor());
			if(L.isLoggable(Level.FINE)){
				L.finer("device: ");
				L.finer(device.getName());
			}
			device.scheduleEvents();
		}
		L.finer("end");
	}

	private WintabDevice getDevice(int cursor) {
		WintabDevice wintabDevice=cursorToDevice.get(cursor);
		if(wintabDevice==null) {
			cursorToDevice.put(cursor, wintabDevice=new WintabDevice(this, cursor));
			devices.clear();
			devices.addAll(cursorToDevice.values());
			getPenManager().firePenDeviceAdded(getConstructor(), wintabDevice);
		}
		return wintabDevice;
	}

	//@Override
	public void penManagerPaused(boolean paused) {
		setPaused(paused);
	}

	private synchronized boolean getPaused(){
		return paused;
	}

	synchronized void setPaused(boolean paused) {
		L.fine("start");
		if(paused==this.paused)
			return;
		this.paused=paused;
		if(!paused){
			L.fine("false paused value");
			screenBounds.reset();
			synchronized(thread) {
				L.fine("going to notify all...");
				thread.notifyAll();
				L.fine("done notifying ");
			}
		}
		wintabAccess.setEnabled(!paused); // this clears the queue
		L.fine("end");
	}
	
	@Override
	public boolean getUseRelativeLocationFilter(){
		return true;
	}
}
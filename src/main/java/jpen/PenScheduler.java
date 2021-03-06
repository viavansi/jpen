/* [{
Copyright 2007, 2008, 2009 Nicolas Carranza <nicarran at gmail.com>

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
package jpen;

import java.awt.geom.Point2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import jpen.owner.PenOwner;
import jpen.internal.filter.RelativeLocationFilter;

final class PenScheduler {

	static final Logger L=Logger.getLogger(PenScheduler.class.getName());
	//static { L.setLevel(Level.ALL); }

	private final Pen pen;
	private PenEvent lastScheduledEvent;
	public final PenState lastScheduledState=new PenState();
	private final SystemMouseFilter systemMouseFilter;
	private final List<PLevel> scheduledLevels=new ArrayList<PLevel>();

	PenScheduler(Pen pen) {
		this.pen=pen;
		lastScheduledEvent=pen.getLastDispatchedEvent();
		this.systemMouseFilter=new SystemMouseFilter(pen.penManager);
	}

	/**
	The SystemMouseFilter is used to filter system mouse movement to avoid conflict with movements coming from other devices.
	*/
	private static class SystemMouseFilter {

		public static int THRESHOLD_PERIOD=100;
		private final PenManager penManager;
		private PLevelEvent lastLevelEvent; // last level event scheduled (not filtered)
		boolean filteredFirstInSecuence;
		private long firstInSecuenceTime;
		boolean nonSystemMouseDevicePresent;
		private int preferredKindTypeNumber=PKind.Type.IGNORE.ordinal();
		boolean mayBeDraggingIn=true;

		SystemMouseFilter(PenManager penManager) {
			this.penManager=penManager;
		}
		/**
		@return {@code true} if the device is the system mouse and other device has already scheduled level events
		*/
		boolean filterOut(PenDevice device) {
			if(device==penManager.emulationDevice)
				return false;
			if(penManager.isSystemMouseDevice(device)) {
				mayBeDraggingIn=false;
				long time=System.currentTimeMillis();
				if(lastLevelEvent!=null &&
						lastLevelEvent.getDeviceId()!=device.getId() &&
						time-lastLevelEvent.time<=THRESHOLD_PERIOD
				  ) {
					return true;
				}
				if(!filteredFirstInSecuence) {
					filteredFirstInSecuence=true;
					firstInSecuenceTime=time;
					return nonSystemMouseDevicePresent;
				}
				if(time-firstInSecuenceTime<=THRESHOLD_PERIOD) {
					return nonSystemMouseDevicePresent;
				}
				preferredKindTypeNumber=device.getKindTypeNumber();
			} else {
				preferredKindTypeNumber=device.getKindTypeNumber();
				if(penManager.getSystemMouseProvider()!=null) {// filter out level events when coming dragging in from another AWT component.
					if(mayBeDraggingIn)
						return true;
				}
				nonSystemMouseDevicePresent=true;
				filteredFirstInSecuence=false;
			}
			return false;
		}

		void setLastLevelEvent(PLevelEvent lastLevelEvent) {
			if(lastLevelEvent.getDeviceId()==penManager.emulationDevice.getId())
				return;
			this.lastLevelEvent=lastLevelEvent;
		}

		void setFirstTimeFilteringAfterPause() {
			nonSystemMouseDevicePresent=false;
			filteredFirstInSecuence=false;
			mayBeDraggingIn=true;
		}

		int getPreferredKindTypeNumber() {
			return preferredKindTypeNumber;
		}
	}

	private volatile boolean firstScheduleAfterPause;

	synchronized void setPaused(boolean paused) {
		if(paused) {
			scheduleEmulatedZeroPressureEvent();
			scheduleButtonReleasedEvents();
		} else {
			firstScheduleAfterPause=true;
			relativeLocationFilter.reset();
			systemMouseFilter.setFirstTimeFilteringAfterPause();
		}
	}

	private synchronized void scheduleEmulatedZeroPressureEvent() {
		if(lastScheduledState.levels.getValue(PLevel.Type.PRESSURE)>0)
			scheduleLevelEvent(new PLevelEvent(getEmulationDevice(), System.currentTimeMillis(),new PLevel[] {new PLevel(PLevel.Type.PRESSURE, 0)}));
	}

	private final Point clipLocationOnScreen=new Point();
	private final Point2D.Float scheduledLocation=new Point2D.Float();
	private final RelativeLocationFilter relativeLocationFilter=new RelativeLocationFilter();

	synchronized boolean scheduleLevelEvent(PenDevice device, long deviceTime, Collection<PLevel> levels, boolean levelsOnScreen) {
		if(device.getProvider().getUseRelativeLocationFilter()) {
			if(relativeLocationFilter.filter(lastScheduledState, device, levels, levelsOnScreen))
				switch(relativeLocationFilter.getState()) {
				case RELATIVE:
					device.penManagerSetUseFractionalMovements(false);
					break;
				case ABSOLUTE:
					device.penManagerSetUseFractionalMovements(true);
					break;
				default:
				}
		}
		
		if(systemMouseFilter.filterOut(device))
			return false;

		int kindTypeNumber=systemMouseFilter.getPreferredKindTypeNumber();
		if(kindTypeNumber!=PKind.Type.IGNORE.ordinal() &&
				kindTypeNumber!=lastScheduledState.getKind().typeNumber &&
				!lastScheduledState.hasPressedButtons()) {
			PKind newKind=PKind.valueOf(kindTypeNumber);
			if(L.isLoggable(Level.FINE)) {
				L.fine("changing kind to:"+newKind);
				L.fine("scheduledLevels: "+scheduledLevels);
				L.fine("device: "+device);
			}
			lastScheduledState.setKind(newKind);
			scheduleEmulatedZeroPressureEvent();
			schedule(new PKindEvent(device, deviceTime, newKind));
		}

		scheduledLevels.clear();
		float scheduledPressure=-1;
		boolean scheduledMovement=false;
		scheduledLocation.x=lastScheduledState.getLevelValue(PLevel.Type.X);
		scheduledLocation.y=lastScheduledState.getLevelValue(PLevel.Type.Y);
		PenOwner penOwner=pen.penManager==null? null:pen.penManager.penOwner; // pen.penManager can be null when running tests
		if(penOwner!=null && levelsOnScreen)
			penOwner.getPenClip().evalLocationOnScreen(clipLocationOnScreen);
		for(PLevel level:levels) {
			if(device!=getEmulationDevice() && pen.levelEmulator!=null &&
					pen.levelEmulator.onActivePolicy(lastScheduledState.getKind().getType().ordinal(),
							level.typeNumber))
				continue;
			if(level.value.isNaN())
				continue;
			if(level.isMovement()) {
				float levelValue=level.value;
				switch(level.getType() ) {
				case X:
					if(levelsOnScreen)
						levelValue-=clipLocationOnScreen.x;
					scheduledLocation.x=levelValue;
					break;
				case Y:
					if(levelsOnScreen)
						levelValue-=clipLocationOnScreen.y;
					scheduledLocation.y=levelValue;
					break;
				default:
					throw new AssertionError();
				}
				if(!firstScheduleAfterPause) {
					float lastLevelValue=lastScheduledState.getLevelValue(level.typeNumber);
					if(levelValue==lastLevelValue)
						continue;
					if(pen.penManager.isSystemMouseDevice(device) && Math.abs(levelValue-lastLevelValue)<1)
						continue;
				}
				level.value=levelValue;
				scheduledMovement=true;
			} else {
				if(!firstScheduleAfterPause && level.value==lastScheduledState.getLevelValue(level.typeNumber))
					continue;
				switch(level.getType()) {
				case PRESSURE:
					scheduledPressure=level.value;
					break;
				default:
				}
			}
			scheduledLevels.add(level);
		}
		if(scheduledLevels.isEmpty())
			return false;

		if(scheduledMovement) {
			if(penOwner!=null &&  !penOwner.isDraggingOut() && !penOwner.getPenClip().contains(scheduledLocation))
				return false;
		}

		scheduleOnPressureButtonEvent(scheduledPressure);

		scheduleLevelEvent(new PLevelEvent(device, deviceTime,
										   scheduledLevels.toArray(new PLevel[scheduledLevels.size()])));

		firstScheduleAfterPause=false;

		return true;
	}

	private void scheduleLevelEvent(PLevelEvent levelEvent) {
		lastScheduledState.levels.setValues(levelEvent);
		schedule(levelEvent);
		systemMouseFilter.setLastLevelEvent(levelEvent);
	}

	private void scheduleOnPressureButtonEvent(float scheduledPressure) {
		if(scheduledPressure==-1)
			return;
		boolean isOnPressure=lastScheduledState.getButtonValue(PButton.Type.ON_PRESSURE);
		if(scheduledPressure>0) {
			if(isOnPressure)
				return;
			scheduleEmulatedButtonEvent(new PButton(PButton.Type.ON_PRESSURE.ordinal(), true));
		} else if(isOnPressure) // here scheduledPressure==0
			scheduleEmulatedButtonEvent(new PButton(PButton.Type.ON_PRESSURE.ordinal(), false));
	}

	synchronized void scheduleButtonReleasedEvents() {
		for(int i=PButton.Type.VALUES.size(); --i>=0;)
			scheduleEmulatedButtonEvent(new PButton(i, false));
		for(Integer extButtonTypeNumber: lastScheduledState.extButtonTypeNumberToValue.keySet())
			scheduleEmulatedButtonEvent(new PButton(extButtonTypeNumber, false));
	}

	private void scheduleEmulatedButtonEvent(PButton button) {
		scheduleButtonEvent(getEmulationDevice(), System.currentTimeMillis(), button);
	}

	private PenDevice getEmulationDevice() {
		return pen.penManager.emulationDevice;
	}

	synchronized void scheduleButtonEvent(PenDevice device, long deviceTime, PButton button) {
		if(lastScheduledState.setButtonValue(button.typeNumber, button.value)) {
			if(L.isLoggable(Level.FINE))
				L.fine("scheduling button event: "+button);
			PButtonEvent buttonEvent=new PButtonEvent(device, deviceTime, button);
			schedule(buttonEvent);
			if(pen.levelEmulator!=null)
				pen.levelEmulator.scheduleEmulatedEvent(buttonEvent);
		}
	}

	synchronized void scheduleScrollEvent(PenDevice device, long deviceTime, PScroll scroll) {
		schedule(new PScrollEvent(device, deviceTime, scroll));
	}

	private void schedule(PenEvent ev) {
		ev.time=System.currentTimeMillis();
		//System.out.println("*** scheduled event *** "+System.currentTimeMillis()+", ev.time="+ev.time);
		ev.setPenOwnerTag(pen.penManager.penOwner.evalPenEventTag(ev));
		lastScheduledEvent.next=ev;
		lastScheduledEvent=ev;
		pen.processNewEvents();
	}
}
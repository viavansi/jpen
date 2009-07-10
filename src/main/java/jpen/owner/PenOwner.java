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
package jpen.owner;

import java.util.Collection;
import jpen.owner.PenClip;
import jpen.PenManager;
import jpen.PenProvider;

/**
A PenOwner defines the available {@link PenProvider}s and unpauses/pauses the {@link PenManager} whenever the mouse cursor enters/exits the {@link PenClip}. The PenOwner allows drag-out operation.
*/
public interface PenOwner{
	/**
	Called once by the {@link PenManager#PenManager(PenOwner)} constructor to get the available {@link jpen.PenProvider.Constructor}s and use them to setup the {@link PenProvider}s.
	*/
	Collection<PenProvider.Constructor> getPenProviderConstructors();

	/**
	Called once by the {@link PenManager#PenManager(PenOwner)} constructor to allow this PenOwner to access the PenManager through the given {@link PenManagerHandle}.
	*/
	void setPenManagerHandle(PenManagerHandle penManagerHandle);

	/**
	Wraps a {@link PenManager} and defines extra accessor methods. See {@link #setPenManagerHandle(PenManagerHandle)}.
	*/
	public interface PenManagerHandle{
		PenManager getPenManager();
		/**
		@return synchronization lock held by the {@link PenManager} when calling {@link #isDraggingOut()}.
		*/
		Object getPenSchedulerLock();
		/**
		Unpause/pause the {@link PenManager}'s {@link PenProvider}s from the scheduling of {@link jpen.PenEvent}s.
		*/
		void setPenManagerPaused(boolean paused);
	}

	/**
	@return The PenClip associated with this PenOwner.
	*/
	PenClip getPenClip();

	/**
	Called by the {@link PenManager} to avoid sending {@link jpen.PLevelEvent}s to clients when the location is outside the PenClip and this method returns {@code false}. The PenManager calls this method holding the {@link PenManagerHandle#getPenSchedulerLock()} synchronization lock.

	@return {@code true} if this PenOwner is currently in a drag-out operation.
	*/
	boolean isDraggingOut();
}
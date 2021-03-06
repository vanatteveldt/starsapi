package org.starsautohost.starsapi.block;

public abstract class Waypoint extends Block{

	public int x;
	public int y;
	public int warp;
	
	public Waypoint cloneWaypoint() throws Exception{
		return (Waypoint)cloneBlock();
	}

	public String toStringOld() {
		return super.toString();
	}
}

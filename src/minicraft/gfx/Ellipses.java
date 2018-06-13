package minicraft.gfx;

import minicraft.core.Updater;
import minicraft.gfx.Ellipses.DotUpdater.CallUpdater;
import minicraft.gfx.Ellipses.DotUpdater.TimeUpdater;

public abstract class Ellipses {
	
	private final DotUpdater updateMethod;
	
	protected Ellipses(DotUpdater updateMethod, int intervalCount) {
		this.updateMethod = updateMethod;
		updateMethod.setIntervalCount(intervalCount);
	}
	
	public String updateAndGet() {
		updateMethod.update();
		return get();
	}
	protected abstract String get();
	protected void nextInterval(int interval) {}
	
	protected int getInterval() { return updateMethod.getInterval(); }
	protected int getIntervalCount() { return updateMethod.getIntervalCount(); }
	
	public static class SequentialEllipses extends Ellipses {
		public SequentialEllipses() { this(new CallUpdater(Updater.normSpeed*2/3)); }
		public SequentialEllipses(DotUpdater updater) {
			super(updater, 3);
		}
		
		@Override
		public String get() {
			StringBuilder dots = new StringBuilder();
			int ePos = getInterval();
			for(int i = 0; i < getIntervalCount(); i++) {
				if (ePos == i)
					dots.append(".");
				else
					dots.append(" ");
			}
			
			return dots.toString();
		}
	}
	
	public static class SmoothEllipses extends Ellipses {
		
		private static final String dotString = "   ";
		
		private final char[] dots = dotString.toCharArray();
		
		public SmoothEllipses() { this(new TimeUpdater()); }
		public SmoothEllipses(DotUpdater updater) {
			super(updater, dotString.length()*2);
			updater.setEllipses(this);
		}
		
		@Override
		public String get() { return new String(dots); }
		
		@Override
		protected void nextInterval(int interval) {
			int epos = interval % dots.length;
			char set = interval < getIntervalCount()/2 ? '.' : ' ';
			dots[epos] = set;
		}
	}
	
	
	public static abstract class DotUpdater {
		private final int countPerCycle;
		private int intervalCount;
		private int curInterval;
		private int countPerInterval;
		private int counter;
		
		private Ellipses ellipses = null;
		
		private boolean started = false;
		
		protected DotUpdater(int countPerCycle) {
			this.countPerCycle = countPerCycle;
		}
		
		void setEllipses(Ellipses ellipses) { this.ellipses = ellipses; }
		
		// called by Ellipses classes, passing their value.
		void setIntervalCount(int numIntervals) {
			intervalCount = numIntervals;
			countPerInterval = Math.max(1, Math.round(countPerCycle / (float)intervalCount));
		}
		
		public int getInterval() { return curInterval; }
		public int getIntervalCount() { return intervalCount; }
		
		private void incInterval(int amt) {
			if(ellipses != null)
				for(int i = curInterval+1; i <= curInterval+amt; i++)
					ellipses.nextInterval(i%intervalCount);
			
			curInterval += amt;
			curInterval %= intervalCount;
		}
		
		protected void incCounter(int amt) {
			counter += amt;
			int intervals = counter / countPerInterval;
			if(intervals > 0) {
				incInterval(intervals);
				counter -= intervals * countPerInterval;
			}
		}
		
		void start() { started = true; }
		void update() {
			if(!started)
				start();
		}
		
		public static class TickUpdater extends DotUpdater {
			private int lastTick;
			
			public TickUpdater() { this(Updater.normSpeed); }
			public TickUpdater(int ticksPerCycle) {
				super(ticksPerCycle);
			}
			
			@Override
			void start() { super.start(); lastTick = Updater.tickCount; }
			
			@Override
			void update() {
				super.update();
				int newTick = Updater.tickCount;
				int ticksPassed = newTick - lastTick;
				lastTick = newTick;
				incCounter(ticksPassed);
			}
		}
		
		public static class TimeUpdater extends DotUpdater {
			private long lastTime;
			
			public TimeUpdater() { this(750); }
			public TimeUpdater(int millisPerCycle) {
				super(millisPerCycle);
			}
			
			@Override
			void start() { super.start(); lastTime = System.nanoTime(); }
			
			@Override
			void update() {
				super.update();
				long now = System.nanoTime();
				int diffMillis = (int) ((now - lastTime) / 1E6);
				lastTime = now;
				incCounter(diffMillis);
			}
		}
		
		public static class CallUpdater extends DotUpdater {
			
			public CallUpdater(int callsPerCycle) {
				super(callsPerCycle);
			}
			
			@Override
			void update() { super.update(); incCounter(1); }
		}
	}
}

package transversal.data_exchange_toolbox;

import service.ConcurentTask;

public class SpeedMonitor {

	private ConcurentTask task;

	public void start() {
		
		task = new ConcurentTask(this);
		
	}

	public void stop() {
		task.stop();
	}

	public double getAverage() {
		task.stop();
		double sum = 0;
		for(Double speed:task.BANDWIDTH){
			sum=sum+speed;
		}
		return sum/(task.BANDWIDTH.size());
	}

}

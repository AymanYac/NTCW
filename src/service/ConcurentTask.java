package service;

import controllers.Auto_classification_launch;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import model.TimeMasterRow;
import transversal.data_exchange_toolbox.SpeedMonitor;
import transversal.generic.Tools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class ConcurentTask {


	private boolean stop=false;
	Thread task;
	public ArrayList<Double> BANDWIDTH = new ArrayList<Double>();
	protected int number_of_dots=0;

	public ConcurentTask(TimeMasterRow timeMasterRow, DownloadSegmenter downloadSegmenter) {
		Task<Void> jfxTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				time_row_segment_refresh(timeMasterRow);
				return null;
			}
		};

		jfxTask.setOnSucceeded(event -> {
			
		});
		jfxTask.setOnFailed(event -> {
			Throwable problem = jfxTask.getException();
		    problem.printStackTrace(System.err);
		});
		jfxTask.setOnCancelled(event -> {
			stop=true;
		});
		
		Thread task = new Thread(jfxTask);; task.setDaemon(true);
		task.start();
		this.task=task;
	}

	
	


	public ConcurentTask(TimeMasterRow timeMasterRow,boolean isData) {
		if(!isData) {
			Task<Void> jfxTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					time_from_start_refresh(timeMasterRow);
					return null;
				}
			};

			jfxTask.setOnSucceeded(event -> {
				
			});
			jfxTask.setOnFailed(event -> {
				Throwable problem = jfxTask.getException();
			    problem.printStackTrace(System.err);
			});
			jfxTask.setOnCancelled(event -> {
				stop=true;
			});
			
			Thread task = new Thread(jfxTask);; task.setDaemon(true);
			task.start();
			this.task=task;
		}else {
			Task<Void> jfxTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					time_row_auto_refresh(timeMasterRow);
					return null;
				}
			};

			jfxTask.setOnSucceeded(event -> {
				
			});
			jfxTask.setOnFailed(event -> {
				Throwable problem = jfxTask.getException();
			    problem.printStackTrace(System.err);
			});
			jfxTask.setOnCancelled(event -> {
				stop=true;
			});
			
			Thread task = new Thread(jfxTask);; task.setDaemon(true);
			task.start();
			this.task=task;
		}
		
		
	}


	

	public ConcurentTask(SpeedMonitor speedMonitor) {
		Task<Void> jfxTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				read_network_speed();
				return null;
			}
		};

		jfxTask.setOnSucceeded(event -> {
			
		});
		jfxTask.setOnFailed(event -> {
			Throwable problem = jfxTask.getException();
		    problem.printStackTrace(System.err);
		});
		jfxTask.setOnCancelled(event -> {
			stop=true;
		});
		
		Thread task = new Thread(jfxTask);; task.setDaemon(true);
		task.start();
		this.task=task;
	}


	




	public ConcurentTask(TimeMasterRow timeMasterRow, HashSet<TimeMasterRow> tmp) {
		Task<Void> jfxTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				summary_refresh(timeMasterRow,tmp);
				return null;
			}
		};

		jfxTask.setOnSucceeded(event -> {
			
		});
		jfxTask.setOnFailed(event -> {
			Throwable problem = jfxTask.getException();
		    problem.printStackTrace(System.err);
		});
		jfxTask.setOnCancelled(event -> {
			stop=true;
		});
		
		Thread task = new Thread(jfxTask);; task.setDaemon(true);
		task.start();
		this.task=task;
		
	}




	protected void summary_refresh(TimeMasterRow tmr, HashSet<TimeMasterRow> tmp) {
		
		while(!stop) {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			
			Platform.runLater(new Runnable() {
			    public void run() {
			    	
			    	double remaining=0;
					for(TimeMasterRow node:tmp) {
						try{
							remaining+= node.remaining_time>0?node.remaining_time:0;
						}catch(Exception V) {
							stop = true;
							
						}
					}
					
					double elapsed=0;
					for(TimeMasterRow node:tmp) {
						try{
							elapsed+= node.elapsed_time>0?node.elapsed_time:0;
						}catch(Exception V) {
							stop = true;
							
						}
					}
					
					
					
					
					
					//tmr.first.setText(Tools.formatDuration(Duration.between(tmr.start, Instant.now())));
					tmr.first.setText(Tools.formatDuration(Duration.ofSeconds((long) elapsed)));
					tmr.second.setText(Tools.formatDuration(Duration.ofNanos((long) remaining)));
					tmr.remaining_time=remaining;
					tmr.elapsed_time = (long) elapsed;
			    }
			});
		}
		
	}




	protected void read_network_speed() {
		while(!stop) {
			String sortie = "CurrentBandwidth: 0.0 kb";
			try {
				sortie = Tools.RunVBS( Paths.get(getClass().getResource("/scripts/bandwidth.vbs").toURI()).toFile() ) ;
			} catch (IOException | URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			double speed = Double.valueOf((sortie.split("CurrentBandwidth: ")[1].split(" ")[0]).replace(",", ""));
			if(speed!=0) {
				BANDWIDTH.add(speed);
			}
		}
		
	}


	protected void time_from_start_refresh(TimeMasterRow timeMasterRow) {
		while(!stop) {
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Platform.runLater(new Runnable() {
			    public void run() {
			    	timeMasterRow.first.setText(Tools.formatDuration(Duration.between(timeMasterRow.start, Instant.now())));
			    	timeMasterRow.elapsed_time = Duration.between(timeMasterRow.start, Instant.now()).getSeconds();
			    }
			});
			
			
			
		}
	}
	
	protected void time_row_segment_refresh(TimeMasterRow timeMasterRow) {
		while(!stop) {
			try {
				TimeUnit.MILLISECONDS.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Platform.runLater(new Runnable() {
			    public void run() {
			    	timeMasterRow.first.setText(Tools.formatDuration(Duration.between(timeMasterRow.start, Instant.now())));
			    	timeMasterRow.remaining_time = timeMasterRow.remaining_time - Duration.ofSeconds(1).toNanos();
			    	timeMasterRow.remaining_time = timeMasterRow.remaining_time>0?timeMasterRow.remaining_time:0;
			    	timeMasterRow.second.setText(Tools.formatDuration(Duration.ofNanos((long) timeMasterRow.remaining_time)));
			    	timeMasterRow.elapsed_time = Duration.between(timeMasterRow.start, Instant.now()).getSeconds();
			    }
			});
			
			
			
		}
	}
	
	protected void time_row_auto_refresh(TimeMasterRow tmr) {
		
		
		
		while(!stop) {
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Platform.runLater(new Runnable() {
			    public void run() {
			    	tmr.first.setText(Tools.formatDuration(Duration.between(tmr.start, Instant.now())));
			    	tmr.elapsed_time = Duration.between(tmr.start, Instant.now()).getSeconds();
			    }
			});
			
			
			Platform.runLater(new Runnable() {
			    public void run() {
			    	
			    	//tmr.increment_data(speed);
			    }
			});
			
		}
	}


	public ConcurentTask(Auto_classification_launch controller) {
		Task<Void> jfxTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				add_dots(controller);
				return null;
			}

			private void add_dots(Auto_classification_launch controller) {
				while(!stop) {
					try {
						TimeUnit.MILLISECONDS.sleep(800);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					number_of_dots = number_of_dots+1;
					int generation = number_of_dots%4;
					
					Platform.runLater(new Runnable() {
					    public void run() {
					    	if(!stop) {
					    		controller.estimated.setText("Estimated time: "+new String(new char[generation]).replace("\0", "."));
					    		try {
					    			controller.parametersController.estimated.setText("Estimated time: "+new String(new char[generation]).replace("\0", "."));
					    		}catch(Exception V) {
					    			
					    		}
					    	}
					    	
					    }
					});
					
					
				}
			}
		};

		jfxTask.setOnSucceeded(event -> {
			
		});
		jfxTask.setOnFailed(event -> {
			Throwable problem = jfxTask.getException();
		    problem.printStackTrace(System.err);
		});
		jfxTask.setOnCancelled(event -> {
			stop=true;
		});
		
		Thread task = new Thread(jfxTask);; task.setDaemon(true);
		task.start();
		this.task=task;
	}
	


	



	public ConcurentTask(ArrayList<TableView> TABLES) {
		Task<Void> jfxTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				
				add_dots(TABLES);
				return null;
			}

			private void add_dots(ArrayList<TableView> TABLES) {
				while(!stop) {
					number_of_dots = number_of_dots+1;
					int generation = number_of_dots%4;
					
					Platform.runLater(new Runnable() {
					    public void run() {
					    	if(!stop) {
					    		
					    		for(TableView table:TABLES) {
					    			Label ph = new Label("Loading projects "+new String(new char[generation]).replace("\0", ".")+new String(new char[3-generation]).replace("\0", " "));
					    			table.setPlaceholder(ph);
					    		}
					    		
					    	}
					    	
					    }
					});
					try {
						TimeUnit.MILLISECONDS.sleep(600);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
			}
		};

		jfxTask.setOnSucceeded(event -> {
			
		});
		jfxTask.setOnFailed(event -> {
			Throwable problem = jfxTask.getException();
		    problem.printStackTrace(System.err);
		});
		jfxTask.setOnCancelled(event -> {
			stop=true;
		});
		
		Thread task = new Thread(jfxTask);; task.setDaemon(true);
		task.start();
		this.task=task;
	}





	public void stop() {
		this.stop=true;
		//this.task.interrupt();
	}

}

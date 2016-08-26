package MCEntityAnimator.distribution.job;

import java.util.PriorityQueue;
import java.util.Queue;

import MCEntityAnimator.gui.GuiHandler;

public class JobHandler 
{
	
	private Queue<Job> jobQueue;
	private JobThread jobThread;

	public JobHandler()
	{
		jobQueue = new PriorityQueue<Job>();
		jobThread = new JobThread();
		new Thread(jobThread).start();
	}
	
	public void queueJob(Job job)
	{
		//TODO remove these two lines once JobThread has been completed (allows debug changes...)
		jobThread = new JobThread();
		new Thread(jobThread).start();
		
		jobQueue.add(job);	
	}
	
	
	private class JobThread implements Runnable 
	{
		private boolean reset = false;

	    public void run() 
	    {
	        while(true)
	        {
	        	Job currentJob = jobQueue.poll();
	        	if(currentJob != null)
	        	{
	        		GuiHandler.mainGui.jobPanel.updateJobNumberLabel(jobQueue.size() + 1);
	        		GuiHandler.mainGui.jobPanel.updateCurrentJobLabel(currentJob.getName());
	        		currentJob.run();
	        		currentJob.completeJob();
	        		reset = false;
	        	}
	        	else if(!reset && GuiHandler.mainGui != null)
	        	{
	        		GuiHandler.mainGui.jobPanel.updateJobNumberLabel(0);
	        		GuiHandler.mainGui.jobPanel.updateCurrentJobLabel("-");
	        		GuiHandler.mainGui.jobPanel.updateCurrentJobStatusLabel("-");
	        		reset = true;
	        	}
	        	try 
	        	{
					Thread.sleep(500);
				} 
	        	catch (InterruptedException e) {e.printStackTrace();}
	        }
	    }
	}
	
}

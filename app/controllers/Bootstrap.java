package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Menu;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class Bootstrap extends Job {
	
	public void doJob() {
		
		Logger.info("start! init the params from applicatioin.conf");
        
	}

}

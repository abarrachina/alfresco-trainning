package com.ixxus.ipm.migration.users;

public class MigrateServiceFactory {

	public static final String COMMENTS = "comments";
	public static final String CONTENT = "content";
	public static final String GROUPS = "groups";
	public static final String PREFERENCES = "preferences";
	public static final String SITES = "sites";
	public static final String USERHOME = "userhome";
	public static final String WORKFLOWS = "workflows";
	
	
	public static MigrateService createMigrateService(String type){
		
		MigrateService migrateService=null;
		switch (type) {
			case COMMENTS:
				migrateService = new MigrateServiceComments();
				break;
			case CONTENT:
				migrateService = new MigrateServiceContent();
				break;
			case GROUPS:
				migrateService = new MigrateServiceGroups();
				break;			
			case PREFERENCES:
				migrateService = new MigrateServicePreferences();
				break;
			case SITES:
				migrateService = new MigrateServiceSites();		
				break;
			case USERHOME:
				migrateService = new MigrateServiceUserHome();
				break;
			case WORKFLOWS:
				migrateService = new MigrateServiceWorkflow();
				break;
			default:
				break;
		}
		
		return migrateService;
	}
}

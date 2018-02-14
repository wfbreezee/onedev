package com.turbodev.server.manager.impl;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.turbodev.launcher.loader.Listen;
import com.turbodev.utils.FileUtils;
import com.turbodev.utils.schedule.SchedulableTask;
import com.turbodev.utils.schedule.TaskScheduler;
import com.turbodev.server.event.lifecycle.SystemStarted;
import com.turbodev.server.event.lifecycle.SystemStopping;
import com.turbodev.server.manager.AttachmentManager;
import com.turbodev.server.manager.StorageManager;
import com.turbodev.server.model.CodeComment;
import com.turbodev.server.model.Project;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.persistence.annotation.Sessional;
import com.turbodev.server.persistence.annotation.Transactional;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.persistence.dao.EntityPersisted;
import com.turbodev.server.persistence.dao.EntityRemoved;
import com.turbodev.server.util.facade.ProjectFacade;

@Singleton
public class DefaultAttachmentManager implements AttachmentManager, SchedulableTask {

	private static final Logger logger = LoggerFactory.getLogger(DefaultAttachmentManager.class);
	
	private static final long TEMP_PRESERVE_PERIOD = 24*3600*1000L; 
	
	private final StorageManager storageManager;
	
	private final TaskScheduler taskScheduler;
	
    private final Dao dao;
    
    private String taskId;
    
	@Inject
	public DefaultAttachmentManager(Dao dao, StorageManager storageManager, TaskScheduler taskScheduler) {
		this.dao = dao;
		this.storageManager = storageManager;
		this.taskScheduler = taskScheduler;
	}
	
	@Override
	public File getAttachmentDir(ProjectFacade project, String attachmentDirUUID) {
		File projectAttachmentDir = storageManager.getProjectAttachmentDir(project.getId());
		File attachmentDir = getPermanentAttachmentDir(projectAttachmentDir, attachmentDirUUID);
		if (attachmentDir.exists())
			return attachmentDir;
		else
			return getTempAttachmentDir(projectAttachmentDir, attachmentDirUUID); 
	}

	private File getPermanentAttachmentDir(File projectAttachmentDir, String attachmentDirUUID) {
		String category = attachmentDirUUID.substring(0, 2);
		return new File(projectAttachmentDir, "permanent/" + category + "/" + attachmentDirUUID);
	}
	
	private File getTempAttachmentDir(File projectAttachmentDir) {
		return new File(projectAttachmentDir, "temp");
	}
	
	private File getTempAttachmentDir(File projectAttachmentDir, String attachmentDirUUID) {
		return new File(getTempAttachmentDir(projectAttachmentDir), attachmentDirUUID);
	}

	private void makeAttachmentPermanent(File projectAttachmentDir, String attachmentDirUUID) {
		File tempAttachmentDir = getTempAttachmentDir(projectAttachmentDir, attachmentDirUUID);
		File permanentAttachmentDir = getPermanentAttachmentDir(projectAttachmentDir, attachmentDirUUID);
		if (tempAttachmentDir.exists() && !permanentAttachmentDir.exists()) {
			try {
				FileUtils.moveDirectory(tempAttachmentDir, permanentAttachmentDir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Listen
	public void on(SystemStarted event) {
		taskId = taskScheduler.schedule(this);
	}

	@Listen
	public void on(SystemStopping event) {
		taskScheduler.unschedule(taskId);
	}

	@Sessional
	@Override
	public void execute() {
		try {
			for (Project project: dao.findAll(Project.class)) {
				File tempDir = getTempAttachmentDir(storageManager.getProjectAttachmentDir(project.getId()));
				if (tempDir.exists()) {
					for (File file: tempDir.listFiles()) {
						if (System.currentTimeMillis() - file.lastModified() > TEMP_PRESERVE_PERIOD) {
							FileUtils.deleteDir(file);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error cleaning up temp attachments", e);
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(0, 0);
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.isNew()) {
			File projectAttachmentDir;
			String attachmentUUID;
			if (event.getEntity() instanceof PullRequest) {
				PullRequest request = (PullRequest) event.getEntity();
				projectAttachmentDir = storageManager.getProjectAttachmentDir(request.getTargetProject().getId());
				attachmentUUID = request.getUUID();
			} else if (event.getEntity() instanceof CodeComment) {
				CodeComment comment = (CodeComment) event.getEntity();
				projectAttachmentDir = storageManager.getProjectAttachmentDir(comment.getProject().getId());
				attachmentUUID = comment.getUUID();
			} else {
				projectAttachmentDir = null;
				attachmentUUID = null;
			}
			if (projectAttachmentDir != null && attachmentUUID != null) {
				dao.doAfterCommit(new Runnable() {

					@Override
					public void run() {
						makeAttachmentPermanent(projectAttachmentDir, attachmentUUID);
					}
					
				});
			}
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		File dirToDelete;
		if (event.getEntity() instanceof Project) {
			Project project = (Project) event.getEntity();
			dirToDelete = storageManager.getProjectAttachmentDir(project.getId());
		} else if (event.getEntity() instanceof CodeComment) {
			CodeComment comment = (CodeComment) event.getEntity();
			dirToDelete = getPermanentAttachmentDir(
					storageManager.getProjectAttachmentDir(comment.getProject().getId()), comment.getUUID());
		} else {
			dirToDelete = null;
		}
		if (dirToDelete != null) {
			dao.doAfterCommit(new Runnable() {

				@Override
				public void run() {
					if (dirToDelete.exists())
						FileUtils.deleteDir(dirToDelete);
				}
				
			});
		}
	}

}
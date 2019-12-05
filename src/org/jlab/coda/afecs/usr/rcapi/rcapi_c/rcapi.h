/*
 * rcapi.h
 *
 *  Created on: Nov 12, 2012
 *      Author: gurjyan
 */

#ifndef PLCMD_H_
#define PLCMD_H_

#include <stdint.h>

#define RCG_DEBUG_RECV (1<<0)
#define RCG_DEBUG_SEND (1<<1)
#define RCG_DEBUG_WAIT (1<<2)

int pl_connect(const char *pLHost, const char *eXPid);
int pl_disconnect();
const char **getPlatformRegisteredRunTypes();
const char **getPlatformRegisteredSessions();
const char **getPlatformRegisteredAgents();
const char **getDbRegisteredRunTypes();
const char **getDbRegisteredSessions();
const char **getPlatformRegisteredDocs();
int64_t getSupervisorRunNumber(const char *runType);
const char *getSupervisorState(const char *runType);
const char **getSupervisorComponentsStates(const char *runType);
const char *getControlStatus(const char *runType);
const char *getSupervisorSession(const char *runType);
const char *getSupervisorRunStartTime(const char *runType);
const char *getSupervisorRunEndTime(const char *runType);
const char *getActiveRunType(const char *session);
const char *getActiveRunState(const char *session);
int64_t getComponentEventNumber(const char *runType, const char *compName);
const char **getComponentOutputFiles(const char *runType, const char *compName);
float getComponentEventRate(const char *runType, const char *compName);
double getComponentDataRate(const char *runType, const char *compName);
const char *getComponentState(const char *runType, const char *compName);
float getComponentLiveTime(const char *runType, const char *compName);
int rcGuiMessage(const char *session, const char *runType, const char *author, const char *message, int severity);

int rcgConfigure(const char *session, const char *runType);
int rcgReleaseAgents(const char *runType);
int rcgDownload(const char *runType);
int rcgPrestart(const char *runType);
int rcgGo(const char *runType);
int rcgEnd(const char *runType);
int rcgStartRun(const char *runType);
int rcgReset(const char *runType);
int rcgProgramScheduler(const char *runType, int numberOfRuns,
			 int eventLimit, int timeLimit);
int rcgResetScheduler(const char *runType);
int rcgDisableScheduler(const char *runType);

void rcgSetDebugMask(uint8_t inmask);

#endif

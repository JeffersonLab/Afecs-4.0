/*
 * rcapi.c
 *
 *  Created on: Nov 13, 2012
 *      Author: gurjyan
 */

#include <stdio.h>
#include <string.h>
#include <time.h>
#include <unistd.h>
#include "rcapi.h"
#include "cMsg.h"
#include "cMsgConstants.h"

static void *domainId;
static const char *myDescription = "afecs client";
static const char *myExPid;
static char cMsgStringResult[80];
static char cMsgStringArrayResult[80][80];
struct timespec timeout;
static uint8_t debugMask = 0;

static int sendAndGetCmsg(const char *func,
			  const char *subject, const char *type,
			  const char *text, void *stringPayloadName,
			  void *stringPayload, int payloadSize,
			  void *replyMsg, struct timespec to);
static int sendCmsg(const char *func, const char *subject, const char *type,
		    const char *text);
static int waitUntilItsDone(const char *runType, const char *response,
			    int tout);
static int waitUntilItsDone2(const char *runType, const char *response,
			     const char *response2, int tout);
static void debugCmsg(const char *type, void *msg);

int TIMEOUT = 20;

int
pl_connect(const char *pLHost, const char *eXPid)
{
  char udl[100];
  char myName[50];
  int err, rval, nameId = 0;

  nameId = (int)time(NULL);

  sprintf(udl, "cMsg://%s/cMsg/%s?regime=low&cmsgpassword=%s",
	  pLHost, eXPid, eXPid);
  sprintf(myName, "Rcg-%d", nameId);

  /* connect to cMsg server */
  err = cMsgConnect(udl, myName, myDescription, &domainId);
  if (err != CMSG_OK)
    {
      printf("%s: cMsgConnect: %s\n", __func__, cMsgPerror(err));
      return -1;
    }

  /* start receiving messages */
  cMsgReceiveStart(domainId);

  myExPid = eXPid;

  return rval;
}

/*--------------------------------------------------------*/

int
pl_disconnect()
{
  int err;

  err = cMsgDisconnect(&domainId);
  if (err != CMSG_OK)
    {
      printf("%s: cMsgDisconnect: %s\n", __func__, cMsgPerror(err));
    }
  return err;
}

/*--------------------------------------------------------*/

const char **
getPlatformRegisteredRunTypes()
{
  void *replyMsg = NULL;
  int err, len, istring;
  const char **result;

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err = sendAndGetCmsg(__func__,
		       "ControlDesigner",
		       "designer/info/request/getConfigFileNames",
		       NULL, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetStringArray(replyMsg, "configFileNames", &result, &len);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr,
		  "%s: Error: payload = configFileNames does not exist.\n",
		  __func__);
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	}
      else
	for (istring = 0; istring < len; istring++)
	  strncpy(cMsgStringArrayResult[istring], result[istring],
		  80 * sizeof(char));
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return (const char **) cMsgStringArrayResult;
}

/*--------------------------------------------------------*/

const char **
getPlatformRegisteredSessions()
{
  void *replyMsg = NULL;
  int err, len, istring;
  const char **result;

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err = sendAndGetCmsg(__func__,
		       myExPid,
		       "platform/info/request/sessions/pl",
		       NULL, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetStringArray(replyMsg, "sessions_p", &result, &len);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = sessions_p does not exist.\n",
		 __func__);
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	}
      else
	for (istring = 0; istring < len; istring++)
	  strncpy(cMsgStringArrayResult[istring], result[istring],
		  80 * sizeof(char));

    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return (const char **) cMsgStringArrayResult;
}

/*--------------------------------------------------------*/

const char **
getPlatformRegisteredAgents()
{
  void *replyMsg = NULL;
  int err, len, istring;
  const char **result;

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err = sendAndGetCmsg(__func__,
		       myExPid,
		       "platform/info/request/agents/pl",
		       NULL, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetStringArray(replyMsg, "agents_p", &result, &len);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = agents_p does not exist.\n", __func__);
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	}
      else
	for (istring = 0; istring < len; istring++)
	  strncpy(cMsgStringArrayResult[istring], result[istring],
		  80 * sizeof(char));
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return (const char **) cMsgStringArrayResult;
}

/*--------------------------------------------------------*/

const char **
getDbRegisteredRunTypes()
{
  void *replyMsg = NULL;
  int err, len, istring;
  const char **result;

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err = sendAndGetCmsg(__func__,
		       myExPid,
		       "platform/info/request/dbRuntypes/pl",
		       NULL, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetStringArray(replyMsg, "dbruntypes_p", &result, &len);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = dbruntypes_p does not exist.\n",
		 __func__);
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	}
      else
	for (istring = 0; istring < len; istring++)
	  strncpy(cMsgStringArrayResult[istring], result[istring],
		  80 * sizeof(char));
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return (const char **) cMsgStringArrayResult;
}

/*--------------------------------------------------------*/

const char **
getDbRegisteredSessions()
{
  void *replyMsg = NULL;
  int err, len, istring;
  const char **result;

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err = sendAndGetCmsg(__func__,
		       myExPid,
		       "platform/info/request/dbSessions/pl",
		       NULL, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetStringArray(replyMsg, "dbsessions_p", &result, &len);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, 
		  "%s: Error: payload = dbsessions_p does not exist.\n",
		  __func__);
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	}
      else
	for (istring = 0; istring < len; istring++)
	  strncpy(cMsgStringArrayResult[istring], result[istring],
		  80 * sizeof(char));
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return (const char **) cMsgStringArrayResult;
}

/*--------------------------------------------------------*/

const char **
getPlatformRegisteredDocs()
{
  void *replyMsg = NULL;
  int err, len, istring;
  const char **result;

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err =
    sendAndGetCmsg(__func__,
		   myExPid,
		   "platform/info/request/docs/pl",
		   NULL, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetStringArray(replyMsg, "docs_p", &result, &len);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = docs_p does not exist.\n", __func__);
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	}
      else
	for (istring = 0; istring < len; istring++)
	  strncpy(cMsgStringArrayResult[istring], result[istring],
		  80 * sizeof(char));
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return (const char **) cMsgStringArrayResult;
}

/*--------------------------------------------------------*/

int64_t
getSupervisorRunNumber(const char *runType)
{
  void *replyMsg = NULL;
  char supName[50];
  int err;
  int32_t result = -1;

  sprintf(supName, "sms_%s", runType);

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err = sendAndGetCmsg(__func__,
		       supName,
		       "supervisor/user/request/runNumber/pl",
		       NULL, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetInt32(replyMsg, "runnumber_p", &result);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = runnumber_p does not exist.\n",
		 __func__);
	  result = -1;
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	  result = -1;
	}
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return result;
}

/*--------------------------------------------------------*/


const char *
getSupervisorState(const char *runType)
{
  void *replyMsg = NULL;
  char supName[50];
  int err;
  const char *result = NULL;

  sprintf(supName, "sms_%s", runType);

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err = sendAndGetCmsg(__func__,
		       supName,
		       "supervisor/user/request/runState/pl",
		       NULL, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetString(replyMsg, "runstate_p", &result);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = runstate_p does not exist.\n",
		 __func__);
	  strncpy(cMsgStringResult, "", 80 * sizeof(char));
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	  rcgSetDebugMask(0xf);
	  strncpy(cMsgStringResult, "", 80 * sizeof(char));
	}
      else
	strncpy(cMsgStringResult, result, 80 * sizeof(char));
    }

  debugCmsg("recv", replyMsg);

  rcgSetDebugMask(0);
  
  if (replyMsg)
    cMsgFreeMessage(&replyMsg);
  
  return cMsgStringResult;
}

/*--------------------------------------------------------*/

const char **
getSupervisorComponentsStates(const char *runType)
{
  void *replyMsg = NULL;
  char supName[50];
  int err, len, istring;
  const char **result;

  sprintf(supName, "sms_%s", runType);

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err =
    sendAndGetCmsg(__func__,
		   supName,
		   "supervisor/user/request/componentStates/pl",
		   NULL, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetStringArray(replyMsg, "states_p", &result, &len);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = states_p does not exist.\n", __func__);
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	}
      else
	for (istring = 0; istring < len; istring++)
	  strncpy(cMsgStringArrayResult[istring], result[istring],
		  80 * sizeof(char));
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return (const char **) cMsgStringArrayResult;
}

/*--------------------------------------------------------*/

const char *
getControlStatus(const char *runType)
{
  void *replyMsg = NULL;
  char supName[50];
  int err;
  const char *result;

  sprintf(supName, "sms_%s", runType);

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err =
    sendAndGetCmsg(__func__,
		   supName,
		   "supervisor/user/request/schedulerStatus/pl",
		   NULL, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetString(replyMsg, "scedulerstatus_p", &result);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = scedulerstatus_p does not exist.\n",
		 __func__);
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	}
      else
	strncpy(cMsgStringResult, result, 80 * sizeof(char));
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return cMsgStringResult;
}

/*--------------------------------------------------------*/

const char *
getSupervisorSession(const char *runType)
{
  void *replyMsg = NULL;
  char supName[50];
  int err;
  const char *result;

  sprintf(supName, "sms_%s", runType);

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err =
    sendAndGetCmsg(__func__,
		   supName,
		   "supervisor/user/request/runSession/pl",
		   NULL, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetString(replyMsg, "supsession_p", &result);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = supsession_p does not exist.\n",
		 __func__);
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	}
      else
	strncpy(cMsgStringResult, result, 80 * sizeof(char));
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return cMsgStringResult;
}

/*--------------------------------------------------------*/

const char *
getSupervisorRunStartTime(const char *runType)
{
  void *replyMsg = NULL;
  char supName[50];
  int err;
  const char *result = NULL;

  sprintf(supName, "sms_%s", runType);

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err =
    sendAndGetCmsg(__func__,
		   supName,
		   "supervisor/user/request/runStartTime/pl",
		   NULL, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetString(replyMsg, "supstarttime_p", &result);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = supstarttime_p does not exist.\n",
		 __func__);
	  result = NULL;
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	  result = NULL;
	}
      else
	strncpy(cMsgStringResult, result, 80 * sizeof(char));
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return cMsgStringResult;
}

/*--------------------------------------------------------*/

const char *
getSupervisorRunEndTime(const char *runType)
{
  void *replyMsg = NULL;
  char supName[50];
  int err;
  const char *result = NULL;

  sprintf(supName, "sms_%s", runType);

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err =
    sendAndGetCmsg(__func__,
		   supName,
		   "supervisor/user/request/runEndTime/pl",
		   NULL, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetString(replyMsg, "supendtime_p", &result);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = supendtime_p does not exist.\n",
		 __func__);
	  result = NULL;
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	  result = NULL;
	}
      else
	strncpy(cMsgStringResult, result, 80 * sizeof(char));
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);


  return cMsgStringResult;
}

/*--------------------------------------------------------*/

const char *
getActiveRunType(const char *session)
{
  void *replyMsg = NULL;
  int err;
  const char *result;

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err =
    sendAndGetCmsg(__func__,
		   myExPid,
		   "platform/info/request/activeRunType",
		   session, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetString(replyMsg, "activeruntype_p", &result);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = activeruntype_p does not exist.\n",
		 __func__);
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	}
      else
	strncpy(cMsgStringResult, result, 80 * sizeof(char));
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return cMsgStringResult;
}

/*--------------------------------------------------------*/

int64_t
getComponentEventNumber(const char *runType, const char *compName)
{
  void *replyMsg = NULL;
  char supName[50];
  int err;
  int64_t result = -1;

  sprintf(supName, "sms_%s", runType);

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err =
    sendAndGetCmsg(__func__,
		   supName,
		   "supervisor/user/request/componentEventNumber/pl",
		   compName, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetInt64(replyMsg, "evtnumber_p", &result);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = evtnumber_p does not exist.\n",
		 __func__);
	  result = -1;
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	  result = -1;
	}
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return result;
}

/*--------------------------------------------------------*/

const char *
getComponentOutputFile(const char *runType, const char *compName)
{
  void *replyMsg = NULL;
  char supName[50];
  int err;
  const char *result = NULL;

  sprintf(supName, "sms_%s", runType);

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err =
    sendAndGetCmsg(__func__,
		   supName,
		   "supervisor/user/request/componentOutputFile/pl",
		   compName, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetString(replyMsg, "outputfile_p", &result);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = outputfile_p does not exist.\n",
		 __func__);
	  result = NULL;
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	  result = NULL;
	}
      else
	strncpy(cMsgStringResult, result, 80 * sizeof(char));
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return cMsgStringResult;
}

/*--------------------------------------------------------*/

float
getComponentEventRate(const char *runType, const char *compName)
{
  void *replyMsg = NULL;
  char supName[50];
  int err;
  float result;

  sprintf(supName, "sms_%s", runType);

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err =
    sendAndGetCmsg(__func__,
		   supName,
		   "supervisor/user/request/componentEventRate/pl",
		   compName, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetFloat(replyMsg, "compevtrate_p", &result);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = compevtrate_p does not exist.\n",
		 __func__);
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	}
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return result;
}

/*--------------------------------------------------------*/

double
getComponentDataRate(const char *runType, const char *compName)
{
  void *replyMsg = NULL;
  char supName[50];
  int err;
  double result;

  sprintf(supName, "sms_%s", runType);

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err =
    sendAndGetCmsg(__func__,
		   supName,
		   "supervisor/user/request/componentDataRate/pl",
		   compName, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetDouble(replyMsg, "compdatarate_p", &result);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = compdatarate_p does not exist.\n",
		 __func__);
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	}
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return result;
}

/*--------------------------------------------------------*/

float
getComponentLiveTime(const char *runType, const char *compName)
{
  void *replyMsg = NULL;
  char supName[50];
  int err;
  float result;

  sprintf(supName, "sms_%s", runType);

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err =
    sendAndGetCmsg(__func__,
		   supName,
		   "supervisor/user/request/componentLiveTime/pl",
		   compName, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetFloat(replyMsg, "complivetime_p", &result);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = complivetime_p does not exist.\n",
		 __func__);
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));
	}
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return result;
}

/*--------------------------------------------------------*/

const char *
getComponentState(const char *runType, const char *compName)
{
  void *replyMsg = NULL;
  char supName[50];
  int err;
  const char *result = NULL;

  sprintf(supName, "sms_%s", runType);

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  err =
    sendAndGetCmsg(__func__,
		   supName,
		   "supervisor/user/request/componentState/pl",
		   compName, NULL, NULL, 0, &replyMsg, timeout);

  if (err == CMSG_OK)
    {
      /* get the interested payload */
      err = cMsgGetString(replyMsg, "compstate_p", &result);
      if (err == CMSG_ERROR)
	{
	  fprintf(stderr, "%s: Error: payload = compstate_p does not exist.\n",
		 __func__);

	  result = NULL;
	}
      else if (err != CMSG_OK)
	{
	  fprintf(stderr, "%s: Error: %s\n", __func__, cMsgPerror(err));

	  rcgSetDebugMask(0xf);

	  result = NULL;
	}
      else
	strncpy(cMsgStringResult, result, 80 * sizeof(char));

    }


  debugCmsg("recv", replyMsg);

  rcgSetDebugMask(0);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return cMsgStringResult;
}

/*--------------------------------------------------------*/

int
rcgConfigure(const char *session, const char *runType)
{
  void *replyMsg = NULL;
  int err, rval = 0;

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  const char *stat = getSupervisorState(runType);
  if ((strcmp(stat, "undefined") == 0) || (strcmp(stat, "booted") == 0)
      || (strcmp(stat, "configured") == 0) || (strcmp(stat, "") == 0))
    {
      const char stringName[2][32] = { "session", "config" };
      char payload[2][32];

      strncpy(payload[0], session, 32);
      strncpy(payload[1], runType, 32);

      err =
	sendAndGetCmsg(__func__,
		       "ControlDesigner",
		       "designer/control/request/configure/control_rcapi",
		       NULL,
		       (void *) stringName,
		       (void *) payload, 2, &replyMsg, timeout);

      if (err == CMSG_OK)
	{
	  waitUntilItsDone(runType, "configured", 10);
	}
    }
  else
    {
      printf("Forbidden transition (%s -> Configure).\n", stat);
      rval = -1;
    }

  debugCmsg("recv", replyMsg);

  if (replyMsg)
    cMsgFreeMessage(&replyMsg);

  return rval;
}

/*--------------------------------------------------------*/

int
rcgReleaseAgents(const char *runType)
{
  int rval = 0;
  char supName[50];

  sprintf(supName, "sms_%s", runType);

  rval = sendCmsg(__func__,
		  supName,
		  "agent/control/request/supervisor/releaseAgents", NULL);

  return rval;
}

/*--------------------------------------------------------*/

int
rcgDownload(const char *runType)
{
  int err, rval = 0;
  char supName[50];

  timeout.tv_sec = 3;
  timeout.tv_nsec = 0;

  const char *stat = getSupervisorState(runType);
  if ((strcmp(stat, "downloaded") == 0) || (strcmp(stat, "configured") == 0))
    {
      sprintf(supName, "sms_%s", runType);

      err = sendCmsg(__func__,
		     supName,
		     "supervisor/control/request/startService",
		     "CodaRcDownload");

      if (err == CMSG_OK)
	{
	  /*wait for supervisor specific state */
	  err = waitUntilItsDone(runType, "downloaded", TIMEOUT);
	  if (err != 0)
	    {
	      printf("Transition failed\n");
	      rval = -1;
	    }
	}
      else
	rval = -1;
    }
  else
    {
      printf("Forbidden transition (%s -> Download).\n", stat);
      rval = -1;
    }

  return rval;
}

/*--------------------------------------------------------*/

int
rcgPrestart(const char *runType)
{
  int err, rval = 0;
  char supName[50];

  sprintf(supName, "sms_%s", runType);

  const char *stat = getSupervisorState(runType);
  if ((strcmp(stat, "downloaded") == 0) || (strcmp(stat, "ended") == 0))
    {

      err = sendCmsg(__func__,
		     supName,
		     "supervisor/control/request/startService",
		     "CodaRcPrestart");

      if (err == CMSG_OK)
	{
	  /*wait for supervisor specific state */
	  err = waitUntilItsDone(runType, "prestarted", TIMEOUT);
	  if (err != 0)
	    {
	      printf("Transition failed\n");
	      rval = -1;
	    }
	}
      else
	rval = -1;
    }
  else
    {
      printf("Forbidden transition (%s -> Prestart).\n", stat);
      rval = -1;
    }

  return rval;
}

/*--------------------------------------------------------*/

int
rcgGo(const char *runType)
{
  int err, rval = 0;;
  char supName[50];

  sprintf(supName, "sms_%s", runType);

  const char *stat = getSupervisorState(runType);
  if ((strcmp(stat, "prestarted") == 0))
    {

      err = sendCmsg(__func__,
		     supName,
		     "supervisor/control/request/startService", "CodaRcGo");

      if (err == CMSG_OK)
	{
	  /*wait for supervisor specific state */
	  err = waitUntilItsDone(runType, "active", TIMEOUT);
	  if (err != 0)
	    {
	      printf("Transition failed\n");
	      rval = -1;
	    }
	}
      else
	rval = -1;
    }
  else
    {
      printf("Forbidden transition (%s -> Go).\n", stat);
      rval = -1;
    }

  return rval;
}

/*--------------------------------------------------------*/

int
rcgEnd(const char *runType)
{
  int err, rval = 0;
  char supName[50];

  sprintf(supName, "sms_%s", runType);

  const char *stat = getSupervisorState(runType);
  if ((strcmp(stat, "downloaded") == 0) || (strcmp(stat, "prestarted") == 0)
      || (strcmp(stat, "active") == 0))
    {

      err = sendCmsg(__func__,
		     supName,
		     "supervisor/control/request/startService", "CodaRcEnd");

      if (err == CMSG_OK)
	{
	  /*wait for supervisor specific state */
	  err = waitUntilItsDone(runType, "ended", TIMEOUT);
	  if (err != 0)
	    {
	      printf("Transition failed\n");
	      rval = -1;
	    }
	}
      else
	rval = -1;
    }
  else
    {
      printf("Forbidden transition (%s -> End).\n", stat);
      rval = -1;
    }

  return rval;
}

/*--------------------------------------------------------*/

int
rcgStartRun(const char *runType)
{
  int err = 0, err2 = 0, rval = 0;
  char supName[50];

  sprintf(supName, "sms_%s", runType);

  err = sendCmsg(__func__,
		 supName, "supervisor/control/request/enableAutoMode", NULL);

  err2 = sendCmsg(__func__,
		  supName,
		  "supervisor/control/request/startService",
		  "CodaRcStartRun");

  if ((err == CMSG_OK) && (err2 == CMSG_OK))
    {
      /*wait for supervisor specific state */
      err = waitUntilItsDone(runType, "active", TIMEOUT);
      if (err != 0)
	{
	  printf("Transition failed\n");
	  rval = -1;
	}
    }
  else
    rval = -1;

  return rval;
}

/*--------------------------------------------------------*/

int
rcgReset(const char *runType)
{
  int err, rval = 0;
  char supName[50];

  sprintf(supName, "sms_%s", runType);

  err = sendCmsg(__func__,
		 supName, "agent/control/request/moveToState", "reseted");

  if (err == CMSG_OK)
    {
      /*wait for supervisor specific state */
      err = waitUntilItsDone2(runType, "booted", "configured", TIMEOUT);
      if (err != 0)
	{
	  printf("Transition failed\n");
	  rval = -1;
	}
    }
  else
    rval = -1;

  return rval;
}

/*--------------------------------------------------------*/

int
rcgProgramScheduler(const char *runType,
		    int numberOfRuns, int eventLimit, int timeLimit)
{
  int err, err2, err3, rval = 0;
  char supName[50];
  char numR[15];
  char evtL[15];
  char timeL[15];

  sprintf(numR, "%d", numberOfRuns);
  sprintf(evtL, "%d", eventLimit);
  sprintf(timeL, "%d", eventLimit);

  sprintf(supName, "sms_%s", runType);

  err = sendCmsg(__func__,
		 supName, "supervisor/control/request/setNumberOfRuns", numR);

  err2 = sendCmsg(__func__,
		  supName, "supervisor/control/request/setEventLimit", evtL);

  err3 = sendCmsg(__func__,
		  supName, "supervisor/control/request/setTimeLimit", timeL);

  if (!((err == CMSG_OK) || (err2 == CMSG_OK) || (err3 == CMSG_OK)))
    rval = -1;

  return rval;
}

/*--------------------------------------------------------*/

int
rcgResetScheduler(const char *runType)
{
  return rcgProgramScheduler(runType, 0, 0, 0);
}

/*--------------------------------------------------------*/

int
rcgDisableScheduler(const char *runType)
{
  int err;
  char supName[50];

  sprintf(supName, "sms_%s", runType);

  err = sendCmsg(__func__,
		 supName, "supervisor/control/request/disableAutoMode", "0");

  return err;
}

/*--------------------------------------------------------*/

static int
sendCmsg(const char *func,
	 const char *subject, const char *type, const char *text)
{
  void *msg;
  int err, rval = 0;

  /* create message to be sent */
  msg = cMsgCreateMessage();

  cMsgSetSubject(msg, subject);

  cMsgSetType(msg, type);

  if (text != NULL)
    cMsgSetText(msg, text);

  debugCmsg("send", msg);
  
  /* send msg */
  err = cMsgSend(domainId, msg);
  if (err != CMSG_OK)
    {
      fprintf(stderr, "%s: Error: %s\n", func, cMsgPerror(err));
      rval = -1;
    }

  if (msg)
    cMsgFreeMessage(&msg);

  return rval;
}

static int
sendAndGetCmsg(const char *func,
	       const char *subject, const char *type, const char *text,
	       void *stringPayloadName, void *stringPayload, int payloadSize,
	       void *replyMsg, struct timespec to)
{
  void *msg;
  int err, rval = 0, istr = 0;
  char (*tmpStringName)[32] = stringPayloadName;
  char (*tmpString)[32] = stringPayload;

  /* create message to be sent */
  msg = cMsgCreateMessage();
  cMsgSetSubject(msg, subject);
  cMsgSetType(msg, type);
  if (text != NULL)
    cMsgSetText(msg, text);

  if (stringPayload != NULL)
    {
      for (istr = 0; istr < payloadSize; istr++)
	{
	  cMsgAddString(msg, tmpStringName[istr], tmpString[istr]);
	}
    }

  debugCmsg("send", msg);

  err = cMsgSendAndGet(domainId, msg, &timeout, replyMsg);
  if (err == CMSG_TIMEOUT)
    {
      fprintf(stderr, "%s(%s): Error: Timeout\n", __func__, func);
      rval = -1;
    }
  else if (err != CMSG_OK)
    {
      fprintf(stderr, "%s(%s): Error: %s\n", __func__, func, cMsgPerror(err));
      rval = -1;
    }

  if (msg)
    cMsgFreeMessage(&msg);

  return rval;
}

static int
waitUntilItsDone(const char *runType, const char *response, int tout)
{
  const char *stat;
  int i;

  for (i = 0; i < tout; i++)
    {
      stat = getSupervisorState(runType);
      if ((strcmp(stat, response) == 0))
	{
	  return 0;
	}
      if ((strcmp(stat, "") == 0))
	{
	  return 1;
	}
      sleep(1);
      if(debugMask & RCG_DEBUG_WAIT)
	{
	  printf("%s(%s,%s) Not Done yet... (%s)\n",
		 __func__, runType, response, stat);
	}
    }
  return 1;
}

static int
waitUntilItsDone2(const char *runType, const char *response,
		  const char *response2, int tout)
{
  const char *stat;
  int i;

  for (i = 0; i < tout; i++)
    {
      stat = getSupervisorState(runType);
      if ((strcmp(stat, response) == 0) || (strcmp(stat, response2) == 0))
	{
	  return 0;
	}
      if ((strcmp(stat, "") == 0))
	{
	  return 1;
	}

      sleep(1);
      if(debugMask & RCG_DEBUG_WAIT)
	{
	  printf("%s(%s,%s || %s) Not Done yet... (%s)\n",
		 __func__, runType, response, response2, stat);
	}
    }
  return 1;
}

void
rcgSetDebugMask(uint8_t inmask)
{
  debugMask = inmask;
}

static void
debugCmsg(const char *type, void *msg)
{
  const char *cresult = NULL;

  if (((strcmp(type, "recv") == 0) && (debugMask & RCG_DEBUG_RECV)) ||
      ((strcmp(type, "send") == 0) && (debugMask & RCG_DEBUG_SEND)))
    {
      if (msg)
	{
	  cMsgToString(msg, (char **) &cresult);
	  printf("%s: %s = \n %s\n", __func__, type, cresult);
	}
      else
	{
	  printf("%s: No valid 'msg'\n",
		 __func__);
	}
    }
}

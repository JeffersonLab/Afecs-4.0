/*
 * accc.c
 *
 *  Created on: Nov 30, 2010
 *      Author: gurjyan
 */
/* include files follow here */
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <sys/resource.h>
#include <sys/time.h>
#include <signal.h>
#include <time.h>
#include <netinet/in.h>
#include <pthread.h>
#include <unistd.h>
#include <dlfcn.h>

#include "accc.h"
#include "cMsg.h"
#include "cMsgConstants.h"

/* prototypes and variables */
static char* myName = "undefined";
static char* myExpid = "undefined";
static char* myState = "booted";
static char* myDescription = "Slow Control component";
static char* myPluginName = "undefined";
static char** myChannelNames;
static int* myChannelTypes;
static int myChannelNumber;

static void* plugin;
static void* domainId;
static int sendStatistics = 1; // controls the monitoring thread

char* infoString = "undefined";
char* warningString = "undefined";
char* errorString = "undefined";

void aClient_thread(void);
int aClient(char *name, char *udl);

static void callbackTransition(void *msg, void *arg);
static void callbackControl(void *msg, void *arg);
static void callbackInfo(void *msg, void *arg);
void usage();
void daLogMsg(char *severity, char *text);
int main(int argc, char **argv);

// -------------------------------------------------
// function main
// -------------------------------------------------
int main(int argc, char **argv) {

	char* result;
	int i = 1;
	pthread_t thread1;
	pthread_attr_t aClient_attr;
	int status = 0;

	/* parse command line arguments */
	if (argc > 1) {
		for (i = 1; i < argc; i++) {
			if (strcmp("-n", argv[i]) == 0) {
				myName = argv[++i];
			} else if (strcmp("-m", argv[i]) == 0) {
				myPluginName = argv[++i];
			} else if (strcmp("-e", argv[i]) == 0) {
				myExpid = argv[++i];
			} else if (strcmp("-h", argv[i]) == 0) {
				usage();
				return (-1);
			} else {
				usage();
				return (-1);
			}
		}
	}

	if ((strcmp(myName, "undefined") == 0)
			|| (strcmp(myPluginName, "undefined") == 0)) {
		usage();
		return (-1);
	}

	if ((strcmp(myExpid, "undefined") == 0)) {
		/* get expid defined as an environmental variable */
		myExpid = getenv("EXPID");

		if (myExpid == 0) {
			printf("Error: EXPID is not defined.\n");
			return (0);
		}
	}

	/* dynamic loading of the user plugin */
	plugin = dlopen(myPluginName, RTLD_NOW);
	if (!plugin) {
		printf("Cannot load %s: %s \n", myPluginName, dlerror());
		return (-1);
	}

	/* get client channel number */
	getChannelNumber = dlsym(plugin, "getChannelNumber");
	result = dlerror();
	if (result) {
		printf("Cannot find getChannelNumber in %s: %s\n", myPluginName, result);
		dlclose(plugin);
		return (0);
	}
	myChannelNumber = getChannelNumber();

	/* get client channel names */
	getChannelNames = dlsym(plugin, "getChannelNames");
	result = dlerror();
	if (result) {
		printf("Cannot find getChannelNames in %s: %s\n", myPluginName, result);
		dlclose(plugin);
		return (0);
	}
	myChannelNames = getChannelNames();

	/* get client channel data types */
	getChannelDataTypes = dlsym(plugin, "getChannelDataTypes");
	result = dlerror();
	if (result) {
		printf("Cannot find getChannelDataTypes in %s: %s\n", myPluginName,
				result);
		dlclose(plugin);
		return (0);
	}
	myChannelTypes = getChannelDataTypes();

	/* Start up the aClient thread for communication with CODA 3 runcontrol */
	pthread_attr_init(&aClient_attr);
	pthread_attr_setdetachstate(&aClient_attr, PTHREAD_CREATE_DETACHED);
	pthread_attr_setscope(&aClient_attr, PTHREAD_SCOPE_SYSTEM);

	status = pthread_create(&thread1, &aClient_attr,
			(void *(*)(void *)) aClient_thread, (void *) NULL);
	sleep(-1);
	return (status);
}

// -------------------------------------------------
// function prints program synopsis
// -------------------------------------------------
void usage() {
	printf("\nSynopsis:  ACodaCC [-n <name> | -m <plugin-name>| -h ] \n");
	printf("                  -h synopsis \n");
	printf("                  -n sets the client name\n");
	printf("                  -m sets the client shared library name\n");
	printf("                  -e sets an experiment ID: EXPID\n");
}
// -------------------------------------------------

// -------------------------------------------------
// function to send daLogMsg
// -------------------------------------------------
void daLogMsg(char *severity, char *text) {
	void *msg;
	int err, sevid = 0;

	/* translate severity into an integer */
	if (strcmp(severity, "INFO") == 0) {
		sevid = 1;
	} else if (strcmp(severity, "WARN") == 0) {
		sevid = 5;
	} else if (strcmp(severity, "ERROR") == 0) {
		sevid = 9;
	} else if (strcmp(severity, "SEVERE") == 0) {
		sevid = 13;
	}

	msg = cMsgCreateMessage();
	cMsgSetSubject(msg, myName);
	cMsgSetType(msg, "rc/report/dalog");
	cMsgSetReliableSend(msg, 1); // TCP
	cMsgSetUserInt(msg, sevid);
	cMsgSetText(msg, text);

	/* create compound payload */
	cMsgAddString(msg, "severity", severity);
	cMsgAddString(msg, "state", myState);
	//  cMsgAddInt32(msg,"codaid",codaObject->codaid);
	//  cMsgAddInt32(msg,"runType",codaObject->runType);
	cMsgAddString(msg, "codaClass", "USR");

	/* send message */
	err = cMsgSend(domainId, msg);
	printf("daLogMsg: %s: %s\n", severity, text);
	cMsgFreeMessage(&msg);
}
// -------------------------------------------------

// -------------------------------------------------
// function to start AClinet thread
// -------------------------------------------------
void aClient_thread(void) {
	int status;

	printf("INFO: Starting up AClient Thread...\n");
	status = aClient(myName, NULL);
	if (status < 0) {
		printf(
				"WARN: AClient thread not started - communication with AFECS control will fail.\n");
		pthread_exit((void *) -1);
	} else {
		pthread_exit(0);
	}
}
// -------------------------------------------------

// -------------------------------------------------
// function to start AClinet
// -------------------------------------------------
int aClient(char *name, char *udl) {

	char UDL[100];
	char *subject;
	char *type;
	int err, debug = 1;
	cMsgSubscribeConfig *config;
	void *unSubHandle;

	/* RC domain UDL is of the form:
	 *        cMsg:rc://host:<port>/expid?broadcastTO=<timeout>&connectTO=<timeout>
	 *
	 * Remember that for this domain:
	 * 1) port is optional with a default of (45200)
	 * 2) host is required - use "multicast" normally
	 *    but may be "localhost" or a hostname or in dotted decimal form
	 * 3) the experiment id or expid is required.
	 *
	 * 4) broadcastTO is the time to wait in seconds before connect returns a
	 *    timeout when a rc broadcast server does not answer
	 * 5) connectTO is the time to wait in seconds before connect returns a
	 *    timeout while waiting for the rc server to send a special (tcp)
	 *    concluding connect message
	 */

	if (name == NULL) {
		printf("AClient:ERROR: no name specified : aClient(name,UDL)\n");
		return (-1);
	}

	if (udl == NULL) {
		sprintf(UDL, "cMsg:rc://multicast/%s", myExpid);
		udl = (char *) &UDL[0];
	}

	if (debug) {
		printf("Running the AFECS client, \"%s\"\n", name);
		printf("  connecting to UDL, %s\n", udl);
	}

	/* subscribe to its name */
	subject = name;

	/* connect to cMsg server */
	err = cMsgConnect(udl, name, myDescription, &domainId);
	if (err != CMSG_OK) {
		if (debug) {
			printf("cMsgConnect: %s\n", cMsgPerror(err));
		}
		return (-1);
	}

	/* start receiving messages */
	cMsgReceiveStart(domainId);

	/* set the subscribe configuration */
	config = cMsgSubscribeConfigCreate();
	cMsgSubscribeSetMaxCueSize(config, 100);
	cMsgSubscribeSetSkipSize(config, 20);
	cMsgSubscribeSetMaySkip(config, 0);
	cMsgSubscribeSetMustSerialize(config, 1);
	cMsgSubscribeSetMaxThreads(config, 10);
	cMsgSubscribeSetMessagesPerThread(config, 150);
	cMsgSubscribeSetStackSize(config, 100000);
	cMsgSetDebugLevel(CMSG_DEBUG_ERROR);

	printf("Connect is completed!\n");

	/* subscribe to Run Transition messages */
	type = "run/transition/*";
	err = cMsgSubscribe(domainId, subject, type, callbackTransition, NULL,
			config, &unSubHandle);
	if (err != CMSG_OK) {
		printf("cMsgSubscribe: %s\n", cMsgPerror(err));
		return (-1);
	}
	printf("subscribing subject = %s type = %s \n", subject, type);

	/* subscribe to Session level Control messages*/
	type = "session/control/*";
	err = cMsgSubscribe(domainId, subject, type, callbackControl, NULL, config,
			&unSubHandle);
	if (err != CMSG_OK) {
		printf("cMsgSubscribe: %s\n", cMsgPerror(err));
		return (-1);
	}

	printf("subscribing subject = %s type = %s \n", subject, type);

	/* subscribe to CODA level Info messages */
	type = "coda/info/*";
	err = cMsgSubscribe(domainId, subject, type, callbackInfo, NULL, config,
			&unSubHandle);
	if (err != CMSG_OK) {
		printf("cMsgSubscribe: %s\n", cMsgPerror(err));
		return (-1);
	}

	printf("subscribing subject = %s type = %s \n", subject, type);

	return (0);
}
// -------------------------------------------------

// -------------------------------------------------
// Run transition messages callback function
// -------------------------------------------------
static void callbackTransition(void *mg, void *arg) {
	int stat;
/*
	int ii,plcount;
    char *pln[10];
    int  plt[10];
*/
    char *cfName = NULL;
    char *cfContent = NULL;
    char *config;
    int  configID, codaID;
	int isparameter;
	char *parameter;
	char *type;
	char *result;

	isparameter = cMsgGetText(mg, (const char **) &parameter);
	stat = cMsgGetType(mg, (const char **) &type);
	printf(">>>>>> AClient(Transition): Got the request to %s\n", type);
	/* Configure */
	if (strcmp(type, "run/transition/configure") == 0) {
		userConfigure = dlsym(plugin, "userConfigure");
		result = dlerror();
		if (result) {
			printf("Cannot find userConfigure in %s: %s\n", myPluginName,
					result);
			return;
		}

/*
    stat = cMsgPayloadGetCount(mg,&plcount);
    printf("Command %s  Payload count = %d\n",parameter,plcount);
    stat = cMsgPayloadGet(mg,pln,plt,10);
    for(ii=0;ii<plcount;ii++) {
      printf(" Payload Name,type = %s,%d\n",pln[ii],plt[ii]);
    }
*/
    stat = cMsgGetString(mg,"fileName",(const char **) &cfName);
    stat = cMsgGetString(mg,"fileContent",(const char **) &cfContent);
    stat = cMsgGetString(mg,"config",(const char **) &config);
    stat = cMsgGetInt32(mg,"configId", (int *) &configID);
    stat = cMsgGetInt32(mg,"codaId", (int *) &codaID);

/*printf("Debug fileName = %s, \n fileContent = %s \n",cfName,cfContent); */

		stat = userConfigure(cfContent);
		if (stat == 0) {
			myState = "configured";
		}

		/* Download */
	} else if (strcmp(type, "run/transition/download") == 0) {
		userDownload = dlsym(plugin, "userDownload");
		result = dlerror();
		if (result) {
			printf("Cannot find userDownload in %s: %s\n", myPluginName, result);
			return;
		}
		stat = userDownload(parameter);
		if (stat == 0) {
			myState = "downloaded";
		}

		/* Prestart */
	} else if (strcmp(type, "run/transition/prestart") == 0) {
		userPrestart = dlsym(plugin, "userPrestart");
		result = dlerror();
		if (result) {
			printf("Cannot find userPrestart in %s: %s\n", myPluginName, result);
			return;
		}
		stat = userPrestart(parameter);
		if (stat == 0) {
		  myState = "paused";
		} else {
		  myState = "ERROR";
		}

		/* GO */
	} else if (strcmp(type, "run/transition/go") == 0) {
		userGo = dlsym(plugin, "userGo");
		result = dlerror();
		if (result) {
			printf("Cannot find userGo in %s: %s\n", myPluginName, result);
			return;
		}
		stat = userGo(parameter);
		if (stat == 0) {
			myState = "active";
		}

		/* End */
	} else if (strcmp(type, "run/transition/end") == 0) {
		userEnd = dlsym(plugin, "userEnd");
		result = dlerror();
		if (result) {
			printf("Cannot find userEnd in %s: %s\n", myPluginName, result);
			return;
		}
		stat = userEnd(parameter);
		if (stat == 0) {
			myState = "downloaded";
		}

		/* Pause */
	} else if (strcmp(type, "run/transition/pause") == 0) {
		userPause = dlsym(plugin, "userPause");
		result = dlerror();
		if (result) {
			printf("Cannot find userpause in %s: %s\n", myPluginName, result);
			return;
		}
		stat = userPause(parameter);
		if (stat == 0) {
			myState = "paused";
		}

		/* Resume */
	} else if (strcmp(type, "run/transition/resume") == 0) {
		userResume = dlsym(plugin, "userResume");
		result = dlerror();
		if (result) {
			printf("Cannot find userResume in %s: %s\n", myPluginName, result);
			return;
		}
		stat = userResume(parameter);
		if (stat == 0) {
			myState = "active";
		}

		/* Reset */
	} else if (strcmp(type, "run/transition/reset") == 0) {
		userReset = dlsym(plugin, "userReset");
		result = dlerror();
		if (result) {
			printf("Cannot find userReset in %s: %s\n", myPluginName, result);
			return;
		}
		stat = userReset(parameter);
		if (stat == 0) {
			myState = "booted";
			sendStatistics = 0;
		}
	} else {
		printf("AClient(Transition): Don't understand the command %s\n", type);
	}
	printf("Current State: %s\n", myState);
}
// -------------------------------------------------

// -------------------------------------------------
// Info messages callback function
// -------------------------------------------------
static void callbackInfo(void *mg, void *arg) {
	int stat, sgr;
	int status;
	char *s;
	void *msg;
	char ss[100];

	stat = cMsgGetText(mg, (const char **) &s);
	printf(">>>>>> AClient(Info): Got the request to %s\n", s);

	/* Check if this is a send and get request */
	stat = cMsgGetGetRequest(mg, &sgr);

	if (sgr) { /* This is a Send & Get request so create reply message */
		msg = cMsgCreateResponseMessage(mg);
	} else { /* Create regular message */
		msg = cMsgCreateMessage();
		cMsgSetReliableSend(msg, 1); /* TCP */
	}
	cMsgSetSubject(msg, myName);
	sprintf(ss, "rc/response/%s", s);
	cMsgSetType(msg, ss);

	/* Object Type */
	if (strcmp(s, "getObjectType") == 0) {
		cMsgSetText(msg, "Coda3");

		/* Component State */
	} else if (strcmp(s, "getState") == 0) {
		cMsgSetText(msg, myState);

		/* CODA Class */
	} else if (strcmp(s, "getCodaClass") == 0) {
		cMsgSetText(msg, "USR");

		/* Component Status */
	} else if (strcmp(s, "getStatus") == 0) {
		cMsgSetText(msg, myState);
		cMsgAddString(msg, "codaName", myName);
		cMsgAddString(msg, "codaClass", "SLC");
		cMsgAddString(msg, "objectType", "Coda3");

	} else {
		printf("AClient(Info): Don't understand the command %s\n", s);
		cMsgSetText(msg, "Error_invalid_command");
	}

	/* Send the message */
	status = cMsgSend(domainId, msg);

	/* Free up any allocated messages or strings before exiting*/
	cMsgFreeMessage(&msg);
	cMsgFreeMessage(&mg);
}
// -------------------------------------------------

// -------------------------------------------------
// Session control messages callback function
// -------------------------------------------------
static void callbackControl(void *mg, void *arg) {
	int err, cuesize = 0;
	int freem = 0;
	struct timespec sleeep;
	int ui_sec = 2;
	int ui_nsec = 0;
	char *type;
	char *text;
	void *msg;
	static unsigned int rcCount;
	float fval;
	char *result;

	cMsgGetType(mg, (const char **) &type);
	cMsgGetText(mg, (const char **) &text);

	/* Check the command to see if we support it */
	if (strcmp(type, "session/control/stopReporting") == 0) {
		sendStatistics = 0;
		printf("Stop sending statistics...\n");
		return;
	} else if (strcmp(type, "session/control/startReporting") == 0) {
		sendStatistics = 1;
		printf("Start sending statistics...\n");
		rcCount = 0;
	} else if (strcmp(type, "session/control/setInterval") == 0) {
		fval = atof(text);
		ui_sec = (int) fval;
		ui_nsec = (int) ((fval - ui_sec) * 1000000000);
		printf("Set Status update interval to %3.1f seconds (%d,%d)\n", fval,
				ui_sec, ui_nsec);
	} else {
		printf("callbackControl: Do not understand the command: %s\n", type);
//		return;
	}

	/* create Statistics message */
	if (sendStatistics > 0) {
		msg = cMsgCreateMessage();
		cMsgSetSubject(msg, myName);
		cMsgSetType(msg, "rc/report/status");
		cMsgSetReliableSend(msg, 0); /* UDP */
		freem = 1;

		/* Set static Payload Items */
		cMsgAddString(msg, "codaName", myName);
		cMsgAddString(msg, "codaClass", "SLC");
		cMsgAddString(msg, "objectType", "Coda3");
	} else {
		freem = 0;
	}

	/* check if getChannel user function is defined */
	getChannel = dlsym(plugin, "getChannel");
	result = dlerror();
	if (result) {
		printf("Cannot find getChannel in %s: %s\n", myPluginName, result);
	} else {

		/* check if getChannelSize user function is defined */
		getChannelDataSize = dlsym(plugin, "getChannelDataSize");
		result = dlerror();
		if (result) {
			printf("Cannot find getChannelDataSize in %s: %s\n", myPluginName,
					result);
		} else {

			/* monitoring loop */
			while (sendStatistics > 0) {

				int i;
				char* ds;
				float* df;
				double* dd;
				int8_t* di8;
				int16_t* di16;
				int32_t* di32;
				int64_t* di64;
				uint8_t* dui8;
				uint16_t* dui16;
				uint32_t* dui32;
				uint64_t* dui64;
				const char** as;
				const float* af;
				const double* ad;
				const int8_t* ai8;
				const int16_t* ai16;
				const int32_t* ai32;
				const int64_t* ai64;
				const uint8_t* aui8;
				const uint16_t* aui16;
				const uint32_t* aui32;
				const uint64_t* aui64;

				/* wait for messages */
				sleeep.tv_sec = ui_sec;
				sleeep.tv_nsec = ui_nsec;
				nanosleep(&sleeep, NULL);

				/*
				 * get info, warning and error messages and report as a daLog messages
				 * daLog messages will be generated only if client changes the messages
				 */

				/* see if user provides reportInfo reportWarning and reportError functions*/
//				reportInfo = dlsym(plugin, "reportInfo");
//				result = dlerror();
//				if (result) {
//				} else {
//					printf(" Hoppa ....... %s %s\n", infoString, reportInfo());
//					if (reportInfo() != NULL) {
//						if (strcmp(reportInfo(), infoString) != 0) {
//                            free(infoString);
//							infoString = (char *) malloc(strlen(reportInfo())+1);
//							strcpy(infoString, reportInfo());
//							free(reportInfo());
//							daLogMsg("Info", infoString);
//						}
//						printf(" DEBIGI ....... %s\n", infoString);
//					}
//				}

//				reportWarning = dlsym(plugin, "reportWarning");
//				result = dlerror();
//				if (result) {
//				} else {
//					if (reportWarning() != NULL) {
//						char* ss = (char *) malloc(strlen(reportWarning()) + 1);
//						if (strcmp(ss, warningString) != 0) {
//							daLogMsg("Warning", ss);
//							warningString = (char *) malloc(strlen(ss)+1);
//							strcpy(warningString, ss);
//						}
//						printf(" DEBIGW ....... %s %s\n", ss, warningString);
//						free(ss);
//					}
//				}
//
//				reportError = dlsym(plugin, "reportError");
//				result = dlerror();
//				if (result) {
//				} else {
//					if (reportError() != NULL) {
//						char* ss = (char *) malloc(strlen(reportError()) + 1);
//						if (strcmp(ss, errorString) != 0) {
//							daLogMsg("Error", ss);
//							errorString = (char *) malloc(strlen(ss)+1);
//							strcpy(errorString, ss);
//						}
//						printf(" DEBIGE ....... %s %s\n", ss, errorString);
//						free(ss);
//					}
//				}

				/* add myState to the message */
				cMsgPayloadRemove(msg, "state");
				cMsgAddString(msg, "state", myState);

				/* user routine adding payload items to the message */
				for (i = 0; i < myChannelNumber; i++) {
					switch (myChannelTypes[i]) {
					case CMSG_CP_STR:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						ds = (char *) getChannel(myChannelNames[i]);
						cMsgAddString(msg, myChannelNames[i], ds);
						break;
					case CMSG_CP_FLT:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						df = (float*) getChannel(myChannelNames[i]);
						cMsgAddFloat(msg, myChannelNames[i], *df);
						break;
					case CMSG_CP_DBL:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						dd = (double*) getChannel(myChannelNames[i]);
												printf("debug %s\n", myChannelNames[i]);
												printf("debug %f\n",*dd);
						cMsgAddDouble(msg, myChannelNames[i], *dd);
						break;
					case CMSG_CP_INT8:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						di8 = (int8_t*) getChannel(myChannelNames[i]);
						cMsgAddInt8(msg, myChannelNames[i], *di8);
						break;
					case CMSG_CP_INT16:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						di16 = (int16_t*) getChannel(myChannelNames[i]);
						cMsgAddInt16(msg, myChannelNames[i], *di16);
						break;
					case CMSG_CP_INT32:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						di32 = (int32_t*) getChannel(myChannelNames[i]);
						cMsgAddInt32(msg, myChannelNames[i], *di32);
						break;
					case CMSG_CP_INT64:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						di64 = (int64_t*) getChannel(myChannelNames[i]);
						cMsgAddInt64(msg, myChannelNames[i], *di64);
						break;
					case CMSG_CP_UINT8:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						dui8 = (uint8_t*) getChannel(myChannelNames[i]);
						cMsgAddUint8(msg, myChannelNames[i], *dui8);
						break;
					case CMSG_CP_UINT16:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						dui16 = (uint16_t*) getChannel(myChannelNames[i]);
						cMsgAddUint16(msg, myChannelNames[i], *dui16);
						break;
					case CMSG_CP_UINT32:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						dui32 = (uint32_t*) getChannel(myChannelNames[i]);
						cMsgAddUint32(msg, myChannelNames[i], *dui32);
						break;
					case CMSG_CP_UINT64:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						dui64 = (uint64_t*) getChannel(myChannelNames[i]);
						cMsgAddUint64(msg, myChannelNames[i], *dui64);
						break;

					case CMSG_CP_STR_A:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						as = (const char**) getChannel(myChannelNames[i]);
						cMsgAddStringArray(msg, myChannelNames[i], as,
								getChannelDataSize(myChannelNames[i]));
						break;
					case CMSG_CP_FLT_A:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						af = (const float*)getChannel(myChannelNames[i]);
						cMsgAddFloatArray(msg, myChannelNames[i], af,
								getChannelDataSize(myChannelNames[i]));
						break;
					case CMSG_CP_DBL_A:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						ad = (const double*) getChannel(myChannelNames[i]);
						                        printf("debug %s\n", myChannelNames[i]);
						                        printf("debug %f\n", ad[3]);

						cMsgAddDoubleArray(msg, myChannelNames[i], ad,
								getChannelDataSize(myChannelNames[i]));
						break;
					case CMSG_CP_INT8_A:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						ai8 = (const int8_t*) getChannel(myChannelNames[i]);
						cMsgAddInt8Array(msg, myChannelNames[i], ai8,
								getChannelDataSize(myChannelNames[i]));
						break;
					case CMSG_CP_INT16_A:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						ai16 = (const int16_t*) getChannel(myChannelNames[i]);
						cMsgAddInt16Array(msg, myChannelNames[i], ai16,
								getChannelDataSize(myChannelNames[i]));
						break;
					case CMSG_CP_INT32_A:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						ai32 = (const int32_t*) getChannel(myChannelNames[i]);
						cMsgAddInt32Array(msg, myChannelNames[i], ai32,
								getChannelDataSize(myChannelNames[i]));
						break;
					case CMSG_CP_INT64_A:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						ai64 = (const int64_t*) getChannel(myChannelNames[i]);
						cMsgAddInt64Array(msg, myChannelNames[i], ai64,
								getChannelDataSize(myChannelNames[i]));
						break;
					case CMSG_CP_UINT8_A:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						aui8 = (const uint8_t*) getChannel(myChannelNames[i]);
						cMsgAddUint8Array(msg, myChannelNames[i], aui8,
								getChannelDataSize(myChannelNames[i]));
						break;
					case CMSG_CP_UINT16_A:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						aui16 = (const uint16_t*) getChannel(myChannelNames[i]);
						cMsgAddUint16Array(msg, myChannelNames[i], aui16,
								getChannelDataSize(myChannelNames[i]));
						break;
					case CMSG_CP_UINT32_A:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						aui32 = (const uint32_t*) getChannel(myChannelNames[i]);
						cMsgAddUint32Array(msg, myChannelNames[i], aui32,
								getChannelDataSize(myChannelNames[i]));
						break;
					case CMSG_CP_UINT64_A:
						cMsgPayloadRemove(msg, myChannelNames[i]);
						aui64 = (const uint64_t*) getChannel(myChannelNames[i]);
						cMsgAddUint64Array(msg, myChannelNames[i], aui64,
								getChannelDataSize(myChannelNames[i]));
						break;
					}
				}

//				char* t;
//				char* s;
//				cMsgGetSubject(msg, (const char **) &s);
//				cMsgGetType(msg, (const char **) &t);
//                 printf("sending .... subject = %s type = %s\n",s,t) ;

				/* send the message */
				err = cMsgSend(domainId, msg);

				/* Check the Subscription queue for any other messages that may have come in */
				cMsgGetSubscriptionCueSize(mg, &cuesize);
				if (cuesize > 0)
					break;
			}
		}
	}
	/* Free up any allocated messages or strings before exiting*/
	if (freem)
		cMsgFreeMessage(&msg);
	cMsgFreeMessage(&mg);
}
// -------------------------------------------------



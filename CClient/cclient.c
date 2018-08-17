/*
 * cclient.c
 *
 *  Created on: Nov 23, 2010
 *      Author: gurjyan
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "cclient.h"

/* globals */
char* c[3];
int i[3];
double data = 7.7;
double x[] = {1.1, 2.2, 3.3, 4.4, 5.5, 4.4, 3.3, 2.2, 1.1 };

int userConfigure(char* name) {
	printf("userConfigure is called with the config  = \n%s\n",name);
	return (0);
}

int userDownload(char* name) {
	printf("userDownload is called.\n");
	return (0);
}

int userPrestart(char* name) {
	printf("userPrestart is called.\n");
	return (1);
}

int userGo(char* name) {
	printf("userGo is called.\n");
	return (0);
}

int userEnd(char* name) {
	printf("userEnd is called.\n");
	return (0);
}

int userPause(char* name) {
	printf("userPause is called.\n");
	return (0);
}

int userResume(char* name) {
	printf("userResume is called.\n");
	return (0);
}

int userReset(char* name) {
	printf("userReset is called.\n");
	return (0);
}

int getChannelNumber() {
	return (3);
}

char** getChannelNames() {
	c[0] = "c1";
	c[1] = "c2";
	c[2] = "c3";
	return (c);
}

int* getChannelDataTypes() {
	i[0] = 12;
	i[1] = 12;
	i[2] = 25;
	return (i);
}

void* getChannel(char* name) {
	if (strcmp(name, "c3") == 0) {
		return (x);
	} else {
	return (&data);
	}
}

int getChannelDataSize(char *name) {
	if (strcmp(name, "c3") == 0) {
		return (9);
	} else {
	return (1);
	}
}

char* reportInfo() {
	return "Info";
}

char* reportWarning() {
	return "Warning";
}

char* reportError() {
	return "Error";
}


/*
 * AComponent.h
 *
 *  Created on: Nov 23, 2010
 *      Author: gurjyan
 */

#ifndef ACOMPONENT_H_
#define ACOMPONENT_H_

char*  reportInfo();
char*  reportWarning();
char*  reportError();
int    getChannelNumber   ();
char** getChannelNames    ();
int*   getChannelDataTypes();
int    getChannelDataSize (char *name);
void*  getChannel         (char* name);

int    userConfigure      (char* name);
int    userDownload       (char* name);
int    userPrestart       (char* name);
int    userGo             (char* name);
int    userEnd            (char* name);
int    userPause          (char* name);
int    userResume         (char* name);
int    userReset          (char* name);

/* Channel data type enumerations */
enum {
  A_STR    = 10, /**< String.                    */
  A_FLT,         /**< 4 byte float.              */
  A_DBL,         /**< 8 byte float.              */
  A_INT8,        /**< 8 bit int.                 */
  A_INT16,       /**< 16 bit int.                */
  A_INT32,       /**< 32 bit int.                */
  A_INT64,       /**< 64 bit int.                */
  A_UINT8,       /**< unsigned  8 bit int.       */
  A_UINT16,      /**< unsigned 16 bit int.       */
  A_UINT32,      /**< unsigned 32 bit int.       */
  A_UINT64,      /**< unsigned 64 bit int.       */
  CMSG_CP_MSG,   /**< cMsg message.              */
  CMSG_CP_BIN,   /**< binary.                    */

  A_STR_A,       /**< String array.              */
  A_FLT_A,       /**< 4 byte float array.        */
  A_DBL_A,       /**< 8 byte float array.        */
  A_INT8_A,      /**< 8 bit  int array.          */
  A_INT16_A,     /**< 16 bit int array.          */
  A_INT32_A,     /**< 32 bit int array.          */
  A_INT64_A,     /**< 64 bit int array.          */
  A_UINT8_A,     /**< unsigned  8 bit int array. */
  A_UINT16_A,    /**< unsigned 16 bit int array. */
  A_UINT32_A,    /**< unsigned 32 bit int array. */
  A_UINT64_A,    /**< unsigned 64 bit int array. */
};
#endif /* ACOMPONENT_H_ */

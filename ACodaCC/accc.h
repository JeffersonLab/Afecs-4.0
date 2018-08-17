/*
 * accc.h
 *
 *  Created on: Nov 19, 2010
 *      Author: gurjyan
 */

#ifndef ACLIENT_H_
#define ACLIENT_H_

  /* API functions defined in the user provided shared library */
  typedef int (*userConfigure_f) ();
  userConfigure_f userConfigure;

  typedef int (*userDownload_f) ();
  userDownload_f userDownload;

  typedef int (*userPrestart_f) ();
  userPrestart_f userPrestart;

  typedef int (*userGo_f) ();
  userGo_f userGo;

  typedef int (*userEnd_f) ();
  userEnd_f userEnd;

  typedef int (*userPause_f) ();
  userPause_f userPause;

  typedef int (*userResume_f) ();
  userResume_f userResume;

  typedef int (*userReset_f) ();
  userReset_f userReset;

  typedef int (*getChannelNumber_f) ();
  getChannelNumber_f getChannelNumber;

  typedef char** (*getChannelNames_f) ();
  getChannelNames_f getChannelNames;

  typedef int* (*getChannelDataTypes_f) ();
  getChannelDataTypes_f getChannelDataTypes;

  typedef int (*getChannelDataSize_f) ();
  getChannelDataSize_f getChannelDataSize;

  typedef void* (*getChannel_f) ();
  getChannel_f getChannel;

  typedef void* (*setChannel_f) ();
  setChannel_f setChannel;

  typedef char* (*reportInfo_f) ();
  reportInfo_f reportInfo;

  typedef char* (*reportWarning_f) ();
  reportWarning_f reportWarning;

  typedef char* (*reportError_f) ();
  reportError_f reportError;

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

#endif /* ACLIENT_H_ */

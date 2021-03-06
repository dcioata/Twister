#!/usr/bin/env python

# File: ExecutionProcess.py ; This file is part of Twister.

# Copyright (C) 2012 , Luxoft

# Authors:
#    Andrei Costachi <acostachi@luxoft.com>
#    Andrei Toma <atoma@luxoft.com>
#    Cristian Constantin <crconstantin@luxoft.com>
#    Daniel Cioata <dcioata@luxoft.com>

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at:

# http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

'''
Execution Process (EP) should be started as a service, on system startup.
Each EP (machine) has a unique name, called Ep Name.
EP gets his status from CE every second. The status can be changed using the Java interface.
When it receives START from CE, it will downloads all necessary libraries,
  start the Runner that will execute all test files from suite, send all Runner logs to CE
  and after the execution, it will wait for another START to repeat the cycle.
EP is basically a simple service, designed to start and stop the Runner.
All the hard work is made by the Runner.

EP can also start "offline" (without a connection to CE). This mode is used for debug,
but it requires that EP was already started "online" once before, because it needs one or more libraries.
Alternatively, the required libraries can be manually copied in "src/.twister_cache/ce_libs".
'''

import os
import sys
import time
import xmlrpclib
import pickle
import binascii
import threading
import subprocess

TWISTER_PATH = os.getenv('TWISTER_PATH')
if not TWISTER_PATH:
    print('TWISTER_PATH environment variable is not set! Exiting!')
    exit(1)
sys.path.append(TWISTER_PATH)

from common.constants import *

#

def saveConfig():
    '''
    Saves EpId, RPC Server IP/ Port and current EpId status in a file.
    This file will be read by the Runner.
    '''
    global globEpId, CEProxy, epStatus, TWISTER_PATH

    EP_CACHE = TWISTER_PATH + '/.twister_cache/' + globEpId
    cache_file = EP_CACHE + '/data.pkl'

    try: os.makedirs(EP_CACHE)
    except: pass

    CONFIG = {}
    CONFIG['ID'] = globEpId
    CONFIG['PROXY'] = CEProxy
    CONFIG['STATUS'] = epStatus

    if 0: # PIPE ?
        if not os.path.exists(cache_file):
            os.mkfifo(cache_file)
        f = os.open(cache_file, os.O_WRONLY)
        data = pickle.dumps(CONFIG)
        os.write(f, data)
        os.close(f) ; del f
    else:
        f = open(cache_file, 'wb')
        data = pickle.dumps(CONFIG)
        f.write(data)
        f.close() ; del f

#

def saveLibraries(proxy):
    '''
    Saves all libraries from CE.
    Not used in offline mode.
    '''
    global TWISTER_PATH
    libs_list = proxy.getLibrariesList()
    libs_path = TWISTER_PATH + '/.twister_cache/ce_libs/'

    try: os.makedirs(libs_path)
    except: pass

    __init = open(libs_path + '/__init__.py', 'w')
    __init.write('\nPROXY = "%s"\n' % CEProxy)
    all_libs = [os.path.splitext(lib)[0] for lib in libs_list]
    __init.write('\nall = ["%s"]\n\n' % ('", "'.join(all_libs)))

    for filename in libs_list:
        # Write in __init__ file.
        __init.write('import %s\n' % os.path.splitext(filename)[0])
        __init.write('from %s import *\n\n' % os.path.splitext(filename)[0])

        lib_pth = libs_path + os.sep + filename
        print('Downloading library `{0}` ...'.format(lib_pth))
        f = open(lib_pth, 'wb')
        lib_data = proxy.getLibraryFile(filename)
        f.write(lib_data.data)
        f.close() ; del f

    __init.close()

#

def saveTests(proxy):
    '''
    Saves all test files from CE.
    Not used in offline mode.
    '''
    global globEpId, TWISTER_PATH
    tests_list = proxy.getTestSuiteFileList(globEpId, False)
    tests_path = TWISTER_PATH + '/.twister_cache/to_execute/'

    try: os.makedirs(tests_path)
    except: pass

    for filename in tests_list:
        file_data = proxy.getTestCaseFile(globEpId, filename)

        # If file is not skipped
        if file_data:
            file_pth = tests_path + os.sep + os.path.split(filename)[1]
            print('Downloading test file `{0}` ...'.format(file_pth))
            f = open(file_pth, 'wb')
            f.write(file_data.data)
            f.close() ; del f

#

class threadCheckStatus(threading.Thread):
    '''
    Threaded class for checking CE Status.
    Not used in offline mode.
    '''
    def __init__(self):
        global CEProxy
        self.errMsg = True
        self.proxy = xmlrpclib.ServerProxy(CEProxy)
        threading.Thread.__init__(self)

    def run(self):
        #
        global epStatus, newEpStatus
        global programExit, globEpId
        #
        while not programExit:

            try:
                # Try to get status from CE!
                newEpStatus = self.proxy.getExecStatus(globEpId)
                if not self.errMsg:
                    print('EP warning: Central Engine is running. Reconnected successfully.')
                    self.errMsg = True
            except:
                if self.errMsg:
                    print('EP warning: Central Engine is down. Trying to reconnect...')
                    self.errMsg = False
                # Wait and retry...
                time.sleep(3)
                continue

            # If status changed
            if newEpStatus != epStatus:
                print('Py debug: For EP %s, CE Server returned a new status: %s.' % \
                    (globEpId.upper(), str(newEpStatus)))
            epStatus = newEpStatus

            saveConfig() # Save configuration EVERY second

            if newEpStatus == 'stopped':
                # PID might be invalid, trying to kill it anyway.
                global tcr_pid
                if tcr_pid:
                    try:
                        os.kill(tcr_pid, 9)
                        print('EP warning: STOP from Central Engine! Killing Runner PID %s.' % str(tcr_pid))
                        tcr_pid = None
                    except:
                        pass

            time.sleep(3)
            #

#

class threadCheckLog(threading.Thread):
    '''
    Threaded class for checking LIVE.log.
    '''
    def __init__(self):
        global CEProxy
        self.proxy = xmlrpclib.ServerProxy(CEProxy)
        self.read_len = 0
        threading.Thread.__init__(self)

    def tail(self, file_path):
        global globEpId
        f = open(file_path, 'rb')
        # Go at "current position"
        f.seek(self.read_len, 0)
        vString = f.read()
        vLen = len(vString)
        # Fix double new-line
        vString = vString.replace('\r\n', '\n')
        vString = vString.replace('\n\r', '\n')
        # Increment "current position"
        self.read_len += vLen
        f.close()
        return vString

    def run(self):
        #
        global globEpId, TWISTER_PATH, programExit

        while not programExit:
            #
            vString = self.tail('{0}/.twister_cache/{1}_LIVE.log'.format(TWISTER_PATH, globEpId))

            try:
                # Send log to CE server.
                self.proxy.logLIVE(globEpId, binascii.b2a_base64(vString))
            except:
                # Wait and retry...
                time.sleep(3)
                continue

            time.sleep(3)
            #

#

if __name__=='__main__':

    epStatus = ''
    newEpStatus = ''
    programExit = False
    OFFLINE = False

    try: os.mkdir(TWISTER_PATH + '/.twister_cache/')
    except: pass

    if len(sys.argv) != 3:
        print('EP error: must supply 2 parameters!')
        print('usage:  python ExecutionProcess.py Ep_Id_Name Host:Port')
        print('OR,')
        print('usage:  python ExecutionProcess.py OFFLINE File_List_Path')
        exit(1)
    else:
        globEpId = sys.argv[1]
        # If EP is started in OFFLINE mode
        if globEpId.upper() == 'OFFLINE':
            OFFLINE = True
            globEpId = 'OFFLINE'
            filelist = sys.argv[2]
            if not os.path.isfile(filelist):
                print('EP error: Invalid file list! File `{0}` does not exits!'.format(filelist))
                exit(1)
        else:
            host = sys.argv[2]
            filelist = ''
            print 'Epid: {0} host: {1}'.format(globEpId, host)

    # Offline mode...
    if OFFLINE:
        epStatus = 'running'
    # Connected to CE...
    else:
        CEProxy = 'http://' + host + '/'
        tcr_pid = None # PID of TC Runner
        threadCheckStatus().start() # Start checking CE status
        threadCheckLog().start()    # Start reading live log
        proxy = xmlrpclib.ServerProxy(CEProxy)

    print('EP debug: Setup done, waiting for START signal.')

    # Run forever and ever
    while 1:

        if epStatus == 'running':
            # If not offline, save all libraries from CE
            if not OFFLINE:
                saveLibraries(proxy)
                #saveTests(proxy)
            print('EP debug: Received start signal from CE!')

            # The same Python interpreter that started EP, will be used to start the Runner
            tcr_fname = TWISTER_PATH + os.sep + 'client/executionprocess/TestCaseRunner.py'
            tcr_proc = subprocess.Popen([sys.executable, '-u', tcr_fname, globEpId, filelist], shell=False)
            tcr_pid = tcr_proc.pid
            # TestCaseRunner should suicide if timer expired
            tcr_proc.wait()
            ret = tcr_proc.returncode

            # Set EP status STOPPED
            if not ret:
                print('EP debug: Successful run!\n')
                proxy.setExecStatus(globEpId, STATUS_STOP, 'Successful run!')
            if ret == -26:
                print('EP debug: TC Runner exit because of timeout!\n')
                proxy.setExecStatus(globEpId, STATUS_STOP, 'TC Runner exit because of timeout!')
            # Return code != 0
            elif ret:
                print('EP debug: TC Runner exit with error code `{0}`!\n'.format(ret))
                if not OFFLINE:
                    # Set EP status STOPPED
                    proxy.setExecStatus(globEpId, STATUS_STOP, 'TC Runner exit with error code `{0}`!'.format(ret))

        # For offline, only 1 cycle is executed
        if OFFLINE:
            break
        else:
            time.sleep(3)

    # If the cicle is broken, try to kill the threads...!
    programExit = True

#

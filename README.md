MINERful RC2 (2015/06/15)
=========================


What is it?
-----------

MINERful is a fast miner for discovering declarative process models out of logs. Logs can be either real or synthetic. MINERful can also create logs on the fly and inject errors inside them, too.
Please notice that this is the command-line version of MINERful. For the GUI-equipped release, please download the [ProM Nightly Build](http://www.promtools.org/prom6/nightly/) and install DeclareMinerFul package.


The Latest Version
------------------
		RC1.       	It is the first public release of MINERful.
		Beta 1.01		Some bugs fixed and experimental features added (still not stable).
		Beta 1.02		Improved CSV export.
		Beta 1.03		ConDec export facility included.
		Beta 1.04		Confidence level filtering (-c param).
		RC2.				It is the second public release of MINERful. Introduces the discovery of Target-branched Declare constraints (-b param).

Installation
------------

You need to have a JRE 6+ installed on your machine.
To launch the SH files, you have to run them on a Unix-based system with a BASH shell.
No installation procedure is required.
This version has been tested on both a Ubuntu Linux (10.04, 12.04) and a Mac OS X (Snow Leopard) machine.

Licensing
---------
Please see the LICENSE file.

Usage (with .sh files)
---------

  The easiest way for you to launch MINERful is to make use of the SH files.
  You have:

  		- run-MINERful.sh
      it launches the miner
  		- run-MINERful-overSimulatedTraces.sh
      it launches the miner over a synthetic log, created on the fly
  		- run-MINERful-overErrorInjectedTraces.sh
      it launches the miner over a synthetic log, which is created on the fly and then altered with a controlled jection of errors in the traces

  		- run-MINERfulTracesMaker.sh
        it launches the builder of synthetic logs

  		- run-MINERfulErrorInjectedTracesMaker.sh
        it launches the builder of synthetic error-injected logs

  		- test-launchers/*.sh
        these files make the miner execute several runs, over synthetic logs - for testing purposes.

  Each of those launchers can be invoked with the
      -h
  parameter. This way, you can read an explanation of the possible parameters you can pass. Depending on the case, some parameters are specified in the SH file, others are left free.
  In case, you can always modify the constants declared at the beginning of the script, so as to customise them.

  The SH scripts that end with "-unstable.sh" suffix do not launch MINERful by the JAR, as they use the bytecode files. In this way, the user can immediately try the modified source code without overwriting the JAR version.

Example usage (with .sh files)
--------
    
  	- Mining a XES log file located in /home/user/file.xes

      	run-MINERful.sh -iLF '/home/user/file.xes' 
  
  	- Mining a XES log file located in /home/user/file.xes, with comprehensive debug lines
       	run-MINERful.sh -d all -iLF '/home/user/file.xes' 
  
  	- Mining a XES log file located in /home/user/file.xes, with comprehensive debug lines. Let results be exported in a CSV file located in /home/user/output.csv

     		run-MINERful.sh -d all -iLF '/home/user/file.xes' -CSV '/home/user/output.csv'
    
  	- Mining a XES log file located in /home/user/file.xes, with comprehensive debug lines. Prune out those which are below. Let results be exported in a CSV file located in /home/user/output.csv

      	run-MINERful.sh -d all -iLF '/home/user/file.xes' -CSV '/home/user/output.csv'


Example usage (with the .class files)
---------

    This is a little bit more trickier, but necessary, in case you have a Microsoft Windows system.
    From your prompt, type:
        java -jar MINERful.jar it.uniroma1.dis.minerful.<LAUNCHER_CLASS> -h
    where <LAUNCHER_CLASS> can be either

  	- MinerFulMinerStarter
        to launch the miner
  	- MinerFulSimuStarter
        to launch the miner over a synthetic log, created on the fly
  	- MinerFulErrorInjectedSimuStarter
        to launch the miner over a synthetic log, which is created on the fly and then altered with a controlled injection of errors in the traces
  	- MinerFulTracesMakerStarter
        to launch the builder of synthetic logs
  	- MinerFulErrorInjectedTracesMakerStarter
        to launch the builder of synthetic error-injected logs

  The "-h" parameter appended at the end of the prompt shows and explains the parameters you can pass. They are exactly the same as the Linux/MacOS version.


Contacts
---------

Please contact the author, Claudio Di Ciccio, for any information, comment or bug reporting 
dc.claudio@gmail.com

MINERful
=========================

What is it?
-----------

MINERful is a fast miner for discovering declarative process models out of logs. Logs can be either real or synthetic, stored as [XES](http://www.xes-standard.org/) files or text documents with strings. Among the other things, MINERful can also create synthetic logs and export them as [XES](http://www.xes-standard.org/) or [MXML](http://www.processmining.org/logs/mxml) files, simplify existing Declare models, and import/export models written in JSON or in the ConDec native language. Simply play around with it!

Installation
------------

You need to have a JRE 7+ installed on your machine.
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

  		- run-MINERfulTracesMaker.sh
      it launches the builder of synthetic logs

  		- test-launchers/*.sh
      these files make the miner execute several runs, over synthetic logs - for testing purposes.

  Each of those launchers can be invoked with the
      -h
  parameter. You can read an explanation of the possible parameters you can pass. Depending on the case, some parameters are specified in the SH file, others are left free.
  In case, you can always modify the constants declared at the beginning of the script, so as to customise them.

  The SH scripts that end with "-unstable.sh" suffix do not launch MINERful by the JAR, as they use the bytecode files. In this way, the user can immediately try the modified source code without overwriting the JAR version.

MINERful as a Java package
---------

For advanced users: You can use MINERful as a Java package and integrate it with your software! Check out the [examples.api](https://github.com/cdc08x/MINERful/tree/master/src/minerful/examples/api) source code to see some examples.

Example usage (with .sh files)
--------
    
  	- Mine a XES log file located in /home/user/file.xes

      	run-MINERful.sh -iLF '/home/user/file.xes' 

  	- Display the help screen

      	run-MINERful.sh -h
  
  	- Mine a XES log file located in /home/user/file.xes and export the discovered model in a XML file located in /home/user/model-condec.xml, formatted as Declare/ConDec. Set a support threshold of 0.95, a confidence level threshold of 0.25, and an interest factor threshold of 0.125

      	run-MINERful.sh -iLF '/home/user/file.xes' -condec '/home/user/model-condec.xml' -s 0.95 -c 0.25 -i 0.125
  
  	- Mine a XES log file located in /home/user/file.xes, with comprehensive debug lines

       	run-MINERful.sh -d all -iLF '/home/user/file.xes' 
  
  	- Mine a XES log file located in /home/user/file.xes, with comprehensive debug lines. Let results be exported in a CSV file located in /home/user/output.csv

     		run-MINERful.sh -d all -iLF '/home/user/file.xes' -CSV '/home/user/output.csv'
    
  	- Mine a XES log file located in /home/user/file.xes, with comprehensive debug lines. Set a support threshold of 0.95, a confidence level threshold of 0.25, and an interest factor threshold of 0.125. Let results be exported in a CSV file located in /home/user/output.csv

      	run-MINERful.sh -d all -iLF '/home/user/file.xes' -CSV '/home/user/output.csv' -s 0.95 -c 0.25 -i 0.125


Usage (with the .class files)
---------

This is a little bit trickier, but necessary, in case you have a Microsoft Windows system.
From your prompt, type:

        java -jar MINERful.jar minerful.<LAUNCHER_CLASS> -h

where the LAUNCHER_CLASS can be either

  	- MinerFulMinerStarter
        to launch the miner
  	- MinerFulTracesMakerStarter
        to launch the builder of synthetic logs

  The "-h" parameter appended at the end of the prompt shows and explains the parameters you can pass. They are exactly the same as the Linux/MacOS version.

Other software packages using MINERful
---------

A ProM package of MINERful exists, although it is in beta version and with limited functionalities: It is available in the [ProM Nightly Build SVN repository](https://svn.win.tue.nl/repos/prom/Packages/DeclareMinerFul/Trunk/).

A GUI-equipped log generator is also in its beta version, based on the [Declare Designer](http://ceur-ws.org/Vol-489/paper1.pdf) tool: It is available for download on the [Synthetic log generator GitHub page](https://github.com/processmining/synthetic-log-generator).

Contacts
---------

Please contact the author, Claudio Di Ciccio, for any information, comment or bug reporting:
[dc.claudio@gmail.com](mailto:dc.claudio@gmail.com)

MINERful
=========================

MINERful is a fast process mining tool for discovering declarative process models out of event logs. Event logs can be either real or synthetic, stored as [XES](http://www.xes-standard.org/) files or text documents with strings (every character is considered as an event, every line as a trace). Among the other things, MINERful can also create synthetic logs and export them as [XES](http://www.xes-standard.org/) or [MXML](http://www.processmining.org/logs/mxml) files, simplify existing Declare models, and import/export models written in [JSON](http://www.json.org/) or in the ConDec native language. Simply play around with it!

Publications and further material
------------
Selected publications about MINERful and presentation slides:
  - The main discovery algorithm:
  
    Claudio Di Ciccio, Massimo Mecella: On the Discovery of Declarative Control Flows for Artful Processes. ACM Trans. Management Inf. Syst. 5(4): 24:1-24:37 (2015)
    - DOI: [10.1145/2629447](http://doi.acm.org/10.1145/2629447)
    - Presentation: [https://www.slideshare.net/cdc08x/automated-discovery-of-declarative-process-models](https://www.slideshare.net/cdc08x/automated-discovery-of-declarative-process-models)
    
  - Discovery of more target-branched (read: more complex) declarative models:
  
    Claudio Di Ciccio, Fabrizio Maria Maggi, Jan Mendling: Efficient discovery of Target-Branched Declare constraints. Inf. Syst. 56: 258-283 (2016)
    - DOI: [10.1016/j.is.2015.06.009](https://doi.org/10.1016/j.is.2015.06.009)
    
  - Getting rid of redundancies and inconsistencies:
  
    Claudio Di Ciccio, Fabrizio Maria Maggi, Marco Montali, Jan Mendling: Resolving inconsistencies and redundancies in declarative process models. Inf. Syst. 64: 425-446 (2017)
    - DOI: [10.1016/j.is.2016.09.005](https://doi.org/10.1016/j.is.2016.09.005)
    - Presentation: [https://www.slideshare.net/cdc08x/resolving-inconsistencies-and-redundancies-in-declarative-process-models](https://www.slideshare.net/cdc08x/resolving-inconsistencies-and-redundancies-in-declarative-process-models)
    
  - Simulation of declarative models:
  
    Claudio Di Ciccio, Mario Luca Bernardi, Marta Cimitile, Fabrizio Maria Maggi: Generating Event Logs Through the Simulation of Declare Models. EOMAS@CAiSE 2015: 20-36
    - DOI: [10.1007/978-3-319-24626-0_2](https://doi.org/10.1007/978-3-319-24626-0_2)

Installation
------------
You need to have a JRE 7+ installed on your machine.
To launch the `.sh` files, you have to run them on a Unix-based system with a BASH shell.
No installation procedure is required.
This version has been tested on both a Ubuntu Linux (16.04) and a Mac OS X (Snow Leopard) machine.

MINERful as a stand-alone application (with .sh files)
---------
The easiest way to launch MINERful is to make use of the `.sh` files.
  - To launch the miner: `run-MINERful.sh`
  - To create synthetic logs: `run-MINERfulTracesMaker.sh`
  - To run tests (as we did for the evaluation sections of our papers): `test-launchers/*.sh`

Each of those launchers can be invoked with the `-h` parameter. An explanation of all possible parameters that can be passed is written there (beware, they are quite an amount so take your time). Depending on the case, some parameters are specified in the `.sh` files, whereas others are left free.
In case, feel free to modify the constants declared at the beginning of the script, so as to customise them.

The scripts that end with the `-unstable.sh` suffix do not launch MINERful via its JAR, but with bytecode files. In this way, we can immediately try the modified source code without overwriting the JAR version.

Some usage examples follow.

Usage examples of MINERful as a stand-alone application (with `.sh` files)
--------
- Mine an `XES` log file located in `/home/user/file.xes`:

      run-MINERful.sh -iLF '/home/user/file.xes' 

- Display the help screen:

      run-MINERful.sh -h
  
 Mine an `XES` log file located in `/home/user/file.xes` and export the discovered model in an `XML` file located in `/home/user/model-condec.xml`, formatted as Declare/ConDec. Set a support threshold of 0.95, a confidence level threshold of 0.25, and an interest factor threshold of 0.125 (if you have no idea what these parameters are about, check [this paper](http://doi.acm.org/10.1145/2629447) paper out):

      run-MINERful.sh -iLF '/home/user/file.xes' -condec '/home/user/model-condec.xml' -s 0.95 -c 0.25 -i 0.125
  
- Mine an `XES` log file located in `/home/user/file.xes`, with comprehensive debug lines

      run-MINERful.sh -d all -iLF '/home/user/file.xes' 
  
- Mine an `XES` log file located in `/home/user/file.xes`, with comprehensive debug lines. Let results be exported in a `CSV` file located in `/home/user/output.csv`:

      run-MINERful.sh -d all -iLF '/home/user/file.xes' -CSV '/home/user/output.csv'
    
- Mine an `XES` log file located in `/home/user/file.xes`, with comprehensive debug lines. Set a support threshold of 0.95, a confidence level threshold of 0.25, and an interest factor threshold of 0.125 (if you have no idea what these parameters are about, check [this paper](http://doi.acm.org/10.1145/2629447) out). Let results be exported in a `CSV` file located in `/home/user/output.csv`:

      run-MINERful.sh -d all -iLF '/home/user/file.xes' -CSV '/home/user/output.csv' -s 0.95 -c 0.25 -i 0.125

Usage examples of MINERful as a stand-alone application (directly with bytecode files)
---------

This is a little bit trickier, but necessary, in case you have a Microsoft Windows system.
From your prompt, type:

      java -jar MINERful.jar minerful.<LAUNCHER_CLASS> -h

where the LAUNCHER_CLASS can be either `MinerFulMinerStarter` to launch the miner, or `MinerFulTracesMakerStarter` to launch the builder of synthetic logs.

The `-h` parameter appended at the end of the prompt shows and explains the parameters you can pass. They are exactly the same as the Linux/MacOS version.

MINERful as a Java package
---------
For advanced users: You can use MINERful as a Java package and integrate it with your software! Check out the [minerful.examples.api](https://github.com/cdc08x/MINERful/tree/master/src/minerful/examples/api) source code to see some examples.
In particular:
- [discovery.MinerFulObserverInvokerOnXesFile](https://github.com/cdc08x/MINERful/tree/master/src/minerful/examples/api/discovery/MinerFulObserverInvokerOnXesFile.java)  demonstrates how to invoke the MINERful miner as an API, and subsequently observe the changes that are applied to the process model in a [publish/subscribe](https://en.wikipedia.org/wiki/Publish%E2%80%93subscribe_pattern) fashion. Lastly, the model is saved as a Declare Map file.
- [discovery.MinerFulCallerOnStringFile](https://github.com/cdc08x/MINERful/tree/master/src/minerful/examples/api/discovery/MinerFulCallerOnStringFile.java) demonstrates how to call MINERful to discover a process model out of strings saved on a file.
- [logmaking.FromCharactersProcessModelToLog](https://github.com/cdc08x/MINERful/tree/master/src/minerful/examples/api/logmaking/FromStringsProcessModelToLog.java) demonstrates how to generate XES logs starting from the definitions of new constraints. It can be interesting also because it shows how to create a process model on the fly.
- [logmaking.FromStringsProcessModelToLog](https://github.com/cdc08x/MINERful/tree/master/src/minerful/examples/api/logmaking/FromStringsProcessModelToLog.java) demonstrates how to generate XES logs starting from the definitions of constraints exerted on activities identified by single characters (more or less the same as above).
- [logmaking.FromDeclareMapToLog](https://github.com/cdc08x/MINERful/tree/master/src/minerful/examples/api/logmaking/FromDeclareMapToLog.java) demonstrates how to generate XES logs from an existing Declare map XML file. It is worth to see how to convert process models from the Declare Maps Miner format, for those who used that tool in [ProM](http://www.promtools.org).
- [logmaking.FromJsonProcessModelToLog](https://github.com/cdc08x/MINERful/tree/master/src/minerful/examples/api/logmaking/FromJsonProcessModelToLog.java) demonstrates how to generate XES logs starting with the definitions of constraints specified with [JSON](http://www.json.org/) objects.
- [simplification.MinerFulSimplificationInvokerOnDeclareMapFile](https://github.com/cdc08x/MINERful/tree/master/src/minerful/examples/api/simplification/MinerFulSimplificationInvokerOnDeclareMapFile.java) demonstrates how to load a Declare Map file as a process model, then run the simplification engine of MINERful to remove the redundant constraints.

Other software packages using MINERful
---------

A ProM package of MINERful exists, although it is in beta version and with limited functionalities: It is available in the [ProM Nightly Build SVN repository](https://svn.win.tue.nl/repos/prom/Packages/DeclareMinerFul/Trunk/). More information is available in this paper:
  - Claudio Di Ciccio, Mitchel H. M. Schouten, Massimiliano de Leoni, Jan Mendling: Declarative Process Discovery with MINERful in ProM. BPM (Demos) 2015: 60-64. URL: [http://ceur-ws.org/Vol-1418/paper13.pdf](http://ceur-ws.org/Vol-1418/paper13.pdf)

A GUI-equipped log generator is also in its beta version, based on the [Declare Designer](http://ceur-ws.org/Vol-489/paper1.pdf) tool: It is available for download on the [Synthetic log generator GitHub page](https://github.com/processmining/synthetic-log-generator). More information is available in this paper:
  - Claudio Di Ciccio, Mario Luca Bernardi, Marta Cimitile, Fabrizio Maria Maggi: Generating Event Logs Through the Simulation of Declare Models. EOMAS@CAiSE 2015: 20-36. DOI: [10.1007/978-3-319-24626-0_2](https://doi.org/10.1007/978-3-319-24626-0_2)

Licensing
=========================
Please read the [LICENSE](https://github.com/cdc08x/MINERful/edit/master/LICENSE) file.

Contacts
=========================

Please contact the developer, [Claudio Di Ciccio](https://www.wu.ac.at/en/infobiz/team/diciccio/), for any information, comment or bug reporting:
[dc.claudio@gmail.com](mailto:dc.claudio@gmail.com)

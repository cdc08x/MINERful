MINERful
=========================

MINERful is a fast process mining tool for discovering declarative process specifications out of event logs. Event logs can be either real or synthetic, stored as [XES](http://www.xes-standard.org/), [MXML](http://www.processmining.org/logs/mxml), or text files (a collection of strings, in which every character is considered as an event, and every line as a trace). Among other things, MINERful can also create synthetic logs and export them as [XES](http://www.xes-standard.org/) or [MXML](http://www.processmining.org/logs/mxml) files, simplify existing Declare specifications, and import/export specifications written in [JSON](http://www.json.org/) or in the ConDec native language. Simply play around with it!

For updated info on the installation, usage, etc., please refer to the [**Wiki**](https://github.com/cdc08x/MINERful/wiki)!

Publications and further material
------------
Selected publications about MINERful and presentation slides:
  - About declarative specifications:
  
    Claudio Di Ciccio, Marco Montali: Declarative Process Specifications: Reasoning, Discovery, Monitoring. Process Mining Handbook: 108-152 (2022)
    - DOI: [10.1007/978-3-031-08848-3_4](https://doi.org/10.1007/978-3-031-08848-3_4) (open access!)
    - Presentation: [https://drive.google.com/uc?export=download&id=1jdla84hdV7m04QGTD6_NTgZ1E_V5fW2L](https://drive.google.com/uc?export=download&id=1jdla84hdV7m04QGTD6_NTgZ1E_V5fW2L)
    
  - The main discovery algorithm:
  
    Claudio Di Ciccio, Massimo Mecella: On the Discovery of Declarative Control Flows for Artful Processes. ACM Trans. Management Inf. Syst. 5(4): 24:1-24:37 (2015)
    - DOI: [10.1145/2629447](http://doi.acm.org/10.1145/2629447)
    - Presentation: [https://www.slideshare.net/cdc08x/automated-discovery-of-declarative-process-models](https://www.slideshare.net/cdc08x/automated-discovery-of-declarative-process-models)
    
  - Quality measures for discovered process specifications:
  
    Alessio Cecconi, Giuseppe De Giacomo, Claudio Di Ciccio, Fabrizio Maria Maggi, Jan Mendling: Measuring the interestingness of temporal logic behavioral specifications in process mining. Inf. Syst. 107: 101920 (2022)
    - DOI: [10.1016/j.is.2021.101920](https://doi.org/10.1016/j.is.2021.101920) (open access!)
    
  - Simulation of declarative specifications:
  
    Claudio Di Ciccio, Mario Luca Bernardi, Marta Cimitile, Fabrizio Maria Maggi: Generating Event Logs Through the Simulation of Declare Specifications. EOMAS@CAiSE 2015: 20-36
    - DOI: [10.1007/978-3-319-24626-0_2](https://doi.org/10.1007/978-3-319-24626-0_2)
    
  - Getting rid of redundancies and inconsistencies:
  
    Claudio Di Ciccio, Fabrizio Maria Maggi, Marco Montali, Jan Mendling: Resolving inconsistencies and redundancies in declarative process models. Inf. Syst. 64: 425-446 (2017)
    - DOI: [10.1016/j.is.2016.09.005](https://doi.org/10.1016/j.is.2016.09.005)
    - Presentation: [https://www.slideshare.net/cdc08x/resolving-inconsistencies-and-redundancies-in-declarative-process-models](https://www.slideshare.net/cdc08x/resolving-inconsistencies-and-redundancies-in-declarative-process-models)

  - Retaining only non-vacuously satisfied (read: relevant) constraints:
  
    Claudio Di Ciccio, Fabrizio Maria Maggi, Marco Montali, Jan Mendling: On the relevance of a business constraint to an event log. Inf. Syst. 78: 144-161 (2018)
    - DOI: [10.1016/j.is.2018.01.011](https://doi.org/10.1016/j.is.2018.01.011)
    - Presentation: [https://www.slideshare.net/cdc08x/semantical-vacuity-detection-in-declarative-process-mining](https://www.slideshare.net/cdc08x/semantical-vacuity-detection-in-declarative-process-mining)


# License
Please read the [LICENSE](https://github.com/cdc08x/MINERful/edit/master/LICENSE) file.

# Contact

Please contact the developer, [Claudio Di Ciccio](https://diciccio.net), for any information, comment or bug reporting:
[claudio@diciccio.net](mailto:claudio@diciccio.net).

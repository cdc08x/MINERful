package minerful.index.comparator.modular;

import minerful.index.SortingPolicy;

public enum CnsSortModularDefaultPolicy implements SortingPolicy {
	SUPPORTCONFIDENCEINTERESTFACTOR,
	FAMILYHIERARCHY,
	ACTIVATIONTARGETBONDS,
	DEFAULT,
	RANDOM
}
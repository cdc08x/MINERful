package minerful.miner.stats.charsets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class CharactersSetCountersCollection  implements Cloneable {

	protected TreeSet<CharactersSetCounter> charactersSetCounterCollection;
	protected Map<Character, TreeSet<CharactersSetCounter>> singleCharIndexer;

	public CharactersSetCountersCollection() {
		super();
	}

	public CharactersSetCounter incrementAt(Set<Character> stuff) {
		return this.incrementAt(stuff, 1);
	}
	
	protected abstract void reIndex(Set<Character> stuff, CharactersSetCounter indexed);
	
	protected boolean addAndReIndex(Set<Character> charsInNuCharactersSetCounter, CharactersSetCounter nuCharactersSetCounter) {
		if (this.charactersSetCounterCollection.add(nuCharactersSetCounter)) {
			reIndex(charsInNuCharactersSetCounter, nuCharactersSetCounter);
			return true;
		}
		return false;
	}
	
	public TreeSet<CharactersSetCounter> getCharactersSetCounterCollection() {
		return this.charactersSetCounterCollection;
	}

	public CharactersSetCounter incrementAt(Character charInNuCharactersSetCounter, int sum) {
		Set<Character> charsInNuCharactersSetCounter = new TreeSet<Character>();
		charsInNuCharactersSetCounter.add(charInNuCharactersSetCounter);
		return this.incrementAt(charsInNuCharactersSetCounter, sum);
	}

	public CharactersSetCounter incrementAt(Set<Character> charsInNuCharactersSetCounter, int sum) {
		// This is the haystack
		CharactersSetCounter needle = new CharactersSetCounter(charsInNuCharactersSetCounter);
		int nuCounter = 0;
		
		if (this.charactersSetCounterCollection.contains(needle)) {
			nuCounter = this.charactersSetCounterCollection.floor(needle).incrementCounter(sum);
		} else {
			this.addAndReIndex(charsInNuCharactersSetCounter, needle);
			nuCounter = needle.incrementCounter(sum);
		}
		
		return needle;
	}

	public Character[] alphabet() {
		return this.singleCharIndexer.keySet().toArray(new Character[this.singleCharIndexer.keySet().size()]);
	}

	public SortedSet<CharactersSetCounter> getCharactersSetsOrderedByAscendingCounter() {
		SortedSet<CharactersSetCounter> nuCharSetCounter =
				new TreeSet<CharactersSetCounter>(
						new CharactersSetCounter.CharactersSetByAscendingCounterComparator()
				);
		
		nuCharSetCounter.addAll(this.charactersSetCounterCollection);
		
		return nuCharSetCounter;
	}

	@Override
	public String toString() {
		StringBuilder sBuil = new StringBuilder();
		sBuil.append('\n');
		sBuil.append(this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1));
		sBuil.append(": {");
		sBuil.append("\n\tList:");
		for (CharactersSetCounter chSCnt : this.charactersSetCounterCollection) {
			sBuil.append(chSCnt.toString().replace("\n", "\n\t\t"));
		}
		sBuil.append("\n\tIndexed:");
		for (Character chr : this.singleCharIndexer.keySet()) {
			sBuil.append("\n\t\tchr=");
			sBuil.append(chr);
			sBuil.append(" => {");
			sBuil.append(this.singleCharIndexer.get(chr).toString().replace("\n", "\n\t\t\t"));
			sBuil.append("\n\t\t}");
		}
		sBuil.append("\n}");
		return sBuil.toString();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

    public CharactersSetCounter get(Collection<Character> indexer) {
		SortedSet<CharactersSetCounter> tailSet = this.charactersSetCounterCollection.tailSet(new CharactersSetCounter(indexer));
		if (tailSet == null || tailSet.size() == 0)
			return null;
		return tailSet.first();
	}

    public CharactersSetCounter getNearest(Collection<Character> indexer) {
    	CharactersSetCounter nearest = this.get(indexer);
    	
    	if (nearest != null) {
    		return nearest;
    	} else {
    		Collection<Character> indexedCharsWithinIndexer = new ArrayList<Character>();
    		for (Character singleIndex : indexer) {
    			if (this.singleCharIndexer.containsKey(singleIndex))
    				indexedCharsWithinIndexer.add(singleIndex);
    		}
    		
    		return this.get(indexedCharsWithinIndexer);
    	}
	}

	public CharactersSetCounter get(Character indexer) {
		return this.charactersSetCounterCollection.tailSet(new CharactersSetCounter(indexer)).first();
	}

}
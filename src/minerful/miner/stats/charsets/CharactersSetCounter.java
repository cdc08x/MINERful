package minerful.miner.stats.charsets;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

public class CharactersSetCounter implements Comparable<CharactersSetCounter>, Cloneable {

	public static class CharactersSetByAscendingCounterComparator implements Comparator<CharactersSetCounter> {
		@Override
		public int compare(CharactersSetCounter o1, CharactersSetCounter o2) {
			int result = Integer.valueOf(o1.counter).compareTo(Integer.valueOf(o2.counter));
			return (
					( result == 0 )
					?	o1.compareTo(o2)
					:	result
			);
		}
	}

	private final String charactersSetString;
	private final TreeSet<Character> charactersSet;
	private int counter;
	
	public CharactersSetCounter(Character character) {
		this.charactersSetString = String.valueOf(character);
		this.charactersSet = new TreeSet<Character>();
		this.charactersSet.add(character);
		this.counter = 0;
	}

	public CharactersSetCounter(Collection<Character> charactersSet) {
		String charsImplosion = StringUtils.join(charactersSet, "");
		this.charactersSetString = charsImplosion;
		this.charactersSet = new TreeSet<Character>(charactersSet);
		this.counter = 0;
	}
	
	private CharactersSetCounter(Collection<Character> charactersSet, String charactersSetString, int counter) {
		this.counter = counter;
		this.charactersSet = new TreeSet<Character>(charactersSet);
		this.charactersSetString = StringUtils.join(charactersSet, "");
	}

	public CharactersSetCounter(Collection<Character> charactersSet, Character characterOnMore) {
		charactersSet.add(characterOnMore);
		String charsImplosion = StringUtils.join(charactersSet, "");
		this.charactersSetString = charsImplosion;
		this.charactersSet = new TreeSet<Character>(charactersSet);
		this.counter = 0;
	}

	public Set<Character> getCharactersSet() {
		return (Set<Character>)(this.charactersSet.clone());
	}
	
	public String getCharactersSetString() {
		return charactersSetString;
	}

	public int getCounter() {
		return counter;
	}
	
	public int incrementCounter() {
		return this.incrementCounter(1);
	}
	
	public int incrementCounter(int sum) {
		this.counter += sum;
		return counter;
	}
    
    public int howManyCharactersInSet() {
        return this.charactersSet.size();
    }
    public boolean isSingleton() {
        return howManyCharactersInSet() == 1;
    }

	@Override
	public int compareTo(CharactersSetCounter other) {
		return this.charactersSetString.compareTo(other.charactersSetString);
	}

	@Override
	public boolean equals(Object other) {
		return this.charactersSetString.equals(
				((CharactersSetCounter)other).getCharactersSetString()
			);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new CharactersSetCounter(charactersSet, this.charactersSetString, this.counter);
	}

	@Override
	public String toString() {
		StringBuilder sBuil = new StringBuilder();
		sBuil.append('\n');
		sBuil.append(this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1));
		sBuil.append(": {");
		sBuil.append("charactersSetString=");
		sBuil.append(this.charactersSetString);
		sBuil.append(" => ");
		sBuil.append("counter=");
		sBuil.append(this.counter);
		sBuil.append("}");		
		return sBuil.toString();
	}
}
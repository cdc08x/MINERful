package minerful.logmaker.errorinjector;

interface IErrorInjector {
	class TargetDataStructure {
		public final int stringNumber;
		public int index;
		
		public TargetDataStructure(int stringNumber, int index) {
			this.stringNumber = stringNumber;
			this.index = index;
		}
	}
	
	class TestBedCandidate {
		public final double candidateProportionalIndex;

		public TestBedCandidate(double candidateProportionalIndex) {
			this.candidateProportionalIndex = candidateProportionalIndex;
		}
		
	}
}
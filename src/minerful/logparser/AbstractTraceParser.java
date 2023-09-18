package minerful.logparser;

public abstract class AbstractTraceParser implements LogTraceParser {

	protected boolean parsing;
	protected SenseOfReading senseOfReading = SenseOfReading.ONWARDS;

	@Override
	public boolean isParsing() {
		return parsing;
	}

	@Override
	public SenseOfReading reverse() {
		this.senseOfReading = this.senseOfReading.switchSenseOfReading();
		return this.senseOfReading;
	}

	@Override
	public SenseOfReading getSenseOfReading() {
		return senseOfReading;
	}
}
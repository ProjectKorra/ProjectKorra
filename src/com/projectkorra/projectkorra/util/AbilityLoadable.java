package com.projectkorra.projectkorra.util;

public class AbilityLoadable implements Cloneable {

	private final String name;

	public AbilityLoadable(String name) {
		this.name = name;
	}

	@Override
	public AbilityLoadable clone() {
		try {
			return (AbilityLoadable) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public LoadResult init() {
		return new LoadResult();
	}

	public boolean isInternal() {
		return false;
	}

	public final String getName() {
		return name;
	}

	public static final class LoadResult {
		private final Result result;
		private final String reason;

		public LoadResult() {
			this(Result.SUCCESS, "");
		}

		public LoadResult(String failReason) {
			this(Result.FAILURE, failReason);
		}

		public LoadResult(Result result, String reason) {
			this.result = result;
			this.reason = reason;
		}

		public String getReason() {
			return reason;
		}

		public Result getResult() {
			return result;
		}

		public enum Result {
			FAILURE, SUCCESS
		}
	}
}

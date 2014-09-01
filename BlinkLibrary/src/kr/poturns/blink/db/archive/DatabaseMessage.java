package kr.poturns.blink.db.archive;

import kr.poturns.blink.internal.comm.BlinkMessage;


public class DatabaseMessage {
	public static final int OBTAIN_DATA_BY_CLASS = 0x00;
	public static final int OBTAIN_DATA_BY_ID = 0x01;
	
	private int Type;
	private String Condition;
	private String DateTimeFrom;
	private String DateTimeTo;
	private int ContainType;
	
	private DatabaseMessage(){
		Type = 0;
		Condition = null;
		DateTimeFrom = null;
		DateTimeTo = null;
		ContainType = 0;
	}
	
	public int getType() {
	    return Type;
    }

	public String getCondition() {
	    return Condition;
    }

	public String getDateTimeTo() {
	    return DateTimeTo;
    }

	public String getDateTimeFrom() {
	    return DateTimeFrom;
    }

	public int getContainType() {
	    return ContainType;
    }

	public static class Builder {

		private DatabaseMessage mDatabaseMessage;
		
		public Builder() {
			mDatabaseMessage = new DatabaseMessage();
		}
		
		public Builder setType(int type) {
			if (type == OBTAIN_DATA_BY_CLASS || type == OBTAIN_DATA_BY_ID )
				mDatabaseMessage.Type = type;
			return this;
		}
		
		public Builder setCondition(String Condition) {
			if (Condition != null)
				mDatabaseMessage.Condition = Condition;
			return this;
		}
		
		public Builder setDateTimeFrom(String DateTimeFrom) {
			if (DateTimeFrom != null)
				mDatabaseMessage.DateTimeFrom = DateTimeFrom;
			return this;
		}
		
		public Builder setDateTimeTo(String DateTimeTo) {
			if (DateTimeTo != null)
				mDatabaseMessage.DateTimeTo = DateTimeTo;
			return this;
		}
		
		public Builder setContainType(int ContainType) {
			if (0 <= ContainType && ContainType <= 2)
				mDatabaseMessage.ContainType = ContainType;
			return this;
		}
		
		public DatabaseMessage build() {
			return mDatabaseMessage;
		}
	}
}

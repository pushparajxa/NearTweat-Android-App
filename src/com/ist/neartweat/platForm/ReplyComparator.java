package com.ist.neartweat.platForm;

import java.util.Comparator;

public class ReplyComparator implements Comparator<Reply> {

	@Override
	public int compare(Reply r1, Reply r2) {
		if(r1.getTime()>r2.getTime())
			return 1;
		else if(r1.getTime()<r2.getTime())
			return -1;
		else
		return 0;
	}

}

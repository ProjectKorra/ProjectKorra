package com.projectkorra.projectkorra.util.attributes;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.util.Attribute.AttributeModifier;

public class AvatarStateModifier<TYPE> implements AttributeModifier<TYPE>{

	@Override
	public boolean canModify(BendingPlayer bPlayer) {
		return bPlayer.isAvatarState();
	}

	@Override
	public TYPE newValue(TYPE value) {
		return value;
	}

	@Override
	public int getPriority() {
		return 0;
	}

}

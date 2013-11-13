package cn.lucifer.sdop.callback;

import cn.lucifer.sdop.IGetLcf;
import cn.lucifer.sdop.Lcf;

public abstract class BaseCallback implements ICallback, IGetLcf {

	@Override
	public Lcf lcf() {
		return Lcf.getInstance();
	}

}

package cn.lucifer.sdop.dispatch;

import cn.lucifer.sdop.IGetLcf;
import cn.lucifer.sdop.Lcf;

public abstract class BaseDispatch implements IProcedure, IGetLcf {
	@Override
	public Lcf lcf() {
		return Lcf.getInstance();
	}
}

package cn.lucifer.sdop;

public abstract class LcfExtend implements IGetLcf{

	@Override
	public Lcf lcf() {
		return Lcf.getInstance();
	}
	
	protected void printStackTrace(){
		Throwable ex = new Throwable();
		ex.printStackTrace();
	}

}
